//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import com.kyleruss.hssa2.commons.CryptoUtils;
import com.kyleruss.hssa2.commons.EncryptedSession;
import com.kyleruss.hssa2.commons.RequestPaths;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;

import javax.crypto.Cipher;

import static com.kyleruss.hssa2.client.communication.CommUtils.parseJsonInput;

//-----------------------------------------
//  ConnectActivity
//-----------------------------------------
//Layout: R.layout.activity_connect
//About: Allow users to connect to the server

public class ConnectActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        getServerPublicKey();
    }

    //Sends a request to fetch the servers public key
    public void getServerPublicKey()
    {
        String url                  =   ClientConfig.CONN_URL + RequestPaths.SERV_PUBLIC_GET_REQ;
        ServiceRequest request      =   new ServiceRequest(url, true);
        ServerPublicFetchTask task  =   new ServerPublicFetchTask();
        task.execute(request);
    }

    //Sends a request to connect to the server
    //Passes the phone id/number as param to identify
    //Request is authenticated and response will need to be verified
    public void connectToServer(View v)
    {
        try
        {
            Map.Entry<String, String> authRequest   =   RequestManager.getInstance().generateRequest();
            JsonObject authObj                      =   CommUtils.prepareAuthenticatedRequest(authRequest.getKey(), authRequest.getValue());
            authObj.addProperty("phoneID", UserManager.getInstance().getPhoneID(this));

            EncryptedSession encSession =   new EncryptedSession(authObj.toString().getBytes("UTF-8"), KeyManager.getInstance().getServerPublicKey());
            ServiceRequest request      =   CommUtils.prepareEncryptedSessionRequest(encSession);
            request.setURL(ClientConfig.CONN_URL + RequestPaths.SERV_CONNECT_REQ);
            request.setGet(false);

            ConnectTask task    =   new ConnectTask();
            task.execute(request);
        }

        catch(Exception e)
        {
            Log.d("CONNECT_FAIL", e.getMessage());
            new ServiceResponse("Failed to connect to the server", false).showToastResponse(ConnectActivity.this);
        }
    }

    public void startAuthCreateActivity(View v)
    {
        Intent intent   =   new Intent(this, AuthCreateActivity.class);
        startActivity(intent);
    }

    private void startHomeActivity()
    {
        Intent intent   =   new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    //Response handler for server connect request
    //Verifies the response and checks if user successfully connected
    //Initializes the active user and then transfers to the home activity
    private class ConnectTask extends HTTPAsync
    {
        @Override
        protected  void onPreExecute()
        {
            showServicingSpinner(ConnectActivity.this, "Signing in");
        }

        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                hideServicingSpinner();

                JsonObject responseObj  =   CommUtils.parseJsonInput(response);
                byte[] key              =   Base64.decode(responseObj.get("key").getAsString(), Base64.DEFAULT);
                byte[] data             =   Base64.decode(responseObj.get("data").getAsString(), Base64.DEFAULT);
                KeyPair clientKeyPair = KeyManager.getInstance().getClientKeyPair();

                //client public key pair should have been generated or loaded from storage
                if (clientKeyPair != null)
                {
                    EncryptedSession encSession = new EncryptedSession(key, data, clientKeyPair.getPrivate());
                    encSession.unlock();
                    JsonObject decryptedResponse = CommUtils.parseJsonInput(new String(encSession.getData()));

                    //check if user successfully connected
                    if(decryptedResponse.get("status").getAsBoolean())
                    {
                        String requestID    =   decryptedResponse.get("requestID").getAsString();
                        String nonce        =   decryptedResponse.get("nonce").getAsString();

                        //verify response
                        if(RequestManager.getInstance().verifyAndDestroy(requestID, nonce))
                        {
                            //initialize the active user
                            User nextUser       =   new User(decryptedResponse.get("phoneID").getAsString(), decryptedResponse.get("name").getAsString());
                            nextUser.setEmail(decryptedResponse.get("email").getAsString());
                            byte[] profileImage =   decryptedResponse.has("profileImage")? Base64.decode(decryptedResponse.get("profileImage").getAsString(), Base64.DEFAULT) : null;
                            nextUser.setProfileImage(profileImage);
                            UserManager.getInstance().setActiveUser(nextUser);

                            new ServiceResponse("Successfully connected", true).showToastResponse(ConnectActivity.this);
                            startHomeActivity();
                        }

                        else new ServiceResponse("Failed to connect to the authenticate response", false).showToastResponse(ConnectActivity.this);
                    }

                    else Toast.makeText(ConnectActivity.this, decryptedResponse.get("statusMessage").getAsString(), Toast.LENGTH_SHORT).show();
                }

                else throw new Exception();
            }

            catch(Exception e)
            {
                Log.d("CONNECT_SERVER_FAIL", e.getMessage());
                new ServiceResponse("Failed to connect to the server", false).showToastResponse(ConnectActivity.this);
            }
        }
    }

    //Response handler for fetching the server public key request
    //Initializes the server public key instance from the responding public key
    private class ServerPublicFetchTask extends HTTPAsync
    {
        @Override
        protected  void onPreExecute()
        {
            showServicingSpinner(ConnectActivity.this, "Connecting to server");
        }

        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                hideServicingSpinner();

                JsonObject responseObj  =    parseJsonInput(response);
                String keyStr           =   responseObj.get("serverPublicKey").getAsString();
                KeyManager.getInstance().setServerPublicKey(keyStr);
                KeyManager.getInstance().loadClientKeyPair(ConnectActivity.this);
            }

            catch(Exception e)
            {
                Log.d("SPUBLIC_FETCH_FAIL", e.getMessage());
                new ServiceResponse("Failed to connect to the server", false).showToastResponse(ConnectActivity.this);
            }
        }
    }
}
