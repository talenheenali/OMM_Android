package com.optionsmoneymaker.optionsmoneymakerbeta.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optionsmoneymaker.optionsmoneymakerbeta.OptionMoneyMaker;
import com.optionsmoneymaker.optionsmoneymakerbeta.R;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.MessageData;
import com.optionsmoneymaker.optionsmoneymakerbeta.sqlitedb.DatabaseHandler;

import static com.optionsmoneymaker.optionsmoneymakerbeta.OptionMoneyMaker.isAppIsInBackground;

/**
 * Created by Ajinkya W on 4/15/2018.
 */

public class FBMessgingService extends FirebaseMessagingService {

    String TAG = "FCMDEBUG";
    MessageData messageData;
    private int badgeCount = 0;

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.v(TAG, "From: " + remoteMessage.getFrom());
        Log.v(TAG, "RAW Content toString : " + remoteMessage.getData().toString());
        Log.v(TAG, "RAW notification_id : " + remoteMessage.getData().get("notification_id"));
        Log.v(TAG, "RAW notification_title : " + remoteMessage.getData().get("notification_title"));
        Log.v(TAG, "RAW notification_message : " + remoteMessage.getData().get("notification_message"));
        Log.v(TAG, "RAW notification_product_name : " + remoteMessage.getData().get("notification_product_name"));
        Log.v(TAG, "RAW notification_sent_time : " + remoteMessage.getData().get("notification_sent_time"));
        Log.v(TAG, "RAW notification_isread : " + remoteMessage.getData().get("notification_isread"));

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                try {

                    messageData = new MessageData();
                    messageData.setId(remoteMessage.getData().get("notification_id"));
                    messageData.setTitle(remoteMessage.getData().get("notification_title"));
                    messageData.setMessage(remoteMessage.getData().get("notification_message"));
                    messageData.setProductName(remoteMessage.getData().get("notification_product_name"));
                    messageData.setDateTime(remoteMessage.getData().get("notification_sent_time"));
                    messageData.setIsRead(remoteMessage.getData().get("notification_isread"));

                    if (!isAppIsInBackground(getApplicationContext())) {

                        Log.v("ajtrial", "app is in foreground");
                        showNewMessageArrived(messageData);
                        Log.v("ajtrial", "notif data : " + remoteMessage.getData().toString());

                    } else {

                        Log.v("ajtrial", "app is in background");
                        showBadge();
                        showNotification(messageData);
                        //this executes when notification is received ( wokred : bg )

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


//        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//            showNotification(remoteMessage.getNotification().getBody());
//        }


    }

    private void showBadge() {

        badgeCount++;
        //  ShortcutBadger.applyCount(getApplicationContext(), badgeCount);

    }

    public void showNewMessageArrived(MessageData notifData) {

        Log.v("ajtrial", "at 254 in app class shownewmessage arrived hit");
        //this executes when notification is received ( worked : fg only )
        DeliveryInterface deliveryInterface = (DeliveryInterface) OptionMoneyMaker.getHomeFragmentContext();
        deliveryInterface.getUpdatedPayload(notifData);

    }

    public void showNotification(MessageData content) {

        Log.v("notifResponse", "giving a notification for isLiveALL for mainACt");
        new DatabaseHandler().storeNewNotif(content);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(("New Notification Received"))
                .setContentText(content.getMessage())
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int id = OptionMoneyMaker.getInstance().getCounter();
        notificationManager.notify(id, notification);

    }


}
