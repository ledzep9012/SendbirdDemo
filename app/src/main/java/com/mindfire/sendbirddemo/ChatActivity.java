package com.mindfire.sendbirddemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vyom on 1/3/2017.
 */
public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    public static final String INTENT_KEY_USER_ID = "UserId";
    public static final String INTENT_KEY_USER_NAME = "UserName";

    // The unique channelIdentifier for the group channel handler
    private static final String channelIdentifier = "SendbirdTest";

    // All the channels must be distinct. This will allow them to be reused and get previous messages
    private static final boolean IS_DISTINCT = true;

    // Maximum load 30 messages
    private static final int MAXIMUM_MESSAGES_LOAD = 30;

    // Views
    private RecyclerView mMessagesRecyclerView;
    private EditText mMessageBoxEditText;
    private Button mViewButton;


    private Context mContext;
    private String mReceiverId;
    private String mReceiverName;
    private List<UserMessage> mMessagesLit;
    private RecyclerView.LayoutManager mLayoutManager;
    private MessagesListAdapter mMessagesListAdapter;
    private GroupChannel mGroupChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the user Id
        mReceiverId = getIntent().getExtras().getString(INTENT_KEY_USER_ID);
        mReceiverName = getIntent().getExtras().getString(INTENT_KEY_USER_NAME);
        Log.d(TAG, "The receiver is " + mReceiverId + ":" + mReceiverName);

        // Init the variables
        mContext = Sendbird.getContext();

        // Link the views
        mMessagesRecyclerView = (RecyclerView) findViewById(R.id.messages_recycler_view);
        mMessageBoxEditText = (EditText) findViewById(R.id.messages_box_edit_text);

        mViewButton = (Button) findViewById(R.id.send_button);
        mViewButton.setOnClickListener(mOnClickListener);

        // Set the name of the receiver
        getSupportActionBar().setTitle(mReceiverName);

        // Set the messages list layout
        mLayoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(mLayoutManager);

        // Create channel
        List<String> userIdsList = new ArrayList<String>();
        userIdsList.add(mReceiverId);
        GroupChannel.createChannelWithUserIds(userIdsList, IS_DISTINCT, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(mContext, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                mGroupChannel = groupChannel;

                setGeneralChannelHandler();
                // Load messages
                loadMessages(groupChannel);
            }
        });
    }

    private void setGeneralChannelHandler() {
        SendBird.addChannelHandler(channelIdentifier, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                Log.d(TAG, "New message received " + baseMessage.getMessageId());
                if (mGroupChannel != null && baseChannel.getUrl().equals(mGroupChannel.getUrl())) {
                    if (mMessagesListAdapter != null && baseMessage instanceof UserMessage) {
                        printUserMesage((UserMessage) baseMessage);
                        mGroupChannel.markAsRead();
                        mMessagesListAdapter.addMessage((UserMessage) baseMessage);
                        // Move the list to the last item
                        mMessagesRecyclerView.smoothScrollToPosition(mMessagesListAdapter.getItemCount());
                    }
                }
            }

            @Override
            public void onReadReceiptUpdated(GroupChannel groupChannel) {
                Log.d(TAG, "The read receipt has been updated for the channel " + groupChannel.getUrl());
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel groupChannel) {
                Log.d(TAG, "The typing status has been updated for the channel " + groupChannel.getUrl());
            }

            @Override
            public void onUserJoined(GroupChannel groupChannel, User user) {
                Log.d(TAG, "New user joined to the channel " + groupChannel.getUrl() + ", " + user.getNickname());
            }

            @Override
            public void onUserLeft(GroupChannel groupChannel, User user) {
                Log.d(TAG, "User left on the channel " + groupChannel.getUrl() + ", " + user.getNickname());
            }
        });
    }

    private void loadMessages(GroupChannel groupChannel) {
        PreviousMessageListQuery previousMessageListQuery = groupChannel.createPreviousMessageListQuery();
        previousMessageListQuery.load(MAXIMUM_MESSAGES_LOAD, false, new PreviousMessageListQuery.MessageListQueryResult() {
            @Override
            public void onResult(List<BaseMessage> baseMessagesList, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(mContext, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                mMessagesLit = new ArrayList<UserMessage>();
                mMessagesListAdapter = new MessagesListAdapter(SendBird.getCurrentUser().getUserId(), mMessagesLit);
                mMessagesRecyclerView.setAdapter(mMessagesListAdapter);

                for (BaseMessage baseMessage : baseMessagesList) {
                    if (baseMessage instanceof UserMessage) {
                        UserMessage userMessage = (UserMessage) baseMessage;
                        printUserMesage(userMessage);
                        mMessagesListAdapter.addMessage(userMessage);
                    }

                    // Move the list to the last item
                    mMessagesRecyclerView.smoothScrollToPosition(mMessagesListAdapter.getItemCount());
                }
            }
        });
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.send_button:
                    if (mGroupChannel == null) {
                        Log.d(TAG, "The group channel must be ready when the user want to send the message");
                        return;
                    }

                    final String textToSend = mMessageBoxEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(textToSend)) {
                        mGroupChannel.sendUserMessage(textToSend, new BaseChannel.SendUserMessageHandler() {
                            @Override
                            public void onSent(UserMessage userMessage, SendBirdException e) {
                                if (e != null) {
                                    Toast.makeText(mContext, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Log.v(TAG, "Message sent to" + mReceiverName + ":" + textToSend);
                                mMessageBoxEditText.setText("");

                                // Update the users list
                                mMessagesListAdapter.addMessage(userMessage);
                                // Move the list to the last item
                                mMessagesRecyclerView.smoothScrollToPosition(mMessagesListAdapter.getItemCount());
                            }
                        });
                    }
                    break;
            }
        }
    };

    private void printUserMesage(UserMessage userMessage) {
        Log.d(TAG, "User message {" +
                "Sender: " + userMessage.getSender().getNickname() +
                ", Message: " + userMessage.getMessage() +
                ", Data: " + userMessage.getData() +
                ", RequestId: " + userMessage.getRequestId() +
                ", Created at: " + userMessage.getCreatedAt() +
                "}"
        );
    }
}
