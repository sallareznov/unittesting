package fr.tp.vending.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Finds which coins to give back: the exact amount, with as few coins as possible,
 * using only the coins available.
 */
public class ChangeCalculator {

    /** @return the coins to give (an empty map for 0), or empty if the amount cannot be made. */
    public Optional<Map<Coin, Integer>> compute(int amount, Map<Coin, Integer> available) {
        if (amount < 0) {
            throw new IllegalArgumentException("Negative amount: " + amount);
        }
        Coin[] coins = Coin.values();
        Map<Coin, Integer> change = new EnumMap<>(Coin.class);
        int left = amount;
        // largest coin first, as many as possible
        for (int i = 0; i < coins.length - 1; i++) {
            Coin coin = coins[i];
            int count = Math.min(left / coin.cents(), available.getOrDefault(coin, 0));
            if (count > 0) {
                change.put(coin, count);
                left -= count * coin.cents();
            }
        }
        return left == 0 ? Optional.of(change) : Optional.empty();
    }
}
