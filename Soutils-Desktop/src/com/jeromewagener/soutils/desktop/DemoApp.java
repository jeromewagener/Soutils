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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.jeromewagener.soutils.desktop.beaconing.BeaconReceiver;
import com.jeromewagener.soutils.desktop.beaconing.BeaconSender;
import com.jeromewagener.soutils.desktop.beaconing.BeaconSenderAndReceiver;
import com.jeromewagener.soutils.desktop.networking.Communication;
import com.jeromewagener.soutils.desktop.networking.CommunicationManager;
import com.jeromewagener.soutils.desktop.networking.MessageReceptionObserver;
import com.jeromewagener.soutils.desktop.networking.NetworkFacade;

public class DemoApp extends JFrame implements MessageReceptionObserver {
	private static final long serialVersionUID = 8202163031446431261L;
	
	// TODO make dynamic
	private static final int PORT = 4141;
	
	private static Communication communication;
	private static CommunicationManager communicationManager;
	private static BeaconSender beaconSender;
	private static BeaconReceiver beaconReceiver;
	private static BeaconSenderAndReceiver beaconSenderAndReceiver;
	
	private static DemoApp instance = new DemoApp();
	private static JTextPane edtTextMessageBox;
	
	private static JButton btnStartCommunication, btnStartCommunicationManager, btnSendMessage, btnSendMessageToAllClients, btnStartBeaconSender, btnStartBeaconReceiver, btnStartBeaconSenderAndReceiver;
	private static JTextField edtPort, edtMessageToBeSent, edtHostAddress;
	
	private static void disableEverything() {
		btnStartCommunication.setEnabled(false);
		btnStartCommunicationManager.setEnabled(false);
		btnSendMessage.setEnabled(false);
		btnSendMessageToAllClients.setEnabled(false);
		btnStartBeaconSender.setEnabled(false);
		btnStartBeaconReceiver.setEnabled(false);
		btnStartBeaconSenderAndReceiver.setEnabled(false);
		
		edtPort.setEnabled(false);
		edtHostAddress.setEnabled(false);
	}
	
