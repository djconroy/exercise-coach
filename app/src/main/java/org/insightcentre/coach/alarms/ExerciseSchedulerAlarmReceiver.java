package org.insightcentre.coach.alarms;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.insightcentre.coach.ChooseNextLevelService;

public class ExerciseSchedulerAlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent chooseNextLevelIntent = new Intent(context, ChooseNextLevelService.class);
        // Start the service, keeping the device awake while the service is launching
        startWakefulService(context, chooseNextLevelIntent);
    }
}
