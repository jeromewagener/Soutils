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
import java.net.InetAddress;
import java.net.SocketException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jeromewagener.soutils.beaconing.BeaconParameters;

/** A thread which sends UDP beacons while receiving and (internally forwarding) other devices's UDP beacons */
public class BeaconSenderAndReceiver extends Thread  {
	/** The message to be transferred by the beacon */
	private String message;
	/** This variable must be set to true in order to stop the thread */
	private boolean done = false;
	/** The android handler to which received messages are forwarded */
	private Handler messageHandler = null;
	/** The address used to broadcast the beacon */
	private InetAddress broadcastAddress;

	/**
	 * Creates a new thread which is able to receive and internally forward UDP beacons. Furthermore, the thread constantly
	 * sends out own beacons. As for any Java thread, the {@link #run()} must be called to actually start the thread.
	 * @param messageHandler the internal handler to which the beacons should be forwarded to
	 * @see Handler
	 * @see #run()
	 */
	public BeaconSenderAndReceiver(String message, InetAddress broadcastAddress, Handler messageHandler) {
		this.broadcastAddress = broadcastAddress;
		this.message = message;
		this.messageHandler = messageHandler;
	}

	/**
	 * Call this method to prevent this thread from receiving and sending any further UDP beacons
	 */
	public void done() {
		done = true;
	}

	/** This method actually sends the beacons */
	private void broadcast(DatagramSocket socket) {
		try {
			DatagramPacket packet = new DatagramPacket(
					message.getBytes(), message.length(), broadcastAddress, BeaconParameters.BEACON_PORT);
			socket.send(packet);
		} catch (SocketException sex) {
			Log.e("SOUTILS", "An error occurred while initializing the datagram socket port and/or buffersize", sex);

		} catch (IOException ex) {
			Log.e("SOUTILS", "An error occurred while sending a packet over the socket", ex);
		}
	}
	
	@Override
	/**
	 * Starts the thread which in turn sends beacons while receiving and forwarding beacons using the default settings. 
	 * A beacon that is forwarded to the handler will contain a bundle with two strings. Use the "receivedMessage" key 
	 * to retrieve the actually received beacon message. Use the "senderIpAddress" key to retrieve the sender IP 
	 * address of the beacon itself.
	 * @see BeaconParameters
	 * @see #done()
	 * */
	public void run() {
		DatagramSocket socket = null;
		
		try {
			socket = new DatagramSocket(BeaconParameters.BEACON_PORT);
			socket.setBroadcast(true);
		} catch (Exception ex) {
			Log.e("SOUTILS", "An error occurred while initializing the datagram socket port", ex);

			return;
		}

		try {
			socket.setBroadcast(true);
			socket.setSoTimeout(500);
		} catch (SocketException e) {
			Log.e("SOUTILS", "An error occurred while initializing the datagram socket", e);
			
			socket.close();

			return;
		}

		while(!done) {			
			try {
				broadcast(socket);
				
				byte[] buffer = new byte[BeaconParameters.DATAGRAM_BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);

				Message message = Message.obtain(messageHandler);
				Bundle bundle = new Bundle();
				bundle.putString("receivedMessage", new String(packet.getData(), "UTF-8").trim());
				bundle.putString("senderIpAddress", packet.getAddress().getHostAddress());
				message.setData(bundle);

				messageHandler.sendMessage(message);
				
				Thread.sleep(BeaconParameters.MILLISECONDS_UNTIL_DATAGRAM_SOCKET_TIMEOUT);
				
			} catch (InterruptedIOException iie) {
				// The socket timed-out, re-enter the loop and listen again			
				continue;
			} catch(Exception ex) {
				socket.close();
				return;
			}
		}

		socket.close();
	}
}
