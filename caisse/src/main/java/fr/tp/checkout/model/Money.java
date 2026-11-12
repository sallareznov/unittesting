package fr.tp.checkout.model;

/** Formats an amount in cents: 130 -> "1,30 €", -180 -> "-1,80 €". */
public final class Money {
    private Money() { }

    public static String format(int cents) {
        return String.format("%d,%02d €", cents / 100, cents % 100);
    }
}
