package com.unittesting.parking.ui;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** A clock the window can move forward, to see fares change without waiting. */
class SettableClock extends Clock {

    private final ZoneId zone = ZoneId.systemDefault();
    private Instant now;

    SettableClock(LocalDateTime start) { set(start); }

    void set(LocalDateTime time) { now = time.atZone(zone).toInstant(); }
    void advance(Duration d) { now = now.plus(d); }

    @Override public Instant instant() { return now; }
    @Override public ZoneId getZone() { return zone; }
    @Override public Clock withZone(ZoneId z) { return this; }
}
