package tas_fa20;

import java.util.*;

import static java.time.temporal.ChronoUnit.MINUTES;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.*;


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
            
    public static String getPunchListAsJSON(ArrayList<Punch> dailyPunchList){
        
        ArrayList<HashMap<String, String>> jsonData = new ArrayList<>();
        
        for(Punch punch : dailyPunchList) {
            
            HashMap<String, String> punchData = new HashMap<>();
            
            punchData.put("id", String.valueOf(punch.getID()));
            punchData.put("badgeid", String.valueOf(punch.getBadgeID()));
            punchData.put("terminalid", String.valueOf(punch.getTerminalID()));
            punchData.put("punchtypeid", String.valueOf(punch.getPunchTypeID()));
            punchData.put("punchdata", String.valueOf(punch.getAdjustmentType()));
            punchData.put("originaltimestamp", String.valueOf(punch.getOriginalTimeStamp()));
            punchData.put("adjustedtimestamp", String.valueOf(punch.getAdjustedTimeStamp()));
            
            jsonData.add(punchData);
            
        }
        
        String json = JSONValue.toJSONString(jsonData);
        
        return json;
    }
    
    public static double calculateAbsenteeism(ArrayList<Punch> punchList, Shift shift) {
        return 0d;
    }
}
