package view;

import utils.Session;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/** AdminReportsUI – Most borrowed books, top users, activity logs (faculty only). */
public class AdminReportsUI extends JFrame {

    public JButton btnDashboard, btnMyBorrowings, btnBookSearch, btnWishlist;
    public JButton btnReviews, btnFinePayment, btnMyAccount;
    public JButton btnInventory, btnUsers, btnReports;
    public JButton btnSignOut;

    public JTable            tblMostBorrowed;
    public DefaultTableModel modelMostBorrowed;
    public JTable            tblTopUsers;
    public DefaultTableModel modelTopUsers;
    public JTable            tblLogs;
    public DefaultTableModel modelLogs;

    public JButton btnRefreshAll, btnExportCSV;
    public JLabel  lblTotalBooks, lblTotalMembers, lblTotalBorrowings;

    private static final Color BG_DARK    = new Color(244, 246, 250);
    private static final Color SIDEBAR_BG = new Color(27, 58, 107);
    private static final Color ACCENT     = new Color(27, 58, 107);
    private static final Color ACCENT_DARK= new Color(18, 40, 75);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color TEXT_DARK  = new Color(26, 33, 51);
    private static final Color TEXT_LIGHT = Color.WHITE;
    private static final Color TEXT_MID   = new Color(75, 85, 99);
    private static final Color DANGER     = new Color(239, 68, 68);

    public AdminReportsUI() {
        setTitle("Reports & Analytics – LibraryMS Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
        new controller.NewControllers.AdminReportsController(this);
    }

    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setBackground(SIDEBAR_BG);
        p.setPreferredSize(new Dimension(230, 800));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        JLabel logo = new JLabel("📚 LibraryMS");
        logo.setForeground(TEXT_LIGHT); logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(logo); p.add(Box.createVerticalStrut(30));
        btnDashboard    = sBtn(" Dashboard", "/images/colourful_house-removebg-preview (1).png");
        btnMyBorrowings = sBtn(" My Borrowings", "/images/colourful_borrowing-removebg-pre.png");
        btnBookSearch   = sBtn(" Book Search", "/images/colourful_search-removebg-previe.png");
        btnWishlist     = sBtn(" Wishlist", "/images/colourful_wishlist-removebg-prev.png");
        btnReviews      = sBtn(" Reviews", "/images/colourful_reviews-removebg-previ.png");
        btnFinePayment  = sBtn(" Fine Payment", "/images/colourful_fine-removebg-preview (1).png");
        btnMyAccount    = sBtn(" My Account", "/images/colorful_my_account-removebg-pre.png");
        for (JButton b : new JButton[]{btnDashboard, btnMyBorrowings, btnBookSearch,
                btnWishlist, btnReviews, btnFinePayment, btnMyAccount}) {
            p.add(b); p.add(Box.createVerticalStrut(6));
        }
        p.add(Box.createVerticalStrut(16));
        JLabel al = new JLabel("  ADMIN"); al.setForeground(TEXT_MID);
        al.setFont(new Font("Segoe UI", Font.BOLD, 11));
        p.add(al); p.add(Box.createVerticalStrut(6));
        btnInventory = sBtn(" Inventory", "/images/colourful_inv_98-removebg-previe.png");
        btnUsers     = sBtn(" Users", "/images/colourful_users-removebg-preview (1).png");
        btnReports   = sBtn(" Reports", "/images/colourful_reports-removebg-previ.png");
        btnReports.setBackground(ACCENT_DARK);
        btnReports.setForeground(TEXT_LIGHT);
        for (JButton b : new JButton[]{btnInventory, btnUsers, btnReports}) {
            p.add(b); p.add(Box.createVerticalStrut(6));
        }
        p.add(Box.createVerticalGlue());
        btnSignOut = sBtn(" Sign Out", null);
        btnSignOut.setBackground(new Color(200, 30, 30)); btnSignOut.setForeground(Color.WHITE);
        p.add(btnSignOut);
        return p;
    }

