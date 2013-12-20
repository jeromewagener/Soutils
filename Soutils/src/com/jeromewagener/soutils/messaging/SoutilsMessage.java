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

package com.jeromewagener.soutils.messaging;

/** A simple message representation */
public class SoutilsMessage {
	/** The sender address for internal messages */
	public static final String INTERNAL = "127.0.0.1";
	
	/** The message type */
	private final MessageType type;
	/** The IP address of the message sender */
	private String senderAddress = null;
	/** The actual content of the message */
	private String content = null;
	/** A throwable in case an error occurred */
	private Throwable throwable;
	
	/**
	 * Creates a message instance based on the supplied parameters
	 * @param type the type of the message
	 * @param senderAddress the IP address of the message sender
	 * @param content the actual content of the message
	 */
	public SoutilsMessage(MessageType type, String senderAddress, String content) {
		this.type = type;
		this.senderAddress = senderAddress;
		this.content = content;
	}
	
	/**
	 * Creates a message instance based on the supplied parameters
	 * @param type the type of the message
	 * @param senderAddress the IP address of the message sender
	 * @param content the actual content of the message
	 */
	public SoutilsMessage(MessageType type, String senderAddress, Throwable throwable) {
		this.type = type;
		this.senderAddress = senderAddress;
		this.content = throwable.getMessage();
		this.throwable = throwable;
	}
	
	public String getSenderAddress() {
		return senderAddress;
	}
	
	public String getContent() {
		return content;
	}

	public MessageType getMessageType() {
		return type;
	}

	public Throwable getThrowable() {
		return throwable;
	}
}
