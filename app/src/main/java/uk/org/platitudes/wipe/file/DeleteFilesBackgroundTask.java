/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.wipe.file;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import uk.org.platitudes.wipe.adapters.ModifiedSimpleAdapter;
import uk.org.platitudes.wipe.main.MainTabActivity;

/**
 * Delete files as a background task. Invoked by the DeleteFilesFragment when the user clicks on the
 * "Wipe files" button and confirms with the LastChanceDialog.
 *
 * Note: according to /Android/Sdk/docs/reference/android/os/AsyncTask.html.

 "AsyncTask is designed to be a helper class around Thread and Handler and does not constitute
 a generic threading framework. AsyncTasks should ideally be used for short operations (a few
 seconds at the most.) If you need to keep threads running for long periods of time, it is
 highly recommended you use the various APIs provided by the java.util.concurrent package such
 as Executor, ThreadPoolExecutor and FutureTask."

 * Despite the above warning, AsyncTask seems to work OK for extended tasks.
 *
 */
public class DeleteFilesBackgroundTask extends AsyncTask<ArrayList<HashMap<String, Object>>, Integer, Void> implements DialogInterface.OnCancelListener {

    /**
     * android.app.ProgressDialog shows how far the file deletion has progressed.
     */
    private ProgressDialog      mProgressDialog;

    /*
     * Used to keep track of overall progress. This has child ProgressCounters
     * to track individual files progress.
     */
    protected ProgressCounter mProgressCounter;

    /**
     * The file currently being wiped is shown in the progress dialog.
     */
    public String               mCurrentFileName;

    /**
     * Used to wipe a file.
     */
    private RealFileWiper       mFileWiper;

    /**
     * TRUE if this is a test delete run.
     */
    private boolean             mTestMode;

    public DeleteFilesBackgroundTask (boolean test) {
        mTestMode = test;
        mFileWiper = new RealFileWiper(this, test);
    }

    /**
     * Mostly sets up the progress dialog.
     */
    protected void onPreExecute () {
        mProgressDialog = new ProgressDialog(MainTabActivity.sTheMainActivity);
        mProgressDialog.setTitle("Wiping files");
        mProgressDialog.setMessage("");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
        mProgressDialog.setOnCancelListener(this);

        mProgressCounter = new ProgressCounter(0);
    }

    /**
     * Called recursively to add the length of each file to the bytes to be wiped.
     *
     * @param f     A file to be wiped or a directory of files to be wiped.
     */
    private void addFileToByteCount (File f) {
        if (isCancelled() || !f.exists())
            return;

        if (f.isFile()) {
            mProgressCounter.addToMax(f.length());
            return;
        }

        // Must be a directory
        File[] files = f.listFiles();
        for (File fileFromDirectory : files) {
            // We're already processing a directory. If we encounter another directory then
            // make sure recursion is allowed.
            if (!directoryRecursionAllowed() && fileFromDirectory.isDirectory()) {
                continue;
            }
            addFileToByteCount(fileFromDirectory);
        }
    }

    /**
     * Goes through all the files in the delete list and adds up their lengths.
     *
     * @param fileList  The list of files to be deleted.
     */
    private void calculateBytesToWipe (ArrayList<HashMap<String, Object>> fileList) {
//        bytesLeftToWipe = 0;
        for (HashMap<String, Object> hashMap : fileList) {
            FileHolder fh = (FileHolder) hashMap.get(ModifiedSimpleAdapter.from[1]);
            File f = fh.file;
            addFileToByteCount (f);
        }
        mFileWiper.updateByteCountWithPassCount(mProgressCounter);
    }

