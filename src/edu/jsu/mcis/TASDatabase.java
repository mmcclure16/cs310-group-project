package edu.jsu.mcis;

import java.util.*;
import java.sql.*;
import java.time.LocalTime;

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
    public Punch getPunch(int punchID){
        try{
        }catch (Exception e) {
            System.err.println(e.toString());
        } 
        return null;
    }
    
    public Badge getBadge(String badgeID){
        Badge b = new Badge(" ", " ");
        try{
            Statement statement = conn.createStatement();
            String query = "SELECT * FROM tas.badge WHERE id = '" + badgeID + "';";
            ResultSet result = statement.executeQuery(query);
            
            while(result.next())
            {
                String id;
                String description;
                id = result.getString("id");
                description = result.getString("description");
                b = new Badge(id, description);
            }
            return b;
        }catch (Exception e) {
            System.err.println(e.toString());
        } 
        return null;
    }
    
    public Shift getShift(int shiftID) {
        return getShift((byte)shiftID);
    }
    
    private Shift getShift(byte shiftID) {
        
        try {
            
            Statement statement = conn.createStatement();
            String query = "SELECT * FROM tas.shift WHERE id = '" + shiftID + "';";
            ResultSet result = statement.executeQuery(query);
            
            if (result.next()) {
                
                HashMap byteResults = new HashMap<String, Byte>();
                HashMap localTimeResults = new HashMap<String, LocalTime>();
                
                byteResults.put("id", shiftID);
                byteResults.put("interval", result.getByte("interval"));
                byteResults.put("gracePeriod", result.getByte("graceperiod"));
                byteResults.put("dock", result.getByte("dock"));
                
                localTimeResults.put("start", result.getTime("start").toLocalTime());
                localTimeResults.put("stop", result.getTime("stop").toLocalTime());
                localTimeResults.put("lunchStart", result.getTime("lunchstart").toLocalTime());
                localTimeResults.put("lunchStop", result.getTime("lunchstop").toLocalTime());
                
                return new Shift(
                        byteResults,
                        localTimeResults,
                        result.getString("description"),
                        result.getShort("lunchdeduct")
                );
                
            }
            
            else throw new Exception("Query unsuccessful: shift with ID " + shiftID + " either does not exist or the database failed.");
            
        } catch (Exception e) {
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


    
