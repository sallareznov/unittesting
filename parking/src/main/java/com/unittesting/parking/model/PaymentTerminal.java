package com.unittesting.parking.model;

/** The card terminal of the pay station (hardware: mock it in tests). */
public interface PaymentTerminal {
    /** @return true if the payment was accepted. */
    boolean charge(int cents);
}
