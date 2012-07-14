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
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.pms.Messages;
import net.pms.network.HTTPResource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VLCVideo extends Player {
	private static final Logger logger = LoggerFactory.getLogger(VLCVideo.class);
	private final PmsConfiguration configuration;
	public static final String ID = "vlctrans";
	protected JCheckBox hardwareAccel;

	public VLCVideo(PmsConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public int purpose() {
		return VIDEO_SIMPLEFILE_PLAYER;
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
	public boolean avisynth() {
		return false;
	}

	@Override
	public String[] args() {
		return new String[]{};
	}

	@Override
	public String name() {
		return "VLC Transcoder";
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

	@Override
	public String mimeType() {
		//I think?
		return HTTPResource.VIDEO_TRANSCODE;
	}

	@Override
	public String executable() {
		return configuration.getVlcPath();
	}

	protected String getEncodingArgs() {
		//Note, changed
		ArrayList<String> args = new ArrayList<String>();
		
		//Codecs to use (mp2 AKA dvd format)
		args.add("vcodec=mp2v");
		args.add("acodec=mp2a");
		
		//Bitrate in kbit/s (TODO: Use global option?)
		args.add("vb=4096");
		args.add("ab=128");
		
		//Video scaling
		args.add("scale=1");
		
		//Channels (TODO: is this nessesary?)
		args.add("channels=2");
		
		//Stream subtitiles to client
		//args.add("scodec=dvbs");
		//args.add("senc=dvbsub");
		
		//Hardcode subtitiles into video
		args.add("soverlay");
		
		return StringUtils.join(args, ",");
	}

	protected String getMux() {
		return "ts";
		//return "mpeg1";
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAResource dlna, DLNAMediaInfo media, OutputParams params) throws IOException {
		boolean isWindows = Platform.isWindows();
		PipeProcess tsPipe = new PipeProcess("VLC" + System.currentTimeMillis() + "." + getMux());
		ProcessWrapper pipe_process = tsPipe.getPipeProcess();

		// XXX it can take a long time for Windows to create a named pipe
		// (and mkfifo can be slow if /tmp isn't memory-mapped), so start this as early as possible
		pipe_process.runInNewThread();
		tsPipe.deleteLater();

		params.input_pipes[0] = tsPipe;
		params.minBufferSize = params.minFileSize;
		params.secondread_minsize = 100000;

		List<String> cmdList = new ArrayList<String>();
		cmdList.add(executable());
		cmdList.add("-I");
		cmdList.add("dummy");

		//Hardware accelleration seems to be more stable now, so its enabled
		if(hardwareAccel.isSelected())
			cmdList.add("--ffmpeg-hw");

		//Stop the DOS box from appearing on windows
		if (isWindows)
			cmdList.add("--dummy-quiet");
		
		//File needs to be given before sout, otherwise vlc complains
		cmdList.add(fileName);
		
		cmdList.add("--sub-language=eng");
		
		//Add our transcode options
		/*String transcodeSpec = String.format(
			"#transcode{%s}:std{access=file,mux=%s,dst=\"%s%s\"}",
			getEncodingArgs(),
			getMux(),
			(isWindows ? "\\\\" : ""),
			tsPipe.getInputPipe());
			*/
		String transcodeSpec = String.format(
			"#transcode{%s}:duplicate{dst=display,dst=std{access=file,mux=%s,dst=\"%s%s\"}}",
			getEncodingArgs(),
			getMux(),
			(isWindows ? "\\\\" : ""),
			tsPipe.getInputPipe());
		cmdList.add("--sout");
		cmdList.add(transcodeSpec);
		
		//Force VLC to die when finished
		cmdList.add("vlc://quit");

		//Pass to process wrapper
		String[] cmdArray = new String[cmdList.size()];
		cmdList.toArray(cmdArray);
		cmdArray = finalizeTranscoderArgs(this, fileName, dlna, media, params, cmdArray);
		logger.debug("Finalized args: " + StringUtils.join(cmdArray, " "));
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		pw.attachProcess(pipe_process);

		//TODO: Why is this here?
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
		}

		pw.runInNewThread();
		return pw;
	}

	@Override
	public JComponent config() {
				FormLayout layout = new FormLayout(
			"left:pref, 0:grow",
			"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.EMPTY_BORDER);
		builder.setOpaque(false);

		CellConstraints cc = new CellConstraints();


		JComponent cmp = builder.addSeparator("VLC Transcoder Settings", cc.xyw(2, 1, 1));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		hardwareAccel = new JCheckBox("Use hardware acceleration");
		hardwareAccel.setContentAreaFilled(false);
		builder.add(hardwareAccel, cc.xy(2, 3));

		return builder.getPanel();
	}
}
