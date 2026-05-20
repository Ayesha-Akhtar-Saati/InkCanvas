package inkcanvas.ui;

import inkcanvas.ds.MyList;
import inkcanvas.model.ReportLog;
import inkcanvas.model.User;
import inkcanvas.service.DataStore;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * ReportPanel — visible to regular users (VIEW ONLY).
 * FIX 4: Shows report reason, work title, and reporter details.
 * No block/unblock buttons — admin-only via AdminPanel.
 */
public class ReportPanel extends JPanel {

    private MainWindow window;
    private JLabel     statReview;
    private JLabel     statBlocked;
    private JLabel     statTotal;
    private JPanel     accountsPanel;
    private JPanel     logPanel;

    public ReportPanel(MainWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(MainWindow.BG_PARCHMENT);
        buildUI();
    }

    private void buildUI() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(MainWindow.BG_PARCHMENT);
        outer.setBorder(BorderFactory.createEmptyBorder(6, 6, 20, 6));

        // ── Info notice ──────────────────────────────────────────
        JPanel notice = new JPanel(new BorderLayout());
        notice.setBackground(new Color(240, 248, 255));
        notice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        notice.setAlignmentX(LEFT_ALIGNMENT);
        notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JPanel noticeText = new JPanel(new GridLayout(2, 1));
        noticeText.setBackground(new Color(240, 248, 255));
        JLabel noticeTitle = new JLabel("\u2139\uFE0F  Report Information (View Only)");
        noticeTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        noticeTitle.setForeground(new Color(37, 99, 235));
        JLabel noticeSub = new JLabel("Only admins can block or unblock accounts. Use the Read page to report a specific work.");
        noticeSub.setFont(MainWindow.FONT_SMALL);
        noticeSub.setForeground(MainWindow.TEXT_MUTED);
        noticeText.add(noticeTitle);
        noticeText.add(noticeSub);
        notice.add(noticeText, BorderLayout.CENTER);

