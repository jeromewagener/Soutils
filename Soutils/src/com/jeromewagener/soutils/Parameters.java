package com.jeromewagener.soutils;

public class Parameters {
	/** ------------------------------------------------------------------ 
	 * Communication parameters to be used by all framework communications 
	 * ------------------------------------------------------------------  */

	/** The default buffer size to be used in bytes */
	public static final int COMMUNICATION_BUFFER_SIZE_IN_BYTES = 4096;
	
	/** The default time in milliseconds between trying to read newly received messages from the buffer */
	public static final int COMMUNICATION_DURATION_BETWEEN_READ_ATTEMPTS_IN_MS = 500;
	
	/** The default time between TCP connection accepts for communication servers */
	public static final int COMMUNICATION_MILLISECONDS_UNTIL_NEXT_CONNECTION_ACCEPT = 500;
	
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
