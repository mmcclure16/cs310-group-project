package edu.jsu.mcis.tas_fa20;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TASDatabase {
    public TASDatabase(){
        Connection conn = null;
        PreparedStatement pstSelect = null, pstUpdate = null;
        ResultSet resultset = null;
        ResultSetMetaData metadata = null;

        String query;

        boolean hasresults;
        int resultCount, columnCount = 0;
        
        try {

            /* Identify the Server */

            String server = ("jdbc:mysql://localhost/tas");
            String username = "tasuser";
            String password = "CS488";
            System.out.println("Connecting to " + server + "...");

            /* Load the MySQL JDBC Driver */

            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            /* Open Connection */

            conn = DriverManager.getConnection(server, username, password);

            /* Test Connection */

            if (conn.isValid(0)) {

                /* Connection Open! */

                System.out.println("Connected Successfully!");
            }
            
        }catch (Exception e) {
            System.err.println(e.toString());
        } 
    }
}
        

 
