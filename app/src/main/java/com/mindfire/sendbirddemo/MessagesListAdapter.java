package com.mindfire.sendbirddemo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sendbird.android.UserMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter binding the Messages Sent/ Received
 * Created by Vyom on 1/3/2017.
 */
public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder> {

    // View holder for the view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mMessagesTextView;
        public TextView mDateTextView;

        public ViewHolder(View view) {
            super(view);
            this.mMessagesTextView = (TextView) view.findViewById(R.id.message_text_view);
            this.mDateTextView = (TextView) view.findViewById(R.id.date_text_view);
        }
    }

    // Messages type
    private static final int MESSAGE_TYPE_SENT = 1;
    private static final int MESSAGE_TYPE_RECEIVED = 2;

    private static final String DATE_FORMAT = "dd/MM HH:mm";

    // Internal data
    private List<UserMessage> mMessagesList;
    private String mMyUserId;
    private SimpleDateFormat mDateFormatter;

    public MessagesListAdapter(String myUserId, List<UserMessage> messagesList) {
        this.mMyUserId = myUserId;
        this.mMessagesList = messagesList;
        mDateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    }

    @Override
    public int getItemViewType(int position) {
        UserMessage message = mMessagesList.get(position);
        if (mMyUserId.equals(message.getSender().getUserId())) {
            return MESSAGE_TYPE_SENT;
        } else {
            return MESSAGE_TYPE_RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;
        switch (viewType) {
            case MESSAGE_TYPE_SENT:
                layoutId = R.layout.sent_message_layout;
                break;
            default:
            case MESSAGE_TYPE_RECEIVED:
                layoutId = R.layout.received_message_layout;
                break;
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserMessage message = mMessagesList.get(position);
        holder.mMessagesTextView.setText(message.getMessage());
        holder.mDateTextView.setText(mDateFormatter.format(new Date(message.getCreatedAt())));
    }

    public void addMessage(UserMessage message) {
        mMessagesList.add(message);
        notifyDataSetChanged();
    }
}
