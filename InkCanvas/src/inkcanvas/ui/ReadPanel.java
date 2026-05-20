package inkcanvas.ui;

import inkcanvas.ds.MyList;
import inkcanvas.model.Comment;
import inkcanvas.model.Rating;
import inkcanvas.model.User;
import inkcanvas.model.Work;
import inkcanvas.service.DataStore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

/**
 * ReadPanel — full work reader with rating, comments, and report.
 *
 * Key fixes
 * ─────────
 * • reportCurrentWork() calls window.refreshAfterReport() after every
 *   successful DataStore.reportUser() call so ReportPanel and AdminPanel
 *   update in-place without the user navigating away.
 * • Author is re-fetched inside the submit lambda (not captured at dialog-open
 *   time) to prevent a stale-reference NPE if the user object changes.
 * • All Swing component interaction happens on the EDT.
 */
public class ReadPanel extends JPanel {

    private final MainWindow window;
    private String returnPanel  = MainWindow.DISCOVER;
    private int    currentWorkId;

    // Header labels
    private JLabel genreLabel;
    private JLabel titleLabel;
    private JLabel authorLabel;
    private JLabel dateLabel;
    private JLabel readsLabel;

    // Body
    private JTextArea bodyArea;

    // Rating
    private JSlider[] criteriaSliders  = new JSlider[5];
    private JLabel[]  criteriaValues   = new JLabel[5];
    private JLabel    avgRatingLabel;
    private JLabel    totalRatingsLabel;
    private JPanel    ratingPanel;

    // Comments
    private JPanel    commentsPanel;
    private JTextField commentInput;

    private static final String[] CRITERIA = {
        "The Hook", "The Voice", "The Theme", "The Structure", "The Impact"
    };

    // ─────────────────────────────────────────────────────────────
    public ReadPanel(MainWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(MainWindow.BG_PARCHMENT);
        buildUI();
    }

    // ═════════════════════════════════════════════════════════════
    // BUILD UI
    // ═════════════════════════════════════════════════════════════
    private void buildUI() {

        // Back button
        JButton backBtn = new JButton("\u2190 Back");
        backBtn.setFont(MainWindow.FONT_BODY);
        backBtn.setForeground(MainWindow.ACCENT);
        backBtn.setBackground(MainWindow.BG_PARCHMENT);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> window.showPanel(returnPanel));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(MainWindow.BG_PARCHMENT);
        topRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        topRow.add(backBtn, BorderLayout.WEST);

        // Main reading area
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(MainWindow.BG_PARCHMENT);
        content.setBorder(BorderFactory.createEmptyBorder(0, 60, 40, 60));

        genreLabel = new JLabel("Genre");
        genreLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        genreLabel.setForeground(MainWindow.ACCENT);
        genreLabel.setAlignmentX(LEFT_ALIGNMENT);

