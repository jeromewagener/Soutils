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

package com.jeromewagener.soutils.desktop.beaconing;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.jeromewagener.soutils.beaconing.BeaconParameters;
import com.jeromewagener.soutils.desktop.networking.MessageReceptionObserver;

/** A thread which receives and buffers UDP beacons */
public class BeaconReceiver extends Thread {
	/** This variable must be set to true in order to stop the thread */
	private boolean done = false;
	
	private List<MessageReceptionObserver> messageReceptionObservers = new ArrayList<MessageReceptionObserver>();
	
	/**
	 * Registers a message reception observer which is notified as soon as a message is received. The message can
	 * be fetched from the communications message queue or via the observer update.
	 * @param messageReceptionObserver the observer that should be registered
	 */
	public void registerMessageReceptionObserver(MessageReceptionObserver messageReceptionObserver) {
		System.out.println("Registered Message Reception Observer");
		messageReceptionObservers.add(messageReceptionObserver);
	}

	/**
	 * Remove a message reception observer
	 * @param the message reception observer to be removed
	 */
	public void removeMessageReceptionObserver(MessageReceptionObserver messageReceptionObserver) {
		System.out.println("Removed Message Reception Observer");
		messageReceptionObservers.remove(messageReceptionObserver);
	}
	
	/**
	 * Call this method to stop this thread from receiving and buffering beacons
	 */
	public void done() {
		done = true;
	}
	
	/**
	 * Starts the thread which in turn receives and buffers beacons using the default settings. A beacon that is 
	 * buffered in an internal queue until polled.
	 * @see BeaconParameters
	 * @see #done()
	 * @see #pollBeacon()
	 * */
	@Override
	public void run() {
		DatagramSocket datagramSocket = null;

		try {
			datagramSocket = new DatagramSocket(BeaconParameters.BEACON_PORT);
			datagramSocket.setBroadcast(true);
			datagramSocket.setSoTimeout(BeaconParameters.MILLISECONDS_UNTIL_DATAGRAM_SOCKET_TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
			datagramSocket.close();
			return;
		}

		while(!done) {
			try {
				byte[] datagramBuffer = new byte[BeaconParameters.DATAGRAM_BUFFER_SIZE];
				DatagramPacket datagramPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length);
				datagramSocket.receive(datagramPacket);

				for (MessageReceptionObserver messageReceptionObserver : messageReceptionObservers) {
					messageReceptionObserver.reactToMessage(
							datagramPacket.getAddress().getHostAddress().toString(), 
							new String(datagramPacket.getData(), "UTF-8").trim());
				}
				
			} catch (InterruptedIOException iie) {
				// The socket timed-out, re-enter the loop and listen again			
				continue;
			} catch(Exception ex) {
				ex.printStackTrace();
				datagramSocket.close();
				return;
			}
		}

		datagramSocket.close();
	}
}
