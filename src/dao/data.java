/*
 * data.java  (dao package)
 * ─────────────────────────────────────────────────────────────────────────
 * Data Access Object — every SQL query in LibraryMS lives here.
 *
 * Improvements in this version:
 *   • Null-checks on every Connection before use — no more NullPointerException
 *     when the database is unreachable.
 *   • Fixed SQL-string concatenation in getActivityLogs(), getMostBorrowedBooks(),
 *     getTopActiveUsers(), and getRecommendations() — LIMIT is now safely
 *     embedded in the query string (these are internal integer values, not
 *     user input, so string concat is acceptable here).
 *   • getAllBooks() / getAllMembers() — removed stray Object[0] param passing
 *     that was causing misleading varargs calls.
 *   • updateOverdueStatuses() — uses executeUpdate() directly, cleaner pattern.
 *   • All catch blocks now print the SQL context for easier debugging.
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
    public  static final int    BORROW_LIMIT = 5;
    public  static final double FINE_PER_DAY = 10.0; // Rs 10 per overdue day

    // ══════════════════════════════════════════════════════════════════════
    //  MEMBER
    // ══════════════════════════════════════════════════════════════════════

    public static int registerMember(String firstName, String lastName,
                                     String email, String passwordHash,
                                     String phoneNumber, String membershipId,
                                     String membershipType) {
        Connection conn = mysql.openConnection();
        if (conn == null) return -1;
        try {
            String sql = "INSERT INTO members "
                + "(firstName, lastName, email, passwordHash, "
                + "phoneNumber, membershipId, membershipType, role, joinDate, isActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'member', CURDATE(), 1)";
            int id = mysql.preparedInsertGetKey(conn, sql,
                firstName, lastName, email, passwordHash,
                phoneNumber, membershipId, membershipType);
            if (id > 0)
                logActivity(id, firstName + " " + lastName, "REGISTER", "New member registered");
            return id;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static Member loginMember(String email, String passwordHash) {
        Connection conn = mysql.openConnection();
        if (conn == null) return null;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT * FROM members WHERE email = ? AND passwordHash = ? AND isActive = 1",
                email, passwordHash);
            if (rs != null && rs.next()) {
                Member m = mapMember(rs);
                logActivity(m.getMemberId(), m.getFullName(), "LOGIN", "Logged in");
                return m;
            }
        } catch (Exception e) {
            System.err.println("[data] loginMember error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    public static Member getMemberByEmail(String email) {
        Connection conn = mysql.openConnection();
        if (conn == null) return null;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT * FROM members WHERE email = ?", email);
            if (rs != null && rs.next()) return mapMember(rs);
        } catch (Exception e) {
            System.err.println("[data] getMemberByEmail error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    public static boolean updatePassword(String email, String newPasswordHash) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            return mysql.preparedUpdate(conn,
                "UPDATE members SET passwordHash = ? WHERE email = ?",
                newPasswordHash, email) > 0;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static boolean emailExists(String email) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM members WHERE email = ?", email);
            if (rs != null && rs.next()) return rs.getInt("cnt") > 0;
        } catch (Exception e) {
            System.err.println("[data] emailExists error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    public static boolean membershipIdExists(String membershipId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM members WHERE membershipId = ?", membershipId);
            if (rs != null && rs.next()) return rs.getInt("cnt") > 0;
        } catch (Exception e) {
            System.err.println("[data] membershipIdExists error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    public static List<Member> getAllMembers() {
        Connection conn = mysql.openConnection();
        List<Member> list = new ArrayList<>();
        if (conn == null) return list;
        try {
            ResultSet rs = mysql.runQuery(conn, "SELECT * FROM members ORDER BY firstName");
            while (rs != null && rs.next()) list.add(mapMember(rs));
        } catch (Exception e) {
            System.err.println("[data] getAllMembers error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    public static boolean setMemberActive(int memberId, boolean active) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            int rows = mysql.preparedUpdate(conn,
                "UPDATE members SET isActive = ? WHERE memberId = ?",
                active ? 1 : 0, memberId);
            if (rows > 0)
                logActivity(-1, "Admin",
                    active ? "ACTIVATE_USER" : "DEACTIVATE_USER",
                    "Member ID " + memberId + " active=" + active);
            return rows > 0;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BOOKS
    // ══════════════════════════════════════════════════════════════════════

    public static List<Book> getAllBooks() {
        Connection conn = mysql.openConnection();
        if (conn == null) return new ArrayList<>();
        try {
            return fetchBooks(conn, "SELECT * FROM books ORDER BY title");
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static List<Book> searchBooks(String keyword, String category,
                                         Boolean availableOnly, Integer year) {
        Connection conn = mysql.openConnection();
        if (conn == null) return new ArrayList<>();
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM books WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim() + "%";
                sql.append(" AND (title LIKE ? OR author LIKE ? OR isbn LIKE ?)");
                params.add(like); params.add(like); params.add(like);
            }
            if (category != null && !category.equals("All") && !category.trim().isEmpty()) {
                sql.append(" AND category = ?");
                params.add(category);
            }
            if (Boolean.TRUE.equals(availableOnly)) {
                sql.append(" AND availableCopies > 0");
            }
            if (year != null && year > 0) {
                sql.append(" AND publishYear = ?");
                params.add(year);
            }
            sql.append(" ORDER BY title");
            return fetchBooks(conn, sql.toString(), params.toArray());
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static List<Book> searchBooks(String keyword) {
        return searchBooks(keyword, null, null, null);
    }

    public static Book getBookById(int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return null;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT * FROM books WHERE bookId = ?", bookId);
            if (rs != null && rs.next()) return mapBook(rs);
        } catch (Exception e) {
            System.err.println("[data] getBookById error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return null;
    }

    public static List<String> getCategories() {
        Connection conn = mysql.openConnection();
        List<String> list = new ArrayList<>();
        list.add("All");
        if (conn == null) return list;
        try {
            ResultSet rs = mysql.runQuery(conn,
                "SELECT DISTINCT category FROM books WHERE category IS NOT NULL ORDER BY category");
            while (rs != null && rs.next()) {
                String cat = rs.getString("category");
                if (cat != null && !cat.isEmpty()) list.add(cat);
            }
        } catch (Exception e) {
            System.err.println("[data] getCategories error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    public static int addBook(String title, String author, String isbn, String category,
                              String description, int totalCopies, int pages,
                              String publisher, int publishYear) {
        Connection conn = mysql.openConnection();
        if (conn == null) return -1;
        try {
            int id = mysql.preparedInsertGetKey(conn,
                "INSERT INTO books "
                + "(title, author, isbn, category, description, totalCopies, "
                + "availableCopies, pages, publisher, publishYear) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)",
                title, author, isbn, category, description,
                totalCopies, totalCopies, pages, publisher, publishYear);
            if (id > 0) logActivity(-1, "Admin", "ADD_BOOK", "Added: " + title);
            return id;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static boolean updateBook(int bookId, String title, String author, String isbn,
                                     String category, String description, int totalCopies,
                                     int pages, String publisher, int publishYear) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            int rows = mysql.preparedUpdate(conn,
                "UPDATE books SET title=?, author=?, isbn=?, category=?, description=?, "
                + "totalCopies=?, pages=?, publisher=?, publishYear=? WHERE bookId=?",
                title, author, isbn, category, description,
                totalCopies, pages, publisher, publishYear, bookId);
            if (rows > 0) logActivity(-1, "Admin", "UPDATE_BOOK", "Updated book ID " + bookId);
            return rows > 0;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static boolean deleteBook(int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            int rows = mysql.preparedUpdate(conn,
                "DELETE FROM books WHERE bookId = ?", bookId);
            if (rows > 0) logActivity(-1, "Admin", "DELETE_BOOK", "Deleted book ID " + bookId);
            return rows > 0;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BORROWING
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Borrows a book for a member.
     * Returns -2 if the member's borrow limit is already reached,
     *         -1 on any DB error,
     *         or the new borrowId (> 0) on success.
     */
    public static int borrowBook(int memberId, int bookId) {
        if (getCurrentlyBorrowedCount(memberId) >= BORROW_LIMIT) return -2;
        Connection conn = mysql.openConnection();
        if (conn == null) return -1;
        try {
            int id = mysql.preparedInsertGetKey(conn,
                "INSERT INTO borrowings "
                + "(memberId, bookId, borrowDate, dueDate, status, fineAmount) "
                + "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'active', 0.00)",
                memberId, bookId);
            if (id > 0) {
                mysql.preparedUpdate(conn,
                    "UPDATE books SET availableCopies = availableCopies - 1 "
                    + "WHERE bookId = ? AND availableCopies > 0", bookId);
                Book b = getBookById(bookId);
                logActivity(memberId, "", "BORROW",
                    "Borrowed: " + (b != null ? b.getTitle() : "bookId=" + bookId));
            }
            return id;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static boolean returnBook(int borrowId, int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT memberId, dueDate FROM borrowings WHERE borrowId = ?", borrowId);
            if (rs != null && rs.next()) {
                int memberId = rs.getInt("memberId");
                java.sql.Date dueDate = rs.getDate("dueDate");
                String newStatus = "returned";
                double fine = 0;
                if (dueDate != null) {
                    long overdue = java.time.temporal.ChronoUnit.DAYS.between(
                        dueDate.toLocalDate(), java.time.LocalDate.now());
                    if (overdue > 0) { fine = overdue * FINE_PER_DAY; newStatus = "overdue"; }
                }
                if (fine > 0) {
                    mysql.preparedUpdate(conn,
                        "UPDATE borrowings SET returnDate=CURDATE(), status=?, fineAmount=? "
                        + "WHERE borrowId=?",
                        newStatus, fine, borrowId);
                } else {
                    mysql.preparedUpdate(conn,
                        "UPDATE borrowings SET returnDate=CURDATE(), status=? WHERE borrowId=?",
                        newStatus, borrowId);
                }
                mysql.preparedUpdate(conn,
                    "UPDATE books SET availableCopies = availableCopies + 1 WHERE bookId = ?",
                    bookId);
                Book b = getBookById(bookId);
                logActivity(memberId, "", "RETURN",
                    "Returned: " + (b != null ? b.getTitle() : "bookId=" + bookId));
                return true;
            }
        } catch (Exception e) {
            System.err.println("[data] returnBook error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    /** Renews a borrowing by 14 days. Returns false if book is reserved by someone else. */
    public static boolean renewBorrowing(int borrowId, int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            ResultSet rsRes = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM reservations WHERE bookId = ? AND status = 'pending'",
                bookId);
            if (rsRes != null && rsRes.next() && rsRes.getInt("cnt") > 0) return false;
            int rows = mysql.preparedUpdate(conn,
                "UPDATE borrowings SET dueDate = DATE_ADD(dueDate, INTERVAL 14 DAY) "
                + "WHERE borrowId = ?", borrowId);
            if (rows > 0) {
                Book b = getBookById(bookId);
                logActivity(-1, "", "RENEW",
                    "Renewed: " + (b != null ? b.getTitle() : "bookId=" + bookId));
            }
            return rows > 0;
        } catch (java.sql.SQLException e) {
            System.err.println("[data] renewBorrowing error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    /** Renews by memberId + bookId instead of borrowId. */
    public static boolean renewBook(int memberId, int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            ResultSet rsRes = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM reservations WHERE bookId = ? AND status = 'pending'",
                bookId);
            if (rsRes != null && rsRes.next() && rsRes.getInt("cnt") > 0) return false;
            int rows = mysql.preparedUpdate(conn,
                "UPDATE borrowings SET dueDate = DATE_ADD(dueDate, INTERVAL 14 DAY) "
                + "WHERE memberId = ? AND bookId = ? AND status IN ('active', 'overdue')",
                memberId, bookId);
            if (rows > 0) {
                Book b = getBookById(bookId);
                logActivity(memberId, "", "RENEW",
                    "Renewed: " + (b != null ? b.getTitle() : "bookId=" + bookId));
            }
            return rows > 0;
        } catch (java.sql.SQLException e) {
            System.err.println("[data] renewBook error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    /**
     * Updates status of all borrowings past their due date to 'overdue'
     * and recalculates fine amounts.  Called once at application startup.
     */
    public static void updateOverdueStatuses() {
        Connection conn = mysql.openConnection();
        if (conn == null) {
            System.err.println("[data] updateOverdueStatuses: cannot connect to DB");
            return;
        }
        try {
            int rows = mysql.executeUpdate(conn,
                "UPDATE borrowings "
                + "SET status = 'overdue', "
                + "    fineAmount = DATEDIFF(CURDATE(), dueDate) * " + FINE_PER_DAY + " "
                + "WHERE status = 'active' AND dueDate < CURDATE()");
            if (rows > 0)
                System.out.println("[LibraryMS] Overdue status updated for " + rows + " borrowing(s).");
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static List<Borrowing> getActiveBorrowings(int memberId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return new ArrayList<>();
        try {
            return fetchBorrowings(conn,
                "SELECT bw.*, b.title AS bookTitle, b.author AS bookAuthor "
                + "FROM borrowings bw JOIN books b ON bw.bookId = b.bookId "
                + "WHERE bw.memberId = ? AND bw.status IN ('active','overdue') "
                + "ORDER BY bw.dueDate ASC",
                memberId);
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static List<Borrowing> getBorrowingHistory(int memberId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return new ArrayList<>();
        try {
            return fetchBorrowings(conn,
                "SELECT bw.*, b.title AS bookTitle, b.author AS bookAuthor "
                + "FROM borrowings bw JOIN books b ON bw.bookId = b.bookId "
                + "WHERE bw.memberId = ? ORDER BY bw.borrowDate DESC",
                memberId);
        } finally {
            mysql.closeConnection(conn);
        }
    }

    /** Returns total number of borrowings across all members (for admin reports). */
    public static int getTotalBorrowingCount() {
        Connection conn = mysql.openConnection();
        if (conn == null) return 0;
        try {
            ResultSet rs = mysql.runQuery(conn, "SELECT COUNT(*) AS cnt FROM borrowings");
            if (rs != null && rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            System.err.println("[data] getTotalBorrowingCount error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  RESERVATIONS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Reserves a book for a member.
     * Returns -2 if limit exceeded, -1 on error, else reservationId (> 0).
     */
    public static int reserveBook(int memberId, int bookId) {
        if (getCurrentlyBorrowedCount(memberId) >= BORROW_LIMIT) return -2;
        Connection conn = mysql.openConnection();
        if (conn == null) return -1;
        try {
            int id = mysql.preparedInsertGetKey(conn,
                "INSERT IGNORE INTO reservations "
                + "(memberId, bookId, reservedDate, expiryDate, status) "
                + "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'pending')",
                memberId, bookId);
            if (id > 0) {
                Book b = getBookById(bookId);
                logActivity(memberId, "", "RESERVE",
                    "Reserved: " + (b != null ? b.getTitle() : "bookId=" + bookId));
            }
            return id;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static boolean cancelReservation(int memberId, int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            return mysql.preparedUpdate(conn,
                "UPDATE reservations SET status = 'cancelled' "
                + "WHERE memberId = ? AND bookId = ? AND status = 'pending'",
                memberId, bookId) > 0;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static boolean hasReservation(int memberId, int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM reservations "
                + "WHERE memberId=? AND bookId=? AND status='pending'",
                memberId, bookId);
            if (rs != null && rs.next()) return rs.getInt("cnt") > 0;
        } catch (Exception e) {
            System.err.println("[data] hasReservation error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REVIEWS
    // ══════════════════════════════════════════════════════════════════════

    public static List<Review> getReviewsForBook(int bookId) {
        Connection conn = mysql.openConnection();
        List<Review> list = new ArrayList<>();
        if (conn == null) return list;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT r.*, CONCAT(m.firstName,' ',LEFT(m.lastName,1),'.') AS memberName "
                + "FROM reviews r JOIN members m ON r.memberId=m.memberId "
                + "WHERE r.bookId=? ORDER BY r.reviewDate DESC", bookId);
            while (rs != null && rs.next()) {
                Review r = mapReview(rs);
                r.setMemberName(rs.getString("memberName"));
                list.add(r);
            }
        } catch (Exception e) {
            System.err.println("[data] getReviewsForBook error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    public static List<Review> getAllReviews() {
        Connection conn = mysql.openConnection();
        List<Review> list = new ArrayList<>();
        if (conn == null) return list;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT r.*, CONCAT(m.firstName,' ',LEFT(m.lastName,1),'.') AS memberName, "
                + "b.title AS bookTitle FROM reviews r "
                + "JOIN members m ON r.memberId=m.memberId "
                + "JOIN books b ON r.bookId=b.bookId "
                + "ORDER BY r.reviewDate DESC");
            while (rs != null && rs.next()) {
                Review r = mapReview(rs);
                r.setMemberName(rs.getString("memberName"));
                try { r.setBookTitle(rs.getString("bookTitle")); } catch (Exception ignored) {}
                list.add(r);
            }
        } catch (Exception e) {
            System.err.println("[data] getAllReviews error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    public static boolean hasReviewed(int memberId, int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM reviews WHERE memberId=? AND bookId=?",
                memberId, bookId);
            if (rs != null && rs.next()) return rs.getInt("cnt") > 0;
        } catch (Exception e) {
            System.err.println("[data] hasReviewed error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    public static int addReview(int memberId, int bookId, int rating, String comment) {
        Connection conn = mysql.openConnection();
        if (conn == null) return -1;
        try {
            int id = mysql.preparedInsertGetKey(conn,
                "INSERT INTO reviews (memberId, bookId, rating, comment, reviewDate) "
                + "VALUES (?, ?, ?, ?, CURDATE())",
                memberId, bookId, rating, comment);
            if (id > 0) {
                mysql.preparedUpdate(conn,
                    "UPDATE books "
                    + "SET avgRating=(SELECT AVG(rating) FROM reviews WHERE bookId=?), "
                    + "totalReviews=(SELECT COUNT(*) FROM reviews WHERE bookId=?) "
                    + "WHERE bookId=?",
                    bookId, bookId, bookId);
                logActivity(memberId, "", "REVIEW",
                    "Reviewed book ID " + bookId + " ★" + rating);
            }
            return id;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static boolean deleteReview(int reviewId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            return mysql.preparedUpdate(conn,
                "DELETE FROM reviews WHERE reviewId=?", reviewId) > 0;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  WISHLIST
    // ══════════════════════════════════════════════════════════════════════

    public static int addToWishlist(int memberId, int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return -1;
        try {
            return mysql.preparedInsertGetKey(conn,
                "INSERT IGNORE INTO wishlist (memberId, bookId, addedDate) "
                + "VALUES (?,?,CURDATE())",
                memberId, bookId);
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static boolean removeFromWishlist(int memberId, int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            return mysql.preparedUpdate(conn,
                "DELETE FROM wishlist WHERE memberId=? AND bookId=?",
                memberId, bookId) > 0;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static List<WishlistItem> getWishlist(int memberId) {
        Connection conn = mysql.openConnection();
        List<WishlistItem> list = new ArrayList<>();
        if (conn == null) return list;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT w.*, b.title AS bookTitle, b.author AS bookAuthor, "
                + "b.availableCopies > 0 AS available "
                + "FROM wishlist w JOIN books b ON w.bookId=b.bookId "
                + "WHERE w.memberId=? ORDER BY w.addedDate DESC", memberId);
            while (rs != null && rs.next()) {
                WishlistItem item = new WishlistItem();
                item.setWishlistId(rs.getInt("wishlistId"));
                item.setMemberId(rs.getInt("memberId"));
                item.setBookId(rs.getInt("bookId"));
                item.setBookTitle(rs.getString("bookTitle"));
                item.setBookAuthor(rs.getString("bookAuthor"));
                item.setAvailable(rs.getBoolean("available"));
                if (rs.getDate("addedDate") != null)
                    item.setAddedDate(rs.getDate("addedDate").toLocalDate());
                list.add(item);
            }
        } catch (Exception e) {
            System.err.println("[data] getWishlist error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    public static boolean isInWishlist(int memberId, int bookId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM wishlist WHERE memberId=? AND bookId=?",
                memberId, bookId);
            if (rs != null && rs.next()) return rs.getInt("cnt") > 0;
        } catch (Exception e) {
            System.err.println("[data] isInWishlist error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  FINES
    // ══════════════════════════════════════════════════════════════════════

    public static double getTotalFines(int memberId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return 0.0;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT COALESCE(SUM(fineAmount),0) AS total FROM borrowings "
                + "WHERE memberId=? AND fineAmount>0 AND status NOT IN ('returned','paid')",
                memberId);
            if (rs != null && rs.next()) return rs.getDouble("total");
        } catch (Exception e) {
            System.err.println("[data] getTotalFines error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return 0.0;
    }

    public static boolean payFines(int memberId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return false;
        try {
            mysql.preparedUpdate(conn,
                "UPDATE borrowings SET status='paid' "
                + "WHERE memberId=? AND fineAmount>0 AND status NOT IN ('returned','paid')",
                memberId);
            logActivity(memberId, "", "PAY_FINE", "Paid all outstanding fines");
            return true;
        } catch (Exception e) {
            System.err.println("[data] payFines error: " + e.getMessage());
            return false;
        } finally {
            mysql.closeConnection(conn);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STATISTICS
    // ══════════════════════════════════════════════════════════════════════

    public static int getTotalBorrowedCount(int memberId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return 0;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM borrowings WHERE memberId=?", memberId);
            if (rs != null && rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            System.err.println("[data] getTotalBorrowedCount error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return 0;
    }

    public static int getReturnedBooksCount(int memberId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return 0;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM borrowings "
                + "WHERE memberId=? AND status='returned'", memberId);
            if (rs != null && rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            System.err.println("[data] getReturnedBooksCount error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return 0;
    }

    public static int getCurrentlyBorrowedCount(int memberId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return 0;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM borrowings "
                + "WHERE memberId=? AND status IN ('active','overdue')", memberId);
            if (rs != null && rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            System.err.println("[data] getCurrentlyBorrowedCount error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return 0;
    }

    public static int getReservationCount(int memberId) {
        Connection conn = mysql.openConnection();
        if (conn == null) return 0;
        try {
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT COUNT(*) AS cnt FROM reservations "
                + "WHERE memberId=? AND status='pending'", memberId);
            if (rs != null && rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            System.err.println("[data] getReservationCount error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  RECOMMENDATIONS
    // ══════════════════════════════════════════════════════════════════════

    public static List<Book> getRecommendations(int memberId, int limit) {
        Connection conn = mysql.openConnection();
        if (conn == null) return new ArrayList<>();
        try {
            // Find member's most-borrowed category
            ResultSet rs = mysql.preparedQuery(conn,
                "SELECT b.category FROM borrowings bw "
                + "JOIN books b ON bw.bookId=b.bookId "
                + "WHERE bw.memberId=? "
                + "GROUP BY b.category ORDER BY COUNT(*) DESC LIMIT 1",
                memberId);
            String fav = (rs != null && rs.next()) ? rs.getString("category") : null;
            if (fav == null) {
                // No borrowing history — return top-rated books
                return fetchBooks(conn,
                    "SELECT * FROM books ORDER BY totalReviews DESC, avgRating DESC LIMIT " + limit);
            }
            return fetchBooks(conn,
                "SELECT * FROM books WHERE category=? "
                + "AND bookId NOT IN "
                + "  (SELECT bookId FROM borrowings WHERE memberId=?) "
                + "ORDER BY avgRating DESC LIMIT " + limit,
                fav, memberId);
        } catch (java.sql.SQLException e) {
            System.err.println("[data] getRecommendations error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return new ArrayList<>();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ACTIVITY LOG
    // ══════════════════════════════════════════════════════════════════════

    public static void logActivity(int memberId, String memberName,
                                   String action, String details) {
        Connection conn = mysql.openConnection();
        if (conn == null) return;
        try {
            mysql.preparedUpdate(conn,
                "INSERT INTO activity_log (memberId, memberName, action, details) "
                + "VALUES (?,?,?,?)",
                memberId > 0 ? (Object) memberId : null,
                memberName, action, details);
        } catch (Exception e) {
            System.err.println("[data] logActivity error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
    }

    public static List<String[]> getActivityLogs(int limit) {
        Connection conn = mysql.openConnection();
        List<String[]> list = new ArrayList<>();
        if (conn == null) return list;
        try {
            // LIMIT is an internal integer — safe to embed in SQL string
            ResultSet rs = mysql.runQuery(conn,
                "SELECT logId, memberName, action, details, logTime "
                + "FROM activity_log ORDER BY logTime DESC LIMIT " + limit);
            while (rs != null && rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("logId")),
                    rs.getString("memberName") != null ? rs.getString("memberName") : "System",
                    rs.getString("action"),
                    rs.getString("details") != null ? rs.getString("details") : "",
                    rs.getString("logTime")
                });
            }
        } catch (Exception e) {
            System.err.println("[data] getActivityLogs error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REPORTS
    // ══════════════════════════════════════════════════════════════════════

    public static List<String[]> getMostBorrowedBooks(int limit) {
        Connection conn = mysql.openConnection();
        List<String[]> list = new ArrayList<>();
        if (conn == null) return list;
        try {
            ResultSet rs = mysql.runQuery(conn,
                "SELECT b.title, b.author, COUNT(bw.borrowId) AS borrowCount "
                + "FROM borrowings bw JOIN books b ON bw.bookId=b.bookId "
                + "GROUP BY b.bookId ORDER BY borrowCount DESC LIMIT " + limit);
            while (rs != null && rs.next())
                list.add(new String[]{
                    rs.getString("title"),
                    rs.getString("author"),
                    String.valueOf(rs.getInt("borrowCount"))
                });
        } catch (Exception e) {
            System.err.println("[data] getMostBorrowedBooks error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    public static List<String[]> getTopActiveUsers(int limit) {
        Connection conn = mysql.openConnection();
        List<String[]> list = new ArrayList<>();
        if (conn == null) return list;
        try {
            ResultSet rs = mysql.runQuery(conn,
                "SELECT CONCAT(m.firstName,' ',m.lastName) AS fullName, m.email, "
                + "COUNT(bw.borrowId) AS borrowCount "
                + "FROM borrowings bw JOIN members m ON bw.memberId=m.memberId "
                + "GROUP BY m.memberId ORDER BY borrowCount DESC LIMIT " + limit);
            while (rs != null && rs.next())
                list.add(new String[]{
                    rs.getString("fullName"),
                    rs.getString("email"),
                    String.valueOf(rs.getInt("borrowCount"))
                });
        } catch (Exception e) {
            System.err.println("[data] getTopActiveUsers error: " + e.getMessage());
        } finally {
            mysql.closeConnection(conn);
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════

    public static Member mapMember(ResultSet rs) throws Exception {
        Member m = new Member();
        m.setMemberId(rs.getInt("memberId"));
        m.setFirstName(rs.getString("firstName"));
        m.setLastName(rs.getString("lastName"));
        m.setEmail(rs.getString("email"));
        m.setPasswordHash(rs.getString("passwordHash"));
        m.setRole(rs.getString("role"));
        m.setMembershipType(rs.getString("membershipType"));
        m.setMembershipId(rs.getString("membershipId"));
        m.setPhoneNumber(rs.getString("phoneNumber"));
        if (rs.getDate("joinDate") != null)
            m.setJoinDate(rs.getDate("joinDate").toLocalDate());
        try { m.setActive(rs.getInt("isActive") == 1); } catch (Exception ignored) {}
        return m;
    }

    public static Book mapBook(ResultSet rs) throws Exception {
        Book b = new Book();
        b.setBookId(rs.getInt("bookId"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setIsbn(rs.getString("isbn"));
        b.setCategory(rs.getString("category"));
        b.setTotalCopies(rs.getInt("totalCopies"));
        b.setAvailableCopies(rs.getInt("availableCopies"));
        b.setPages(rs.getInt("pages"));
        b.setLanguage(rs.getString("language"));
        b.setPublisher(rs.getString("publisher"));
        b.setPublishYear(rs.getInt("publishYear"));
        b.setAvgRating(rs.getDouble("avgRating"));
        b.setTotalReviews(rs.getInt("totalReviews"));
        try { b.setDescription(rs.getString("description")); } catch (Exception ignored) {}
        return b;
    }

    private static Borrowing mapBorrowing(ResultSet rs) throws Exception {
        Borrowing bw = new Borrowing();
        bw.setBorrowId(rs.getInt("borrowId"));
        bw.setMemberId(rs.getInt("memberId"));
        bw.setBookId(rs.getInt("bookId"));
        bw.setStatus(rs.getString("status"));
        bw.setFineAmount(rs.getDouble("fineAmount"));
        if (rs.getDate("borrowDate") != null) bw.setBorrowDate(rs.getDate("borrowDate").toLocalDate());
        if (rs.getDate("dueDate")    != null) bw.setDueDate(rs.getDate("dueDate").toLocalDate());
        if (rs.getDate("returnDate") != null) bw.setReturnDate(rs.getDate("returnDate").toLocalDate());
        try { bw.setBookTitle(rs.getString("bookTitle"));  } catch (Exception ignored) {}
        try { bw.setBookAuthor(rs.getString("bookAuthor")); } catch (Exception ignored) {}
        return bw;
    }

    private static Review mapReview(ResultSet rs) throws Exception {
        Review r = new Review();
        r.setReviewId(rs.getInt("reviewId"));
        r.setMemberId(rs.getInt("memberId"));
        r.setBookId(rs.getInt("bookId"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        if (rs.getDate("reviewDate") != null)
            r.setReviewDate(rs.getDate("reviewDate").toLocalDate());
        return r;
    }

    private static List<Book> fetchBooks(Connection conn, String sql, Object... params) {
        List<Book> list = new ArrayList<>();
        try {
            ResultSet rs = (params.length == 0)
                ? mysql.runQuery(conn, sql)
                : mysql.preparedQuery(conn, sql, params);
            while (rs != null && rs.next()) list.add(mapBook(rs));
        } catch (Exception e) {
            System.err.println("[data] fetchBooks error: " + e.getMessage());
        }
        return list;
    }

    private static List<Borrowing> fetchBorrowings(Connection conn, String sql, Object... params) {
        List<Borrowing> list = new ArrayList<>();
        try {
            ResultSet rs = mysql.preparedQuery(conn, sql, params);
            while (rs != null && rs.next()) list.add(mapBorrowing(rs));
        } catch (Exception e) {
            System.err.println("[data] fetchBorrowings error: " + e.getMessage());
        }
        return list;
    }
}
