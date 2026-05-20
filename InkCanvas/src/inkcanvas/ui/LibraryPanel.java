package inkcanvas.ui;

import inkcanvas.ds.MyList;
import inkcanvas.model.Work;
import inkcanvas.service.DataStore;
import inkcanvas.service.FileManager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * My Library panel — shows all works owned by the current user.
 * FIX 1: refresh() always re-reads from DataStore so new works appear immediately.
 * FIX 2: Custom genres refresh from DataStore on every tab rebuild.
 * FIX 3: Edit button removed from Published works.
 */
public class LibraryPanel extends JPanel {

    private MainWindow window;
    private JPanel     tabBar;
    private JPanel     worksPanel;
    private JLabel     summaryLabel;
    private String     activeGenre = "All";

    public LibraryPanel(MainWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(MainWindow.BG_PARCHMENT);
        buildUI();
    }

    private void buildUI() {
        // ── Top bar ─────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(MainWindow.BG_PARCHMENT);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        summaryLabel = new JLabel("");
        summaryLabel.setFont(MainWindow.FONT_SMALL);
        summaryLabel.setForeground(MainWindow.TEXT_MUTED);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(MainWindow.BG_PARCHMENT);

        JButton addGenreBtn = MainWindow.styledButton("+ Add Genre", false);
        addGenreBtn.addActionListener(e -> addCustomGenre());

        JButton newBtn = MainWindow.styledButton("+ New Work", true);
        newBtn.addActionListener(e -> window.showPanel(MainWindow.WRITE));

        btnRow.add(addGenreBtn);
        btnRow.add(newBtn);

        top.add(summaryLabel, BorderLayout.WEST);
        top.add(btnRow,       BorderLayout.EAST);

        // ── Genre tabs ──────────────────────────────────────────
        tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        tabBar.setBackground(MainWindow.BG_PARCHMENT);

        // ── Works list ──────────────────────────────────────────
        worksPanel = new JPanel();
        worksPanel.setLayout(new BoxLayout(worksPanel, BoxLayout.Y_AXIS));
        worksPanel.setBackground(MainWindow.BG_PARCHMENT);
        worksPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JScrollPane scroll = new JScrollPane(worksPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(MainWindow.BG_PARCHMENT);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(MainWindow.BG_PARCHMENT);
        northPanel.add(top,    BorderLayout.NORTH);
        northPanel.add(tabBar, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(scroll,     BorderLayout.CENTER);
    }

    /** Called every time user navigates to My Library — always re-reads fresh data */
    public void refresh() {
        activeGenre = "All";     // reset to All on each visit
        buildTabs();
        loadWorks();
        updateSummary();
    }

    // ── Genre tabs ────────────────────────────────────────────────
    private void buildTabs() {
        tabBar.removeAll();

        // FIX 2: read genres fresh from DataStore every time
        MyList<String> genres = DataStore.get().getGenres();

        // Build "All" tab + one per genre
        String[] tabs = new String[genres.size() + 1];
        tabs[0] = "All";
        for (int i = 0; i < genres.size(); i++) tabs[i + 1] = genres.get(i);

        for (String g : tabs) {
            tabBar.add(makeTab(g));
        }
        tabBar.revalidate();
        tabBar.repaint();
    }

    private JButton makeTab(String genre) {
        String uid = DataStore.get().getCurrentUser().getUserId();
        int count = 0;
        if (genre.equals("All")) {
            count = DataStore.get().getMyWorks(uid).size();
        } else {
            MyList<Work> mine = DataStore.get().getMyWorks(uid);
            for (int i = 0; i < mine.size(); i++)
                if (mine.get(i).getGenre().equals(genre)) count++;
        }

        JButton btn = new JButton(genre + " (" + count + ")");
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        if (genre.equals(activeGenre)) {
            btn.setBackground(MainWindow.ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MainWindow.ACCENT_DARK),
                    BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(MainWindow.TEXT_MAIN);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        }
        btn.addActionListener(e -> { activeGenre = genre; buildTabs(); loadWorks(); });
        return btn;
    }

    // ── Works list ────────────────────────────────────────────────
    private void loadWorks() {
        worksPanel.removeAll();

        // FIX 1: always re-read from DataStore to get newly uploaded works
        String uid = DataStore.get().getCurrentUser().getUserId();
        MyList<Work> list = DataStore.get().getMyWorksByGenre(uid, activeGenre);

        if (list.isEmpty()) {
            JLabel empty = new JLabel("No works in this folder yet. Create one!");
            empty.setFont(MainWindow.FONT_BODY);
            empty.setForeground(MainWindow.TEXT_MUTED);
            empty.setBorder(BorderFactory.createEmptyBorder(30, 10, 10, 10));
            empty.setAlignmentX(LEFT_ALIGNMENT);
            worksPanel.add(empty);
        } else {
            for (int i = 0; i < list.size(); i++) {
                worksPanel.add(makeWorkRow(list.get(i)));
                worksPanel.add(Box.createVerticalStrut(10));
            }
        }
        worksPanel.revalidate();
        worksPanel.repaint();
    }

    private JPanel makeWorkRow(Work w) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        row.setAlignmentX(LEFT_ALIGNMENT);

        // ── Info ─────────────────────────────────────────────────
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 3));
        info.setBackground(Color.WHITE);

