package fr.tp.vending.model;

/** Formats an amount in cents, e.g. 130 -> "1,30 €" and -5 -> "-0,05 €". */
public final class Money {
    private Money() { }

    public static String format(int cents) {
        return String.format("%d,%02d €", cents / 100, cents % 100);
    }
}
