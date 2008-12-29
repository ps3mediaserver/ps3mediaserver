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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;

public class FFMpegVideo extends Player {
	
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
		if (PMS.get().getFfmpegSettings() != null) {
			StringTokenizer st = new StringTokenizer(PMS.get().getFfmpegSettings() + " -ab " + PMS.get().getAudiobitrate() + "k -threads " + PMS.get().getNbcores(), " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		String defaut [] = getDefaultArgs();
		if (overridenArgs != null) { 
			args = new String [defaut.length + overridenArgs.length];
			for(int i=0;i<defaut.length;i++)
				args[i] = defaut[i];
			for(int i=0;i<overridenArgs.length;i++) {
				if (overridenArgs[i].equals("-f") || overridenArgs[i].equals("-acodec") || overridenArgs[i].equals("-vcodec")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					PMS.minimal("FFmpeg encoder settings: You cannot change Muxer, Video Codec or Audio Codec"); //$NON-NLS-1$
					overridenArgs[i] = "-title"; //$NON-NLS-1$
					if (i + 1 < overridenArgs.length)
						overridenArgs[i+1] = "NewTitle"; //$NON-NLS-1$
				}
				args[i+defaut.length] = overridenArgs[i];
			}
		} else
			args = defaut;
		return args;
			
	}

	@Override
	public String mimeType() {
		return "video/mpeg"; //$NON-NLS-1$
	}

	@Override
	public String executable() {
		return PMS.get().getFFmpegPath();
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media, OutputParams params) throws IOException {
		return getFFMpegTranscode(fileName, media, params);
	}

	protected ProcessWrapperImpl getFFMpegTranscode(String fileName, DLNAMediaInfo media, OutputParams params) throws IOException {
		
		/*String videoPipe = "mplayer_vid" + System.currentTimeMillis();
		String audioPipe = "mplayer_aud" + System.currentTimeMillis();
		
		String pipeprefix = "\\\\.\\pipe\\";
		if (!PMS.get().isWindows())
			pipeprefix = PMS.get().getTempFolder() + "/";*/
		
		PipeProcess videoP = new PipeProcess("mplayer_vid" + System.currentTimeMillis()); //$NON-NLS-1$
		PipeProcess audioP = new PipeProcess("mplayer_aud" + System.currentTimeMillis()); //$NON-NLS-1$
		PipeProcess ffPipe = null;
		
		String cmdArray [] = new String [14+args().length];
		cmdArray[0] = executable();
		cmdArray[1] = "-title"; //$NON-NLS-1$
		cmdArray[2] = "dummy"; //$NON-NLS-1$
		if (params.timeseek > 0 && !PMS.get().isForceMPlayer() && !mplayer()) {
			cmdArray[1] = "-ss"; //$NON-NLS-1$
			cmdArray[2] = "" + params.timeseek; //$NON-NLS-1$
		}
		cmdArray[3] = "-title"; //$NON-NLS-1$
		cmdArray[4] = "dummy"; //$NON-NLS-1$
		cmdArray[5] = "-title"; //$NON-NLS-1$
		cmdArray[6] = "dummy"; //$NON-NLS-1$
		if (type() == Format.VIDEO) {
			cmdArray[5] = "-i"; //$NON-NLS-1$
			cmdArray[6] = fileName;
			if (PMS.get().isForceMPlayer() || mplayer()) {
				cmdArray[3] = "-f"; //$NON-NLS-1$
				cmdArray[4] = "yuv4mpegpipe"; //$NON-NLS-1$
				//cmdArray[6] = pipeprefix + videoPipe + (PMS.get().isWindows()?".2":"");
				cmdArray[6] = videoP.getOutputPipe();
			} else if (avisynth()) {
				File avsFile = getAVSScript(fileName, params.fromFrame, params.toFrame);
				cmdArray[6] = avsFile.getAbsolutePath();
			}
		}
		cmdArray[7] = "-title"; //$NON-NLS-1$
		cmdArray[8] = "dummy"; //$NON-NLS-1$
		cmdArray[9] = "-title"; //$NON-NLS-1$
		cmdArray[10] = "dummy"; //$NON-NLS-1$
		if (type() == Format.VIDEO || type() == Format.AUDIO) {
			if (type() == Format.VIDEO && (PMS.get().isForceMPlayer() || mplayer())) {
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
		for(int i=0;i<args().length;i++)
			cmdArray[11+i] = args()[i];
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
		if (PMS.get().isFilebuffer()) {
			File m = new File(PMS.get().getTempFolder(), "pms-transcode.tmp"); //$NON-NLS-1$
			if (m.exists() && !m.delete()) {
				PMS.minimal("Temp file currently used.. Waiting 3 seconds"); //$NON-NLS-1$
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) { }
				if (m.exists() && !m.delete()) {
					PMS.minimal("Temp file cannot be deleted... Serious ERROR"); //$NON-NLS-1$
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
		
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		
		if (type() != Format.AUDIO && (PMS.get().isForceMPlayer() || mplayer())) {
			
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
			
			
			String sMp = PMS.get().getMplayerSettings();
			
			String overiddenMPlayerArgs [] = null;
			if (sMp != null) {
				StringTokenizer st = new StringTokenizer(sMp, " "); //$NON-NLS-1$
				overiddenMPlayerArgs = new String [st.countTokens()];
				int i = 0;
				while (st.hasMoreTokens()) {
					overiddenMPlayerArgs[i++] = st.nextToken();
				}
			} else
				overiddenMPlayerArgs = new String [0];
			
			
			String mPlayerdefaultVideoArgs [] = new String [] { fileName, seek_param, seek_value, "-vo", "yuv4mpeg:file=" + /*pipeprefix + videoPipe+(PMS.get().isWindows()?".1":"")*/videoP.getInputPipe(), "-ao", "null", "-nosound", "-benchmark", "-noframedrop", "-speed", "100", "-quiet" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
			OutputParams mplayer_vid_params = new OutputParams(PMS.configuration);
			mplayer_vid_params.maxBufferSize = 1;
			
			String videoArgs [] = new String [1 + overiddenMPlayerArgs.length + mPlayerdefaultVideoArgs.length];
			videoArgs[0] = PMS.get().getMPlayerPath();
			System.arraycopy(overiddenMPlayerArgs, 0, videoArgs, 1, overiddenMPlayerArgs.length);
			System.arraycopy(mPlayerdefaultVideoArgs, 0, videoArgs, 1 + overiddenMPlayerArgs.length, mPlayerdefaultVideoArgs.length);
			ProcessWrapperImpl mplayer_vid_process = new ProcessWrapperImpl(videoArgs, mplayer_vid_params);
			
			String mPlayerdefaultAudioArgs [] = new String [] { fileName, seek_param, seek_value, "-vo", "null", "-ao", "pcm:file=" +/* pipeprefix + audioPipe+(PMS.get().isWindows()?".1":"")*/audioP.getInputPipe(), "-ao", "pcm:fast", "-quiet", "-noframedrop"  }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			OutputParams mplayer_aud_params = new OutputParams(PMS.configuration);
			mplayer_aud_params.maxBufferSize = 1;
			
			String audioArgs [] = new String [1 + overiddenMPlayerArgs.length + mPlayerdefaultAudioArgs.length];
			audioArgs[0] = PMS.get().getMPlayerPath();
			System.arraycopy(overiddenMPlayerArgs, 0, audioArgs, 1, overiddenMPlayerArgs.length);
			System.arraycopy(mPlayerdefaultAudioArgs, 0, audioArgs, 1 + overiddenMPlayerArgs.length, mPlayerdefaultAudioArgs.length);
			ProcessWrapperImpl mplayer_aud_process = new ProcessWrapperImpl(audioArgs, mplayer_aud_params);
			
			if (type() == Format.VIDEO)
				pw.attachProcess(mkfifo_vid_process);
			if (type() == Format.VIDEO || type() == Format.AUDIO)
				pw.attachProcess(mkfifo_aud_process);
			if (type() == Format.VIDEO)
				pw.attachProcess(mplayer_vid_process);
			if (type() == Format.VIDEO || type() == Format.AUDIO)
				pw.attachProcess(mplayer_aud_process);
			
			if (type() == Format.VIDEO)
				mkfifo_vid_process.runInNewThread();
			if (type() == Format.VIDEO || type() == Format.AUDIO)
				mkfifo_aud_process.runInNewThread();
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) { }
			if (type() == Format.VIDEO) {
				/*if (!PMS.get().isWindows()) {
					File f = new File(pipeprefix + videoPipe);
					f.deleteOnExit();
				}*/
				videoP.deleteLater();
				mplayer_vid_process.runInNewThread();
			}
			if (type() == Format.VIDEO || type() == Format.AUDIO) {
				/*if (!PMS.get().isWindows()) {
					File f = new File(pipeprefix + audioPipe);
					f.deleteOnExit();
				}*/
				audioP.deleteLater();
				mplayer_aud_process.runInNewThread();
			}
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
	
	public static File getAVSScript(String fileName) throws IOException {
		return getAVSScript(fileName, -1, -1);
	}
	
	public static File getAVSScript(String fileName, int fromFrame, int toFrame) throws IOException {
		String onlyFileName = fileName.substring(1+fileName.lastIndexOf("\\")); //$NON-NLS-1$
		File file = new File(PMS.get().getTempFolder(), "pms-avs-" + onlyFileName + ".avs"); //$NON-NLS-1$ //$NON-NLS-2$
		PrintWriter pw = new PrintWriter(new FileOutputStream(file));
		
		String convertfps = ""; //$NON-NLS-1$
		if (PMS.get().isAvisynth_convertfps())
			convertfps = ", convertfps=true"; //$NON-NLS-1$
		String movieLine = "clip=DirectShowSource(\"" + fileName + "\"" + convertfps + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String subLine = null;
		String woExt = fileName.substring(0, fileName.length()-4);
		if (PMS.get().isUsesubs() && !PMS.configuration.isMencoderDisableSubs()) {
			File srtFile = new File(woExt + ".srt"); //$NON-NLS-1$
			if (srtFile.exists()) {
				subLine = "clip=TextSub(clip, \"" + srtFile.getAbsolutePath() + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			File assFile = new File(woExt + ".ass"); //$NON-NLS-1$
			if (assFile.exists()) {
				subLine = "clip=TextSub(clip, \"" + assFile.getAbsolutePath() + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			File subFile = new File(woExt + ".sub"); //$NON-NLS-1$
			File idxFile = new File(woExt + ".idx"); //$NON-NLS-1$
			if (subFile.exists()) {
				String function = "TextSub"; //$NON-NLS-1$
				if (idxFile.exists())
					function = "VobSub"; //$NON-NLS-1$
				subLine = "clip=" +function+ "(clip, \"" + subFile.getAbsolutePath() + "\")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		
		ArrayList<String> lines = new ArrayList<String>();
		
		boolean fullyManaged = false;
		String script = PMS.get().getAvisynth_script();
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
        
        
       
       builder.addSeparator(Messages.getString("FFMpegVideo.0"),  cc.xyw(2, 1, 1)); //$NON-NLS-1$
       ffmpeg = new JTextField(PMS.get().getFfmpegSettings());
       ffmpeg.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setFfmpeg(ffmpeg.getText());
   		}
       	   
          });
       builder.add(ffmpeg, cc.xy(2, 3));
       
        return builder.getPanel();
	}
}
