package com.unittesting.parking.model;

import java.time.ZonedDateTime;

/** The ticket printed at the entrance (or the exit pass sold for a lost ticket). */
public final class Ticket {

    private final int number;
    private final ZonedDateTime entry;
    private int amountPaid;
    private ZonedDateTime paidAt;
    private boolean used;

    Ticket(int number, ZonedDateTime entry) {
        this.number = number;
        this.entry = entry;
    }

    void recordPayment(int amount, ZonedDateTime when) {
        amountPaid += amount;
        paidAt = when;
    }

    void markUsed() { used = true; }

    public int number() { return number; }
    public ZonedDateTime entry() { return entry; }
    public int amountPaid() { return amountPaid; }
    public ZonedDateTime paidAt() { return paidAt; }
    public boolean isPaid() { return paidAt != null; }
    public boolean isUsed() { return used; }
}
