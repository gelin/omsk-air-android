package ru.opsb.myxa.android;

import java.io.File;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class Graphs {

    /** URI of content provider for images */
    static final Uri IMAGES_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    //TODO: this requires SD card existence, need to detect it
    /** Name of the Bucket */
    static final String BUCKET_NAME = "omsk_temp";
    /** Bucket directory */
    static final File BUCKET_DIR = new File(
            Environment.getExternalStorageDirectory(), BUCKET_NAME);
    
    static class GraphInfo {
        public long id = -1;    //-1 means not existed
        public String url;
        public File path;
        public String name;
        public int title;
        public int view;
        public String mimeType = "image/png";
        public long lastModified;
        public GraphInfo(String url, String name, int title, int view) {
            this.url = url;
            this.name = name;
            this.title = title;
            this.view = view;
            this.path = new File(BUCKET_DIR, name);
        }
        public GraphInfo copy() {
            GraphInfo result = new GraphInfo(
                    this.url, this.name, this.title, this.view);
            result.path = this.path;
            result.mimeType = this.mimeType;
            result.lastModified = this.lastModified;
            return result;
        }
    }
    
    /** Daily graph */
    static final GraphInfo DAILY = new GraphInfo(
            "http://myxa.opsb.ru/pics/daily.png", "daily.png", 
            R.string.daily_graph, R.id.daily_graph);
    /** Weekly graph */
    static final GraphInfo WEEKLY = new GraphInfo(
            "http://myxa.opsb.ru/pics/weekly.png", "weekly.png", 
            R.string.weekly_graph, R.id.weekly_graph);
    /** Monthly graph */
    static final GraphInfo MONTHLY = new GraphInfo(
            "http://myxa.opsb.ru/pics/monthly.png", "monthly.png", 
            R.string.monthly_graph, R.id.monthly_graph);
    /** Yearly graph */
    static final GraphInfo YEARLY = new GraphInfo(
            "http://myxa.opsb.ru/pics/annual.png", "annual.png",
            R.string.annual_graph, R.id.annual_graph);
    
    /** All graphs */
    static final GraphInfo[] GRAPHS = {DAILY, WEEKLY, MONTHLY, YEARLY};

}
