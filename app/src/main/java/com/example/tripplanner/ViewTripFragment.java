package com.example.tripplanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewTripFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ViewTripFragment extends Fragment implements MessageRecyclerAdapter.MessageInteractionListener{


    /**Private attributes**/
    private static final int REQ_IMAGE_INTENT = 300;

    private OnFragmentInteractionListener mListener;
    private Trip trip;

    private Button btn_join;
    private Button btn_send;
    private Button btn_attach;
    private Button btn_back;

    private MessageRecyclerAdapter messageRecyclerAdapter;
    private RecyclerView rv_chats;
    private List<Message> chats;

    private TextView lbl_vt_tripTitle;
    private TextView lbl_vt_createdBy;
    private TextView lbl_vt_location;
    private ImageView iv_vt_tripdp;
    private EditText txt_message;

    private String currentUsername;

    //To store snapshot listener which is used to detach if trip is left by the creator
    private ListenerRegistration chatListnerResgistration;

    /**Private attributes end**/

    /**Private methods**/
    private void init() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUsername = mAuth.getCurrentUser().getEmail();

        lbl_vt_tripTitle = getActivity().findViewById(R.id.lbl_vt_tripTitle);
        lbl_vt_createdBy = getActivity().findViewById(R.id.lbl_vt_createdBy);
        lbl_vt_location = getActivity().findViewById(R.id.lbl_vt_tripLocation);
        lbl_vt_tripTitle = getActivity().findViewById(R.id.lbl_vt_tripTitle);

        btn_send = getActivity().findViewById(R.id.btn_vt_send);
        btn_join = getActivity().findViewById(R.id.btn_vt_join);
        btn_back = getActivity().findViewById(R.id.btn_vt_back);
        btn_attach = getActivity().findViewById(R.id.btn_vt_attachImage);

        txt_message = getActivity().findViewById(R.id.txt_message);

        rv_chats = getActivity().findViewById(R.id.rv_vt_chatsView);

        iv_vt_tripdp = getActivity().findViewById(R.id.iv_vt_dp);
        Picasso.get().load(trip.getImageUrl()).into(iv_vt_tripdp);

        //Set values
        lbl_vt_tripTitle.setText(trip.getTitle());
        lbl_vt_location.setText(trip.getLocation());
        lbl_vt_createdBy.setText("Created by: " + trip.getCreatedBy());

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(btn_join.getText().toString().equalsIgnoreCase("leave")){

                    leaveTrip();
                }

                else {
                    joinTrip();
                    enableChatroom();
                }

            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onBackButtonClicked();
            }
        });

        //Set btn_send onclick
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!txt_message.getText().equals("") || !txt_message.getText().equals(null)){
                    final Message message = new Message();
                    message.setContent(txt_message.getText().toString());
                    message.setMessageType("text");
                    message.setSender(currentUsername);
                    //set timestamp at the time of sending the message
                    message.setTimestamp(new Timestamp(new Date().getTime()).toString());

                    txt_message.setText("");

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("chatrooms")
                            .document(trip.getTitle())
                            .update("messages",FieldValue.arrayUnion(message.getMessageMap()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("viewtripfrag", "onSuccess: "+message);
                                }
                            });
                }
            }
        });

        if(isUserMemberOfCurrentTrip()){
            enableChatroom();
        }
        else {
            disableChatroom();
        }



        //set btn_attach onClick
        btn_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,REQ_IMAGE_INTENT);
            }
        });
    }

    private void joinTrip() {
        //Add the trip in the user
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();
        DocumentReference userUpdateRef = db.collection("profiles").
                document(currentUsername);
        batch.update(userUpdateRef, "tripsAddedTo", FieldValue.arrayUnion(trip.getTitle()));

        //Add user to trip
        DocumentReference tripUpdateRef = db.collection("trips").
                document(trip.getTitle());
        batch.update(tripUpdateRef, "members", FieldValue.arrayUnion(currentUsername));


        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getContext(), "Trip joined successfully!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void leaveTrip() {

        if(currentUsername.equals(trip.getCreatedBy())){

            final AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(getContext());
            confirmationDialog.setTitle("Caution!");
            confirmationDialog.setMessage("You are the creator of this trip." +
                    "Leaving the trip will permanently delete all the trip information including chats." +
                    "Are you sure to leave this trip and delete all its contents?");
            confirmationDialog.setPositiveButton("YES, Leave!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    WriteBatch batch = db.batch();



                    //Remove snapshotListener on chats
                    chatListnerResgistration.remove();

                    //Remove trips from all users added
                    //For all users added to trip, get username and batch update the documents
                    for (String user:trip.getMembers()) {
                        DocumentReference userUpdateRef = db.collection("profiles").
                                document(user);
                        batch.update(userUpdateRef,
                                "tripsAddedTo",
                                FieldValue.arrayRemove(trip.getTitle()));
                    }

                    //Remove the chatroom
                    DocumentReference chatroomDeleteRef = db.collection("chatrooms").
                            document(trip.getTitle());
                    batch.delete(chatroomDeleteRef);

                    //Remove the trip
                    DocumentReference tripDeleteRef = db.collection("trips").
                            document(trip.getTitle());
                    batch.delete(tripDeleteRef);


                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getContext(), "Trip removed successfully!", Toast.LENGTH_SHORT).show();
                                mListener.onTripLeftByCreator();
                            }
                        }
                    });

                }});
            confirmationDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface arg0, int arg1) {

                }});
            confirmationDialog.show();
        }
        //If current user is not a creator, remove user from the trip only
        else{
            final AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(getContext());
            confirmationDialog.setTitle("Confirm Action");
            confirmationDialog.setMessage("Are you sure to leave this trip?");
            confirmationDialog.setPositiveButton("YES, Leave!", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface arg0, int arg1) {

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    //remove member from the trip
                    db.collection("trips").document(trip.getTitle())
                            .update("members",FieldValue.arrayRemove(currentUsername));

                    //Remove trip from user profile
                    db.collection("profiles").document(currentUsername)
                            .update("tripsAddedTo",FieldValue.arrayRemove(trip.getTitle()));

                    //Disable chatroom
                    disableChatroom();
                }});
            confirmationDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface arg0, int arg1) {

                }});
            confirmationDialog.show();
        }
    }

    private void populateChats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference doc = db.collection("chatrooms")
                .document(trip.getTitle());
        chatListnerResgistration = doc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null){
                            return;
                        }
                        else {
                            chats.clear();
                            ArrayList<Object> realtimeChats = (ArrayList<Object>)documentSnapshot.getData().get("messages");
                            Log.d("Realtime", "onEvent: new msgs"+realtimeChats);
                            //chats.addAll(realtimeChats);
                            for (Object message:realtimeChats) {
                                Message temp = new Message((HashMap<String, Object>) message);
                                chats.add(temp);
                            }
                            messageRecyclerAdapter.notifyDataSetChanged();
                            rv_chats.smoothScrollToPosition(chats.size()-1);
                        }
                    }
                });
    }

    private void sendImage(Uri imageUri) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        Bitmap image = null;
        try
        {
            image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            //Log.d("dp_uri", "signUpUser: dp_uri"+imageUri);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        StorageReference rootDirRef = firebaseStorage.getReference().child("chatroom_shared_pics");
        final StorageReference imageRef = rootDirRef.child(java.util.UUID.randomUUID().toString()+".png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG,50,baos);

        byte[] imageByteArray = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(imageByteArray);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                else
                    return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    final Message message = new Message();
                    //Set image Url in the message content
                    message.setContent(task.getResult().toString());
                    message.setMessageType("image");
                    message.setSender(currentUsername);
                    //set timestamp at the time of sending the message
                    message.setTimestamp(new Timestamp(new Date().getTime()).toString());

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("chatrooms")
                            .document(trip.getTitle())
                            .update("messages",FieldValue.arrayUnion(message.getMessageMap()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("viewtripfrag", "onSuccess: image updated");
                                }
                            });
                }
            }
        });
    }

    private boolean isUserMemberOfCurrentTrip(){

        if(trip.getMembers().contains(currentUsername))
            return true;
        return false;
    }

    private void enableChatroom(){
        txt_message.setVisibility(View.VISIBLE);
        btn_send.setVisibility(View.VISIBLE);
        btn_attach.setVisibility(View.VISIBLE);
        rv_chats.setVisibility(View.VISIBLE);
        btn_join.setText("leave");
        getActivity().findViewById(R.id.lbl_alertToJoinTrip).setVisibility(View.GONE);

        chats = new ArrayList<>();
        messageRecyclerAdapter = new MessageRecyclerAdapter(chats,this,currentUsername);
        rv_chats = getActivity().findViewById(R.id.rv_vt_chatsView);
        rv_chats.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_chats.setAdapter(messageRecyclerAdapter);
        populateChats();

    }

    private void disableChatroom(){
        getActivity().findViewById(R.id.lbl_alertToJoinTrip).setVisibility(View.VISIBLE);
        txt_message.setVisibility(View.GONE);
        btn_send.setVisibility(View.GONE);
        btn_attach.setVisibility(View.GONE);
        btn_join.setText("join");
        rv_chats.setVisibility(View.GONE);
    }

    /**Private methods end**/


    /**Editable area**/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("Trip Planner - View Trip Details");
        init();
    }




    public ViewTripFragment(Trip trip) {
        // Required empty public constructor
        this.trip = trip;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQ_IMAGE_INTENT){

                //Get the image and upload to the firebase storage
                final Uri attachment = data.getData();

                final AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(getContext());
                confirmationDialog.setTitle("Confirm Action");
                confirmationDialog.setMessage("Are you sure to attach this image?");
                confirmationDialog.setPositiveButton("YES, Send!", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        sendImage(attachment);
                    }});
                confirmationDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {

                    }});
                confirmationDialog.show();

                //imageUpdated = true;
            }
        }
    }



    /**Editable area end**/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_trip, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onChatClick(String tripTitle) {

    }

    @Override
    public void onTripCardClick(Trip trip) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onTripLeftByCreator();
        void onBackButtonClicked();
    }
}