	private static void enableEverything() {
		btnStartCommunication.setEnabled(true);
		btnStartCommunicationManager.setEnabled(true);
		
		btnStartBeaconSender.setEnabled(true);
		btnStartBeaconReceiver.setEnabled(true);
		btnStartBeaconSenderAndReceiver.setEnabled(true);
		
		edtPort.setEnabled(true);
		edtHostAddress.setEnabled(true);
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setTitle("Soutils Demo App");

	    JPanel panelContainer = new JPanel();
	    panelContainer.setLayout(new GridLayout(5, 1));
	    panelContainer.setBorder(new EmptyBorder(3,3,3,3));
	    
	    Border blackline = BorderFactory.createLineBorder(Color.black);
	    
	    JPanel panelGeneralInformation = new JPanel();
	    panelGeneralInformation.setBorder(BorderFactory.createTitledBorder(blackline , "General Information"));
	    panelGeneralInformation.setLayout(new GridLayout(3, 2));
	    
	    JLabel lblCurrentIPAddress = new JLabel();
	    lblCurrentIPAddress.setText("Current IP Address:");
	    panelGeneralInformation.add(lblCurrentIPAddress);
	    
	    JTextField edtCurrentIPAddress = new JTextField(10);
	    edtCurrentIPAddress.setText(NetworkFacade.getCurrentIPv4Address());
	    edtCurrentIPAddress.setEnabled(false);
	    panelGeneralInformation.add(edtCurrentIPAddress);
	    
	    JLabel lblSoutilsPort = new JLabel();
	    lblSoutilsPort.setText("Application Port:");
	    panelGeneralInformation.add(lblSoutilsPort);
	    
	    edtPort = new JTextField(10);
	    edtPort.setText(String.valueOf(PORT));
	    panelGeneralInformation.add(edtPort);
	    
	    JLabel lblMessageToBeSent = new JLabel();
	    lblMessageToBeSent.setText("Message to be sent:");
	    panelGeneralInformation.add(lblMessageToBeSent);
	    
	    edtMessageToBeSent = new JTextField(10);
	    edtMessageToBeSent.setText("Hello Message");
	    panelGeneralInformation.add(edtMessageToBeSent);
	    
	    JPanel panelCommunication = new JPanel();
	    panelCommunication.setBorder(BorderFactory.createTitledBorder(blackline , "Connect to Remote Server"));
	    panelCommunication.setLayout(new GridLayout(2, 2));
	    
	    edtHostAddress = new JTextField(10);
	    edtHostAddress.setText("192.168.x.x");
	    panelCommunication.add(edtHostAddress);
	    
	    JLabel lblEmpty = new JLabel();
	    lblEmpty.setText("");
	    panelCommunication.add(lblEmpty);
	    
	    btnStartCommunication = new JButton("Start Communication");
	    btnStartCommunication.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (communication == null) {
					disableEverything();
					
					btnStartCommunication.setEnabled(true);
					btnStartCommunication.setText("Stop Communication");
					btnSendMessage.setEnabled(true);
					
					communication = new Communication(edtHostAddress.getText(), PORT);
					communication.start();
					
					communication.registerMessageReceptionObserver(instance);
				} else {
					communication.done();
					communication = null;
					
					btnStartCommunication.setText("Start Communication");
					enableEverything();										
				}
			}
		});
	    panelCommunication.add(btnStartCommunication);
	    
	    btnSendMessage = new JButton("Send Message");
	    btnSendMessage.setEnabled(false);
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
	    panelCommunication.add(btnSendMessage);
	    
	    JPanel panelCommunicationManager = new JPanel();
	    panelCommunicationManager.setBorder(BorderFactory.createTitledBorder(blackline , "Start Local Server"));
	    panelCommunicationManager.setLayout(new GridLayout(1, 2));
	    
	    btnStartCommunicationManager = new JButton("Start Comm. Manager");
	    btnStartCommunicationManager.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (communicationManager == null) {
					disableEverything();
					
					btnStartCommunicationManager.setEnabled(true);
					btnStartCommunicationManager.setText("Stop Comm. Manager");
					btnSendMessageToAllClients.setEnabled(true);
					
					communicationManager = new CommunicationManager(PORT);
					communicationManager.start();
					
					communicationManager.registerMessageReceptionObserver(instance);
				} else {
					communicationManager.done();
					communicationManager = null;
					
					btnStartCommunicationManager.setText("Start Comm. Manager");
					enableEverything();										
				}
			}
		});   
	    panelCommunicationManager.add(btnStartCommunicationManager);
	    
	    btnSendMessageToAllClients = new JButton("Message all Clients");
	    btnSendMessageToAllClients.setEnabled(false);
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
	    
	    panelCommunicationManager.add(btnSendMessageToAllClients);
	    
	    JPanel panelBeaconing = new JPanel();
	    panelBeaconing.setBorder(BorderFactory.createTitledBorder(blackline , "Send and/or receive Beacons"));
	    panelBeaconing.setLayout(new GridLayout(3, 1));
	    
	    btnStartBeaconSender = new JButton("Start Beacon Sender");
	    btnStartBeaconSender.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (beaconSender == null) {
					disableEverything();
					
					btnStartBeaconSender.setText("Stop Beacon Sender");
					btnStartBeaconSender.setEnabled(true);
					
					HashMap<InetAddress, InetAddress> ipsAndBroadcastAddresses = NetworkFacade.getAllIPsAndAssignedBroadcastAddresses();
					InetAddress firstBroadcastAddress = ipsAndBroadcastAddresses.get(ipsAndBroadcastAddresses.keySet().iterator().next());				
					
					beaconSender = new BeaconSender(edtMessageToBeSent.getText(), firstBroadcastAddress);
					beaconSender.start();
				} else {
					beaconSender.done();
					beaconSender = null;
					
					btnStartBeaconSender.setText("Start Beacon Sender");
					enableEverything();
				}
			}
		});
	    panelBeaconing.add(btnStartBeaconSender);
	    
	    btnStartBeaconReceiver = new JButton("Start Beacon Receiver");
	    btnStartBeaconReceiver.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (beaconReceiver == null) {
					disableEverything();
					
					btnStartBeaconReceiver.setText("Stop Beacon Receiver");
					btnStartBeaconReceiver.setEnabled(true);
					
					beaconReceiver = new BeaconReceiver();
					beaconReceiver.start();
					beaconReceiver.registerMessageReceptionObserver(instance);
				} else {
					beaconReceiver.done();
					beaconReceiver = null;
					
					btnStartBeaconReceiver.setText("Start Beacon Receiver");
					enableEverything();
				}
			}
		});
	    panelBeaconing.add(btnStartBeaconReceiver);
	    
	    btnStartBeaconSenderAndReceiver = new JButton("Start Sender & Receiver");
	    btnStartBeaconSenderAndReceiver.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (beaconSenderAndReceiver == null) {
					disableEverything();
					
					btnStartBeaconSenderAndReceiver.setText("Stop Sender & Receiver");
					btnStartBeaconSenderAndReceiver.setEnabled(true);
					
					HashMap<InetAddress, InetAddress> ipsAndBroadcastAddresses = NetworkFacade.getAllIPsAndAssignedBroadcastAddresses();
					InetAddress firstBroadcastAddress = ipsAndBroadcastAddresses.get(ipsAndBroadcastAddresses.keySet().iterator().next());				
									
					beaconSenderAndReceiver = new BeaconSenderAndReceiver(edtMessageToBeSent.getText(), firstBroadcastAddress);
					beaconSenderAndReceiver.start();
					beaconSenderAndReceiver.registerMessageReceptionObserver(instance);
				} else {
					beaconSenderAndReceiver.done();
					beaconSenderAndReceiver = null;
					
					btnStartBeaconSenderAndReceiver.setText("Start Sender & Receiver");
					enableEverything();
				}
			}
		});
	    panelBeaconing.add(btnStartBeaconSenderAndReceiver);
	    
	    JPanel panelMessageBox = new JPanel();
	    panelMessageBox.setBorder(BorderFactory.createTitledBorder(blackline , "Received Messages & Beacons"));
	    panelMessageBox.setLayout(new GridLayout(1, 1));
	    
	    edtTextMessageBox = new JTextPane();
	    edtTextMessageBox.setPreferredSize(new Dimension(400, 100));
	    panelMessageBox.add(edtTextMessageBox);
	    
	    addWithPadding(panelContainer, panelGeneralInformation);
	    addWithPadding(panelContainer, panelCommunication);
	    addWithPadding(panelContainer, panelCommunicationManager);
	    addWithPadding(panelContainer, panelBeaconing);
	    addWithPadding(panelContainer, panelMessageBox);
	    
	    frame.add(panelContainer);
	    
	    frame.pack();
	    frame.setVisible(true);
	}
	
	private static void addWithPadding(JPanel parentPanel, JPanel panelToBeAdded) {
		JPanel spaceContainer = new JPanel(); 
		spaceContainer.setBorder(new EmptyBorder(3,3,3,3));
		spaceContainer.add(panelToBeAdded);
		
		parentPanel.add(spaceContainer);
	}

	
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	@Override
	public void reactToMessage(String ipAddress, String message) {
		edtTextMessageBox.setText(
				sdf.format(new Date()) + ": " + 
				message + " (" + ipAddress + ")\n" + 
				edtTextMessageBox.getText());
	}
}
