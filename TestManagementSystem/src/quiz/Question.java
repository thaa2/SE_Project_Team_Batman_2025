package quiz;

public class Question {
    private int id;
    private String text;
    private String[] options;
    private char correctAnswer;
    private String type;

    public Question(int id, String text, String[] options, char correctAnswer) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctAnswer = Character.toUpperCase(correctAnswer);
        this.type = "MCQ";
    }

    public Question(int id, String text, char correctAnswer) {
        this.id = id;
        this.text = text;
        this.correctAnswer = Character.toUpperCase(correctAnswer);
        this.type = "TF";
        this.options = new String[]{"A. True", "B. False"};
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public char getCorrectAnswer() { return correctAnswer; }
}