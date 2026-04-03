package database_setup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RegisterDialog extends JDialog {

    private JTextField     usernameField;
    private JTextField     fullNameField;
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private UserDAO        userDAO;

    public RegisterDialog(JFrame parent, UserDAO userDAO) {
        super(parent, "Register", true);
        this.userDAO = userDAO;
        setSize(420, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(30, 40, 60));
        setContentPane(outer);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        card.setPreferredSize(new Dimension(320, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;

        // Title
        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        title.setForeground(new Color(30, 40, 60));
        card.add(title, gbc);

        // Username
        gbc.gridy++;
        usernameField = new JTextField(15);
        styleTextField(usernameField, "Username");
        card.add(usernameField, gbc);

        // Full Name
        gbc.gridy++;
        fullNameField = new JTextField(15);
        styleTextField(fullNameField, "Full name");
        card.add(fullNameField, gbc);

        // Email
        gbc.gridy++;
        emailField = new JTextField(15);
        styleTextField(emailField, "Email address");
        card.add(emailField, gbc);

        // Password
        gbc.gridy++;
        passwordField = new JPasswordField(15);
        styleTextField(passwordField, "Password");
        card.add(passwordField, gbc);

        // Confirm Password
        gbc.gridy++;
        confirmField = new JPasswordField(15);
        styleTextField(confirmField, "Confirm password");
        card.add(confirmField, gbc);

        // Register button
        gbc.gridy++;
        gbc.insets = new Insets(14, 0, 5, 0);
        JButton regBtn = new JButton("Register");
        regBtn.setBackground(new Color(255, 165, 0));
        regBtn.setForeground(Color.WHITE);
        regBtn.setFont(new Font("Arial", Font.BOLD, 14));
        regBtn.setFocusPainted(false);
        regBtn.setBorderPainted(false);
        regBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regBtn.setPreferredSize(new Dimension(200, 36));
        regBtn.addActionListener(e -> doRegister());
        card.add(regBtn, gbc);

        // Back to login
        gbc.gridy++;
        gbc.insets = new Insets(4, 0, 0, 0);
        JLabel back = new JLabel("Back to login");
        back.setForeground(new Color(0, 100, 200));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setFont(new Font("Arial", Font.PLAIN, 11));
        back.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
        });
        card.add(back, gbc);

        getRootPane().setDefaultButton(regBtn);
        outer.add(card);
    }

    private void styleTextField(JTextField f, String placeholder) {
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
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

    private void doRegister() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String pwd      = new String(passwordField.getPassword());
        String confirm  = new String(confirmField.getPassword());

        if (username.equals("Username") || username.isEmpty()) {
            error("Please enter a username."); return;
        }
        if (username.length() < 3) {
            error("Username must be at least 3 characters."); return;
        }
        if (fullName.equals("Full name") || fullName.isEmpty()) {
            error("Please enter your full name."); return;
        }
        if (email.equals("Email address") || email.isEmpty() || !email.contains("@")) {
            error("Enter a valid email address."); return;
        }
        if (pwd.equals("Password") || pwd.length() < 6) {
            error("Password must be at least 6 characters."); return;
        }
        if (!pwd.equals(confirm)) {
            error("Passwords do not match."); return;
        }

        boolean success = userDAO.registerUser(username, pwd, email, fullName);
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Account created successfully! You can now log in.",
                "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            error("Registration failed. Username or email may already be in use.");
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
