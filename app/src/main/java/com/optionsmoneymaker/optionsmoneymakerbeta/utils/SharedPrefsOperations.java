package com.optionsmoneymaker.optionsmoneymakerbeta.utils;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;


public class SharedPrefsOperations {

    Context context;

    public SharedPrefsOperations(Context context) {
        this.context = context;
    }

    public void storePreferencesData(String input_key, String input_value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
        editor.putString(input_key, input_value);
        editor.commit();

    }

    public String getPreferencesData(String input_key) {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getString(input_key, null);
    }


}
