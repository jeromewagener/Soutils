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
import java.net.InetAddress;
import java.net.SocketException;

import com.jeromewagener.soutils.Parameters;
import com.jeromewagener.soutils.messaging.MessageType;
import com.jeromewagener.soutils.messaging.SoutilsMessage;
import com.jeromewagener.soutils.messaging.SoutilsObservable;
import com.jeromewagener.soutils.messaging.SoutilsObserver;

/** A thread which sends UDP beacons while receiving and buffering other devices's UDP beacons */
public class BeaconSenderAndReceiver extends SoutilsObservable  {
	/** The port used to send and receive beacon messages at the same time */
	private final int port;
	/** The message to be transferred by the beacon */
	private String message;
	/** This variable must be set to true in order to stop the thread */
	private boolean done = false;
	/** The broadcast address to be used for distributing the beacon */
	private InetAddress broadcastAddress;

	/**
	 * Creates a new thread which is able to sent and receive UDP beacons. As for any Java thread, 
	 * the {@link #run()} must be called to actually start the thread.
	 * @param port the port used to send and receive beacon messages at the same time
	 * @param message the message respectively beacon to be broadcasted
	 * @param broadcastAddress the IP broadcast address to be used
	 * @param soutilsObserver the observer to which will be notified in case of received beacons
	 * @see #run()
	 */
	public BeaconSenderAndReceiver(String beaconMessage, InetAddress broadcastAddress, int broadcastPort, SoutilsObserver soutilsObserver) {
		this.port = broadcastPort;
		this.broadcastAddress = broadcastAddress;
		this.message = beaconMessage;
		this.registerSoutilsObserver(soutilsObserver);
	}

	/**
	 * Call this method to stop the thread and to prevent it from sending any further beacons
	 */
	public void done() {
		done = true;
	}

	/** This method sends out a single beacon transferring the message provided via the constructor */
	private void broadcast(DatagramSocket socket) {
		try {
			DatagramPacket packet = new DatagramPacket(
					message.getBytes(), message.length(), broadcastAddress, port);
			socket.send(packet);
		} catch (Exception exception) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, exception));
		}
	}

	/** 
	 * Starts the thread which continuously sends out beacons using the default settings while buffering beacons
	 * received from other devices
	 * @see BeaconParameters 
	 * @see #done()
	 */
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

		while(!done) {
			try {
				broadcast(datagramSocket);				

				byte[] datagramBuffer = new byte[Parameters.BEACON_DATAGRAM_BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(datagramBuffer, datagramBuffer.length);
				datagramSocket.receive(packet);
				notifyAllObservers(new SoutilsMessage(
						MessageType.BEACON, packet.getAddress().getHostAddress(), new String(packet.getData(), "UTF-8").trim()));

				Thread.sleep(Parameters.BEACON_MILLISECONDS_UNTIL_NEXT_BROADCAST);
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
