/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
<<<<<<< HEAD
=======

package controller;

import dao.data;
import model.java.Member;
import utils.OTPService;
import utils.PasswordUtils;
import utils.Session;

import view.LMS_Login_Page;
import view.Signup_Page;
import view.Forgot_password_page;
import view.MyBorrowingsUI;
import view.WishlistMGMT;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// ══════════════════════════════════════════════════════════════════════
//  1.  LoginController
// ══════════════════════════════════════════════════════════════════════
public class control {

    private final LMS_Login_Page loginView;

    public control(LMS_Login_Page loginView) {
        this.loginView = loginView;

        // Wire buttons
        loginView.Signinbutton.addActionListener(new SignInListener());
        loginView.CreateAnAccount.addActionListener(e -> openSignup());
        loginView.Forgotpasswordbutton.addActionListener(e -> openForgotPassword());
    }

    public void open()  { loginView.setVisible(true); }
    public void close() { loginView.dispose(); }

    // ── Sign-in logic ────────────────────────────────────────────────
    private class SignInListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email    = loginView.EmailTextField.getText().trim();
            String password = loginView.PasswordTextField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(loginView,
                    "Please enter your email and password.", "Login",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            String hash   = PasswordUtils.hash(password);
            Member member = data.loginMember(email, hash);

            if (member == null) {
                JOptionPane.showMessageDialog(loginView,
                    "Invalid email or password. Please try again.", "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Store session
            Session.setMemberId(member.getMemberId());
            Session.setEmail(member.getEmail());
            Session.setFullName(member.getFullName());
            Session.setMembershipId(member.getMembershipId());
            Session.setMembershipType(member.getMembershipType());
            Session.setRole(member.getRole());

            close();

            // Open the main application screen (My Borrowings by default)
            MyBorrowingsUI borrowingsView = new MyBorrowingsUI();
            new MyBorrowingsController(borrowingsView);
            borrowingsView.setVisible(true);
        }
    }

    // ── Navigation helpers ───────────────────────────────────────────
    private void openSignup() {
        close();
        Signup_Page signupView = new Signup_Page();
        new SignupController(signupView, loginView);
        signupView.setVisible(true);
    }

    private void openForgotPassword() {
        loginView.setVisible(false);
        Forgot_password_page forgotView = new Forgot_password_page();
        new ForgotPasswordController(forgotView, loginView);
        forgotView.setVisible(true);
    }
}


// ══════════════════════════════════════════════════════════════════════
//  2.  SignupController
// ══════════════════════════════════════════════════════════════════════
class SignupController {

    private final Signup_Page signupView;
    private final LMS_Login_Page loginView;

    public SignupController(Signup_Page signupView, LMS_Login_Page loginView) {
        this.signupView = signupView;
        this.loginView  = loginView;

        // Wire buttons
        signupView.CreateAccountButton.addActionListener(e -> handleSignup());
        signupView.SignInButton.addActionListener(e -> backToLogin());

        // Membership type toggle — only one can be selected at a time
        signupView.StudentMembership.addActionListener(e -> {
            signupView.FactultyMembership.setSelected(false);
            signupView.PublicMembership.setSelected(false);
        });
        signupView.FactultyMembership.addActionListener(e -> {
            signupView.StudentMembership.setSelected(false);
            signupView.PublicMembership.setSelected(false);
        });
        signupView.PublicMembership.addActionListener(e -> {
            signupView.StudentMembership.setSelected(false);
            signupView.FactultyMembership.setSelected(false);
        });
    }

