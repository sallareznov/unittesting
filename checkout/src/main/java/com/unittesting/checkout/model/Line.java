package com.unittesting.checkout.model;

/** One line of the receipt: a product and how many units (or how many grams). */
public final class Line {

    private final Product product;
    private int quantity;
    private final int grams;

    Line(Product product, int quantity, int grams) {
        this.product = product;
        this.quantity = quantity;
        this.grams = grams;
    }

    /** Price of the line, in cents. Weighed products: price per kilo × weight, rounded to the nearest cent. */
    public int amount() {
        if (product.byWeight()) {
            return product.price() * grams / 1000;
        }
        return product.price() * quantity;
    }

    void addOne() { quantity++; }
    void removeOne() { quantity--; }

    public Product product() { return product; }
    public int quantity() { return quantity; }
    public int grams() { return grams; }
}
