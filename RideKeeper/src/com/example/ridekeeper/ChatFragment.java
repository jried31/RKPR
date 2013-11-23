package com.example.ridekeeper;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import com.parse.Parse;
import com.parse.ParseUser;

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatFragment extends DialogFragment {
	private ImageView ivSendPic;
	private EditText etMessage;
	private Button btSendMsg;
	private ScrollView scrollContainer;
	private LinearLayout msgContainer;
	
	private String roomname;
	private MultiUserChatController mucController;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		roomname = getArguments().getString("roomname");
		
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_chat, container, false);
		
		ivSendPic = (ImageView) view.findViewById(R.id.imageView_sendPic);
		etMessage = (EditText) view.findViewById(R.id.editText_msg);
		btSendMsg = (Button) view.findViewById(R.id.buttton_sendMsg);
		scrollContainer = (ScrollView) view.findViewById(R.id.scrollContainer);
		msgContainer = (LinearLayout) view.findViewById(R.id.messageContainer);
		
		ivSendPic.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), "Not yet implemented!", Toast.LENGTH_SHORT).show();
			}
		});
		
		btSendMsg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
		
		//Start to connect and join chat server
		ivSendPic.setEnabled(false);
		btSendMsg.setEnabled(false);
		btSendMsg.setText("Connecting...");

		mucController = new MultiUserChatController(getActivity(),
													roomname,
													MyQBUser.getUserJabberIDfromCache(),
													MyQBUser.DUMMPY_PASSWORD,
													ParseUser.getCurrentUser().getUsername()); // use parse username as chat room nickname
		
		mucController.connect( new AfterConnectCallback() {
			@Override
			public void done(String errorMsg) {
				if (errorMsg == null){
					try {
						mucController.join();
					} catch (XMPPException e) {
						btSendMsg.setText("Error Joining");
						Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
						e.printStackTrace();
						return;
					}

					mucController.addMsgListener(new PacketListener() {
						@Override
						public void processPacket(Packet packet) {
							Message msg = (Message) packet;
							//convert "5111_room123@muc.chat.quickblox.com/romeo" to "romeo"
							String from = StringUtils.parseResource(msg.getFrom());
							//Log.d("RECEIVED MSG", from + ": " + msg.getBody());
							pushMessageToContainer( from + ": " + msg.getBody());
							//Toast.makeText(getActivity(), msg.getBody(), Toast.LENGTH_SHORT).show();
						}
					});

					ivSendPic.setEnabled(true);
					btSendMsg.setEnabled(true);
					btSendMsg.setText("Send");
					Toast.makeText(getActivity(), "Joined chat room", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getActivity(), "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
					btSendMsg.setText("Send");
				}
				
			}
		});
		
		return view;
	}
	
	private void sendMessage(){
		String tmp = etMessage.getText().toString();

		if (!tmp.isEmpty()){
			mucController.sendMessage(tmp);
			etMessage.setText("");
		}
		
	}
	
	//Put a text into the chat window
	private void pushMessageToContainer(String msg){
		final TextView textView = new TextView(getActivity());
		textView.setText(msg);
		textView.setTextSize(20);
		textView.setPadding(5, 0, 5, 10);
		
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgContainer.addView(textView);
                // Scroll to bottom
                scrollContainer.post( new Runnable() {
					@Override
					public void run() {
						scrollContainer.fullScroll(View.FOCUS_DOWN);
					}
				});
            }
        });
	}
	
}
