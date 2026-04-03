package database_setup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ForgotPasswordDialog extends JDialog {

    private UserDAO userDAO;
    private String  verifiedEmail = null;

    // Step panels
    private JPanel  cardPanel;
    private CardLayout cardLayout;

    // Step 1 — Email entry
    private JTextField emailField;

    // Step 2 — New password
    private JPasswordField newPwdField;
    private JPasswordField confirmPwdField;

    // ✅ Constructor takes JFrame parent + UserDAO
    public ForgotPasswordDialog(JFrame parent, UserDAO userDAO) {
        super(parent, "Forgot Password", true);
        this.userDAO = userDAO;
        setSize(400, 320);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(30, 40, 60));
        setContentPane(outer);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setPreferredSize(new Dimension(300, 240));
        cardPanel.setBackground(Color.WHITE);

        cardPanel.add(buildStep1(), "step1");
        cardPanel.add(buildStep2(), "step2");

        cardLayout.show(cardPanel, "step1");
        outer.add(cardPanel);
    }

    // ── Step 1: enter email ───────────────────────────────────────────────────
    private JPanel buildStep1() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx  = 0; gbc.gridy = 0;

        JLabel heading = new JLabel("Reset Password");
        heading.setFont(new Font("Georgia", Font.BOLD, 18));
        heading.setForeground(new Color(30, 40, 60));
        p.add(heading, gbc);

        gbc.gridy++;
        JLabel sub = new JLabel("Enter your registered email address.");
        sub.setFont(new Font("Arial", Font.PLAIN, 11));
        sub.setForeground(Color.GRAY);
        p.add(sub, gbc);

        gbc.gridy++;
        emailField = new JTextField(15);
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        emailField.setFont(new Font("Arial", Font.PLAIN, 13));
        styleWithPlaceholder(emailField, "Email address");
        p.add(emailField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(12, 0, 4, 0);
        JButton nextBtn = new JButton("Continue");
        styleButton(nextBtn, new Color(255, 165, 0));
        nextBtn.addActionListener(e -> checkEmail());
        p.add(nextBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 0, 0);
        JLabel back = new JLabel("Back to login");
        back.setForeground(new Color(0, 100, 200));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setFont(new Font("Arial", Font.PLAIN, 11));
        back.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
        });
        p.add(back, gbc);

        return p;
    }

    // ── Step 2: new password ──────────────────────────────────────────────────
    private JPanel buildStep2() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx  = 0; gbc.gridy = 0;

        JLabel heading = new JLabel("Set New Password");
        heading.setFont(new Font("Georgia", Font.BOLD, 18));
        heading.setForeground(new Color(30, 40, 60));
        p.add(heading, gbc);

        gbc.gridy++;
        newPwdField = new JPasswordField(15);
        newPwdField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        styleWithPlaceholder(newPwdField, "New password");
        p.add(newPwdField, gbc);

        gbc.gridy++;
        confirmPwdField = new JPasswordField(15);
        confirmPwdField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        styleWithPlaceholder(confirmPwdField, "Confirm new password");
        p.add(confirmPwdField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(12, 0, 4, 0);
        JButton resetBtn = new JButton("Reset Password");
        styleButton(resetBtn, new Color(30, 40, 60));
        resetBtn.addActionListener(e -> doReset());
        p.add(resetBtn, gbc);

        return p;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────
    private void checkEmail() {
        String email = emailField.getText().trim();
        if (email.equals("Email address") || email.isEmpty() || !email.contains("@")) {
            error("Please enter a valid email address."); return;
        }
        // ✅ Uses emailExists() from UserDAO
        if (!userDAO.emailExists(email)) {
            error("No account found with that email address."); return;
        }
        verifiedEmail = email;
        cardLayout.show(cardPanel, "step2");
    }

    private void doReset() {
        String newPwd  = new String(newPwdField.getPassword());
        String confirm = new String(confirmPwdField.getPassword());

        if (newPwd.equals("New password") || newPwd.length() < 6) {
            error("Password must be at least 6 characters."); return;
        }
        if (!newPwd.equals(confirm)) {
            error("Passwords do not match."); return;
        }
        // ✅ Uses resetPassword() from UserDAO
        if (userDAO.resetPassword(verifiedEmail, newPwd)) {
            JOptionPane.showMessageDialog(this,
                "Password reset successfully! You can now log in.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            error("Failed to reset password. Please try again.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 34));
    }

    private void styleWithPlaceholder(JTextField f, String placeholder) {
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setForeground(Color.GRAY);
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(Color.BLACK);
                    if (f instanceof JPasswordField)
                        ((JPasswordField) f).setEchoChar('•');
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(Color.GRAY);
                    if (f instanceof JPasswordField)
                        ((JPasswordField) f).setEchoChar((char) 0);
                }
            }
        });
        if (f instanceof JPasswordField)
            ((JPasswordField) f).setEchoChar((char) 0);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
