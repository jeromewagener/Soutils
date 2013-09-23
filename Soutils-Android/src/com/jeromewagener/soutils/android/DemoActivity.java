/* The MIT License (MIT)

Copyright (c) 2012 Jerome Wagener

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.*/

package com.jeromewagener.soutils.android;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jeromewagener.soutils.android.beaconing.BeaconReceiver;
import com.jeromewagener.soutils.android.beaconing.BeaconSender;
import com.jeromewagener.soutils.android.beaconing.BeaconSenderAndReceiver;
import com.jeromewagener.soutils.android.networking.Communication;
import com.jeromewagener.soutils.android.networking.CommunicationManager;
import com.jeromewagener.soutils.android.networking.NetworkFacade;

public class DemoActivity extends Activity {
	private static CommunicationManager communicationManager = null;
	private static Communication communication = null;  
	private static BeaconSender beaconSender = null;
	private static BeaconReceiver beaconReceiver = null;
	private static BeaconSenderAndReceiver beaconSenderAndReceiver = null;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
	
	private static class MyHandler extends Handler {
		private final WeakReference<DemoActivity> demoActivity;

		public MyHandler(DemoActivity activity) {
			demoActivity = new WeakReference<DemoActivity>(activity);
		}

		@Override
		public void handleMessage(Message message) {
			DemoActivity activity = demoActivity.get();
			if (activity != null) {
				String senderIpAddress = message.getData().getString("senderIpAddress");
				String receivedMessage = message.getData().getString("receivedMessage");

				((TextView) activity.findViewById(R.id.edtReceivedMessagesAndBeacons)).setText(
						sdf.format(new Date()) + ": " + 
						receivedMessage + " (" + senderIpAddress + ")\n" + 
						((TextView) activity.findViewById(R.id.edtReceivedMessagesAndBeacons)).getText().toString());
			}
		}
	}
	
	private void disableEverything() {
		((TextView) findViewById(R.id.edtSoutilsPort)).setEnabled(false);
		((TextView) findViewById(R.id.edtHostIPAddress)).setEnabled(false);
		
		((Button) findViewById(R.id.btnStartCommunication)).setEnabled(false);
		((Button) findViewById(R.id.btnStartCommunicationManager)).setEnabled(false);
		
		((Button) findViewById(R.id.btnSendMessageToServer)).setEnabled(false);
		((Button) findViewById(R.id.btnSendMessageToAllClients)).setEnabled(false);
		
		((Button) findViewById(R.id.btnStartBeaconSender)).setEnabled(false);
		((Button) findViewById(R.id.btnStartBeaconReceiver)).setEnabled(false);
		((Button) findViewById(R.id.btnStartBeaconSenderReceiver)).setEnabled(false);
	}
	
	private void enableEverything() {
		((TextView) findViewById(R.id.edtSoutilsPort)).setEnabled(true);
		((TextView) findViewById(R.id.edtHostIPAddress)).setEnabled(true);
		
		((Button) findViewById(R.id.btnStartCommunication)).setEnabled(true);
		((Button) findViewById(R.id.btnStartCommunicationManager)).setEnabled(true);
		
		((Button) findViewById(R.id.btnStartBeaconSender)).setEnabled(true);
		((Button) findViewById(R.id.btnStartBeaconReceiver)).setEnabled(true);
		((Button) findViewById(R.id.btnStartBeaconSenderReceiver)).setEnabled(true);
	}
	
	private final MyHandler messageHandler = new MyHandler(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo_selection);
		
		TextView currentIPAddress = (TextView) findViewById(R.id.edtCurrentIPAddress);
		currentIPAddress.setText(NetworkFacade.getCurrentIpAddress());
		
