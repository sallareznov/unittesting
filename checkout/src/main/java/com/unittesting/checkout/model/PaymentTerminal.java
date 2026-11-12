package com.unittesting.checkout.model;

/** The bank card terminal (hardware: mock it in tests). */
public interface PaymentTerminal {
    /** @return true if the payment was accepted. */
    boolean charge(int cents);
}
