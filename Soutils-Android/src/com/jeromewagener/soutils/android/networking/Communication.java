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

package com.jeromewagener.soutils.android.networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jeromewagener.soutils.Utilities;
import com.jeromewagener.soutils.networking.CommunicationParameters;

/** The treaded client of a wrapped socket communication */
public class Communication extends Thread {
	// TODO make customizable
	private final static String DEFAULT_MESSAGE_ROOT = "<msg>";
	
	private SocketChannel socketChannel = null;
	private Handler messageHandler = null;
	private String clientAddress = "";
	private boolean done = false;

	/** Call this method to stop the thread and terminate the communication */
	public void done() {
		done = true;
	}

	/**
	 * Creates a new client communication thread with a host device. As for any Java thread this thread needs to be
	 * started using the {@link #run()} method.
	 * @param ipAddress the IP address of the host
	 * @param port the TCP port to which you want to connect to
	 * @param messageHandler the handler to which received messages are forwarded. See {@link #run()} for more details
	 * @see #run()
	 */
	public Communication(String ipAddress, int port, Handler messageHandler) {
		try {
			SocketAddress address = new InetSocketAddress(ipAddress, port);
			this.socketChannel = SocketChannel.open(address);
			this.socketChannel.configureBlocking(false);
		} catch (IOException e) {
			Log.e("SOUTILS", "An error occurred while setting up a new communication", e);
		}

		this.clientAddress = this.socketChannel.socket().getInetAddress().getHostAddress();
		this.messageHandler = messageHandler;
	}
	
	/**
	 * Creates a new client communication thread with a host device. As for any Java thread this thread needs to be
	 * started using the {@link #run()} method.
	 * @param socketChannel the socketChannel to be used during the communication
	 * @param messageHandler the handler to which received messages are forwarded. See {@link #run()} for more details
	 * @see #run()
	 */
	public Communication(SocketChannel socketChannel, Handler messageHandler) {
		this.socketChannel = socketChannel;

		try {
			this.socketChannel.configureBlocking(false);
		} catch (IOException e) {
			Log.e("SOUTILS", "An error occurred while creating a new client communication thread with a host device", e);
		}

		this.clientAddress = this.socketChannel.socket().getInetAddress().getHostAddress();
		this.messageHandler = messageHandler;
	}

	
	/** 
	 * Starts receiving messages which are then forwarded using the specified message handler. A message 
	 * forwarded to the handler will contain a bundle with two strings. Use the "receivedMessage" key 
	 * to retrieve the actually received message. Use the "senderIpAddress" key to retrieve the sender IP 
	 * address of the message itself.
	 * @see #done()
	 */
	@Override
	public void run() {
		ByteBuffer buffer = ByteBuffer.allocate(CommunicationParameters.BUFFER_SIZE_IN_BYTES);

		while (!done) {		
			try {
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
						List<String> receivedMessages = Utilities.splitMultiMessageString(bufferContent, DEFAULT_MESSAGE_ROOT);

						for (String receivedMessage : receivedMessages) {
							Message message = Message.obtain(messageHandler);

							Bundle bundle = new Bundle();
							bundle.putString("receivedMessage", receivedMessage);
							bundle.putString("senderIpAddress", clientAddress);

							message.setData(bundle);

							messageHandler.sendMessage(message);
						}
					}
				}			
			} 
			catch (IOException ioe) {
				Log.e("SOUTILS", "An error occurred while reading from the socket channel buffer", ioe);

				break;
			}

			try {
				Thread.sleep(CommunicationParameters.DURATION_BETWEEN_READ_ATTEMPTS_IN_MS);
			} catch (InterruptedException e) {
				Log.e("SOUTILS", "An error occured while sleeping", e);
				break;
			}
		}

		try {
			socketChannel.socket().close();
			socketChannel.close();
		} catch (IOException e) {
			Log.e("SOUTILS", "An error occurred while closing the socket channel", e);
		}
	}

	/**
	 * Sends a message to the host
	 * @param messageContent the message to be sent
	 */
	public synchronized void sendMessage(String messageContent) {
		Charset charset = Charset.forName("UTF-8");
		CharsetEncoder encoder = charset.newEncoder();

		try {
			ByteBuffer buffer = encoder.encode(CharBuffer.wrap(messageContent));
			while (buffer.remaining() > 0) {
				socketChannel.write(buffer);
			}
		} catch (IOException e) {
			Log.e("SOUTILS", "An error occurred while encoding the message and/or writing it to the buffer", e);
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
