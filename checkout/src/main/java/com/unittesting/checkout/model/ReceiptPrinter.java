package com.unittesting.checkout.model;

/** The receipt printer (hardware: mock it in tests). */
public interface ReceiptPrinter {
    void print(Receipt receipt);
}
