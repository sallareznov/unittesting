package fr.tp.vending.ui;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/** A clock that gives the real time, or a time set from the maintenance panel. */
class SimulatedClock extends Clock {

    private final ZoneId zone = ZoneId.systemDefault();
    private LocalTime simulated;   // null = real time

    void useRealTime() { simulated = null; }
    void simulate(LocalTime time) { simulated = time; }
    boolean isSimulated() { return simulated != null; }

    @Override
    public Instant instant() {
        return simulated == null ? Instant.now() : LocalDate.now(zone).atTime(simulated).atZone(zone).toInstant();
    }

    @Override public ZoneId getZone() { return zone; }
    @Override public Clock withZone(ZoneId z) { return this; }
}
