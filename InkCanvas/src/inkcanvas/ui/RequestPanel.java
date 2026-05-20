package inkcanvas.ui;

import inkcanvas.ds.MyList;
import inkcanvas.model.User;
import inkcanvas.model.WritingRequest;
import inkcanvas.service.DataStore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class RequestPanel extends JPanel {

    private MainWindow window;
    private JPanel     listPanel;

    public RequestPanel(MainWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(MainWindow.BG_PARCHMENT);
        buildUI();
    }

    // ─────────────────────────────────────────────────────────────
    private void buildUI() {

        // ── Top bar ──────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(MainWindow.BG_PARCHMENT);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel subtitle = new JLabel("Community writing requests — open for all writers to respond");
        subtitle.setFont(MainWindow.FONT_SMALL);
        subtitle.setForeground(MainWindow.TEXT_MUTED);

        JButton submitBtn = MainWindow.styledButton("+ Submit Request", true);
        submitBtn.addActionListener(e -> showSubmitDialog());

        topBar.add(subtitle,  BorderLayout.WEST);
        topBar.add(submitBtn, BorderLayout.EAST);

        // ── Request list ─────────────────────────────────────────
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(MainWindow.BG_PARCHMENT);
        listPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        JScrollPane scroll = new JScrollPane(listPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(MainWindow.BG_PARCHMENT);

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Refresh ───────────────────────────────────────────────────
    public void refresh() {
        listPanel.removeAll();

        MyList<WritingRequest> reqs = DataStore.get().getAllRequests();

        if (reqs.isEmpty()) {
            JLabel empty = new JLabel("No requests yet. Be the first to submit one!");
            empty.setFont(MainWindow.FONT_BODY);
            empty.setForeground(MainWindow.TEXT_MUTED);
            empty.setBorder(BorderFactory.createEmptyBorder(30, 8, 8, 8));
            empty.setAlignmentX(LEFT_ALIGNMENT);
            listPanel.add(empty);
        } else {
            for (int i = 0; i < reqs.size(); i++) {
                listPanel.add(makeRequestCard(reqs.get(i)));
                listPanel.add(Box.createVerticalStrut(12));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    // ── Request card ──────────────────────────────────────────────
    private JPanel makeRequestCard(WritingRequest r) {

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 18, 14, 18)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        card.setAlignmentX(LEFT_ALIGNMENT);

        // ── Header: topic + genre tag ─────────────────────────
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(Color.WHITE);

        JLabel topicLbl = new JLabel("<html><b>" + r.getTopic() + "</b></html>");
        topicLbl.setFont(new Font("Serif", Font.BOLD, 14));
        topicLbl.setForeground(MainWindow.TEXT_MAIN);

        String genreText = (r.getTargetGenre() == null || r.getTargetGenre().isEmpty())
                ? "Any Genre" : r.getTargetGenre();
        JLabel genreTag = new JLabel(genreText);
        genreTag.setFont(new Font("SansSerif", Font.BOLD, 11));
        genreTag.setForeground(MainWindow.ACCENT);
        genreTag.setBackground(MainWindow.ACCENT_LIGHT);
        genreTag.setOpaque(true);
        genreTag.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        header.add(topicLbl, BorderLayout.CENTER);
        header.add(genreTag, BorderLayout.EAST);

        // ── Description ───────────────────────────────────────
        JLabel descLbl = new JLabel(
                "<html><div style='width:540px;color:#7a6f65'>" + r.getDescription() + "</div></html>");
        descLbl.setFont(MainWindow.FONT_BODY);

        // ── Footer: meta info + respond button ────────────────
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setBackground(Color.WHITE);

        String targetPart = (r.getTargetWriterName() == null || r.getTargetWriterName().isEmpty())
                ? "" : "  \u00b7  For: " + r.getTargetWriterName();

        JLabel metaLbl = new JLabel(
                "By " + r.getRequesterName() + "  \u00b7  " + r.getFormattedDate() + targetPart);
        metaLbl.setFont(MainWindow.FONT_SMALL);
        metaLbl.setForeground(MainWindow.TEXT_MUTED);

        JButton respondBtn = MainWindow.styledButton("Write a Response", false);
        respondBtn.addActionListener(e -> {
            window.showPanel(MainWindow.WRITE);
            MainWindow.showToast(window,
                    "Request loaded — start writing your response!", true);
        });

        footer.add(metaLbl,    BorderLayout.WEST);
        footer.add(respondBtn, BorderLayout.EAST);

        card.add(header,  BorderLayout.NORTH);
        card.add(descLbl, BorderLayout.CENTER);
        card.add(footer,  BorderLayout.SOUTH);

        return card;
    }

    // ── Submit dialog ─────────────────────────────────────────────
    private void showSubmitDialog() {

        JDialog dlg = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Submit a Writing Request", true);
        dlg.setSize(490, 450);
        dlg.setLocationRelativeTo(window);
        dlg.setResizable(false);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Title
        JLabel heading = new JLabel("Submit a Writing Request");
        heading.setFont(new Font("Serif", Font.BOLD, 18));
        heading.setForeground(MainWindow.TEXT_MAIN);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        // Fields
        JTextField topicField  = dialogField();
        JTextField writerField = dialogField();

        JTextArea descArea = new JTextArea(4, 30);
        descArea.setFont(MainWindow.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 9, 6, 9)));
        JScrollPane descScroll = new JScrollPane(descArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        descScroll.setAlignmentX(LEFT_ALIGNMENT);

        // Genre combo
        MyList<String> genres = DataStore.get().getGenres();
        String[] genreOpts = new String[genres.size() + 1];
        genreOpts[0] = "Any genre";
        for (int i = 0; i < genres.size(); i++) genreOpts[i + 1] = genres.get(i);
        JComboBox<String> genreBox = new JComboBox<>(genreOpts);
        genreBox.setFont(MainWindow.FONT_BODY);
        genreBox.setBackground(Color.WHITE);
        genreBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        genreBox.setAlignmentX(LEFT_ALIGNMENT);

        // Error label
        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(MainWindow.FONT_SMALL);
        errLbl.setForeground(MainWindow.DANGER_FG);
        errLbl.setAlignmentX(LEFT_ALIGNMENT);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = MainWindow.styledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dlg.dispose());

        JButton submitBtn = MainWindow.styledButton("Submit Request", true);
        submitBtn.addActionListener(e -> {
            String topic  = topicField.getText().trim();
            String desc   = descArea.getText().trim();
            if (topic.isEmpty()) {
                errLbl.setText("Please enter a request title."); return;
            }
            if (desc.isEmpty()) {
                errLbl.setText("Please enter a description."); return;
            }
            String genre  = genreBox.getSelectedIndex() == 0
                    ? "" : (String) genreBox.getSelectedItem();
            String writer = writerField.getText().trim();

            User me = DataStore.get().getCurrentUser();
            WritingRequest req = new WritingRequest(
                    me.getUserId(), me.getUsername(), topic, desc, genre, writer);
            DataStore.get().addRequest(req);

            dlg.dispose();
            refresh();
            MainWindow.showToast(window, "Your request has been submitted!", true);
        });

        btnRow.add(cancelBtn);
        btnRow.add(submitBtn);

        // Assemble form
        form.add(heading);
        form.add(Box.createVerticalStrut(16));
        form.add(dialogLabel("Request Title *"));
        form.add(Box.createVerticalStrut(4));
        form.add(topicField);
        form.add(Box.createVerticalStrut(10));
        form.add(dialogLabel("Description *"));
        form.add(Box.createVerticalStrut(4));
        form.add(descScroll);
        form.add(Box.createVerticalStrut(10));
        form.add(dialogLabel("Preferred Genre"));
        form.add(Box.createVerticalStrut(4));
        form.add(genreBox);
        form.add(Box.createVerticalStrut(10));
        form.add(dialogLabel("Target Writer (optional)"));
        form.add(Box.createVerticalStrut(4));
        form.add(writerField);
        form.add(Box.createVerticalStrut(12));
        form.add(errLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(btnRow);

        dlg.add(form);
        dlg.setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private JLabel dialogLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(MainWindow.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField dialogField() {
        JTextField f = new JTextField();
        f.setFont(MainWindow.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }
}
