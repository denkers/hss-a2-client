//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.communication.CommUtils;
import com.kyleruss.hssa2.client.communication.HTTPAsync;
import com.kyleruss.hssa2.client.communication.ServiceRequest;
import com.kyleruss.hssa2.client.communication.ServiceResponse;
import com.kyleruss.hssa2.client.core.ClientConfig;
import com.kyleruss.hssa2.client.core.KeyManager;
import com.kyleruss.hssa2.client.core.RequestManager;
import com.kyleruss.hssa2.client.core.User;
import com.kyleruss.hssa2.client.core.UserManager;
import com.kyleruss.hssa2.commons.CryptoCommons;
import com.kyleruss.hssa2.commons.EncryptedSession;
import com.kyleruss.hssa2.commons.Password;
import com.kyleruss.hssa2.commons.RequestPaths;

import java.net.URLEncoder;
import java.security.KeyPair;
import java.util.Map;

public class AuthCreateActivity extends Activity
{
    private User registerUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_create);
    }

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

    public void createUser(View v)
    {
        String email    =   ((EditText) findViewById(R.id.regEmailField)).getText().toString();
        String phoneID  =   ((EditText) findViewById(R.id.phoneNumberField)).getText().toString();
        String name     =   ((EditText) findViewById(R.id.regUsernameField)).getText().toString();

        if(email.equals("") || name.equals("") || phoneID.equals(""))
        {
            registerUser    =   null;
            Toast.makeText(this, "Invalid input, please try again", Toast.LENGTH_SHORT);
            return;
        }

        else
        {
            registerUser = new User(phoneID, name);
            registerUser.setEmail(email);
            sendPasswordAuthEmail();
        }
    }

    private void showCompletionActivity()
    {
        Intent intent   =   new Intent(this, RegistrationCompleteActivity.class);
        intent.putExtra("registerUser", registerUser);
        startActivity(intent);
    }


    private class PasswordSendTask extends HTTPAsync
    {
        @Override
        protected void onPreExecute()
        {
            ImageView registerControl    =   (ImageView) findViewById(R.id.registerBtn);
            showServicingSpinner(registerControl);
        }

        @Override
        protected void onPostExecute(String response)
        {
            ImageView registerControl    =   (ImageView) findViewById(R.id.registerBtn);
            hideServicingSpinner(registerControl, R.drawable.register_image);

            JsonObject responseObj  =   CommUtils.parseJsonInput(response);

            if(responseObj.get("actionStatus").getAsBoolean())
            {
                new ServiceResponse("A verification code has been sent to your email", true).setInfo(true).showToastResponse(AuthCreateActivity.this);
                showCompletionActivity();
            }

            else new ServiceResponse("Failed to send verification code", false).showToastResponse(AuthCreateActivity.this);
                //Toast.makeText(AuthCreateActivity.this, "Failed to send authentication code", Toast.LENGTH_SHORT).show();
        }
    }
}
