package inkcanvas.ui;

import inkcanvas.model.Admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class AdminLoginPanel extends JPanel {

    private MainWindow     window;
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;

    public AdminLoginPanel(MainWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(MainWindow.BG_PARCHMENT);
        buildUI();
    }

    private void buildUI() {

        // ── Left dark panel ──────────────────────────────────────
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(new Color(20, 30, 48));
        left.setPreferredSize(new Dimension(340, 0));

        JPanel leftInner = new JPanel();
        leftInner.setLayout(new BoxLayout(leftInner, BoxLayout.Y_AXIS));
        leftInner.setBackground(new Color(20, 30, 48));
        leftInner.setBorder(BorderFactory.createEmptyBorder(60, 40, 40, 40));

        JLabel icon = new JLabel("\u2699");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 52));
        icon.setForeground(new Color(100, 160, 255));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel brand = new JLabel("Admin Portal");
        brand.setFont(new Font("Serif", Font.BOLD, 28));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Ink Canvas Management");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(new Color(140, 160, 200));
        sub.setAlignmentX(CENTER_ALIGNMENT);
        sub.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel featTitle = new JLabel("Admin Capabilities:");
        featTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        featTitle.setForeground(new Color(180, 200, 240));
        featTitle.setAlignmentX(LEFT_ALIGNMENT);

        String[] features = {
            "  \u2726  Change competition theme & date",
            "  \u2726  View all registered users",
            "  \u2726  Manage blocked / restricted accounts"
        };

        JPanel featPanel = new JPanel();
        featPanel.setLayout(new BoxLayout(featPanel, BoxLayout.Y_AXIS));
        featPanel.setBackground(new Color(20, 30, 48));
        featPanel.setAlignmentX(LEFT_ALIGNMENT);

        for (String f : features) {
            JLabel l = new JLabel(f);
            l.setFont(new Font("SansSerif", Font.PLAIN, 12));
            l.setForeground(new Color(160, 190, 230));
            l.setAlignmentX(LEFT_ALIGNMENT);
            featPanel.add(l);
            featPanel.add(Box.createVerticalStrut(6));
        }

        leftInner.add(Box.createVerticalGlue());
        leftInner.add(icon);
        leftInner.add(Box.createVerticalStrut(12));
        leftInner.add(brand);
        leftInner.add(Box.createVerticalStrut(4));
        leftInner.add(sub);
        leftInner.add(Box.createVerticalStrut(36));
        leftInner.add(featTitle);
        leftInner.add(Box.createVerticalStrut(10));
        leftInner.add(featPanel);
        leftInner.add(Box.createVerticalGlue());
        left.add(leftInner, BorderLayout.CENTER);

        // ── Right form ───────────────────────────────────────────
        JPanel right = new JPanel(new java.awt.GridBagLayout());
        right.setBackground(MainWindow.BG_PARCHMENT);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(32, 36, 32, 36)));
        card.setPreferredSize(new Dimension(360, 390));

        JLabel title = new JLabel("Admin Sign In");
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setForeground(MainWindow.TEXT_MAIN);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Restricted access — authorised personnel only");
        subtitle.setFont(MainWindow.FONT_SMALL);
        subtitle.setForeground(MainWindow.TEXT_MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(MainWindow.FONT_SMALL);
        errorLabel.setForeground(MainWindow.DANGER_FG);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        usernameField = makeField();
        passwordField = makePassField();
        passwordField.addActionListener(e -> doLogin());

        JButton loginBtn = MainWindow.styledButton("Sign In as Admin", true);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());

        JPanel backRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 0));
        backRow.setBackground(Color.WHITE);
        JLabel backTxt = new JLabel("Not an admin?");
        backTxt.setFont(MainWindow.FONT_SMALL);
        backTxt.setForeground(MainWindow.TEXT_MUTED);
        JLabel backLink = new JLabel("Back to Login");
        backLink.setFont(new Font("SansSerif", Font.BOLD, 11));
        backLink.setForeground(MainWindow.ACCENT);
        backLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { window.showPanel(MainWindow.LOGIN); }
            public void mouseEntered(MouseEvent e) { backLink.setForeground(MainWindow.ACCENT_DARK); }
            public void mouseExited(MouseEvent e)  { backLink.setForeground(MainWindow.ACCENT); }
        });
        backRow.add(backTxt);
        backRow.add(backLink);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(14));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(fldLabel("Admin Username"));
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));
        card.add(fldLabel("Admin Password"));
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(14));
        card.add(backRow);

        right.add(card);
        add(left,  BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both username and password."); return;
        }
        Admin admin = new Admin();
        if (!admin.authenticate(user, pass)) {
            errorLabel.setText("Invalid admin credentials. Please try again.");
            passwordField.setText(""); return;
        }
        errorLabel.setText(" ");
        usernameField.setText("");
        passwordField.setText("");
        window.showPanel(MainWindow.ADMIN);
    }

    private JLabel fldLabel(String t) {
        JLabel l = new JLabel(t);
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
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }

    private JPasswordField makePassField() {
        JPasswordField f = new JPasswordField();
        f.setFont(MainWindow.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }
}

