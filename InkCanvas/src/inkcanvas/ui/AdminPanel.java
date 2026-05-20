package inkcanvas.ui;

import inkcanvas.ds.MyList;
import inkcanvas.model.Competition;
import inkcanvas.model.ReportLog;
import inkcanvas.model.User;
import inkcanvas.model.Work;
import inkcanvas.service.DataStore;
import inkcanvas.service.FileManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class AdminPanel extends JPanel {

    private final MainWindow window;
    private CardLayout2      subLayout;
    private JPanel           subPanel;

    private static final String TAB_OVERVIEW    = "OVERVIEW";
    private static final String TAB_COMPETITION = "COMPETITION";
    private static final String TAB_USERS       = "USERS";
    private static final String TAB_ACCOUNTS    = "ACCOUNTS";
    private static final String TAB_CONTENT     = "CONTENT";

    private JButton btnOverview, btnCompetition, btnUsers, btnAccounts, btnContent;

    private JTextField compMonthField;
    private JTextField compThemeField;
    private JTextField compEndDateField;
    private JLabel     compCurrentMonth;
    private JLabel     compCurrentTheme;
    private JLabel     compCurrentEnd;
    private JLabel     compStatusMsg;

    private JPanel usersListPanel;
    private JPanel accountsListPanel;
    private JPanel contentListPanel;

    private JLabel ovTotalUsers;
    private JLabel ovBlocked;
    private JLabel ovWarned;
    private JLabel ovTotalWorks;
    private JLabel ovPubWorks;
    private JLabel ovCompEntries;

    public AdminPanel(MainWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(MainWindow.BG_PARCHMENT);
        buildUI();
    }

    private void buildUI() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(20, 30, 48));
        sidebar.setPreferredSize(new Dimension(210, 0));

        JPanel logoArea = new JPanel(new BorderLayout());
        logoArea.setBackground(new Color(20, 30, 48));
        logoArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 70, 100)),
                BorderFactory.createEmptyBorder(16, 14, 14, 14)));
        JLabel logoTitle = new JLabel("\u2699  Admin Portal");
        logoTitle.setFont(new Font("Serif", Font.BOLD, 17));
        logoTitle.setForeground(Color.WHITE);
        JLabel logoSub = new JLabel("Ink Canvas Management");
        logoSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        logoSub.setForeground(new Color(120, 150, 200));
        logoArea.add(logoTitle, BorderLayout.CENTER);
        logoArea.add(logoSub,   BorderLayout.SOUTH);

        JPanel navArea = new JPanel();
        navArea.setLayout(new BoxLayout(navArea, BoxLayout.Y_AXIS));
        navArea.setBackground(new Color(20, 30, 48));
        navArea.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));

        btnOverview    = createNavBtn("\uD83D\uDCCA  Overview");
        btnCompetition = createNavBtn("\uD83C\uDFC6  Competition");
        btnUsers       = createNavBtn("\uD83D\uDC65  All Users");
        btnAccounts    = createNavBtn("\uD83D\uDEAB  Accounts");
        btnContent     = createNavBtn("\uD83D\uDCDD  All Content");

        btnOverview.addActionListener(e    -> switchTab(TAB_OVERVIEW,    0));
        btnCompetition.addActionListener(e -> switchTab(TAB_COMPETITION, 1));
        btnUsers.addActionListener(e       -> switchTab(TAB_USERS,       2));
        btnAccounts.addActionListener(e    -> switchTab(TAB_ACCOUNTS,    3));
        btnContent.addActionListener(e     -> switchTab(TAB_CONTENT,     4));

        navArea.add(btnOverview);    navArea.add(Box.createVerticalStrut(2));
        navArea.add(btnCompetition); navArea.add(Box.createVerticalStrut(2));
        navArea.add(btnUsers);       navArea.add(Box.createVerticalStrut(2));
        navArea.add(btnAccounts);    navArea.add(Box.createVerticalStrut(2));
        navArea.add(btnContent);

        JPanel logoutArea = new JPanel(new BorderLayout());
        logoutArea.setBackground(new Color(20, 30, 48));
        logoutArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 70, 100)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JButton logoutBtn = new JButton("\u2190  Back to Main");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logoutBtn.setForeground(new Color(160, 190, 230));
        logoutBtn.setBackground(new Color(20, 30, 48));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setOpaque(true);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> window.showPanel(MainWindow.LOGIN));
        logoutArea.add(logoutBtn, BorderLayout.CENTER);

        sidebar.add(logoArea);
        sidebar.add(navArea);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutArea);

        subLayout = new CardLayout2();
        subPanel  = new JPanel(subLayout);
        subPanel.setBackground(MainWindow.BG_PARCHMENT);

        subPanel.add(buildOverviewTab(),    TAB_OVERVIEW);
        subPanel.add(buildCompetitionTab(), TAB_COMPETITION);
        subPanel.add(buildUsersTab(),       TAB_USERS);
        subPanel.add(buildAccountsTab(),    TAB_ACCOUNTS);
        subPanel.add(buildContentTab(),     TAB_CONTENT);

        add(sidebar,  BorderLayout.WEST);
        add(subPanel, BorderLayout.CENTER);
    }

    private JButton createNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(new Color(180, 200, 240));
        btn.setBackground(new Color(20, 30, 48));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setContentAreaFilled(false);
        return btn;
    }

    private void switchTab(String tabName, int activeIdx) {
        subLayout.show(subPanel, tabName);
        JButton[] all = { btnOverview, btnCompetition, btnUsers, btnAccounts, btnContent };
        for (int i = 0; i < all.length; i++) {
            if (i == activeIdx) {
                all[i].setBackground(new Color(37, 99, 235));
                all[i].setForeground(Color.WHITE);
            } else {
                all[i].setBackground(new Color(20, 30, 48));
                all[i].setForeground(new Color(180, 200, 240));
            }
        }
        if (TAB_OVERVIEW.equals(tabName))    refreshOverviewTab();
        if (TAB_USERS.equals(tabName))       refreshUsersTab();
        if (TAB_ACCOUNTS.equals(tabName))    refreshAccountsTab();
        if (TAB_COMPETITION.equals(tabName)) refreshCompetitionDisplay();
        if (TAB_CONTENT.equals(tabName))     refreshContentTab();
    }

    public void refresh() {
        switchTab(TAB_OVERVIEW, 0);
    }

    public void refreshAllTabs() {
        refreshOverviewTab();
        refreshUsersTab();
        refreshAccountsTab();
        refreshContentTab();
    }

    // ── TAB 1: OVERVIEW ──────────────────────────────────────────
    private JPanel buildOverviewTab() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(MainWindow.BG_PARCHMENT);
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Admin Overview");
        heading.setFont(new Font("Serif", Font.BOLD, 22));
        heading.setForeground(MainWindow.TEXT_MAIN);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Platform summary — all data reflects live state");
        sub.setFont(MainWindow.FONT_SMALL);
        sub.setForeground(MainWindow.TEXT_MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        ovTotalUsers  = new JLabel("0");
        ovBlocked     = new JLabel("0");
        ovWarned      = new JLabel("0");
        ovTotalWorks  = new JLabel("0");
        ovPubWorks    = new JLabel("0");
        ovCompEntries = new JLabel("0");

        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        statsGrid.setBackground(MainWindow.BG_PARCHMENT);
        statsGrid.setAlignmentX(LEFT_ALIGNMENT);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        statsGrid.add(buildStatCard("\uD83D\uDC65 Total Users",         ovTotalUsers,  new Color(37, 99, 235)));
        statsGrid.add(buildStatCard("\uD83D\uDEAB Blocked Accounts",    ovBlocked,     MainWindow.DANGER_FG));
        statsGrid.add(buildStatCard("\u26A0  Warned Accounts",          ovWarned,      MainWindow.WARN_FG));
        statsGrid.add(buildStatCard("\uD83D\uDCDD Total Works",         ovTotalWorks,  MainWindow.ACCENT));
        statsGrid.add(buildStatCard("\uD83D\uDCE2 Published Works",     ovPubWorks,    MainWindow.SUCCESS_FG));
        statsGrid.add(buildStatCard("\uD83C\uDFC6 Competition Entries", ovCompEntries, new Color(120, 80, 200)));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setBackground(MainWindow.BG_PARCHMENT);
        actionRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton goComp    = MainWindow.styledButton("\uD83C\uDFC6  Change Theme", true);
        JButton goUsers   = MainWindow.styledButton("\uD83D\uDC65  View Users",   false);
        JButton goAccs    = MainWindow.styledButton("\uD83D\uDEAB  Accounts",     false);
        JButton goContent = MainWindow.styledButton("\uD83D\uDCDD  All Content",  false);
        goComp.addActionListener(e    -> switchTab(TAB_COMPETITION, 1));
        goUsers.addActionListener(e   -> switchTab(TAB_USERS,       2));
        goAccs.addActionListener(e    -> switchTab(TAB_ACCOUNTS,    3));
        goContent.addActionListener(e -> switchTab(TAB_CONTENT,     4));
        actionRow.add(goComp); actionRow.add(goUsers);
        actionRow.add(goAccs); actionRow.add(goContent);

        outer.add(heading);         outer.add(Box.createVerticalStrut(4));
        outer.add(sub);             outer.add(Box.createVerticalStrut(20));
        outer.add(sectionLbl("Platform Statistics")); outer.add(Box.createVerticalStrut(10));
        outer.add(statsGrid);       outer.add(Box.createVerticalStrut(24));
        outer.add(sectionLbl("Quick Actions")); outer.add(Box.createVerticalStrut(10));
        outer.add(actionRow);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(MainWindow.BG_PARCHMENT);
        wrap.add(wrapScroll(outer), BorderLayout.CENTER);
        return wrap;
    }

    private void refreshOverviewTab() {
        if (ovTotalUsers == null) return;
        DataStore ds = DataStore.get();
        MyList<User> allUsers = ds.getAllUsers();
        int totalUsers = allUsers.size(), blocked = 0, warned = 0;
        for (int i = 0; i < allUsers.size(); i++) {
            User u = allUsers.get(i);
            if (u.isBlocked())                blocked++;
            else if (u.getReportCount() >= 5) warned++;
        }
        ovTotalUsers.setText(String.valueOf(totalUsers));
        ovBlocked.setText(String.valueOf(blocked));
        ovWarned.setText(String.valueOf(warned));
        ovTotalWorks.setText(String.valueOf(ds.getAllWorks().size()));
        ovPubWorks.setText(String.valueOf(ds.getPublishedWorks().size()));
        ovCompEntries.setText(String.valueOf(ds.getCompetition().getEntryWorkIds().size()));
    }

    // ── TAB 2: COMPETITION ────────────────────────────────────────
    private JPanel buildCompetitionTab() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(MainWindow.BG_PARCHMENT);
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Competition Settings");
        heading.setFont(new Font("Serif", Font.BOLD, 22));
        heading.setForeground(MainWindow.TEXT_MAIN);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Changes save immediately and persist across restarts.");
        sub.setFont(MainWindow.FONT_SMALL);
        sub.setForeground(MainWindow.TEXT_MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        JPanel currentCard = new JPanel();
        currentCard.setLayout(new BoxLayout(currentCard, BoxLayout.Y_AXIS));
        currentCard.setBackground(new Color(240, 248, 255));
        currentCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        currentCard.setAlignmentX(LEFT_ALIGNMENT);
        currentCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel currentTitle = new JLabel("Current Competition");
        currentTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        currentTitle.setForeground(new Color(37, 99, 235));
        currentTitle.setAlignmentX(LEFT_ALIGNMENT);

        compCurrentMonth = new JLabel(" "); compCurrentMonth.setFont(MainWindow.FONT_BODY); compCurrentMonth.setAlignmentX(LEFT_ALIGNMENT);
        compCurrentTheme = new JLabel(" "); compCurrentTheme.setFont(MainWindow.FONT_BODY); compCurrentTheme.setAlignmentX(LEFT_ALIGNMENT);
        compCurrentEnd   = new JLabel(" "); compCurrentEnd.setFont(MainWindow.FONT_BODY);   compCurrentEnd.setAlignmentX(LEFT_ALIGNMENT);

        currentCard.add(currentTitle);
        currentCard.add(Box.createVerticalStrut(8));
        currentCard.add(compCurrentMonth);
        currentCard.add(Box.createVerticalStrut(3));
        currentCard.add(compCurrentTheme);
        currentCard.add(Box.createVerticalStrut(3));
        currentCard.add(compCurrentEnd);

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        formCard.setAlignmentX(LEFT_ALIGNMENT);
        formCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        JLabel formTitle = new JLabel("Update Competition");
        formTitle.setFont(new Font("Serif", Font.BOLD, 16));
        formTitle.setForeground(MainWindow.TEXT_MAIN);
        formTitle.setAlignmentX(LEFT_ALIGNMENT);
        JLabel hint = new JLabel("Date format: YYYY-MM-DD  (e.g. 2026-06-30)");
        hint.setFont(MainWindow.FONT_SMALL); hint.setForeground(MainWindow.TEXT_MUTED); hint.setAlignmentX(LEFT_ALIGNMENT);

        compMonthField   = makeInputField();
        compThemeField   = makeInputField();
        compEndDateField = makeInputField();

        compStatusMsg = new JLabel(" ");
        compStatusMsg.setFont(MainWindow.FONT_SMALL);
        compStatusMsg.setAlignmentX(LEFT_ALIGNMENT);

        JButton saveBtn  = MainWindow.styledButton("Save Competition Settings", true);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> saveCompetition());

        JButton resetBtn = MainWindow.styledButton("Reset to Current", false);
        resetBtn.setAlignmentX(LEFT_ALIGNMENT);
        resetBtn.addActionListener(e -> { prefillCompetitionFields(); compStatusMsg.setText(" "); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setBackground(Color.WHITE); btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.add(saveBtn); btnRow.add(resetBtn);

        formCard.add(formTitle);             formCard.add(Box.createVerticalStrut(4));
        formCard.add(hint);                  formCard.add(Box.createVerticalStrut(14));
        formCard.add(makeFieldLabel("Month Label  (e.g. June 2026)")); formCard.add(Box.createVerticalStrut(4));
        formCard.add(compMonthField);        formCard.add(Box.createVerticalStrut(10));
        formCard.add(makeFieldLabel("Theme / Title")); formCard.add(Box.createVerticalStrut(4));
        formCard.add(compThemeField);        formCard.add(Box.createVerticalStrut(10));
        formCard.add(makeFieldLabel("End Date (YYYY-MM-DD)")); formCard.add(Box.createVerticalStrut(4));
        formCard.add(compEndDateField);      formCard.add(Box.createVerticalStrut(12));
        formCard.add(compStatusMsg);         formCard.add(Box.createVerticalStrut(8));
        formCard.add(btnRow);

        outer.add(heading); outer.add(Box.createVerticalStrut(4));
        outer.add(sub);     outer.add(Box.createVerticalStrut(16));
        outer.add(sectionLbl("Current Settings")); outer.add(Box.createVerticalStrut(8));
        outer.add(currentCard); outer.add(Box.createVerticalStrut(16));
        outer.add(sectionLbl("Edit Settings")); outer.add(Box.createVerticalStrut(8));
        outer.add(formCard);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(MainWindow.BG_PARCHMENT);
        wrap.add(wrapScroll(outer), BorderLayout.CENTER);
        return wrap;
    }

    private void refreshCompetitionDisplay() {
        Competition c = DataStore.get().getCompetition();
        if (compCurrentMonth == null) return;
        compCurrentMonth.setText("<html><b>Month:</b>  " + c.getMonth() + "</html>");
        compCurrentTheme.setText("<html><b>Theme:</b>  \"" + c.getTheme() + "\"</html>");
        compCurrentEnd.setText("<html><b>Ends:</b>   "
                + c.getEndDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) + "</html>");
        prefillCompetitionFields();
    }

    private void prefillCompetitionFields() {
        Competition c = DataStore.get().getCompetition();
        if (compMonthField == null) return;
        compMonthField.setText(c.getMonth());
        compThemeField.setText(c.getTheme());
        compEndDateField.setText(c.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    private void saveCompetition() {
        String month   = compMonthField.getText().trim();
        String theme   = compThemeField.getText().trim();
        String dateStr = compEndDateField.getText().trim();
        if (month.isEmpty())   { setCompMsg("Month label cannot be empty.",  false); return; }
        if (theme.isEmpty())   { setCompMsg("Theme cannot be empty.",        false); return; }
        if (dateStr.isEmpty()) { setCompMsg("End date cannot be empty.",     false); return; }
        LocalDate endDate;
        try {
            endDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException ex) {
            setCompMsg("Invalid date format. Use YYYY-MM-DD.", false); return;
        }
        if (endDate.isBefore(LocalDate.now())) {
            setCompMsg("End date cannot be in the past.", false); return;
        }
        Competition oldComp = DataStore.get().getCompetition();
        Competition newComp = new Competition(month, theme, endDate);
        MyList<Integer> oldIds = oldComp.getEntryWorkIds();
        for (int i = 0; i < oldIds.size(); i++) newComp.addEntry(oldIds.get(i));
        DataStore.get().setCompetition(newComp);
        SwingUtilities.invokeLater(() -> window.getWritePanel().refresh());
        refreshCompetitionDisplay();
        setCompMsg("\u2713 Competition updated and saved successfully!", true);
    }

    private void setCompMsg(String msg, boolean ok) {
        compStatusMsg.setText(msg);
        compStatusMsg.setForeground(ok ? MainWindow.SUCCESS_FG : MainWindow.DANGER_FG);
    }

    // ── TAB 3: ALL USERS ─────────────────────────────────────────
    private JPanel buildUsersTab() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(MainWindow.BG_PARCHMENT);
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(MainWindow.BG_PARCHMENT);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        JLabel heading = new JLabel("All Registered Users");
        heading.setFont(new Font("Serif", Font.BOLD, 22));
        heading.setForeground(MainWindow.TEXT_MAIN);
        topBar.add(heading, BorderLayout.WEST);

        usersListPanel = new JPanel();
        usersListPanel.setLayout(new BoxLayout(usersListPanel, BoxLayout.Y_AXIS));
        usersListPanel.setBackground(MainWindow.BG_PARCHMENT);

        outer.add(topBar,                    BorderLayout.NORTH);
        outer.add(wrapScroll(usersListPanel), BorderLayout.CENTER);
        return outer;
    }

    private void refreshUsersTab() {
        if (usersListPanel == null) return;
        usersListPanel.removeAll();

        MyList<User> allUsers = DataStore.get().getAllUsers();
        int total = allUsers.size(), blocked = 0, warned = 0, active = 0;
        for (int i = 0; i < allUsers.size(); i++) {
            User u = allUsers.get(i);
            if      (u.isBlocked())           blocked++;
            else if (u.getReportCount() >= 5) warned++;
            else                              active++;
        }

        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        summaryBar.setBackground(new Color(240, 248, 255));
        summaryBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253)),
                BorderFactory.createEmptyBorder(2, 10, 2, 10)));
        summaryBar.setAlignmentX(LEFT_ALIGNMENT);
        summaryBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        summaryBar.add(makeChipLabel("Total: "   + total,   new Color(37, 99, 235)));
        summaryBar.add(makeChipLabel("Active: "  + active,  MainWindow.SUCCESS_FG));
        summaryBar.add(makeChipLabel("Warned: "  + warned,  MainWindow.WARN_FG));
        summaryBar.add(makeChipLabel("Blocked: " + blocked, MainWindow.DANGER_FG));
        usersListPanel.add(summaryBar);
        usersListPanel.add(Box.createVerticalStrut(12));

        JPanel header = new JPanel(new GridLayout(1, 5, 0, 0));
        header.setBackground(MainWindow.BG_CREAM);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.add(hdrCell("User"));   header.add(hdrCell("Email"));
        header.add(hdrCell("Joined")); header.add(hdrCell("Reports")); header.add(hdrCell("Status"));
        usersListPanel.add(header);

        for (int i = 0; i < allUsers.size(); i++) usersListPanel.add(buildUserRow(allUsers.get(i)));
        usersListPanel.revalidate();
        usersListPanel.repaint();
    }

    private JPanel buildUserRow(User u) {
        JPanel row = new JPanel(new GridLayout(1, 5, 0, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setAlignmentX(LEFT_ALIGNMENT);

        Color avColor = u.isBlocked() ? MainWindow.DANGER_FG
                : (u.getReportCount() >= 5 ? MainWindow.WARN_FG : new Color(37, 99, 235));
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        namePanel.setBackground(Color.WHITE);
        namePanel.add(buildAvatar(getInitials(u.getUsername()), avColor, 28));
        JLabel nameLbl = new JLabel(u.getUsername());
        nameLbl.setFont(MainWindow.FONT_BOLD);
        nameLbl.setForeground(MainWindow.TEXT_MAIN);
        namePanel.add(nameLbl);

        JLabel emailLbl  = new JLabel(u.getEmail());
        emailLbl.setFont(MainWindow.FONT_SMALL); emailLbl.setForeground(MainWindow.TEXT_MUTED);
        JLabel joinedLbl = new JLabel(u.getJoinDateFormatted());
        joinedLbl.setFont(MainWindow.FONT_SMALL); joinedLbl.setForeground(MainWindow.TEXT_MUTED);

        JProgressBar bar = new JProgressBar(0, 10);
        bar.setValue(Math.min(u.getReportCount(), 10));
        bar.setStringPainted(true);
        bar.setString(u.getReportCount() + "/10");
        bar.setFont(new Font("SansSerif", Font.PLAIN, 10));
        bar.setBackground(MainWindow.BG_CREAM);
        bar.setForeground(u.isBlocked() ? MainWindow.DANGER_FG
                : (u.getReportCount() >= 5 ? MainWindow.WARN_FG : MainWindow.SUCCESS_FG));

        String statusText = u.isBlocked()        ? "Blocked"
                : u.getReportCount() >= 5        ? "Warned"
                : u.getReportCount() > 0         ? "Reported" : "Active";
        Color statusFg = u.isBlocked()           ? MainWindow.DANGER_FG
                : u.getReportCount() >= 5        ? MainWindow.WARN_FG
                : u.getReportCount() > 0         ? new Color(180, 100, 0) : MainWindow.SUCCESS_FG;
        Color statusBg = u.isBlocked()           ? MainWindow.DANGER_BG
                : u.getReportCount() >= 5        ? MainWindow.WARN_BG
                : u.getReportCount() > 0         ? new Color(255, 237, 213) : MainWindow.SUCCESS_BG;
        JLabel statusLbl = new JLabel(statusText);
        statusLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusLbl.setForeground(statusFg);
        statusLbl.setBackground(statusBg);
        statusLbl.setOpaque(true);
        statusLbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        row.add(namePanel); row.add(emailLbl); row.add(joinedLbl);
        row.add(bar);       row.add(statusLbl);
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { row.setBackground(MainWindow.BG_PARCHMENT); namePanel.setBackground(MainWindow.BG_PARCHMENT); }
            public void mouseExited (MouseEvent e) { row.setBackground(Color.WHITE);             namePanel.setBackground(Color.WHITE); }
        });
        return row;
    }

    // ── TAB 4: ACCOUNTS ──────────────────────────────────────────
    private JPanel buildAccountsTab() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(MainWindow.BG_PARCHMENT);
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(MainWindow.BG_PARCHMENT);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        JLabel heading = new JLabel("Blocked / Restricted Accounts");
        heading.setFont(new Font("Serif", Font.BOLD, 22));
        heading.setForeground(MainWindow.TEXT_MAIN);
        topBar.add(heading, BorderLayout.WEST);

        accountsListPanel = new JPanel();
        accountsListPanel.setLayout(new BoxLayout(accountsListPanel, BoxLayout.Y_AXIS));
        accountsListPanel.setBackground(MainWindow.BG_PARCHMENT);

        outer.add(topBar,                       BorderLayout.NORTH);
        outer.add(wrapScroll(accountsListPanel), BorderLayout.CENTER);
        return outer;
    }

    private void refreshAccountsTab() {
        if (accountsListPanel == null) return;
        accountsListPanel.removeAll();

        MyList<User> allUsers    = DataStore.get().getAllUsers();
        MyList<User> blockedList  = new MyList<>();
        MyList<User> warnedList   = new MyList<>();
        MyList<User> reportedList = new MyList<>();

        for (int i = 0; i < allUsers.size(); i++) {
            User u = allUsers.get(i);
            if      (u.isBlocked())           blockedList.add(u);
            else if (u.getReportCount() >= 5) warnedList.add(u);
            else if (u.getReportCount() > 0)  reportedList.add(u);
        }

        if (blockedList.size() == 0 && warnedList.size() == 0 && reportedList.size() == 0) {
            JLabel none = new JLabel("No blocked or reported accounts at this time.");
            none.setFont(MainWindow.FONT_BODY); none.setForeground(MainWindow.TEXT_MUTED);
            none.setAlignmentX(LEFT_ALIGNMENT);
            accountsListPanel.add(none);
        } else {
            if (blockedList.size() > 0) {
                accountsListPanel.add(buildSectionBanner("Blocked (" + blockedList.size() + ")", MainWindow.DANGER_FG, MainWindow.DANGER_BG));
                accountsListPanel.add(Box.createVerticalStrut(8));
                for (int i = 0; i < blockedList.size(); i++) { accountsListPanel.add(buildAccountCard(blockedList.get(i))); accountsListPanel.add(Box.createVerticalStrut(10)); }
                accountsListPanel.add(Box.createVerticalStrut(8));
            }
            if (warnedList.size() > 0) {
                accountsListPanel.add(buildSectionBanner("Warned — 5+ reports (" + warnedList.size() + ")", MainWindow.WARN_FG, MainWindow.WARN_BG));
                accountsListPanel.add(Box.createVerticalStrut(8));
                for (int i = 0; i < warnedList.size(); i++) { accountsListPanel.add(buildAccountCard(warnedList.get(i))); accountsListPanel.add(Box.createVerticalStrut(10)); }
                accountsListPanel.add(Box.createVerticalStrut(8));
            }
            if (reportedList.size() > 0) {
                accountsListPanel.add(buildSectionBanner("Under Review (" + reportedList.size() + ")", new Color(180, 100, 0), new Color(255, 237, 213)));
                accountsListPanel.add(Box.createVerticalStrut(8));
                for (int i = 0; i < reportedList.size(); i++) { accountsListPanel.add(buildAccountCard(reportedList.get(i))); accountsListPanel.add(Box.createVerticalStrut(10)); }
            }
        }
        accountsListPanel.revalidate();
        accountsListPanel.repaint();
    }

    private JPanel buildAccountCard(User u) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        card.setAlignmentX(LEFT_ALIGNMENT);

        Color avBg = u.isBlocked() ? MainWindow.DANGER_FG
                : (u.getReportCount() >= 5 ? MainWindow.WARN_FG : new Color(180, 100, 0));
        JLabel avLbl = buildAvatar(getInitials(u.getUsername()), avBg, 38);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 3));
        info.setBackground(Color.WHITE);
        JLabel nameLbl = new JLabel(u.getUsername() + "   \u00b7   " + u.getEmail());
        nameLbl.setFont(MainWindow.FONT_BOLD); nameLbl.setForeground(MainWindow.TEXT_MAIN);
        JLabel metaLbl = new JLabel("Joined " + u.getJoinDateFormatted() + "   \u00b7   "
                + u.getReportCount() + " report" + (u.getReportCount() != 1 ? "s" : ""));
        metaLbl.setFont(MainWindow.FONT_SMALL); metaLbl.setForeground(MainWindow.TEXT_MUTED);
        info.add(nameLbl); info.add(metaLbl);

        JPanel barPanel = new JPanel(new BorderLayout(0, 3));
        barPanel.setBackground(Color.WHITE);
        barPanel.setPreferredSize(new Dimension(160, 36));
        JProgressBar bar = new JProgressBar(0, 10);
        bar.setValue(Math.min(u.getReportCount(), 10));
        bar.setBackground(MainWindow.BG_CREAM);
        bar.setForeground(u.isBlocked() ? MainWindow.DANGER_FG
                : (u.getReportCount() >= 5 ? MainWindow.WARN_FG : new Color(180, 100, 0)));
        JLabel barLbl = new JLabel(u.getReportCount() + " / 10");
        barLbl.setFont(MainWindow.FONT_SMALL); barLbl.setForeground(MainWindow.TEXT_MUTED);
        barPanel.add(bar, BorderLayout.NORTH); barPanel.add(barLbl, BorderLayout.CENTER);

        JPanel actPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actPanel.setBackground(Color.WHITE);
        if (u.isBlocked()) {
            JButton unblockBtn = MainWindow.styledButton("Unblock", false);
            unblockBtn.addActionListener(e -> {
                u.setBlocked(false);
                DataStore.get().addReportLog(new ReportLog("UNBLOCKED", u.getUsername() + " unblocked by admin."));
                FileManager.saveUsers(DataStore.get());
                refreshAccountsTab(); refreshUsersTab();
                MainWindow.showToast(window, u.getUsername() + " unblocked.", true);
            });
            actPanel.add(unblockBtn);
        } else {
            JButton blockBtn = new JButton("Block Now");
            blockBtn.setFont(MainWindow.FONT_SMALL);
            blockBtn.setBackground(MainWindow.DANGER_BG); blockBtn.setForeground(MainWindow.DANGER_FG);
            blockBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            blockBtn.setFocusPainted(false); blockBtn.setOpaque(true);
            blockBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            blockBtn.setContentAreaFilled(false);
            blockBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            blockBtn.addActionListener(e -> {
                u.setBlocked(true);
                DataStore.get().addReportLog(new ReportLog("BLOCKED", u.getUsername() + " manually blocked by admin."));
                FileManager.saveUsers(DataStore.get());
                refreshAccountsTab(); refreshUsersTab();
                MainWindow.showToast(window, u.getUsername() + " blocked.", true);
            });
            actPanel.add(blockBtn);
        }

        JPanel middle = new JPanel(new BorderLayout(20, 0));
        middle.setBackground(Color.WHITE);
        middle.add(info, BorderLayout.CENTER); middle.add(barPanel, BorderLayout.EAST);
        card.add(avLbl, BorderLayout.WEST); card.add(middle, BorderLayout.CENTER); card.add(actPanel, BorderLayout.EAST);
        return card;
    }

    // ── TAB 5: ALL CONTENT ────────────────────────────────────────
    private JPanel buildContentTab() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(MainWindow.BG_PARCHMENT);
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(MainWindow.BG_PARCHMENT);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel headingPanel = new JPanel(new BorderLayout());
        headingPanel.setBackground(MainWindow.BG_PARCHMENT);
        JLabel heading = new JLabel("All User Content");
        heading.setFont(new Font("Serif", Font.BOLD, 22));
        heading.setForeground(MainWindow.TEXT_MAIN);
        JLabel sub = new JLabel("Admin view: every work. Reported works highlighted. Click View Content to inspect.");
        sub.setFont(MainWindow.FONT_SMALL);
        sub.setForeground(MainWindow.TEXT_MUTED);
        headingPanel.add(heading, BorderLayout.NORTH);
        headingPanel.add(sub,     BorderLayout.SOUTH);
        topBar.add(headingPanel, BorderLayout.WEST);

        contentListPanel = new JPanel();
        contentListPanel.setLayout(new BoxLayout(contentListPanel, BoxLayout.Y_AXIS));
        contentListPanel.setBackground(MainWindow.BG_PARCHMENT);

        outer.add(topBar,                       BorderLayout.NORTH);
        outer.add(wrapScroll(contentListPanel),  BorderLayout.CENTER);
        return outer;
    }

    private void refreshContentTab() {
        if (contentListPanel == null) return;
        contentListPanel.removeAll();

        MyList<Work> allWorks = DataStore.get().getAllWorks();
        if (allWorks.isEmpty()) {
            JLabel none = new JLabel("No works in the system yet.");
            none.setFont(MainWindow.FONT_BODY); none.setForeground(MainWindow.TEXT_MUTED);
            none.setAlignmentX(LEFT_ALIGNMENT);
            contentListPanel.add(none);
        } else {
            JPanel header = new JPanel(new BorderLayout(0, 0));
            header.setBackground(MainWindow.BG_CREAM);
            header.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, MainWindow.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            header.setAlignmentX(LEFT_ALIGNMENT);

            JPanel hdrInfo = new JPanel(new GridLayout(1, 5, 0, 0));
            hdrInfo.setBackground(MainWindow.BG_CREAM);
            hdrInfo.add(hdrCell("Title"));     hdrInfo.add(hdrCell("Author"));
            hdrInfo.add(hdrCell("Genre"));     hdrInfo.add(hdrCell("Status"));
            hdrInfo.add(hdrCell("Reported?"));
            JPanel hdrAction = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            hdrAction.setBackground(MainWindow.BG_CREAM);
            hdrAction.setPreferredSize(new Dimension(110, 18));
            hdrAction.add(hdrCell("Actions"));
            header.add(hdrInfo,   BorderLayout.CENTER);
            header.add(hdrAction, BorderLayout.EAST);
            contentListPanel.add(header);

            for (int i = 0; i < allWorks.size(); i++)
                contentListPanel.add(buildContentRow(allWorks.get(i)));
        }
        contentListPanel.revalidate();
        contentListPanel.repaint();
    }

    private JPanel buildContentRow(Work w) {
        User    author     = DataStore.get().getUserById(w.getAuthorId());
        boolean isReported = author != null && author.getReportCount() > 0;
        Color   rowBg      = isReported ? new Color(255, 248, 230) : Color.WHITE;

        JPanel row = new JPanel(new BorderLayout(0, 0));
        row.setBackground(rowBg);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel infoGrid = new JPanel(new GridLayout(1, 5, 0, 0));
        infoGrid.setBackground(rowBg);

        JLabel titleLbl = new JLabel("<html><b>" + truncate(w.getTitle(), 22) + "</b></html>");
        titleLbl.setFont(new Font("Serif", Font.BOLD, 13));
        titleLbl.setForeground(MainWindow.TEXT_MAIN);

        JLabel authorLbl = new JLabel(w.getAuthorName());
        authorLbl.setFont(MainWindow.FONT_SMALL); authorLbl.setForeground(MainWindow.TEXT_MUTED);

        JLabel genreLbl = new JLabel(w.getGenre());
        genreLbl.setFont(new Font("SansSerif", Font.BOLD, 10)); genreLbl.setForeground(MainWindow.ACCENT);

        String statusText = w.isPublished() ? "Published " + w.getPublishedDateFormatted() : "Draft";
        JLabel statusLbl  = new JLabel(statusText);
        statusLbl.setFont(MainWindow.FONT_SMALL);
        statusLbl.setForeground(w.isPublished() ? MainWindow.SUCCESS_FG : MainWindow.TEXT_MUTED);

        String repText = isReported ? "\u26A0 " + author.getReportCount() + " report(s)" : "\u2713 Clean";
        JLabel repLbl  = new JLabel(repText);
        repLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        repLbl.setForeground(isReported ? MainWindow.WARN_FG : MainWindow.SUCCESS_FG);

        infoGrid.add(titleLbl); infoGrid.add(authorLbl); infoGrid.add(genreLbl);
        infoGrid.add(statusLbl); infoGrid.add(repLbl);

        final int capturedWorkId = w.getWorkId();
        JButton viewBtn = new JButton("View Content");
        viewBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        viewBtn.setBackground(new Color(37, 99, 235));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        viewBtn.setFocusPainted(false);
        viewBtn.setOpaque(true);
        viewBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        viewBtn.setContentAreaFilled(true);
        viewBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { viewBtn.setBackground(new Color(20, 60, 180)); }
            public void mouseExited (MouseEvent e) { viewBtn.setBackground(new Color(37, 99, 235)); }
        });
        viewBtn.addActionListener(e -> {
            Work freshWork   = DataStore.get().getWorkById(capturedWorkId);
            User freshAuthor = (freshWork != null) ? DataStore.get().getUserById(freshWork.getAuthorId()) : null;
            if (freshWork != null) showFullContent(freshWork, freshAuthor);
        });

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        actionPanel.setBackground(rowBg);
        actionPanel.setPreferredSize(new Dimension(110, 36));
        actionPanel.add(viewBtn);

        row.add(infoGrid,    BorderLayout.CENTER);
        row.add(actionPanel, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                Color h = isReported ? new Color(255, 240, 200) : MainWindow.BG_PARCHMENT;
                row.setBackground(h); infoGrid.setBackground(h); actionPanel.setBackground(h);
            }
            public void mouseExited(MouseEvent e) {
                row.setBackground(rowBg); infoGrid.setBackground(rowBg); actionPanel.setBackground(rowBg);
            }
        });
        return row;
    }

    private void showFullContent(Work w, User author) {
        JDialog dlg = new JDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                "Admin: View Content", true);
        dlg.setSize(680, 580);
        dlg.setLocationRelativeTo(window);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLbl = new JLabel(w.getTitle());
        titleLbl.setFont(new Font("Serif", Font.BOLD, 20));
        titleLbl.setForeground(MainWindow.TEXT_MAIN);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        metaRow.setBackground(Color.WHITE); metaRow.setAlignmentX(LEFT_ALIGNMENT);
        addMeta(metaRow, "Author", w.getAuthorName());
        addMeta(metaRow, "Genre",  w.getGenre());
        addMeta(metaRow, "Status", w.isPublished() ? "Published " + w.getPublishedDateFormatted() : "Draft");
        addMeta(metaRow, "Words",  String.valueOf(w.getWordCount()));
        addMeta(metaRow, "Reads",  String.valueOf(w.getReadCount()));
        addMeta(metaRow, "Rating", String.format("%.1f", w.getAverageRating()) + " (" + w.getRatings().size() + ")");

        JPanel reportCard = null;
        if (author != null && author.getReportCount() > 0) {
            reportCard = new JPanel();
            reportCard.setLayout(new BoxLayout(reportCard, BoxLayout.Y_AXIS));
            reportCard.setBackground(new Color(255, 248, 230));
            reportCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MainWindow.WARN_FG),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            reportCard.setAlignmentX(LEFT_ALIGNMENT);
            reportCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            JLabel repTitle = new JLabel("\u26A0  Report Details for " + author.getUsername());
            repTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
            repTitle.setForeground(MainWindow.WARN_FG);
            repTitle.setAlignmentX(LEFT_ALIGNMENT);
            reportCard.add(repTitle);
            reportCard.add(Box.createVerticalStrut(8));

            MyList<ReportLog> logs = DataStore.get().getReportLogs();
            int shown = 0;
            for (int i = 0; i < logs.size() && shown < 5; i++) {
                ReportLog log = logs.get(i);
                if (log.getTargetUser().equals(author.getUsername())
                        || log.getMessage().contains(author.getUsername())) {
                    JPanel logRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
                    logRow.setBackground(new Color(255, 248, 230));
                    JLabel typeLbl   = new JLabel("[" + log.getType() + "]");
                    typeLbl.setFont(new Font("SansSerif", Font.BOLD, 10)); typeLbl.setForeground(MainWindow.ACCENT);
                    JLabel reasonLbl = new JLabel(log.getReason().isEmpty() ? log.getMessage() : "Reason: " + log.getReason());
                    reasonLbl.setFont(MainWindow.FONT_SMALL); reasonLbl.setForeground(MainWindow.TEXT_MAIN);
                    JLabel timeLbl   = new JLabel(log.getFormattedTime());
                    timeLbl.setFont(MainWindow.FONT_SMALL); timeLbl.setForeground(MainWindow.TEXT_MUTED);
                    logRow.add(typeLbl); logRow.add(reasonLbl); logRow.add(timeLbl);
                    reportCard.add(logRow);
                    shown++;
                }
            }
            if (shown == 0) {
                JLabel noLog = new JLabel("No specific report logs found for this user.");
                noLog.setFont(MainWindow.FONT_SMALL); noLog.setForeground(MainWindow.TEXT_MUTED);
                noLog.setAlignmentX(LEFT_ALIGNMENT);
                reportCard.add(noLog);
            }
        }

        JLabel contentTitle = new JLabel("Full Content:");
        contentTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        contentTitle.setForeground(MainWindow.TEXT_MUTED);
        contentTitle.setAlignmentX(LEFT_ALIGNMENT);

        JTextArea contentArea = new JTextArea(w.getContent());
        contentArea.setFont(new Font("Serif", Font.PLAIN, 14));
        contentArea.setLineWrap(true); contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(MainWindow.BG_PARCHMENT);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setPreferredSize(new Dimension(600, 200));
        contentScroll.setAlignmentX(LEFT_ALIGNMENT);

        JPanel dlgBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        dlgBtnRow.setBackground(Color.WHITE);
        dlgBtnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton removeBtn = new JButton("Remove Work");
        removeBtn.setFont(MainWindow.FONT_SMALL);
        removeBtn.setBackground(MainWindow.DANGER_BG);
        removeBtn.setForeground(MainWindow.DANGER_FG);
        removeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.DANGER_FG),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        removeBtn.setFocusPainted(false);
        removeBtn.setOpaque(true);
        removeBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        removeBtn.setContentAreaFilled(true);
        removeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeBtn.addActionListener(e -> {
            int confirm = javax.swing.JOptionPane.showConfirmDialog(
                    dlg,
                    "Are you sure you want to remove \"" + w.getTitle() + "\"?\nThis cannot be undone.",
                    "Confirm Remove",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                DataStore.get().removeWork(w.getWorkId());
                dlg.dispose();
                refreshContentTab();
                refreshOverviewTab();
                MainWindow.showToast(window, "\"" + w.getTitle() + "\" has been removed.", true);
            }
        });

        JButton closeBtn = MainWindow.styledButton("Close", false);
        closeBtn.addActionListener(e -> dlg.dispose());
        dlgBtnRow.add(removeBtn);
        dlgBtnRow.add(closeBtn);

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(8));
        panel.add(metaRow);
        panel.add(Box.createVerticalStrut(10));
        if (reportCard != null) { panel.add(reportCard); panel.add(Box.createVerticalStrut(10)); }
        panel.add(contentTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(contentScroll);
        panel.add(Box.createVerticalStrut(14));
        panel.add(dlgBtnRow);

        dlg.add(panel);
        dlg.setVisible(true);
    }

    private void addMeta(JPanel row, String label, String value) {
        JLabel l = new JLabel("<html><b>" + label + ":</b> " + value + "</html>");
        l.setFont(MainWindow.FONT_SMALL); l.setForeground(MainWindow.TEXT_MUTED);
        row.add(l);
    }

    // ── SHARED HELPERS ────────────────────────────────────────────
    private JLabel buildAvatar(String initials, Color bgColor, int size) {
        return new JLabel(initials) {
            @Override public void paintComponent(Graphics g) {
                g.setColor(bgColor); g.fillOval(0, 0, size, size);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, size <= 28 ? 10 : 12));
                java.awt.FontMetrics fm = g.getFontMetrics();
                String t = getText();
                g.drawString(t, (size - fm.stringWidth(t)) / 2, (size - fm.getHeight()) / 2 + fm.getAscent());
            }
            @Override public Dimension getPreferredSize() { return new Dimension(size, size); }
        };
    }

    private String getInitials(String username) {
        if (username == null || username.isEmpty()) return "?";
        String[] parts = username.trim().split("[\\s\\-_]+");
        if (parts.length >= 2 && !parts[1].isEmpty())
            return "" + Character.toUpperCase(parts[0].charAt(0)) + Character.toUpperCase(parts[1].charAt(0));
        return username.substring(0, Math.min(2, username.length())).toUpperCase();
    }

    private String truncate(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max) + "\u2026" : s;
    }

    private JLabel sectionLbl(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(MainWindow.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(MainWindow.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel hdrCell(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(MainWindow.TEXT_MUTED);
        return l;
    }

    private JLabel makeChipLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(color);
        return l;
    }

    private JTextField makeInputField() {
        JTextField f = new JTextField();
        f.setFont(MainWindow.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }

    private JPanel buildStatCard(String label, JLabel valLabel, Color valueColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        valLabel.setFont(new Font("Serif", Font.BOLD, 28));
        valLabel.setForeground(valueColor);
        valLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lblLbl = new JLabel(label, SwingConstants.CENTER);
        lblLbl.setFont(MainWindow.FONT_SMALL); lblLbl.setForeground(MainWindow.TEXT_MUTED);
        card.add(valLabel); card.add(lblLbl);
        return card;
    }

    private JPanel buildSectionBanner(String text, Color fg, Color bg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        p.setBackground(bg); p.setBorder(BorderFactory.createLineBorder(fg, 1));
        p.setAlignmentX(LEFT_ALIGNMENT); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel l = new JLabel(text); l.setFont(new Font("SansSerif", Font.BOLD, 12)); l.setForeground(fg);
        p.add(l); return p;
    }

    private JScrollPane wrapScroll(java.awt.Component c) {
        JScrollPane sp = new JScrollPane(c,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(MainWindow.BG_PARCHMENT);
        return sp;
    }

    private static final class CardLayout2 extends java.awt.CardLayout {
        CardLayout2() { super(0, 0); }
    }
}

