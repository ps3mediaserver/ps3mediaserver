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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.pms.network.HTTPResource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		//Xbox: wmv2, wma, asf (WORKING)
		//PS3: mp1v, mpga, mpeg1 (WORKING)
		ArrayList<String> args = new ArrayList<String>();
		
		//Codecs to use (mp2 AKA dvd format)
		args.add("venc=ffmpeg");
		args.add("vcodec=" + codecVideo.getText());
		args.add("acodec=" + codecAudio.getText());
		
		//Bitrate in kbit/s (TODO: Use global option?)
		args.add("vb=4096");
		args.add("ab=128");
		
		//Video scaling (TODO: Why is this needed?
		args.add("scale=" + scale.getText());
		
		//Channels (TODO: is this nessesary?)
		args.add("channels=2");
		
		//Static sample rate
		args.add("samplerate=48000");
		
		//Stream subtitiles to client
		//args.add("scodec=dvbs");
		//args.add("senc=dvbsub");
		
		//Hardcode subtitiles into video
		args.add("soverlay");
		
		//This has caused garbled audio, so only enable when told to
		if(audioSyncEnabled.isSelected())
			args.add("audio-sync");
		
		return StringUtils.join(args, ",");
	}

	protected String getMux() {
		return codecContainer.getText();
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
		
		//Useful for the more esoteric codecs people use
		if(experimentalCodecs.isSelected())
			cmdList.add("--sout-ffmpeg-strict=-2");

		//Stop the DOS box from appearing on windows
		if (isWindows)
			cmdList.add("--dummy-quiet");
		
		//File needs to be given before sout, otherwise vlc complains
		cmdList.add(fileName);
		
		if(subtitleEnabled.isSelected())
			cmdList.add("--sub-language=" + subtitlePri.getText());
		cmdList.add("--audio-language=" + audioPri.getText());
		
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
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		hardwareAccel = new JCheckBox("Use hardware acceleration");
		hardwareAccel.setContentAreaFilled(false);
		mainPanel.add(hardwareAccel);
		
		experimentalCodecs = new JCheckBox("Enable experimental codecs");
		experimentalCodecs.setContentAreaFilled(false);
		mainPanel.add(experimentalCodecs);
		
		audioSyncEnabled = new JCheckBox("Enable audio sync");
		audioSyncEnabled.setContentAreaFilled(false);
		mainPanel.add(audioSyncEnabled);
		
		//Try adding a label with a text field
		audioPri = genTextField("Audio Language Priority", "jpn,eng", mainPanel);
		subtitleEnabled = new JCheckBox("Enable Subtitles");
		subtitleEnabled.setSelected(true);
		mainPanel.add(subtitleEnabled);
		subtitlePri = genTextField("Subtitle Language Priority", "eng,jpn", mainPanel);
		
		//Add slider for scale
		JPanel sliderPanel = new JPanel();
		sliderPanel.add(new JLabel("Video scale: "));
		scale = new JTextField("" + scaleDefault);
		sliderPanel.add(scale);
		final JSlider scaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, (int)(scaleDefault*10));
		Hashtable scaleLabels = new Hashtable();
		scaleLabels.put(0, new JLabel("0.0") );
		scaleLabels.put(5, new JLabel("0.5") );
		scaleLabels.put(10, new JLabel("1.0") );
		scaleSlider.setLabelTable(scaleLabels);
		scaleSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				scale.setText(String.valueOf((double)scaleSlider.getValue()/10));
			}
		});
		scale.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String typed = scale.getText();
                scaleSlider.setValue(0);
                if(!typed.matches("\\d+") || typed.length() > 3)
                    return;
                scaleSlider.setValue(Integer.parseInt(typed)*10);
			}
		});
		sliderPanel.add(scaleSlider);
		mainPanel.add(sliderPanel);
		
		//Allow user to choose codec
		JPanel codecPanel = new JPanel();
		codecPanel.add(new JLabel("<html>Codecs that VLC will use. <br>Good places to start:" +
				"<br> XBox: wmv2, wma, asf" +
				"<br> PS3: mp1v, mpga, mpeg</html>"));
		codecVideo = genTextField("Video codec: ", "wmv2", codecPanel, 6);
		codecAudio = genTextField("Audio codec: ", "wma", codecPanel, 6);
		codecContainer = genTextField("Container: ", "asf", codecPanel, 6);
		mainPanel.add(codecPanel);
		
		return mainPanel;
	}
	
	protected JTextField genTextField(String labelText, String fieldText, JPanel target) {
		return genTextField(labelText, fieldText, target, 20);
	}
	
	protected JTextField genTextField(String labelText, String fieldText, JPanel target, int columns) {
		JPanel container = new JPanel();
		JLabel label = new JLabel(labelText);
		JTextField field = new JTextField(fieldText);
		field.setColumns(columns);
		label.setLabelFor(field);
	
		container.add(label);
		container.add(field);
		
		target.add(container);
		
		return field;
	}
}
