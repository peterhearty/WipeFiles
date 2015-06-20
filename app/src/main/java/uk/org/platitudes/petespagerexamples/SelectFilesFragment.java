package uk.org.platitudes.petespagerexamples;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pete on 30/04/15.
 */
public class SelectFilesFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";
    public static final String ARG_SELECTION_COUNT = "Selection count";

    private View mRootView;
    public ListView mListView;
    public SimpleAdapter simpleAdapter;
    private File mCurDir;
    public ControlButtonHandler mControlButtonHandler;

    // theData is an ArrayList, one entry for each row displayed.
    // Each row is represented by a single HashMap.
    // Each HashMap has two keys, "file type" and "File".
    // The values for "file type" are one of the strings "dir" or "file".
    // The values for "File" are always a FileHolder object.

    private String[] from = new String[] {"file_type", "File" };
    private int[] to = new int[] { R.id.text1, R.id.text2 };
    private ArrayList<HashMap<String, Object>> theData;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SelectFilesFragment newInstance(int sectionNumber) {
        SelectFilesFragment fragment = new SelectFilesFragment();

        // Used to use the args to pass in the section number.
        // Just use the attribute mSectionNumber now.
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putInt(ARG_SELECTION_COUNT, 0);
        fragment.setArguments(args);

        return fragment;
    }

    // Is the no-args constructor compulsory?
    // That would explain why a factory is used to create instances.
    public SelectFilesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // data from previous incarnation
            String dirPath = savedInstanceState.getString("directory");
            mCurDir = new File(dirPath);
            setAppTitle();
        } else {
            mCurDir = Environment.getExternalStorageDirectory();
        }
        // container is the ViewPager
        // rootView is the RelativeLayout or whatever at the root of this Fragment
        mRootView = inflater.inflate(R.layout.tab_without_button, container, false);

        mControlButtonHandler = new ControlButtonHandler(mRootView);

        mListView = (ListView) mRootView.findViewById(R.id.listView);
        populateData(mCurDir);

        // NOTE - BELOW IS WHERE ROW_LAYOUT GETS USED
        simpleAdapter = new ModifiedSimpleAdapter(mRootView.getContext(), theData, R.layout.row_layout, from, to);
        mListView.setAdapter(simpleAdapter);
        mListView.setSelected(false);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        setText();
        return mRootView;
    }

    public void resetAdapter () {
        // This relies on a side affect of setting the adapter to clear the view cache in ListView.
        // simply doing simpleAdapter.notifyDataSetChanged() or mListView.invalidateViews() will
        // retain cached views if possible. If the font size gets reduced then we get smaller text
        // but stil inside large views.
        mListView.setAdapter(simpleAdapter);
    }

    private void addRowForFile (FileHolder f) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(from[0], "File");
        if (f.file.isDirectory()) map.put(from[0], "Dir");
        map.put(from[1], f);
        theData.add(map);
    }

    public void setAppTitle () {
        String title = "";
        if (mCurDir != null) {
            try {
                title = mCurDir.getCanonicalPath().toString();
            } catch (IOException ioe) {
                title = ioe.toString();
            }
        }
        MainTabActivity.sTheMainActivity.mActionBar.setWindowTitle(title);
    }

    private void populateData (File dirFile) {
        if (!dirFile.isDirectory()) return;

        if (theData == null) theData = new ArrayList<HashMap<String, Object>>();

        File[] files = dirFile.listFiles();
        if (files == null) return;                              // Check for no permission to read directory

        mCurDir = dirFile;
        setAppTitle();

        FileHolder.sortByName(files);

        theData.clear();

        // Add parent directory
        File pf = dirFile.getParentFile();
        if (pf != null) {
            FileHolder header = new FileHolder(pf);
            header.nameOverride = "..";
            addRowForFile(header);
        }

        // Add files in directory
        for(int i = 0; i < files.length; i++) {
            File f = files[i];
            FileHolder fh = new FileHolder(f);
            fh.nameOverride = f.getName();
            addRowForFile(fh);
        }
    }

    public void setText () {
        if (mRootView == null) return; // This can get called before the view has been constructed
        TextView tv = (TextView) mRootView.findViewById(R.id.section_label);
        tv.setText("Long click file to add to delete list");

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putString("directory", mCurDir.getCanonicalPath().toString());
        } catch (IOException ioe) {
            Log.e("app", "saving instance dir", ioe);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileHolder fh = (FileHolder) theData.get(position).get(from[1]);
        File f = fh.file;
        populateData(f);
        mListView.invalidateViews();
        simpleAdapter.notifyDataSetChanged();
    }

    private void displayMessage (String s) {
        Context context = MainTabActivity.sTheMainActivity.getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, s, duration);
        toast.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        FileHolder fh = (FileHolder) theData.get(position).get(from[1]);
        File f = fh.file;

        if (f.isDirectory()) {
            // Attempt to add a directory - make sure this is allowed.
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainTabActivity.sTheMainActivity);
            Boolean allowDirectories = sharedPref.getBoolean("allow_directories_key", false);
            if (!allowDirectories) {
                displayMessage ("Directories not permitted - check settings");
                return true;
            }
        }

        DeleteFilesFragment delFrag = (DeleteFilesFragment) MainTabActivity.sTheMainActivity.mSectionsPagerAdapter.getItem(1);
        boolean added = delFrag.addRowForFile(f);

        if (!added) {
            displayMessage (f.getName() + " already there");
        } else {
            displayMessage (f.getName() + " added to delete list");
        }

        // Return true to indicate that the click was consumed.
        // Without this, onItemClick, above, would get called.
        return true;
    }

}
