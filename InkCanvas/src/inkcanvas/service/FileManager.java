package inkcanvas.service;
/*It is responsible for:

saving all application data into files
loading data back when application starts*/
import inkcanvas.ds.MyList;
import inkcanvas.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;


public class FileManager {

    private static final String DATA_DIR;
    private static final DateTimeFormatter DT  = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String P = "|";

    static {
        DATA_DIR = System.getProperty("user.home")
                + File.separator + "InkCanvasData" + File.separator;
        new File(DATA_DIR).mkdirs();
        System.out.println("InkCanvas data folder: " + DATA_DIR);
    }

    // ── Save all ──────────────────────────────────────────────────
    public static void saveAll(DataStore ds) {
        saveGenres(ds);
        saveUsers(ds);
        saveWorks(ds);
        saveRequests(ds);
        saveReportLogs(ds);
        saveCompetition(ds);
    }

    // ── Load all ──────────────────────────────────────────────────
    public static void loadAll(DataStore ds) {
        loadGenres(ds);
        loadUsers(ds);
        loadWorks(ds);
        loadRequests(ds);
        loadReportLogs(ds);
        loadCompetition(ds);
    }

    // ════════════════════════════════════════════════════════════════
    // GENRES  —  one genre per line
    // ════════════════════════════════════════════════════════════════
    public static void saveGenres(DataStore ds) {
        try (PrintWriter pw = writer("genres.txt")) {
            MyList<String> g = ds.getGenres();
            for (int i = 0; i < g.size(); i++) pw.println(g.get(i));
        } catch (IOException e) { err("saveGenres", e); }
    }

    public static void loadGenres(DataStore ds) {
        try (BufferedReader br = reader("genres.txt")) {
            if (br == null) return;
            String line;
            while ((line = br.readLine()) != null) {
                String g = line.trim();
                if (!g.isEmpty()) ds.addCustomGenre(g);
            }
        } catch (IOException e) { err("loadGenres", e); }
    }

