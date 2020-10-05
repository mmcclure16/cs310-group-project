package edu.jsu.mcis;

import java.util.*;

public class Punch {
    
    private int id;
    private byte terminalID;
    private String badgeID;
    private Long originalTimeStamp;
    private byte punchTypeID;
    
    private String adjustmentType;
    
    public Punch(String badgeID, Map<String, Byte> byteParams) {
        this(
                0,
                byteParams.get("terminalID"),
                badgeID,
                null,
                byteParams.get("punchTypeID")
        );
    }
    
    public Punch(int id, String badgeID, Map<String, Byte> byteParams, Long originalTimeStamp) {
        this(
                id,
                byteParams.get("terminalID"),
                badgeID,
                originalTimeStamp,
                byteParams.get("punchTypeID")
        );
    }
    
    private Punch(int id, byte terminalID, String badgeID, Long originalTimeStamp, byte punchTypeID) {
        this.id = id;
        this.terminalID = terminalID;
        this.badgeID = badgeID;
        this.originalTimeStamp = originalTimeStamp;
        this.punchTypeID = punchTypeID;
        
        adjustmentType = null;
    }
    
    public int getID(){
        return id;
    }
    
    public int getTerminalID(){
        return terminalID;
    }
    
    public String getBadgeID(){
        return badgeID;
    }
    
    public Long getOriginalTimeStamp(){
        return originalTimeStamp;
    }
    
    public int getPunchTypeID(){
        return punchTypeID;
    }
    
    public String getAdjustmentType(){
        return adjustmentType;
    }
    
    public String printOriginalTimestamp(){
        return null;
    }
    
}
