//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonObject;
import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.communication.CommUtils;
import com.kyleruss.hssa2.client.communication.HTTPAsync;
import com.kyleruss.hssa2.client.communication.ServiceRequest;
import com.kyleruss.hssa2.client.core.ClientConfig;
import com.kyleruss.hssa2.client.core.KeyManager;
import com.kyleruss.hssa2.client.core.RequestManager;
import com.kyleruss.hssa2.client.core.User;
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
    private KeyPair currentKeyPair;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_create);
        sendPasswordAuthEmail();
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

    private void sendRegisterPackage()
    {
        try
        {
            String password = "Y2TLKT7T";
            Map.Entry<String, String> requestEntry = RequestManager.getInstance().generateRequest();
            String requestID = requestEntry.getKey();
            String nonce = requestEntry.getValue();
            String salt = registerUser.getPhoneID().substring(0, 4) + registerUser.getEmail().substring(0, 4);

            JsonObject userDataObj = CommUtils.prepareAuthenticatedRequest(requestID, nonce);
            currentKeyPair = KeyManager.getInstance().generateClientKeyPair();
            String publicKey = Base64.encodeToString(currentKeyPair.getPublic().getEncoded(), Base64.NO_WRAP);
            userDataObj.addProperty("publicKey", publicKey);
            userDataObj.addProperty("phoneID", registerUser.getPhoneID());
            userDataObj.addProperty("email", registerUser.getEmail());
            userDataObj.addProperty("name", registerUser.getName());
            byte[] pbEncryptedData  =   CryptoCommons.pbeEncrypt(password.getBytes("UTF-8"), salt.getBytes("UTF-8"), userDataObj.toString().getBytes("UTF-8"));
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
        }

        catch(Exception e)
        {
            Log.d("SEND_REGISTER_FAIL", e.getMessage());
        }
    }

    private void createTempUser()
    {
        String email    =   "kyleruss2030@gmail.com";
        String name     =   "Kyle Russell";
        String phone    =   "(09) 0212522373";
        registerUser    =   new User(name, phone);
        registerUser.setEmail(email);
    }


    private class UserRegisterTask extends HTTPAsync
    {
        @Override
        protected void onPostExecute(String response)
        {
            Log.d("REIGSTER_SEND_RESP", response);
        }
    }

    private class PasswordSendTask extends HTTPAsync
    {
        @Override
        protected void onPostExecute(String response)
        {
            Log.d("PASSWORD_SEND_RESP", response);
        }
    }
}
