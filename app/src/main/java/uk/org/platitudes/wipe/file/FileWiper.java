package uk.org.platitudes.wipe.file;

import java.io.File;

/**
 * TestFileWiper and RealFileWiper both implement this. It provides the basic interface
 * for both classes.
 *
 *  This source code is not owned by anybody. You can can do what you like with it.
 *
 * @author  Peter Hearty
 * @date    April 2015

 */
public interface FileWiper {

    public void wipeFile (File f);

}
