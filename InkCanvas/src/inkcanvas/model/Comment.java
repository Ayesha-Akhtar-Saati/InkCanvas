package inkcanvas.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comment {
    private static int counter = 1;

    private int    commentId;
    private String authorId;
    private String authorName;
    private String content;
    private LocalDateTime timestamp;

    public Comment(String authorId, String authorName, String content) {
        this.commentId  = counter++;
        this.authorId   = authorId;
        this.authorName = authorName;
        this.content    = content;
        this.timestamp  = LocalDateTime.now();
    }

    public int    getCommentId()  { return commentId; }
    public String getAuthorId()   { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getContent()    { return content; }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm"));
    }

    @Override public String toString() {
        return authorName + "  [" + getFormattedTime() + "]\n" + content;
    }
}
