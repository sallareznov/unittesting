package fr.tp.parking.model;

/** Formats an amount in cents: 960 -> "9,60 €", 100 -> "1,00 €". */
public final class Money {
    private Money() { }

    public static String format(int cents) {
        return cents / 100 + "," + cents % 100 + " €";
    }
}
