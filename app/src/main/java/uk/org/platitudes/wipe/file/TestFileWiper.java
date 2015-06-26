/**
 * This source code is not owned by anybody. You can can do what you like with it.
 *
 * @author  Peter Hearty
 * @date    April 2015
 */
package uk.org.platitudes.wipe.file;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

import uk.org.platitudes.wipe.main.MainTabActivity;

/**
 * Performs a simulated wipe of a file.
 */
public class TestFileWiper implements FileWiper {

    /**
     * The thread that uses this helper class to perform a test delete.
     */
    private DeleteFilesBackgroundTask deleteFilesBackgroundTask;

    /**
     * Used when testing the app to simulate time passing while files are pretended to be deleted.
     */
    private int testModeSleepTime;

    public TestFileWiper (DeleteFilesBackgroundTask dfbt) {
        deleteFilesBackgroundTask = dfbt;

        // Set up the test mode delay.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainTabActivity.sTheMainActivity);
        String testModeSleepTimeString = sharedPref.getString("test_mode_sleep_time_key", "10");
        testModeSleepTime = Integer.valueOf(testModeSleepTimeString);

        // Always force a minumum sleep time
        if (testModeSleepTime < 1)
            testModeSleepTime = 1;
    }

    @Override
    public void wipeFile(File f) {
        deleteFilesBackgroundTask.currentFileName = f.getName();

        // An ordinary file to wipe
        ProgressCounter counter = new ProgressCounter(f.length());
        counter.setParentCounter(deleteFilesBackgroundTask.progressCounter);

        deleteFilesBackgroundTask.addLogMessage("TEST Wiping " + deleteFilesBackgroundTask.currentFileName + " size " + f.length());

        while (!counter.isFinished()) {

            deleteFilesBackgroundTask.currentFileName = f.getName()+" "+counter.getCurrentValue()+"/"+counter.getMaxValue();

            try {
                Thread.sleep(testModeSleepTime);
            } catch (InterruptedException e) {
                Log.e("app", "Background delete", e);
            }
            counter.add(8192);
            deleteFilesBackgroundTask.progress(counter.getProgressPercent());
            if (deleteFilesBackgroundTask.isCancelled()) {
                deleteFilesBackgroundTask.addLogMessage ("Delete cancelled");
                break;
            }
        }

        counter.finish();
        deleteFilesBackgroundTask.progress(counter.getProgressPercent());
        if (!deleteFilesBackgroundTask.isCancelled()){
            deleteFilesBackgroundTask.addLogMessage("TEST wipe complete " + deleteFilesBackgroundTask.currentFileName);
        }

    }
}
