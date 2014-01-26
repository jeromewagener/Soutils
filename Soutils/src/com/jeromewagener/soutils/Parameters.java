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

package com.jeromewagener.soutils;

/** Various Soutils parameters (E.g. default buffer sizes...) */
public final class Parameters {
	/** ------------------------------------------------------------------ 
	 * Communication parameters to be used by all framework communications 
	 * ------------------------------------------------------------------  */

	/** The default buffer size to be used in bytes */
	public static final int COMMUNICATION_BUFFER_SIZE_IN_BYTES = 4096;
	
	/** The default time in milliseconds between trying to read newly received messages from the buffer */
	public static final int COMMUNICATION_DURATION_BETWEEN_READ_ATTEMPTS_IN_MS = 500;
	
	/** The default time between TCP connection accepts for communication servers */
	public static final int COMMUNICATION_MILLISECONDS_UNTIL_NEXT_CONNECTION_ACCEPT = 500;
	
	/** A splitter string which allows to split received multi message strings into SoutilsMessages.
	 * E.g. Using the default message splitter ({@code<soutils>}), the following string: 
	 * <pre>{@code<soutils>my message 1</soutils><soutils>my message 2</soutils>}</pre> 
	 * would be split into two SoutilsMessages having as content
	 * <pre>{@code<soutils>my message 1</soutils>}</pre> 
	 * and 
	 * <pre>{@code<soutils>my message 2</soutils>}</pre> */
	public static String messageSplitter = "<soutils>";
	
	/** ------------------------------------------------------------------ 
	 * Default parameter constants for beaconing to be used by all framework beacon implementations 
	 * ------------------------------------------------------------------  */

	/** The time in milliseconds until the datagram socket times out. After it times out, a new cycle is started */
	public final static int BEACON_MILLISECONDS_UNTIL_DATAGRAM_SOCKET_TIMEOUT = 500;
	
	/** The time in milliseconds until the beacon is broadcasted again */
	public final static int BEACON_MILLISECONDS_UNTIL_NEXT_BROADCAST = 500;
	
	/** The size of the buffer used for the datagram socket */
	public final static int BEACON_DATAGRAM_BUFFER_SIZE = 4096;
	
	/** ------------------------------------------------------------------ 
	 * File transfer parameters to be used by all framework file transfer implementations
	 * ------------------------------------------------------------------  */
	
	/** The default buffer size in bytes for file transfers */
	public static final int FILE_TRANSFER_BUFFER_SIZE_IN_BYTES = 8192;
}
