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

import java.util.ArrayList;
import java.util.List;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.util.ProcessUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that provides access to the list of protocols supported by FFmpeg.
 */
public final class FFmpegProtocols {
	private final Logger logger = LoggerFactory.getLogger(FFmpegProtocols.class);
	private final List<String> protocols = new ArrayList<String>();
	private final PmsConfiguration configuration;
	private boolean convertMmsToMmsh = false;

	// package-private constructor
	FFmpegProtocols(PmsConfiguration configuration) {
		this.configuration = configuration;

		queryProtocols();

		if (!protocols.contains("mms") && protocols.contains("mmsh")) {
			protocols.add("mms");
			convertMmsToMmsh = true;
		}

		logger.debug("FFmpeg supported protocols: {}", protocols);
	}

	public boolean isSupportedProtocol(String protocol) {
		return protocols.contains(protocol);
	}

	public String getFilename(String filename) {
		// XXX work around an ffmpeg bug: http://ffmpeg.org/trac/ffmpeg/ticket/998
		if (convertMmsToMmsh) {
			if (filename.startsWith("mms:")) {
				filename = "mmsh:" + filename.substring(4);
			}
		}

		return filename;
	}

	private void queryProtocols() {
		String output = ProcessUtil.run(configuration.getFfmpegPath(), "-protocols");
		boolean add = false;

		for (String line : output.split("\n")) {
			if (line.equals("Input:")) {
				add = true;
			} else if (line.equals("Output:")) {
				break;
			} else if (add) {
				protocols.add(line);
			}
		}
	}
}
