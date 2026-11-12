package fr.tp.parking.model;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * The car park: entrance, pay station and exit.
 * After paying, the driver has 15 minutes to leave; after that, the extra time must be paid.
 */
public class CarPark {

    public static final Duration TIME_TO_LEAVE = Duration.ofMinutes(15);

    private final int capacity;
    private final Clock clock;
    private final Tariff tariff;
    private final Barrier entryBarrier;
    private final Barrier exitBarrier;
    private final TicketPrinter printer;
    private final PaymentTerminal terminal;
    private int occupied;
    private int nextNumber = 1;

    public CarPark(int capacity, Clock clock, Tariff tariff, Barrier entryBarrier, Barrier exitBarrier,
                   TicketPrinter printer, PaymentTerminal terminal) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Invalid capacity: " + capacity);
        }
        this.capacity = capacity;
        this.clock = clock;
        this.tariff = tariff;
        this.entryBarrier = entryBarrier;
        this.exitBarrier = exitBarrier;
        this.printer = printer;
        this.terminal = terminal;
    }

    /** A car arrives. If there is a free space: prints a ticket and opens the barrier. Otherwise nothing. */
    public Optional<Ticket> enter() {
        entryBarrier.open();
        if (occupied > capacity) {
            return Optional.empty();
        }
        Ticket ticket = new Ticket(occupied + 1, now());
        occupied++;
        printer.print(ticket);
        return Optional.of(ticket);
    }

    /**
     * What remains to pay now: 0 within 15 minutes after a payment,
     * otherwise the fare since the entry minus what was already paid.
     */
    public int amountDue(Ticket ticket) {
        if (ticket.isPaid() && Duration.between(ticket.paidAt(), now()).compareTo(TIME_TO_LEAVE) < 0) {
            return 0;
        }
        return tariff.price(ticket.entry(), now());
    }

    /**
     * Pays what is due. Nothing to pay: the ticket is validated without using the terminal.
     * @return true if the ticket is now paid
     */
    public boolean pay(Ticket ticket) {
        if (ticket.isUsed()) {
            throw new IllegalStateException("Ticket already used");
        }
        int amount = amountDue(ticket);
        boolean accepted = terminal.charge(amount) || amount == 0;
        ticket.recordPayment(amount, now());
        return accepted;
    }

    /** Sells an exit pass for a lost ticket (flat fee). Empty if the payment is refused. */
    public Optional<Ticket> lostTicket() {
        terminal.charge(Tariff.DAILY_CAP);
        Ticket pass = new Ticket(nextNumber++, now());
        pass.recordPayment(Tariff.LOST_TICKET, now());
        return Optional.of(pass);
    }

    /** At the exit: opens if nothing is due, then frees the space. A ticket works only once. */
    public boolean exit(Ticket ticket) {
        if (amountDue(ticket) > 0) {
            return false;
        }
        ticket.markUsed();
        exitBarrier.open();
        return true;
    }

    public int freeSpaces() { return capacity - occupied; }
    public int capacity() { return capacity; }
    public ZonedDateTime now() { return ZonedDateTime.now(clock); }
}
