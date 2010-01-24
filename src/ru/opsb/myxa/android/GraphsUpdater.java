package ru.opsb.myxa.android;

/**
 *  Downloads temperature graphs and updates the graph images
 *  in the MediaStore.
 */
public class GraphsUpdater implements Runnable {

    static class GraphInfo {
        public String url;
        public GraphInfo(String url) {
            this.url = url;
        }
    }
    
    /** Daily graph */
    static final GraphInfo DAILY = new GraphInfo(
            "http://myxa.opsb.ru/pics/daily.png");
    /** Weekly graph */
    static final GraphInfo WEEKLY = new GraphInfo(
            "http://myxa.opsb.ru/pics/weekly.png");
    /** Monthly graph */
    static final GraphInfo MONTHLY = new GraphInfo(
            "http://myxa.opsb.ru/pics/monthly.png");
    /** Yearly graph */
    static final GraphInfo YEARLY = new GraphInfo(
            "http://myxa.opsb.ru/pics/annual.png");
    /** All graphs */
    static final GraphInfo[] GRAPHS = {DAILY, WEEKLY, MONTHLY, YEARLY};
    
    /**
     *  Creates the updater.
     */
    public GraphsUpdater() {
        
    }
    
    public void run() {

    }

}
