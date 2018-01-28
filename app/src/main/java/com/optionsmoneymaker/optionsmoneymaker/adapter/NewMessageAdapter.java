package com.optionsmoneymaker.optionsmoneymaker.adapter;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.optionsmoneymaker.optionsmoneymaker.MessageDetailActivity;
import com.optionsmoneymaker.optionsmoneymaker.R;
import com.optionsmoneymaker.optionsmoneymaker.fragment.MessageActionDialogFragment;
import com.optionsmoneymaker.optionsmoneymaker.model.MessageData;
import com.optionsmoneymaker.optionsmoneymaker.sqlitedb.DatabaseHandler;
import com.optionsmoneymaker.optionsmoneymaker.utils.Constants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ajinkya on 1/18/2018.
 */

public class NewMessageAdapter extends RecyclerView.Adapter<NewMessageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MessageData> msgList;

    public NewMessageAdapter(Context context, ArrayList<MessageData> list) {

        this.context = context;
        this.msgList = list;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_message_item, parent, false);

        return new ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.txtProName.setText(msgList.get(position).getProductName());
        holder.txtMessage.setText(Html.fromHtml(msgList.get(position).getTitle()));
        holder.txtDate.setText(msgList.get(position).getDateTime());
        holder.txtProName.setText(msgList.get(position).getProductName());

        if (msgList.get(position).getIsRead().equalsIgnoreCase("1")) {

            //convertView.setBackground(context.getResources().getDrawable(R.drawable.btn_dark_grey));
            //convertView.setPadding(10, 10, 10, 10);
            holder.txtProName.setTextColor(context.getResources().getColor(R.color.app_date_color));
            holder.txtDate.setTextColor(context.getResources().getColor(R.color.app_date_color));
            holder.txtMessage.setTextColor(context.getResources().getColor(R.color.app_date_color));
            holder.txtProName.setTypeface(Typeface.DEFAULT);
            holder.txtDate.setTypeface(Typeface.DEFAULT);
            holder.txtMessage.setTypeface(Typeface.DEFAULT);

        } else {
            //convertView.setBackground(context.getResources().getDrawable(R.drawable.btn_light_grey));
            //convertView.setPadding(10, 10, 10, 10);
            holder.txtMessage.setTextColor(context.getResources().getColor(R.color.black_font));
            holder.txtProName.setTypeface(Typeface.DEFAULT_BOLD);
            holder.txtDate.setTypeface(Typeface.DEFAULT_BOLD);
            holder.txtMessage.setTypeface(Typeface.DEFAULT_BOLD);
        }

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MessageData dataToUpdate = msgList.get(holder.getAdapterPosition());
                dataToUpdate.setIsRead("1");

                new DatabaseHandler().updateNotif(dataToUpdate);
                notifyItemChanged(holder.getAdapterPosition());

                Intent intent = new Intent(context, MessageDetailActivity.class);
                intent.putExtra(Constants.TYPE, "list");
                intent.putExtra(Constants.ID, msgList.get(holder.getAdapterPosition()).getId());
                intent.putExtra(Constants.MESSAGE, msgList.get(holder.getAdapterPosition()).getMessage());
                intent.putExtra(Constants.PRODUCT, msgList.get(holder.getAdapterPosition()).getProductName());
                intent.putExtra(Constants.DATE, msgList.get(holder.getAdapterPosition()).getDateTime());
                intent.putExtra(Constants.TITLE, msgList.get(holder.getAdapterPosition()).getTitle());
                context.startActivity(intent);

            }
        });

        holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if(msgList.get(holder.getAdapterPosition()).getIsRead().equals("1")){

                        //this means it is readed
                }else{

                    //this means not readed
                }

                MessageActionDialogFragment newFragment =
                        MessageActionDialogFragment.newInstance(msgList.get(holder.getAdapterPosition()).getId(),
                                msgList.get(holder.getAdapterPosition()).getIsRead() );

                FragmentManager manager = ((Activity) context).getFragmentManager();
                newFragment.show(manager, "MessageAction");
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public void addNewItemToList(MessageData newMessageDataItem) {

        msgList.add(0, newMessageDataItem);
        notifyItemInserted(0);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_product_name)
        TextView txtProName;
        @BindView(R.id.txt_message)
        TextView txtMessage;
        @BindView(R.id.txt_date)
        TextView txtDate;
        RelativeLayout itemLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemLayout = itemView.findViewById(R.id.rowItem);
        }
    }

}
