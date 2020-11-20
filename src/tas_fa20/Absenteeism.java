package tas_fa20;

import java.time.*;
import java.time.format.DateTimeFormatter; 

import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.previous;

public class Absenteeism {
    
    private String badgeID;
    private double percentage;
    
    private LocalDate payPeriodDate;
    
    private static final DateTimeFormatter timeStampFormat  = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    
    public Absenteeism(String badgeID, long timeStamp, double percentage) {
        
        
        /* Apply direct values */
        
        this.badgeID = badgeID;
        this.percentage = percentage;
        
        
        /* Set pay period start */
        
        Instant TS_Instant = Instant.ofEpochMilli(timeStamp);
        payPeriodDate = TS_Instant.atZone(ZoneId.systemDefault()).toLocalDate();
        //.with(previous(SUNDAY));
        if (payPeriodDate.getDayOfWeek() != SUNDAY) {
            payPeriodDate = payPeriodDate.with(previous(SUNDAY));
        }
        
    }
    
    @Override
    public String toString() {
        return ("#" + badgeID + " (Pay Period Starting " + timeStampFormat.format(payPeriodDate) + ") : "
                + percentage + "%");
    }
    
}
