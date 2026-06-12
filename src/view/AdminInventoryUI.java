package view;

import model.java.Book;
import utils.Session;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/** AdminInventoryUI – Librarian add/update/delete books (faculty only). */
public class AdminInventoryUI extends JFrame {

    public JButton btnDashboard, btnMyBorrowings, btnBookSearch, btnWishlist;
    public JButton btnReviews, btnFinePayment, btnMyAccount;
    public JButton btnInventory, btnUsers, btnReports;
    public JButton btnSignOut;

    public JTable tblBooks;
    public DefaultTableModel tblModel;

    // Form fields
    public JTextField txtTitle, txtAuthor, txtIsbn, txtCategory, txtCopies,
                      txtPages, txtPublisher, txtYear;
    public JTextArea  txtDesc;
    public JButton    btnAdd, btnUpdate, btnDelete, btnClearForm, btnRefresh;
    public JLabel     lblFormTitle;

    private static final Color BG_DARK    = new Color(244, 246, 250);
    private static final Color SIDEBAR_BG = new Color(27, 58, 107);
    private static final Color ACCENT     = new Color(27, 58, 107);
    private static final Color ACCENT_DARK= new Color(18, 40, 75);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color TEXT_DARK  = new Color(26, 33, 51);
    private static final Color TEXT_LIGHT = Color.WHITE;
    private static final Color TEXT_MID   = new Color(75, 85, 99);
    private static final Color SUCCESS    = new Color(34, 197, 94);
    private static final Color DANGER     = new Color(239, 68, 68);
    private static final Color WARNING    = new Color(234, 179, 8);

    public AdminInventoryUI() {
        setTitle("Inventory Management – LibraryMS Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
        new controller.NewControllers.AdminInventoryController(this);
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
        btnInventory.setBackground(ACCENT_DARK);
        btnInventory.setForeground(TEXT_LIGHT);
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
        JLabel title = new JLabel("📦 Inventory Management");
        title.setForeground(TEXT_DARK); title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        p.add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildTableSection(), buildFormSection());
        split.setDividerLocation(340);
        split.setBackground(BG_DARK); split.setBorder(null);
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildTableSection() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG_DARK);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setBackground(BG_DARK);
        btnRefresh = styledBtn("🔄 Refresh", new Color(71, 85, 105));
        top.add(btnRefresh);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Author", "ISBN", "Category", "Total", "Available", "Year"};
        tblModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblBooks = styledTable(tblModel);
        JScrollPane sp = new JScrollPane(tblBooks);
        sp.setBorder(BorderFactory.createEmptyBorder());
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildFormSection() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        lblFormTitle = new JLabel("Add New Book");
        lblFormTitle.setForeground(TEXT_DARK);
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 4, 10, 8));
        fields.setBackground(CARD_BG);

        txtTitle    = addField(fields, "Title *");
        txtAuthor   = addField(fields, "Author *");
        txtIsbn     = addField(fields, "ISBN");
        txtCategory = addField(fields, "Category");
        txtCopies   = addField(fields, "Total Copies");
        txtPages    = addField(fields, "Pages");
        txtPublisher = addField(fields, "Publisher");
        txtYear     = addField(fields, "Publish Year");
        p.add(fields, BorderLayout.CENTER);

        // Description row
        JPanel descRow = new JPanel(new BorderLayout(8, 0));
        descRow.setBackground(CARD_BG);
        JLabel dl = new JLabel("Description:"); dl.setForeground(TEXT_MID);
        dl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDesc = new JTextArea(2, 30);
        txtDesc.setBackground(Color.WHITE); txtDesc.setForeground(TEXT_DARK);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDesc.setLineWrap(true); txtDesc.setWrapStyleWord(true);
        txtDesc.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,200,200)), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        descRow.add(dl, BorderLayout.WEST);
        descRow.add(new JScrollPane(txtDesc), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setBackground(CARD_BG);
        btnAdd       = styledBtn("➕ Add Book",    SUCCESS);
        btnUpdate    = styledBtn("✏️ Update Book",  WARNING);
        btnDelete    = styledBtn("🗑 Delete Book",  DANGER);
        btnClearForm = styledBtn("🔄 Clear Form",  new Color(71, 85, 105));
        btns.add(btnAdd); btns.add(btnUpdate); btns.add(btnDelete); btns.add(btnClearForm);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setBackground(CARD_BG);
        south.add(descRow, BorderLayout.NORTH);
        south.add(btns,    BorderLayout.SOUTH);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JTextField addField(JPanel parent, String label) {
        JLabel l = new JLabel(label); l.setForeground(TEXT_MID);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        parent.add(l);
        JTextField f = new JTextField();
        f.setBackground(Color.WHITE); f.setForeground(TEXT_DARK);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,200,200)), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        parent.add(f);
        return f;
    }

    public void populateTable(List<Book> books) {
        tblModel.setRowCount(0);
        for (Book b : books) {
            tblModel.addRow(new Object[]{
                b.getBookId(), b.getTitle(), b.getAuthor(),
                b.getIsbn() != null ? b.getIsbn() : "",
                b.getCategory() != null ? b.getCategory() : "",
                b.getTotalCopies(), b.getAvailableCopies(),
                b.getPublishYear() > 0 ? b.getPublishYear() : ""
            });
        }
    }

    public void fillForm(Book b) {
        txtTitle.setText(b.getTitle());
        txtAuthor.setText(b.getAuthor());
        txtIsbn.setText(b.getIsbn() != null ? b.getIsbn() : "");
        txtCategory.setText(b.getCategory() != null ? b.getCategory() : "");
        txtCopies.setText(String.valueOf(b.getTotalCopies()));
        txtPages.setText(b.getPages() > 0 ? String.valueOf(b.getPages()) : "");
        txtPublisher.setText(b.getPublisher() != null ? b.getPublisher() : "");
        txtYear.setText(b.getPublishYear() > 0 ? String.valueOf(b.getPublishYear()) : "");
        txtDesc.setText(b.getDescription() != null ? b.getDescription() : "");
        lblFormTitle.setText("Edit Book: " + b.getTitle());
    }

    public void clearForm() {
        for (JTextField f : new JTextField[]{txtTitle, txtAuthor, txtIsbn, txtCategory,
                txtCopies, txtPages, txtPublisher, txtYear}) f.setText("");
        txtDesc.setText("");
        lblFormTitle.setText("Add New Book");
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(CARD_BG); t.setForeground(TEXT_DARK);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13)); t.setRowHeight(28);
        t.setShowGrid(true); t.setGridColor(new Color(240, 240, 240));
        t.setSelectionBackground(new Color(230, 240, 255));
        t.setSelectionForeground(TEXT_DARK);
        t.getTableHeader().setBackground(new Color(245, 245, 245));
        t.getTableHeader().setForeground(TEXT_MID);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
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
