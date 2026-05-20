package inkcanvas.ui;

import inkcanvas.ds.MyList;
import inkcanvas.model.Competition;
import inkcanvas.model.Work;
import inkcanvas.service.DataStore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class CompetitionPanel extends JPanel {

    private MainWindow window;
    private JLabel     daysLabel;
    private JLabel     hrsLabel;
    private JLabel     minLabel;
    private JLabel     themeLabel;
    private JLabel     monthLabel;
    private JPanel     leaderPanel;

    public CompetitionPanel(MainWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(MainWindow.BG_PARCHMENT);
        buildUI();
    }

    // ─────────────────────────────────────────────────────────────
    private void buildUI() {

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(MainWindow.BG_PARCHMENT);
        outer.setBorder(BorderFactory.createEmptyBorder(4, 4, 20, 4));

        outer.add(buildBanner());
        outer.add(Box.createVerticalStrut(16));
        outer.add(buildCriteriaSection());
        outer.add(Box.createVerticalStrut(12));
        outer.add(buildLeaderSection());

        JScrollPane scroll = new JScrollPane(outer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(MainWindow.BG_PARCHMENT);

        add(scroll, BorderLayout.CENTER);
    }

    // ── Banner ────────────────────────────────────────────────────
    private JPanel buildBanner() {

        JPanel banner = new JPanel(new BorderLayout(0, 10));
        banner.setBackground(new Color(44, 26, 14));
        banner.setBorder(BorderFactory.createEmptyBorder(26, 28, 22, 28));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        banner.setAlignmentX(LEFT_ALIGNMENT);

        // Top: month + theme + description
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(new Color(44, 26, 14));

        // monthLabel and themeLabel are instance fields — refresh() updates them live
        monthLabel = new JLabel();
        monthLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        monthLabel.setForeground(new Color(180, 160, 140));
        monthLabel.setAlignmentX(LEFT_ALIGNMENT);

        themeLabel = new JLabel();
        themeLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 28));
        themeLabel.setForeground(Color.WHITE);
        themeLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel desc = new JLabel("Write a story, poem, or piece exploring the current theme.");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(new Color(180, 160, 140));
        desc.setAlignmentX(LEFT_ALIGNMENT);

        top.add(monthLabel);
        top.add(Box.createVerticalStrut(6));
        top.add(themeLabel);
        top.add(Box.createVerticalStrut(4));
        top.add(desc);

        // Bottom: countdown units
        JPanel timerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        timerRow.setBackground(new Color(44, 26, 14));

        JPanel daysUnit = makeTimerUnit();
        JPanel hrsUnit  = makeTimerUnit();
        JPanel minUnit  = makeTimerUnit();

        daysLabel = (JLabel) daysUnit.getComponent(0);
        hrsLabel  = (JLabel) hrsUnit.getComponent(0);
        minLabel  = (JLabel) minUnit.getComponent(0);

        ((JLabel) daysUnit.getComponent(1)).setText("DAYS");
        ((JLabel) hrsUnit.getComponent(1)).setText("HRS");
        ((JLabel) minUnit.getComponent(1)).setText("MIN");

        timerRow.add(daysUnit);
        timerRow.add(hrsUnit);
        timerRow.add(minUnit);

        banner.add(top,      BorderLayout.CENTER);
        banner.add(timerRow, BorderLayout.SOUTH);

        return banner;
    }

    private JPanel makeTimerUnit() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(new Color(44, 26, 14));
        p.setPreferredSize(new Dimension(56, 48));

        JLabel numLbl = new JLabel("--", SwingConstants.CENTER);
        numLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        numLbl.setForeground(Color.WHITE);

        JLabel unitLbl = new JLabel("", SwingConstants.CENTER);
        unitLbl.setFont(new Font("SansSerif", Font.PLAIN, 9));
        unitLbl.setForeground(new Color(160, 140, 120));

        p.add(numLbl);
        p.add(unitLbl);
        return p;
    }

    // ── Criteria section ──────────────────────────────────────────
    private JPanel buildCriteriaSection() {

        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(MainWindow.BG_PARCHMENT);
        section.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = sectionTitle("Quality Criteria");
        section.add(title);
        section.add(Box.createVerticalStrut(8));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        chips.setBackground(MainWindow.BG_PARCHMENT);
        chips.setAlignmentX(LEFT_ALIGNMENT);

        String[][] criteria = {
            { "Hook",      "Opening engagement"     },
            { "Voice",     "Unique & consistent"    },
            { "Theme",     "Creative use of prompt" },
            { "Structure", "Logical narrative flow" },
            { "Impact",    "Lasting impression"     }
        };

        for (String[] c : criteria) {
            chips.add(makeCriteriaChip(c[0], c[1]));
        }

        section.add(chips);
        return section;
    }

    private JPanel makeCriteriaChip(String name, String description) {
        JPanel chip = new JPanel(new GridLayout(2, 1, 0, 3));
        chip.setBackground(Color.WHITE);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        chip.setPreferredSize(new Dimension(148, 52));

        JLabel nameLbl = new JLabel("\u2726 " + name);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLbl.setForeground(MainWindow.ACCENT);

        JLabel descLbl = new JLabel(description);
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        descLbl.setForeground(MainWindow.TEXT_MUTED);

        chip.add(nameLbl);
        chip.add(descLbl);
        return chip;
    }

    // ── Leaderboard section ───────────────────────────────────────
    private JPanel buildLeaderSection() {

        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(MainWindow.BG_PARCHMENT);
        section.setAlignmentX(LEFT_ALIGNMENT);

        // Title row with submit button
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(MainWindow.BG_PARCHMENT);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        titleRow.add(sectionTitle("Leaderboard"), BorderLayout.WEST);

        JButton submitBtn = MainWindow.styledButton("Submit My Work", true);
        submitBtn.addActionListener(e -> window.showPanel(MainWindow.WRITE));
        titleRow.add(submitBtn, BorderLayout.EAST);

        section.add(titleRow);
        section.add(Box.createVerticalStrut(10));

        // The actual leaderboard table
        leaderPanel = new JPanel();
        leaderPanel.setLayout(new BoxLayout(leaderPanel, BoxLayout.Y_AXIS));
        leaderPanel.setBackground(Color.WHITE);
        leaderPanel.setBorder(BorderFactory.createLineBorder(MainWindow.BORDER_COLOR));
        leaderPanel.setAlignmentX(LEFT_ALIGNMENT);

        section.add(leaderPanel);
        return section;
    }

    // ── Refresh ───────────────────────────────────────────────────
    /**
     * Called whenever this panel becomes visible (e.g. tab switch) and after
     * the admin saves a new competition theme. Reads all live data from DataStore
     * so the banner and leaderboard are always up-to-date.
     */
    public void refresh() {
        updateTimer();
        buildLeaderboard();
    }

    private void updateTimer() {
        Competition comp = DataStore.get().getCompetition();
        LocalDate   end  = comp.getEndDate();
        LocalDate   now  = LocalDate.now();

        long days = ChronoUnit.DAYS.between(now, end);
        if (days < 0) days = 0;

        if (daysLabel  != null) daysLabel.setText(String.valueOf(days));
        if (hrsLabel   != null) hrsLabel.setText("00");
        if (minLabel   != null) minLabel.setText("00");

        // Always re-read from DataStore — never use a cached/hardcoded value
        if (themeLabel != null) themeLabel.setText("\"" + comp.getTheme() + "\"");
        if (monthLabel != null) monthLabel.setText(comp.getMonth().toUpperCase() + " COMPETITION");
    }

    private void buildLeaderboard() {
        leaderPanel.removeAll();

        // ── Header row ────────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(1, 4, 0, 0));
        header.setBackground(MainWindow.BG_CREAM);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        header.add(headerCell("Rank"));
        header.add(headerCell("Work / Author"));
        header.add(headerCell("Submitted"));
        header.add(headerCell("Rating"));
        leaderPanel.add(header);

        // ── Data rows ─────────────────────────────────────────
        MyList<Work> entries = DataStore.get().getSortedCompetitionEntries();

        if (entries.isEmpty()) {
            JPanel empty = new JPanel(new FlowLayout(FlowLayout.LEFT));
            empty.setBackground(Color.WHITE);
            JLabel lbl = new JLabel("No competition entries yet. Be the first to submit!");
            lbl.setFont(MainWindow.FONT_BODY);
            lbl.setForeground(MainWindow.TEXT_MUTED);
            lbl.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));
            empty.add(lbl);
            leaderPanel.add(empty);
        } else {
            String[] medals = { "\uD83C\uDFC6", "\uD83E\uDD48", "\uD83E\uDD49" };
            for (int i = 0; i < entries.size(); i++) {
                String rankStr = (i < 3) ? medals[i] : String.valueOf(i + 1);
                leaderPanel.add(makeLeaderRow(i + 1, entries.get(i), rankStr));
            }
        }

        leaderPanel.revalidate();
        leaderPanel.repaint();
    }

    private JPanel makeLeaderRow(int rank, Work w, String rankLabel) {

        JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Rank
        JLabel rankLbl = new JLabel(rankLabel, SwingConstants.CENTER);
        rankLbl.setFont(new Font("Serif", Font.BOLD, 18));
        if      (rank == 1) rankLbl.setForeground(new Color(200, 162, 74));
        else if (rank == 2) rankLbl.setForeground(new Color(160, 160, 160));
        else if (rank == 3) rankLbl.setForeground(new Color(184, 115, 51));
        else                rankLbl.setForeground(MainWindow.TEXT_MUTED);

        // Work title + author
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLbl = new JLabel(w.getTitle());
        titleLbl.setFont(new Font("Serif", Font.BOLD, 13));
        titleLbl.setForeground(MainWindow.TEXT_MAIN);
        JLabel authLbl = new JLabel(w.getAuthorName());
        authLbl.setFont(MainWindow.FONT_SMALL);
        authLbl.setForeground(MainWindow.TEXT_MUTED);
        titlePanel.add(titleLbl);
        titlePanel.add(authLbl);

        // Date
        JLabel dateLbl = new JLabel(w.getPublishedDateFormatted());
        dateLbl.setFont(MainWindow.FONT_SMALL);
        dateLbl.setForeground(MainWindow.TEXT_MUTED);
        dateLbl.setHorizontalAlignment(SwingConstants.CENTER);

        // Rating
        double avg = w.getAverageRating();
        JLabel ratingLbl = new JLabel(buildStars(avg) + "  " + String.format("%.1f", avg));
        ratingLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        ratingLbl.setForeground(new Color(200, 162, 74));
        ratingLbl.setHorizontalAlignment(SwingConstants.CENTER);

        row.add(rankLbl);
        row.add(titlePanel);
        row.add(dateLbl);
        row.add(ratingLbl);

        // Hover + click
        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                window.openWork(w.getWorkId(), MainWindow.COMPETITION);
            }
            public void mouseEntered(MouseEvent e) {
                row.setBackground(MainWindow.BG_PARCHMENT);
                titlePanel.setBackground(MainWindow.BG_PARCHMENT);
            }
            public void mouseExited(MouseEvent e) {
                row.setBackground(Color.WHITE);
                titlePanel.setBackground(Color.WHITE);
            }
        });

        return row;
    }

    // ── Helpers ───────────────────────────────────────────────────
    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(MainWindow.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel headerCell(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(MainWindow.TEXT_MUTED);
        return l;
    }

    private String buildStars(double avg) {
        int full = (int) Math.round(avg);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < full ? "\u2605" : "\u2606");
        return sb.toString();
    }
}
