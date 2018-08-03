package com.example.acquaint.xmppappdemo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.acquaint.xmppappdemo.R;
import com.example.acquaint.xmppappdemo.adapter.GroupAdapter;
import com.example.acquaint.xmppappdemo.app.XmppApp;
import com.example.acquaint.xmppappdemo.model.GroupResponse;
import com.example.acquaint.xmppappdemo.networking_setup.ApiClass;
import com.example.acquaint.xmppappdemo.networking_setup.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFragment extends Fragment {

    private GroupAdapter mGroupAdapter;
    private List<String> mListOfGroups = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        initialiseView(view);
        getListOfUser();
        return view;
    }

    private void getListOfUser() {
        ApiInterface apiInterface = ApiClass.getRetrofitObject();
        Call<GroupResponse> call = apiInterface.getUserGroup(XmppApp.getLoginUserName());
        call.enqueue(new Callback<GroupResponse>() {
            @Override
            public void onResponse(@NonNull Call<GroupResponse> call, @NonNull Response<GroupResponse> response) {
                if (response.isSuccessful()) {
                    GroupResponse groupResponse = response.body();
                    mListOfGroups.addAll(groupResponse.getGroupname());
                    mGroupAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GroupResponse> call, @NonNull Throwable t) {

            }
        });
    }

    private void initialiseView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_contact_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mGroupAdapter = new GroupAdapter(getContext(), mListOfGroups);
        recyclerView.setAdapter(mGroupAdapter);
    }
}
