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

/** The treaded client of a wrapped socket communication */
public class Communication extends SoutilsObservable {
	// TODO make configurable
	private static final String rootMessageTag = "<msg>";
	
	private SocketChannel socketChannel = null;
	private String clientAddress = null;
	private boolean done = false;

	/** Call this method to stop the thread and terminate the communication */
	public void done() {
		done = true;
	}

	/**
	 * Creates a new client communication thread with a host device. As for any Java thread this thread needs to be
	 * started using the {@link #run()} method.
	 * Returns a new Communication (client) thread which is able to connect with a CommunicationManager (server) thread.
	 * As for any Java thread, this thread needs to be first started. If the communication is terminated, the thread
	 * needs to be stopped.
	 * @param ipAddress the IP address of the host
	 * @param port the TCP port to which you want to connect to
	 * @param soutilsObserver the observer to which received messages should be forwarded to
	 * @see #run()
	 */
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

	/**
	 * Creates a new client communication thread with a host device. As for any Java thread this thread needs to be
	 * started using the {@link #run()} method.
	 * @param socketChannel the socketChannel to be used for the communication
	 * @see #run()
	 */
	public Communication(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;

		try {
			this.socketChannel.configureBlocking(false);
		} catch (IOException ioException) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, ioException));
		}

		this.clientAddress = this.socketChannel.socket().getInetAddress().getHostAddress();
	}

	/** 
	 * Starts receiving messages which are then buffered using the message queue. If there are registered observers, then
	 * the message is forwarded to these observers.
	 * @see #done()
	 */
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
						List<String> messages = Soutilities.splitMultiMessageString(bufferContent, rootMessageTag);

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

	/**
	 * Sends a message to the host
	 * @param messageContent the message to be sent
	 * @throws CommunicationException
	 */
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
	
	/**
	 * Get the IP address of the local device
	 * @return the IP address of the local device
	 */
	public synchronized String getClientAddress() {
		return clientAddress;
	}
}
