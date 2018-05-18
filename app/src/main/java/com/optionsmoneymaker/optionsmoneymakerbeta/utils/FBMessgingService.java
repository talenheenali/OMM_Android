package com.optionsmoneymaker.optionsmoneymakerbeta.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optionsmoneymaker.optionsmoneymakerbeta.MessageDetailActivity;
import com.optionsmoneymaker.optionsmoneymakerbeta.OptionMoneyMaker;
import com.optionsmoneymaker.optionsmoneymakerbeta.R;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.MessageData;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.NotificationResult;
import com.optionsmoneymaker.optionsmoneymakerbeta.rest.RestClient;
import com.optionsmoneymaker.optionsmoneymakerbeta.sqlitedb.DatabaseHandler;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.optionsmoneymaker.optionsmoneymakerbeta.OptionMoneyMaker.isAppIsInBackground;

/**
 * Created by Ajinkya W on 4/15/2018.
 */

public class FBMessgingService extends FirebaseMessagingService {

    String TAG = "FCMDEBUG";
    MessageData messageData;
    SessionManager session;
    ConnectionDetector cd;
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

                    if ("apptest".equalsIgnoreCase(remoteMessage.getData().get("notification_product_name"))) {
                        messageData.setProductName("App Test");
                    } else {
                        messageData.setProductName(remoteMessage.getData().get("notification_product_name"));
                    }

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

        if (null == cd) {
            cd = new ConnectionDetector(getApplicationContext());
        }

        if (null == session) {
            session = new SessionManager(getApplicationContext());
        }

        Log.v("notifResponse", "storing bg notif into db");
        new DatabaseHandler().storeNewNotif(content);

        if (cd.isConnectingToInternet()) {

            RestClient.getMoneyMaker().messageRead(content.getId(), session.getUserID(),
                    content.getDateTime(), new Callback<NotificationResult>() {
                        @Override
                        public void success(NotificationResult result, Response response) {
                            if ((int) result.getStatus() == 1) {

                                MessageData data1 = new MessageData();
                                Log.v("Notifnew", "at 139 in app class data - " + result.getData());
                                Log.v("Notifnew", "at 140 in app class response reason - " + response.getReason());
                                Log.v("Notifnew", "at 141 in app class response body - " + response.getBody());
                                Log.v("Notifnew", "at 142 in app class headers - " + response.getHeaders());
                                Log.v("Notifnew", "at 143 in app class status - " + response.getStatus());

                            } else if ((int) result.getStatus() == 0) {
                                Log.v("Notifnew", "at 158 in app class status code zero so didnt read anything");
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.v("Notifnew", "API failure " + error);
                        }
                    });

            // The following can be used to open an Activity of your choice.
//            Intent intent = new Intent(getApplicationContext(), MessageDetailActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(Constants.ID, content.getId());
//            intent.putExtra(Constants.TYPE, "notification");
//            startActivity(intent);

        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(getApplicationContext(), MessageDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.ID, content.getId());
        intent.putExtra(Constants.TYPE, "notification");
        int id = OptionMoneyMaker.getInstance().getCounter();

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String channel_id = "OMM_NOTIFICATIONS";

        Notification notification = new NotificationCompat.Builder(this, "OMM")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(content.getTitle())
                .setContentText(Html.fromHtml(content.getMessage()))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setChannelId(channel_id)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);


    }


}
