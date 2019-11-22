package com.example.tripplanner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TripsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Trip> resources;
    private List<String> memberOf;

    public interface TripCardInteractionListener {
        void onJoinButtonClick(String tripTitle);
        void onTripCardClick(String tripTitle);

    }


    TripCardInteractionListener tripCardInteractionListener;

    public TripsRecyclerAdapter(List<Trip> resources,
                                TripCardInteractionListener tripCardInteractionListener,
                                List<String> memberOf) {

        this.resources = resources;
        this.tripCardInteractionListener = tripCardInteractionListener;
        this.memberOf = memberOf;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item_layout,parent,false);

        MyHolder myHolder = new MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        String imageUrl = resources.get(position).getImageUrl();

        final MyHolder tempMyHolder = (MyHolder) holder;

        Picasso.get().load(imageUrl).into(tempMyHolder.iv_trip_dp);
        tempMyHolder.lbl_tripTitle.setText(resources.get(position).getTitle());
        tempMyHolder.lbl_tripLocation.setText(resources.get(position).getLocation());
        tempMyHolder.lbl_tripBy.setText(resources.get(position).getCreatedBy());

        Log.d("TripsAdapter", "onBindViewHolder: "+memberOf.toString());
        if(memberOf.contains(resources.get(position).getTitle())){
            //Log.d("TripsAdapter", "onBindViewHolder: "+memberOf.toString());
            tempMyHolder.btn_join.setVisibility(View.INVISIBLE);
        }

        tempMyHolder.btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tripCardInteractionListener.
                        onJoinButtonClick(resources.get(tempMyHolder.getLayoutPosition()).getTitle());
            }
        });

        tempMyHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripCardInteractionListener.
                        onTripCardClick(resources.get(tempMyHolder.getLayoutPosition()).getTitle());
            }
        });

        //Set onClickListner on whole card view to view emails
        tempMyHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripCardInteractionListener.
                        onJoinButtonClick(resources.get(tempMyHolder.getLayoutPosition()).getTitle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    private static class MyHolder extends RecyclerView.ViewHolder{

        View cardView;
        ImageView iv_trip_dp;
        TextView lbl_tripTitle;
        TextView lbl_tripLocation;
        TextView lbl_tripBy;
        Button btn_join;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView;
            iv_trip_dp = itemView.findViewById(R.id.iv_trip_item_iv_tripdp);
            lbl_tripTitle = itemView.findViewById(R.id.lbl_trip_item_tripTitle);
            lbl_tripLocation = itemView.findViewById(R.id.lbl_trip_item_tripLocation);
            btn_join = itemView.findViewById(R.id.btn_trip_item_join);
            lbl_tripBy = itemView.findViewById(R.id.lbl_trip_item_createdBy);

        }
    }
}
