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
import com.kyleruss.hssa2.client.core.MessageManager;
import com.kyleruss.hssa2.client.core.RequestManager;
import com.kyleruss.hssa2.client.core.UserManager;
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
        //getActivity().getActionBar().setTitle("Send SMS");
        View view   =   inflater.inflate(R.layout.fragment_send_sms, container, false);
        view.findViewById(R.id.sendSmsBtn).setOnClickListener(this);

        Bundle data =   getArguments();
        if(data != null && data.containsKey("phoneID"))
        {
            String phoneID  =   data.getString("phoneID");
            ((EditText) view.findViewById(R.id.phoneIDField)).setText(phoneID);
        }


        return view;
    }

    public void sendSMS()
    {
        MessageManager messageManager   =   MessageManager.getInstance();
        String receiverID               =   ((EditText) getView().findViewById(R.id.phoneIDField)).getText().toString();
        EditText contentField           =   (EditText) getView().findViewById(R.id.msgContentField);
        String content                  =   contentField.getText().toString();

        if(receiverID.equals("") || content.equals(""))
        {
            String errorMsg =   "Please enter " + (receiverID.equals("")? "the recipients phone number" : "the message body");
            new ServiceResponse(errorMsg, false).showToastResponse(getActivity());
            return;
        }

        KeyManager keyManager           =   KeyManager.getInstance();
        PublicKey recvPublicKey         =   keyManager.getPublicKey(receiverID);

        if(recvPublicKey == null) requestUserPublicKey(receiverID);

        else
        {
            Log.d("SEND_SMS", "user public key found");
            SecretKeySpec secretKey =   keyManager.getSessionKey(receiverID);
            if(secretKey == null)
            {
                try
                {
                    byte[] generatedKey = keyManager.generateSessionKey();
                    keyManager.setUserSessionKey(receiverID, generatedKey);
                    secretKey   =   keyManager.getSessionKey(receiverID);

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
                byte[] encryptedContent = CryptoCommons.AES(secretKey, content.getBytes("UTF-8"), true);
                messageManager.sendEncodedMessage(receiverID, encryptedContent);
            }

            catch(Exception e)
            {
             new ServiceResponse("Failed to send SMS", false).showToastResponse(getActivity());
                e.printStackTrace();
             //   Log.d("SEND_SMS_FAIL", e.getMessage());
                Toast.makeText(getActivity().getApplicationContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
            }

            contentField.setText("");
            new ServiceResponse("Message has been sent", true).showToastResponse(getActivity());
        }
    }

    private void requestUserPublicKey(String user)
    {
        try
        {
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
            e.printStackTrace();
            //Toast.makeText(getActivity().getApplicationContext(), "Failed to send public key request", Toast.LENGTH_SHORT).show();
            new ServiceResponse("Failed to request public key", false).showToastResponse(getActivity());
            Log.d("REQUEST_PUB_KEY_FAIL", e.getMessage());
        }
    }

    @Override
    public void onClick(View v)
    {
        int id  =   v.getId();

        if(id == R.id.sendSmsBtn)
            sendSMS();
    }

    private class ClientPublicKeyTask extends HTTPAsync
    {

        @Override
        protected void onPreExecute()
        {
            ImageView sendSmsControl    =   (ImageView) getView().findViewById(R.id.sendSmsBtn);
            showServicingSpinner(getActivity(), "Sending message");
        }

        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                ImageView sendSmsControl    =   (ImageView) getView().findViewById(R.id.sendSmsBtn);
                hideServicingSpinner();

                if(response.equals(""))
                {
                    new ServiceResponse("Failed to get public key for user", false).showToastResponse(getActivity());
                    return;
                }


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
                    sendSMS();
                }

                else  new ServiceResponse("Failed to authenticate response", false).showToastResponse(getActivity());
            }

            catch(Exception e)
            {
                e.printStackTrace();
                new ServiceResponse("Failed to get public key for user", false).showToastResponse(getActivity());
                //Log.d("CLIENT_PUBLIC_REQ_FAIL", e.getMessage());
            }
        }
    }
}
