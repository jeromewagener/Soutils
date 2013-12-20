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

/** An easy way to offer downloads to {@link FileTransferClient}'s via a TCP connection using a single Thread */
public class FileTransferServer extends SoutilsObservable {
	private String storageLocationAsAbsolutPath = null;
	private final int fileTransferPort;
	private boolean done = false;
	
	/**
	 * Create a download server which offers a specific file for an one time download
	 * @param storageLocationAsAbsolutPath the location of the file offered for downloading
	 * @param fileTransferPort the port used to offer the file download
	 */
	public FileTransferServer(String storageLocationAsAbsolutPath, int fileTransferPort, SoutilsObserver soutilsObserver) {
		this.storageLocationAsAbsolutPath = storageLocationAsAbsolutPath;
		this.fileTransferPort = fileTransferPort;
		this.registerSoutilsObserver(soutilsObserver);
	}
	
	/** Starts offering to upload the specified file via either the default or a specified port. 
	 * This thread must be running in order for clients to be able to download*/
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
	
	/** This method allows to manually terminate the upload of a file. To be used WITH CAUTION! */
	public void setDone(boolean done) {
		this.done = done;
	}
	
	/**
	 * Check whether the upload to another device is finished or not
	 * @return true if finished, false otherwise
	 */
	public boolean isDone() {
		return done;
	}
}
