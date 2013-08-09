/*
 * PS3 Media Server, for streaming any media to your PS3.
 * Copyright (C) 2008-2013 A.Brochard
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
package net.pms.encoders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;

import org.slf4j.Logger;

/**
 * Methods common to FFmpeg subclasses.
 */
public abstract class FFmpegBase extends Player {
	private final PmsConfiguration configuration;
	private static FFmpegProtocols protocols;

	public FFmpegBase(PmsConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Returns an instance of the {@link FFmpegProtocols} class used by
	 * FFmpeg web engines for protocol compatibility tests and substitutions
	 *
	 * @return the singleton instance of FFmpegProtocol
	 * @since 1.90.1
	 */
	protected synchronized FFmpegProtocols getProtocols() {
		if (protocols == null) {
			protocols = new FFmpegProtocols(configuration);
		}

		return protocols;
	}

	@Override
	public String executable() {
		return configuration.getFfmpegPath();
	}

	/**
	 * Returns a list of global ffmpeg options:
	 * ffmpeg -global-options -input-options -i file -output-options
	 *
	 * @param logger the caller's {@link Logger} instance — used to determine the log level.
	 * @return a {@link List} of <code>String</code>s representing the global options.
	 * @since 1.90.0
	 */
	protected List<String> getGlobalOptions(Logger logger) {
		List<String> options = new ArrayList<String>();

		// don't wait for user interaction
		options.add("-y");

		// set the log level
		options.add("-loglevel");
		if (logger.isTraceEnabled()) { // Set -loglevel in accordance with the caller's logger setting
			options.add("info"); // Could be changed to "verbose" or "debug" if "info" level is not enough
		} else {
			// XXX "warning" floods the logfile with "packet too large, ignoring buffer limits to mux it" errors
			options.add("fatal");
		}

		return options;
	}
}
