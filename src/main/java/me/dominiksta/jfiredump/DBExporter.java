package me.dominiksta.jfiredump;

import java.io.BufferedWriter;

/**
 * The abstract class representing an 'exporter'. An 'exporter' can take some
 * arbitrary sql expression and export it into a file or anything else. The
 * details are deliberately left up to the implementing class.
 */
public abstract class DBExporter {

    /** A connection to the database to export from */
    protected DBConnection con;
    /** This file should be written to for exports */
    protected BufferedWriter outFileWriter;

    public DBExporter(DBConnection con, BufferedWriter outFileWriter) {
        this.con = con;
        this.outFileWriter = outFileWriter;
    }

    /**
     * Run an arbitrary sql query and export it. The export will insert into the
     * table given by `targetTable`.
     */
    public abstract void exportQuery(String query, String targetTable);
    /** Export a table by name */
    public abstract void exportTable(String table);
    /** Export all tables */
    public abstract void exportAll();
}