    private void handleSignup() {
        String firstName        = signupView.NameTextField.getText().trim();
        String membershipId     = signupView.MembershipIDTextField.getText().trim();
        String email            = signupView.EmailTextField.getText().trim();
        String phone            = signupView.PhNumberTextField.getText().trim();
        String password         = signupView.PasswordTextField.getText().trim();
        String confirmPassword  = signupView.ConfirmPasswordTextField.getText().trim();
        String membershipType   = getSelectedMembershipType();

        // ── Validation ────────────────────────────────────────────────
        if (firstName.isEmpty() || membershipId.isEmpty() ||
            email.isEmpty()     || phone.isEmpty()        ||
            password.isEmpty()  || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(signupView,
                "Please fill in all fields.", "Signup", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (membershipType == null) {
            JOptionPane.showMessageDialog(signupView,
                "Please select a membership type (Student / Faculty / Public).",
                "Signup", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!signupView.TermsAndServicesBox.isSelected()) {
            JOptionPane.showMessageDialog(signupView,
                "You must agree to the Terms of Service to continue.",
                "Signup", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(signupView,
                "Passwords do not match.", "Signup", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pwError = PasswordUtils.validate(password);
        if (pwError != null) {
            JOptionPane.showMessageDialog(signupView,
                pwError, "Password Requirements", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (data.emailExists(email)) {
            JOptionPane.showMessageDialog(signupView,
                "An account with that email address already exists.",
                "Signup", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (data.membershipIdExists(membershipId)) {
            JOptionPane.showMessageDialog(signupView,
                "That Membership ID is already registered.",
                "Signup", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Register ──────────────────────────────────────────────────
        // Split name: first word = first name, rest = last name
        String[] parts    = firstName.split("\\s+", 2);
        String   fName    = parts[0];
        String   lName    = parts.length > 1 ? parts[1] : "";
        String   hash     = PasswordUtils.hash(password);

        int newId = data.registerMember(fName, lName, email, hash,
                                        phone, membershipId, membershipType);
        if (newId > 0) {
            JOptionPane.showMessageDialog(signupView,
                "Account created successfully! Please sign in.", "Welcome",
                JOptionPane.INFORMATION_MESSAGE);
            signupView.dispose();
            loginView.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(signupView,
                "Registration failed. Please try again.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedMembershipType() {
        if (signupView.StudentMembership.isSelected())   return "student";
        if (signupView.FactultyMembership.isSelected())  return "faculty";
        if (signupView.PublicMembership.isSelected())    return "public";
        return null;
    }

    private void backToLogin() {
        signupView.dispose();
        loginView.setVisible(true);
    }
}


// ══════════════════════════════════════════════════════════════════════
//  3.  ForgotPasswordController
// ══════════════════════════════════════════════════════════════════════
class ForgotPasswordController {

    private final Forgot_password_page forgotView;
    private final LMS_Login_Page       loginView;

    /** True once the OTP has been sent — the same button then verifies. */
    private boolean otpSent   = false;
    private String  otpTarget = "";   // email to which OTP was sent

    public ForgotPasswordController(Forgot_password_page forgotView,
                                    LMS_Login_Page loginView) {
        this.forgotView = forgotView;
        this.loginView  = loginView;

        forgotView.SendOTp.addActionListener(e -> handleSendOrVerify());
        forgotView.ResendOTP.addActionListener(e -> resendOTP());
        forgotView.BackToLoginButton.addActionListener(e -> backToLogin());
    }

    private void handleSendOrVerify() {
        if (!otpSent) {
            sendOTP();
        } else {
            verifyOTP();
        }
    }

    private void sendOTP() {
        String email = forgotView.EmailTextField.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(forgotView,
                "Please enter your registered email address.", "Reset Password",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (data.getMemberByEmail(email) == null) {
            JOptionPane.showMessageDialog(forgotView,
                "No account found with that email address.", "Not Found",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            utils.EmailSender.sendOTP(email);
            otpTarget = email;
            otpSent   = true;
            forgotView.SendOTp.setText("VERIFY OTP ->");
            forgotView.RegisterLabel.setText("ENTER OTP");
            forgotView.EmailTextField.setText("");
            forgotView.EmailTextField.setEnabled(false);
            JOptionPane.showMessageDialog(forgotView,
                "OTP sent to " + email + ". Check your inbox.", "OTP Sent",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            // During development without email, print OTP to console
            String otp = OTPService.generateOTP(email);
            otpTarget  = email;
            otpSent    = true;
            forgotView.SendOTp.setText("VERIFY OTP ->");
            forgotView.RegisterLabel.setText("ENTER OTP");
            forgotView.EmailTextField.setText("");
            forgotView.EmailTextField.setEnabled(false);
            System.out.println("[DEV] OTP for " + email + " → " + otp);
            JOptionPane.showMessageDialog(forgotView,
                "OTP generated (email unavailable). Check console for OTP code.",
                "OTP Ready", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resendOTP() {
        if (otpTarget.isEmpty()) {
            JOptionPane.showMessageDialog(forgotView,
                "Please enter your email first.", "Resend OTP",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        OTPService.clearOTP(otpTarget);
        otpSent = false;
        forgotView.EmailTextField.setEnabled(true);
        forgotView.EmailTextField.setText(otpTarget);
        forgotView.SendOTp.setText("SEND OTP ->");
        forgotView.RegisterLabel.setText("REGISTERED EMAIL");
        sendOTP();
    }

    private void verifyOTP() {
        String entered = forgotView.EmailTextField.getText().trim();
        if (entered.isEmpty()) {
            JOptionPane.showMessageDialog(forgotView,
                "Please enter the OTP you received.", "Verify OTP",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!OTPService.verifyOTP(otpTarget, entered)) {
            JOptionPane.showMessageDialog(forgotView,
                "Invalid or expired OTP. Please try again.", "OTP Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // OTP verified — prompt for new password
        String newPassword = javax.swing.JOptionPane.showInputDialog(forgotView,
            "OTP verified! Enter your new password:");
        if (newPassword == null || newPassword.trim().isEmpty()) return;

        String pwError = PasswordUtils.validate(newPassword.trim());
        if (pwError != null) {
            JOptionPane.showMessageDialog(forgotView,
                pwError, "Password Requirements", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String confirmPassword = javax.swing.JOptionPane.showInputDialog(forgotView,
            "Confirm your new password:");
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(forgotView,
                "Passwords do not match.", "Password Reset", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean updated = data.updatePassword(otpTarget, PasswordUtils.hash(newPassword.trim()));
        if (updated) {
            JOptionPane.showMessageDialog(forgotView,
                "Password reset successfully! Please sign in with your new password.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            backToLogin();
        } else {
            JOptionPane.showMessageDialog(forgotView,
                "Failed to update password. Please try again.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backToLogin() {
        forgotView.dispose();
        loginView.setVisible(true);
    }
}


// ══════════════════════════════════════════════════════════════════════
//  4.  MyBorrowingsController
// ══════════════════════════════════════════════════════════════════════
class MyBorrowingsController {

    private final MyBorrowingsUI view;

    public MyBorrowingsController(MyBorrowingsUI view) {
        this.view = view;
        loadMemberInfo();
        loadBorrowingHistory();
        wireNavigation();
    }

    /** Populates the profile panel with live session data. */
    private void loadMemberInfo() {
        String name = Session.getFullName();
        String mid  = Session.getMembershipId();
        String type = capitalize(Session.getMembershipType());

        view.jLabel2.setText(name.isEmpty() ? "Member" : name);
        view.jLabel3.setText("ID : " + mid + " - " + type.toUpperCase());
        view.jLabel22.setText(name.isEmpty() ? "Member" : name);
        view.jLabel23.setText(type + " Member");

        // Profile button initials
        String[] parts    = name.split("\\s+");
        String   initials = "";
        if (parts.length >= 2)
            initials = ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        else if (parts.length == 1 && !parts[0].isEmpty())
            initials = String.valueOf(parts[0].charAt(0)).toUpperCase();
        if (!initials.isEmpty()) {
            view.ProfileButton.setText(initials);
            view.jButton11.setText(initials);
            view.jButton12.setText(initials);
        }

        // Stats
        int total     = data.getTotalBorrowedCount(Session.getMemberId());
        int returned  = data.getReturnedBooksCount(Session.getMemberId());
        int active    = data.getCurrentlyBorrowedCount(Session.getMemberId());
        view.jLabel6.setText(String.valueOf(total));
        view.jLabel7.setText(String.valueOf(active));
        view.jLabel8.setText(String.valueOf(returned));
    }

    /**
     * Loads borrowing history rows into the table labels.
     * The UI currently shows 9 static rows; we populate as many as the DB returns.
     */
    private void loadBorrowingHistory() {
        java.util.List<model.java.Borrowing> history =
            data.getBorrowingHistory(Session.getMemberId());

        javax.swing.JLabel[] bookLabels = {
            view.BookName1, view.BookName2, view.BookName3,
            view.BookName4, view.BookName5, view.BookName6,
            view.BookName7, view.BookName8, view.BookName9
        };
        javax.swing.JLabel[] authorLabels = {
            view.AuthorName1, view.AuthorName2, view.AuthorName3,
            view.AuthorName4, view.AuthorName5, view.AuthorName6,
            view.AuthorName7, view.AuthorName8, view.AuthorName9
        };
        javax.swing.JLabel[] borrowedLabels = {
            view.BorrowedDate1, view.BorrowedDate2, view.BorrowedDate3,
            view.BorrowedDate4, view.BorrowedDate5, view.BorrowedDate6,
            view.BorrowedDate7, view.BorrowedDate8, view.BorrowedDate10
        };
        javax.swing.JLabel[] returnedLabels = {
            view.ReturnedDate1, view.ReturnedDate2, view.ReturnedDate3,
            view.ReturnedDate4, view.ReturnedDate5, view.ReturnedDate6,
            view.ReturnedDate7, view.ReturnedDate8, view.ReturnedDate9
        };
        java.awt.Label[] statusLabels = {
            view.ActiveStatus1, view.ActiveStatus2, view.ActiveStatus3,
            view.ReturnedDate, view.ReturnedStatus2, view.ReturnedStatus3,
            view.ReturnedStatus4, view.OverdueStatus2, view.OverDueLabel
        };
        java.awt.Label[] fineLabels = { view.PriceTag, view.PriceTag2 };

        int limit = Math.min(history.size(), bookLabels.length);
        for (int i = 0; i < limit; i++) {
            model.java.Borrowing bw = history.get(i);
            bookLabels[i].setText(bw.getBookTitle()   != null ? bw.getBookTitle()   : "Unknown");
            authorLabels[i].setText(bw.getBookAuthor() != null ? bw.getBookAuthor() : "");
            borrowedLabels[i].setText(bw.getBorrowDate() != null ? bw.getBorrowDate().toString() : "-");
            returnedLabels[i].setText(bw.getReturnDate() != null ? bw.getReturnDate().toString() : "-");
            if (i < statusLabels.length) {
                statusLabels[i].setText(capitalize(bw.getStatus()));
                colorStatus(statusLabels[i], bw.getStatus());
            }
        }

        // Populate fine labels with total fine
        double totalFine = data.getTotalFines(Session.getMemberId());
        if (fineLabels.length > 0 && totalFine > 0)
            fineLabels[0].setText("Rs " + (int) totalFine);
    }

    /** Wires all sidebar navigation buttons. */
    private void wireNavigation() {
        // Sidebar navigation
        view.DashboardButton.addActionListener(e -> navigateToBorrowings());
        view.BorrowingButton.addActionListener(e -> navigateToBorrowings());
        view.WishlistButton.addActionListener(e  -> navigateToWishlist());
        view.BorrowingHistoryButton.addActionListener(e -> navigateToBorrowings());
        view.WishlistButton2.addActionListener(e -> navigateToWishlist());

        // Search bar
        view.SearchBar.addActionListener(e -> {
            String query = view.SearchBar.getText().trim();
            if (!query.isEmpty() && !query.equals("Search books, authors, ISBN...."))
                JOptionPane.showMessageDialog(view,
                    "Search for: \"" + query + "\"\n(Open BookSearch screen to see results.)",
                    "Search", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void navigateToBorrowings() {
        // Already on borrowings — refresh
        loadBorrowingHistory();
    }

    private void navigateToWishlist() {
        view.dispose();
        WishlistMGMT wishlistView = new WishlistMGMT();
        new WishlistController(wishlistView);
        wishlistView.setVisible(true);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private static void colorStatus(java.awt.Label label, String status) {
        if (status == null) return;
        switch (status.toLowerCase()) {
            case "active":
                label.setForeground(new java.awt.Color(255, 153, 51));   break;
            case "overdue":
                label.setForeground(new java.awt.Color(255, 51, 51));    break;
            case "returned": case "paid":
                label.setForeground(new java.awt.Color(0, 135, 90));     break;
            default:
                label.setForeground(java.awt.Color.GRAY);
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
//  5.  WishlistController
// ══════════════════════════════════════════════════════════════════════
class WishlistController {

    private final WishlistMGMT view;
    private java.util.List<model.java.WishlistItem> wishlistItems;

    public WishlistController(WishlistMGMT view) {
        this.view = view;
        loadMemberInfo();
        loadWishlist();
        wireNavigation();
    }

    private void loadMemberInfo() {
        String name = Session.getFullName();
        String mid  = Session.getMembershipId();
        String type = Session.getMembershipType();

        view.jLabel2.setText(name.isEmpty() ? "Member" : name);
        view.jLabel3.setText("ID : " + mid + " - " + type.toUpperCase());
        view.jLabel22.setText(name.isEmpty() ? "Member" : name);
        view.jLabel23.setText(
            type.isEmpty() ? "Member" :
            (Character.toUpperCase(type.charAt(0)) + type.substring(1).toLowerCase() + " Member"));

        String[] parts    = name.split("\\s+");
        String   initials = "";
        if (parts.length >= 2)
            initials = ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        else if (parts.length == 1 && !parts[0].isEmpty())
            initials = String.valueOf(parts[0].charAt(0)).toUpperCase();
        if (!initials.isEmpty()) {
            view.ProfileButton.setText(initials);
            view.jButton11.setText(initials);
            view.jButton12.setText(initials);
        }

        // Stats
        int total    = data.getTotalBorrowedCount(Session.getMemberId());
        int active   = data.getCurrentlyBorrowedCount(Session.getMemberId());
        int returned = data.getReturnedBooksCount(Session.getMemberId());
        view.jLabel6.setText(String.valueOf(total));
        view.jLabel7.setText(String.valueOf(active));
        view.jLabel8.setText(String.valueOf(returned));
    }

    /** Populates the 6 wishlist book panels from the database. */
    private void loadWishlist() {
        wishlistItems = data.getWishlist(Session.getMemberId());

        // Arrays mirror the 6 book panels in WishlistMGMT
        javax.swing.JLabel[] bookNames  = { view.BookName1, view.BookName2, view.BookName3,
                                             view.BookName4, view.BookName5, view.BookName6 };
        javax.swing.JLabel[] authors    = { view.AuthorName1, view.AuthorName2, view.AuthorName3,
                                             view.AuthorName4, view.AuthorName5, view.AuthorName6 };
        java.awt.Label[]    available   = { view.AvailableLabel, view.AvailableLabel1,
                                             view.AvailableLabel2, view.AvailableLabel3,
                                             view.AvailableLabel4, view.AvailableLabel5 };
        javax.swing.JButton[] borrowBtns = { view.BorrowNowButton1, view.BorrowNowButton2,
                                              view.BorrowNowButton3, view.BorrowNowButton4,
                                              view.BorrowNowButton5, view.BorrowNowButton6 };

        // Update the title label with actual count
        view.jLabel17.setText("My Wishlist ( " + wishlistItems.size() + " Books )");

        int limit = Math.min(wishlistItems.size(), bookNames.length);
        for (int i = 0; i < limit; i++) {
            model.java.WishlistItem item = wishlistItems.get(i);
            bookNames[i].setText(item.getBookTitle()  != null ? item.getBookTitle()  : "Unknown");
            authors[i].setText(item.getBookAuthor()   != null ? item.getBookAuthor() : "");

            if (item.isAvailable()) {
                available[i].setText("Available");
                available[i].setForeground(new java.awt.Color(153, 255, 153));
                borrowBtns[i].setEnabled(true);
                borrowBtns[i].setBackground(new java.awt.Color(27, 58, 107));
            } else {
                available[i].setText("Checked Out");
                available[i].setForeground(new java.awt.Color(255, 51, 51));
                borrowBtns[i].setEnabled(false);
                borrowBtns[i].setBackground(new java.awt.Color(204, 204, 204));
            }

            final int index = i;
            // Remove listener duplicates by creating per-item references
            borrowBtns[i].addActionListener(e -> handleBorrowNow(index));
        }

        // Hide panels that have no wishlist items
        javax.swing.JPanel[] panels = { view.BookPanel1, view.BookPanel2, view.BookPanel3,
                                         view.BookPanel4, view.BookPanel5, view.BookPanel6 };
        for (int i = limit; i < panels.length; i++) panels[i].setVisible(false);
    }

    private void handleBorrowNow(int index) {
        if (index >= wishlistItems.size()) return;
        model.java.WishlistItem item = wishlistItems.get(index);

        int confirm = JOptionPane.showConfirmDialog(view,
            "Borrow \"" + item.getBookTitle() + "\" now?",
            "Confirm Borrow", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int borrowId = data.borrowBook(Session.getMemberId(), item.getBookId());
        if (borrowId > 0) {
            JOptionPane.showMessageDialog(view,
                "\"" + item.getBookTitle() + "\" has been borrowed!\n" +
                "Return it within 14 days to avoid fines.",
                "Borrowed", JOptionPane.INFORMATION_MESSAGE);
            loadWishlist();  // refresh to update availability
        } else {
            JOptionPane.showMessageDialog(view,
                "Could not borrow the book. Please try again.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void wireNavigation() {
        view.DashboardButton.addActionListener(e  -> navigateToBorrowings());
        view.BorrowingButton.addActionListener(e  -> navigateToBorrowings());
        view.WishlistButton.addActionListener(e   -> loadWishlist());   // refresh
        view.WishlistButton2.addActionListener(e  -> loadWishlist());
        view.BorrowingButton2.addActionListener(e -> navigateToBorrowings());

        view.SearchBar.addActionListener(e -> {
            String query = view.SearchBar.getText().trim();
            if (!query.isEmpty() && !query.equals("Search books, authors, ISBN...."))
                JOptionPane.showMessageDialog(view,
                    "Search for: \"" + query + "\"",
                    "Search", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void navigateToBorrowings() {
        view.dispose();
        MyBorrowingsUI borrowingsView = new MyBorrowingsUI();
        new MyBorrowingsController(borrowingsView);
        borrowingsView.setVisible(true);
    }
}
>>>>>>> e9e35153e47632ee72ec00dad9fc7f869ac5d26d
