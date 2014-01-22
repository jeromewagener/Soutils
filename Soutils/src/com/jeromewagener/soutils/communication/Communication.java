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

package com.jeromewagener.soutils.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;

import com.jeromewagener.soutils.Parameters;
import com.jeromewagener.soutils.messaging.MessageType;
import com.jeromewagener.soutils.messaging.SoutilsMessage;
import com.jeromewagener.soutils.messaging.SoutilsObservable;
import com.jeromewagener.soutils.messaging.SoutilsObserver;
import com.jeromewagener.soutils.utilities.Soutilities;

/** A SoutilsObservable thread which wraps the client part of a socket communication. Such a client communication is 
 * able to connect and exchange messages with a server which is represented by a CommunicationManager. If a message
 * is received from the server, all registered SoutilsObservers are informed about the newly received message.
 * @see CommunicationManager
 * @see SoutilsObservable */
public class Communication extends SoutilsObservable {	
	private SocketChannel socketChannel = null;
	private String clientAddress = null;
	private boolean done = false;

	/** Call this method to stop the thread from receiving and and forwarding future messages. Once the thread
	 * has been stopped, it cannot be started again. You must instead instantiate a new Communication.
	 * @see #run() */
	public void done() {
		done = true;
	}

	/** Sets up a new client communication thread with a host device. The constructor initializes a new Communication (client) 
	 * thread which is able to connect with a CommunicationManager (server) thread which must be running. 
	 * Received messages are forwarded to the given SoutilsObserver whereas addition SoutilsObservers can be registered
	 * using the corresponding register method. As for any Java thread this thread needs to be
	 * started using the {@link #run()} method. If the communication is terminated, the thread needs to be stopped.
	 * @param ipAddress the IP address of the host (the device that runs the CommunicationManager)
	 * @param port the TCP port over which you want to exchange messages
	 * @param soutilsObserver the observer to which all received messages should be forwarded to
	 * @see CommunicationManager
	 * @see #run()
	 * @see SoutilsObservable
	 * @see #registerSoutilsObserver(SoutilsObserver) */
	public Communication(String ipAddress, int port, SoutilsObserver soutilsObserver) {		
		try {
			SocketAddress address = new InetSocketAddress(ipAddress, port);
			this.socketChannel = SocketChannel.open(address);
			this.socketChannel.configureBlocking(false);
			this.registerSoutilsObserver(soutilsObserver);
		} catch (IOException ioException) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, ioException));
		}

		this.clientAddress = this.socketChannel.socket().getInetAddress().getHostAddress();
	}

	/** Creates a new client communication thread with a host device. As for any Java thread this thread needs to be
	 * started using the {@link #run()} method. Once the thread has been stopped, it cannot be started again. 
	 * You must instead instantiate a new Communication.
	 * @param socketChannel the socketChannel to be used for the communication
	 * @see #run() */
	public Communication(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;

		try {
			this.socketChannel.configureBlocking(false);
		} catch (IOException ioException) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, ioException));
		}

		this.clientAddress = this.socketChannel.socket().getInetAddress().getHostAddress();
	}

	/** Start listening for incoming messages. As soon as a message has been received, 
	 * all registered SoutilsObservers are informed about the newly received message.
	 * @see #done() */
	@Override
	public void run() {		
		ByteBuffer buffer = ByteBuffer.allocate(Parameters.COMMUNICATION_BUFFER_SIZE_IN_BYTES);

		while(!done) {		
			try{
				// This loop clears the buffer
				for (int i=0; i<Parameters.COMMUNICATION_BUFFER_SIZE_IN_BYTES; i++) {
					buffer.put(i, Byte.valueOf("0", 16));
				}

				// Rewind the buffer
				buffer.rewind();

				// Read (if possible)
				if(socketChannel.read(buffer) > 0) {		
					String bufferContent = new String(buffer.array(), "UTF-8").trim();

					if (!bufferContent.equals("")) {
						List<String> messages = Soutilities.splitMultiMessageString(bufferContent, Parameters.messageSplitter);

						for (String message : messages) {
							notifyAllObservers(new SoutilsMessage(MessageType.COMMUNICATION, clientAddress, message));
						}
					}			
				}
			} 
			catch (SocketException socketException) {
				notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, socketException));
				
				break;
			}
			catch (Exception exception) {
				notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, exception));
			}

			try {
				Thread.sleep(Parameters.COMMUNICATION_DURATION_BETWEEN_READ_ATTEMPTS_IN_MS);
			} catch (InterruptedException interruptedException) {
				notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, interruptedException));

				break;
			}
		}

		try {
			socketChannel.socket().close();
			socketChannel.close();
		} catch (IOException ioException) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, ioException));
		}
	}

	/** Sends a message to the host specified within the constructor
	 * @param messageContent the message to be sent
	 * @throws CommunicationException */
	public synchronized void sendMessage(String messageContent) throws CommunicationException {
		if (socketChannel.isConnected()) {
			Charset charset = Charset.forName("UTF-8");
			CharsetEncoder encoder = charset.newEncoder();

			try {
				ByteBuffer buffer = encoder.encode(CharBuffer.wrap(messageContent));
				while (buffer.remaining()>0) {
					socketChannel.write(buffer);
				}
			} catch (Exception e) {
				throw new CommunicationException("Communication Error");
			}
		} else {
			throw new CommunicationException("Communication not connected!");
		}
	}
	
	/** Get the IP address of the local device
	 * @return the IP address of the local device */
	public synchronized String getClientAddress() {
		return clientAddress;
	}
}
