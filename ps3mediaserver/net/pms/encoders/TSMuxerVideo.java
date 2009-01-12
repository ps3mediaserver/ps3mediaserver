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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeIPCProcess;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;

public class TSMuxerVideo extends Player {

	public static final String ID = "tsmuxer"; //$NON-NLS-1$
	private PmsConfiguration configuration;
	
	public TSMuxerVideo(PmsConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public boolean excludeFormat(Format extension) {
		String m = extension.getMatchedId();
		return m != null && !m.equals("mkv") && !m.equals("ts") && !m.equals("m2ts") && !m.equals("mpg") && !m.equals("evo") && !m.equals("mpeg") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			&& !m.equals("vob") && !m.equals("m2v") && !m.equals("mts"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	public String[] args() {
		return null;
	}

	@Override
	public String executable() {
		return configuration.getTsmuxerPath();
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media, OutputParams params)
			throws IOException {
		
		
		String videoType = null;
		if (media.types == null) {
			media.types = new String [10000];
			String cmd [] = new String [] { executable(), fileName };
			params.log = true;
			ProcessWrapperImpl p = new ProcessWrapperImpl(cmd, params);
			p.run();
			List<String> results = p.getOtherResults();
			
			int id = 0;
			String type = null;
			if (results != null)
			for(String s:results) {
				if (s.startsWith("Track ID:")) { //$NON-NLS-1$
					id = Integer.parseInt(s.substring(9).trim());
				} else if (s.startsWith("Stream ID:") && id > 0) { //$NON-NLS-1$
					type = s.substring(10).trim();
					if (type.startsWith("V")) //$NON-NLS-1$
						videoType = type;
					media.types [id] = type;
				}
			}
		}
		
		String fps = null;
		if (media != null && configuration.isTsmuxerForceFps()) {
			fps = media.getValidFps(false);
		}
		
		for(int i=0;i<media.types.length;i++) {
			if (media.types[i] != null && media.types[i].startsWith("V")) { //$NON-NLS-1$
				videoType = media.types[i];
			}
		}
		
		
	/*	if (PMS.get().isTsmuxer_preremux_pcm() && media.losslessaudio) {
			MEncoderVideo mev = new MEncoderVideo();
			params.losslessaudio = true;
			params.no_videoencode = true;
			params.forceType = videoType;
			params.forceFps = media.getValidFps(false);
			return mev.launchTranscode(fileName, media, params);
		}
		
		if (PMS.get().isTsmuxer_preremux_ac3()) {
			MEncoderVideo mev = new MEncoderVideo();
			params.lossyaudio = true;
			params.no_videoencode = true;
			params.forceType = videoType;
			params.forceFps = media.getValidFps(false);
			return mev.launchTranscode(fileName, media, params);
		}
		*/
		
		PipeIPCProcess ffVideoPipe = null;
		ProcessWrapperImpl ffVideo = null;
		
		PipeIPCProcess ffAudioPipe = null;
		ProcessWrapperImpl ffAudio = null;
		
		
		if (this instanceof TsMuxerAudio) {
			
			ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "fakevideo", System.currentTimeMillis() + "videoout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
			String ffmpegLPCMextract [] = new String [] { configuration.getFfmpegPath(), "-t", "" +params.timeend, "-loop_input", "-i", "win32/fake.jpg", "-f", "h264", "-vcodec", "libx264", "-an", "-y", ffVideoPipe.getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
			//videoType = "V_MPEG-2";
			videoType = "V_MPEG4/ISO/AVC"; //$NON-NLS-1$
			if (params.timeend < 1) {
				ffmpegLPCMextract [1] = "-title"; //$NON-NLS-1$
				ffmpegLPCMextract [2] = "dummy"; //$NON-NLS-1$
			}
				
			OutputParams ffparams = new OutputParams(PMS.getConfiguration());
			ffparams.maxBufferSize = 1;
			ffVideo = new ProcessWrapperImpl(ffmpegLPCMextract, ffparams);
			
			if (fileName.toLowerCase().endsWith(".flac") && media != null && media.bitsperSample >=24 && media.getSampleRate()%48000==0) { //$NON-NLS-1$
				ffAudioPipe = new PipeIPCProcess(System.currentTimeMillis() + "flacaudio", System.currentTimeMillis() + "audioout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
				String flacCmd [] = new String [] { configuration.getFlacPath(), "--output-name=" + ffAudioPipe.getInputPipe(), "-d", "-F", fileName }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				ffparams = new OutputParams(PMS.getConfiguration());
				ffparams.maxBufferSize = 1;
				ffAudio = new ProcessWrapperImpl(flacCmd, ffparams);
			} else {
				ffAudioPipe = new PipeIPCProcess(System.currentTimeMillis() + "mlpaudio", System.currentTimeMillis() + "audioout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
				String depth = "pcm_s16le"; //$NON-NLS-1$
				String rate = "48000"; //$NON-NLS-1$
				if (media != null && media.bitsperSample >=24)
					depth = "pcm_s24le"; //$NON-NLS-1$
				if (media != null && media.getSampleRate() >48000)
					rate = "96000"; //$NON-NLS-1$
				String flacCmd [] = new String [] { configuration.getFfmpegPath(), "-ar", rate, "-i", fileName , "-f", "wav", "-acodec", depth, "-y", ffAudioPipe.getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				
				ffparams = new OutputParams(PMS.getConfiguration());
				ffparams.maxBufferSize = 1;
				ffAudio = new ProcessWrapperImpl(flacCmd, ffparams);
			}
			
		} else {
		
			if ((configuration.isTsmuxerPreremuxPcm() && media.losslessaudio) || configuration.isTsmuxerPreremuxAc3()) {
				ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegvideo", System.currentTimeMillis() + "videoout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
				String outputType = "h264"; //$NON-NLS-1$
				if (videoType.indexOf("MPEG-2") > -1) //$NON-NLS-1$
					outputType = "mpeg2video"; //$NON-NLS-1$
				String ffmpegLPCMextract [] = new String [] { configuration.getFfmpegPath(), "-ss", "0", "-i", fileName, "-f", outputType, "-vbsf", "h264_mp4toannexb", "-vcodec", "copy", "-an", "-y", ffVideoPipe.getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
				//String ffmpegLPCMextract []= new String [] { PMS.get().getMPlayerPath(), "-ss", "0", fileName, "-dumpvideo", "-dumpfile", ffVideoPipe.getInputPipe()};
				
				if (params.timeseek > 0)
					ffmpegLPCMextract [2] = "" + params.timeseek; //$NON-NLS-1$
				OutputParams ffparams = new OutputParams(PMS.getConfiguration());
				ffparams.maxBufferSize = 1;
				ffVideo = new ProcessWrapperImpl(ffmpegLPCMextract, ffparams);
				
				
				ffAudioPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegaudio", System.currentTimeMillis() + "audioout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
				if (configuration.isTsmuxerPreremuxPcm() && media.losslessaudio) {
					ffmpegLPCMextract = new String [] { configuration.getFfmpegPath(), "-ss", "0", "-i", fileName, "-f", "wav", "-acodec", "pcm_s16le", "-ac", "6", "-vn", "-y", ffAudioPipe.getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
				} else if (configuration.isTsmuxerPreremuxAc3())
					ffmpegLPCMextract = new String [] { configuration.getFfmpegPath(), "-ss", "0", "-i", fileName, "-f", "ac3", "-ab", "" + configuration.getAudioBitrate()*1000 + "", "-ac", "6", "-vn", "-y", ffAudioPipe.getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
				
				
				//ffmpegLPCMextract = new String [] { PMS.get().getMPlayerPath(), "-ss", "0", fileName, "-channels", "6", "-vc", "null",  "-vo", "null", "-quiet", "-ao", "pcm:fast:waveheader:file=" + ffAudioPipe.getInputPipe()};
				if (params.timeseek > 0) {
					ffmpegLPCMextract [2] = "" + params.timeseek; //$NON-NLS-1$
				}
				ffparams = new OutputParams(PMS.getConfiguration());
				ffparams.maxBufferSize = 1;
				ffAudio = new ProcessWrapperImpl(ffmpegLPCMextract, ffparams);
			}
			
		}
		
		File f = new File(configuration.getTempFolder(), "pms-tsmuxer.meta"); //$NON-NLS-1$
		params.log = false;
		PrintWriter pw = new PrintWriter(f);
		pw.print("MUXOPT --no-pcr-on-video-pid "); //$NON-NLS-1$
		//if (ffVideo == null)
			pw.print("--new-audio-pes "); //$NON-NLS-1$
		if (ffVideo != null)
				pw.print("--no-asyncio "); //$NON-NLS-1$
		pw.print(" --vbr"); //$NON-NLS-1$
		if (params.timeseek > 0 && ffVideoPipe == null) {
			pw.print(" --cut-start=" + params.timeseek + "s "); //$NON-NLS-1$ //$NON-NLS-2$
			params.timeseek = 0;
		}
		pw.println(" --vbv-len=500"); //$NON-NLS-1$
		
		
		
		
			
			
			boolean audio_consumed = false;
			boolean video_consumed = false;
			boolean lpcm_forced = false;
			for(int i=0;i<media.types.length;i++) {
				if (media.types[i] != null && media.types[i].equals("A_LPCM")) { //$NON-NLS-1$
					lpcm_forced = true;
					break;
				}
			}
			
				
		
			for(int i=0;i<media.types.length;i++) {
				if (media.types[i] != null) {
					if (ffVideoPipe == null && !video_consumed && media.types[i].startsWith("V")) { //$NON-NLS-1$
						String insertSEI = "insertSEI, "; //$NON-NLS-1$
						
						pw.println(media.types[i] + ", \"" + fileName + "\", " + (fps!=null?("fps=" +fps + ", "):"") +"level=4.1, " + insertSEI + "contSPS, track=" + i); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
						video_consumed = true;
					}
					if (ffAudioPipe == null && !audio_consumed && media.types[i].startsWith("A")) { //$NON-NLS-1$
						if (!lpcm_forced || (lpcm_forced && media.types[i].equals("A_LPCM"))) { //$NON-NLS-1$
							pw.println(media.types[i] + ", \"" + fileName + "\", track=" + i); //$NON-NLS-1$ //$NON-NLS-2$
							audio_consumed = true;
						}
					}
				}
			}
			if (ffVideoPipe != null) {
				String videoparams = "level=4.1, insertSEI, contSPS, track=1"; //$NON-NLS-1$
				if (this instanceof TsMuxerAudio)
					videoparams = "track=224"; //$NON-NLS-1$
				pw.println(videoType + ", \"" + ffVideoPipe.getOutputPipe() + "\", "  + (fps!=null?("fps=" +fps + ", "):"") + videoparams); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
			if (ffAudioPipe != null) {
				String type = "A_AC3"; //$NON-NLS-1$
				if ((configuration.isTsmuxerPreremuxPcm() && media.losslessaudio) || this instanceof TsMuxerAudio)
					type = "A_LPCM"; //$NON-NLS-1$
				pw.println(type + ", \"" + ffAudioPipe.getOutputPipe() + "\", track=2"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			pw.close();
			
		
			
	
		
		PipeProcess tsPipe = new PipeProcess(System.currentTimeMillis() + "tsmuxerout.ts"); //$NON-NLS-1$
		String cmd [] = new String [] { executable(), f.getAbsolutePath(), tsPipe.getInputPipe() };
		ProcessWrapperImpl p = new ProcessWrapperImpl(cmd, params);
		params.maxBufferSize = 100;
		params.input_pipes[0] = tsPipe;
		
		ProcessWrapper pipe_process = tsPipe.getPipeProcess();
		p.attachProcess(pipe_process);
		pipe_process.runInNewThread();
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) { }
		tsPipe.deleteLater();
		
		if (ffVideoPipe != null) {
			ProcessWrapper ff_pipe_process = ffVideoPipe.getPipeProcess();
			p.attachProcess(ff_pipe_process);
			ff_pipe_process.runInNewThread();
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) { }
			ffVideoPipe.deleteLater();
			
			p.attachProcess(ffVideo);
			ffVideo.runInNewThread();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) { }
		}
		
		if (ffAudioPipe != null) {
			ProcessWrapper ff_pipe_process = ffAudioPipe.getPipeProcess();
			p.attachProcess(ff_pipe_process);
			ff_pipe_process.runInNewThread();
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) { }
			ffAudioPipe.deleteLater();
			
			p.attachProcess(ffAudio);
			ffAudio.runInNewThread();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) { }
		}
		
		
		
		p.runInNewThread();
		return p;
	}

	@Override
	public String mimeType() {
		return "video/mpeg"; //$NON-NLS-1$
	}

	@Override
	public String name() {
		return "TsMuxer"; //$NON-NLS-1$
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}
	
	private JCheckBox tsmuxerforcefps;
	private JCheckBox tsmuxerforcepcm;
	private JCheckBox tsmuxerforceac3;

	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout(
                "left:pref, 0:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        
        JComponent cmp = builder.addSeparator(Messages.getString("TSMuxerVideo.3"),  cc.xyw(2, 1, 1)); //$NON-NLS-1$
        cmp = (JComponent) cmp.getComponent(0);
        cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
        
        tsmuxerforcefps = new JCheckBox(Messages.getString("TSMuxerVideo.2")); //$NON-NLS-1$
        tsmuxerforcefps.setContentAreaFilled(false);
        if (configuration.isTsmuxerForceFps())
        	tsmuxerforcefps.setSelected(true);
        tsmuxerforcefps.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				configuration.setTsmuxerForceFps(e.getStateChange() == ItemEvent.SELECTED);
 			}
        	
        });
        builder.add(tsmuxerforcefps, cc.xy(2, 3));
        
        tsmuxerforcepcm = new JCheckBox(Messages.getString("TSMuxerVideo.1")); //$NON-NLS-1$
        tsmuxerforcepcm.setContentAreaFilled(false);
        if (configuration.isTsmuxerPreremuxPcm())
        	tsmuxerforcepcm.setSelected(true);
        //tsmuxerforcepcm.setEnabled(false);
        tsmuxerforcepcm.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				configuration.setTsmuxerPreremuxPcm(e.getStateChange() == ItemEvent.SELECTED);
 			}
        	
        });
        builder.add(tsmuxerforcepcm, cc.xy(2, 5));
        
        tsmuxerforceac3 = new JCheckBox(Messages.getString("TSMuxerVideo.0")); //$NON-NLS-1$
        tsmuxerforceac3.setContentAreaFilled(false);
        if (configuration.isTsmuxerPreremuxAc3())
        	tsmuxerforceac3.setSelected(true);
        //tsmuxerforcepcm.setEnabled(false);
        tsmuxerforceac3.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				configuration.setTsmuxerPreremuxAc3(e.getStateChange() == ItemEvent.SELECTED);
 			}
        	
        });
        builder.add(tsmuxerforceac3, cc.xy(2, 7));
        
        return builder.getPanel();
	}

}
