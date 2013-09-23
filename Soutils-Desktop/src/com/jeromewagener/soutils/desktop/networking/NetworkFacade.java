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

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import com.jeromewagener.soutils.Utilities;

/** A gateway for quick access to certain network related functionality */
public class NetworkFacade {
	/**
	 * Get the IP version 4 IP address of the local device
	 * @return the IP version 4 IP address of the local device
	 */
	public static String getCurrentIPv4Address() {
		try {
			for (Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces(); 
					networkInterfaceEnumeration.hasMoreElements();) {
				NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
				for (Enumeration<InetAddress> ipAddressEnumeration = networkInterface.getInetAddresses(); 
						ipAddressEnumeration.hasMoreElements();) {
					InetAddress inetAddress = ipAddressEnumeration.nextElement();
					
					if (!inetAddress.isLoopbackAddress() && Utilities.isValidIPv4Address(inetAddress.getHostAddress())) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}

		return null;		
	}

	/**
	 * Get the network mac address of the local device
	 * @return the network mac address of the local device
	 */
	public static String getCurrentNetworkMacAddress() {
		return getNetworkMacAddress(getCurrentIPv4Address());
	}
	
	/**
	 * Get the network mac address associated with a particular IP address
	 * @param ipAddress the IP address for which the mac address is requested
	 * @return the network mac address
	 */
	public static String getNetworkMacAddress(String ipAddress) {
		String networkMacAddressAsString = "";
		
		try {
			InetAddress address = InetAddress.getByName(ipAddress);
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
			
			if (networkInterface != null) {
				byte[] networkMacAddress = networkInterface.getHardwareAddress();
				if (networkMacAddress != null) {
					for (int i = 0; i < networkMacAddress.length; i++) {
						networkMacAddressAsString += String.format(
								"%02X%s", networkMacAddress[i], (i < networkMacAddress.length - 1) ? ":" : "");
					}
				} else {
					throw new Exception("Cannot determine network mac address");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return networkMacAddressAsString;
	}
	
	/** 
	 * Get a mapping of all assigned IP-addresses to their according broadcast addresses 
	 * @return a mapping of all assigned IP-addresses to their according broadcast addresses 
	 * */
	public static HashMap<InetAddress, InetAddress> getAllIPsAndAssignedBroadcastAddresses() { 
		HashMap<InetAddress, InetAddress> ipAndBroadcastAddresses = new HashMap<InetAddress, InetAddress>();
		Enumeration<?> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();

			while(networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();

				if(networkInterface != null && !networkInterface.isLoopback() && networkInterface.isUp()) {
					Iterator<?> it = networkInterface.getInterfaceAddresses().iterator();
					while (it.hasNext()) {
						InterfaceAddress interfaceAddress = (InterfaceAddress) it.next();

						if (interfaceAddress != null && interfaceAddress.getBroadcast() != null) {
							ipAndBroadcastAddresses.put(interfaceAddress.getAddress(), interfaceAddress.getBroadcast());
						}
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}

		return ipAndBroadcastAddresses;
	}
	
	/**
	 * Returns a new Communication (client) thread which is able to connect with a CommunicationManager (server) thread.
	 * As for any Java thread, this thread needs to be first started. If the communication is terminated, the thread
	 * needs to be stopped.
	 * @param socketChannel the socketchannel to be used during the communication
	 * @return a new communication (client) thread which must be manually started and terminated
	 * @see Communication
	 */
	public static Communication getCommunicationThread(SocketChannel socketChannel) {
		return new Communication(socketChannel);
	}
	
	/**
	 * Returns a new Communication (client) thread which is able to connect with a CommunicationManager (server) thread.
	 * As for any Java thread, this thread needs to be first started. If the communication is terminated, the thread
	 * needs to be stopped.
	 * @param ipAddress the IP address of the host
	 * @param port the TCP port to which you want to connect to
	 * @return a new communication (client) thread which must be manually started and terminated
	 * @see Communication
	 */
	public static Communication getCommunicationThread(String ipAddress, int port) {
		return new Communication(ipAddress, port);
	}
	
	/**
	 * Returns a new CommunicationManager (server) thread to which Communications (clients) can connect to. 
	 * As for any Java thread, this thread needs to be first started. If the communication is terminated, the thread
	 * needs to be stopped.
	 * @param port the port to be used by the server
	 * @return a new communication manager (server) thread which must be manually started and terminated
	 * @see CommunicationManager
	 */
	public static CommunicationManager getCommunicationManagerThread(int port) {
		return new CommunicationManager(port);
	}
}
