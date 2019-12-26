package com.example.my_music_store;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_music_store.model.Location;
import com.example.my_music_store.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class CartActivity extends AppCompatActivity {

    private MusicItemReaderDbHelper dbHelper;
    private TextView cartNotificationView;
    public ListView cartItemsListView;

    private ArrayList<String> songsList;
    private DatabaseReference orders;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        orders = database.getReference("orders");

        dbHelper = new MusicItemReaderDbHelper(CartActivity.this);
        cartItemsListView = findViewById(R.id.cartListView);
        songsList = new ArrayList<>();
        new LoadCartItemsTask(dbHelper, this, songsList).execute();

        cartItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = (String)parent.getItemAtPosition(position);

                new RemoveCartItemTask(dbHelper, name).execute();
                new LoadCartItemsTask(dbHelper, CartActivity.this, songsList).execute();
                new UpdateCartNotificationTask(dbHelper, cartNotificationView).execute();
            }
        });

        Button button = findViewById(R.id.submit_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, MapsActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (LoginActivity.currentlyLoggedInUser == null)
                    return;

                if (songsList.size() < 1)
                    return;

                final Timestamp ts = new Timestamp(new Date().getTime());
                final Location location = new Location();
                if (data != null) {
                    location.setLatitude(data.getDoubleExtra("latitude", 0));
                    location.setLongitude(data.getDoubleExtra("longitude", 0));
                    location.setLocale(data.getStringExtra("locale"));
                } else {
                    return;
                }

                orders.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Order order = new Order(LoginActivity.currentlyLoggedInUser, ts, songsList, location);
                        orders.push().setValue(order);
                        Toast.makeText(CartActivity.this, "Order submitted!",
                                Toast.LENGTH_SHORT).show();

                        for (String item: songsList) {
                            new RemoveCartItemTask(dbHelper, item).execute();
                        }
                        new LoadCartItemsTask(dbHelper, CartActivity.this, songsList).execute();
                        new UpdateCartNotificationTask(dbHelper, cartNotificationView).execute();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.cart_item);
        cartNotificationView = item.getActionView().findViewById(R.id.actionbar_notifcation_textview);
        new UpdateCartNotificationTask(dbHelper, cartNotificationView).execute();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out: {
                LoginActivity.currentlyLoggedInUser = null;
                Intent intent = new Intent(CartActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                sendBroadcast(new Intent("finish_home_activity"));
                return true;
            }
            case R.id.cart_item: {
                Intent intent = new Intent(CartActivity.this, CartActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            case R.id.action_orders: {
                Intent intent = new Intent(CartActivity.this, OrdersActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new UpdateCartNotificationTask(dbHelper, cartNotificationView).execute();
    }
}

class LoadCartItemsTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<CartActivity> activityWeakReference;
    private ArrayList<String> songsList;
    private MusicItemReaderDbHelper dbHelper;

    LoadCartItemsTask(MusicItemReaderDbHelper dbHelper, CartActivity activity, ArrayList<String> songsList) {
        this.dbHelper = dbHelper;
        this.activityWeakReference = new WeakReference<>(activity);
        this.songsList = songsList;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + MusicItemReaderDbHelper.MusicItemEntry.TABLE_NAME,
                null);

        songsList.clear();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex(
                        MusicItemReaderDbHelper.MusicItemEntry.COLUMN_NAME));
                songsList.add(name);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return null;
    }

    @Override
    protected void onPostExecute(Void o) {
        CartActivity activity = activityWeakReference.get();
        if (activity != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                    R.layout.music_list_item, R.id.textView);
            adapter.addAll(songsList);
            activity.cartItemsListView.setAdapter(adapter);
        }
    }
}

class RemoveCartItemTask extends AsyncTask<Void, Void, Void> {

    private MusicItemReaderDbHelper dbHelper;
    private String songName;

    RemoveCartItemTask(MusicItemReaderDbHelper dbHelper, String songName) {
        this.dbHelper = dbHelper;
        this.songName = songName;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MusicItemReaderDbHelper.MusicItemEntry.TABLE_NAME,
                MusicItemReaderDbHelper.MusicItemEntry.COLUMN_NAME + "='" + songName + "'",
                null);
        return null;
    }
}