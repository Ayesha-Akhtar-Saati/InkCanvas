package inkcanvas.service;

import inkcanvas.ds.MyList;
import inkcanvas.ds.MyMap;
import inkcanvas.model.Comment;
import inkcanvas.model.Competition;
import inkcanvas.model.Rating;
import inkcanvas.model.ReportLog;
import inkcanvas.model.User;
import inkcanvas.model.Work;
import inkcanvas.model.WritingRequest;

import java.time.LocalDate;


public class DataStore {

    // ── Storage ───────────────────────────────────────────────────
    private MyMap<String,  User>           users;//key userId value user object
    private MyMap<Integer, Work>           works; //key workid value value wok obj
    private MyMap<Integer, WritingRequest> requests;//key request id value request obj
    private MyList<String>                 genres;
    private MyList<ReportLog>              reportLogs;
    private Competition                    competition;
    private User                           currentUser;

    // ── Singleton ─────────────────────────────────────────────────
    private static DataStore instance;

    public static DataStore get() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    private DataStore() {
        users      = new MyMap<>();
        works      = new MyMap<>();
        requests   = new MyMap<>();
        genres     = new MyList<>();
        reportLogs = new MyList<>();

        // Neutral placeholder — overwritten immediately by FileManager.loadAll()
        // if competition.txt exists on disk. Never use a theme name here.
        competition = new Competition("", "", LocalDate.now().plusDays(30));

        // Load saved data from disk (overwrites placeholder above if file exists)
        FileManager.loadAll(this);

        // First run only — no users found on disk → seed demo data
        if (users.isEmpty()) {
            seedGenres();
            seedData();
            FileManager.saveAll(this);
        }
    }

    // ── Genres ────────────────────────────────────────────────────
    private void seedGenres() {
        genres.add("Fantasy");
        genres.add("Horror");
        genres.add("Romance");
        genres.add("Sci-Fi");
        genres.add("Poetry");
    }

    public void addCustomGenre(String name) {
        if (!genres.contains(name)) {
            genres.add(name);
            FileManager.saveGenres(this);
        }
    }

    public MyList<String> getGenres() { return genres; }

