package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import util.DataStore;
import auth.User;
import student.Student;

public class StudentDashboard extends JFrame {
    private Student student;
    private DataStore dataStore;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public StudentDashboard(Student student) {
        this.student = student;
        this.dataStore = new DataStore();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Student Dashboard - Test Management System");
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("logo.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Logo file not found, continue without icon
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        // Main container
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(240, 242, 245));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        container.add(headerPanel, BorderLayout.NORTH);

        // Sidebar and Content
        JPanel bodyPanel = new JPanel(new BorderLayout());
        JPanel sidebarPanel = createSidebarPanel();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(240, 242, 245));

        // Add different panels to CardLayout
        mainPanel.add(createDashboardPanel(), "dashboard");
        mainPanel.add(createResultsPanel(), "results");
        mainPanel.add(createAttemptPanel(), "attempt");
        mainPanel.add(createCoursesPanel(), "courses");
        mainPanel.add(new ForumPanel(student), "forum");
        mainPanel.add(createProfilePanel(), "profile");

        bodyPanel.add(sidebarPanel, BorderLayout.WEST);
        bodyPanel.add(mainPanel, BorderLayout.CENTER);

        container.add(bodyPanel, BorderLayout.CENTER);
        setContentPane(container);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
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
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JLabel titleLabel = new JLabel("Student Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // JLabel userLabel = new JLabel("Welcome, " + student.getName());
        // userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // userLabel.setForeground(new Color(255, 255, 255, 200));
        // headerPanel.add(userLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        String[] menuItems = {"Dashboard", "My Results", "Attempt Quiz", "Courses", "Forum", "Profile"};
        String[] panelNames = {"dashboard", "results", "attempt", "courses", "forum", "profile"};

        for (int i = 0; i < menuItems.length; i++) {
            JButton button = createSidebarButton(menuItems[i], panelNames[i]);
            sidebarPanel.add(button);
            if (i < menuItems.length - 1) {
                sidebarPanel.add(Box.createVerticalStrut(10));
            }
        }

        sidebarPanel.add(Box.createVerticalGlue());

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        logoutButton.setOpaque(true);
        logoutButton.setContentAreaFilled(true);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                new LoginGUI().setVisible(true);
            }
        });
        sidebarPanel.add(logoutButton);

        return sidebarPanel;
    }

    private JButton createSidebarButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBackground(new Color(240, 242, 245));
        button.setForeground(new Color(60, 60, 60));
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(220, 230, 250));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(240, 242, 245));
            }
        });
        button.addActionListener(e -> cardLayout.show(mainPanel, panelName));
        return button;
    }

    private JComponent createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel welcomeLabel = new JLabel("Welcome to Your Dashboard, " + student.getName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(60, 60, 60));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(welcomeLabel);

        panel.add(Box.createVerticalStrut(20));

        // Statistics cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(new Color(240, 242, 245));
        statsPanel.add(createStatCard("Quizzes Attempted", "0", new Color(67, 97, 238)));
        statsPanel.add(createStatCard("Average Score", "0%", new Color(103, 58, 183)));
        statsPanel.add(createStatCard("Courses Enrolled", "0", new Color(76, 175, 80)));
        panel.add(statsPanel);

        panel.add(Box.createVerticalStrut(30));

        // Quick actions
        JLabel actionsLabel = new JLabel("Quick Actions");
        actionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        actionsLabel.setForeground(new Color(60, 60, 60));
        actionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(actionsLabel);

        panel.add(Box.createVerticalStrut(15));

        JPanel actionsWrapperPanel = new JPanel();
        actionsWrapperPanel.setBackground(new Color(240, 242, 245));
        actionsWrapperPanel.setLayout(new BoxLayout(actionsWrapperPanel, BoxLayout.X_AXIS));
        
        JPanel actionsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        actionsPanel.setBackground(new Color(240, 242, 245));
        actionsPanel.setMaximumSize(new Dimension(900, 200));
        actionsPanel.add(createActionCard("Attempt Quiz", "Take a new quiz", e -> cardLayout.show(mainPanel, "attempt")));
        actionsPanel.add(createActionCard("View Results", "Check your scores", e -> cardLayout.show(mainPanel, "results")));
        actionsPanel.add(createActionCard("Browse Courses", "Enroll in courses", e -> cardLayout.show(mainPanel, "courses")));
        
        actionsWrapperPanel.add(Box.createHorizontalGlue());
        actionsWrapperPanel.add(actionsPanel);
        actionsWrapperPanel.add(Box.createHorizontalGlue());
        
        panel.add(actionsWrapperPanel);

        panel.add(Box.createVerticalGlue());

        return new JScrollPane(panel);
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(150, 150, 150));
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(10));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);
        card.add(valueLabel);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        return card;
    }

    private JPanel createActionCard(String title, String description, ActionListener action) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(60, 60, 60));
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(150, 150, 150));
        card.add(descLabel);

        card.add(Box.createVerticalGlue());

        JButton button = new JButton("Go");
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(67, 97, 238));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addActionListener(action);
        card.add(button);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        return card;
    }

    private JComponent createResultsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Your Quiz Results");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        // Results table
        String[] columnNames = {"Quiz", "Score", "Total", "Percentage", "Date"};
        Object[][] data = {}; // Will be populated from database

        JTable resultsTable = new JTable(data, columnNames);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultsTable.setRowHeight(30);
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultsTable.getTableHeader().setBackground(new Color(67, 97, 238));
        resultsTable.getTableHeader().setForeground(Color.black);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        resultsTable.getColumnModel().getColumn(0).setMinWidth(100);
        resultsTable.getColumnModel().getColumn(1).setMinWidth(70);
        resultsTable.getColumnModel().getColumn(2).setMinWidth(70);
        resultsTable.getColumnModel().getColumn(3).setMinWidth(90);
        resultsTable.getColumnModel().getColumn(4).setMinWidth(100);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        panel.add(scrollPane);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JComponent createAttemptPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Attempt a Quiz");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JLabel infoLabel = new JLabel("Select a quiz category to begin:");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(infoLabel);

        panel.add(Box.createVerticalStrut(15));

        JPanel quizPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        quizPanel.setBackground(new Color(240, 242, 245));
        quizPanel.add(createQuizCard("General Quiz", "Test your general knowledge", e -> {
            JOptionPane.showMessageDialog(this, "General Quiz selected. Starting quiz...");
        }));
        quizPanel.add(createQuizCard("Course Quiz", "Take a course-specific quiz", e -> {
            JOptionPane.showMessageDialog(this, "Course Quiz selected. Choose a course...");
        }));

        panel.add(quizPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JComponent createCoursesPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Available Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JPanel coursesPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        coursesPanel.setBackground(new Color(240, 242, 245));

        // Sample courses
        coursesPanel.add(createCourseCard("Java Programming", "Learn Java basics"));
        coursesPanel.add(createCourseCard("Web Development", "HTML, CSS, JavaScript"));
        coursesPanel.add(createCourseCard("Database Design", "SQL and NoSQL"));
        coursesPanel.add(createCourseCard("Data Structures", "Algorithms and Complexity"));

        panel.add(coursesPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createCourseCard(String courseName, String description) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setBackground(Color.WHITE);

        JLabel courseNameLabel = new JLabel(courseName);
        courseNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        courseNameLabel.setForeground(new Color(60, 60, 60));
        card.add(courseNameLabel);

        card.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(150, 150, 150));
        card.add(descLabel);

        card.add(Box.createVerticalGlue());

        JButton enrollButton = new JButton("Enroll");
        enrollButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        enrollButton.setBackground(new Color(76, 175, 80));
        enrollButton.setForeground(Color.BLACK);
        enrollButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        card.add(enrollButton);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        return card;
    }

    private JPanel createQuizCard(String quizName, String description, ActionListener action) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setBackground(Color.WHITE);

        JLabel quizNameLabel = new JLabel(quizName);
        quizNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        quizNameLabel.setForeground(new Color(60, 60, 60));
        card.add(quizNameLabel);

        card.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(150, 150, 150));
        card.add(descLabel);

        card.add(Box.createVerticalGlue());

        JButton startButton = new JButton("Start Quiz →");
        startButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        startButton.setBackground(new Color(67, 97, 238));
        startButton.setForeground(Color.BLACK);
        startButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        startButton.addActionListener(action);
        card.add(startButton);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        return card;
    }

    private JComponent createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Your Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JPanel profileCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        profileCard.setBackground(Color.WHITE);

        String[] labels = {"Name:", "Email:", "Age:", "Gender:", "Birth Date:", "User ID:"};
        String[] values = {student.getName(), student.getEmail(), String.valueOf(student.getAge()), student.getGender(), student.getBirthDate(), String.valueOf(student.getUserId())};

        for (int i = 0; i < labels.length; i++) {
            JPanel fieldPanel = new JPanel(new BorderLayout(10, 0));
            fieldPanel.setBackground(Color.WHITE);
            fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            JLabel labelLabel = new JLabel(labels[i]);
            labelLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            labelLabel.setForeground(new Color(60, 60, 60));
            labelLabel.setPreferredSize(new Dimension(100, 0));

            JLabel valueLabel = new JLabel(values[i]);
            valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            valueLabel.setForeground(new Color(100, 100, 100));

            fieldPanel.add(labelLabel, BorderLayout.WEST);
            fieldPanel.add(valueLabel, BorderLayout.CENTER);
            profileCard.add(fieldPanel);

            if (i < labels.length - 1) {
                profileCard.add(Box.createVerticalStrut(15));
            }
        }

        panel.add(profileCard);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    public void showMenu() {
        // For backward compatibility with console-based code
        cardLayout.show(mainPanel, "dashboard");
    }

    private void viewResults() {
        dataStore.displayStudentResults(student.getName());
    }
}
