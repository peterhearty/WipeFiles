package uk.org.platitudes.wipe.file;

import java.io.File;

/**
 * Created by pete on 06/04/15.
 */
public class FileHolder {

    public String nameOverride;
    public File file;
    public boolean selected;


    public FileHolder(File f) {
        file = f;
    }

    public String toString() {
        String result = nameOverride;
        if (result == null) result = file.getName();
        return result;
    }

    /*
     Not really part of this class, just compares two files.
     */
    public static boolean isLowerNameThan (File f1, File f2) {
        // Directories comes before files
        if (f1.isDirectory() && !f2.isDirectory()) return true;
        if (f2.isDirectory() && !f1.isDirectory()) return false;

        String name = f1.getName();
        String otherName = f2.getName();
        if (name.compareTo(otherName) < 0)
            return true;

        return false;
    }

    /*
     Not really part of this class, just sorts files by name.
     */
    public static void sortByName (File[] files) {
        for (int i=1; i < files.length; i++) {
            File currentFile = files[i];
            int curPosn = i; // current posn of file being sorted

            // Compare the file at posn i with all the ones above it.
            for (int j=i-1; j >= 0; j--) {
                File compareFile = files[j];
                if (isLowerNameThan(currentFile, compareFile)) {
                    // currentfile < compareFile
                    files[j] = currentFile;
                    files[curPosn] = compareFile;
                    curPosn = j;
                } else {
                    // currentfile >= compareFile, so in correct place
                    break;
                }
            } // for j
        }
    }
}
