package com.jeromewagener.soutils.android;

import android.widget.Button;
import android.widget.TextView;

public class ActivityHelpers {
	public static void disableEverything(DemoActivity demoActivity) {
		((TextView) demoActivity.findViewById(R.id.edtSoutilsPort)).setEnabled(false);
		((TextView) demoActivity.findViewById(R.id.edtHostIPAddress)).setEnabled(false);
		
		((Button) demoActivity.findViewById(R.id.btnStartCommunication)).setEnabled(false);
		((Button) demoActivity.findViewById(R.id.btnStartCommunicationManager)).setEnabled(false);
		
		((Button) demoActivity.findViewById(R.id.btnSendMessageToServer)).setEnabled(false);
		((Button) demoActivity.findViewById(R.id.btnSendMessageToAllClients)).setEnabled(false);
		
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSender)).setEnabled(false);
		((Button) demoActivity.findViewById(R.id.btnStartBeaconReceiver)).setEnabled(false);
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSenderReceiver)).setEnabled(false);
	}
	
	public static void enableEverything(DemoActivity demoActivity) {
		((TextView) demoActivity.findViewById(R.id.edtSoutilsPort)).setEnabled(true);
		((TextView) demoActivity.findViewById(R.id.edtHostIPAddress)).setEnabled(true);
		
		((Button) demoActivity.findViewById(R.id.btnStartCommunication)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartCommunicationManager)).setEnabled(true);
		
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSender)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartBeaconReceiver)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSenderReceiver)).setEnabled(true);
	}
	
	public static void adaptGUIForCommunicationManager(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnStartCommunicationManager)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartCommunicationManager)).setText("Stop Comm. Manager");
		((Button) demoActivity.findViewById(R.id.btnSendMessageToAllClients)).setEnabled(true);
	}
	
	public static void resetGUIForCommunicationManager(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnStartCommunicationManager)).setText("Start Comm. Manager");
		((Button) demoActivity.findViewById(R.id.btnSendMessageToAllClients)).setEnabled(false);
	}
	
	public static void adaptGUIForCommunication(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnStartCommunication)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartCommunication)).setText("Stop Communication");
		((Button) demoActivity.findViewById(R.id.btnSendMessageToServer)).setEnabled(true);
	}
	
	public static void resetGUIForCommunication(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnStartCommunication)).setText("Start Communication");
		((Button) demoActivity.findViewById(R.id.btnSendMessageToServer)).setEnabled(false);
	}
	
	public static void adaptGUIForBeaconSender(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSender)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSender)).setText("Stop Beacon Sender");
	}
	
	public static void resetGUIForBeaconSender(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSender)).setText("Start Beacon Sender");
	}
	
	public static void adaptGUIForBeaconReceiver(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnStartBeaconReceiver)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartBeaconReceiver)).setText("Stop Beacon Receiver");
	}
	
	public static void resetGUIForBeaconReceiver(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSender)).setText("Start Beacon Receiver");
	}
	
	public static void adaptGUIForBeaconSenderReceiver(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSenderReceiver)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSenderReceiver)).setText("Stop Sender & Receiver");
	}
	
	public static void resetGUIForBeaconSenderReceiver(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSenderReceiver)).setText("Stop Sender & Receiver");
	}
}
