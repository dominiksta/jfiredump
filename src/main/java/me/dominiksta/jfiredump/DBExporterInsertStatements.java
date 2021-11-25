package me.dominiksta.jfiredump;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

public class DBExporterInsertStatements extends DBExporter {

    private class TableData
        extends HashMap<String, Tuple<Integer, ArrayList<String>>> {}

    public DBExporterInsertStatements(DBConnection con, BufferedWriter outFileWriter) {
        super(con, outFileWriter);
    }

    private TableData getTableData(String table) {
        ResultSet rs = this.con.executeQuery("SELECT * FROM " + table);
        try {
            TableData tableData = new TableData();
            ResultSetMetaData rsmd = rs.getMetaData();

            for (int i = 1; i < rsmd.getColumnCount(); i++)
                tableData.put(rsmd.getColumnLabel(i),
                    new Tuple<Integer, ArrayList<String>>(
                        rsmd.getColumnType(i),
                        new ArrayList<String>()
                    )
                );
            App.logger.fine("Got column labels: " + tableData.keySet());

            if (!rs.isBeforeFirst()) {
                App.logger.info("No data in specified table " + table);
                return tableData;
            }

            while(rs.next()) {
                for(String column : tableData.keySet()) {
                    Object value = rs.getObject(column);
                    App.logger.finest(
                        "Adding value " + value + " in row " + rs.getRow() +
                        " with type " + tableData.get(column).a
                    );
                    Tuple<Integer, ArrayList<String>> col = tableData.get(column);

                    // TODO: Repeat myself less with the 'Unsupported Types' message
                    switch(col.a) {
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
                        case Types.DATE:
                            App.logger.warning("Unsupported type: DATE");
                            col.b.add("NULL");
                            break;
                        case Types.TIME:
                            App.logger.warning("Unsupported type: TIME");
                            col.b.add("NULL");
                            break;
                        case Types.TIMESTAMP:
                            App.logger.warning("Unsupported type: TIMESTAMP");
                            col.b.add("NULL");
                            break;
                        case Types.BINARY:
                            App.logger.warning("Unsupported type: BINARY");
                            col.b.add("NULL");
                            break;
                        case Types.VARBINARY:
                            App.logger.warning("Unsupported type: VARBINARY");
                            col.b.add("NULL");
                            break;
                        case Types.LONGVARBINARY:
                            App.logger.warning("Unsupported type: LONGVARBINARY");
                            col.b.add("NULL");
                            break;
                        case Types.NULL:
                            col.b.add("NULL");
                            break;
                        case Types.OTHER:
                            App.logger.warning("Unsupported type: OTHER");
                            col.b.add("NULL");
                            break;
                        case Types.JAVA_OBJECT:
                            App.logger.warning("Unsupported type: JAVA_OBJECT");
                            col.b.add("NULL");
                            break;
                        case Types.DISTINCT:
                            App.logger.warning("Unsupported type: DISTINCT");
                            col.b.add("NULL");
                            break;
                        case Types.ARRAY:
                            App.logger.warning("Unsupported type: ARRAY");
                            col.b.add("NULL");
                            break;
                        case Types.BLOB:
                            App.logger.warning("Unsupported type: BLOB");
                            col.b.add("NULL");
                            break;
                        case Types.CLOB:
                            App.logger.warning("Unsupported type: CLOB");
                            col.b.add("NULL");
                            break;
                        case Types.REF:
                            App.logger.warning("Unsupported type: REF");
                            col.b.add("NULL");
                            break;
                        case Types.DATALINK:
                            App.logger.warning("Unsupported type: DATALINK");
                            col.b.add("NULL");
                            break;
                        case Types.BOOLEAN:
                            col.b.add(value == null ? "NULL" : value.toString());
                            break;
                        case Types.ROWID:
                            App.logger.warning("Unsupported type: ROWID");
                            col.b.add("NULL");
                            break;
                        case Types.NCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGNVARCHAR:
                            col.b.add(value == null ? "NULL" : "'" + value.toString() + "'");
                            break;
                        case Types.NCLOB:
                            App.logger.warning("Unsupported type: NCLOB");
                            col.b.add("NULL");
                            break;
                        case Types.SQLXML:
                            App.logger.warning("Unsupported type: SQLXML");
                            col.b.add("NULL");
                            break;
                        case Types.REF_CURSOR:
                            App.logger.warning("Unsupported type: REF_CURSOR");
                            col.b.add("NULL");
                            break;
                        case Types.TIME_WITH_TIMEZONE:
                            App.logger.warning("Unsupported type: TIME_WITH_TIMEZONE");
                            col.b.add("NULL");
                            break;
                        case Types.TIMESTAMP_WITH_TIMEZONE:
                            App.logger.warning("Unsupported type: TIMESTAMP_WITH_TIMEZONE");
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

    void export(String table) {
        TableData tableData = this.getTableData(table);
        // they have to be all the same length, so we can just take the length
        // of the first one
        int length = tableData.get(tableData.keySet().iterator().next()).b.size();
        String columns = "";
        for (String column : tableData.keySet())
            columns += column + ",";
        columns = columns.substring(0, columns.length() - 1);

        for (int i = 0; i < length; i++) {
            try {
                String values = "";
                for (String column : tableData.keySet()) {
                    values += tableData.get(column).b.get(i) + ",";
                }
                values = values.substring(0, values.length() - 1);
                this.outFileWriter.write(
                    "INSERT INTO " + table + " (" + columns + ") VALUES ("
                    + values + ");\n"
                );
            } catch(IOException e) {
                App.logger.severe("Could not write to file");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    void exportAll() {
        // TODO: Implement
    }
}
