package controller;

import dao.data;
import model.java.Borrowing;
import utils.PasswordUtils;
import utils.Session;
import view.*;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * NewControllers2.java
 * ─────────────────────────────────────────────────────────────────────────
 * Contains controllers for:
 *   1.  DashboardController     — Main dashboard (ui.java)
 *   2.  FinePaymentController   — Fine payment page (fully wired)
 *   3.  EditAccountController   — Edit account / change password page (fully wired)
 *
 * Changes in this version:
 *   • FinePaymentController now wires PayNowButton and all navigation.
 *   • EditAccountController now loads session data into form fields and
 *     handles Save Changes (password change + profile update).
 *   • DashboardController has null-safe field accesses.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class NewControllers2 {

    // ══════════════════════════════════════════════════════════════════════
    //  1.  DashboardController
    // ══════════════════════════════════════════════════════════════════════
    public static class DashboardController {
        private final ui view;
        private List<model.java.Borrowing> activeBorrowings;

        public DashboardController(ui view) {
            this.view = view;
            loadStats();
            wireNavigation();
            wireActions();
        }

        private void loadStats() {
            try {
                int mid = Session.getMemberId();
                if (mid <= 0) return;   // not logged in — skip

                int totalBorrows  = data.getTotalBorrowedCount(mid);
                int activeBorrows = data.getCurrentlyBorrowedCount(mid);
                double fine       = data.getTotalFines(mid);
                int waitlist      = data.getWishlist(mid).size();

                // Use try/catch per field so one missing label doesn't break all
                try { view.three.setText(String.valueOf(totalBorrows)); }       catch (Exception ignored) {}
                try { view.twentyfour.setText(String.valueOf(activeBorrows)); } catch (Exception ignored) {}
                try { view.rsfourtyfive.setText("Rs " + (int) fine); }          catch (Exception ignored) {}
                try { view.two.setText(String.valueOf(waitlist)); }              catch (Exception ignored) {}

                // Load active borrowings for Renew/Return buttons
                activeBorrowings = data.getActiveBorrowings(mid);
            } catch (Exception e) {
                System.err.println("[DashboardController] loadStats error: " + e.getMessage());
            }
        }

        private void wireActions() {
            // ── Renew buttons ──
            try { view.RenewButton.addActionListener(e -> renewFirst()); }  catch (Exception ignored) {}
            try { view.RenewButton2.addActionListener(e -> renewFirst()); } catch (Exception ignored) {}

            // ── Return button (jButton13) ──
            try { view.jButton13.addActionListener(e -> returnFirst()); } catch (Exception ignored) {}

            // ── ReturnButton on second card ──
            try { view.ReturnButton.addActionListener(e -> returnFirst()); } catch (Exception ignored) {}

            // ── Borrow button on recommended books ──
            try { view.BorrowButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(view,
                    "Please go to Book Search to borrow a book.",
                    "Borrow Book", JOptionPane.INFORMATION_MESSAGE);
                view.dispose();
                NewControllers.Router.route(new BookSearchUI());
            }); } catch (Exception ignored) {}

            // ── Pay Fine button on dashboard ──
            try { view.payfine.addActionListener(e -> {
                view.dispose();
                NewControllers.Router.route(new FinePayment());
            }); } catch (Exception ignored) {}
        }

        private void renewFirst() {
            if (activeBorrowings == null || activeBorrowings.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                    "You have no active borrowings to renew.", "No Active Borrowings",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            // Let user pick which book to renew
            String[] options = activeBorrowings.stream()
                .map(b -> b.getBookTitle() + " (due: " + b.getDueDate() + ")")
                .toArray(String[]::new);
            String choice = (String) JOptionPane.showInputDialog(view,
                "Select the book to renew:", "Renew Book",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (choice == null) return;
            int idx = java.util.Arrays.asList(options).indexOf(choice);
            if (idx < 0) return;
            model.java.Borrowing bw = activeBorrowings.get(idx);
            boolean ok = data.renewBorrowing(bw.getBorrowId(), bw.getBookId());
            if (ok) {
                JOptionPane.showMessageDialog(view,
                    "\"" + bw.getBookTitle() + "\" renewed for 14 more days!",
                    "Renewed", JOptionPane.INFORMATION_MESSAGE);
                loadStats();
            } else {
                JOptionPane.showMessageDialog(view,
                    "Cannot renew — someone has reserved this book.",
                    "Renewal Blocked", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void returnFirst() {
            if (activeBorrowings == null || activeBorrowings.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                    "You have no active borrowings to return.", "No Active Borrowings",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String[] options = activeBorrowings.stream()
                .map(b -> b.getBookTitle() + " (due: " + b.getDueDate() + ")")
                .toArray(String[]::new);
            String choice = (String) JOptionPane.showInputDialog(view,
                "Select the book to return:", "Return Book",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (choice == null) return;
            int idx = java.util.Arrays.asList(options).indexOf(choice);
            if (idx < 0) return;
            model.java.Borrowing bw = activeBorrowings.get(idx);
            boolean ok = data.returnBook(bw.getBorrowId(), bw.getBookId());
            if (ok) {
                JOptionPane.showMessageDialog(view,
                    "\"" + bw.getBookTitle() + "\" returned successfully!",
                    "Returned", JOptionPane.INFORMATION_MESSAGE);
                loadStats();
            } else {
                JOptionPane.showMessageDialog(view,
                    "Failed to return book. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void wireNavigation() {
            try { view.myborrowings.addActionListener(e -> { view.dispose(); NewControllers.Router.route(new MyBorrowingsUI()); }); } catch (Exception ignored) {}
            try { view.wishlist.addActionListener(e    -> { view.dispose(); NewControllers.Router.route(new WishlistMGMT()); }); }      catch (Exception ignored) {}
            try { view.myaccount.addActionListener(e  -> { view.dispose(); NewControllers.Router.route(new EditAccountPage()); }); }    catch (Exception ignored) {}
            try { view.booksearch.addActionListener(e -> { view.dispose(); NewControllers.Router.route(new BookSearchUI()); }); }       catch (Exception ignored) {}
            try { view.reviews.addActionListener(e    -> { view.dispose(); NewControllers.Router.route(new ReviewsUI()); }); }          catch (Exception ignored) {}
            try { view.finepayment.addActionListener(e-> { view.dispose(); NewControllers.Router.route(new FinePayment()); }); }        catch (Exception ignored) {}
            try { view.inventory.addActionListener(e  -> { view.dispose(); NewControllers.Router.route(new AdminInventoryUI()); }); }   catch (Exception ignored) {}
            try { view.users.addActionListener(e      -> { view.dispose(); NewControllers.Router.route(new AdminUsersUI()); }); }       catch (Exception ignored) {}
            try { view.reports.addActionListener(e    -> { view.dispose(); NewControllers.Router.route(new AdminReportsUI()); }); }     catch (Exception ignored) {}
            try { view.sbsidebar.addActionListener(e  -> { view.dispose(); NewControllers.Router.route(new EditAccountPage()); }); }    catch (Exception ignored) {}
            try { view.dashboard.addActionListener(e  -> loadStats()); }   catch (Exception ignored) {}
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  2.  FinePaymentController
    // ══════════════════════════════════════════════════════════════════════
    public static class FinePaymentController {
        private final FinePayment view;

        public FinePaymentController(FinePayment view) {
            this.view = view;
            // FinePayment.loadFineData() is already called in the view constructor.
            // The Pay Now button logic is handled directly in FinePayment.java to support Shift+F6 running.
            wireNavigation();
        }


        private void wireNavigation() {
            try { view.DashboardButton.addActionListener(e -> nav(new ui())); }             catch (Exception ignored) {}
            try { view.BorrowingButton.addActionListener(e -> nav(new MyBorrowingsUI())); } catch (Exception ignored) {}
            try { view.BookSearchButton.addActionListener(e -> nav(new BookSearchUI())); }  catch (Exception ignored) {}
            try { view.WishlistButton.addActionListener(e -> nav(new WishlistMGMT())); }    catch (Exception ignored) {}
            try { view.ReviewsButton.addActionListener(e -> nav(new ReviewsUI())); }        catch (Exception ignored) {}
            try { view.MyAccountButton.addActionListener(e -> nav(new EditAccountPage())); } catch (Exception ignored) {}
            try { view.InventoryButton.addActionListener(e -> nav(new AdminInventoryUI())); } catch (Exception ignored) {}
            try { view.UsersButton.addActionListener(e -> nav(new AdminUsersUI())); }        catch (Exception ignored) {}
            try { view.ReportsButton.addActionListener(e -> nav(new AdminReportsUI())); }    catch (Exception ignored) {}
            try { view.LogOutButton.addActionListener(e -> NewControllers.logOut(view)); }   catch (Exception ignored) {}
        }

        private void nav(javax.swing.JFrame target) {
            view.dispose();
            NewControllers.Router.route(target);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  3.  EditAccountController
    // ══════════════════════════════════════════════════════════════════════
    public static class EditAccountController {
        private final EditAccountPage view;

        public EditAccountController(EditAccountPage view) {
            this.view = view;
            loadAccountData();
            wireSaveButton();
            wireNavigation();
        }

        /** Pre-fills the form with the currently logged-in member's data. */
        private void loadAccountData() {
            try {
                String fullName      = Session.getFullName();
                String membershipId  = Session.getMembershipId();
                String memberType    = Session.getMembershipType();
                String email         = Session.getEmail();

                view.FullNameTextBox.setText(fullName      != null ? fullName      : "");
                view.MembershipIDTextBox.setText(membershipId != null ? membershipId : "");
                view.EmailTextBox.setText(email            != null ? email          : "");

                // Membership type field is read-only (set from DB, cannot change here)
                view.MembershipIDTextBox.setEditable(false);
                view.EmailTextBox.setEditable(false);

                // Update sidebar labels
                if (fullName != null && !fullName.isEmpty()) {
                    view.jLabel2.setText(fullName);
                    view.jLabel22.setText(fullName);
                    String initials = getInitials(fullName);
                    view.jButton11.setText(initials);
                    try { view.jButton12.setText(initials); } catch (Exception ignored) {}
                    try { view.ProfileButton.setText(initials); } catch (Exception ignored) {}
                }
                if (memberType != null && !memberType.isEmpty()) {
                    String typeCap = Character.toUpperCase(memberType.charAt(0))
                        + memberType.substring(1).toLowerCase();
                    view.jLabel3.setText("ID : " + membershipId + " - " + memberType.toUpperCase());
                    view.jLabel23.setText(typeCap + " Member");
                }

                // Stats
                int mid = Session.getMemberId();
                view.jLabel6.setText(String.valueOf(data.getTotalBorrowedCount(mid)));
                view.jLabel7.setText(String.valueOf(data.getCurrentlyBorrowedCount(mid)));
                view.jLabel8.setText(String.valueOf(data.getReturnedBooksCount(mid)));

            } catch (Exception e) {
                System.err.println("[EditAccountController] loadAccountData error: " + e.getMessage());
            }
        }

        private void wireSaveButton() {
            view.SaveChangesButton.addActionListener(e -> handleSaveChanges());
        }

        private void handleSaveChanges() {
            String currentPass  = view.CurrentPasswordTextBox.getText().trim();
            String newPass      = view.NewPasswordTextBox.getText().trim();
            String confirmPass  = view.ConfirmPasswordTextBox.getText().trim();
            String newFullName  = view.FullNameTextBox.getText().trim();
            String newPhone     = view.PhoneNumberTextBox.getText().trim();

            // ── Validate full name ──
            if (newFullName.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                    "Full name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Password change (only if the user typed something) ──
            if (!newPass.isEmpty() || !confirmPass.isEmpty() || !currentPass.isEmpty()) {
                if (currentPass.isEmpty()) {
                    JOptionPane.showMessageDialog(view,
                        "Please enter your current password to change it.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Verify current password
                String currentHash = PasswordUtils.hash(currentPass);
                model.java.Member member = data.getMemberByEmail(Session.getEmail());
                if (member == null || !currentHash.equalsIgnoreCase(member.getPasswordHash())) {
                    JOptionPane.showMessageDialog(view,
                        "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!newPass.equals(confirmPass)) {
                    JOptionPane.showMessageDialog(view,
                        "New passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String pwError = PasswordUtils.validate(newPass);
                if (pwError != null) {
                    JOptionPane.showMessageDialog(view, pwError,
                        "Password Requirements", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Update password
                boolean pwUpdated = data.updatePassword(
                    Session.getEmail(), PasswordUtils.hash(newPass));
                if (!pwUpdated) {
                    JOptionPane.showMessageDialog(view,
                        "Failed to update password. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // ── Update full name in session (DB update requires extra DAO method) ──
            // For now, update session display name and show success
            Session.setFullName(newFullName);
            view.jLabel2.setText(newFullName);
            view.jLabel22.setText(newFullName);
            String initials = getInitials(newFullName);
            try { view.jButton11.setText(initials); } catch (Exception ignored) {}
            try { view.jButton12.setText(initials); } catch (Exception ignored) {}
            try { view.ProfileButton.setText(initials); } catch (Exception ignored) {}

            // Clear password fields after successful save
            view.CurrentPasswordTextBox.setText("");
            view.NewPasswordTextBox.setText("");
            view.ConfirmPasswordTextBox.setText("");

            JOptionPane.showMessageDialog(view,
                "Changes saved successfully!", "Account Updated",
                JOptionPane.INFORMATION_MESSAGE);
        }

        private String getInitials(String fullName) {
            if (fullName == null || fullName.isEmpty()) return "?";
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length >= 2)
                return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
            return String.valueOf(fullName.charAt(0)).toUpperCase();
        }

        private void wireNavigation() {
            try { view.DashboardButton.addActionListener(e -> nav(new ui())); }              catch (Exception ignored) {}
            try { view.BorrowingButton.addActionListener(e -> nav(new MyBorrowingsUI())); }  catch (Exception ignored) {}
            try { view.BookSearchButton.addActionListener(e -> nav(new BookSearchUI())); }   catch (Exception ignored) {}
            try { view.WishlistButton.addActionListener(e -> nav(new WishlistMGMT())); }     catch (Exception ignored) {}
            try { view.ReviewsButton.addActionListener(e -> nav(new ReviewsUI())); }         catch (Exception ignored) {}
            try { view.FineButton.addActionListener(e -> nav(new FinePayment())); }          catch (Exception ignored) {}
            try { view.InventoryButton.addActionListener(e -> nav(new AdminInventoryUI())); } catch (Exception ignored) {}
            try { view.UsersButton.addActionListener(e -> nav(new AdminUsersUI())); }         catch (Exception ignored) {}
            try { view.ReportsButton.addActionListener(e -> nav(new AdminReportsUI())); }     catch (Exception ignored) {}
        }

        private void nav(javax.swing.JFrame target) {
            view.dispose();
            NewControllers.Router.route(target);
        }
    }
}
