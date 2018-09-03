package com.example.dell.yoursapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.yoursapp.Common.Common;
import com.example.dell.yoursapp.Database.Database;
import com.example.dell.yoursapp.Helper.RecyclerItemTouchHelper;
import com.example.dell.yoursapp.Interface.RecyclerItemTouchHelperListener;
import com.example.dell.yoursapp.Model.Food;
import com.example.dell.yoursapp.Model.MyResponse;
import com.example.dell.yoursapp.Model.Order;
import com.example.dell.yoursapp.Model.Receipt;
import com.example.dell.yoursapp.Model.Request;
import com.example.dell.yoursapp.Model.Sender;
import com.example.dell.yoursapp.Model.Token;
import com.example.dell.yoursapp.Model.User;
import com.example.dell.yoursapp.Remote.APIService;
import com.example.dell.yoursapp.Remote.IGoogleService;
import com.example.dell.yoursapp.ViewHolder.CartAdapter;
import com.example.dell.yoursapp.ViewHolder.CartViewHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.SnackBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static  com.example.dell.yoursapp.Cart.inventory;
import static  com.example.dell.yoursapp.Cart.inventoryList;

public class Cart extends AppCompatActivity implements RecyclerItemTouchHelperListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    String address,comment;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

   public TextView txtTotalPrice;
    Button btnPlace;
    float totalPrice;

    Place shippingAddress;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    static List<List<Order>> orderList = new ArrayList<>();
    static List<Food> inventoryList = new ArrayList<>();
    static Food inventory;
    static List<String> requestId  = new ArrayList<>();
    static List<Request> requestList = new ArrayList<>();
    static float total;

    RelativeLayout rootLayout;

    private boolean partial = false;

    APIService mService;
    IGoogleService mGoogleMapService;

    //Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL=5000;
    private static final int FASTEST_INTERVAL=3000;
    private static final int DISPLACEMENT=10;
    private static final int LOCATION_REQUEST_CODE=9999;
    private static final int PLAY_SERVICES_REQUEST=9997;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //permissions
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            },LOCATION_REQUEST_CODE);

        }
        else {
            if(checkPlayService()){
                buildGoogleApiClient();
                createLocationRequest();
            }
        }
        mGoogleMapService=Common.getGoogleMapAPI();
        mService=Common.getFCMService();

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout=findViewById(R.id.rootLayout);
        //swipe  delete item
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback=new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);


        txtTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cart.size()>0)
                      showAlertDialog();
                else
                    Toast.makeText(Cart.this,"Your cart is empty!!!",Toast.LENGTH_SHORT).show();

                }
        });

        loadListFood();

    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

    }

    private boolean checkPlayService() {
        int resultCode=GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_REQUEST).show();
            else {
                Toast.makeText(this,"This device is not supported !!",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address");

        LayoutInflater inflater=this.getLayoutInflater();
        View order_address_comment=inflater.inflate(R.layout.order_address_comment,null);

      //  final MaterialEditText edtAddress=order_address_comment.findViewById(R.id.edtAddress);

     final PlaceAutocompleteFragment edtAddress=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
     //
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your address Manually");

        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);

        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress=place;
            }

            @Override
            public void onError(Status status) {

                Log.e("ERROR",status.getStatusMessage());
            }
        });
        final MaterialEditText edtComment=order_address_comment.findViewById(R.id.edtComment);


        final RadioButton rdiShipToAddress=order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress=order_address_comment.findViewById(R.id.rdiHomeAddress);
        final RadioButton rdiCOD=order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiPaypal=order_address_comment.findViewById(R.id.rdiPayPal);
        final RadioButton rdiBalance=order_address_comment.findViewById(R.id.rdiBalance);


        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    try {
                                        JSONObject jsonObject=new JSONObject(response.body().toString());

                                        JSONArray resultsArray=jsonObject.getJSONArray("results");
                                        JSONObject firstObject=resultsArray.getJSONObject(0);
                                        address=firstObject.getString("formatted_address");
                                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    if(!TextUtils.isEmpty(Common.currentUser.getHomeAddress()) || Common.currentUser.getHomeAddress() != null){
                        address=Common.currentUser.getHomeAddress();
                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                .setText(address);
                    }

                    else
                    {  Toast.makeText(Cart.this,"Please update your home address",Toast.LENGTH_SHORT).show();}

                }
            }
        });

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (!rdiShipToAddress.isChecked() && !rdiHomeAddress.isChecked()) {
                    if (shippingAddress != null)
                        address = shippingAddress.getAddress().toString();
                    else {
                        Toast.makeText(Cart.this, "Please enter address or select any option address", Toast.LENGTH_SHORT).show();
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }
                }
                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(Cart.this, "Please enter address or select any option address", Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }
                comment = edtComment.getText().toString();

                if (!rdiCOD.isChecked() && !rdiPaypal.isChecked() && !rdiBalance.isChecked()) {
                    Toast.makeText(Cart.this, "Please Select Payment option", Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                } else if (rdiCOD.isChecked()) {
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            txtTotalPrice.getText().toString(),
                            "0",
                            edtComment.getText().toString(),
                            "COD",
                            "Unpaid",
                            String.format("%s %s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                            cart
                    );


                    String order_number = String.valueOf(System.currentTimeMillis());
                    requests.child(order_number)
                            .setValue(request);
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                    sendNotificationOrder(order_number);



                }
                else  if(rdiBalance.isChecked()){
                    double amount=0;
                    try {
                        amount=Common.formatCurrency(txtTotalPrice.getText().toString(),Locale.US).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(Double.parseDouble(Common.currentUser.getBalance().toString()) >= amount){
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                edtComment.getText().toString(),
                                "FoodCubo Balance",
                                "paid",
                                String.format("%s %s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                                cart
                        );


                        final String order_number = String.valueOf(System.currentTimeMillis());
                        requests.child(order_number)
                                .setValue(request);
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                        double balance=Double.parseDouble(Common.currentUser.getBalance().toString()) - amount;
                        Map<String,Object> update_balance =new HashMap<>();
                        update_balance.put("balance",balance);

                        FirebaseDatabase.getInstance()
                                .getReference("User")
                                .child(Common.currentUser.getPhone())
                                .updateChildren(update_balance)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            FirebaseDatabase.getInstance()
                                                    .getReference("User")
                                                    .child(Common.currentUser.getPhone())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            Common.currentUser = dataSnapshot.getValue(User.class);
                                                            sendNotificationOrder(order_number);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    }
                                });

                    }
                    else {
                        Toast.makeText(Cart.this,"Your balance not enough ,Please choose anoother option",Toast.LENGTH_SHORT).show();
                    }

                }
                /////optional
                ///abha
                Toast.makeText(Cart.this, "Thank you, Order placed", Toast.LENGTH_SHORT).show();
                finish();
            }


        });


        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });

        alertDialog.show();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case  LOCATION_REQUEST_CODE:
            {
                if(grantResults.length >0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    if(checkPlayService()){
                        buildGoogleApiClient();;
                        createLocationRequest();
                    }
                }
            }
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Request request=new Request(
//                Common.currentUser.getPhone(),
//                Common.currentUser.getName(),
//                address,
//                txtTotalPrice.getText().toString(),
//                "0",
//                comment,
//                String.format("%s %s",shippingAddress.getLatLng().longitude,shippingAddress.getLatLng().latitude),
//                cart
//        );
//
//        String order_number=String.valueOf(System.currentTimeMillis());
//        requests.child(order_number)
//                .setValue(request);
//
//        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
//        sendNotificationOrder(order_number);
//        Toast.makeText(Cart.this,"Thank you order placed",Toast.LENGTH_SHORT).show();
//        finish();
//    }

    private void sendNotificationOrder(final String order_number) {

        DatabaseReference tokens= FirebaseDatabase.getInstance().getReference("Tokens");
        Query data=tokens.orderByChild("isServerToken").equalTo(true);   //servertoken
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Token serverToken=postSnapshot.getValue(Token.class);

                    com.example.dell.yoursapp.Model.Notification notification=new com.example.dell.yoursapp.Model.Notification
                            ("Shivani","You have new order "+ order_number);
                    Sender content=new Sender(serverToken.getToken(),notification);
                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(Cart.this, "Thank you , Order Place", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(Cart.this, "Failed !!!!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                    Log.e("ERROR",t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void loadListFood() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
       // orderList.add(cart);
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate total price
        total = 0;
        for(Order order:cart)
            total+=(float) (Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);


        // add tax, profit to total, do we need to show the tax and profit on the app??
        float tax= (float) (total*0.06);
        float profit = (float) (total*0.3);
        total+=tax+profit;

        totalPrice =total;

        txtTotalPrice.setText(fmt.format(total));

    }

    //Delete item


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
                    return true;
    }

    private void deleteCart(int order) {
        cart.remove(order);
        new Database(this).cleanCart(Common.currentUser.getPhone());

        for(Order item:cart)
            new Database(this).addToCart(item);

        loadListFood();
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof CartViewHolder){
            String name=((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem =((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex=viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(),Common.currentUser.getPhone());

            int total = 0;
            List<Order> orders=new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
          //  List<Order> orders=new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for(Order item:orders)
                total+=(float) (Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en","US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);


            // add tax, profit to total, do we need to show the tax and profit on the app??
            float tax= (float) (total*0.06);
            float profit = (float) (total*0.3);
            total+=tax+profit;

            txtTotalPrice.setText(fmt.format(total));

            Snackbar snackBar=Snackbar.make(rootLayout,name + " removed from cart !!!!!!",Snackbar.LENGTH_LONG);

            snackBar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  adapter.restoreItem(deleteItem,deleteIndex);
                  new Database(getBaseContext()).addToCart(deleteItem);
                    int total = 0;
                    List<Order> orders=new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    //  List<Order> orders=new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for(Order item:orders)
                        total+=(float) (Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("en","US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);


                    // add tax, profit to total, do we need to show the tax and profit on the app??
                    float tax= (float) (total*0.06);
                    float profit = (float) (total*0.3);
                    total+=tax+profit;

                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackBar.setActionTextColor(Color.YELLOW);
            snackBar.show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){
            Log.e("LOCATION","your location : "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
        }
        else {
            Log.e("LOCATION","Could not get your location");
        }
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();
    }
}
