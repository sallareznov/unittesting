package fr.tp.vending.model;

/** Something the machine sells. The price is in cents, positive and a multiple of 5. */
public record Product(String name, int price) {
    public Product {
        if (name == null) {
            throw new IllegalArgumentException("A product needs a name");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Invalid price: " + price);
        }
    }
}
