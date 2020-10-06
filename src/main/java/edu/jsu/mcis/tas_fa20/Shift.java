package edu.jsu.mcis.tas_fa20;

import java.time.LocalTime;
import java.time.temporal.*;


public class Shift {
    private int id;
    private String description;
    private LocalTime start;
    private LocalTime stop;
    private int interval;
    private int graceperiod;
    private int dock;
    private int shiftduration;
    private LocalTime lunchstart;
    private LocalTime lunchstop;
    private int lunchduration;
    private int lunchdeduct;
    
    public Shift(int id, String description, LocalTime start, LocalTime stop, int interval, int graceperiod, int dock, LocalTime lunchstart, LocalTime lunchstop, int lunchdeduct){
        this.id = id;
        this.description = description;
        this.start = start;
        this.stop = stop;
        this.interval = interval;
        this.graceperiod = graceperiod;
        this.dock = dock;
        this.shiftduration = ((int)(ChronoUnit.MINUTES.between(start, stop)));
        this.lunchstart = lunchstart;
        this.lunchstop = lunchstop;
        this.lunchduration = ((int)(ChronoUnit.MINUTES.between(lunchstart, lunchstop)));
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
    
    public long getLunchDuration(){
        return lunchduration;
    }
    
    public int getLunchDeduct(){
        return lunchdeduct;
    }
    
    public void setID(int id){
        this.id = id;
    }
    
    public void setDescription(String description){
        this.description = description;
    }
    
    public void setStart(LocalTime start){
        this.start = start;
    }
    
    public void setStop(LocalTime stop){
        this.stop = stop;
    }
    
    public void setInterval(int interval){
        this.interval = interval;
    }
    
    public void setGracePeriod(int graceperiod){
        this.graceperiod = graceperiod;
    }
    
    public void setDock(int dock){
        this.dock = dock;
    }
    
    public void setShiftDuration(int shiftduration){
        this.shiftduration = shiftduration;
    }
    
    public void setLunchStart(LocalTime lunchstart){
        this.lunchstart = lunchstart;
    }
    
    public void setLunchStop(LocalTime lunchstop){
        this.lunchstop = lunchstop;
    }
    
    public void setLunchDuration(int lunchduration){
        this.lunchduration = lunchduration;
    }
    
    public void setLunchDeduct(int lunchdeduct){
        this.lunchdeduct = lunchdeduct;
    }
    
    @Override
    public String toString(){
        StringBuilder shift = new StringBuilder();
        
        shift.append(description).append(": ").append(start).append(" - ").append(stop).append(" (").append(shiftduration).append(" minutes);");
        shift.append(" Lunch: ").append(lunchstart).append(" - ").append(lunchstop).append(" (").append(lunchduration).append(" minutes)");
        return (shift.toString());
    }
}
