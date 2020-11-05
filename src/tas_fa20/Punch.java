package tas_fa20;

import java.util.*;
import java.time.*;
import java.text.SimpleDateFormat;

import static java.time.temporal.ChronoUnit.SECONDS;

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
    
    
    private final int[] RECOGNIZED_PUNCHTYPE_IDS = {0, 1, 2};
    
    /* 
    * A  constant list of `punchtypeid`s that represent an "ending punch"
    * (ie, punching out or timing out), as defined by the database
    */
    public static final int[] TERMINATING_PUNCHTYPE_IDS = {0, 2};
    
    
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
        
        // reset on each attempt
        adjustedTimeStamp = null;
         
        Instant OTS_Instant = Instant.ofEpochMilli(originalTimeStamp);
        
        LocalDate punchDate = OTS_Instant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime formattedPunchTime = OTS_Instant.atZone(ZoneId.systemDefault()).toLocalTime().withNano(0);
        
        int gracePeriodMinutes = util.UnsignedByteHandler.getAsShort(s.getGracePeriod());
        int intervalMinutes = util.UnsignedByteHandler.getAsShort(s.getInterval());
        
        
        // Is it a Weekend?
        if (punchDate.getDayOfWeek() == DayOfWeek.SATURDAY || punchDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            
            // is perfect interval?
            if (isPerfectInterval(formattedPunchTime.getMinute(), intervalMinutes)) {
                adjustmentType = "None";
            }
                    
            else adjustmentType = "Interval Round";
                    
        }
        
        // no compatible ruleset exists for this (Canvas-defined) label
        else if (false) adjustmentType = "Shift Grace";
        
        // It must be a weekday
        else {
            
            /* Note: "Shift Grace" is roped in with "Shift Start"/"Shift Stop"
             * within the tests, so its implementation as defined in the
             * "Feature 3" module is absent (in other words, grace period is
             * counted, but it does not have a unique label as listed on the module)
            */
           
            switch (punchTypeID) {
           
                case 1: { // in-punch
                    
                    // clock in sometime after lunch started?
                    if (formattedPunchTime.isAfter( s.getLunchStart() )) {
                        adjustmentType = "Lunch Stop";
                    }
                    
                    // clock in during Shift Start interval-radius?
                    else if (intervalMinutes*60 >= Math.abs(SECONDS.between(formattedPunchTime, s.getStart()))) {

                        // clock in within grace period?
                        if (formattedPunchTime.compareTo(s.getStart().plusMinutes(gracePeriodMinutes)) <= 0) {
                            adjustmentType = "Shift Start";
                            adjustedTimeStamp = dateAndTimeToUnixTimestamp(punchDate, s.getStart());
                        }

                        else adjustmentType = "Shift Dock";
                        
                    }

                    else if (isPerfectInterval(formattedPunchTime.getMinute(), intervalMinutes)) {
                        adjustmentType = "None";
                    }
                    
                    else adjustmentType = "Interval Round";
                    
                    break;
                }

                case 0: { // out-punch
                    
                    // clock out sometime before lunch has ended?
                    if (formattedPunchTime.isBefore( s.getLunchStop() )) {
                        adjustmentType = "Lunch Start";
                    }
                    
                    // clock in during Shift Stop interval-radius?
                    else if (intervalMinutes*60 >= Math.abs(SECONDS.between(formattedPunchTime, s.getStop()))) {
                        
                        // clock out within grace period?
                        if (formattedPunchTime.compareTo(s.getStop().minusMinutes(gracePeriodMinutes)) >= 0) {
                            adjustmentType = "Shift Stop";
                            adjustedTimeStamp = dateAndTimeToUnixTimestamp(punchDate, s.getStop());
                        }
                        
                        else adjustmentType = "Shift Dock";
                        
                    }

                    else if (isPerfectInterval(formattedPunchTime.getMinute(), intervalMinutes)) {
                        adjustmentType = "None";
                    }

                    else adjustmentType = "Interval Round";
                    
                    break;
                }
            
                default: // unhandled punchTypeID
                    verifyPunchTypeID();
                    return;
            }
        
        }
        
        if (adjustedTimeStamp == null) {
            adjustedTimeStamp = dateAndMinutesToUnixTimestamp(
                    punchDate,
                    getNearestAdjustmentInterval(
                        formattedPunchTime,
                        intervalMinutes
                    )
            );
        }
        
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
    
    private Long dateAndTimeToUnixTimestamp(LocalDate date, LocalTime time) {
        return date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    private Long dateAndMinutesToUnixTimestamp(LocalDate date, int minutes) {
        return dateAndTimeToUnixTimestamp(date, LocalTime.of(0,0).plusMinutes(minutes));
    }
    
    private int getNearestAdjustmentInterval(LocalTime currentTime, int intervalMinutes) {
        
        return hourMinuteAsMinutes(
                currentTime.getHour(),
                (int)Math.round( (currentTime.getMinute() + (currentTime.getSecond()/60f)) / intervalMinutes ) * intervalMinutes
        );
    }
    
    private int hourMinuteAsMinutes(int hours, int minutes) {
        return hours*60 + minutes;
    }
    
    private boolean isPerfectInterval(int minute, int intervalMinutes) {
        return 0 == (minute % intervalMinutes);
    }
    
    /* Error handling/building */
    
    /**
     * Throws and catches an Exception if we've not specified that we recognize 
     * this object's given `punchTypeID`
     */
    private void verifyPunchTypeID() {
        
        try {
            
            if (!(Arrays.asList(RECOGNIZED_PUNCHTYPE_IDS)).contains(util.UnsignedByteHandler.getAsShort(this.punchTypeID)))
                throw new Exception("Invalid or unknown `punchTypeID` specified:"
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