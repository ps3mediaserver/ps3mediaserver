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

public class HTTPXMLHelper {
	
	public final static String CRLF = "\r\n";
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	public static final String SOAP_ENCODING_HEADER = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" + CRLF + "<s:Body>";
	public static final String SOAP_ENCODING_FOOTER = "</s:Body>" + CRLF + "</s:Envelope>";
	public static final String GETSYSTEMUPDATEID_HEADER = "<u:GetSystemUpdateIDResponse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:1\">";
	public static final String GETSYSTEMUPDATEID_FOOTER = "</u:GetSystemUpdateIDResponse>";
	public static final String BROWSERESPONSE_HEADER = "<u:BrowseResponse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:1\">";
	public static final String BROWSERESPONSE_FOOTER = "</u:BrowseResponse>";
	public static final String RESULT_HEADER = "<Result>";
	public static final String RESULT_FOOTER = "</Result>";
	
	public static final String DIDL_HEADER = "&lt;DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\"&gt;";
	public static final String DIDL_FOOTER = "&lt;/DIDL-Lite&gt;";
	
	public static final String XBOX_1 = "<u:IsValidatedResponse xmlns:u=\"urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1\">" + CRLF + "<Result>1</Result>" + CRLF + "</u:IsValidatedResponse>";
	public static final String XBOX_2 = "<u:IsAuthorizedResponse xmlns:u=\"urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1\">" + CRLF + "<Result>1</Result>" + CRLF + "</u:IsAuthorizedResponse>";
}
