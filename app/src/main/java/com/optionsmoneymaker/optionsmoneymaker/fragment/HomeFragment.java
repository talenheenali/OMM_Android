package com.optionsmoneymaker.optionsmoneymaker.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.onesignal.OSNotification;
import com.onesignal.OSNotificationPayload;
import com.onesignal.OneSignal;
import com.optionsmoneymaker.optionsmoneymaker.adapter.MessageAdapter;
import com.optionsmoneymaker.optionsmoneymaker.interfaces.CallBacks;
import com.optionsmoneymaker.optionsmoneymaker.OptionMoneyMaker;
import com.optionsmoneymaker.optionsmoneymaker.R;
import com.optionsmoneymaker.optionsmoneymaker.adapter.NewMessageAdapter;
import com.optionsmoneymaker.optionsmoneymaker.model.MessageData;
import com.optionsmoneymaker.optionsmoneymaker.model.MessageEvent;
import com.optionsmoneymaker.optionsmoneymaker.rest.RestClient;
import com.optionsmoneymaker.optionsmoneymaker.sqlitedb.DatabaseHandler;
import com.optionsmoneymaker.optionsmoneymaker.utils.DeliveryInterface;
import com.optionsmoneymaker.optionsmoneymaker.utils.SessionManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

/**
 * Created by Sagar on 10/1/2016.
 */
public class HomeFragment extends BaseFragment implements DeliveryInterface, CallBacks {

    ProgressBar progressBar;
    RecyclerView recyclerView;
    OneSignal.NotificationReceivedHandler handler;
    public static boolean active = false;
    NewMessageAdapter messageAdapter;
    ArrayList<MessageData> list;
    RecyclerView.LayoutManager mLayoutManager;

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

                        if ((int) main.getInt("status") == 1) {

                            session.setFirstTime(false);

                            //session.setLatestMessage(main.toString());

                            Log.v("datalog", "at 195 homefrag " + main.toString());

                            new DatabaseHandler().syncWithWeb(main.toString());
                            list = new DatabaseHandler().getAllNotifs();
                            Collections.reverse(list);
                            messageAdapter = new NewMessageAdapter(getActivity(), list);
                            recyclerView.setAdapter(messageAdapter);
                            //  progressBar.setVisibility(View.GONE);

                        } else if ((int) main.getInt("status") == 0) {
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


    @Override
    public void getUpdatedPayload(OSNotificationPayload notificationPayload) {

        Log.v("ajtrial", "at 208 in homefrag data is " + notificationPayload.toJSONObject().toString());

        try {

            JSONObject jsonObject = notificationPayload.toJSONObject();

            String title = jsonObject.getString("title");
            String body = jsonObject.getString("body");
            String id = jsonObject.getString("notificationID");

            MessageData data = new MessageData();
            data.setId(id);
            data.setTitle(title);
            data.setMessage(body);
            long d = new Date().getTime();
            data.setDateTime(String.valueOf(d));
            messageAdapter.addNewItemToList(data);

            new DatabaseHandler().storeNewNotif(data);
            Log.v("ajtrial", "at 226 in home frag add new item complete hit");

            recyclerView.smoothScrollToPosition(0);
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getActivity());
            }
            builder.setTitle("New Message")
                    .setMessage(R.string.arrived_msg)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void callback(String msgId, String str) {

        Log.v("callback", "received " + str);
        if (str.equals("Delete")) {

            for(int i = 0 ; i < list.size() ; i++){
                if(list.get(i).getId().equals(msgId)){
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





