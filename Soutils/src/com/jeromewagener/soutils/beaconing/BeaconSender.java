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

package com.jeromewagener.soutils.beaconing;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.jeromewagener.soutils.Parameters;
import com.jeromewagener.soutils.messaging.MessageType;
import com.jeromewagener.soutils.messaging.SoutilsMessage;
import com.jeromewagener.soutils.messaging.SoutilsObservable;
import com.jeromewagener.soutils.messaging.SoutilsObserver;

/** A SoutilsObservable thread which continuously sends UDP beacons. */
public class BeaconSender extends SoutilsObservable {
	/** The port used to broadcast beacon messages */
	private final int port;
	/** The message to be transferred by the beacon */
	private String message = null;
	/** The address used to broadcast the beacon */
	private InetAddress broadcastAddress;
	/** This variable must be set to true in order to stop the thread */
	private boolean done = false;
	
	/** Creates a new SoutilsObservable thread which continuously sends UDP beacons. As for any Java thread, 
	 * the {@link #run()} must be called to actually start the thread.
	 * @param port the port used to listen for beacon messages
	 * @param message the beacon message to be broadcasted
	 * @param broadcastAddress the IP broadcast address to be used
	 * @see SoutilsObserver
	 * @see #run() */
	public BeaconSender(String message, InetAddress broadcastAddress, int broadcastPort, SoutilsObserver soutilsObserver) {
		this.port = broadcastPort;
		this.message = message;
		this.broadcastAddress = broadcastAddress;
		this.registerSoutilsObserver(soutilsObserver);		
	}
	
	/** Replace the beacon message by another beacon message. */
	public void updateBeacon(String message) {
		this.message = message;
	}
	
	/** Call this method to stop the thread and to prevent it from sending any additional beacons. Once the thread 
	 * has been stopped, it cannot be started again. You must instead instantiate a new BeaconSenderAndReceiver instead.
	 * @see #run() */
	public void done() {
		done = true;
	}

	/** This method sends out a single beacon with the message specified within the constructor */
	private void broadcast() { 
		DatagramSocket socket = null;
		
		try {
			socket = new DatagramSocket(port);
			socket.setBroadcast(true);
			DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), broadcastAddress, port);
			socket.send(packet);
		} catch (Exception ex) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, ex));
		}

		socket.close();
	}

	/** Starts the BeaconSender thread which will continuously send out beacons with the message specified within the constructor.
	 * @see BeaconParameters 
	 * @see #done() */
	@Override
	public void run() {		
		while (done == false) {
			broadcast();

			try {
				Thread.sleep(Parameters.BEACON_MILLISECONDS_UNTIL_NEXT_BROADCAST);
			} catch (InterruptedException interruptedException) {
				notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, interruptedException));
			}
		}
	}
}
