//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.safesms.client.fragment;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.kyleruss.safesms.client.R;
import com.kyleruss.safesms.client.communication.CommUtils;
import com.kyleruss.safesms.client.communication.HTTPAsync;
import com.kyleruss.safesms.client.communication.ServiceRequest;
import com.kyleruss.safesms.client.communication.ServiceResponse;
import com.kyleruss.safesms.client.core.ClientConfig;
import com.kyleruss.safesms.client.core.KeyManager;
import com.kyleruss.safesms.client.core.MessageManager;
import com.kyleruss.safesms.client.core.RequestManager;
import com.kyleruss.safesms.client.core.UserManager;
import com.kyleruss.hssa2.commons.CryptoCommons;
import com.kyleruss.hssa2.commons.EncryptedSession;
import com.kyleruss.hssa2.commons.RequestPaths;

import java.security.PublicKey;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;


public class SendSMSFragment extends Fragment implements View.OnClickListener
{
    public SendSMSFragment () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view   =   inflater.inflate(R.layout.fragment_send_sms, container, false);
        view.findViewById(R.id.sendSmsBtn).setOnClickListener(this);

        //Set phone field if phone id no. is passed
        Bundle data =   getArguments();
        if(data != null && data.containsKey("phoneID"))
        {
            String phoneID  =   data.getString("phoneID");
            ((EditText) view.findViewById(R.id.phoneIDField)).setText(phoneID);
        }

        return view;
    }

    //sends a SMS message to a user
    //Fetches the users public key if the client doesn't have it already
    //Generates and sends a session key to the user if one doesn't already exist
    //Session key is encrypted with the users public key
    //Once session key is established, all further messages are encrypted in AES with the session key
    public void sendSMS()
    {
        MessageManager messageManager   =   MessageManager.getInstance();
        String receiverID               =   ((EditText) getView().findViewById(R.id.phoneIDField)).getText().toString();
        EditText contentField           =   (EditText) getView().findViewById(R.id.msgContentField);
        String content                  =   contentField.getText().toString();

        //check input, make sure user enters phone no. and some message
        if(receiverID.equals("") || content.equals(""))
        {
            String errorMsg =   "Please enter " + (receiverID.equals("")? "the recipients phone number" : "the message body");
            new ServiceResponse(errorMsg, false).showToastResponse(getActivity());
            return;
        }

        KeyManager keyManager      =   KeyManager.getInstance();
        PublicKey recvPublicKey    =   keyManager.getPublicKey(receiverID);

        //Users public key not found, request it
        if(recvPublicKey == null) requestUserPublicKey(receiverID);

        //User public key found, try use the session key
        else
        {
            SecretKeySpec secretKey =   keyManager.getSessionKey(receiverID);

            //Session key not found, generate one and send it to the user
            if(secretKey == null)
            {
                try
                {
                    byte[] generatedKey = keyManager.generateSessionKey();
                    keyManager.setUserSessionKey(receiverID, generatedKey);
                    secretKey   =   keyManager.getSessionKey(receiverID);

                    //Session key encrypted with RSA using the receivers public key
                    byte[] encryptedKey = keyManager.wrapSessionKey(receiverID, generatedKey);
                    messageManager.sendEncodedMessage(receiverID, encryptedKey);
                }

                catch(Exception e)
                {
                    new ServiceResponse("Failed to send SMS", false).showToastResponse(getActivity());
                    keyManager.removeSessionKey(receiverID);
                    return;
                }
            }

            try
            {
                //Encrypt the message with AES using the session key
                byte[] encryptedContent = CryptoCommons.AES(secretKey, content.getBytes("UTF-8"), true);
                messageManager.sendEncodedMessage(receiverID, encryptedContent);

                contentField.setText("");
                new ServiceResponse("Message has been sent", true).showToastResponse(getActivity());
            }

            catch(Exception e)
            {
                new ServiceResponse("Failed to send SMS", false).showToastResponse(getActivity());
                Log.d("SEND_SMS_FAIL", e.getMessage());
            }
        }
    }

    //Sends a request for the passed users public key
    private void requestUserPublicKey(String user)
    {
        try
        {
            //Response will need to be verified
            Map.Entry<String, String> authRequest = RequestManager.getInstance().generateRequest();
            JsonObject requestObj = CommUtils.prepareAuthenticatedRequest(authRequest.getKey(), authRequest.getValue());
            requestObj.addProperty("userID", UserManager.getInstance().getActiveUser().getPhoneID());
            requestObj.addProperty("reqUserID", user);

            //Encrypt message with AES, encrypt key with server public key
            EncryptedSession encSession =   new EncryptedSession(requestObj.toString().getBytes("UTF-8"), KeyManager.getInstance().getServerPublicKey());
            ServiceRequest request      =   CommUtils.prepareEncryptedSessionRequest(encSession);
            request.setURL(ClientConfig.CONN_URL + RequestPaths.USER_PUBLIC_GET_REQ);
            request.setGet(false);

            ClientPublicKeyTask task    =   new ClientPublicKeyTask();
            task.execute(request);
        }

        catch(Exception e)
        {
            new ServiceResponse("Failed to request public key", false).showToastResponse(getActivity());
            Log.d("REQUEST_PUB_KEY_FAIL", e.getMessage());
        }
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.sendSmsBtn)
            sendSMS();
    }

    //Response handler for user public key request
    //Need to verify response then if OK, add the users public key
    //Then send the SMS message
    private class ClientPublicKeyTask extends HTTPAsync
    {

        @Override
        protected void onPreExecute()
        {
            showServicingSpinner(getActivity(), "Sending message");
        }

        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                hideServicingSpinner();

                if(response.equals(""))
                {
                    new ServiceResponse("Failed to get public key for user", false).showToastResponse(getActivity());
                    return;
                }


                JsonObject responseObj = CommUtils.parseJsonInput(response);

                //Decrypt key using client private key then decrypt message with the key
                byte[] key  =   Base64.decode(responseObj.get("key").getAsString(), Base64.DEFAULT);
                byte[] data =   Base64.decode(responseObj.get("data").getAsString(), Base64.DEFAULT);
                EncryptedSession encSession = new EncryptedSession(key, data, KeyManager.getInstance().getClientPrivateKey());
                encSession.unlock();
                JsonObject decryptedResponse = CommUtils.parseJsonInput(new String(encSession.getData()));

                String nonce        =   decryptedResponse.get("nonce").getAsString();
                String requestID    =   decryptedResponse.get("requestID").getAsString();

                //Verify response
                if(RequestManager.getInstance().verifyAndDestroy(requestID, nonce))
                {
                    //Add the resulting public key for the requested user
                    //Send the SMS message which the client was previously trying to do
                    String requestedUser    =   decryptedResponse.get("requestedUser").getAsString();
                    byte[] requestedKey     =   Base64.decode(decryptedResponse.get("requestedKey").getAsString(), Base64.DEFAULT);
                    KeyManager.getInstance().setUserPublicKey(requestedUser, requestedKey);
                    sendSMS();
                }

                else  new ServiceResponse("Failed to authenticate response", false).showToastResponse(getActivity());
            }

            catch(Exception e)
            {
                new ServiceResponse("Failed to get public key for user", false).showToastResponse(getActivity());
                Log.d("CLIENT_PUBLIC_REQ_FAIL", e.getMessage());
            }
        }
    }
}
