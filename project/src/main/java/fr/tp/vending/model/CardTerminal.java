package fr.tp.vending.model;

/** The bank card reader (hardware: mock it in tests). */
public interface CardTerminal {
    /** @return true if the payment was accepted. */
    boolean charge(int cents);
}
