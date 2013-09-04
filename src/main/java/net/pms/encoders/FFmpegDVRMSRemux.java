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

import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;

import net.pms.Messages;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.util.PlayerUtil;

import org.apache.commons.lang3.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FFmpegDVRMSRemux extends FFmpegBase {
	private final Logger logger = LoggerFactory.getLogger(FFmpegDVRMSRemux.class);
	private final PmsConfiguration configuration;
	private JTextField altffpath;
	public static final String ID = "ffmpegdvrmsremux";

	public FFmpegDVRMSRemux(PmsConfiguration configuration) {
		super(configuration);
		this.configuration = configuration;
	}

	@Override
	public PlayerPurpose getPurpose() {
		return PlayerPurpose.MISC_PLAYER;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public boolean isTimeSeekable() {
		return true;
	}

	@Override
	public String name() {
		return "FFmpeg DVR-MS Remux";
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

	@Deprecated
	protected String[] getDefaultArgs() {
		return new String[] {
			"-vcodec", "copy",
			"-acodec", "copy",
			"-threads", "2",
			"-g", "1",
			"-qscale", "1",
			"-qmin", "2",
			"-f", "vob",
			"-copyts"
		};
	}

	@Override
	@Deprecated
	public String[] args() {
		return getDefaultArgs();

	}

	@Override
	public String mimeType() {
		return "video/mpeg";
	}

	@Override
	public ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		String ffmpegAlternativePath = configuration.getFfmpegAlternativePath();
		List<String> cmdList = new ArrayList<String>();
		final String filename = dlna.getSystemName();
		cmdList.addAll(getGlobalOptions(logger));

		if (ffmpegAlternativePath != null && ffmpegAlternativePath.length() > 0) {
			cmdList.add(ffmpegAlternativePath);
		} else {
			cmdList.add(executable());
		}

		if (params.timeseek > 0) {
			cmdList.add("-ss");
			cmdList.add("" + params.timeseek);
		}

		cmdList.add("-i");
		cmdList.add(filename);

		for (String arg : args()) {
			cmdList.add(arg);
		}

		String customSettingsString = configuration.getMPEG2MainSettingsFFmpeg();
		if (StringUtils.isNotBlank(customSettingsString)) {
			String[] customSettingsArray = StringUtils.split(customSettingsString);

			if (customSettingsArray != null) {
				for (String option : customSettingsArray) {
					cmdList.add(option);
				}
			}
		}

		cmdList.add("pipe:");
		String[] cmdArray = new String[cmdList.size()];
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
	public JComponent config() {
		FormLayout layout = new FormLayout(
			"left:pref, 3dlu, p, 3dlu, 0:grow",
			"p, 3dlu, p, 3dlu, 0:grow"
		);
		PanelBuilder builder = new PanelBuilder(layout);

		CellConstraints cc = new CellConstraints();

		JComponent cmp = builder.addSeparator(Messages.getString("FFmpegDVRMSRemux.1"), cc.xyw(1, 1, 5));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		builder.addLabel(Messages.getString("FFmpegDVRMSRemux.0"), cc.xy(1, 3));
		altffpath = new JTextField(configuration.getFfmpegAlternativePath());
		altffpath.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setFfmpegAlternativePath(altffpath.getText());
			}
		});
		builder.add(altffpath, cc.xyw(3, 3, 3));

		return builder.getPanel();
	}

	@Override
	public boolean isPlayerCompatible(RendererConfiguration mediaRenderer) {
		return mediaRenderer.isTranscodeToMPEGPSAC3();
	}

	@Override
	public boolean isCompatible(DLNAResource resource) {
		return PlayerUtil.isVideo(resource, Format.Identifier.DVRMS);
	}
}
