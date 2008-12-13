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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import net.pms.PMS;



public class UPNPHelper {
	
	private final static String CRLF = "\r\n";
	private final static String ALIVE = "ssdp:alive";
	private final static String UPNP_HOST = "239.255.255.250";
	private final static int UPNP_PORT = 1900;
	private final static String BYEBYE = "ssdp:byebye";
	private static Thread listener; 
	
	public static void sendAlive() throws IOException {
		
		PMS.info( "Sending ALIVE...");
		
		MulticastSocket ssdpSocket = getNewMulticastSocket();
		sendMessage(ssdpSocket, "upnp:rootdevice", ALIVE);
		sendMessage(ssdpSocket,  PMS.get().usn(), ALIVE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:device:MediaServer:1", ALIVE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:service:ContentDirectory:1", ALIVE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:service:ConnectionManager:1", ALIVE);
		//sendMessage(ssdpSocket,  "urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1", ALIVE);
		/*try {
			Thread.sleep(200);
		} catch (InterruptedException e) { }
		sendMessage(ssdpSocket, "upnp:rootdevice", ALIVE);
		sendMessage(ssdpSocket,  PMS.get().usn(), ALIVE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:device:MediaServer:1", ALIVE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:service:ContentDirectory:1", ALIVE);*/
		
		ssdpSocket.leaveGroup(getUPNPAddress());
		ssdpSocket.close();
		ssdpSocket = null;
	}
	
	private static MulticastSocket getNewMulticastSocket() throws IOException {
		MulticastSocket ssdpSocket = new MulticastSocket();
		if (PMS.get().getHostname() != null && PMS.get().getHostname().length() > 0) {
			PMS.debug("Searching network interface for " + PMS.get().getHostname());
			NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(PMS.get().getHostname()));
			if (ni != null)
				ssdpSocket.setNetworkInterface(ni);
		} else if ( PMS.get().getServer().getNi() != null) {
			PMS.debug("Setting multicast network interface: " +  PMS.get().getServer().getNi());
			ssdpSocket.setNetworkInterface( PMS.get().getServer().getNi());
		}
		PMS.debug("Sending message from multicast socket on network interface: " + ssdpSocket.getNetworkInterface());
		PMS.debug("Multicast socket is on interface: " + ssdpSocket.getInterface());
		ssdpSocket.setTimeToLive(32);
		ssdpSocket.setLoopbackMode(true);
		ssdpSocket.joinGroup(getUPNPAddress());
		PMS.debug("Socket Timeout: " + ssdpSocket.getSoTimeout());
		PMS.debug("Socket TTL: " + ssdpSocket.getTimeToLive());
		return ssdpSocket;
	}
	
	//private static boolean first = true;
	
	public static void sendByeBye() throws IOException {
		
		PMS.info( "Sending BYEBYE...");
		//if (!first) {
		MulticastSocket ssdpSocket = getNewMulticastSocket();
		
		sendMessage(ssdpSocket, "upnp:rootdevice", BYEBYE);
		sendMessage(ssdpSocket,  PMS.get().usn(), BYEBYE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:device:MediaServer:1", BYEBYE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:service:ContentDirectory:1", BYEBYE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:service:ConnectionManager:1", ALIVE);
		//sendMessage(ssdpSocket,  "urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1", ALIVE);
		/*try {
			Thread.sleep(200);
		} catch (InterruptedException e) { }
		sendMessage(ssdpSocket, "upnp:rootdevice", BYEBYE);
		sendMessage(ssdpSocket,  PMS.get().usn(), BYEBYE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:device:MediaServer:1", BYEBYE);
		sendMessage(ssdpSocket,  "urn:schemas-upnp-org:service:ContentDirectory:1", BYEBYE);*/
		
		ssdpSocket.leaveGroup(getUPNPAddress());
		ssdpSocket.close();
		ssdpSocket = null;
		//}
		//first = false;
	}

	
	private static void sendMessage(DatagramSocket socket, String nt, String message) throws IOException {
		String msg = buildMsg(nt, message);
		PMS.debug( "Sending this SSDP packet: " + PMS.get().replace(msg, CRLF, "<CRLF>"));
		DatagramPacket ssdpPacket = new DatagramPacket(msg.getBytes(), msg.length(), getUPNPAddress(), UPNP_PORT);
		socket.send(ssdpPacket);
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) { }
		socket.send(ssdpPacket);
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) { }
		
	}
	
	public static void listen() throws IOException {
		Runnable r = new Runnable() {
			public void run() {
			
				while (true) {
					byte[] buf = new byte[1024];
					DatagramPacket packet_r = new DatagramPacket(buf, buf.length);
					try { 
						MulticastSocket socket = new MulticastSocket(1900);
						if (PMS.get().getHostname() != null && PMS.get().getHostname().length() > 0) {
							PMS.debug("Searching network interface for " + PMS.get().getHostname());
							NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(PMS.get().getHostname()));
							if (ni != null)
								socket.setNetworkInterface(ni);
						} else if ( PMS.get().getServer().getNi() != null) {
							PMS.debug("Setting multicast network interface: " +  PMS.get().getServer().getNi());
							socket.setNetworkInterface( PMS.get().getServer().getNi());
						}
						socket.setTimeToLive(4);
						socket.setReuseAddress(true);
				        socket.joinGroup(getUPNPAddress());
				        socket.receive(packet_r);
				        socket.leaveGroup(getUPNPAddress());
				        socket.close();
						
				        String s = new String(packet_r.getData());
						
						if (s.startsWith("M-SEARCH")) {
							PMS.minimal( "Receiving search request from " + packet_r.getAddress().getCanonicalHostName() + "! Sending alive message...");
							sendAlive();
							//listen();
						}
					} catch (IOException e) {
						PMS.error("UPNP network exception", e);
					}
				}
		
			}};
		listener = new Thread(r);
		listener.start();
	}
	
	public static void shutDownListener() {
		listener.interrupt();
	}
	
	private static String buildMsg(String nt, String message) {
		StringBuffer sb = new StringBuffer();
		sb.append("NOTIFY * HTTP/1.1");
		sb.append(CRLF);
		sb.append("HOST: ");
		sb.append(UPNP_HOST);
		sb.append(":");
		sb.append(UPNP_PORT);
		sb.append(CRLF);
		if (!message.equals(BYEBYE)) {
			sb.append("CACHE-CONTROL: max-age=1800");
			sb.append(CRLF);
			sb.append("LOCATION: http://");
			sb.append(PMS.get().getServer().getHost());
			sb.append(":");
			sb.append(PMS.get().getServer().getPort());
			sb.append("/description/fetch");
			sb.append(CRLF);
		}
		sb.append("NT: ");
		sb.append(nt);
		sb.append(CRLF);
		sb.append("NTS: ");
		sb.append(message);
		sb.append(CRLF);
		if (!message.equals(BYEBYE)) {
			sb.append("Server: ");
			sb.append(PMS.get().getServerName());
			sb.append(CRLF);
		}
		sb.append("USN: ");
		sb.append(PMS.get().usn());
		if (!nt.equals(PMS.get().usn()))
			sb.append(nt);
		sb.append(CRLF);
		sb.append(CRLF);
		return sb.toString();
	}
	
	private static InetAddress getUPNPAddress() throws IOException {
		return InetAddress.getByAddress(UPNP_HOST, new byte[]{(byte) 239, (byte)255, (byte)255, (byte)250});
	}

}
