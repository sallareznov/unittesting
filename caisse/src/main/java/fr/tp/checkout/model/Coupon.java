package fr.tp.checkout.model;

import java.time.LocalDate;

/** A discount voucher: valid until its expiry date included, from a minimum purchase. */
public record Coupon(String code, int amount, int minimumPurchase, LocalDate expiry) { }
