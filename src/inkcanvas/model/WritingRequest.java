package inkcanvas.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WritingRequest {

    private static int counter = 1;

    private int           requestId;
    private String        requesterId;
    private String        requesterName;
    private String        topic;
    private String        description;
    private String        targetGenre;
    private String        targetWriterName;
    private LocalDateTime submittedAt;

    public WritingRequest(String requesterId, String requesterName,
                          String topic, String description,
                          String targetGenre, String targetWriterName) {
        this.requestId        = counter++;
        this.requesterId      = requesterId;
        this.requesterName    = requesterName;
        this.topic            = topic;
        this.description      = description;
        this.targetGenre      = targetGenre == null      ? "" : targetGenre;
        this.targetWriterName = targetWriterName == null ? "" : targetWriterName;
        this.submittedAt      = LocalDateTime.now();
    }

    // ── Getters ───────────────────────────────────────────────────
    public int    getRequestId()        { return requestId; }
    public String getRequesterId()      { return requesterId; }
    public String getRequesterName()    { return requesterName; }
    public String getTopic()            { return topic; }
    public String getDescription()      { return description; }
    public String getTargetGenre()      { return targetGenre; }
    public String getTargetWriterName() { return targetWriterName; }

    public String getFormattedDate() {
        return submittedAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm"));
    }

    /** Used by FileManager to restore exact requestId from file */
    public void setRequestIdOverride(int id) {
        this.requestId = id;
        if (id >= counter) counter = id + 1;
    }

    @Override
    public String toString() {
        return "[" + getFormattedDate() + "] " + requesterName + ": " + topic;
    }
}

