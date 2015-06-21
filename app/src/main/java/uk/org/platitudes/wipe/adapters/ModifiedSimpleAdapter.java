/**
 * This source code is not owned by anybody. You can can do what you like with it.
 *
 * @author  Peter Hearty
 * @date    April 2015
 */
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
 * Modifies the SimpleAdapter used to display a row of data in a ListView.
 * Allows us to modify the icon in response to a file type and to alter the
 * text size on the basis of user preferences.
 *
 * @Layout  row_layout.xml Passed as a resource ID to the constructor.
 */
public class ModifiedSimpleAdapter extends SimpleAdapter {

    /**
     * Passed from
     */
    private Context mContext;

    /**
     * See SelectFilesFragment for a description of this data structure.
     */
    private ArrayList<HashMap<String, Object>> mData;

    /**
     * The resource ID of the row layout, in this case row_layout.xml.
     */
    private int mResourceId;

    /**
     * The keys used to access each HashMap row of ArrayList<HashMap<String, Object>> mData.
     */
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
