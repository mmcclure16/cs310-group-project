package tas_fa20;

import java.util.*;
import java.sql.*;
import java.time.LocalTime;

public class TASDatabase {
    
        Connection conn = null;
        PreparedStatement pstSelect = null, pstUpdate = null;
        ResultSet resultSet = null;
        ResultSetMetaData metaData = null;

        String query;

        boolean hasresults;
        int resultCount, columnCount = 0;
        
    public TASDatabase() {
        
        try {

            String server = ("jdbc:mysql://localhost/tas");
            String username = "tasuser";
            String password = "CS488";

            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(server, username, password);
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        
    }
    
    public Punch getPunch(int punchID) {
        
        try {
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        
        return null;
        
    }
    
    public Badge getBadge(String badgeID) {
        
        try {
            
            query = "SELECT * FROM badge WHERE id = ?";
            pstSelect = conn.prepareStatement(query);
            pstSelect.setString(1, badgeID);
            
            resultSet = pstSelect.executeQuery();
            
            if (resultSet.next())
            {
                return new Badge(
                        resultSet.getString("id"),
                        resultSet.getString("description")
                );
            }
            
            else throw new Exception(
                    "Query unsuccessful: badge entry with ID `" + badgeID
                    + "` either does not exist or the database has failed."
            );
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        
        return null;
        
    }
    
    public Shift getShift(int shiftID) {
        return getShift((byte)shiftID);
    }
    
    private Shift getShift(byte shiftID) {
        
        try {
            
            query = "SELECT * FROM shift WHERE id = ?";
            pstSelect = conn.prepareStatement(query);
            pstSelect.setByte(1, shiftID);
            
            resultSet = pstSelect.executeQuery();
            
            if (resultSet.next()) {
                
                HashMap byteResults = new HashMap<String, Byte>();
                HashMap localTimeResults = new HashMap<String, LocalTime>();
                
                byteResults.put("id", shiftID);
                byteResults.put("interval", (byte)resultSet.getShort("interval"));
                byteResults.put("gracePeriod", (byte)resultSet.getShort("graceperiod"));
                byteResults.put("dock", (byte)resultSet.getShort("dock"));
                
                localTimeResults.put("start", resultSet.getTime("start").toLocalTime());
                localTimeResults.put("stop", resultSet.getTime("stop").toLocalTime());
                localTimeResults.put("lunchStart", resultSet.getTime("lunchstart").toLocalTime());
                localTimeResults.put("lunchStop", resultSet.getTime("lunchstop").toLocalTime());
                
                return new Shift(
                        byteResults,
                        localTimeResults,
                        resultSet.getString("description"),
                        resultSet.getShort("lunchdeduct")
                );
                
            }
            
            else throw new Exception(
                    "Query unsuccessful: shift entry with ID `" + shiftID
                    + "` either does not exist or the database has failed."
            );
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        
        return null;
        
    }
    
    public Shift getShift(Badge badge) {
        
        try {
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        
        return null;
        
    }
    
    public void close() throws SQLException {
        try {
            
            conn.close();
            
            if (resultSet != null) {
                    try {
                        resultSet.close();
                        resultSet = null;
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

            } catch (Exception e) {
            System.err.println(e.toString());
            
        }
    }
}


    
