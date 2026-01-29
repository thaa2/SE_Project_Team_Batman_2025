package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import util.DataStore;
import quiz.QuizManager;
import quiz.QuizService;
import auth.User;
import educator.Educator;

public class EducatorDashboard extends JFrame {
    private Educator educator;
    private DataStore dataStore;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private QuizService quizService;
    private QuizManager quizManager;

    public EducatorDashboard(Educator educator) {
        this.educator = educator;
        this.dataStore = new DataStore();
        this.quizService = new QuizService();
        this.quizManager = new QuizManager(quizService);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Educator Dashboard - Test Management System");
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("logo.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Logo file not found, continue without icon
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
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
        mainPanel.add(createAddQuestionsPanel(), "addQuestions");
        mainPanel.add(createCoursesPanel(), "courses");
        mainPanel.add(createStudentResultsPanel(), "results");
        mainPanel.add(createAnalyticsPanel(), "analytics");
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

        JLabel titleLabel = new JLabel("Educator Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel userLabel = new JLabel("Welcome, " + educator.getName());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(255, 255, 255, 200));
        headerPanel.add(userLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        String[] menuItems = {"📊 Dashboard", "❓ Add Questions", "📚 Manage Courses", "👥 Student Results", "📈 Analytics", "👤 Profile"};
        String[] panelNames = {"dashboard", "addQuestions", "courses", "results", "analytics", "profile"};

        for (int i = 0; i < menuItems.length; i++) {
            JButton button = createSidebarButton(menuItems[i], panelNames[i]);
            sidebarPanel.add(button);
            if (i < menuItems.length - 1) {
                sidebarPanel.add(Box.createVerticalStrut(10));
            }
        }

        sidebarPanel.add(Box.createVerticalGlue());

        // Logout button
        JButton logoutButton = new JButton("🚪 Logout");
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
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
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

        JLabel welcomeLabel = new JLabel("Welcome, " + educator.getName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(60, 60, 60));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(welcomeLabel);

        panel.add(Box.createVerticalStrut(20));

        // Statistics cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(new Color(240, 242, 245));
        statsPanel.add(createStatCard("❓ Total Questions", String.valueOf(getTotalQuestions()), new Color(67, 97, 238)));
        statsPanel.add(createStatCard("📚 My Courses", String.valueOf(getTotalCourses()), new Color(103, 58, 183)));
        statsPanel.add(createStatCard("👥 Students Taught", String.valueOf(getStudentCount()), new Color(76, 175, 80)));
        statsPanel.add(createStatCard("🎯 Quizzes Created", String.valueOf(getQuizCount()), new Color(255, 152, 0)));
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
        actionsPanel.add(createActionCard("❓ Add Questions", "Create new quiz questions", e -> cardLayout.show(mainPanel, "addQuestions")));
        actionsPanel.add(createActionCard("📊 View Results", "Check student performance", e -> cardLayout.show(mainPanel, "results")));
        actionsPanel.add(createActionCard("📚 Manage Courses", "Create and edit courses", e -> cardLayout.show(mainPanel, "courses")));
        
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
        button.setForeground(Color.WHITE);
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

    private JScrollPane createAddQuestionsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Add New Questions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        JLabel instructionLabel = new JLabel("Select a question type to add:");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructionLabel.setForeground(new Color(60, 60, 60));
        cardPanel.add(instructionLabel);

        cardPanel.add(Box.createVerticalStrut(20));

        JPanel questionTypesPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        questionTypesPanel.setBackground(Color.WHITE);
        questionTypesPanel.add(createQuestionTypeCard("MCQ", "Multiple Choice Question", new Color(67, 97, 238), e -> {
            openMCQDialog();
        }));
        questionTypesPanel.add(createQuestionTypeCard("True/False", "True or False", new Color(103, 58, 183), e -> {
            openTrueFalseDialog();
        }));
        questionTypesPanel.add(createQuestionTypeCard("Short Answer", "Text-based answer", new Color(76, 175, 80), e -> {
            openShortAnswerDialog();
        }));

        cardPanel.add(questionTypesPanel);
        cardPanel.add(Box.createVerticalGlue());

        panel.add(cardPanel);
        panel.add(Box.createVerticalGlue());

        return new JScrollPane(panel);
    }

    private JPanel createQuestionTypeCard(String title, String description, Color color, ActionListener action) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(240, 245, 255));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        card.setBackground(new Color(240, 245, 255));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(color);
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 100, 100));
        card.add(descLabel);

