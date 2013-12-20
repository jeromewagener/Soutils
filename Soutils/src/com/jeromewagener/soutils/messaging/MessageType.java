package com.jeromewagener.soutils.messaging;

public enum MessageType {
	/** A standard message retrieved from a communication (socket) */
	COMMUNICATION,
	/** A beacon message that has been broadcasted using UDP beacons */
	BEACON,
	/** An internal information message to notify observers about the completion of the file transfer */
	FILE_TRANSFER_COMPLETE,
	/** An internal information message that contains status information. E.g the current file transfer percentage */
	INFO,
	/** An internal error message */
	ERROR
}
