package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single book reservation made by a member.
 *
 * This is an in-memory record only — it is not persisted to disk or a
 * database, so all reservations are lost when the application restarts.
 */
public class Reservation {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final String bookTitle;
    private final String author;
    private final String memberName;
    private final LocalDateTime reservedAt;

    public Reservation(String bookTitle, String author, String memberName) {
        this.bookTitle = bookTitle;
        this.author = author;
        this.memberName = memberName;
        this.reservedAt = LocalDateTime.now();
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public String getMemberName() {
        return memberName;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public String getReservedAtDisplay() {
        return reservedAt.format(DISPLAY_FORMAT);
    }

    @Override
    public String toString() {
        return bookTitle + " by " + author + " — reserved by " + memberName
                + " on " + getReservedAtDisplay();
    }
}
