package quiz;

public class Question {
    private int id;
    private String text;
    private String[] options;
    private char correctAnswer;
    private String questionType;
    private int numberOfOptions;

    public Question(int id, String text, String[] options, char correctAnswer, int numberOfOptions) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctAnswer = Character.toUpperCase(correctAnswer);
        this.questionType = "MCQ";
        this.numberOfOptions = Math.max(3, Math.min(5, numberOfOptions));
        this.questionType = "MCQ";
        this.numberOfOptions = Math.max(3, Math.min(5, numberOfOptions));
    }

    public Question(int id, String text, char correctAnswer) {
        this.id = id;
        this.text = text;
        this.questionType = "TF";
        this.correctAnswer = Character.toUpperCase(correctAnswer);
        this.numberOfOptions = 2;
        this.options = new String[]{"A. True", "B. False"};
    }

    public Question(String text, String[] options, char correctAnswer, int numberOfOptions) {
        this(-1, text, options, correctAnswer, numberOfOptions);
    }

    public Question(String text, char correctAnswer) {
        this(-1, text, correctAnswer);
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public char getCorrectAnswer() { return correctAnswer; }
    
    
    public String getQuestionType() { return questionType; }
    public int getNumberOfOptions() { return numberOfOptions; }
    
    public String getAnswerRange() {
        if (questionType.equals("TF")) {
            return "A-B";
        } else {
            switch (numberOfOptions) {
                case 3: return "A-C";
                case 4: return "A-D";
                case 5: return "A-E";
                default: return "A-E";
            }
        }
        }
    }
