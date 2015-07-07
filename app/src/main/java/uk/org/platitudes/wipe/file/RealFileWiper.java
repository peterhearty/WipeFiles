/**
 * This source code is not owned by anybody. You can can do what you like with it.
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

    private static final String sAllowedFilenameChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private int numberPasses;
    private boolean performZeroWipe;
    private boolean testMode;
    private int writeBlockSize;
    private byte[] zeroBlock;
    private byte[] randomBlock;
    private static final String sReadWriteMode = "rw"; // "rws" does synchronous writes, could make this an option.

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
        performZeroWipe = sharedPref.getBoolean("zero_wipe", false);
        writeBlockSize = getIntPreference("block_size", 512, 65536, 8192);

        String testModeSleepTimeString = sharedPref.getString("test_mode_sleep_time_key", "10");
        testModeSleepTime = getIntPreference("test_mode_sleep_time_key", 1, 65536, 1);

        zeroBlock = new byte[writeBlockSize];
        randomBlock = new byte[writeBlockSize];
    }

    private void log (String s) {
        deleteFilesBackgroundTask.addLogMessage(s);
    }

    private void renamefile (File f) {
        String currentName = f.getName();
        String directory = f.getParent();
        Random r = new Random();

        for (int j=0; j<3; j++) {
            // We try the rename several times in case the target name already exists.

            char[] newName = new char[currentName.length()];
            for (int i=0; i<newName.length; i++) {
                newName[i] = sAllowedFilenameChars.charAt(r.nextInt(sAllowedFilenameChars.length()));
            }

            String newFileName = new String(newName);
            String newPathName = directory+File.separator+newFileName;
            File newFile = new File(newPathName);

            String message = "Renaming "+currentName+" to "+newFileName;
            deleteFilesBackgroundTask.addLogMessage(message);
            if (testMode) {
                log("TEST - skipping rename");
                return;
            } else {
                boolean renameWorked  = f.renameTo(newFile);
                if (renameWorked) {
                    log("Rename succeeded - attempting delete");
                    boolean deleteWorked = newFile.delete();
                    if (deleteWorked) {
                        log("Delete succeeded");
                    }
                    break;
                }
            }
        }
    }

    private void closeRandomAccessFile (RandomAccessFile raf) {
        if (raf == null) return;
        try {
            raf.close();
        } catch (IOException ioe) {
            log("Closing random access file " + ioe);
        }
    }

    private void wipePass (String prefixString, File f, ProgressCounter counter, boolean randomPass) {
        if (testMode) {
            prefixString = "TEST "+prefixString;
        }
        prefixString = "- "+prefixString;

        // The random numbers don't have to be cryptographically strong.
        // Plain old vanilla Random is good enough.
        // This won't get used on a zero fill or a test pass.
        Random randomNumberGenretaor = new Random();

        // By default, assume the random block of data will be used.
        byte[] writeBlock = randomBlock;
        if (!randomPass)
            writeBlock = zeroBlock;

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f, sReadWriteMode);
            raf.seek(0);

            log (prefixString+"starting");
            while (!counter.isFinished()) {
                deleteFilesBackgroundTask.mCurrentFileName = prefixString+" "+counter.getCurrentValue()+"/"+counter.getMaxValue();
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
                        log("TEST sleep " + e);
                    }
                } else {
                    // perform a real wipe
                    raf.write(writeBlock, 0, (int) writeSize);
                }
                counter.add(writeSize);
                deleteFilesBackgroundTask.progress(counter.getProgressPercent());

                if (deleteFilesBackgroundTask.isCancelled()) {
                    log("Delete cancelled");
                    break;
                }
            }
        } catch (IOException ioe) {
            log(prefixString + ioe);
        } finally {
            closeRandomAccessFile(raf);
        }

        log(prefixString + "complete");

        counter.finish();
    }

    public void wipeFile(File f) {
        deleteFilesBackgroundTask.mCurrentFileName = f.getName();

        log ("Wiping " + deleteFilesBackgroundTask.mCurrentFileName + " size " + f.length());

        if (!f.isDirectory()) {
            // Don't wipe directory files, just do rename and delete (below).
            for (int i = 0; i < numberPasses; i++) {
                ProgressCounter singlePassCounter = new ProgressCounter(f.length());
                singlePassCounter.setParentCounter(deleteFilesBackgroundTask.mProgressCounter);

                if (performZeroWipe) {
                    // Wipe with zeros first
                    ProgressCounter zeroCounter = singlePassCounter.copy();
                    String progressPrefixString = "PASS " + (i + 1) + " ZEROES " + f.getName() + " ";
                    wipePass(progressPrefixString, f, zeroCounter, false);
                    if (deleteFilesBackgroundTask.isCancelled())
                        return;
                }
                String progressPrefixString = "PASS " + (i + 1) + " RANDOMS " + f.getName() + " ";
                wipePass(progressPrefixString, f, singlePassCounter, true);
                if (deleteFilesBackgroundTask.isCancelled())
                    return;
            }
        }

        renamefile(f);

        log("Wipe complete: " + f.getName());
    }

    public void updateByteCountWithPassCount(ProgressCounter counter) {
        long counterMaxValue = counter.getMaxValue();
        counterMaxValue *= numberPasses;
        if (performZeroWipe)
            counterMaxValue *= 2;
        counter.setMaxValue(counterMaxValue);
    }
}
