package com.brenda.agenda;

import android.app.*;
import android.content.*;
import android.os.Build;
import org.json.JSONObject;

final class Alarms {
    static PendingIntent intent(Context c, JSONObject item) {
        return PendingIntent.getBroadcast(c,item.optInt("id"),new Intent(c,AlarmReceiver.class)
            .putExtra("id",item.optInt("id")),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static void cancel(Context c, JSONObject item) { c.getSystemService(AlarmManager.class).cancel(intent(c,item)); }
    static boolean schedule(Context c,JSONObject item) {
        AlarmManager manager=c.getSystemService(AlarmManager.class);
        if(Build.VERSION.SDK_INT>=31 && !manager.canScheduleExactAlarms()) return false;
        try {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,item.optLong("time"),intent(c,item));
            return true;
        } catch(SecurityException e) { return false; }
    }
}
