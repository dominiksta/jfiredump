package me.dominiksta.jfiredump;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch(ClassNotFoundException e) {
            System.out.println("Could not find Firebird JDBC Driver!");
            e.printStackTrace();
        }
        try {
            Connection connection = DriverManager.getConnection(
                "jdbc:firebirdsql:localhost/3055:c:/users/dominik/source/git/enp-online/db/site.gdb",
                "SYSDBA", "masterkey"
            );
            Statement stmt = connection.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM license");
                rs.next();
                System.out.println(rs.getObject(2));
            } finally {
                stmt.close();
            }
        } catch(SQLException e) {
            System.out.println("Fatal SQL Error!");
            e.printStackTrace();
        }
    }
}
