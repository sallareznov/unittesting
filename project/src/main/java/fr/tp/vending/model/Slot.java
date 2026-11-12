package fr.tp.vending.model;

/** One spiral of the machine, e.g. "B2", with its product and how many are left. */
public class Slot {

    private final String code;
    private final Product product;
    private int quantity;

    public Slot(String code, Product product, int quantity) {
        this.code = code;
        this.product = product;
        this.quantity = quantity;
    }

    public boolean isEmpty() { return quantity == 0; }

    void takeOne() {
        if (isEmpty()) {
            throw new IllegalStateException("Slot " + code + " is empty");
        }
        quantity--;
    }

    public String code() { return code; }
    public Product product() { return product; }
    public int quantity() { return quantity; }
}
