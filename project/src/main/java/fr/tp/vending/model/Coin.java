package fr.tp.vending.model;

/** The coins the machine accepts, from the largest to the smallest. Values are in cents. */
public enum Coin {
    TWO_EUROS(200), ONE_EURO(100), FIFTY_CENTS(50), TWENTY_CENTS(20), TEN_CENTS(10), FIVE_CENTS(5);

    private final int cents;

    Coin(int cents) { this.cents = cents; }

    public int cents() { return cents; }
}
