package uk.org.platitudes.wipe.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.org.platitudes.wipe.file.FileHolder;
import uk.org.platitudes.wipe.main.MainTabActivity;
import uk.org.platitudes.petespagerexamples.R;

/**
 * Created by pete on 09/04/15.
 */
public class ModifiedSimpleAdapter extends SimpleAdapter {

    private Context mContext;
    private ArrayList<HashMap<String, Object>> mData;
    private int mResourceId;
    private String[] mFrom;



    public ModifiedSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);

        mContext = context;
        mData = (ArrayList<HashMap<String, Object>>) data;
        mResourceId = resource;
        mFrom = from;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // The main point of overriding this method is to be able to set the row icon
        // differently for files and folders.
        View result = null;
        if (convertView != null) {
            // ListView recycles views. We have to undo any modifications
            // that ListView doesn't know about.
            result = convertView;
        } else {
            // We have to inflate the row from the xml
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            result = inflater.inflate(mResourceId, parent, false);
        }

        HashMap<String, Object> rowData = mData.get(position);
        String col1 = (String) rowData.get(mFrom[0]);
        FileHolder col2 = (FileHolder) rowData.get(mFrom[1]);
        TextView tv1 = (TextView) result.findViewById(R.id.text1);
        TextView tv2 = (TextView) result.findViewById(R.id.text2);
        tv1.setText(col1);
        tv2.setText(col2.toString());

        tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainTabActivity.sTheMainActivity.mTextSize);
        tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainTabActivity.sTheMainActivity.mTextSize);

        // The icon will be set to a folder by the inflater.
        // If a file, or a reused row, then change it.

        ImageView myIcon = (ImageView) result.findViewById(R.id.myIcon1);
        if (col2.file.isDirectory()) {
            myIcon.setImageResource(R.drawable.folder_blue);
        } else {
            myIcon.setImageResource(R.drawable.document_multiple);
        }

        return result;
    }
}
