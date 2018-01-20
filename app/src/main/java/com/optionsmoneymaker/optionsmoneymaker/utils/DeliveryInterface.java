package com.optionsmoneymaker.optionsmoneymaker.utils;

import com.onesignal.OSNotificationPayload;
import com.optionsmoneymaker.optionsmoneymaker.model.MessageData;

/**
 * Created by Ajinkya on 1/5/2018.
 */

public interface DeliveryInterface {


    void getUpdatedPayload(OSNotificationPayload notificationPayload);
}
