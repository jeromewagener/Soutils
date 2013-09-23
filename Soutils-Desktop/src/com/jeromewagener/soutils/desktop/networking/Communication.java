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

package com.jeromewagener.soutils.desktop.networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.jeromewagener.soutils.Utilities;
import com.jeromewagener.soutils.networking.CommunicationParameters;

/** The treaded client of a wrapped socket communication */
public class Communication extends Thread {
	// TODO make configurable
	private static final String rootMessageTag = "<msg>";
	
	private SocketChannel socketChannel = null;
	private String clientAddress = null;
	private List<MessageReceptionObserver> messageReceptionObservers = new ArrayList<MessageReceptionObserver>();	
	private Queue<Message> receivedMessages = new LinkedList<Message>();
	private boolean done = false;

	/** Call this method to stop the thread and terminate the communication */
	public void done() {
		done = true;
	}

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
	 * Remove all message reception observers
	 */
	public void removeAllMessageReceptionObservers() {
		messageReceptionObservers.clear();
	}

	/**
	 * Creates a new client communication thread with a host device. As for any Java thread this thread needs to be
	 * started using the {@link #run()} method.
	 * @param ipAddress the IP address of the host
	 * @param port the TCP port to which you want to connect to
	 * @see #run()
	 */
	public Communication(String ipAddress, int port) {		
		try {
			SocketAddress address = new InetSocketAddress(ipAddress, port);
			this.socketChannel = SocketChannel.open(address);
			this.socketChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
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
		} catch (IOException e) {
			e.printStackTrace();
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
		ByteBuffer buffer = ByteBuffer.allocate(CommunicationParameters.BUFFER_SIZE_IN_BYTES);

		while(!done) {		
			try{
				// This loop clears the buffer
				for (int i=0; i<CommunicationParameters.BUFFER_SIZE_IN_BYTES; i++) {
					buffer.put(i, Byte.valueOf("0", 16));
				}

				// Rewind the buffer
				buffer.rewind();

				// Read (if possible)
				if(socketChannel.read(buffer) > 0) {		
					String bufferContent = new String(buffer.array(), "UTF-8").trim();

					if (!bufferContent.equals("")) {
						List<String> messages = Utilities.splitMultiMessageString(bufferContent, rootMessageTag);

						for (String message : messages) {
							receivedMessages.add(new Message(clientAddress, message));

							for (MessageReceptionObserver messageReceptionObserver : messageReceptionObservers) {
								messageReceptionObserver.reactToMessage(clientAddress, message);
							}
						}
					}			
				}
			} 
			catch (SocketException se) {
				se.printStackTrace();
				
				break;
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(CommunicationParameters.DURATION_BETWEEN_READ_ATTEMPTS_IN_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();

				break;
			}
		}

		try {
			socketChannel.socket().close();
			socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
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
	 * Get the oldest not processed message from the internal message queue. A message is only considered being 
	 * processed if polled using this method
	 * @return the head of the message queue (which is then removed). null if no message available. */
	public synchronized Message pollMessage() {
		return receivedMessages.poll();
	}

	/**
	 * Check whether the internal message queue has any unprocessed messages waiting
	 * @return true if there is a message waiting, false otherwise
	 * @see #pollMessage()
	 */
	public boolean hasMessageWaiting() {
		return !(receivedMessages.isEmpty());
	} 
	
	/**
	 * Get the IP address of the local device
	 * @return the IP address of the local device
	 */
	public synchronized String getClientAddress() {
		return clientAddress;
	}
}
