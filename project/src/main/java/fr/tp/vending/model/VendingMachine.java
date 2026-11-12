package fr.tp.vending.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The machine's logic. It knows nothing about JavaFX: it talks to the hardware through
 * Display, CoinDispenser, ProductDispenser and CardTerminal, which tests replace with mocks.
 */
public class VendingMachine {

    public static final int MAX_CREDIT = 500;

    private final Map<String, Slot> slots = new LinkedHashMap<>();
    private final Map<Coin, Integer> coins = new EnumMap<>(Coin.class);
    private final Map<String, Integer> sales = new HashMap<>();
    private final ChangeCalculator calculator;
    private final Pricing pricing;
    private final Display display;
    private final CoinDispenser coinDispenser;
    private final ProductDispenser productDispenser;
    private final CardTerminal cardTerminal;
    private int credit;
    private int revenue;

    public VendingMachine(List<Slot> slots, Map<Coin, Integer> initialCoins, ChangeCalculator calculator,
                          Pricing pricing, Display display, CoinDispenser coinDispenser,
                          ProductDispenser productDispenser, CardTerminal cardTerminal) {
        slots.forEach(s -> this.slots.put(s.code(), s));
        this.coins.putAll(initialCoins);
        this.calculator = calculator;
        this.pricing = pricing;
        this.display = display;
        this.coinDispenser = coinDispenser;
        this.productDispenser = productDispenser;
        this.cardTerminal = cardTerminal;
        showWelcome();
    }

    /** Accepts a coin, unless the credit would go over MAX_CREDIT: then the coin is given back. */
    public void insert(Coin coin) {
        if (credit + coin.cents() >= MAX_CREDIT) {
            coinDispenser.dispense(coin, 1);
            display.show("Crédit maximum", Money.format(MAX_CREDIT));
            return;
        }
        coins.merge(coin, 1, Integer::sum);
        credit += coin.cents();
        display.show("Crédit", Money.format(credit));
    }

    /** Buys with the coins inserted. Codes are not case-sensitive ("b2" is "B2"). */
    public void select(String code) {
        Slot slot = findAvailable(code);
        if (slot == null) {
            return;
        }
        int price = pricing.priceOf(slot.product());
        if (credit <= price) {
            display.show(slot.product().name(), "Prix " + Money.format(price));
            return;
        }
        Optional<Map<Coin, Integer>> change = calculator.compute(credit - price, coins);
        if (change.isEmpty()) {
            display.show("Pas de monnaie", "Faites l'appoint");
            return;
        }
        slot.takeOne();
        productDispenser.release(slot);
        giveBack(change.get());
        display.show("Rendu " + Money.format(price), "Bonne dégustation !");
        recordSale(slot, credit);
        credit = 0;
    }

    /** Buys by card. Only when no coins have been inserted. */
    public void payByCard(String code) {
        if (credit > 0) {
            display.show("Pièces insérées", "Choisissez avec le crédit");
            return;
        }
        Slot slot = findAvailable(code);
        if (slot == null) {
            return;
        }
        int price = pricing.priceOf(slot.product());
        slot.takeOne();
        productDispenser.release(slot);
        if (!cardTerminal.charge(slot.product().price())) {
            display.show("Carte refusée", "Autre moyen ?");
            return;
        }
        display.show("Paiement accepté", "Bonne dégustation !");
        recordSale(slot, price);
    }

    /** Gives back the whole credit. If the change cannot be made, the credit is kept. */
    public void cancel() {
        calculator.compute(credit, coins).ifPresent(this::giveBack);
        credit = 0;
        showWelcome();
    }

    public void restock(String code, int count) {
        Slot slot = slots.get(normalise(code));
        if (slot == null) {
            throw new IllegalArgumentException("Unknown code: " + code);
        }
        slot.restock(count);
    }

    private Slot findAvailable(String code) {
        Slot slot = slots.get(normalise(code));
        if (slot == null) {
            display.show("Code inconnu", String.valueOf(code));
            return null;
        }
        if (slot.isEmpty()) {
            display.show(slot.code() + " épuisé", "Autre choix ?");
            return null;
        }
        return slot;
    }

    private static String normalise(String code) {
        return code;
    }

    private void recordSale(Slot slot, int price) {
        revenue += price;
        sales.merge(slot.code(), 1, Integer::sum);
    }

    private void giveBack(Map<Coin, Integer> change) {
        change.forEach((coin, count) -> {
            if (count > 0) {
                coins.merge(coin, count, Integer::sum);
                coinDispenser.dispense(coin, count);
            }
        });
    }

    private void showWelcome() {
        display.show("Bonjour !", "Insérez des pièces");
    }

    public int credit() { return credit; }
    public int revenue() { return revenue; }
    public int salesOf(String code) { return sales.getOrDefault(normalise(code), 0); }
    public Map<String, Slot> slots() { return Collections.unmodifiableMap(slots); }
    public Map<Coin, Integer> coins() { return Collections.unmodifiableMap(coins); }
}
