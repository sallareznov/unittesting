package com.unittesting.checkout.model;

import java.util.List;
import java.util.Optional;

/**
 * "Second one half price": for every 2 units of this product, the second one is half price
 * (half a cent is rounded up).
 */
public record SecondHalfPrice(String code) implements Promotion {
    @Override
    public Optional<Discount> apply(List<Line> lines, int subtotal) {
        for (Line l : lines) {
            if (l.product().code().equals(code) && !l.product().byWeight()) {
                int pairs = (l.quantity() + 1) / 2;
                if (pairs > 0) {
                    int half = l.product().price() / 2;
                    return Optional.of(new Discount("2e à -50 % · " + l.product().name(), pairs * half));
                }
            }
        }
        return Optional.empty();
    }
}
