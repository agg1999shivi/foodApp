package com.example.dell.yoursapp.Common;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.dell.yoursapp.Model.User;
import com.example.dell.yoursapp.Remote.APIService;
import com.example.dell.yoursapp.Remote.GoogleRetrofitClient;
import com.example.dell.yoursapp.Remote.IGoogleService;
import com.example.dell.yoursapp.Remote.RetrofitClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Common {
    public static String topicName="News";
    public static User currentUser;
    public static final String DELETE = "Delete";
    public static final String USER_KEY="User";
    public static final String USER_PASSWORD="Password";

    public static String PHONE_TEXT="userPhone";

    public static String currentKey;

    public static final String INTENT_FOOD_ID="FoodId";

    private static final String BASE_URL="https://fcm.googleapis.com/";

    private static final String GOOGLE_API_URL="https://maps.googleapis.com/";

    public static APIService getFCMService(){
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);

    }

    public static IGoogleService getGoogleMapAPI(){
        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }


    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager=
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager !=null){
            NetworkInfo[] info =connectivityManager.getAllNetworkInfo();
            if(info!=null){
                for (int i=0;i<info.length;i++){
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;

    }
    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "On your way";
        else if(status.equals("2"))
            return "Received";
        else
            return "Food Ready";
    }

    public  static BigDecimal formatCurrency(String amount, Locale locale) throws ParseException{
        NumberFormat format=NumberFormat.getCurrencyInstance(locale);
        if(format instanceof DecimalFormat)
            ((DecimalFormat)format).setParseBigDecimal(true);
        return  (BigDecimal)format.parse(amount.replace("[^\\d.,]",""));
    }
}