        card.add(Box.createVerticalGlue());

        JButton button = new JButton("Add " + title);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addActionListener(action);
        card.add(button);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        return card;
    }

    private void openMCQDialog() {
        JDialog dialog = new JDialog(this, "Add MCQ Question", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Question Text:"));
        JTextField questionField = new JTextField();
        panel.add(questionField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Number of Options (3-5):"));
        JSpinner optionCountSpinner = new JSpinner(new SpinnerNumberModel(4, 3, 5, 1));
        panel.add(optionCountSpinner);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Options:"));
        JTextField[] optionFields = new JTextField[5];
        for (int i = 0; i < 5; i++) {
            optionFields[i] = new JTextField();
            optionFields[i].setEnabled(i < 4);
            panel.add(new JLabel("Option " + (char)('A' + i) + ":"));
            panel.add(optionFields[i]);
            panel.add(Box.createVerticalStrut(5));
        }

        panel.add(new JLabel("Correct Answer:"));
        JComboBox<String> answerBox = new JComboBox<>(new String[]{"A", "B", "C", "D", "E"});
        panel.add(answerBox);
        panel.add(Box.createVerticalStrut(20));

        JButton saveButton = new JButton("Save Question");
        saveButton.setBackground(new Color(67, 97, 238));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "MCQ Question saved successfully!");
            dialog.dispose();
        });
        panel.add(saveButton);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void openTrueFalseDialog() {
        JDialog dialog = new JDialog(this, "Add True/False Question", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Question Text:"));
        JTextField questionField = new JTextField();
        panel.add(questionField);
        panel.add(Box.createVerticalStrut(15));

        panel.add(new JLabel("Correct Answer:"));
        JComboBox<String> answerBox = new JComboBox<>(new String[]{"True", "False"});
        panel.add(answerBox);
        panel.add(Box.createVerticalStrut(20));

        JButton saveButton = new JButton("Save Question");
        saveButton.setBackground(new Color(67, 97, 238));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "True/False Question saved successfully!");
            dialog.dispose();
        });
        panel.add(saveButton);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void openShortAnswerDialog() {
        JDialog dialog = new JDialog(this, "Add Short Answer Question", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Question Text:"));
        JTextField questionField = new JTextField();
        panel.add(questionField);
        panel.add(Box.createVerticalStrut(15));

        panel.add(new JLabel("Correct Answer:"));
        JTextField answerField = new JTextField();
        panel.add(answerField);
        panel.add(Box.createVerticalStrut(20));

        JButton saveButton = new JButton("Save Question");
        saveButton.setBackground(new Color(67, 97, 238));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Short Answer Question saved successfully!");
            dialog.dispose();
        });
        panel.add(saveButton);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private JComponent createCoursesPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Manage Your Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JButton createCourseButton = new JButton("+ Create New Course");
        createCourseButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        createCourseButton.setBackground(new Color(67, 97, 238));
        createCourseButton.setForeground(Color.WHITE);
        createCourseButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        createCourseButton.addActionListener(e -> openCreateCourseDialog());
        panel.add(createCourseButton);

        panel.add(Box.createVerticalStrut(20));

        // Courses list
        String[] columnNames = {"Course ID", "Course Name", "Lesson Content", "Action"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable coursesTable = new JTable(model);
        coursesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        coursesTable.setRowHeight(30);
        coursesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        coursesTable.getTableHeader().setBackground(new Color(67, 97, 238));
        coursesTable.getTableHeader().setForeground(Color.WHITE);

        // Load courses from database
        loadCoursesIntoTable(model);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        panel.add(scrollPane);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void openCreateCourseDialog() {
        JDialog dialog = new JDialog(this, "Create New Course", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Course Name:"));
        JTextField courseNameField = new JTextField();
        panel.add(courseNameField);
        panel.add(Box.createVerticalStrut(15));

        panel.add(new JLabel("Lesson Content:"));
        JTextArea contentArea = new JTextArea(5, 30);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(15));

        JButton saveButton = new JButton("Create Course");
        saveButton.setBackground(new Color(76, 175, 80));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            String courseName = courseNameField.getText().trim();
            String content = contentArea.getText().trim();
            if (!courseName.isEmpty() && !content.isEmpty()) {
                saveCourseToDatabase(courseName, content);
                JOptionPane.showMessageDialog(dialog, "Course created successfully!");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(saveButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void saveCourseToDatabase(String courseName, String lessonContent) {
        String sql = "INSERT INTO Courses (course_name, lesson_content, educator_id) VALUES (?, ?, ?)";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseName);
            pstmt.setString(2, lessonContent);
            pstmt.setInt(3, educator.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving course: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCoursesIntoTable(DefaultTableModel model) {
        String sql = "SELECT id, course_name, lesson_content FROM Courses WHERE educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("course_name"),
                    rs.getString("lesson_content"),
                    "Edit"
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JComponent createStudentResultsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Student Results");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        String[] columnNames = {"Student Name", "Score", "Total Questions", "Percentage", "Attempt Date"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable resultsTable = new JTable(model);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultsTable.setRowHeight(30);
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultsTable.getTableHeader().setBackground(new Color(67, 97, 238));
        resultsTable.getTableHeader().setForeground(Color.WHITE);

        // Load results from database
        loadResultsIntoTable(model);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        panel.add(scrollPane);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void loadResultsIntoTable(DefaultTableModel model) {
        String sql = "SELECT studentName, totalScore, totalQuestions, percentage, attemptDate FROM QuizScores ORDER BY attemptDate DESC";
        try (Connection conn = DataStore.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("studentName"),
                    rs.getInt("totalScore"),
                    rs.getInt("totalQuestions"),
                    String.format("%.2f%%", rs.getDouble("percentage")),
                    rs.getString("attemptDate")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading results: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JComponent createAnalyticsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Analytics & Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBackground(new Color(240, 242, 245));
        statsPanel.add(createStatCard("Average Student Score", "75.5%", new Color(67, 97, 238)));
        statsPanel.add(createStatCard("Total Attempts", "156", new Color(103, 58, 183)));
        statsPanel.add(createStatCard("Highest Score", "98%", new Color(76, 175, 80)));
        statsPanel.add(createStatCard("Lowest Score", "42%", new Color(255, 152, 0)));

        panel.add(statsPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
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
        String[] values = {educator.getName(), educator.getEmail(), String.valueOf(educator.getAge()), educator.getGender(), educator.getBirthDate(), String.valueOf(educator.getUserId())};

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

    // Helper methods to get statistics
    private int getTotalQuestions() {
        String sql = "SELECT COUNT(*) FROM Questions WHERE educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalCourses() {
        String sql = "SELECT COUNT(*) FROM Courses WHERE educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getStudentCount() {
        String sql = "SELECT COUNT(DISTINCT studentName) FROM QuizScores";
        try (Connection conn = DataStore.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getQuizCount() {
        String sql = "SELECT COUNT(*) FROM QuizScores WHERE studentName IN (SELECT DISTINCT studentName FROM QuizScores)";
        try (Connection conn = DataStore.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void showMenu() {
        // For backward compatibility with console-based code
        cardLayout.show(mainPanel, "dashboard");
    }
}
