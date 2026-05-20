package inkcanvas.ui;

import inkcanvas.ds.MyList;
import inkcanvas.model.Work;
import inkcanvas.service.DataStore;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Discover panel — shows all published works.
 *
 * BUG-FIX (Bug 2 — Discover Page Visibility Issue):
 * ──────────────────────────────────────────────────
 * Root cause 1 — WrapLayout width fallback:
 *   WrapLayout.layout() used Integer.MAX_VALUE as a fallback when the
 *   container width was 0 (which happens while the panel is hidden behind
 *   another CardLayout card).  With an effectively infinite row-width every
 *   card was placed on a single invisible row, so the scroll-pane computed a
 *   preferred height equal to only one card tall and never updated after
 *   navigation.  Fix: fall back to 640 px instead of MAX_VALUE.
 *
 * Root cause 2 — stale scroll-pane layout after CardLayout switch:
 *   refresh() called worksPanel.revalidate() but never revalidated the
 *   JScrollPane itself, so the scroll-pane kept its cached (wrong) preferred
 *   height from the first (hidden) layout pass.  Fix: promote 'scroll' from a
 *   local variable to a class field and call scroll.revalidate() inside an
 *   invokeLater() at the end of refresh() so the layout is recalculated after
 *   the CardLayout animation completes.
 */
public class DiscoverPanel extends JPanel {

    private MainWindow        window;
    private JTextField        searchField;
    private JComboBox<String> searchTypeBox;
    private JComboBox<String> genreFilterBox;
    private JPanel            worksPanel;
    private JLabel            resultCount;

    // FIX 2 — promoted from local variable so refresh() can revalidate it
    private JScrollPane scroll;

    public DiscoverPanel(MainWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(MainWindow.BG_PARCHMENT);
        buildUI();
    }

