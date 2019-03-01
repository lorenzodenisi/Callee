package com.callee.calleeclient.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callee.calleeclient.R;
import com.callee.calleeclient.database.Contact;

public class UserInfoFragment extends Fragment {

    Contact user;
    ViewGroup container;
    View view;
    FloatingActionButton button;
    boolean isUser = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        if (b != null) {
            user = b.getParcelable("contact");
            if (b.containsKey("isUser")) {
                isUser = b.getBoolean("isUser");
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.container = container;
        view = inflater.inflate(R.layout.user_info, container, false);
        if (user != null) {
            TextView username = view.findViewById(R.id.user_info_username);
            username.setText(user.getName());
            TextView email = view.findViewById(R.id.user_info_email);
            email.setText(user.getEmail());
        }

        button = view.findViewById(R.id.back_button);
        if (!isUser) {
            this.setBackButton();
        } else button.hide();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setBackButton() {
        button.show();
        button.setOnClickListener((v) -> {
            assert getParentFragment() != null;
            ((ContactFragment) getParentFragment()).removeUserInfo();
        });
    }
}
