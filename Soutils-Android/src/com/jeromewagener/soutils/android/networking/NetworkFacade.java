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

package com.jeromewagener.soutils.android.networking;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;


/** A gateway for quick access to certain network related functionality */
@SuppressWarnings("deprecation")
public class NetworkFacade {	
	
	/**
	 * Get the current IP address of the local device
	 * @return the current IP address of the local device
	 */
	public static String getCurrentIpAddress() {
        try {
            for (Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces(); 
            		networkInterfaceEnumeration.hasMoreElements();) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                for (Enumeration<InetAddress> ipAddressEnumeration = networkInterface.getInetAddresses(); 
                		ipAddressEnumeration.hasMoreElements();) {
                    InetAddress inetAddress = ipAddressEnumeration.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        	Log.e("SOUTILS", "An error occurred while retrieving the local IP address", ex);
        }
        
        return null;		
	}
	
	/**
	 * Get the broadcast address of the current network
	 * @param context the application context to be used to access the wifimanager
	 * @author not me, unfortunately I forgot where I found this snippet
	 * @return the broadcast address of the current network
	 */
	public static InetAddress getCurrentBroadcastAddress(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

		if (dhcpInfo == null) {
			return null;
		}

		int broadcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
		byte[] quads = new byte[4];

		for (int k = 0; k < 4; k++) {
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		}

		InetAddress broadcastAddress = null;

		try {
			broadcastAddress = InetAddress.getByAddress(quads);
		} catch (UnknownHostException e) {
			Log.e("SOUTILS", "An error occurred while retrieving the current broadcast address", e);
		}

		return broadcastAddress;
	}
	
	/**
	 * Returns a new Communication (client) thread which is able to connect with a CommunicationManager (server) thread.
	 * As for any Java thread, this thread needs to be first started. If the communication is terminated, the thread
	 * needs to be stopped.
	 * @param socketChannel the socketchannel to be used during the communication
	 * @param messageHandler the handler to which received messages are forwarded
	 * @return a new communication (client) thread which must be manually started and terminated
	 * @see Communication
	 */
	public static Communication getCommunicationThread(SocketChannel socketChannel, Handler messageHandler) {
		return new Communication(socketChannel, messageHandler);
	}
	
	/**
	 * Returns a new Communication (client) thread which is able to connect with a CommunicationManager (server) thread.
	 * As for any Java thread, this thread needs to be first started. If the communication is terminated, the thread
	 * needs to be stopped.
	 * @param ipAddress the IP address of the host
	 * @param port the TCP port to which you want to connect to
	 * @param messageHandler the handler to which received messages are forwarded
	 * @return a new communication (client) thread which must be manually started and terminated
	 * @see Communication
	 */
	public static Communication getCommunicationThread(String ipAddress, int port, Handler messageHandler) {
		return new Communication(ipAddress, port, messageHandler);
	}
	
	/**
	 * Returns a new CommunicationManager (server) thread to which Communications (clients) can connect to. 
	 * As for any Java thread, this thread needs to be first started. If the communication is terminated, the thread
	 * needs to be stopped.
	 * @param port the port to be used by the server
	 * @param messageHandler the handler to which received messages are forwarded
	 * @return a new communication manager (server) thread which must be manually started and terminated
	 * @see CommunicationManager
	 */
	public static CommunicationManager getCommunicationManagerThread(int port, Handler messageHandler) {
		return new CommunicationManager(port, messageHandler);
	}
}
