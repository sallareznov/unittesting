package fr.tp.vending.model;

/** Turns a spiral to drop a product (hardware: mock it in tests). */
public interface ProductDispenser {
    void release(Slot slot);
}
