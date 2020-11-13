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
    LunchStatus adjustedLunchStatus;
    
    public abstract class adjustmentTitles {
        public static final String START = "Shift Start";
        public static final String STOP = "Shift Stop";
        public static final String LUNCH_START = "Lunch Start";
        public static final String LUNCH_STOP = "Lunch Stop";
        public static final String DOCK = "Shift Dock";
        public static final String GRACE = "Shift Grace";
        public static final String ROUND = "Interval Round";
        public static final String NONE = "None";
    }
    
    // index 0: lower bounds -- index 1: upper bounds
    private static final int[] RECOGNIZED_PUNCHTYPE_ID_RANGE = {0, 2};
    
    enum LunchStatus {
        OCCURRING,
        OCCURRED,
        NOT_OCCURRING,
        INAPPLICABLE
    };
    
    
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
    
    
    /* Master Constructor/Support methods */
    
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
        
        // to be overridden if lunch occurs or if lunch doesn't exist
        adjustedLunchStatus = LunchStatus.NOT_OCCURRING;
        
        // Is it a Weekend?
        if (punchDate.getDayOfWeek() == DayOfWeek.SATURDAY || punchDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            
            // inapplicable - weekends have no lunch schedule
            adjustedLunchStatus = LunchStatus.INAPPLICABLE;
            
            if (isPerfectInterval(formattedPunchTime.getMinute(), intervalMinutes)) {
                adjustmentType = adjustmentTitles.NONE;
            }
                    
            else adjustmentType = adjustmentTitles.ROUND;
                    
        }
        
        // no compatible ruleset exists for this (Canvas-defined) label
        else if (false) adjustmentType = adjustmentTitles.GRACE;
        
        // It must be a weekday
        else {
           
            switch (punchTypeID) {
           
                case 1: { // in-punch
                    
                    if (formattedPunchTime.isAfter( s.getLunchStart() )) {
                        adjustmentType = adjustmentTitles.LUNCH_STOP;
                        adjustedLunchStatus = LunchStatus.OCCURRED;
                    }
                    
                    // Within start interval-radius?
                    else if (intervalMinutes*60 >= Math.abs(SECONDS.between(formattedPunchTime, s.getStart()))) {

                        if (formattedPunchTime.compareTo(s.getStart().plusMinutes(gracePeriodMinutes)) <= 0) {
                            adjustmentType = adjustmentTitles.START;
                            adjustedTimeStamp = dateAndTimeToUnixTimestamp(punchDate, s.getStart());
                        }

                        else adjustmentType = adjustmentTitles.DOCK;
                        
                    }

                    else if (isPerfectInterval(formattedPunchTime.getMinute(), intervalMinutes)) {
                        adjustmentType = adjustmentTitles.NONE;
                    }
                    
                    else adjustmentType = adjustmentTitles.ROUND;
                    
                    break;
                    
                }

                case 0: { // out-punch
                    
                    if (formattedPunchTime.isBefore( s.getLunchStop() )) {
                        adjustmentType = adjustmentTitles.LUNCH_START;
                        adjustedLunchStatus = LunchStatus.OCCURRING;
                    }
                    
                    // Within stop interval-radius?
                    else if (intervalMinutes*60 >= Math.abs(SECONDS.between(formattedPunchTime, s.getStop()))) {
                        
                        if (formattedPunchTime.compareTo(s.getStop().minusMinutes(gracePeriodMinutes)) >= 0) {
                            adjustmentType = adjustmentTitles.STOP;
                            adjustedTimeStamp = dateAndTimeToUnixTimestamp(punchDate, s.getStop());
                        }
                        
                        else adjustmentType = adjustmentTitles.DOCK;
                        
                    }

                    else if (isPerfectInterval(formattedPunchTime.getMinute(), intervalMinutes)) {
                        adjustmentType = adjustmentTitles.NONE;
                    }

                    else adjustmentType = adjustmentTitles.ROUND;
                    
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
        
        if (verify_AdjustedTimeIsSet()) return adjustmentType;
        
        else return null;
        
    }
    
    public Long getAdjustedTimeStamp() {
        
        if (verify_AdjustedTimeIsSet()) return adjustedTimeStamp;
        
        else return null;
        
    }
    
    /**
     * Determines if whether the post-adjustment timestamp occurs outside of a
     * shift's scheduled lunch start-end duration or not
     * @return TRUE if it's outside of post-adjustment shift's scheduled lunch;
     * FALSE otherwise
     */
    public boolean isAdjustedPunchOutsideShiftLunch() {
        
        verify_AdjustedTimeIsSet();
        return (adjustedLunchStatus == LunchStatus.NOT_OCCURRING);
        
    }
    
    
    /* Formatted-Timestamp printing */
    
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

        if (verify_AdjustedTimeIsSet()) {
            return printableTimestamp(adjustedTimeStamp)
                + " (" + adjustmentType + ")";
        }

        else return null;
    }
    
    
    /* Time Conversions/Calculations */
    
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
            
            if (
                    !(util.UnsignedByteHandler.getAsShort(punchTypeID) >= RECOGNIZED_PUNCHTYPE_ID_RANGE[0]
                    && util.UnsignedByteHandler.getAsShort(punchTypeID) <= RECOGNIZED_PUNCHTYPE_ID_RANGE[1])
                ) {
                throw new Exception("Invalid or unknown `punchTypeID` specified:"
                        + " Value '" + punchTypeID + "' is not recognized"
                        + " as a `punchTypeID` by the program.\n"
                        + "Attatched Punch ID: " + this.id
                );
            }
                
        } catch(Exception e) {
            System.err.println(e.toString());
        }
       
    }
    
    /**
     * Determines if adjusted timestamp exists
     */
    private boolean adjustedTimeIsSet() {
        return (adjustedTimeStamp != null);
    }
    
    /**
     * Determines if adjusted timestamp exists; Throws and catches an Exception
     * if `adjust(Shift)` has not yet created an adjusted timestamp
     */
    private boolean verify_AdjustedTimeIsSet() {
        
        boolean res = adjustedTimeIsSet();
        
        try {

            if (!res) {
                throw new Exception("Cannot provide data related to 'adjusted timestamp'"
                    + " until it has been generated by a successful call to"
                    + " `adjust(Shift)`. Data passed may be incorrect.");
            }
            
        } catch(Exception e) {
            System.err.println(e.toString());
        }
        
        return res;
        
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