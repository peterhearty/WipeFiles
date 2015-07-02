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

    public DeleteLogAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        TextView tv = (TextView) v;
        tv.setTextSize(MainTabActivity.sTheMainActivity.mLogTextSize);
        if ((position & 1) == 1) {
            tv.setBackgroundColor(Color.LTGRAY);
        } else {
            tv.setBackgroundColor(Color.WHITE);
        }
        return v;
    }
}
