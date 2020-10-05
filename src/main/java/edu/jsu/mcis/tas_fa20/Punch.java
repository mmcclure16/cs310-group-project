package edu.jsu.mcis.tas_fa20;


public class Punch {
    private int id;
    private int terminalid;
    private String badgeid;
    private Long originaltimestamp;
    private int punchtypeid;
    private String adjustmenttype;
    
    public Punch(int id, int terminalid, String badgeid, Long originaltimestamp, int punchtypeid, String adjustmenttype){
        this.id = id;
        this.terminalid = terminalid;
        this.badgeid = badgeid;
        this.originaltimestamp = originaltimestamp;
        this.punchtypeid = punchtypeid;
        this.adjustmenttype = adjustmenttype;
    }
    
    public int getID(){
        return id;
    }
    
    public int getTerminalID(){
        return terminalid;
    }
    
    public String getBadgeID(){
        return badgeid;
    }
    
    public Long getOriginalTimeStamp(){
        return originaltimestamp;
    }
    
    public int getPunchTypeID(){
        return punchtypeid;
    }
    
    public String getAdjustmentType(){
        return adjustmenttype;
    }
    
    public String printOriginalTimestamp(){
        return null;
    }
}
