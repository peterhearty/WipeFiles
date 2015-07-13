/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.wipe.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import uk.org.platitudes.wipe.main.MainTabActivity;

/**
 * Text size can be changed and alternate lines highlighted.
 */
public class DeleteLogAdapter extends ArrayAdapter {

    private List theData;
    private boolean[] alternateBackgrounds;

    public DeleteLogAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        theData = objects;
        alternateBackgrounds = new boolean[theData.size()];

        // go through the data row by row, setting the background color
        boolean alternateBackground = false;
        for (int i=0; i<theData.size(); i++) {
            String currentRow = (String)theData.get(i);
            if (currentRow.startsWith("Wiping")) {
                // Start of log messages for a new file - switch background color
                alternateBackground = !alternateBackground;
            }
            alternateBackgrounds[i] = alternateBackground;

        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        TextView tv = (TextView) v;
        tv.setTextSize(MainTabActivity.sTheMainActivity.mLogTextSize);
        if (alternateBackgrounds[position]) {
            tv.setBackgroundColor(Color.LTGRAY);
        } else {
            tv.setBackgroundColor(Color.WHITE);
        }
        return v;
    }
}
