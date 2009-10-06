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

import java.io.IOException;

import javax.swing.JComponent;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.network.HTTPResource;

public class FFMpegAudio extends FFMpegVideo {
	
	public static final String ID = "ffmpegaudio"; //$NON-NLS-1$
	
	@SuppressWarnings("unused")
	private final PmsConfiguration configuration;
	
	public FFMpegAudio(PmsConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public JComponent config() {
		return null;
	}
	
	@Override
	public int purpose() {
		return AUDIO_SIMPLEFILE_PLAYER;
	}

	@Override
	public String id() {
		return ID;
	}
	
	@Override
	public boolean isTimeSeekable() {
		return false;
	}
	
	public boolean avisynth() {
		return false;
	}

	@Override
	public String name() {
		return "FFmpeg Audio"; //$NON-NLS-1$
	}

	@Override
	public int type() {
		return Format.AUDIO;
	}

	@Override
	public String[] args() {
		/*if (overridenArgs != null)
			return overridenArgs;
		else*/
			return new String [] { "-f", "wav", "-ar", "48000"}; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String mimeType() {
		return HTTPResource.AUDIO_TRANSCODE; //$NON-NLS-1$
	}

	/*@Override
	public String executable() {
		return PMS.get().getFFmpegPath();
	}*/

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media, OutputParams params) throws IOException {
		params.maxBufferSize = PMS.getConfiguration().getMaxAudioBuffer();
		params.waitbeforestart = 2000;
		String args [] = args();
		if (params.mediaRenderer.isTranscodeToMP3()) {
			args = new String [] { "-f", "mp3", "-ar", "48000", "-ab", "320000" };
		}
		if (params.mediaRenderer.isTranscodeAudioTo441())
			args[3] = "44100";
		return getFFMpegTranscode(fileName, media, params,args);
	}

}
