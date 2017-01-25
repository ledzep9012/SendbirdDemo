package com.mindfire.sendbirddemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mindfire.sendbirddemo.constants.AppDetails;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;

import java.security.MessageDigest;
import java.util.List;

/**
 * Shows the UI for user list activity
 * Created by Vyom on 1/3/2017.
 */
public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UserListActivity";

    // Views
    private ListView mUsersListView;

    // Internal variables
    private Context mContext;
    private String mUserId;
    private UserListQuery mUserListQuery;
    private ProgressBar mProgressBar;

    private List<User> mUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        // Init variables
        this.mContext = this;

        mProgressBar = (ProgressBar) findViewById(R.id.progress_users_chat);

        // Link the views
        mUsersListView = (ListView) findViewById(R.id.users_list_view);

        mUserId = generateDeviceUUID();
        Log.d(TAG, "The user id is " + mUserId);

        final String userName = "User-" + mUserId.substring(0, 5);
        Log.d(TAG, "onCreate: User Name" + userName);

        // Initialising Sendbird SDK
        SendBird.init(AppDetails.APP_ID, mContext);

        mProgressBar.setVisibility(View.VISIBLE);

        SendBird.connect(mUserId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(UserListActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "onConnected: ");
                Toast.makeText(UserListActivity.this, "The app has connected", Toast.LENGTH_SHORT).show();

                // Progress Bar Ends here
                mProgressBar.setVisibility(View.GONE);

                // Fill users list
                fillUsersList();
            }
        });
    }

    private void fillUsersList() {
        // Get the list of users
        mUserListQuery = SendBird.createUserListQuery();
        mUserListQuery.setLimit(30);

        if (mUserListQuery != null && mUserListQuery.hasNext() && !mUserListQuery.isLoading()) {
            mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
                @Override
                public void onResult(List<User> users, SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(mContext, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    /*// Get the user list
                    Log.d(TAG, "The list of users is ");
                    for (User user : users) {
                        Log.d(TAG, user.getUserId() + ":" + user.getNickname());
                    }*/

                    // Get the list of users
                    mUsersList = users;
                    String[] userNames = new String[users.size() - 1];

                    for (int i = 0; i < userNames.length; i++) {
                        if (users.get(i).getUserId().equals(mUserId)) {
                            getSupportActionBar().setTitle(userNames[i]);
                            continue;
                        }
                        userNames[i] = users.get(i).getNickname();

                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                            android.R.layout.simple_list_item_1, android.R.id.text1,
                            userNames);
                    mUsersListView.setAdapter(adapter);

                    mUsersListView.setOnItemClickListener(new ListView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            User userSelected = mUsersList.get(i);
                            Log.d(TAG, "The user has selected " + userSelected.getUserId() + ":" + userSelected.getNickname());

                            Intent intent = new Intent(mContext, ChatActivity.class);
                            intent.putExtra(ChatActivity.INTENT_KEY_USER_ID, userSelected.getUserId());
                            intent.putExtra(ChatActivity.INTENT_KEY_USER_NAME, userSelected.getNickname());
                            startActivity(intent);
                        }
                    });
                }
            });
        }
    }

    private String generateDeviceUUID() {
        String serial = android.os.Build.SERIAL;
        String androidID = Settings.Secure.ANDROID_ID;
        String deviceUUID = serial + androidID;

        /*
         * SHA-1
         */
        MessageDigest digest;
        byte[] result;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            result = digest.digest(deviceUUID.getBytes("UTF-8"));
        } catch (Exception e) {
            Log.d(TAG, "Error generating the devices UUID", e);
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

}
