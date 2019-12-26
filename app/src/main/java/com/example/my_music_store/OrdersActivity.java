package com.example.my_music_store;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.my_music_store.model.Location;
import com.example.my_music_store.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class OrdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        final ListView lv = findViewById(R.id.ordersList);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference orders = database.getReference("orders");

        orders.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Order> orders = new ArrayList<>();
                ArrayList<String> orderIds = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Order order = ds.getValue(Order.class);
                    if (order == null)
                        return;

                    if (order.getUser().getUsername().equals(LoginActivity.currentlyLoggedInUser.getUsername())) {
                        orders.add(ds.getValue(Order.class));
                        orderIds.add(Objects.requireNonNull(ds.getKey()).substring(1));
                    }
                }
                OrdersAdapter adapter = new OrdersAdapter(OrdersActivity.this, orders, orderIds);
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

class OrdersAdapter extends ArrayAdapter<Order> {
    private final ArrayList<Order> orders;
    private final ArrayList<String> orderIds;

    OrdersAdapter(OrdersActivity activity, ArrayList<Order> orders, ArrayList<String> orderIds) {
        super(activity, 0, orders);
        this.orders = orders;
        this.orderIds = orderIds;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View orderItemView = convertView;
        if (orderItemView == null)
            orderItemView = LayoutInflater.from(getContext()).inflate(R.layout.order_list_item, parent,
                    false);

        String orderId = Math.abs(orderIds.get(position).hashCode()) + "";
        TextView orderIdTextView = orderItemView.findViewById(R.id.orderId);
        orderIdTextView.setText(orderId);

        Order order = orders.get(position);

        Location location = order.getLocation();
        if (location != null) {
            TextView locationTextView = orderItemView.findViewById(R.id.order_location);
            locationTextView.setText(order.getLocation().getLocale());
        }

        ArrayList songNames = order.getSongNames();
        if (songNames != null) {
            TextView totalNumTextView = orderItemView.findViewById(R.id.total_number_orders);
            totalNumTextView.setText(order.getSongNames().size() + "");

            ListView songsListView = orderItemView.findViewById(R.id.song_items_list);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.order_items_list_item);
            adapter.addAll(order.getSongNames());
            songsListView.setAdapter(adapter);

            // set the height of the listview
            ViewGroup.LayoutParams params = songsListView.getLayoutParams();
            params.height = order.getSongNames().size() *  75 +
                    (songsListView.getDividerHeight() * (songsListView.getCount() - 1));
            songsListView.setLayoutParams(params);
            songsListView.requestLayout();
        }

        return orderItemView;
    }
}