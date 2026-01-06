package auth;

public abstract class User {
    protected int userId;
    protected String name;
    protected String email;
    protected String password;
    protected Role role;

    // Constructor
    public User(int userId, String name, String email, String password, Role role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Common methods
    public void login() {
        System.out.println(name + " logged in as " + role);
    }

    public void logout() {
        System.out.println(name + " logged out.");
    }

    // Abstract method - subclasses must implement
    public abstract void displayInfo();
}

