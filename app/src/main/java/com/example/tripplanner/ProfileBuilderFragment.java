package com.example.tripplanner;

import android.app.Activity;
import android.content.Context;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileBuilderFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ProfileBuilderFragment extends Fragment {

    /**Private attributes**/
    private boolean afterSignup;
    private OnFragmentInteractionListener mListener;
    private TextView lbl_optional;
    private EditText txt_su_fname;
    private EditText txt_su_lname;
    private ImageView iv_dp;
    private Button btn_save;
    private Button btn_cancel;
    private RadioGroup rg_gender;
    private RadioButton rb_male;
    private RadioButton rb_female;

    private static final int REQ_IMAGE_INTENT = 300;
    private Uri display_pic_uri;
    private User user;
    private boolean imageUpdated = false;
    /**Private attributes end **/

    /**Private methods**/
    private void init(){

        //Get logged in user's information from the authentication
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        user.setUsername(currentUser.getEmail());

        lbl_optional = getActivity().findViewById(R.id.lbl_optional_step2);
        txt_su_fname = getActivity().findViewById(R.id.txt_fname2);
        txt_su_lname = getActivity().findViewById(R.id.txt_lname2);
        iv_dp = getActivity().findViewById(R.id.iv_dp2);
        btn_save = getActivity().findViewById(R.id.btn_save);
        btn_cancel = getActivity().findViewById(R.id.btn_cancel);

        rb_male = getActivity().findViewById(R.id.rb_male);
        rb_female = getActivity().findViewById(R.id.rb_female);


        if(afterSignup == false){
            //Get logged in user's information from the authentication

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
                                    //Load display pic
                                    Picasso.get().load(user.getImageUrl()).into(iv_dp);
                                    txt_su_fname.setText(user.getFname());
                                    txt_su_lname.setText(user.getLname());
                                    display_pic_uri = Uri.parse(user.getImageUrl());
                                    if (user.getGender().equalsIgnoreCase("male")){
                                        rb_male.setChecked(true);
                                    }
                                    else {
                                        rb_female.setChecked(true);
                                    }
                                }
                            }
                        }
                    });
        }


        rg_gender = getActivity().findViewById(R.id.rg_gender);
        rg_gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i){
                    case R.id.rb_male:
                        user.setGender("Male");
                        break;

                    case R.id.rb_female:
                        user.setGender("Female");
                        break;
                }

            }
        });

        iv_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getActivity(), "IV clicked", Toast.LENGTH_SHORT).show();

                //Start intent to get image from the gallery
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,REQ_IMAGE_INTENT);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfilePhase1();

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onEditProfile_Cancel_Click();
            }
        });
    }

    private boolean validate(){

        Boolean validFlag = true;

        if(txt_su_fname.getText().toString().equals("") || txt_su_fname.getText().toString().equals(null)){
            txt_su_fname.setError("First name cannot be empty!");
            validFlag = false;
        }

        if(txt_su_lname.getText().toString().equals("") || txt_su_lname.getText().toString().equals(null)){
            txt_su_lname.setError("Last name cannot be empty!");
            validFlag = false;
        }

        if(display_pic_uri == null){
            Toast.makeText(getContext(), "Select Display Pic!", Toast.LENGTH_SHORT).show();
            validFlag = false;
        }

        return validFlag;

    }

    /**
     * We are completing the Profile saving process in 2 phases -
     * 1. Upload the display pic to the firebase storage
     * 2. If 1 successful, Create/update new profile in the firebase database
     * */
    private void updateProfilePhase1() {
        //Check if image was updated. This check is required for 'edit profile' option.
        if(imageUpdated){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            Bitmap image = null;
            try
            {
                image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), display_pic_uri);
                Log.d("dp_uri", "signUpUser: dp_uri"+display_pic_uri);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            StorageReference rootDirRef = firebaseStorage.getReference().child("Display_pics");
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
                        updateProfilePhase2(task.getResult().toString());
                    }
                }
            });
        }

        else {
            updateProfilePhase2(user.getImageUrl());
        }

    }

    private void updateProfilePhase2(String downloadUrl){
        user.setImageUrl(downloadUrl);
        user.setFname(txt_su_fname.getText().toString());
        user.setLname(txt_su_lname.getText().toString());
        //firebase DB instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("profiles")
                .document(user.getUsername())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Update profile", "Profile saved successfully!"+user.toString());
                        Toast.makeText(getContext(),
                                "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                        mListener.onProfileSaved();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error updating profile!", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    /**Private methods end **/

    /**Edit area**/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        if(afterSignup){
            getActivity().setTitle("Trip Planner - Complete profile setup");
            lbl_optional = getActivity().findViewById(R.id.lbl_optional_step2);
            lbl_optional.setText("Step 2: Complete your profile");
            btn_cancel.setVisibility(View.INVISIBLE);
        }
        else{
            getActivity().setTitle("Trip Planner - Edit profile");
            lbl_optional = getActivity().findViewById(R.id.lbl_optional_step2);
            lbl_optional.setText("Edit Profile");
            btn_cancel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQ_IMAGE_INTENT){

                //Get the image and upload to the firebase storage
                display_pic_uri = data.getData();
                Picasso.get().load(display_pic_uri).into(iv_dp);
                imageUpdated = true;
            }


        }
    }

    /**Edit area ends**/

    public ProfileBuilderFragment(boolean afterSignup) {
        // Required empty public constructor
        this.afterSignup = afterSignup;
        this.user = new User();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_builder, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onProfileSaved();
        }
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
        void onProfileSaved();
        void onEditProfile_Cancel_Click();
    }
}
