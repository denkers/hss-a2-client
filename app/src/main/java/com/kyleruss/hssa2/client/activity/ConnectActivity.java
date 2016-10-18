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
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.communication.CommUtils;
import com.kyleruss.hssa2.client.communication.HTTPAsync;
import com.kyleruss.hssa2.client.communication.ServiceRequest;
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

public class ConnectActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        getServerPublicKey();
    }

    public void getServerPublicKey()
    {
        String url                  =   ClientConfig.CONN_URL + RequestPaths.SERV_PUBLIC_GET_REQ;
        ServiceRequest request      =   new ServiceRequest(url, true);
        ServerPublicFetchTask task  =   new ServerPublicFetchTask();
        task.execute(request);
    }

    public void connectToServer(View v)
    {
        try
        {
            Map.Entry<String, String> authRequest = RequestManager.getInstance().generateRequest();
            JsonObject authObj = CommUtils.prepareAuthenticatedRequest(authRequest.getKey(), authRequest.getValue());
            authObj.addProperty("phoneID", UserManager.getInstance().getPhoneID(this));
            Log.d("CONNECT_REQUEST", authObj.toString());
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
            Toast.makeText(this, "Failed to connect to the server", Toast.LENGTH_SHORT).show();
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

    private class ConnectTask extends HTTPAsync
    {
        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                JsonObject responseObj = CommUtils.parseJsonInput(response);
                byte[] key = Base64.decode(responseObj.get("key").getAsString(), Base64.DEFAULT); // could be
                byte[] data = Base64.decode(responseObj.get("data").getAsString(), Base64.DEFAULT);
                KeyPair clientKeyPair = KeyManager.getInstance().getClientKeyPair();
                Log.d("CONNECT_RESPONSE", responseObj.toString());

                if (clientKeyPair != null)
                {
                    EncryptedSession encSession = new EncryptedSession(key, data, clientKeyPair.getPrivate());
                    encSession.unlock();
                    JsonObject decryptedResponse = CommUtils.parseJsonInput(new String(encSession.getData()));
                    Log.d("Decrypted response", decryptedResponse.toString());

                    if(decryptedResponse.get("status").getAsBoolean())
                    {
                        String requestID    =   decryptedResponse.get("requestID").getAsString();
                        String nonce        =   decryptedResponse.get("nonce").getAsString();
                        if(RequestManager.getInstance().verifyAndDestroy(requestID, nonce))
                        {
                            User nextUser       =   new User(decryptedResponse.get("phoneID").getAsString(), decryptedResponse.get("name").getAsString());
                            nextUser.setEmail(decryptedResponse.get("email").getAsString());
                            byte[] profileImage =   decryptedResponse.has("profileImage")? Base64.decode(decryptedResponse.get("profileImage").getAsString(), Base64.DEFAULT) : null;
                            nextUser.setProfileImage(profileImage);
                            UserManager.getInstance().setActiveUser(nextUser);

                            Toast.makeText(ConnectActivity.this, "Successfully connected", Toast.LENGTH_SHORT).show();
                            startHomeActivity();
                        }

                        else Toast.makeText(ConnectActivity.this, "Failed to authenticate response", Toast.LENGTH_SHORT).show();
                    }

                    else Toast.makeText(ConnectActivity.this, decryptedResponse.get("statusMessage").getAsString(), Toast.LENGTH_SHORT).show();
                }

                else throw new Exception();
            }

            catch(Exception e)
            {
                e.printStackTrace();
              //  Log.d("CONNECT_FAIL", e.getMessage());
                Toast.makeText(ConnectActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ServerPublicFetchTask extends HTTPAsync
    {
        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                JsonObject responseObj  =    parseJsonInput(response);
                String keyStr           =   responseObj.get("serverPublicKey").getAsString();
                KeyManager.getInstance().setServerPublicKey(keyStr);
                KeyManager.getInstance().loadClientKeyPair(ConnectActivity.this);
            }

            catch(Exception e)
            {
                e.printStackTrace();
              //  Log.d("SPUBLIC_FETCH_FAIL", e.getMessage());
            }
        }
    }
}
