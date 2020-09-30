package edu.jsu.mcis.tas_fa20;

import java.time.LocalTime;


public class Shift {
    private int id;
    private String description;
    private LocalTime start;
    private LocalTime stop;
    private int interval;
    private int graceperiod;
    private int dock;
    private LocalTime lunchstart;
    private LocalTime lunchstop;
    private int lunchduration;
    private int lunchdeduct;
    
    public Shift(int id, String description, LocalTime start, LocalTime stop, int interval, int graceperiod, int dock, LocalTime lunchstart, LocalTime lunchstop, int lunchduration, int lunchdeduct){
        this.id = id;
        this.description = description;
        this.start = start;
        this.stop = stop;
        this.interval = interval;
        this.graceperiod = graceperiod;
        this.dock = dock;
        this.lunchstart = lunchstart;
        this.lunchstop = lunchstop;
        this.lunchduration = lunchduration;
        this.lunchdeduct = lunchdeduct;
    }
    
    public int getID(){
        return id;
    }
    
    public String getDescription(){
        return description;
    }
    
    public LocalTime getStart(){
        return start;
    }
    
    public LocalTime getStop(){
        return stop;
    }
    
    public int getInterval(){
        return interval;
    }
    
    public int getGracePeriod(){
        return graceperiod;
    }
    
    public int getDock(){
        return dock;
    }
    
    public LocalTime getLunchStart(){
        return lunchstart;
    }
    
    public LocalTime getLunchStop(){
        return lunchstop;
    }
    
    public int getLunchDuration(){
        return lunchduration;
    }
    
    public int getLunchDeduct(){
        return lunchdeduct;
    }
}
