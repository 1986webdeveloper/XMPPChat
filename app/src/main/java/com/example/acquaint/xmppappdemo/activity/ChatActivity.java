package com.example.acquaint.xmppappdemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.acquaint.xmppappdemo.R;
import com.example.acquaint.xmppappdemo.connectionService.XmppConnection;
import com.example.acquaint.xmppappdemo.connectionService.XmppConnectionService;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;


public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private String contactJid;
    private ChatView mChatView;
    private BroadcastReceiver mBroadcastReceiver;
    private ChatManager chatManager;
    private IncomingChatMessageListener OnIncomingMessageListener = new IncomingChatMessageListener() {
        @Override
        public void newIncomingMessage(EntityBareJid from, final Message message, Chat chat) {
            Log.e(TAG, "newIncomingMessage: " + message.getBody());
            //if (from.equals(contactJid)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatMessage chatMessage = new ChatMessage(message.getBody(), System.currentTimeMillis(), ChatMessage.Type.RECEIVED);
                    mChatView.addMessage(chatMessage);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatView = findViewById(R.id.rooster_chat_view);

        mChatView.setOnSentMessageListener(new ChatView.OnSentMessageListener() {
            @Override
            public boolean sendMessage(ChatMessage chatMessage) {
                if (XmppConnectionService.getState().equals(XmppConnection.ConnectionState.CONNECTED)) {
                    Log.d(TAG, "The client is connected to the server,Sending Message");
                    Intent intent = new Intent(XmppConnectionService.SEND_MESSAGE);
                    intent.putExtra(XmppConnectionService.BUNDLE_MESSAGE_BODY,
                            mChatView.getTypedMessage());
                    intent.putExtra(XmppConnectionService.BUNDLE_TO, contactJid);
                    sendBroadcast(intent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Client not connected to server ,Message not sent!",
                            Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        Intent intent = getIntent();
        contactJid = intent.getStringExtra("EXTRA_CONTACT_JID");
        setTitle(contactJid);
        //getUserDetails(contactJid);

        chatManager = ChatManager.getInstanceFor(XmppConnection.getInstance());
        chatManager.addIncomingListener(OnIncomingMessageListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case XmppConnectionService.NEW_MESSAGE:
                        String from = intent.getStringExtra(XmppConnectionService.BUNDLE_FROM_JID);
                        String body = intent.getStringExtra(XmppConnectionService.BUNDLE_MESSAGE_BODY);
                        if (from.equals(contactJid)) {
                            ChatMessage chatMessage = new ChatMessage(body, System.currentTimeMillis(), ChatMessage.Type.RECEIVED);
                            mChatView.addMessage(chatMessage);
                        } else {
                            Log.d(TAG, "Got a message from jid :" + from);
                        }
                        return;
                }
            }
        };

        IntentFilter filter = new IntentFilter(XmppConnectionService.NEW_MESSAGE);
        registerReceiver(mBroadcastReceiver, filter);
    }
}