        JLabel title = new JLabel(w.getTitle());
        title.setFont(new Font("Serif", Font.BOLD, 14));
        title.setForeground(MainWindow.TEXT_MAIN);

        String meta = w.getGenre() + "  \u00b7  " + w.getWordCount() + " words  \u00b7  "
                + (w.isPublished()
                    ? "Published " + w.getPublishedDateFormatted()
                    : "Last edited \u2014 Draft");
        JLabel metaLbl = new JLabel(meta);
        metaLbl.setFont(MainWindow.FONT_SMALL);
        metaLbl.setForeground(MainWindow.TEXT_MUTED);

        info.add(title);
        info.add(metaLbl);

        // ── Status badge ──────────────────────────────────────────
        JLabel badge = new JLabel(w.isPublished() ? "Published" : "Draft");
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 9, 2, 9));
        if (w.isPublished()) {
            badge.setBackground(MainWindow.SUCCESS_BG);
            badge.setForeground(MainWindow.SUCCESS_FG);
        } else {
            badge.setBackground(MainWindow.BG_CREAM);
            badge.setForeground(MainWindow.TEXT_MUTED);
        }

        // ── Action buttons ────────────────────────────────────────
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setBackground(Color.WHITE);

        // FIX 3: Only show Publish button for DRAFTS — no Edit for published works
        if (!w.isPublished()) {
            JButton pubBtn = MainWindow.styledButton("Publish", true);
            pubBtn.addActionListener(e -> {
                w.publish();
                DataStore.get().saveWork(w);
                MainWindow.showToast(window, "\"" + w.getTitle() + "\" published!", true);
                refresh();
            });

            JButton editBtn = MainWindow.styledButton("Edit", false);
            editBtn.addActionListener(e -> {
                WritePanel wp = window.getWritePanel();
                if (wp != null) wp.loadWorkForEditing(w);
                window.showPanel(MainWindow.WRITE);
            });

            actions.add(pubBtn);
            actions.add(editBtn);
        }

        // Move to genre dropdown
        JComboBox<String> moveBox = buildMoveBox(w);
        actions.add(moveBox);

        JButton readBtn = MainWindow.styledButton("Read", false);
        readBtn.addActionListener(e -> window.openWork(w.getWorkId(), MainWindow.LIBRARY));
        actions.add(readBtn);

        // ── Right panel ───────────────────────────────────────────
        JPanel right = new JPanel(new BorderLayout(10, 0));
        right.setBackground(Color.WHITE);
        right.add(badge,   BorderLayout.WEST);
        right.add(actions, BorderLayout.EAST);

        row.add(info,  BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JComboBox<String> buildMoveBox(Work w) {
        MyList<String> genres = DataStore.get().getGenres();
        String[] opts = new String[genres.size() + 1];
        opts[0] = "Move to...";
        for (int i = 0; i < genres.size(); i++) opts[i + 1] = genres.get(i);

        JComboBox<String> box = new JComboBox<>(opts);
        box.setFont(MainWindow.FONT_SMALL);
        box.setBackground(Color.WHITE);
        box.addActionListener(e -> {
            String sel = (String) box.getSelectedItem();
            if (sel != null && !sel.equals("Move to...")) {
                w.setGenre(sel);
                DataStore.get().saveWork(w);
                MainWindow.showToast(window, "Moved to " + sel, true);
                refresh();
            }
        });
        return box;
    }

    private void updateSummary() {
        String uid = DataStore.get().getCurrentUser().getUserId();
        MyList<Work> all = DataStore.get().getMyWorks(uid);
        int pub = 0, draft = 0;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).isPublished()) pub++; else draft++;
        }
        summaryLabel.setText(all.size() + " works  \u00b7  "
                + pub   + " published  \u00b7  "
                + draft + " draft" + (draft != 1 ? "s" : ""));
    }

    private void addCustomGenre() {
        String name = JOptionPane.showInputDialog(window,
                "Enter a name for your custom genre:",
                "Create Genre", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            String trimmed = name.trim();
            // FIX 2: add to DataStore AND save genres to disk immediately
            DataStore.get().addCustomGenre(trimmed);
            FileManager.saveGenres(DataStore.get());
            MainWindow.showToast(window, "Genre \"" + trimmed + "\" created!", true);
            refresh(); // rebuild tabs so new genre appears immediately
        }
    }
}

