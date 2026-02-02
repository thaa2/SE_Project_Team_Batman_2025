package forum;

public class Message {
    private int id;
    private int threadId;
    private int authorId;
    private String content;
    private String createdAt;
    private String authorName;

    public Message(int id, int threadId, int authorId, String content, String createdAt, String authorName) {
        this.id = id;
        this.threadId = threadId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
        this.authorName = authorName;
    }

    public int getId() { return id; }
    public int getThreadId() { return threadId; }
    public int getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }
    public String getAuthorName() { return authorName; }
}
