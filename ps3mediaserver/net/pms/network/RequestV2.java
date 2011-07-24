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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.external.StartStopListenerDelegate;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.stream.ChunkedStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles all forms of incoming requests by constructing a proper response. 
 */
public class RequestV2 extends HTTPResource {
	private static final Logger logger = LoggerFactory.getLogger(RequestV2.class);
	private final static String CRLF = "\r\n";
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
	private static int BUFFER_SIZE = 8 * 1024;
	int sendB = 0;
	private String method;
	private String soapaction;
	private String content;
	private String objectID;
	private int startingIndex;
	private int requestCount;
	private String browseFlag;
	private InputStream inputStream;
	private RendererConfiguration mediaRenderer;
	private String transferMode;
	private String contentFeatures;
	private double timeseek;
	private boolean http10;
	
	/**
	 * A {@link String} that contains the argument with which this {@link RequestV2} was
	 * created. It contains a command, a unique resource id and a resource name, all
	 * separated by slashes. For example: "get/0$0$2$17/big_buck_bunny_1080p_h264.mov" or
	 * "get/0$0$2$13/thumbnail0000Sintel.2010.1080p.mkv"
	 */
	private String argument;
	
	/**
	 * When sending an input stream, the lowRange indicates which byte to start from.  
	 */
	private long lowRange;

	/**
	 * When sending an input stream, the highRange indicates which byte to stop at.  
	 */
	private long highRange;
	

	public RendererConfiguration getMediaRenderer() {
		return mediaRenderer;
	}

