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
 * This source code is not owned by anybody. You can can do what you like with it.
 *
 * @author  Peter Hearty
 * @date    April 2015
 */
package uk.org.platitudes.petespagerexamples;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DeleteFilesBackgroundTask extends AsyncTask<ArrayList<HashMap<String, Object>>, Integer, Void> implements DialogInterface.OnCancelListener {

    /**
     * android.app.ProgressDialog shows how far the file deletion has progressed.
     */
    private ProgressDialog mProgressDialog;

    /*
     * Hopefully next 2 are self explanatory.
     */
    private long bytesLeftToWipe;
    private long maxBytesToWipe;

    /**
     * The file currently being wiped is shown in the progress dialog.
     */
    private String currentFileName;

    /**
     * Used when testing the app to simulate time passing while files are pretended to be deleted.
     */
    private int testModeSleepTime;

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

        // Set up the test mode delay.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainTabActivity.sTheMainActivity);
        String testModeSleepTimeString = sharedPref.getString("test_mode_sleep_time_key", "10");
        testModeSleepTime = Integer.valueOf(testModeSleepTimeString);
    }

    /**
     * Called recursively to add the length of each file to the bytes to be wiped.
     *
     * @param f     A file to be wiped or a directory of files to be wiped.
     */
    private void addFileToByteCount (File f) {
        if (f.isFile()) {
            bytesLeftToWipe += f.length();
            return;
        }

        // Must be a directory
        File[] files = f.listFiles();
        for (File fileFromDirectory : files) {
            addFileToByteCount(fileFromDirectory);
        }
    }

    private void calculateBytesToWipe (ArrayList<HashMap<String, Object>> fileList) {
        bytesLeftToWipe = 0;
        for (HashMap<String, Object> hashMap : fileList) {
            FileHolder fh = (FileHolder) hashMap.get(DeleteFilesFragment.from[1]);
            File f = fh.file;
            addFileToByteCount (f);
        }
    }

    private void wipeFile (File f) {
        if (isCancelled())
            return;

        if (f.isFile()) {
            currentFileName = f.getName();

            // An ordinary file to wipe
            long fileSize = f.length();
            long bytesWiped = 0;

            MainTabActivity.sTheMainActivity.mDeleteLog.add("Wiping "+currentFileName+" size "+fileSize);

            while (bytesWiped < fileSize) {

                currentFileName = f.getName()+" "+bytesWiped+"/"+fileSize;
                int progress = (int) ((maxBytesToWipe-bytesLeftToWipe+bytesWiped)*100/maxBytesToWipe);
                publishProgress(progress);

                try {
                    Thread.sleep(testModeSleepTime);
                } catch (InterruptedException e) {
                    Log.e("app", "Background delete", e);
                }
                bytesWiped += 1024;
                if (isCancelled()) {
                    MainTabActivity.sTheMainActivity.mDeleteLog.add ("Delete cancelled");
                    break;
                }
            }

            bytesLeftToWipe -= f.length();

            int progress = (int) ((maxBytesToWipe-bytesLeftToWipe)*100/maxBytesToWipe);
            publishProgress(progress);
            return;
        }

        // Must be a directory
        File[] files = f.listFiles();
        MainTabActivity.sTheMainActivity.mDeleteLog.add ("Entering directory "+f.getName());
        for (File fileFromDirectory : files) {

            // We're already processing a directory. If we encounter another directory then
            // make sure recursion is allowed.
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainTabActivity.sTheMainActivity);
            Boolean allowRecursion = sharedPref.getBoolean("allow_recursion_key", false);
            if (!allowRecursion && fileFromDirectory.isDirectory()) {
                currentFileName = fileFromDirectory.getName();
                MainTabActivity.sTheMainActivity.mDeleteLog.add("Tree wipe disabled, skipping '"+currentFileName+"'");
                continue;
            }

            wipeFile(fileFromDirectory);
            if (isCancelled()) {
                break;
            }
        }
        MainTabActivity.sTheMainActivity.mDeleteLog.add ("Leaving directory "+f.getName());
    }

    @Override
    @SafeVarargs
    protected final Void doInBackground(ArrayList<HashMap<String, Object>>... params) {
        ArrayList<HashMap<String, Object>> theData = params[0];
        calculateBytesToWipe (theData);
        maxBytesToWipe = bytesLeftToWipe;
        for (HashMap<String, Object> hashMap : theData) {
            FileHolder fh = (FileHolder) hashMap.get(DeleteFilesFragment.from[1]);
            File f = fh.file;
            wipeFile (f);
            if (isCancelled()) {
                Log.i("bgrnd", "file delete cancelled");
                break;
            }
        }
        return null;
    }

    /**
     * Runs on the UI thread after cancel(boolean) is invoked and doInBackground(Object[]) has finished.
     * The default implementation simply invokes onCancelled() and ignores the result. If you
     * write your own implementation, do not call super.onCancelled(result).
     */
    protected void onCancelled (Void result) {
        mProgressDialog.hide();
    }

    protected void onProgressUpdate(Integer... progress) {
        mProgressDialog.setMessage(currentFileName);
        mProgressDialog.setProgress(progress[0]);
    }

    protected void onPostExecute(Void result) {
        mProgressDialog.hide();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cancel(true);
    }
}
