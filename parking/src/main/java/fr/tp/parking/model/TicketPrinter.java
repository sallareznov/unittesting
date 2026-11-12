package fr.tp.parking.model;

/** The ticket printer at the entrance (hardware: mock it in tests). */
public interface TicketPrinter {
    void print(Ticket ticket);
}
