package inkcanvas.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * MainWindow — application shell.
 *
 * Static helpers used by every panel:
 *   styledButton(String, boolean)  — primary or secondary button
 *   sectionLabel(String)           — small all-caps muted heading label
 *   card(LayoutManager)            — white bordered card panel
 *   showToast(Component,String,boolean) — modal info/warning dialog
 *   scrollWrap(Component)          — borderless scroll pane
 *
 * Navigation:
 *   showPanel(String)          — flip card + refresh target panel
 *   openWork(int, String)      — load work into ReadPanel then show it
 *   refreshAfterReport()       — called by ReadPanel after report submit;
 *                                updates ReportPanel + AdminPanel in-place
 *   refreshSidebarUser()       — repaint sidebar avatar + name
 *   getWritePanel()            — expose WritePanel to LibraryPanel for editing
 */
public class MainWindow extends JFrame {

    // ── Panel name constants ──────────────────────────────────────
    public static final String LOGIN       = "LOGIN";
    public static final String DISCOVER    = "DISCOVER";
    public static final String LIBRARY     = "LIBRARY";
    public static final String WRITE       = "WRITE";
    public static final String READ        = "READ";
    public static final String REQUESTS    = "REQUESTS";
    public static final String COMPETITION = "COMPETITION";
    public static final String PROFILE     = "PROFILE";
    public static final String REPORTS     = "REPORTS";
    public static final String ADMIN_LOGIN = "ADMIN_LOGIN";
    public static final String ADMIN       = "ADMIN";

    // ── Design tokens — shared by all panels ──────────────────────
    public static final Color BG_PARCHMENT = new Color(250, 247, 242);
    public static final Color BG_CREAM     = new Color(243, 237, 227);
    public static final Color ACCENT       = new Color(181,  69,  27);
    public static final Color ACCENT_DARK  = new Color(138,  50,  18);
    public static final Color ACCENT_LIGHT = new Color(240, 230, 223);
    public static final Color TEXT_MAIN    = new Color( 26,  16,   8);
    public static final Color TEXT_MUTED   = new Color(122, 111, 101);
    public static final Color BORDER_COLOR = new Color(200, 190, 180);
    public static final Color WHITE        = Color.WHITE;
    public static final Color SUCCESS_BG   = new Color(232, 245, 233);
    public static final Color SUCCESS_FG   = new Color( 46, 125,  50);
    public static final Color WARN_BG      = new Color(255, 248, 225);
    public static final Color WARN_FG      = new Color(180,  83,   9);
    public static final Color DANGER_BG    = new Color(254, 226, 226);
    public static final Color DANGER_FG    = new Color(185,  28,  28);

    // ── Shared fonts ──────────────────────────────────────────────
    public static final Font FONT_TITLE = new Font("Serif",     Font.BOLD,  22);
    public static final Font FONT_H2    = new Font("Serif",     Font.BOLD,  17);
    public static final Font FONT_H3    = new Font("Serif",     Font.BOLD,  14);
    public static final Font FONT_BODY  = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_BOLD  = new Font("SansSerif", Font.BOLD,  13);

    // ── Instance fields ───────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel     cardPanel;
    private JPanel     sidebar;
    private JPanel     topBar;
    private JLabel     pageTitle;
    private JButton    newWorkBtn;
    private JLabel     sidebarNameLabel;
    private JLabel     sidebarAvatarLabel;

    private LoginPanel       loginPanel;
    private DiscoverPanel    discoverPanel;
    private LibraryPanel     libraryPanel;
    private WritePanel       writePanel;
    private ReadPanel        readPanel;
    private RequestPanel     requestPanel;
    private CompetitionPanel competitionPanel;
    private ProfilePanel     profilePanel;
    private ReportPanel      reportPanel;
    private AdminLoginPanel  adminLoginPanel;
    private AdminPanel       adminPanel;

    // ─────────────────────────────────────────────────────────────
    public MainWindow() {
        setTitle("Ink Canvas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_PARCHMENT);
        setLayout(new BorderLayout());

        buildSidebar();
        buildMainArea();
        showPanel(LOGIN);
    }

    // ═════════════════════════════════════════════════════════════
    // SIDEBAR
    // ═════════════════════════════════════════════════════════════
    private void buildSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_CREAM);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        sidebar.setVisible(false);

