package course;

public class Course {
    private int id;
    private String courseName;
    private int educatorId;

    public Course(int id, String courseName, int educatorId) {
        this.id = id;
        this.courseName = courseName;
        this.educatorId = educatorId;
    }

    // Getters
    public int getId() { return id; }
    public String getCourseName() { return courseName; }
    public int getEducatorId() { return educatorId; }
}