package inkcanvas.model;
/*ReportLog is a model class used to store report and moderation details
 *  such as report type, reason, reporter name, reported user, work title, and time.
 *  It helps the admin panel track reports, blocked users, and notifications,
 *  and is usually stored inside MyList for managing multiple logs.*/
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportLog {

    private String        type;       // REPORT, BLOCKED, NOTIFIED, UNBLOCKED
    private String        message;    // human-readable summary
    private String        reason;     // Harassment, Plagiarism, etc.
    private String        targetUser; // username of reported account
    private String        workTitle;  // title of reported work
    private String        reporterName;
    private LocalDateTime time;

    /** Full constructor with all details */
    public ReportLog(String type, String message,
                     String reason, String targetUser,
                     String workTitle, String reporterName) {
        this.type         = type;
        this.message      = message;
        this.reason       = reason == null      ? "" : reason;
        this.targetUser   = targetUser == null  ? "" : targetUser;
        this.workTitle    = workTitle == null   ? "" : workTitle;
        this.reporterName = reporterName == null? "" : reporterName;
        this.time         = LocalDateTime.now();
    }

    /** Simple constructor for system messages (blocked, notified, etc.) */
    public ReportLog(String type, String message) {
        this(type, message, "", "", "", "");
    }

    public String getType()         { return type; }
    public String getMessage()      { return message; }
    public String getReason()       { return reason; }
    public String getTargetUser()   { return targetUser; }
    public String getWorkTitle()    { return workTitle; }
    public String getReporterName() { return reporterName; }

    public String getFormattedTime() {
        return time.format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm"));
    }

    public void setTime(LocalDateTime t) { this.time = t; }

    @Override
    public String toString() {
        return "[" + getFormattedTime() + "] " + type + ": " + message;
    }
}
