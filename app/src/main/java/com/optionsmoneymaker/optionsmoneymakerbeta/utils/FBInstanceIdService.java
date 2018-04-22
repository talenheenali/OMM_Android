package com.optionsmoneymaker.optionsmoneymakerbeta.utils;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Ajinkya W on 4/15/2018.
 */

public class FBInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        Log.v("FBTOKEN", "TOKEN \n " + FirebaseInstanceId.getInstance().getToken());
    }

}
