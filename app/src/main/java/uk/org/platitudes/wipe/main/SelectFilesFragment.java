/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.wipe.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import uk.org.platitudes.wipe.adapters.ModifiedSimpleAdapter;
import uk.org.platitudes.wipe.file.FileHolder;
import uk.org.platitudes.wipefiles.R;

/**
 * Allows a user to select files to add to the deletetion list.
 * Holds the contents of a single directory at a time.
 *
 * Layout tab_without_button
 */
public class SelectFilesFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, ControlButtonHandler.GetControlButtonHandler {

    /**
     * The tope level view of the inflated layout.
     */
    private View                    mRootView;

    /**
     * The view that holds the list of files.
     */
    public  ListView                mListView;

    /**
     * A MofifiedSimpleAdapter.
     */
    public  SimpleAdapter           simpleAdapter;

    /**
     * The directory currently being displayed in the ListView.
     */
    private File                    mCurDir;

    /**
     * Handles the small button that shows/hides the ActionBar.
     */
    private  ControlButtonHandler    mControlButtonHandler;

    /**
     * Holds a listing of the current directory.
     * See ModifiedSimpleAdapter for a description of this.
     */
    private ArrayList<HashMap<String, Object>> theData;

    // Is the no-args constructor compulsory?
    // That would explain why a factory is used to create instances.
    public SelectFilesFragment() {}

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

        mListView = (ListView) mRootView.findViewById(R.id.listOfFiles);
        populateData(mCurDir); // Side effect = theData gets initialised

        if (theData.size()==0) {
            // This happens if the external storage directory doesn't exist or cannot be read
            mCurDir = Environment.getRootDirectory();
            populateData(mCurDir); // Side effect = theData gets initialised
        }

        // NOTE - BELOW IS WHERE ROW_LAYOUT GETS USED
        simpleAdapter = new ModifiedSimpleAdapter(
                mRootView.getContext(),
                theData,
                R.layout.row_layout,
                ModifiedSimpleAdapter.from,
                ModifiedSimpleAdapter.to);
        mListView.setAdapter(simpleAdapter);
        mListView.setSelected(false);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        setText();
        return mRootView;
    }

    public void resetAdapter () {
        // This relies on a side affect of setting the adapter to clear the view cache in ListView.
        // simply doing mSimpleAdapter.notifyDataSetChanged() or mListView.invalidateViews() will
        // retain cached views if possible. If the font size gets reduced then we get smaller text
        // but stil inside large views.
        mListView.setAdapter(simpleAdapter);
    }

    private void addRowForFile (FileHolder f) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(ModifiedSimpleAdapter.from[0], "File");
        if (f.file.isDirectory()) map.put(ModifiedSimpleAdapter.from[0], "Dir");
        map.put(ModifiedSimpleAdapter.from[1], f);
        theData.add(map);
    }

    public void setAppTitle () {
        String title = "";
        if (mCurDir != null) {
            try {
                title = mCurDir.getCanonicalPath();
            } catch (IOException ioe) {
                title = ioe.toString();
            }
        }
        MainTabActivity.sTheMainActivity.mActionBar.setWindowTitle(title);
    }

    public void resetListing () {
        if (mCurDir==null)
            mCurDir = Environment.getExternalStorageDirectory();
        populateData(mCurDir); // Side effect = theData gets initialised
    }

    public boolean handleBackKey () {
        boolean result = false;

        File parentDir = mCurDir.getParentFile();
        if (parentDir == null)
            return result;

        if (parentDir.canRead()) {
            populateData(parentDir);
            if (mCurDir ==parentDir) {
                // populate worked
                result = true;
                mListView.invalidateViews();
                simpleAdapter.notifyDataSetChanged();
            }
        }
        return result;
    }

    private void populateData (File dirFile) {
        if (!dirFile.isDirectory()) return;

        if (theData == null) theData = new ArrayList<>();

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
            header.nameOverride = "<Up one directory>";
            addRowForFile(header);
        }

        // Add files in directory
        for (File f : files) {
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
            outState.putString("directory", mCurDir.getCanonicalPath());
        } catch (IOException ioe) {
            MainTabActivity.sTheMainActivity.mDeleteLog.add("Error saving instance dir " + ioe);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        populateData(mCurDir);
        mListView.invalidateViews();
        simpleAdapter.notifyDataSetChanged();
        super.onResume();
    }

    public static void tryToShowFile (Fragment frag, File file) {
        if (file==null) return;
        if (file.isDirectory()) return;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        String uriString = uri.toString();
        String extension = MimeTypeMap.getFileExtensionFromUrl(uriString);
        extension = extension.toLowerCase();
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        sendIntent.setDataAndType(uri, mimeType);

        // Verify that the intent will resolve to an activity
        if (sendIntent.resolveActivity(MainTabActivity.sTheMainActivity.getPackageManager()) != null) {
            frag.startActivity(sendIntent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileHolder fh = (FileHolder) theData.get(position).get(ModifiedSimpleAdapter.from[1]);
        File f = fh.file;
        if (f.isDirectory()) {
            populateData(f);
            mListView.invalidateViews();
            simpleAdapter.notifyDataSetChanged();
        } else {
            tryToShowFile (this, f);
        }
    }

    private void displayMessage (String s) {
        Context context = MainTabActivity.sTheMainActivity.getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, s, duration);
        toast.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        FileHolder fh = (FileHolder) theData.get(position).get(ModifiedSimpleAdapter.from[1]);
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
            // TODO - add a dialog to ask if they want to remove the file
            displayMessage (f.getName() + " already there");
        } else {
            displayMessage (f.getName() + " added to delete list");
        }

        // Return true to indicate that the click was consumed.
        // Without this, onItemClick, above, would get called.
        return true;
    }

    @Override
    public ControlButtonHandler getControlButtonHandler() {
        return mControlButtonHandler;
    }
}
