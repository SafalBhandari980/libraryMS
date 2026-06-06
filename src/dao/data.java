/*
 * data.java  (dao package)
 * ─────────────────────────────────────────────────────────────────
 * Data Access Object — every SQL query in LibraryMS lives here.
 * Only this class calls db.java.
 *
 * Pattern:
 *   View → Controller → DAO(data) → db → MySqlConnector → MySQL
 * ─────────────────────────────────────────────────────────────────
 */
package dao;

import database.db;
import java.awt.print.Book;
import model.java.Borrowing;
import model.java.Member;
import model.java.Review;
import model.java.WishlistItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class data {

    // ══════════════════════════════════════════════════════════════
    //  MEMBER
    // ══════════════════════════════════════════════════════════════

    /**
     * Registers a new member. Returns generated member_id or -1 on failure.
     * membershipType should be "student", "faculty", or "public".
     */
    public static int registerMember(String firstName, String lastName,
                                     String email, String passwordHash,
                                     String phoneNumber, String membershipType) {
        String sql =
            "INSERT INTO members " +
            "(first_name, last_name, email, password_hash, phone_number, " +
            " membership_type, role, join_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, 'member', CURDATE())";
        try {
            return db.executeInsertGetKey(sql,
                firstName, lastName, email, passwordHash,
                phoneNumber, membershipType);
        } catch (SQLException e) {
            System.err.println("registerMember error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Login — returns matching Member or null if credentials are wrong.
     */
    public static Member loginMember(String email, String passwordHash) {
        String sql = "SELECT * FROM members WHERE email = ? AND password_hash = ?";
        try (ResultSet rs = db.executeQuery(sql, email, passwordHash)) {
            if (rs.next()) return mapMember(rs);
        } catch (SQLException e) {
            System.err.println("loginMember error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a member by email only (used for password-reset OTP flow).
     * Returns null if no account exists with that email.
     */
    public static Member getMemberByEmail(String email) {
        String sql = "SELECT * FROM members WHERE email = ?";
        try (ResultSet rs = db.executeQuery(sql, email)) {
            if (rs.next()) return mapMember(rs);
        } catch (SQLException e) {
            System.err.println("getMemberByEmail error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Saves a new password hash after OTP verification.
     * Returns true on success.
     */
    public static boolean updatePassword(String email, String newPasswordHash) {
        String sql = "UPDATE members SET password_hash = ? WHERE email = ?";
        try {
            return db.executeUpdate(sql, newPasswordHash, email) > 0;
        } catch (SQLException e) {
            System.err.println("updatePassword error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns true if an email is already registered (used in signup validation).
     */
    public static boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) AS cnt FROM members WHERE email = ?";
        try (ResultSet rs = db.executeQuery(sql, email)) {
            if (rs.next()) return rs.getInt("cnt") > 0;
        } catch (SQLException e) {
            System.err.println("emailExists error: " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    //  BOOKS
    // ══════════════════════════════════════════════════════════════

    /** Returns every book ordered by title. */
    public static List<Book> getAllBooks() {
        return fetchBooks("SELECT * FROM books ORDER BY title");
    }

    /**
     * Full-text search by title, author, or ISBN.
     * Case-insensitive; partial matches supported.
     */
    public static List<Book> searchBooks(String keyword) {
        String like = "%" + keyword + "%";
        String sql  =
            "SELECT * FROM books " +
            "WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? " +
            "ORDER BY title";
        return fetchBooks(sql, like, like, like);
    }

    /** Returns one book by ID, or null if not found. */
    public static Book getBookById(int bookId) {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (ResultSet rs = db.executeQuery(sql, bookId)) {
            if (rs.next()) return mapBook(rs);
        } catch (SQLException e) {
            System.err.println("getBookById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns up to 4 books in the same category (Similar Books sidebar).
     * Excludes the book currently being viewed.
     */
    public static List<Book> getSimilarBooks(String category, int excludeBookId) {
        String sql =
            "SELECT * FROM books " +
            "WHERE category = ? AND book_id != ? " +
            "ORDER BY avg_rating DESC LIMIT 4";
        return fetchBooks(sql, category, excludeBookId);
    }

    // ══════════════════════════════════════════════════════════════
    //  BORROWING
    // ══════════════════════════════════════════════════════════════

    /**
     * Creates a borrowing record (14-day loan) and decrements available_copies.
     * Returns the new borrow_id, or -1 on failure.
     */
    public static int borrowBook(int memberId, int bookId) {
        String insertSql =
            "INSERT INTO borrowings " +
            "(member_id, book_id, borrow_date, due_date, status, fine_amount) " +
            "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'active', 0.00)";
        String decrementSql =
            "UPDATE books SET available_copies = available_copies - 1 " +
            "WHERE book_id = ? AND available_copies > 0";
        try {
            int id = db.executeInsertGetKey(insertSql, memberId, bookId);
            if (id > 0) db.executeUpdate(decrementSql, bookId);
            return id;
        } catch (SQLException e) {
            System.err.println("borrowBook error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Marks a borrowing returned and restores available_copies.
     * Returns true on success.
     */
    public static boolean returnBook(int borrowId, int bookId) {
        String updateBorrow =
            "UPDATE borrowings SET return_date = CURDATE(), status = 'returned' " +
            "WHERE borrow_id = ?";
        String updateBook =
            "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?";
        try {
            db.executeUpdate(updateBorrow, borrowId);
            db.executeUpdate(updateBook, bookId);
            return true;
        } catch (SQLException e) {
            System.err.println("returnBook error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns all active/overdue borrowings for one member,
     * soonest due date first (for My Borrowings screen).
     */
    public static List<Borrowing> getActiveBorrowings(int memberId) {
        String sql =
            "SELECT * FROM borrowings " +
            "WHERE member_id = ? AND status IN ('active','overdue') " +
            "ORDER BY due_date ASC";
        return fetchBorrowings(sql, memberId);
    }

    /** Returns the full borrowing history for a member. */
    public static List<Borrowing> getBorrowingHistory(int memberId) {
        String sql =
            "SELECT * FROM borrowings WHERE member_id = ? " +
            "ORDER BY borrow_date DESC";
        return fetchBorrowings(sql, memberId);
    }

    // ══════════════════════════════════════════════════════════════
    //  REVIEWS
    // ══════════════════════════════════════════════════════════════

    /**
     * Returns all reviews for a book (newest first).
     * Joins member name so the view can display "Firstname L."
     */
    public static List<Review> getReviewsForBook(int bookId) {
        String sql =
            "SELECT r.*, CONCAT(m.first_name, ' ', LEFT(m.last_name,1), '.') AS member_name " +
            "FROM reviews r " +
            "JOIN members m ON r.member_id = m.member_id " +
            "WHERE r.book_id = ? " +
            "ORDER BY r.review_date DESC";
        List<Review> list = new ArrayList<>();
        try (ResultSet rs = db.executeQuery(sql, bookId)) {
            while (rs.next()) {
                Review r = mapReview(rs);
                r.setMemberName(rs.getString("member_name"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("getReviewsForBook error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Inserts a review and updates the book's avg_rating + total_reviews.
     * Returns new review_id, or -1 on failure.
     */
    public static int addReview(int memberId, int bookId, int rating, String comment) {
        String insertSql =
            "INSERT INTO reviews (member_id, book_id, rating, comment, review_date) " +
            "VALUES (?, ?, ?, ?, CURDATE())";
        String updateSql =
            "UPDATE books SET " +
            "avg_rating = (SELECT AVG(rating) FROM reviews WHERE book_id = ?), " +
            "total_reviews = (SELECT COUNT(*) FROM reviews WHERE book_id = ?) " +
            "WHERE book_id = ?";
        try {
            int id = db.executeInsertGetKey(insertSql, memberId, bookId, rating, comment);
            if (id > 0) db.executeUpdate(updateSql, bookId, bookId, bookId);
            return id;
        } catch (SQLException e) {
            System.err.println("addReview error: " + e.getMessage());
            return -1;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  WISHLIST
    // ══════════════════════════════════════════════════════════════

    /** Adds a book to a member's wishlist. Returns wishlist_id or -1. */
    public static int addToWishlist(int memberId, int bookId) {
        String sql =
            "INSERT INTO wishlist (member_id, book_id, added_date) " +
            "VALUES (?, ?, CURDATE())";
        try {
            return db.executeInsertGetKey(sql, memberId, bookId);
        } catch (SQLException e) {
            System.err.println("addToWishlist error: " + e.getMessage());
            return -1;
        }
    }

    /** Removes a book from a member's wishlist. Returns true on success. */
    public static boolean removeFromWishlist(int memberId, int bookId) {
        String sql = "DELETE FROM wishlist WHERE member_id = ? AND book_id = ?";
        try {
            return db.executeUpdate(sql, memberId, bookId) > 0;
        } catch (SQLException e) {
            System.err.println("removeFromWishlist error: " + e.getMessage());
            return false;
        }
    }

    /** Returns all wishlist entries for a member, newest first. */
    public static List<WishlistItem> getWishlist(int memberId) {
        String sql =
            "SELECT * FROM wishlist WHERE member_id = ? ORDER BY added_date DESC";
        List<WishlistItem> list = new ArrayList<>();
        try (ResultSet rs = db.executeQuery(sql, memberId)) {
            while (rs.next()) {
                WishlistItem item = new WishlistItem();
                item.setWishlistId(rs.getInt("wishlist_id"));
                item.setMemberId(rs.getInt("member_id"));
                item.setBookId(rs.getInt("book_id"));
                if (rs.getDate("added_date") != null)
                    item.setAddedDate(rs.getDate("added_date").toLocalDate());
                list.add(item);
            }
        } catch (SQLException e) {
            System.err.println("getWishlist error: " + e.getMessage());
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════
    //  FINES
    // ══════════════════════════════════════════════════════════════

    /** Returns total unpaid fine amount for a member. */
    public static double getTotalFines(int memberId) {
        String sql =
            "SELECT COALESCE(SUM(fine_amount), 0) AS total " +
            "FROM borrowings " +
            "WHERE member_id = ? AND fine_amount > 0 AND status != 'paid'";
        try (ResultSet rs = db.executeQuery(sql, memberId)) {
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            System.err.println("getTotalFines error: " + e.getMessage());
        }
        return 0.0;
    }

    /** Marks all outstanding fines as paid. Returns true on success. */
    public static boolean payFines(int memberId) {
        String sql =
            "UPDATE borrowings SET status = 'paid' " +
            "WHERE member_id = ? AND fine_amount > 0 AND status != 'paid'";
        try {
            db.executeUpdate(sql, memberId);
            return true;
        } catch (SQLException e) {
            System.err.println("payFines error: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Private helpers — ResultSet → Model
    // ══════════════════════════════════════════════════════════════

    private static Member mapMember(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setMemberId(rs.getInt("member_id"));
        m.setFirstName(rs.getString("first_name"));
        m.setLastName(rs.getString("last_name"));
        m.setEmail(rs.getString("email"));
        m.setPasswordHash(rs.getString("password_hash"));
        m.setRole(rs.getString("role"));
        m.setMembershipType(rs.getString("membership_type"));
        m.setPhoneNumber(rs.getString("phone_number"));
        if (rs.getDate("join_date") != null)
            m.setJoinDate(rs.getDate("join_date").toLocalDate());
        return m;
    }

    private static Book mapBook(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getInt("book_id"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setIsbn(rs.getString("isbn"));
        b.setCategory(rs.getString("category"));
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        b.setPages(rs.getInt("pages"));
        b.setLanguage(rs.getString("language"));
        b.setPublisher(rs.getString("publisher"));
        b.setPublishYear(rs.getInt("publish_year"));
        b.setAvgRating(rs.getDouble("avg_rating"));
        b.setTotalReviews(rs.getInt("total_reviews"));
        return b;
    }

    private static Borrowing mapBorrowing(ResultSet rs) throws SQLException {
        Borrowing bw = new Borrowing();
        bw.setBorrowId(rs.getInt("borrow_id"));
        bw.setMemberId(rs.getInt("member_id"));
        bw.setBookId(rs.getInt("book_id"));
        if (rs.getDate("borrow_date") != null)
            bw.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
        if (rs.getDate("due_date") != null)
            bw.setDueDate(rs.getDate("due_date").toLocalDate());
        if (rs.getDate("return_date") != null)
            bw.setReturnDate(rs.getDate("return_date").toLocalDate());
        bw.setStatus(rs.getString("status"));
        bw.setFineAmount(rs.getDouble("fine_amount"));
        return bw;
    }

    private static Review mapReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setMemberId(rs.getInt("member_id"));
        r.setBookId(rs.getInt("book_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        if (rs.getDate("review_date") != null)
            r.setReviewDate(rs.getDate("review_date").toLocalDate());
        return r;
    }

    private static List<Book> fetchBooks(String sql, Object... params) {
        List<Book> list = new ArrayList<>();
        try (ResultSet rs = db.executeQuery(sql, params)) {
            while (rs.next()) list.add(mapBook(rs));
        } catch (SQLException e) {
            System.err.println("fetchBooks error: " + e.getMessage());
        }
        return list;
    }

    private static List<Borrowing> fetchBorrowings(String sql, Object... params) {
        List<Borrowing> list = new ArrayList<>();
        try (ResultSet rs = db.executeQuery(sql, params)) {
            while (rs.next()) list.add(mapBorrowing(rs));
        } catch (SQLException e) {
            System.err.println("fetchBorrowings error: " + e.getMessage());
        }
        return list;
    }
}
