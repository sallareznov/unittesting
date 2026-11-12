package com.unittesting.checkout.model;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One customer at the self-checkout: scanned items, promotions, coupons, loyalty points and payment.
 * Talks to the outside world only through Catalogue, Scale, PaymentTerminal, ReceiptPrinter and Clock.
 */
public class Checkout {

    /** Weight of the scale's tray, in grams. */
    public static final int TRAY_GRAMS = 20;

    private final Catalogue catalogue;
    private final List<Promotion> promotions;
    private final Scale scale;
    private final PaymentTerminal terminal;
    private final ReceiptPrinter printer;
    private final Clock clock;
    private final List<Line> lines = new ArrayList<>();
    private final List<Coupon> coupons = new ArrayList<>();
    private int loyaltyPoints = -1;   // -1: no loyalty card
    private boolean paid;

    public Checkout(Catalogue catalogue, List<Promotion> promotions, Scale scale,
                    PaymentTerminal terminal, ReceiptPrinter printer, Clock clock) {
        this.catalogue = catalogue;
        this.promotions = promotions;
        this.scale = scale;
        this.terminal = terminal;
        this.printer = printer;
        this.clock = clock;
    }

    /**
     * Scans a barcode. Units of the same product go on the same line.
     * A weighed product reads the scale (minus the tray) and always makes a new line.
     */
    public void scan(String code) {
        Product product = catalogue.find(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown product: " + code));
        if (product.byWeight()) {
            int net = scale.grams();
            if (net < 0) {
                throw new IllegalStateException("Nothing on the scale");
            }
            lines.add(new Line(product, 1, net));
            return;
        }
        if (!lines.isEmpty() && lines.get(lines.size() - 1).product().equals(product)) {
            lines.get(lines.size() - 1).addOne();
            return;
        }
        lines.add(new Line(product, 1, 0));
    }

    /**
     * Cancels one unit of a product (for a weighed product: its last weighing).
     * A line that falls to 0 disappears.
     */
    public void cancel(String code) {
        requireOpen();
        for (int i = lines.size() - 1; i >= 0; i--) {
            Line line = lines.get(i);
            if (line.product().code().equals(code)) {
                lines.remove(i);
                return;
            }
        }
    }

    /** Adds a coupon if it is valid today, the subtotal reaches its minimum, and it was not used yet. */
    public boolean applyCoupon(Coupon coupon) {
        requireOpen();
        LocalDate today = LocalDate.now(clock);
        boolean expired = !today.isBefore(coupon.expiry());
        boolean alreadyUsed = coupons.stream().anyMatch(c -> c == coupon);
        if (expired || alreadyUsed || subtotal() <= coupon.minimumPurchase()) {
            return false;
        }
        coupons.add(coupon);
        return true;
    }

    public void scanLoyaltyCard(int currentPoints) { loyaltyPoints = currentPoints; }

    public int subtotal() { return lines.stream().mapToInt(Line::amount).sum(); }

    /** Promotions first (in order), then coupons. */
    public List<Discount> discounts() {
        int subtotal = subtotal();
        List<Discount> result = new ArrayList<>();
        for (Promotion p : promotions) {
            p.apply(lines, subtotal - result.stream().mapToInt(Discount::amount).sum()).ifPresent(result::add);
        }
        for (Coupon c : coupons) {
            result.add(new Discount("Coupon " + c.code(), c.amount()));
        }
        return result;
    }

    /** Subtotal minus all discounts, never below 0. */
    public int total() {
        int total = subtotal() - discounts().stream().mapToInt(Discount::amount).sum();
        return total;
    }

    /** One point per started euro of the total (0,01 € to 1,00 € = 1 point). */
    public int pointsEarned() {
        return subtotal() / 100;
    }

    /**
     * Pays the total by card. If the terminal accepts: prints the receipt, credits the points,
     * and closes the checkout. If it refuses: nothing else happens.
     */
    public boolean pay() {
        requireOpen();
        int total = total();
        printer.print(new Receipt(List.copyOf(lines), discounts(), total, pointsEarned()));
        if (hasLoyaltyCard()) {
            loyaltyPoints += pointsEarned();
        }
        if (!terminal.charge(subtotal())) {
            return false;
        }
        int earned = pointsEarned();
        paid = true;
        return true;
    }

    private void requireOpen() {
        if (paid) {
            throw new IllegalStateException("Already paid");
        }
    }

    public boolean isPaid() { return paid; }
    public boolean hasLoyaltyCard() { return loyaltyPoints >= 0; }
    public int loyaltyPoints() { return loyaltyPoints; }
    public List<Line> lines() { return Collections.unmodifiableList(lines); }
    public List<Coupon> coupons() { return Collections.unmodifiableList(coupons); }
}
