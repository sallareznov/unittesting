package fr.tp.checkout.model;

import java.util.List;

/** What is printed after a successful payment. */
public record Receipt(List<Line> lines, List<Discount> discounts, int total, int pointsEarned) { }
