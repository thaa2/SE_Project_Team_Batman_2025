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
import quiz.QuizService;
import quiz.Question;
import java.util.Map;
import java.util.HashMap;
import educator.Educator;

public class EducatorDashboard extends JFrame {
    private Educator educator;
    private DataStore dataStore;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private QuizService quizService; 

    // UI models/tables to allow refresh after create/save
    private javax.swing.table.DefaultTableModel coursesTableModel;
    private javax.swing.JTable coursesTable;

    private javax.swing.table.DefaultTableModel resultsTableModel;
    private javax.swing.JTable resultsTable;    

    // Question management fields
    private javax.swing.table.DefaultTableModel questionsTableModel;
    private javax.swing.JTable questionsTable;
    private Map<Integer, Question> questionMap = new HashMap<>();

    // Analytics cached labels (updated on panel creation/refresh)    
    // These labels are used to display computed values for the educator's courses
    private javax.swing.JLabel analyticsAvgLabel;
    private javax.swing.JLabel analyticsAttemptsLabel;
    private javax.swing.JLabel analyticsMaxLabel;
    private javax.swing.JLabel analyticsMinLabel;    

    public EducatorDashboard(Educator educator) {
        this.educator = educator;
        this.quizService = new QuizService();
        this.dataStore = new DataStore();

        // Safety: wrap UI initialization so any exception is shown to the user and logged
        System.out.println("Opening EducatorDashboard for user id=" + (educator != null ? educator.getUserId() : "null") + ", name=" + (educator != null ? educator.getName() : "null"));
        try {
            if (this.educator == null) throw new IllegalStateException("Educator object is null — cannot create dashboard");
            initializeUI();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to open Educator Dashboard: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeUI() {
        System.out.println("Initializing EducatorDashboard UI for educator: " + educator.getName());
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
        mainPanel.add(createManageQuestionsPanel(), "manageQuestions");
        mainPanel.add(createCoursesPanel(), "courses");
        mainPanel.add(new ForumPanel(educator), "forum");
        mainPanel.add(createStudentResultsPanel(), "results");
        mainPanel.add(createAnalyticsPanel(), "analytics");
        mainPanel.add(new ForumPanel(educator), "forum");
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
        headerPanel.setLayout(new BorderLayout(20, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JLabel titleLabel = new JLabel("Educator Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // JLabel userLabel = new JLabel("Welcome, " + educator.getName());
        // userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // userLabel.setForeground(Color.WHITE);
        // userLabel.setHorizontalAlignment(JLabel.RIGHT);
        // headerPanel.add(userLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        String[] menuItems = {"Dashboard", "Add Questions", "Manage Questions", "Manage Courses", "Forum", "Student Results", "Analytics", "Profile"};
        String[] panelNames = {"dashboard", "addQuestions", "manageQuestions", "courses", "forum", "results", "analytics", "profile"};

        for (int i = 0; i < menuItems.length; i++) {
            JButton button = createSidebarButton(menuItems[i], panelNames[i]);
            sidebarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
        button.addActionListener(e -> {
            // Refresh data when opening certain panels
            if ("results".equals(panelName) && resultsTableModel != null) {
                loadResultsIntoTable(resultsTableModel);
            }
            if ("manageQuestions".equals(panelName) && questionsTableModel != null) {
                loadQuestionsIntoTable(questionsTableModel);
            }
            if ("analytics".equals(panelName)) {
                refreshAnalytics();
            }
            cardLayout.show(mainPanel, panelName);
        });
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
        statsPanel.add(createStatCard("Total Questions", new JLabel(String.valueOf(getTotalQuestions())), new Color(67, 97, 238)));
        statsPanel.add(createStatCard("My Courses", new JLabel(String.valueOf(getTotalCourses())), new Color(103, 58, 183)));
        statsPanel.add(createStatCard("Students Taught", new JLabel(String.valueOf(getStudentCount())), new Color(76, 175, 80)));
        statsPanel.add(createStatCard("Quizzes Created", new JLabel(String.valueOf(getQuizCount())), new Color(255, 152, 0)));
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
        actionsPanel.add(createActionCard("Add Questions", "Create new quiz questions", e -> cardLayout.show(mainPanel, "addQuestions")));
        actionsPanel.add(createActionCard("View Results", "Check student performance", e -> { loadResultsIntoTable(resultsTableModel); cardLayout.show(mainPanel, "results"); }));
        actionsPanel.add(createActionCard("Manage Courses", "Create and edit courses", e -> cardLayout.show(mainPanel, "courses")));
        
        actionsWrapperPanel.add(Box.createHorizontalGlue());
        actionsWrapperPanel.add(actionsPanel);
        actionsWrapperPanel.add(Box.createHorizontalGlue());
        
        panel.add(actionsWrapperPanel);

        panel.add(Box.createVerticalGlue());

        return new JScrollPane(panel);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
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

    private JScrollPane createAddQuestionsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Add New Questions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
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

    private JScrollPane createManageQuestionsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 242, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Manage Your Questions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        // Table
        String[] columnNames = {"ID", "Type", "Course", "Question", "Correct", "Options"};
        questionsTableModel = new javax.swing.table.DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        questionsTable = new JTable(questionsTableModel);
        questionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        questionsTable.setRowHeight(30);
        questionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        questionsTable.getTableHeader().setBackground(new Color(67, 97, 238));
        questionsTable.getTableHeader().setForeground(Color.black);
        questionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        JScrollPane tableScroll = new JScrollPane(questionsTable);
        tableScroll.setPreferredSize(new Dimension(0, 400));
        panel.add(tableScroll);

        panel.add(Box.createVerticalStrut(12));

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBackground(new Color(240, 242, 245));
        JButton editBtn = new JButton("Edit Selected");
        JButton delBtn = new JButton("Delete Selected");
        actionsPanel.add(editBtn);
        actionsPanel.add(delBtn);
        panel.add(actionsPanel);

        // Button behaviors
        editBtn.addActionListener(e -> {
            int r = questionsTable.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Please select a question to edit.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
            int id = (int) questionsTableModel.getValueAt(r, 0);
            Question q = questionMap.get(id);
            if (q != null) openEditQuestionDialog(q);
        });

        delBtn.addActionListener(e -> {
            int r = questionsTable.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Please select a question to delete.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
            int id = (int) questionsTableModel.getValueAt(r, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this question?","Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = quizService.deleteQuestion(id, educator.getUserId());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Question deleted.");
                    loadQuestionsIntoTable(questionsTableModel);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete question.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // initial load
        loadQuestionsIntoTable(questionsTableModel);

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
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addActionListener(action);
        card.add(button);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        return card;
    }

    private void loadQuestionsIntoTable(javax.swing.table.DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        questionMap.clear();
        java.util.List<Question> questions = quizService.getQuestionsByTeacher(educator.getUserId());
        for (Question q : questions) {
            String courseName = "General";
            if (q.getCourseId() > 0) {
                // fetch course name
                String sql = "SELECT course_name FROM Courses WHERE id = ?";
                try (Connection conn = DataStore.connect();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, q.getCourseId());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) courseName = rs.getString("course_name");
                } catch (SQLException ex) {
                    System.out.println("Error fetching course name: " + ex.getMessage());
                }
            }
            String opts = null;
            if (q.getOptions() != null) opts = String.join("|", q.getOptions());
            // Display friendly correct answer for TF questions (map stored A/B to True/False)
            String displayCorrect = q.getCorrectAnswer();
            if ("TF".equalsIgnoreCase(q.getQuestionType())) {
                String corr = displayCorrect != null ? displayCorrect : "";
                if (corr.equalsIgnoreCase("A") || corr.equalsIgnoreCase("True")) displayCorrect = "True";
                else if (corr.equalsIgnoreCase("B") || corr.equalsIgnoreCase("False")) displayCorrect = "False";
            }
            model.addRow(new Object[]{q.getId(), q.getQuestionType(), courseName, q.getText(), displayCorrect, opts});
            questionMap.put(q.getId(), q);
        }
    }

    private void openMCQDialog() {
        JDialog dialog = new JDialog(this, "Add MCQ Question", true);
        dialog.setSize(500, 700);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Question Text:"));
        JTextField questionField = new JTextField();
        panel.add(questionField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Select Course (or General):"));
        JComboBox<String> courseBox = new JComboBox<>();
        courseBox.addItem("General (No Course)");
        java.util.Map<String, Integer> courseMap = new java.util.HashMap<>();
        String sql = "SELECT id, course_name FROM Courses WHERE educator_id = ?";
        try (java.sql.Connection conn = DataStore.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("course_name");
                courseBox.addItem(name);
                courseMap.put(name, id);
            }
        } catch (Exception ex) {
            System.out.println("Error loading courses: " + ex.getMessage());
        }
        panel.add(courseBox);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Number of Options (3-5):"));
        int defaultCount = 4;
        JSpinner optionCountSpinner = new JSpinner(new SpinnerNumberModel(defaultCount, 3, 5, 1));
        panel.add(optionCountSpinner);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Options:"));
        JTextField[] optionFields = new JTextField[5];
        JLabel[] optionLabels = new JLabel[5];
        for (int i = 0; i < 5; i++) {
            optionFields[i] = new JTextField();
            optionLabels[i] = new JLabel("Option " + (char)('A' + i) + ":");
            boolean visible = i < defaultCount;
            optionLabels[i].setVisible(visible);
            optionFields[i].setVisible(visible);
            panel.add(optionLabels[i]);
            panel.add(optionFields[i]);
            panel.add(Box.createVerticalStrut(5));
        }

        panel.add(new JLabel("Correct Answer:"));
        JComboBox<String> answerBox = new JComboBox<>();
        // initialize answer choices A.. based on defaultCount
        for (int i = 0; i < defaultCount; i++) answerBox.addItem(String.valueOf((char)('A' + i)));
        panel.add(answerBox);
        panel.add(Box.createVerticalStrut(20));

        // Update visible option fields and answer choices when spinner changes
        optionCountSpinner.addChangeListener(e -> {
            int n = (Integer) optionCountSpinner.getValue();
            // toggle labels and fields visibility
            for (int i = 0; i < 5; i++) {
                boolean show = i < n;
                optionLabels[i].setVisible(show);
                optionFields[i].setVisible(show);
            }
            // update answerBox items
            answerBox.removeAllItems();
            for (int i = 0; i < n; i++) answerBox.addItem(String.valueOf((char)('A' + i)));
            // ensure UI refresh
            panel.revalidate();
            panel.repaint();
            dialog.pack();
        });

        JButton saveButton = new JButton("Save Question");
        saveButton.setBackground(new Color(67, 97, 238));
        saveButton.setForeground(Color.BLACK);
        saveButton.addActionListener(e -> {
            String text = questionField.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Question text cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int numOptions = (Integer) optionCountSpinner.getValue();
            String[] opts = new String[numOptions];
            for (int i = 0; i < numOptions; i++) {
                opts[i] = optionFields[i].getText().trim();
                if (opts[i].isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All options must be filled", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String correct = (String) answerBox.getSelectedItem();

            int courseId = 0; // 0 -> general
            String sel = (String) courseBox.getSelectedItem();
            if (sel != null && courseMap.containsKey(sel)) courseId = courseMap.get(sel);

            quiz.Question question = new quiz.Question(0, text, opts, correct, "MCQ", courseId);
            quizService.saveQuestion(question, educator.getUserId(), courseId);

            JOptionPane.showMessageDialog(dialog, "MCQ Question saved successfully!");
            // Refresh the questions table and analytics so educator sees new data immediately
            if (questionsTableModel != null) loadQuestionsIntoTable(questionsTableModel);
            refreshAnalytics();
            dialog.dispose();
        });
        panel.add(saveButton);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void openEditQuestionDialog(Question q) {
        JDialog dialog = new JDialog(this, "Edit Question", true);
        dialog.setSize(500, 700);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Question Text:"));
        JTextField questionField = new JTextField(q.getText());
        panel.add(questionField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Select Course (or General):"));
        JComboBox<String> courseBox = new JComboBox<>();
        courseBox.addItem("General (No Course)");
        java.util.Map<String, Integer> courseMap = new java.util.HashMap<>();
        String sql = "SELECT id, course_name FROM Courses WHERE educator_id = ?";
        try (java.sql.Connection conn = DataStore.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("course_name");
                courseBox.addItem(name);
                courseMap.put(name, id);
                if (id == q.getCourseId()) courseBox.setSelectedItem(name);
            }
        } catch (Exception ex) {
            System.out.println("Error loading courses: " + ex.getMessage());
        }
        panel.add(courseBox);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Question Type:"));
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"MCQ", "TF", "SHORT"});
        typeBox.setSelectedItem(q.getQuestionType());
        panel.add(typeBox);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Number of Options (3-5):"));
        // Some question types (TF) have 2 options. Ensure the spinner is initialized within the allowed bounds (3-5)
        int rawCount = q.getOptions() != null ? q.getOptions().length : 3;
        int defaultCount = Math.min(Math.max(rawCount, 3), 5); // clamp between 3 and 5
        JSpinner optionCountSpinner = new JSpinner(new SpinnerNumberModel(defaultCount, 3, 5, 1));
        panel.add(optionCountSpinner);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Options:"));
        JTextField[] optionFields = new JTextField[5];
        JLabel[] optionLabels = new JLabel[5];
        for (int i = 0; i < 5; i++) {
            optionFields[i] = new JTextField();
            optionLabels[i] = new JLabel("Option " + (char)('A' + i) + ":");
            boolean visible = i < defaultCount;
            optionLabels[i].setVisible(visible);
            optionFields[i].setVisible(visible);
            if (q.getOptions() != null && i < q.getOptions().length) optionFields[i].setText(q.getOptions()[i]);
            panel.add(optionLabels[i]);
            panel.add(optionFields[i]);
            panel.add(Box.createVerticalStrut(5));
        }

        panel.add(new JLabel("Correct Answer:"));
        // MCQ chooser (A/B/C...)
        JComboBox<String> answerBox = new JComboBox<>();
        for (int i = 0; i < defaultCount; i++) answerBox.addItem(String.valueOf((char)('A' + i)));
        panel.add(answerBox);

        // TF chooser (True/False) - hidden by default
        JComboBox<String> tfBox = new JComboBox<>(new String[]{"True", "False"});
        tfBox.setVisible(false);
        panel.add(tfBox);

        // Short answer field - hidden by default
        JTextField shortAnswerField = new JTextField();
        shortAnswerField.setVisible(false);
        panel.add(shortAnswerField);

        panel.add(Box.createVerticalStrut(20));

        // Initialize visibility based on question type
        String initialType = q.getQuestionType() != null ? q.getQuestionType().toUpperCase() : "MCQ";
        if ("TF".equals(initialType)) {
            // hide MCQ options, show TF box
            answerBox.setVisible(false);
            optionCountSpinner.setVisible(false);
            for (int i = 0; i < 5; i++) { optionLabels[i].setVisible(false); optionFields[i].setVisible(false); }
            tfBox.setVisible(true);
            // Map stored correct ('A'/'B' or 'True'/'False') to TF display
            String corr = q.getCorrectAnswer();
            if (corr != null && (corr.equalsIgnoreCase("A") || corr.equalsIgnoreCase("True"))) tfBox.setSelectedItem("True");
            else tfBox.setSelectedItem("False");
        } else if ("SHORT".equals(initialType)) {
            answerBox.setVisible(false);
            optionCountSpinner.setVisible(false);
            for (int i = 0; i < 5; i++) { optionLabels[i].setVisible(false); optionFields[i].setVisible(false); }
            shortAnswerField.setVisible(true);
            shortAnswerField.setText(q.getCorrectAnswer() != null ? q.getCorrectAnswer() : "");
        } else {
            // MCQ default - setup answer box and selection
            answerBox.setVisible(true);
            optionCountSpinner.setVisible(true);
            for (int i = 0; i < defaultCount; i++) answerBox.addItem(String.valueOf((char)('A' + i)));
            if (q.getCorrectAnswer() != null && !q.getCorrectAnswer().isEmpty()) {
                try { answerBox.setSelectedItem(q.getCorrectAnswer()); } catch (Exception ignored) {}
            }
        }

        // Toggle visibility when question type changes
        typeBox.addActionListener(evt2 -> {
            String selType = ((String) typeBox.getSelectedItem()).toUpperCase();
            boolean isMCQ = "MCQ".equals(selType);
            boolean isTF = "TF".equals(selType);
            boolean isSHORT = "SHORT".equals(selType);

            answerBox.setVisible(isMCQ);
            optionCountSpinner.setVisible(isMCQ);
            for (int i = 0; i < 5; i++) { optionLabels[i].setVisible(isMCQ && i < (Integer) optionCountSpinner.getValue()); optionFields[i].setVisible(isMCQ && i < (Integer) optionCountSpinner.getValue()); }

            tfBox.setVisible(isTF);
            shortAnswerField.setVisible(isSHORT);

            // adjust answer contents
            if (isTF) {
                // map existing correct to True/False
                String corr = q.getCorrectAnswer();
                if (corr != null && (corr.equalsIgnoreCase("A") || corr.equalsIgnoreCase("True"))) tfBox.setSelectedItem("True"); else tfBox.setSelectedItem("False");
            } else if (isSHORT) {
                shortAnswerField.setText(q.getCorrectAnswer() != null ? q.getCorrectAnswer() : "");
            } else {
                // MCQ: ensure answerBox has correct options according to spinner
                answerBox.removeAllItems();
                int n = (Integer) optionCountSpinner.getValue();
                for (int i = 0; i < n; i++) answerBox.addItem(String.valueOf((char)('A' + i)));
                if (q.getCorrectAnswer() != null && !q.getCorrectAnswer().isEmpty()) {
                    try { answerBox.setSelectedItem(q.getCorrectAnswer()); } catch (Exception ignored) {}
                }
            }

            panel.revalidate(); panel.repaint(); dialog.pack();
        });

        // Update visible option fields and answer choices when spinner changes
        optionCountSpinner.addChangeListener(e -> {
            int n = (Integer) optionCountSpinner.getValue();
            for (int i = 0; i < 5; i++) {
                boolean show = i < n;
                optionLabels[i].setVisible(show);
                optionFields[i].setVisible(show);
            }
            if (answerBox.isVisible()) {
                answerBox.removeAllItems();
                for (int i = 0; i < n; i++) answerBox.addItem(String.valueOf((char)('A' + i)));
            }
            panel.revalidate();
            panel.repaint();
            dialog.pack();
        });

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(evt -> {
            String text = questionField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            int selectedCourseId = 0;
            String selCourse = (String) courseBox.getSelectedItem();
            if (selCourse != null && courseMap.containsKey(selCourse)) selectedCourseId = courseMap.get(selCourse);

            int n = (Integer) optionCountSpinner.getValue();
            String[] opts = null;
            String correct = null;
            if ("MCQ".equalsIgnoreCase(type)) {
                opts = new String[n];
                for (int i = 0; i < n; i++) opts[i] = optionFields[i].getText();
                correct = (String) answerBox.getSelectedItem();
            } else if ("TF".equalsIgnoreCase(type)) {
                String tfSel = (String) tfBox.getSelectedItem();
                // store TF as A/B to stay compatible with existing entries
                correct = ("True".equalsIgnoreCase(tfSel)) ? "A" : "B";
            } else {
                correct = shortAnswerField.getText().trim();
            }

            Question updated = new Question(q.getId(), text, opts, correct, type, selectedCourseId);
            boolean ok = quizService.updateQuestion(updated, educator.getUserId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Question updated.");
                loadQuestionsIntoTable(questionsTableModel);
                refreshAnalytics();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update question.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(saveBtn);
        panel.add(Box.createVerticalGlue());

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }


    private void openTrueFalseDialog() {
        JDialog dialog = new JDialog(this, "Add True/False Question", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Question Text:"));
        JTextField questionField = new JTextField();
        panel.add(questionField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Select Course (or General):"));
        JComboBox<String> courseBox = new JComboBox<>();
        courseBox.addItem("General (No Course)");
        java.util.Map<String, Integer> courseMap = new java.util.HashMap<>();
        String sql = "SELECT id, course_name FROM Courses WHERE educator_id = ?";
        try (java.sql.Connection conn = DataStore.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("course_name");
                courseBox.addItem(name);
                courseMap.put(name, id);
            }
        } catch (Exception ex) {
            System.out.println("Error loading courses: " + ex.getMessage());
        }
        panel.add(courseBox);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Correct Answer:"));
        JComboBox<String> answerBox = new JComboBox<>(new String[]{"True", "False"});
        panel.add(answerBox);
        panel.add(Box.createVerticalStrut(20));

        JButton saveButton = new JButton("Save Question");
        saveButton.setBackground(new Color(67, 97, 238));
        saveButton.setForeground(Color.BLACK);
        saveButton.addActionListener(e -> {
            String text = questionField.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Question text cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String correct = ((String) answerBox.getSelectedItem()).equalsIgnoreCase("True") ? "A" : "B";
            int courseId = 0;
            String sel = (String) courseBox.getSelectedItem();
            if (sel != null && courseMap.containsKey(sel)) courseId = courseMap.get(sel);

            quiz.Question question = new quiz.Question(0, text, correct, "TF");
            quizService.saveQuestion(question, educator.getUserId(), courseId);

            JOptionPane.showMessageDialog(dialog, "True/False Question saved successfully!");
            if (questionsTableModel != null) loadQuestionsIntoTable(questionsTableModel);
            refreshAnalytics();
            dialog.dispose();
        });
        panel.add(saveButton);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void openShortAnswerDialog() {
        JDialog dialog = new JDialog(this, "Add Short Answer Question", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Question Text:"));
        JTextField questionField = new JTextField();
        panel.add(questionField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Select Course (or General):"));
        JComboBox<String> courseBox = new JComboBox<>();
        courseBox.addItem("General (No Course)");
        java.util.Map<String, Integer> courseMap = new java.util.HashMap<>();
        String sql = "SELECT id, course_name FROM Courses WHERE educator_id = ?";
        try (java.sql.Connection conn = DataStore.connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("course_name");
                courseBox.addItem(name);
                courseMap.put(name, id);
            }
        } catch (Exception ex) {
            System.out.println("Error loading courses: " + ex.getMessage());
        }
        panel.add(courseBox);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Correct Answer:"));
        JTextField answerField = new JTextField();
        panel.add(answerField);
        panel.add(Box.createVerticalStrut(20));

        JButton saveButton = new JButton("Save Question");
        saveButton.setBackground(new Color(67, 97, 238));
        saveButton.setForeground(Color.BLACK);
        saveButton.addActionListener(e -> {
            String text = questionField.getText().trim();
            String correct = answerField.getText().trim();
            if (text.isEmpty() || correct.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Fields cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int courseId = 0;
            String sel = (String) courseBox.getSelectedItem();
            if (sel != null && courseMap.containsKey(sel)) courseId = courseMap.get(sel);

            quiz.Question question = new quiz.Question(0, text, correct, "SHORT");
            quizService.saveQuestion(question, educator.getUserId(), courseId);

            JOptionPane.showMessageDialog(dialog, "Short Answer Question saved successfully!");
            if (questionsTableModel != null) loadQuestionsIntoTable(questionsTableModel);
            refreshAnalytics();
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
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JButton createCourseButton = new JButton("+ Create New Course");
        createCourseButton.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Bold often looks better for buttons
        createCourseButton.setBackground(new Color(67, 97, 238)); // Your Blue
        createCourseButton.setForeground(Color.WHITE);           // Your White Text

        // Crucial for custom colors to show up on all platforms:
        createCourseButton.setContentAreaFilled(false);
        createCourseButton.setOpaque(true);

        // Removing the default border and adding padding
        createCourseButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        createCourseButton.setFocusPainted(false); // Removes the thin dotted line when clicked

        createCourseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createCourseButton.addActionListener(e -> openCreateCourseDialog());

        panel.add(createCourseButton);
        panel.add(Box.createVerticalStrut(20));

        // Courses list (kept as fields so we can refresh after creating a new course)
        String[] columnNames = {"Course ID", "Course Name", "Lesson Content", "Time Limit"};
        coursesTableModel = new DefaultTableModel(columnNames, 0);
        coursesTable = new JTable(coursesTableModel);
        coursesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        coursesTable.setRowHeight(30);
        coursesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        coursesTable.getTableHeader().setBackground(new Color(67, 97, 238));
        coursesTable.getTableHeader().setForeground(Color.black);
        coursesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        coursesTable.getColumnModel().getColumn(0).setMinWidth(80);
        coursesTable.getColumnModel().getColumn(1).setMinWidth(120);
        coursesTable.getColumnModel().getColumn(2).setMinWidth(150);
        coursesTable.getColumnModel().getColumn(3).setMinWidth(80);

        // Load courses from database into the field model
        loadCoursesIntoTable(coursesTableModel);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        panel.add(scrollPane);

        panel.add(Box.createVerticalStrut(12));
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBackground(new Color(240, 242, 245));
        JButton editCourseBtn = new JButton("Edit Selected");
        JButton delCourseBtn = new JButton("Delete Selected");
        actionsPanel.add(editCourseBtn);
        actionsPanel.add(delCourseBtn);
        panel.add(actionsPanel);

        // Edit course behavior
        editCourseBtn.addActionListener(e -> {
            int r = coursesTable.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Please select a course to edit.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
            int id = (int) coursesTableModel.getValueAt(r, 0);
            openEditCourseDialog(id);
        });

        // Delete course behavior
        delCourseBtn.addActionListener(e -> {
            int r = coursesTable.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Please select a course to delete.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
            int id = (int) coursesTableModel.getValueAt(r, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this course?","Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = deleteCourseFromDatabase(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Course deleted.");
                    loadCoursesIntoTable(coursesTableModel);
                    refreshAnalytics();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

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

        panel.add(new JLabel("Quiz Time Limit (minutes, 0 = no limit):"));
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1440, 1));
        panel.add(timeSpinner);
        panel.add(Box.createVerticalStrut(15));

        JButton saveButton = new JButton("Create Course");
        // Use a nice "Success Green" (like Emerald or Forest Green)
        saveButton.setBackground(new Color(40, 167, 69)); 
        saveButton.setForeground(Color.WHITE); // White text
        saveButton.setContentAreaFilled(false);
        saveButton.setOpaque(true);
        saveButton.setFocusPainted(false); // Removes the focus ring
        saveButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        saveButton.addActionListener(e -> {
            String courseName = courseNameField.getText().trim();
            String content = contentArea.getText().trim();
            int timeLimit = (Integer) timeSpinner.getValue();
            if (!courseName.isEmpty() && !content.isEmpty()) {
                        boolean ok = saveCourseToDatabase(courseName, content, timeLimit);
                if (ok) {
                    // Refresh the courses table to show the newly created course immediately
                    if (coursesTableModel != null) {
                        loadCoursesIntoTable(coursesTableModel);
                        refreshAnalytics();
                    }
                    JOptionPane.showMessageDialog(dialog, "Course created successfully!");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create course. See console for details.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(saveButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private boolean saveCourseToDatabase(String courseName, String lessonContent, int timeLimitMinutes) {
        String sql = "INSERT INTO Courses (course_name, lesson_content, educator_id, time_limit) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, courseName);
            pstmt.setString(2, lessonContent);
            pstmt.setInt(3, educator.getUserId());
            pstmt.setInt(4, timeLimitMinutes);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        System.out.println("Course inserted: id=" + newId + ", name='" + courseName + "', educator_id=" + educator.getUserId());
                    }
                }
                return true;
            } else {
                System.err.println("No rows inserted when saving course: " + courseName);
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving course: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("SQLException saving course: " + e.getMessage());
            return false;
        }
    }

    // Update an existing course. Returns true on success. Includes a small retry when SQLITE_BUSY is encountered.
    private boolean updateCourseInDatabase(int courseId, String courseName, String lessonContent, int timeLimitMinutes) {
        String sql = "UPDATE Courses SET course_name = ?, lesson_content = ?, time_limit = ? WHERE id = ? AND educator_id = ?";
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Connection conn = DataStore.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, courseName);
                pstmt.setString(2, lessonContent);
                pstmt.setInt(3, timeLimitMinutes);
                pstmt.setInt(4, courseId);
                pstmt.setInt(5, educator.getUserId());
                int updated = pstmt.executeUpdate();
                if (updated > 0) System.out.println("✓ Course updated (id=" + courseId + ")");
                return updated > 0;
            } catch (SQLException e) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("database is locked") || msg.contains("busy")) {
                    if (attempt == maxAttempts) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error updating course: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    try { Thread.sleep(150); } catch (InterruptedException ignored) {}
                    continue; // retry
                }
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating course: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    private boolean deleteCourseFromDatabase(int courseId) {
        String sql = "DELETE FROM Courses WHERE id = ? AND educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, educator.getUserId());
            int deleted = pstmt.executeUpdate();
            if (deleted > 0) System.out.println("✓ Course deleted (id=" + courseId + ")");
            return deleted > 0;
        } catch (SQLException e) {
            // If delete failed due to foreign key constraint, offer cascade delete
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("foreign key") || msg.contains("constraint")) {
                int resp = JOptionPane.showConfirmDialog(this, "This course has related quiz attempts or questions and cannot be deleted directly.\nDo you want to delete the course along with ALL associated attempts and questions? This action is irreversible.", "Delete Course and Related Data?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (resp == JOptionPane.YES_OPTION) {
                    boolean ok = deleteCourseAndDependents(courseId);
                    if (!ok) JOptionPane.showMessageDialog(this, "Failed to delete course and its dependents.", "Error", JOptionPane.ERROR_MESSAGE);
                    return ok;
                }
                return false;
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting course: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Deletes attempts details -> quiz scores -> questions -> course in a single transaction
    private boolean deleteCourseAndDependents(int courseId) {
        String delAttemptDetails = "DELETE FROM AttemptDetails WHERE attempt_id IN (SELECT id FROM QuizScores WHERE course_id = ?)";
        String delQuizScores = "DELETE FROM QuizScores WHERE course_id = ?";
        String delQuestions = "DELETE FROM Questions WHERE course_id = ?";
        String delCourse = "DELETE FROM Courses WHERE id = ? AND educator_id = ?";

        try (Connection conn = DataStore.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(delAttemptDetails);
                 PreparedStatement p2 = conn.prepareStatement(delQuizScores);
                 PreparedStatement p3 = conn.prepareStatement(delQuestions);
                 PreparedStatement p4 = conn.prepareStatement(delCourse)) {

                p1.setInt(1, courseId); p1.executeUpdate();
                p2.setInt(1, courseId); p2.executeUpdate();
                p3.setInt(1, courseId); p3.executeUpdate();
                p4.setInt(1, courseId); p4.setInt(2, educator.getUserId());
                int deleted = p4.executeUpdate();

                conn.commit();
                if (deleted > 0) System.out.println("✓ Course and dependents deleted (id=" + courseId + ")");
                return deleted > 0;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting course dependents: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting course: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void openEditCourseDialog(int courseId) {
        // Read course values and close DB resources BEFORE showing the modal dialog to avoid locking issues
        String sql = "SELECT id, course_name, lesson_content, time_limit FROM Courses WHERE id = ? AND educator_id = ?";
        String existingName = null;
        String existingContent = null;
        int existingTimeLimit = 0;
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, educator.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Course not found or you do not have permission to edit it.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                existingName = rs.getString("course_name");
                existingContent = rs.getString("lesson_content");
                existingTimeLimit = rs.getInt("time_limit");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading course: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build and display dialog using already retrieved values
        JDialog dialog = new JDialog(this, "Edit Course", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Course Name:"));
        JTextField courseNameField = new JTextField(existingName);
        panel.add(courseNameField);
        panel.add(Box.createVerticalStrut(15));

        panel.add(new JLabel("Lesson Content:"));
        JTextArea contentArea = new JTextArea(existingContent, 5, 30);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(15));

        panel.add(new JLabel("Quiz Time Limit (minutes, 0 = no limit):"));
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(existingTimeLimit, 0, 1440, 1));
        panel.add(timeSpinner);
        panel.add(Box.createVerticalStrut(15));

        JButton saveButton = new JButton("Save Changes");
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setContentAreaFilled(false);
        saveButton.setOpaque(true);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        saveButton.addActionListener(e -> {
            String courseName = courseNameField.getText().trim();
            String content = contentArea.getText().trim();
            int timeLimit = (Integer) timeSpinner.getValue();
            if (!courseName.isEmpty() && !content.isEmpty()) {
                boolean ok = updateCourseInDatabase(courseId, courseName, content, timeLimit);
                if (ok) {
                    loadCoursesIntoTable(coursesTableModel);
                    refreshAnalytics();
                    JOptionPane.showMessageDialog(dialog, "Course updated successfully!");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(saveButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void loadCoursesIntoTable(DefaultTableModel model) {
        System.out.println("Loading courses for educator id=" + educator.getUserId());
        model.setRowCount(0); // clear existing rows so refresh works correctly
        String sql = "SELECT id, course_name, lesson_content, time_limit FROM Courses WHERE educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int timeLimit = rs.getInt("time_limit");
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("course_name"),
                    rs.getString("lesson_content"),
                    timeLimit > 0 ? timeLimit + " min" : "No limit"
                };
                model.addRow(row);
            }
            System.out.println("Courses loaded into table.");
        } catch (SQLException e) {
            e.printStackTrace();
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
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        String[] columnNames = {"ID", "Student Name", "Score", "Total Questions", "Percentage", "Attempt Date", "Course"};
        resultsTableModel = new DefaultTableModel(columnNames, 0);
        resultsTable = new JTable(resultsTableModel);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultsTable.setRowHeight(30);
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultsTable.getTableHeader().setBackground(new Color(67, 97, 238));
        resultsTable.getTableHeader().setForeground(Color.black);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // hide id column
        resultsTable.getColumnModel().getColumn(0).setMinWidth(0);
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        resultsTable.getColumnModel().getColumn(0).setWidth(0);
        resultsTable.getColumnModel().getColumn(1).setMinWidth(120);
        resultsTable.getColumnModel().getColumn(2).setMinWidth(60);
        resultsTable.getColumnModel().getColumn(2).setMinWidth(100);
        resultsTable.getColumnModel().getColumn(3).setMinWidth(90);
        resultsTable.getColumnModel().getColumn(4).setMinWidth(120);
        resultsTable.getColumnModel().getColumn(5).setMinWidth(140);

        // Load results from database (only results for this educator's courses)
        loadResultsIntoTable(resultsTableModel);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(0, 420));
        panel.add(scrollPane);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewDetailsBtn = new JButton("View Details");
        viewDetailsBtn.addActionListener(e -> {
            int row = resultsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a result to view details.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Integer attemptId = (Integer) resultsTableModel.getValueAt(row, 0);
            if (attemptId != null) openAttemptDetails(attemptId);
        });
        actions.add(viewDetailsBtn);
        panel.add(actions);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void loadResultsIntoTable(DefaultTableModel model) {
        // Show only attempts linked to courses owned by this educator (course_id -> Courses.educator_id)
        model.setRowCount(0);
        String sql = "SELECT qs.id, qs.studentName, qs.totalScore, qs.totalQuestions, qs.percentage, qs.attemptDate, c.course_name, qs.quiz_type "
                   + "FROM QuizScores qs LEFT JOIN Courses c ON qs.course_id = c.id "
                   + "WHERE (c.educator_id = ? OR (qs.quiz_type = 'GENERAL' AND qs.educator_id = ?)) "
                   + "ORDER BY qs.attemptDate DESC";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            pstmt.setInt(2, educator.getUserId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int attemptId = rs.getInt("id");
                String courseName = rs.getString("course_name");
                String quizType = rs.getString("quiz_type");
                if (courseName == null || courseName.trim().isEmpty()) {
                    if ("GENERAL".equalsIgnoreCase(quizType)) courseName = "General"; else courseName = "N/A";
                }
                Object[] row = {
                    attemptId,
                    rs.getString("studentName"),
                    rs.getInt("totalScore"),
                    rs.getInt("totalQuestions"),
                    String.format("%.2f%%", rs.getDouble("percentage")),
                    rs.getString("attemptDate"),
                    courseName
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading results: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        // Update analytics after loading results
        refreshAnalytics();
    }

    private void openAttemptDetails(int attemptId) {
        String sql = "SELECT ad.question_id, ad.selectedAnswer, q.text, q.options, q.correctAnswer, q.questionType " +
                     "FROM AttemptDetails ad JOIN Questions q ON q.id = ad.question_id WHERE ad.attempt_id = ? ORDER BY ad.id ASC";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, attemptId);
            ResultSet rs = pstmt.executeQuery();

            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(new String[]{"#","Question","Student Answer","Correct","Result"}, 0);
            int i = 1;
            boolean any = false;
            while (rs.next()) {
                any = true;
                String sel = rs.getString("selectedAnswer");
                String qtext = rs.getString("text");
                String options = rs.getString("options");
                String correct = rs.getString("correctAnswer");
                String type = rs.getString("questionType");

                String selDisplay = formatAnswerForDisplay(sel, options, type);
                String correctDisplay = formatAnswerForDisplay(correct, options, type);
                String result = (sel != null && !sel.isEmpty() && sel.equals(correct)) ? "Correct" : "Incorrect";
                if (sel == null || sel.isEmpty()) result = "No Answer";

                model.addRow(new Object[]{i++, qtext, selDisplay, correctDisplay, result});
            }

            if (!any) {
                JOptionPane.showMessageDialog(this, "No detailed answers found for this attempt.", "No Details", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JTable table = new JTable(model);
            table.setRowHeight(28);
            table.getColumnModel().getColumn(0).setMaxWidth(50);

            JScrollPane sp = new JScrollPane(table);
            sp.setPreferredSize(new Dimension(800, 400));

            JOptionPane.showMessageDialog(this, sp, "Attempt Details (ID=" + attemptId + ")", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading attempt details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatAnswerForDisplay(String letter, String optionsStr, String type) {
        if (letter == null || letter.isEmpty()) return "-";
        letter = letter.trim();
        if ("MCQ".equalsIgnoreCase(type) && optionsStr != null) {
            String[] opts = optionsStr.split("\\|");
            switch (letter) {
                case "A": return (opts.length > 0 ? opts[0] : "A");
                case "B": return (opts.length > 1 ? opts[1] : "B");
                case "C": return (opts.length > 2 ? opts[2] : "C");
                case "D": return (opts.length > 3 ? opts[3] : "D");
                default: return letter;
            }
        } else if ("TF".equalsIgnoreCase(type)) {
            if (letter.equals("A")) return "True";
            if (letter.equals("B")) return "False";
            return letter;
        } else {
            return letter;
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
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBackground(new Color(240, 242, 245));

        // Create live labels for analytics and seed them with current computed values
        analyticsAvgLabel = new JLabel(getAverageScoreForEducator());
        analyticsAttemptsLabel = new JLabel(String.valueOf(getAttemptCountForEducator()));
        analyticsMaxLabel = new JLabel(getMaxScoreForEducator());
        analyticsMinLabel = new JLabel(getMinScoreForEducator());

        statsPanel.add(createStatCard("Average Student Score", analyticsAvgLabel, new Color(67, 97, 238)));
        statsPanel.add(createStatCard("Total Attempts", analyticsAttemptsLabel, new Color(103, 58, 183)));
        statsPanel.add(createStatCard("Highest Score", analyticsMaxLabel, new Color(76, 175, 80)));
        statsPanel.add(createStatCard("Lowest Score", analyticsMinLabel, new Color(255, 152, 0)));

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
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
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

        String[] labels = {"Name:", "Email:", "Age:", "Gender:", "Birth Date:", "Teacher ID:"};
        String[] values = {educator.getName(), educator.getEmail(), String.valueOf(educator.getAge()), educator.getGender(), educator.getBirthDate(), dataStore.getTeacherIdentifierByUserId(educator.getUserId())};

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

    // Analytics helpers for educator-specific course statistics
    private String getAverageScoreForEducator() {
        String sql = "SELECT AVG(qs.percentage) FROM QuizScores qs JOIN Courses c ON qs.course_id = c.id WHERE c.educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double val = rs.getDouble(1);
                if (rs.wasNull()) return "N/A";
                return String.format("%.2f%%", val);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private int getAttemptCountForEducator() {
        String sql = "SELECT COUNT(*) FROM QuizScores qs JOIN Courses c ON qs.course_id = c.id WHERE c.educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getMaxScoreForEducator() {
        String sql = "SELECT MAX(qs.percentage) FROM QuizScores qs JOIN Courses c ON qs.course_id = c.id WHERE c.educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double val = rs.getDouble(1);
                if (rs.wasNull()) return "N/A";
                return String.format("%.2f%%", val);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String getMinScoreForEducator() {
        String sql = "SELECT MIN(qs.percentage) FROM QuizScores qs JOIN Courses c ON qs.course_id = c.id WHERE c.educator_id = ?";
        try (Connection conn = DataStore.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educator.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double val = rs.getDouble(1);
                if (rs.wasNull()) return "N/A";
                return String.format("%.2f%%", val);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private void refreshAnalytics() {
        if (analyticsAvgLabel != null) analyticsAvgLabel.setText(getAverageScoreForEducator());
        if (analyticsAttemptsLabel != null) analyticsAttemptsLabel.setText(String.valueOf(getAttemptCountForEducator()));
        if (analyticsMaxLabel != null) analyticsMaxLabel.setText(getMaxScoreForEducator());
        if (analyticsMinLabel != null) analyticsMinLabel.setText(getMinScoreForEducator());
    }

    public void showMenu() {
        // For backward compatibility with console-based code
        cardLayout.show(mainPanel, "dashboard");
    }
}
