package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Reservation;

/**
 * In-memory reservation manager.
 *
 * Holds all reservations made during the current application session.
 * Nothing here is written to disk or a database, so the list resets every
 * time the application restarts. Swap this out for a database-backed
 * implementation later without changing any UI code, as long as the
 * method signatures stay the same.
 */
public class ReservationService {

    private static final ReservationService INSTANCE = new ReservationService();

    private final List<Reservation> reservations = new ArrayList<>();

    private ReservationService() {
    }

    public static ReservationService getInstance() {
        return INSTANCE;
    }

    /**
     * Returns true if the given member already has an active reservation
     * for the given book title, so we don't let them reserve it twice.
     */
    public boolean isAlreadyReserved(String bookTitle, String memberName) {
        for (Reservation r : reservations) {
            if (r.getBookTitle().equalsIgnoreCase(bookTitle)
                    && r.getMemberName().equalsIgnoreCase(memberName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Records a new reservation and returns it.
     */
    public Reservation reserve(String bookTitle, String author, String memberName) {
        Reservation reservation = new Reservation(bookTitle, author, memberName);
        reservations.add(reservation);
        return reservation;
    }

    /**
     * Removes a reservation for the given book/member, if one exists.
     * Returns true if a reservation was found and removed.
     */
    public boolean cancelReservation(String bookTitle, String memberName) {
        return reservations.removeIf(r ->
                r.getBookTitle().equalsIgnoreCase(bookTitle)
                        && r.getMemberName().equalsIgnoreCase(memberName));
    }

    /**
     * Returns an unmodifiable snapshot of all current reservations.
     */
    public List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(new ArrayList<>(reservations));
    }
}
