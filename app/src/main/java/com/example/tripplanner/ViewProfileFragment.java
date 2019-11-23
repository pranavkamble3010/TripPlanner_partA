package com.example.tripplanner;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ViewProfileFragment extends Fragment implements TripsRecyclerAdapter.TripCardInteractionListener{

    /**Private attributes**/

    private OnFragmentInteractionListener mListener;
    private TextView lbl_profile_header;
    private ImageView iv_vp_dp;

    private User user;
    private Button btn_editProfile;
    private Button btn_addTrip;
    private Button btn_signout;
    private TextView lbl_name;
    private TextView lbl_gender;
    private TextView lbl_username;

    private RecyclerView rv_trips;
    private TripsRecyclerAdapter tripsAdapter;
    private List<Trip> trips;


    /**Private attributes end**/

    /**Private methods**/

    private void init(){

        lbl_profile_header = getActivity().findViewById(R.id.lbl_viewProfileHeader);
        lbl_name = getActivity().findViewById(R.id.lbl_vp_name);
        lbl_gender = getActivity().findViewById(R.id.lbl_vp_gender);
        lbl_username = getActivity().findViewById(R.id.lbl_vp_username);
        iv_vp_dp = getActivity().findViewById(R.id.iv_vp_dp);
        btn_editProfile = getActivity().findViewById(R.id.btn_editProfile);
        rv_trips = getActivity().findViewById(R.id.rv_vp_trips);

        //init trips
        trips = new ArrayList<>();

        //Get logged in user's information from the authentication
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Get user details from database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("profiles")
                .document(currentUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot doc = task.getResult();
                            if(doc.exists()){
                                user = new User((HashMap<String, Object>) doc.getData());
                                //Load header
                                lbl_profile_header.setText("Welcome, "+ user.getFname()+"!");
                                //Load display pic
                                Picasso.get().load(user.getImageUrl()).into(iv_vp_dp);
                                lbl_name.setText(user.getFname()+" "+user.getLname());
                                lbl_gender.setText(user.getGender());
                                lbl_username.setText(user.getUsername());

                                //Populate trips data
                                populateRecyclerView();
                            }
                        }
                    }
                });


        btn_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.editProfileClicked();
            }
        });

        btn_addTrip = getActivity().findViewById(R.id.btn_add_trip);

        btn_addTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.addTripClicked();
            }
        });

        btn_signout = getActivity().findViewById(R.id.btn_logout);

        btn_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                Toast.makeText(getContext(), "User signed out successfully!", Toast.LENGTH_SHORT).show();
                mListener.signoutClicked();
            }
        });
    }

    private void populateRecyclerView() {

        tripsAdapter = new TripsRecyclerAdapter(trips,
                            this,
                                                user.getUsername());

        rv_trips.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_trips.setAdapter(tripsAdapter);
        populateTrips();
    }

    private void populateTrips() {

        trips.removeAll(trips);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("trips")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc: task.getResult()) {
                                trips.add(new Trip((HashMap<String, Object>) doc.getData()));
                            }
                            tripsAdapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(getContext(), "Error loading trips!", Toast.LENGTH_SHORT).show();
                            Log.d("ViewProfileFrag", "onComplete: Error getting trips data");
                        }
                    }
                });
    }
    /**Private methods end**/

    /**Edit area**/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Trip Planner");
        init();
    }

    /**Edit area end**/

    public ViewProfileFragment() {
        // Required empty public constructor
        //user = new User();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_profile, container, false);
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
    public void onJoinButtonClick(String tripTitle) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        //Add the trip in the user
        DocumentReference userUpdateRef = db.collection("profiles").
                document(user.getUsername());
        batch.update(userUpdateRef, "tripsAddedTo", FieldValue.arrayUnion(tripTitle));


        DocumentReference tripUpdateRef = db.collection("trips").
                document(tripTitle);
        batch.update(tripUpdateRef, "members", FieldValue.arrayUnion(user.getUsername()));


        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getContext(), "Trip joined successfully!", Toast.LENGTH_SHORT).show();
                    populateTrips();
                }

            }
        });

    }

    @Override
    public void onTripCardClick(Trip trip) {

        Log.d("viewProfileFrag", "onTripCardClick: "+trip.toString());

        mListener.tripCardClicked(trip);
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
        void editProfileClicked();
        void addTripClicked();
        void signoutClicked();
        void tripCardClicked(Trip trip);
    }
}
