package edu.jsu.mcis.tas_fa20;


public class Punch {
    private String id;
    private String terminalid;
    private String badgeid;
    private Long originaltimestamp;
    private String punchtypeid;
    private String adjustmenttype;
    
    public Punch(String id, String terminalid, String badgeid, Long originaltimestamp, String punchtypeid, String adjustmenttype){
        this.id = id;
        this.terminalid = terminalid;
        this.badgeid = badgeid;
        this.originaltimestamp = originaltimestamp;
        this.punchtypeid = punchtypeid;
        this.adjustmenttype = adjustmenttype;
    }
    
    
}
