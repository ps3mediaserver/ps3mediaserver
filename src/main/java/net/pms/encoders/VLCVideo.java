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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use VLC as a backend transcoder. Note that 0.x and 1.x versions are unsupported 
 * (and probably will crash). Only the latest version will be supported
 * 
 * @author Leon Blakey <lord.quackstar@gmail.com>
 */
public class VLCVideo extends Player {
	private static final Logger logger = LoggerFactory.getLogger(VLCVideo.class);
	private final PmsConfiguration configuration;
	public static final String ID = "vlctrans";
	protected JCheckBox hardwareAccel;
	protected JTextField audioPri;
	protected JTextField subtitlePri;
	protected JCheckBox subtitleEnabled;
	protected JTextField scale;
	protected final double scaleDefault = 1.0;
	protected JTextField codecVideo;
	protected JTextField codecAudio;
	protected JTextField codecContainer;
	protected JCheckBox experimentalCodecs;
	protected JCheckBox audioSyncEnabled;
	protected JTextField sampleRate;

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

	@Override
	public boolean isCompatible(Format format) {
		//VLC is a general transcoder that should support every format
		//Until problem occurs, assume compatible
		return true;
	}

	@Override
	public boolean isCompatible(DLNAMediaInfo mediaInfo) {
		//See above for reason why this is always true
		return true;
	}

	protected List<String> getEncodingArgs() {
		//See: http://www.videolan.org/doc/streaming-howto/en/ch03.html
		//See: http://wiki.videolan.org/Codec
		//Xbox: wmv2, wma, asf (WORKING)
		//PS3: mp1v, mpga, mpeg1 (WORKING)
		List<String> args = new ArrayList<String>();

		//Codecs to use
		args.add("venc=ffmpeg");
		args.add("vcodec=" + codecVideo.getText());
		args.add("acodec=" + codecAudio.getText());

		//Bitrate in kbit/s (TODO: Use global option?)
		args.add("vb=4096");
		args.add("ab=128");

		//Video scaling
		args.add("scale=" + scale.getText());

		//Channels (TODO: is this necessary?)
		args.add("channels=2");

		//Static sample rate
		args.add("samplerate=" + sampleRate.getText());

		//Stream subtitles to client
		//args.add("scodec=dvbs");
		//args.add("senc=dvbsub");

		//Hardcode subtitles into video
		args.add("soverlay");

		//This has caused garbled audio, so only enable when told to
		if (audioSyncEnabled.isSelected())
			args.add("audio-sync");

		return args;
	}

