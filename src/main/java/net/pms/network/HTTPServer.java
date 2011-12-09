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
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Executors;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPServer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(HTTPServer.class);
	private final int port;
	private String hostName;
	private ServerSocketChannel serverSocketChannel;
	private ServerSocket serverSocket;
	private boolean stop;
	private Thread runnable;
	private InetAddress iafinal = null;
	private ChannelFactory factory;
	private Channel channel;
	private NetworkInterface ni = null;
	private ChannelGroup group;
	private ExecutionHandler executionHandler = null;

	public InetAddress getIafinal() {
		return iafinal;
	}

	public NetworkInterface getNi() {
		return ni;
	}

	public HTTPServer(int port) {
		this.port = port;
	}

	public boolean start() throws IOException {
		final PmsConfiguration configuration = PMS.getConfiguration();

		hostName = configuration.getServerHostname();
		InetSocketAddress address = null;
		if (hostName != null && hostName.length() > 0) {
			logger.info("Using forced address " + hostName);
			InetAddress tempIA = InetAddress.getByName(hostName);
			if (tempIA != null && ni != null && ni.equals(NetworkInterface.getByInetAddress(tempIA))) {
				address = new InetSocketAddress(tempIA, port);
			} else {
				address = new InetSocketAddress(hostName, port);
			}
		} else if (isAddressFromInterfaceFound(configuration.getNetworkInterface())) {
			logger.info("Using address " + iafinal + " found on network interface: " + ni.toString().trim().replace('\n', ' '));
			address = new InetSocketAddress(iafinal, port);
		} else {
			logger.info("Using localhost address");
			address = new InetSocketAddress(port);
		}
		logger.info("Created socket: " + address);

		if (!configuration.isHTTPEngineV2()) {
			serverSocketChannel = ServerSocketChannel.open();

			serverSocket = serverSocketChannel.socket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(address);

			if (hostName == null && iafinal != null) {
				hostName = iafinal.getHostAddress();
			} else if (hostName == null) {
				hostName = InetAddress.getLocalHost().getHostAddress();
			}

			runnable = new Thread(this, "HTTP Server");
			runnable.setDaemon(false);
			runnable.start();
		} else {
			group = new DefaultChannelGroup("myServer");
			factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

			// The OrderedMemoryAwareThreadPoolExecutor makes that all requests
			// are handled sequentially in the correct order. Without it hiccups
			// and double requests may occur. (See issue 1156)
			//
			// Setting corePoolSize to 1 because the PMS classes involved in
			// streaming are not thread safe. Multiple threads handling the
			// same request unintentionally cause ArrayOutOfBoundsExceptions
			// and NullPointerExceptions.
			executionHandler = new ExecutionHandler(
					new OrderedMemoryAwareThreadPoolExecutor(1, 1048576, 1048576));

			ServerBootstrap bootstrap = new ServerBootstrap(factory);
			HttpServerPipelineFactory pipeline = new HttpServerPipelineFactory(group, executionHandler);
			bootstrap.setPipelineFactory(pipeline);
			bootstrap.setOption("child.tcpNoDelay", true);
			bootstrap.setOption("child.keepAlive", true);
			bootstrap.setOption("reuseAddress", true);
			bootstrap.setOption("child.reuseAddress", true);
			bootstrap.setOption("child.sendBufferSize", 65536);
			bootstrap.setOption("child.receiveBufferSize", 65536);
			channel = bootstrap.bind(address);
			group.add(channel);
			if (hostName == null && iafinal != null) {
				hostName = iafinal.getHostAddress();
			} else if (hostName == null) {
				hostName = InetAddress.getLocalHost().getHostAddress();
			}
		}
		return true;
	}

	private boolean isAddressFromInterfaceFound(String networkInterfaceName) {
		NetworkConfiguration.InterfaceAssociation ia = !StringUtils.isEmpty(networkInterfaceName) ? NetworkConfiguration.getInstance()
				.getAddressForNetworkInterfaceName(networkInterfaceName)
				: null;
		if (ia == null) {
			ia = NetworkConfiguration.getInstance().getDefaultNetworkInterfaceAddress();
		}
		if (ia != null) {
			iafinal = ia.getAddr();
			ni = ia.getIface();
		}
		return ia != null;
	}

	public void stop() {
		logger.info("Stopping server on host " + hostName + " and port " + port + "...");
		if (!PMS.getConfiguration().isHTTPEngineV2()) {
			runnable.interrupt();
			runnable = null;
			try {
				serverSocket.close();
				serverSocketChannel.close();
			} catch (IOException e) {
			}
		} else if (channel != null) {
			if (group != null) {
				group.close().awaitUninterruptibly();
			}
			if (factory != null) {
				factory.releaseExternalResources();
			}
			if (executionHandler != null) {
				executionHandler.releaseExternalResources();
			}			
		}
		NetworkConfiguration.forgetConfiguration();
	}

	public void run() {
		logger.info("Starting DLNA Server on host " + hostName + " and port " + port + "...");

		while (!stop) {
			try {
				Socket socket = serverSocket.accept();
				InetAddress inetAddress = socket.getInetAddress();
				String ip = inetAddress.getHostAddress();
				// basic ipfilter: solntcev at gmail dot com
				boolean ignore = false;
				if (!PMS.getConfiguration().getIpFiltering().allowed(inetAddress)) {
					ignore = true;
					socket.close();
					logger.trace("Ignoring request from: " + ip);
				} else {
					logger.trace("Receiving a request from: " + ip);
				}
				if (!ignore) {
					RequestHandler request = new RequestHandler(socket);
					Thread thread = new Thread(request, "Request Handler");
					thread.start();
				}
			} catch (ClosedByInterruptException e) {
				stop = true;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (stop && serverSocket != null) {
						serverSocket.close();
					}
					if (stop && serverSocketChannel != null) {
						serverSocketChannel.close();
					}
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
