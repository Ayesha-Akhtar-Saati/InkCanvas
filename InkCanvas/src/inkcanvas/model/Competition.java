package inkcanvas.model;

import inkcanvas.ds.MyList;
import java.time.LocalDate;

public class Competition {
    private String theme;
    private String month;
    private LocalDate endDate;
    private MyList<Integer> entryWorkIds; // workIds of submitted entries

    public Competition(String month, String theme, LocalDate endDate) {
        this.month        = month;
        this.theme        = theme;
        this.endDate      = endDate;
        this.entryWorkIds = new MyList<>();
    }

    public void addEntry(int workId) {
        if (!entryWorkIds.contains(workId)) entryWorkIds.add(workId);
    }

    public boolean hasEntry(int workId) { return entryWorkIds.contains(workId); }

    public boolean isActive() { return !LocalDate.now().isAfter(endDate); }

    public String   getTheme()        { return theme; }
    public String   getMonth()        { return month; }
    public LocalDate getEndDate()     { return endDate; }
    public MyList<Integer> getEntryWorkIds() { return entryWorkIds; }
}

