package com.example.dell.yoursapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.dell.yoursapp.Common.Common;
import com.example.dell.yoursapp.Interface.ItemClickListener;
import com.example.dell.yoursapp.Model.Food;
import com.example.dell.yoursapp.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference foodList;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;



    ////search
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList =new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Food");

        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);



        swipeRefreshLayout=findViewById(R.id.swipe_layout);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(getIntent()!=null)
                    categoryId=getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId!=null) {
                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else {
                        Toast.makeText(getBaseContext(), "Please check your connection!!!!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

            }
        });


        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(getIntent()!=null)
                    categoryId=getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId!=null) {
                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else {
                        Toast.makeText(getBaseContext(), "Please check your connection!!!!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        //Get Intent here
        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if(!categoryId.isEmpty() && categoryId != null){
            if(Common.isConnectedToInternet(getBaseContext()))
                loadListFood(categoryId);
            else {
                Toast.makeText(FoodList.this, "Please check your Connection!!!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        materialSearchBar=findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your Food");
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<String> suggest=new ArrayList<String>();
                for(String search:suggestList){
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }

                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!enabled){
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void startSearch(CharSequence text) {
        searchAdapter=new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("Name").equalTo(text.toString())
        ) {
            @Override
            protected void populateViewHolder(final FoodViewHolder viewHolder, final Food model, final int position) {
                viewHolder.food_name.setText(model.getName());

                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

//                // Add fav
//                if(localDB.isFavorite(adapter.getRef(position).getKey()))
//                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
//
//
//                //Click to change status of fav
//
//                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if(!localDB.isFavorite(adapter.getRef(position).getKey()))
//                        {
//                            localDB.addToFavorites(adapter.getRef(position).getKey());
//                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
//                            Toast.makeText(FoodList.this,""+model.getName()+" was added to Fav",Toast.LENGTH_SHORT).show();
//                        }
//                        else{
//                            localDB.removeFromFavorites(adapter.getRef(position).getKey());
//                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
//                            Toast.makeText(FoodList.this,""+model.getName()+" was removed from Fav",Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });


                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //start new activity
                        Intent foodDetail =  new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }

        };
        recyclerView.setAdapter(searchAdapter);
    }

    private void loadSuggest() {
        foodList.orderByChild("MenuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Food item=postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(String categoryID){
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("MenuId").equalTo(categoryId)) { //like Select * from Foods where MenuID =
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //start new activity
                        Intent foodDetail =  new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }
        };
        //Set Adapter
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }
}
