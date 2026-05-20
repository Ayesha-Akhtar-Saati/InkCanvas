package inkcanvas.ui;

import inkcanvas.ds.MyList;
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ProfilePanel extends JPanel {

    private MainWindow window;

    // ── Display labels ─────────────────────────────────────────
    private AvatarLabel avatarLabel;
    private JLabel      displayName;
    private JLabel      displayBio;
    private JLabel      displayLocation;
    private JLabel      displayWebsite;
    private JLabel      displayJoined;

    // ── Stat labels ────────────────────────────────────────────
    private JLabel statPublished;
    private JLabel statDrafts;
    private JLabel statReads;
    private JLabel statRating;

    // ── Edit fields ────────────────────────────────────────────
    private JTextField editName;
    private JTextField editEmail;
    private JTextField editLocation;
    private JTextField editWebsite;
    private JTextArea  editBio;

    // ── Panels toggled ─────────────────────────────────────────
    private JPanel editCard;
    private JPanel worksListPanel;

    // ─────────────────────────────────────────────────────────────
    public ProfilePanel(MainWindow window) {
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
        outer.setBorder(BorderFactory.createEmptyBorder(6, 6, 20, 6));

        outer.add(buildHeaderCard());
        outer.add(Box.createVerticalStrut(10));

        editCard = buildEditCard();
        editCard.setVisible(false);
        editCard.setAlignmentX(LEFT_ALIGNMENT);
        outer.add(editCard);
        outer.add(Box.createVerticalStrut(10));

        outer.add(buildStatsRow());
        outer.add(Box.createVerticalStrut(18));

        JLabel worksTitle = new JLabel("My Published Works");
        worksTitle.setFont(new Font("Serif", Font.BOLD, 16));
        worksTitle.setForeground(MainWindow.TEXT_MAIN);
        worksTitle.setAlignmentX(LEFT_ALIGNMENT);
        outer.add(worksTitle);
        outer.add(Box.createVerticalStrut(10));

        worksListPanel = new JPanel();
        worksListPanel.setLayout(new BoxLayout(worksListPanel, BoxLayout.Y_AXIS));
        worksListPanel.setBackground(MainWindow.BG_PARCHMENT);
        worksListPanel.setAlignmentX(LEFT_ALIGNMENT);
        outer.add(worksListPanel);

        JScrollPane scroll = new JScrollPane(outer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(MainWindow.BG_PARCHMENT);

        add(scroll, BorderLayout.CENTER);
    }

    // ── Profile header card ───────────────────────────────────────
    private JPanel buildHeaderCard() {

        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(22, 22, 22, 22)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.setAlignmentX(LEFT_ALIGNMENT);

        // Avatar
        avatarLabel = new AvatarLabel("AH");
        avatarLabel.setPreferredSize(new Dimension(68, 68));

        // Info panel
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);

        displayName = new JLabel("Loading...");
        displayName.setFont(new Font("Serif", Font.BOLD, 22));
        displayName.setForeground(MainWindow.TEXT_MAIN);
        displayName.setAlignmentX(LEFT_ALIGNMENT);

        displayBio = new JLabel(" ");
        displayBio.setFont(MainWindow.FONT_BODY);
        displayBio.setForeground(MainWindow.TEXT_MUTED);
        displayBio.setAlignmentX(LEFT_ALIGNMENT);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        metaRow.setBackground(Color.WHITE);
        metaRow.setAlignmentX(LEFT_ALIGNMENT);

        displayLocation = new JLabel("\uD83D\uDCCD \u2014");
        displayLocation.setFont(MainWindow.FONT_SMALL);
        displayLocation.setForeground(MainWindow.TEXT_MUTED);

        displayWebsite = new JLabel("\uD83C\uDF10 \u2014");
        displayWebsite.setFont(MainWindow.FONT_SMALL);
        displayWebsite.setForeground(MainWindow.ACCENT);

        displayJoined = new JLabel("\uD83D\uDCC5 \u2014");
        displayJoined.setFont(MainWindow.FONT_SMALL);
        displayJoined.setForeground(MainWindow.TEXT_MUTED);

        metaRow.add(displayLocation);
        metaRow.add(displayWebsite);
        metaRow.add(displayJoined);

        // Buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton editBtn = MainWindow.styledButton("Edit Profile", false);
        editBtn.addActionListener(e -> showEditCard(true));

        JButton shareBtn = MainWindow.styledButton("Share Profile", false);
        shareBtn.addActionListener(e ->
                MainWindow.showToast(window, "Profile link copied to clipboard!", true));

        btnRow.add(editBtn);
        btnRow.add(shareBtn);

        info.add(displayName);
        info.add(Box.createVerticalStrut(5));
        info.add(displayBio);
        info.add(Box.createVerticalStrut(6));
        info.add(metaRow);
        info.add(Box.createVerticalStrut(10));
        info.add(btnRow);

        card.add(avatarLabel, BorderLayout.WEST);
        card.add(info,        BorderLayout.CENTER);
        return card;
    }

    // ── Edit card ─────────────────────────────────────────────────
    private JPanel buildEditCard() {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        JLabel heading = new JLabel("Edit Profile");
        heading.setFont(new Font("Serif", Font.BOLD, 16));
        heading.setForeground(MainWindow.TEXT_MAIN);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        // Row 1: Name + Email
        JPanel row1 = new JPanel(new GridLayout(1, 2, 14, 0));
        row1.setBackground(Color.WHITE);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row1.setAlignmentX(LEFT_ALIGNMENT);
        editName  = makeField();
        editEmail = makeField();
        row1.add(labeledField("Display Name", editName));
        row1.add(labeledField("Email",        editEmail));

        // Row 2: Location + Website
        JPanel row2 = new JPanel(new GridLayout(1, 2, 14, 0));
        row2.setBackground(Color.WHITE);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row2.setAlignmentX(LEFT_ALIGNMENT);
        editLocation = makeField();
        editWebsite  = makeField();
        row2.add(labeledField("Location", editLocation));
        row2.add(labeledField("Website",  editWebsite));

        // Bio
        editBio = new JTextArea(3, 30);
        editBio.setFont(MainWindow.FONT_BODY);
        editBio.setLineWrap(true);
        editBio.setWrapStyleWord(true);
        editBio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 9, 6, 9)));
        JScrollPane bioScroll = new JScrollPane(editBio,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bioScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        bioScroll.setAlignmentX(LEFT_ALIGNMENT);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = MainWindow.styledButton("Cancel", false);
        cancelBtn.addActionListener(e -> showEditCard(false));

        JButton saveBtn = MainWindow.styledButton("Save Changes", true);
        saveBtn.addActionListener(e -> saveProfile());

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        card.add(heading);
        card.add(Box.createVerticalStrut(14));
        card.add(row1);
        card.add(Box.createVerticalStrut(10));
        card.add(row2);
        card.add(Box.createVerticalStrut(10));
        card.add(fieldLabel("Bio"));
        card.add(Box.createVerticalStrut(4));
        card.add(bioScroll);
        card.add(Box.createVerticalStrut(12));
        card.add(btnRow);

        return card;
    }

    // ── Stats row ─────────────────────────────────────────────────
    private JPanel buildStatsRow() {

        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setBackground(MainWindow.BG_PARCHMENT);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        statPublished = new JLabel("0");
        statDrafts    = new JLabel("0");
        statReads     = new JLabel("0");
        statRating    = new JLabel("\u2014");

        row.add(statCard(statPublished, "Published Works"));
        row.add(statCard(statDrafts,    "Drafts"));
        row.add(statCard(statReads,     "Total Reads"));
        row.add(statCard(statRating,    "Avg. Rating"));

        return row;
    }

    private JPanel statCard(JLabel numLabel, String labelText) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        numLabel.setFont(new Font("Serif", Font.BOLD, 26));
        numLabel.setForeground(MainWindow.ACCENT);
        numLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setFont(MainWindow.FONT_SMALL);
        lbl.setForeground(MainWindow.TEXT_MUTED);

        card.add(numLabel);
        card.add(lbl);
        return card;
    }

    // ── Refresh ───────────────────────────────────────────────────
    public void refresh() {

        User u = DataStore.get().getCurrentUser();

        // Avatar initials
        String[] parts    = u.getUsername().trim().split("\\s+");
        String   initials = parts.length >= 2
                ? "" + Character.toUpperCase(parts[0].charAt(0))
                    + Character.toUpperCase(parts[1].charAt(0))
                : u.getUsername().substring(0, Math.min(2, u.getUsername().length())).toUpperCase();
        avatarLabel.setInitials(initials);

        // Display labels
        displayName.setText(u.getUsername());
        displayBio.setText("<html><div style='width:480px'>"
                + (u.getBio().isEmpty() ? "No bio yet. Click Edit Profile to add one." : u.getBio())
                + "</div></html>");
        displayLocation.setText("\uD83D\uDCCD "
                + (u.getLocation().isEmpty() ? "\u2014" : u.getLocation()));
        displayWebsite.setText("\uD83C\uDF10 "
                + (u.getWebsite().isEmpty() ? "\u2014" : u.getWebsite()));
        displayJoined.setText("\uD83D\uDCC5 Joined " + u.getJoinDateFormatted());

        // Pre-fill edit fields
        editName.setText(u.getUsername());
        editEmail.setText(u.getEmail());
        editLocation.setText(u.getLocation());
        editWebsite.setText(u.getWebsite());
        editBio.setText(u.getBio());

        // Stats
        String       uid   = u.getUserId();
        MyList<Work> all   = DataStore.get().getMyWorks(uid);
        int    pub = 0, draft = 0, reads = 0;
        double totalRating = 0;
        int    ratingCount = 0;

        for (int i = 0; i < all.size(); i++) {
            Work w = all.get(i);
            if (w.isPublished()) {
                pub++;
                reads += w.getReadCount();
            } else {
                draft++;
            }
            if (w.getRatings().size() > 0) {
                totalRating += w.getAverageRating();
                ratingCount++;
            }
        }

        statPublished.setText(String.valueOf(pub));
        statDrafts.setText(String.valueOf(draft));
        statReads.setText(String.valueOf(reads));
        statRating.setText(ratingCount > 0
                ? String.format("%.1f", totalRating / ratingCount) : "\u2014");

        // Works list
        refreshWorksList(uid);
    }

    private void refreshWorksList(String uid) {

        worksListPanel.removeAll();
        MyList<Work> all    = DataStore.get().getMyWorks(uid);
        boolean      anyPub = false;

        for (int i = 0; i < all.size(); i++) {
            Work w = all.get(i);
            if (!w.isPublished()) continue;
            anyPub = true;

            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            row.setAlignmentX(LEFT_ALIGNMENT);

            JPanel info = new JPanel(new GridLayout(2, 1, 0, 3));
            info.setBackground(Color.WHITE);

            JLabel titleLbl = new JLabel(w.getTitle());
            titleLbl.setFont(new Font("Serif", Font.BOLD, 13));
            titleLbl.setForeground(MainWindow.TEXT_MAIN);

            JLabel meta = new JLabel(w.getGenre()
                    + "  \u00b7  " + w.getReadCount() + " reads"
                    + "  \u00b7  \u2605 " + String.format("%.1f", w.getAverageRating())
                    + "  \u00b7  Published " + w.getPublishedDateFormatted());
            meta.setFont(MainWindow.FONT_SMALL);
            meta.setForeground(MainWindow.TEXT_MUTED);

            info.add(titleLbl);
            info.add(meta);

            JButton readBtn = MainWindow.styledButton("Read", false);
            readBtn.addActionListener(e ->
                    window.openWork(w.getWorkId(), MainWindow.PROFILE));

            row.add(info,    BorderLayout.CENTER);
            row.add(readBtn, BorderLayout.EAST);
            worksListPanel.add(row);
            worksListPanel.add(Box.createVerticalStrut(10));
        }

        if (!anyPub) {
            JLabel none = new JLabel("No published works yet. Start writing!");
            none.setFont(MainWindow.FONT_BODY);
            none.setForeground(MainWindow.TEXT_MUTED);
            none.setAlignmentX(LEFT_ALIGNMENT);
            worksListPanel.add(none);
        }

        worksListPanel.revalidate();
        worksListPanel.repaint();
    }

    // ── Edit helpers ──────────────────────────────────────────────
    private void showEditCard(boolean show) {
        editCard.setVisible(show);
        revalidate();
        repaint();
    }

    private void saveProfile() {
        User u = DataStore.get().getCurrentUser();

        String newName = editName.getText().trim();
        if (!newName.isEmpty()) u.setUsername(newName);

        String newEmail = editEmail.getText().trim();
        if (!newEmail.isEmpty()) u.setEmail(newEmail);

        u.setLocation(editLocation.getText().trim());
        u.setWebsite(editWebsite.getText().trim());
        u.setBio(editBio.getText().trim());

        DataStore.get().saveUserProfile(u);   // ← persist to disk

        showEditCard(false);
        refresh();
        MainWindow.showToast(window, "Profile saved successfully!", true);
    }

    // ── UI helpers ────────────────────────────────────────────────
    private JPanel labeledField(String labelText, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.add(fieldLabel(labelText), BorderLayout.NORTH);
        p.add(field,                 BorderLayout.CENTER);
        return p;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(MainWindow.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makeField() {
        JTextField f = new JTextField();
        f.setFont(MainWindow.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 9, 6, 9)));
        return f;
    }

    // ── Inner class: painted avatar ───────────────────────────────
    static class AvatarLabel extends JLabel {
        private String initials;

        AvatarLabel(String initials) {
            this.initials = initials;
            setPreferredSize(new Dimension(68, 68));
        }

        void setInitials(String s) {
            this.initials = s;
            repaint();
        }

        public void paintComponent(Graphics g) {
            g.setColor(MainWindow.ACCENT);
            g.fillOval(0, 0, 68, 68);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Serif", Font.BOLD, 22));
            FontMetrics fm = g.getFontMetrics();
            String t = initials == null ? "" : initials;
            g.drawString(t,
                    (68 - fm.stringWidth(t)) / 2,
                    (68 - fm.getHeight()) / 2 + fm.getAscent());
        }

        public Dimension getPreferredSize() { return new Dimension(68, 68); }
    }
}

