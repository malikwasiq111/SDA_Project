package models;

public class Student {
    private int roll;
    private String name;
    private String email;
    private String password;

    public Student(int roll, String name, String email, String password) {
        this.roll = roll;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public boolean login(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

    public int getRoll()           { return roll; }
    public String getName()        { return name; }
    public String getEmail()       { return email; }
    public String getPassword()    { return password; }
    public void setName(String n)  { this.name = n; }
    public void setEmail(String e) { this.email = e; }

    @Override
    public String toString() { return name + " (" + email + ")"; }
}