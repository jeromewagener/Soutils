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
		
		((Button) demoActivity.findViewById(R.id.btnSendFile)).setEnabled(false);
		((Button) demoActivity.findViewById(R.id.btnReceiveFile)).setEnabled(false);
	}
	
	public static void enableEverything(DemoActivity demoActivity) {
		((TextView) demoActivity.findViewById(R.id.edtSoutilsPort)).setEnabled(true);
		((TextView) demoActivity.findViewById(R.id.edtHostIPAddress)).setEnabled(true);
		
		((Button) demoActivity.findViewById(R.id.btnStartCommunication)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartCommunicationManager)).setEnabled(true);
		
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSender)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartBeaconReceiver)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnStartBeaconSenderReceiver)).setEnabled(true);
		
		((Button) demoActivity.findViewById(R.id.btnSendFile)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnReceiveFile)).setEnabled(true);
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
	
	public static void adaptGUIForSendingFile(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnSendFile)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnSendFile)).setText("Stop Sending");
	}
	
	public static void resetGUIForSendingFile(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnSendFile)).setText("Send File");
	}
	
	public static void adaptGUIForReceivingFile(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnReceiveFile)).setEnabled(true);
		((Button) demoActivity.findViewById(R.id.btnReceiveFile)).setText("Stop Receiving");
	}
	
	public static void resetGUIForReceivingFile(DemoActivity demoActivity) {
		((Button) demoActivity.findViewById(R.id.btnReceiveFile)).setText("Receive File");
	}
}
