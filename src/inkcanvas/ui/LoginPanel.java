package inkcanvas.ui;

import inkcanvas.model.User;
import inkcanvas.service.DataStore;

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
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginPanel extends JPanel {

    private MainWindow     window;
    private JPanel         formCard;
    private JTextField     loginUser;
    private JPasswordField loginPass;
    private JTextField     regUser;
    private JPasswordField regPass;
    private JPasswordField regPass2;
    private JTextField     regEmail;
    private JLabel         errorLabel;
    private JLabel         switchLink;

    public LoginPanel(MainWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(MainWindow.BG_PARCHMENT);
        buildUI();
    }

    private void buildUI() {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(new Color(44, 26, 14));
        left.setPreferredSize(new Dimension(340, 0));

        JPanel leftInner = new JPanel();
        leftInner.setLayout(new BoxLayout(leftInner, BoxLayout.Y_AXIS));
        leftInner.setBackground(new Color(44, 26, 14));
        leftInner.setBorder(BorderFactory.createEmptyBorder(60, 40, 40, 40));

        JLabel brand = new JLabel("Ink Canvas");
        brand.setFont(new Font("Serif", Font.BOLD, 36));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("<html><center>Write \u00b7 Share \u00b7 Discover</center></html>");
        tagline.setFont(new Font("Serif", Font.ITALIC, 15));
        tagline.setForeground(new Color(200, 180, 160));
        tagline.setHorizontalAlignment(SwingConstants.CENTER);
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        JLabel quote = new JLabel("<html><center><i>\"The scariest moment is always<br/>just before you start.\"</i><br/><br/>\u2014 Stephen King</center></html>");
        quote.setFont(new Font("SansSerif", Font.PLAIN, 12));
        quote.setForeground(new Color(160, 140, 120));
        quote.setHorizontalAlignment(SwingConstants.CENTER);
        quote.setAlignmentX(CENTER_ALIGNMENT);

        leftInner.add(Box.createVerticalGlue());
        leftInner.add(brand);
        leftInner.add(Box.createVerticalStrut(12));
        leftInner.add(tagline);
        leftInner.add(Box.createVerticalStrut(40));
        leftInner.add(quote);
        leftInner.add(Box.createVerticalGlue());
        left.add(leftInner, BorderLayout.CENTER);

        JPanel right = new JPanel(new java.awt.GridBagLayout());
        right.setBackground(MainWindow.BG_PARCHMENT);

        formCard = new JPanel(new BorderLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(32, 36, 32, 36)));
        formCard.setPreferredSize(new Dimension(360, 440));

        buildLoginForm();
        right.add(formCard);

        add(left,  BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    // ── Login Form ────────────────────────────────────────────────
    private void buildLoginForm() {
        formCard.removeAll();
        formCard.setPreferredSize(new Dimension(360, 440));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(Color.WHITE);

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setForeground(MainWindow.TEXT_MAIN);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your Ink Canvas account");
        sub.setFont(MainWindow.FONT_SMALL);
        sub.setForeground(MainWindow.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(MainWindow.FONT_SMALL);
        errorLabel.setForeground(MainWindow.DANGER_FG);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        loginUser = makeField();
        loginPass = makePassField();

        JButton btn = MainWindow.styledButton("Sign In", true);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.addActionListener(e -> doLogin());
        loginPass.addActionListener(e -> doLogin());

        JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 0));
        row.setBackground(Color.WHITE);
        JLabel txt = new JLabel("Don't have an account?");
        txt.setFont(MainWindow.FONT_SMALL);
        txt.setForeground(MainWindow.TEXT_MUTED);
        switchLink = makeLink("Register here");
        switchLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { buildRegisterForm(); }
        });
        row.add(txt); row.add(switchLink);

        // Admin link
        JSeparator sep = new JSeparator();
        sep.setForeground(MainWindow.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(CENTER_ALIGNMENT);

        JPanel adminRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 0));
        adminRow.setBackground(Color.WHITE);
        JLabel adminTxt = new JLabel("Are you an admin?");
        adminTxt.setFont(MainWindow.FONT_SMALL);
        adminTxt.setForeground(MainWindow.TEXT_MUTED);
        JLabel adminLink = makeLink("Admin Login");
        adminLink.setForeground(new Color(37, 99, 235));
        adminLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { window.showPanel(MainWindow.ADMIN_LOGIN); }
            public void mouseEntered(MouseEvent e) { adminLink.setForeground(new Color(20, 60, 180)); }
            public void mouseExited(MouseEvent e)  { adminLink.setForeground(new Color(37, 99, 235)); }
        });
        adminRow.add(adminTxt); adminRow.add(adminLink);

        inner.add(title);
        inner.add(Box.createVerticalStrut(4));
        inner.add(sub);
        inner.add(Box.createVerticalStrut(14));
        inner.add(errorLabel);
        inner.add(Box.createVerticalStrut(4));
        inner.add(fieldLabel("Username"));
        inner.add(Box.createVerticalStrut(4));
        inner.add(loginUser);
        inner.add(Box.createVerticalStrut(12));
        inner.add(fieldLabel("Password"));
        inner.add(Box.createVerticalStrut(4));
        inner.add(loginPass);
        inner.add(Box.createVerticalStrut(20));
        inner.add(btn);
        inner.add(Box.createVerticalStrut(12));
        inner.add(row);
        inner.add(Box.createVerticalStrut(10));
        inner.add(sep);
        inner.add(Box.createVerticalStrut(8));
        inner.add(adminRow);

        formCard.add(inner, BorderLayout.CENTER);
        formCard.revalidate();
        formCard.repaint();
    }

    // ── Register Form ─────────────────────────────────────────────
    private void buildRegisterForm() {
        formCard.removeAll();
        formCard.setPreferredSize(new Dimension(360, 560));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(Color.WHITE);

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setForeground(MainWindow.TEXT_MAIN);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Join the Ink Canvas community");
        sub.setFont(MainWindow.FONT_SMALL);
        sub.setForeground(MainWindow.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(MainWindow.FONT_SMALL);
        errorLabel.setForeground(MainWindow.DANGER_FG);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        regUser  = makeField();
        regEmail = makeField();
        regPass  = makePassField();
        regPass2 = makePassField();

        // Field hints
        JLabel emailHint = new JLabel("Must be a valid email (e.g. name@gmail.com)");
        emailHint.setFont(new Font("SansSerif", Font.ITALIC, 10));
        emailHint.setForeground(MainWindow.TEXT_MUTED);
        emailHint.setAlignmentX(LEFT_ALIGNMENT);

        JLabel passHint = new JLabel("Minimum 6 characters, must contain a number");
        passHint.setFont(new Font("SansSerif", Font.ITALIC, 10));
        passHint.setForeground(MainWindow.TEXT_MUTED);
        passHint.setAlignmentX(LEFT_ALIGNMENT);

        JButton btn = MainWindow.styledButton("Create Account", true);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.addActionListener(e -> doRegister());

        JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 0));
        row.setBackground(Color.WHITE);
        JLabel txt = new JLabel("Already have an account?");
        txt.setFont(MainWindow.FONT_SMALL);
        txt.setForeground(MainWindow.TEXT_MUTED);
        switchLink = makeLink("Sign in");
        switchLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { buildLoginForm(); }
        });
        row.add(txt); row.add(switchLink);

        inner.add(title);
        inner.add(Box.createVerticalStrut(4));
        inner.add(sub);
        inner.add(Box.createVerticalStrut(10));
        inner.add(errorLabel);
        inner.add(Box.createVerticalStrut(2));
        inner.add(fieldLabel("Username"));
        inner.add(Box.createVerticalStrut(4));
        inner.add(regUser);
        inner.add(Box.createVerticalStrut(10));
        inner.add(fieldLabel("Email Address"));
        inner.add(Box.createVerticalStrut(4));
        inner.add(regEmail);
        inner.add(Box.createVerticalStrut(2));
        inner.add(emailHint);
        inner.add(Box.createVerticalStrut(10));
        inner.add(fieldLabel("Password"));
        inner.add(Box.createVerticalStrut(4));
        inner.add(regPass);
        inner.add(Box.createVerticalStrut(2));
        inner.add(passHint);
        inner.add(Box.createVerticalStrut(10));
        inner.add(fieldLabel("Confirm Password"));
        inner.add(Box.createVerticalStrut(4));
        inner.add(regPass2);
        inner.add(Box.createVerticalStrut(18));
        inner.add(btn);
        inner.add(Box.createVerticalStrut(12));
        inner.add(row);

        formCard.add(inner, BorderLayout.CENTER);
        formCard.revalidate();
        formCard.repaint();
    }

    // ── Validation helpers ────────────────────────────────────────

    /** Full email validation: must have @, domain, and valid extension */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        // Must contain exactly one @
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) return false;
        if (email.indexOf('@', atIndex + 1) != -1) return false;

        String local  = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);

        // Local part must not be empty
        if (local.trim().isEmpty()) return false;

        // Domain must contain a dot and valid extension (.com .net .org .edu .io etc.)
        int dotIndex = domain.lastIndexOf('.');
        if (dotIndex <= 0) return false;

        String extension = domain.substring(dotIndex + 1).toLowerCase();
        if (extension.length() < 2 || extension.length() > 6) return false;

        // Domain part before the dot must not be empty
        String domainName = domain.substring(0, dotIndex);
        if (domainName.trim().isEmpty()) return false;

        // No spaces allowed anywhere
        if (email.contains(" ")) return false;

        return true;
    }

    /** Password must be at least 6 chars and contain at least one digit */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) return false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) { hasDigit = true; break; }
        }
        return hasDigit;
    }

    /** Username: 3-20 chars, letters/numbers/underscores only */
    private boolean isValidUsername(String username) {
        if (username == null || username.length() < 3 || username.length() > 20) return false;
        for (char c : username.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_' && c != ' ') return false;
        }
        return true;
    }

    // ── Actions ───────────────────────────────────────────────────
    private void doLogin() {
        String user = loginUser.getText().trim();
        String pass = new String(loginPass.getPassword());

        if (user.isEmpty()) {
            showError("Username cannot be empty."); return;
        }
        if (pass.isEmpty()) {
            showError("Password cannot be empty."); return;
        }

        User u = DataStore.get().login(user, pass);
        if (u == null) {
            showError("Incorrect username or password. Please try again."); return;
        }
        if (u.isBlocked()) {
            showError("This account has been blocked due to community reports."); return;
        }

        DataStore.get().setCurrentUser(u);
        errorLabel.setText(" ");
        loginUser.setText("");
        loginPass.setText("");
        window.showPanel(MainWindow.DISCOVER);
    }

    private void doRegister() {
        String user  = regUser.getText().trim();
        String email = regEmail.getText().trim();
        String pass  = new String(regPass.getPassword());
        String pass2 = new String(regPass2.getPassword());

        // ── Username validation ───────────────────────────────────
        if (user.isEmpty()) {
            showError("Username cannot be empty."); return;
        }
        if (!isValidUsername(user)) {
            showError("Username must be 3-20 characters. Letters, numbers, and underscores only."); return;
        }

        // ── Email validation ──────────────────────────────────────
        if (email.isEmpty()) {
            showError("Email address cannot be empty."); return;
        }
        if (!email.contains("@")) {
            showError("Invalid email: missing '@' symbol. Example: name@gmail.com"); return;
        }
        if (!isValidEmail(email)) {
            showError("Invalid email format. Must be like name@gmail.com or name@yahoo.com"); return;
        }

        // ── Password validation ───────────────────────────────────
        if (pass.isEmpty()) {
            showError("Password cannot be empty."); return;
        }
        if (pass.length() < 6) {
            showError("Password is too short. Minimum 6 characters required."); return;
        }
        if (!isValidPassword(pass)) {
            showError("Password must contain at least one number (e.g. Pass1word)."); return;
        }
        if (!pass.equals(pass2)) {
            showError("Passwords do not match. Please re-enter both passwords."); return;
        }

        // ── Register ──────────────────────────────────────────────
        boolean ok = DataStore.get().registerUser(user, pass, email);
        if (!ok) {
            showError("Username \"" + user + "\" is already taken. Please choose another."); return;
        }

        MainWindow.showToast(this, "Account created successfully! You can now sign in.", true);
        buildLoginForm();
    }

    private void showError(String msg) {
        errorLabel.setText("<html><div style='text-align:center;color:#b91c1c'>" + msg + "</div></html>");
    }

    // ── UI helpers ────────────────────────────────────────────────
    private JLabel fieldLabel(String t) {
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

    private JLabel makeLink(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(MainWindow.ACCENT);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }
}
