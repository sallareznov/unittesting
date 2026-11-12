package fr.tp.vending.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The machine's logic. It knows nothing about JavaFX: it talks to the hardware through
 * Display, CoinDispenser and ProductDispenser, which tests replace with mocks.
 */
public class VendingMachine {

    private final Map<String, Slot> slots = new LinkedHashMap<>();
    private final Map<Coin, Integer> coins = new EnumMap<>(Coin.class);
    private final ChangeCalculator calculator;
    private final Display display;
    private final CoinDispenser coinDispenser;
    private final ProductDispenser productDispenser;
    private int credit;

    public VendingMachine(List<Slot> slots, Map<Coin, Integer> initialCoins, ChangeCalculator calculator,
                          Display display, CoinDispenser coinDispenser, ProductDispenser productDispenser) {
        slots.forEach(s -> this.slots.put(s.code(), s));
        this.coins.putAll(initialCoins);
        this.calculator = calculator;
        this.display = display;
        this.coinDispenser = coinDispenser;
        this.productDispenser = productDispenser;
        showWelcome();
    }

    public void insert(Coin coin) {
        coins.merge(coin, 1, Integer::sum);
        credit += coin.cents();
        display.show("Crédit", Money.format(credit));
    }

    public void select(String code) {
        Slot slot = slots.get(code);
        if (slot == null) {
            display.show("Code inconnu", code);
            return;
        }
        if (slot.isEmpty()) {
            display.show(code + " épuisé", "Autre choix ?");
            return;
        }
        int price = slot.product().price();
        if (credit < price) {
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
        display.show("Rendu " + Money.format(credit - price), "Bonne dégustation !");
        credit = 0;
    }

    /** Gives back the whole credit. */
    public void cancel() {
        calculator.compute(credit, coins).ifPresent(this::giveBack);
        credit = 0;
        showWelcome();
    }

    private void giveBack(Map<Coin, Integer> change) {
        change.forEach((coin, count) -> {
            coins.merge(coin, -count, Integer::sum);
            coinDispenser.dispense(coin, count);
        });
    }

    private void showWelcome() {
        display.show("Bonjour !", "Insérez des pièces");
    }

    public int credit() { return credit; }
    public Map<String, Slot> slots() { return Collections.unmodifiableMap(slots); }
    public Map<Coin, Integer> coins() { return Collections.unmodifiableMap(coins); }
}
