package me.dominiksta.jfiredump;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The abstract class representing an 'exporter'. An 'exporter' can take some
 * arbitrary sql expression and export it into a file or anything else. The
 * details are deliberately left up to the implementing class.
 */
public abstract class DBExporter {

    /** A connection to the database to export from */
    protected DBConnection con;

    public DBExporter(DBConnection con) {
        this.con = con;
    }

    /**
     * Run an arbitrary sql query and export it to `fileName`. The export will
     * insert into the table given by `targetTable`.
     */
    public abstract void exportQuery(String query, String targetTable, String fileName);
    /** Export a table by name to `fileName` */
    public abstract void exportTable(String table, String fileName);
    /** Export all tables to `directoryName` */
    public abstract void exportAllTables(String directoryName);

    /** Helper to return an open BufferedWriter for `fileName` */
    protected BufferedWriter writerForPath(String fileName) {
        try {
            return new BufferedWriter(new FileWriter(fileName));
        } catch(IOException e) {
            App.logger.severe("Could not open file with path " + fileName);
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    /** Return a default filename for exporting `table` */
    protected static String defaultFileName(String tableName) {
        return String.format("%s %s.sql",
            new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()),
            tableName
        );
    }
}