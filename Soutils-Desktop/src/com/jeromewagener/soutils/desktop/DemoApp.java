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

package com.jeromewagener.soutils.desktop;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import com.jeromewagener.soutils.desktop.beaconing.BeaconReceiver;
import com.jeromewagener.soutils.desktop.beaconing.BeaconSender;
import com.jeromewagener.soutils.desktop.beaconing.BeaconSenderAndReceiver;
import com.jeromewagener.soutils.desktop.networking.Communication;
import com.jeromewagener.soutils.desktop.networking.CommunicationManager;
import com.jeromewagener.soutils.desktop.networking.MessageReceptionObserver;
import com.jeromewagener.soutils.desktop.networking.NetworkFacade;

public class DemoApp extends JFrame implements MessageReceptionObserver {
	private static final long serialVersionUID = 8202163031446431261L;
	
	private static Communication communication;
	private static CommunicationManager communicationManager;
	private static BeaconSender beaconSender;
	private static BeaconReceiver beaconReceiver;
	private static BeaconSenderAndReceiver beaconSenderAndReceiver;
	
	private static final DemoApp instance = new DemoApp();
	private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
	
	// The following GUI components are package protected to allow access from the DemoHelper
	static JTextPane edtTextMessageBox;
	static JButton btnStartCommunication, btnStartCommunicationManager, btnSendMessage, btnSendMessageToAllClients, btnStartBeaconSender, btnStartBeaconReceiver, btnStartBeaconSenderAndReceiver;
	static JTextField edtPort, edtMessageToBeSent, edtHostAddress;
	
	public static void main(String[] args) {
		// Initializes the GUI, including all text boxes, buttons, labels... Not important with respect to Soutils
		DemoHelper.setupGUI();
	    
		/**
		 * Creating a socket server using the communication manager
		 * --------------------------------------------------------
		 **/
	    btnStartCommunicationManager.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (communicationManager == null) {
					DemoHelper.disableEverything();
					
					btnStartCommunicationManager.setEnabled(true);
					btnStartCommunicationManager.setText("Stop Comm. Manager");
					btnSendMessageToAllClients.setEnabled(true);
					
					communicationManager = new CommunicationManager(Integer.valueOf(edtPort.getText()));
					communicationManager.start();
					
					communicationManager.registerMessageReceptionObserver(instance);
				} else {
					communicationManager.done();
					communicationManager = null;
					
					btnStartCommunicationManager.setText("Start Comm. Manager");
					DemoHelper.enableEverything();										
				}
			}
		});
		
	    /**
		 * Message all clients using the communication manager
		 * ---------------------------------------------------
		 **/
	    btnSendMessageToAllClients.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				communicationManager.sendMessageToAllConnectedPeers(edtMessageToBeSent.getText());
			}
		});
	    
		/**
		 * Creating a socket client using a communication
		 * ----------------------------------------------
		 **/
	    btnStartCommunication.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (communication == null) {
					DemoHelper.disableEverything();
					
					btnStartCommunication.setEnabled(true);
					btnStartCommunication.setText("Stop Communication");
					btnSendMessage.setEnabled(true);
					
					communication = new Communication(edtHostAddress.getText(), Integer.valueOf(edtPort.getText()));
					communication.start();
					
					communication.registerMessageReceptionObserver(instance);
				} else {
					communication.done();
					communication = null;
					
					btnStartCommunication.setText("Start Communication");
					DemoHelper.enableEverything();										
				}
			}
		});
	    
		/**
		 * Send a message to the communication manager
		 * -------------------------------------------
		 **/
	    btnSendMessage.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				communication.sendMessage(edtMessageToBeSent.getText());
			}
		});
	    
		/**
		 * Start broadcasting UDP beacons / messages
		 * -----------------------------------------
		 **/
	    btnStartBeaconSender.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (beaconSender == null) {
					DemoHelper.disableEverything();
					
					btnStartBeaconSender.setText("Stop Beacon Sender");
					btnStartBeaconSender.setEnabled(true);
					
					// TODO do not simply use the first broadcast address! Loopy loop..
					HashMap<InetAddress, InetAddress> ipsAndBroadcastAddresses = NetworkFacade.getAllIPsAndAssignedBroadcastAddresses();
					InetAddress firstBroadcastAddress = ipsAndBroadcastAddresses.get(ipsAndBroadcastAddresses.keySet().iterator().next());				
					
					beaconSender = new BeaconSender(edtMessageToBeSent.getText(), firstBroadcastAddress);
					beaconSender.start();
				} else {
					beaconSender.done();
					beaconSender = null;
					
					btnStartBeaconSender.setText("Start Beacon Sender");
					DemoHelper.enableEverything();
				}
			}
		});
	    
	    /**
		 * Start listening for UDP beacons / messages
		 * ------------------------------------------
		 **/
	    btnStartBeaconReceiver.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (beaconReceiver == null) {
					DemoHelper.disableEverything();
					
					btnStartBeaconReceiver.setText("Stop Beacon Receiver");
					btnStartBeaconReceiver.setEnabled(true);
					
					beaconReceiver = new BeaconReceiver();
					beaconReceiver.start();
					beaconReceiver.registerMessageReceptionObserver(instance);
				} else {
					beaconReceiver.done();
					beaconReceiver = null;
					
					btnStartBeaconReceiver.setText("Start Beacon Receiver");
					DemoHelper.enableEverything();
				}
			}
		});
	    
		/**
		 * Start broadcasting and receiving UDP beacons / messages simultaneously
		 * -----------------------------------------------------------------------
		 **/
	    btnStartBeaconSenderAndReceiver.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {} 
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (beaconSenderAndReceiver == null) {
					DemoHelper.disableEverything();
					
					btnStartBeaconSenderAndReceiver.setText("Stop Sender & Receiver");
					btnStartBeaconSenderAndReceiver.setEnabled(true);
					
					// TODO do not simply use the first broadcast address! Loopy loop..
					HashMap<InetAddress, InetAddress> ipsAndBroadcastAddresses = NetworkFacade.getAllIPsAndAssignedBroadcastAddresses();
					InetAddress firstBroadcastAddress = ipsAndBroadcastAddresses.get(ipsAndBroadcastAddresses.keySet().iterator().next());				
									
					beaconSenderAndReceiver = new BeaconSenderAndReceiver(edtMessageToBeSent.getText(), firstBroadcastAddress);
					beaconSenderAndReceiver.start();
					beaconSenderAndReceiver.registerMessageReceptionObserver(instance);
				} else {
					beaconSenderAndReceiver.done();
					beaconSenderAndReceiver = null;
					
					btnStartBeaconSenderAndReceiver.setText("Start Sender & Receiver");
					DemoHelper.enableEverything();
				}
			}
		});
	}
	
	@Override
	public void reactToMessage(String ipAddress, String message) {
		edtTextMessageBox.setText(
				timeFormatter.format(new Date()) + ": " + message + " (" + ipAddress + ")\n" + 
				edtTextMessageBox.getText());
	}
}
