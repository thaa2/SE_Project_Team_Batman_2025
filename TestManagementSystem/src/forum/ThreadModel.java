package forum;

public class ThreadModel {
    private int id;
    private String title;
    private int creatorId;
    private String createdAt;

    public ThreadModel(int id, String title, int creatorId, String createdAt) {
        this.id = id;
        this.title = title;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getCreatorId() { return creatorId; }
    public String getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return title + " (" + createdAt + ")";
    }
}
