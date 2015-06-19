package uk.org.platitudes.petespagerexamples;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

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
        tv.setTextSize(MainTabActivity.sTheMainActivity.logTextSize);
        if ((position & 1) == 1) {
            tv.setBackgroundColor(Color.LTGRAY);
        } else {
            tv.setBackgroundColor(Color.WHITE);
        }
        return v;
    }
}
