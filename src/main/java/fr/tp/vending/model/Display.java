package fr.tp.vending.model;

/** The machine's screen (hardware: mock it in tests). */
public interface Display {
    void show(String line1, String line2);
}
