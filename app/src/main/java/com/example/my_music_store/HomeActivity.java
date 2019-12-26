package com.example.my_music_store;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    MusicItemReaderDbHelper dbHelper;
    private TextView cartNotificationView;
    BroadcastReceiver finishReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new MusicItemReaderDbHelper(HomeActivity.this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference songs = database.getReference("songs");

        songs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> songNames = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    String songName = ds.getValue(String.class);
                    if (songName != null) {
                        songNames.add(songName);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity.this,
                        R.layout.music_list_item, R.id.textView, songNames);
                ListView lv = findViewById(R.id.musicListView);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String name = (String)parent.getItemAtPosition(position);

                        new SaveCartItemTask(name, HomeActivity.this).execute();
                        new UpdateCartNotificationTask(dbHelper, cartNotificationView).execute();
                    }
                });
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        finishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.requireNonNull(intent.getAction()).equals("finish_home_activity")) {
                    HomeActivity.this.finish();
                }
            }
        };
        registerReceiver(finishReceiver, new IntentFilter("finish_home_activity"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        new UpdateCartNotificationTask(dbHelper, cartNotificationView).execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(finishReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.cart_item);
        cartNotificationView = item.getActionView().findViewById(R.id.actionbar_notifcation_textview);
        new UpdateCartNotificationTask(dbHelper, cartNotificationView).execute();

        menu.findItem(R.id.cart_item).getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out: {
                LoginActivity.currentlyLoggedInUser = null;

                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                unregisterReceiver(finishReceiver);
                finish();

                return true;
            }
            case R.id.action_orders: {
                Intent intent = new Intent(HomeActivity.this, OrdersActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

class SaveCartItemTask extends AsyncTask<Void, Void, Void> {

    private MusicItemReaderDbHelper dbHelper;
    private String songName;
    private WeakReference<HomeActivity> activity;
    private boolean itemAdded = false;

    SaveCartItemTask(String songName, HomeActivity activity) {
        this.dbHelper = activity.dbHelper;
        this.songName = songName;
        this.activity = new WeakReference<>(activity);
    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MusicItemReaderDbHelper.MusicItemEntry.COLUMN_NAME, songName);

        Cursor c = db.rawQuery("select * from " + MusicItemReaderDbHelper.MusicItemEntry.TABLE_NAME
            + " where " + MusicItemReaderDbHelper.MusicItemEntry.COLUMN_NAME + "='" + songName + "'",
                null);
        if (!c.moveToFirst()) {
            db.insert(MusicItemReaderDbHelper.MusicItemEntry.TABLE_NAME, null, values);
            itemAdded = true;
        }
        c.close();

        return null;
    }

    @Override
    protected void onPostExecute(Void o) {
        Context context = activity.get();
        if (context != null) {
            if (itemAdded) {
                Toast.makeText(context, "Item '" + songName + "' was added to cart.", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(context, "Item '" + songName + "' is already in cart!", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
}

class UpdateCartNotificationTask extends AsyncTask<Void, Void, Void> {

    private MusicItemReaderDbHelper dbHelper;
    private WeakReference<TextView> cartNotificationView;
    private int count;

    UpdateCartNotificationTask(MusicItemReaderDbHelper dbHelper, TextView cartNotificationView) {
        this.dbHelper = dbHelper;
        this.cartNotificationView = new WeakReference<>(cartNotificationView);
    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + MusicItemReaderDbHelper.MusicItemEntry.TABLE_NAME,
                null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                count++;
                cursor.moveToNext();
            }
        }
        cursor.close();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        TextView tv = cartNotificationView.get();
        if (tv != null)
            tv.setText(count + "");
    }
}
