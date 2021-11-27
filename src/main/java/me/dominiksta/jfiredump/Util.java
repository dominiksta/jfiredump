package me.dominiksta.jfiredump;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/** A class containing some simple static helper methods */
public class Util {
    /** Shorter access to File.seperator */
    public static String sep = File.separator;

    /**
     * Close a resource and warn when closing was not possible.
     */
    public static void closeWarn(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ex) {
                App.logger.warning(
                    "Could not close resource [" + closeable.hashCode() + "]"
                );
            }
        } else {
            App.logger.warning("Tried to close a null resource");
        }
    }
}
