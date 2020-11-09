package tas_fa20;

import java.util.*;

import static java.time.temporal.ChronoUnit.MINUTES;


public class TASLogic {
    
    public static int calculateTotalMinutes(ArrayList<Punch> dailyPunchList, Shift shift) {
        
        int minsWorked = 0;
        boolean hasNotHadScheduledLunch = true;
        
        for (int i = 0; i+1 < dailyPunchList.size(); i += 2) {
            minsWorked += (
                    dailyPunchList.get(i+1).getAdjustedTimeStamp()
                    - dailyPunchList.get(i).getAdjustedTimeStamp()
                    ) / 60000;
            
            if (hasNotHadScheduledLunch) hasNotHadScheduledLunch = dailyPunchList.get(i).isAdjustedPunchOutsideShiftLunch();
        }
        
        if (hasNotHadScheduledLunch && (minsWorked > shift.getLunchDeduct())) {
            minsWorked -= MINUTES.between(shift.getLunchStart(), shift.getLunchStop());
        }
        
        return minsWorked;
        
    }
            
}
