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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.jeromewagener.soutils.networking.CommunicationParameters;

/** The treaded server of a wrapped socket communication */
public class CommunicationManager extends Thread {
	private List<MessageReceptionObserver> messageReceptionObservers = new ArrayList<MessageReceptionObserver>();
	private ArrayList<Communication> communications = new ArrayList<Communication>();
	private ServerSocketChannel server = null;
	private SocketChannel socketChannel = null;
	private boolean done = false;
	
	/** Call this method to terminate the communications with all connected clients */
	public void done() {
		done = true;
	}
	
	/**
	 * Registers a message reception observer which is notified as soon as a message is received. The message can
	 * be fetched from the communications message queue or via the observer update.
	 * @param messageReceptionObserver the observer that should be registered
	 */
	public void registerMessageReceptionObserver(MessageReceptionObserver messageReceptionObserver) {
		messageReceptionObservers.add(messageReceptionObserver);
	}
	
	/**
	 * Remove a message reception observer
	 * @param the message reception observer to be removed
	 */
	public void removeMessageReceptionObserver(MessageReceptionObserver messageReceptionObserver) {
		messageReceptionObservers.remove(messageReceptionObserver);
	}
	
	/**
	 * Create a new server thread. As for any Java thread this thread needs to be started using the {@link #run()} method.
	 * @param port the port to be used by the server
	 */
	public CommunicationManager(int port) {
		try {
			server = ServerSocketChannel.open();
			server.socket().bind(new InetSocketAddress(port));
			server.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a message to all connected clients
	 * @param messageContent the message to be sent to all connected clients
	 */
	public synchronized void sendMessageToAllConnectedPeers(String messageContent) {
		for(Communication communication : communications) {
			try {
				communication.sendMessage(messageContent);
			} catch (CommunicationException ce) {
				ce.printStackTrace();
				
				communications.remove(communication);
			}
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
				try {
					communication.sendMessage(messageContent);
				} catch (CommunicationException ce) {
					ce.printStackTrace();
					
					communications.remove(communication);
				}
			}
		}
	}
	
	/**
	 * Get all queue messages received by the host. All those messages will be considered being processed after 
	 * calling this method.
	 * @return a queue of all messages received by the host
	 */
	public Queue<Message> getAllReceivedMessages() {
		Queue<Message> allReceivedMessagesQueue = new LinkedList<Message>();
		Message message = null;
		
		for(Communication communication : communications) {
			message = null;
			
			while ((message = communication.pollMessage()) != null) {
				allReceivedMessagesQueue.add(message);
			}
		}
		
		return allReceivedMessagesQueue;
	}
	
	/**
	 * Check whether the internal message queue has any unprocessed messages waiting
	 * @return true if there is a message waiting, false otherwise
	 * @see #getAllReceivedMessages()
	 */
	public boolean hasMessageWaiting() {
		for (Communication communication : communications) {
			if (communication.hasMessageWaiting()) {
				return true;
			}
		}
		
		return false;
	}
	
	/** 
	 * Starts receiving messages from all connected clients which are then buffered using an internal message queue. 
	 * If a message reception observers are registered, then these observers will be notified.
	 * @see #done()
	 */
	@Override
	public void run() {
		while (!done) {
			try {
				socketChannel = server.accept();

				if (socketChannel == null) {
					Thread.sleep(CommunicationParameters.MILLISECONDS_UNTIL_NEXT_CONNECTION_ACCEPT);
				} else {
					Communication communication = new Communication(socketChannel);
					Thread clientServiceThread = new Thread(communication);
					communications.add(communication);
					
					for (MessageReceptionObserver messageReceptionObserver : messageReceptionObservers) {
						communication.registerMessageReceptionObserver(messageReceptionObserver);
					}
					
					clientServiceThread.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				break;
			}
		}		
		
		for (Communication communication : communications) {
			communication.done();
		}
				
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cancels the communication with a specific client
	 * @param messageSender the IP address of the client communication that should be cancelled
	 */
	public void shutdownCommunication(String ipAddress) {
		Communication communicationToBeRemoved = null;
		
		for (Communication communication : communications) {
			if (communication.getClientAddress().equals(ipAddress)) {
				communication.done();
				communicationToBeRemoved = communication;
				break;
			}
		}
		
		communications.remove(communicationToBeRemoved);
	}
}
