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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import uk.org.platitudes.wipe.main.MainTabActivity;

/**
 * Performs a real wipe of a file.
 */
public class RealFileWiper {

    /**
     * The thread that uses this helper class to perform a test delete.
     */
    private DeleteFilesBackgroundTask deleteFilesBackgroundTask;

    /**
     * Used when testing the app to simulate time passing while files are pretended to be deleted.
     */
    private int testModeSleepTime;

    private int numberPasses;
    private boolean performZeroWipe;
    private boolean performRandomWipe;
    private boolean testMode;
    private int writeBlockSize;
    private byte[] zeroBlock;
    private byte[] randomBlock;

    private int getIntPreference (String s, int min, int max, int defaultValue) {
        int result = defaultValue;
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainTabActivity.sTheMainActivity);
            String defaultString = Integer.toString(defaultValue);
            String stringVal = sharedPref.getString(s, defaultString);
            result = Integer.parseInt(stringVal);
            if (result < min || result > max)
                result = defaultValue;
        } catch (Exception e) {
            Log.e("Wipe", "Getting preference", e);
        }
        return result;
    }

    public RealFileWiper (DeleteFilesBackgroundTask dfbt, boolean test) {
        deleteFilesBackgroundTask = dfbt;
        testMode = test;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainTabActivity.sTheMainActivity);
        numberPasses = getIntPreference("number_passes", 1, 32, 1);
        performZeroWipe = sharedPref.getBoolean("zero_wipe", true);
        writeBlockSize = getIntPreference("block_size", 512, 65536, 8192);

        String testModeSleepTimeString = sharedPref.getString("test_mode_sleep_time_key", "10");
        testModeSleepTime = getIntPreference("test_mode_sleep_time_key", 1, 65536, 10);

        zeroBlock = new byte[writeBlockSize];
        randomBlock = new byte[writeBlockSize];
    }

    private void renamefile (File f) {
        String currentName = f.getName();

    }

    private void wipePass (String prefixString, RandomAccessFile raf, ProgressCounter counter, boolean randomPass) throws IOException {
        raf.seek(0);

        // The random numbers don't have to be cryptographically strong.
        // Plain old vanilla Random is good enough.
        // This won't get used on a zero fill pass.
        Random randomNumberGenretaor = new Random();

        // By default, assume the random block of data will be used.
        byte[] writeBlock = randomBlock;
        if (!randomPass)
            writeBlock = zeroBlock;

        while (!counter.isFinished()) {
            deleteFilesBackgroundTask.currentFileName = prefixString+" "+counter.getCurrentValue()+"/"+counter.getMaxValue();
            long writeSize = writeBlockSize;
            long bytesLeft = counter.getMaxValue() - counter.getCurrentValue();
            if (bytesLeft < writeBlockSize)
                // limit the size of the final write
                writeSize = bytesLeft;
            if (randomPass) {
                // wipe with random data
                randomNumberGenretaor.nextBytes(randomBlock);
            }
            // According to javadoc "output operations write bytes starting at the file pointer
            // and advance the file pointer past the bytes written". so no need to move the file
            // pointer.
            if (testMode) {
                try {
                    Thread.sleep(testModeSleepTime);
                } catch (Exception e) {
                    Log.e("app", "Background delete", e);
                }

            } else {
                // perform a real wipe
                raf.write(writeBlock, 0, (int) writeSize);
            }
            counter.add(writeSize);
            deleteFilesBackgroundTask.progress(counter.getProgressPercent());

            if (deleteFilesBackgroundTask.isCancelled()) {
                deleteFilesBackgroundTask.addLogMessage ("Delete cancelled");
                break;
            }
        }
        deleteFilesBackgroundTask.addLogMessage(prefixString+"complete");

        counter.finish();
    }

    public void wipeFile(File f) {
        deleteFilesBackgroundTask.currentFileName = f.getName();

        deleteFilesBackgroundTask.addLogMessage("Wiping " + deleteFilesBackgroundTask.currentFileName + " size " + f.length());

        for (int i=0; i < numberPasses; i++) {
            ProgressCounter singlePassCounter = new ProgressCounter(f.length());
            singlePassCounter.setParentCounter(deleteFilesBackgroundTask.progressCounter);

            String readWriteMode = "rw"; // "rws" does synchronous writes, could make this an option.
            try {
                RandomAccessFile raf = new RandomAccessFile(f, readWriteMode);
                if (performZeroWipe) {
                    // Wipe with zeros first
                    ProgressCounter zeroCounter = singlePassCounter.copy();
                    String progressPrefixString = "PASS "+(i+1)+" ZEROES "+f.getName()+" ";
                    wipePass(progressPrefixString, raf, zeroCounter, false);
                }
                String progressPrefixString = "PASS "+(i+1)+" RANDOMS "+f.getName()+" ";
                wipePass(progressPrefixString, raf, singlePassCounter, true);
                raf.close();
            } catch (Exception e) {
                MainTabActivity.sTheMainActivity.mDeleteLog.add ("Exception: "+e.toString());
                return;
            }
        }

        deleteFilesBackgroundTask.addLogMessage("Wipe complete: " + f.getName());

        //TODO - rename file
    }

    public void updateByteCountWithPassCount(ProgressCounter counter) {
        long counterMaxValue = counter.getMaxValue();
        counterMaxValue *= numberPasses;
        if (performZeroWipe)
            counterMaxValue *= 2;
        counter.setMaxValue(counterMaxValue);
    }
}
