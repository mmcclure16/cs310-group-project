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
    private final Short[] RECOGNIZED_PUNCHTYPE_IDS = {0, 1, 2};
    
    /* 
    * A  constant list of `punchtypeid`s that represent an "ending punch"
    * (ie, punching out or timing out), as defined by the database
    */
    private final Short[] TERMINATING_PUNCHTYPE_IDS = {0, 2};
    
    
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
         
        Instant OTS_Instant = Instant.ofEpochMilli(originalTimeStamp);
        
        LocalDate punchDate = OTS_Instant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime formattedPunchTime = OTS_Instant.atZone(ZoneId.systemDefault()).toLocalTime().withNano(0);
        
        int adjustedPunchTime_dayStartMinuteOffset = 0;
        
        // In seconds: Grace period for late-clockin and early-clockout
        int gracePeriodSeconds =  util.UnsignedByteHandler.getAsShort(s.getGracePeriod()) * 60;
        
        int intervalMinutes = util.UnsignedByteHandler.getAsShort(s.getInterval());
        int intervalSeconds = intervalMinutes * 60;

        // Is it an in-Punch?
        if (punchTypeID == 1) {
            
            // (Latest possible clock-in) Too late for lunch-stop?
            if ( formattedPunchTime.isAfter(s.getLunchStop().plusSeconds(gracePeriodSeconds)) ) {
                adjustmentType = "Shift Dock";
                adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                    formattedPunchTime.getHour(),
                    getNearestAdjustmentInterval(formattedPunchTime.getMinute(), intervalMinutes)
                );
            }
            
            // ... not late for lunch-stop
            else {
                
                // Not too late for lunch-stop - so is it during Lunch?
                if (formattedPunchTime.isAfter( s.getLunchStart() )) {
                    adjustmentType = "Lunch Stop";
                    adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                            s.getLunchStop().getHour(),
                            s.getLunchStop().getMinute()
                    );
                }
                
                // ...not during lunch - is it a late Shift-Start clockin? (After Shift Start + Grace Period)
                else if (formattedPunchTime.isAfter( s.getStart().plusSeconds(gracePeriodSeconds) )) {
                    
                    // is perfect interval?
                    if (0 == (formattedPunchTime.getMinute() % intervalMinutes)) {
                        adjustmentType = "None";
                        adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                            formattedPunchTime.getHour(),
                            formattedPunchTime.getMinute()
                        );
                    }
                    
                    else {
                        adjustmentType = "Shift Dock";
                        adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                            formattedPunchTime.getHour(),
                            getNearestAdjustmentInterval(formattedPunchTime.getMinute(), intervalMinutes)
                        );
                    }
                    
                }
                
                // ...not during lunch, not late Shift Start - must be on time or before Shift Start
                else  {
                    
                    // is before Shift-Start's interval?
                    if (formattedPunchTime.isBefore(s.getStart().minusMinutes(intervalMinutes))) {
                        
                        // is perfect interval?
                        if (0 == (formattedPunchTime.getMinute() % intervalMinutes)) {
                            adjustmentType = "None";
                            adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                                    formattedPunchTime.getHour(),
                                    formattedPunchTime.getMinute()
                            );
                        }
                        
                        // not a perfect interval, so change it into one
                        else {
                            adjustmentType = "Interval Round";
                            adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                                formattedPunchTime.getHour(),
                                getNearestAdjustmentInterval(formattedPunchTime.getMinute(), intervalMinutes)
                            );
                        }
                            
                    }
                    
                    // ... not before Shift-Start's interval - is it within Shift Start's interval?
                    else if (formattedPunchTime.getMinute() >= (s.getStart().getMinute())) {
                        adjustmentType = "Shift Start";
                        adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                            s.getStart().getHour(),
                            s.getStart().getMinute()
                        );
                    }
                    
                    /*
                     * Path-recap:
                     *
                     * 1. Not after shift-start + grace period passes
                     * 2. Not before Shift-Start's interval
                     * 3. Not within Shift-Start's interval
                     *
                     * Therefore, it's within the grace period
                    */
                    else {
                        adjustmentType = "Shift Grace";
                        adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                            s.getStart().getHour(),
                            s.getStart().getMinute()
                        );
                    }
                    
                }
                
            }
            
        }

        // Is it an out-Punch?
        else if ( (Arrays.asList(RECOGNIZED_PUNCHTYPE_IDS)).contains(util.UnsignedByteHandler.getAsShort(punchTypeID))) {
            
            // (Earliest possible clock out) Too early for lunch-start?
            if ( formattedPunchTime.isBefore(s.getLunchStart().minusSeconds(gracePeriodSeconds)) ) {
                adjustmentType = "Shift Dock";
                adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                    formattedPunchTime.getHour(),
                    getNearestAdjustmentInterval(formattedPunchTime.getMinute(), intervalMinutes)
                );
            }
            
            // ... not early for lunch-start
            else {
                
                // Not too early for lunch-stop - so is it during Lunch?
                if (formattedPunchTime.isBefore( s.getLunchStop() )) {
                    adjustmentType = "Lunch Start";
                    adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                            s.getLunchStart().getHour(),
                            s.getLunchStart().getMinute()
                    );
                }
                
                // Leave early and before grace period
                else if (formattedPunchTime.isBefore(s.getStop().minusSeconds(gracePeriodSeconds))) {
                    
                    // is perfect interval?
                    if (0 == (formattedPunchTime.getMinute() % intervalMinutes)) {
                        adjustmentType = "None";
                        adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                            formattedPunchTime.getHour(),
                            formattedPunchTime.getMinute()
                        );
                    }

                    else {
                        adjustmentType = "Shift Dock";
                        adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                            formattedPunchTime.getHour(),
                            getNearestAdjustmentInterval(formattedPunchTime.getMinute(), intervalMinutes)
                        );
                    }
                }
                
                //Clocking out late
                else {
                    
                    if (formattedPunchTime.isAfter(s.getStop().plusMinutes(intervalMinutes))){
                        
                        // is perfect interval?
                        if (0 == (formattedPunchTime.getMinute() % intervalMinutes)) {
                            adjustmentType = "None";
                            adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                                    formattedPunchTime.getHour(),
                                    formattedPunchTime.getMinute()
                            );
                        }
                        
                        // not a perfect interval, so change it into one
                        else {
                            adjustmentType = "Interval Round";
                            adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                                formattedPunchTime.getHour(),
                                getNearestAdjustmentInterval(formattedPunchTime.getMinute(), intervalMinutes)
                            );
                        }
                             
                    }
                    
                    else if (formattedPunchTime.getMinute() <= (s.getStop().getMinute())) {
                        adjustmentType = "Shift Stop";
                        adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                            s.getStop().getHour(),
                            s.getStop().getMinute()
                        );
                    }
                    
                    else {
                        adjustmentType = "Shift Grace";
                        adjustedPunchTime_dayStartMinuteOffset = hourMinuteAsMinutes(
                            s.getStop().getHour(),
                            s.getStop().getMinute()
                        );
                    }
                }
            }
        }
        
        // Is it a punch we don't recognize?
        else {
            return;
        }
        
        // Add time to start of day; convert to Unix timestamp
        adjustedTimeStamp = punchDate.atStartOfDay().plusMinutes(
                adjustedPunchTime_dayStartMinuteOffset
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
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
    
    private int hourMinuteAsMinutes(int hours, int minutes) {
        return hours*60 + minutes;
    }
    
    /* Error handling/building */
    
    /**
     * Throws and catches an Exception if we've not specified that we recognize 
     * this object's given `punchTypeID`
     */
    private void verifyPunchTypeID() {
        
        try {
            
            if (!(Arrays.asList(RECOGNIZED_PUNCHTYPE_IDS)).contains(util.UnsignedByteHandler.getAsShort(this.punchTypeID)))
                throw new Exception("Invalid or unknown `punchTypeID` specn mified:"
                        + " Value '" + this.punchTypeID + "' is not recognized"
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
