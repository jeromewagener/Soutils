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

package com.jeromewagener.soutils.android.beaconing;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jeromewagener.soutils.beaconing.BeaconParameters;

/** A thread which receives and (internally forwards) UDP beacons */
public class BeaconReceiver extends Thread {
	/** This variable must be set to true in order to stop the thread */
	private boolean done = false;
	/** The android handler to which received messages are forwarded */
	private Handler messageHandler = null;	
	
	/**
	 * Creates a new thread which is able to receive and internally forward UDP beacons. As for any Java thread, 
	 * the {@link #run()} must be called to actually start the thread.
	 * @param messageHandler the internal handler to which the beacons should be forwarded to
	 * @see Handler
	 * @see #run()
	 */
	public BeaconReceiver(Handler messageHandler) {
		this.messageHandler = messageHandler;
	}
	
	/**
	 * Call this method to stop this thread from receiving and forwarding beacons
	 */
	public void done() {
		done = true;
	}

	/**
	 * Starts the thread which in turn receives and forwards beacons using the default settings. A beacon that is 
	 * forwarded to the handler will contain a bundle with two strings. Use the "receivedMessage" key to retrieve the 
	 * actually received beacon message. Use the "senderIpAddress" key to retrieve the sender IP address of the beacon.
	 * @see BeaconParameters
	 * @see #done()
	 * */
	public void run() {
		DatagramSocket socket = null;

		try {
			socket = new DatagramSocket(BeaconParameters.BEACON_PORT);
		} catch (Exception ex) {
			Log.e("SOUTILS", "An error occurred while initializing the datagram socket port", ex);

			return;
		}

		try {
			socket.setBroadcast(true);
			socket.setSoTimeout(BeaconParameters.DATAGRAM_BUFFER_SIZE);
		} catch (SocketException e) {
			Log.e("SOUTILS", "An error occurred while initializing the datagram socket buffer size", e);
			socket.close();

			return;
		}

		while(!done) {
			try {
				byte[] buffer = new byte[BeaconParameters.DATAGRAM_BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);

				Message message = Message.obtain(messageHandler);
				Bundle bundle = new Bundle();
				bundle.putString("receivedMessage", new String(packet.getData(), "UTF-8").trim());
				bundle.putString("senderIpAddress", packet.getAddress().getHostAddress());
				message.setData(bundle);

				messageHandler.sendMessage(message);
				
			} catch (InterruptedIOException iie) {
				// The socket timed-out, re-enter the loop and listen again			
				continue;
			} catch(IOException ioe) {
				Log.e("SOUTILS", "An error occurred while receiving a datagram packet and/or decoding the received message", ioe);
				socket.close();
				return;
			}
		}

		socket.close();
	}
}
