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
    }

    @Override
    public void wipeFile(File f) {
        deleteFilesBackgroundTask.currentFileName = f.getName();

        // An ordinary file to wipe
        long fileSize = f.length();
        long bytesWiped = 0;

        MainTabActivity.sTheMainActivity.mDeleteLog.add("TEST Wiping "+deleteFilesBackgroundTask.currentFileName+" size "+fileSize);

        while (bytesWiped < fileSize) {

            deleteFilesBackgroundTask.currentFileName = f.getName()+" "+bytesWiped+"/"+fileSize;
            deleteFilesBackgroundTask.progress(bytesWiped);

            try {
                if (testModeSleepTime > 0)
                    Thread.sleep(testModeSleepTime);
            } catch (InterruptedException e) {
                Log.e("app", "Background delete", e);
            }
            bytesWiped += 8192;
            if (deleteFilesBackgroundTask.isCancelled()) {
                MainTabActivity.sTheMainActivity.mDeleteLog.add ("Delete cancelled");
                break;
            }
        }

        deleteFilesBackgroundTask.bytesLeftToWipe -= f.length();
        deleteFilesBackgroundTask.progress(0);

    }
}
