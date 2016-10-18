//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.nav;

import android.os.Bundle;
import android.app.Fragment;
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
import com.kyleruss.hssa2.client.core.ClientConfig;
import com.kyleruss.hssa2.client.core.KeyManager;
import com.kyleruss.hssa2.client.core.RequestManager;
import com.kyleruss.hssa2.client.core.User;
import com.kyleruss.hssa2.client.core.UserManager;
import com.kyleruss.hssa2.commons.EncryptedSession;
import com.kyleruss.hssa2.commons.RequestPaths;

import java.util.Map;


public class SettingsFragment extends Fragment implements View.OnClickListener
{
    private User tempUser;

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getActivity().getActionBar().setTitle("Settings");
        View view           =   inflater.inflate(R.layout.fragment_settings, container, false);
        EditText nameField  =   (EditText) view.findViewById(R.id.nameField);
        EditText emailField =   (EditText) view.findViewById(R.id.emailField);

        UserManager userManager =   UserManager.getInstance();
        nameField.setText(userManager.getActiveUser().getName());
        emailField.setText(userManager.getActiveUser().getEmail());

        view.findViewById(R.id.settingsSaveBtn).setOnClickListener(this);
        return view;
    }

    public void saveSettings()
    {
        try
        {
            String name     =   ((EditText) getView().findViewById(R.id.nameField)).getText().toString();
            String email    =   ((EditText) getView().findViewById(R.id.emailField)).getText().toString();
            String userID   =   UserManager.getInstance().getActiveUser().getPhoneID();
            tempUser        =   new User(userID, name);
            tempUser.setEmail(email);

            Map.Entry<String, String> authRequest = RequestManager.getInstance().generateRequest();
            JsonObject requestObj = CommUtils.prepareAuthenticatedRequest(authRequest.getKey(), authRequest.getValue());
            requestObj.addProperty("userID", userID);
            requestObj.addProperty("name", name);
            requestObj.addProperty("email", email);


            EncryptedSession encSession =   new EncryptedSession(requestObj.toString().getBytes("UTF-8"), KeyManager.getInstance().getServerPublicKey());
            ServiceRequest request      =   CommUtils.prepareEncryptedSessionRequest(encSession);
            request.setURL(ClientConfig.CONN_URL + RequestPaths.USER_SETTINGS_SAVE);
            request.setGet(false);

            SaveSettingsTask task   =   new SaveSettingsTask();
            task.execute(request);
        }

        catch (Exception e)
        {
            Log.d("SAVE_SETTINGS_SEND_FAIL", e.getMessage());
            Toast.makeText(getActivity().getApplicationContext(), "Failed to save settings", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.settingsSaveBtn)
            saveSettings();
    }

    private class SaveSettingsTask extends HTTPAsync
    {
        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                EncryptedSession encSession =   CommUtils.decryptSessionResponse(response, KeyManager.getInstance().getClientPrivateKey());
                JsonObject responseObj      =   CommUtils.parseJsonInput(new String(encSession.getData()));
                String requestID            =   responseObj.get("requestID").getAsString();
                String nonce                =   responseObj.get("nonce").getAsString();

                if(RequestManager.getInstance().verifyAndDestroy(requestID, nonce))
                {
                    Toast.makeText(getActivity().getApplicationContext(), responseObj.get("message").getAsString(), Toast.LENGTH_SHORT).show();
                    if(responseObj.get("actionStatus").getAsBoolean())
                    {
                        User activeUser =   UserManager.getInstance().getActiveUser();
                        activeUser.setName(tempUser.getName());
                        activeUser.setEmail(tempUser.getEmail());
                        activeUser.setProfileImage(tempUser.getProfileImage());
                        tempUser = null;
                    }
                }
            }

            catch(Exception e)
            {
                Toast.makeText(getActivity().getApplicationContext(), "Failed to update account", Toast.LENGTH_SHORT).show();
                Log.d("SAVE_SETTINGS_FAIL", e.getMessage());
            }
        }
    }
}
