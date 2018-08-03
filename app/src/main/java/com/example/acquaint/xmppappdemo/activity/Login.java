package com.example.acquaint.xmppappdemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.example.acquaint.xmppappdemo.R;
import com.example.acquaint.xmppappdemo.app.XmppApp;
import com.example.acquaint.xmppappdemo.connectionService.XmppConnection;
import com.example.acquaint.xmppappdemo.connectionService.XmppConnectionService;

public class Login extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private View.OnClickListener mOnButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (TextUtils.isEmpty(etUsername.getText().toString().trim())) {
                etUsername.setError("Username cannot be empty");
                etUsername.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
                etPassword.setError("Password cannot be empty");
                etPassword.requestFocus();
                return;
            }
            XmppConnection.login(etUsername.getText().toString(), etPassword.getText().toString());

        }
    };
    private BroadcastReceiver mOnLoginSuccessBroadcastReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(XmppConnectionService.UI_AUTHENTICATED)) {
                XmppApp.loginUserName = etUsername.getText().toString().trim();
                ShowContactListActivity();
            }
        }
    };
    private View.OnClickListener mOnRegisterButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(Login.this, RegisterActivity.class));
            finish();
        }
    };

    private void ShowContactListActivity() {
        startActivity(new Intent(Login.this, ContactListActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.registerReceiver(mOnLoginSuccessBroadcastReceived, new IntentFilter(XmppConnectionService.UI_AUTHENTICATED));
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        findViewById(R.id.btn_login).setOnClickListener(mOnButtonClickListener);
        findViewById(R.id.btn_register).setOnClickListener(mOnRegisterButtonClickListener);
    }

    @Override
    protected void onPause() {
        this.unregisterReceiver(mOnLoginSuccessBroadcastReceived);
        super.onPause();
    }
}