    // ── Seed demo data (first run only) ───────────────────────────
    private void seedData() {

        // Users
        User aisha  = new User("u1", "Aisha Hassan", "pass123", "aisha@ink.com");
        aisha.setBio("I write at the intersection of horror and quiet introspection.");
        aisha.setLocation("Karachi, PK");
        aisha.setWebsite("inkcanvas.io/aisha");

        User leila  = new User("u2", "Leila Osei",  "pass123", "leila@ink.com");
        leila.setBio("Fantasy world-builder and map-maker.");
        leila.setLocation("Accra, Ghana");

        User tomas  = new User("u3", "Tomas Ruiz",  "pass123", "tomas@ink.com");
        tomas.setBio("Science-fiction, quiet apocalypses.");
        tomas.setLocation("Madrid, Spain");

        User nadia  = new User("u4", "Nadia Bloom",  "pass123", "nadia@ink.com");
        nadia.setBio("Poet. Beekeeper. Occasional insomniac.");

        User marcus = new User("u5", "Marcus Vile",  "pass123", "marcus@ink.com");
        for (int i = 1; i <= 10; i++) marcus.receiveReport("r" + i);

        User zara   = new User("u6", "Zara K.",      "pass123", "zara@ink.com");
        for (int i = 1; i <= 7;  i++) zara.receiveReport("r" + i);

        users.put("u1", aisha);
        users.put("u2", leila);
        users.put("u3", tomas);
        users.put("u4", nadia);
        users.put("u5", marcus);
        users.put("u6", zara);

        // Works
        Work w1 = new Work("u2", "Leila Osei",
                "The Cartographer of Lost Skies",
                "She drew maps of places that didn't exist yet — or so she thought, "
                + "until the first traveler returned from one of her invented lands.\n\n"
                + "'You went there,' she said.\n\n"
                + "'The mists are colder than you drew them,' he said. "
                + "'The bridges are made of regret — compressed into a form that holds weight.'",
                "Fantasy");
        w1.publish();
        w1.addRating(new Rating("u3", 5, 5, 4, 5, 5));
        w1.addRating(new Rating("u4", 4, 5, 5, 4, 5));
        w1.addComment(new Comment("u3", "Tomas Ruiz",  "The ending absolutely wrecked me!"));
        w1.addComment(new Comment("u4", "Nadia Bloom", "I want a whole novel in this world."));
        w1.incrementReadCount(); w1.incrementReadCount(); w1.incrementReadCount();

        Work w2 = new Work("u3", "Tomas Ruiz",
                "Last Signal",
                "The last radio operator on Earth was still transmitting. "
                + "Nobody told her when everyone else had left.\n\n"
                + "She had her tea, her crossword puzzle, and the low hum of equipment "
                + "that felt more like company than machinery.\n\n"
                + "She went back inside and kept transmitting.",
                "Sci-Fi");
        w2.publish();
        w2.addRating(new Rating("u1", 4, 5, 4, 5, 4));
        w2.addRating(new Rating("u2", 5, 4, 4, 4, 5));
        w2.addComment(new Comment("u4", "Nadia Bloom", "Haunting in the best way."));
        w2.incrementReadCount(); w2.incrementReadCount();

        Work w3 = new Work("u1", "Aisha Hassan",
                "What the Mirror Kept",
                "My grandmother warned me never to look in the mirror in a dark room.\n\n"
                + "Then I stood in the bathroom at 2am, the lights off, and looked.\n\n"
                + "My reflection looked back. Then it blinked. I hadn't.\n\n"
                + "The thing in the mirror wasn't a monster. "
                + "It was the version of me that didn't pretend.",
                "Horror");
        w3.publish();
        w3.addRating(new Rating("u2", 5, 5, 5, 5, 5));
        w3.addRating(new Rating("u3", 5, 5, 4, 5, 5));
        w3.addComment(new Comment("u2", "Leila Osei",  "That last line is everything."));
        w3.addComment(new Comment("u3", "Tomas Ruiz",  "A perfect short horror piece."));
        for (int i = 0; i < 4; i++) w3.incrementReadCount();

        Work w4 = new Work("u4", "Nadia Bloom",
                "In the Language of Bees",
                "Tell me what the bees know\nabout the geometry of longing —\n"
                + "each cell a room\nyou return to\nalready knowing\nit will be empty.\n\n"
                + "And still you fill it.\nAnd still you seal it.\n"
                + "And still the sweetness\naccumulates in the dark.",
                "Poetry");
        w4.publish();
        w4.addRating(new Rating("u1", 5, 5, 4, 5, 4));
        w4.addComment(new Comment("u2", "Leila Osei", "Stunning imagery."));
        w4.incrementReadCount(); w4.incrementReadCount();

        Work w5 = new Work("u1", "Aisha Hassan",
                "Echoes of Tomorrow",
                "She remembered things that hadn't happened yet — not visions, "
                + "more like a word on the tip of your tongue, "
                + "except the word was an entire future.\n\n"
                + "When the day finally came, she stood at the edge of it "
                + "like a shore she had swum to from very far away.\n\n"
                + "The present, for her, was always a kind of memory.",
                "Sci-Fi");
        w5.publish();
        w5.setInCompetition(true);
        w5.addRating(new Rating("u2", 5, 5, 5, 5, 5));
        w5.addRating(new Rating("u3", 5, 4, 5, 5, 5));
        w5.addComment(new Comment("u2", "Leila Osei", "Deserving every star. Ethereal."));
        w5.incrementReadCount(); w5.incrementReadCount();

        Work w6 = new Work("u1", "Aisha Hassan",
                "The Glass Forest",
                "Draft — a forest made of crystalline trees that shatter "
                + "if you speak above a whisper...",
                "Fantasy");
        // w6 is a draft — not published

        works.put(w1.getWorkId(), w1);
        works.put(w2.getWorkId(), w2);
        works.put(w3.getWorkId(), w3);
        works.put(w4.getWorkId(), w4);
        works.put(w5.getWorkId(), w5);
        works.put(w6.getWorkId(), w6);

        // Competition — only set on first run; FileManager.loadAll() takes over after that
        competition = new Competition("April 2026", "Echoes of Tomorrow",
                LocalDate.of(2026, 4, 30));
        competition.addEntry(w5.getWorkId());

        // Writing requests
        WritingRequest r1 = new WritingRequest("u4", "Nadia Bloom",
                "A love story set in a library after closing time",
                "Two people who keep returning to the same section, leaving notes in the margins.",
                "Romance", "");
        WritingRequest r2 = new WritingRequest("u3", "Tomas Ruiz",
                "Horror story told from the monster's perspective",
                "Subvert the genre. Make me feel sympathetic to something frightening.",
                "Horror", "Aisha Hassan");
        WritingRequest r3 = new WritingRequest("u2", "Leila Osei",
                "A poem about waiting for rain during a drought",
                "Capture the agony of anticipation — hope, relief, change.",
                "Poetry", "");
        WritingRequest r4 = new WritingRequest("u1", "Aisha Hassan",
                "Sci-fi story: first contact but boring bureaucracy",
                "Aliens arrive and the first thing that happens is paperwork.",
                "Sci-Fi", "");

        requests.put(r1.getRequestId(), r1);
        requests.put(r2.getRequestId(), r2);
        requests.put(r3.getRequestId(), r3);
        requests.put(r4.getRequestId(), r4);

        // Seed report logs
        reportLogs.add(new ReportLog("BLOCKED",
                "Marcus Vile's account blocked after reaching 10 reports."));
        reportLogs.add(new ReportLog("NOTIFIED",
                "Zara K. notified — 5 reports threshold reached."));
        reportLogs.add(new ReportLog("REPORT",
                "New report filed against Zara K. (now 7 total)."));

        currentUser = aisha;
    }

