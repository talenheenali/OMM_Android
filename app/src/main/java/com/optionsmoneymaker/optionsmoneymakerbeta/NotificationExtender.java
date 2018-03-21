package com.optionsmoneymaker.optionsmoneymakerbeta;

import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;
import com.optionsmoneymaker.optionsmoneymakerbeta.utils.SessionManager;

public class NotificationExtender extends NotificationExtenderService {

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult osNotificationReceivedResult) {
        // Read properties from result.

        Log.v("ajtrial","at 15 inNotificationExtender hit");

          // Return true to stop the notification from displaying.
        SessionManager session = new SessionManager(NotificationExtender.this);
        if (session.isLoggedIn()) {
            return false;
        } else {
            return true;
        }
    }

}