		Button startCommunicationManager = (Button) findViewById(R.id.btnStartCommunicationManager);
		startCommunicationManager.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	if (communicationManager == null) {
		    		disableEverything();
		    		
		    		((Button) findViewById(R.id.btnStartCommunicationManager)).setEnabled(true);
		    		((Button) findViewById(R.id.btnStartCommunicationManager)).setText("Stop Comm. Manager");
		    		((Button) findViewById(R.id.btnSendMessageToAllClients)).setEnabled(true);
		    		
		    		communicationManager = NetworkFacade.getCommunicationManagerThread(
		    				Integer.valueOf(((TextView) findViewById(R.id.edtSoutilsPort)).getText().toString()), messageHandler);
		    		communicationManager.start();
				} else {
					enableEverything();
					
					((Button) findViewById(R.id.btnStartCommunicationManager)).setText("Start Comm. Manager");
					((Button) findViewById(R.id.btnSendMessageToAllClients)).setEnabled(false);
					
					communicationManager.done();
					communicationManager = null;
				}
		    }
		});
		
		Button sendMessageToAllConnectedClients = (Button) findViewById(R.id.btnSendMessageToAllClients);
		sendMessageToAllConnectedClients.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	communicationManager.sendMessageToAllConnectedPeers(
		    			((TextView) findViewById(R.id.edtMessageToBeSent)).getText().toString());
		    }
		});
		
		Button startCommunication = (Button) findViewById(R.id.btnStartCommunication);
		startCommunication.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	TextView textView = (TextView) findViewById(R.id.edtHostIPAddress);
		    	TextView applicationPort = (TextView) findViewById(R.id.edtSoutilsPort);
		    	
		    	if (communication == null) {
		    		disableEverything();
		    		
		    		((Button) findViewById(R.id.btnStartCommunication)).setEnabled(true);
		    		((Button) findViewById(R.id.btnStartCommunication)).setText("Stop Communication");
		    		((Button) findViewById(R.id.btnSendMessageToServer)).setEnabled(true);
		    		
					communication = NetworkFacade.getCommunicationThread(
							textView.getText().toString(), 
							Integer.valueOf(applicationPort.getText().toString()), 
							messageHandler);
					communication.start();
				} else {
					enableEverything();
					
					((Button) findViewById(R.id.btnStartCommunication)).setText("Start Communication");
					((Button) findViewById(R.id.btnSendMessageToServer)).setEnabled(false);
					
					communication.done();
					communication = null;
				}
		    }
		});
		
		Button sendMessageToServer = (Button) findViewById(R.id.btnSendMessageToServer);
		sendMessageToServer.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	communication.sendMessage(((TextView) findViewById(R.id.edtMessageToBeSent)).getText().toString());
		    }
		});
		
		Button startBeaconSender = (Button) findViewById(R.id.btnStartBeaconSender);
		startBeaconSender.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	if (beaconSender == null) {
		    		disableEverything();
		    		
		    		((Button) findViewById(R.id.btnStartBeaconSender)).setEnabled(true);
		    		((Button) findViewById(R.id.btnStartBeaconSender)).setText("Stop Beacon Sender");
		    		
			    	beaconSender = new BeaconSender(
			    			((TextView) findViewById(R.id.edtMessageToBeSent)).getText().toString(), 
			    			NetworkFacade.getCurrentBroadcastAddress(v.getContext()));
			    	beaconSender.start();
		    	} else {
		    		enableEverything();
		    		
		    		((Button) findViewById(R.id.btnStartBeaconSender)).setText("Start Beacon Sender");
		    		
		    		beaconSender.done();
		    		beaconSender = null;
		    	}
		    }
		});
		
		Button startBeaconReceiver = (Button) findViewById(R.id.btnStartBeaconReceiver);
		startBeaconReceiver.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	if (beaconReceiver == null) {
		    		disableEverything();
		    		
		    		((Button) findViewById(R.id.btnStartBeaconReceiver)).setEnabled(true);
		    		((Button) findViewById(R.id.btnStartBeaconReceiver)).setText("Stop Beacon Receiver");
		    		
			    	beaconReceiver = new BeaconReceiver(messageHandler);
			    	beaconReceiver.start();
		    	} else {
		    		enableEverything();
		    		
		    		((Button) findViewById(R.id.btnStartBeaconSender)).setText("Start Beacon Receiver");
		    		
		    		beaconReceiver.done();
		    		beaconReceiver = null;
		    	}
		    }
		});
		
		Button startBeaconSenderReceiver = (Button) findViewById(R.id.btnStartBeaconSenderReceiver);
		startBeaconSenderReceiver.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	if (beaconSenderAndReceiver == null) {
		    		disableEverything();
		    		
		    		((Button) findViewById(R.id.btnStartBeaconSenderReceiver)).setEnabled(true);
		    		((Button) findViewById(R.id.btnStartBeaconSenderReceiver)).setText("Stop Sender & Receiver");
		    		
		    		beaconSenderAndReceiver = new BeaconSenderAndReceiver(
		    				((TextView) findViewById(R.id.edtMessageToBeSent)).getText().toString(),
		    				NetworkFacade.getCurrentBroadcastAddress(v.getContext()), messageHandler);
			    	beaconSenderAndReceiver.start();
		    	} else {
		    		enableEverything();
		    		
		    		((Button) findViewById(R.id.btnStartBeaconSenderReceiver)).setText("Stop Sender & Receiver");
		    		
		    		beaconSenderAndReceiver.done();
		    		beaconSenderAndReceiver = null;
		    	}
		    }
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.demo_selection, menu);
		return true;
	}
}
