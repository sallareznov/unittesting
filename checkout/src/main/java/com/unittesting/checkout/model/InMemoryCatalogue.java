package com.unittesting.checkout.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** A catalogue held in memory (used by the window; tests may use it or a mock). */
public class InMemoryCatalogue implements Catalogue {

    private final Map<String, Product> products = new LinkedHashMap<>();

    public InMemoryCatalogue(List<Product> products) {
        products.forEach(p -> this.products.put(p.code(), p));
    }

    @Override
    public Optional<Product> find(String code) {
        return Optional.ofNullable(products.get(code));
    }

    public List<Product> all() { return List.copyOf(products.values()); }
}
