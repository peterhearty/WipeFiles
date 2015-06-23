/**
 * This source code is not owned by anybody. You can can do what you like with it.
 *
 * @author  Peter Hearty
 * @date    April 2015
 */
package uk.org.platitudes.wipe.file;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.RandomAccessFile;

import uk.org.platitudes.wipe.main.MainTabActivity;

/**
 * Performs a real wipe of a file.
 */
public class RealFileWiper implements  FileWiper {
    /**
     * The thread that uses this helper class to perform a test delete.
     */
    private DeleteFilesBackgroundTask deleteFilesBackgroundTask;

    private int numberPasses;
    private boolean performZeroWipe;
    private boolean performRandomWipe;
    private int writeBlockSize;

    public RealFileWiper (DeleteFilesBackgroundTask dfbt) {
        deleteFilesBackgroundTask = dfbt;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainTabActivity.sTheMainActivity);
        numberPasses = sharedPref.getInt("number_passes", 1);
        performZeroWipe = sharedPref.getBoolean("zero_wipe", false);
        performRandomWipe = sharedPref.getBoolean("random_wipe", true);
        writeBlockSize = sharedPref.getInt("block_size", 8192);

    }

    @Override
    public void wipeFile(File f) {
        deleteFilesBackgroundTask.currentFileName = f.getName();

        // An ordinary file to wipe
        long fileSize = f.length();
        long bytesWiped = 0;

        MainTabActivity.sTheMainActivity.mDeleteLog.add("Wiping "+deleteFilesBackgroundTask.currentFileName+" size "+fileSize);

        for (int i=0; i < numberPasses; i++) {
            String readWriteMode = "rw"; // "rws" does synchronous writes, could make this an option.
            try {
                RandomAccessFile raf = new RandomAccessFile(f, readWriteMode);
            } catch (Exception e) {
                MainTabActivity.sTheMainActivity.mDeleteLog.add ("Exception: "+e.toString());
                return;
            }
        }

        MainTabActivity.sTheMainActivity.mDeleteLog.add("Wipe complete: "+deleteFilesBackgroundTask.currentFileName);
    }
}