        titleLabel = new JLabel("Title");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 30));
        titleLabel.setForeground(MainWindow.TEXT_MAIN);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel bylinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        bylinePanel.setBackground(MainWindow.BG_PARCHMENT);
        bylinePanel.setAlignmentX(LEFT_ALIGNMENT);
        authorLabel = new JLabel("Author");
        authorLabel.setFont(MainWindow.FONT_BODY);
        authorLabel.setForeground(MainWindow.TEXT_MUTED);
        dateLabel = new JLabel("Date");
        dateLabel.setFont(MainWindow.FONT_BODY);
        dateLabel.setForeground(MainWindow.TEXT_MUTED);
        readsLabel = new JLabel("Reads");
        readsLabel.setFont(MainWindow.FONT_BODY);
        readsLabel.setForeground(MainWindow.TEXT_MUTED);

        JLabel sep = new JLabel("|");
        sep.setForeground(MainWindow.BORDER_COLOR);
        JLabel sep2 = new JLabel("|");
        sep2.setForeground(MainWindow.BORDER_COLOR);

        bylinePanel.add(authorLabel);
        bylinePanel.add(sep);
        bylinePanel.add(dateLabel);
        bylinePanel.add(sep2);
        bylinePanel.add(readsLabel);

        JSeparator divider = new JSeparator();
        divider.setForeground(MainWindow.BORDER_COLOR);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Report button row
        JButton reportBtn = new JButton("\u2691 Report this work");
        reportBtn.setFont(MainWindow.FONT_SMALL);
        reportBtn.setForeground(MainWindow.DANGER_FG);
        reportBtn.setBackground(MainWindow.DANGER_BG);
        reportBtn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        reportBtn.setFocusPainted(false);
        reportBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reportBtn.setOpaque(true);
        reportBtn.addActionListener(e -> reportCurrentWork());

        JPanel reportRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        reportRow.setBackground(MainWindow.BG_PARCHMENT);
        reportRow.setAlignmentX(LEFT_ALIGNMENT);
        reportRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        reportRow.add(reportBtn);

        bodyArea = new JTextArea();
        bodyArea.setFont(new Font("Serif", Font.PLAIN, 15));
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setEditable(false);
        bodyArea.setBackground(MainWindow.BG_PARCHMENT);
        bodyArea.setForeground(MainWindow.TEXT_MAIN);
        bodyArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        bodyArea.setAlignmentX(LEFT_ALIGNMENT);

        ratingPanel = buildRatingPanel();
        ratingPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel commentsSection = buildCommentsSection();
        commentsSection.setAlignmentX(LEFT_ALIGNMENT);

        content.add(genreLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(bylinePanel);
        content.add(Box.createVerticalStrut(6));
        content.add(divider);
        content.add(reportRow);
        content.add(Box.createVerticalStrut(16));
        content.add(bodyArea);
        content.add(Box.createVerticalStrut(20));
        content.add(ratingPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(commentsSection);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(MainWindow.BG_PARCHMENT);
        wrapper.add(topRow,  BorderLayout.NORTH);
        wrapper.add(content, BorderLayout.CENTER);

        JScrollPane scroll = MainWindow.scrollWrap(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);
    }

    // ── Rating panel ──────────────────────────────────────────────
    private JPanel buildRatingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel title = new JLabel("Rate This Work");
        title.setFont(new Font("Serif", Font.BOLD, 16));
        title.setForeground(MainWindow.TEXT_MAIN);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Score each of the 5 quality criteria (1–5)");
        sub.setFont(MainWindow.FONT_SMALL);
        sub.setForeground(MainWindow.TEXT_MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(3));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(12));

        for (int i = 0; i < CRITERIA.length; i++) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(Color.WHITE);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            row.setAlignmentX(LEFT_ALIGNMENT);

            JLabel lbl = new JLabel(CRITERIA[i]);
            lbl.setFont(MainWindow.FONT_BODY);
            lbl.setForeground(MainWindow.TEXT_MAIN);
            lbl.setPreferredSize(new Dimension(130, 24));

            criteriaSliders[i] = new JSlider(1, 5, 3);
            criteriaSliders[i].setMajorTickSpacing(1);
            criteriaSliders[i].setPaintTicks(true);
            criteriaSliders[i].setPaintLabels(true);
            criteriaSliders[i].setSnapToTicks(true);
            criteriaSliders[i].setBackground(Color.WHITE);

            criteriaValues[i] = new JLabel("3/5");
            criteriaValues[i].setFont(MainWindow.FONT_BOLD);
            criteriaValues[i].setForeground(MainWindow.ACCENT);
            criteriaValues[i].setPreferredSize(new Dimension(36, 24));

            final int idx = i;
            final ChangeListener cl = e ->
                criteriaValues[idx].setText(criteriaSliders[idx].getValue() + "/5");
            criteriaSliders[i].addChangeListener(cl);

            row.add(lbl,                BorderLayout.WEST);
            row.add(criteriaSliders[i], BorderLayout.CENTER);
            row.add(criteriaValues[i],  BorderLayout.EAST);
            panel.add(row);
            panel.add(Box.createVerticalStrut(6));
        }

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(MainWindow.BORDER_COLOR);

        JPanel avgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        avgRow.setBackground(Color.WHITE);
        avgRow.setAlignmentX(LEFT_ALIGNMENT);
        avgRatingLabel = new JLabel("0.0");
        avgRatingLabel.setFont(new Font("Serif", Font.BOLD, 26));
        avgRatingLabel.setForeground(MainWindow.ACCENT);
        totalRatingsLabel = new JLabel("0 ratings");
        totalRatingsLabel.setFont(MainWindow.FONT_SMALL);
        totalRatingsLabel.setForeground(MainWindow.TEXT_MUTED);
        avgRow.add(avgRatingLabel);
        avgRow.add(totalRatingsLabel);

        JButton submitBtn = MainWindow.styledButton("Submit Rating", true);
        submitBtn.setAlignmentX(LEFT_ALIGNMENT);
        submitBtn.addActionListener(e -> submitRating());

        panel.add(sep);
        panel.add(Box.createVerticalStrut(6));
        panel.add(avgRow);
        panel.add(Box.createVerticalStrut(8));
        panel.add(submitBtn);
        return panel;
    }

    // ── Comments section ──────────────────────────────────────────
    private JPanel buildCommentsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(MainWindow.BG_PARCHMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel title = new JLabel("Comments");
        title.setFont(new Font("Serif", Font.BOLD, 18));
        title.setForeground(MainWindow.TEXT_MAIN);
        title.setAlignmentX(LEFT_ALIGNMENT);

        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setBackground(MainWindow.BG_PARCHMENT);
        commentsPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setBackground(MainWindow.BG_PARCHMENT);
        inputRow.setAlignmentX(LEFT_ALIGNMENT);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        commentInput = new JTextField();
        commentInput.setFont(MainWindow.FONT_BODY);
        commentInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        commentInput.addActionListener(e -> postComment());

        JButton postBtn = MainWindow.styledButton("Post", true);
        postBtn.addActionListener(e -> postComment());

        inputRow.add(commentInput, BorderLayout.CENTER);
        inputRow.add(postBtn,      BorderLayout.EAST);

        section.add(title);
        section.add(Box.createVerticalStrut(12));
        section.add(commentsPanel);
        section.add(Box.createVerticalStrut(14));
        section.add(inputRow);
        return section;
    }

    // ═════════════════════════════════════════════════════════════
    // PUBLIC — load a work
    // ═════════════════════════════════════════════════════════════
    public void loadWork(int workId, String returnPanel) {
        this.returnPanel    = returnPanel;
        this.currentWorkId  = workId;

        Work w = DataStore.get().getWorkById(workId);
        if (w == null) return;

        w.incrementReadCount();

        genreLabel.setText(w.getGenre().toUpperCase());
        titleLabel.setText(w.getTitle());
        authorLabel.setText("by " + w.getAuthorName());
        dateLabel.setText(w.getPublishedDateFormatted());
        readsLabel.setText(w.getReadCount() + " reads");
        bodyArea.setText(w.getContent());
        bodyArea.setCaretPosition(0);

        String  uid   = DataStore.get().getCurrentUser().getUserId();
        boolean isOwn = w.getAuthorId().equals(uid);
        ratingPanel.setVisible(!isOwn);
        if (!isOwn) {
            updateRatingDisplay(w);
            boolean alreadyRated = w.hasRatedBy(uid);
            for (JSlider s : criteriaSliders) s.setEnabled(!alreadyRated);
        }

        loadComments(w);
    }

    // ── Rating helpers ────────────────────────────────────────────
    private void updateRatingDisplay(Work w) {
        double avg = w.getAverageRating();
        avgRatingLabel.setText(String.format("%.1f", avg));
        int n = w.getRatings().size();
        totalRatingsLabel.setText(n + " rating" + (n != 1 ? "s" : ""));
    }

    private void submitRating() {
        Work w = DataStore.get().getWorkById(currentWorkId);
        if (w == null) return;
        String uid = DataStore.get().getCurrentUser().getUserId();

        if (w.getAuthorId().equals(uid)) {
            MainWindow.showToast(window, "You cannot rate your own work.", false);
            return;
        }
        if (w.hasRatedBy(uid)) {
            MainWindow.showToast(window, "You have already rated this work.", false);
            return;
        }

        Rating r = new Rating(uid,
                criteriaSliders[0].getValue(),
                criteriaSliders[1].getValue(),
                criteriaSliders[2].getValue(),
                criteriaSliders[3].getValue(),
                criteriaSliders[4].getValue());
        w.addRating(r);
        updateRatingDisplay(w);
        for (JSlider s : criteriaSliders) s.setEnabled(false);
        DataStore.get().persistWork();
        MainWindow.showToast(window,
                "Rating submitted! Average: " + String.format("%.1f", w.getAverageRating()),
                true);
    }

    // ── Comment helpers ───────────────────────────────────────────
    private void loadComments(Work w) {
        commentsPanel.removeAll();
        MyList<Comment> comments = w.getComments();
        if (comments.isEmpty()) {
            JLabel none = new JLabel("No comments yet. Be the first!");
            none.setFont(MainWindow.FONT_BODY);
            none.setForeground(MainWindow.TEXT_MUTED);
            none.setAlignmentX(LEFT_ALIGNMENT);
            commentsPanel.add(none);
        } else {
            for (int i = 0; i < comments.size(); i++) {
                commentsPanel.add(makeCommentRow(comments.get(i)));
                commentsPanel.add(Box.createVerticalStrut(10));
            }
        }
        commentsPanel.revalidate();
        commentsPanel.repaint();
    }

    private JPanel makeCommentRow(Comment c) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setAlignmentX(LEFT_ALIGNMENT);

        String initials = c.getAuthorName().length() >= 2
                ? c.getAuthorName().substring(0, 2).toUpperCase()
                : c.getAuthorName().toUpperCase();

        JLabel avatar = new JLabel(initials) {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(MainWindow.ACCENT_LIGHT);
                g.fillOval(0, 0, 34, 34);
                g.setColor(MainWindow.ACCENT);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(initials,
                        (34 - fm.stringWidth(initials)) / 2,
                        (34 - fm.getHeight()) / 2 + fm.getAscent());
            }
            @Override
            public Dimension getPreferredSize() { return new Dimension(34, 34); }
        };

        JPanel body = new JPanel(new GridLayout(3, 1, 0, 2));
        body.setBackground(Color.WHITE);
        JLabel authorLbl = new JLabel(c.getAuthorName());
        authorLbl.setFont(MainWindow.FONT_BOLD);
        authorLbl.setForeground(MainWindow.TEXT_MAIN);
        JLabel textLbl = new JLabel("<html>" + c.getContent() + "</html>");
        textLbl.setFont(MainWindow.FONT_BODY);
        textLbl.setForeground(MainWindow.TEXT_MUTED);
        JLabel timeLbl = new JLabel(c.getFormattedTime());
        timeLbl.setFont(MainWindow.FONT_SMALL);
        timeLbl.setForeground(MainWindow.BORDER_COLOR);
        body.add(authorLbl); body.add(textLbl); body.add(timeLbl);

        row.add(avatar, BorderLayout.WEST);
        row.add(body,   BorderLayout.CENTER);
        return row;
    }

    private void postComment() {
        String text = commentInput.getText().trim();
        if (text.isEmpty()) {
            MainWindow.showToast(window, "Please write something before posting.", false);
            return;
        }
        Work w = DataStore.get().getWorkById(currentWorkId);
        if (w == null) return;

        User me = DataStore.get().getCurrentUser();
        w.addComment(new Comment(me.getUserId(), me.getUsername(), text));
        commentInput.setText("");
        loadComments(w);
        DataStore.get().persistWork();
        MainWindow.showToast(window, "Comment posted!", true);
    }

    // ═════════════════════════════════════════════════════════════
    // REPORT  — reason-selection dialog + DataStore call
    // ═════════════════════════════════════════════════════════════
    private void reportCurrentWork() {
        Work w = DataStore.get().getWorkById(currentWorkId);
        if (w == null) return;

        String uid = DataStore.get().getCurrentUser().getUserId();

        // Cannot report own work
        if (w.getAuthorId().equals(uid)) {
            MainWindow.showToast(window, "You cannot report your own work.", false);
            return;
        }

        // Already reported by this user (pre-check before showing dialog)
        User targetUser = DataStore.get().getUserById(w.getAuthorId());
        if (targetUser != null && targetUser.getReportedBy().contains(uid)) {
            MainWindow.showToast(window, "You have already reported this account.", false);
            return;
        }

        // ── Build reason-selection dialog ────────────────────────
        JDialog dlg = new JDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                "Report This Work", true);
        dlg.setSize(420, 360);
        dlg.setLocationRelativeTo(window);
        dlg.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = new JLabel("Report: \"" + w.getTitle() + "\"");
        titleLbl.setFont(new Font("Serif", Font.BOLD, 16));
        titleLbl.setForeground(MainWindow.TEXT_MAIN);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel("Select the reason for your report:");
        subLbl.setFont(MainWindow.FONT_SMALL);
        subLbl.setForeground(MainWindow.TEXT_MUTED);
        subLbl.setAlignmentX(LEFT_ALIGNMENT);

        final String[] reasons     = { "Harassment or Bullying", "Plagiarism",
                                        "Inappropriate Content",  "Spam" };
        final String[] reasonIcons = { "\uD83D\uDDE3", "\uD83D\uDCCB",
                                        "\uD83D\uDEAB", "\uD83D\uDCE7" };
        final String[] reasonDescs = {
            "Content that targets or threatens individuals",
            "Content copied from another author without credit",
            "Content that violates community guidelines",
            "Repetitive or irrelevant promotional content"
        };

        final String[] selectedReason = { null };
        JPanel[]       reasonCards    = new JPanel[reasons.length];

        JPanel reasonsPanel = new JPanel();
        reasonsPanel.setLayout(new BoxLayout(reasonsPanel, BoxLayout.Y_AXIS));
        reasonsPanel.setBackground(Color.WHITE);
        reasonsPanel.setAlignmentX(LEFT_ALIGNMENT);

        for (int i = 0; i < reasons.length; i++) {
            final int    idx = i;
            final String rsn = reasons[i];

            JPanel card = new JPanel(new BorderLayout(10, 0));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
            card.setAlignmentX(LEFT_ALIGNMENT);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel iconLbl = new JLabel(reasonIcons[i]);
            iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 18));

            JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 2));
            textBlock.setBackground(Color.WHITE);
            JLabel nameLbl = new JLabel(reasons[i]);
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            nameLbl.setForeground(MainWindow.TEXT_MAIN);
            JLabel descLbl = new JLabel(reasonDescs[i]);
            descLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            descLbl.setForeground(MainWindow.TEXT_MUTED);
            textBlock.add(nameLbl);
            textBlock.add(descLbl);

            card.add(iconLbl,   BorderLayout.WEST);
            card.add(textBlock, BorderLayout.CENTER);
            reasonCards[idx] = card;

            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedReason[0] = rsn;
                    for (int j = 0; j < reasonCards.length; j++) {
                        if (j == idx) {
                            reasonCards[j].setBackground(MainWindow.ACCENT_LIGHT);
                            reasonCards[j].setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(MainWindow.ACCENT, 2),
                                    BorderFactory.createEmptyBorder(9, 11, 9, 11)));
                        } else {
                            reasonCards[j].setBackground(Color.WHITE);
                            reasonCards[j].setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
                        }
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!rsn.equals(selectedReason[0]))
                        card.setBackground(MainWindow.BG_PARCHMENT);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!rsn.equals(selectedReason[0]))
                        card.setBackground(Color.WHITE);
                }
            });

            reasonsPanel.add(card);
            if (i < reasons.length - 1) reasonsPanel.add(Box.createVerticalStrut(6));
        }

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(MainWindow.FONT_SMALL);
        errLbl.setForeground(MainWindow.DANGER_FG);
        errLbl.setAlignmentX(LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = MainWindow.styledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dlg.dispose());

        JButton submitBtn = MainWindow.styledButton("Submit Report", true);
        submitBtn.addActionListener(e -> {
            if (selectedReason[0] == null) {
                errLbl.setText("Please select a reason before submitting.");
                return;
            }
            dlg.dispose();

            // Re-fetch the work at submit time — avoids stale captures
            Work  target       = DataStore.get().getWorkById(currentWorkId);
            String reporterName = DataStore.get().getCurrentUser().getUsername();

            if (target == null) {
                MainWindow.showToast(window, "Work no longer exists.", false);
                return;
            }

            // DataStore.reportUser() saves users.txt and reportlogs.txt immediately
            int result = DataStore.get().reportUser(
                    target.getAuthorId(), uid,
                    selectedReason[0], target.getTitle(), reporterName);

            if (result == -1) {
                MainWindow.showToast(window,
                        "You have already reported this account.", false);
            } else {
                // ── Push the saved data into the live UI panels at once ──
                // DataStore has already written to disk; this call triggers
                // reportPanel.refresh() and adminPanel.refresh() so both panels
                // show the new entry without requiring the user to navigate away.
                window.refreshAfterReport();

                if (result == 2) {
                    MainWindow.showToast(window,
                            "Report submitted: " + selectedReason[0]
                            + ". Account has now been blocked (10 reports).", true);
                } else if (result == 1) {
                    MainWindow.showToast(window,
                            "Report submitted: " + selectedReason[0]
                            + ". Author notified (5-report threshold reached).", true);
                } else {
                    MainWindow.showToast(window,
                            "Report submitted: " + selectedReason[0] + ". Thank you.", true);
                }
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(submitBtn);

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subLbl);
        panel.add(Box.createVerticalStrut(14));
        panel.add(reasonsPanel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(errLbl);
        panel.add(Box.createVerticalStrut(6));
        panel.add(btnRow);

        dlg.add(panel);
        dlg.setVisible(true);   // blocks (modal) until dismissed
    }
}
