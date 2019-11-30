package com.example.tripplanner;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LoginFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Button btn_signUp;
    private Button btn_login;

    private EditText txt_username;
    private EditText txt_password;


    public LoginFragment() {
        // Required empty public constructor
    }



    /**  Private methods    * */

    private void init(){


        txt_username = getActivity().findViewById(R.id.txt_userName);
        txt_password = getActivity().findViewById((R.id.txt_password));

        btn_signUp = getActivity().findViewById(R.id.btn_signup);
        btn_login = getActivity().findViewById(R.id.btn_login);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(validate()){
                   btn_login.setEnabled(false);
                   btn_signUp.setEnabled(false);
                   login();
               }
            }
        });


        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.login_OnSignUpButtonClick();
            }
        });
    }

    private void login(){

        //start progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Authenticating user");
        progressDialog.show();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email = txt_username.getText().toString();
        String password = txt_password.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Login", "signInWithEmail:success");
                            progressDialog.dismiss();
                            mListener.login_OnLoginSuccessful();
                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w("Login", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            btn_login.setEnabled(true);
                            btn_signUp.setEnabled(true);
                        }

                        // ...
                    }
                });


    }

    private boolean validate(){

        Boolean validFlag = true;

        String emailChecker = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailChecker);

        if (txt_username.getText().toString().equals("") || txt_username.getText().toString().equals(null)){
            txt_username.setError("Username cannot be empty!");
            validFlag = false;
        } else if(!pattern.matcher(txt_username.getText().toString().trim()).matches()){
                txt_username.setError("Invalid Email");
                validFlag = false;
            }

        if (txt_password.getText().toString().equals("") || txt_password.getText().toString().equals(null)){
            txt_password.setError("Password cannot be empty!");
            validFlag = false;
        } else if(txt_password.getText().toString().trim().length() < 8){
            txt_password.setError("Password must be of 8 letters");
            validFlag = false;
        }

        return validFlag;

    }

    /** Private methods end  * */


    /** Edit area    **/
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void login_OnSignUpButtonClick();
        void login_OnLoginSuccessful();
    }




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Trip Planner - Login");
        init();

    }
    /**  Edit area ends   **/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
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


}
