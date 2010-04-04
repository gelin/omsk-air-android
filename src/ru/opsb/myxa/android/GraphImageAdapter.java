package ru.opsb.myxa.android;

import static ru.opsb.myxa.android.Graphs.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *  Adapter to represent graph image into a gallery.
 */
public class GraphImageAdapter extends BaseAdapter {

    final Context context;
    
    public GraphImageAdapter(Context context) {
        this.context = context;
    }
    
    public int getCount() {
        return GRAPHS.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        //View result = View.inflate(context, R.layout.gallery_item, parent);
        GraphInfo graphInfo = GRAPHS[position];
        
        //ImageView image = (ImageView)result.findViewById(R.id.graph_image);
        ImageView image = new ImageView(context);
        Bitmap bitmap = getBitmap(graphInfo);
        image.setImageBitmap(bitmap);
        
        //TextView text = (TextView)result.findViewById(R.id.graph_title);
        //text.setText(graphInfo.title);
        
        //return result;
        return image;
    }
    
    Bitmap getBitmap(GraphInfo graphInfo) {
        Bitmap bitmap = BitmapFactory.decodeFile(graphInfo.path.toString());
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.empty_graph);
        }
        return bitmap;
    }

}
