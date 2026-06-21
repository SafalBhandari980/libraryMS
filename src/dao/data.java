/*
 * data.java  (dao package)
 * ─────────────────────────────────────────────────────────────────────────
 * Data Access Object — every SQL query in LibraryMS lives here.
 * Only this class talks to the database layer.
 *
 * Pattern:  View  →  Controller  →  DAO (data)  →  MySqlConnector  →  MySQL
 *
 * Uses MySqlConnector.preparedQuery / preparedUpdate / preparedInsertGetKey
 * so every query is properly parameterised (no SQL injection).
 * ─────────────────────────────────────────────────────────────────────────
 */
package dao;

import database.MySqlConnector;
import model.java.Book;
import model.java.Borrowing;
import model.java.Member;
import model.java.Review;
import model.java.WishlistItem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class data {

    private static final MySqlConnector mysql = new MySqlConnector();

    // ══════════════════════════════════════════════════════════════════════
    //  MEMBER – used by Login, Signup, ForgotPassword controllers
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Registers a new library member.
     * membershipType should be "student", "faculty", or "public".
     * Returns the auto-generated member_id, or -1 on failure.
     */
    public static int registerMember(String firstName, String lastName,
                                     String email, String passwordHash,
                                     String phoneNumber, String membershipId,
                                     String membershipType) {
        Connection conn = mysql.openConnection();
        try {
            String sql =
                "INSERT INTO members " +
                "(first_name, last_name, email, password_hash, phone_number, " +
                " membership_id, membership_type, role, join_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'member', CURDATE())";
            return mysql.preparedInsertGetKey(conn, sql,
                firstName, lastName, email, passwordHash,
                phoneNumber, membershipId, membershipType);
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /**
     * Authenticates a member by email + password hash.
     * Returns the matching Member object, or null if credentials are wrong.
     */
    public static Member loginMember(String email, String passwordHash) {
        Connection conn = mysql.openConnection();
        try {
            String sql = "SELECT * FROM members WHERE email = ? AND password_hash = ?";
            ResultSet rs = mysql.preparedQuery(conn, sql, email, passwordHash);
            if (rs != null && rs.next()) return mapMember(rs);
        } catch (Exception e) {
            System.err.println("loginMember error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    /**
     * Finds a member by email only — used in the Forgot Password OTP flow.
     * Returns null if no account with that email exists.
     */
    public static Member getMemberByEmail(String email) {
        Connection conn = mysql.openConnection();
        try {
            String sql = "SELECT * FROM members WHERE email = ?";
            ResultSet rs = mysql.preparedQuery(conn, sql, email);
            if (rs != null && rs.next()) return mapMember(rs);
        } catch (Exception e) {
            System.err.println("getMemberByEmail error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    /**
     * Updates the password hash after OTP verification.
     * Returns true on success.
     */
    public static boolean updatePassword(String email, String newPasswordHash) {
        Connection conn = mysql.openConnection();
        try {
            String sql = "UPDATE members SET password_hash = ? WHERE email = ?";
            return mysql.preparedUpdate(conn, sql, newPasswordHash, email) > 0;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /**
     * Checks whether an email address is already registered.
     * Used in Signup validation.
     */
    public static boolean emailExists(String email) {
        Connection conn = mysql.openConnection();
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM members WHERE email = ?";
            ResultSet rs = mysql.preparedQuery(conn, sql, email);
            if (rs != null && rs.next()) return rs.getInt("cnt") > 0;
        } catch (Exception e) {
            System.err.println("emailExists error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    /**
     * Checks whether a membership ID is already taken.
     * Used in Signup validation.
     */
    public static boolean membershipIdExists(String membershipId) {
        Connection conn = mysql.openConnection();
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM members WHERE membership_id = ?";
            ResultSet rs = mysql.preparedQuery(conn, sql, membershipId);
            if (rs != null && rs.next()) return rs.getInt("cnt") > 0;
        } catch (Exception e) {
            System.err.println("membershipIdExists error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BOOKS – used by BookSearch controller
    // ══════════════════════════════════════════════════════════════════════

    /** Returns all books ordered by title. */
    public static List<Book> getAllBooks() {
        Connection conn = mysql.openConnection();
        try {
            return fetchBooks(conn, "SELECT * FROM books ORDER BY title");
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /**
     * Full-text book search by title, author, or ISBN.
     * Case-insensitive; partial matches supported.
     */
    public static List<Book> searchBooks(String keyword) {
        Connection conn = mysql.openConnection();
        try {
            String like = "%" + keyword + "%";
            String sql  =
                "SELECT * FROM books " +
                "WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? " +
                "ORDER BY title";
            return fetchBooks(conn, sql, like, like, like);
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /** Returns a single book by its primary key, or null if not found. */
    public static Book getBookById(int bookId) {
        Connection conn = mysql.openConnection();
        try {
            String sql = "SELECT * FROM books WHERE book_id = ?";
            ResultSet rs = mysql.preparedQuery(conn, sql, bookId);
            if (rs != null && rs.next()) return mapBook(rs);
        } catch (Exception e) {
            System.err.println("getBookById error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BORROWING – used by MyBorrowings controller
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Creates a 14-day borrowing record and decrements available_copies.
     * Returns the new borrow_id, or -1 on failure.
     */
    public static int borrowBook(int memberId, int bookId) {
        Connection conn = mysql.openConnection();
        try {
            String insertSql =
                "INSERT INTO borrowings " +
                "(member_id, book_id, borrow_date, due_date, status, fine_amount) " +
                "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'active', 0.00)";
            int id = mysql.preparedInsertGetKey(conn, insertSql, memberId, bookId);
            if (id > 0) {
                String decrementSql =
                    "UPDATE books SET available_copies = available_copies - 1 " +
                    "WHERE book_id = ? AND available_copies > 0";
                mysql.preparedUpdate(conn, decrementSql, bookId);
            }
            return id;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /**
     * Marks a borrowing as returned and restores available_copies.
     * Returns true on success.
     */
    public static boolean returnBook(int borrowId, int bookId) {
        Connection conn = mysql.openConnection();
        try {
            mysql.preparedUpdate(conn,
                "UPDATE borrowings SET return_date = CURDATE(), status = 'returned' " +
                "WHERE borrow_id = ?", borrowId);
            mysql.preparedUpdate(conn,
                "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?",
                bookId);
            return true;
        } catch (Exception e) {
            System.err.println("returnBook error: " + e.getMessage());
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /**
     * Returns all active / overdue borrowings for a member (My Borrowings screen).
     * Joins book title and author for display.
     * Ordered soonest-due first.
     */
    public static List<Borrowing> getActiveBorrowings(int memberId) {
        Connection conn = mysql.openConnection();
        try {
            String sql =
                "SELECT bw.*, b.title AS book_title, b.author AS book_author " +
                "FROM borrowings bw " +
                "JOIN books b ON bw.book_id = b.book_id " +
                "WHERE bw.member_id = ? AND bw.status IN ('active','overdue') " +
                "ORDER BY bw.due_date ASC";
            return fetchBorrowings(conn, sql, memberId);
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /**
     * Returns the complete borrowing history for a member.
     * Joins book title and author for display.
     * Ordered newest first.
     */
    public static List<Borrowing> getBorrowingHistory(int memberId) {
        Connection conn = mysql.openConnection();
        try {
            String sql =
                "SELECT bw.*, b.title AS book_title, b.author AS book_author " +
                "FROM borrowings bw " +
                "JOIN books b ON bw.book_id = b.book_id " +
                "WHERE bw.member_id = ? " +
                "ORDER BY bw.borrow_date DESC";
            return fetchBorrowings(conn, sql, memberId);
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REVIEWS – used by Reviews controller
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Returns all reviews for a book, newest first.
     * Includes member display name from the members table.
     */
    public static List<Review> getReviewsForBook(int bookId) {
        Connection conn = mysql.openConnection();
        List<Review> list = new ArrayList<>();
        try {
            String sql =
                "SELECT r.*, CONCAT(m.first_name, ' ', LEFT(m.last_name, 1), '.') AS member_name " +
                "FROM reviews r " +
                "JOIN members m ON r.member_id = m.member_id " +
                "WHERE r.book_id = ? " +
                "ORDER BY r.review_date DESC";
            ResultSet rs = mysql.preparedQuery(conn, sql, bookId);
            while (rs != null && rs.next()) {
                Review r = mapReview(rs);
                r.setMemberName(rs.getString("member_name"));
                list.add(r);
            }
        } catch (Exception e) {
            System.err.println("getReviewsForBook error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    /**
     * Inserts a review and recalculates the book's average rating.
     * Returns the new review_id, or -1 on failure.
     */
    public static int addReview(int memberId, int bookId, int rating, String comment) {
        Connection conn = mysql.openConnection();
        try {
            int id = mysql.preparedInsertGetKey(conn,
                "INSERT INTO reviews (member_id, book_id, rating, comment, review_date) " +
                "VALUES (?, ?, ?, ?, CURDATE())",
                memberId, bookId, rating, comment);
            if (id > 0) {
                mysql.preparedUpdate(conn,
                    "UPDATE books SET " +
                    "avg_rating = (SELECT AVG(rating) FROM reviews WHERE book_id = ?), " +
                    "total_reviews = (SELECT COUNT(*) FROM reviews WHERE book_id = ?) " +
                    "WHERE book_id = ?",
                    bookId, bookId, bookId);
            }
            return id;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  WISHLIST – used by WishlistMGMT controller
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Adds a book to a member's wishlist.
     * Returns the new wishlist_id, or -1 on failure (e.g. duplicate).
     */
    public static int addToWishlist(int memberId, int bookId) {
        Connection conn = mysql.openConnection();
        try {
            return mysql.preparedInsertGetKey(conn,
                "INSERT INTO wishlist (member_id, book_id, added_date) VALUES (?, ?, CURDATE())",
                memberId, bookId);
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /**
     * Removes a book from a member's wishlist.
     * Returns true on success.
     */
    public static boolean removeFromWishlist(int memberId, int bookId) {
        Connection conn = mysql.openConnection();
        try {
            return mysql.preparedUpdate(conn,
                "DELETE FROM wishlist WHERE member_id = ? AND book_id = ?",
                memberId, bookId) > 0;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /**
     * Returns all wishlist items for a member, newest first.
     * Joins book title, author, and availability for display.
     */
    public static List<WishlistItem> getWishlist(int memberId) {
        Connection conn = mysql.openConnection();
        List<WishlistItem> list = new ArrayList<>();
        try {
            String sql =
                "SELECT w.*, b.title AS book_title, b.author AS book_author, " +
                "       b.available_copies > 0 AS available " +
                "FROM wishlist w " +
                "JOIN books b ON w.book_id = b.book_id " +
                "WHERE w.member_id = ? ORDER BY w.added_date DESC";
            ResultSet rs = mysql.preparedQuery(conn, sql, memberId);
            while (rs != null && rs.next()) {
                WishlistItem item = new WishlistItem();
                item.setWishlistId(rs.getInt("wishlist_id"));
                item.setMemberId(rs.getInt("member_id"));
                item.setBookId(rs.getInt("book_id"));
                item.setBookTitle(rs.getString("book_title"));
                item.setBookAuthor(rs.getString("book_author"));
                item.setAvailable(rs.getBoolean("available"));
                if (rs.getDate("added_date") != null)
                    item.setAddedDate(rs.getDate("added_date").toLocalDate());
                list.add(item);
            }
        } catch (Exception e) {
            System.err.println("getWishlist error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  FINES – used by FinePayment controller
    // ══════════════════════════════════════════════════════════════════════

    /** Returns the total unpaid fine amount for a member. */
    public static double getTotalFines(int memberId) {
        Connection conn = mysql.openConnection();
        try {
            String sql =
                "SELECT COALESCE(SUM(fine_amount), 0) AS total " +
                "FROM borrowings " +
                "WHERE member_id = ? AND fine_amount > 0 AND status != 'paid'";
            ResultSet rs = mysql.preparedQuery(conn, sql, memberId);
            if (rs != null && rs.next()) return rs.getDouble("total");
        } catch (Exception e) {
            System.err.println("getTotalFines error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return 0.0;
    }

    /**
     * Marks all outstanding fines for a member as paid.
     * Returns true on success.
     */
    public static boolean payFines(int memberId) {
        Connection conn = mysql.openConnection();
        try {
            mysql.preparedUpdate(conn,
                "UPDATE borrowings SET status = 'paid' " +
                "WHERE member_id = ? AND fine_amount > 0 AND status != 'paid'",
                memberId);
            return true;
        } catch (Exception e) {
            System.err.println("payFines error: " + e.getMessage());
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STATISTICS – used by Dashboard / Profile panels
    // ══════════════════════════════════════════════════════════════════════

    /** Returns how many books a member has borrowed in total (all statuses). */
    public static int getTotalBorrowedCount(int memberId) {
        Connection conn = mysql.openConnection();
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM borrowings WHERE member_id = ?";
            ResultSet rs = mysql.preparedQuery(conn, sql, memberId);
            if (rs != null && rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            System.err.println("getTotalBorrowedCount error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return 0;
    }

    /** Returns how many books a member has returned (status = 'returned'). */
    public static int getReturnedBooksCount(int memberId) {
        Connection conn = mysql.openConnection();
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM borrowings " +
                         "WHERE member_id = ? AND status = 'returned'";
            ResultSet rs = mysql.preparedQuery(conn, sql, memberId);
            if (rs != null && rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            System.err.println("getReturnedBooksCount error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return 0;
    }

    /** Returns how many books a member currently has active (status = 'active' | 'overdue'). */
    public static int getCurrentlyBorrowedCount(int memberId) {
        Connection conn = mysql.openConnection();
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM borrowings " +
                         "WHERE member_id = ? AND status IN ('active','overdue')";
            ResultSet rs = mysql.preparedQuery(conn, sql, memberId);
            if (rs != null && rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            System.err.println("getCurrentlyBorrowedCount error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS – ResultSet → Model
    // ══════════════════════════════════════════════════════════════════════

    private static Member mapMember(ResultSet rs) throws Exception {
        Member m = new Member();
        m.setMemberId(rs.getInt("member_id"));
        m.setFirstName(rs.getString("first_name"));
        m.setLastName(rs.getString("last_name"));
        m.setEmail(rs.getString("email"));
        m.setPasswordHash(rs.getString("password_hash"));
        m.setRole(rs.getString("role"));
        m.setMembershipType(rs.getString("membership_type"));
        m.setMembershipId(rs.getString("membership_id"));
        m.setPhoneNumber(rs.getString("phone_number"));
        if (rs.getDate("join_date") != null)
            m.setJoinDate(rs.getDate("join_date").toLocalDate());
        return m;
    }

    private static Book mapBook(ResultSet rs) throws Exception {
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

    private static Borrowing mapBorrowing(ResultSet rs) throws Exception {
        Borrowing bw = new Borrowing();
        bw.setBorrowId(rs.getInt("borrow_id"));
        bw.setMemberId(rs.getInt("member_id"));
        bw.setBookId(rs.getInt("book_id"));
        bw.setStatus(rs.getString("status"));
        bw.setFineAmount(rs.getDouble("fine_amount"));
        if (rs.getDate("borrow_date") != null)
            bw.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
        if (rs.getDate("due_date") != null)
            bw.setDueDate(rs.getDate("due_date").toLocalDate());
        if (rs.getDate("return_date") != null)
            bw.setReturnDate(rs.getDate("return_date").toLocalDate());
        // Optional joined columns (only present when the SQL joins books)
        try { bw.setBookTitle(rs.getString("book_title")); }  catch (Exception ignored) {}
        try { bw.setBookAuthor(rs.getString("book_author")); } catch (Exception ignored) {}
        return bw;
    }

    private static Review mapReview(ResultSet rs) throws Exception {
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

    private static List<Book> fetchBooks(Connection conn, String sql, Object... params) {
        List<Book> list = new ArrayList<>();
        try {
            ResultSet rs = mysql.preparedQuery(conn, sql, params);
            while (rs != null && rs.next()) list.add(mapBook(rs));
        } catch (Exception e) {
            System.err.println("fetchBooks error: " + e.getMessage());
        }
        return list;
    }

   private static List<Borrowing> fetchBorrowings(Connection conn, String sql, Object... params) {
        List<Borrowing> list = new ArrayList<>();
        try {
            ResultSet rs = mysql.preparedQuery(conn, sql, params);
            while (rs != null && rs.next()) list.add(mapBorrowing(rs));
        } catch (Exception e) {
            System.err.println("fetchBorrowings error: " + e.getMessage());
        }
        return list;
    }
}
