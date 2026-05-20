package inkcanvas.ui;

import inkcanvas.ds.MyList;
import inkcanvas.model.Work;
import inkcanvas.service.DataStore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * WritePanel — text editor for creating and editing works.
 *
 * Design notes
 * ────────────
 * • compCheck and compThemeLabel are instance fields so refresh() can update
 *   them every time the panel becomes visible, reading the live competition
 *   from DataStore. Nothing is hardcoded.
 * • saveWork() persists via DataStore.saveWork() which writes works.txt
 *   immediately, so newly published works appear in Discover on next navigation.
 * • loadWorkForEditing() only accepts drafts; published works are blocked.
 * • sideCardWrapper() calls MainWindow.sectionLabel() — a public static method
 *   defined in MainWindow that produces a small all-caps muted heading label.
 */
public class WritePanel extends JPanel {

    private final MainWindow        window;
    private JTextField              titleField;
    private JTextArea               bodyArea;
    private JComboBox<String>       genreBox;
    private JTextField              customGenreField;
    private JPanel                  customGenrePanel;
    private JCheckBox               compCheck;       // instance field — text set in refresh()
    private JLabel                  compThemeLabel;  // instance field — text set in refresh()
    private JLabel                  wordCountLabel;

    /** The draft currently open for editing; null when creating a new work. */
    private Work editingWork = null;

    // ─────────────────────────────────────────────────────────────
    public WritePanel(MainWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(MainWindow.BG_PARCHMENT);
        buildUI();
    }

