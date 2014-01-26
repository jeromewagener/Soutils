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

package com.jeromewagener.soutils.android.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jeromewagener.soutils.beaconing.BeaconReceiver;
import com.jeromewagener.soutils.beaconing.BeaconSender;
import com.jeromewagener.soutils.beaconing.BeaconSenderAndReceiver;
import com.jeromewagener.soutils.communication.Communication;
import com.jeromewagener.soutils.communication.CommunicationManager;
import com.jeromewagener.soutils.filetransfer.FileTransferClient;
import com.jeromewagener.soutils.filetransfer.FileTransferServer;
import com.jeromewagener.soutils.messaging.SoutilsMessage;
import com.jeromewagener.soutils.messaging.SoutilsObserver;
import com.jeromewagener.soutils.utilities.InetAddressUtilities;

public class DemoActivity extends Activity implements SoutilsObserver {
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
	/** The file transfer server which is able to offer files to a connected file transfer client */
	private static FileTransferServer fileTransferServer = null;
	/** The file transfer client which is able to receive files from a connected file transfer server */
	private static FileTransferClient fileTransferClient = null;
	
	@Override
	/** PLEASE NOTE: The following is bad practice! However, to illustrate the use of Soutils, all code resides within the onCreate. */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo_selection);
		
		TextView currentIPAddress = (TextView) findViewById(R.id.edtCurrentIPAddress);
		try {
			currentIPAddress.setText(InetAddressUtilities.getCurrentIPv4Address());
		} catch (SocketException e) {
			Log.e("SOUTILS", "Cannot determine IP address");
		}
		
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
		    		communicationManager = new CommunicationManager(port, DemoActivity.this);
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
					communication = new Communication(hostIPAddress, port, DemoActivity.this);
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
		    		
		    		try {
			    		// Retrieve broadcast address (for the demo we will use the first broadcast address available)
			    		InetAddress broadcastAddress = InetAddressUtilities.getAllIPsAndAssignedBroadcastAddresses().values().iterator().next();
			    		
			    		// Retrieve the port from the edit box
			    		int port = Integer.valueOf(((TextView) findViewById(R.id.edtSoutilsPort)).getText().toString());
			    		
			    		// Create the beacon sender
				    	beaconSender = new BeaconSender(beaconMessage, broadcastAddress, port, DemoActivity.this);
				    	beaconSender.start();
		    		} catch (SocketException socketException) {
		    			Log.e("SOUTILS", "Could not determine broadcast address", socketException);
		    		}
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
		    		
		    		// Retrieve the port from the edit box
		    		int port = Integer.valueOf(((TextView) findViewById(R.id.edtSoutilsPort)).getText().toString());
		    		
		    		// Start listening
			    	beaconReceiver = new BeaconReceiver(port, DemoActivity.this);
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
		    		
					try {
						// Retrieve broadcast address (for the demo we will use the first broadcast address available)
			    		InetAddress broadcastAddress = InetAddressUtilities.getAllIPsAndAssignedBroadcastAddresses().values().iterator().next();
						// Start broadcasting and listening at the same time
			    		beaconSenderAndReceiver = new BeaconSenderAndReceiver(message, broadcastAddress, 4242, DemoActivity.this);
				    	beaconSenderAndReceiver.start();
					} catch (SocketException e) {
						Log.e("SOUTILS", "Couldn't determine broadcast address");
					}
		    		
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
		
		/**
		 * Starting a file transfer server to offer a file to a connected file transfer client
		 * -----------------------------------------------------------------------
		 **/
		Button startFileTransferServer = (Button) findViewById(R.id.btnSendFile);
		startFileTransferServer.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	if (fileTransferServer == null) {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.disableEverything(DemoActivity.this);
		    		ActivityHelpers.adaptGUIForSendingFile(DemoActivity.this);
		    		
		    		// Generating dummy file on external storage (you can use any type of file)
		    		File autoGeneratedTextFile = generateTextFileForTransfer();
		    		
		    		// Retrieve the port from the edit box
		    		int port = Integer.valueOf(((TextView) findViewById(R.id.edtSoutilsPort)).getText().toString());
		    		
		    		// Start offering the file
		    		FileTransferServer fileTransferServer = new FileTransferServer(autoGeneratedTextFile.getAbsolutePath(), port, DemoActivity.this);
		    		fileTransferServer.start();
		    		
		    		// Wait for upload to finish. This will block the UI!
					// This busy waiting is not very pretty! Please check the documentation for better alternatives
					while (!fileTransferServer.isDone()) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
					}
					
					// Show small info message and adapt GUI - Not important with respect to Soutils
					TextView edtReceivedMessagesAndBeacons = ((TextView) findViewById(R.id.edtReceivedMessagesAndBeacons));
					edtReceivedMessagesAndBeacons.setText("File successfully uploaded\n" + edtReceivedMessagesAndBeacons.getText().toString());
					ActivityHelpers.enableEverything(DemoActivity.this);
		    		ActivityHelpers.resetGUIForSendingFile(DemoActivity.this);
		    		fileTransferServer = null;
		    	} else {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.enableEverything(DemoActivity.this);
		    		ActivityHelpers.resetGUIForSendingFile(DemoActivity.this);	    		
		    		
		    		// Terminate the file transfer server
		    		if (!fileTransferServer.isDone()) {
		    			fileTransferServer.done();
		    		}
		    		fileTransferServer = null;
		    	}
		    }
		});
		
		/**
		 * Starting a file transfer client to download a file from a connected file transfer server
		 * -----------------------------------------------------------------------
		 **/
		Button startFileTransferClient = (Button) findViewById(R.id.btnReceiveFile);
		startFileTransferClient.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	if (fileTransferClient == null) {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.disableEverything(DemoActivity.this);
		    		ActivityHelpers.adaptGUIForReceivingFile(DemoActivity.this);
		    		
		    		// Get server address
		    		String hostIPAddress = ((TextView) findViewById(R.id.edtHostIPAddress)).getText().toString();
		    		
		    		// Retrieve the port from the edit box
		    		int port = Integer.valueOf(((TextView) findViewById(R.id.edtSoutilsPort)).getText().toString());
		    		
		    		// Start downloading the file
		    		fileTransferClient = new FileTransferClient(Environment.getExternalStorageDirectory() + "/soutils.txt", hostIPAddress, port, DemoActivity.this);
					fileTransferClient.start();
		    		
		    		// Wait for upload to finish. This will block the UI!
					// This busy waiting is not very pretty! Please check the documentation for better alternatives
					while (!fileTransferClient.isDone()) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
					}
					
					// Show small info message and adapt GUI - Not important with respect to Soutils
					TextView edtReceivedMessagesAndBeacons = ((TextView) findViewById(R.id.edtReceivedMessagesAndBeacons));
					edtReceivedMessagesAndBeacons.setText("File successfully downloaded to external storage\n" + edtReceivedMessagesAndBeacons.getText().toString());
					ActivityHelpers.enableEverything(DemoActivity.this);
		    		ActivityHelpers.resetGUIForReceivingFile(DemoActivity.this);
		    		fileTransferClient = null;
		    	} else {
		    		// Adapt GUI - Not important with respect to Soutils
		    		ActivityHelpers.enableEverything(DemoActivity.this);
		    		ActivityHelpers.resetGUIForReceivingFile(DemoActivity.this);	    		
		    		
		    		// Terminate the file transfer client
		    		if (!fileTransferClient.isDone()) {
		    			fileTransferClient.done();
		    		}
		    		fileTransferClient = null;
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
	
	public File generateTextFileForTransfer() {
		File file = new File(Environment.getExternalStorageDirectory() + "/soutilsAndroid.txt");
		Calendar cal = Calendar.getInstance();      
		String format = String.valueOf(cal.get(Calendar.YEAR)) + "/" + 
						String.valueOf(cal.get(Calendar.MONTH)) + "/" + 
						String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + " " + 
						String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" +
						String.valueOf(cal.get(Calendar.MINUTE));
		try {
		     FileOutputStream os = new FileOutputStream(file, false); 
		     OutputStreamWriter out = new OutputStreamWriter(os);
		     out.write(android.os.Build.MANUFACTURER + "\n" + android.os.Build.MODEL + "\n" + format);
		     out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		return file;
	}

	private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
	
	@Override
	public void handleSoutilsMessage(final SoutilsMessage soutilsMessage) {
		runOnUiThread(new Runnable(){
		    @Override
		    public void run() {
				TextView edtReceivedMessagesAndBeacons = ((TextView) DemoActivity.this.findViewById(R.id.edtReceivedMessagesAndBeacons));
				edtReceivedMessagesAndBeacons.setText(
						timeFormatter.format(new Date()) + ": " + 
						soutilsMessage.getMessageType() + " : " + 
						soutilsMessage.getContent() + " (" + soutilsMessage.getSenderAddress() + ")\n" + 
						edtReceivedMessagesAndBeacons.getText().toString());
		    }
		});
	}
}
