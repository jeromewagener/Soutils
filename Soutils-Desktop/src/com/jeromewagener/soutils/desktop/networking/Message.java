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

/** A simple message representation */
public class Message {
	/** The IP address of the message sender */
	private String senderIpAddress = null;
	/** The actual content of the message */
	private String content = null;
	
	/**
	 * Creates a message instance based on the supplied parameters
	 * @param senderIpAddress the IP address of the message sender
	 * @param content the actual content of the message
	 */
	public Message(String senderIpAddress, String content) {
		this.senderIpAddress = senderIpAddress;
		this.content = content;
	}
	
	/**
	 * Get the IP address of the message sender
	 * @return the IP address of the message sender
	 */
	public String getSenderIpAddress() {
		return senderIpAddress;
	}
	
	/**
	 * Set the IP address of the message sender
	 * @param senderIpAddress the IP address of the message sender
	 */
	public void setSenderIpAddress(String senderIpAddress) {
		this.senderIpAddress = senderIpAddress;
	}
	
	/**
	 * Get the actual content of the message
	 * @return the actual content of the message
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * Set the actual content of the message
	 * @param content the actual content of the message
	 */
	public void setContent(String content) {
		this.content = content;
	}
}