    // ═════════════════════════════════════════════════════════════
    // BUILD UI
    // ═════════════════════════════════════════════════════════════
    private void buildUI() {

        // ── Left: editor area ────────────────────────────────────
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBackground(MainWindow.BG_PARCHMENT);
        editorPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        titleField = new JTextField();
        titleField.setFont(new Font("Serif", Font.BOLD, 24));
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 2, 10, 2)));
        titleField.setBackground(MainWindow.BG_PARCHMENT);
        titleField.setForeground(MainWindow.TEXT_MAIN);

        bodyArea = new JTextArea();
        bodyArea.setFont(new Font("Serif", Font.PLAIN, 15));
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setBackground(Color.WHITE);
        bodyArea.setForeground(MainWindow.TEXT_MAIN);
        bodyArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        bodyArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updateWordCount(); }
            public void removeUpdate(DocumentEvent e)  { updateWordCount(); }
            public void changedUpdate(DocumentEvent e) { updateWordCount(); }
        });

        JScrollPane bodyScroll = new JScrollPane(bodyArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bodyScroll.setBorder(BorderFactory.createLineBorder(MainWindow.BORDER_COLOR));

        wordCountLabel = new JLabel("0 words");
        wordCountLabel.setFont(MainWindow.FONT_SMALL);
        wordCountLabel.setForeground(MainWindow.TEXT_MUTED);
        JPanel wcRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 3));
        wcRow.setBackground(MainWindow.BG_PARCHMENT);
        wcRow.add(wordCountLabel);

        editorPanel.add(titleField,  BorderLayout.NORTH);
        editorPanel.add(bodyScroll,  BorderLayout.CENTER);
        editorPanel.add(wcRow,       BorderLayout.SOUTH);

        // ── Right: sidebar ───────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(MainWindow.BG_PARCHMENT);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Genre card
        JPanel genreCard = buildSideCard();
        MyList<String> genres = DataStore.get().getGenres();
        String[] genreOpts = new String[genres.size() + 2];
        genreOpts[0] = "Select genre...";
        for (int i = 0; i < genres.size(); i++) genreOpts[i + 1] = genres.get(i);
        genreOpts[genres.size() + 1] = "Other (custom)";

        genreBox = new JComboBox<>(genreOpts);
        genreBox.setFont(MainWindow.FONT_BODY);
        genreBox.setForeground(MainWindow.TEXT_MAIN);
        genreBox.setBackground(Color.WHITE);
        genreBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        genreBox.setAlignmentX(LEFT_ALIGNMENT);
        genreBox.addActionListener((ActionEvent e) -> {
            boolean custom = "Other (custom)".equals(genreBox.getSelectedItem());
            customGenrePanel.setVisible(custom);
        });

        customGenrePanel = new JPanel(new BorderLayout());
        customGenrePanel.setBackground(Color.WHITE);
        customGenrePanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        customGenreField = new JTextField();
        customGenreField.setFont(MainWindow.FONT_BODY);
        customGenreField.setForeground(MainWindow.TEXT_MAIN);
        customGenreField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        JLabel customHint = new JLabel("Enter custom genre name:");
        customHint.setFont(MainWindow.FONT_SMALL);
        customHint.setForeground(MainWindow.TEXT_MUTED);
        customGenrePanel.add(customHint,       BorderLayout.NORTH);
        customGenrePanel.add(customGenreField, BorderLayout.CENTER);
        customGenrePanel.setVisible(false);

        genreCard.add(genreBox);
        genreCard.add(customGenrePanel);

        // Competition card — labels set by refresh(), never hardcoded
        JPanel compCard = buildSideCard();
        compCheck = new JCheckBox();
        compCheck.setFont(MainWindow.FONT_BODY);
        compCheck.setBackground(Color.WHITE);
        compCheck.setForeground(MainWindow.TEXT_MAIN);
        compCheck.setAlignmentX(LEFT_ALIGNMENT);

        compThemeLabel = new JLabel();
        compThemeLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        compThemeLabel.setForeground(MainWindow.TEXT_MUTED);
        compThemeLabel.setAlignmentX(LEFT_ALIGNMENT);

        compCard.add(compCheck);
        compCard.add(Box.createVerticalStrut(5));
        compCard.add(compThemeLabel);

        // Actions card
        JPanel actCard = buildSideCard();
        JButton draftBtn = MainWindow.styledButton("Save as Draft", false);
        draftBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        draftBtn.setAlignmentX(LEFT_ALIGNMENT);
        draftBtn.addActionListener(e -> saveWork(false));

        JButton pubBtn = MainWindow.styledButton("Publish", true);
        pubBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        pubBtn.setAlignmentX(LEFT_ALIGNMENT);
        pubBtn.addActionListener(e -> saveWork(true));

        JButton clearBtn = MainWindow.styledButton("Clear Editor", false);
        clearBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        clearBtn.setAlignmentX(LEFT_ALIGNMENT);
        clearBtn.addActionListener(e -> clearEditor());

        actCard.add(draftBtn);
        actCard.add(Box.createVerticalStrut(8));
        actCard.add(pubBtn);
        actCard.add(Box.createVerticalStrut(8));
        actCard.add(clearBtn);

        sidebar.add(sideCardWrapper("GENRE",       genreCard));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(sideCardWrapper("COMPETITION", compCard));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(sideCardWrapper("ACTIONS",     actCard));
        sidebar.add(Box.createVerticalGlue());

        add(editorPanel, BorderLayout.CENTER);
        add(sidebar,     BorderLayout.EAST);
    }

    // ═════════════════════════════════════════════════════════════
    // REFRESH  — called by MainWindow.showPanel() every time this
    //            panel becomes visible, so competition info is live
    // ═════════════════════════════════════════════════════════════
    public void refresh() {
        inkcanvas.model.Competition comp = DataStore.get().getCompetition();
        String month = comp.getMonth();
        String theme = comp.getTheme();

        if (compCheck      != null) compCheck.setText("Submit to " + month + " competition");
        if (compThemeLabel != null) compThemeLabel.setText("Theme: \"" + theme + "\"");
    }

    // ═════════════════════════════════════════════════════════════
    // SAVE LOGIC
    // ═════════════════════════════════════════════════════════════
    private void saveWork(boolean publish) {
        String title = titleField.getText().trim();
        String body  = bodyArea.getText().trim();

        if (title.isEmpty()) {
            MainWindow.showToast(window, "Please add a title before saving.", false);
            return;
        }
        if (publish && body.isEmpty()) {
            MainWindow.showToast(window,
                    "Cannot publish an empty work. Add some content first.", false);
            return;
        }

        String genre = (String) genreBox.getSelectedItem();
        if (genre == null || genre.equals("Select genre...")) {
            MainWindow.showToast(window, "Please select a genre.", false);
            return;
        }
        if (genre.equals("Other (custom)")) {
            genre = customGenreField.getText().trim();
            if (genre.isEmpty()) {
                MainWindow.showToast(window, "Please enter a custom genre name.", false);
                return;
            }
            DataStore.get().addCustomGenre(genre);  // writes genres.txt immediately
        }

        if (editingWork != null) {
            // ── Update existing draft ──────────────────────────────
            editingWork.setTitle(title);
            editingWork.setContent(body);
            editingWork.setGenre(genre);
            if (publish && !editingWork.isPublished()) {
                editingWork.publish();
                if (compCheck.isSelected()) {
                    editingWork.setInCompetition(true);
                    DataStore.get().getCompetition().addEntry(editingWork.getWorkId());
                    DataStore.get().persistCompetition();
                }
            }
            DataStore.get().saveWork(editingWork);  // writes works.txt immediately
            MainWindow.showToast(window,
                    publish ? "\"" + title + "\" published!" : "Draft saved!", true);
        } else {
            // ── Create new work ────────────────────────────────────
            String uid  = DataStore.get().getCurrentUser().getUserId();
            String name = DataStore.get().getCurrentUser().getUsername();
            Work work = new Work(uid, name, title, body, genre);
            if (publish) {
                work.publish();
                if (compCheck.isSelected()) {
                    work.setInCompetition(true);
                    DataStore.get().getCompetition().addEntry(work.getWorkId());
                    DataStore.get().persistCompetition();
                }
            }
            DataStore.get().saveWork(work);         // writes works.txt immediately
            MainWindow.showToast(window,
                    publish ? "\"" + title + "\" published!" : "Draft saved!", true);
        }

        clearEditor();
        // Navigate: published → Discover (triggers discoverPanel.refresh())
        //           draft     → Library  (triggers libraryPanel.refresh())
        window.showPanel(publish ? MainWindow.DISCOVER : MainWindow.LIBRARY);
    }

    private void clearEditor() {
        titleField.setText("");
        bodyArea.setText("");
        genreBox.setSelectedIndex(0);
        customGenreField.setText("");
        customGenrePanel.setVisible(false);
        compCheck.setSelected(false);
        wordCountLabel.setText("0 words");
        editingWork = null;
    }

    private void updateWordCount() {
        String text  = bodyArea.getText().trim();
        int    words = text.isEmpty() ? 0 : text.split("\\s+").length;
        wordCountLabel.setText(words + " word" + (words != 1 ? "s" : ""));
    }

    // ═════════════════════════════════════════════════════════════
    // LOAD A DRAFT FOR EDITING  (published works are blocked)
    // ═════════════════════════════════════════════════════════════
    public void loadWorkForEditing(Work w) {
        if (w == null) return;
        if (w.isPublished()) {
            MainWindow.showToast(window, "Published works cannot be edited.", false);
            return;
        }
        editingWork = w;
        titleField.setText(w.getTitle());
        bodyArea.setText(w.getContent());

        String  genre = w.getGenre();
        boolean found = false;
        for (int i = 0; i < genreBox.getItemCount(); i++) {
            if (genreBox.getItemAt(i).equals(genre)) {
                genreBox.setSelectedIndex(i);
                found = true;
                break;
            }
        }
        if (!found) {
            genreBox.setSelectedItem("Other (custom)");
            customGenreField.setText(genre);
            customGenrePanel.setVisible(true);
        }
        compCheck.setSelected(w.isInCompetition());
        updateWordCount();
    }

    // ═════════════════════════════════════════════════════════════
    // SIDEBAR CARD HELPERS
    // ═════════════════════════════════════════════════════════════

    /**
     * Creates a blank inner card panel (BoxLayout Y_AXIS, white background).
     * Content is added by the caller before wrapping with sideCardWrapper().
     */
    private JPanel buildSideCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    /**
     * Wraps an inner card panel in a bordered container with a section label.
     *
     * Calls MainWindow.sectionLabel(label) — a public static method on MainWindow
     * that returns a small all-caps muted JLabel. This is the call shown in the
     * stack trace; it requires MainWindow.sectionLabel() to be public static.
     */
    private JPanel sideCardWrapper(String label, JPanel inner) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        wrapper.setAlignmentX(LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // MainWindow.sectionLabel() — must be public static on MainWindow
        JLabel lbl = MainWindow.sectionLabel(label);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(lbl);
        wrapper.add(inner);
        return wrapper;
    }
}