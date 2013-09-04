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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import net.pms.Messages;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;
import net.pms.util.PlayerUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FFmpegAudio extends FFmpegVideo {
	public static final String ID = "ffmpegaudio";
	private final PmsConfiguration configuration;
	private Logger logger = LoggerFactory.getLogger(FFmpegAudio.class);

	// should be private
	@Deprecated
	JCheckBox noresample;

	public FFmpegAudio(PmsConfiguration configuration) {
		super(configuration);
		this.configuration = configuration;
	}

	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout(
			"left:pref, 0:grow",
			"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow"
		);
		PanelBuilder builder = new PanelBuilder(layout);

		CellConstraints cc = new CellConstraints();

		JComponent cmp = builder.addSeparator("Audio settings", cc.xyw(2, 1, 1));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		noresample = new JCheckBox(Messages.getString("TrTab2.22"));
		noresample.setContentAreaFilled(false);
		noresample.setSelected(configuration.isAudioResample());
		noresample.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setAudioResample(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		builder.add(noresample, cc.xy(2, 3));

		return builder.getPanel();
	}

	@Override
	public PlayerPurpose getPurpose() {
		return PlayerPurpose.AUDIO_FILE_PLAYER;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public boolean isTimeSeekable() {
		return true;
	}

	public boolean avisynth() {
		return false;
	}

	@Override
	public String name() {
		return "FFmpeg Audio";
	}

	@Override
	public int type() {
		return Format.AUDIO;
	}

	@Override
	@Deprecated
	public String[] args() {
		// unused: kept for backwards compatibility
		return new String[] { "-f", "s16be", "-ar", "48000" };
	}

	@Override
	public String mimeType() {
		return HTTPResource.AUDIO_TRANSCODE;
	}

	/**
	 * Returns a list of <code>String</code>s representing ffmpeg output
	 * options (i.e. options that define the output file's format,
	 * bitrate and sample rate) compatible with the renderer's
	 * <code>TranscodeAudio</code> profile.
	 *
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the file being streamed. May contain
	 *     unset/null values (e.g. for web streams).
	 * @param params The {@link net.pms.io.OutputParams} context object used to
	 *     store miscellaneous parameters for this request.
	 * @return a {@link List} of <code>String</code>s representing the
	 *     FFmpeg output parameters for the renderer according
	 *     to its <code>TranscodeAudio</code> profile.
	 * @since 1.90.0
	 */
	protected List<String> getAudioTranscodeOptions(DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		List<String> options = new ArrayList<String>();

		if (params.mediaRenderer.isTranscodeToMP3()) {
			options.add("-f");
			options.add("mp3");
			options.add("-ab");
			options.add("320000");
		} else if (params.mediaRenderer.isTranscodeToWAV()) {
			options.add("-f");
			options.add("wav");
		} else { // default: LPCM
			options.add("-f");
			options.add("s16be"); // same as -f wav, but without a WAV header
		}

		if (configuration.isAudioResample()) {
			if (params.mediaRenderer.isTranscodeAudioTo441()) {
				options.add("-ar");
				options.add("44100");
			} else {
				options.add("-ar");
				options.add("48000");
			}
		}

		return options;
	}

	@Override
	public ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		final String filename = dlna.getSystemName();
		params.maxBufferSize = configuration.getMaxAudioBuffer();
		params.waitbeforestart = 2000;
		params.manageFastStart();

		int nThreads = configuration.getNumberOfCpuCores();
		List<String> cmdList = new ArrayList<String>();

		cmdList.add(executable());
		cmdList.addAll(getGlobalOptions(logger));

		if (params.timeseek > 0) {
			cmdList.add("-ss");
			cmdList.add("" + params.timeseek);
		}

		// decoder threads
		cmdList.add("-threads");
		cmdList.add("" + nThreads);

		cmdList.add("-i");
		cmdList.add(filename);

		// encoder threads
		cmdList.add("-threads");
		cmdList.add("" + nThreads);

		if (params.timeend > 0) {
			cmdList.add("-t");
			cmdList.add("" + params.timeend);
		}

		// Add the output options (-f, -ab, -ar)
		cmdList.addAll(getAudioTranscodeOptions(dlna, media, params));
		cmdList.add("pipe:");

		String[] cmdArray = new String[ cmdList.size() ];
		cmdList.toArray(cmdArray);

		cmdArray = finalizeTranscoderArgs(
			filename,
			dlna,
			media,
			params,
			cmdArray
		);

		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		pw.runInNewThread();

		return pw;
	}

	@Override
	public boolean isCompatible(DLNAResource resource) {
		return PlayerUtil.isAudio(resource, Format.Identifier.FLAC)
			|| PlayerUtil.isAudio(resource, Format.Identifier.M4A)
			|| PlayerUtil.isAudio(resource, Format.Identifier.OGG)
			|| PlayerUtil.isAudio(resource, Format.Identifier.WAV);
	}
}
