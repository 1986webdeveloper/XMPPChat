package com.example.acquaint.xmppappdemo.connectionService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.acquaint.xmppappdemo.R;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

public class XmppConnection {

    private static final String TAG = "XmppConnection";
    private static XMPPTCPConnection mConnection;
    private final Context mApplicationContext;
    private BroadcastReceiver uiThreadMessageReceiver;//Receives messages from the ui thread.

    /**
     * Listener to receive the connection events
     */
    private ConnectionListener mOnConnectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            XmppConnectionService.sConnectionState = ConnectionState.CONNECTED;
            Log.d(TAG, "Connected Successfully");
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            XmppConnectionService.sConnectionState = ConnectionState.CONNECTED;
            Log.d(TAG, "Authenticated Successfully");
            showContactListActivityWhenAuthenticated();
        }

        @Override
        public void connectionClosed() {
            XmppConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
            Log.d(TAG, "Connectionclosed()");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            XmppConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
            Log.d(TAG, "ConnectionClosedOnError, error " + e.toString());

        }

        @Override
        public void reconnectionSuccessful() {
            XmppConnectionService.sConnectionState = ConnectionState.CONNECTED;
            Log.d(TAG, "ReconnectionSuccessful()");
        }

        @Override
        public void reconnectingIn(int seconds) {
            XmppConnectionService.sConnectionState = ConnectionState.CONNECTING;
            Log.d(TAG, "ReconnectingIn() ");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            XmppConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
            Log.d(TAG, "ReconnectionFailed()");
        }
    };

    public XmppConnection(Context context) {
        Log.d(TAG, "XmppConnection Constructor called.");
        mApplicationContext = context.getApplicationContext();
    }

    public static XMPPConnection getInstance() {
        return mConnection;
    }

    public static void login(String username, String password) {
        try {
            mConnection.login(username, password);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * method to connect the client to the server
     * @throws IOException
     * @throws XMPPException
     * @throws SmackException
     */
    public void connect() throws IOException, XMPPException, SmackException {

        XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(mApplicationContext.getString(R.string.txt_domain_name)) // name of the domain
                .setHost(mApplicationContext.getString(R.string.txt_server_address)) // address of the server
                .setResource(mApplicationContext.getString(R.string.txt_resource)) // resource from where your request is sent
                .setPort(5222) // static port number to connect
                .setKeystoreType(null) //To avoid authentication problem. Not recommended for production build
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setCompressionEnabled(true).build();

        //Set up the ui thread broadcast message receiver.
        setupUiThreadBroadCastMessageReceiver();

        mConnection = new XMPPTCPConnection(conf);
        mConnection.addConnectionListener(mOnConnectionListener);
        try {
            Log.d(TAG, "Calling connect() ");
            mConnection.connect();
            Presence presence = new Presence(Presence.Type.available);
            mConnection.sendPacket(presence);

            Log.d(TAG, " login() Called ");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         * Listener to receive the incoming message
         */

        ChatManager.getInstanceFor(mConnection).addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid messageFrom, Message message, Chat chat) {
                String from = message.getFrom().toString();

                String contactJid = "";
                if (from.contains("/")) {
                    contactJid = from.split("/")[0];
                    Log.d(TAG, "The real jid is :" + contactJid);
                    Log.d(TAG, "The message is from :" + from);
                } else {
                    contactJid = from;
                }

                //Bundle up the intent and send the broadcast.
                Intent intent = new Intent(XmppConnectionService.NEW_MESSAGE);
                intent.setPackage(mApplicationContext.getPackageName());
                intent.putExtra(XmppConnectionService.BUNDLE_FROM_JID, contactJid);
                intent.putExtra(XmppConnectionService.BUNDLE_MESSAGE_BODY, message.getBody());
                mApplicationContext.sendBroadcast(intent);
                Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");
            }
        });


        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        reconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();

    }

    /**
     * broadcast method to send message from one client to another
     */
    private void setupUiThreadBroadCastMessageReceiver() {
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if (action.equals(XmppConnectionService.SEND_MESSAGE)) {
                    //Send the message.
                    sendMessage(intent.getStringExtra(XmppConnectionService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(XmppConnectionService.BUNDLE_TO));
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(XmppConnectionService.SEND_MESSAGE);
        mApplicationContext.registerReceiver(uiThreadMessageReceiver, filter);

    }

    /**
     * method to send message from one client to another
     * @param body -- message to be sent
     * @param toJid -- id of the receiver
     */
    private void sendMessage(String body, String toJid) {
        Log.d(TAG, "Sending message to :" + toJid);

        EntityBareJid jid = null;

        toJid = toJid + "@" + mApplicationContext.getString(R.string.txt_domain_name) + "/" + mApplicationContext.getString(R.string.txt_resource);
        ChatManager chatManager = ChatManager.getInstanceFor(mConnection);

        try {
            jid = JidCreate.entityBareFrom(toJid);
            Log.e(TAG, "sendMessage: jid : " + jid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        Chat chat = chatManager.chatWith(jid);
        try {
            Message message = new Message(jid, Message.Type.chat);
            message.setBody(body);
            chat.send(message);

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * method to disconnect the user from the server
     */
    public void disconnect() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        prefs.edit().putBoolean("xmpp_logged_in", false).commit();

        if (mConnection != null) {
            mConnection.disconnect();
        }

        mConnection = null;
        // Unregister the message broadcast receiver.
        if (uiThreadMessageReceiver != null) {
            mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
            uiThreadMessageReceiver = null;
        }
    }

    private void showContactListActivityWhenAuthenticated() {
        Intent i = new Intent(XmppConnectionService.UI_AUTHENTICATED);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
    }


    public static enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED;
    }

    public static enum LoggedInState {
        LOGGED_IN, LOGGED_OUT;
    }
}
