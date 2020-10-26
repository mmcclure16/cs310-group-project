package tas_fa20;

import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.text.SimpleDateFormat;

public class Punch {
    
    private int id;
    private byte terminalID;
    private String badgeID;
    private Long originalTimeStamp;
    private byte punchTypeID;
    
    private String punchTypeTitle;
    private final SimpleDateFormat timeStampFormat = new SimpleDateFormat("EEE MM/dd/yyyy HH:mm:ss");
    
    private String adjustmentType;
    private Long adjustedTimeStamp;
    
    /* 
    * A constant list of `punchtypeid`s that are known to/handled by this
    * class
    */
    private final List<Integer> RECOGNIZED_PUNCHTYPE_IDS = Arrays.asList(0, 1, 2);
    
    /* 
    * A  constant list of `punchtypeid`s that represent an "ending punch"
    * (ie, punching out or timing out), as defined by the database
    */
    private final List<Integer> TERMINATING_PUNCHTYPE_IDS = Arrays.asList(0, 2);
    
    
    /* Constructors for new punch */
    
    public Punch(Badge badge, int terminalID, int punchtypeID)
    {
        this(
            0,
            (byte)terminalID,
            badge.getID(),
            null,
            (byte)punchtypeID
        );
    }
    
    
    public Punch(String badgeID, Map<String, Byte> byteParams) {
        this(
                0,
                byteParams.get("terminalID"),
                badgeID,
                null,
                byteParams.get("punchTypeID")
        );
    }
    
    
    /* Constructors for existing punch */
    
    public Punch(int id, String badgeID, Map<String, Byte> byteParams, Long originalTimeStamp) {
        this(
                id,
                byteParams.get("terminalID"),
                badgeID,
                originalTimeStamp,
                byteParams.get("punchTypeID")
        );
    }
    
    
    /* Master Constructor/Supprot methods */
    
    private Punch(int id, byte terminalID, String badgeID, Long originalTimeStamp, byte punchTypeID) {
        this.id = id;
        this.terminalID = terminalID;
        this.badgeID = badgeID;
        this.punchTypeID = punchTypeID;
        
        verifyPunchTypeID();
        
        // set time to system if not pre-provided
        this.originalTimeStamp = (originalTimeStamp != null ? originalTimeStamp : System.currentTimeMillis());
        
        buildPunchTypeTitle();
        
        adjustmentType = null;
        adjustedTimeStamp = null;
    }
    
    private void buildPunchTypeTitle() {
            
        switch(punchTypeID) {

            case 0:
                punchTypeTitle = "CLOCKED OUT";
                break;

            case 1:
                punchTypeTitle = "CLOCKED IN";
                break;

            case 2:
                punchTypeTitle = "TIMED OUT";
                break;

        }
        
    }
    
    
    /* Get/set methods */
    
