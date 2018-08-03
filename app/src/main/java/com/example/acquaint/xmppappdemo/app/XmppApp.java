package com.example.acquaint.xmppappdemo.app;

import android.app.Application;
import android.content.Intent;

import com.example.acquaint.xmppappdemo.connectionService.XmppConnection;
import com.example.acquaint.xmppappdemo.connectionService.XmppConnectionService;

public class XmppApp extends Application {

    public static String loginUserName = "";

    public static String getLoginUserName() {
        return loginUserName;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (XmppConnectionService.getState() == XmppConnection.ConnectionState.DISCONNECTED) {
            Intent i1 = new Intent(this, XmppConnectionService.class);
            startService(i1);
        }

    }
}
