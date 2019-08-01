package com.example.acquaint.xmppappdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.example.acquaint.xmppappdemo.R;
import com.example.acquaint.xmppappdemo.connectionService.XmppConnection;
import com.example.acquaint.xmppappdemo.connectionService.XmppConnectionService;
import com.example.acquaint.xmppappdemo.model.RegisterRequestResponse;
import com.example.acquaint.xmppappdemo.networking_setup.ApiClass;
import com.example.acquaint.xmppappdemo.networking_setup.ApiInterface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText etUserName, etPassword, etFullName, etEmailAddress;
    private View.OnClickListener mOnRegisterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (TextUtils.isEmpty(etUserName.getText().toString())) {
                etUserName.setError("Enter username");
                return;
            }
            if (TextUtils.isEmpty(etPassword.getText().toString())) {
                etUserName.setError("Enter password");
                return;
            }
            if (TextUtils.isEmpty(etFullName.getText().toString())) {
                etUserName.setError("Enter full name");
                return;
            }
            if (TextUtils.isEmpty(etEmailAddress.getText().toString())) {
                etUserName.setError("Enter email address");
                return;
            }

            if (XmppConnectionService.getState().equals(XmppConnection.ConnectionState.DISCONNECTED)) {
                Intent i1 = new Intent(RegisterActivity.this, XmppConnectionService.class);
                startService(i1);
            }
            ApiInterface apiInterface = ApiClass.getRetrofitObject();
            RegisterRequestResponse registerRequestResponse = new RegisterRequestResponse();
            registerRequestResponse.setUsername(etUserName.getText().toString());
            registerRequestResponse.setPassword(etPassword.getText().toString());
            registerRequestResponse.setName(etFullName.getText().toString());
            registerRequestResponse.setEmail(etEmailAddress.getText().toString());
            Call<ResponseBody> call = apiInterface.registerUser(registerRequestResponse);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.code() == 201) {
                        startActivity(new Intent(RegisterActivity.this, Login.class));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUserName = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etFullName = findViewById(R.id.et_full_name);
        etEmailAddress = findViewById(R.id.et_email_address);
        findViewById(R.id.btn_register).setOnClickListener(mOnRegisterClickListener);
    }
}
