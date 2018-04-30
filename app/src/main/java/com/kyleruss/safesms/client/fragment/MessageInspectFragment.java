//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.safesms.client.fragment;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyleruss.safesms.client.R;
import com.kyleruss.safesms.client.communication.ServiceResponse;
import com.kyleruss.safesms.client.core.Message;
import com.kyleruss.safesms.client.core.MessageManager;


public class MessageInspectFragment extends Fragment implements View.OnClickListener
{
    private Message currentMessage;

    public MessageInspectFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_message_inspect, container, false);
        ImageView decryptBtn    =   ((ImageView) view.findViewById(R.id.decryptBtn));
        ImageView replyBtn      =   ((ImageView) view.findViewById(R.id.replyBtn));

        decryptBtn.setOnClickListener(this);
        replyBtn.setOnClickListener(this);

        //Set the view info for the passed message
        Bundle data =   getArguments();
        if(data != null)
        {
            currentMessage  =   (Message) data.getSerializable("message");
            ((TextView) view.findViewById(R.id.fromText)).setText("From: " + currentMessage.getFrom());
            ((TextView) view.findViewById(R.id.recvText)).setText("Received: " + currentMessage.recvDateToString());
            ((TextView) view.findViewById(R.id.msgContent)).setText(currentMessage.getContent());

            if(currentMessage.isDecrypted())
                ((ImageView) view.findViewById(R.id.decryptBtn)).setImageResource(R.drawable.decrypt_unavail_image);
        }

        return view;
    }

    //Redirects to send sms with the current phone number
    public void replyMessage()
    {
        Fragment smsFragment    =   new SendSMSFragment();
        Bundle data             =   new Bundle();
        data.putString("phoneID", currentMessage.getFrom());
        smsFragment.setArguments(data);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, smsFragment)
                .addToBackStack(null)
                .commit();
    }

    //Decrypts the inspected message
    //Denies the user from decrypting if it's already been decrypted
    public void decryptMessage()
    {
        //Message already been decrypted
        if(currentMessage.isDecrypted())
            new ServiceResponse("Message is already decrypted", true).setInfo(true).showToastResponse(getActivity());

        //Not decrypted, decrypt the inspected message
        //See MessageManager@decryptMessage for decryption
        else
        {
            MessageManager messageManager   =   MessageManager.getInstance();
            if(messageManager.decryptMessage(currentMessage))
            {
                String content  =   currentMessage.getContent();
                ((TextView) getView().findViewById(R.id.msgContent)).setText(content);
                ((ImageView) getView().findViewById(R.id.decryptBtn)).setImageResource(R.drawable.decrypt_unavail_image);
            }

            else new ServiceResponse("Failed to decrypt message", true).setInfo(true).showToastResponse(getActivity());
        }
    }

    @Override
    public void onClick(View v)
    {
        int id  =   v.getId();

        if(id == R.id.replyBtn)
            replyMessage();

        else if(id == R.id.decryptBtn)
            decryptMessage();
    }
}
