package com.optionsmoneymaker.optionsmoneymakerbeta;

import android.app.NotificationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.optionsmoneymaker.optionsmoneymakerbeta.model.MessageData;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.MessageDetail;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.NotificationResult;
import com.optionsmoneymaker.optionsmoneymakerbeta.rest.RestClient;
import com.optionsmoneymaker.optionsmoneymakerbeta.sqlitedb.DatabaseHandler;
import com.optionsmoneymaker.optionsmoneymakerbeta.utils.Constants;
import com.optionsmoneymaker.optionsmoneymakerbeta.utils.DeliveryInterface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MessageDetailActivity extends BaseActivity implements DeliveryInterface {

    private final String mimeType = "text/html";
    private final String encoding = "UTF-8";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.webview)
    WebView webView;
    @BindView(R.id.txt_product_name)
    TextView txtProName;
    @BindView(R.id.txt_date)
    TextView txtDate;
    @BindView(R.id.txt_message)
    TextView txtTitle;
    String formattedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //txtMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, session.getFontSize());
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.abs_layout);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            ((TextView) actionBar.getCustomView().findViewById(R.id.txt_action_title)).setText("HOME");
        }

        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        nMgr.cancelAll();

        if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase("list")) {

            webView.loadDataWithBaseURL("", getIntent().getStringExtra(Constants.MESSAGE), mimeType, encoding, "");
            txtProName.setText(getIntent().getStringExtra(Constants.PRODUCT));
            txtTitle.setText(getIntent().getStringExtra(Constants.TITLE));

            try {

                String strDate = getIntent().getStringExtra(Constants.DATE);
                strDate = convertTimeToLocal(strDate);
                txtDate.setText(strDate);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cd.isConnectingToInternet()) {
                hideKeyboard();
                try {

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);

                    RestClient.getMoneyMaker().messageRead(getIntent().getStringExtra(Constants.ID), session.getUserID(),
                            dateFormatter.format(new Date()), new Callback<NotificationResult>() {
                                @Override
                                public void success(NotificationResult result, Response response) {
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    Log.e("Message Detail", "API failure " + error);
                                }
                            });
                } catch (Exception e) {

                }
            } else {
                //toast(getResources().getString(R.string.no_internet));
            }
        } else if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase("notification")) {
            if (cd.isConnectingToInternet()) {
                hideKeyboard();
                messageDetail(getIntent().getStringExtra(Constants.ID));
            } else {
                toast(getResources().getString(R.string.no_internet));
            }
        }

        Log.v("current", "in MessageDetailActivity");
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
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {

            case android.R.id.home:
                this.finish();
                startActivity(MainActivity.class);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void messageDetail(String id) {

        showProgressbar("Message Detail");

        try {

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            String strTime = dateFormatter.format(new Date());

            RestClient.getMoneyMaker().messageDetail(id, strTime, session.getUserID(), new Callback<MessageDetail>() {
                @Override
                public void success(MessageDetail result, Response response) {

                    if ((int) result.getStatus() == 1) {
                        webView.loadDataWithBaseURL("", result.getMessage(), mimeType, encoding, "");
                        txtProName.setText(result.getProductName());
                        //    txtProName.setText("AppTet");
                        txtTitle.setText(result.getTitle());

                        String strTime = result.getDateTime();
                        strTime = convertTimeToLocal(strTime);
                        txtDate.setText(strTime);

                    } else if ((int) result.getStatus() == 0) {
                        toast("No Data Found");
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("Message Detail", "API failure " + error);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dismiss();
        }

    }

    @Override
    public void getUpdatedPayload(MessageData notificationPayload) {

        Log.v("ajtrial", "at 208 in MessageDetailActivity data is " + notificationPayload);

        try {

            Uri notification = RingtoneManager.getActualDefaultRingtoneUri(OptionMoneyMaker.getInstance(), RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(OptionMoneyMaker.getInstance(), notification);
            r.play();

            Log.v("jsondata", "body - " + notificationPayload.getMessage());
            Log.v("jsondata", "title - " + notificationPayload.getTitle());

            if (notificationPayload.getMessage().equals("") || notificationPayload.getMessage().isEmpty()) {
                notificationPayload.setMessage("--");
            }

            if (notificationPayload.getTitle().equals("") || notificationPayload.getTitle().isEmpty()) {
                notificationPayload.setTitle("--");
            }

            new DatabaseHandler().storeNewNotif(notificationPayload);

            notificationPayload.setDateTime(convertTimeToLocal(notificationPayload.getDateTime()));
        //    messageAdapter.addNewItemToList(notificationPayload);

            Log.v("ajtrial", "at 226 in home frag add new item complete hit");

        //    recyclerView.smoothScrollToPosition(0);

            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.new_message_dialog, null);

            AlertDialog.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(MessageDetailActivity.this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
            } else {
                builder = new AlertDialog.Builder(MessageDetailActivity.this);
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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
