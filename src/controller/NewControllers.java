package controller;

import dao.data;
import model.java.Book;
import model.java.Borrowing;
import model.java.Member;
import model.java.Review;
import model.java.WishlistItem;
import utils.Session;
import view.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * NewControllers.java
 * Contains all controllers for the newly added Phase 3 UI pages.
 */
public class NewControllers {

    // ══════════════════════════════════════════════════════════════════════
    //  1.  BookSearchController
    // ══════════════════════════════════════════════════════════════════════
    public static class BookSearchController {
        private final BookSearchUI view;

        public BookSearchController(BookSearchUI view) {
            this.view = view;
            view.loadCategories();
            wireNavigation();
            wireActions();
            performSearch(); // initial load
        }

        private void wireNavigation() {
            view.btnDashboard.addActionListener(e -> navigateTo(new ui()));
            view.btnMyBorrowings.addActionListener(e -> navigateTo(new MyBorrowingsUI()));
            view.btnWishlist.addActionListener(e -> navigateTo(new WishlistMGMT()));
            view.btnReviews.addActionListener(e -> navigateTo(new ReviewsUI()));
            view.btnFinePayment.addActionListener(e -> navigateTo(new FinePayment()));
            view.btnMyAccount.addActionListener(e -> navigateTo(new EditAccountPage()));
            if (view.btnInventory != null) view.btnInventory.addActionListener(e -> navigateTo(new AdminInventoryUI()));
            if (view.btnUsers != null) view.btnUsers.addActionListener(e -> navigateTo(new AdminUsersUI()));
            if (view.btnReports != null) view.btnReports.addActionListener(e -> navigateTo(new AdminReportsUI()));
            view.btnSignOut.addActionListener(e -> logOut(view));
        }

