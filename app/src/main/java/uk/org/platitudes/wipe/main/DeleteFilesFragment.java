package uk.org.platitudes.wipe.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import uk.org.platitudes.petespagerexamples.R;
import uk.org.platitudes.wipe.file.DeleteFilesBackgroundTask;
import uk.org.platitudes.wipe.adapters.ModifiedSimpleAdapter;
import uk.org.platitudes.wipe.file.FileHolder;

/**
 * Created by pete on 11/05/15.
 */
public class DeleteFilesFragment extends Fragment implements AdapterView.OnItemLongClickListener, View.OnClickListener {

    private View mRootView;
    public ListView mListView;
    public SimpleAdapter simpleAdapter;

    // theData is an ArrayList, one entry for each row displayed.
    // Each row is represented by a single HashMap.
    // Each HashMap has two keys, "file type" and "File".
    // The values for "file type" are one of the strings "dir" or "file".
    // The values for "File" are always a FileHolder object.

    public static String[] from = new String[] {"file_type", "File" };
    private int[] to = new int[] { R.id.text1, R.id.text2 };
    private ArrayList<HashMap<String, Object>> theData;
    public ControlButtonHandler mControlButtonHandler;

    // Is the no-args constructor compulsory?
    // That would explain why a factory is used to create instances.
    public DeleteFilesFragment() {
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {
        theData = new ArrayList<HashMap<String, Object>>();

        if (savedInstanceState != null) {
            // data from previous incarnation
            ArrayList<String> filesToDelete = savedInstanceState.getStringArrayList("filesToDelete");
            for (String s : filesToDelete) {
                File f = new File (s);
                addRowForFile (f);
            }
        }

        // container is the ViewPager
        // rootView is the RelativeLayout or whatever at the root of this Fragment
        mRootView = inflater.inflate(R.layout.fragment_petes_tab_example, container, false);
        mControlButtonHandler = new ControlButtonHandler(mRootView);

        Button button = (Button) mRootView.findViewById(R.id.main_button);
        button.setText("Wipe files");
        button.setOnClickListener(this);

        mListView = (ListView) mRootView.findViewById(R.id.listView);

        // NOTE - BELOW IS WHERE ROW_LAYOUT GETS USED
        simpleAdapter = new ModifiedSimpleAdapter(mRootView.getContext(), theData, R.layout.row_layout, from, to);
        mListView.setAdapter(simpleAdapter);
        mListView.setSelected(false);
        mListView.setOnItemLongClickListener(this);

        TextView tv = (TextView) mRootView.findViewById(R.id.section_label);
        tv.setText("Long click to remove file from list");
        return mRootView;
    }

    public void resetAdapter () {
        // This relies on a side affect of setting the adapter to clear the view cache in ListView.
        // simply doing simpleAdapter.notifyDataSetChanged() or mListView.invalidateViews() will
        // retain cached views if possible. If the font size gets reduced then we get smaller text
        // but still inside large views.
        mListView.setAdapter(simpleAdapter);
    }

    public boolean addRowForFile (File f) {
        // First check to see if file already in list
        for (HashMap<String, Object> hm : theData) {
            FileHolder fh = (FileHolder) hm.get(from[1]);
            try {
                String existingPath = fh.file.getCanonicalPath();
                String newPath = f.getCanonicalPath();
                if (newPath.equals(existingPath))
                    return false;
            } catch (Exception e) {
                Log.e("app", "adding file to delete", e);
            }
        }

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(from[0], "File");
        if (f.isDirectory()) map.put(from[0], "Dir");
        FileHolder fh = new FileHolder(f);
        map.put(from[1], fh);
        theData.add(map);
        if (simpleAdapter != null)
            // Can be null at startup as files are added when app is recreated
            // Keep it is this way to prevent multiple calls to notifyDataSetInvalidated
            simpleAdapter.notifyDataSetInvalidated();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int numberOfFiles = theData.size();
        ArrayList<String> filesToDelete = new ArrayList<String>(numberOfFiles);
        try {
            for (HashMap<String, Object> hm : theData) {
                FileHolder fh = (FileHolder) hm.get(from[1]);
                File f = fh.file;
                String filename = f.getCanonicalPath();
                filesToDelete.add(filename);
            }
            outState.putStringArrayList("filesToDelete", filesToDelete);
        } catch (IOException ioe) {
            Log.e("app", "saving instance dir", ioe);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        theData.remove(position);
        simpleAdapter.notifyDataSetInvalidated();

        return true;
    }

    private void showToast (String msg) {
        Context context = MainTabActivity.sTheMainActivity.getApplicationContext();
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onClick(View v) {
        // Gets called when button is clicked.
        if (theData.isEmpty()) {
            showToast("No files to delete");
            return;
        }
        LastChanceDialog lastChanceDialog = new LastChanceDialog();
        FragmentManager fm = getFragmentManager();
        lastChanceDialog.show(fm, "Last chance dialog");
    }

    /**
     * Called by the LastChanceDialog when the user selects "Yes".
     */
    public void startFileDeletion () {
        showToast("OK - delete files");
        DeleteFilesBackgroundTask dfbt = new DeleteFilesBackgroundTask();
        dfbt.execute(theData);
    }
}