	public void setMediaRenderer(RendererConfiguration mediaRenderer) {
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

	public String getTransferMode() {
		return transferMode;
	}

	public void setTransferMode(String transferMode) {
		this.transferMode = transferMode;
	}

	public String getContentFeatures() {
		return contentFeatures;
	}

	public void setContentFeatures(String contentFeatures) {
		this.contentFeatures = contentFeatures;
	}

	public double getTimeseek() {
		return timeseek;
	}

	public void setTimeseek(double timeseek) {
		this.timeseek = timeseek;
	}

	public long getHighRange() {
		return highRange;
	}

	public void setHighRange(long highRange) {
		this.highRange = highRange;
	}

	public boolean isHttp10() {
		return http10;
	}

	public void setHttp10(boolean http10) {
		this.http10 = http10;
	}

	public RequestV2(String method, String argument) {
		this.method = method;
		this.argument = argument;
		this.lowRange = 0;
		this.highRange = 0;
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

	
	/**
	 * Retrieves the HTTP method with which this {@link RequestV2} was created.
	 * @return The (@link String} containing the HTTP method.
	 */
	public String getMethod() {
		return method;
	}

	
	/**
	 * Retrieves the argument with which this {@link RequestV2} was created. It contains
	 * a command, a unique resource id and a resource name, all separated by slashes. For
	 * example: "get/0$0$2$17/big_buck_bunny_1080p_h264.mov" or "get/0$0$2$13/thumbnail0000Sintel.2010.1080p.mkv"
	 * @return The {@link String} containing the argument.
	 */
	public String getArgument() {
		return argument;
	}

	
	/**
	 * Construct a proper HTTP response to a received request. After the response has been
	 * created, it is sent and the resulting {@link ChannelFuture} object is returned.
	 * See <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">RFC-2616</a>
	 * for HTTP header field definitions. 
	 * @param output The {@link HttpResponse} object that will be used to construct the response.
	 * @param event The {@link MessageEvent} object used to communicate with the client that sent
	 * 			the request.
	 * @param close Set to true to close the channel after sending the response. By default the
	 * 			channel is not closed after sending.
	 * @param startStopListenerDelegate The {@link StartStopListenerDelegate} object that is used
	 * 			to notify plugins that the {@link DNLAResource} is about to start playing.
	 * @return The {@link ChannelFuture} object via which the response was sent.
	 * @throws IOException
	 */
	public ChannelFuture answer(HttpResponse output, MessageEvent event, final boolean close,
			final StartStopListenerDelegate startStopListenerDelegate) throws IOException {
		
		ChannelFuture future = null;
		long CLoverride = -1;
		StringBuilder response = new StringBuilder();
		DLNAResource dlna = null;
		boolean xbox = mediaRenderer.isXBOX();

		if ((method.equals("GET") || method.equals("HEAD")) && argument.startsWith("console/")) {
			// Request to output a page to the HTLM console.
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
			response.append(HTMLConsole.servePage(argument.substring(8)));
		} else if ((method.equals("GET") || method.equals("HEAD")) && argument.startsWith("get/")) {
			// Request to retrieve a file
			
			// Extract the resource id from the argument string.
			String id = argument.substring(4, argument.lastIndexOf("/"));
			
			// Some clients escape the separators in their request, unescape them.
			id = id.replace("%24", "$");
			
			// Retrieve the DLNAresource itself .
			ArrayList<DLNAResource> files = PMS.get().getRootFolder(mediaRenderer).getDLNAResources(id, false, 0, 0, mediaRenderer);

			if (transferMode != null) {
				output.setHeader("TransferMode.DLNA.ORG", transferMode);
			}

			if (files.size() == 1) {
				// DNLAresource was found.
				dlna = files.get(0);
				String fileName = argument.substring(argument.lastIndexOf("/") + 1);
				
				if (fileName.startsWith("thumbnail0000")) {
					// This a is request for a thumbnail file
					output.setHeader(HttpHeaders.Names.CONTENT_TYPE, files.get(0).getThumbnailContentType());
					output.setHeader(HttpHeaders.Names.ACCEPT_RANGES, "bytes");
					output.setHeader(HttpHeaders.Names.EXPIRES, getFUTUREDATE() + " GMT");
					output.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
					
					if (mediaRenderer.isMediaParserV2()) {
						dlna.checkThumbnail();
					}
					
					inputStream = dlna.getThumbnailInputStream();
				} else {
					// This is a request for a regular file.
					inputStream = dlna.getInputStream(lowRange, highRange, timeseek, mediaRenderer);
					
					if (inputStream != null) {
						// Notify plugins that the DLNAresource is about to start playing
						startStopListenerDelegate.start(dlna);
					}
					
					output.setHeader(HttpHeaders.Names.CONTENT_TYPE, getRendererMimeType(files.get(0).mimeType(), mediaRenderer));
					String name = dlna.getDisplayName(mediaRenderer);

					if (dlna.media != null) {
						if (StringUtils.isNotBlank(dlna.media.container)) {
							name += " [container: " + dlna.media.container + "]";
						}
						
						if (StringUtils.isNotBlank(dlna.media.codecV)) {
							name += " [video: " + dlna.media.codecV + "]";
						}
					}
					
					PMS.get().getFrame().setStatusLine("Serving " + name);
					
					// Determine the total size and Content-Length override. Note: when transcoding the length is
					// not known in advance, so DLNAMediaInfo.TRANS_SIZE will be returned instead.
					long totalsize = files.get(0).length(mediaRenderer);
					CLoverride = totalsize;

					// FIXME: Not sure this will ever happen because DLNAResources usually return a length > -1.
					if (totalsize == -1) {
						// Cannot determine total size, send everything that is available.
						lowRange = 0;
						totalsize = inputStream.available();
						highRange = totalsize - 1;
					}
					
					// Sanity checks
					if (totalsize <= 1) {
						// TODO: Nothing to stream?
					}
					
					if (highRange >= totalsize) {
						highRange = totalsize - 1;
					}
						
					if (lowRange >= highRange) {
						lowRange = highRange - 1;
					}
					
					if (contentFeatures != null) {
						output.setHeader("ContentFeatures.DLNA.ORG", files.get(0).getDlnaContentFeatures());
					}
					
					output.setHeader(HttpHeaders.Names.ACCEPT_RANGES, "bytes");
					output.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
				}
			}
		} else if ((method.equals("GET") || method.equals("HEAD")) && (argument.toLowerCase().endsWith(".png") || argument.toLowerCase().endsWith(".jpg") || argument.toLowerCase().endsWith(".jpeg"))) {
			if (argument.toLowerCase().endsWith(".png")) {
				output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "image/png");
			} else {
				output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "image/jpeg");
			}
			output.setHeader(HttpHeaders.Names.ACCEPT_RANGES, "bytes");
			output.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
			output.setHeader(HttpHeaders.Names.EXPIRES, getFUTUREDATE() + " GMT");
			inputStream = getResourceInputStream(argument);
		} else if ((method.equals("GET") || method.equals("HEAD")) && (argument.equals("description/fetch") || argument.endsWith("1.0.xml"))) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=\"utf-8\"");
			output.setHeader(HttpHeaders.Names.CACHE_CONTROL, "no-cache");
			output.setHeader(HttpHeaders.Names.EXPIRES, "0");
			output.setHeader(HttpHeaders.Names.ACCEPT_RANGES, "bytes");
			output.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
			inputStream = getResourceInputStream((argument.equals("description/fetch") ? "PMS.xml" : argument));
			if (argument.equals("description/fetch")) {
				byte b[] = new byte[inputStream.available()];
				inputStream.read(b);
				String s = new String(b);
				s = s.replace("uuid:1234567890TOTO", PMS.get().usn());//.substring(0, PMS.get().usn().length()-2));
				String profileName = PMS.getConfiguration().getProfileName();
				if (PMS.get().getServer().getHost() != null) {
					s = s.replace("<host>", PMS.get().getServer().getHost());
					s = s.replace("<port>", "" + PMS.get().getServer().getPort());
				}
				if (xbox) {
					logger.debug("DLNA changes for Xbox360");
					s = s.replace("PS3 Media Server", "PS3 Media Server [" + profileName + "] : Windows Media Connect");
					s = s.replace("<modelName>PMS</modelName>", "<modelName>Windows Media Connect</modelName>");
					s = s.replace("<serviceList>", "<serviceList>" + CRLF + "<service>" + CRLF
						+ "<serviceType>urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1</serviceType>" + CRLF
						+ "<serviceId>urn:microsoft.com:serviceId:X_MS_MediaReceiverRegistrar</serviceId>" + CRLF
						+ "<SCPDURL>/upnp/mrr/scpd</SCPDURL>" + CRLF
						+ "<controlURL>/upnp/mrr/control</controlURL>" + CRLF
						+ "</service>" + CRLF);
				} else {
					s = s.replace("PS3 Media Server", "PS3 Media Server [" + profileName + "]");
				}

				if (!mediaRenderer.isPS3()) {
					// hacky stuff. replace the png icon by a jpeg one. Like mpeg2 remux,
					// really need a proper format compatibility list by renderer
					s = s.replace("<mimetype>image/png</mimetype>", "<mimetype>image/jpeg</mimetype>");
					s = s.replace("/images/thumbnail-256.png", "/images/thumbnail-120.jpg");
					s = s.replace(">256<", ">120<");
				}
				response.append(s);
				inputStream = null;
			}
		} else if (method.equals("POST") && (argument.contains("MS_MediaReceiverRegistrar_control") || argument.contains("mrr/control"))) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=\"utf-8\"");
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
		} else if (method.equals("POST") && argument.equals("upnp/control/connection_manager")) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=\"utf-8\"");
			if (soapaction.indexOf("ConnectionManager:1#GetProtocolInfo") > -1) {
				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.PROTOCOLINFO_RESPONSE);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
			}
		} else if (method.equals("POST") && argument.equals("upnp/control/content_directory")) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=\"utf-8\"");
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
			} else if (soapaction.indexOf("ContentDirectory:1#GetSortCapabilities") > -1) {
				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SORTCAPS_RESPONSE);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
			} else if (soapaction.indexOf("ContentDirectory:1#GetSearchCapabilities") > -1) {
				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SEARCHCAPS_RESPONSE);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
			} else if (soapaction.contains("ContentDirectory:1#Browse") || soapaction.contains("ContentDirectory:1#Search")) {
				objectID = getEnclosingValue(content, "<ObjectID>", "</ObjectID>");
				String containerID = null;
				if ((objectID == null || objectID.length() == 0) && xbox) {
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

				if (sI != null) {
					startingIndex = Integer.parseInt(sI.toString());
				}

				if (rC != null) {
					requestCount = Integer.parseInt(rC.toString());
				}

				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);

				if (soapaction.contains("ContentDirectory:1#Search")) {
					response.append(HTTPXMLHelper.SEARCHRESPONSE_HEADER);
				} else {
					response.append(HTTPXMLHelper.BROWSERESPONSE_HEADER);
				}

				response.append(CRLF);
				response.append(HTTPXMLHelper.RESULT_HEADER);
				response.append(HTTPXMLHelper.DIDL_HEADER);

				if (soapaction.contains("ContentDirectory:1#Search")) {
					browseFlag = "BrowseDirectChildren";
				}

				// XBOX virtual containers ... d'oh!
				String searchCriteria = null;
				if (xbox && PMS.getConfiguration().getUseCache() && PMS.get().getLibrary() != null && containerID != null) {
					if (containerID.equals("7") && PMS.get().getLibrary().getAlbumFolder() != null) {
						objectID = PMS.get().getLibrary().getAlbumFolder().getId();
					} else if (containerID.equals("6") && PMS.get().getLibrary().getArtistFolder() != null) {
						objectID = PMS.get().getLibrary().getArtistFolder().getId();
					} else if (containerID.equals("5") && PMS.get().getLibrary().getGenreFolder() != null) {
						objectID = PMS.get().getLibrary().getGenreFolder().getId();
					} else if (containerID.equals("F") && PMS.get().getLibrary().getPlaylistFolder() != null) {
						objectID = PMS.get().getLibrary().getPlaylistFolder().getId();
					} else if (containerID.equals("4") && PMS.get().getLibrary().getAllFolder() != null) {
						objectID = PMS.get().getLibrary().getAllFolder().getId();
					} else if (containerID.equals("1")) {
						String artist = getEnclosingValue(content, "upnp:artist = &quot;", "&quot;)");
						if (artist != null) {
							objectID = PMS.get().getLibrary().getArtistFolder().getId();
							searchCriteria = artist;
						}
					}
				}

				ArrayList<DLNAResource> files = PMS.get().getRootFolder(mediaRenderer).getDLNAResources(objectID, browseFlag != null && browseFlag.equals("BrowseDirectChildren"), startingIndex, requestCount, mediaRenderer);
				if (searchCriteria != null && files != null) {
					for (int i = files.size() - 1; i >= 0; i--) {
						if (!files.get(i).getName().equals(searchCriteria)) {
							files.remove(i);
						}
					}
					if (files.size() > 0) {
						files = files.get(0).getChildren();
					}
				}

				int minus = 0;
				if (files != null) {
					for (DLNAResource uf : files) {
						if (xbox && containerID != null) {
							uf.setFakeParentId(containerID);
						}
						if (uf.isCompatible(mediaRenderer) && (uf.getPlayer() == null || uf.getPlayer().isPlayerCompatible(mediaRenderer))) {
							response.append(uf.toString(mediaRenderer));
						} else {
							minus++;
						}
					}
				}

				response.append(HTTPXMLHelper.DIDL_FOOTER);
				response.append(HTTPXMLHelper.RESULT_FOOTER);
				response.append(CRLF);
				int filessize = 0;
				if (files != null) {
					filessize = files.size();
				}
				response.append("<NumberReturned>").append(filessize - minus).append("</NumberReturned>");
				response.append(CRLF);
				DLNAResource parentFolder = null;
				if (files != null && filessize > 0) {
					parentFolder = files.get(0).getParent();
				}
				if (browseFlag != null && browseFlag.equals("BrowseDirectChildren") && mediaRenderer.isMediaParserV2() && mediaRenderer.isDLNATreeHack()) {
					// with the new parser, files are parsed and analyzed *before* creating the DLNA tree,
					// every 10 items (the ps3 asks 10 by 10),
					// so we do not know exactly the total number of items in the DLNA folder to send
					// (regular files, plus the #transcode folder, maybe the #imdb one, also files can be
					// invalidated and hidden if format is broken or encrypted, etc.).
					// let's send a fake total size to force the renderer to ask following items
					int totalCount = startingIndex + requestCount + 1; // returns 11 when 10 asked
					if (filessize - minus <= 0) // if no more elements, send the startingIndex
					{
						totalCount = startingIndex;
					}
					response.append("<TotalMatches>").append(totalCount).append("</TotalMatches>");
				} else if (browseFlag != null && browseFlag.equals("BrowseDirectChildren")) {
					response.append("<TotalMatches>").append(((parentFolder != null) ? parentFolder.childrenNumber() : filessize) - minus).append("</TotalMatches>");
				} else { //from upnp spec: If BrowseMetadata is specified in the BrowseFlags then TotalMatches = 1
					response.append("<TotalMatches>1</TotalMatches>");
				}
				response.append(CRLF);
				response.append("<UpdateID>");
				if (parentFolder != null) {
					response.append(parentFolder.getUpdateId());
				} else {
					response.append("1");
				}
				response.append("</UpdateID>");
				response.append(CRLF);
				if (soapaction.contains("ContentDirectory:1#Search")) {
					response.append(HTTPXMLHelper.SEARCHRESPONSE_FOOTER);
				} else {
					response.append(HTTPXMLHelper.BROWSERESPONSE_FOOTER);
				}
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
				response.append(CRLF);
			}
		} else if (method.equals("SUBSCRIBE")) {
			output.setHeader("SID", PMS.get().usn());
			output.setHeader("TIMEOUT", "Second-1800");
		} else if (method.equals("NOTIFY")) {
			output.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml");
			output.setHeader("NT", "upnp:event");
			output.setHeader("NTS", "upnp:propchange");
			output.setHeader("SID", PMS.get().usn());
			output.setHeader("SEQ", "0");
			response.append("<e:propertyset xmlns:e=\"urn:schemas-upnp-org:event-1-0\">");
			response.append("<e:property>");
			response.append("<TransferIDs></TransferIDs>");
			response.append("</e:property>");
			response.append("<e:property>");
			response.append("<ContainerUpdateIDs></ContainerUpdateIDs>");
			response.append("</e:property>");
			response.append("<e:property>");
			response.append("<SystemUpdateID>").append(DLNAResource.systemUpdateId).append("</SystemUpdateID>");
			response.append("</e:property>");
			response.append("</e:propertyset>");
		}

		output.setHeader("Server", PMS.get().getServerName());

		if (response.length() > 0) {
			// A response message was constructed; convert it to data ready to be sent.
			byte responseData[] = response.toString().getBytes("UTF-8");
			output.setStatus(HttpResponseStatus.OK);
			output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "" + responseData.length);

			// HEAD requests only require headers to be set.
			if (!method.equals("HEAD")) {
				// Not a HEAD request, so set the contents of the response.
				ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseData);
				output.setContent(buf);
			}
			
			// Send the response to the client.
			future = event.getChannel().write(output);
			
			if (close) {
				// Close the channel after the response is sent.
				future.addListener(ChannelFutureListener.CLOSE);
			}
		} else if (inputStream != null) {
			// There is an input stream to send as a response.

			if (CLoverride > -1) {
				// Content-Length override has been set, so use it.
				if (lowRange == 0 && highRange == 0) {
					// lowRange and highRange have not specifically been set.
					
					// DLNAMediaInfo.TRANS_SIZE is a marker value used by DLNA resources when their
					// length is not exactly known, e.g. when transcoding.
					if (CLoverride != DLNAMediaInfo.TRANS_SIZE) {
						// Send the determined content length
						output.setStatus(HttpResponseStatus.OK);
						output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "" + CLoverride);
					} else {
						// Since PS3 firmware 2.50, it is wiser not to send an arbitrary Content-Length,
						// as the PS3 will display a network error and request the last seconds of the
						// transcoded video. Better to send no Content-Length at all.
						output.setStatus(HttpResponseStatus.OK);
					}
				} else if (lowRange == 0 && highRange == CLoverride - 1) {
					// lowRange and highRange have been set and indicate the full range.
					// Send full content instead of a range of bytes.
					output.setStatus(HttpResponseStatus.OK);
					output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "" + CLoverride);
				} else {
					// lowRange and highRange have been set, send a range of bytes.
					// See http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.16 for details.
					output.setStatus(HttpResponseStatus.PARTIAL_CONTENT);
					output.setHeader(HttpHeaders.Names.CONTENT_RANGE, "bytes " + lowRange + "-" + highRange + "/" + CLoverride);
					output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "" + (highRange - lowRange + 1));
				}
			} else {
				// No Content-Length override set, use the available length.
				int cl = inputStream.available();
				output.setStatus(HttpResponseStatus.OK);
				output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "" + cl);
			}

			if (timeseek > 0 && dlna != null) {
				// Add timeseek information headers.
				String timeseekValue = DLNAMediaInfo.getDurationString(timeseek);
				String timetotalValue = dlna.media.duration;
				output.setHeader("TimeSeekRange.dlna.org", "npt=" + timeseekValue + "-" + timetotalValue + "/" + timetotalValue);
				output.setHeader("X-Seek-Range", "npt=" + timeseekValue + "-" + timetotalValue + "/" + timetotalValue);
			}
			
			// Send the response headers to the client.
			future = event.getChannel().write(output);

			if (!method.equals("HEAD")) {
				// Send the response body to the client in chunks.
				ChannelFuture chunkWriteFuture = event.getChannel().write(new ChunkedStream(inputStream, BUFFER_SIZE));

				// Add a listener to clean up after sending the entire response body.
				chunkWriteFuture.addListener(new ChannelFutureListener() {
					public void operationComplete(ChannelFuture future) {
						try {
							PMS.get().getRegistry().reenableGoToSleep();
							inputStream.close();
						} catch (IOException e) {
						}

						// Always close the channel after the response is sent because of
						// a freeze at the end of video when the channel is not closed.
						future.getChannel().close();
						startStopListenerDelegate.stop();
					}
				});
			} else {
				// HEAD method, so simply clean up after the response was sent.
				try {
					PMS.get().getRegistry().reenableGoToSleep();
					inputStream.close();
				} catch (IOException ioe) {
				}
				
				if (close) {
					// Close the channel after the response is sent
					future.addListener(ChannelFutureListener.CLOSE);
				}
				
				startStopListenerDelegate.stop();
			}
		} else {
			// No response data and no input stream. Seems we are merely serving up headers.
			output.setStatus(HttpResponseStatus.OK);
			output.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "0");
			
			// Send the response headers to the client.
			future = event.getChannel().write(output);

			if (close) {
				// Close the channel after the response is sent.
				future.addListener(ChannelFutureListener.CLOSE);
			}
		}
		
		// Log trace information
		logger.trace("Sent to socket: " + output.getStatus().toString());
		Iterator<String> it = output.getHeaderNames().iterator();

		while (it.hasNext()) {
			String headerName = it.next();
			logger.trace("Sent to socket: " + headerName + ": " + output.getHeader(headerName));
		}

		return future;
	}

	/**
	 * Returns a date somewhere in the far future.
	 * @return The {@link String} containing the date
	 */
	private String getFUTUREDATE() {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date(10000000000L + System.currentTimeMillis()));
	}

	private String getEnclosingValue(String content, String leftTag, String rightTag) {
		String result = null;
		int leftTagPos = content.indexOf(leftTag);
		int rightTagPos = content.indexOf(rightTag, leftTagPos + 1);
		if (leftTagPos > -1 && rightTagPos > leftTagPos) {
			result = content.substring(leftTagPos + leftTag.length(), rightTagPos);
		}
		return result;
	}
}
