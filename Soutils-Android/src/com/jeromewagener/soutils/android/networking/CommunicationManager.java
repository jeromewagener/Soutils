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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;

import com.jeromewagener.soutils.networking.CommunicationParameters;

/** The treaded server of a wrapped socket communication */
public class CommunicationManager extends Thread {
	private ArrayList<Communication> communications = new ArrayList<Communication>();
	private ServerSocketChannel serverSocketChannel = null;
	private SocketChannel socketChannel = null;
	private Handler messageHandler = null;
	private boolean done = false;
	
	/** Call this method to terminate the communications with all connected clients */
	public void done() {
		done = true;
	}
	
	/**
	 * Create a new server thread. As for any Java thread this thread needs to be started using the {@link #run()} method.
	 * @param port the port to be used by the server
	 * @param messageHandler the handler to which received messages are forwarded. See {@link #run()} for more details
	 */
	public CommunicationManager(int port, Handler messageHandler) {
		try {
			this.messageHandler = messageHandler;
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(port));
			serverSocketChannel.configureBlocking(false);
		} catch (IOException e) {
			Log.e("SOUTILS", "An error occurred while initializing the communication manager", e);
		}
	}

	/**
	 * Sends a message to all connected clients
	 * @param messageContent the message to be sent to all connected clients
	 */
	public synchronized void sendMessageToAllConnectedPeers(String messageContent) {
		for(Communication communication : communications) {
			communication.sendMessage(messageContent);
		}
	}
	
	/**
	 * Sends a message to the specified client
	 * @param receiverAddress the recipient's IP address
	 * @param messageContent the message to be sent
	 */
	public synchronized void sendMessage(String receiverAddress, String messageContent) {
		for(Communication communication : communications) {			
			if (communication.getClientAddress().equals(receiverAddress)) {
				communication.sendMessage(messageContent);
			}
		}
	}
	
	/** 
	 * Starts receiving messages from all connected clients which are then forwarded using the specified message handler. 
	 * A message forwarded to the handler will contain a bundle with two strings. Use the "receivedMessage" key 
	 * to retrieve the actually received message. Use the "senderIpAddress" key to retrieve the sender IP 
	 * address of the message itself.
	 * @see #done()
	 */
	@Override
	public void run() {
		while (!done) {
			try {
				socketChannel = serverSocketChannel.accept();

				if (socketChannel == null) {
					Thread.sleep(CommunicationParameters.MILLISECONDS_UNTIL_NEXT_CONNECTION_ACCEPT);
				} else {
					Communication communication = new Communication(socketChannel, messageHandler);
					Thread clientServiceThread = new Thread(communication);
					communications.add(communication);
					clientServiceThread.start();
				}
			} catch (IOException e) {
				Log.e("SOUTILS", "An error occurred while waiting for / handling new communications", e);
			} catch (InterruptedException e) {
				break;
			}
		}		
		
		for (Communication communication : communications) {
			communication.done();
		}
				
		try {
			serverSocketChannel.close();
		} catch (IOException e) {
			Log.e("SOUTILS", "An error occurred while closing the server socket channel", e);
		}
	}

	/**
	 * Cancels the communication with a specific client
	 * @param messageSender the IP address of the client communication that should be cancelled
	 */
	public void shutdownCommunication(String messageSender) {
		Communication communicationToBeRemoved = null;
		for (Communication communication : communications) {
			if (communication.getClientAddress().equals(messageSender)) {
				communication.done();
				communicationToBeRemoved = communication;
				break;
			}
		}
		
		communications.remove(communicationToBeRemoved);
	}
}
