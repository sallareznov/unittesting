package com.unittesting.checkout.model;

import java.util.List;
import java.util.Optional;

/** A promotion looks at the basket and may give a discount. */
public interface Promotion {
    /** @param subtotal the basket before any discount */
    Optional<Discount> apply(List<Line> lines, int subtotal);
}
