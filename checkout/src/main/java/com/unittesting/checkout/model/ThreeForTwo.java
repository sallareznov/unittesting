package com.unittesting.checkout.model;

import java.util.List;
import java.util.Optional;

/** "3 for the price of 2": for every 3 units of this product, one is free. */
public record ThreeForTwo(String code) implements Promotion {
    @Override
    public Optional<Discount> apply(List<Line> lines, int subtotal) {
        for (Line l : lines) {
            if (l.product().code().equals(code) && !l.product().byWeight()) {
                int free = (l.quantity() - 1) / 2;
                if (free > 0) {
                    return Optional.of(new Discount("3 pour 2 · " + l.product().name(), free * l.product().price()));
                }
            }
        }
        return Optional.empty();
    }
}
