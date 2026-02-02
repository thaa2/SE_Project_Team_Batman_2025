package forum;

public class Announcement {
    private int id;
    private String title;
    private String content;
    private int educatorId;
    private String createdAt;
    private String educatorName;

    public Announcement(int id, String title, String content, int educatorId, String createdAt, String educatorName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.educatorId = educatorId;
        this.createdAt = createdAt;
        this.educatorName = educatorName;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getEducatorId() { return educatorId; }
    public String getCreatedAt() { return createdAt; }
    public String getEducatorName() { return educatorName; }

    @Override
    public String toString() {
        return "[" + educatorName + "] " + title + " (" + createdAt + ")";
    }
}
