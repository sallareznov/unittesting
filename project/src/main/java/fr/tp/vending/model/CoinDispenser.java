package fr.tp.vending.model;

/** Drops coins into the change tray (hardware: mock it in tests). */
public interface CoinDispenser {
    void dispense(Coin coin, int count);
}
