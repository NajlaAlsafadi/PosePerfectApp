package com.example.poseperfect.homeNav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.poseperfect.R;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "yogaReminderChannel")
                .setSmallIcon(R.drawable.ic_exercises)
                .setContentTitle("Yoga Practice Reminder")
                .setContentText("Time for your yoga practice!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);


        notificationManager.notify(200, builder.build());
    }
}

