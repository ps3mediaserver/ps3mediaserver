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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;

public class Request extends HTTPResource {
	
	private final static String CRLF = "\r\n";
	private final static String HTTP_200_OK = "HTTP/1.1 200 OK";
	private final static String HTTP_206_OK = "HTTP/1.1 206 Partial Content" ;
	
	private final static String HTTP_200_OK_10 = "HTTP/1.0 200 OK";
	private final static String HTTP_206_OK_10 = "HTTP/1.0 206 Partial Content";
	
	private final static String CONTENT_TYPE_UTF8 = "CONTENT-TYPE: text/xml; charset=\"utf-8\"";
	private final static String CONTENT_TYPE = "Content-Type: text/xml";
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
	
	public static final int PS3 = 0;
	public static final int XBOX = 1;
	
	private String method;
	private String argument;
	private String soapaction;
	private String content;
	private OutputStream output;
	private String objectID;
	private int startingIndex;
	private int requestCount;
	private String browseFlag;
	private long lowRange;
	private InputStream inputStream;
	private int mediaRenderer;
	
	public int getMediaRenderer() {
		return mediaRenderer;
	}

	public void setMediaRenderer(int mediaRenderer) {
		this.mediaRenderer = mediaRenderer;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public long getLowRange() {
		return lowRange;
	}

	public void setLowRange(long lowRange) {
		this.lowRange = lowRange;
	}
	
	private String transferMode;

	public String getTransferMode() {
		return transferMode;
	}

	public void setTransferMode(String transferMode) {
		this.transferMode = transferMode;
	}

	private double timeseek;
	
	public double getTimeseek() {
		return timeseek;
	}

	public void setTimeseek(double timeseek) {
		this.timeseek = timeseek;
	}

	private long highRange;
	
	public long getHighRange() {
		return highRange;
	}

	public void setHighRange(long highRange) {
		this.highRange = highRange;
	}
	
	private boolean http10;

	public boolean isHttp10() {
		return http10;
	}

	public void setHttp10(boolean http10) {
		this.http10 = http10;
	}

	public Request(String method, String argument) {
		this.method = method;
		this.argument = argument;
	}

	public String getSoapaction() {
		return soapaction;
	}

	public void setSoapaction(String soapaction) {
		this.soapaction = soapaction;
	}

	public String getTextContent() {
		return content;
	}

	public void setTextContent(String content) {
		this.content = content;
	}

	public String getMethod() {
		return method;
	}

	public String getArgument() {
		return argument;
	}
	
	public void answer(OutputStream output) throws IOException {
		this.output = output;
		
		long CLoverride = -1;
		if (lowRange > 0 || highRange > 0) {
			output(output, http10?HTTP_206_OK_10:HTTP_206_OK);
		}
		else
			output(output, http10?HTTP_200_OK_10:HTTP_200_OK);
		
		StringBuffer response = new StringBuffer();
		
		if ((method.equals("GET") || method.equals("HEAD")) && argument.startsWith("console/")) {
			output(output, "Content-Type: text/html");
			response.append(HTMLConsole.servePage(argument.substring(8)));
		} else
		
		if ((method.equals("GET") || method.equals("HEAD")) && argument.startsWith("get/")) {
			String id = argument.substring(argument.indexOf("get/") + 4, argument.lastIndexOf("/"));
			ArrayList<DLNAResource> files = PMS.get().getRootFolder().getDLNAResources(id, false, 0, 0);
			if (files.size() == 1) {
				String fileName = argument.substring(argument.lastIndexOf("/")+1);
				if (fileName.startsWith("thumbnail0000")) {
					output(output, "Content-Type: " + files.get(0).getThumbnailContentType());
					output(output, "Accept-Ranges: bytes");
					output(output, "Expires: " + getFUTUREDATE() + " GMT");
					output(output, "Connection: keep-alive");
					inputStream = files.get(0).getThumbnailInputStream();
				} else {
					inputStream = files.get(0).getInputStream(lowRange, highRange, timeseek);
					output(output, "Content-Type: " + files.get(0).mimeType());
					/*if (getTransferMode() != null) {
						output(output, getTransferMode());
						output(output, "contentFeatures.dlna.org: " + files.get(0).getFlags().substring(1));
					}*/
					CLoverride = files.get(0).length();
					if (lowRange > 0 || highRange > 0) {
						long totalsize = CLoverride;
						if (highRange >= CLoverride)
							highRange = CLoverride-1;
						if (CLoverride == -1) {
							lowRange = 0;
							totalsize = inputStream.available();
							highRange = totalsize -1;
						}
						output(output, "CONTENT-RANGE: bytes " + lowRange + "-" + highRange + "/" +totalsize);
					} /*else if (lowRange == 0 && highRange == DLNAMediaInfo.TRANS_SIZE) {
						output(output, "CONTENT-RANGE: bytes 0-" + (CLoverride-1) + "/" + CLoverride);
					}*/
					if (files.get(0).getPlayer() == null)
						output(output, "Accept-Ranges: bytes");
					//output(output, "Expires: 0");
					output(output, "Connection: keep-alive");
				}
			}
		} else if ((method.equals("GET") || method.equals("HEAD")) && (argument.toLowerCase().endsWith(".png") || argument.toLowerCase().endsWith(".jpg") || argument.toLowerCase().endsWith(".jpeg"))) {
			output(output, "Content-Type: image/jpeg");
			output(output, "Accept-Ranges: bytes");
			output(output, "Connection: keep-alive");
			output(output, "Expires: " + getFUTUREDATE() + " GMT");
			inputStream = getResourceInputStream(argument);
		} else if ((method.equals("GET") || method.equals("HEAD")) && (argument.equals("description/fetch") || argument.endsWith("1.0.xml"))) {
			output(output, CONTENT_TYPE);
			output(output, "Cache-Control: no-cache");
			output(output, "Expires: 0");
			output(output, "Accept-Ranges: bytes");
			output(output, "Connection: keep-alive");
			inputStream = getResourceInputStream((argument.equals("description/fetch")?"PMS.xml":argument));
			if (argument.equals("description/fetch")) {
				byte b [] = new byte [inputStream.available()];
				inputStream.read(b);
				String s = new String(b);
				s = s.replace("uuid:1234567890TOTO", PMS.get().usn().substring(0, PMS.get().usn().length()-2));
				if (mediaRenderer == XBOX) {
					PMS.debug("Doing DLNA changes for Xbox360");
					s = s.replace("Java PS3 Media Server", "PS3 Media Server [" + InetAddress.getLocalHost().getHostName() + "] : 1");
					s = s.replace("<modelName>PMS</modelName>", "<modelName>Windows Media Connect BLAH</modelName>");
					s = s.replace("<serviceList>", "<serviceList>" + CRLF + "<service>" + CRLF +
							"<serviceType>urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1</serviceType>" + CRLF +
							"<serviceId>urn:microsoft.com:serviceId:X_MS_MediaReceiverRegistrar</serviceId>" + CRLF +
							"<SCPDURL>_urn:microsoft.com:serviceId:X_MS_MediaReceiverRegistrar_scpd.xml</SCPDURL>" + CRLF +
							"<controlURL>_urn:microsoft.com:serviceId:X_MS_MediaReceiverRegistrar_control</controlURL>" + CRLF +
							"<eventSubURL>_urn:microsoft.com:serviceId:X_MS_MediaReceiverRegistrar_event</eventSubURL>" + CRLF + "</service>" + CRLF);
				} else
					s = s.replace("Java PS3 Media Server", "PS3 Media Server [" + InetAddress.getLocalHost().getHostName() + "]");
				inputStream = new ByteArrayInputStream(s.getBytes());
			}
		} else if (method.equals("POST") && argument.contains("MS_MediaReceiverRegistrar_control")) {
			output(output, CONTENT_TYPE_UTF8);
			response.append(HTTPXMLHelper.XML_HEADER);
			response.append(CRLF);
			response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
			response.append(CRLF);
			if (soapaction != null && soapaction.contains("IsAuthorized")) {
				response.append(HTTPXMLHelper.XBOX_2);
				response.append(CRLF);
			} else if (soapaction != null && soapaction.contains("IsValidated")) {
				response.append(HTTPXMLHelper.XBOX_1);
				response.append(CRLF);
			}
			response.append(HTTPXMLHelper.BROWSERESPONSE_FOOTER);
			response.append(CRLF);
			response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
			response.append(CRLF);
		} else if (method.equals("POST") && argument.equals("upnp/control/content_directory")) {
			output(output, CONTENT_TYPE_UTF8);
			if (soapaction.indexOf("ContentDirectory:1#GetSystemUpdateID") > -1) {
				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.GETSYSTEMUPDATEID_HEADER);
				response.append(CRLF);
				response.append("<Id>" + DLNAResource.systemUpdateId + "</Id>");
				response.append(CRLF);
				response.append(HTTPXMLHelper.GETSYSTEMUPDATEID_FOOTER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
			} else if (soapaction.indexOf("ContentDirectory:1#Browse") > -1) {
				objectID = getEnclosingValue(content, "<ObjectID>", "</ObjectID>");
				String containerID = null;
				if ((objectID == null || objectID.length() == 0) && mediaRenderer == XBOX) {
					containerID = getEnclosingValue(content, "<ContainerID>", "</ContainerID>");
					if (!containerID.contains("$")) {
						objectID = "0";
					} else {
						objectID = containerID;
						containerID = null;
					}
				}
				Object sI = getEnclosingValue(content, "<StartingIndex>", "</StartingIndex>");
				Object rC = getEnclosingValue(content, "<RequestedCount>", "</RequestedCount>");
				browseFlag = getEnclosingValue(content, "<BrowseFlag>", "</BrowseFlag>");
				if (sI != null)
					startingIndex = Integer.parseInt(sI.toString());
				if (rC != null)
					requestCount = Integer.parseInt(rC.toString());
				
				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.BROWSERESPONSE_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.RESULT_HEADER);
				
				response.append(HTTPXMLHelper.DIDL_HEADER);
				ArrayList<DLNAResource> files = PMS.get().getRootFolder().getDLNAResources(objectID, browseFlag!=null&&browseFlag.equals("BrowseDirectChildren"), startingIndex, requestCount);
				if (files != null) {
					for(DLNAResource uf:files) {
						if (mediaRenderer == XBOX && containerID != null)
							uf.setFakeParentId(containerID);
						response.append(uf.toString());
					}
				}
				response.append(HTTPXMLHelper.DIDL_FOOTER);
				
				response.append(HTTPXMLHelper.RESULT_FOOTER);
				response.append(CRLF);
				int filessize = 0;
				if (files != null)
					filessize = files.size();
				response.append("<NumberReturned>" + filessize + "</NumberReturned>");
				response.append(CRLF);
				DLNAResource parentFolder = null;
				if (files != null && filessize > 0)
					parentFolder = files.get(0).getParent();
				response.append("<TotalMatches>" + ((parentFolder!=null)?parentFolder.childrenNumber():filessize) + "</TotalMatches>");
				response.append(CRLF);
				response.append("<UpdateID>");
				if (parentFolder != null)
					response.append(parentFolder.getUpdateId());
				else
					response.append("1");
				response.append("</UpdateID>");
				response.append(CRLF);
				response.append(HTTPXMLHelper.BROWSERESPONSE_FOOTER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
				
			}
		}
		
		//output(output, "DATE: " + getDATE() + " GMT");
		//output(output, "LAST-MODIFIED: " + getOLDDATE() + " GMT");
		output(output, "Server: " + PMS.get().getServerName());
		
		
		if (response.length() > 0) {
			output(output, "Content-Length: " + response.length());
			output(output, "");
			if (!method.equals("HEAD"))
				output.write(response.toString().getBytes(PMS.get().getEncoding()));
		} else if (inputStream != null) {
			if (CLoverride > -1) {
				if (lowRange > 0 && highRange > 0) {
					output(output, "Content-Length: " + (highRange-lowRange+1));
				} else if (CLoverride != DLNAMediaInfo.TRANS_SIZE) // since 2.50, it's wiser not to send an arbitrary Content length,
																	// as the PS3 displays a network error and asks the last seconds of the transcoded video
					output(output, "Content-Length: " + CLoverride);
			}
			else {
				int cl = inputStream.available();
				PMS.debug("Available Content-Length: " + cl);
				output(output, "Content-Length: " + cl);
			}
			output(output, "");
			int sendB = 0;
			if (lowRange != DLNAMediaInfo.ENDFILE_POS && !method.equals("HEAD"))
				sendB = sendBytes(inputStream); //, ((lowRange > 0 && highRange > 0)?(highRange-lowRange):-1)
			PMS.debug( "Sending stream: " + sendB + " bytes of " + argument);
		} else {
			if (lowRange > 0 && highRange > 0)
				output(output, "Content-Length: " + (highRange-lowRange+1));
			else
				output(output, "Content-Length: 0");
			output(output, "");
		}
	}
	
	private void output(OutputStream output, String line) throws IOException {
		output.write((line + CRLF).getBytes());
		PMS.debug( "Wrote on socket: " + line);
	}
	
	private String getFUTUREDATE() {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date(10000000000L + System.currentTimeMillis()));
	}
	/*
	private String getDATE() {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date(System.currentTimeMillis()));
	}
	
	private String getOLDDATE() {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date(0));
	}
	*/
	

	private int sendBytes(InputStream fis) throws IOException {
		byte[] buffer = new byte[64*1024];
		int bytes = 0;
		int sendBytes = 0;
		try {
			while ((bytes = fis.read(buffer)) != -1) {
				output.write(buffer, 0, bytes);
				sendBytes += bytes;
			}
		} catch (IOException e) {
			PMS.debug("Sending stream with premature end : " + sendBytes + " bytes of " + argument + ". Reason: " + e.getMessage());
		} finally {
			fis.close();
		}
		return sendBytes;
	}
	
	private String getEnclosingValue(String content, String leftTag, String rightTag) {
		String result = null;
		int leftTagPos = content.indexOf(leftTag);
		int rightTagPos =  content.indexOf(rightTag, leftTagPos+1);
		if (leftTagPos > -1 && rightTagPos > leftTagPos) {
			result = content.substring(leftTagPos + leftTag.length(), rightTagPos);
		}
		return result;
	}
	
}
