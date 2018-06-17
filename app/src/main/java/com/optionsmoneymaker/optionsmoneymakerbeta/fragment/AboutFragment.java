package com.optionsmoneymaker.optionsmoneymakerbeta.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.optionsmoneymaker.optionsmoneymakerbeta.R;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.About;
import com.optionsmoneymaker.optionsmoneymakerbeta.rest.RestClient;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Sagar on 10/1/2016.
 */
public class AboutFragment extends BaseFragment {

    Context context;

    @BindView(R.id.tv_about)
    TextView tvAbout;

    @BindView(R.id.tvUserId)
    TextView tv_userId;
    @BindView(R.id.tvOsName)
    TextView tv_osName;
    @BindView(R.id.tvOsVersionNumber)
    TextView tv_osVer;
    @BindView(R.id.tvAppVer)
    TextView tv_AppVer;
    @BindView(R.id.tvShowPlayerId)
    TextView tv_showId;


    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, rootView);

        if (cd.isConnectingToInternet()) {
            hideKeyboard();
            aboutUS();
        } else {
            Snackbar.make(container, getResources().getString(R.string.no_internet), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }

        PackageInfo pInfo = null;
        try {

            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = pInfo.versionName;

            Field[] fields = Build.VERSION_CODES.class.getFields();
            String osName = fields[Build.VERSION.SDK_INT + 1].getName();

            tv_userId.setText(Html.fromHtml("<b>User Id : </b>" + session.getEmailId()));
            //     tv_osName.setText(Html.fromHtml("<b>OS : </b>" + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName()));
            tv_osName.setText(Html.fromHtml("<b>OS : </b>" + osName));
            tv_osVer.setText(Html.fromHtml("<b>OS Ver : </b>" + android.os.Build.VERSION.SDK_INT + 1));
            tv_AppVer.setText(Html.fromHtml("<b>Application Ver : </b>" + version));

            tv_showId.setText("Show Device Id");
            tv_showId.setTextColor(getResources().getColor(R.color.green_color));
            tv_showId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View dialogView = inflater.inflate(R.layout.show_device_id, null);

                    AlertDialog.Builder builder;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
                    } else {
                        builder = new AlertDialog.Builder(context);
                    }

                    builder.setView(dialogView);
                    final AlertDialog alertDialog = builder.create();

                    Button btn = dialogView.findViewById(R.id.btnOk);
                    TextView tvData = dialogView.findViewById(R.id.tv_dev_id);
                    tvData.setText(session.getRegisterID());

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.cancel();
                        }
                    });

                    alertDialog.show();

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void aboutUS() {
        try {

            showProgressbar("About US");
            RestClient.getMoneyMaker().aboutUS("about_us", new Callback<About>() {
                @Override
                public void success(About result, Response response) {


                    dismiss();
                    if (result.getStatus() == 1) {
                        tvAbout.setText(result.getData());
                    } else if (result.getStatus() == 0) {
                        toast(result.getData());
                    }

                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("About US", "API failure " + error);
                    dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
