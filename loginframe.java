package database_setup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private UserDAO        userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Online Shopping Cart");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 380);
        setLocationRelativeTo(null);
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
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.insets    = new Insets(5, 0, 5, 0);
        gbc.gridx     = 0;
        gbc.gridy     = 0;
        gbc.gridwidth = 2;

        // Title
        JLabel title = new JLabel("Login");
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        card.add(title, gbc);

        // Username
        gbc.gridy++;
        usernameField = new JTextField(15);
        usernameField.setBorder(BorderFactory.createTitledBorder("Username"));
        card.add(usernameField, gbc);

        // Password
        gbc.gridy++;
        passwordField = new JPasswordField(15);
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));
        card.add(passwordField, gbc);

        // Login button
        gbc.gridy++;
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(255, 165, 0));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> doLogin());
        card.add(loginBtn, gbc);

        // Forgot password
        gbc.gridy++;
        JLabel forgot = new JLabel("Forgot password?");
        forgot.setForeground(new Color(0, 100, 200));
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { openForgotPassword(); }
        });
        card.add(forgot, gbc);

        // Register
        gbc.gridy++;
        JLabel reg = new JLabel("New user? Register here");
        reg.setForeground(new Color(0, 100, 200));
        reg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reg.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { openRegister(); }
        });
        card.add(reg, gbc);

        getRootPane().setDefaultButton(loginBtn);
        outer.add(card);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter username and password.",
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int[] result = userDAO.authenticate(username, password);
        if (result == null) {
            JOptionPane.showMessageDialog(this,
                "Invalid username or password.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            int    userId       = result[0];
            String loggedInUser = userDAO.getLastUsername();
            dispose(); // Close login window
            SwingUtilities.invokeLater(() ->
                new MainWindow(userId, loggedInUser) // Open main window
            );
        }
    }

    private void openForgotPassword() {
        new ForgotPasswordDialog(this, userDAO);
    }

    private void openRegister() {
        new RegisterDialog(this, userDAO);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