        // ── Stats ────────────────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setBackground(MainWindow.BG_PARCHMENT);
        statsRow.setAlignmentX(LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        statReview  = new JLabel("0");
        statBlocked = new JLabel("0");
        statTotal   = new JLabel("0");

        statsRow.add(buildStatCard(statReview,  "Under Review",     MainWindow.WARN_FG));
        statsRow.add(buildStatCard(statBlocked, "Accounts Blocked", MainWindow.DANGER_FG));
        statsRow.add(buildStatCard(statTotal,   "Total Reports",    MainWindow.TEXT_MUTED));

        // ── Reported accounts ────────────────────────────────────
        JLabel accsTitle = sectionHeading("Reported Accounts");
        accountsPanel = new JPanel();
        accountsPanel.setLayout(new BoxLayout(accountsPanel, BoxLayout.Y_AXIS));
        accountsPanel.setBackground(MainWindow.BG_PARCHMENT);
        accountsPanel.setAlignmentX(LEFT_ALIGNMENT);

        // ── Activity log ─────────────────────────────────────────
        JLabel logTitle = sectionHeading("Activity Log (with Report Details)");
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(Color.WHITE);
        logPanel.setBorder(BorderFactory.createLineBorder(MainWindow.BORDER_COLOR));
        logPanel.setAlignmentX(LEFT_ALIGNMENT);

        outer.add(notice);
        outer.add(Box.createVerticalStrut(14));
        outer.add(statsRow);
        outer.add(Box.createVerticalStrut(18));
        outer.add(accsTitle);
        outer.add(Box.createVerticalStrut(10));
        outer.add(accountsPanel);
        outer.add(Box.createVerticalStrut(18));
        outer.add(logTitle);
        outer.add(Box.createVerticalStrut(8));
        outer.add(logPanel);

        JScrollPane scroll = new JScrollPane(outer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(MainWindow.BG_PARCHMENT);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        refreshStats();
        refreshAccounts();
        refreshLog();
    }

    private void refreshStats() {
        MyList<User> reported = DataStore.get().getReportedUsers();
        int underReview = 0, blocked = 0, total = 0;
        for (int i = 0; i < reported.size(); i++) {
            User u = reported.get(i);
            if (u.isBlocked()) blocked++;
            else               underReview++;
            total += u.getReportCount();
        }
        statReview.setText(String.valueOf(underReview));
        statBlocked.setText(String.valueOf(blocked));
        statTotal.setText(String.valueOf(total));
    }

    private void refreshAccounts() {
        accountsPanel.removeAll();
        MyList<User> reported = DataStore.get().getReportedUsers();

        if (reported.isEmpty()) {
            JLabel none = new JLabel("No reported accounts at this time.");
            none.setFont(MainWindow.FONT_BODY);
            none.setForeground(MainWindow.TEXT_MUTED);
            none.setAlignmentX(LEFT_ALIGNMENT);
            accountsPanel.add(none);
        } else {
            for (int i = 0; i < reported.size(); i++) {
                accountsPanel.add(buildAccountRow(reported.get(i)));
                accountsPanel.add(Box.createVerticalStrut(10));
            }
        }
        accountsPanel.revalidate();
        accountsPanel.repaint();
    }

    private JPanel buildAccountRow(User u) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        row.setAlignmentX(LEFT_ALIGNMENT);

        // Avatar
        String[] parts = u.getUsername().trim().split("\\s+");
        String initials = parts.length >= 2
                ? "" + Character.toUpperCase(parts[0].charAt(0))
                    + Character.toUpperCase(parts[1].charAt(0))
                : u.getUsername().substring(0, Math.min(2, u.getUsername().length())).toUpperCase();
        Color avatarBg = u.isBlocked() ? MainWindow.DANGER_FG
                : (u.getReportCount() >= 5 ? MainWindow.WARN_FG : MainWindow.TEXT_MUTED);

        JLabel avatar = new JLabel(initials) {
            public void paintComponent(Graphics g) {
                g.setColor(avatarBg);
                g.fillOval(0, 0, 38, 38);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                java.awt.FontMetrics fm = g.getFontMetrics();
                String t = getText();
                g.drawString(t, (38 - fm.stringWidth(t)) / 2,
                        (38 - fm.getHeight()) / 2 + fm.getAscent());
            }
            public Dimension getPreferredSize() { return new Dimension(38, 38); }
        };

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 3));
        info.setBackground(Color.WHITE);
        JLabel nameLbl = new JLabel(u.getUsername());
        nameLbl.setFont(MainWindow.FONT_BOLD);
        nameLbl.setForeground(MainWindow.TEXT_MAIN);
        JLabel metaLbl = new JLabel("Joined " + u.getJoinDateFormatted()
                + "  \u00b7  " + u.getReportCount() + " report"
                + (u.getReportCount() != 1 ? "s" : ""));
        metaLbl.setFont(MainWindow.FONT_SMALL);
        metaLbl.setForeground(MainWindow.TEXT_MUTED);
        info.add(nameLbl); info.add(metaLbl);

        // Progress bar
        JPanel barPanel = new JPanel(new BorderLayout(0, 3));
        barPanel.setBackground(Color.WHITE);
        barPanel.setPreferredSize(new Dimension(170, 36));
        JProgressBar bar = new JProgressBar(0, 10);
        bar.setValue(Math.min(u.getReportCount(), 10));
        bar.setBackground(MainWindow.BG_CREAM);
        bar.setForeground(u.isBlocked() ? MainWindow.DANGER_FG
                : (u.getReportCount() >= 5 ? MainWindow.WARN_FG : MainWindow.SUCCESS_FG));
        JLabel barLbl = new JLabel(u.getReportCount() + " / 10");
        barLbl.setFont(MainWindow.FONT_SMALL);
        barLbl.setForeground(MainWindow.TEXT_MUTED);
        barPanel.add(bar, BorderLayout.NORTH); barPanel.add(barLbl, BorderLayout.CENTER);

        // Status badge (view only — no buttons)
        String statusText = u.isBlocked() ? "Blocked"
                : (u.getReportCount() >= 5 ? "Warned" : "Notified");
        Color statusBg = u.isBlocked() ? MainWindow.DANGER_BG
                : (u.getReportCount() >= 5 ? MainWindow.WARN_BG : MainWindow.SUCCESS_BG);
        Color statusFg = u.isBlocked() ? MainWindow.DANGER_FG
                : (u.getReportCount() >= 5 ? MainWindow.WARN_FG : MainWindow.SUCCESS_FG);
        JLabel badge = new JLabel(statusText);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setBackground(statusBg); badge.setForeground(statusFg);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 9, 2, 9));

        JPanel middle = new JPanel(new BorderLayout(20, 0));
        middle.setBackground(Color.WHITE);
        middle.add(info, BorderLayout.CENTER);
        middle.add(barPanel, BorderLayout.EAST);

        row.add(avatar, BorderLayout.WEST);
        row.add(middle, BorderLayout.CENTER);
        row.add(badge,  BorderLayout.EAST);
        return row;
    }

    private void refreshLog() {
        logPanel.removeAll();

        // Header
        JPanel header = new JPanel(new GridLayout(1, 4, 0, 0));
        header.setBackground(MainWindow.BG_CREAM);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        header.add(hdrCell("Time"));
        header.add(hdrCell("Event / Work"));
        header.add(hdrCell("Reason"));
        header.add(hdrCell("Type"));
        logPanel.add(header);

        MyList<ReportLog> logs = DataStore.get().getReportLogs();
        if (logs.isEmpty()) {
            JLabel none = new JLabel("  No activity logged yet.");
            none.setFont(MainWindow.FONT_BODY);
            none.setForeground(MainWindow.TEXT_MUTED);
            none.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
            logPanel.add(none);
        } else {
            for (int i = 0; i < logs.size(); i++) logPanel.add(buildLogRow(logs.get(i)));
        }
        logPanel.revalidate();
        logPanel.repaint();
    }

    private JPanel buildLogRow(ReportLog log) {
        JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        String icon = log.getType().equals("BLOCKED")   ? "\uD83D\uDEAB"
                    : log.getType().equals("NOTIFIED")  ? "\u26A0\uFE0F"
                    : log.getType().equals("UNBLOCKED") ? "\u2705" : "\u2691";

        JLabel timeLbl = new JLabel(icon + " " + log.getFormattedTime());
        timeLbl.setFont(MainWindow.FONT_SMALL);
        timeLbl.setForeground(MainWindow.TEXT_MUTED);

        // FIX 4: show work title and message
        String eventText = log.getWorkTitle() != null && !log.getWorkTitle().isEmpty()
                ? "\"" + log.getWorkTitle() + "\""
                : log.getMessage();
        JLabel msgLbl = new JLabel("<html><div style='width:160px'>" + eventText + "</div></html>");
        msgLbl.setFont(MainWindow.FONT_BODY);
        msgLbl.setForeground(MainWindow.TEXT_MAIN);

        // FIX 4: show reason
        String reasonText = log.getReason() != null && !log.getReason().isEmpty()
                ? log.getReason() : "\u2014";
        JLabel reasonLbl = new JLabel(reasonText);
        reasonLbl.setFont(MainWindow.FONT_SMALL);
        reasonLbl.setForeground(MainWindow.TEXT_MUTED);

        Color typeFg = log.getType().equals("BLOCKED")   ? MainWindow.DANGER_FG
                     : log.getType().equals("NOTIFIED")  ? MainWindow.WARN_FG
                     : log.getType().equals("UNBLOCKED") ? MainWindow.SUCCESS_FG
                     : MainWindow.TEXT_MUTED;
        JLabel typeLbl = new JLabel(log.getType());
        typeLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        typeLbl.setForeground(typeFg);

        row.add(timeLbl); row.add(msgLbl); row.add(reasonLbl); row.add(typeLbl);
        return row;
    }

    private JPanel buildStatCard(JLabel numLabel, String labelText, Color numColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        numLabel.setFont(new Font("Serif", Font.BOLD, 26));
        numLabel.setForeground(numColor);
        numLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setFont(MainWindow.FONT_SMALL);
        lbl.setForeground(MainWindow.TEXT_MUTED);
        card.add(numLabel); card.add(lbl);
        return card;
    }

    private JLabel sectionHeading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Serif", Font.BOLD, 15));
        l.setForeground(MainWindow.TEXT_MAIN);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel hdrCell(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(MainWindow.TEXT_MUTED);
        return l;
    }
}