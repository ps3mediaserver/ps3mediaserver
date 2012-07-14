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

import com.sun.jna.Platform;
import java.awt.GridLayout;
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
import javax.swing.border.TitledBorder;
import net.pms.network.HTTPResource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VLCVideo extends Player {
	private static final Logger logger = LoggerFactory.getLogger(VLCVideo.class);
	private final PmsConfiguration configuration;
	public static final String ID = "vlctrans";
	protected JCheckBox hardwareAccel;
	protected JTextField languagePri;
	protected JTextField subtitlePri;
	protected JCheckBox subtitleEnabled;

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
		//See: http://www.videolan.org/doc/streaming-howto/en/ch03.html
		//See: http://wiki.videolan.org/Codec
		//Xbox: wmv2, wma, asf
		//PS3: mp1v, mpga, mpeg1 (WORKING)
		ArrayList<String> args = new ArrayList<String>();
		
		//Codecs to use (mp2 AKA dvd format)
		args.add("venc=ffmpeg");
		args.add("vcodec=mp1v");
		args.add("acodec=mpga");
		
		//Bitrate in kbit/s (TODO: Use global option?)
		args.add("vb=4096");
		args.add("ab=128");
		
		//Video scaling (TODO: Why is this needed?
		//args.add("scale=1");
		
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
		return "mpeg1";
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
		String transcodeSpec = String.format(
			"#transcode{%s}:std{access=file,mux=%s,dst=\"%s%s\"}",
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
		//JGoodies Formlayout makes no sense. 
		//Working impl in swing in 6 mins > semi-working impl in jgoodies in 6 hours
		JPanel mainPanel = new JPanel();
		TitledBorder titledBorder = BorderFactory.createTitledBorder("VLC Transcoder Settings");
        titledBorder.setTitleJustification(TitledBorder.LEFT);
		mainPanel.setBorder(titledBorder);
		//Yes this is ugly, 
		mainPanel.setLayout(new GridLayout(9, 3));
		
		hardwareAccel = new JCheckBox("Use hardware acceleration");
		hardwareAccel.setContentAreaFilled(false);
		mainPanel.add(hardwareAccel);
		
		//Try adding a label with a text field
		languagePri = genTextField("Audio Language Priority", "eng,jpn", mainPanel);
		subtitleEnabled = new JCheckBox("Enable Subtitles");
		subtitleEnabled.setSelected(true);
		mainPanel.add(subtitleEnabled);
		subtitlePri = genTextField("Subtitle Language Priority", "jpn,eng", mainPanel);
		return mainPanel;
	}
	
	protected JTextField genTextField(String labelText, String fieldText, JPanel target) {
		JPanel container = new JPanel();
		JLabel label = new JLabel(labelText);
		JTextField field = new JTextField(fieldText);
		field.setColumns(20);
		label.setLabelFor(field);
	
		container.add(label);
		container.add(field);
		
		target.add(container);
		
		return field;
	}
}
