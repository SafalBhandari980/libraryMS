package view;

import model.java.Member;
import utils.Session;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/** AdminUsersUI – Activate/deactivate member accounts (faculty only). */
public class AdminUsersUI extends JFrame {

    public JButton btnDashboard, btnMyBorrowings, btnBookSearch, btnWishlist;
    public JButton btnReviews, btnFinePayment, btnMyAccount;
    public JButton btnInventory, btnUsers, btnReports;
    public JButton btnSignOut;

    public JTable            tblMembers;
    public DefaultTableModel tblModel;
    public JButton           btnActivate, btnDeactivate, btnRefresh;
    public JTextField        txtSearch;
    public JButton           btnSearch;

    private static final Color BG_DARK    = new Color(244, 246, 250); // MAIN BG
    private static final Color SIDEBAR_BG = new Color(27, 58, 107);
    private static final Color ACCENT     = new Color(27, 58, 107);
    private static final Color ACCENT_DARK= new Color(18, 40, 75);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color TEXT_DARK  = new Color(26, 33, 51);
    private static final Color TEXT_LIGHT = Color.WHITE;
    private static final Color TEXT_MID   = new Color(75, 85, 99);
    private static final Color SUCCESS    = new Color(34, 197, 94);
    private static final Color DANGER     = new Color(239, 68, 68);

    public AdminUsersUI() {
        setTitle("User Management – LibraryMS Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
        new controller.NewControllers.AdminUsersController(this);
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
        btnUsers.setBackground(ACCENT_DARK);
        btnUsers.setForeground(TEXT_LIGHT);
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

        JLabel title = new JLabel("👥 User Account Management");
        title.setForeground(TEXT_DARK); title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        p.add(title, BorderLayout.NORTH);

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setBackground(BG_DARK);
        txtSearch = new JTextField(20);
        txtSearch.setBackground(Color.WHITE); txtSearch.setForeground(TEXT_DARK);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,200,200)), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        btnSearch  = styledBtn("🔍 Search",  ACCENT);
        btnRefresh = styledBtn("🔄 Refresh", new Color(71, 85, 105));
        searchPanel.add(new JLabel("Search:") {{ setForeground(TEXT_MID); }});
        searchPanel.add(txtSearch); searchPanel.add(btnSearch); searchPanel.add(btnRefresh);
        p.add(searchPanel, BorderLayout.CENTER);

        // Table
        String[] cols = {"ID", "Name", "Email", "Membership ID", "Type", "Role", "Status", "Joined"};
        tblModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblMembers = new JTable(tblModel);
        tblMembers.setBackground(CARD_BG); tblMembers.setForeground(TEXT_DARK);
        tblMembers.setFont(new Font("Segoe UI", Font.PLAIN, 13)); tblMembers.setRowHeight(30);
        tblMembers.setShowGrid(true); tblMembers.setGridColor(new Color(240, 240, 240));
        tblMembers.setSelectionBackground(new Color(230, 240, 255));
        tblMembers.setSelectionForeground(TEXT_DARK);
        tblMembers.getTableHeader().setBackground(new Color(245, 245, 245));
        tblMembers.getTableHeader().setForeground(TEXT_MID);
        tblMembers.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane sp = new JScrollPane(tblMembers);
        sp.setBorder(BorderFactory.createEmptyBorder());

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setBackground(BG_DARK);
        btnActivate   = styledBtn("✅ Activate User",   SUCCESS);
        btnDeactivate = styledBtn("🚫 Deactivate User", DANGER);
        actions.add(btnActivate); actions.add(btnDeactivate);

        JPanel tableArea = new JPanel(new BorderLayout(0, 8));
        tableArea.setBackground(BG_DARK);
        tableArea.add(sp,      BorderLayout.CENTER);
        tableArea.add(actions, BorderLayout.SOUTH);

        p.add(tableArea, BorderLayout.SOUTH);
        return p;
    }

    public void populateTable(List<Member> members) {
        tblModel.setRowCount(0);
        for (Member m : members) {
            tblModel.addRow(new Object[]{
                m.getMemberId(),
                m.getFullName(),
                m.getEmail(),
                m.getMembershipId(),
                m.getMembershipType(),
                m.getRole(),
                m.isActive() ? "✅ Active" : "🚫 Inactive",
                m.getJoinDate() != null ? m.getJoinDate().toString() : ""
            });
        }
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
