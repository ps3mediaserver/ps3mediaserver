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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;

public class RequestHandler implements Runnable {
	
	public final static int SOCKET_BUF_SIZE = 32768;
	
	private Socket socket;
	private OutputStream output;
	private BufferedReader br;
	
	public RequestHandler(Socket socket) throws IOException {
		this.socket = socket;
		if (PMS.getConfiguration().isTurboModeEnabled()) {
			try {
				//socket.setSendBufferSize(SOCKET_BUF_SIZE);
			} catch(Exception e) {
				PMS.error(null, e);
			}
			try {
				socket.setTcpNoDelay(true);
			} catch(Exception e) {
				PMS.error(null, e);
			}
		}
		
		this.output = socket.getOutputStream();
		this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public void run() {
		
		Request request = null;
		try {
			
			PMS.debug("Opened handler on socket " + socket);
			
			int receivedContentLength = -1;
			
			String headerLine = br.readLine();
			while (headerLine != null && headerLine.length() > 0) {
				PMS.debug( "Received on socket: " + headerLine);
				if (headerLine != null && headerLine.indexOf("PLAYSTATION") >-1)
					PMS.get().setPs3found(true);
				else if (headerLine != null && headerLine.indexOf("Xbox") >-1) {
					PMS.get().setXboxfound(true);
					request.setMediaRenderer(Request.XBOX);
				}
				try {
					StringTokenizer s = new StringTokenizer(headerLine);
					String temp = s.nextToken();
					if (temp.equals("GET") || temp.equals("POST") || temp.equals("HEAD")) {
						request = new Request(temp, s.nextToken().substring(1));
						if (s.hasMoreTokens() && s.nextToken().equals("HTTP/1.0"))
							request.setHttp10(true);
					} else if (request != null && temp.toUpperCase().equals("SOAPACTION:")) {
						request.setSoapaction(s.nextToken());
					} else if (headerLine.toUpperCase().contains("CONTENT-LENGTH:")) {
						receivedContentLength = Integer.parseInt(headerLine.substring(headerLine.toUpperCase().indexOf("CONTENT-LENGTH: ")+16));
					} else if (headerLine.indexOf("Range: bytes=") > -1) {
						String nums = headerLine.substring(headerLine.indexOf("Range: bytes=")+13).trim();
						StringTokenizer st = new StringTokenizer(nums, "-");
						if (!nums.startsWith("-"))
							request.setLowRange(Long.parseLong(st.nextToken()));
						if (!nums.startsWith("-") && !nums.endsWith("-"))
							request.setHighRange(Long.parseLong(st.nextToken()));
						else
							request.setHighRange(DLNAMediaInfo.TRANS_SIZE);
					} else if (headerLine.indexOf("transferMode.dlna.org:") > -1) {
						request.setTransferMode(headerLine);
					} else if (headerLine.indexOf("TimeSeekRange.dlna.org: npt=") > -1) { // firmware 2.50+
						String timeseek = headerLine.substring(headerLine.indexOf("TimeSeekRange.dlna.org: npt=")+28);
						if (timeseek.endsWith("-"))
							timeseek = timeseek.substring(0, timeseek.length()-1);
						request.setTimeseek(Double.parseDouble(timeseek));
					} else if (headerLine.indexOf("TimeSeekRange.dlna.org : npt=") > -1) { // firmware 2.40
						String timeseek = headerLine.substring(headerLine.indexOf("TimeSeekRange.dlna.org : npt=")+29);
						if (timeseek.endsWith("-"))
							timeseek = timeseek.substring(0, timeseek.length()-1);
						request.setTimeseek(Double.parseDouble(timeseek));
					}
				} catch (Exception e) {
					PMS.error("Error in parsing HTTP headers", e);
				}
				
				headerLine = br.readLine();
			}
			
			
			if (receivedContentLength > 0) {
				
				char buf [] = new char [receivedContentLength-1];
				br.read(buf);
				if (request != null)
					request.setTextContent(new String(buf));
			}
			
			if (request != null)
				PMS.info( "HTTP: " + request.getArgument() + " / " + request.getLowRange() + "-" + request.getHighRange());
			
			if (request != null)
				request.answer(output);
			
			if (request != null && request.getInputStream() != null)
				request.getInputStream().close();
			
		} catch (IOException e) {
			PMS.debug("Unexpected IO Error: " + e.getClass() + ": " +  e.getMessage());
			if (request != null && request.getInputStream() != null) {
				try {
					PMS.debug( "Close InputStream" + request.getInputStream());
					request.getInputStream().close();
				} catch (IOException e1) {
					PMS.error("Close InputStream Error", e);
				}
			}
		} finally {
			try {
				output.close();
				br.close();
				
				socket.close();
			} catch (IOException e) {
				PMS.error("Close Connection Error", e);
			}
			PMS.debug("Close Connection");
		}
	}
	
	

}
