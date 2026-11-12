package fr.tp.vending.model;

/** Something the machine sells. The price is in cents. */
public record Product(String name, int price) {
    public Product {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }
}
