package com.callee.calleeclient.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.callee.calleeclient.Global;
import com.callee.calleeclient.R;
import com.callee.calleeclient.client.Message;
import com.callee.calleeclient.client.ToM;
import com.callee.calleeclient.database.Contact;
import com.callee.calleeclient.database.dbDriver;
import com.callee.calleeclient.thread.RegisterThread;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.lang.System.currentTimeMillis;

public class WelcomeActivity extends AppCompatActivity {

    private static boolean auth;        //indicates which fragment need to be reloaded
    LoginFragment loginFragment;
    private AuthFragment authFragment;

    public static RegisterThread rt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.welcome_activity);

        if (Global.db == null) {
            Global.db = new dbDriver();
        }
        Global.db.openConnection(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        Contact credentials = new Contact(null, null, null);

        Thread t = Global.db.getCredentials(credentials);
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (credentials.getEmail() == null || credentials.getName() == null) {

            FragmentManager fm = this.getSupportFragmentManager();
            if (auth) {
                authFragment = new AuthFragment();
                fm.beginTransaction().replace(R.id.welcome_activity_container, authFragment, "auth fragment").commit();
            } else {
                loginFragment = new LoginFragment();
                fm.beginTransaction().replace(R.id.welcome_activity_container, loginFragment, "login fragment").commit();
            }

        } else {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("auth", auth);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        LinearLayout doneButton = this.findViewById(R.id.login_button);
        if (loginFragment != null && loginFragment.getView() != null)
            doneButton.setOnClickListener(new DoneListener(loginFragment.getView()));

    }

    private class DoneListener implements View.OnClickListener {

        private Pattern userPattern, mailPattern;
        private EditText username, mail, mail_confirm;
        private View view;

        DoneListener(View view) {
            this.userPattern = Pattern.compile(".*[:;\\\\@()'\"].*");
            this.mailPattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$");
            username = view.findViewById(R.id.username_editText);
            mail = view.findViewById(R.id.email_editText);
            mail_confirm = view.findViewById(R.id.email_repeat_editText);
            this.view = view;
        }


        public void onClick(View v) {
            String user = username.getText().toString();
            String email = mail.getText().toString();
            String email_confirm = mail_confirm.getText().toString();

            if (userPattern.matcher(user).matches() || user.equals("")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    username.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
                }
                username.setText("");
                username.setHint("Invalid username");
                username.setHintTextColor(getResources().getColorStateList(android.R.color.holo_red_light));
            }

            if (!mailPattern.matcher(email).matches() || email.equals("")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mail.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
                }
                mail.setText("");
                mail.setHint("Wrong emailField address");
                mail.setHintTextColor(getResources().getColorStateList(android.R.color.holo_red_light));
            }

            if (!email.equals(email_confirm)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mail_confirm.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
                }
                mail_confirm.setText("");
                mail_confirm.setHint("Emails not corresponding");
                mail_confirm.setHintTextColor(getResources().getColorStateList(android.R.color.holo_red_light));
            }


            if (!userPattern.matcher(user).matches() && mailPattern.matcher(email).matches() && email.equals(email_confirm)) {
                Message m = new Message(-1L, user, "SERVER", email,
                        "server@server.server", currentTimeMillis(), ToM.REGISTERUSER);

                //thread read from 2 and write on 1
                if (rt != null) {
                    rt.customJoin();
                }
                rt = new RegisterThread(m);
                rt.start();

                //main read from 1 and write on 2
                int res = 0;
                //wait thread to complete register first message
                try {
                    res = rt.in1.read();     //1=OK 0=ERROR
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (res != 1) {
                    Toast.makeText(v.getContext(), "Email already taken", Toast.LENGTH_LONG).show();
                    rt.customJoin();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mail_confirm.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
                        mail.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
                    }
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    authFragment = new AuthFragment();
                    auth = true;
                    fm.beginTransaction().replace(R.id.welcome_activity_container, authFragment, "auth fragment").commit();
                }
            }
        }
    }


    public static class LoginFragment extends Fragment {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.login_fragment, container, false);
        }

    }

    public static class AuthFragment extends Fragment {

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.auth_fragment, container, false);
        }

        @Override
        public void onStart() {
            super.onStart();

            WelcomeActivity welcomeActivity = (WelcomeActivity) getActivity();

            LinearLayout authButton = null;
            if (welcomeActivity != null) {
                authButton = welcomeActivity.findViewById(R.id.auth_button);
            }
            if (authButton != null) {
                authButton.setOnClickListener(new ConfirmListener(Objects.requireNonNull(getView()), WelcomeActivity.rt));
            }
        }


        private class ConfirmListener implements View.OnClickListener {

            View view;
            RegisterThread rT;
            EditText codeField;
            int res;

            ConfirmListener(View view, RegisterThread rT) {
                this.rT = rT;
                codeField = view.findViewById(R.id.auth_code);
                this.view = view;
            }

            @Override
            public void onClick(View v) {
                int code = Integer.parseInt(codeField.getText().toString());
                try {
                    String codeString = String.format(Locale.ENGLISH, "%06d", code);
                    rT.out2.write(codeString.getBytes());        //write to thread the code typed in the field
                    res = rT.in1.read();        //read from thread the result of second part of registration
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (res == 1) {
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    startActivity(intent);
                }
                if (res == 0){
                    codeField.setText("");
                    codeField.setHint("Wrong code!");
                }
                if(res==2){
                    WelcomeActivity parent = (WelcomeActivity) getActivity();
                    parent.loginFragment=new LoginFragment();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.welcome_activity_container, parent.loginFragment , "login fragment").commit();

                }
            }
        }
    }
}
