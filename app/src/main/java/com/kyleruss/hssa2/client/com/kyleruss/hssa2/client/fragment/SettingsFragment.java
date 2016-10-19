//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
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
import com.kyleruss.hssa2.client.core.RequestManager;
import com.kyleruss.hssa2.client.core.User;
import com.kyleruss.hssa2.client.core.UserManager;
import com.kyleruss.hssa2.commons.EncryptedSession;
import com.kyleruss.hssa2.commons.RequestPaths;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class SettingsFragment extends Fragment implements View.OnClickListener
{
    private User tempUser;
    private byte[] tempProfileImage;

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //getActivity().getActionBar().setTitle("Settings");
        View view                   =   inflater.inflate(R.layout.fragment_settings, container, false);
        EditText nameField          =   (EditText) view.findViewById(R.id.nameField);
        EditText emailField         =   (EditText) view.findViewById(R.id.emailField);
        ImageView profileImageField =   (ImageView) view.findViewById(R.id.changeProfileImage);

        UserManager userManager =   UserManager.getInstance();
        User activeUser         =   userManager.getActiveUser();
        nameField.setText(activeUser.getName());
        emailField.setText(activeUser.getEmail());

        if(activeUser.getProfileImage() != null)
        {
            Bitmap profileBtmp  =   BitmapFactory.decodeByteArray(activeUser.getProfileImage(), 0, activeUser.getProfileImage().length);
            profileImageField.setImageBitmap(profileBtmp);
        }

        view.findViewById(R.id.settingsSaveBtn).setOnClickListener(this);
        view.findViewById(R.id.changeProfileImage).setOnClickListener(this);
        return view;
    }

    public void openImage()
    {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && null != data)
        {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap imgBtmap   =   BitmapFactory.decodeFile(picturePath);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imgBtmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            tempProfileImage = stream.toByteArray();

            ((ImageView) getView().findViewById(R.id.changeProfileImage)).setImageBitmap(imgBtmap);
        }
    }

    public void saveSettings()
    {
        try
        {
            String name     =   ((EditText) getView().findViewById(R.id.nameField)).getText().toString();
            String email    =   ((EditText) getView().findViewById(R.id.emailField)).getText().toString();
            String userID   =   UserManager.getInstance().getActiveUser().getPhoneID();
            String profile  =   tempProfileImage != null? Base64.encodeToString(tempProfileImage, Base64.NO_WRAP) : null;

            tempUser        =   new User(userID, name);
            tempUser.setEmail(email);
            if(tempProfileImage == null)
                tempUser.setProfileImage(UserManager.getInstance().getActiveUser().getProfileImage());
            else
            tempUser.setProfileImage(tempProfileImage);

            Map.Entry<String, String> authRequest = RequestManager.getInstance().generateRequest();
            JsonObject requestObj = CommUtils.prepareAuthenticatedRequest(authRequest.getKey(), authRequest.getValue());
            requestObj.addProperty("userID", userID);
            requestObj.addProperty("name", name);
            requestObj.addProperty("email", email);

            if(profile != null)
                requestObj.addProperty("profileImage", profile);


            EncryptedSession encSession =   new EncryptedSession(requestObj.toString().getBytes("UTF-8"), KeyManager.getInstance().getServerPublicKey());
            ServiceRequest request      =   CommUtils.prepareEncryptedSessionRequest(encSession);
            request.setURL(ClientConfig.CONN_URL + RequestPaths.USER_SETTINGS_SAVE);
            request.setGet(false);

            SaveSettingsTask task   =   new SaveSettingsTask();
            task.execute(request);
        }

        catch (Exception e)
        {
            e.printStackTrace();
            new ServiceResponse("Failed to save settings", false).showToastResponse(getActivity());
            Log.d("SAVE_SETTINGS_SEND_FAIL", e.getMessage());
            Toast.makeText(getActivity().getApplicationContext(), "Failed to save settings", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v)
    {
        int id  =   v.getId();
        if(id == R.id.settingsSaveBtn)
            saveSettings();

        else if(id == R.id.changeProfileImage)
            openImage();
    }

    private class SaveSettingsTask extends HTTPAsync
    {
        protected void onPreExecute()
        {
            showServicingSpinner(getActivity(), "Updating settings");
        }

        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                hideServicingSpinner();

                EncryptedSession encSession =   CommUtils.decryptSessionResponse(response, KeyManager.getInstance().getClientPrivateKey());
                JsonObject responseObj      =   CommUtils.parseJsonInput(new String(encSession.getData()));
                String requestID            =   responseObj.get("requestID").getAsString();
                String nonce                =   responseObj.get("nonce").getAsString();

                if(RequestManager.getInstance().verifyAndDestroy(requestID, nonce))
                {

                    if(responseObj.get("actionStatus").getAsBoolean())
                    {
                        User activeUser =   UserManager.getInstance().getActiveUser();
                        activeUser.setName(tempUser.getName());
                        activeUser.setEmail(tempUser.getEmail());
                        activeUser.setProfileImage(tempUser.getProfileImage());
                        tempUser            =   null;
                        tempProfileImage    =   null;
                        new ServiceResponse("Successfully updated settings", true).showToastResponse(getActivity());
                    }

                    else new ServiceResponse("Failed to save settings", false).showToastResponse(getActivity());
                }

                else  new ServiceResponse("Failed to authenticate response", false).showToastResponse(getActivity());
            }

            catch(Exception e)
            {
                e.printStackTrace();
                new ServiceResponse("Failed to save settings", false).showToastResponse(getActivity());
                //Log.d("SAVE_SETTINGS_FAIL", e.getMessage());
            }
        }
    }
}
