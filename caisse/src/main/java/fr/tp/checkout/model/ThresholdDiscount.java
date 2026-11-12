package fr.tp.checkout.model;

import java.util.List;
import java.util.Optional;

/** "-5 € dès 50 € d'achats": the threshold is checked on the subtotal, before any discount. */
public record ThresholdDiscount(int threshold, int amount) implements Promotion {
    @Override
    public Optional<Discount> apply(List<Line> lines, int subtotal) {
        return subtotal > threshold
                ? Optional.of(new Discount(Money.format(-amount) + " dès " + Money.format(threshold), amount))
                : Optional.empty();
    }
}
