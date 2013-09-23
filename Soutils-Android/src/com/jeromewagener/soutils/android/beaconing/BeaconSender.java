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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.util.Log;

import com.jeromewagener.soutils.beaconing.BeaconParameters;

/** A thread which sends UDP beacons */
public class BeaconSender extends Thread {
	/** The message to be transferred by the beacon */
	private String message = null;
	/** The address used to broadcast the beacon */
	private InetAddress broadcastAddress;
	/** This variable must be set to true in order to stop the thread */
	private boolean done = false;

	/**
	 * Creates a new thread which is able to sent UDP beacons. As for any Java thread, 
	 * the {@link #run()} must be called to actually start the thread.
	 * @param message the message respectively beacon to be broadcasted
	 * @param broadcastAddress the IP broadcast address to be used
	 * @see #run()
	 */
	public BeaconSender(String message, InetAddress broadcastAddress) {
		this.message = message;
		this.broadcastAddress = broadcastAddress;
	}

	/** 
	 * Replace the beacon message by another updated beacon message.
	 * message an updated message respectively beacon
	 */
	public void updateBeacon(String message) {
		this.message = message;
	}

	/** 
	 * Call this method to stop the thread and to prevent it from sending any further beacons
	 */
	public void done() {
		done = true;
	}

	/** 
	 * This method actually sends the beacon
	 */
	private void broadcast() { 
		DatagramSocket socket = null;

		try {
			socket = new DatagramSocket(BeaconParameters.BEACON_PORT);
			socket.setBroadcast(true);
			DatagramPacket packet = new DatagramPacket(
					message.getBytes(), message.length(), broadcastAddress, BeaconParameters.BEACON_PORT);
			socket.send(packet);
		} catch (SocketException sex) {
			Log.e("SOUTILS", "An error occurred while initializing the datagram socket port and/or buffersize", sex);

		} catch (IOException ex) {
			Log.e("SOUTILS", "An error occurred while sending a packet over the socket", ex);
		}

		socket.close();
	}

	/** 
	 * Starts the thread which continuously sends out beacons using the default settings 
	 * @see BeaconParameters 
	 * @see #done()
	 */
	@Override
	public void run() {
		while (done == false) {
			broadcast();

			try {
				Thread.sleep(BeaconParameters.MILLISECONDS_UNTIL_NEXT_BROADCAST);
			} catch (InterruptedException ex) {
				Log.e("SOUTILS", "An error occurred while waiting to broadcast another time", ex);
			}
		}
	}
}
