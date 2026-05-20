package inkcanvas.model;

public class Admin {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public boolean authenticate(String user, String pass) {
        return ADMIN_USERNAME.equalsIgnoreCase(user)
                && ADMIN_PASSWORD.equals(pass);
    }

    public String getUsername() { return ADMIN_USERNAME; }
}

