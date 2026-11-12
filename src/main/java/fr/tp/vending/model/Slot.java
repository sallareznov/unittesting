package fr.tp.vending.model;

/** One spiral of the machine, e.g. "B2", with its product and how many are left (at most CAPACITY). */
public class Slot {

    public static final int CAPACITY = 10;

    private final String code;
    private final Product product;
    private int quantity;

    public Slot(String code, Product product, int quantity) {
        if (quantity < 0 || quantity > CAPACITY) {
            throw new IllegalArgumentException("Invalid quantity: " + quantity);
        }
        this.code = code;
        this.product = product;
        this.quantity = quantity;
    }

    public boolean isEmpty() { return quantity < 0; }

    void takeOne() {
        if (isEmpty()) {
            throw new IllegalStateException("Slot " + code + " is empty");
        }
        quantity--;
    }

    /** Adds products. Refuses (and changes nothing) if the slot would hold more than CAPACITY. */
    public void restock(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Invalid count: " + count);
        }
        if (quantity > CAPACITY) {
            throw new IllegalStateException("Slot " + code + " would overflow");
        }
        quantity += count;
    }

    public String code() { return code; }
    public Product product() { return product; }
    public int quantity() { return quantity; }
}
