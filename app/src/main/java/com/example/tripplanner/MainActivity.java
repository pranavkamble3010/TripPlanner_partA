package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener,
        SignupFragment.OnFragmentInteractionListener,
ProfileBuilderFragment.OnFragmentInteractionListener,
ViewProfileFragment.OnFragmentInteractionListener,
AddTripFragment.OnFragmentInteractionListener{

    /** * Private attributes* **/
    public static final int REQ_LOAD_LOGIN_FRAGMENT = 100;
    public static final int REQ_LOAD_SIGNUP_FRAGMENT = 101;
    public static final int REQ_LOAD_UPDATE_FRAGMENT_AFTER_SIGNUP = 102;
    public static final int REQ_LOAD_VIEW_PROFILE_FRAGMENT = 103;
    public static final int REQ_LOAD_UPDATE_FRAGMENT = 104;
    public static final int REQ_LOAD_ADD_TRIP_FRAGMENT = 105;
    public static final int REQ_LOGIN_SUCCESSFUL = 200;


    /** * Private methods* **/

    /**
     * Handler class
     * */

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {

            switch (msg.what){
                case REQ_LOAD_LOGIN_FRAGMENT:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainActivity_container,new LoginFragment(),"login_fragment")
                            .commit();
                    break;

                case REQ_LOAD_SIGNUP_FRAGMENT:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainActivity_container,new SignupFragment(),"signup_fragment")
                            .commit();
                    break;

                case REQ_LOAD_UPDATE_FRAGMENT_AFTER_SIGNUP:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainActivity_container,new ProfileBuilderFragment(true),"updateProfile_fragment_after_signup")
                            .commit();
                    break;

                case REQ_LOAD_UPDATE_FRAGMENT:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainActivity_container,new ProfileBuilderFragment(false),"updateProfile_fragment")
                            .commit();
                    break;

                case REQ_LOAD_VIEW_PROFILE_FRAGMENT:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainActivity_container,new ViewProfileFragment(),"viewProfile_fragment")
                            .commit();
                    break;

                case REQ_LOAD_ADD_TRIP_FRAGMENT:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainActivity_container,new AddTripFragment(),"addTrip_fragment")
                            .commit();
                    break;

            }
        }
    };


    /** * Private methods* **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Trip Planner - Login");

        //Load login fragment at first
        Message msg = handler.obtainMessage(REQ_LOAD_LOGIN_FRAGMENT);
        msg.sendToTarget();
    }

    @Override
    public void login_OnSignUpButtonClick() {
        Message msg = handler.obtainMessage(REQ_LOAD_SIGNUP_FRAGMENT);
        msg.sendToTarget();
    }

    @Override
    public void login_OnLoginSuccessful() {
        Message msg = handler.obtainMessage(REQ_LOAD_VIEW_PROFILE_FRAGMENT);
        msg.sendToTarget();

    }

    @Override
    public void signUp_onCancelButtonClick() {
        Message msg = handler.obtainMessage(REQ_LOAD_LOGIN_FRAGMENT);
        msg.sendToTarget();
    }

    @Override
    public void signUp_onSignUpSuccessful() {
        Toast.makeText(this, "User signed up successfully!", Toast.LENGTH_SHORT).show();
        Message msg = handler.obtainMessage(REQ_LOAD_UPDATE_FRAGMENT_AFTER_SIGNUP);
        msg.sendToTarget();
    }

    @Override
    public void onProfileSaved() {
        Message msg = handler.obtainMessage(REQ_LOAD_VIEW_PROFILE_FRAGMENT);
        msg.sendToTarget();
    }

    @Override
    public void onEditProfile_Cancel_Click() {
        Message msg = handler.obtainMessage(REQ_LOAD_VIEW_PROFILE_FRAGMENT);
        msg.sendToTarget();
    }

    @Override
    public void editProfileClicked() {
        Message msg = handler.obtainMessage(REQ_LOAD_UPDATE_FRAGMENT);
        msg.sendToTarget();
    }

    @Override
    public void addTripClicked() {
        Message msg = handler.obtainMessage(REQ_LOAD_ADD_TRIP_FRAGMENT);
        msg.sendToTarget();

    }

    @Override
    public void signoutClicked() {
        Message msg = handler.obtainMessage(REQ_LOAD_LOGIN_FRAGMENT);
        msg.sendToTarget();
    }

    @Override
    public void onTripSaved() {
        Message msg = handler.obtainMessage(REQ_LOAD_VIEW_PROFILE_FRAGMENT);
        msg.sendToTarget();
    }

    @Override
    public void onTripSaveCanceled() {
        Message msg = handler.obtainMessage(REQ_LOAD_VIEW_PROFILE_FRAGMENT);
        msg.sendToTarget();

    }
}
