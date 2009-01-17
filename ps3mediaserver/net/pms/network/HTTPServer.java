/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.network;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;

import net.pms.PMS;
import net.pms.util.PMSUtil;

public class HTTPServer implements Runnable {
	
	private ArrayList<String> ips;
	private int port;
	private String hostName;
	private ServerSocketChannel serverSocketChannel;
	private ServerSocket serverSocket;
	private boolean stop;
	private Thread runnable;
	private InetAddress iafinal = null;
	public InetAddress getIafinal() {
		return iafinal;
	}

	private NetworkInterface ni = null;
	
	public NetworkInterface getNi() {
		return ni;
	}

	public HTTPServer(int port) {
		this.port = port;
		ips = new ArrayList<String>();
	}
	
	public boolean start() throws IOException {
		
		Enumeration<NetworkInterface> enm = NetworkInterface.getNetworkInterfaces();
		InetAddress ia = null;
		boolean found = false;
		String fixedNetworkInterfaceName = PMS.getConfiguration().getNetworkInterface();
		NetworkInterface fixedNI = NetworkInterface.getByName(fixedNetworkInterfaceName);
		while (enm.hasMoreElements()) {
			ni = enm.nextElement();
			if (fixedNI != null)
				ni = fixedNI;
			PMS.minimal("Scanning network interface " + ni.getName() + " / " + ni.getDisplayName());
			if (!PMSUtil.isNetworkInterfaceLoopback(ni) && ni.getName() != null) {
				
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					ia = addrs.nextElement();
					if (!(ia instanceof Inet6Address) && !ia.isLoopbackAddress()) {
						iafinal = ia;
						found = true;
						if (StringUtils.isNotEmpty(PMS.getConfiguration().getServerHostname())) {
							found = iafinal.equals(InetAddress.getByName(PMS.getConfiguration().getServerHostname()));
						}
						break;
					}
				}
				
			}
			if (found || fixedNI != null)
				break;
		}
		hostName = PMS.getConfiguration().getServerHostname();
		SocketAddress address = null;
		if (hostName != null && hostName.length() > 0) {
			PMS.minimal("Using forced address " + hostName);
			InetAddress tempIA = InetAddress.getByName(hostName);
			if (tempIA != null && ni != null && ni.equals(NetworkInterface.getByInetAddress(tempIA))) {
				address = new InetSocketAddress(tempIA, port);
			} else
				address = new InetSocketAddress(hostName, port);
		} else if (iafinal != null) {
			PMS.minimal("Using address " + iafinal + " found on network interface: " + ni.toString().trim().replace('\n', ' '));
			address = new InetSocketAddress(iafinal, port);
		} else {
			PMS.minimal("Using localhost address");
			address = new InetSocketAddress(port);
		}
		PMS.minimal("Created socket: " + address);
		serverSocketChannel = ServerSocketChannel.open();
		
		serverSocket = serverSocketChannel.socket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(address);
		
		if (hostName == null && iafinal !=null)
			hostName = iafinal.getHostAddress();
		else if (hostName == null)
			hostName = InetAddress.getLocalHost().getHostAddress();
		
		
		runnable = new Thread(this);
		runnable.setDaemon(false);
		runnable.start();
		
		return true;
	}
	
	public void stop() {
		PMS.info( "Stopping server on host " + hostName + " and port " + port + "...");
		runnable.interrupt();
		runnable = null;
		try {
			serverSocket.close();
			serverSocketChannel.close();
		} catch (IOException e) {}
		
	}
	
	public void run() {

		PMS.minimal( "Starting DLNA Server on host " + hostName + " and port " + port + "...");
		while (!stop) {
			try {
				Socket socket = serverSocket.accept();
				String ip = socket.getInetAddress().getHostAddress();
				if (!ips.contains(ip)) {
					ips.add(ip);
					PMS.minimal("Receiving a request from: " + ip);
				}
				RequestHandler request = new RequestHandler(socket);
				Thread thread = new Thread(request);
				thread.start();
			} catch (ClosedByInterruptException e) {
				stop = true;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (stop && serverSocket != null)
						serverSocket.close();
					if (stop && serverSocketChannel != null)
						serverSocketChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public String getURL() {
		return "http://" + hostName + ":" + port;
	}
	
	public String getHost() {
		return hostName;
	}
	
	public int getPort() {
		return port;
	}

}
