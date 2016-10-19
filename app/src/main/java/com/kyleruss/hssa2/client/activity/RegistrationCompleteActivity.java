package com.kyleruss.hssa2.client.activity;

import android.app.Activity;
import android.content.Intent;
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
import com.kyleruss.hssa2.commons.RequestPaths;

import java.net.URLEncoder;
import java.security.KeyPair;
import java.util.Map;

public class RegistrationCompleteActivity extends Activity
{
    private User registerUser;
    private KeyPair currentKeyPair;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_complete);
        registerUser    =   (User) getIntent().getSerializableExtra("registerUser");
    }

    private void showConnectActivity()
    {
        Intent intent   =   new Intent(this, ConnectActivity.class);
        startActivity(intent);
    }

    public void sendRegisterPackage(View v)
    {
        try
        {
            String password = ((EditText) findViewById(R.id.authCodeField)).getText().toString();

            //-------------------------------------------------------------------------------
            //Generate request ID, nonce and salts
            //-------------------------------------------------------------------------------
            Map.Entry<String, String> requestEntry = RequestManager.getInstance().generateRequest();
            String requestID    =   requestEntry.getKey();
            String nonce        =   requestEntry.getValue();
            String salt         =   registerUser.getPhoneID().substring(0, 4) + registerUser.getEmail().substring(0, 4);

            //MD5 hash Salt & Pass for required key length
            byte[] hashedSalt   =   CryptoCommons.generateHash(salt.getBytes("UTF-8"));
            byte[] hashedPass   =   CryptoCommons.generateHash(password.getBytes("UTF-8"));
            //-------------------------------------------------------------------------------

            JsonObject userDataObj = CommUtils.prepareAuthenticatedRequest(requestID, nonce);
            currentKeyPair      =   KeyManager.getInstance().generateClientKeyPair();
            String publicKey    =   Base64.encodeToString(currentKeyPair.getPublic().getEncoded(), Base64.NO_WRAP);
            userDataObj.addProperty("publicKey", publicKey);
            userDataObj.addProperty("phoneID", registerUser.getPhoneID());
            userDataObj.addProperty("email", registerUser.getEmail());
            userDataObj.addProperty("name", registerUser.getName());
            byte[] pbEncryptedData  =   CryptoCommons.pbeEncrypt(hashedPass, hashedSalt, userDataObj.toString().getBytes("UTF-8"));
            String encodedUserData  =   URLEncoder.encode(Base64.encodeToString(pbEncryptedData, Base64.NO_WRAP), "UTF-8");

            JsonObject authContentsObj = new JsonObject();
            authContentsObj.addProperty("password", password);
            authContentsObj.addProperty("salt", salt);
            byte[] encryptedAuthContents    =   CryptoCommons.publicEncrypt(authContentsObj.toString().getBytes("UTF-8"), KeyManager.getInstance().getServerPublicKey());
            String encodedAuthContents      =   URLEncoder.encode(Base64.encodeToString(encryptedAuthContents, Base64.NO_WRAP), "UTF-8");

            ServiceRequest request  =   new ServiceRequest(ClientConfig.CONN_URL + RequestPaths.USER_PUBLIC_SEND_REQ, false);
            request.addParam("authContents", encodedAuthContents);
            request.addParam("clientData", encodedUserData);

            UserRegisterTask task   =   new UserRegisterTask();
            task.execute(request);
            Log.d("SEND_STUFF", "sent stuff");
        }

        catch(Exception e)
        {
            e.printStackTrace();
            Log.d("SEND_REGISTER_FAIL", "" + e.getMessage());
        }
    }

    private class UserRegisterTask extends HTTPAsync
    {
        @Override
        protected void onPreExecute()
        {
            showServicingSpinner(RegistrationCompleteActivity.this, "Finalizing registration");
        }

        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                hideServicingSpinner();

                JsonObject responseObj = CommUtils.parseJsonInput(response);
                byte[] key = Base64.decode(responseObj.get("key").getAsString(), Base64.DEFAULT);
                byte[] data = Base64.decode(responseObj.get("data").getAsString(), Base64.DEFAULT);
                EncryptedSession encSession = new EncryptedSession(key, data, currentKeyPair.getPrivate());
                encSession.unlock();
                JsonObject  decryptedResponse   =   CommUtils.parseJsonInput(new String(encSession.getData()));
                boolean status                  =   decryptedResponse.get("status").getAsBoolean();
                String statusMessage            =   decryptedResponse.get("statusMessage").getAsString();

                if(status)
                {
                    String nonce        =   decryptedResponse.get("nonce").getAsString();
                    String requestID    =   decryptedResponse.get("requestID").getAsString();

                    if(RequestManager.getInstance().verifyAndDestroy(requestID, nonce))
                    {
                        KeyManager.getInstance().setClientKeyPair(currentKeyPair);
                        KeyManager.getInstance().saveClientKeyPair(RegistrationCompleteActivity.this);
                        UserManager.getInstance().savePhoneID(registerUser.getPhoneID(), RegistrationCompleteActivity.this);
                        showConnectActivity();
                    }
                }

                new ServiceResponse(statusMessage, status).showToastResponse(RegistrationCompleteActivity.this);
            }

            catch (Exception e)
            {
                Log.d("REGISTER_TASK_FAIL", e.getMessage());
            }
        }
    }


}
