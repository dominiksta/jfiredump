package me.dominiksta.jfiredump;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * A wrapper for some JDBC functionality to make connecting to the firebird
 * database a little bit easier for me.
 */
public class DBConnection {

    private Connection con;
    private Statement stmt;
    private Properties props;

    /** Connect to a firebird database as specified by the arguments */
    public DBConnection(
        String host, int port, String path, String user, String password,
        String encoding
    ) {
        String connectionString = "jdbc:firebirdsql:" + host + "/" + port + ":" + path;
        App.logger.info(
            "Connecting to " + connectionString + " with user " + user
        );
        // load firebird driver
        // ----------------------------------------------------------------------
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch(ClassNotFoundException e) {
            System.out.println("Could not find Firebird JDBC Driver!");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            // connect to server
            // ----------------------------------------------------------------------
            props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", password);
            // see https://github.com/FirebirdSQL/jaybird/wiki/Character-encodings
            if (encoding != null) props.setProperty("encoding", encoding);

            this.con = DriverManager.getConnection(connectionString, props);
            App.logger.info("Connection successful");
            this.stmt = this.con.createStatement();

            // detect version
            // ----------------------------------------------------------------------
            ResultSet rs = this.executeQuery(
                "SELECT rdb$get_context('SYSTEM', 'ENGINE_VERSION') from rdb$database;"
            );
            rs.next();
            String version = rs.getString(1);
            App.logger.info("Detected firebird version: " + version);
            if (!version.startsWith("2")) {
                App.logger.warning("Only firebird major version 2 is currently supported!");
                App.logger.warning("Execution will continue, but things may break!");
            }
        } catch(SQLException e) {
            App.logger.severe("Fatal SQL Error!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /** Close the database connection */
    public void close() throws SQLException {
        this.stmt.close();
        this.con.close();
    }

    /** Run a `query` and return a `ResultSet` */
    public ResultSet executeQuery(String query) {
        App.logger.fine("Running SQL: " + query);
        try {
            return this.stmt.executeQuery(query);
        } catch(SQLException e) {
            App.logger.severe("Fatal SQL Error!");
            e.printStackTrace();
            return null;
        }
    }

    /** List all (non-system) tables in the database. */
    public ResultSet listTables(){
        // One could use a firebird specific query to retrieve all table
        // names. If I however should want to at some point in the future for
        // some reason extend this tool for other databases, then this way of
        // doing it with JDBC would still work.
        try {
            DatabaseMetaData md = this.con.getMetaData();
            // If the last parameter is set to null, all tables including system
            // tables and views will be listed.
            return md.getTables(null, null, "%", new String[]{"TABLE"});
        } catch(SQLException e) {
            App.logger.severe("Fatal SQL Error!");
            e.printStackTrace();
            return null;
        }
    }

    /** Get the set Firebird encoding */
    public String getEncoding() {
        return this.props.getProperty("encoding") == null ?
            "NONE" : this.props.getProperty("encoding");
    }
}
