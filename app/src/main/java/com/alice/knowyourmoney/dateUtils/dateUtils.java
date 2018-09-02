package com.alice.knowyourmoney.dateUtils;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.alice.knowyourmoney.database.AccountComment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dateUtils {

    public void dateUtils(){

    }
    public float SumForThisWeek(ArrayList<AccountComment> weekRecords) {
        float sum = 0;
        for(int i = 0;  i < weekRecords.size(); i++){
            sum += weekRecords.get(i).getPrice();
        }
        return sum;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Map<String, Object>> GetWeekAndDay(LocalDate day, DayOfWeek dayOfWeek) {
        List<Map<String, Object>> week_list;
        week_list = new ArrayList<Map<String, Object>>();
        String[] dayOfWeekString = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        int[] loopDay = new int[7];
        int weekTemp = (dayOfWeek.getValue() == 7) ? 0: dayOfWeek.getValue();
        loopDay[weekTemp] = day.getDayOfMonth();
        Log.d("alice_debug", "dayOfWeek returns " + dayOfWeek.getValue());
        Log.d("alice_debug", "today is " + day);
        int dayTemp;
        int weekBefore = weekTemp;
        int number = 1;
        while (weekBefore-- > 0) {
            LocalDate ld = day.minusDays(number++);
            dayTemp = ld.getDayOfMonth();
            loopDay[weekBefore] = dayTemp;
        }

        int weekAfter = weekTemp + 1;
        number = 1;
        while (weekAfter < 7) {
            LocalDate ld = day.plusDays(number++);
            dayTemp = ld.getDayOfMonth();
            loopDay[weekAfter++] = dayTemp;
        }

        for (int loop = 0; loop < 7; loop++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("week", dayOfWeekString[loop]);
            map.put("day", loopDay[loop]); //Need confirm how can I get the correct month Day
            week_list.add(map);
        }
        return week_list;
    }
}
