package com.example.dell.yoursapp.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.dell.yoursapp.R;

public class ShowCommentViewHolder extends RecyclerView.ViewHolder {
    public TextView txtUserPhone,txtComment;
    public RatingBar ratingBar;
    public ShowCommentViewHolder(View itemView) {
        super(itemView);

        txtComment=itemView.findViewById(R.id.txtComment);
        txtUserPhone=itemView.findViewById(R.id.txtUserPhone);
        ratingBar=itemView.findViewById(R.id.ratingBar);
    }
}
