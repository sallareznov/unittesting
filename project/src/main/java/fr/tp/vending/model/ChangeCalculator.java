package fr.tp.vending.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Finds which coins to give back, using as few coins as possible and only the coins available.
 * Greedy: take the largest coin that still fits, as many times as possible.
 */
public class ChangeCalculator {

    /** @return the coins to give, or empty if the exact amount cannot be made. */
    public Optional<Map<Coin, Integer>> compute(int amount, Map<Coin, Integer> available) {
        if (amount < 0) {
            throw new IllegalArgumentException("Negative amount: " + amount);
        }
        Map<Coin, Integer> change = new EnumMap<>(Coin.class);
        int left = amount;
        for (Coin coin : Coin.values()) {
            int count = Math.min(left / coin.cents(), available.getOrDefault(coin, 0));
            if (count > 0) {
                change.put(coin, count);
                left -= count * coin.cents();
            }
        }
        return left == 0 ? Optional.of(change) : Optional.empty();
    }
}
