package org.insightcentre.coach.alarms;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.insightcentre.coach.notify.WeeklyNotificationService;

public class WeeklyAlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent weeklyNotificationIntent = new Intent(context, WeeklyNotificationService.class);
        // Start the service, keeping the device awake while the service is launching
        startWakefulService(context, weeklyNotificationIntent);
    }
}
