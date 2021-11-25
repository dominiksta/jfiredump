package me.dominiksta.jfiredump;

import java.io.BufferedWriter;

public abstract class DBExporter {

    protected DBConnection con;
    protected BufferedWriter outFileWriter;

    public DBExporter(DBConnection con, BufferedWriter outFileWriter) {
        this.con = con;
        this.outFileWriter = outFileWriter;
    }

    abstract void export(String table);
    abstract void exportAll();
}