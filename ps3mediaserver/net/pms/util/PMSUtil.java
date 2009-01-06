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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.Arrays;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.newgui.LooksFrame;

public class PMSUtil {
	
	public static <T> T[] copyOf(T[] original, int newLength) {
		return Arrays.copyOf(original, newLength);
	}
	
	public static boolean isNetworkInterfaceLoopback(NetworkInterface ni) throws SocketException {
		return ni.isLoopback();
	}
	
	public static void browseURI(String uri) {
		try {
			Desktop.getDesktop().browse(new URI(uri)); //$NON-NLS-1$
		} catch (Exception e1) {}
	}
	
	public static void addSystemTray(final LooksFrame frame) {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			Image image = Toolkit.getDefaultToolkit().getImage(frame.getClass().getResource("/resources/images/Play1Hot_256.png")); //$NON-NLS-1$

			PopupMenu popup = new PopupMenu();
			MenuItem defaultItem = new MenuItem(Messages.getString("LooksFrame.5")); //$NON-NLS-1$
			MenuItem traceItem = new MenuItem(Messages.getString("LooksFrame.6")); //$NON-NLS-1$

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

			final TrayIcon trayIcon = new TrayIcon(image, "Java PS3 Media Server v" + PMS.VERSION, popup); //$NON-NLS-1$

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
	
	public static byte [] getHardwareAddress(NetworkInterface ni) throws SocketException {
		return ni.getHardwareAddress();
	}

}
