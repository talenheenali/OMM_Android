package com.optionsmoneymaker.optionsmoneymakerbeta.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.optionsmoneymaker.optionsmoneymakerbeta.R;
import com.optionsmoneymaker.optionsmoneymakerbeta.model.NavDrawerItem;

import java.util.Collections;
import java.util.List;

/**
 * Created by Sagar on 10/1/2016.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {

    List<NavDrawerItem> data = Collections.emptyList();
    TypedArray icons;
    private LayoutInflater inflater;
    private Context context;

    public NavigationDrawerAdapter(Context context, List<NavDrawerItem> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        icons = context.getResources().obtainTypedArray(R.array.drawer_icon);
    }

    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.nav_drawer_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.title.setText(data.get(position).getTitle());
        holder.ivIcon.setImageResource(icons.getResourceId(position, 0));
        //Log.e("Fragment", data.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView ivIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            ivIcon = itemView.findViewById(R.id.iv_icon);
        }
    }
}