        // Logo
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(BG_CREAM);
        logoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 16, 14, 16)));
        JLabel logoLabel = new JLabel("Ink Canvas");
        logoLabel.setFont(new Font("Serif", Font.BOLD, 20));
        logoLabel.setForeground(TEXT_MAIN);
        JLabel logoSub = new JLabel("Write \u00b7 Share \u00b7 Discover");
        logoSub.setFont(FONT_SMALL);
        logoSub.setForeground(TEXT_MUTED);
        logoPanel.add(logoLabel, BorderLayout.NORTH);
        logoPanel.add(logoSub,   BorderLayout.SOUTH);

        // Nav
        JPanel navPanel = new JPanel();
        navPanel.setBackground(BG_CREAM);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        navPanel.add(navSection("EXPLORE"));
        navPanel.add(navItem("\u25c8  Discover",      DISCOVER));
        navPanel.add(navItem("\u2709  Request Board", REQUESTS));
        navPanel.add(navItem("\u2605  Competition",   COMPETITION));
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(navSection("MY WORK"));
        navPanel.add(navItem("\u229e  My Library",    LIBRARY));
        navPanel.add(navItem("\u270e  New Work",      WRITE));
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(navSection("ACCOUNT"));
        navPanel.add(navItem("\u25c9  Profile",       PROFILE));
        navPanel.add(navItem("\u2691  Reports",       REPORTS));

        JScrollPane navScroll = new JScrollPane(navPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.setBorder(BorderFactory.createEmptyBorder());

        // Logout
        JPanel logoutRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        logoutRow.setBackground(BG_CREAM);
        logoutRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        JButton logoutBtn = new JButton("\u2190  Logout");
        logoutBtn.setFont(FONT_SMALL);
        logoutBtn.setForeground(DANGER_FG);
        logoutBtn.setBackground(BG_CREAM);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setOpaque(true);
        logoutBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            inkcanvas.service.DataStore.get().setCurrentUser(null);
            showPanel(LOGIN);
        });
        logoutRow.add(logoutBtn);

        // User area — custom-painted circular avatar
        JPanel userArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        userArea.setBackground(BG_CREAM);

        sidebarAvatarLabel = new JLabel("") {
            @Override
            public void paintComponent(Graphics g) {
                inkcanvas.model.User cur =
                        inkcanvas.service.DataStore.get().getCurrentUser();
                String ini = (cur != null) ? getInitials(cur.getUsername()) : "?";
                g.setColor(ACCENT);
                g.fillOval(0, 0, 34, 34);
                g.setColor(WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(ini,
                        (34 - fm.stringWidth(ini)) / 2,
                        (34 - fm.getHeight())       / 2 + fm.getAscent());
            }
            @Override
            public Dimension getPreferredSize() { return new Dimension(34, 34); }
        };

        sidebarNameLabel = new JLabel("Loading...");
        sidebarNameLabel.setFont(FONT_BOLD);
        sidebarNameLabel.setForeground(TEXT_MAIN);

        JLabel urole = new JLabel("Writer & Reader");
        urole.setFont(FONT_SMALL);
        urole.setForeground(TEXT_MUTED);

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setBackground(BG_CREAM);
        userInfo.add(sidebarNameLabel);
        userInfo.add(urole);
        userArea.add(sidebarAvatarLabel);
        userArea.add(userInfo);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(BG_CREAM);
        southPanel.add(logoutRow, BorderLayout.NORTH);
        southPanel.add(userArea,  BorderLayout.SOUTH);

        sidebar.add(logoPanel,  BorderLayout.NORTH);
        sidebar.add(navScroll,  BorderLayout.CENTER);
        sidebar.add(southPanel, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
    }

    private JLabel navSection(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 8, 3, 8));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton navItem(String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_MAIN);
        btn.setBackground(BG_CREAM);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(226, 218, 208)); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(BG_CREAM); }
        });
        btn.addActionListener(e -> showPanel(panelName));
        return btn;
    }

    // ═════════════════════════════════════════════════════════════
    // MAIN CONTENT AREA
    // ═════════════════════════════════════════════════════════════
    private void buildMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(BG_PARCHMENT);

        topBar = new JPanel(new BorderLayout());
        topBar.setBackground(WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        pageTitle = new JLabel("Discover");
        pageTitle.setFont(new Font("Serif", Font.BOLD, 18));
        pageTitle.setForeground(TEXT_MAIN);
        topBar.add(pageTitle, BorderLayout.WEST);

        newWorkBtn = styledButton("+ New Work", true);
        newWorkBtn.addActionListener(e -> showPanel(WRITE));
        topBar.add(newWorkBtn, BorderLayout.EAST);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BG_PARCHMENT);

        // Instantiate panels — order does not matter; all panels receive 'this'
        loginPanel       = new LoginPanel(this);
        discoverPanel    = new DiscoverPanel(this);
        libraryPanel     = new LibraryPanel(this);
        writePanel       = new WritePanel(this);
        readPanel        = new ReadPanel(this);
        requestPanel     = new RequestPanel(this);
        competitionPanel = new CompetitionPanel(this);
        profilePanel     = new ProfilePanel(this);
        reportPanel      = new ReportPanel(this);
        adminLoginPanel  = new AdminLoginPanel(this);
        adminPanel       = new AdminPanel(this);

        cardPanel.add(loginPanel,       LOGIN);
        cardPanel.add(discoverPanel,    DISCOVER);
        cardPanel.add(libraryPanel,     LIBRARY);
        cardPanel.add(writePanel,       WRITE);
        cardPanel.add(readPanel,        READ);
        cardPanel.add(requestPanel,     REQUESTS);
        cardPanel.add(competitionPanel, COMPETITION);
        cardPanel.add(profilePanel,     PROFILE);
        cardPanel.add(reportPanel,      REPORTS);
        cardPanel.add(adminLoginPanel,  ADMIN_LOGIN);
        cardPanel.add(adminPanel,       ADMIN);

        mainArea.add(topBar,    BorderLayout.NORTH);
        mainArea.add(cardPanel, BorderLayout.CENTER);
        add(mainArea, BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═════════════════════════════════════════════════════════════

    /**
     * Switch the visible card and refresh the target panel.
     *
     * Refresh is dispatched via SwingUtilities.invokeLater so the card flip
     * and initial layout pass complete before the panel tries to measure its
     * own width. This fixes the Discover page WrapLayout blank-on-first-show
     * issue where getWidth() returned 0 before the component was rendered.
     */
    public void showPanel(String name) {
        cardLayout.show(cardPanel, name);
        updateTopBar(name);

        boolean isLoginScreen = name.equals(LOGIN) || name.equals(ADMIN_LOGIN);
        boolean isAdminScreen = name.equals(ADMIN);

        sidebar.setVisible(!isLoginScreen && !isAdminScreen);

        if (newWorkBtn != null)
            newWorkBtn.setVisible(!isLoginScreen && !isAdminScreen);

        if (topBar != null)
            topBar.setVisible(!isLoginScreen);

        // Defer refresh so the panel has a real width before WrapLayout runs
        SwingUtilities.invokeLater(() -> {
            if (name.equals(DISCOVER))    discoverPanel.refresh();
            if (name.equals(LIBRARY))     libraryPanel.refresh();
            if (name.equals(WRITE))       writePanel.refresh();
            if (name.equals(REQUESTS))    requestPanel.refresh();
            if (name.equals(COMPETITION)) competitionPanel.refresh();
            if (name.equals(PROFILE))     profilePanel.refresh();
            if (name.equals(REPORTS))     reportPanel.refresh();
            if (name.equals(ADMIN))       adminPanel.refresh();
        });

        refreshSidebarUser();
    }

    /**
     * Called by ReadPanel immediately after DataStore.reportUser() succeeds.
     *
     * DataStore has already persisted users.txt and reportlogs.txt.
     * This pushes the change into the live UI panels without requiring the
     * user to navigate away and back.
     */
    public void refreshAfterReport() {
        SwingUtilities.invokeLater(() -> {
            reportPanel.refresh();      // user-facing: stats, accounts, log
            adminPanel.refreshAllTabs(); // admin: all tabs updated at once
        });
    }

    /** Expose WritePanel so LibraryPanel can load a draft for editing. */
    public WritePanel getWritePanel() { return writePanel; }

    /**
     * Navigate to ReadPanel to display the given work.
     * @param workId      DataStore key for the work
     * @param returnPanel panel name to show when user presses "← Back"
     */
    public void openWork(int workId, String returnPanel) {
        readPanel.loadWork(workId, returnPanel);
        showPanel(READ);
    }

    private void updateTopBar(String name) {
        switch (name) {
            case LOGIN:       pageTitle.setText("Welcome");         break;
            case DISCOVER:    pageTitle.setText("Discover");        break;
            case LIBRARY:     pageTitle.setText("My Library");      break;
            case WRITE:       pageTitle.setText("New Work");        break;
            case READ:        pageTitle.setText("Reading");         break;
            case REQUESTS:    pageTitle.setText("Request Board");   break;
            case COMPETITION: pageTitle.setText("Competition");     break;
            case PROFILE:     pageTitle.setText("My Profile");      break;
            case REPORTS:     pageTitle.setText("Reports");         break;
            case ADMIN_LOGIN: pageTitle.setText("Admin Login");     break;
            case ADMIN:       pageTitle.setText("Admin Dashboard"); break;
            default:          pageTitle.setText("");                break;
        }
    }

    /** Repaint the sidebar avatar and username (call after login or profile save). */
    public void refreshSidebarUser() {
        inkcanvas.model.User cur =
                inkcanvas.service.DataStore.get().getCurrentUser();
        if (cur == null) return;
        if (sidebarNameLabel != null) {
            sidebarNameLabel.setText(cur.getUsername());
            sidebarNameLabel.revalidate();
            sidebarNameLabel.repaint();
        }
        if (sidebarAvatarLabel != null) sidebarAvatarLabel.repaint();
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("[\\s\\-_]+");
        if (parts.length >= 2 && !parts[1].isEmpty())
            return "" + Character.toUpperCase(parts[0].charAt(0))
                      + Character.toUpperCase(parts[1].charAt(0));
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    // ═════════════════════════════════════════════════════════════
    // STATIC SHARED UI HELPERS
    // All methods below are public static so every panel can use them
    // without holding a MainWindow reference.
    // ═════════════════════════════════════════════════════════════

    /**
     * Primary (filled accent) or secondary (white outlined) button.
     * Called from WritePanel, CompetitionPanel, AdminPanel, ReportPanel, etc.
     */
    public static JButton styledButton(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        if (primary) {
            btn.setBackground(ACCENT);
            btn.setForeground(WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_DARK),
                    BorderFactory.createEmptyBorder(6, 14, 6, 14)));
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT_DARK); }
                @Override public void mouseExited (MouseEvent e) { btn.setBackground(ACCENT); }
            });
        } else {
            btn.setBackground(WHITE);
            btn.setForeground(TEXT_MAIN);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR),
                    BorderFactory.createEmptyBorder(5, 12, 5, 12)));
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btn.setBackground(BG_CREAM); }
                @Override public void mouseExited (MouseEvent e) { btn.setBackground(WHITE); }
            });
        }
        return btn;
    }

    /**
     * Small all-caps muted section-heading label.
     *
     * Called from WritePanel.sideCardWrapper() and AdminPanel.
     * Must be public static — WritePanel calls MainWindow.sectionLabel(label).
     */
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    /**
     * White bordered card panel with standard padding.
     * Used for generic card containers in any panel.
     */
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return p;
    }

    /**
     * Modal info (success=true) or warning (success=false) dialog.
     * Centralised so all panels produce consistent feedback messages.
     */
    public static void showToast(Component parent, String msg, boolean success) {
        JOptionPane.showMessageDialog(parent, msg, "Ink Canvas",
                success ? JOptionPane.INFORMATION_MESSAGE
                        : JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Borderless scroll pane with the parchment viewport background.
     * Used by ReadPanel and any panel that needs full-panel vertical scrolling.
     */
    public static JScrollPane scrollWrap(Component c) {
        JScrollPane sp = new JScrollPane(c,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG_PARCHMENT);
        return sp;
    }
}

