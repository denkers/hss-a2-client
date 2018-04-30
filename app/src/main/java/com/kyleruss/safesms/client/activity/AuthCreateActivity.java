//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.safesms.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.kyleruss.safesms.client.R;
import com.kyleruss.safesms.client.communication.CommUtils;
import com.kyleruss.safesms.client.communication.HTTPAsync;
import com.kyleruss.safesms.client.communication.ServiceRequest;
import com.kyleruss.safesms.client.communication.ServiceResponse;
import com.kyleruss.safesms.client.core.ClientConfig;
import com.kyleruss.safesms.client.core.KeyManager;
import com.kyleruss.safesms.client.core.User;
import com.kyleruss.hssa2.commons.EncryptedSession;
import com.kyleruss.hssa2.commons.RequestPaths;

//-----------------------------------------
//  AuthCreateActivity
//-----------------------------------------
//  Layout: R.layout.activity_auth_create
//  About: Create user accounts and request password email

public class AuthCreateActivity extends Activity
{
    private User registerUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_create);
    }

    //Sends a request to for a verification
    //password to be sent to the users email
    //Password is sent to the email user inputs
    private void sendPasswordAuthEmail()
    {
        try
        {
            String email = registerUser.getEmail();
            JsonObject requestObj = new JsonObject();
            requestObj.addProperty("email", email);

            EncryptedSession encSession =   new EncryptedSession(requestObj.toString().getBytes("UTF-8"), KeyManager.getInstance().getServerPublicKey());
            ServiceRequest request      =   CommUtils.prepareEncryptedSessionRequest(encSession);
            request.setURL(ClientConfig.CONN_URL + RequestPaths.PASS_REQ);
            request.setGet(false);

            PasswordSendTask task   =   new PasswordSendTask();
            task.execute(request);
        }

        catch(Exception e)
        {
            Log.d("PASSWORD_SEND_FAIL", e.getMessage());
        }
    }

    //Creates a temporary user from the registration fields
    //Initializes the registerUser field which is later passed
    //to the RegistrationCompleteActivity to finalize the registration
    public void createUser(View v)
    {
        String email    =   ((EditText) findViewById(R.id.regEmailField)).getText().toString();
        String phoneID  =   ((EditText) findViewById(R.id.phoneNumberField)).getText().toString();
        String name     =   ((EditText) findViewById(R.id.regUsernameField)).getText().toString();

        if(email.equals("") || name.equals("") || phoneID.equals(""))
        {
            registerUser    =   null;
            new ServiceResponse("Invalid input, please enter correct details", false).showToastResponse(this);
            return;
        }

        else
        {
            registerUser = new User(phoneID, name);
            registerUser.setEmail(email);
            sendPasswordAuthEmail();
        }
    }

    //Starts the activity to finalize registration
    //Passes the temporary registration user data
    //which will be saved if the registration is completed
    private void showCompletionActivity()
    {
        Intent intent   =   new Intent(this, RegistrationCompleteActivity.class);
        intent.putExtra("registerUser", registerUser);
        startActivity(intent);
    }


    //Response handler for email password request
    //Verifies if password email was successfully sent
    //and then transfers user to finalize the registration
    private class PasswordSendTask extends HTTPAsync
    {
        @Override
        protected void onPreExecute()
        {
            showServicingSpinner(AuthCreateActivity.this, "Creating account");
        }

        @Override
        protected void onPostExecute(String response)
        {
            hideServicingSpinner();
            JsonObject responseObj  =   CommUtils.parseJsonInput(response);

            //Check if server successfully sent the password email
            //If so, transfer user to the registration finalization
            if(responseObj.get("actionStatus").getAsBoolean())
            {
                new ServiceResponse("A verification code has been sent to your email address", true).setInfo(true).showToastResponse(AuthCreateActivity.this);
                showCompletionActivity();
            }

            else new ServiceResponse("Failed to send verification code", false).showToastResponse(AuthCreateActivity.this);
        }
    }
}
