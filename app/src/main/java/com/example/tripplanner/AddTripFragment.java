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

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddTripFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AddTripFragment extends Fragment {

    /**Private attributes**/
    private OnFragmentInteractionListener mListener;
    private ImageView iv_trip_dp;
    private EditText txt_trip_title;
    private EditText txt_trip_location;
    private Button btn_trip_save;
    private Button btn_trip_cancel;
    private TextView lbl_add_user;
    private TextView lbl_users_added;

    private static final int REQ_IMAGE_INTENT = 300;
    private Uri trip_dp_uri;
    private ArrayList<User> users;
    private String userNames[];
    private AlertDialog alertDialog;
    private Trip trip;
    private String currentUser;
    private ProgressBar pb_loader;
    /**Private attributes end**/

    /**Private methods**/
    private void init(){

        trip = new Trip();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getEmail();
        trip.setCreatedBy(currentUser);

        txt_trip_title = getActivity().findViewById(R.id.txt_trip_title);
        txt_trip_location = getActivity().findViewById(R.id.txt_trip_location);
        btn_trip_save = getActivity().findViewById(R.id.btn_save_trip);
        btn_trip_cancel = getActivity().findViewById(R.id.btn_trip_save_cancel);
        pb_loader = getActivity().findViewById(R.id.pb_loader);

        iv_trip_dp = getActivity().findViewById(R.id.iv_trip_dp);

        btn_trip_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onTripSaveCanceled();
            }
        });

        btn_trip_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validate()){
                    
                    saveTripPhase1();
                    
                }

            }
        });

        iv_trip_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,REQ_IMAGE_INTENT);
            }
        });

        lbl_add_user = getActivity().findViewById(R.id.lbl_trip_members);
        lbl_users_added = getActivity().findViewById(R.id.lbl_users_added);

        lbl_add_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                lbl_add_user.setEnabled(false);
                populateAllUsersAndShowDialog();

            }
        });

    }

    private void saveTripPhase1() {

        disableAll();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        Bitmap image = null;
        try
        {
            image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), trip_dp_uri);
            Log.d("dp_uri", "signUpUser: dp_uri"+trip_dp_uri);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        StorageReference rootDirRef = firebaseStorage.getReference().child("Trip_cover_pics");
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
                    saveTripPhase2(task.getResult().toString());
                }
            }
        });



    }

    private void disableAll() {

        pb_loader.setVisibility(View.VISIBLE);
        txt_trip_title.setEnabled(false);
        txt_trip_location.setEnabled(false);
        btn_trip_save.setEnabled(false);
        btn_trip_cancel.setEnabled(false);
    }

    private void enableAll() {
        pb_loader.setVisibility(View.INVISIBLE);
        txt_trip_title.setEnabled(true);
        txt_trip_location.setEnabled(true);
        btn_trip_save.setEnabled(true);
        btn_trip_cancel.setEnabled(true);
    }

    private void saveTripPhase2(String imageUrl) {

        trip.setTitle(txt_trip_title.getText().toString());
        trip.setLocation(txt_trip_location.getText().toString());
        trip.setImageUrl(imageUrl);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("trips")
                .document(trip.getTitle())
                .set(trip)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //1. Save the trip
                        Log.d("Update profile", "Trip saved successfully!"+trip.toString());
                        Toast.makeText(getContext(),
                                "Trip saved successfully!", Toast.LENGTH_SHORT).show();

                        //2. batch Update all users which are added to the trip
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        WriteBatch batch = db.batch();

                        //For all users added to trip, get username and batch update the documents
                        for (String user:trip.getMembers()) {
                            DocumentReference userUpdateRef = db.collection("profiles").
                                    document(user);
                            batch.update(userUpdateRef,
                                    "tripsAddedTo",
                                    FieldValue.arrayUnion(trip.getTitle()));
                        }

                        //Also add the trip in the initiator user
                        DocumentReference userUpdateRef = db.collection("profiles").
                                document(currentUser);
                        batch.update(userUpdateRef,
                                "tripsAddedTo",
                                FieldValue.arrayUnion(trip.getTitle()));


                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                enableAll();
                                mListener.onTripSaved();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error updating trip!", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private boolean validate() {

        boolean validFlag = true;

        if(txt_trip_title.getText().toString().equals("") || txt_trip_title.getText().toString().equals(null)){
            txt_trip_title.setError("Trip title cannot be empty!");
            validFlag = false;
        }

        if(txt_trip_location.getText().toString().equals("") || txt_trip_location.getText().toString().equals(null)){
            txt_trip_location.setError("Trip location cannot be empty!");
            validFlag = false;
        }

        if(trip_dp_uri == null){
            Toast.makeText(getContext(), "Select Trip cover photo!", Toast.LENGTH_SHORT).show();
            validFlag = false;
        }

        return validFlag;
    }


    private void populateAllUsersAndShowDialog()
    {
        users = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("profiles")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d("AddTripFragment", "user: "+document.getData());
                                User user = new User( (HashMap<String, Object>) document.getData());
                                if(!user.getUsername().equals(currentUser)){
                                    users.add(user);
                                }
                            }
                            Log.d("AddTripFragment", "populateAllUsersAndShowDialog: "+users.size());

                            userNames = new String[users.size()];
                            int i = 0;
                            for (User user:users) {
                                userNames[i++] = user.getFname()+" "+user.getLname();
                            }

                            lbl_add_user.setEnabled(true);
                            showDialog();
                        }
                        else {
                            Toast.makeText(getContext(), "Error loading users!", Toast.LENGTH_SHORT).show();
                            Log.d("AddTripFragment", "onComplete: Error getting users data");
                        }
                    }
                });



    }

    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose a Keyword")

                .setItems(userNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(!lbl_users_added.getText().toString().contains(userNames[i])){
                            lbl_users_added.append(userNames[i]+"\n");

                            //Add trip to user first and then add the user to the trip's user list
                            trip.addMember(users.get(i).addTrip(trip.getTitle()).getUsername());
                        }

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.cancel();
                    }
                });

        alertDialog = builder.create();
        alertDialog.show();

    }


    /**Private methods end**/

    /**Editable area**/
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("Trip Planner - Add trip");
        init();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQ_IMAGE_INTENT){

                //Get the image and upload to the firebase storage
                trip_dp_uri = data.getData();
                Picasso.get().load(trip_dp_uri).into(iv_trip_dp);
                //imageUpdated = true;
            }
        }
    }

    /**Editable area end**/

    public AddTripFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_trip, container, false);
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
        void onTripSaved();
        void onTripSaveCanceled();
    }
}
