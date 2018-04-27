package com.optionsmoneymaker.optionsmoneymakerbeta.utils;


import com.optionsmoneymaker.optionsmoneymakerbeta.model.MessageData;

/**
 * Created by Ajinkya on 1/5/2018.
 */

public interface DeliveryInterface {


    void getUpdatedPayload(MessageData notificationPayload);
}
