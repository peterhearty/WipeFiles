/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.wipe.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
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

import uk.org.platitudes.wipefiles.R;
import uk.org.platitudes.wipe.file.DeleteFilesBackgroundTask;
import uk.org.platitudes.wipe.adapters.ModifiedSimpleAdapter;
import uk.org.platitudes.wipe.file.FileHolder;

/**
 * Provides one of the two main tab views, the other being SelectFilesFragment.
 * Both get created by MyFragmentPagerAdapter which in turn gets created and
 * added to the ViewPager ViewGroup by MainTabActivity.
 *
 * Layout:   file_list_with_title_and_button.xml
 */
public class DeleteFilesFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener, ControlButtonHandler.GetControlButtonHandler {

    /**
     * The ListView is defined in file_list_with_title.xml.
     */
    public ListView         mListView;

    /**
     * Actually an instance of ModifiedSimpleAdapter. This allows us to intercept the views being
     * created for the ListView and modify icons and font size.
     */
    public SimpleAdapter    mSimpleAdapter;

    /**
     * This gets clicked to test out what files get wiped.
     */
    private Button          mTestWipeButton;

    /**
     * This gets clicked to perform the file wipe.
     */
    private Button          mRealWipeButton;

    /**
     * See ModifiedSimpleAdapter for a description of this.
     */
    private ArrayList<HashMap<String, Object>> theData;

    /**
     * Handler for the button that hides/shows the ActionBar.
     */
    private ControlButtonHandler mControlButtonHandler;

    // Is the no-args constructor compulsory?
    // That would explain why a factory is used to create instances.
    public DeleteFilesFragment() {}

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {
        theData = new ArrayList<>();

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
        View rootView = inflater.inflate(R.layout.file_list_with_title_and_button, container, false);
        mControlButtonHandler = new ControlButtonHandler(rootView);

        // Listen for clicks on the buttons
        mTestWipeButton = (Button) rootView.findViewById(R.id.test_wipe_button);
        mTestWipeButton.setOnClickListener(this);
        mRealWipeButton = (Button) rootView.findViewById(R.id.real_wipe_button);
        mRealWipeButton.setOnClickListener(this);

        // lostOfFiles is defined in file_list_with_title which is included in
        // file_list_with_title_and_button
        mListView = (ListView) rootView.findViewById(R.id.listOfFiles);

        // NOTE - BELOW IS WHERE ROW_LAYOUT GETS USED
        mSimpleAdapter = new ModifiedSimpleAdapter(
                rootView.getContext(),
                theData,
                R.layout.row_layout,
                ModifiedSimpleAdapter.from,
                ModifiedSimpleAdapter.to);
        mListView.setAdapter(mSimpleAdapter);
        mListView.setSelected(false);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        TextView tv = (TextView) rootView.findViewById(R.id.section_label);
        tv.setText("Long click to remove file from list");
        return rootView;
    }

    public void resetAdapter () {
        // This relies on a side affect of setting the adapter to clear the view cache in ListView.
        // Simply doing mSimpleAdapter.notifyDataSetChanged() or mListView.invalidateViews() will
        // retain cached views if possible. If the font size gets reduced then we get smaller text
        // but still inside large (cached) views.
        mListView.setAdapter(mSimpleAdapter);
    }

    public boolean addRowForFile (File f) {
        // First check to see if file already in list
        for (HashMap<String, Object> hm : theData) {
            FileHolder fh = (FileHolder) hm.get(ModifiedSimpleAdapter.from[1]);
            try {
                String existingPath = fh.file.getCanonicalPath();
                String newPath = f.getCanonicalPath();
                if (newPath.equals(existingPath))
                    return false;
            } catch (Exception e) {
                Log.e("app", "adding file to delete", e);
            }
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put(ModifiedSimpleAdapter.from[0], "File");
        if (f.isDirectory()) map.put(ModifiedSimpleAdapter.from[0], "Dir");
        FileHolder fh = new FileHolder(f);
        map.put(ModifiedSimpleAdapter.from[1], fh);
        theData.add(map);
        if (mSimpleAdapter != null)
            // Can be null at startup as files are added when app is recreated
            // Keep it is this way to prevent multiple calls to notifyDataSetInvalidated
            mSimpleAdapter.notifyDataSetInvalidated();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int numberOfFiles = theData.size();
        ArrayList<String> filesToDelete = new ArrayList<>(numberOfFiles);
        try {
            for (HashMap<String, Object> hm : theData) {
                FileHolder fh = (FileHolder) hm.get(ModifiedSimpleAdapter.from[1]);
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
        mSimpleAdapter.notifyDataSetInvalidated();

        return true;
    }

    //TODO - onItemClick show file is possible

    private void showToast (String msg) {
        Context context = MainTabActivity.sTheMainActivity.getApplicationContext();
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Handles clicks from the "test wipe" and "real wipe" buttons.
     * @param v
     */
    @Override
    public void onClick(View v) {
        // Gets called when button is clicked.
        if (theData.isEmpty()) {
            showToast("No files to delete");
            return;
        }

        Button b = (Button) v;
        if (b == mTestWipeButton) {
            // perform test wipe
            DeleteFilesBackgroundTask dfbt = new DeleteFilesBackgroundTask(true);
            dfbt.execute(theData);
        } else {
            // must be the real wipe - confirm first
            LastChanceDialog lastChanceDialog = new LastChanceDialog();
            FragmentManager fm = getFragmentManager();
            lastChanceDialog.show(fm, "Last chance dialog");
        }
    }

    /**
     * Called by the LastChanceDialog when the user selects "Yes".
     */
    public void startFileDeletion () {
        showToast("OK - delete files");
        DeleteFilesBackgroundTask dfbt = new DeleteFilesBackgroundTask(false);
        dfbt.execute(theData);
    }

    @Override
    public ControlButtonHandler getControlButtonHandler() {
        return mControlButtonHandler;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileHolder fh = (FileHolder) theData.get(position).get(ModifiedSimpleAdapter.from[1]);
        File f = fh.file;
        SelectFilesFragment.tryToShowFile (this, f);

    }
}
