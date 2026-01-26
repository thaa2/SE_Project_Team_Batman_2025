package quiz;

public class Question {
    private int id;
    private String text;
    private String[] options;
    private String correctAnswer; // Changed from char to String
    private String questionType;

    // 1. Full Constructor (Used for MCQ)
    public Question(int id, String text, String[] options, String correctAnswer, String type) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.questionType = type;
    }

    // 2. Short Constructor (Used for TF and Short Answer)
    public Question(int id, String text, String correctAnswer, String type) {
        this(id, text, null, correctAnswer, type);
    }

    // Getters
    public int getId() { return id; }
    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getQuestionType() { return questionType; }

    // This method fixes the error in your DataStore.java screenshot
    public String getCorrectAnswerString() {
        return correctAnswer;
    }
}