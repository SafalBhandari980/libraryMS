package view;

import dao.data;
import model.java.Book;
import utils.Session;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * BookSearchUI – Premium redesigned book search page matching the LibraryMS design system.
 */
public class BookSearchUI extends JFrame {

    // ── Sidebar nav buttons ──
    public JButton btnDashboard, btnMyBorrowings, btnBookSearch, btnWishlist;
    public JButton btnReviews, btnFinePayment, btnMyAccount;
    public JButton btnInventory, btnUsers, btnReports;
    public JButton btnSignOut;

    // ── Top bar ──
    public JTextField txtTopSearch;
    public JButton    btnNotification;

    // ── Search & filter ──
    public JTextField        txtSearch;
    public JButton           btnSearch;
    public JComboBox<String> cmbCategory;
    public JCheckBox         chkAvailableOnly;
    public JTextField        txtYear;
    public JButton           btnFilter;
    public JButton           btnClearFilter;

    // ── Results table ──
    public JTable            tblBooks;
    public DefaultTableModel tblModel;

    // ── Action buttons ──
    public JButton btnBorrow, btnReserve, btnAddWishlist, btnViewDetails, btnAddReview;

    // ── Design tokens (matching the rest of the app) ──
    private static final Color BG_DARK     = new Color(27,  58,  107); // main sidebar blue
    private static final Color SIDEBAR_BG  = new Color(20,  45,  85);
    private static final Color CONTENT_BG  = Color.WHITE;
    private static final Color ACCENT      = new Color(27,  58,  107);
    private static final Color ACCENT_DARK = new Color(18,  40,  75);
    private static final Color CARD_BG     = new Color(245, 247, 252);
    private static final Color TEXT_DARK   = new Color(30,  40,  60);
    private static final Color TEXT_MID    = new Color(122, 134, 154);
    private static final Color TEXT_LIGHT  = Color.WHITE;
    private static final Color SUCCESS     = new Color(34,  197, 94);
    private static final Color DANGER      = new Color(255, 51,  51);
    private static final Color WARNING     = new Color(255, 167, 38);
    private static final Color PURPLE      = new Color(139, 92,  246);
    private static final Color PINK        = new Color(236, 72,  153);
    private static final Color ORANGE      = new Color(249, 115, 22);

