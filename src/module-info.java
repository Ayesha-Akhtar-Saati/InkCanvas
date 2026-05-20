module InkCanvas {

    // ── Java SE modules required by the application ───────────────

    // Swing and AWT (all UI panels, MainWindow, layouts, painting)
    requires java.desktop;

    // java.time (LocalDate, LocalDateTime used in Work, User, Competition)
    requires java.base;

    // Base64 encoding used in FileManager (java.util.Base64)
    // java.base already covers this — listed here for clarity

    // ── Export our packages so all classes can see each other ─────

    // Manual data structures (MyList, MyMap, MyStack)
    exports inkcanvas.ds;

    // Data model classes (User, Work, Comment, Rating, etc.)
    exports inkcanvas.model;

    // Business logic and persistence (DataStore, FileManager)
    exports inkcanvas.service;

    // All Swing UI panels and windows
    exports inkcanvas.ui;
}