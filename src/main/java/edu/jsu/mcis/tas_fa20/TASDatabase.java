package edu.jsu.mcis.tas_fa20;

import java.sql.*;


public class TASDatabase {
        Connection conn = null;
        PreparedStatement pstSelect = null, pstUpdate = null;
        ResultSet resultset = null;
        ResultSetMetaData metadata = null;

        String query;

        boolean hasresults;
        int resultCount, columnCount = 0;
    public TASDatabase(){
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
    public Punch getPunch(String punchID){
        try{
            
        }catch (Exception e) {
            System.err.println(e.toString());
        } 
        return null;
    }
    
    public Badge getBadge(String badgeID){
        try{
            
        }catch (Exception e) {
            System.err.println(e.toString());
        } 
        return null;
    }
    
    public Shift getShift(String shiftID){
        try{
            
        }catch (Exception e) {
            System.err.println(e.toString());
        } 
        return null;
    }
    
    public Shift getShift(Badge badge){
        try{
            
        }catch (Exception e) {
            System.err.println(e.toString());
        } 
        return null;
    }
    
    public void close() throws SQLException{
        try{
            conn.close();
            
            if (resultset != null) {
                    try {
                        resultset.close();
                        resultset = null;
                    } catch (Exception e) {
                    }
                }

                if (pstSelect != null) {
                    try {
                        pstSelect.close();
                        pstSelect = null;
                    } catch (Exception e) {
                    }
                }

                if (pstUpdate != null) {
                    try {
                        pstUpdate.close();
                        pstUpdate = null;
                    } catch (Exception e) {
                    }
                }

            }catch (Exception e) {
            System.err.println(e.toString());
        }          
    }
}


    
