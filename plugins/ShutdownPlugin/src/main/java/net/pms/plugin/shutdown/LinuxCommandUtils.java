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

/**
 * Linux and OSX specific implementation of {@link CommandUtils}.
 */
public final class LinuxCommandUtils extends CommandUtils {
	/**
	 * {@inheritDoc}
	 */
	String[] getPowerOffCommand() {
		return new String[] { "shutdown", "-h", "now" };
	}

	/**
	 * {@inheritDoc}
	 */
	String[] getRestartCommand() {
		return new String[] { "shutdown", "-r", "now" };
	}
}
