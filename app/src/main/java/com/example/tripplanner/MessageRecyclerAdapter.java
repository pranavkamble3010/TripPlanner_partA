package com.example.tripplanner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> resources;
    private String username;

    public interface MessageInteractionListener {
        void onJoinButtonClick(String tripTitle);
        void onTripCardClick(Trip trip);
    }


    MessageInteractionListener tripCardInteractionListener;

    public MessageRecyclerAdapter(List<Message> resources,
                                  MessageInteractionListener tripCardInteractionListener,
                                  String username) {

        this.resources = resources;
        this.tripCardInteractionListener = tripCardInteractionListener;
        this.username = username;
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

        final MyHolder tempMyHolder = (MyHolder) holder;

        if(resources.get(position).getMessageType().equals("image")){
            tempMyHolder.iv_message.setVisibility(View.VISIBLE);
            tempMyHolder.lbl_content.setVisibility(View.INVISIBLE);
            Picasso.get().load(resources.get(position).getContent()).into(tempMyHolder.iv_message);
        }
        else {
            tempMyHolder.iv_message.setVisibility(View.INVISIBLE);
            tempMyHolder.lbl_content.setVisibility(View.VISIBLE);

        }

        tempMyHolder.lbl_timestamp.setText(resources.get(position).getTimestamp().toString());
        tempMyHolder.lbl_sender.setText(resources.get(position).getSender());
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    private static class MyHolder extends RecyclerView.ViewHolder{

        View cardView;
        ImageView iv_message;
        TextView lbl_sender;
        TextView lbl_content;
        TextView lbl_timestamp;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView;
            iv_message = itemView.findViewById(R.id.iv_msg_image);
            lbl_sender = itemView.findViewById(R.id.lbl_msg_sender);
            lbl_content = itemView.findViewById(R.id.lbl_msg_content);
            lbl_timestamp = itemView.findViewById(R.id.lbl_msg_timestamp);

        }
    }
}
