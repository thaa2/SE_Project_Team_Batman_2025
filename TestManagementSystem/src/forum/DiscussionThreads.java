package forum;

import java.util.ArrayList;

class Topic {
    String title;
    ArrayList<String> replies;

    Topic(String title) {
        this.title = title;
        this.replies = new ArrayList<>();
    }

    void addReply(String reply) {
        replies.add(reply);
    }

    void showThread() {
        System.out.println("Topic: " + title);
        for (String r : replies) {
            System.out.println("  -> Reply: " + r);
        }
    }
}

public class DiscussionThreads {
    public static void main(String[] args) {
        // Sub-Task: Create new discussion topic
        Topic examTopic = new Topic("Midterm Exam Date?");
        
        // Sub-Task: Reply to topics
        examTopic.addReply("It is scheduled for next Monday.");
        examTopic.addReply("Will it cover Chapter 5?");
        
        examTopic.showThread();
    }
}