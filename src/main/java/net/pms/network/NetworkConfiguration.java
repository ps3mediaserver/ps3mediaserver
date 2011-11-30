/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2011  G.Zsombor
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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.pms.PMS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class stores the network configuration : which network contains which IP-s, etc.
 * 
 * @author zsombor
 *
 */
public class NetworkConfiguration {

	public static class InterfaceAssociation {
		String parentName;
		InetAddress addr;
		NetworkInterface iface;

		public InterfaceAssociation(InetAddress addr, NetworkInterface iface, String parentName) {
			super();
			this.addr = addr;
			this.iface = iface;
			this.parentName = parentName;
		}

		/**
		 * @return the addr
		 */
		public InetAddress getAddr() {
			return addr;
		}

		/**
		 * @return the iface
		 */
		public NetworkInterface getIface() {
			return iface;
		}

		public String getParentName() {
			return parentName;
		}

		public String getShortName() {
			return iface.getName();
		}

		public String getDisplayName() {
			return iface.getDisplayName().trim() + (addr != null ? " (" + addr.getHostAddress() + ")" : "");
		}
	}

	private final static Logger LOG = LoggerFactory.getLogger(NetworkConfiguration.class);

	List<InterfaceAssociation> interfaces = new ArrayList<InterfaceAssociation>();
	Map<String, InterfaceAssociation> mainAddress = new HashMap<String, InterfaceAssociation>();
	Map<String, Set<InetAddress>> addressMap = new HashMap<String, Set<InetAddress>>();
	List<String> skipNetworkInterfaces = PMS.getConfiguration().getSkipNetworkInterfaces();

	public NetworkConfiguration() {
		try {
			checkNetworkInterface(NetworkInterface.getNetworkInterfaces(), null);
		} catch (SocketException e) {
			LOG.error("Inspecting the network is failed:" + e.getMessage(), e);
		}
	}

	/**
	 * Collect the all relevant address for the given network interface, and return it.
	 * @param netIface
	 * @return
	 */
	private Set<InetAddress> addAvailableAddresses(NetworkInterface netIface) {
		Set<InetAddress> addrSet = new HashSet<InetAddress>();
		for (InterfaceAddress ia : netIface.getInterfaceAddresses()) {
			InetAddress address = ia.getAddress();
			if (isRelevantAddress(address)) {
				addrSet.add(ia.getAddress());
			}
		}
		addressMap.put(netIface.getName(), addrSet);
		return addrSet;
	}

	/**
	 * 
	 * @param address
	 * @return true if it's not ipv6 address, and not loopback
	 */
	private boolean isRelevantAddress(InetAddress address) {
		return !(address instanceof Inet6Address || address.isLoopbackAddress());
	}

	private void checkNetworkInterface(Enumeration<NetworkInterface> enm, String parentName) {
		while (enm.hasMoreElements()) {
			NetworkInterface ni = enm.nextElement();
			if (!skipNetworkInterface(ni.getName(), ni.getDisplayName())) {
				// check for interface has at least one ip address.
				checkNetworkInterface(ni, parentName);
			}
		}
	}

	private Set<InetAddress> getAllAvailableAddresses(Enumeration<NetworkInterface> en) {
		Set<InetAddress> addrSet = new HashSet<InetAddress>();
		while (en.hasMoreElements()) {
			NetworkInterface element = en.nextElement();
			Set<InetAddress> set = addressMap.get(element.getName());
			if (set != null) {
				addrSet.addAll(set);
			}
		}
		return addrSet;
	}

	private void checkNetworkInterface(NetworkInterface netIface, String parentName) {
		LOG.debug("checking " + netIface.getName());
		addAvailableAddresses(netIface);
		checkNetworkInterface(netIface.getSubInterfaces(), netIface.getName());
		// create address / iface pairs which are not IP address of the child iface too
		Set<InetAddress> subAddress = getAllAvailableAddresses(netIface.getSubInterfaces());
		boolean foundAddress = false;
		for (InterfaceAddress ifaceAddr : netIface.getInterfaceAddresses()) {
			InetAddress address = ifaceAddr.getAddress();
			if (isRelevantAddress(address)) {
				if (!subAddress.contains(address)) {
					LOG.debug("found " + netIface.getName() + " -> " + address.getHostAddress());
					final InterfaceAssociation ni = new InterfaceAssociation(address, netIface, parentName);
					interfaces.add(ni);
					mainAddress.put(netIface.getName(), ni);
					foundAddress = true;
				}
			}
		}
		if (!foundAddress) {
			interfaces.add(new InterfaceAssociation(null, netIface, parentName));
			LOG.info("found " + netIface.getName() + ", without valid address");
		}
	}

	/**
	 * 
	 * @return the list of interface names
	 */
	public List<String> getKeys() {
		List<String> result = new ArrayList<String>(interfaces.size());
		for (InterfaceAssociation i : interfaces) {
			result.add(i.getShortName());
		}
		return result;
	}

	/**
	 * 
	 * @return the user friendly name of the interfaces, with the IP address 
	 */
	public List<String> getDisplayNames() {
		List<String> result = new ArrayList<String>(interfaces.size());
		for (InterfaceAssociation i : interfaces) {
			result.add(i.getDisplayName());
		}
		return result;
	}

	/**
	 * 
	 * @return the first NetworkInterface which doesn't have a parent, so defaulting will avoid using alias interfaces
	 */
	public InterfaceAssociation getDefaultNetworkInterfaceAddress() {
		if (interfaces.size() > 0) {
			InterfaceAssociation association = interfaces.get(0);
			if (association.getParentName() != null) {
				return getAddressForNetworkInterfaceName(association.getParentName());
			} else {
				return association;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param name the interface name
	 * @return the default IP address, and network interface for the given name
	 */
	public InterfaceAssociation getAddressForNetworkInterfaceName(String name) {
		return mainAddress.get(name);
	}

	private boolean skipNetworkInterface(String name, String displayName) {
		for (String current : skipNetworkInterfaces) {
			if (name != null && lcontains(name, current)) {
				return true;
			}

			if (displayName != null && lcontains(displayName, current)) {
				return true;
			}
		}
		return false;
	}

	private boolean lcontains(String txt, String substr) {
		return txt.toLowerCase().contains(substr);
	}

	private static NetworkConfiguration config;

	/**
	 * @return a configured NetworkConfiguration object
	 */
	public synchronized static NetworkConfiguration getInstance() {
		if (config == null) {
			config = new NetworkConfiguration();
		}
		return config;
	}

	/**
	 * Forget the cached configuration
	 */
	public synchronized static void forgetConfiguration() {
		config = null;
	}
}
