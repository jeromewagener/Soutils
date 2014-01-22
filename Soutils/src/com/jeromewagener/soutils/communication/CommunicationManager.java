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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import com.jeromewagener.soutils.Parameters;
import com.jeromewagener.soutils.messaging.MessageType;
import com.jeromewagener.soutils.messaging.SoutilsMessage;
import com.jeromewagener.soutils.messaging.SoutilsObservable;
import com.jeromewagener.soutils.messaging.SoutilsObserver;

/** A SoutilsObservable thread which wraps the server part of a socket communication. The CommunicationManager accepts
 * connections from client Communications. The CommunicationManager is able to simultaneously manage multiple 
 * communications from multiple devices. As soon as a message is received from one of these communications, all registered
 * SoutilsObservers are informed about the incoming message. Messages can be either sent using a particular client 
 * communication identified by the client IP address, or to multiple devices at once.
 * @see Communication
 * @see SoutilsObservable */
public class CommunicationManager extends SoutilsObservable {
	private ArrayList<Communication> communications = new ArrayList<Communication>();
	private ServerSocketChannel server = null;
	private SocketChannel socketChannel = null;
	private boolean done = false;
	
	/** Call this method to stop the thread from receiving and and forwarding future messages. Once the thread
	 * has been stopped, it cannot be started again. You must instead instantiate a new CommunicationManager.
	 * @see #run() */
	public void done() {
		done = true;
	}
	
	/** Sets up a new server communication thread to which clients can connect. The constructor initializes a new 
	 * CommunicationManager thread (server) which accepts new incoming Communications (client). 
	 * Received messages are forwarded to the given SoutilsObserver whereas addition SoutilsObservers can be registered
	 * using the corresponding register method. As for any Java thread this thread needs to be
	 * started using the {@link #run()} method. If the communication is terminated, the thread needs to be stopped.
	 * @param port the TCP port to which you want to connect to
	 * @param soutilsObserver the observer to which all received messages should be forwarded to
	 * @see Communication
	 * @see #run()
	 * @see SoutilsObservable
	 * @see #registerSoutilsObserver(SoutilsObserver) */
	public CommunicationManager(int port, SoutilsObserver soutilsObserver) {
		try {
			server = ServerSocketChannel.open();
			server.socket().bind(new InetSocketAddress(port));
			server.configureBlocking(false);
			this.registerSoutilsObserver(soutilsObserver);
		} catch (IOException ioException) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, ioException));
		}
	}

	/** Sends a message to all connected clients. I.e. to all communications that are currently managed.
	 * @param messageContent the message to be sent to all connected clients */
	public synchronized void sendMessageToAllConnectedPeers(String messageContent) {
		for(Communication communication : communications) {
			try {
				communication.sendMessage(messageContent);
			} catch (CommunicationException communicationException) {
				notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, communicationException));
				communications.remove(communication);
			}
		}
	}
	
	/** Sends a message to the specified client using a managed communication
	 * @param receiverAddress the recipient's IP address
	 * @param messageContent the message to be sent */
	public synchronized void sendMessage(String receiverAddress, String messageContent) {
		for(Communication communication : communications) {			
			if (communication.getClientAddress().equals(receiverAddress)) {
				try {
					communication.sendMessage(messageContent);
				} catch (CommunicationException communicationException) {
					notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, communicationException));
					communications.remove(communication);
				}
			}
		}
	}
	
	/** Starts listening for incoming messages from all connected clients. As soon as a message has been received, 
	 * all registered SoutilsObservers are informed about the newly received message.
	 * @see #done() */
	@Override
	public void run() {
		while (!done) {
			try {
				socketChannel = server.accept();

				if (socketChannel == null) {
					Thread.sleep(Parameters.COMMUNICATION_MILLISECONDS_UNTIL_NEXT_CONNECTION_ACCEPT);
				} else {
					Communication communication = new Communication(socketChannel);
					Thread clientServiceThread = new Thread(communication);
					communications.add(communication);
					
					for (SoutilsObserver soutilsObserver : getSoutilsObservers()) {
						communication.registerSoutilsObserver(soutilsObserver);
					}
					
					clientServiceThread.start();
				}
			} catch (IOException ioException) {
				notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, ioException));
			} catch (InterruptedException e) {
				break;
			}
		}		
		
		for (Communication communication : communications) {
			communication.done();
		}
				
		try {
			server.close();
		} catch (IOException ioException) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, ioException));
		}
	}

	/** Shuts down the communication with a specific client
	 * @param ipAddress the IP address of the client communication that should be terminated */
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
