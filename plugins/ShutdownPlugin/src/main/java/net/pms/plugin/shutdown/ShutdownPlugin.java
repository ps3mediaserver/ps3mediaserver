/*
 * Shutdown Plugin for PS3 Media Server
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
package net.pms.plugin.shutdown;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JComponent;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;
import net.pms.external.AdditionalFolderAtRoot;
import net.pms.io.Gob;
import net.pms.util.ProcessUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

/**
 * This class implements a computer shutdown plugin for PS3 Media Server.
 */
public class ShutdownPlugin implements AdditionalFolderAtRoot {
	/**
	 * Logger for writing messages to the log file.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ShutdownPlugin.class);

	/**
	 * Resource bundle that holds the locale dependent messages.
	 */
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("messages");

	/**
	 * Constructor for the plugin.
	 */
	public ShutdownPlugin() {
		LOG.info("Initializing shutdown plugin");
	}

	/**
	 * Returns the Computer Shutdown folder and its contents.
	 * 
	 * @return The folder
	 */
	@Override
	public DLNAResource getChild() {
		// Create computer shutdown folder.
		DLNAResource shutdownFolder = new VirtualFolder(MESSAGES.getString("menu.foldername"), null);

		// Add power off menu item
		shutdownFolder.addChild(getPowerOffAction());

		// Add restart menu item
		shutdownFolder.addChild(getRestartAction());

		return shutdownFolder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JComponent config() {
		// No configuration needed
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name() {
		return "Shutdown Plugin";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() {
		LOG.trace("Shutting down shutdown plugin");
	}

	/**
	 * Constructs and returns the virtual video action to power off the computer.
	 * @return The virtual video action.
	 */
	private DLNAResource getPowerOffAction() {
		DLNAResource action = new VirtualVideoAction(MESSAGES.getString("menu.poweroff"), true) {
			/**
			 * This method is called when the user selects the menu item.
			 * @return Always returns true.
			 */
			@Override
			public boolean enable() {
				ProcessBuilder pb;
				CommandUtils utils;

		    	LOG.trace("Attempting to shut down the computer.");

		        if (Platform.isWindows()) {
		        	utils = new WindowsCommandUtils();
		        } else {
		        	utils = new LinuxCommandUtils();
		        }

		        pb = new ProcessBuilder(utils.getPowerOffCommand());
		        pb.redirectErrorStream(true);

				try {
					// Start the command process
					Process process = pb.start();

					// Capture the output, but discard it.
					new Gob(process.getErrorStream()).start();
					new Gob(process.getInputStream()).start();

					// Interesting conundrum: waiting for the power off to finish?
					// I guess we're waiting for it to fail to finish. ;-)
					ProcessUtil.waitFor(process);

					if (process.exitValue() != 0) {
						LOG.error("Failed to execute power off command.");
					}
				} catch (IOException e) {
					LOG.error("Failed to execute power off command.");
				}

				return true;

			}
		};

		return action;
	}

	/**
	 * Constructs and returns the virtual video action to restart the computer.
	 * @return The virtual video action.
	 */
	private DLNAResource getRestartAction() {
		DLNAResource action = new VirtualVideoAction(MESSAGES.getString("menu.restart"), true) {
			/**
			 * This method is called when the user selects the menu item.
			 * @return Always returns true.
			 */
			@Override
			public boolean enable() {
				ProcessBuilder pb;
				CommandUtils utils;

		    	LOG.trace("Attempting to restart the computer.");

		        if (Platform.isWindows()) {
		        	utils = new WindowsCommandUtils();
		        } else {
		        	utils = new LinuxCommandUtils();
		        }

		        pb = new ProcessBuilder(utils.getRestartCommand());
				pb.redirectErrorStream(true);

				try {
					// Start the command process
					Process process = pb.start();

					// Capture the output, but discard it.
					new Gob(process.getErrorStream()).start();
					new Gob(process.getInputStream()).start();

					// Interesting conundrum: waiting for the restart to finish?
					// I guess we're waiting for it to fail to finish. ;-)
					ProcessUtil.waitFor(process);

					if (process.exitValue() != 0) {
						LOG.error("Failed to execute restart command.");
					}
				} catch (IOException e) {
					LOG.error("Failed to execute restart command.");
				}
				return true;
			}
		};

		return action;
	}
}
