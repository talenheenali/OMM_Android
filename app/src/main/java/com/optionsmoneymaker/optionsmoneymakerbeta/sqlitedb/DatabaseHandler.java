package com.optionsmoneymaker.optionsmoneymakerbeta.sqlitedb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.optionsmoneymaker.optionsmoneymakerbeta.OptionMoneyMaker;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.MessageData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DB_OMM = "DB_OMM";
    // aLL tableS names
    private static final String TABLE_NOTIFS_MASTER = "NOTIFS_MASTER";
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;
    String formattedDate;

    public DatabaseHandler() {

        super(OptionMoneyMaker.getInstance(), DB_OMM, null, DATABASE_VERSION);

    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_NOTIFS_MASTER = "CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFS_MASTER + " (" +
                "  `NOTIF_ID` INTEGER PRIMARY KEY NOT NULL," +
                "  `NOTIF_TITLE` TEXT NOT NULL," +
                "  `NOTIF_PRODUCT_NAME` TEXT NOT NULL," +
                "  `NOTIF_DATE_TIME` TEXT NOT NULL," +
                "  `NOTIF_MESSAGE` TEXT NOT NULL," +
                "  `NOTIF_ISREAD` TEXT NOT NULL" +
                ")";

        db.execSQL(CREATE_TABLE_NOTIFS_MASTER);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFS_MASTER);
        // Create tables again
        onCreate(db);
    }

    // insert new customer basic data
    public long storeNewNotif(MessageData messageDataModel) {

        long returnId = 0;

        try {
            sqLiteDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            //  SimpleDateFormat sdf = new SimpleDateFormat(InfoResources.DateFormatForView);
            values.put("NOTIF_ID", messageDataModel.getId());
            values.put("NOTIF_TITLE", messageDataModel.getTitle());
            values.put("NOTIF_PRODUCT_NAME", messageDataModel.getProductName());

            String strDate = messageDataModel.getDateTime();
            strDate = convertTimeToLocal(strDate);

            values.put("NOTIF_DATE_TIME", strDate);
            values.put("NOTIF_MESSAGE", messageDataModel.getMessage());
            values.put("NOTIF_ISREAD", messageDataModel.getIsRead());

            Log.v("NotifIncomingData","in databasehandle at 87");
            Log.v("NotifIncomingData","id " + messageDataModel.getId());
            Log.v("NotifIncomingData","isRead " +messageDataModel.getIsRead());
            Log.v("NotifIncomingData","title "+messageDataModel.getTitle());
            Log.v("NotifIncomingData","mesg " +messageDataModel.getMessage());
            Log.v("NotifIncomingData","\n----\n----\n");


            returnId = sqLiteDatabase.insertOrThrow(TABLE_NOTIFS_MASTER, null, values);
            Log.v("dbdemo", "newly inserted row id is = " + returnId + " , in table - " + TABLE_NOTIFS_MASTER);

        } catch (Exception e) {

            if (e.getMessage().contains("UNIQUE constraint")) {
                Log.v("SQLITEEXCEPTION", "data already present with id " + messageDataModel.getId());
            } else {
                e.printStackTrace();
            }

        }

        sqLiteDatabase.close(); // Closing database connection
        return returnId;

    }

    //insert customer fees and other details
    public ArrayList<MessageData> getAllNotifs() {

        MessageData messageDataModel;
        ArrayList<MessageData> arrayList = new ArrayList<>();

        try {

            String selectQuery = "SELECT * FROM " + TABLE_NOTIFS_MASTER;
            sqLiteDatabase = this.getWritableDatabase();
            cursor = sqLiteDatabase.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {

                do {

                    messageDataModel = new MessageData();
                    messageDataModel.setId((cursor.getString(0)));
                    messageDataModel.setTitle((cursor.getString(1)));
                    messageDataModel.setProductName((cursor.getString(2)));
                    messageDataModel.setDateTime(cursor.getString(3));
                    messageDataModel.setMessage((cursor.getString(4)));
                    messageDataModel.setIsRead((cursor.getString(5)));
                    arrayList.add(messageDataModel);

                } while (cursor.moveToNext());
            }

            sqLiteDatabase.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return arrayList;

    }

    public String convertTimeToLocal(String timeString) {

        try {

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(timeString);
            df.setTimeZone(TimeZone.getDefault());
            formattedDate = df.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return formattedDate;

    }

    public void updateNotif(MessageData messageDataModel) {

        sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("NOTIF_ISREAD", messageDataModel.getIsRead());

        // updating row
        int res = sqLiteDatabase.update(TABLE_NOTIFS_MASTER, values, "NOTIF_ID" + " = " + messageDataModel.getId(), null);
        Log.v("dbdemo", "updation to notif master , result = " + res);

        sqLiteDatabase.close();

    }

    public void syncAndStoreIntoDb(ArrayList<MessageData> arrayList) {

        MessageData messageDataModel;

        //collect the server response into arraylist
        for (int i = 0; i < arrayList.size(); i++) {

            messageDataModel = arrayList.get(i);
            storeNewNotif(messageDataModel);

        }

    }

    public void syncWithWeb(String rawJsonDataString, ArrayList<MessageData> oldList) {

        MessageData messageDataModel;
        ArrayList<MessageData> arrayList = new ArrayList<>();

        try {

            Log.v("InsertData", "arraylist received from server size " + arrayList.size());

            //collect the server response into arraylist

            JSONObject jsonObject = new JSONObject(rawJsonDataString);
            String status = jsonObject.getString("status");

            JSONArray jsonArray = jsonObject.getJSONArray("data");
            Log.v("InsertData", "jsonArray received from server size " + jsonArray.length());

            if (jsonArray.length() > 0) {



                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    messageDataModel = new MessageData();
                    messageDataModel.setId(jsonObject1.getString("id"));
                    messageDataModel.setTitle(jsonObject1.getString("title"));
                    messageDataModel.setProductName(jsonObject1.getString("product_name"));
                    messageDataModel.setDateTime(jsonObject1.getString("date_time"));
                    messageDataModel.setMessage(jsonObject1.getString("message"));
                    messageDataModel.setIsRead(jsonObject1.getString("isRead"));
                    arrayList.add(messageDataModel);

                }

                //check and set read unread status
                for (int i = 0; i < oldList.size() ; i++) {

                    if(oldList.get(i).getIsRead().equalsIgnoreCase("0")){

                        for (int j = 0; j < arrayList.size(); j++) {

                            if(arrayList.get(j).getId().equalsIgnoreCase(oldList.get(i).getId())){

                                arrayList.get(j).setIsRead("0");
                            }

                        }

                    }
                }

                // delete all the old notifications data
                deleteAllNotifs();

                //add data into db
                for (int j = 0; j < arrayList.size(); j++) {
                    storeNewNotif(arrayList.get(j));
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void deleteNotif(int notifId) {

        SQLiteDatabase db = this.getWritableDatabase();
        int res = db.delete(TABLE_NOTIFS_MASTER, "NOTIF_ID" + " = " + notifId, null);
        Log.v("deleteLog", "delete return value from " + TABLE_NOTIFS_MASTER + " - " + res);
        db.close();

    }

    public void deleteAllNotifs() {

        SQLiteDatabase db = this.getWritableDatabase();
        int res = db.delete(TABLE_NOTIFS_MASTER, null, null);
        Log.v("deleteLog", "delete return value from " + TABLE_NOTIFS_MASTER + " - " + res);
        db.close();

    }


}