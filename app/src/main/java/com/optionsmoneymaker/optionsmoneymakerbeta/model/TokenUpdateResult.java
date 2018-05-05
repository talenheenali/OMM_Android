package com.optionsmoneymaker.optionsmoneymakerbeta.model;

import com.google.gson.annotations.Expose;

/**
 * Created by Ajinkya W on 5/3/2018.
 */

public class TokenUpdateResult {

    @Expose
    String deviceId;

    @Expose
    String user_id;

    @Expose
    String message;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
