/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.wipe.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.org.platitudes.wipe.file.FileHolder;
import uk.org.platitudes.wipe.main.MainTabActivity;
import uk.org.platitudes.wipefiles.R;

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

    // mData is an ArrayList, one entry for each row displayed.
    // Each row is represented by a single HashMap.
    // Each HashMap has two keys, "file type" and "File".
    // The values for "file type" are one of the strings "dir" or "file".
    // The values for "File" are always a FileHolder object.
    //  R.id.text1 and R.id.text2 are TextView objects defined in row_layout.xml.
    // This data structure is mandated by simpleAdapter.

    public static final String[] from = new String[] {"file_type", "File" };
    public static final int[] to = new int[] { R.id.text1, R.id.text2 };

    /**
     * See SelectFilesFragment for a description of this data structure.
     */
    private ArrayList<HashMap<String, Object>> mData;

    /**
     * The resource ID of the row layout, in this case row_layout.xml.
     */
    private int mResourceId;

    public ModifiedSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);

        mContext = context;
        mData = (ArrayList<HashMap<String, Object>>) data;
        mResourceId = resource;

        // We don't have to save the "from" or "to" parameters as this class defines it
        // and the SelectFiles and DeleteFiles both take it from here.
    }

    private static HashMap<String, Drawable> iconCache = new HashMap<>();

    private void setIcon (ImageView icon, File f) {
        // http://stackoverflow.com/questions/8589645/how-to-determine-mime-type-of-file-in-android
        Uri u = Uri.fromFile(f);
        String uriString = u.toString();
        String extension = MimeTypeMap.getFileExtensionFromUrl(uriString);

        Drawable mimeIcon = iconCache.get(extension);
        if (mimeIcon != null) {
            icon.setImageDrawable(mimeIcon);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(u);

        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
        if (matches.size() == 0) {
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            intent.setType(type);
            matches = pm.queryIntentActivities(intent, 0);
        }

        if (matches.size() > 0) {
            mimeIcon = matches.get(0).loadIcon(pm);
            if (mimeIcon != null) {
                iconCache.put(extension, mimeIcon);
                icon.setImageDrawable(mimeIcon);
                return;
            }
        }

        icon.setImageResource(R.drawable.ic_action_txt_icon);
        mimeIcon = icon.getDrawable();
        iconCache.put(extension, mimeIcon);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result;
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
        String col1 = (String) rowData.get(from[0]);
        FileHolder col2 = (FileHolder) rowData.get(from[1]);
        TextView tv1 = (TextView) result.findViewById(to[0]);
        TextView tv2 = (TextView) result.findViewById(to[1]);
        tv1.setText(col1);
        tv2.setText(col2.toString());

        tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainTabActivity.sTheMainActivity.mTextSize);
        tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainTabActivity.sTheMainActivity.mTextSize);

        // The icon will be set to a folder by the inflater.
        // If a file, or a reused row, then change it.

        ImageView myIcon = (ImageView) result.findViewById(R.id.myIcon1);
        myIcon.setAdjustViewBounds(true);
        myIcon.setMaxHeight(48);

        if (col2.file.isDirectory()) {
            //  /usr/share/icons/oxygen/16x16/places/
//            setIcon(myIcon, col2.file);
//            Drawable d = mContext.getPackageManager().getDefaultActivityIcon();
//            myIcon.setImageDrawable(d);
            myIcon.setImageResource(R.drawable.ic_folder);
        } else {
            setIcon(myIcon, col2.file);
//            myIcon.setImageResource(R.drawable.ic_action_txt_icon);
        }

        return result;
    }
}
