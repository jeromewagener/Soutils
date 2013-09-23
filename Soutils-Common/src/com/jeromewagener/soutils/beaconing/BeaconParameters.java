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

package com.jeromewagener.soutils.beaconing;

/** Default parameter constants for beaconing to be used by all framework beacon implementations */
public class BeaconParameters {
	/** The time in milliseconds until the datagram socket times out. After it times out, a new cycle is started */
	public final static int MILLISECONDS_UNTIL_DATAGRAM_SOCKET_TIMEOUT = 500;
	
	/** The time in milliseconds until the beacon is broadcasted again */
	public final static int MILLISECONDS_UNTIL_NEXT_BROADCAST = 500;
	
	/** The size of the buffer used for the datagram socket */
	public final static int DATAGRAM_BUFFER_SIZE = 4096;
	
	/** The port over which beacons are sent */
	public final static int BEACON_PORT = 4242;
}