    private void buildUI() {
        // ── Top search bar ──────────────────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topBar.setBackground(MainWindow.BG_PARCHMENT);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(MainWindow.FONT_BOLD);
        searchLbl.setForeground(MainWindow.TEXT_MUTED);

        searchField = new JTextField(22);
        searchField.setFont(MainWindow.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        searchTypeBox = new JComboBox<>(new String[]{"Keyword", "Author", "Title"});
        searchTypeBox.setFont(MainWindow.FONT_BODY);
        searchTypeBox.setBackground(Color.WHITE);
        searchTypeBox.setForeground(MainWindow.TEXT_MAIN);

        // Genre filter — placeholder; rebuilt in refresh()
        genreFilterBox = new JComboBox<>(new String[]{"All Genres"});
        genreFilterBox.setFont(MainWindow.FONT_BODY);
        genreFilterBox.setBackground(Color.WHITE);
        genreFilterBox.setForeground(MainWindow.TEXT_MAIN);

        JButton searchBtn = MainWindow.styledButton("Search", true);
        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());

        resultCount = new JLabel("");
        resultCount.setFont(MainWindow.FONT_SMALL);
        resultCount.setForeground(MainWindow.TEXT_MUTED);

        topBar.add(searchLbl);
        topBar.add(searchField);
        topBar.add(searchTypeBox);
        topBar.add(genreFilterBox);
        topBar.add(searchBtn);
        topBar.add(resultCount);

        // ── Works grid ──────────────────────────────────────────
        worksPanel = new JPanel();
        worksPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 14, 14));
        worksPanel.setBackground(MainWindow.BG_PARCHMENT);

        // FIX 2 — assign to field instead of local variable
        scroll = new JScrollPane(worksPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(MainWindow.BG_PARCHMENT);

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Called every time the user navigates to Discover.
     *
     * FIX 2a — re-reads published works from DataStore on every navigation.
     * FIX 2b — forces the scroll-pane to redo its layout on the EDT after the
     *           CardLayout switch completes, so newly-published works appear.
     */
    public void refresh() {
        rebuildGenreFilter();
        searchField.setText("");
        searchTypeBox.setSelectedIndex(0);
        loadWorks(DataStore.get().getPublishedWorks());

        // FIX 2b — run after the current event (CardLayout show) has finished
        // so the scroll-pane measures real pixel dimensions, not zero.
        SwingUtilities.invokeLater(() -> {
            scroll.revalidate();
            scroll.repaint();
        });
    }

    /** Rebuilds the genre combo box from DataStore (picks up custom genres). */
    private void rebuildGenreFilter() {
        genreFilterBox.removeAllItems();
        genreFilterBox.addItem("All Genres");
        MyList<String> genres = DataStore.get().getGenres();
        for (int i = 0; i < genres.size(); i++) {
            genreFilterBox.addItem(genres.get(i));
        }
    }

    private void doSearch() {
        String query = searchField.getText().trim();
        String type  = (String) searchTypeBox.getSelectedItem();
        String genre = (String) genreFilterBox.getSelectedItem();

        MyList<Work> results = DataStore.get().searchWorks(query, type);

        if (genre != null && !genre.equals("All Genres")) {
            MyList<Work> filtered = new MyList<>();
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).getGenre().equals(genre)) filtered.add(results.get(i));
            }
            results = filtered;
        }
        loadWorks(results);
    }

    private void loadWorks(MyList<Work> list) {
        worksPanel.removeAll();

        if (list.isEmpty()) {
            JLabel empty = new JLabel("No works found. Try a different search.");
            empty.setFont(MainWindow.FONT_BODY);
            empty.setForeground(MainWindow.TEXT_MUTED);
            empty.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
            worksPanel.add(empty);
        } else {
            for (int i = 0; i < list.size(); i++) {
                worksPanel.add(makeWorkCard(list.get(i)));
            }
        }

        int n = list.size();
        resultCount.setText(n + " work" + (n != 1 ? "s" : ""));
        worksPanel.revalidate();
        worksPanel.repaint();
    }

    private JPanel makeWorkCard(Work w) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(230, 210));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel genre = new JLabel(w.getGenre().toUpperCase());
        genre.setFont(new Font("SansSerif", Font.BOLD, 10));
        genre.setForeground(MainWindow.ACCENT);
        genre.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("<html><b>" + w.getTitle() + "</b></html>");
        title.setFont(new Font("Serif", Font.BOLD, 14));
        title.setForeground(MainWindow.TEXT_MAIN);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel author = new JLabel("by " + w.getAuthorName());
        author.setFont(MainWindow.FONT_SMALL);
        author.setForeground(MainWindow.TEXT_MUTED);
        author.setAlignmentX(LEFT_ALIGNMENT);

        String excerpt = w.getContent().length() > 100
                ? w.getContent().substring(0, 100).replace("\n", " ") + "..."
                : w.getContent().replace("\n", " ");
        JLabel ex = new JLabel("<html><div style='width:190px'>" + excerpt + "</div></html>");
        ex.setFont(new Font("SansSerif", Font.PLAIN, 11));
        ex.setForeground(MainWindow.TEXT_MUTED);
        ex.setAlignmentX(LEFT_ALIGNMENT);

        double avg   = w.getAverageRating();
        String stars = buildStars(avg);
        JLabel rating = new JLabel(stars + "  " + String.format("%.1f", avg)
                + " (" + w.getRatings().size() + ")   " + w.getReadCount() + " reads");
        rating.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rating.setForeground(MainWindow.TEXT_MUTED);
        rating.setAlignmentX(LEFT_ALIGNMENT);

        card.add(genre);
        card.add(Box.createVerticalStrut(5));
        card.add(title);
        card.add(Box.createVerticalStrut(3));
        card.add(author);
        card.add(Box.createVerticalStrut(8));
        card.add(ex);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(8));
        card.add(rating);

        final JPanel c = card;
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                window.openWork(w.getWorkId(), MainWindow.DISCOVER);
            }
            public void mouseEntered(MouseEvent e) {
                c.setBackground(new Color(252, 250, 248));
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(MainWindow.ACCENT),
                        BorderFactory.createEmptyBorder(14, 14, 14, 14)));
            }
            public void mouseExited(MouseEvent e) {
                c.setBackground(Color.WHITE);
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                        BorderFactory.createEmptyBorder(14, 14, 14, 14)));
            }
        });
        return card;
    }

    private String buildStars(double avg) {
        int full = (int) Math.round(avg);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < full ? "\u2605" : "\u2606");
        return sb.toString();
    }

    // ── WrapLayout (inner class) — FIX 2a ────────────────────────────────────
    /**
     * Flow-layout that wraps cards onto multiple rows inside a JScrollPane.
     *
     * FIX 2a: When the container width is 0 (panel not yet shown on screen,
     * e.g. hidden behind a CardLayout card) we now fall back to 640 px instead
     * of Integer.MAX_VALUE.  The old MAX_VALUE fallback caused all cards to be
     * placed on a single, very wide row; the scroll-pane then computed a
     * preferred height equal to only one row, and never re-measured after the
     * panel became visible — so newly-published works were invisible.
     */
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        public Dimension preferredLayoutSize(Container t) { return layout(t, true); }
        public Dimension minimumLayoutSize(Container t)   { return layout(t, false); }

        private Dimension layout(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int maxW = target.getWidth();

                // FIX 2a — sensible fallback so multi-row height is computed
                // correctly even during the first (hidden) layout pass.
                if (maxW == 0) maxW = 640;

                Insets ins = target.getInsets();
                int x = ins.left + getHgap();
                int rowH = 0, totalH = ins.top + ins.bottom;

                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component c = target.getComponent(i);
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x + d.width > maxW - ins.right && x > ins.left + getHgap()) {
                        totalH += rowH + getVgap();
                        rowH = 0;
                        x = ins.left + getHgap();
                    }
                    rowH = Math.max(rowH, d.height);
                    x += d.width + getHgap();
                }
                totalH += rowH + getVgap();
                return new Dimension(maxW, totalH);
            }
        }
    }
}