	protected String getMux() {
		return codecContainer.getText();
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAResource dlna, DLNAMediaInfo media, OutputParams params) throws IOException {
		boolean isWindows = Platform.isWindows();
		PipeProcess tsPipe = new PipeProcess("VLC" + System.currentTimeMillis() + "." + getMux());
		ProcessWrapper pipe_process = tsPipe.getPipeProcess();

		logger.debug("filename: " + fileName);
		logger.debug("dlna: " + dlna);
		logger.debug("media: " + media);
		logger.debug("outputparams: " + params);

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

		//Hardware acceleration seems to be more stable now, so its enabled
		if (hardwareAccel.isSelected())
			cmdList.add("--ffmpeg-hw");

		//Useful for the more esoteric codecs people use
		if (experimentalCodecs.isSelected())
			cmdList.add("--sout-ffmpeg-strict=-2");

		//Stop the DOS box from appearing on windows
		if (isWindows)
			cmdList.add("--dummy-quiet");

		//File needs to be given before sout, otherwise vlc complains
		cmdList.add(fileName);

		//Handle audio language
		String audioLang;
		if (params.aid != null)
			//User specified language at the client, acknowledge it
			if (params.aid.getLang() == null || params.aid.getLang().equals("und"))
				//VLC doesn't understand und, but does understand none
				audioLang = "none";
			else
				audioLang = params.aid.getLang();
		else
			//Not specified, use language from GUI
			audioLang = audioPri.getText();
		cmdList.add("--audio-language=" + audioLang);

		//Handle subtitile language
		String subtitleLang;
		if (params.sid != null)
			//User specified language at the client, acknowledge it
			if (params.sid.getLang() == null || params.sid.getLang().equals("und"))
				//VLC doesn't understand und, but does understand none
				subtitleLang = "none";
			else
				subtitleLang = params.sid.getLang();
		else
			//Not specified, use language from GUI if enabled
			if (subtitleEnabled.isSelected())
				subtitleLang = audioPri.getText();
			else
				subtitleLang = "none";
		cmdList.add("--sub-language=" + subtitleLang);

		//Add our transcode options
		String transcodeSpec = String.format(
				"#transcode{%s}:std{access=file,mux=%s,dst=\"%s%s\"}",
				StringUtils.join(getEncodingArgs(), ","),
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
		//Here goes my 3rd try to learn JGoodies Form
		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, pref:grow, 7dlu, right:pref, 3dlu, pref:grow", //columns
				""); //rows (none, dynamic)
		layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});
		DefaultFormBuilder mainPanel = new DefaultFormBuilder(layout);

		mainPanel.appendSeparator("VLC Transcoder Settings");
		mainPanel.append(hardwareAccel = new JCheckBox("Use hardware acceleration"), 3);
		hardwareAccel.setContentAreaFilled(false);
		mainPanel.append(experimentalCodecs = new JCheckBox("Enable experimental codecs"), 3);
		experimentalCodecs.setContentAreaFilled(false);

		mainPanel.append(audioSyncEnabled = new JCheckBox("Enable audio sync"), 3);
		audioSyncEnabled.setContentAreaFilled(false);
		mainPanel.append(subtitleEnabled = new JCheckBox("Enable Subtitles"), 3);
		subtitleEnabled.setSelected(true);
		audioSyncEnabled.setContentAreaFilled(false);

		mainPanel.append("Audio Language Priority", audioPri = new JTextField("jpn,eng"));
		mainPanel.append("Subtitle Language Priority", subtitlePri = new JTextField("eng,jpn"));

		//Developer stuff. Thoretically is temporary 
		mainPanel.appendSeparator("Advanced Settings");

		//Add scale as a subpanel because it has an awkward layout
		mainPanel.append("Video scale: ");
		FormLayout scaleLayout = new FormLayout("pref,3dlu,pref", "");
		DefaultFormBuilder scalePanel = new DefaultFormBuilder(scaleLayout);
		scalePanel.append(scale = new JTextField("" + scaleDefault));
		final JSlider scaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, (int) (scaleDefault * 10));
		scalePanel.append(scaleSlider);
		Hashtable<Integer, JLabel> scaleLabels = new Hashtable<Integer, JLabel>();
		scaleLabels.put(0, new JLabel("0.0"));
		scaleLabels.put(5, new JLabel("0.5"));
		scaleLabels.put(10, new JLabel("1.0"));
		scaleSlider.setLabelTable(scaleLabels);
		scaleSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				scale.setText(String.valueOf((double) scaleSlider.getValue() / 10));
			}
		});
		scale.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String typed = scale.getText();
				scaleSlider.setValue(0);
				if (!typed.matches("\\d+") || typed.length() > 3)
					return;
				scaleSlider.setValue(Integer.parseInt(typed) * 10);
			}
		});
		mainPanel.append(scalePanel.getPanel(), 3);

		//Allow user to choose codec
		mainPanel.nextLine();
		FormLayout codecLayout = new FormLayout(
				"right:pref, 3dlu, right:pref, 3dlu, pref:grow, 7dlu, right:pref, 3dlu, pref:grow, 7dlu, right:pref, 3dlu, pref:grow", //columns
				""); //rows (none, dynamic)
		codecLayout.setColumnGroups(new int[][]{{5, 9, 13}, {3, 7, 11}});
		DefaultFormBuilder codecPanel = new DefaultFormBuilder(codecLayout);
		codecPanel.append(new JLabel("<html>Codecs that VLC will use. <br>Good places to start:"
				+ "<br> XBox: wmv2, wma, asf"
				+ "<br> PS3: mp1v, mpga, mpeg</html>"));
		codecPanel.append("Video codec: ", codecVideo = new JTextField("wmv2"));
		codecPanel.append("Audio codec: ", codecAudio = new JTextField("wma"));
		codecPanel.append("Container: ", codecContainer = new JTextField("asf"));
		mainPanel.append(codecPanel.getPanel(), 7);

		//Audio sample rate
		mainPanel.append("<html>Audio sample rate<br>Potential Values: 44100 (unstable), 48000", new JTextField("48000"));

		return mainPanel.getPanel();
	}
}
