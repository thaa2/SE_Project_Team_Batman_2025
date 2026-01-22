package forum; // This must match your folder name

import java.util.ArrayList;

// Sub-Issue 6.1: Design Post and Forum classes
class Post {
    int id;
    String author;
    String content;

    Post(int id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
    }
}

class Forum {
    String courseName;
    ArrayList<Post> posts = new ArrayList<>();

    Forum(String courseName) {
        this.courseName = courseName;
    }

    void addPost(int id, String user, String text) {
        posts.add(new Post(id, user, text));
    }
    
    void editPost(int id, String newText) {
        for (Post p : posts) {
            if (p.id == id) {
                p.content = newText;
                return;
            }
        }
    }

    void deletePost(int id) {
        posts.removeIf(p -> p.id == id);
    }

    void display() {
        System.out.println("\n--- " + courseName + " Forum ---");
        for (Post p : posts) {
            System.out.println(p.author + ": " + p.content + " (ID: " + p.id + ")");
        }
    }
}

public class ForumApp {
    public static void main(String[] args) {
        // 6.1: Create forum per course
        Forum javaForum = new Forum("Java Programming");

        // 6.2: Create new discussion topics/posts
        javaForum.addPost(1, "User_A", "How do I fix package errors?");
        javaForum.addPost(2, "User_B", "Check your folder structure!");

        // 6.3: Edit and Delete own post
        javaForum.editPost(1, "Fixed it! It was a folder mismatch.");
        javaForum.deletePost(2);

        javaForum.display();
    }
}