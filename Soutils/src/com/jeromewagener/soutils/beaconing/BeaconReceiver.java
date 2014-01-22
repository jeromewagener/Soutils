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

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.jeromewagener.soutils.Parameters;
import com.jeromewagener.soutils.messaging.MessageType;
import com.jeromewagener.soutils.messaging.SoutilsMessage;
import com.jeromewagener.soutils.messaging.SoutilsObservable;
import com.jeromewagener.soutils.messaging.SoutilsObserver;

/** A SoutilsObservable thread which listens for UDP beacons which are then 
 * forwarded to to all registered SoutilsObservers */
public class BeaconReceiver extends SoutilsObservable {
	/** The port used to listen for UDP beacon messages. Set via the constructor. */
	private final int port;
	/** This variable must be set to true in order to stop the thread */
	private boolean done = false;
	
	/** Creates a beacon receiver thread (SoutilsObservable) which notifies the registered 
	 * beacon reception observer (SoutilsObserver) about received beacons. Please note that 
	 * additional observers can be added using the register. As for any Java thread
	 * the {@link #run()} must be called to start the thread.
	 * @see SoutilsObserver
	 * @see #run()*/
	public BeaconReceiver(int port, SoutilsObserver soutilsObserver) {
		this.port = port;
		this.registerSoutilsObserver(soutilsObserver);
	}
	
	/** Call this method to stop the thread from receiving and and forwarding future beacons. Once the thread
	 * has been stopped, it cannot be started again. You must instead instantiate a new BeaconReceiver.
	 * @see #run() */
	public void done() {
		done = true;
	}
	
	/** Start listening for UDP beacons which will then be forwarded to all registered observers.
	 * @see #done() */
	@Override
	public void run() {
		DatagramSocket datagramSocket = null;

		try {
			datagramSocket = new DatagramSocket(port);
			datagramSocket.setBroadcast(true);
			datagramSocket.setSoTimeout(Parameters.BEACON_MILLISECONDS_UNTIL_DATAGRAM_SOCKET_TIMEOUT);
		} catch (SocketException socketException) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, socketException));
			
			datagramSocket.close();
			return;
		}

		while (!done) {
			try {
				byte[] datagramBuffer = new byte[Parameters.BEACON_DATAGRAM_BUFFER_SIZE];
				DatagramPacket datagramPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length);
				datagramSocket.receive(datagramPacket);
				
				notifyAllObservers(new SoutilsMessage(
						MessageType.BEACON, 
						datagramPacket.getAddress().getHostAddress(), 
						new String(datagramPacket.getData(), "UTF-8").trim()));
				
			} catch (InterruptedIOException iie) {
				// The socket timed-out, re-enter the loop and listen again			
				continue;
			} catch(Exception exception) {
				notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, exception));
				
				datagramSocket.close();
				return;
			}
		}

		datagramSocket.close();
	}
}