    public void adjust(Shift s) {
         
        /* 1. Establish date and time of punch */
        Instant instant = Instant.ofEpochMilli(originalTimeStamp);
        
        LocalDate punchDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime formattedPunchTime = instant.atZone(ZoneId.systemDefault()).toLocalTime().withNano(0);
        
        Duration adjustedPunchTime_Offset = Duration.ofHours(formattedPunchTime.getHour());
        
        // In seconds: The amount of duration of the grace period
        int gracePeriodSeconds =  util.UnsignedByteHandler.getAsShort(s.getGracePeriod()) / 60;
        
        int intervalMinutes = util.UnsignedByteHandler.getAsShort(s.getInterval());
        int intervalSeconds = intervalMinutes * 60;
        
        
        /* 2. Establish punch type (in, out) */
        // ...in
        if (punchTypeID == 1) {
            
            /* 3. Determine the precise (clock-in) type/  */
            
            // Before it's possible to be late for the post-lunch shift?
            if (formattedPunchTime.isBefore( s.getLunchStop().plusSeconds(gracePeriodSeconds + 1) )) {
                
                /* NOTE: this can (should) be localized within the conditionals that call for it */
                boolean isPerfectInterval = ((formattedPunchTime.getMinute() % intervalMinutes) == 0);
                
                // Is it during lunch?
                if (formattedPunchTime.isAfter( s.getLunchStop().minusSeconds(intervalSeconds + 1) )) {
                    // clock-in time is equal to "getLunchStop()" | adjustmentType -> LUNCH STOP
                }
                
                // Else, is it before it's possble to be late for the start-shift?
                else if (formattedPunchTime.isBefore( s.getStart().plusSeconds(gracePeriodSeconds + 1) )) {
                    
                    // if(before pre-shift start interval)
                    if (formattedPunchTime.isBefore(s.getStart().minusMinutes(intervalMinutes))) {
                        if (isPerfectInterval) { //do not adjust | adjustmentType -> NONE }
                        }
                        
                        else {
                            
                            adjustmentType = "Interval Round";
                            adjustedPunchTime_Offset = adjustedPunchTime_Offset.plusMinutes(
getNearestAdjustmentInterval(formattedPunchTime.getMinute(), intervalMinutes)
                            );
                            
                        }
                            //push to nearest-forward interval | adjustmentType -> INTERVAL ROUND }
                    }
                    
                    // else if(Less than or equal to start interval?) { clock in time is perfect | adjustmentType -> SHIFT START }
                    
                    // THE FEATURE TEST SCRIPTS DO NOT ACKNOWLEDGE 'GRACE PERIOD' AS DEFINED BY CANVAS
                    // else [(this is implicitly within the grace period)] { push back to shift start | adjustmentType -> SHIFT GRACE }
                }
                
                // Else, start-shift is late
                else {
                    // if (`isPerfectInterval`) { do not adjust | adjustmentType -> NONE }
                    // else { push to nearest-forward interval | adjustmentType -> SHIFT DOCKED }
                }
                
            }
            
            // Else, lunch-end is late
            else {
            }
        }

        // ...out
        else if (TERMINATING_PUNCHTYPE_IDS.contains(punchTypeID)) {
            
             /* 3. Determine the precise (clock-out) type  */
            
            // ...
        }
        
        // Exception (got this far with a punchTypeID we don't recognize)
        else {}
        
        adjustedTimeStamp = punchDate.atStartOfDay().plusMinutes(adjustedPunchTime_Offset.toMinutes()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
    }
    
    public int getID() {
        return id;
    }
    
    public byte getTerminalID() {
        return terminalID;
    }
    
    public String getBadgeID() {
        return badgeID;
    }
    
    public Long getOriginalTimeStamp() {
        return originalTimeStamp;
    }
    
    public byte getPunchTypeID() {
        return punchTypeID;
    }
    
    public String getAdjustmentType() {
        return adjustmentType;
    }
    
    /* Timestamp printing */
    
    private String makeTimeStampString(Long timeStamp) {
        return (timeStampFormat.format(new Date(timeStamp))).toUpperCase();
    }
    
    private String printableTimestamp(Long timeStamp) {
             
        StringBuilder printableTimeStamp = new StringBuilder();
        
        printableTimeStamp.append('#').append(badgeID).append(' ').append(punchTypeTitle);
        printableTimeStamp.append(": ").append(makeTimeStampString(timeStamp));
                
        return printableTimeStamp.toString();
        
    }
    
    public String printOriginalTimestamp() {      
        return printableTimestamp(originalTimeStamp);
    }
    
    public String printAdjustedTimestamp() {
        
        try {

            if (adjustedTimeStamp != null)
                return printableTimestamp(adjustedTimeStamp)
                        + " (" + adjustmentType + ")";

            else throw new Exception("Cannot print 'adjusted time' until it has"
                    + " been generated by a successful call to `adjust(Shift)`");
            
        } catch(Exception e) {
            System.err.println(e.toString());
        }
        
        return null;
        
    }
    
    private int getNearestAdjustmentInterval(int currentMinutes, int intervalMinutes) {
        return (int)Math.round(1f*currentMinutes/intervalMinutes) * intervalMinutes;
    }
    
    /* Error handling/building */
    
    /**
     * Throws and catches an Exception if we've not specified that we recognize 
     * this object's given `punchTypeID`
     */
    private void verifyPunchTypeID() {
        
        try {
            
            if (!RECOGNIZED_PUNCHTYPE_IDS.contains((int)this.punchTypeID))
                throw new Exception("Invalid or unknown `punchTypeID` specn mified:"
                        + " Integer '" + this.punchTypeID + "' is not recognized"
                        + " as a `punchTypeID` by the program.\n"
                        + "Attatched Shift ID: " + this.id
                );
                
        } catch(Exception e) {
            System.err.println(e.toString());
        }
       
    }
    
    
    /* Alternatively titled method aliases for Feature Test compatability */
    
    public short getTerminalid() {
        return util.UnsignedByteHandler.getAsShort(getTerminalID());
    }
    
    public String getBadgeid() {
        return getBadgeID();
    }
    
    public Long getOriginaltimestamp() {
        return  getOriginalTimeStamp();
    }
    
    public short getPunchtypeid() {
        return util.UnsignedByteHandler.getAsShort(getPunchTypeID());
    }
    
}