    /**
     * Checks the preferences to see if recursive wipe of directories is allowed.
     */
    private boolean directoryRecursionAllowed () {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainTabActivity.sTheMainActivity);
        Boolean allowRecursion = sharedPref.getBoolean("allow_recursion_key", false);
        boolean result = allowRecursion.booleanValue();
        return result;
    }

    /**
     * Called by the FileWiper to update progress.
     * Executed in background thread.
     */
    void progress (int percent) {
        // The following sends a message to the UI thread and invokes onProgressUpdate (p)
        publishProgress(percent);
    }

    private void wipeFile (File f) {
        if (isCancelled())
            return;

        if (!f.exists()) {
            addLogMessage(f.getName()+" no longer exists");
            return;
        }

        if (f.isFile()) {
            mFileWiper.wipeFile(f);
            return;
        }

        // Must be a directory
        File[] files = f.listFiles();
        addLogMessage ("Entering directory "+f.getAbsolutePath());
        for (File fileFromDirectory : files) {

            // We're already processing a directory. If we encounter another directory then
            // make sure recursion is allowed.
            if (!directoryRecursionAllowed() && fileFromDirectory.isDirectory()) {
                mCurrentFileName = fileFromDirectory.getName();
                addLogMessage("Tree wipe disabled, skipping '"+ mCurrentFileName + "'");
                continue;
            }

            wipeFile(fileFromDirectory);
            if (isCancelled()) {
                break;
            }
        }
        addLogMessage("Leaving directory " + f.getName());

        // Now wipe the empty directory
        if (!isCancelled())
            if (f.listFiles().length > 0) {
                // Test for non-empty dir. This could be slow because it relies on listFiles.
                addLogMessage ("Skipping dir delete - not empty.");
            } else {
                mFileWiper.wipeFile(f);
            }
    }

    public void addLogMessage (String s) {
        MainTabActivity.sTheMainActivity.mDeleteLog.add(s);
    }

    /**
     * Executed in background thread.
     */
    @Override
    @SafeVarargs
    protected final Void doInBackground(ArrayList<HashMap<String, Object>>... params) {
        MainTabActivity.sTheMainActivity.mDeleteLog.clear();
        ArrayList<HashMap<String, Object>> theData = params[0];
        calculateBytesToWipe(theData);

        // Take a copy of the data to traverse in the for loop.
        // The real data gets modified as files are deleted.
        ArrayList<HashMap<String, Object>> datacopy = (ArrayList<HashMap<String, Object>>) theData.clone();

        for (HashMap<String, Object> hashMap : datacopy) {
            FileHolder fh = (FileHolder) hashMap.get(ModifiedSimpleAdapter.from[1]);
            File f = fh.file;
            wipeFile (f);
            if (!mTestMode && !mFileWiper.errorOccurred) {
                theData.remove(hashMap);
            }
            if (isCancelled()) {
                addLogMessage("File delete cancelled");
                break;
            }
        }
        // Any return value is passed to the onPostExecute method.
        return null;
    }

    /**
     * Runs on the UI thread after cancel(boolean) is invoked and doInBackground(Object[]) has finished.
     * The default implementation simply invokes onCancelled() and ignores the result. If you
     * write your own implementation, do not call super.onCancelled(result).
     */
    protected void onCancelled (Void result) {
        mProgressDialog.dismiss();
        MainTabActivity.sTheMainActivity.onWipeCompletion();
    }

    /**
     * Executed on the UI thread. Invoked via a msg from the background thread
     * when "publisProgress" is called.
     */
    protected void onProgressUpdate(Integer... progress) {
        // Up until this point the meaning of "progress" is entirely application defined.
        // ProgressDialog seems to interpret it as a percentage. It's docs says the
        // range is 0..10000 but that number doesn't appear anywhere in the code.
        mProgressDialog.setMessage(mCurrentFileName);
        mProgressDialog.setProgress(progress[0]);
    }

    /**
     * Runs in the UI thread after doInBackground completes. The parameter is the return
     * value of doInBackground.
     */
    protected void onPostExecute(Void result) {
        mProgressDialog.dismiss();
        MainTabActivity.sTheMainActivity.onWipeCompletion();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cancel(true);
    }
}
