package com.example.my_music_store;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.my_music_store.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    static User currentlyLoggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Utils.initUIComponents(this, findViewById(R.id.login_parent));

        Button btnToSignUp = findViewById(R.id.to_sign_up_btn);
        btnToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent s = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(s);
                finish();
            }
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference users = database.getReference("users");

        final EditText edtUserName = findViewById(R.id.username);
        final EditText edtPasswd = findViewById(R.id.passwd);

        Button signInBtn = findViewById(R.id.log_in);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = edtUserName.getText().toString();
                final String password = edtPasswd.getText().toString();

                users.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(username).exists()) {
                            if (!username.isEmpty()) {
                                User login = dataSnapshot.child(username).getValue(User.class);
                                if (login == null)
                                    return;

                                if (login.getPassword().equals(password)) {
                                    currentlyLoggedInUser = login;
                                    Intent s = new Intent(getApplicationContext(), HomeActivity.class);
                                    startActivity(s);
                                    sendBroadcast(new Intent("finish_signup_activity"));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Invalid login", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
