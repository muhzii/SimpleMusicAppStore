package com.example.my_music_store;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.my_music_store.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    BroadcastReceiver finishReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Utils.initUIComponents(this, findViewById(R.id.signup_parent));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference users = database.getReference("users");

        final EditText edtUserName = findViewById(R.id.username);
        final EditText edtEmail = findViewById(R.id.email);
        final EditText edtPasswd = findViewById(R.id.passwd);

        Button signUpBtn = findViewById(R.id.sign_up);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final User user = new User(edtUserName.getText().toString(),
                        edtEmail.getText().toString(), edtPasswd.getText().toString());

                users.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(user.getUsername()).exists())
                            Toast.makeText(SignUpActivity.this, "Username already exits",
                                    Toast.LENGTH_SHORT).show();
                        else {
                            users.child(user.getUsername()).setValue(user);
                            Toast.makeText(SignUpActivity.this, "Registration successful",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        Button btnSignIn = findViewById(R.id.to_sign_in_btn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent s = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(s);
            }
        });

        finishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.requireNonNull(intent.getAction()).equals("finish_signup_activity")) {
                    SignUpActivity.this.finish();
                }
            }
        };
        registerReceiver(finishReceiver, new IntentFilter("finish_signup_activity"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(finishReceiver);
    }
}
