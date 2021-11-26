package me.dominiksta.jfiredump;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private Connection con;
    private Statement stmt;

    public DBConnection(
        String host, int port, String path, String user, String password
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
            this.con = DriverManager.getConnection(connectionString, user, password);
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

    public void close() throws SQLException {
        this.stmt.close();
        this.con.close();
    }

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
}
