package net.pms.util;

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
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.io.OutputTextConsumer;
import net.pms.newgui.LooksFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.sun.jna.Platform;

public class PMSUtil {
	private static LooksFrame frameRef;
	private static final Logger logger = LoggerFactory.getLogger(PMSUtil.class);

	
	@SuppressWarnings("unchecked")
	public static <T> T[] copyOf(T[] original, int newLength) {
		if (Platform.isMac()) {
			// FIXME: Suspicious... Why would this be different on OSX? If this turns out to be
			// legacy code, this whole method can be factored out in favor of Arrays.copyOf().
			Class newType = original.getClass();
			T[] copy = ((Object) newType == (Object) Object[].class) ? (T[]) new Object[newLength]
					: (T[]) Array.newInstance(newType.getComponentType(), newLength);
			System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
			return copy;
		} else {
			return Arrays.copyOf(original, newLength);
		}
	}

	/**
	 * Open HTTP URLs in the default browser.
	 * @param uri URI string to open externally.
	 */	
	public static void browseURI(String uri) {
		try {
			if (Platform.isMac()) {
				// On OSX, open the given URI with the "open" command.
				// This will open HTTP URLs in the default browser.
				Runtime.getRuntime().exec(new String[] { "open", uri });
			} else {
				Desktop.getDesktop().browse(new URI(uri)); //$NON-NLS-1$
			}
		} catch (IOException e) {
			logger.trace("Unable to open the given URI: " + uri + ".");
		} catch (URISyntaxException e) {
			logger.trace("Unable to open the given URI: " + uri + ".");
		}
	}

	public static void addSystemTray(final LooksFrame frame) {
		if (Platform.isMac()) {
			frameRef = frame;
			Application.getApplication().addApplicationListener(
					new com.apple.eawt.ApplicationAdapter() {

						public void handleReOpenApplication(ApplicationEvent e) {
							if (!frameRef.isVisible())
								frameRef.setVisible(true);
						}

						public void handleQuit(ApplicationEvent e) {
							System.exit(0);
						}

					});
		} else {
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
	}

	public static boolean isNetworkInterfaceLoopback(NetworkInterface ni) throws SocketException {
		if (Platform.isMac()) {
			return false;
		} else {
			return ni.isLoopback();
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
	public static byte[] getHardwareAddress(NetworkInterface ni) throws SocketException {
		if (Platform.isMac()) {
			// On Mac OS, fetch the hardware address from the command line tool "ifconfig".
			byte[] aHardwareAddress = null;

			try {
				Process aProc = Runtime.getRuntime().exec(new String[] { "ifconfig", ni.getName(), "ether" });
				aProc.waitFor();
				OutputTextConsumer aConsumer = new OutputTextConsumer(aProc.getInputStream(), false);
				aConsumer.run();
				List<String> aLines = aConsumer.getResults();
				String aMacStr = null;
				Pattern aMacPattern = Pattern.compile("\\s*ether\\s*([a-d0-9]{2}:[a-d0-9]{2}:[a-d0-9]{2}:[a-d0-9]{2}:[a-d0-9]{2}:[a-d0-9]{2})");
				
				for (String aLine : aLines) {
					Matcher aMacMatcher = aMacPattern.matcher(aLine);
					
					if (aMacMatcher.find()) {
						aMacStr = aMacMatcher.group(1);
						break;
					}
				}
				
				if (aMacStr != null) {
					String[] aComps = aMacStr.split(":");
					aHardwareAddress = new byte[aComps.length];
					
					for (int i = 0; i < aComps.length; i++) {
						String aComp = aComps[i];
						aHardwareAddress[i] = (byte) Short.valueOf(aComp, 16).shortValue();
					}
				}
			} catch (IOException e) {
				logger.debug("Failed to execute ifconfig", e);
			} catch (InterruptedException e) {
				logger.debug("Interrupted while waiting for ifconfig", e);
				Thread.interrupted(); // XXX work around a Java bug - see ProcessUtil.waitFor()
			}
			return aHardwareAddress;			
		} else {
			return ni.getHardwareAddress();
		}
	}
}
