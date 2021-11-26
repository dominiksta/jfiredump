package me.dominiksta.jfiredump;

import java.io.BufferedWriter;

public abstract class DBExporter {

    protected DBConnection con;
    protected BufferedWriter outFileWriter;

    public DBExporter(DBConnection con, BufferedWriter outFileWriter) {
        this.con = con;
        this.outFileWriter = outFileWriter;
    }

    public abstract void exportQuery(String query, String targetTable);
    public abstract void exportTable(String table);
    public abstract void exportAll();
}