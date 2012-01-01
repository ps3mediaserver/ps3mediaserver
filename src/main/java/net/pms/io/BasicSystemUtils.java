/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2011 G.Zsombor
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
package net.pms.io;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.newgui.LooksFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for the SystemUtils class for the generic cases.
 * @author zsombor
 *
 */
public class BasicSystemUtils implements SystemUtils {
	private final static Logger logger = LoggerFactory.getLogger(BasicSystemUtils.class); 
	
	protected String vlcp;
	protected String vlcv;
	protected boolean avis;

	@Override
	public void disableGoToSleep() {

	}

	@Override
	public void reenableGoToSleep() {

	}

	@Override
	public File getAvsPluginsDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortPathNameW(String longPathName) {
		return longPathName;
	}

	@Override
	public String getWindowsDirectory() {
		return null;
	}

	@Override
	public String getDiskLabel(File f) {
		return null;
	}

	@Override
	public boolean isKerioFirewall() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pms.io.SystemUtils#getVlcp()
	 */
	@Override
	public String getVlcp() {
		return vlcp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pms.io.SystemUtils#getVlcv()
	 */
	@Override
	public String getVlcv() {
		return vlcv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pms.io.SystemUtils#isAvis()
	 */
	@Override
	public boolean isAvis() {
		return avis;
	}

	@Override
	public void browseURI(String uri) {
		try {
			Desktop.getDesktop().browse(new URI(uri));
		} catch (IOException e) {
			logger.trace("Unable to open the given URI: " + uri + ".");
		} catch (URISyntaxException e) {
			logger.trace("Unable to open the given URI: " + uri + ".");
		}
	}

	@Override
	public boolean isNetworkInterfaceLoopback(NetworkInterface ni) throws SocketException {
		return ni.isLoopback();
	}

	@Override
	public void addSystemTray(final LooksFrame frame) {

		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			Image image = Toolkit.getDefaultToolkit().getImage(frame.getClass().getResource("/resources/images/icon-16.png"));

			PopupMenu popup = new PopupMenu();
			MenuItem defaultItem = new MenuItem(Messages.getString("LooksFrame.5"));
			MenuItem traceItem = new MenuItem(Messages.getString("LooksFrame.6"));

			defaultItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.quit();
				}
			});

			traceItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.setVisible(true);
				}
			});

			popup.add(traceItem);
			popup.add(defaultItem);

			final TrayIcon trayIcon = new TrayIcon(image, "PS3 Media Server " + PMS.getVersion(), popup);

			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.setVisible(true);
					frame.setFocusable(true);
				}
			});
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Fetch the hardware address for a network interface.
	 * 
	 * @param ni Interface to fetch the mac address for
	 * @return the mac address as bytes, or null if it couldn't be fetched.
	 * @throws SocketException
	 *             This won't happen on Mac OS, since the NetworkInterface is
	 *             only used to get a name.
	 */
	@Override
	public byte[] getHardwareAddress(NetworkInterface ni) throws SocketException {
		return ni.getHardwareAddress();
	}
	
	/**
	 * Return the platform specific ping command. 
	 * @param hostAddress
	 * @param count
	 * @param packetSize
	 * @return
	 */
	@Override
	public String[] getPingCommand(String hostAddress, int count, int packetSize) {
		return new String[] { "ping", /* count */ "-c" , Integer.toString(count), /* size */ "-s", Integer.toString(packetSize), hostAddress };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getPowerOffCommand() {
		return new String[] { "shutdown", "-h", "now" };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getRestartCommand() {
		return new String[] { "shutdown", "-r", "now" };
	}
}
