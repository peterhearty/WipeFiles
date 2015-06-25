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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

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
    private byte[] zeroBlock;
    private byte[] randomBlock;

    public RealFileWiper (DeleteFilesBackgroundTask dfbt) {
        deleteFilesBackgroundTask = dfbt;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainTabActivity.sTheMainActivity);
        numberPasses = sharedPref.getInt("number_passes", 1);
        performZeroWipe = sharedPref.getBoolean("zero_wipe", false);
        writeBlockSize = sharedPref.getInt("block_size", 8192);
        if (writeBlockSize < 0 || writeBlockSize > 65536)
            writeBlockSize = 8192;
        zeroBlock = new byte[writeBlockSize];
        randomBlock = new byte[writeBlockSize];
    }

    private void wipePass (RandomAccessFile raf, ProgressCounter counter, boolean randomPass) throws IOException {
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
//            raf.write(writeBlock, 0, (int) writeSize);
            counter.add(writeSize);
            deleteFilesBackgroundTask.progress(counter.getProgressPercent());

            if (deleteFilesBackgroundTask.isCancelled()) {
                deleteFilesBackgroundTask.addLogMessage ("Delete cancelled");
                break;
            }
        }

        counter.finish();
    }

    @Override
    public void wipeFile(File f) {
        deleteFilesBackgroundTask.currentFileName = f.getName();

        // An ordinary file to wipe
        ProgressCounter fileProgressCounter = new ProgressCounter(f.length());
        fileProgressCounter.setParentCounter(deleteFilesBackgroundTask.progressCounter);

        deleteFilesBackgroundTask.addLogMessage("Wiping " + deleteFilesBackgroundTask.currentFileName + " size " + f.length());

        for (int i=0; i < numberPasses; i++) {
            ProgressCounter singlePassCounter = new ProgressCounter(f.length());
            singlePassCounter.setParentCounter(fileProgressCounter);
            singlePassCounter.multiplyCompressFactor(numberPasses);

            String readWriteMode = "rw"; // "rws" does synchronous writes, could make this an option.
            try {
                RandomAccessFile raf = new RandomAccessFile(f, readWriteMode);
                if (performZeroWipe) {
                    // Wipe with zeros first
                    singlePassCounter.multiplyCompressFactor(2);
                    ProgressCounter zeroCounter = singlePassCounter.copy();
                    wipePass(raf, zeroCounter, false);
                }
                wipePass(raf, singlePassCounter, true);
            } catch (Exception e) {
                MainTabActivity.sTheMainActivity.mDeleteLog.add ("Exception: "+e.toString());
                return;
            }
        }

        fileProgressCounter.finish();
        deleteFilesBackgroundTask.progress(fileProgressCounter.getProgressPercent());

        deleteFilesBackgroundTask.addLogMessage("Wipe complete: " + deleteFilesBackgroundTask.currentFileName);
    }
}
