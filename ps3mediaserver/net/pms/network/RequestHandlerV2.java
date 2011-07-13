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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.external.StartStopListenerDelegate;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandlerV2 extends SimpleChannelUpstreamHandler {
	private static final Logger logger = LoggerFactory.getLogger(RequestHandlerV2.class);
	private volatile HttpRequest nettyRequest;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		throws Exception {
		RequestV2 request = null;

		HttpRequest nettyRequest = this.nettyRequest = (HttpRequest) e.getMessage();

		InetAddress ia = ((InetSocketAddress) e.getChannel().getRemoteAddress()).getAddress();
		RendererConfiguration renderer = RendererConfiguration.getRendererConfigurationBySocketAddress(ia);
		logger.trace("Opened request handler on socket " + e.getChannel().getRemoteAddress() + (renderer != null ? (" // " + renderer) : ""));
		PMS.get().getRegistry().disableGoToSleep();
		boolean useragentfound = renderer != null;
		String userAgentString = null;
		if (HttpMethod.GET.equals(nettyRequest.getMethod())) {
			request = new RequestV2("GET", nettyRequest.getUri().substring(1));
		} else if (HttpMethod.POST.equals(nettyRequest.getMethod())) {
			request = new RequestV2("POST", nettyRequest.getUri().substring(1));
		} else if (HttpMethod.HEAD.equals(nettyRequest.getMethod())) {
			request = new RequestV2("HEAD", nettyRequest.getUri().substring(1));
		} else {
			request = new RequestV2(nettyRequest.getMethod().getName(), nettyRequest.getUri().substring(1));
		}
		logger.trace("Request: " + nettyRequest.getProtocolVersion().getText() + " : " + request.getMethod() + " : " + request.getArgument());
		if (nettyRequest.getProtocolVersion().getMinorVersion() == 0) {
			request.setHttp10(true);
		}
		if (useragentfound) {
			PMS.get().setRendererfound(renderer);
			request.setMediaRenderer(renderer);
		}
		for (String name : nettyRequest.getHeaderNames()) {
			String headerLine = name + ": " + nettyRequest.getHeader(name);
			logger.trace("Received on socket: " + headerLine);
			if (!useragentfound && headerLine != null
				&& headerLine.toUpperCase().startsWith("USER-AGENT")
				&& request != null) {
				userAgentString = headerLine.substring(headerLine.indexOf(":") + 1).trim();
				renderer = RendererConfiguration.getRendererConfigurationByUA(userAgentString);
				if (renderer != null) {
					request.setMediaRenderer(renderer);
					renderer.associateIP(ia);
					PMS.get().setRendererfound(renderer);
					useragentfound = true;
				}
			}
			if (!useragentfound && headerLine != null && request != null) {
				RendererConfiguration alternateRenderer = RendererConfiguration.getRendererConfigurationByUAAHH(headerLine);
				if (alternateRenderer != null) {
					request.setMediaRenderer(alternateRenderer);
					alternateRenderer.associateIP(ia);
					PMS.get().setRendererfound(alternateRenderer);
					useragentfound = true;
				}
			}

			try {
				StringTokenizer s = new StringTokenizer(headerLine);
				String temp = s.nextToken();
				if (request != null && temp.toUpperCase().equals("SOAPACTION:")) {
					request.setSoapaction(s.nextToken());
				} else if (headerLine.toUpperCase().indexOf("RANGE: BYTES=") > -1) {
					String nums = headerLine.substring(
						headerLine.toUpperCase().indexOf(
						"RANGE: BYTES=") + 13).trim();
					StringTokenizer st = new StringTokenizer(nums, "-");
					if (!nums.startsWith("-")) {
						request.setLowRange(Long.parseLong(st.nextToken()));
					}
					if (!nums.startsWith("-") && !nums.endsWith("-")) {
						request.setHighRange(Long.parseLong(st.nextToken()));
					} else {
						request.setHighRange(DLNAMediaInfo.TRANS_SIZE);
					}
				} else if (headerLine.toLowerCase().indexOf("transfermode.dlna.org:") > -1) {
					request.setTransferMode(headerLine.substring(headerLine.toLowerCase().indexOf("transfermode.dlna.org:") + 22).trim());
				} else if (headerLine.toLowerCase().indexOf("getcontentfeatures.dlna.org:") > -1) {
					request.setContentFeatures(headerLine.substring(headerLine.toLowerCase().indexOf("getcontentfeatures.dlna.org:") + 28).trim());
				} else if (headerLine.toUpperCase().indexOf(
					"TIMESEEKRANGE.DLNA.ORG: NPT=") > -1) { // firmware
					// 2.50+
					String timeseek = headerLine.substring(headerLine.toUpperCase().indexOf(
						"TIMESEEKRANGE.DLNA.ORG: NPT=") + 28);
					if (timeseek.endsWith("-")) {
						timeseek = timeseek.substring(0, timeseek.length() - 1);
					} else if (timeseek.indexOf("-") > -1) {
						timeseek = timeseek.substring(0, timeseek.indexOf("-"));
					}
					request.setTimeseek(Double.parseDouble(timeseek));
				} else if (headerLine.toUpperCase().indexOf(
					"TIMESEEKRANGE.DLNA.ORG : NPT=") > -1) { // firmware
					// 2.40
					String timeseek = headerLine.substring(headerLine.toUpperCase().indexOf(
						"TIMESEEKRANGE.DLNA.ORG : NPT=") + 29);
					if (timeseek.endsWith("-")) {
						timeseek = timeseek.substring(0, timeseek.length() - 1);
					} else if (timeseek.indexOf("-") > -1) {
						timeseek = timeseek.substring(0, timeseek.indexOf("-"));
					}
					request.setTimeseek(Double.parseDouble(timeseek));
				}
			} catch (Exception ee) {
				logger.error("Error parsing HTTP headers", ee);
			}

		}

		// if client not recognized, take a default renderer config
		if (request != null) {
			if (request.getMediaRenderer() == null) {
				request.setMediaRenderer(RendererConfiguration.getDefaultConf());
				logger.trace("Using default media renderer " + request.getMediaRenderer().getRendererName()); //$NON-NLS-1$
				
				if (userAgentString != null && !userAgentString.equals("FDSSDP")) { //$NON-NLS-1$
					// we have found an unknown renderer
					logger.info("Media renderer was not recognized. HTTP User-Agent: " + userAgentString); //$NON-NLS-1$
					PMS.get().setRendererfound(request.getMediaRenderer());
				}
			} else {
				if (userAgentString != null) {
					logger.trace("HTTP User-Agent: " + userAgentString); //$NON-NLS-1$
				}
				logger.trace("Recognized media renderer " + request.getMediaRenderer().getRendererName()); //$NON-NLS-1$
			}
		}
		
		if (HttpHeaders.getContentLength(nettyRequest) > 0) {
			byte data[] = new byte[(int) HttpHeaders.getContentLength(nettyRequest)];
			ChannelBuffer content = nettyRequest.getContent();
			content.readBytes(data);
			request.setTextContent(new String(data, "UTF-8"));
		}

		if (request != null) {
			logger.trace("HTTP: " + request.getArgument() + " / "
				+ request.getLowRange() + "-" + request.getHighRange());
		}

		writeResponse(e, request, ia);
	}

	private void writeResponse(MessageEvent e, RequestV2 request, InetAddress ia) {
		// Decide whether to close the connection or not.
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(nettyRequest.getHeader(HttpHeaders.Names.CONNECTION))
			|| nettyRequest.getProtocolVersion().equals(
			HttpVersion.HTTP_1_0)
			&& !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(nettyRequest.getHeader(HttpHeaders.Names.CONNECTION));

		// Build the response object.
		HttpResponse response = null;
		if (request.getLowRange() > 0 || request.getHighRange() > 0) {
			response = new DefaultHttpResponse(
				/*request.isHttp10() ? HttpVersion.HTTP_1_0
				: */HttpVersion.HTTP_1_1,
				HttpResponseStatus.PARTIAL_CONTENT);
		} else {
			response = new DefaultHttpResponse(
				/*request.isHttp10() ? HttpVersion.HTTP_1_0
				: */HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		}

		StartStopListenerDelegate startStopListenerDelegate = new StartStopListenerDelegate(ia.getHostAddress());

		try {
			request.answer(response, e, close, startStopListenerDelegate);
		} catch (IOException e1) {
			logger.trace("HTTP request V2 IO error: " + e1.getMessage());
			// note: we don't call stop() here in a finally block as
			// answer() is non-blocking. we only (may) need to call it
			// here in the case of an exception. it's a no-op if it's
			// already been called
			startStopListenerDelegate.stop();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
		throws Exception {
		Channel ch = e.getChannel();
		Throwable cause = e.getCause();
		if (cause instanceof TooLongFrameException) {
			sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		if (cause != null && !cause.getClass().equals(ClosedChannelException.class) && !cause.getClass().equals(IOException.class)) {
			cause.printStackTrace();
		}
		if (ch.isConnected()) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
		e.getChannel().close();
	}

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(
			HttpVersion.HTTP_1_1, status);
		response.setHeader(
			HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.setContent(ChannelBuffers.copiedBuffer(
			"Failure: " + status.toString() + "\r\n", Charset.forName("UTF-8")));

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
		throws Exception {
		// as seen in http://www.jboss.org/netty/community.html#nabble-td2423020
		super.channelOpen(ctx, e);
		if (HTTPServer.group != null) {
			HTTPServer.group.add(ctx.getChannel());
		}
	}
}
