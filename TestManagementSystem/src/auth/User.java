package auth;

public abstract class User {
    protected int userId; 
    protected String name;
    protected int age; // Moved up to match the constructor order
    protected String gender;
    protected String birthDate;
    protected String email;
    protected String password;
    protected Role role;

    public User() {}

    // Ensure the parameter order matches what Student sends in super()
    public User(int userId, String name, int age, String gender, String birthDate, String email, String password, Role role) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.birthDate = birthDate;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Role getRole() { return role; }
    public int getUserId() { return userId; }
    public String getName() { return name; }

    public abstract void displayInfo();
}