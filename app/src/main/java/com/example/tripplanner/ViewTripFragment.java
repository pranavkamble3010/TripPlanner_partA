package com.example.tripplanner;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.util.Base64Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewTripFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ViewTripFragment extends Fragment {

    /**Private attributes**/

    private OnFragmentInteractionListener mListener;
    private Trip trip;

    private Button btn_join;
    private Button btn_send;
    private Button btn_attach;
    private Button btn_back;
    private RecyclerView rv_chats;

    private TextView lbl_vt_tripTitle;
    private TextView lbl_vt_createdBy;
    private TextView lbl_vt_location;
    private ImageView iv_vt_tripdp;
    private EditText txt_message;

    private String currentUsername;

    /**Private attributes**/

    /**Private methods**/

    /**Private methods end**/


    /**Editable area**/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("Trip Planner - View Trip details");
        init();
    }

    private void init() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUsername = mAuth.getCurrentUser().getEmail();

        lbl_vt_tripTitle = getActivity().findViewById(R.id.lbl_vt_tripTitle);
        lbl_vt_createdBy = getActivity().findViewById(R.id.lbl_vt_createdBy);
        lbl_vt_location = getActivity().findViewById(R.id.lbl_vt_tripLocation);
        lbl_vt_tripTitle = getActivity().findViewById(R.id.lbl_vt_tripTitle);
        iv_vt_tripdp = getActivity().findViewById(R.id.iv_vt_dp);
        btn_send = getActivity().findViewById(R.id.btn_vt_send);
        txt_message = getActivity().findViewById(R.id.txt_message);

        btn_join = getActivity().findViewById(R.id.btn_vt_join);
        Picasso.get().load(trip.getImageUrl()).into(iv_vt_tripdp);

        lbl_vt_tripTitle.setText(trip.getTitle());
        lbl_vt_location.setText(trip.getLocation());
        lbl_vt_createdBy.setText("Created by: " + trip.getCreatedBy());

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btn_back = getActivity().findViewById(R.id.btn_vt_back);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onBackButtonClicked();
            }
        });

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

                    /*HashMap<String,Message> msg = new HashMap<>();
                    msg.put(message.getTimestamp(),message);*/

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


    }


    public ViewTripFragment(Trip trip) {
        // Required empty public constructor
        this.trip = trip;
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
        void onFragmentInteraction(Uri uri);
        void onBackButtonClicked();
    }
}
