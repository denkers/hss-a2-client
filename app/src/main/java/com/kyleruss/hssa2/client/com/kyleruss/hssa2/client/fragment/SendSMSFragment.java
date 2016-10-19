//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.fragment;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.communication.CommUtils;
import com.kyleruss.hssa2.client.communication.HTTPAsync;
import com.kyleruss.hssa2.client.communication.ServiceRequest;
import com.kyleruss.hssa2.client.core.ClientConfig;
import com.kyleruss.hssa2.client.core.KeyManager;
import com.kyleruss.hssa2.client.core.RequestManager;
import com.kyleruss.hssa2.client.core.UserManager;
import com.kyleruss.hssa2.commons.EncryptedSession;
import com.kyleruss.hssa2.commons.RequestPaths;

import java.util.Map;


public class SendSMSFragment extends Fragment
{
    public SendSMSFragment () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getActivity().getActionBar().setTitle("Send SMS");
        View view   =   inflater.inflate(R.layout.fragment_send_sms, container, false);

        Bundle data =   getArguments();
        if(data != null && data.containsKey("phoneID"))
        {
            String phoneID  =   data.getString("phoneID");
            ((EditText) view.findViewById(R.id.phoneIDField)).setText(phoneID);
        }

        requestUserPublicKey(UserManager.getInstance().getActiveUser().getPhoneID());
        return view;
    }

    private void requestUserPublicKey(String user)
    {
        try {
            Map.Entry<String, String> authRequest = RequestManager.getInstance().generateRequest();
            JsonObject requestObj = CommUtils.prepareAuthenticatedRequest(authRequest.getKey(), authRequest.getValue());
            requestObj.addProperty("userID", UserManager.getInstance().getActiveUser().getPhoneID());
            requestObj.addProperty("reqUserID", user);

            EncryptedSession encSession =   new EncryptedSession(requestObj.toString().getBytes("UTF-8"), KeyManager.getInstance().getServerPublicKey());
            ServiceRequest request      =   CommUtils.prepareEncryptedSessionRequest(encSession);
            request.setURL(ClientConfig.CONN_URL + RequestPaths.USER_PUBLIC_GET_REQ);
            request.setGet(false);

            ClientPublicKeyTask task    =   new ClientPublicKeyTask();
            task.execute(request);
        }

        catch(Exception e)
        {
            Toast.makeText(getActivity().getApplicationContext(), "Failed to send public key request", Toast.LENGTH_SHORT).show();
            Log.d("REQUEST_PUB_KEY_FAIL", e.getMessage());
        }
    }

    private class ClientPublicKeyTask extends HTTPAsync
    {

        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                JsonObject responseObj = CommUtils.parseJsonInput(response);
                byte[] key  =   Base64.decode(responseObj.get("key").getAsString(), Base64.DEFAULT);
                byte[] data =   Base64.decode(responseObj.get("data").getAsString(), Base64.DEFAULT);
                EncryptedSession encSession = new EncryptedSession(key, data, KeyManager.getInstance().getClientPrivateKey());
                encSession.unlock();
                JsonObject decryptedResponse = CommUtils.parseJsonInput(new String(encSession.getData()));

                String nonce        =   decryptedResponse.get("nonce").getAsString();
                String requestID    =   decryptedResponse.get("requestID").getAsString();

                if(RequestManager.getInstance().verifyAndDestroy(requestID, nonce))
                {
                    String requestedUser    =   decryptedResponse.get("requestedUser").getAsString();
                    byte[] requestedKey     =   Base64.decode(decryptedResponse.get("requestedKey").getAsString(), Base64.DEFAULT);
                    KeyManager.getInstance().setUserPublicKey(requestedUser, requestedKey);
                }
            }

            catch(Exception e)
            {
                Log.d("CLIENT_PUBLIC_REQ_FAIL", e.getMessage());
            }
        }
    }
}
