package com.optionsmoneymaker.optionsmoneymakerbeta.fragment;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.onesignal.OSNotificationPayload;
import com.onesignal.OneSignal;
import com.optionsmoneymaker.optionsmoneymakerbeta.OptionMoneyMaker;
import com.optionsmoneymaker.optionsmoneymakerbeta.R;
import com.optionsmoneymaker.optionsmoneymakerbeta.adapter.NewMessageAdapter;
import com.optionsmoneymaker.optionsmoneymakerbeta.interfaces.CallBacks;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.MessageData;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.MessageEvent;
import com.optionsmoneymaker.optionsmoneymakerbeta.rest.RestClient;
import com.optionsmoneymaker.optionsmoneymakerbeta.sqlitedb.DatabaseHandler;
import com.optionsmoneymaker.optionsmoneymakerbeta.utils.DeliveryInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

/**
 * Created by Sagar on 10/1/2016.
 */
public class HomeFragment extends BaseFragment implements DeliveryInterface, CallBacks {

    public static boolean active = false;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    OneSignal.NotificationReceivedHandler handler;
    NewMessageAdapter messageAdapter;
    ArrayList<MessageData> list;
    RecyclerView.LayoutManager mLayoutManager;
    String formattedDate;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OptionMoneyMaker.setHomeFragmentContext(HomeFragment.this);
        Log.v("ajtrial", "at 65 in homefrag onCreate hit");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);
        Log.v("ajtrial", "at 75 in homefrag onCreateView hit");
        recyclerView = rootView.findViewById(R.id.recyclerView);
        progressBar = rootView.findViewById(R.id.progressBar);

        // Inflate the layout for this fragment
        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        progressBar = new ProgressBar(getActivity());
        progressBar.setVisibility(View.VISIBLE);

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        list = new ArrayList<MessageData>();

        progressBar.setVisibility(View.VISIBLE);
        reload();

    }

    public void reload() {

        if (cd.isConnectingToInternet()) {
            hideKeyboard();
            RestClient.getMoneyMaker().lastMessage(session.getUserID(), new Callback<Response>() {
                @Override
                public void success(Response result, Response response) {
                    dismiss();
                    try {
                        JSONObject main = new JSONObject(new String(((TypedByteArray) response.getBody()).getBytes()));

                        if (main.getInt("status") == 1) {

                            session.setFirstTime(false);

                            //session.setLatestMessage(main.toString());

                            Log.v("datalog", "at 195 homefrag " + main.toString());

                            new DatabaseHandler().syncWithWeb(main.toString());
                            list = new DatabaseHandler().getAllNotifs();
                            Collections.reverse(list);
                            messageAdapter = new NewMessageAdapter(getActivity(), list);
                            recyclerView.setAdapter(messageAdapter);
                            //  progressBar.setVisibility(View.GONE);

                        } else if (main.getInt("status") == 0) {
                            Toast.makeText(getActivity(), "No Data Found.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("Home", "API failure " + error);
                    dismiss();
                }
            });

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
        EventBus.getDefault().unregister(this);
    }

    @Subscribe()
    public void onMessageEvent(MessageEvent event) {
        /* Do something */

    }

    @Override
    public void onResume() {

        super.onResume();

        Log.v("ajtrial", "at 181 in homefrag onresume hit");

        if (session.getFirstTime()) {

            //reload();

        } else {

            progressBar.setVisibility(View.VISIBLE);
            list = new DatabaseHandler().getAllNotifs();
            Collections.reverse(list);
            messageAdapter = new NewMessageAdapter(getActivity(), list);
            mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(messageAdapter);
            progressBar.setVisibility(View.GONE);

        }
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


    @Override
    public void getUpdatedPayload(OSNotificationPayload notificationPayload) {

        Log.v("ajtrial", "at 208 in homefrag data is " + notificationPayload.toJSONObject().toString());

        try {

            Uri notification = RingtoneManager.getActualDefaultRingtoneUri(OptionMoneyMaker.getInstance(), RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(OptionMoneyMaker.getInstance(), notification);
            r.play();

            JSONObject jsonObject = notificationPayload.toJSONObject();
            String body = jsonObject.optString("body");
            String title = jsonObject.optString("title");

            Log.v("jsondata", "body - " + body);
            Log.v("jsondata", "title - " + title);

            if (body.equals("") || body.isEmpty()) {
                body = " -- ";
            }

            if (title.equals("") || title.isEmpty()) {
                title = " -- ";
            }

            JSONObject jsonObject1 = jsonObject.getJSONObject("additionalData");
            int id = jsonObject1.getInt("message_id");

            MessageData forAdapter = new MessageData();
            forAdapter.setId(String.valueOf(id));
            forAdapter.setTitle(title);
            forAdapter.setMessage(body);
            String strDate = jsonObject1.getString("sent_time");
            strDate = convertTimeToLocal(strDate);
            forAdapter.setDateTime(strDate);

            MessageData forDb = new MessageData();
            forDb.setId(String.valueOf(id));
            forDb.setTitle(title);
            forDb.setMessage(body);
            forDb.setDateTime(jsonObject1.getString("sent_time"));

            messageAdapter.addNewItemToList(forAdapter);

            new DatabaseHandler().storeNewNotif(forDb);
            Log.v("ajtrial", "at 226 in home frag add new item complete hit");

            recyclerView.smoothScrollToPosition(0);

            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.new_message_dialog, null);

            AlertDialog.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
            } else {
                builder = new AlertDialog.Builder(getActivity());
            }

            builder.setView(dialogView);
            final AlertDialog alertDialog = builder.create();

            Button btn = dialogView.findViewById(R.id.btnOk);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                }
            });

            alertDialog.show();

            if (!r.isPlaying()) {
                r.play();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void callback(String msgId, String str) {

        Log.v("callback", "received " + str);
        if (str.equals("Delete")) {

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(msgId)) {
                    list.remove(i);
                    messageAdapter.notifyItemRemoved(i);
                    new DatabaseHandler().deleteNotif(Integer.parseInt(msgId));
                }
            }

        } else {


            for (int i = 0; i < list.size(); i++) {

                if ((list.get(i).getId()).equals(msgId)) {
                    Log.v("callback", "found at " + i);
                    if (str.equals("mark_message_unread")) {

                        list.get(i).setIsRead("2");
                        MessageData tempData = list.get(i);
                        tempData.setIsRead("2");
                        new DatabaseHandler().updateNotif(tempData);

                    } else if (str.equals("message_read")) {

                        list.get(i).setIsRead("1");
                        MessageData tempData = list.get(i);
                        tempData.setIsRead("1");
                        new DatabaseHandler().updateNotif(tempData);

                    }

                    messageAdapter.notifyDataSetChanged();
                }
            }

        }
    }


}





