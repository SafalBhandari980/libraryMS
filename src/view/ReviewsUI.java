package view;

import model.java.Borrowing;
import model.java.Review;
import utils.Session;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * ReviewsUI – Submit and view book reviews/ratings.
 * Styled to match the main project design (navy sidebar, light main panel).
 */
public class ReviewsUI extends JFrame {

    // ── Sidebar navigation buttons (public for controller access) ──
    public JButton btnDashboard, btnMyBorrowings, btnBookSearch, btnWishlist;
    public JButton btnReviews, btnFinePayment, btnMyAccount;
    public JButton btnInventory, btnUsers, btnReports;
    public JButton btnSignOut;

    // ── Review panel components ──
    public JTable           tblBorrowings;
    public DefaultTableModel tblBorrowModel;
    public JTable           tblReviews;
    public DefaultTableModel tblReviewModel;

    public JSpinner  spinRating;
    public JTextArea txtComment;
    public JButton   btnSubmitReview;
    public JButton   btnDeleteReview;
    public JLabel    lblSelectedBook;

    // ── Colours matching the project design system ──
    private static final Color SIDEBAR_BG   = new Color(27, 58, 107);   // navy
    private static final Color CONTENT_BG   = new Color(244, 246, 250); // light grey page bg
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color ACCENT_DARK  = new Color(20, 45, 90);    // darker navy (active btn)
    private static final Color TEXT_LIGHT   = Color.WHITE;
    private static final Color TEXT_DARK    = new Color(26, 33, 51);
    private static final Color TEXT_MID     = new Color(180, 200, 230);  // sidebar muted
    private static final Color ACCENT       = new Color(99, 102, 241);   // indigo highlight
    private static final Color SUCCESS      = new Color(34, 197, 94);
    private static final Color DANGER       = new Color(220, 38, 38);
    private static final Color ORANGE       = new Color(251, 146, 60);

