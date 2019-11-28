package com.example.tripplanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SignupFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    //A user that is being created
    private User user = new User();

    private ImageView iv_dp;
    private Button btn_su_signUp;
    private Button btn_su_cancel;
    private EditText txt_su_username;
    private EditText txt_su_password;
    private EditText txt_su_repeatPassword;

    //Firebase auth instance
    private FirebaseAuth mAuth;
    //firebase DB instance
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //Request code used for starting select file intent
    private static final int REQ_IMAGE_INTENT = 300;
    //Temporary variable that holds the display pic uri
    private Uri display_pic_uri;


    public SignupFragment() {
        // Required empty public constructor
    }

    /**  Private methods **/

    private void init(){

        mAuth = FirebaseAuth.getInstance();

        txt_su_username = getActivity().findViewById(R.id.txt_su_userName);
        txt_su_password = getActivity().findViewById(R.id.txt_su_password);
        txt_su_repeatPassword = getActivity().findViewById(R.id.txt_su_confirm_password);

        btn_su_signUp = getActivity().findViewById(R.id.btn_su_signup);
        btn_su_cancel = getActivity().findViewById(R.id.btn_su_cancel);

        btn_su_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()){
                    btn_su_signUp.setEnabled(false);
                    btn_su_cancel.setEnabled(false);
                    signUpUser();
                }
            }
        });


        btn_su_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.signUp_onCancelButtonClick();
            }
        });
    }

    private void signUpUser(){

        final String email = txt_su_username.getText().toString();
        String password = txt_su_password.getText().toString();

        //Set progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Signing up...");
        progressDialog.show();

        //Create account in firebase authetication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            mListener.signUp_onSignUpSuccessful();
                        }
                        else {
                            btn_su_signUp.setEnabled(true);
                            btn_su_cancel.setEnabled(true);
                            Toast.makeText(getContext(), "Error signing up! " +
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });



    }

    private boolean validate(){

        Boolean validFlag = true;

        /*if(txt_su_fname.getText().toString().equals("") || txt_su_fname.getText().toString().equals(null)){
            txt_su_fname.setError("First name cannot be empty!");
            validFlag = false;
        }

        if(txt_su_lname.getText().toString().equals("") || txt_su_lname.getText().toString().equals(null)){
            txt_su_lname.setError("Last name cannot be empty!");
            validFlag = false;
        }*/

        if (txt_su_username.getText().toString().equals("") || txt_su_username.getText().toString().equals(null)){
            txt_su_username.setError("Username cannot be empty!");
            validFlag = false;
        }


        if (txt_su_password.getText().toString().equals("") || txt_su_password.getText().toString().equals(null)){
            txt_su_password.setError("Password cannot be empty!");
            validFlag = false;
        }

        if (txt_su_repeatPassword.getText().toString().equals("") || txt_su_repeatPassword.getText().toString().equals(null)){
            txt_su_repeatPassword.setError("Please repeat the password!");
            validFlag = false;
        }

        if (!txt_su_repeatPassword.getText().toString().equals(txt_su_password.getText().toString())){
            txt_su_password.setError("");
            txt_su_repeatPassword.setError("");
            Toast.makeText(getContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
            validFlag = false;
        }

        /*if(display_pic_uri == null){
            Toast.makeText(getContext(), "Select Display Pic!", Toast.LENGTH_SHORT).show();
            validFlag = false;
        }*/

        return validFlag;

    }

    /** Private methods end  * */

    /** Edit area    **/
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Trip Planner - Sign Up");
        init();
    }

    /** Edit area end **/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void signUp_onCancelButtonClick();
        void signUp_onSignUpSuccessful();
    }
}
