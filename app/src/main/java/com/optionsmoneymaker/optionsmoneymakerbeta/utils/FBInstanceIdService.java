package com.optionsmoneymaker.optionsmoneymakerbeta.utils;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.optionsmoneymaker.optionsmoneymakerbeta.OptionMoneyMaker;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.TokenUpdateResult;
import com.optionsmoneymaker.optionsmoneymakerbeta.rest.RestClient;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ajinkya W on 4/15/2018.
 */

public class FBInstanceIdService extends FirebaseInstanceIdService {

    SessionManager sessionManager;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        sessionManager = new SessionManager(OptionMoneyMaker.getInstance());

        Log.v("FBTOKEN", "REFRESH TOKEN \n " + FirebaseInstanceId.getInstance().getToken());

        RestClient.getMoneyMaker().updateFirebaseToken(FirebaseInstanceId.getInstance().getToken(), sessionManager.getUserID(), "Android", new Callback<TokenUpdateResult>() {
            @Override
            public void success(TokenUpdateResult tokenUpdateResult, Response response) {

                sessionManager.setRegisterID(FirebaseInstanceId.getInstance().getToken());

            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });

    }

}