    // ════════════════════════════════════════════════════════════════
    // USER OPERATIONS
    // ════════════════════════════════════════════════════════════════

    public User         getCurrentUser()       { return currentUser; }
    public void         setCurrentUser(User u) { currentUser = u; }
    public User         getUserById(String id) { return users.get(id); }
    public MyList<User> getAllUsers()           { return users.values(); }

    /** Called by FileManager to restore a user from disk. */
    public void putUser(String id, User u) { users.put(id, u); }

    public boolean registerUser(String username, String password, String email) {
        MyList<User> all = users.values();
        for (int i = 0; i < all.size(); i++)
            if (all.get(i).getUsername().equalsIgnoreCase(username)) return false;

        String newId = "u" + System.currentTimeMillis();
        User u = new User(newId, username, password, email);
        users.put(newId, u);
        FileManager.saveUsers(this);
        return true;
    }

    public User login(String username, String password) {
        MyList<User> all = users.values();
        for (int i = 0; i < all.size(); i++) {
            User u = all.get(i);
            if (u.getUsername().equalsIgnoreCase(username)
                    && u.getPassword().equals(password)) return u;
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════
    // WORK OPERATIONS
    // ════════════════════════════════════════════════════════════════

    public MyList<Work> getAllWorks()      { return works.values(); }
    public Work         getWorkById(int id){ return works.get(id); }

    /** Called by FileManager to restore a work from disk. */
    public void putWork(int id, Work w) { works.put(id, w); }

    /** Persist a new or updated work immediately. */
    public void saveWork(Work w) {
        works.put(w.getWorkId(), w);
        FileManager.saveWorks(this);
    }

    public MyList<Work> getPublishedWorks() {
        MyList<Work> result = new MyList<>();
        MyList<Work> all    = works.values();
        for (int i = 0; i < all.size(); i++)
            if (all.get(i).isPublished()) result.add(all.get(i));
        return result;
    }

    public MyList<Work> getMyWorks(String userId) {
        MyList<Work> result = new MyList<>();
        MyList<Work> all    = works.values();
        for (int i = 0; i < all.size(); i++)
            if (all.get(i).getAuthorId().equals(userId)) result.add(all.get(i));
        return result;
    }

    public MyList<Work> getMyWorksByGenre(String userId, String genre) {
        MyList<Work> result = new MyList<>();
        MyList<Work> mine   = getMyWorks(userId);
        for (int i = 0; i < mine.size(); i++) {
            Work w = mine.get(i);
            if (genre.equals("All") || w.getGenre().equals(genre)) result.add(w);
        }
        return result;
    }

    public MyList<Work> searchWorks(String query, String searchType) {
        if (query == null || query.isBlank()) return getPublishedWorks();
        String       q      = query.toLowerCase();
        MyList<Work> result = new MyList<>();
        MyList<Work> pub    = getPublishedWorks();
        for (int i = 0; i < pub.size(); i++) {
            Work    w     = pub.get(i);
            boolean match;
            switch (searchType) {
                case "Author":
                    match = w.getAuthorName().toLowerCase().contains(q);
                    break;
                case "Title":
                    match = w.getTitle().toLowerCase().contains(q);
                    break;
                default:
                    match = w.getTitle().toLowerCase().contains(q)
                         || w.getAuthorName().toLowerCase().contains(q)
                         || w.getContent().toLowerCase().contains(q)
                         || w.getGenre().toLowerCase().contains(q);
                    break;
            }
            if (match) result.add(w);
        }
        return result;
    }

    /** Persist works + users to disk (call after ratings / comments change). */
    public void persistWork() {
        FileManager.saveWorks(this);
        FileManager.saveUsers(this);
    }

    /** Remove a work by id — admin only. Persists immediately. */
    public void removeWork(int workId) {
        works.remove(workId);
        // Also remove from competition entries if present
        competition.getEntryWorkIds().removeItem(workId);
        FileManager.saveWorks(this);
        FileManager.saveCompetition(this);
    }

    // ════════════════════════════════════════════════════════════════
    // REQUEST OPERATIONS
    // ════════════════════════════════════════════════════════════════

    public MyList<WritingRequest> getAllRequests() { return requests.values(); }

    /** Called by FileManager to restore a request from disk. */
    public void putRequest(int id, WritingRequest r) { requests.put(id, r); }

    public void addRequest(WritingRequest r) {
        requests.put(r.getRequestId(), r);
        FileManager.saveRequests(this);
    }

    // ════════════════════════════════════════════════════════════════
    // COMPETITION
    // ════════════════════════════════════════════════════════════════

    public Competition getCompetition() { return competition; }

    /**
     * Replace the active competition and persist immediately.
     * Called by AdminPanel when the theme / end-date is updated,
     * and by FileManager.loadCompetition() during startup.
     */
    public void setCompetition(Competition c) {
        competition = c;
        FileManager.saveCompetition(this);
    }

    /** Persist only the competition file (called after adding a competition entry). */
    public void persistCompetition() {
        FileManager.saveCompetition(this);
    }

    /**
     * Returns competition entries sorted descending by average rating.
     * Tie-break: earlier submission wins.
     * Uses a plain Work[] array + bubble sort — no java.util.
     */
    public MyList<Work> getSortedCompetitionEntries() {
        MyList<Integer> ids = competition.getEntryWorkIds();
        Work[] arr = new Work[ids.size()];
        for (int i = 0; i < ids.size(); i++) arr[i] = works.get(ids.get(i));

        // Bubble sort descending by rating
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j] == null || arr[j + 1] == null) continue;
                double r1   = arr[j].getAverageRating();
                double r2   = arr[j + 1].getAverageRating();
                boolean swap = r1 < r2;
                if (r1 == r2
                        && arr[j].getPublishedAt()     != null
                        && arr[j + 1].getPublishedAt() != null) {
                    swap = arr[j].getPublishedAt().isAfter(arr[j + 1].getPublishedAt());
                }
                if (swap) {
                    Work tmp  = arr[j];
                    arr[j]    = arr[j + 1];
                    arr[j + 1] = tmp;
                }
            }
        }

        MyList<Work> sorted = new MyList<>();
        for (int i = 0; i < arr.length; i++)
            if (arr[i] != null) sorted.add(arr[i]);
        return sorted;
    }

    // ════════════════════════════════════════════════════════════════
    // REPORT OPERATIONS
    // ════════════════════════════════════════════════════════════════

    public MyList<ReportLog> getReportLogs() { return reportLogs; }

    public void addReportLog(ReportLog log) {
        reportLogs.addFirst(log);
        FileManager.saveReportLogs(this);
    }

    public MyList<User> getReportedUsers() {
        MyList<User> result = new MyList<>();
        MyList<User> all    = users.values();
        for (int i = 0; i < all.size(); i++)
            if (all.get(i).getReportCount() > 0) result.add(all.get(i));
        return result;
    }

    /**
     * Report a user with full details.
     *
     * Returns:
     *   -1 = already reported by this user (no change)
     *    0 = report recorded, no threshold reached
     *    1 = 5-report threshold reached → user notified
     *    2 = 10-report threshold reached → user blocked
     *
     * Saves users.txt and reportlogs.txt immediately on every successful report.
     */
    public int reportUser(String targetUserId, String reporterId,
                          String reason, String workTitle, String reporterName) {
        User target = users.get(targetUserId);
        if (target == null) return 0;

        // receiveReport() returns -1 if this reporter already reported this user.
        // We rely solely on that return value — no separate pre-check needed.
        int result = target.receiveReport(reporterId);

        if (result == -1) {
            // Already reported — nothing changed, nothing to save
            return -1;
        }

        // New report recorded — log it and persist immediately
        addReportLog(new ReportLog("REPORT",
                "Work \"" + workTitle + "\" reported for: " + reason,
                reason, target.getUsername(), workTitle, reporterName));

        if (result == 2) {
            addReportLog(new ReportLog("BLOCKED",
                    target.getUsername() + "'s account blocked (10 reports reached)."));
        } else if (result == 1) {
            addReportLog(new ReportLog("NOTIFIED",
                    target.getUsername() + " notified (5-report threshold reached)."));
        }

        // Persist both files immediately so admin sees the update without restart
        FileManager.saveUsers(this);
        FileManager.saveReportLogs(this);
        return result;
    }

    /** Backward-compatible overload used by internal calls. */
    public int reportUser(String targetUserId, String reporterId) {
        return reportUser(targetUserId, reporterId, "Unspecified", "Unknown", "Unknown");
    }

    /** Save a user profile change and persist immediately. */
    public void saveUserProfile(User u) {
        users.put(u.getUserId(), u);
        FileManager.saveUsers(this);
    }
}
