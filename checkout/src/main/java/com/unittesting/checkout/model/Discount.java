package com.unittesting.checkout.model;

/** A reduction shown on the receipt (amount in cents, positive). */
public record Discount(String label, int amount) { }
