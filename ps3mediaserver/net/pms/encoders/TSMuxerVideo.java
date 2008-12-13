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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeIPCProcess;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;

public class TSMuxerVideo extends Player {

	public static final String ID = "tsmuxer";
	
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
		return PMS.get().getTsmuxerPath();
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
			ArrayList<String> results = p.getOtherResults();
			
			int id = 0;
			String type = null;
			for(String s:results) {
				if (s.startsWith("Track ID:")) {
					id = Integer.parseInt(s.substring(9).trim());
				} else if (s.startsWith("Stream ID:") && id > 0) {
					type = s.substring(10).trim();
					if (type.startsWith("V"))
						videoType = type;
					media.types [id] = type;
				}
			}
		}
		
		String fps = null;
		if (media != null && PMS.get().isTsmuxer_forcefps()) {
			fps = media.getValidFps(false);
		}
		
		for(int i=0;i<media.types.length;i++) {
			if (media.types[i] != null && media.types[i].startsWith("V")) {
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
		
		if ((PMS.get().isTsmuxer_preremux_pcm() && media.losslessaudio) || PMS.get().isTsmuxer_preremux_ac3()) {
			ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegvideo", System.currentTimeMillis() + "videoout", false, true);
			String outputType = "h264";
			if (videoType.indexOf("MPEG-2") > -1)
				outputType = "mpeg2video";
			String ffmpegLPCMextract [] = new String [] { PMS.get().getFFmpegPath(), "-ss", "0", "-i", fileName, "-f", outputType, "-vbsf", "h264_mp4toannexb", "-vcodec", "copy", "-an", "-y", ffVideoPipe.getInputPipe() };
			//String ffmpegLPCMextract []= new String [] { PMS.get().getMPlayerPath(), "-ss", "0", fileName, "-dumpvideo", "-dumpfile", ffVideoPipe.getInputPipe()};
			
			if (params.timeseek > 0)
				ffmpegLPCMextract [2] = "" + params.timeseek;
			OutputParams ffparams = new OutputParams();
			ffparams.maxBufferSize = 1;
			ffVideo = new ProcessWrapperImpl(ffmpegLPCMextract, ffparams);
			
			ffAudioPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegaudio", System.currentTimeMillis() + "audioout", false, true);
			if (PMS.get().isTsmuxer_preremux_pcm() && media.losslessaudio) {
				ffmpegLPCMextract = new String [] { PMS.get().getFFmpegPath(), "-ss", "0", "-i", fileName, "-f", "wav", "-acodec", "pcm_s16le", "-ac", "6", "-vn", "-y", ffAudioPipe.getInputPipe() };
			} else if (PMS.get().isTsmuxer_preremux_ac3())
				ffmpegLPCMextract = new String [] { PMS.get().getFFmpegPath(), "-ss", "0", "-i", fileName, "-f", "ac3", "-ab", "" + PMS.get().getAudiobitrate()*1000 + "", "-ac", "6", "-vn", "-y", ffAudioPipe.getInputPipe() };
			
			
			//ffmpegLPCMextract = new String [] { PMS.get().getMPlayerPath(), "-ss", "0", fileName, "-channels", "6", "-vc", "null",  "-vo", "null", "-quiet", "-ao", "pcm:fast:waveheader:file=" + ffAudioPipe.getInputPipe()};
			if (params.timeseek > 0) {
				ffmpegLPCMextract [2] = "" + params.timeseek;
			}
			ffparams = new OutputParams();
			ffparams.maxBufferSize = 1;
			ffAudio = new ProcessWrapperImpl(ffmpegLPCMextract, ffparams);
		}
		
		
		File f = new File(PMS.get().getTempFolder(), "pms-tsmuxer.meta");
		params.log = false;
		PrintWriter pw = new PrintWriter(f);
		pw.print("MUXOPT --no-pcr-on-video-pid ");
		//if (ffVideo == null)
			pw.print("--new-audio-pes ");
		pw.print(" --vbr");
		if (params.timeseek > 0 && ffVideoPipe == null) {
			pw.print(" --cut-start=" + params.timeseek + "s ");
			params.timeseek = 0;
		}
		pw.println(" --vbv-len=500");
		
		
		
		
			
			
			boolean audio_consumed = false;
			boolean video_consumed = false;
			boolean lpcm_forced = false;
			for(int i=0;i<media.types.length;i++) {
				if (media.types[i] != null && media.types[i].equals("A_LPCM")) {
					lpcm_forced = true;
					break;
				}
			}
			
				
		
			for(int i=0;i<media.types.length;i++) {
				if (media.types[i] != null) {
					if (ffVideoPipe == null && !video_consumed && media.types[i].startsWith("V")) {
						String insertSEI = "insertSEI, ";
						
						pw.println(media.types[i] + ", \"" + fileName + "\", " + (fps!=null?("fps=" +fps + ", "):"") +"level=4.1, " + insertSEI + "contSPS, track=" + i);
						video_consumed = true;
					}
					if (ffAudioPipe == null && !audio_consumed && media.types[i].startsWith("A")) {
						if (!lpcm_forced || (lpcm_forced && media.types[i].equals("A_LPCM"))) {
							pw.println(media.types[i] + ", \"" + fileName + "\", track=" + i);
							audio_consumed = true;
						}
					}
				}
			}
			if (ffVideoPipe != null) {
				pw.println(videoType + ", \"" + ffVideoPipe.getOutputPipe() + "\", "  + (fps!=null?("fps=" +fps + ", "):"") + "level=4.1, insertSEI, contSPS, track=1");
			}
			if (ffAudioPipe != null) {
				String type = "A_AC3";
				if (PMS.get().isTsmuxer_preremux_pcm() && media.losslessaudio)
					type = "A_LPCM";
				pw.println(type + ", \"" + ffAudioPipe.getOutputPipe() + "\", track=2");
			}
			pw.close();
			
		
			
	
		
		PipeProcess tsPipe = new PipeProcess(System.currentTimeMillis() + "tsmuxerout.ts");
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
		return "video/mpeg";
	}

	@Override
	public String name() {
		return "TsMuxer";
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
                "left:pref, 0:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        
        builder.addSeparator("Video decoder settings for TsMuxer engine only",  cc.xyw(2, 1, 1));
        
        tsmuxerforcefps = new JCheckBox("Force FPS parsed from FFmpeg in the meta file");
        tsmuxerforcefps.setContentAreaFilled(false);
        if (PMS.get().isTsmuxer_forcefps())
        	tsmuxerforcefps.setSelected(true);
        tsmuxerforcefps.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.get().setTsmuxer_forcefps(e.getStateChange() == ItemEvent.SELECTED);
 			}
        	
        });
        builder.add(tsmuxerforcefps, cc.xy(2, 3));
        
        tsmuxerforcepcm = new JCheckBox("Force PCM remuxing with DTS/FLAC audio [Careful, seeking not working well]");
        tsmuxerforcepcm.setContentAreaFilled(false);
        if (PMS.get().isTsmuxer_preremux_pcm())
        	tsmuxerforcepcm.setSelected(true);
        //tsmuxerforcepcm.setEnabled(false);
        tsmuxerforcepcm.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.get().setTsmuxer_preremux_pcm(e.getStateChange() == ItemEvent.SELECTED);
 			}
        	
        });
        builder.add(tsmuxerforcepcm, cc.xy(2, 5));
        
        tsmuxerforceac3 = new JCheckBox("Force AC3 remuxing with all files [Careful, seeking not working well]");
        tsmuxerforceac3.setContentAreaFilled(false);
        if (PMS.get().isTsmuxer_preremux_ac3())
        	tsmuxerforceac3.setSelected(true);
        //tsmuxerforcepcm.setEnabled(false);
        tsmuxerforceac3.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.get().setTsmuxer_preremux_ac3(e.getStateChange() == ItemEvent.SELECTED);
 			}
        	
        });
        builder.add(tsmuxerforceac3, cc.xy(2, 7));
        
        return builder.getPanel();
	}

}
