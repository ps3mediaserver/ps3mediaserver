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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JTextField;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeIPCProcess;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;
import net.pms.util.ProcessUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FFMpegVideo extends Player {
	public static final Logger logger = LoggerFactory.getLogger(FFMpegVideo.class);

	public static final String ID = "avsffmpeg"; //$NON-NLS-1$
	
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
		return true;
	}

	private String overridenArgs [];
	
	public FFMpegVideo() {
		if (PMS.getConfiguration().getFfmpegSettings() != null) {
			StringTokenizer st = new StringTokenizer(PMS.getConfiguration().getFfmpegSettings() + " -ab " + PMS.getConfiguration().getAudioBitrate() + "k -threads " + PMS.getConfiguration().getNumberOfCpuCores(), " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			overridenArgs = new String [st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				overridenArgs[i++] = st.nextToken();
			}
		}
	}

	@Override
	public String name() {
		return "AviSynth/FFmpeg"; //$NON-NLS-1$
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}
	
	protected String [] getDefaultArgs() {
		return new String [] { "-vcodec", "mpeg2video", "-f", "vob", "-acodec", "ac3" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}

	@Override
	public String[] args() {
		String args [] = null;
		String defaultArgs [] = getDefaultArgs();
		if (overridenArgs != null) { 
			args = new String [defaultArgs.length + overridenArgs.length];
			for(int i=0;i<defaultArgs.length;i++)
				args[i] = defaultArgs[i];
			for(int i=0;i<overridenArgs.length;i++) {
				if (overridenArgs[i].equals("-f") || overridenArgs[i].equals("-acodec") || overridenArgs[i].equals("-vcodec")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					logger.info("FFmpeg encoder settings: You cannot change Muxer, Video Codec or Audio Codec"); //$NON-NLS-1$
					overridenArgs[i] = "-title"; //$NON-NLS-1$
					if (i + 1 < overridenArgs.length)
						overridenArgs[i+1] = "NewTitle"; //$NON-NLS-1$
				}
				args[i+defaultArgs.length] = overridenArgs[i];
			}
		} else
			args = defaultArgs;
		return args;
			
	}
	
	public boolean mplayer() {
		return false;
	}

	@Override
	public String mimeType() {
		return HTTPResource.VIDEO_TRANSCODE; //$NON-NLS-1$
	}

	@Override
	public String executable() {
		return PMS.getConfiguration().getFfmpegPath();
	}

	@Override
	public ProcessWrapper launchTranscode(
		String fileName,
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		return getFFMpegTranscode(fileName, dlna, media, params, args());
	}

	protected ProcessWrapperImpl getFFMpegTranscode(
		String fileName,
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params,
		String args[]
	) throws IOException {
		setAudioAndSubs(fileName, media, params, PMS.getConfiguration());
		
		PipeIPCProcess videoP = null;
		PipeIPCProcess audioP = null;
		if (mplayer()) {
			videoP = new PipeIPCProcess("mplayer_vid1" + System.currentTimeMillis(), "mplayer_vid2" + System.currentTimeMillis(), false, false); //$NON-NLS-1$ //$NON-NLS-2$
			audioP = new PipeIPCProcess("mplayer_aud1" + System.currentTimeMillis(), "mplayer_aud2" + System.currentTimeMillis(), false, false); //$NON-NLS-1$ //$NON-NLS-2$
		}
		PipeProcess ffPipe = null;
		
		String cmdArray [] = new String [14+args.length];
		cmdArray[0] = executable();
		cmdArray[1] = "-sn"; //$NON-NLS-1$
		cmdArray[2] = "-sn"; //$NON-NLS-1$
		if (params.timeseek > 0 && !mplayer()) {
			cmdArray[1] = "-ss"; //$NON-NLS-1$
			cmdArray[2] = "" + params.timeseek; //$NON-NLS-1$
		}
		cmdArray[3] = "-sn"; //$NON-NLS-1$
		cmdArray[4] = "-sn"; //$NON-NLS-1$
		cmdArray[5] = "-sn"; //$NON-NLS-1$
		cmdArray[6] = "-sn"; //$NON-NLS-1$
		if (type() == Format.VIDEO) {
			cmdArray[5] = "-i"; //$NON-NLS-1$
			cmdArray[6] = fileName;
			if (mplayer()) {
				cmdArray[3] = "-f"; //$NON-NLS-1$
				cmdArray[4] = "yuv4mpegpipe"; //$NON-NLS-1$
				//cmdArray[6] = pipeprefix + videoPipe + (PMS.get().isWindows()?".2":"");
				cmdArray[6] = videoP.getOutputPipe();
			} else if (avisynth()) {
				File avsFile = getAVSScript(fileName, params.sid, params.fromFrame, params.toFrame);
				cmdArray[6] = ProcessUtil.getShortFileNameIfWideChars(avsFile.getAbsolutePath());
			}
		}
		cmdArray[7] = "-sn"; //$NON-NLS-1$
		cmdArray[8] = "-sn"; //$NON-NLS-1$
		cmdArray[9] = "-sn"; //$NON-NLS-1$
		cmdArray[10] = "-sn"; //$NON-NLS-1$
		if (type() == Format.VIDEO || type() == Format.AUDIO) {
			if (type() == Format.VIDEO && (mplayer())) {
				cmdArray[7] = "-f"; //$NON-NLS-1$
				cmdArray[8] = "wav"; //$NON-NLS-1$
				cmdArray[9] = "-i"; //$NON-NLS-1$
				//cmdArray[10] = pipeprefix + audioPipe + (PMS.get().isWindows()?".2":"");
				cmdArray[10] = audioP.getOutputPipe();
			} else if (type() == Format.AUDIO) {
				cmdArray[7] = "-i"; //$NON-NLS-1$
				cmdArray[8] = fileName;
			}
		}
		if (params.timeend > 0) {
			cmdArray[9] = "-t"; //$NON-NLS-1$
			cmdArray[10] = "" + params.timeend; //$NON-NLS-1$
		}
		for(int i=0;i<args.length;i++)
			cmdArray[11+i] = args[i];
		/*
		String mm = PMS.get().getMaximumbitrate();
		int bufs = 0;
		if (mm.contains("(") && mm.contains(")")) {
			bufs = Integer.parseInt(mm.substring(mm.indexOf("(")+1, mm.indexOf(")")));
		}
		if (mm.contains("("))
			mm = mm.substring(0, mm.indexOf("(")).trim();
		
		int mb = Integer.parseInt(mm);
		if (mb > 0 && !PMS.get().getFfmpegSettings().contains("bufsize") && !PMS.get().getFfmpegSettings().contains("maxrate")) {
			mb = 1000*mb;
			if (mb > 60000)
				mb = 60000;
			int bufSize = 1835;
			if (media.isHDVideo())
				bufSize = mb / 3;
			if (bufSize > 7000)
				bufSize = 7000;
			
			if (bufs > 0)
				bufSize = bufs * 1000;
			
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length+6);
			cmdArray [cmdArray.length-9] = "-b";
			cmdArray [cmdArray.length-8] = "" + mb;
			cmdArray [cmdArray.length-7] = "-maxrate";
			cmdArray [cmdArray.length-6] = "" + mb;
			cmdArray [cmdArray.length-5] = "-bufsize";
			cmdArray [cmdArray.length-4] = "" + bufSize;
		}
		*/
		cmdArray[cmdArray.length-3] = "-muxpreload"; //$NON-NLS-1$
		cmdArray[cmdArray.length-2] = "0"; //$NON-NLS-1$
		/*double fr = 0;
		if (media.frameRate != null && media.frameRate.length() > 0) {
			fr = Double.parseDouble(media.frameRate);
		}
		if (params.timeseek > 0 && fr > 0) {
			cmdArray[cmdArray.length-3] = "-timecode_frame_start";
			cmdArray[cmdArray.length-2] = "" + (int) Math.round(params.timeseek * fr);
			params.timeseek = 0;
		}*/
		if (PMS.getConfiguration().isFileBuffer()) {
			File m = new File(PMS.getConfiguration().getTempFolder(), "pms-transcode.tmp"); //$NON-NLS-1$
			if (m.exists() && !m.delete()) {
				logger.info("Temp file currently used.. Waiting 3 seconds"); //$NON-NLS-1$
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) { }
				if (m.exists() && !m.delete()) {
					logger.info("Temp file cannot be deleted... Serious ERROR"); //$NON-NLS-1$
				}
			}
			params.outputFile = m;
			params.minFileSize = params.minBufferSize;
			m.deleteOnExit();
			cmdArray[cmdArray.length-1] = m.getAbsolutePath();
		}
		else {
			cmdArray[cmdArray.length-1] = "pipe:"; //$NON-NLS-1$
			//ffPipe = new PipeProcess("ffmpegout");
			//cmdArray[cmdArray.length-1] = ffPipe.getInputPipe();
		}
		
		cmdArray = finalizeTranscoderArgs(
			this,
			fileName,
			dlna,
			media,
			params,
			cmdArray
		);

		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		
		if (type() != Format.AUDIO && (mplayer())) {
			
			/*
			OutputParams mkfifo_vid_params = new OutputParams();
			mkfifo_vid_params.maxBufferSize = 0.1;
			ProcessWrapperImpl mkfifo_vid_process = new ProcessWrapperImpl(new String[] { PMS.get().getMKfifoPath(), PMS.get().isWindows()?"":"--mode=777", (PMS.get().isWindows()?"":pipeprefix) + videoPipe }, mkfifo_vid_params);
			
			OutputParams mkfifo_aud_params = new OutputParams();
			mkfifo_aud_params.maxBufferSize = 0.1;
			ProcessWrapperImpl mkfifo_aud_process = new ProcessWrapperImpl(new String[] { PMS.get().getMKfifoPath(), PMS.get().isWindows()?"":"--mode=777", (PMS.get().isWindows()?"":pipeprefix) + audioPipe }, mkfifo_aud_params);
			*/
			ProcessWrapper mkfifo_vid_process = videoP.getPipeProcess();
			ProcessWrapper mkfifo_aud_process = audioP.getPipeProcess();
			
			String seek_param = "-quiet"; //$NON-NLS-1$
			String seek_value = "-quiet"; //$NON-NLS-1$
			if (params.timeseek > 0) {
				seek_param = "-ss"; //$NON-NLS-1$
				seek_value = "" + params.timeseek; //$NON-NLS-1$
			}
			
			
			//String sMp = PMS.get().getMPlayerPath();
			
			String overiddenMPlayerArgs [] = null;
			/*if (sMp != null) {
				StringTokenizer st = new StringTokenizer(sMp, " "); //$NON-NLS-1$
				overiddenMPlayerArgs = new String [st.countTokens()];
				int i = 0;
				while (st.hasMoreTokens()) {
					overiddenMPlayerArgs[i++] = st.nextToken();
				}
			} else*/
				overiddenMPlayerArgs = new String [0];
			
			
			String mPlayerdefaultVideoArgs [] = new String [] { fileName, seek_param, seek_value, "-vo", "yuv4mpeg:file=" + videoP.getInputPipe(), "-ao", "pcm:waveheader:file="+ audioP.getInputPipe(), "-benchmark", "-noframedrop", "-speed", "100"/*, "-quiet"*/ }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
			OutputParams mplayer_vid_params = new OutputParams(PMS.getConfiguration());
			mplayer_vid_params.maxBufferSize = 1;
			
			String videoArgs [] = new String [1 + overiddenMPlayerArgs.length + mPlayerdefaultVideoArgs.length];
			videoArgs[0] = PMS.getConfiguration().getMplayerPath();
			System.arraycopy(overiddenMPlayerArgs, 0, videoArgs, 1, overiddenMPlayerArgs.length);
			System.arraycopy(mPlayerdefaultVideoArgs, 0, videoArgs, 1 + overiddenMPlayerArgs.length, mPlayerdefaultVideoArgs.length);
			ProcessWrapperImpl mplayer_vid_process = new ProcessWrapperImpl(videoArgs, mplayer_vid_params);
			
//			String mPlayerdefaultAudioArgs [] = new String [] { fileName, seek_param, seek_value, "-vo", "null", "-ao", "pcm:file=" +/* pipeprefix + audioPipe+(PMS.get().isWindows()?".1":"")*/audioP.getInputPipe(), "-ao", "pcm:fast", "-quiet", "-noframedrop"  }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
//			OutputParams mplayer_aud_params = new OutputParams(PMS.configuration);
//			mplayer_aud_params.maxBufferSize = 1;
//			
//			String audioArgs [] = new String [1 + overiddenMPlayerArgs.length + mPlayerdefaultAudioArgs.length];
//			audioArgs[0] = PMS.get().getMPlayerPath();
//			System.arraycopy(overiddenMPlayerArgs, 0, audioArgs, 1, overiddenMPlayerArgs.length);
//			System.arraycopy(mPlayerdefaultAudioArgs, 0, audioArgs, 1 + overiddenMPlayerArgs.length, mPlayerdefaultAudioArgs.length);
//			ProcessWrapperImpl mplayer_aud_process = new ProcessWrapperImpl(audioArgs, mplayer_aud_params);
//			
			if (type() == Format.VIDEO)
				pw.attachProcess(mkfifo_vid_process);
			if (type() == Format.VIDEO || type() == Format.AUDIO)
				pw.attachProcess(mkfifo_aud_process);
			if (type() == Format.VIDEO)
				pw.attachProcess(mplayer_vid_process);
//			if (type() == Format.VIDEO || type() == Format.AUDIO)
//				pw.attachProcess(mplayer_aud_process);
			
			if (type() == Format.VIDEO)
				mkfifo_vid_process.runInNewThread();
			if (type() == Format.VIDEO || type() == Format.AUDIO)
				mkfifo_aud_process.runInNewThread();
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) { }
			if (type() == Format.VIDEO) {
				videoP.deleteLater();
				mplayer_vid_process.runInNewThread();
			}
//			if (type() == Format.VIDEO || type() == Format.AUDIO) {
//				audioP.deleteLater();
//				mplayer_aud_process.runInNewThread();
//			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) { }
		} else if (ffPipe != null) {
			params.input_pipes [0] = ffPipe;
			
			ProcessWrapper pipe_process = ffPipe.getPipeProcess();
			pw.attachProcess(pipe_process);
			pipe_process.runInNewThread();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) { }
			ffPipe.deleteLater();
		}
		
		pw.runInNewThread();
		return pw;
	}
	
	public static File getAVSScript(String fileName, DLNAMediaSubtitle subTrack) throws IOException {
		return getAVSScript(fileName, subTrack, -1, -1);
	}
	
	public static File getAVSScript(String fileName, DLNAMediaSubtitle subTrack, int fromFrame, int toFrame) throws IOException {
		String onlyFileName = fileName.substring(1+fileName.lastIndexOf("\\")); //$NON-NLS-1$
		File file = new File(PMS.getConfiguration().getTempFolder(), "pms-avs-" + onlyFileName + ".avs"); //$NON-NLS-1$ //$NON-NLS-2$
		PrintWriter pw = new PrintWriter(new FileOutputStream(file));
		
		String convertfps = ""; //$NON-NLS-1$
		if (PMS.getConfiguration().getAvisynthConvertFps())
			convertfps = ", convertfps=true"; //$NON-NLS-1$
		File f = new File(fileName);
		if (f.exists())
			fileName = ProcessUtil.getShortFileNameIfWideChars(fileName);
		String movieLine = "clip=DirectShowSource(\"" + fileName + "\"" + convertfps + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String subLine = null;
		if (subTrack != null && PMS.getConfiguration().getUseSubtitles() && !PMS.getConfiguration().isMencoderDisableSubs()) {
			logger.trace("Avisynth script: Using sub track: " + subTrack);
			if (subTrack.file != null) {
				String function = "TextSub"; //$NON-NLS-1$
				if (subTrack.type == DLNAMediaSubtitle.VOBSUB)
					function = "VobSub"; //$NON-NLS-1$
				subLine = "clip=" +function+ "(clip, \"" + ProcessUtil.getShortFileNameIfWideChars(subTrack.file.getAbsolutePath()) + "\")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		
		ArrayList<String> lines = new ArrayList<String>();
		
		boolean fullyManaged = false;
		String script = PMS.getConfiguration().getAvisynthScript();
		StringTokenizer st = new StringTokenizer(script, PMS.AVS_SEPARATOR);
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			if (line.contains("<movie") || line.contains("<sub")) //$NON-NLS-1$ //$NON-NLS-2$
				fullyManaged = true;
			lines.add(line);
		}
		
		if (fullyManaged) {
			for(String s:lines) {
				s = s.replace("<moviefilename>", fileName); //$NON-NLS-1$
				if (movieLine != null)
					s = s.replace("<movie>", movieLine); //$NON-NLS-1$
				
				s = s.replace("<sub>", subLine!=null?subLine:"#"); //$NON-NLS-1$ //$NON-NLS-2$
				pw.println(s);
			}
		} else {
			pw.println(movieLine);
			if (subLine != null)
				pw.println(subLine);
			pw.println("clip"); //$NON-NLS-1$
			
		}
		
		pw.close();
		file.deleteOnExit();
		return file;
	}

	private JTextField ffmpeg;
	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout(
                "left:pref, 0:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        
       
        JComponent cmp = builder.addSeparator(Messages.getString("FFMpegVideo.0"),  cc.xyw(2, 1, 1)); //$NON-NLS-1$
       cmp = (JComponent) cmp.getComponent(0);
       cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
       
       ffmpeg = new JTextField(PMS.getConfiguration().getFfmpegSettings());
       ffmpeg.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.getConfiguration().setFfmpegSettings(ffmpeg.getText());
   		}
       	   
          });
       builder.add(ffmpeg, cc.xy(2, 3));
       
        return builder.getPanel();
	}
}
