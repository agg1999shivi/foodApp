package com.example.dell.yoursapp.Service;

import com.example.dell.yoursapp.Common.Common;
import com.example.dell.yoursapp.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefreshed=FirebaseInstanceId.getInstance().getToken();

        if(Common.currentUser!=null)
        updateTokenToFirebase(tokenRefreshed);
    }

    private void updateTokenToFirebase(String tokenRefreshed) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token token = new Token(tokenRefreshed,false);
        tokens.child(Common.currentUser.getPhone()).setValue(token);
    }
}
