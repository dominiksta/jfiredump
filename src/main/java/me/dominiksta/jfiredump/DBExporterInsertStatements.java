package me.dominiksta.jfiredump;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * An implementation of `DBExporter` that exports to files consisting of a series
 * of SQL INSERT statements.
 */
public class DBExporterInsertStatements extends DBExporter {

    /** Hold the data retrieved from the database in `getQueryData` */
    private class TableData
        extends HashMap<String, Tuple<Integer, ArrayList<String>>> {}

    /** Contains a mapping of JDBC types as integers to strings */
    private HashMap<Integer, String> jdbcTypeToString;

    public DBExporterInsertStatements(DBConnection con) {
        super(con);

        // prepare jdbc type information for later
        // ----------------------------------------------------------------------
        this.jdbcTypeToString = new HashMap<Integer, String>();
        try {
            Field[] fields = Types.class.getFields();
            for (Field field : fields) {
                this.jdbcTypeToString.put(field.getInt(null), field.getName());
            }
        } catch (IllegalAccessException e) {
            App.logger.severe("Failed getting JDBC type information");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Return the data for a given `query`. All fields are converted to Strings
     * that can be used to assemble INSERT statements.
     */
    private TableData getQueryData(String query) {
        if (!query.substring(0, 6).equalsIgnoreCase("select"))
            throw new IllegalArgumentException("Query does not start with `select`");

        ResultSet rs = this.con.executeQuery(query);
        try {
            TableData tableData = new TableData();
            ResultSetMetaData rsmd = rs.getMetaData();

            for (int i = 1; i <= rsmd.getColumnCount(); i++)
                tableData.put(rsmd.getColumnLabel(i),
                    new Tuple<Integer, ArrayList<String>>(
                        rsmd.getColumnType(i),
                        new ArrayList<String>()
                    )
                );
            App.logger.fine("Got column labels: " + tableData.keySet());

            if (!rs.isBeforeFirst()) {
                App.logger.info("No data returned by specified query: " + query);
                return tableData;
            }

            // only warn once per type, don't spam the user
            ArrayList<Integer> typesAlreadWarned = new ArrayList<Integer>();

            while(rs.next()) {
                for(String column : tableData.keySet()) {
                    Object value = rs.getObject(column);
                    App.logger.finest(
                        "Adding value " + value + " in row " + rs.getRow() +
                        " with type " + tableData.get(column).a
                    );
                    Tuple<Integer, ArrayList<String>> col = tableData.get(column);

                    switch(col.a) {
                        // ------------------------------------------------------------
                        // supported types
                        // ------------------------------------------------------------
                        case Types.BIT:
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.INTEGER:
                        case Types.BIGINT:
                        case Types.FLOAT:
                        case Types.REAL:
                        case Types.DOUBLE:
                        case Types.NUMERIC:
                        case Types.DECIMAL:
                            col.b.add(value == null ? "NULL" : value.toString());
                            break;
                        case Types.CHAR:
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR:
                            col.b.add(value == null ? "NULL" : "'" + value.toString() + "'");
                            break;
                        case Types.NULL:
                            col.b.add("NULL");
                            break;
                        case Types.BOOLEAN:
                            col.b.add(value == null ? "NULL" : value.toString());
                            break;
                        case Types.NCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGNVARCHAR:
                            col.b.add(value == null ? "NULL" : "'" + value.toString() + "'");
                            break;
                        case Types.DATE:
                        case Types.TIME:
                        case Types.TIMESTAMP:
                            // NOTE: These are implemented differently depending on the
                            // firebird dialect. This implementation assumes the use of
                            // dialect 3, which is why that dialect should be explicitly
                            // in the exported file.

                            // See https://firebirdsql.org/file/documentation/pdf/en/
                            // refdocs/fblangref25/firebird-25-language-reference.pdf
                            // on page 31 for details.

                            // In dialect 3, the following semantics apply:
                            // DATE: date-only
                            // DATETIME: date + time
                            // TIMESTAMP: same as DATETIME

                            // Also note that we are dealing with jdbc types here, which
                            // are technically differnt from firebird types. However, the
                            // firebird types seem to map directly to the equally named
                            // jdbc types.

                            if (value == null) {
                                col.b.add("NULL");
                                break;
                            }

                            Calendar c = GregorianCalendar.getInstance();
                            // note: java.sql.Timestamp, java.sql.Date and java.sql.Time
                            // all inherit from java.util.Date, so we can upcast to
                            // java.util.Date here
                            c.setTime((Date) value);
                            String date = String.format(
                                "%04d-%02d-%02d", c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)
                            );
                            String time = String.format(
                                "%02d:%02d:%02d.%04d", c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
                                c.get(Calendar.MILLISECOND)
                            );

                            if (col.a == Types.DATE) {
                                col.b.add("'" + date + "'");
                            } else if (col.a == Types.TIME) {
                                col.b.add("'" + time + "'");
                            } else if (col.a == Types.TIMESTAMP) {
                                col.b.add("'" + date + " " + time + "'");
                            } else {
                                throw new RuntimeException("This makes no sense");
                            }
                            break;
                        // ------------------------------------------------------------
                        // unsupported types
                        // ------------------------------------------------------------
                        case Types.BINARY:
                        case Types.VARBINARY:
                        case Types.LONGVARBINARY:
                        case Types.BLOB:
                        case Types.CLOB:
                        case Types.OTHER:
                            if (typesAlreadWarned.indexOf(col.a) == -1) {
                                App.logger.warning(
                                    "Unsupported type: " + this.jdbcTypeToString.get(col.a)
                                );
                                typesAlreadWarned.add(col.a);
                            }
                            col.b.add("'[BINARY_DATA_LOST_IN_EXPORT]'");
                            break;
                        case Types.JAVA_OBJECT:
                        case Types.DISTINCT:
                        case Types.ARRAY:
                        case Types.REF:
                        case Types.DATALINK:
                        case Types.ROWID:
                        case Types.NCLOB:
                        case Types.SQLXML:
                        case Types.REF_CURSOR:
                        case Types.TIME_WITH_TIMEZONE:
                        case Types.TIMESTAMP_WITH_TIMEZONE:
                            if (typesAlreadWarned.indexOf(col.a) == -1) {
                                App.logger.warning(
                                    "Unsupported type: " + this.jdbcTypeToString.get(col.a)
                                );
                                typesAlreadWarned.add(col.a);
                            }
                            col.b.add("NULL");
                            break;
                        default:
                            App.logger.warning("Unsupported type: ?");
                            col.b.add("NULL");
                    }
                }
            }

            return tableData;
        } catch(SQLException e) {
            App.logger.warning("SQL Error!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Run an arbitrary sql query and export it to `fileName`. The export will
     * insert into the table given by `targetTable`. Exports as a series of SQL
     * INSERT statements. If `fileName` is `null`, a default filename is
     * generated.
     */
    @Override
    public void exportQuery(String query, String targetTable, String fileName) {
        if (targetTable.length() == 0)
            throw new IllegalArgumentException("Table name may not be empty");

        if (fileName == null) fileName = DBExporter.defaultFileName(targetTable);

        App.logger.info(
            "Starting export of query '" + query + "' to '" + fileName + "'..."
        );

        TableData tableData = this.getQueryData(query);
        // they have to be all the same length, so we can just take the length
        // of the first one
        int length = tableData.get(tableData.keySet().iterator().next()).b.size();
        String columns = "";
        for (String column : tableData.keySet())
            columns += column + ",";
        columns = columns.substring(0, columns.length() - 1);

        BufferedWriter out = null;
        try {
            out = this.writerForPath(fileName);
            out.write(
                "-- File generated by `jfirebird`. " +
                "See https://github.com/dominiksta/jfiredump\n" +
                "-- Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date()) + "\n" +
                "-- Exported query: " + query + "\n" +
                "-- Target table: " + targetTable + "\n" +
                "-- Note that this file is made for Firebird 2.x with dialect 3 " +
                "- other database \n" +
                "-- technologies may or may not accept this file\n" +
                "-- SET SQL_DIALECT 3;\n"
            );
            for (int i = 0; i < length; i++) {
                String values = "";
                for (String column : tableData.keySet()) {
                    values += tableData.get(column).b.get(i) + ",";
                }
                values = values.substring(0, values.length() - 1);
                out.write(
                    "INSERT INTO " + targetTable + " (" + columns + ") VALUES ("
                    + values + ");\n"
                );
            }
            App.logger.info("Done exporting query '" + query + "'");
        } catch(IOException e) {
            App.logger.severe("Could not write to file");
            e.printStackTrace();
            System.exit(1);
        } finally {
            Util.closeWarn(out);
        }
    }

    /**
     * Export a table by name. Exports to `fileName` as a series of SQL INSERT
     * statements. If `fileName` is `null`, a default filename is generated.
     */
    @Override
    public void exportTable(String table, String fileName) {
        this.exportQuery("SELECT * FROM " + table, table, fileName);
    }

    /**
     * Export all tables. Exports to files in `directoryName` (individual
     * fileNames are generated from `DBExporter.defaultFileName`) as a series of
     * SQL INSERT statements. If `directoryName` is `null`, the default
     * directory './out' is used.
     */
    @Override
    public void exportAllTables(String directoryName) {
        if (directoryName == null) directoryName = "." + Util.sep + "out";

        File dir = new File(directoryName);
        if (dir.mkdirs()) {
            App.logger.info("Created directory " + directoryName);
        } else {
            App.logger.info(
                "Directory " + directoryName + " already exists or cannot be created"
            );
        }

        ResultSet rs = this.con.listTables();
        try {
            while (rs.next()) {
                String fileName = directoryName + Util.sep +
                    DBExporter.defaultFileName(rs.getString(3));
                this.exportTable(rs.getString(3), fileName);
            }
        } catch(SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
