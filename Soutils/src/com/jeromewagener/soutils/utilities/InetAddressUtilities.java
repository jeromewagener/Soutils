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

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;


/** A gateway for quick access to certain network related functionality */
public class InetAddressUtilities {
	/**
	 * Get the IP version 4 IP address of the local device
	 * @return the IP version 4 IP address of the local device
	 * @throws SocketException 
	 */
	public static String getCurrentIPv4Address() throws SocketException {
		for (Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces(); 
				networkInterfaceEnumeration.hasMoreElements();) {
			NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
			for (Enumeration<InetAddress> ipAddressEnumeration = networkInterface.getInetAddresses(); 
					ipAddressEnumeration.hasMoreElements();) {
				InetAddress inetAddress = ipAddressEnumeration.nextElement();
				
				if (!inetAddress.isLoopbackAddress() && Soutilities.isValidIPv4Address(inetAddress.getHostAddress())) {
					return inetAddress.getHostAddress().toString();
				}
			}
		}

		return null;		
	}

	/**
	 * Get the network mac address of the local device
	 * @return the network mac address of the local device
	 * @throws SocketException 
	 * @throws UnknownHostException 
	 */
	public static String getCurrentNetworkMacAddress() throws SocketException, UnknownHostException {
		return getNetworkMacAddress(getCurrentIPv4Address());
	}
	
	/**
	 * Get the network mac address associated with a particular IP address
	 * @param ipAddress the IP address for which the mac address is requested
	 * @return the network mac address
	 * @throws UnknownHostException 
	 * @throws SocketException 
	 */
	public static String getNetworkMacAddress(String ipAddress) throws UnknownHostException, SocketException {
		String networkMacAddressAsString = "";
		
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
				throw new RuntimeException("Cannot determine network mac address");
			}
		}
		
		return networkMacAddressAsString;
	}
	
	/** 
	 * Get a mapping of all assigned IP-addresses to their according broadcast addresses 
	 * @return a mapping of all assigned IP-addresses to their according broadcast addresses 
	 * @throws SocketException 
	 * */
	public static HashMap<InetAddress, InetAddress> getAllIPsAndAssignedBroadcastAddresses() throws SocketException { 
		HashMap<InetAddress, InetAddress> ipAndBroadcastAddresses = new HashMap<InetAddress, InetAddress>();
		Enumeration<?> networkInterfaces;
		
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

		return ipAndBroadcastAddresses;
	}	
}
