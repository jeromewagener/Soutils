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
import java.net.InetAddress;
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
	/** The communication manager is able to handle multiple incoming communications. This is the server TCP part. */
	private static CommunicationManager communicationManager = null;
	/** The communication reflects a communication line to a communication manager. This is the client TCP part. */
	private static Communication communication = null;  
	/** The beacon sender broadcasts beacons (messages) using UDP packages */
	private static BeaconSender beaconSender = null;
	/** The beacon receiver will listen for broadcasted beacons (messages) on the specified port */
	private static BeaconReceiver beaconReceiver = null;
	/** The beacon sender and receive will send and receive UDP beacons (messages) while listening for other incoming packages */
	private static BeaconSenderAndReceiver beaconSenderAndReceiver = null;
	
	/** This handler will treat all received messages or beacons. In this Demo-App all received beacons/messages are written to an editbox*/
	private static class MessageHandler extends Handler {
		private final WeakReference<DemoActivity> demoActivity;
		private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
		
		public MessageHandler(DemoActivity activity) {
			demoActivity = new WeakReference<DemoActivity>(activity);
		}

		@Override
		public void handleMessage(Message message) {
			DemoActivity activity = demoActivity.get();
			
			if (activity != null) {
				String senderIpAddress = message.getData().getString("senderIpAddress");
				String receivedMessage = message.getData().getString("receivedMessage");

				TextView edtReceivedMessagesAndBeacons = ((TextView) activity.findViewById(R.id.edtReceivedMessagesAndBeacons));
				edtReceivedMessagesAndBeacons.setText(
						timeFormatter.format(new Date()) + ": " + 
						receivedMessage + " (" + senderIpAddress + ")\n" + 
						edtReceivedMessagesAndBeacons.getText().toString());
			}
		}
	}
	
	/** Instantiate the handler. The reference will be needed by several Soutils classes */
	private final MessageHandler messageHandler = new MessageHandler(this);
	
	@Override
	/** PLEASE NOTE: The following is bad practice! However, to illustrate the use of Soutils, all code resides within the onCreate. */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo_selection);
		
		TextView currentIPAddress = (TextView) findViewById(R.id.edtCurrentIPAddress);
		currentIPAddress.setText(NetworkFacade.getCurrentIpAddress());
		
		/**
		 * Creating a socket server using the communication manager
		 * --------------------------------------------------------
		 **/
		Button startCommunicationManager = (Button) findViewById(R.id.btnStartCommunicationManager);
		startCommunicationManager.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	if (communicationManager == null) {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.disableEverything(DemoActivity.this);
		    		ActivityHelpers.adaptGUIForCommunicationManager(DemoActivity.this);
		    		
		    		// Retrieve the port from the edit box
		    		int port = Integer.valueOf(((TextView) findViewById(R.id.edtSoutilsPort)).getText().toString());
		    		
		    		// Create the server
		    		communicationManager = NetworkFacade.getCommunicationManagerThread(port, messageHandler);
		    		communicationManager.start();
				} else {
					// Adapt GUI - Not important with respect to Soutils
					ActivityHelpers.enableEverything(DemoActivity.this);
					ActivityHelpers.resetGUIForCommunicationManager(DemoActivity.this);
					
					// Terminate the server
					communicationManager.done();
					communicationManager = null;
				}
		    }
		});
		
		/**
		 * Message all clients using the communication manager
		 * ---------------------------------------------------
		 **/
		Button sendMessageToAllConnectedClients = (Button) findViewById(R.id.btnSendMessageToAllClients);
		sendMessageToAllConnectedClients.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	// Retrieve message from edit box
		    	String message = ((TextView) findViewById(R.id.edtMessageToBeSent)).getText().toString();
		    	
		    	// Send message to all connected clients
		    	// PLEASE NOTE: You can also send a message to a particular client. 
		    	// In this case use the sendMessage method which required the IP 
		    	// address of the corresponding device!
		    	communicationManager.sendMessageToAllConnectedPeers(message);
		    }
		});
		
		/**
		 * Creating a socket client using a communication
		 * ----------------------------------------------
		 **/
		Button startCommunication = (Button) findViewById(R.id.btnStartCommunication);
		startCommunication.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	String hostIPAddress = ((TextView) findViewById(R.id.edtHostIPAddress)).getText().toString();
		    	int port = Integer.valueOf(((TextView) findViewById(R.id.edtSoutilsPort)).getText().toString());
		    	
		    	if (communication == null) {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.disableEverything(DemoActivity.this);
		    		ActivityHelpers.adaptGUIForCommunication(DemoActivity.this);		    		
		    		
		    		// Create the client
					communication = NetworkFacade.getCommunicationThread(hostIPAddress, port, messageHandler);
					communication.start();
				} else {
					// Adapt GUI - Not important with respect to Soutils
					ActivityHelpers.enableEverything(DemoActivity.this);
					ActivityHelpers.resetGUIForCommunication(DemoActivity.this);	
					
					// Terminate the client
					communication.done();
					communication = null;
				}
		    }
		});
		
		/**
		 * Send a message to the communication manager
		 * -------------------------------------------
		 **/
		Button sendMessageToServer = (Button) findViewById(R.id.btnSendMessageToServer);
		sendMessageToServer.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	// Retrieve message from edit box
		    	String message = ((TextView) findViewById(R.id.edtMessageToBeSent)).getText().toString();
		    	
		    	// Send message to the communication manager (server)
		    	communication.sendMessage(message);
		    }
		});
		
		/**
		 * Start broadcasting UDP beacons / messages
		 * -----------------------------------------
		 **/
		Button startBeaconSender = (Button) findViewById(R.id.btnStartBeaconSender);
		startBeaconSender.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	if (beaconSender == null) {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.disableEverything(DemoActivity.this);
		    		ActivityHelpers.adaptGUIForBeaconSender(DemoActivity.this);
		    		
		    		// Retrieve beacon message from edit box
		    		String beaconMessage = ((TextView) findViewById(R.id.edtMessageToBeSent)).getText().toString();
		    		
		    		// Retrieve broadcast address
		    		InetAddress broadcastAddress = NetworkFacade.getCurrentBroadcastAddress(v.getContext());
		    		
		    		// Create the beacon sender
			    	beaconSender = new BeaconSender(beaconMessage, broadcastAddress);
			    	beaconSender.start();
		    	} else {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.enableEverything(DemoActivity.this);
		    		ActivityHelpers.resetGUIForBeaconSender(DemoActivity.this);
		    		
		    		// Terminate the beacon sender
		    		beaconSender.done();
		    		beaconSender = null;
		    	}
		    }
		});
		
		/**
		 * Start listening for UDP beacons / messages
		 * ------------------------------------------
		 **/
		Button startBeaconReceiver = (Button) findViewById(R.id.btnStartBeaconReceiver);
		startBeaconReceiver.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	if (beaconReceiver == null) {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.disableEverything(DemoActivity.this);
		    		ActivityHelpers.adaptGUIForBeaconReceiver(DemoActivity.this);
		    		
		    		// Start listening
			    	beaconReceiver = new BeaconReceiver(messageHandler);
			    	beaconReceiver.start();
		    	} else {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.enableEverything(DemoActivity.this);
		    		ActivityHelpers.resetGUIForBeaconReceiver(DemoActivity.this);
		    		
		    		// Terminate listening
		    		beaconReceiver.done();
		    		beaconReceiver = null;
		    	}
		    }
		});
		
		/**
		 * Start broadcasting and receiving UDP beacons / messages simultaneously
		 * -----------------------------------------------------------------------
		 **/
		Button startBeaconSenderReceiver = (Button) findViewById(R.id.btnStartBeaconSenderReceiver);
		startBeaconSenderReceiver.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	if (beaconSenderAndReceiver == null) {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.disableEverything(DemoActivity.this);
		    		ActivityHelpers.adaptGUIForBeaconSenderReceiver(DemoActivity.this);
		    		
		    		// Retrieve beacon message from edit box
		    		String message = ((TextView) findViewById(R.id.edtMessageToBeSent)).getText().toString();
		    		
		    		// Retrieve broadcast address
		    		InetAddress broadcastAddress = NetworkFacade.getCurrentBroadcastAddress(v.getContext());
		    		
		    		// Start broadcasting and listening at the same time
		    		beaconSenderAndReceiver = new BeaconSenderAndReceiver(message, broadcastAddress, messageHandler);
			    	beaconSenderAndReceiver.start();
		    	} else {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.enableEverything(DemoActivity.this);
		    		ActivityHelpers.resetGUIForBeaconSenderReceiver(DemoActivity.this);	    		
		    		
		    		// Terminate simultaneously broadcasting and listening UDP beacons
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
