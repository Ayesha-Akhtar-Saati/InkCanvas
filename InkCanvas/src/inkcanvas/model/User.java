package inkcanvas.model;

import inkcanvas.ds.MyList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class User {
    private String         userId;
    private String         username;
    private String         password;
    private String         email;
    private String         bio;
    private String         location;
    private String         website;
    private LocalDate      joinDate;
    private int            reportCount;
    private boolean        blocked;
    private boolean        notified;
    private MyList<String> reportedBy;

    public User(String userId, String username, String password, String email) {
        this.userId      = userId;
        this.username    = username;
        this.password    = password;
        this.email       = email;
        this.bio         = "";
        this.location    = "";
        this.website     = "";
        this.joinDate    = LocalDate.now();
        this.reportCount = 0;
        this.blocked     = false;
        this.notified    = false;
        this.reportedBy  = new MyList<>();
    }

   
    public int receiveReport(String reporterId) {
        // Return -1 so DataStore.reportUser() and ReadPanel both get a clear signal
        if (reportedBy.contains(reporterId)) return -1;
        reportedBy.add(reporterId);
        reportCount++;
        if (reportCount >= 10 && !blocked)  { blocked  = true; return 2; }
        if (reportCount >= 5  && !notified) { notified = true; return 1; }
        return 0;
    }

    // ── Getters ───────────────────────────────────────────────────
    public String         getUserId()     { return userId; }
    public String         getUsername()   { return username; }
    public String         getPassword()   { return password; }
    public String         getEmail()      { return email; }
    public String         getBio()        { return bio; }
    public String         getLocation()   { return location; }
    public String         getWebsite()    { return website; }
    public int            getReportCount(){ return reportCount; }
    public boolean        isBlocked()     { return blocked; }
    public boolean        isNotified()    { return notified; }
    public MyList<String> getReportedBy() { return reportedBy; }

    public String getJoinDateFormatted() {
        return joinDate.format(DateTimeFormatter.ofPattern("MMM yyyy"));
    }

    // ── Setters ───────────────────────────────────────────────────
    public void setUsername(String v)    { username    = v; }
    public void setPassword(String v)    { password    = v; }
    public void setEmail(String v)       { email       = v; }
    public void setBio(String v)         { bio         = v; }
    public void setLocation(String v)    { location    = v; }
    public void setWebsite(String v)     { website     = v; }
    public void setBlocked(boolean v)    { blocked     = v; }
    public void setNotified(boolean v)   { notified    = v; }
    public void setReportCount(int v)    { reportCount = v; }   // used by FileManager

    @Override
    public String toString() { return username; }//When object printed: display username toString convert object in readable text otherwise it will print memory address
}

