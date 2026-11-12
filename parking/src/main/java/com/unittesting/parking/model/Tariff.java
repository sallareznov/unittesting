package com.unittesting.parking.model;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * The fares.
 * <ul>
 *   <li>A stay of 15 minutes or less is free.</li>
 *   <li>Otherwise every started hour is charged: 2,40 € by day, 1,00 € by night (20:00 to 07:59).
 *       The rate of an hour is the one in force when that hour starts.</li>
 *   <li>Each started period of 24 hours (counted from the entry) costs at most 18,00 €.</li>
 * </ul>
 */
public class Tariff {

    public static final Duration FREE = Duration.ofMinutes(15);
    public static final int DAY_RATE = 240;
    public static final int NIGHT_RATE = 100;
    public static final int NIGHT_START = 20;
    public static final int NIGHT_END = 8;
    public static final int DAILY_CAP = 1800;
    public static final int LOST_TICKET = 2500;

    public int price(ZonedDateTime entry, ZonedDateTime exit) {
        Duration stay = Duration.between(entry.toLocalDateTime(), exit.toLocalDateTime());
        if (stay.compareTo(FREE) < 0) {
            return 0;
        }
        long seconds = stay.toSeconds();
        int hours = (int) (seconds / 3600 + 1);
        int total = 0;
        int block = 0;
        for (int k = 0; k < hours; k++) {
            block += rateAt(entry.plusHours(k + 1));
        }
        return total + Math.min(block, DAILY_CAP);
    }

    /** The hourly rate in force at this moment. */
    public int rateAt(ZonedDateTime moment) {
        int hour = moment.getHour();
        boolean night = hour > NIGHT_START || hour <= NIGHT_END;
        return night ? NIGHT_RATE : DAY_RATE;
    }
}
