package com.example.dell.yoursapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dell.yoursapp.Common.Common;
import com.example.dell.yoursapp.Model.Food;
import com.example.dell.yoursapp.Model.Request;
import com.example.dell.yoursapp.ViewHolder.FoodViewHolder;
import com.example.dell.yoursapp.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import static com.example.dell.yoursapp.Common.Common.convertCodeToStatus;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(getIntent().getExtras()==null)
        loadOrders(Common.currentUser.getPhone());
        else
            loadOrders(getIntent().getStringExtra("userPhone"));

    }

    private void loadOrders(String phone) {
        Query getOrderByUser=requests.orderByChild("phone")
                .equalTo(phone);

        FirebaseRecyclerOptions<Request> requestOptions=new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(getOrderByUser,Request.class)
                .build();
        adapter =new FirebaseRecyclerAdapter<Request, OrderViewHolder>(requestOptions) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, int position, @NonNull Request model) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderphone.setText(model.getPhone());

            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_layout,parent,false);
                return new OrderViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
