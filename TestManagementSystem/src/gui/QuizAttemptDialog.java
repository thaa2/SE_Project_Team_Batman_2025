package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quiz.Question;
import quiz.Quiz;
import quiz.QuizAttempt;
import quiz.QuizService;

public class QuizAttemptDialog extends JDialog {
    private final String studentName;
    private final List<Question> questions;
    private final QuizService quizService;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Map<Integer, Character> answers = new HashMap<>();

    private final int timeLimitMinutes; // 0 = no limit
    private final int courseId; // 0 = general
    private final String quizType; // "GENERAL" or "COURSE"
    private final int educatorId; // optional: source educator for GENERAL quizzes
    private int secondsRemaining;
    private javax.swing.Timer countdownTimer;
    private JLabel timeLabel;
    private boolean submitted = false;

    public QuizAttemptDialog(Frame parent, String studentName, List<Question> questions, String title, QuizService quizService, int timeLimitMinutes, int courseId, String quizType, int educatorId) {
        super(parent, "Attempt Quiz - " + title, true);
        this.studentName = studentName;
        this.questions = questions;
        this.quizService = quizService;
        this.timeLimitMinutes = Math.max(0, timeLimitMinutes);
        this.courseId = Math.max(0, courseId);
        this.quizType = quizType != null ? quizType : "GENERAL";
        this.educatorId = Math.max(0, educatorId);
        setSize(600, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Top panel for title and time
        JPanel top = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Attempting: " + title);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        top.add(lblTitle, BorderLayout.WEST);

        timeLabel = new JLabel("");
        timeLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        top.add(timeLabel, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        buildCards();

        add(cards, BorderLayout.CENTER);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton prev = new JButton("Previous");
        JButton next = new JButton("Next");
        JButton submit = new JButton("Submit");

        prev.addActionListener(e -> cardLayout.previous(cards));
        next.addActionListener(e -> cardLayout.next(cards));
        submit.addActionListener(e -> submitQuiz());

        nav.add(prev);
        nav.add(next);
        nav.add(submit);

        add(nav, BorderLayout.SOUTH);

        // Start countdown if needed
        if (this.timeLimitMinutes > 0) {
            this.secondsRemaining = this.timeLimitMinutes * 60;
            updateTimeLabel();
            countdownTimer = new javax.swing.Timer(1000, e -> {
                secondsRemaining--;
                updateTimeLabel();
                if (secondsRemaining <= 0) {
                    countdownTimer.stop();
                    if (!submitted) {
                        JOptionPane.showMessageDialog(this, "Time is up! Auto-submitting your quiz.");
                        submitQuizAuto();
                    }
                }
            });
            countdownTimer.start();
        }
    }

    private void buildCards() {
        int idx = 0;
        for (Question q : questions) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel qLabel = new JLabel((idx + 1) + ". " + q.getText());
            qLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            p.add(qLabel);
            p.add(Box.createVerticalStrut(12));

            String[] opts = q.getOptions();
            if (opts != null && opts.length > 0) {
                ButtonGroup bg = new ButtonGroup();
                JPanel optsPanel = new JPanel(new GridLayout(opts.length, 1, 5, 5));
                char letter = 'A';
                for (int i = 0; i < opts.length; i++) {
                    String optionText = opts[i];
                    JRadioButton rb = new JRadioButton(letter + ". " + optionText);
                    final char answerChar = (char) ("A".charAt(0) + i);
                    rb.addActionListener(ev -> answers.put(q.getId(), answerChar));
                    bg.add(rb);
                    optsPanel.add(rb);
                    letter++;
                }
                p.add(optsPanel);
            } else {
                // Short answer -> text field
                JTextField tf = new JTextField();
                tf.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        String text = tf.getText().trim();
                        if (!text.isEmpty()) answers.put(q.getId(), text.charAt(0));
                    }
                });
                p.add(tf);
            }

            cards.add(p, "q" + idx);
            idx++;
        }
    }

    private void submitQuiz() {
        // For safety, prompt user
        int confirm = JOptionPane.showConfirmDialog(this, "Submit quiz?", "Confirm Submit", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        doSubmit(false);
    }

    // Auto submit without asking for confirmation
    private void submitQuizAuto() {
        doSubmit(true);
    }

    private void doSubmit(boolean auto) {
        if (submitted) return;
        submitted = true;
        if (countdownTimer != null) countdownTimer.stop();

        Quiz quiz = new Quiz("GUI Attempt");
        for (Question q : questions) quiz.addQuestion(q);

        // Pass metadata into the attempt so we save course/type info (include educatorId for general quizzes)
        QuizAttempt attempt = new QuizAttempt(studentName, quiz, quizService, this.courseId, this.quizType, this.educatorId);

        // copy answers
        attempt.getAnswers().putAll(answers);

        int score = quizService.gradeQuiz(attempt);

        if (!auto) {
            JOptionPane.showMessageDialog(this, "You scored " + score + " / " + questions.size());
        } else {
            JOptionPane.showMessageDialog(this, "Time expired. Your quiz was auto-submitted. You scored " + score + " / " + questions.size());
        }
        // Notify parent (if it's a StudentDashboard) so it can refresh its lists/stats
        if (getParent() instanceof StudentDashboard) {
            ((StudentDashboard) getParent()).refreshData();
        }
        dispose();
    }

    private void updateTimeLabel() {
        int mins = secondsRemaining / 60;
        int secs = secondsRemaining % 60;
        String txt = String.format("Time left: %02d:%02d", mins, secs);
        timeLabel.setText(txt);
    }
}
