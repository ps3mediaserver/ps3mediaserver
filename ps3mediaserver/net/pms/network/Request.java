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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.Range;
import net.pms.external.StartStopListenerDelegate;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Request extends HTTPResource {
	private static final Logger logger = LoggerFactory.getLogger(Request.class);
	private final static String CRLF = "\r\n";
	private final static String HTTP_200_OK = "HTTP/1.1 200 OK";
	private final static String HTTP_206_OK = "HTTP/1.1 206 Partial Content";
	private final static String HTTP_200_OK_10 = "HTTP/1.0 200 OK";
	private final static String HTTP_206_OK_10 = "HTTP/1.0 206 Partial Content";
	private final static String CONTENT_TYPE_UTF8 = "CONTENT-TYPE: text/xml; charset=\"utf-8\"";
	private final static String CONTENT_TYPE = "Content-Type: text/xml; charset=\"utf-8\"";
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
	private final String method;
	private final String argument;
	private String soapaction;
	private String content;
	private OutputStream output;
	private String objectID;
	private int startingIndex;
	private int requestCount;
	private String browseFlag;
	private long lowRange;
	private InputStream inputStream;
	private RendererConfiguration mediaRenderer;
	private String transferMode;
	private String contentFeatures;
	private Double timeseek;
	private Double timeRangeEnd;
	private long highRange;
	private boolean http10;

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

	public void setTimeRangeEnd(double timeRangeEnd) {
		this.timeRangeEnd = timeRangeEnd;
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

	public void answer(OutputStream output, StartStopListenerDelegate startStopListenerDelegate) throws IOException {
		this.output = output;

		long CLoverride = -2; // 0 and above are valid Content-Length values, -1 means omit
		if (lowRange != 0 || highRange != 0) {
			output(output, http10 ? HTTP_206_OK_10 : HTTP_206_OK);
		} else {
			output(output, http10 ? HTTP_200_OK_10 : HTTP_200_OK);
		}

		StringBuilder response = new StringBuilder();
		DLNAResource dlna = null;
		boolean xbox = mediaRenderer.isXBOX();

		if ((method.equals("GET") || method.equals("HEAD")) && argument.startsWith("console/")) {
			output(output, "Content-Type: text/html");
			response.append(HTMLConsole.servePage(argument.substring(8)));
		} else if ((method.equals("GET") || method.equals("HEAD")) && argument.startsWith("get/")) {
			String id = argument.substring(argument.indexOf("get/") + 4, argument.lastIndexOf("/"));
			id = id.replace("%24", "$"); // popcorn hour ?
			List<DLNAResource> files = PMS.get().getRootFolder(mediaRenderer).getDLNAResources(id, false, 0, 0, mediaRenderer);
			if (transferMode != null) {
				output(output, "TransferMode.DLNA.ORG: " + transferMode);
			}
			if (files.size() == 1) {
				String fileName = argument.substring(argument.lastIndexOf("/") + 1);
				if (fileName.startsWith("thumbnail0000")) {
					output(output, "Content-Type: " + files.get(0).getThumbnailContentType());
					output(output, "Accept-Ranges: bytes");
					output(output, "Expires: " + getFUTUREDATE() + " GMT");
					output(output, "Connection: keep-alive");
					if (mediaRenderer.isMediaParserV2()) {
						files.get(0).checkThumbnail();
					}
					inputStream = files.get(0).getThumbnailInputStream();
				} else {
					dlna = files.get(0);
					String name = dlna.getDisplayName(mediaRenderer);
					inputStream = dlna.getInputStream(Range.create(lowRange, highRange, timeseek, timeRangeEnd), mediaRenderer);
					if (inputStream == null) {
						// No inputStream indicates that transcoding / remuxing probably crashed.
						logger.error("There is no inputstream to return for " + name);
					} else {
						output(output, "Content-Type: " + getRendererMimeType(dlna.mimeType(), mediaRenderer));
					    final DLNAMediaInfo media = dlna.getMedia();
						if (media != null) {
							if (StringUtils.isNotBlank(media.container)) {
								name += " [container: " + media.container + "]";
							}
							if (StringUtils.isNotBlank(media.codecV)) {
								name += " [video: " + media.codecV + "]";
							}
						}
						PMS.get().getFrame().setStatusLine("Serving " + name);

						// Response modes:
						//   Default          - Content-Length refers to total media size.
						//   Chunked          - Content-Length refers to chunk size.
						// We use -1 for arithmetic convenience but don't send it as a value. 
						// If Content-Length < 0 we omit it, for Content-Range we use '*' to signify unspecified.
						
						boolean chunked = mediaRenderer.isChunkedTransfer();
						
						// Determine the total size. Note: when transcoding the length is
						// not known in advance, so DLNAMediaInfo.TRANS_SIZE will be returned instead.
						
						long totalsize = dlna.length();
						
						if (chunked && totalsize == DLNAMediaInfo.TRANS_SIZE) {
							// In chunked mode we try to avoid arbitrary values.
							totalsize = -1;
						}
						
						long available = inputStream.available();

						if (chunked) {
							long requested = highRange - lowRange;
							
							if (requested < 0) {
								CLoverride = (totalsize > 0 ? available : -1);
							} else {
								requested += (requested > 0 ? 1 : 0);
								CLoverride = (available < requested ? available : requested);
							}
						} else {
							CLoverride = available;
						}
						
						// Calculate the corresponding highRange (this is usually redundant).
						highRange = lowRange + CLoverride - (CLoverride > 0 ? 1 : 0);
						
						if (!chunked) {
							CLoverride = totalsize;
						}
					
						logger.trace((chunked ? "Using chunked response. " : "")  + "Available Content-Length: " + available);

						output(output, "Content-Range: bytes " + lowRange + "-" 
							+ (highRange > -1 ? highRange : "*") + "/" + (totalsize > -1 ? totalsize : "*"));

						if (contentFeatures != null) {
							output(output, "ContentFeatures.DLNA.ORG: " + dlna.getDlnaContentFeatures());
						}
						if (dlna.getPlayer() == null || xbox) {

							output(output, "Accept-Ranges: bytes");
						}
						output(output, "Connection: keep-alive");
					}
				}
			}
		} else if ((method.equals("GET") || method.equals("HEAD")) && (argument.toLowerCase().endsWith(".png") || argument.toLowerCase().endsWith(".jpg") || argument.toLowerCase().endsWith(".jpeg"))) {
			if (argument.toLowerCase().endsWith(".png")) {
				output(output, "Content-Type: image/png");
			} else {
				output(output, "Content-Type: image/jpeg");
			}
			output(output, "Accept-Ranges: bytes");
			output(output, "Connection: keep-alive");
			output(output, "Expires: " + getFUTUREDATE() + " GMT");
			inputStream = getResourceInputStream(argument);
		} else if ((method.equals("GET") || method.equals("HEAD")) && (argument.equals("description/fetch") || argument.endsWith("1.0.xml"))) {
			String profileName = PMS.getConfiguration().getProfileName();
			output(output, CONTENT_TYPE);
			output(output, "Cache-Control: no-cache");
			output(output, "Expires: 0");
			output(output, "Accept-Ranges: bytes");
			output(output, "Connection: keep-alive");
			inputStream = getResourceInputStream((argument.equals("description/fetch") ? "PMS.xml" : argument));

			if (argument.equals("description/fetch")) {
				byte b[] = new byte[inputStream.available()];
				inputStream.read(b);
				String s = new String(b);
				s = s.replace("uuid:1234567890TOTO", PMS.get().usn());//.substring(0, PMS.get().usn().length()-2));
				s = s.replace("<host>", PMS.get().getServer().getHost());
				s = s.replace("<port>", "" + PMS.get().getServer().getPort());
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
				inputStream = new ByteArrayInputStream(s.getBytes());
			}
		} else if (method.equals("POST") && (argument.contains("MS_MediaReceiverRegistrar_control") || argument.contains("mrr/control"))) {
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
			response.append(HTTPXMLHelper.SOAP_ENCODING_FOOTER);
			response.append(CRLF);
		} else if (method.equals("POST") && argument.equals("upnp/control/connection_manager")) {
			output(output, CONTENT_TYPE_UTF8);
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
			output(output, CONTENT_TYPE_UTF8);
			if (soapaction.indexOf("ContentDirectory:1#GetSystemUpdateID") > -1) {
				response.append(HTTPXMLHelper.XML_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.SOAP_ENCODING_HEADER);
				response.append(CRLF);
				response.append(HTTPXMLHelper.GETSYSTEMUPDATEID_HEADER);
				response.append(CRLF);
				response.append("<Id>").append(DLNAResource.systemUpdateId).append("</Id>");
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
				//logger.trace(content);
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

				// XBOX virtual containers ... doh
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

				List<DLNAResource> files = PMS.get().getRootFolder(mediaRenderer).getDLNAResources(objectID, browseFlag != null && browseFlag.equals("BrowseDirectChildren"), startingIndex, requestCount, mediaRenderer);
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
					// with the new parser, files are parsed and analyzed *before*
					// creating the DLNA tree, every 10 items (the ps3 asks 10 by 10),
					// so we do not know exactly the total number of items in the DLNA folder to send
					// (regular files, plus the #transcode folder, maybe the #imdb one, also files can be
					// invalidated and hidden if format is broken or encrypted, etc.).
					// let's send a fake total size to force the renderer to ask following items
					int totalCount = startingIndex + requestCount + 1; // returns 11 when 10 asked
					if (filessize - minus <= 0) // if no more elements, send startingIndex
					{
						totalCount = startingIndex;
					}
					response.append("<TotalMatches>").append(totalCount).append("</TotalMatches>");
				} 
				else if(browseFlag!=null && browseFlag.equals("BrowseDirectChildren"))
					response.append("<TotalMatches>").append(((parentFolder != null) ? parentFolder.childrenNumber() : filessize) - minus).append("</TotalMatches>");
				else
					//from upnp spec: If BrowseMetadata is specified in the BrowseFlags then TotalMatches = 1
					response.append("<TotalMatches>1</TotalMatches>");
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
				//logger.trace(response.toString());
			}
		}

		output(output, "Server: " + PMS.get().getServerName());

		if (response.length() > 0) {
			byte responseData[] = response.toString().getBytes("UTF-8");
			output(output, "Content-Length: " + responseData.length);
			output(output, "");
			if (!method.equals("HEAD")) {
				output.write(responseData);
				//logger.trace(response.toString());
			}
		} else if (inputStream != null) {
			if (CLoverride > -2) {
				// Content-Length override has been set, send or omit as appropriate
				if (CLoverride > -1 && CLoverride != DLNAMediaInfo.TRANS_SIZE) {
					// Since PS3 firmware 2.50, it is wiser not to send an arbitrary Content-Length,
					// as the PS3 will display a network error and request the last seconds of the
					// transcoded video. Better to send no Content-Length at all.
					output(output, "Content-Length: " + CLoverride);
				}
			} else {
				int cl = inputStream.available();
				logger.trace("Available Content-Length: " + cl);
				output(output, "Content-Length: " + cl);
			}

			if (timeseek > 0 && dlna != null) {
				String timeseekValue = DLNAMediaInfo.getDurationString(timeseek);
				String timetotalValue = dlna.getMedia().getDurationString();
				output(output, "TimeSeekRange.dlna.org: npt=" + timeseekValue + "-" + timetotalValue + "/" + timetotalValue);
				output(output, "X-Seek-Range: npt=" + timeseekValue + "-" + timetotalValue + "/" + timetotalValue);
			}

			output(output, "");
			int sendB = 0;
			if (lowRange != DLNAMediaInfo.ENDFILE_POS && !method.equals("HEAD")) {
				sendB = sendBytes(inputStream); //, ((lowRange > 0 && highRange > 0)?(highRange-lowRange):-1)
			}
			logger.trace("Sending stream: " + sendB + " bytes of " + argument);
			PMS.get().getFrame().setStatusLine(null);
		} else {
			if (lowRange > 0 && highRange > 0) {
				output(output, "Content-Length: " + (highRange - lowRange + 1));
			} else {
				output(output, "Content-Length: 0");
			}
			output(output, "");
		}
	}

	private void output(OutputStream output, String line) throws IOException {
		output.write((line + CRLF).getBytes("UTF-8"));
		logger.trace("Wrote on socket: " + line);
	}

	private String getFUTUREDATE() {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date(10000000000L + System.currentTimeMillis()));
	}

	// VISTA tip ?: netsh interface tcp set global autotuninglevel=disabled
	private int sendBytes(InputStream fis) throws IOException {
		byte[] buffer = new byte[32 * 1024];
		int bytes = 0;
		int sendBytes = 0;
		try {
			while ((bytes = fis.read(buffer)) != -1) {
				output.write(buffer, 0, bytes);
				sendBytes += bytes;
			}
		} catch (IOException e) {
			logger.trace("Sending stream with premature end: " + sendBytes + " bytes of " + argument + ". Reason: " + e.getMessage());
		} finally {
			fis.close();
		}
		return sendBytes;
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
