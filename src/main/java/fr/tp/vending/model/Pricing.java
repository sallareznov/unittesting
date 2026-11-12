package fr.tp.vending.model;

import java.time.Clock;
import java.time.LocalTime;

/**
 * The price actually paid. Happy hour, from 14:00 to 15:59: 20 % off,
 * rounded to the nearest 5 cents (an exact half rounds up).
 */
public class Pricing {

    private static final LocalTime START = LocalTime.of(14, 0);
    private static final LocalTime END = LocalTime.of(16, 0);

    private final Clock clock;

    public Pricing(Clock clock) {
        this.clock = clock;
    }

    public boolean isHappyHour() {
        LocalTime now = LocalTime.now(clock);
        return now.isAfter(START) && now.isBefore(END);
    }

    public int priceOf(Product product) {
        if (!isHappyHour()) {
            return product.price();
        }
        int discounted = product.price() * 80 / 100;
        return discounted / 5 * 5;
    }
}