    public ReviewsUI() {
        setTitle("Reviews & Ratings – LibraryMS");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Sidebar
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setBackground(SIDEBAR_BG);
        p.setPreferredSize(new Dimension(230, 800));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Logo
        JLabel logo = new JLabel(" LibraryMS");
        java.net.URL logoUrl = getClass().getResource("/images/library_1-removebg-preview (2).png");
        if (logoUrl != null) logo.setIcon(new ImageIcon(logoUrl));
        logo.setForeground(TEXT_LIGHT);
        logo.setFont(new Font("Segoe UI Black", Font.BOLD, 18));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(logo);
        p.add(Box.createVerticalStrut(10));

        JLabel memberLbl = new JLabel("  MEMBER");
        memberLbl.setForeground(TEXT_MID);
        memberLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        p.add(memberLbl);
        p.add(Box.createVerticalStrut(8));

        btnDashboard    = sBtn(" Dashboard",    "/images/colourful_house-removebg-preview (1).png");
        btnMyBorrowings = sBtn(" My Borrowings","/images/colourful_borrowing-removebg-pre.png");
        btnBookSearch   = sBtn(" Book Search",  "/images/colourful_search-removebg-previe.png");
        btnWishlist     = sBtn(" Wishlist",     "/images/colourful_wishlist-removebg-prev.png");
        btnReviews      = sBtn(" Reviews",      "/images/colourful_reviews-removebg-previ.png");
        btnFinePayment  = sBtn(" Fine Payment", "/images/colourful_fine-removebg-preview (1).png");
        btnMyAccount    = sBtn(" My Account",   "/images/colorful_my_account-removebg-pre.png");

        // Highlight active page
        btnReviews.setBackground(ACCENT_DARK);
        btnReviews.setForeground(TEXT_LIGHT);
        btnReviews.setBorder(new CompoundBorder(
            new MatteBorder(0, 3, 0, 0, SUCCESS),
            BorderFactory.createEmptyBorder(0, 9, 0, 0)
        ));

        for (JButton b : new JButton[]{btnDashboard, btnMyBorrowings, btnBookSearch,
                btnWishlist, btnReviews, btnFinePayment, btnMyAccount}) {
            p.add(b); p.add(Box.createVerticalStrut(6));
        }

        // Admin section
        if ("faculty".equalsIgnoreCase(Session.getMembershipType())) {
            p.add(Box.createVerticalStrut(12));
            JLabel adminLbl = new JLabel("  ADMIN"); adminLbl.setForeground(TEXT_MID);
            adminLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            p.add(adminLbl); p.add(Box.createVerticalStrut(6));
            btnInventory = sBtn(" Inventory", "/images/colourful_inv_98-removebg-previe.png");
            btnUsers     = sBtn(" Users",     "/images/colourful_users-removebg-preview (1).png");
            btnReports   = sBtn(" Reports",   "/images/colourful_reports-removebg-previ.png");
            for (JButton b : new JButton[]{btnInventory, btnUsers, btnReports}) {
                p.add(b); p.add(Box.createVerticalStrut(6));
            }
        }

        p.add(Box.createVerticalGlue());

        // User card
        String name = Session.getFullName();
        String type = Session.getMembershipType();
        String initials = (name != null && name.length() >= 2) ? name.substring(0,2).toUpperCase() : "?";
        JPanel userCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userCard.setBackground(SIDEBAR_BG);
        userCard.setMaximumSize(new Dimension(210, 50));
        JButton avatar = new JButton(initials);
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setBackground(new Color(99, 102, 241));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setBorderPainted(false); avatar.setFocusPainted(false);
        JPanel namePanel = new JPanel();
        namePanel.setBackground(SIDEBAR_BG);
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        JLabel nameLbl = new JLabel(name != null ? name : "User");
        nameLbl.setForeground(TEXT_LIGHT); nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel typeLbl = new JLabel(type != null ? type : "Member");
        typeLbl.setForeground(TEXT_MID); typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        namePanel.add(nameLbl); namePanel.add(typeLbl);
        userCard.add(avatar); userCard.add(namePanel);
        p.add(userCard);
        p.add(Box.createVerticalStrut(8));

        btnSignOut = sBtn(" Sign Out", null);
        btnSignOut.setBackground(new Color(200, 30, 30));
        btnSignOut.setForeground(Color.WHITE);
        p.add(btnSignOut);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  Main content
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildMain() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(CONTENT_BG);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));
        JLabel title = new JLabel("⭐  Reviews & Ratings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);
        topBar.add(title, BorderLayout.WEST);
        p.add(topBar, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setBackground(CONTENT_BG);
        body.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        body.add(buildBorrowingPanel());
        body.add(buildReviewPanel());
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildBorrowingPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel lbl = new JLabel("📖  Select a Book to Review");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(TEXT_DARK);
        card.add(lbl, BorderLayout.NORTH);

        JLabel hint = new JLabel("Click any row from your borrowing history to load its reviews.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(new Color(100, 120, 150));

        String[] cols = {"Book Title", "Author", "Borrowed On", "Status"};
        tblBorrowModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblBorrowings = styledTable(tblBorrowModel);

        JScrollPane sp = new JScrollPane(tblBorrowings);
        sp.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        sp.getViewport().setBackground(CARD_BG);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBackground(CARD_BG);
        center.add(hint, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildReviewPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel lbl1 = new JLabel("💬  Reviews for Selected Book");
        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl1.setForeground(TEXT_DARK);
        card.add(lbl1, BorderLayout.NORTH);

        // Reviews table
        String[] revCols = {"Reviewer", "Rating", "Comment", "Date"};
        tblReviewModel = new DefaultTableModel(revCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblReviews = styledTable(tblReviewModel);
        JScrollPane sp = new JScrollPane(tblReviews);
        sp.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        sp.getViewport().setBackground(CARD_BG);
        card.add(sp, BorderLayout.CENTER);

        // Submit form
        JPanel form = new JPanel();
        form.setBackground(new Color(248, 250, 252));
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        lblSelectedBook = new JLabel("← Select a book from the left panel");
        lblSelectedBook.setForeground(new Color(100, 120, 160));
        lblSelectedBook.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        form.add(lblSelectedBook);
        form.add(Box.createVerticalStrut(10));

        JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ratingRow.setBackground(new Color(248, 250, 252));
        JLabel rLbl = new JLabel("Your Rating:");
        rLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rLbl.setForeground(TEXT_DARK);
        spinRating = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        spinRating.setPreferredSize(new Dimension(60, 30));
        spinRating.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel starLbl = new JLabel("★ (1–5 stars)");
        starLbl.setForeground(new Color(251, 191, 36));
        starLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ratingRow.add(rLbl); ratingRow.add(spinRating); ratingRow.add(starLbl);
        form.add(ratingRow);
        form.add(Box.createVerticalStrut(8));

        JLabel commentLbl = new JLabel("Your Comment:");
        commentLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        commentLbl.setForeground(TEXT_DARK);
        form.add(commentLbl);
        form.add(Box.createVerticalStrut(4));

        txtComment = new JTextArea(4, 30);
        txtComment.setBackground(Color.WHITE);
        txtComment.setForeground(TEXT_DARK);
        txtComment.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        txtComment.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane commentScroll = new JScrollPane(txtComment);
        commentScroll.setBorder(BorderFactory.createEmptyBorder());
        form.add(commentScroll);
        form.add(Box.createVerticalStrut(10));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(new Color(248, 250, 252));
        btnSubmitReview = actionBtn("⭐  Submit Review", ORANGE);
        btnDeleteReview = actionBtn("🗑  Delete My Review", DANGER);
        btnRow.add(btnSubmitReview); btnRow.add(btnDeleteReview);
        form.add(btnRow);

        card.add(form, BorderLayout.SOUTH);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════
    //  Public populate methods (called by ReviewsController)
    // ══════════════════════════════════════════════════════════════════
    public void populateBorrowings(List<Borrowing> list) {
        tblBorrowModel.setRowCount(0);
        for (Borrowing b : list) {
            tblBorrowModel.addRow(new Object[]{
                b.getBookTitle(), b.getBookAuthor(),
                b.getBorrowDate() != null ? b.getBorrowDate().toString() : "",
                b.getStatus()
            });
        }
    }

    public void populateReviews(List<Review> list) {
        tblReviewModel.setRowCount(0);
        for (Review r : list) {
            String stars = "★".repeat(r.getRating()) + "☆".repeat(5 - r.getRating());
            tblReviewModel.addRow(new Object[]{
                r.getMemberName(), stars, r.getComment(),
                r.getReviewDate() != null ? r.getReviewDate().toString() : ""
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════════════
    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(CARD_BG);
        t.setForeground(TEXT_DARK);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(ACCENT);
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);
        JTableHeader header = t.getTableHeader();
        header.setBackground(new Color(244, 246, 250));
        header.setForeground(new Color(70, 90, 130));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        return t;
    }

    private JButton sBtn(String text, String iconPath) {
        JButton b = new JButton(text);
        if (iconPath != null) {
            java.net.URL imgURL = getClass().getResource(iconPath);
            if (imgURL != null) b.setIcon(new ImageIcon(imgURL));
        }
        b.setMaximumSize(new Dimension(210, 38)); b.setPreferredSize(new Dimension(210, 38));
        b.setBackground(SIDEBAR_BG); b.setForeground(TEXT_MID);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        return b;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return b;
    }
}
