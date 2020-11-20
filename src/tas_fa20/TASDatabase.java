package tas_fa20;

import java.util.*;
import java.sql.*;
import java.time.*;

public class TASDatabase {
    
        private Connection conn = null;
        private PreparedStatement pstSelect = null, pstUpdate = null;
        private ResultSet resultSet = null;
        private ResultSetMetaData metaData = null;

        private String query;

        private boolean hasresults;
        private int resultCount, columnCount = 0;
        
        // SQL query items as commonly selected from `punch`
        private final String PUNCH_SELECT_ITEMS = "id, terminalid, badgeid,"
                + " punchtypeid, UNIX_TIMESTAMP(originaltimestamp)*1000 AS"
                + " originaltimestamp_unix_mili";
        
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
    
    /**
     * Method for retrieving punch entry from a given punch ID
     * @param punchID
     * @return Punch object representing punch info linked to the given punch ID
     */
    public Punch getPunch(int punchID) {
        
        try {
            
            query = "SELECT " + PUNCH_SELECT_ITEMS + " FROM punch WHERE id = ?";
            pstSelect = conn.prepareStatement(query);
            pstSelect.setInt(1, punchID);
            
            resultSet = pstSelect.executeQuery();
            
            if (resultSet.next()) {
                
                Punch ret =  makePunch(resultSet);
                
                return ret;
                
            }
            
            else throw new Exception(
                    "Query unsuccessful: punch entry with ID `" + punchID
                    + "` either does not exist or the database has failed."
            );
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        
        return null;
        
    }
    
    public ArrayList<Punch> getDailyPunchList(Badge badge, long ts) {
        
        try {
            
            LocalDate parsedDate = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate();
            ArrayList<Punch> punches = new ArrayList<>();

            
            /* First query:
            *  Get all punches made by this employee on the given date
            */
            
            query = "SELECT " + PUNCH_SELECT_ITEMS + " FROM punch WHERE (originaltimestamp BETWEEN '"
                    + parsedDate + " 00:00:00' AND '" + parsedDate + " 23:59:59') AND badgeid = ?;";
            pstSelect = conn.prepareStatement(query);
            pstSelect.setString(1, badge.getID());
            
            resultSet = pstSelect.executeQuery();
            
            
            // prime-START: ensure there are results before trying to loop through them
            
            if (resultSet.next()) punches.add(makePunch(resultSet));
            
            else throw new Exception(
                    "Query unsuccessful: either no punches made by the employee "
                    + "with the badge id `" + badge.getID() + "` exist, or the "
                    + "database has failed."
            );
             
            // prime-END
                
            while (resultSet.next()) punches.add(makePunch(resultSet));
            
            
            /* Second query:
            *  After the given date, if this employees's very next punch
            *  indicates the end of a shift, this means they are completing a
            *  shift that has carried over from the given date; it therefore
            *  should be logged with the given date's punches
            */
            
            query = "SELECT " + PUNCH_SELECT_ITEMS + " FROM ("
                        + "(SELECT * FROM punch WHERE badgeid = ? AND (originaltimestamp > '"
                        + parsedDate + " 23:59:59') ORDER BY originaltimestamp ASC LIMIT 1)"
                    + ") tmp WHERE punchtypeid = 0";

            pstSelect = conn.prepareStatement(query);
            pstSelect.setString(1, badge.getID());
            
            resultSet = pstSelect.executeQuery();
        
            if (resultSet.next()) punches.add(makePunch(resultSet));
            
            return punches;
            
        }
        
        catch (Exception e) {
            System.err.println(e.toString());
        }
        
        return null;
        
    }
    
    /**
     * Method for retrieving badge info and populating a Badge object from a
     * given badge ID
     * @param badgeID ID of the given badge
     * @return Badge object representing the badge info linked to the given
     * badge ID
     */
    public Badge getBadge(String badgeID) {
        
        try {
            
            query = "SELECT id, description FROM badge WHERE id = ?";
            pstSelect = conn.prepareStatement(query);
            pstSelect.setString(1, badgeID);
            
            resultSet = pstSelect.executeQuery();
            
            if (resultSet.next())
            {
                
                Badge ret =  new Badge(
                        resultSet.getString("id"),
                        resultSet.getString("description")
                );
                
                return ret;
                
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
    
    /**
     * Overloaded method to allow Integer-based input to passed on to
     * getShift(byte)
     * @param shiftID ID of the given shift type; assumed to be 8-bit
     * @return Shift object representing the shift type linked to the given
     * shift ID
     */
    public Shift getShift(int shiftID) {
        return getShift((byte)shiftID);
    }
    
    /**
     * Method for retrieving shift type and populating a Shift object based upon
     * a given shift type ID
     * @param shiftID ID of the shift type to retrieve
     * @return Shift object representing the shift type linked to the given
     * shift ID
     */
    private Shift getShift(byte shiftID) {
        
        try {
            
            query = "SELECT * FROM shift WHERE id = ?";
            pstSelect = conn.prepareStatement(query);
            pstSelect.setByte(1, shiftID);
            
            resultSet = pstSelect.executeQuery();
            
            if (resultSet.next()) {
                
                Shift ret = makeShift(resultSet);
                
                return ret;
                
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
    
    /**
     * Method for retrieving shift type and populating a Shift object based upon
     * a given Badge; the Badge object's ID is checked against employees who
     * possess a matching ID, from which a shift type is determined
     * @param badge Badge object to derive a employee's Badge ID from
     * @return Shift object representing the shift type for the employee
     * possessing the given Badge
     */
    public Shift getShift(Badge badge) {
        
        try {
            
            query = "SELECT shift.* FROM shift LEFT JOIN employee ON "
                    + "employee.shiftid=shift.id WHERE employee.badgeid = ?";
            pstSelect = conn.prepareStatement(query);
            pstSelect.setString(1, badge.getID());
            
            resultSet = pstSelect.executeQuery();
            
            if (resultSet.next()) {
                
                Shift ret = makeShift(resultSet);
                
                return ret;
                
            }
            
            else throw new Exception(
                    "Query unsuccessful: either no employee with badge ID `" + badge.getID()
                    + "` exists, or the database has failed."
            );
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        
        return null;
        
    }
    
    public ArrayList<Punch> getPayPeriodPunchList(Badge badge, long ts){
        return null;
    }
    
    public Absenteeism getAbsenteeism(String badgeID, Long timeStamp) {
         try {
            
            query = "SELECT * FROM absenteeism WHERE badgeID = ? AND payperiod = ?";
            pstSelect = conn.prepareStatement(query);
            pstSelect.setString(1, badgeID);
            pstSelect.setLong(2, timeStamp);
            
            resultSet = pstSelect.executeQuery();
            
            if (resultSet.next()) {
                
                Absenteeism ret = new Absenteeism(
                        resultSet.getString("badgeID"), 
                        resultSet.getLong("payperiod"), 
                        resultSet.getDouble("percentage"));
                
                return ret;
                
            }
            
            else throw new Exception(
                    "Query unsuccessful: absenteeism entry with ID `" + badgeID
                    + "` either does not exist or the database has failed."
            );
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }
    
    /**
     * Inserts an entry into the `punch` table based off a given `Punch` object
     * @param p The `Punch` object whose information should be inserted into
     * the database
     * @return The ID of the inserted punch record
     */
    public int insertPunch(Punch p)
    {
        try {
            
            query = "INSERT INTO punch (terminalid, badgeid, originaltimestamp, punchtypeid) VALUES(?, ?, FROM_UNIXTIME(?/1000), ?)";
            pstUpdate = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstUpdate.setShort(1, util.UnsignedByteHandler.getAsShort(p.getTerminalID()));
            pstUpdate.setString(2, p.getBadgeID());
            pstUpdate.setLong(3, p.getOriginalTimeStamp());
            pstUpdate.setShort(4, util.UnsignedByteHandler.getAsShort(p.getPunchTypeID()));
            
            pstUpdate.execute();
            
            resultSet = pstUpdate.getGeneratedKeys();
            
            if (resultSet.next()) {
                
                int res = resultSet.getInt(1);
            
                return res;
                
            }
            
            else throw new Exception(
                    "Insertion unsuccessful. Failed to insert Punch: \n"
                    + p.printOriginalTimestamp() // PLACE HOLDER: should be `p.toString()` once it is implemented
            );
                    
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        
        return 0;
        
    }
    
    public void insertAbsenteeism(Absenteeism absenteeism) {
        
    }
        
    /**
     * Generic method designed to create a new Punch object from a "SELECT *,
     * UNIX_TIMESTAMP(originaltimestamp)*1000 FROM shift"-derived ResultSet
     * @param result Query result containing punch information
     * @return New Punch object containing the ResultSet's data, if the
     * ResultSet contains proper information; null otherwise
     */
    private Punch makePunch(ResultSet result) {
        
        HashMap byteResults = new HashMap<String, Byte>();
        
        try {
        
            byteResults.put("terminalID", (byte)result.getShort("terminalid"));
            byteResults.put("punchTypeID", (byte)result.getShort("punchtypeiD"));
                
            Punch ret = new Punch(
                result.getInt("id"),
                result.getString("badgeid"),
                byteResults,
                result.getLong("originaltimestamp_unix_mili")
            );
            
            return ret;
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        
        return null;
        
    }
    
    /**
     * Generic method designed to create a new Shift object from a "SELECT * FROM
     * shift"-derived ResultSet
     * @param result Query result containing shift information
     * @return New Shift object containing the ResultSet's data, if the
     * ResultSet contains proper information; null otherwise
     */
    private Shift makeShift(ResultSet result) {
        
        HashMap byteResults = new HashMap<String, Byte>();
        HashMap localTimeResults = new HashMap<String, LocalTime>();
        
        try {
            
            byteResults.put("id", (byte)result.getShort("id"));
            byteResults.put("interval", (byte)result.getShort("interval"));
            byteResults.put("gracePeriod", (byte)result.getShort("graceperiod"));
            byteResults.put("dock", (byte)result.getShort("dock"));

            localTimeResults.put("start", result.getTime("start").toLocalTime());
            localTimeResults.put("stop", result.getTime("stop").toLocalTime());
            localTimeResults.put("lunchStart", result.getTime("lunchstart").toLocalTime());
            localTimeResults.put("lunchStop", result.getTime("lunchstop").toLocalTime());
            
            Shift ret = new Shift(
                byteResults,
                localTimeResults,
                result.getString("description"),
                result.getShort("lunchdeduct")
            );

            return ret;
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        
        return null;
        
    }
    
    /**
     * Given a column name and array of Integer values, generates a series of
     * "... OR {column} = ..." SQL statements for the column for each value in
     * the array
     * @param column Name of column to check for each value
     * @param items Array of integer values check
     * @return As a String, a series of "... OR {column} = ..." SQL statements for
     * each value in the passed array; the first value does not have a leading "OR"
     */
    private String query_OrEqualsBuilder(String column, int[] items) {
        
        StringBuilder res = new StringBuilder();
        
        // prime-START: create first entry without leading "OR"
        
        res.append(column + " = " + items[0]);
        
        // prime-END
        
        for (int i = 1; i < items.length; ++i)
            res.append(" OR " + column + " = " + items[i]);
        
        return res.toString();
        
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


    
