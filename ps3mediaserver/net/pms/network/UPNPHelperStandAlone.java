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
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class UPNPHelperStandAlone {
	
	private final static String CRLF = "\r\n";
	private final static String ALIVE = "ssdp:alive";
	private final static String UPNP_HOST = "239.255.255.250";
	private final static int UPNP_PORT = 1900;
	private final static String BYEBYE = "ssdp:byebye";
	private static MulticastSocket ssdpSocket; 
	
	public static void sendAlive() throws IOException {
		
		System.out.println( "Sending ALIVE...");
		sendMessage("upnp:rootdevice", ALIVE);
		sendMessage( "uuid:1234567890TOTO::", ALIVE);
		sendMessage( "urn:schemas-upnp-org:device:MediaServer:1", ALIVE);
		sendMessage( "urn:schemas-upnp-org:service:ContentDirectory:1", ALIVE);
		getSocket().close();
		ssdpSocket = null;
	}
	
	public static void sendByeBye() throws IOException {
		
		System.out.println( "Sending BYEBYE...");
		sendMessage("upnp:rootdevice", BYEBYE);
		sendMessage( "uuid:1234567890TOTO::", BYEBYE);
		sendMessage( "urn:schemas-upnp-org:device:MediaServer:1", BYEBYE);
		sendMessage( "urn:schemas-upnp-org:service:ContentDirectory:1", BYEBYE);
		getSocket().close();
		ssdpSocket = null;
	}
	
	private static DatagramSocket getSocket() throws IOException {
		if (ssdpSocket == null) {
			ssdpSocket = new MulticastSocket(null);
			ssdpSocket.setReuseAddress(true);
			ssdpSocket.setTimeToLive(4);
			ssdpSocket.bind(new InetSocketAddress(UPNP_PORT));
		}
		return ssdpSocket;
	}
	
	private static void sendMessage(String nt, String message) throws IOException {
		String msg = buildMsg(nt, message);
		System.out.println( "Sending SSDP packet: " + msg);
		DatagramPacket ssdpPacket = new DatagramPacket(msg.getBytes(), msg.length(), getUPNPAddress(), UPNP_PORT);
		getSocket().send(ssdpPacket);
		
	}
	
	public static void listen() throws IOException {
		Runnable r = new Runnable() {
			public void run() {
			
				byte[] buf = new byte[1024];
				DatagramPacket packet_r = new DatagramPacket(buf, buf.length);
				try { 
					MulticastSocket socket = new MulticastSocket(1900);
					
					socket.setReuseAddress(true);
			        socket.joinGroup(getUPNPAddress());
			        socket.receive(packet_r);
			        socket.leaveGroup(getUPNPAddress());
			        socket.close();
					
			        String s = new String(packet_r.getData());
					
					if (s.startsWith("M-SEARCH")) {
						System.out.println( "Receiving search request!");
						sendAlive();
						listen();
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("UPNP network exception");
				}
		
			}};
		new Thread(r).start();
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
		sb.append("CACHE-CONTROL: max-age=1800");
		sb.append(CRLF);
		sb.append("LOCATION: http://192.168.0.12:6001/description/fetch");
		sb.append(CRLF);
		sb.append("nt: ");
		sb.append(nt);
		sb.append(CRLF);
		sb.append("nts: ");
		sb.append(message);
		sb.append(CRLF);
		sb.append("Server: ");
		sb.append("MyServer");
		sb.append(CRLF);
		sb.append("USN: ");
		sb.append("uuid:1234567890TOTO::");
		if (!nt.equals("uuid:1234567890TOTO::"))
			sb.append(nt);
		sb.append(CRLF);
		sb.append(CRLF);
		return sb.toString();
	}
	
	private static InetAddress getUPNPAddress() throws IOException {
		return InetAddress.getByAddress(UPNP_HOST, new byte[]{(byte) 239, (byte)255, (byte)255, (byte)250});
	}
	
	
	public static void main(String args[]) throws IOException {
		sendByeBye();
		sendAlive();
	}

}
