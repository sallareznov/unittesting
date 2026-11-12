package fr.tp.checkout.model;

import java.util.Optional;

/** Finds products by barcode (in real life, a server: mock it in tests). */
public interface Catalogue {
    Optional<Product> find(String code);
}
