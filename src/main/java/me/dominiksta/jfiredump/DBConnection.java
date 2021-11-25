package me.dominiksta.jfiredump;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DBConnection {

    private Connection con;
    private Statement stmt;

    public DBConnection(
        String host, int port, String path, String user, String password
    ) {
        String connectionString = "jdbc:firebirdsql:" + host + "/" + port + ":" + path;
        App.logger.info(
            "Connecting to " + connectionString + " with " + user + ":" + password
        );
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch(ClassNotFoundException e) {
            System.out.println("Could not find Firebird JDBC Driver!");
            e.printStackTrace();
        }
        try {
            this.con = DriverManager.getConnection(connectionString, user, password);
            App.logger.info("Connection successful");
            this.stmt = this.con.createStatement();
        } catch(SQLException e) {
            App.logger.severe("Fatal SQL Error!");
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String query) {
        App.logger.log(Level.DEBUG, "Running SQL: " + query);
        try {
            return this.stmt.executeQuery(query);
        } catch(SQLException e) {
            App.logger.severe("Fatal SQL Error!");
            e.printStackTrace();
            return null;
        }
    }
}
