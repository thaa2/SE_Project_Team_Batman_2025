package forum;

import java.util.ArrayList;

class UserPost {
    int id;
    String text;

    UserPost(int id, String text) {
        this.id = id;
        this.text = text;
    }

    // Sub-Task: Edit own post
    void editPost(String newText) {
        this.text = newText;
        System.out.println("Post " + id + " updated.");
    }
}

public class PostManagement {
    public static void main(String[] args) {
        ArrayList<UserPost> myPosts = new ArrayList<>();
        myPosts.add(new UserPost(1, "Hello everyone!"));
        myPosts.add(new UserPost(2, "Java is hard."));

        // Edit post
        myPosts.get(1).editPost("Java is challenging but fun!");

        // Sub-Task: Delete own post
        myPosts.remove(0); 
        System.out.println("Post deleted. Remaining posts: " + myPosts.size());
    }
}
package forum;

import java.util.ArrayList;

class UserPost {
    int id;
    String text;

    UserPost(int id, String text) {
        this.id = id;
        this.text = text;
    }

    // Sub-Task: Edit own post
    void editPost(String newText) {
        this.text = newText;
        System.out.println("Post " + id + " updated.");
    }
}

public class PostManagement {
    public static void main(String[] args) {
        ArrayList<UserPost> myPosts = new ArrayList<>();
        myPosts.add(new UserPost(1, "Hello everyone!"));
        myPosts.add(new UserPost(2, "Java is hard."));

        // Edit post
        myPosts.get(1).editPost("Java is challenging but fun!");

        // Sub-Task: Delete own post
        myPosts.remove(0); 
        System.out.println("Post deleted. Remaining posts: " + myPosts.size());
    }
}