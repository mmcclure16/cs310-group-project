package tas_fa20;

import java.util.*;
import java.time.LocalTime;

public class Shift {
    
    private byte id;
    private String description;
    private LocalTime start;
    private LocalTime stop;
    private byte interval;
    private byte gracePeriod;
    private byte dock;
    private LocalTime lunchStart;
    private LocalTime lunchStop;
    private short lunchDeduct;
    
    private long shiftDuration;
    private long lunchDuration;
    
    public Shift (Map<String, Byte> byteParams, Map <String, LocalTime> localTimeParams, String description, short lunchDeduct) {
        this(
                byteParams.get("id"),
                description,
                localTimeParams.get("start"),
                localTimeParams.get("stop"),
                byteParams.get("interval"),
                byteParams.get("gracePeriod"),
                byteParams.get("dock"),
                localTimeParams.get("lunchStart"),
                localTimeParams.get("lunchStop"),
                lunchDeduct
        );
    }
    
    private Shift(byte id, String description, LocalTime start, LocalTime stop, byte interval, byte gracePeriod, byte dock, LocalTime lunchStart, LocalTime lunchStop, short lonchDeduct) {
        this.id = id;
        this.description = description;
        this.start = start;
        this.stop = stop;
        this.interval = interval;
        this.gracePeriod = gracePeriod;
        this.dock = dock;
        this.lunchStart = lunchStart;
        this.lunchStop = lunchStop;
        this.lunchDeduct = lunchDeduct;
        
        // TO-DO: for Feature 4
        shiftDuration = -1;
        lunchDuration = -1;
    }
    
    public byte getID(){
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
    
    public byte getInterval(){
        return interval;
    }
    
    public byte getGracePeriod(){
        return gracePeriod;
    }
    
    public byte getDock(){
        return dock;
    }
    
    public LocalTime getLunchStart(){
        return lunchStart;
    }
    
    public LocalTime getLunchStop(){
        return lunchStop;
    }
    
    public long getLunchDuration(){
        return lunchDuration;
    }
    
    public short getLunchDeduct(){
        return lunchDeduct;
    }
    
    public void setID(byte id){
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
    
    public void setInterval(byte interval){
        this.interval = interval;
    }
    
    public void setGracePeriod(byte graceperiod){
        this.gracePeriod = graceperiod;
    }
    
    public void setDock(byte dock){
        this.dock = dock;
    }
    
    public void setShiftDuration(long shiftDuration){
        this.shiftDuration = shiftDuration;
    }
    
    public void setLunchStart(LocalTime lunchStart){
        this.lunchStart = lunchStart;
    }
    
    public void setLunchStop(LocalTime lunchStop){
        this.lunchStop = lunchStop;
    }
    
    public void setLunchDuration(long lunchDuration){
        this.lunchDuration = lunchDuration;
    }
    
    public void setLunchDeduct(short lunchDeduct){
        this.lunchDeduct = lunchDeduct;
    }
    
    @Override
    public String toString(){
        StringBuilder shift = new StringBuilder();
        
        shift.append(description).append(": ").append(start).append(" - ").append(stop).append(" (").append(shiftDuration).append(" minutes);");
        shift.append(" Lunch: ").append(lunchStart).append(" - ").append(lunchStop).append(" (").append(lunchDuration).append(" minutes)");
        return (shift.toString());
    }
}
