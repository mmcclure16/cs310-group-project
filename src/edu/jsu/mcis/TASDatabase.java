package edu.jsu.mcis;

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
    
    public Shift getShift(int shiftID){
        Shift s = new Shift(0, null, null, null, 0, 0, 0, null, null, 0);
        try{
            Statement statement = conn.createStatement();
            String query = "SELECT * FROM tas.shift WHERE id = '" + shiftID + "';";
            ResultSet result = statement.executeQuery(query);
            
            while(result.next()){
                int id, interval, graceperiod, dock, lunchdeduct;;
                String description;
                LocalTime start, stop, lunchstart, lunchstop;;
                Time T;

                id = result.getInt("id");
                description = result.getString("description");
                T = result.getTime("start");
                start = T.toLocalTime();
                T = result.getTime("stop");
                stop = T.toLocalTime();
                interval = result.getInt("interval");
                graceperiod = result.getInt("graceperiod");
                dock = result.getInt("dock");
                T = result.getTime("lunchstart");
                lunchstart = T.toLocalTime();
                T = result.getTime("lunchstop");
                lunchstop = T.toLocalTime();
                lunchdeduct = result.getInt("lunchdeduct");
                s = new Shift(id, description, start, stop, interval, graceperiod, dock, lunchstart, lunchstop, lunchdeduct);
            }
            return s;
            
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


    
