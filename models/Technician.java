package models;

public class Technician {
    private int ID;
    private String name;
    private String email;
    private String password;

    public Technician(int ID, String name, String email, String password) {
        this.ID = ID;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public boolean login(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

    public int getID()          { return ID; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }

    @Override
    public String toString() { return name + " (" + email + ")"; }
}