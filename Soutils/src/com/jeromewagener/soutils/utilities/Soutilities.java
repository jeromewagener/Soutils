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

package com.jeromewagener.soutils.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.iharder.Base64;

/** Various utility methods */
public class Soutilities {
	/** Calculates the MD5 hash of a given string 
	 * @author Tom V. http://m2tec.be/blog/2010/02/03/java-md5-hex-0093 
	 * @param stringToBeHashed the string to be hashed 
	 * @return the MD5 hash as a string 
	 * @throws NoSuchAlgorithmException */
	public static String stringToMD5(String stringToBeHashed) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		StringBuffer md5Hash = new StringBuffer();
		byte[] array = messageDigest.digest(stringToBeHashed.getBytes());

		for (int i = 0; i < array.length; i++) {
			md5Hash.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		}

		return md5Hash.toString();
	}

	/** Calculates the MD5 checksum of a file
	 * @param filepath The path to the file for which the MD5 checksum should be calculated 
	 * @author Réal Gagnon - See http://www.rgagnon.com/javadetails/java-0416.html 
	 * @return the MD5 hash in a byte array representation */
	private static byte[] createChecksum(String filepath) throws IOException, NoSuchAlgorithmException {
		InputStream fileInputStream = new FileInputStream(filepath);

		byte[] buffer = new byte[1024];
		MessageDigest md5HashInstance = MessageDigest.getInstance("MD5");
		int numberOfBytesRead;

		do {
			numberOfBytesRead = fileInputStream.read(buffer);
			if (numberOfBytesRead > 0) {
				md5HashInstance.update(buffer, 0, numberOfBytesRead);
			}
		} while (numberOfBytesRead != -1);

		fileInputStream.close();

		return md5HashInstance.digest();
	}

	/** Calculates the MD5 checksum of a file
	 * @param filepath The path to the file for which the MD5 checksum should be calculated 
	 * @author Réal Gagnon - See http://www.rgagnon.com/javadetails/java-0416.html 
	 * @return the MD5 hash as a string */
	public static String fileToMD5(String filepath) throws IOException, NoSuchAlgorithmException {
		byte[] b = createChecksum(filepath);
		String result = "";

		for (int i=0; i < b.length; i++) {
			result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}

		return result;
	}


	/** Reads the specified file into a string
	 * @param filePath the path to the file that should be read
	 * @return the content of the specified file as a string or an empty string if the file does not exist 
	 * @throws IOException */
	public static String readFileAsString(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			return "";
		}

		byte[] buffer = new byte[(int) new File(filePath).length()];

		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
		bufferedInputStream.read(buffer);
		bufferedInputStream.close();

		return new String(buffer);
	}

	/** Creates, respectively overwrites the specified file with a given string 
	 * @param stringToBeWritten the string that should be written to the specified location 
	 * @param filePath the path to the output file. This file will be overridden if it exists! 
	 * @throws IOException */
	public static void writeStringToFile(String stringToBeWritten, String filePath) throws IOException {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
		bufferedWriter.write(stringToBeWritten);

		bufferedWriter.close();
	}

	/** Returns the filename from an absolute filepath 
	 * @param filepath the filepath which contains a filename 
	 * @return the file name from the specified file. Attention, this method assumes that everything behind the
	 * last slash "/" from the filepath corresponds to the filename! */
	public static String getFilenameFromFilepath(String filepath) {
		int index = filepath.lastIndexOf("/");

		return filepath.substring(index + 1, filepath.length());
	}

	/** Returns a string representation of the specified input stream. 
	 * @param inputStream the input stream which should be converted to a string
	 * @return A string representation of the stream or an empty string if a problem occurred
	 * @author Paval Repin - http://stackoverflow.com/a/5445161 */
	public static String convertInputStreamToString(java.io.InputStream inputStream) {
		try {
			return new java.util.Scanner(inputStream).useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	}

	/** Returns an input stream based on the specified string 
	 * @param string the string which should be converted into an input stream
	 * @return An input stream of the specified string, or null if a problem occurred 
	 * @throws UnsupportedEncodingException */
	public static InputStream convertStringToInputStream(String string) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(string.getBytes("UTF-8"));
	}

	private final static String[] units = 
			new String[] { "Bytes", "KiloBytes", "MegaBytes", "GigaBytes", "TeraBytes"};
	/** Returns a human readable representation of a filesize given in bytes
	 * @author Mr Ed - http://stackoverflow.com/a/5599842 
	 * @param fileSizeInBytes the file size in bytes 
	 * @return a human readable representation of the filesize using the following measures: Bytes, KiloBytes, MegaBytes,
	 * GigaBytes, TeraBytes */
	public static String getReadableFileSize(long fileSizeInBytes) {
		if (fileSizeInBytes <= 0) {
			return "0";
		}

		int digitGroups = (int) (Math.log10(fileSizeInBytes)/Math.log10(1024));

		return new DecimalFormat("#,##0.#").format(fileSizeInBytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	/** Downloads a file from the specified URL and stores the file to the specified location 
	 * @param fileUrl the URL from which the file should be downloaded
	 * @param storageLocation the location to which the downloaded file should be stored. If the file exists, it will
	 * be overridden! 
	 * @throws IOException */
	public static void downloadFileFromWebserver(String fileUrl, String storageLocation) throws IOException {
		URL url = new URL(fileUrl);
		File file = new File(storageLocation);

		URLConnection urlConnection = url.openConnection();
		InputStream inputStream = urlConnection.getInputStream();
		BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
		FileOutputStream fileOutputStream = new FileOutputStream(file);

		byte[] buffer = new byte[1024];
		int bytesInBuffer = 0;
		while ((bytesInBuffer = bufferedInputStream.read(buffer)) != -1) {
			fileOutputStream.write(buffer, 0 , bytesInBuffer);
		}

		fileOutputStream.close();
	}

	/** Returns a random number from the specified range 
	 * @param min the smallest value that should occur when randomly generating numbers 
	 * @param max the largest value that should occur when randomly generating numbers 
	 * @return a random number between min and max. If min is larger than max, an exception will be thrown! */
	public static int randomNumberBetweenIntervals(int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("The minimum bound cannot be larger or equal to the maximum bound!");
		}

		return min + (int)(Math.random() * ((max - min) + 1));
	}

	/** Returns true if the supplied string corresponds to a valid IPv4 address. This method does only return true for
	 * valid IP addresses in the following format xxx.xxx.xxx.xxx 
	 * @param ipv4Address the ip address that should be checked for validity 
	 * @return true if it is a valid IP address, false otherwise. This method checks only if the string contains 
	 * three dots and is a convenience method to filter between different IPv4 formats. */
	public static boolean isValidIPv4Address(String ipv4Address) {
		int firstDot = -1, secondDot = -1;

		if (((firstDot = ipv4Address.indexOf(".", 0)) != -1)
				&& ((secondDot = ipv4Address.indexOf(".", firstDot + 1)) != -1)
				&& (ipv4Address.indexOf(".", secondDot + 1) != -1)) {
			
			// FIXME previous method no longer supported by java 1.7+
			return true;
		}

		return false;
	}

	/** Returns true if the supplied string corresponds to a valid IPv6 address 
	 * @param ipv6Address the string to be checked for validity 
	 * @return true if the string represents a valid IPv6 address, false otherwise */
	public static boolean isValidIPv6Address(String ipv6Address) {
		// FIXME previous method no longer supported by java 1.7+
		return true;
	}

	/** Returns a base64 encoded string
	 * @param stringToBeEncoded the string to be encoded with base64
	 * @return the string which is encoded with base64 */
	public static String encodeStringWithBase64(String stringToBeEncoded) {
		return Base64.encodeBytes(stringToBeEncoded.getBytes());
	}

	/** Returns a string decoded from a base64 encoded string
	 * @param stringToBeDecoded the string to be decoded with base64
	 * @return the string which is decoded using base64, or null if an error occurs! 
	 * @throws IOException */
	public static String decodeStringWithBase64(String stringToBeDecoded) throws IOException {
		byte[] decodedBytes = Base64.decode(stringToBeDecoded);

		return new String(decodedBytes);
	}
	
	/** Returns a base64 encoded string that is URL_SAFE
	 * @param stringToBeEncoded the string to be encoded with base64
	 * @return the string which is encoded with base64, but URL_SAFE (or null if an error occurred) 
	 * @throws IOException */
	public static String encodeStringWithBase64UrlSafe(String stringToBeEncoded) throws IOException {
		return Base64.encodeBytes(stringToBeEncoded.getBytes(), Base64.URL_SAFE);
	}

	/** Returns a string decoded from an URL_SAFE base64 encoded string
	 * @param stringToBeDecoded the string to be decoded using URL_SAFE base64
	 * @return the string which is decoded using URL_SAFE base64, or null if an error occurs! 
	 * @throws IOException */
	public static String decodeStringWithBase64UrlSafe(String stringToBeDecoded) throws IOException {
		byte[] decodedBytes = Base64.decode(stringToBeDecoded, Base64.URL_SAFE);

		return new String(decodedBytes);
	}
	
	/** Splits a multi-message xml string into several message strings. If the provided string only contains 
	 * one message, only this message will be returned. If the multiMessageString does not contain at 
	 * least one device-directory-message, the original string will be returned */
	public static List<String> splitMultiMessageString(String multiMessageString, String rootXmlTag) {
		ArrayList<String> messages = new ArrayList<String>();
		
		if (multiMessageString.startsWith(rootXmlTag)) {
			for (String message : multiMessageString.split(rootXmlTag)) {
				if (!(message.trim()).isEmpty()) {
					messages.add(rootXmlTag + message);
				}
			}

			return messages;
		}

		messages.add(multiMessageString);
		
		return messages;
	}
}
