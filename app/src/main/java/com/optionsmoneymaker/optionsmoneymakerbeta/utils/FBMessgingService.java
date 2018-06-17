package com.optionsmoneymaker.optionsmoneymakerbeta.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.text.Spanned;
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
    String channel_id = "OMM_NOTIFICATIONS";
    MessageData globalContent;
    String tempMsg;
    PendingIntent pendingIntent;
    NotificationManager notificationManager;
    NotificationChannelGroup channelGroup;
    //Notification notification;
    String notifGroup = "OMM_GROUP";
    NotificationChannel mChannel;
    boolean isShowing;
    NotificationCompat.InboxStyle inboxStyle;
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

                    createNotifInits();

                    if (!isAppIsInBackground(getApplicationContext())) {

                        Log.v("ajtrial", "app is in foreground");
                        showNewMessageArrived(messageData);
                        Log.v("ajtrial", "notif data : " + remoteMessage.getData().toString());

                    } else {

                        Log.v("ajtrial", "app is in background");
                        showBadge();
                        showNotification(messageData);

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
        // DeliveryInterface deliveryInterface = (DeliveryInterface) OptionMoneyMaker.getHomeFragmentContext();
        DeliveryInterface deliveryInterface = (DeliveryInterface) OptionMoneyMaker.getHomeFragmentContext();
        deliveryInterface.getUpdatedPayload(notifData);

        DeliveryInterface deliveryInterface1 = (DeliveryInterface) OptionMoneyMaker.getMessageDetailActivity();
        deliveryInterface1.getUpdatedPayload(notifData);

    }

    public void createNotifInits() {

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String id = "OMM";
            // The user-visible name of the channel.
            CharSequence name = "OMM";
            // The user-visible description of the channel.
            String description = "OMM";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            channelGroup = new NotificationChannelGroup(notifGroup, "Options Money Maker");
            mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);

            notificationManager.createNotificationChannelGroup(channelGroup);
            notificationManager.createNotificationChannel(mChannel);
            inboxStyle = new NotificationCompat.InboxStyle();


        }

    }

    public void showNotification(MessageData content) {

        Log.v("NotifIncomingData", "in fbmessingservice at 172");
        Log.v("NotifIncomingData", "id " + content.getId());
        Log.v("NotifIncomingData", "isRead " + content.getIsRead());
        Log.v("NotifIncomingData", "title " + content.getTitle());
        Log.v("NotifIncomingData", "mesg " + content.getMessage());
        Log.v("NotifIncomingData", "\n----\n----\n");

        globalContent = content;
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
        intent.putExtra("Model", content);
        int id = OptionMoneyMaker.getInstance().getCounter();

        pendingIntent = PendingIntent.getActivity(getApplicationContext(), id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Spanned spannedText;

        tempMsg = content.getMessage();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spannedText = Html.fromHtml(tempMsg, Html.FROM_HTML_MODE_COMPACT);
        } else {
            spannedText = Html.fromHtml(tempMsg);
        }

        tempMsg = spannedText.toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spannedText = Html.fromHtml(tempMsg, Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH);
        } else {
            spannedText = Html.fromHtml(tempMsg);
        }

        tempMsg = spannedText.toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spannedText = Html.fromHtml(tempMsg, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spannedText = Html.fromHtml(tempMsg);
        }

        tempMsg = spannedText.toString();

        //    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                StatusBarNotification notifs[] = notificationManager.getActiveNotifications();
                Log.v("NOTIFDEBUG", "\n \n \n ->active notifs : " + notifs.length);
                int count = 0;
                if (notifs.length > 0) {

                    for (int i = 0; i < notifs.length; i++) {
                        if (notifs[i].getPackageName().equalsIgnoreCase(getPackageName())) {
                             count++;
                        }
                    }

                    Log.v("notifdebug", "fina count "+count);

                    if(count > 0){
                        new SharedPrefsOperations(this).storePreferencesData("ActiveNotifs", "1");
                    }
                }

            }

            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            isShowing = true;

            if ("0".equals(new SharedPrefsOperations(this).getPreferencesData("ActiveNotifs"))) {

                String tempData = new SharedPrefsOperations(this).getPreferencesData("ActiveNotifs");
                Log.v("NotifDebug", tempData + " - primary notif hit : " + content.getTitle());

                Notification notification = new NotificationCompat.Builder(this, "OMM")
                        .setSmallIcon(R.mipmap.ic_static_notif)
                        .setContentTitle(content.getTitle())
                        .setContentText(tempMsg)
                        .setContentIntent(pendingIntent)
                        .setGroup(notifGroup)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(tempMsg))
                        .build();

                notificationManager.notify(id, notification);

                new SharedPrefsOperations(this).storePreferencesData("ActiveNotifs", "1");

            } else {

                String tempData = new SharedPrefsOperations(this).getPreferencesData("ActiveNotifs");
                Log.v("NotifDebug", tempData + " - secondary notif hit with id " + id + " , data : " + content.getTitle());

                Notification notification = new NotificationCompat.Builder(this, "OMM")
                        .setSmallIcon(R.mipmap.ic_static_notif)
                        .setContentTitle(content.getTitle())
                        .setContentText(tempMsg)
                        .setContentIntent(pendingIntent)
                        .setGroup(notifGroup)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(tempMsg))
                        .build();

                notificationManager.notify(id, notification);

                if ("1".equals(new SharedPrefsOperations(this).getPreferencesData("ActiveNotifs"))) {
                    initGroup();
                }

                new SharedPrefsOperations(this).storePreferencesData("ActiveNotifs", "2");

            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void initGroup() {

        Notification summaryNotification =
                new NotificationCompat.Builder(this, channel_id)
                        .setContentTitle("Options Money Maker")
                        .setContentText("New messages")
                        .setSmallIcon(R.mipmap.ic_static_notif)
                        .setStyle(new NotificationCompat.InboxStyle())
                        .setGroup(notifGroup)
                        .setGroupSummary(true)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, summaryNotification);

    }


}
