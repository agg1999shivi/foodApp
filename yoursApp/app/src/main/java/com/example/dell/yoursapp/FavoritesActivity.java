package com.example.dell.yoursapp;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.dell.yoursapp.Common.Common;
import com.example.dell.yoursapp.Database.Database;
import com.example.dell.yoursapp.Helper.RecyclerItemTouchHelper;
import com.example.dell.yoursapp.Interface.RecyclerItemTouchHelperListener;
import com.example.dell.yoursapp.Model.Favorites;
import com.example.dell.yoursapp.Model.Order;
import com.example.dell.yoursapp.ViewHolder.FavoritesAdapter;
import com.example.dell.yoursapp.ViewHolder.FavoritesViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stepstone.apprating.C;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FavoritesActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    FavoritesAdapter adapter;
    RelativeLayout rootLayout;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rootLayout=findViewById(R.id.root_layout);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_fav);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //swipe  delete item
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback=new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);


        loadFavorites();


    }

    private void loadFavorites() {
        adapter=new FavoritesAdapter(this,new Database(this).getAllFavorites(Common.currentUser.getPhone()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof FavoritesViewHolder){
            String name=((FavoritesAdapter)recyclerView.getAdapter()).getItem(position).getFoodName();
            final Favorites deleteItem=((FavoritesAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex=viewHolder.getAdapterPosition();
            adapter.removeItem(viewHolder.getAdapterPosition());
            new Database(getBaseContext()).removeFromFavorites(deleteItem.getFoodId(), Common.currentUser.getPhone());

            Snackbar snackBar=Snackbar.make(rootLayout,name + " removed from cart !!!!!!",Snackbar.LENGTH_LONG);

            snackBar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToFavorites(deleteItem);

                }
            });
            snackBar.setActionTextColor(Color.YELLOW);
            snackBar.show();
        }
    }
}
