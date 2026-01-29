package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import util.DataStore;
import educator.Educator;
import student.Student;
import auth.*;

public class RegisterGUI extends JFrame {
    private JTextField nameField, emailField, ageField, birthDateField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> genderCombo, roleCombo;
    private JButton registerButton, loginButton;
    private DataStore dataStore;

    public RegisterGUI() {
        dataStore = new DataStore();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Test Management System - Register");
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("logo.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Logo file not found, continue without icon
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 750);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with gradient
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

        // Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(120, 30, 300, 40);
        mainPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Join us today!");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setBounds(180, 70, 200, 25);
        mainPanel.add(subtitleLabel);

        // White card panel
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBounds(30, 120, 440, 560);
        cardPanel.setLayout(null);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        int yPos = 20;
        int fieldHeight = 35;
        int labelHeight = 20;
        int spacing = 65;

        // Name
        addFieldLabel(cardPanel, "Full Name", 30, yPos);
        nameField = addTextField(cardPanel, 30, yPos + 25, 380, fieldHeight);
        yPos += spacing;

        // Email
        addFieldLabel(cardPanel, "Email Address", 30, yPos);
        emailField = addTextField(cardPanel, 30, yPos + 25, 380, fieldHeight);
        yPos += spacing;

        // Age
        addFieldLabel(cardPanel, "Age", 30, yPos);
        ageField = addTextField(cardPanel, 30, yPos + 25, 180, fieldHeight);
        
        // Gender (same row)
        addFieldLabel(cardPanel, "Gender", 230, yPos);
        String[] genders = {"Male", "Female"};
        genderCombo = new JComboBox<>(genders);
        genderCombo.setBounds(230, yPos + 25, 180, fieldHeight);
        genderCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        genderCombo.setBackground(Color.WHITE);
        cardPanel.add(genderCombo);
        yPos += spacing;

        // Birth Date
        addFieldLabel(cardPanel, "Birth Date (DD/MM/YYYY)", 30, yPos);
        birthDateField = addTextField(cardPanel, 30, yPos + 25, 180, fieldHeight);
        birthDateField.setToolTipText("Format: DD/MM/YYYY");
        
        // Role (same row)
        addFieldLabel(cardPanel, "Role", 230, yPos);
        String[] roles = {"Student", "Educator"};
        roleCombo = new JComboBox<>(roles);
        roleCombo.setBounds(230, yPos + 25, 180, fieldHeight);
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roleCombo.setBackground(Color.WHITE);
        cardPanel.add(roleCombo);
        yPos += spacing;

        // Password
        addFieldLabel(cardPanel, "Password", 30, yPos);
        passwordField = addPasswordField(cardPanel, 30, yPos + 25, 380, fieldHeight);
        yPos += spacing;

        // Confirm Password
        addFieldLabel(cardPanel, "Confirm Password", 30, yPos);
        confirmPasswordField = addPasswordField(cardPanel, 30, yPos + 25, 380, fieldHeight);
        yPos += spacing + 10;

        // Register button
        registerButton = new JButton("CREATE ACCOUNT");
        registerButton.setBounds(30, yPos, 380, 45);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBackground(new Color(67, 97, 238));
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> handleRegistration());
        
        registerButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                registerButton.setBackground(new Color(57, 87, 228));
            }
            public void mouseExited(MouseEvent e) {
                registerButton.setBackground(new Color(67, 97, 238));
            }
        });
        cardPanel.add(registerButton);
        yPos += 60;

        // Login link
        loginButton = new JButton("Already have an account? Login");
        loginButton.setBounds(100, yPos, 250, 30);
        loginButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loginButton.setForeground(new Color(67, 97, 238));
        loginButton.setBackground(Color.WHITE);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> openLoginForm());
        cardPanel.add(loginButton);

        mainPanel.add(cardPanel);
        add(mainPanel);
    }

    private void addFieldLabel(JPanel panel, String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(60, 60, 60));
        label.setBounds(x, y, 200, 20);
        panel.add(label);
    }

    private JTextField addTextField(JPanel panel, int x, int y, int width, int height) {
        JTextField field = new JTextField();
        field.setBounds(x, y, width, height);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(field);
        return field;
    }

    private JPasswordField addPasswordField(JPanel panel, int x, int y, int width, int height) {
        JPasswordField field = new JPasswordField();
        field.setBounds(x, y, width, height);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(field);
        return field;
    }

    private void handleRegistration() {
        // Validate inputs
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String ageText = ageField.getText().trim();
        String birthDate = birthDateField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String gender = (String) genderCombo.getSelectedItem();
        String role = (String) roleCombo.getSelectedItem();

        // Name validation
        if (!name.matches("[a-zA-Z ]+")) {
            showError("Name must contain only letters and spaces");
            return;
        }

        // Email validation
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Invalid email format");
            return;
        }

        // Age validation
        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0 || age > 120) {
                showError("Please enter a valid age (1-120)");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Age must be a number");
            return;
        }

        // Birth date validation
        if (!birthDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
            showError("Birth date format must be DD/MM/YYYY");
            return;
        }

        // Password validation
        if (password.length() < 8 || !password.matches(".*\\d.*")) {
            showError("Password must be at least 8 characters and include a number");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Create user object
        User user;
        String selectedRole;
        if (role.equals("Student")) {
            user = new Student(0, name, age, gender, birthDate, email, password);
            selectedRole = "STUDENT";
        } else {
            user = new Educator(0, name, age, gender, birthDate, email, password);
            selectedRole = "EDUCATOR";
        }

        // Save to database
        dataStore.InsertUser(name, age, gender, birthDate, email, password, selectedRole);
        
        JOptionPane.showMessageDialog(this, 
            "Registration Successful!\nWelcome, " + name + "!", 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // Launch appropriate dashboard based on role
        if (role.equals("Student")) {
            Student student = new Student(0, name, age, gender, birthDate, email, password);
            new StudentDashboard(student).setVisible(true);
        } else {
            Educator educator = new Educator(0, name, age, gender, birthDate, email, password);
            new EducatorDashboard(educator).setVisible(true);
        }
        
        // Close register window
        dispose();
    }

    private void openLoginForm() {
        new LoginGUI().setVisible(true);
        dispose();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new RegisterGUI().setVisible(true);
        });
    }
}