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
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;

public class FFMpegVideoRemux extends Player {
	
	public static final String ID = "ffmpegdvrmsremux";
	
	@Override
	public int purpose() {
		return MISC_PLAYER;
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

	
	public FFMpegVideoRemux() {
	}

	@Override
	public String name() {
		return "FFmpeg DVR-MS Remux";
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}
	
	protected String [] getDefaultArgs() {
		return new String [] { "-f", "vob", "-copyts", "-vcodec", "copy", "-acodec", "copy" };
	}

	@Override
	public String[] args() {
		return getDefaultArgs();
			
	}

	@Override
	public String mimeType() {
		return "video/mpeg";
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
		
		
		
		String cmdArray [] = new String [8+args().length];
		cmdArray[0] = executable();
		if (PMS.get().getAlternativeffmpegPath() != null && PMS.get().getAlternativeffmpegPath().length() > 0)
			cmdArray[0] = PMS.get().getAlternativeffmpegPath();
		cmdArray[1] = "-title";
		cmdArray[2] = "dummy";
		
		/*cmdArray[3] = "-f";
		cmdArray[4] = "asf";
		if (params.forceType != null) {
			cmdArray[4] = params.forceType;
		}*/
		cmdArray[3] = "-i";
		cmdArray[4] = fileName;
		cmdArray[5] = "-title";
		cmdArray[6] = "dummy";
		if (params.timeseek > 0 && !PMS.get().isForceMPlayer() && !mplayer()) {
			cmdArray[1] = "-ss";
			cmdArray[2] = "" + params.timeseek;
			 //params.timeseek = 0;
		}
		for(int i=0;i<args().length;i++)
			cmdArray[7+i] = args()[i];
		
		StringTokenizer st = new StringTokenizer(PMS.get().getFfmpegSettings(), " ");
		if (st.countTokens() > 0) {
			int i=cmdArray.length-1;
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +st.countTokens());
			while (st.hasMoreTokens()) {
				cmdArray[i] = st.nextToken();
				i++;
			}
		}
		
		cmdArray[cmdArray.length-1] = "pipe:";
			
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
			
		pw.runInNewThread();
		return pw;
	}
	
	private JTextField altffpath;

	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout(
                "left:pref, 3dlu, p, 3dlu, 0:grow",
                "p, 3dlu, p, 3dlu, 0:grow");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        
        builder.addSeparator("Settings for DVR-MS remuxing",  cc.xyw(1, 1, 5));
        
        builder.addLabel("Alternative FFmpeg Path:", cc.xy(1, 3));
        altffpath = new JTextField(PMS.get().getAlternativeffmpegPath());
        altffpath.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
    			PMS.get().setAlternativeffmpegPath(altffpath.getText());
    		}
        	   
           });
        builder.add(altffpath, cc.xyw(3, 3, 3));
        
		return builder.getPanel();
	}
	
}
