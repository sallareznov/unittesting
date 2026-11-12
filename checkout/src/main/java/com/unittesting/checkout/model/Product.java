package com.unittesting.checkout.model;

/**
 * An item of the shop. The price is in cents, per unit, or per kilo when byWeight is true.
 */
public record Product(String code, String name, int price, boolean byWeight) {
    public Product {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("A product needs a code and a name");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Invalid price: " + price);
        }
    }
}
