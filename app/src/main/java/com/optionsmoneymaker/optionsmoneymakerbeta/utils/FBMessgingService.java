package com.optionsmoneymaker.optionsmoneymakerbeta.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optionsmoneymaker.optionsmoneymakerbeta.OptionMoneyMaker;
import com.optionsmoneymaker.optionsmoneymakerbeta.R;

/**
 * Created by Ajinkya W on 4/15/2018.
 */

public class FBMessgingService extends FirebaseMessagingService {

    String TAG = "FCMDEBUG";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.v(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            showNotification(remoteMessage.getData().toString());

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(remoteMessage.getNotification().getBody());
        }


    }

    public void showNotification(String content) {

        Log.v("notifResponse", "giving a notification for isLiveALL for mainACt");

        Resources r = getResources();
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(("New Notification Received"))
                .setContentText(content)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int id = OptionMoneyMaker.getInstance().getCounter();
        notificationManager.notify(id, notification);

    }


}