    // ════════════════════════════════════════════════════════════════
    // USERS
    // userId|username|password|email|bio|location|website|joinDate|
    //        reportCount|blocked|notified|reportedBy(csv)
    // ════════════════════════════════════════════════════════════════
    public static void saveUsers(DataStore ds) {
        try (PrintWriter pw = writer("users.txt")) {
            MyList<User> users = ds.getAllUsers();
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                MyList<String> rb = u.getReportedBy();
                StringBuilder rbSb = new StringBuilder();
                for (int j = 0; j < rb.size(); j++) {
                    if (j > 0) rbSb.append(",");
                    rbSb.append(rb.get(j));
                }
                pw.println(
                    esc(u.getUserId())       + P +
                    esc(u.getUsername())     + P +
                    esc(u.getPassword())     + P +
                    esc(u.getEmail())        + P +
                    enc(u.getBio())          + P +
                    esc(u.getLocation())     + P +
                    esc(u.getWebsite())      + P +
                    u.getJoinDateFormatted() + P +
                    u.getReportCount()       + P +
                    u.isBlocked()            + P +
                    u.isNotified()           + P +
                    rbSb
                );
            }
        } catch (IOException e) { err("saveUsers", e); }
    }

    public static void loadUsers(DataStore ds) {
        try (BufferedReader br = reader("users.txt")) {
            if (br == null) return;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 11) continue;
                try {
                    // FIX: trim every field to remove invisible whitespace
                    String  userId      = p[0].trim();
                    String  username    = p[1].trim();
                    String  password    = p[2].trim();
                    String  email       = p[3].trim();
                    String  bio         = dec(p[4]);
                    String  location    = p[5].trim();
                    String  website     = p[6].trim();
                    int     reportCount = Integer.parseInt(p[8].trim());
                    boolean blocked     = Boolean.parseBoolean(p[9].trim());
                    boolean notified    = Boolean.parseBoolean(p[10].trim());
                    String  rbRaw       = p.length > 11 ? p[11].trim() : "";

                    User u = new User(userId, username, password, email);
                    u.setBio(bio);
                    u.setLocation(location);
                    u.setWebsite(website);
                    u.setBlocked(blocked);
                    u.setNotified(notified);
                    u.setReportCount(reportCount);

                    if (!rbRaw.isEmpty()) {
                        for (String rid : rbRaw.split(",")) {
                            String trimmed = rid.trim();
                            if (!trimmed.isEmpty()) u.getReportedBy().add(trimmed);
                        }
                    }
                    ds.putUser(userId, u);
                } catch (Exception ex) {
                    System.err.println("Skipping user line: " + ex.getMessage());
                }
            }
        } catch (IOException e) { err("loadUsers", e); }
    }

    // ════════════════════════════════════════════════════════════════
    // WORKS — three record types in one file:
    //   WORK|id|authorId|authorName|title|content(b64)|genre|
    //        published|publishedAt|inComp|readCount
    //   RATING|workId|userId|hook|voice|theme|structure|impact
    //   COMMENT|workId|authorId|authorName|content(b64)|time
    // ════════════════════════════════════════════════════════════════
    public static void saveWorks(DataStore ds) {
        try (PrintWriter pw = writer("works.txt")) {
            MyList<Work> works = ds.getAllWorks();
            for (int i = 0; i < works.size(); i++) {
                Work w = works.get(i);
                pw.println(
                    "WORK"                 + P +
                    w.getWorkId()          + P +
                    esc(w.getAuthorId())   + P +
                    esc(w.getAuthorName()) + P +
                    esc(w.getTitle())      + P +
                    enc(w.getContent())    + P +
                    esc(w.getGenre())      + P +
                    w.isPublished()        + P +
                    (w.getPublishedAt() != null
                            ? w.getPublishedAt().format(DT) : "null") + P +
                    w.isInCompetition()    + P +
                    w.getReadCount()
                );
                MyList<Rating> ratings = w.getRatings();
                for (int j = 0; j < ratings.size(); j++) {
                    Rating r = ratings.get(j);
                    pw.println("RATING" + P + w.getWorkId() + P +
                            r.getUserId() + P +
                            r.getHook()   + P + r.getVoice()  + P +
                            r.getTheme()  + P + r.getStructure() + P +
                            r.getImpact());
                }
                MyList<Comment> comments = w.getComments();
                for (int j = 0; j < comments.size(); j++) {
                    Comment c = comments.get(j);
                    pw.println("COMMENT" + P + w.getWorkId()      + P +
                            esc(c.getAuthorId())   + P +
                            esc(c.getAuthorName()) + P +
                            enc(c.getContent())    + P +
                            c.getFormattedTime());
                }
            }
        } catch (IOException e) { err("saveWorks", e); }
    }

    public static void loadWorks(DataStore ds) {
        try (BufferedReader br = reader("works.txt")) {
            if (br == null) return;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 1) continue;
                try {
                    switch (p[0].trim()) {
                        case "WORK": {
                            if (p.length < 11) break;
                            // FIX: trim every field
                            int     id        = Integer.parseInt(p[1].trim());
                            String  authorId  = p[2].trim();
                            String  authorNm  = p[3].trim();
                            String  title     = p[4].trim();
                            String  content   = dec(p[5]);
                            String  genre     = p[6].trim();
                            boolean pub       = Boolean.parseBoolean(p[7].trim());
                            String  pubAtStr  = p[8].trim();
                            boolean inComp    = Boolean.parseBoolean(p[9].trim());
                            int     reads     = Integer.parseInt(p[10].trim());

                            Work w = new Work(authorId, authorNm, title, content, genre);
                            w.setWorkIdOverride(id);
                            if (pub) {
                                w.publish();
                                if (!pubAtStr.equals("null")) {
                                    try {
                                        w.setPublishedAtOverride(
                                                LocalDateTime.parse(pubAtStr, DT));
                                    } catch (Exception ignored) {}
                                }
                            }
                            w.setInCompetition(inComp);
                            for (int r = 0; r < reads; r++) w.incrementReadCount();
                            ds.putWork(id, w);
                            break;
                        }
                        case "RATING": {
                            if (p.length < 8) break;
                            int    wId = Integer.parseInt(p[1].trim());
                            String uid = p[2].trim();
                            double h   = Double.parseDouble(p[3].trim());
                            double v   = Double.parseDouble(p[4].trim());
                            double t   = Double.parseDouble(p[5].trim());
                            double s   = Double.parseDouble(p[6].trim());
                            double im  = Double.parseDouble(p[7].trim());
                            Work w = ds.getWorkById(wId);
                            if (w != null) w.addRating(new Rating(uid, h, v, t, s, im));
                            break;
                        }
                        case "COMMENT": {
                            if (p.length < 6) break;
                            int    wId      = Integer.parseInt(p[1].trim());
                            String authId   = p[2].trim();
                            String authName = p[3].trim();
                            String content  = dec(p[4]);
                            Work w = ds.getWorkById(wId);
                            if (w != null)
                                w.addComment(new Comment(authId, authName, content));
                            break;
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Skipping work line: " + ex.getMessage());
                }
            }
        } catch (IOException e) { err("loadWorks", e); }
    }

    // ════════════════════════════════════════════════════════════════
    // REQUESTS
    // id|requesterId|requesterName|topic|description(b64)|genre|
    //    targetWriter|date
    // ════════════════════════════════════════════════════════════════
    public static void saveRequests(DataStore ds) {
        try (PrintWriter pw = writer("requests.txt")) {
            MyList<WritingRequest> reqs = ds.getAllRequests();
            for (int i = 0; i < reqs.size(); i++) {
                WritingRequest r = reqs.get(i);
                pw.println(
                    r.getRequestId()            + P +
                    esc(r.getRequesterId())      + P +
                    esc(r.getRequesterName())    + P +
                    esc(r.getTopic())            + P +
                    enc(r.getDescription())      + P +
                    esc(r.getTargetGenre())      + P +
                    esc(r.getTargetWriterName()) + P +
                    r.getFormattedDate()
                );
            }
        } catch (IOException e) { err("saveRequests", e); }
    }

    public static void loadRequests(DataStore ds) {
        try (BufferedReader br = reader("requests.txt")) {
            if (br == null) return;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 7) continue;
                try {
                    int    id      = Integer.parseInt(p[0].trim());
                    String rerId   = p[1].trim();
                    String rerName = p[2].trim();
                    String topic   = p[3].trim();
                    String desc    = dec(p[4]);
                    String genre   = p[5].trim();
                    String writer  = p[6].trim();
                    WritingRequest r = new WritingRequest(
                            rerId, rerName, topic, desc, genre, writer);
                    r.setRequestIdOverride(id);
                    ds.putRequest(id, r);
                } catch (Exception ex) {
                    System.err.println("Skipping request line: " + ex.getMessage());
                }
            }
        } catch (IOException e) { err("loadRequests", e); }
    }

    // ════════════════════════════════════════════════════════════════
    // REPORT LOGS
    // type|message(b64)|reason|targetUser|workTitle|reporterName|time
    // ════════════════════════════════════════════════════════════════
    public static void saveReportLogs(DataStore ds) {
        try (PrintWriter pw = writer("reportlogs.txt")) {
            MyList<ReportLog> logs = ds.getReportLogs();
            for (int i = 0; i < logs.size(); i++) {
                ReportLog l = logs.get(i);
                pw.println(
                    esc(l.getType())         + P +
                    enc(l.getMessage())      + P +
                    esc(l.getReason())       + P +
                    esc(l.getTargetUser())   + P +
                    esc(l.getWorkTitle())    + P +
                    esc(l.getReporterName()) + P +
                    l.getFormattedTime()
                );
            }
        } catch (IOException e) { err("saveReportLogs", e); }
    }

    public static void loadReportLogs(DataStore ds) {
        try (BufferedReader br = reader("reportlogs.txt")) {
            if (br == null) return;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 2) continue;
                try {
                    String type     = p[0].trim();
                    String message  = dec(p[1]);
                    String reason   = p.length > 2 ? p[2].trim() : "";
                    String target   = p.length > 3 ? p[3].trim() : "";
                    String work     = p.length > 4 ? p[4].trim() : "";
                    String reporter = p.length > 5 ? p[5].trim() : "System";
                    ds.getReportLogs().add(
                            new ReportLog(type, message, reason, target, work, reporter));
                } catch (Exception ex) {
                    System.err.println("Skipping log line: " + ex.getMessage());
                }
            }
        } catch (IOException e) { err("loadReportLogs", e); }
    }

    // ════════════════════════════════════════════════════════════════
    // COMPETITION
    // month|theme|endDate(yyyy-MM-dd)|entryId1,entryId2,...
    // ════════════════════════════════════════════════════════════════
    public static void saveCompetition(DataStore ds) {
        try (PrintWriter pw = writer("competition.txt")) {
            Competition c = ds.getCompetition();
            StringBuilder sb = new StringBuilder();
            MyList<Integer> ids = c.getEntryWorkIds();
            for (int i = 0; i < ids.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(ids.get(i));
            }
            pw.println(esc(c.getMonth()) + P + esc(c.getTheme()) + P
                    + c.getEndDate().format(DAY) + P + sb);
        } catch (IOException e) { err("saveCompetition", e); }
    }

    public static void loadCompetition(DataStore ds) {
        try (BufferedReader br = reader("competition.txt")) {
            if (br == null) return;
            String line = br.readLine();
            if (line == null || line.trim().isEmpty()) return;
            String[] p = line.split("\\|", -1);
            if (p.length < 3) return;
            String    month   = p[0].trim();
            String    theme   = p[1].trim();
            LocalDate endDate = LocalDate.parse(p[2].trim(), DAY);
            Competition comp  = new Competition(month, theme, endDate);
            if (p.length > 3 && !p[3].trim().isEmpty()) {
                for (String id : p[3].split(",")) {
                    String trimmed = id.trim();
                    if (!trimmed.isEmpty()) comp.addEntry(Integer.parseInt(trimmed));
                }
            }
            ds.setCompetition(comp);
        } catch (IOException e) { err("loadCompetition", e); }
    }

    // ════════════════════════════════════════════════════════════════
    // I/O helpers
    // ════════════════════════════════════════════════════════════════
    private static PrintWriter writer(String filename) throws IOException {
        return new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(DATA_DIR + filename),
                StandardCharsets.UTF_8));
    }

    private static BufferedReader reader(String filename) {
        File f = new File(DATA_DIR + filename);
        if (!f.exists()) return null;
        try {
            return new BufferedReader(new InputStreamReader(
                    new FileInputStream(f), StandardCharsets.UTF_8));
        } catch (IOException e) { return null; }
    }

    /** Escape pipe and newline characters in plain strings */
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("|", "<<P>>").replace("\n", "<<N>>").replace("\r", "");
    }

    /** Base64-encode strings that may contain pipes or newlines */
    private static String enc(String s) {
        if (s == null) return "";
        return Base64.getEncoder().encodeToString(
                s.getBytes(StandardCharsets.UTF_8));
    }

    /** Base64-decode */
    private static String dec(String s) {
        if (s == null || s.isEmpty()) return "";
        try {
            return new String(Base64.getDecoder().decode(s),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s.replace("<<P>>", "|").replace("<<N>>", "\n");
        }
    }

    private static void err(String method, Exception e) {
        System.err.println("[FileManager." + method + "] " + e.getMessage());
    }
}
