package inkcanvas.model;

import inkcanvas.ds.MyList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Work {

    private static int counter = 1;

    private int              workId;
    private String           authorId;
    private String           authorName;
    private String           title;
    private String           content;
    private String           genre;
    private boolean          published;
    private boolean          inCompetition;
    private LocalDateTime    createdAt;
    private LocalDateTime    publishedAt;
    private MyList<Comment>  comments;
    private MyList<Rating>   ratings;
    private int              readCount;

    public Work(String authorId, String authorName,
                String title, String content, String genre) {
        this.workId        = counter++;
        this.authorId      = authorId;
        this.authorName    = authorName;
        this.title         = title;
        this.content       = content;
        this.genre         = genre;
        this.published     = false;
        this.inCompetition = false;
        this.createdAt     = LocalDateTime.now();
        this.comments      = new MyList<>();
        this.ratings       = new MyList<>();
        this.readCount     = 0;
    }

    public void publish() {
        this.published  = true;
        this.publishedAt = LocalDateTime.now();
    }

    public void addComment(Comment c)  { comments.add(c); }

    public boolean addRating(Rating r) {
        for (int i = 0; i < ratings.size(); i++)
            if (ratings.get(i).getUserId().equals(r.getUserId())) return false;
        ratings.add(r);
        return true;
    }

    public double getAverageRating() {
        if (ratings.isEmpty()) return 0.0;
        double sum = 0;
        for (int i = 0; i < ratings.size(); i++) sum += ratings.get(i).getAverage();
        return Math.round((sum / ratings.size()) * 10.0) / 10.0;
    }

    public boolean hasRatedBy(String userId) {
        for (int i = 0; i < ratings.size(); i++)
            if (ratings.get(i).getUserId().equals(userId)) return true;
        return false;
    }

    public void incrementReadCount() { readCount++; }

    // ── Getters ───────────────────────────────────────────────────
    public int              getWorkId()       { return workId; }
    public String           getAuthorId()     { return authorId; }
    public String           getAuthorName()   { return authorName; }
    public String           getTitle()        { return title; }
    public String           getContent()      { return content; }
    public String           getGenre()        { return genre; }
    public boolean          isPublished()     { return published; }
    public boolean          isInCompetition() { return inCompetition; }
    public LocalDateTime    getCreatedAt()    { return createdAt; }
    public LocalDateTime    getPublishedAt()  { return publishedAt; }
    public MyList<Comment>  getComments()     { return comments; }
    public MyList<Rating>   getRatings()      { return ratings; }
    public int              getReadCount()    { return readCount; }

    public String getPublishedDateFormatted() {
        if (publishedAt == null) return "\u2014";
        return publishedAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    public int getWordCount() {
        if (content == null || content.isBlank()) return 0;
        return content.trim().split("\\s+").length;
    }

    // ── Setters ───────────────────────────────────────────────────
    public void setTitle(String v)          { title         = v; }
    public void setContent(String v)        { content       = v; }
    public void setGenre(String v)          { genre         = v; }
    public void setInCompetition(boolean v) { inCompetition = v; }

    /** Used by FileManager to restore exact workId from file */
    public void setWorkIdOverride(int id)  {
        this.workId = id;
        if (id >= counter) counter = id + 1;
    }

    /** Used by FileManager to restore exact publishedAt from file */
    public void setPublishedAtOverride(LocalDateTime dt) {
        this.publishedAt = dt;
    }

    @Override
    public String toString() {
        return "[" + workId + "] \"" + title + "\" — " + authorName
                + " (" + (published ? "Published " + getPublishedDateFormatted() : "Draft") + ")";
    }
}