        private void wireActions() {
            // Placeholder logic for Search Bar
            view.txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (view.txtSearch.getText().equals("Search by title, author or ISBN...")) {
                        view.txtSearch.setText("");
                        view.txtSearch.setForeground(new java.awt.Color(30, 40, 60)); // TEXT_DARK
                    }
                }
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (view.txtSearch.getText().isEmpty()) {
                        view.txtSearch.setForeground(new java.awt.Color(122, 134, 154)); // TEXT_MID
                        view.txtSearch.setText("Search by title, author or ISBN...");
                    }
                }
            });

            view.btnSearch.addActionListener(e -> performSearch());
            view.btnFilter.addActionListener(e -> performSearch());
            view.btnClearFilter.addActionListener(e -> {
                view.txtSearch.setText("Search by title, author or ISBN...");
                view.txtSearch.setForeground(new java.awt.Color(122, 134, 154));
                view.cmbCategory.setSelectedIndex(0);
                view.chkAvailableOnly.setSelected(false);
                view.txtYear.setText("");
                performSearch();
            });

            view.btnBorrow.addActionListener(e -> borrowSelected());
            view.btnReserve.addActionListener(e -> reserveSelected());
            view.btnAddWishlist.addActionListener(e -> wishlistSelected());
            view.btnViewDetails.addActionListener(e -> viewDetailsSelected());
            view.btnAddReview.addActionListener(e -> reviewSelected());
            
            // Allow clicking a row to immediately view details
            view.tblBooks.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() >= 1) { // Works on single or double click
                        viewDetailsSelected();
                    }
                }
            });
        }

        private void performSearch() {
            String kw = view.txtSearch.getText().trim();
            if (kw.equals("Search by title, author or ISBN...")) {
                kw = ""; // Treat placeholder as empty search
            }
            String cat = (String) view.cmbCategory.getSelectedItem();
            boolean avail = view.chkAvailableOnly.isSelected();
            Integer year = null;
            try {
                if (!view.txtYear.getText().trim().isEmpty())
                    year = Integer.parseInt(view.txtYear.getText().trim());
            } catch (NumberFormatException ignored) {}

            List<Book> books = data.searchBooks(kw, cat, avail, year);
            view.populateTable(books);
        }

        /**
         * Returns the Book corresponding to the currently selected row.
         * Column 0 of the table model stores the bookId (hidden or visible);
         * we read it directly to avoid the fragile title+author re-search.
         */
        private Book getSelectedBook() {
            int row = view.tblBooks.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(view,
                    "Please select a book from the list first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            try {
                // Column 0 holds the bookId — fetch directly from DB
                Object idVal = view.tblModel.getValueAt(row, 0);
                if (idVal instanceof Integer) {
                    return data.getBookById((Integer) idVal);
                }
                // Fallback: search by title + author if ID column isn't numeric
                String title  = view.tblModel.getValueAt(row, 1).toString();
                String author = view.tblModel.getValueAt(row, 2).toString();
                List<Book> res = data.searchBooks(title);
                for (Book b : res) {
                    if (author.equals(b.getAuthor())) return b;
                }
            } catch (Exception e) {
                System.err.println("[BookSearchController] getSelectedBook error: " + e.getMessage());
            }
            return null;
        }

        private void borrowSelected() {
            Book b = getSelectedBook();
            if (b == null) return;
            if (!b.isAvailable()) {
                JOptionPane.showMessageDialog(view, "This book is currently unavailable.", "Unavailable", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int res = data.borrowBook(Session.getMemberId(), b.getBookId());
            if (res == -2) JOptionPane.showMessageDialog(view, "Borrowing limit exceeded (" + data.BORROW_LIMIT + " books maximum).", "Limit Reached", JOptionPane.WARNING_MESSAGE);
            else if (res > 0) {
                JOptionPane.showMessageDialog(view, "Successfully borrowed: " + b.getTitle(), "Success", JOptionPane.INFORMATION_MESSAGE);
                performSearch();
            } else JOptionPane.showMessageDialog(view, "Failed to borrow book.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        private void reserveSelected() {
            Book b = getSelectedBook();
            if (b == null) return;
            if (b.isAvailable()) {
                JOptionPane.showMessageDialog(view, "This book is available to borrow now. No need to reserve.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int res = data.reserveBook(Session.getMemberId(), b.getBookId());
            if (res == -2) JOptionPane.showMessageDialog(view, "Reservation limit reached (tied to borrow limit).", "Limit Reached", JOptionPane.WARNING_MESSAGE);
            else if (res > 0) JOptionPane.showMessageDialog(view, "Successfully reserved: " + b.getTitle() + ".\nYou will be notified when it is available.", "Success", JOptionPane.INFORMATION_MESSAGE);
            else JOptionPane.showMessageDialog(view, "Failed to reserve or already reserved.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        private void wishlistSelected() {
            Book b = getSelectedBook();
            if (b == null) return;
            int res = data.addToWishlist(Session.getMemberId(), b.getBookId());
            if (res > 0) JOptionPane.showMessageDialog(view, "Added to wishlist: " + b.getTitle(), "Success", JOptionPane.INFORMATION_MESSAGE);
            else JOptionPane.showMessageDialog(view, "Book is already in your wishlist.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        private void viewDetailsSelected() {
            Book b = getSelectedBook();
            if (b == null) return;
            String details = String.format("Title: %s\nAuthor: %s\nISBN: %s\nCategory: %s\nYear: %d\nCopies Available: %d / %d\nRating: %.1f ★ (%d reviews)\n\nDescription:\n%s",
                b.getTitle(), b.getAuthor(), b.getIsbn(), b.getCategory(), b.getPublishYear(), b.getAvailableCopies(), b.getTotalCopies(), b.getAvgRating(), b.getTotalReviews(), b.getDescription());
            
            // Custom dialog panel
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            JTextArea textArea = new JTextArea(details);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textArea.setBackground(new Color(245, 247, 252));
            textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(450, 300));
            panel.add(scrollPane, BorderLayout.CENTER);

            // Action buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnBorrowPop = new JButton("Borrow Book");
            btnBorrowPop.setBackground(new Color(27, 58, 107));
            btnBorrowPop.setForeground(Color.WHITE);
            btnBorrowPop.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            JButton btnCancel = new JButton("Close");
            btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            buttonPanel.add(btnCancel);
            buttonPanel.add(btnBorrowPop);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            JDialog dialog = new JDialog(view, "Book Details", true);
            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(view);

            btnCancel.addActionListener(e -> dialog.dispose());
            btnBorrowPop.addActionListener(e -> {
                dialog.dispose();
                borrowSelected(); // Reuse existing borrow logic
            });

            dialog.setVisible(true);
        }

        private void reviewSelected() {
            Book b = getSelectedBook();
            if (b == null) return;
            navigateTo(new ReviewsUI()); // they can write a review on the reviews page
        }

        private void navigateTo(javax.swing.JFrame target) {
            view.dispose();
            Router.route(target);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  2.  ReviewsController
    // ══════════════════════════════════════════════════════════════════════
    public static class ReviewsController {
        private final ReviewsUI view;
        private List<Borrowing> borrowings;
        private List<Review>    currentReviews;
        private int selectedBookId = -1;

        public ReviewsController(ReviewsUI view) {
            this.view = view;
            loadBorrowings();
            wireNavigation();
            wireActions();
        }

        private void loadBorrowings() {
            borrowings = data.getBorrowingHistory(Session.getMemberId());
            view.populateBorrowings(borrowings);
        }

        private void loadReviews(int bookId) {
            selectedBookId = bookId;
            currentReviews = data.getReviewsForBook(bookId);
            view.populateReviews(currentReviews);
            Book b = data.getBookById(bookId);
            if (b != null) view.lblSelectedBook.setText("Writing review for: " + b.getTitle());
        }

        private void wireActions() {
            view.tblBorrowings.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = view.tblBorrowings.getSelectedRow();
                    if (row >= 0 && row < borrowings.size()) {
                        loadReviews(borrowings.get(row).getBookId());
                    }
                }
            });

            view.btnSubmitReview.addActionListener(e -> {
                if (selectedBookId == -1) {
                    JOptionPane.showMessageDialog(view,
                        "Please select a book from your borrowing history first.",
                        "No Book Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (view.txtComment.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(view,
                        "Please write a comment before submitting.",
                        "Empty Comment", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int rating = (int) view.spinRating.getValue();
                String comment = view.txtComment.getText().trim();
                if (data.hasReviewed(Session.getMemberId(), selectedBookId)) {
                    JOptionPane.showMessageDialog(view,
                        "You have already reviewed this book. Delete your existing review first to re-submit.",
                        "Already Reviewed", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int res = data.addReview(Session.getMemberId(), selectedBookId, rating, comment);
                if (res > 0) {
                    JOptionPane.showMessageDialog(view,
                        "Review submitted successfully! ★".repeat(rating),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    view.txtComment.setText("");
                    view.spinRating.setValue(5);
                    loadReviews(selectedBookId);
                } else {
                    JOptionPane.showMessageDialog(view,
                        "Failed to submit review. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            view.btnDeleteReview.addActionListener(e -> {
                if (selectedBookId == -1) {
                    JOptionPane.showMessageDialog(view,
                        "Please select a book first.", "No Book Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Find the current user's review for the selected book
                if (currentReviews == null || currentReviews.isEmpty()) {
                    JOptionPane.showMessageDialog(view,
                        "No reviews to delete for the selected book.",
                        "No Reviews", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int myMemberId = Session.getMemberId();
                Review myReview = null;
                for (Review r : currentReviews) {
                    if (r.getMemberId() == myMemberId) { myReview = r; break; }
                }
                if (myReview == null) {
                    JOptionPane.showMessageDialog(view,
                        "You have not reviewed this book yet.",
                        "No Review Found", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(view,
                    "Delete your review for this book?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
                if (data.deleteReview(myReview.getReviewId())) {
                    JOptionPane.showMessageDialog(view,
                        "Your review has been deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadReviews(selectedBookId);
                } else {
                    JOptionPane.showMessageDialog(view,
                        "Failed to delete review.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        private void wireNavigation() {
            view.btnDashboard.addActionListener(e -> navigateTo(new ui()));
            view.btnMyBorrowings.addActionListener(e -> navigateTo(new MyBorrowingsUI()));
            view.btnBookSearch.addActionListener(e -> navigateTo(new BookSearchUI()));
            view.btnWishlist.addActionListener(e -> navigateTo(new WishlistMGMT()));
            view.btnFinePayment.addActionListener(e -> navigateTo(new FinePayment()));
            view.btnMyAccount.addActionListener(e -> navigateTo(new EditAccountPage()));
            if (view.btnInventory != null) view.btnInventory.addActionListener(e -> navigateTo(new AdminInventoryUI()));
            if (view.btnUsers != null) view.btnUsers.addActionListener(e -> navigateTo(new AdminUsersUI()));
            if (view.btnReports != null) view.btnReports.addActionListener(e -> navigateTo(new AdminReportsUI()));
            view.btnSignOut.addActionListener(e -> logOut(view));
        }

        private void navigateTo(javax.swing.JFrame target) { view.dispose(); Router.route(target); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  3.  AdminInventoryController
    // ══════════════════════════════════════════════════════════════════════
    public static class AdminInventoryController {
        private final AdminInventoryUI view;
        private List<Book> books;
        private int selectedBookId = -1;

        public AdminInventoryController(AdminInventoryUI view) {
            this.view = view;
            loadBooks();
            wireNavigation();
            wireActions();
        }

        private void loadBooks() {
            books = data.getAllBooks();
            view.populateTable(books);
        }

        private void wireActions() {
            view.tblBooks.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = view.tblBooks.getSelectedRow();
                    if (row >= 0 && row < books.size()) {
                        Book b = books.get(row);
                        selectedBookId = b.getBookId();
                        view.fillForm(b);
                    }
                }
            });

            view.btnRefresh.addActionListener(e -> loadBooks());
            view.btnClearForm.addActionListener(e -> { view.clearForm(); selectedBookId = -1; view.tblBooks.clearSelection(); });

            view.btnAdd.addActionListener(e -> {
                try {
                    int tc = Integer.parseInt(view.txtCopies.getText().trim());
                    int py = view.txtYear.getText().trim().isEmpty() ? 0 : Integer.parseInt(view.txtYear.getText().trim());
                    int pg = view.txtPages.getText().trim().isEmpty() ? 0 : Integer.parseInt(view.txtPages.getText().trim());
                    if (view.txtTitle.getText().trim().isEmpty() || view.txtAuthor.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(view, "Title and Author are mandatory.", "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int id = data.addBook(view.txtTitle.getText().trim(), view.txtAuthor.getText().trim(), view.txtIsbn.getText().trim(),
                        view.txtCategory.getText().trim(), view.txtDesc.getText().trim(), tc, pg, view.txtPublisher.getText().trim(), py);
                    if (id > 0) {
                        JOptionPane.showMessageDialog(view, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadBooks(); view.clearForm();
                    } else JOptionPane.showMessageDialog(view, "Failed to add book.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(view, "Please enter valid numbers for Copies, Pages and Year.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            view.btnUpdate.addActionListener(e -> {
                if (selectedBookId == -1) {
                    JOptionPane.showMessageDialog(view, "Select a book to update.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    int tc = Integer.parseInt(view.txtCopies.getText().trim());
                    int py = view.txtYear.getText().trim().isEmpty() ? 0 : Integer.parseInt(view.txtYear.getText().trim());
                    int pg = view.txtPages.getText().trim().isEmpty() ? 0 : Integer.parseInt(view.txtPages.getText().trim());
                    boolean ok = data.updateBook(selectedBookId, view.txtTitle.getText().trim(), view.txtAuthor.getText().trim(), view.txtIsbn.getText().trim(),
                        view.txtCategory.getText().trim(), view.txtDesc.getText().trim(), tc, pg, view.txtPublisher.getText().trim(), py);
                    if (ok) {
                        JOptionPane.showMessageDialog(view, "Book updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadBooks();
                    } else JOptionPane.showMessageDialog(view, "Failed to update book.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(view, "Please enter valid numbers for Copies, Pages and Year.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            view.btnDelete.addActionListener(e -> {
                if (selectedBookId == -1) return;
                int c = JOptionPane.showConfirmDialog(view, "Delete this book completely?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    if (data.deleteBook(selectedBookId)) {
                        JOptionPane.showMessageDialog(view, "Book deleted.");
                        loadBooks(); view.clearForm(); selectedBookId = -1;
                    }
                }
            });
        }

        private void wireNavigation() {
            view.btnDashboard.addActionListener(e -> navigateTo(new ui()));
            view.btnMyBorrowings.addActionListener(e -> navigateTo(new MyBorrowingsUI()));
            view.btnBookSearch.addActionListener(e -> navigateTo(new BookSearchUI()));
            view.btnWishlist.addActionListener(e -> navigateTo(new WishlistMGMT()));
            view.btnReviews.addActionListener(e -> navigateTo(new ReviewsUI()));
            view.btnFinePayment.addActionListener(e -> navigateTo(new FinePayment()));
            view.btnMyAccount.addActionListener(e -> navigateTo(new EditAccountPage()));
            view.btnUsers.addActionListener(e -> navigateTo(new AdminUsersUI()));
            view.btnReports.addActionListener(e -> navigateTo(new AdminReportsUI()));
            view.btnSignOut.addActionListener(e -> logOut(view));
        }
        private void navigateTo(javax.swing.JFrame target) { view.dispose(); Router.route(target); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  4.  AdminUsersController
    // ══════════════════════════════════════════════════════════════════════
    public static class AdminUsersController {
        private final AdminUsersUI view;
        private List<Member> members;

        public AdminUsersController(AdminUsersUI view) {
            this.view = view;
            loadMembers();
            wireNavigation();
            wireActions();
        }

        private void loadMembers() {
            members = data.getAllMembers();
            view.populateTable(members);
        }

        private void wireActions() {
            view.btnRefresh.addActionListener(e -> loadMembers());
            view.btnActivate.addActionListener(e -> setStatus(true));
            view.btnDeactivate.addActionListener(e -> setStatus(false));
            
            view.btnSearch.addActionListener(e -> {
                String q = view.txtSearch.getText().toLowerCase().trim();
                List<Member> filtered = new java.util.ArrayList<>();
                for(Member m : data.getAllMembers()) {
                    if (m.getFullName().toLowerCase().contains(q) || m.getEmail().toLowerCase().contains(q)) filtered.add(m);
                }
                members = filtered;
                view.populateTable(members);
            });
        }

        private void setStatus(boolean active) {
            int row = view.tblMembers.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(view, "Select a user first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = members.get(row).getMemberId();
            if (id == Session.getMemberId()) {
                JOptionPane.showMessageDialog(view, "You cannot deactivate your own account.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (data.setMemberActive(id, active)) {
                JOptionPane.showMessageDialog(view, "User status updated to " + (active ? "Active" : "Inactive") + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMembers();
            } else JOptionPane.showMessageDialog(view, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        private void wireNavigation() {
            view.btnDashboard.addActionListener(e -> navigateTo(new ui()));
            view.btnMyBorrowings.addActionListener(e -> navigateTo(new MyBorrowingsUI()));
            view.btnBookSearch.addActionListener(e -> navigateTo(new BookSearchUI()));
            view.btnWishlist.addActionListener(e -> navigateTo(new WishlistMGMT()));
            view.btnReviews.addActionListener(e -> navigateTo(new ReviewsUI()));
            view.btnFinePayment.addActionListener(e -> navigateTo(new FinePayment()));
            view.btnMyAccount.addActionListener(e -> navigateTo(new EditAccountPage()));
            view.btnInventory.addActionListener(e -> navigateTo(new AdminInventoryUI()));
            view.btnReports.addActionListener(e -> navigateTo(new AdminReportsUI()));
            view.btnSignOut.addActionListener(e -> logOut(view));
        }
        private void navigateTo(javax.swing.JFrame target) { view.dispose(); Router.route(target); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  5.  AdminReportsController
    // ══════════════════════════════════════════════════════════════════════
    public static class AdminReportsController {
        private final AdminReportsUI view;

        public AdminReportsController(AdminReportsUI view) {
            this.view = view;
            loadData();
            wireNavigation();
        }

        private void loadData() {
            view.populateMostBorrowed(data.getMostBorrowedBooks(20));
            view.populateTopUsers(data.getTopActiveUsers(20));
            view.populateLogs(data.getActivityLogs(50));

            // Real stats from the database
            view.lblTotalBooks.setText(String.valueOf(data.getAllBooks().size()));
            view.lblTotalMembers.setText(String.valueOf(data.getAllMembers().size()));
            // Fixed: use getTotalBorrowingCount() — not activity log count
            view.lblTotalBorrowings.setText(String.valueOf(data.getTotalBorrowingCount()));
        }

        private void wireNavigation() {
            view.btnRefreshAll.addActionListener(e -> loadData());
            view.btnExportCSV.addActionListener(e -> JOptionPane.showMessageDialog(view, "Export to CSV successful (simulated).", "Export", JOptionPane.INFORMATION_MESSAGE));
            
            view.btnDashboard.addActionListener(e -> navigateTo(new ui()));
            view.btnMyBorrowings.addActionListener(e -> navigateTo(new MyBorrowingsUI()));
            view.btnBookSearch.addActionListener(e -> navigateTo(new BookSearchUI()));
            view.btnWishlist.addActionListener(e -> navigateTo(new WishlistMGMT()));
            view.btnReviews.addActionListener(e -> navigateTo(new ReviewsUI()));
            view.btnFinePayment.addActionListener(e -> navigateTo(new FinePayment()));
            view.btnMyAccount.addActionListener(e -> navigateTo(new EditAccountPage()));
            view.btnInventory.addActionListener(e -> navigateTo(new AdminInventoryUI()));
            view.btnUsers.addActionListener(e -> navigateTo(new AdminUsersUI()));
            view.btnSignOut.addActionListener(e -> logOut(view));
        }
        private void navigateTo(javax.swing.JFrame target) { view.dispose(); Router.route(target); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Helper Router Methods
    // ══════════════════════════════════════════════════════════════════════

    public static void logOut(javax.swing.JFrame view) {
        int c = JOptionPane.showConfirmDialog(view, "Are you sure you want to sign out?", "Sign Out", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            Session.clear();
            view.dispose();
            LMS_Login_Page loginPage = new LMS_Login_Page();
            new control(loginPage);
            loginPage.setVisible(true);
        }
    }

    public static class Router {
        public static void route(javax.swing.JFrame target) {
            if (target instanceof ui) {
                new NewControllers2.DashboardController((ui) target);
            } else if (target instanceof MyBorrowingsUI) {
                new controller.MyBorrowingsController((MyBorrowingsUI) target);
            } else if (target instanceof BookSearchUI) {
                new BookSearchController((BookSearchUI) target);
            } else if (target instanceof WishlistMGMT) {
                new controller.WishlistController((WishlistMGMT) target);
            } else if (target instanceof ReviewsUI) {
                new ReviewsController((ReviewsUI) target);
            } else if (target instanceof FinePayment) {
                new NewControllers2.FinePaymentController((FinePayment) target);
            } else if (target instanceof EditAccountPage) {
                new NewControllers2.EditAccountController((EditAccountPage) target);
            } else if (target instanceof AdminInventoryUI) {
                // AdminInventoryController is now instantiated inside the view's constructor for native run support
            } else if (target instanceof AdminUsersUI) {
                // AdminUsersController is now instantiated inside the view's constructor
            } else if (target instanceof AdminReportsUI) {
                // AdminReportsController is now instantiated inside the view's constructor
            }
            target.setVisible(true);
        }
    }
}
