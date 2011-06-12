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

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.InputFile;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeIPCProcess;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.io.StreamModifier;
import net.pms.util.CodecUtil;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TSMuxerVideo extends Player {

	public static final String ID = "tsmuxer"; //$NON-NLS-1$
	private PmsConfiguration configuration;
	
	public TSMuxerVideo(PmsConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public boolean excludeFormat(Format extension) {
		String m = extension.getMatchedId();
		return m != null && !m.equals("mp4") && !m.equals("mkv") && !m.equals("ts") && !m.equals("tp") && !m.equals("m2ts") && !m.equals("m2t") && !m.equals("mpg") && !m.equals("evo") && !m.equals("mpeg") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			&& !m.equals("vob") && !m.equals("m2v") && !m.equals("mts") && !m.equals("mov"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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

		setAudioAndSubs(fileName, media, params, configuration);
		
		PipeIPCProcess ffVideoPipe = null;
		ProcessWrapperImpl ffVideo = null;
		
		PipeIPCProcess ffAudioPipe [] = null;
		ProcessWrapperImpl ffAudio [] = null;
		
		String fps = media.getValidFps(false);
		String videoType = "V_MPEG4/ISO/AVC"; //$NON-NLS-1$
		if (media != null && media.codecV != null && media.codecV.equals("mpeg2video")) { //$NON-NLS-1$
			videoType = "V_MPEG-2"; //$NON-NLS-1$
		}
		
		if (this instanceof TsMuxerAudio && media.getFirstAudioTrack() != null) {
			
			ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "fakevideo", System.currentTimeMillis() + "videoout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
			String ffmpegLPCMextract [] = new String [] { configuration.getFfmpegPath(), "-t", "" +params.timeend, "-loop_input", "-i", "resources/images/fake.jpg", "-qcomp", "0.6", "-qmin", "10", "-qmax", "51", "-qdiff", "4" ,"-me_range", "4", "-f", "h264", "-vcodec", "libx264", "-an", "-y", ffVideoPipe.getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
			//videoType = "V_MPEG-2";
			videoType = "V_MPEG4/ISO/AVC"; //$NON-NLS-1$
			if (params.timeend < 1) {
				ffmpegLPCMextract [1] = "-title"; //$NON-NLS-1$
				ffmpegLPCMextract [2] = "dummy"; //$NON-NLS-1$
			}
				
			OutputParams ffparams = new OutputParams(PMS.getConfiguration());
			ffparams.maxBufferSize = 1;
			ffVideo = new ProcessWrapperImpl(ffmpegLPCMextract, ffparams);
			
			if (fileName.toLowerCase().endsWith(".flac") && media != null && media.getFirstAudioTrack().bitsperSample >=24 && media.getFirstAudioTrack().getSampleRate()%48000==0) { //$NON-NLS-1$
				ffAudioPipe = new PipeIPCProcess [1];
				ffAudioPipe[0] = new PipeIPCProcess(System.currentTimeMillis() + "flacaudio", System.currentTimeMillis() + "audioout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
				String flacCmd [] = new String [] { configuration.getFlacPath(), "--output-name=" + ffAudioPipe[0].getInputPipe(), "-d", "-F", fileName }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				ffparams = new OutputParams(PMS.getConfiguration());
				ffparams.maxBufferSize = 1;
				ffAudio = new ProcessWrapperImpl[1];
				ffAudio[0] = new ProcessWrapperImpl(flacCmd, ffparams);
			} else {
				ffAudioPipe = new PipeIPCProcess [1];
				ffAudioPipe[0] = new PipeIPCProcess(System.currentTimeMillis() + "mlpaudio", System.currentTimeMillis() + "audioout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
				String depth = "pcm_s16le"; //$NON-NLS-1$
				String rate = "48000"; //$NON-NLS-1$
				if (media != null && media.getFirstAudioTrack().bitsperSample >=24)
					depth = "pcm_s24le"; //$NON-NLS-1$
				if (media != null && media.getFirstAudioTrack().getSampleRate() >48000)
					rate = "" + media.getFirstAudioTrack().getSampleRate(); //$NON-NLS-1$
				String flacCmd [] = new String [] { configuration.getFfmpegPath(), "-ar", rate, "-i", fileName , "-f", "wav", "-acodec", depth, "-y", ffAudioPipe[0].getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				
				ffparams = new OutputParams(PMS.getConfiguration());
				ffparams.maxBufferSize = 1;
				ffAudio = new ProcessWrapperImpl[1];
				ffAudio[0] = new ProcessWrapperImpl(flacCmd, ffparams);
			}
			
		} else {
			
			params.waitbeforestart = 5000;
			params.manageFastStart();

			String mencoderPath = configuration.getMencoderPath();
			boolean isMultiCore = configuration.getNumberOfCpuCores() > 1;

			// Figure out which version of MEncoder we want to use
			if (
				(media.muxingMode != null && media.muxingMode.equals("Header stripping")) ||
				(media.getFirstAudioTrack() != null && media.getFirstAudioTrack().muxingModeAudio != null && media.getFirstAudioTrack().muxingModeAudio.equals("Header stripping"))
			) {
			// Use the newer version of MEncoder
				if (isMultiCore && configuration.getMencoderMT()) {
					if (new File(configuration.getMencoderAlternateMTPath()).exists()) {
						mencoderPath = configuration.getMencoderAlternateMTPath();
					}
				} else {
					if (new File(configuration.getMencoderAlternatePath()).exists()) {
						mencoderPath = configuration.getMencoderAlternatePath();
					}
				}
			} else if (isMultiCore && configuration.getMencoderMT()) {
			// Use the older MEncoder with multithreading
				if (new File(configuration.getMencoderMTPath()).exists()) {
					mencoderPath = configuration.getMencoderMTPath();
				}
			} else {
			// Use the older MEncoder
				if (new File(configuration.getMencoderPath()).exists()) {
					mencoderPath = configuration.getMencoderPath();
				}
			}

			ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegvideo", System.currentTimeMillis() + "videoout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
			String ffmpegLPCMextract [] = new String [] { mencoderPath, "-ss", "0", fileName, "-quiet", "-quiet", "-really-quiet", "-msglevel", "statusline=2", "-ovc", "copy", "-nosound", "-mc", "0", "-noskip", "-of", "rawvideo", "-o", ffVideoPipe.getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$

			if (fileName.toLowerCase().endsWith(".evo")) { //$NON-NLS-1$
				ffmpegLPCMextract[4] = "-psprobe"; //$NON-NLS-1$
				ffmpegLPCMextract[5] = "1000000"; //$NON-NLS-1$
			}

			if (params.stdin != null)
				ffmpegLPCMextract[3] = "-"; //$NON-NLS-1$

			InputFile newInput = new InputFile();
			newInput.filename = fileName;
			newInput.push = params.stdin;

			if (media != null) { //$NON-NLS-1$ //$NON-NLS-2$
				boolean compat = (media.isVideoPS3Compatible(newInput) || !params.mediaRenderer.isH264Level41Limited());
				if (!compat && params.mediaRenderer.isPS3())
					PMS.minimal("The video will not play or show a black screen on the ps3...");
				if (media.h264_annexB != null && media.h264_annexB.length > 0) {
					StreamModifier sm = new StreamModifier();
					sm.setHeader(media.h264_annexB);
					sm.setH264_annexb(true);
					ffVideoPipe.setModifier(sm);
				}
			}

			if (params.timeseek > 0)
				ffmpegLPCMextract [2] = "" + params.timeseek; //$NON-NLS-1$
			OutputParams ffparams = new OutputParams(PMS.getConfiguration());
			ffparams.maxBufferSize = 1;
			ffparams.stdin = params.stdin;
			ffVideo = new ProcessWrapperImpl(ffmpegLPCMextract, ffparams);

			int numAudioTracks = 1;
			if (media != null && media.audioCodes != null && media.audioCodes.size() > 1 && configuration.isMuxAllAudioTracks())
				numAudioTracks = media.audioCodes.size();

			boolean singleMediaAudio = media != null && media.audioCodes.size() <= 1;

			if (params.aid != null) {
				if (numAudioTracks <= 1) {
					ffAudioPipe = new PipeIPCProcess [numAudioTracks];
					ffAudioPipe[0] = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegaudio01", System.currentTimeMillis() + "audioout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
					if ((configuration.isMencoderUsePcm() || configuration.isDTSEmbedInPCM()) && (params.aid.isLossless() || params.aid.isDTS()) && params.mediaRenderer.isDTSPlayable()) {
						StreamModifier sm = new StreamModifier();
						sm.setPcm(true);
						sm.setDtsembed(configuration.isDTSEmbedInPCM() && params.aid.isDTS()); //$NON-NLS-1$ //$NON-NLS-2$
						sm.setNbchannels(sm.isDtsembed()?2:CodecUtil.getRealChannelCount(configuration, params.aid));
						sm.setSampleFrequency(params.aid.getSampleRate()<48000?48000:params.aid.getSampleRate());
						sm.setBitspersample(16);
						String mixer = CodecUtil.getMixerOutput(!sm.isDtsembed(), sm.getNbchannels());
						ffmpegLPCMextract = new String [] { mencoderPath, "-ss", "0", fileName, "-quiet", "-quiet", "-really-quiet", "-msglevel", "statusline=2", "-channels", "" + sm.getNbchannels(), "-ovc", "copy", "-of", "rawaudio", "-mc", sm.isDtsembed()?"0.1":"0", "-noskip", "-oac", sm.isDtsembed()?"copy":"pcm", mixer!=null?"-af":"-quiet", mixer!=null?mixer:"-quiet", singleMediaAudio?"-quiet":"-aid", singleMediaAudio?"-quiet":("" + params.aid.id), "-srate", "48000", "-o", ffAudioPipe[0].getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$ //$NON-NLS-19$ //$NON-NLS-20$ //$NON-NLS-21$ //$NON-NLS-22$ //$NON-NLS-23$ //$NON-NLS-24$ //$NON-NLS-25$ //$NON-NLS-26$ //$NON-NLS-27$ //$NON-NLS-28$ //$NON-NLS-29$
						if (!params.mediaRenderer.isMuxDTSToMpeg())
							ffAudioPipe[0].setModifier(sm);
					} else {
						ffmpegLPCMextract = new String [] {
							mencoderPath, "-ss", "0", fileName, //$NON-NLS-1$ //$NON-NLS-2$
							"-quiet", "-quiet", "-really-quiet", "-msglevel", "statusline=2", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							"-channels", "" + CodecUtil.getAC3ChannelCount(configuration, params.aid), //$NON-NLS-1$ //$NON-NLS-2$
							"-ovc", "copy", "-of", "rawaudio", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							"-mc", "0",	"-noskip", "-oac", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							(params.aid.isAC3() && configuration.isRemuxAC3()) ? "copy" : "lavc", //$NON-NLS-1$ //$NON-NLS-2$
							params.aid.isAC3() ? "-fafmttag" : "-quiet", //$NON-NLS-1$ //$NON-NLS-2$
							params.aid.isAC3() ? "0x2000" : "-quiet", "-lavcopts", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							"acodec=" + (configuration.isMencoderAc3Fixed() ? "ac3_fixed" : "ac3") + ":abitrate=" + CodecUtil.getAC3Bitrate(configuration, params.aid), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							"-af", "lavcresample=48000", "-srate", "48000", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							singleMediaAudio ? "-quiet" : "-aid", //$NON-NLS-1$ //$NON-NLS-2$
							singleMediaAudio ? "-quiet" : ("" + params.aid.id), //$NON-NLS-1$
							"-o", ffAudioPipe[0].getInputPipe() //$NON-NLS-1$
						};
					}
					
					if (fileName.toLowerCase().endsWith(".evo")) { //$NON-NLS-1$
						ffmpegLPCMextract[4] = "-psprobe"; //$NON-NLS-1$
						ffmpegLPCMextract[5] = "1000000"; //$NON-NLS-1$
					}

					if (params.stdin != null)
						ffmpegLPCMextract[3] = "-"; //$NON-NLS-1$

					if (params.timeseek > 0) {
						ffmpegLPCMextract [2] = "" + params.timeseek; //$NON-NLS-1$
					}
					ffparams = new OutputParams(PMS.getConfiguration());
					ffparams.maxBufferSize = 1;
					ffparams.stdin = params.stdin;
					ffAudio = new ProcessWrapperImpl[numAudioTracks];
					ffAudio[0] = new ProcessWrapperImpl(ffmpegLPCMextract, ffparams);
				} else {
					ffAudioPipe = new PipeIPCProcess [numAudioTracks];
					ffAudio = new ProcessWrapperImpl[numAudioTracks];
					for(int i=0;i<media.audioCodes.size();i++) {
						DLNAMediaAudio audio = media.audioCodes.get(i);
						ffAudioPipe[i] = new PipeIPCProcess(System.currentTimeMillis() + "ffmpeg" + i, System.currentTimeMillis() + "audioout" + i, false, true); //$NON-NLS-1$ //$NON-NLS-2$
						if ((audio.isLossless() || audio.isDTS()) && (configuration.isMencoderUsePcm() || configuration.isDTSEmbedInPCM()) && params.mediaRenderer.isDTSPlayable()) {
							StreamModifier sm = new StreamModifier();
							sm.setPcm(true);
							sm.setDtsembed(configuration.isDTSEmbedInPCM() && audio.isDTS()); //$NON-NLS-1$ //$NON-NLS-2$
							sm.setNbchannels(sm.isDtsembed()?2:CodecUtil.getRealChannelCount(configuration, audio));
							sm.setSampleFrequency(48000);
							sm.setBitspersample(16);
							if (!params.mediaRenderer.isMuxDTSToMpeg())
								ffAudioPipe[i].setModifier(sm);
							String mixer = CodecUtil.getMixerOutput(!sm.isDtsembed(), sm.getNbchannels());
							ffmpegLPCMextract = new String [] { mencoderPath, "-ss", "0", fileName, "-quiet", "-quiet", "-really-quiet", "-msglevel", "statusline=2", "-channels", "" + sm.getNbchannels(), "-ovc", "copy", "-of", "rawaudio", "-mc", sm.isDtsembed()?"0.1":"0", "-noskip", "-oac", sm.isDtsembed()?"copy":"pcm", mixer!=null?"-af":"-quiet", mixer!=null?mixer:"-quiet", singleMediaAudio?"-quiet":"-aid", singleMediaAudio?"-quiet":("" + audio.id), "-srate", "48000", "-o", ffAudioPipe[i].getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$ //$NON-NLS-19$ //$NON-NLS-20$ //$NON-NLS-21$ //$NON-NLS-22$ //$NON-NLS-23$ //$NON-NLS-24$ //$NON-NLS-25$ //$NON-NLS-26$ //$NON-NLS-27$ //$NON-NLS-28$ //$NON-NLS-29$
						} else {
							ffmpegLPCMextract = new String [] {
								mencoderPath, "-ss", "0", fileName, //$NON-NLS-1$ //$NON-NLS-2$
								"-quiet", "-quiet", "-really-quiet", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								"-msglevel", "statusline=2", "-channels", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								"" + CodecUtil.getAC3ChannelCount(configuration, audio), //$NON-NLS-1$
								"-ovc", "copy", "-of", "rawaudio", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								"-mc", "0", "-noskip", "-oac", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								(audio.isAC3() && configuration.isRemuxAC3()) ? "copy" : "lavc", //$NON-NLS-1$ //$NON-NLS-2$
								audio.isAC3() ? "-fafmttag" : "-quiet", //$NON-NLS-1$ //$NON-NLS-2$
								audio.isAC3() ? "0x2000" : "-quiet", //$NON-NLS-1$ //$NON-NLS-2$
								"-lavcopts", //$NON-NLS-1$ //$NON-NLS-2$
								"acodec=" + (configuration.isMencoderAc3Fixed() ? "ac3_fixed" : "ac3") + ":abitrate=" + CodecUtil.getAC3Bitrate(configuration, audio), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								"-af", "lavcresample=48000", "-srate", "48000", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								singleMediaAudio ? "-quiet" : "-aid", //$NON-NLS-1$ //$NON-NLS-2$
								singleMediaAudio ? "-quiet" : ("" + audio.id), //$NON-NLS-1$ //$NON-NLS-2$
								"-o", ffAudioPipe[i].getInputPipe() //$NON-NLS-1$
							};
						}

						if (fileName.toLowerCase().endsWith(".evo")) { //$NON-NLS-1$
							ffmpegLPCMextract[4] = "-psprobe"; //$NON-NLS-1$
							ffmpegLPCMextract[5] = "1000000"; //$NON-NLS-1$
						}

						if (params.stdin != null)
							ffmpegLPCMextract[3] = "-"; //$NON-NLS-1$

						if (params.timeseek > 0) {
							ffmpegLPCMextract [2] = "" + params.timeseek; //$NON-NLS-1$
						}
						ffparams = new OutputParams(PMS.getConfiguration());
						ffparams.maxBufferSize = 1;
						ffparams.stdin = params.stdin;
						ffAudio[i] = new ProcessWrapperImpl(ffmpegLPCMextract, ffparams);
					}
				}
			}
		}
		
		File f = new File(configuration.getTempFolder(), "pms-tsmuxer.meta"); //$NON-NLS-1$
		params.log = false;
		PrintWriter pw = new PrintWriter(f);
		pw.print("MUXOPT --no-pcr-on-video-pid "); //$NON-NLS-1$
		pw.print("--new-audio-pes "); //$NON-NLS-1$
		if (ffVideo != null)
				pw.print("--no-asyncio "); //$NON-NLS-1$
		pw.print(" --vbr"); //$NON-NLS-1$
		if (params.timeseek > 0 && ffVideoPipe == null) {
			pw.print(" --cut-start=" + params.timeseek + "s "); //$NON-NLS-1$ //$NON-NLS-2$
			params.timeseek = 0;
		}
		pw.println(" --vbv-len=500"); //$NON-NLS-1$

		if (ffVideoPipe != null) {
			String videoparams = "level=4.1, insertSEI, contSPS, track=1"; //$NON-NLS-1$
			if (this instanceof TsMuxerAudio)
				videoparams = "track=224"; //$NON-NLS-1$
			if (configuration.isFix25FPSAvMismatch()) {
				fps = "25";
			}
			pw.println(videoType + ", \"" + ffVideoPipe.getOutputPipe() + "\", "  + (fps!=null?("fps=" +fps + ", "):"") + videoparams); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		if (ffAudioPipe != null && ffAudioPipe.length == 1) {
			String timeshift = ""; //$NON-NLS-1$
			String type = "A_AC3"; //$NON-NLS-1$
			if (((configuration.isMencoderUsePcm() || configuration.isDTSEmbedInPCM()) && (params.aid.isDTS() || params.aid.isLossless()) && params.mediaRenderer.isDTSPlayable()) || this instanceof TsMuxerAudio) {
				type = "A_LPCM"; //$NON-NLS-1$
				if (params.mediaRenderer.isMuxDTSToMpeg())
					type = "A_DTS"; //$NON-NLS-1$
			}
			if (params.aid != null && params.aid.delay != 0)
				timeshift = "timeshift=" + params.aid.delay + "ms, "; //$NON-NLS-1$ //$NON-NLS-2$
			pw.println(type + ", \"" + ffAudioPipe[0].getOutputPipe() + "\", " + timeshift + "track=2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (ffAudioPipe != null) {
			for(int i=0;i<media.audioCodes.size();i++) {
				DLNAMediaAudio lang = media.audioCodes.get(i);
				String timeshift = ""; //$NON-NLS-1$
				boolean lossless = false;
				if ((lang.isDTS() || lang.isLossless()) && (configuration.isMencoderUsePcm() || configuration.isDTSEmbedInPCM()) && params.mediaRenderer.isDTSPlayable()) {
					lossless = true;
				}
				String type = "A_AC3"; //$NON-NLS-1$
				if (lossless) {
					type = "A_LPCM"; //$NON-NLS-1$
					if (params.mediaRenderer.isMuxDTSToMpeg())
						type = "A_DTS"; //$NON-NLS-1$
				}
				if (lang.delay != 0)
					timeshift = "timeshift=" + lang.delay + "ms, "; //$NON-NLS-1$ //$NON-NLS-2$
				pw.println(type + ", \"" + ffAudioPipe[i].getOutputPipe() + "\", " + timeshift + "track=" + (2+i)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		pw.close();

		PipeProcess tsPipe = new PipeProcess(System.currentTimeMillis() + "tsmuxerout.ts"); //$NON-NLS-1$
		String cmd [] = new String [] { executable(), f.getAbsolutePath(), tsPipe.getInputPipe() };
		ProcessWrapperImpl p = new ProcessWrapperImpl(cmd, params);
		params.maxBufferSize = 100;
		params.input_pipes[0] = tsPipe;
		params.stdin = null;
		ProcessWrapper pipe_process = tsPipe.getPipeProcess();
		p.attachProcess(pipe_process);
		pipe_process.runInNewThread();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) { }
		tsPipe.deleteLater();
		
		if (ffVideoPipe != null) {
			ProcessWrapper ff_pipe_process = ffVideoPipe.getPipeProcess();
			p.attachProcess(ff_pipe_process);
			ff_pipe_process.runInNewThread();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) { }
			ffVideoPipe.deleteLater();
			
			p.attachProcess(ffVideo);
			ffVideo.runInNewThread();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) { }
		}
		
		if (ffAudioPipe != null && params.aid != null) {
			for(int i=0;i<ffAudioPipe.length;i++) {
				ProcessWrapper ff_pipe_process = ffAudioPipe[i].getPipeProcess();
				p.attachProcess(ff_pipe_process);
				ff_pipe_process.runInNewThread();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) { }
				ffAudioPipe[i].deleteLater();
				p.attachProcess(ffAudio[i]);
				ffAudio[i].runInNewThread();
				
			}
		}
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) { }
		
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
	private JCheckBox muxallaudiotracks;

	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout(
                "left:pref, 0:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow"); //$NON-NLS-1$
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

        muxallaudiotracks = new JCheckBox(Messages.getString("TSMuxerVideo.19")); //$NON-NLS-1$
        muxallaudiotracks.setContentAreaFilled(false);
        if (configuration.isMuxAllAudioTracks())
        	muxallaudiotracks.setSelected(true);

        muxallaudiotracks.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				configuration.setMuxAllAudioTracks(e.getStateChange() == ItemEvent.SELECTED);
 			}
        	
        });
        builder.add(muxallaudiotracks, cc.xy(2, 5));
        
        return builder.getPanel();
	}
	
	public boolean isInternalSubtitlesSupported() {
		return false;
	}
	public boolean isExternalSubtitlesSupported() {
		return false;
	}

	@Override
	public boolean isPlayerCompatible(RendererConfiguration mediaRenderer) {
		return mediaRenderer != null && mediaRenderer.isMuxH264MpegTS();
	}
}
