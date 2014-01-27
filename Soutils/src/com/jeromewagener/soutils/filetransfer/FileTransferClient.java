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

package com.jeromewagener.soutils.filetransfer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;

import com.jeromewagener.soutils.Parameters;
import com.jeromewagener.soutils.messaging.MessageType;
import com.jeromewagener.soutils.messaging.SoutilsMessage;
import com.jeromewagener.soutils.messaging.SoutilsObservable;
import com.jeromewagener.soutils.messaging.SoutilsObserver;

/** A SoutilsObservable thread which wraps a file transfer client that is able to download an offered file 
 * from a specified file transfer server. This transfer is executed using a TCP connection.
 * All registered SoutilsObservables will be informed as soon as as the transfer has been completed.
 * @see FileTransferServer
 * @see SoutilsObservable */
public class FileTransferClient extends SoutilsObservable {
	private static final int UNKNOWN_NUMBER_OF_BYTES_TO_BE_TRANSFERRED = -1;
	
	private final String storageLocationAsAbsolutPath ;
	private final String serverAddress;
	private final int fileTransferPort;
	
	private long totalNumberOfBytesToBeTransferred = UNKNOWN_NUMBER_OF_BYTES_TO_BE_TRANSFERRED;
	private long numberOfBytesAlreadyTransferred = 0;
	private boolean done = false;
	
	/** Sets up a file transfer client which is able to download a file offered by a file transfer server
	 * As for any Java thread, the {@link #start()} method must be called to actually start the thread.
	 * @param storageLocationAsAbsolutPath the storage location for the downloaded the file
	 * @param serverIpAddress the IP address of the file transfer server
	 * @param fileTransferPort the port used by the server to offer the file transfer
	 * @param soutilsObserver the observer that is informed in case of errors of in case the download has been completed
	 * @see #start() */
	public FileTransferClient(String storageLocationAsAbsolutPath, String serverIpAddress, int fileTransferPort, SoutilsObserver soutilsObserver) {
		this.storageLocationAsAbsolutPath = storageLocationAsAbsolutPath;
		this.serverAddress = serverIpAddress;
		this.fileTransferPort = fileTransferPort;
		this.registerSoutilsObserver(soutilsObserver);
	}

	/** Sets up a file transfer client which is able to download a file offered by a file transfer server
	 * As for any Java thread, the {@link #start()} method must be called to actually start the thread.
	 * @param storageLocationAsAbsolutPath the storage location for the downloaded the file
	 * @param serverIpAddress the IP address of the file transfer server
	 * @param fileTransferPort the port used by the server to offer the file transfer
	 * @param soutilsObserver the observer that is informed in case of errors of in case the download has been completed
	 * @param totalNumberOfBytesToBeTransferred in order to be able to calculated the transfer percentage, the client must know
	 * about the total number of bytes that need to be transferred. Please note that you will usually transfer the size
	 * with a previous communication message that will initiate the transfer.
	 * @see #start()
	 * @see #getFileTransferPercentage() */
	public FileTransferClient(String storageLocationAsAbsolutPath, String serverIpAddress, int fileTransferPort, SoutilsObserver soutilsObserver, long totalNumberOfBytesToBeTransferred) {
		this.storageLocationAsAbsolutPath = storageLocationAsAbsolutPath;
		this.serverAddress = serverIpAddress;
		this.fileTransferPort = fileTransferPort;
		this.registerSoutilsObserver(soutilsObserver);
		
		this.totalNumberOfBytesToBeTransferred = totalNumberOfBytesToBeTransferred;
	}
	
	/** Starts downloading the offered file to the specified location using the specified port. 
	 * The file transfer server must already be running and offering the file before calling this method */
	public void run() {
		try {
			Socket socket = new Socket(serverAddress, fileTransferPort);

			FileOutputStream fileOutputStream = new FileOutputStream(storageLocationAsAbsolutPath);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			InputStream socketInputStream = socket.getInputStream();

			int counter = 0;
			byte[] buffer = new byte[Parameters.FILE_TRANSFER_BUFFER_SIZE_IN_BYTES];
			while ((counter = socketInputStream.read(buffer)) > 0 && !done) {
				bufferedOutputStream.write(buffer, 0, counter);
				bufferedOutputStream.flush();
				
				numberOfBytesAlreadyTransferred += counter;
			}

			bufferedOutputStream.close();
			fileOutputStream.close();
			socket.close();
		} catch (Exception exception) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, exception));
		}
		
		done = true;
		notifyAllObservers(new SoutilsMessage(MessageType.FILE_TRANSFER_COMPLETE, SoutilsMessage.INTERNAL, "File transfer successful!"));
	}
	
	/** Call this method to stop the thread from downloading the offered file. Once the thread
	 * has been stopped, it cannot be started again. You must instead instantiate a new FileTransferClient.
	 * @see #start() */
	public void done() {
		this.done = true;
	}
	
	/** Check whether the download has finished or not */
	public boolean isDone() {
		return done;
	}
	
	/** Returns the transfer percentage as an integer value between 0 and 100 
	 * The percentage is only correct if the file transfer client has been initialized with the total number of bytes to be transferred 
	 * Otherwise, the method will return 0 if the transfer is still ongoing or 100 if the transfer has finished */
	public int getFileTransferPercentage() {
		if (done || totalNumberOfBytesToBeTransferred == UNKNOWN_NUMBER_OF_BYTES_TO_BE_TRANSFERRED) {
			return done ? 100 : 0;
		}
		
		Double transferRatio = ((double) numberOfBytesAlreadyTransferred / (double) totalNumberOfBytesToBeTransferred) * 100;
		return transferRatio.intValue();
	}
}