    private JPanel buildMain() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Title + buttons
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_DARK);
        JLabel title = new JLabel("📊 Reports & Analytics");
        title.setForeground(TEXT_DARK); title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        topBar.add(title, BorderLayout.WEST);
        JPanel topBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topBtns.setBackground(BG_DARK);
        btnRefreshAll = styledBtn("🔄 Refresh",     new Color(71, 85, 105));
        btnExportCSV  = styledBtn("📥 Export CSV",  ACCENT);
        topBtns.add(btnRefreshAll); topBtns.add(btnExportCSV);
        topBar.add(topBtns, BorderLayout.EAST);
        p.add(topBar, BorderLayout.NORTH);

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        statsRow.setBackground(BG_DARK);
        lblTotalBooks     = statCard("Total Books",     "0", statsRow);
        lblTotalMembers   = statCard("Total Members",   "0", statsRow);
        lblTotalBorrowings= statCard("Total Borrowings","0", statsRow);
        p.add(statsRow, BorderLayout.CENTER);

        // Three tables side-by-side
        JPanel tables = new JPanel(new GridLayout(1, 3, 14, 0));
        tables.setBackground(BG_DARK);

        modelMostBorrowed = new DefaultTableModel(new String[]{"Title","Author","Borrows"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblMostBorrowed = styledTable(modelMostBorrowed);
        tables.add(wrapTable("🏆 Most Borrowed Books", tblMostBorrowed));

        modelTopUsers = new DefaultTableModel(new String[]{"Name","Email","Borrows"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTopUsers = styledTable(modelTopUsers);
        tables.add(wrapTable("🧑 Top Active Users", tblTopUsers));

        modelLogs = new DefaultTableModel(new String[]{"ID","User","Action","Details","Time"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLogs = styledTable(modelLogs);
        tables.add(wrapTable("📋 Activity Logs", tblLogs));

        p.add(tables, BorderLayout.SOUTH);
        return p;
    }

    private JLabel statCard(String label, String value, JPanel parent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_MID); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel val = new JLabel(value);
        val.setForeground(TEXT_DARK); val.setFont(new Font("Segoe UI", Font.BOLD, 32));
        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        parent.add(card);
        return val;
    }

    private JPanel wrapTable(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG_DARK);
        JLabel lbl = new JLabel(title);
        lbl.setForeground(TEXT_MID); lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(lbl, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setPreferredSize(new Dimension(350, 300));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    public void populateMostBorrowed(List<String[]> rows) {
        modelMostBorrowed.setRowCount(0);
        for (String[] r : rows) modelMostBorrowed.addRow(r);
    }

    public void populateTopUsers(List<String[]> rows) {
        modelTopUsers.setRowCount(0);
        for (String[] r : rows) modelTopUsers.addRow(r);
    }

    public void populateLogs(List<String[]> rows) {
        modelLogs.setRowCount(0);
        for (String[] r : rows) modelLogs.addRow(r);
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(CARD_BG); t.setForeground(TEXT_DARK);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12)); t.setRowHeight(26);
        t.setShowGrid(true); t.setGridColor(new Color(240, 240, 240));
        t.setSelectionBackground(new Color(230, 240, 255));
        t.setSelectionForeground(TEXT_DARK);
        t.getTableHeader().setBackground(new Color(245, 245, 245));
        t.getTableHeader().setForeground(TEXT_MID);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        return t;
    }

    private JButton sBtn(String text, String iconPath) {
        JButton b = new JButton(text);
        if (iconPath != null) {
            java.net.URL imgURL = getClass().getResource(iconPath);
            if (imgURL != null) b.setIcon(new ImageIcon(imgURL));
        }
        b.setMaximumSize(new Dimension(210, 38)); b.setPreferredSize(new Dimension(210, 38));
        b.setBackground(SIDEBAR_BG); b.setForeground(new Color(200, 215, 235));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        return b;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return b;
    }
}
