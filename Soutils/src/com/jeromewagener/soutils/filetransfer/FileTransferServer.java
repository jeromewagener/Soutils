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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.jeromewagener.soutils.Parameters;
import com.jeromewagener.soutils.messaging.MessageType;
import com.jeromewagener.soutils.messaging.SoutilsMessage;
import com.jeromewagener.soutils.messaging.SoutilsObservable;
import com.jeromewagener.soutils.messaging.SoutilsObserver;

/** A SoutilsObservable thread which wraps a file transfer server that is able to offer a file for 
 * downloading using a TCP connection. All registered SoutilsObservables will be informed as soon 
 * as as the transfer has been completed.
 * @see FileTransferClient
 * @see SoutilsObservable */
public class FileTransferServer extends SoutilsObservable {
	private final String storageLocationAsAbsolutPath;
	private final int fileTransferPort;
	private final long totalNumberOfBytesToBeTransferred;
	
	private long numberOfBytesAlreadyTransferred = 0;
	private boolean done = false;
	
	/** Sets up a download server which offers a specified file for an one time download
	 * As for any Java thread, the {@link #start()} method must be called to actually start the thread.
	 * @param storageLocationAsAbsolutPath the location of the file offered for downloading
	 * @param fileTransferPort the port used to offer the file download
	 * @param soutilsObserver the observer that is informed in case of errors of in case the download has been completed
	 * @see #start() */
	public FileTransferServer(String storageLocationAsAbsolutPath, int fileTransferPort, SoutilsObserver soutilsObserver) {
		this.storageLocationAsAbsolutPath = storageLocationAsAbsolutPath;
		this.fileTransferPort = fileTransferPort;
		this.registerSoutilsObserver(soutilsObserver);
		
		totalNumberOfBytesToBeTransferred = new File(storageLocationAsAbsolutPath).length();
	}
	
	/** Starts offering a download of the specified file via the specified port. 
	 * This thread must be running in order for clients to be able to download the specified file */
	public void run() {
		try {
			File fileToBeTranferred = new File(storageLocationAsAbsolutPath);

			ServerSocket serverSocket = new ServerSocket(fileTransferPort);
			Socket socket = serverSocket.accept();

			BufferedInputStream bufferedFileInputStream = 
					new BufferedInputStream(new FileInputStream(fileToBeTranferred));
			OutputStream socketOutputStream = socket.getOutputStream();

			int counter = 0;
			byte[] buffer = new byte[Parameters.FILE_TRANSFER_BUFFER_SIZE_IN_BYTES];
			while ((counter = bufferedFileInputStream.read(buffer)) > 0 && !done) {
				socketOutputStream.write(buffer, 0, counter);
				socketOutputStream.flush();
				
				numberOfBytesAlreadyTransferred += counter;
			}

			socketOutputStream.close();
			bufferedFileInputStream.close();

			socket.close();
			serverSocket.close();
		} catch (Exception ex) {
			notifyAllObservers(new SoutilsMessage(MessageType.ERROR, SoutilsMessage.INTERNAL, ex));
		}
		
		done = true;
		notifyAllObservers(new SoutilsMessage(MessageType.FILE_TRANSFER_COMPLETE, SoutilsMessage.INTERNAL, "File transfer successful!"));
	}
	
	/** Call this method to stop the thread from offering the file for download. Once the thread
	 * has been stopped, it cannot be started again. You must instead instantiate a new FileTransferServer.
	 * @see #start() */
	public void done() {
		this.done = true;
	}
	
	/** Check whether the download has finished or not */
	public boolean isDone() {
		return done;
	}
	
	/** Returns the file transfer percentage as an integer value between 0 and 100 */
	public long getFileTransferPercentage() {
		if (done) {
			return 100;
		}
		
		Double transferRatio = ((double) numberOfBytesAlreadyTransferred / (double) totalNumberOfBytesToBeTransferred) * 100;
		return transferRatio.intValue();
	}
}
