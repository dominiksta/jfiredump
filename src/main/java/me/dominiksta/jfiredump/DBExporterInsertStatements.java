package me.dominiksta.jfiredump;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

public class DBExporterInsertStatements extends DBExporter {

    private class TableData
        extends HashMap<String, Tuple<Integer, ArrayList<String>>> {}

    private HashMap<Integer, String> jdbcTypeToString;

    public DBExporterInsertStatements(DBConnection con, BufferedWriter outFileWriter) {
        super(con, outFileWriter);

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
                        // ------------------------------------------------------------
                        // unsupported types
                        // ------------------------------------------------------------
                        case Types.DATE:
                        case Types.TIME:
                        case Types.TIMESTAMP:
                        case Types.BINARY:
                        case Types.VARBINARY:
                        case Types.LONGVARBINARY:
                        case Types.OTHER:
                        case Types.JAVA_OBJECT:
                        case Types.DISTINCT:
                        case Types.ARRAY:
                        case Types.BLOB:
                        case Types.CLOB:
                        case Types.REF:
                        case Types.DATALINK:
                        case Types.ROWID:
                        case Types.NCLOB:
                        case Types.SQLXML:
                        case Types.REF_CURSOR:
                        case Types.TIME_WITH_TIMEZONE:
                        case Types.TIMESTAMP_WITH_TIMEZONE:
                            App.logger.warning(
                                "Unsupported type: " + this.jdbcTypeToString.get(col.a)
                            );
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

    @Override
    public void exportQuery(String query, String targetTable) {
        if (targetTable.length() == 0)
            throw new IllegalArgumentException("Table name may not be empty");

        TableData tableData = this.getQueryData(query);
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
                    "INSERT INTO " + targetTable + " (" + columns + ") VALUES ("
                    + values + ");\n"
                );
            } catch(IOException e) {
                App.logger.severe("Could not write to file");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    @Override
    public void exportTable(String table) {
        this.exportQuery("SELECT * FROM " + table, table);
    }

    @Override
    public void exportAll() {
        // TODO: Implement
    }
}
