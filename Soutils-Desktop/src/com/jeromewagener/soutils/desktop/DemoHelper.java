package com.jeromewagener.soutils.desktop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.jeromewagener.soutils.desktop.networking.NetworkFacade;

public class DemoHelper {
	public static void setupGUI() {
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
	    
	    DemoApp.edtPort = new JTextField(10);
	    DemoApp.edtPort.setText("4141");
	    panelGeneralInformation.add(DemoApp.edtPort);
	    
	    JLabel lblMessageToBeSent = new JLabel();
	    lblMessageToBeSent.setText("Message to be sent:");
	    panelGeneralInformation.add(lblMessageToBeSent);
	    
	    DemoApp.edtMessageToBeSent = new JTextField(10);
	    DemoApp.edtMessageToBeSent.setText("Hello Message");
	    panelGeneralInformation.add(DemoApp.edtMessageToBeSent);
	    
	    JPanel panelCommunication = new JPanel();
	    panelCommunication.setBorder(BorderFactory.createTitledBorder(blackline , "Connect to Remote Server"));
	    panelCommunication.setLayout(new GridLayout(2, 2));
	    
	    DemoApp.edtHostAddress = new JTextField(10);
	    DemoApp.edtHostAddress.setText("192.168.x.x");
	    panelCommunication.add(DemoApp.edtHostAddress);
	    
	    JLabel lblEmpty = new JLabel();
	    lblEmpty.setText("");
	    panelCommunication.add(lblEmpty);
	    
	    DemoApp.btnStartCommunication = new JButton("Start Communication");
	    panelCommunication.add(DemoApp.btnStartCommunication);
	    
	    DemoApp.btnSendMessage = new JButton("Send Message");
	    DemoApp.btnSendMessage.setEnabled(false);
	    panelCommunication.add(DemoApp.btnSendMessage);
	    
	    JPanel panelCommunicationManager = new JPanel();
	    panelCommunicationManager.setBorder(BorderFactory.createTitledBorder(blackline , "Start Local Server"));
	    panelCommunicationManager.setLayout(new GridLayout(1, 2));
	    
	    DemoApp.btnStartCommunicationManager = new JButton("Start Comm. Manager");
	       
	    panelCommunicationManager.add(DemoApp.btnStartCommunicationManager);
	    
	    DemoApp.btnSendMessageToAllClients = new JButton("Message all Clients");
	    DemoApp.btnSendMessageToAllClients.setEnabled(false);
	    
	    
	    panelCommunicationManager.add(DemoApp.btnSendMessageToAllClients);
	    
	    JPanel panelBeaconing = new JPanel();
	    panelBeaconing.setBorder(BorderFactory.createTitledBorder(blackline , "Send and/or receive Beacons"));
	    panelBeaconing.setLayout(new GridLayout(3, 1));
	    
	    DemoApp.btnStartBeaconSender = new JButton("Start Beacon Sender");
	    panelBeaconing.add(DemoApp.btnStartBeaconSender);
	    
	    DemoApp.btnStartBeaconReceiver = new JButton("Start Beacon Receiver");
	    panelBeaconing.add(DemoApp.btnStartBeaconReceiver);
	    
	    DemoApp.btnStartBeaconSenderAndReceiver = new JButton("Start Sender & Receiver");
	    panelBeaconing.add(DemoApp.btnStartBeaconSenderAndReceiver);
	    
	    JPanel panelMessageBox = new JPanel();
	    panelMessageBox.setBorder(BorderFactory.createTitledBorder(blackline , "Received Messages & Beacons"));
	    panelMessageBox.setLayout(new GridLayout(1, 1));
	    
	    DemoApp.edtTextMessageBox = new JTextPane();
	    DemoApp.edtTextMessageBox.setPreferredSize(new Dimension(400, 100));
	    panelMessageBox.add(DemoApp.edtTextMessageBox);
	    
	    DemoHelper.addWithPadding(panelContainer, panelGeneralInformation);
	    DemoHelper.addWithPadding(panelContainer, panelCommunication);
	    DemoHelper.addWithPadding(panelContainer, panelCommunicationManager);
	    DemoHelper.addWithPadding(panelContainer, panelBeaconing);
	    DemoHelper.addWithPadding(panelContainer, panelMessageBox);
	    
	    frame.add(panelContainer);
	    
	    frame.pack();
	    frame.setVisible(true);
	}
	
	public static void disableEverything() {
		DemoApp.btnStartCommunication.setEnabled(false);
		DemoApp.btnStartCommunicationManager.setEnabled(false);
		DemoApp.btnSendMessage.setEnabled(false);
		DemoApp.btnSendMessageToAllClients.setEnabled(false);
		DemoApp.btnStartBeaconSender.setEnabled(false);
		DemoApp.btnStartBeaconReceiver.setEnabled(false);
		DemoApp.btnStartBeaconSenderAndReceiver.setEnabled(false);
		
		DemoApp.edtPort.setEnabled(false);
		DemoApp.edtHostAddress.setEnabled(false);
	}
	
	public static void enableEverything() {
		DemoApp.btnStartCommunication.setEnabled(true);
		DemoApp.btnStartCommunicationManager.setEnabled(true);
		
		DemoApp.btnStartBeaconSender.setEnabled(true);
		DemoApp.btnStartBeaconReceiver.setEnabled(true);
		DemoApp.btnStartBeaconSenderAndReceiver.setEnabled(true);
		
		DemoApp.edtPort.setEnabled(true);
		DemoApp.edtHostAddress.setEnabled(true);
	}
	
	public static void addWithPadding(JPanel parentPanel, JPanel panelToBeAdded) {
		JPanel spaceContainer = new JPanel(); 
		spaceContainer.setBorder(new EmptyBorder(3,3,3,3));
		spaceContainer.add(panelToBeAdded);
		
		parentPanel.add(spaceContainer);
	}
}
