package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import util.DataStore;
import auth.*;
import student.Student;
import educator.Educator;

public class LoginGUI extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private DataStore dataStore;

    public LoginGUI() {
        dataStore = new DataStore();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Test Management System - Login");
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("logo.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Logo file not found, continue without icon
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                Color color1 = new Color(67, 97, 238);
                Color color2 = new Color(103, 58, 183);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(null);

        // Welcome label
        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBounds(100, 50, 300, 40);
        mainPanel.add(welcomeLabel);

        JLabel subtitleLabel = new JLabel("Sign in to continue");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setBounds(140, 90, 200, 25);
        mainPanel.add(subtitleLabel);

        // White card panel
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBounds(40, 150, 370, 320);
        cardPanel.setLayout(null);
        cardPanel.setBorder(BorderFactory.createEmptyBorder());
        
        // Add shadow effect
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Email label and field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emailLabel.setForeground(new Color(60, 60, 60));
        emailLabel.setBounds(30, 30, 150, 25);
        cardPanel.add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(30, 60, 310, 40);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        cardPanel.add(emailField);

        // Password label and field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passwordLabel.setForeground(new Color(60, 60, 60));
        passwordLabel.setBounds(30, 110, 150, 25);
        cardPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(30, 140, 310, 40);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        cardPanel.add(passwordField);

        // Login button
        loginButton = new JButton("LOGIN");
        loginButton.setBounds(30, 200, 310, 45);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(67, 97, 238));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> handleLogin());
        
        // Hover effect
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(57, 87, 228));
            }
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(67, 97, 238));
            }
        });
        cardPanel.add(loginButton);

        // Register button
        registerButton = new JButton("Don't have an account? Register");
        registerButton.setBounds(60, 260, 250, 30);
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registerButton.setForeground(new Color(67, 97, 238));
        registerButton.setBackground(Color.WHITE);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> openRegisterForm());
        cardPanel.add(registerButton);

        mainPanel.add(cardPanel);

        // Add Enter key support
        passwordField.addActionListener(e -> handleLogin());

        add(mainPanel);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showMessage("Invalid email format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User authenticatedUser = dataStore.getAuthenticatedUser(email, password);

        if (authenticatedUser != null) {
            showMessage("Welcome back, " + authenticatedUser.getName() + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Launch appropriate dashboard based on user role
            if (authenticatedUser.getRole() == Role.STUDENT) {
                Student student = (Student) authenticatedUser;
                new StudentDashboard(student).setVisible(true);
            } else if (authenticatedUser.getRole() == Role.EDUCATOR) {
                Educator educator = (Educator) authenticatedUser;
                new EducatorDashboard(educator).setVisible(true);
            }
            
            // Close login window
            dispose();
        } else {
            showMessage("Invalid email or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    private void openRegisterForm() {
        new RegisterGUI().setVisible(true);
        dispose();
    }

    private void showMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }

    public static void main(String[] args) {
        // Ensure DB tables exist before any GUI action
        new util.DataStore().createTables();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginGUI().setVisible(true);
        });
    }
}