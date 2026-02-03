package quiz;

public class Question {
    private int id;
    private String text;
    private String[] options;
    private String correctAnswer; // Changed from char to String
    private String questionType;
    private int courseId = 0; // optional association to a course

    // 1. Full Constructor (Used for MCQ) with courseId
    public Question(int id, String text, String[] options, String correctAnswer, String type, int courseId) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.questionType = type;
        this.courseId = courseId;
    }

    // 2. Full Constructor without courseId (backwards compatible)
    public Question(int id, String text, String[] options, String correctAnswer, String type) {
        this(id, text, options, correctAnswer, type, 0);
    }

    // 3. Short Constructor (Used for TF and Short Answer)
    public Question(int id, String text, String correctAnswer, String type) {
        this(id, text, null, correctAnswer, type, 0);
    }

    // 4. Short Constructor with courseId
    public Question(int id, String text, String correctAnswer, String type, int courseId) {
        this(id, text, null, correctAnswer, type, courseId);
    }

    // Getters
    public int getId() { return id; }
    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getQuestionType() { return questionType; }
    public int getCourseId() { return courseId; }

    // This method fixes the error in your DataStore.java screenshot
    public String getCorrectAnswerString() {
        return correctAnswer;
    }
}