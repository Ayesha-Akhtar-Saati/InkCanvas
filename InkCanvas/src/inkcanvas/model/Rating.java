package inkcanvas.model;

public class Rating {
    private String userId;
    private double hook;
    private double voice;
    private double theme;
    private double structure;
    private double impact;

    public Rating(String userId, double hook, double voice,
                  double theme, double structure, double impact) {
        this.userId    = userId;
        this.hook      = hook;
        this.voice     = voice;
        this.theme     = theme;
        this.structure = structure;
        this.impact    = impact;
    }

    public double getAverage() {
        return (hook + voice + theme + structure + impact) / 5.0;
    }

    public String getUserId()   { return userId; }
    public double getHook()     { return hook; }
    public double getVoice()    { return voice; }
    public double getTheme()    { return theme; }
    public double getStructure(){ return structure; }
    public double getImpact()   { return impact; }

    @Override public String toString() {
        return String.format("avg=%.1f (Hook:%.0f Voice:%.0f Theme:%.0f Structure:%.0f Impact:%.0f)",
                getAverage(), hook, voice, theme, structure, impact);
    }
}