    public BookSearchUI() {
        setTitle("Book Search – LibraryMS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CONTENT_BG);

        add(buildSidebar(),  BorderLayout.WEST);
        add(buildContent(),  BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setBackground(BG_DARK);
        p.setPreferredSize(new Dimension(220, 800));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(24, 12, 16, 12));

        // Logo
        JLabel logo = new JLabel("📚 LibraryMs");
        logo.setForeground(TEXT_LIGHT);
        logo.setFont(new Font("Segoe UI Black", Font.BOLD, 18));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(logo);
        p.add(Box.createVerticalStrut(6));

        JLabel section = new JLabel("MEMBER");
        section.setForeground(new Color(180, 200, 230));
        section.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        section.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(section);
        p.add(Box.createVerticalStrut(20));

        // Nav items
        btnDashboard    = sidebarBtn(" Dashboard",         "/images/colourful_house-removebg-preview (1).png", false);
        btnMyBorrowings = sidebarBtn(" My Borrowings",     "/images/colourful_borrowing-removebg-pre.png", false);
        btnBookSearch   = sidebarBtn(" Book Search",       "/images/colourful_search-removebg-previe.png", true);   // active
        btnWishlist     = sidebarBtn(" Wishlist",           "/images/colourful_wishlist-removebg-prev.png", false);
        btnReviews      = sidebarBtn(" Reviews",           "/images/colourful_reviews-removebg-previ.png", false);
        btnFinePayment  = sidebarBtn(" Fine Payment",      "/images/colourful_fine-removebg-preview (1).png", false);
        btnMyAccount    = sidebarBtn(" My Account",        "/images/colorful_my_account-removebg-pre.png", false);

        for (JButton b : new JButton[]{btnDashboard, btnMyBorrowings, btnBookSearch,
                btnWishlist, btnReviews, btnFinePayment, btnMyAccount}) {
            p.add(b); p.add(Box.createVerticalStrut(4));
        }

        // Admin section (faculty only)
        boolean isFaculty = "faculty".equalsIgnoreCase(Session.getMembershipType());
        if (isFaculty) {
            p.add(Box.createVerticalStrut(14));
            JLabel adminLbl = new JLabel("  ADMIN");
            adminLbl.setForeground(new Color(180, 200, 230));
            adminLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            adminLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(adminLbl);
            p.add(Box.createVerticalStrut(4));

            btnInventory = sidebarBtn(" Inventory", "/images/colourful_inv_98-removebg-previe.png", false);
            btnUsers     = sidebarBtn(" Users",     "/images/colourful_users-removebg-preview (1).png", false);
            btnReports   = sidebarBtn(" Reports",   "/images/colourful_reports-removebg-previ.png", false);
            for (JButton b : new JButton[]{btnInventory, btnUsers, btnReports}) {
                p.add(b); p.add(Box.createVerticalStrut(4));
            }
        }

        // User card at bottom
        p.add(Box.createVerticalGlue());

        String name = Session.getFullName();
        String type = Session.getMembershipType();
        String initials = getInitials(name != null ? name : "U");

        JPanel userCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userCard.setBackground(ACCENT_DARK);
        userCard.setMaximumSize(new Dimension(196, 54));
        userCard.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton avatar = new JButton(initials);
        avatar.setBackground(SUCCESS);
        avatar.setForeground(TEXT_LIGHT);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setBorderPainted(false); avatar.setFocusPainted(false);
        userCard.add(avatar);

        JPanel namePanel = new JPanel();
        namePanel.setBackground(ACCENT_DARK);
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        JLabel nameLbl = new JLabel(name != null ? name : "Member");
        nameLbl.setForeground(TEXT_LIGHT);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel typeLbl = new JLabel(type != null ? type.substring(0,1).toUpperCase() + type.substring(1) + " Member" : "Member");
        typeLbl.setForeground(new Color(180, 200, 230));
        typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        namePanel.add(nameLbl); namePanel.add(typeLbl);
        userCard.add(namePanel);
        p.add(userCard);

        p.add(Box.createVerticalStrut(8));
        btnSignOut = sidebarBtn(" Sign Out", null, false);
        btnSignOut.setBackground(new Color(200, 30, 30));
        btnSignOut.setForeground(TEXT_LIGHT);
        p.add(btnSignOut);

        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  MAIN CONTENT
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(CONTENT_BG);

        p.add(buildTopBar(),       BorderLayout.NORTH);
        p.add(buildMainArea(),     BorderLayout.CENTER);

        return p;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(CONTENT_BG);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(220, 225, 235)),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        JLabel title = new JLabel("Book Search");
        title.setForeground(TEXT_DARK);
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 20));
        bar.add(title, BorderLayout.WEST);

        // Right: search bar + profile
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(CONTENT_BG);

        txtTopSearch = new JTextField("Search books, authors, ISBN...");
        txtTopSearch.setPreferredSize(new Dimension(320, 36));
        txtTopSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTopSearch.setForeground(TEXT_MID);
        txtTopSearch.setBackground(CARD_BG);
        txtTopSearch.setBorder(new CompoundBorder(
            new LineBorder(new Color(210, 215, 230), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        right.add(txtTopSearch);

        btnNotification = new JButton("🔔");
        btnNotification.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnNotification.setBackground(CARD_BG);
        btnNotification.setForeground(TEXT_MID);
        btnNotification.setBorderPainted(false);
        btnNotification.setFocusPainted(false);
        btnNotification.setPreferredSize(new Dimension(42, 36));
        right.add(btnNotification);

        String initials = getInitials(Session.getFullName() != null ? Session.getFullName() : "SB");
        JButton profileBtn = new JButton(initials);
        profileBtn.setBackground(SUCCESS);
        profileBtn.setForeground(TEXT_LIGHT);
        profileBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        profileBtn.setPreferredSize(new Dimension(40, 36));
        profileBtn.setBorderPainted(false); profileBtn.setFocusPainted(false);
        right.add(profileBtn);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildMainArea() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Sub-nav tabs (matching the page style)
        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabRow.setBackground(CONTENT_BG);
        for (String tab : new String[]{"Borrowing History", "Wishlist", "My Reviews", "Fines and Payment", "Edit Profile"}) {
            JToggleButton tb = new JToggleButton(tab);
            tb.setFont(new Font("MS PGothic", Font.BOLD, 12));
            tb.setBackground(CONTENT_BG);
            tb.setForeground(TEXT_DARK);
            tb.setBorder(new CompoundBorder(
                new LineBorder(new Color(210, 215, 230), 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
            ));
            tb.setFocusPainted(false);
            tabRow.add(tb);
        }
        p.add(tabRow, BorderLayout.NORTH);

        // Center: search + table
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(CONTENT_BG);
        center.add(buildSearchPanel(), BorderLayout.NORTH);

        JPanel tableArea = new JPanel(new BorderLayout(0, 10));
        tableArea.setBackground(CONTENT_BG);
        tableArea.add(buildTablePanel(), BorderLayout.CENTER);
        tableArea.add(buildActionBar(),  BorderLayout.SOUTH);
        center.add(tableArea, BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildSearchPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(210, 215, 230), 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Row 1: Search bar
        JPanel row1 = new JPanel(new BorderLayout(8, 0));
        row1.setBackground(CARD_BG);
        txtSearch = new JTextField("Search by title, author or ISBN...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_MID);
        txtSearch.setBackground(CONTENT_BG);
        txtSearch.setBorder(new CompoundBorder(
            new LineBorder(new Color(210, 215, 230), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        txtSearch.setPreferredSize(new Dimension(300, 36));

        btnSearch = new JButton("🔍 Search");
        btnSearch.setBackground(ACCENT);
        btnSearch.setForeground(TEXT_LIGHT);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.setBorderPainted(false); btnSearch.setFocusPainted(false);
        btnSearch.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        row1.add(txtSearch,  BorderLayout.CENTER);
        row1.add(btnSearch,  BorderLayout.EAST);
        card.add(row1);
        card.add(Box.createVerticalStrut(10));

        // Row 2: Filters
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row2.setBackground(CARD_BG);

        row2.add(filterLabel("Category:"));
        cmbCategory = new JComboBox<>(new String[]{"All"});
        cmbCategory.setBackground(CONTENT_BG);
        cmbCategory.setForeground(TEXT_DARK);
        cmbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbCategory.setBorder(new LineBorder(new Color(210, 215, 230), 1));
        cmbCategory.setPreferredSize(new Dimension(150, 32));
        row2.add(cmbCategory);

        row2.add(filterLabel("Year:"));
        txtYear = new JTextField();
        txtYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtYear.setBackground(CONTENT_BG);
        txtYear.setBorder(new CompoundBorder(
            new LineBorder(new Color(210, 215, 230), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        txtYear.setPreferredSize(new Dimension(75, 32));
        row2.add(txtYear);

        chkAvailableOnly = new JCheckBox("Available Only");
        chkAvailableOnly.setBackground(CARD_BG);
        chkAvailableOnly.setForeground(TEXT_DARK);
        chkAvailableOnly.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row2.add(chkAvailableOnly);

        btnFilter      = actionBtn("✅ Apply Filters", SUCCESS);
        btnClearFilter = actionBtn("✕ Clear",         new Color(108, 117, 130));
        row2.add(btnFilter);
        row2.add(btnClearFilter);

        card.add(row2);
        return card;
    }

    private JScrollPane buildTablePanel() {
        String[] cols = {"ID", "Title", "Author", "ISBN", "Category", "Year", "Available", "Rating", "Description"};
        tblModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblBooks = new JTable(tblModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? CONTENT_BG : CARD_BG);
                    c.setForeground(TEXT_DARK);
                }
                return c;
            }
        };
        tblBooks.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblBooks.setRowHeight(32);
        tblBooks.setShowGrid(false);
        tblBooks.setIntercellSpacing(new Dimension(0, 0));
        tblBooks.setSelectionBackground(ACCENT);
        tblBooks.setSelectionForeground(TEXT_LIGHT);
        tblBooks.setFillsViewportHeight(true);

        JTableHeader header = tblBooks.getTableHeader();
        header.setBackground(BG_DARK);
        header.setForeground(TEXT_LIGHT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createEmptyBorder());

        // Column widths
        int[] widths = {35, 250, 150, 110, 120, 60, 100, 70, 0};
        for (int i = 0; i < widths.length; i++) {
            tblBooks.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            if (widths[i] == 0) {
                tblBooks.getColumnModel().getColumn(i).setMinWidth(0);
                tblBooks.getColumnModel().getColumn(i).setMaxWidth(0);
            }
        }

        JScrollPane sp = new JScrollPane(tblBooks);
        sp.setBorder(new LineBorder(new Color(210, 215, 230), 1));
        sp.getViewport().setBackground(CONTENT_BG);
        sp.setPreferredSize(new Dimension(900, 360));
        return sp;
    }

    private JPanel buildActionBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.setBackground(CONTENT_BG);
        p.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 225, 235)));

        btnBorrow      = actionBtn("📖 Borrow",          SUCCESS);
        btnReserve     = actionBtn("🔖 Reserve / Hold",  WARNING);
        btnAddWishlist = actionBtn("❤️ Add to Wishlist", PINK);
        btnViewDetails = actionBtn("📋 View Details",    ACCENT);
        btnAddReview   = actionBtn("⭐ Write Review",    ORANGE);

        p.add(btnBorrow);
        p.add(btnReserve);
        p.add(btnAddWishlist);
        p.add(btnViewDetails);
        p.add(btnAddReview);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════════════
    private JButton sidebarBtn(String text, String iconPath, boolean active) {
        JButton b = new JButton(text);
        if (iconPath != null) {
            java.net.URL imgURL = getClass().getResource(iconPath);
            if (imgURL != null) b.setIcon(new ImageIcon(imgURL));
        }
        b.setMaximumSize(new Dimension(196, 40));
        b.setPreferredSize(new Dimension(196, 40));
        b.setBackground(active ? ACCENT_DARK : BG_DARK);
        b.setForeground(active ? TEXT_LIGHT : new Color(200, 215, 235));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        if (active) b.setBorder(new CompoundBorder(
            new MatteBorder(0, 3, 0, 0, SUCCESS),
            BorderFactory.createEmptyBorder(0, 11, 0, 0)
        ));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!active) b.setBackground(ACCENT_DARK); }
            public void mouseExited(MouseEvent e)  { if (!active) b.setBackground(BG_DARK); }
        });
        return b;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(TEXT_LIGHT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return b;
    }

    private JLabel filterLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(TEXT_MID);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[parts.length-1].charAt(0)).toUpperCase();
        return name.length() >= 2 ? name.substring(0, 2).toUpperCase() : name.toUpperCase();
    }

    /** Populate category combo from DB */
    public void loadCategories() {
        List<String> cats = data.getCategories();
        cmbCategory.removeAllItems();
        cmbCategory.addItem("All");
        for (String c : cats) cmbCategory.addItem(c);
    }

    /** Fill results table */
    public void populateTable(List<Book> books) {
        tblModel.setRowCount(0);
        for (Book b : books) {
            String avail = b.getAvailableCopies() > 0
                ? "✅ " + b.getAvailableCopies() + " avail." : "❌ Unavailable";
            String rating = b.getTotalReviews() > 0
                ? String.format("%.1f ★", b.getAvgRating()) : "N/A";
            tblModel.addRow(new Object[]{
                b.getBookId(),   // Column 0 = real bookId so the controller can look up the correct book
                b.getTitle(), b.getAuthor(),
                b.getIsbn()        != null ? b.getIsbn()        : "",
                b.getCategory()    != null ? b.getCategory()    : "",
                b.getPublishYear() > 0     ? b.getPublishYear() : "",
                avail, rating,
                b.getDescription() != null ? b.getDescription() : ""
            });
        }
    }

    public int getSelectedBookIndex() { return tblBooks.getSelectedRow(); }
}
