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
package net.pms.encoders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JComponent;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.formats.v2.SubtitleType;
import net.pms.util.ProcessUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the Windows specific AviSynth/FFmpeg player combination. 
 */
public class FFMpegAviSynthVideo extends FFMpegVideo {
	private static final Logger logger = LoggerFactory.getLogger(FFMpegAviSynthVideo.class);
	public static final String ID      = "avsffmpeg";

	public FFMpegAviSynthVideo() {
		super(PMS.getConfiguration());
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String name() {
		return "AviSynth/FFmpeg";
	}

	@Override
	public boolean avisynth() {
		return true;
	}

	@Override
	public JComponent config() {
		return config("FFMpegVideo.0");
	}

	public static File getAVSScript(String filename, DLNAMediaSubtitle subTrack) throws IOException {
		return getAVSScript(filename, subTrack, -1, -1);
	}

	public static File getAVSScript(String filename, DLNAMediaSubtitle subTrack, int fromFrame, int toFrame) throws IOException {
		String onlyFileName = filename.substring(1 + filename.lastIndexOf("\\"));
		File file = new File(PMS.getConfiguration().getTempFolder(), "pms-avs-" + onlyFileName + ".avs");
		PrintWriter pw = new PrintWriter(new FileOutputStream(file));

		String convertfps = "";
		if (PMS.getConfiguration().getAvisynthConvertFps()) {
			convertfps = ", convertfps=true";
		}
		File f = new File(filename);
		if (f.exists()) {
			filename = ProcessUtil.getShortFileNameIfWideChars(filename);
		}
		String movieLine = "clip=DirectShowSource(\"" + filename + "\"" + convertfps + ")";
		String subLine = null;

		if (subTrack != null && PMS.getConfiguration().isAutoloadSubtitles() && !PMS.getConfiguration().isDisableSubtitles()) {
			logger.trace("Avisynth script: Using sub track: " + subTrack);
			if (subTrack.getExternalFile() != null) {
				String function = "TextSub";
				if (subTrack.getType() == SubtitleType.VOBSUB) {
					function = "VobSub";
				}
				subLine = "clip=" + function + "(clip, \"" + ProcessUtil.getShortFileNameIfWideChars(subTrack.getExternalFile().getAbsolutePath()) + "\")";
			}
		}

		ArrayList<String> lines = new ArrayList<String>();

		boolean fullyManaged = false;
		String script = PMS.getConfiguration().getAvisynthScript();
		StringTokenizer st = new StringTokenizer(script, PMS.AVS_SEPARATOR);
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			if (line.contains("<movie") || line.contains("<sub"))
			{
				fullyManaged = true;
			}
			lines.add(line);
		}

		if (fullyManaged) {
			for (String s : lines) {
				s = s.replace("<moviefilename>", filename);
				if (movieLine != null) {
					s = s.replace("<movie>", movieLine);
				}
				s = s.replace("<sub>", subLine != null ? subLine : "#");
				pw.println(s);
			}
		} else {
			pw.println(movieLine);
			if (subLine != null) {
				pw.println(subLine);
			}
			pw.println("clip");

		}

		pw.close();
		file.deleteOnExit();
		return file;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompatible(DLNAResource resource) {
		if (resource == null || resource.getFormat().getType() != Format.VIDEO) {
			return false;
		}

		Format format = resource.getFormat();

		if (format != null) {
			Format.Identifier id = format.getIdentifier();

			if (id.equals(Format.Identifier.MKV)
					|| id.equals(Format.Identifier.MPG)) {
				return true;
			}
		}

		return false;
	}
}
