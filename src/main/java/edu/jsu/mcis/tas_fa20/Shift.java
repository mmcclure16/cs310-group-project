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
    private int lunchdeduct;
    
    public Shift(int id, String description, LocalTime start, LocalTime stop, int interval, int graceperiod, int dock, LocalTime lunchstart, LocalTime lunchstop, int lunchdeduct){
        this.id = id;
        this.description = description;
        this.start = start;
        this.stop = stop;
        this.interval = interval;
        this.graceperiod = graceperiod;
        this.dock = dock;
        this.lunchstart = lunchstart;
        this.lunchstop = lunchstop;
        this.lunchdeduct = lunchdeduct;
    }
}
