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

/** An easy way to download a file from a {@link FileTransferServer} instance via a TCP connection using a single Thread */
public class FileTransferClient extends SoutilsObservable {
	private static final int UNKNOWN_NUMBER_OF_BYTES_TO_BE_TRANSFERRED = -1;
	
	private final String storageLocationAsAbsolutPath ;
	private final String serverAddress;
	private final int fileTransferPort;
	
	private long totalNumberOfBytesToBeTransferred = UNKNOWN_NUMBER_OF_BYTES_TO_BE_TRANSFERRED;
	private long numberOfBytesAlreadyTransferred = 0;
	private boolean done = false;
	
	/**
	 * Create a file transfer client able to download a file offered by a file transfer server
	 * @param storageLocationAsAbsolutPath the download location of the downloaded file
	 * @param serverIpAddress the ip address of the file transfer server
	 * @param fileTransferPort the port used by the server to offer the file transfer
	 */
	public FileTransferClient(String storageLocationAsAbsolutPath, String serverIpAddress, int fileTransferPort, SoutilsObserver soutilsObserver) {
		this.storageLocationAsAbsolutPath = storageLocationAsAbsolutPath;
		this.serverAddress = serverIpAddress;
		this.fileTransferPort = fileTransferPort;
		this.registerSoutilsObserver(soutilsObserver);
	}

	/**
	 * Create a file transfer client able to download a file offered by a file transfer server
	 * @param storageLocationAsAbsolutPath the download location of the downloaded file
	 * @param serverIpAddress the ip address of the file transfer server
	 * @param fileTransferPort the port used by the server to offer the file transfer
	 */
	public FileTransferClient(String storageLocationAsAbsolutPath, String serverIpAddress, int fileTransferPort, SoutilsObserver soutilsObserver, long totalNumberOfBytesToBeTransferred) {
		this.storageLocationAsAbsolutPath = storageLocationAsAbsolutPath;
		this.serverAddress = serverIpAddress;
		this.fileTransferPort = fileTransferPort;
		this.registerSoutilsObserver(soutilsObserver);
		
		this.totalNumberOfBytesToBeTransferred = totalNumberOfBytesToBeTransferred;
	}
	
	/** Starts downloading the specified file to the specified location via either the default or a specified port. 
	 * The file transfer server must be running in order for this method to work */
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
	
	/** This method allows to manually terminate the download of a file. To be used WITH CAUTION! */
	public void setDone(boolean done) {
		this.done = done;
	}
	
	/**
	 * Check whether the download is finished or not
	 * @return true if finished, false otherwise
	 */
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
