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

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.newgui.LooksFrame;
import net.pms.newgui.MyComboBoxModel;

public class MEncoderVideo extends Player {
	
	public JCheckBox getCheckBox() {
		return checkBox;
	}
private JTextField mencoder_ass_scale;
	private JTextField mencoder_ass_margin;
	private JTextField mencoder_ass_outline;
	private JTextField mencoder_ass_shadow;
	private JTextField mencoder_noass_scale;
	private JTextField mencoder_noass_subpos;
	private JTextField mencoder_noass_blur;
	private JTextField mencoder_noass_outline;
	private JTextField decode;
	private JTextField langs;
	private JTextField defaultsubs;
	private JComboBox subcp;
	private JCheckBox  forcefps ;
	private JCheckBox  fc ;
	private JCheckBox  ass ;
	private JCheckBox  checkBox ;
	private JCheckBox  noskip ;
	private JCheckBox  intelligentsync ;
	public JCheckBox getNoskip() {
		return noskip;
	}
	private JCheckBox  subs ;
	public JCheckBox getSubs() {
		return subs;
	}
	
	private JCheckBox fribidi;
	
	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout(
                "left:pref, 3dlu, p:grow, 3dlu, right:p:grow, 3dlu, p:grow, 3dlu, right:p:grow,3dlu, p:grow, 3dlu, right:p:grow,3dlu, pref:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, p , 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
         checkBox = new JCheckBox("Skips loop filter deblocking for H264: COULD DEGRADE QUALITY, disable it if your CPU is fast enough!!");
        checkBox.setContentAreaFilled(false);
        if (PMS.get().isSkiploopfilter())
        	checkBox.setSelected(true);
        checkBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setSkipLoopFilter(e.getStateChange() == ItemEvent.SELECTED);
			}
        	
        });
        
        builder.addSeparator("Video/Audio decoder settings for MEncoder engine only",  cc.xyw(1, 1, 15));
       builder.add(checkBox,          cc.xyw(1,  3, 15));
       
       noskip = new JCheckBox("A/V sync correction");
       noskip.setContentAreaFilled(false);
       if (PMS.get().isMencoder_nooutofsync())
    	   noskip.setSelected(true);
       noskip.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_nooutofsync(e.getStateChange() == ItemEvent.SELECTED);
				intelligentsync.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       
       builder.add(noskip,          cc.xy(1,  5));
       
       intelligentsync = new JCheckBox("Intelligent A/V correction (based on file type)");
       intelligentsync.setContentAreaFilled(false);
       if (PMS.get().isMencoder_nooutofsync())
    	   intelligentsync.setSelected(true);
       intelligentsync.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_intelligent_sync(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       if (!PMS.get().isMencoder_nooutofsync())
    	   intelligentsync.setEnabled(false);
       builder.add(intelligentsync,          cc.xyw(3,  5, 12));
       
       forcefps = new JCheckBox("Force framerate parsed from FFMpeg");
       forcefps.setContentAreaFilled(false);
       if (PMS.get().isMencoder_forcefps())
    	   forcefps.setSelected(true);
       forcefps.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_forcefps(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       
       builder.add(forcefps,          cc.xyw(1,  7, 15));
       
       
  
       builder.addSeparator("You can add here a denoise filter for example: -vf hqdn3d  or a scaler: -vf scale=1280:-2)", cc.xyw(1, 9, 15));
       builder.addLabel("Decoding settings:", cc.xy(1, 11));
       decode = new JTextField(PMS.get().getMencoder_decode());
       decode.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_decode(decode.getText());
   		}
       	   
          });
       builder.add(decode, cc.xyw(3, 11, 13));
       
      
       
       builder.addLabel("Default audio language priority:", cc.xyw(1, 13, 15));
       langs = new JTextField(PMS.get().getMencoder_audiolangs());
       langs.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_audiolangs(langs.getText());
   		}
       	   
          });
       builder.add(langs, cc.xyw(3, 13, 13));
       
       builder.addSeparator("Subtitles settings",  cc.xyw(1, 15, 15));
       
        
     
       
       builder.addLabel("Default subtitles language priority:", cc.xy(1, 17));
       defaultsubs = new JTextField(PMS.get().getMencoder_sublangs());
       defaultsubs.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_sublangs(defaultsubs.getText());
   		}
       	   
          });
       builder.add(defaultsubs, cc.xyw(3, 17, 4));
       
       builder.addLabel("Subtitles codepage:", cc.xy(1, 21));
       /*subcp = new JTextField(PMS.get().getMencoder_subcp());
       subcp.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_subcp(subcp.getText());
   		}
       	   
          });*/
       Object data [] = new Object [] { PMS.get().getMencoder_subcp(),
    		   "cp1250  /* Windows - Eastern Europe */",
    		   "cp1251  /* Windows - Cyrillic */",
    		   "cp1252  /* Windows - Western Europe */",
    		   "cp1253  /* Windows - Greek */",
    		   "cp1254  /* Windows - Turkish */",
    		   "cp1255  /* Windows - Hebrew */",
    		   "cp1256  /* Windows - Arabic */",
    		   "cp1257  /* Windows - Baltic */",
    		   "cp1258  /* Windows - Vietnamese */",
    		   "ISO 8859-1 /* Western Europe */",
    		   "ISO 8859-2 /* Western and Central Europe */",
    		   "ISO 8859-3 /* Western Europe and South European */",
    		   "ISO 8859-4 /* Western Europe and Baltic countries */",
    		   "ISO 8859-5 /* Cyrillic alphabet */",
    		   "ISO 8859-6 /* Arabic */",
    		   "ISO 8859-7 /* Greek */",
    		   "ISO 8859-8 /* Hebrew */",
    		   "ISO 8859-9 /* Western Europe with amended Turkish */",
    		   "ISO 8859-10 /* Western Europe with Nordic languages */",
    		   "ISO 8859-11 /* Thai */",
    		   "ISO 8859-13 /* Baltic languages plus Polish */",
    		   "ISO 8859-14 /* Celtic languages */",
    		   "ISO 8859-15 /* Added the Euro sign */",
    		   "ISO 8859-16 /* Central European languages */"};
       MyComboBoxModel cbm = new MyComboBoxModel(data);
       
       subcp = new JComboBox(cbm);
       subcp.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String s = (String) e.getItem();
					if (s.indexOf("/*") > -1) {
						s = s.substring(0, s.indexOf("/*")).trim();
					}
					PMS.get().setMencoder_subcp(s);
				}
			}
       	
       });
       subcp.setEditable(true);
       builder.add(subcp, cc.xyw(3, 21,7));
       
       fribidi = new JCheckBox("FriBiDi mode");
       fribidi.setContentAreaFilled(false);
       if (PMS.get().isMencoder_subfribidi())
    	   fribidi.setSelected(true);
       fribidi.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_subfribidi(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       builder.add(fribidi, cc.xyw(11, 21, 4));
       
       builder.addLabel("ASS font settings: Font scale", cc.xy(1, 25));
       mencoder_ass_scale = new JTextField(PMS.get().getMencoder_ass_scale());
       mencoder_ass_scale.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_ass_scale(mencoder_ass_scale.getText());
   		}
       	   
          });
       
       builder.addLabel("Font outline", cc.xy(5, 25));
       mencoder_ass_outline = new JTextField(PMS.get().getMencoder_ass_outline());
       mencoder_ass_outline.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_ass_outline(mencoder_ass_outline.getText());
   		}
       	   
          });
       
       builder.addLabel("Font shadow", cc.xy(9, 25));
       mencoder_ass_shadow = new JTextField(PMS.get().getMencoder_ass_shadow());
       mencoder_ass_shadow.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_ass_shadow(mencoder_ass_shadow.getText());
   		}
       	   
          });
       
       builder.addLabel("Font sub margin", cc.xy(13, 25));
       mencoder_ass_margin = new JTextField(PMS.get().getMencoder_ass_margin());
       mencoder_ass_margin.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_ass_margin(mencoder_ass_margin.getText());
   		}
       	   
          });
 builder.add(mencoder_ass_scale, cc.xy(3, 25));
       
       builder.add(mencoder_ass_outline, cc.xy(7, 25));
       
       builder.add(mencoder_ass_shadow, cc.xy(11, 25));
      
       builder.add(mencoder_ass_margin, cc.xy(15, 25));
       
       
       builder.addLabel("Default font settings: Font scale", cc.xy(1, 27));
       mencoder_noass_scale = new JTextField(PMS.get().getMencoder_noass_scale());
       mencoder_noass_scale.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_noass_scale(mencoder_noass_scale.getText());
   		}
       	   
          });
       
       builder.addLabel("Font outline", cc.xy(5, 27));
       mencoder_noass_outline = new JTextField(PMS.get().getMencoder_noass_outline());
       mencoder_noass_outline.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_noass_outline(mencoder_noass_outline.getText());
   		}
       	   
          });
       
       builder.addLabel("Font blur", cc.xy(9, 27));
       mencoder_noass_blur = new JTextField(PMS.get().getMencoder_noass_blur());
       mencoder_noass_blur.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_noass_blur(mencoder_noass_blur.getText());
   		}
       	   
          });
       
       builder.addLabel("Font sub margin", cc.xy(13, 27));
       mencoder_noass_subpos = new JTextField(PMS.get().getMencoder_noass_subpos());
       mencoder_noass_subpos.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_noass_subpos(mencoder_noass_subpos.getText());
   		}
       	   
          });
 builder.add(mencoder_noass_scale, cc.xy(3, 27));
       
       builder.add(mencoder_noass_outline, cc.xy(7, 27));
       
       builder.add(mencoder_noass_blur, cc.xy(11, 27));
      
       builder.add(mencoder_noass_subpos, cc.xy(15, 27));
       
       
       ass = new JCheckBox("ASS/SSA Subtitles");
       ass.setContentAreaFilled(false);
       ass.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e != null)
					PMS.get().setMencoder_ass(e.getStateChange() == ItemEvent.SELECTED);
				mencoder_ass_scale.setEnabled(PMS.get().isMencoder_ass());
				mencoder_ass_outline.setEnabled(PMS.get().isMencoder_ass());
				mencoder_ass_shadow.setEnabled(PMS.get().isMencoder_ass());
				mencoder_ass_margin.setEnabled(PMS.get().isMencoder_ass());
				mencoder_noass_scale.setEnabled(!PMS.get().isMencoder_ass());
				mencoder_noass_outline.setEnabled(!PMS.get().isMencoder_ass());
				mencoder_noass_blur.setEnabled(!PMS.get().isMencoder_ass());
				mencoder_noass_subpos.setEnabled(!PMS.get().isMencoder_ass());
			}
       	
       });
       
       builder.add(ass,          cc.xy(1,  23));
       ass.setSelected(PMS.get().isMencoder_ass());
      ass.getItemListeners()[0].itemStateChanged(null);
     
      fc = new JCheckBox("Fontconfig / Embedded fonts [Unstable on some configs!]");
      fc.setContentAreaFilled(false);
      fc.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_fontconfig(e.getStateChange() == ItemEvent.SELECTED);
			}
      	
      });
      
      builder.add(fc,          cc.xyw(3,  23, 12));
      fc.setSelected(PMS.get().isMencoder_fontconfig());
       
       subs = new JCheckBox("Autoload *.srt/*.sub subtitles with the same file name");
       subs.setContentAreaFilled(false);
       if (PMS.get().isUsesubs())
    	   subs.setSelected(true);
       subs.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setUsesubs(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       builder.add(subs, cc.xyw(1, 31, 15));
       
       JTextArea decodeTips = new JTextArea("Decode Tips:\n\n- You can choose ASS subtitles support or not (Linux Mencoder build could have troubles with it).\n- The \"-lavdopts fast\" switch must not be used as it could cause some problems when playing dvd/mpeg2/vob files\n");
       decodeTips.setEditable(false);
       decodeTips.setBorder(BorderFactory.createEtchedBorder());
       decodeTips.setBackground(new Color(255, 255, 192));
       builder.add(decodeTips, cc.xyw(1, 33, 15));
       
      
      JCheckBox disableSubs = ((LooksFrame) PMS.get().getFrame()).getTr().getDisableSubs();
      disableSubs.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_disablesubs(e.getStateChange() == ItemEvent.SELECTED);
				
				subs.setEnabled(!PMS.get().isMencoder_disablesubs());
				defaultsubs.setEnabled(!PMS.get().isMencoder_disablesubs());
				subcp.setEnabled(!PMS.get().isMencoder_disablesubs());
				ass.setEnabled(!PMS.get().isMencoder_disablesubs());
				
				fribidi.setEnabled(!PMS.get().isMencoder_disablesubs());
				fc.setEnabled(!PMS.get().isMencoder_disablesubs());
				mencoder_ass_scale.setEnabled(!PMS.get().isMencoder_disablesubs());
				mencoder_ass_outline.setEnabled(!PMS.get().isMencoder_disablesubs());
				mencoder_ass_shadow.setEnabled(!PMS.get().isMencoder_disablesubs());
				mencoder_ass_margin.setEnabled(!PMS.get().isMencoder_disablesubs());
				mencoder_noass_scale.setEnabled(!PMS.get().isMencoder_disablesubs());
				mencoder_noass_outline.setEnabled(!PMS.get().isMencoder_disablesubs());
				mencoder_noass_blur.setEnabled(!PMS.get().isMencoder_disablesubs());
				mencoder_noass_subpos.setEnabled(!PMS.get().isMencoder_disablesubs());
				
				if (!PMS.get().isMencoder_disablesubs())
				 ass.getItemListeners()[0].itemStateChanged(null);
			}
      	
      });
     if (PMS.get().isMencoder_disablesubs())
  	   disableSubs.setSelected(true);
     
      
     
        return builder.getPanel();
	}

	@Override
	public int purpose() {
		return VIDEO_SIMPLEFILE_PLAYER;
	}
	
	public static final String ID = "mencoder";
	
	@Override
	public String id() {
		return ID;
	}
	
	@Override
	public boolean avisynth() {
		return false;
	}

	@Override
	public boolean isTimeSeekable() {
		return true;
	}
	
	protected boolean ac3;
	protected boolean pcm;
	protected boolean ovccopy;
	protected boolean dvd;
	

	protected String overridenMainArgs [];
	protected String defaultSubArgs [];
	
//	> -af channels=6:6:0:4:1:0:2:1:3:2:4:3:5:5
//	[...]
//
//	I've just noticed that I've posted the wrong remapping for DTS. The above 
//	is actually the one for AAC.
//
//	The right remapping for DTS is the following one:
//
//	-af channels=6:6:0:0:1:2:2:3:3:5:4:1:5:4
	
	protected String [] getDefaultArgs() { // 6:0:0:1:4:2:5:3:2:4:1:5:3
		return new String [] { "-quiet",/* pcm?"-quiet":"-af", pcm?"channels=6:6:0:0:1:2:2:3:3:5:4:1:5:5":"-quiet", */ /* pcm?"-format":"-quiet", pcm?"s24le":"", */ "-oac", pcm?"pcm":"lavc", "-of", (pcm||ac3)?"avi":"mpeg", /*"-lavfopts", "format=mpegts", */"-mpegopts", "format=mpeg2:muxrate=500000:vbuf_size=1194:abuf_size=64", "-ovc", ovccopy?"copy":"lavc" };
	}

	@Override
	public String[] args() {
		String args [] = null;
		String defaut [] = getDefaultArgs();
		if (overridenMainArgs != null) { 
			args = new String [defaut.length + overridenMainArgs.length];
			for(int i=0;i<defaut.length;i++)
				args[i] = defaut[i];
			for(int i=0;i<overridenMainArgs.length;i++) {
				if (overridenMainArgs[i].equals("-of") || overridenMainArgs[i].equals("-oac") || overridenMainArgs[i].equals("-ovc") || overridenMainArgs[i].equals("-mpegopts")) {
					PMS.minimal("MEncoder encoder settings: You cannot change Muxer, Muxer options, Video Codec or Audio Codec");
					overridenMainArgs[i] = "-quiet";
					if (i + 1 < overridenMainArgs.length)
						overridenMainArgs[i+1] = "-quiet";
				}
				args[i+defaut.length] = overridenMainArgs[i];
			}
		} else
			args = defaut;
		return args;
			
	}

	@Override
	public String executable() {
		return PMS.get().getMEncoderPath();
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media, OutputParams params)
			throws IOException {
		
		
		dvd = false;
		if (media != null && media.dvdtrack > 0)
			dvd = true;
		
		if ((media.losslessaudio && PMS.get().isMencoder_usepcm()) || (PMS.get().isTsmuxer_preremux_pcm() && params.losslessaudio)) {
			pcm = true;
			ac3 = false;
			params.losslessaudio = true;
			params.forceFps = media.getValidFps(false);
			if (params.no_videoencode)
				ovccopy = true;
		} else if (PMS.get().isTsmuxer_preremux_ac3() && params.lossyaudio) {
			pcm = false;
			ac3 = true;
			params.forceFps = media.getValidFps(false);
			if (params.no_videoencode)
				ovccopy = true;
		} else {
			ac3 = false;
			pcm = false;
		}
		
		String add = "";
		if (PMS.get().getMencoder_decode() == null || PMS.get().getMencoder_decode().indexOf("-lavdopts") == -1) {
			add = " -lavdopts debug=0";
		}
		
		String alternativeCodec = "";//"-ac ffac3,ffdca, "; 
		if (dvd)
			alternativeCodec = "";
		StringTokenizer st = new StringTokenizer(alternativeCodec + "-channels " + PMS.get().getAudiochannels() + " " + PMS.get().getMencoder_decode() + add, " ");
		overridenMainArgs = new String [st.countTokens()];
		int i = 0;
		boolean next = false;
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			if (next) {
				int nbcores = PMS.get().getNbcores();
				if (dvd || fileName.toLowerCase().endsWith("dvr-ms"))
					nbcores = 1;
				token += ":threads=" + nbcores;
				if (PMS.get().isSkiploopfilter() && !avisynth())
					token += ":skiploopfilter=all";
				next = false;
			} 
			if (token.toLowerCase().contains("lavdopts")) {
				next = true;
			}
			
			overridenMainArgs[i++] = token;
		}
		//}
		if (PMS.get().getMencoderMainSettings() != null) {
			String encodeSettings = "-psprobe 10000 -lavcopts vcodec=mpeg2video:acodec=ac3:abitrate=" + PMS.get().getAudiobitrate() + ":threads=" + PMS.get().getNbcores() + ":" + PMS.get().getMencoderMainSettings();
			String m = PMS.get().getMaximumbitrate();
			int bufs = 0;
			if (m.contains("(") && m.contains(")")) {
				bufs = Integer.parseInt(m.substring(m.indexOf("(")+1, m.indexOf(")")));
			}
			if (m.contains("("))
				m = m.substring(0, m.indexOf("(")).trim();
			
			int mb = Integer.parseInt(m);
			if (mb > 0 && !PMS.get().getMencoderMainSettings().contains("vrc_buf_size") && !PMS.get().getMencoderMainSettings().contains("vrc_maxrate") && !PMS.get().getMencoderMainSettings().contains("vbitrate")) {
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
				
				//bufSize = 2000;
				encodeSettings += ":vrc_maxrate=" + mb + ":vrc_buf_size=" + bufSize;
			}
			st = new StringTokenizer(encodeSettings, " ");
			int oldc = overridenMainArgs.length;
			overridenMainArgs = Arrays.copyOf(overridenMainArgs, overridenMainArgs.length + st.countTokens());
			i = oldc;
			while (st.hasMoreTokens()) {
				overridenMainArgs[i++] = st.nextToken();
			}
		}

		
		StringBuffer sb = new StringBuffer();
		if (!PMS.get().isMencoder_disablesubs() && PMS.get().isMencoder_ass() && !dvd)
			sb.append("-ass -" + (PMS.get().isMencoder_fontconfig()?"":"no") + "fontconfig ");
		if (!PMS.get().isMencoder_disablesubs() && PMS.get().getMencoder_subcp() != null && PMS.get().getMencoder_subcp().length() >  0) {
			sb.append("-subcp " +PMS.get().getMencoder_subcp() + " ");
			if (PMS.get().isMencoder_subfribidi()) {
				sb.append("-fribidi-charset " +PMS.get().getMencoder_subcp() + " ");
			}
		}
		if (PMS.get().getMencoder_audiolangs() != null && PMS.get().getMencoder_audiolangs().length() >  0)
			sb.append("-alang " +PMS.get().getMencoder_audiolangs() + " ");
		if (!PMS.get().isMencoder_disablesubs()) {
			if (PMS.get().isMencoder_ass()) {
				sb.append("-ass-color ffffff00 -ass-font-scale " + PMS.get().getMencoder_ass_scale());
				sb.append(" -ass-force-style FontName=Arial,Outline=" + PMS.get().getMencoder_ass_outline() + ",Shadow=" + PMS.get().getMencoder_ass_shadow() + ",MarginV=" + PMS.get().getMencoder_ass_margin() + " ");
			} else {
				sb.append("-subfont-text-scale " + PMS.get().getMencoder_noass_scale());
				sb.append(" -subfont-outline " + PMS.get().getMencoder_noass_outline());
				sb.append(" -subfont-blur " + PMS.get().getMencoder_noass_blur());
				int subpos = 1;
				try {
					subpos = Integer.parseInt(PMS.get().getMencoder_noass_subpos());
				} catch (NumberFormatException n) {}
				sb.append(" -subpos " + (100-subpos));
			}
		}
		
		st = new StringTokenizer(sb.toString(), " ");
		int oldc = overridenMainArgs.length;
		overridenMainArgs = Arrays.copyOf(overridenMainArgs, overridenMainArgs.length + st.countTokens());
		i = oldc;
		next = false;
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (next) {
				s = "-quiet";
				next =false;
			}
			if ((!PMS.get().isMencoder_ass() || dvd) && s.contains("-ass")) {
				s = "-quiet";
				
				next = true;
			}
			overridenMainArgs[i++] = s;
		}
		
		sb = new StringBuffer();
		if (!PMS.get().isMencoder_disablesubs() && PMS.get().getMencoder_sublangs() != null && PMS.get().getMencoder_sublangs().length() >  0) {
			sb.append("-slang " +PMS.get().getMencoder_sublangs() + " ");
		} else {
			int maxid = 1000;
			if (media != null && media.maxsubid > 0)
				maxid = media.maxsubid+1;
			sb.append("-sid " + maxid + " ");
		}
		
		String subs = sb.toString().trim();
		if (subs != null && subs.length() > 0) {
			
			st = new StringTokenizer(subs, " ");
			defaultSubArgs = new String [st.countTokens()];
			i = 0;
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				defaultSubArgs[i++] = s;
			}
		}
		
		boolean avisynth = avisynth()/* || params.avisynth*/;
		
		PipeProcess pipe = new PipeProcess("mencoder" + System.currentTimeMillis());
		params.input_pipes [0] = pipe;
		
		boolean vobsub = false;
		String subString = null;
		if (!avisynth && PMS.get().isUsesubs()) {
			String woExt = fileName.substring(0, fileName.length()-4);
			File srtFile = new File(woExt + ".srt");
			if (srtFile.exists()) {
				subString=srtFile.getAbsolutePath();
			}
			File assFile = new File(woExt + ".ass");
			if (assFile.exists()) {
				subString=assFile.getAbsolutePath();
			}
			File subFile = new File(woExt + ".sub");
			if (subFile.exists()) {
				subString=subFile.getAbsolutePath();
			}
			File idxFile = new File(woExt + ".idx");
			if (idxFile.exists()) {
				vobsub = true;
			}
		}
		
		if (this instanceof MEncoderWebVideo) {
			overridenMainArgs = new String [] { };
			defaultSubArgs = new String [] { };
			
		}
		
		String cmdArray [] = new String [18+args().length];
		cmdArray[0] = executable();
		cmdArray[1] = "-ss";
		if (params.timeseek > 0) {
			cmdArray[2] = "" + params.timeseek;
		} else {
			cmdArray[2] = "0";
		}
		cmdArray[3] = "-quiet";
		if (media != null&& media.dvdtrack > 0) {
			cmdArray[3] = "-dvd-device";
		}
		if (avisynth && !fileName.toLowerCase().endsWith(".iso")) {
			File avsFile = FFMpegVideo.getAVSScript(fileName, params.fromFrame, params.toFrame);
			cmdArray[4] = avsFile.getAbsolutePath();
		} else
			cmdArray[4] = fileName;
		cmdArray[5] = "-quiet";
		if (media != null&& media.dvdtrack > 0) {
			cmdArray[5] = "dvd://" + media.dvdtrack;
		}
		String arguments [] = args();
		for(i=0;i<arguments.length;i++) {
			cmdArray[6+i] = arguments[i];
			if (params.timeseek > 0 && arguments[i].contains("format=mpegts")) {
				cmdArray[6+i] += ":preload=" + params.timeseek;
				params.timeseek = 0; 
			}
			if (arguments[i].contains("format=mpeg2") && media.aspect != null && media.getValidAspect(true) != null) {
				cmdArray[6+i] += ":vaspect=" + media.getValidAspect(true);
			}
		}
		
		
		cmdArray[cmdArray.length-12] = "-quiet";
		cmdArray[cmdArray.length-11] = "-quiet";
		cmdArray[cmdArray.length-10] = "-quiet";
		cmdArray[cmdArray.length-9] = "-quiet";
		if (params.aid > -1 || params.sid > -1) {
			if (params.aid > -1) {
				cmdArray[cmdArray.length-12] = "-aid";
				cmdArray[cmdArray.length-11] = "" + params.aid;
			}
			cmdArray[cmdArray.length-10] = "-sid";
			if (params.sid > -1) {
				cmdArray[cmdArray.length-9] = "" + params.sid;
			} else {
				int maxid = 1000;
				if (media != null && media.maxsubid > 0)
					maxid = media.maxsubid+1;
				cmdArray[cmdArray.length-9] = maxid + "";
			}
		} else if (subString == null) {
			for(i=0;i<defaultSubArgs.length;i++) {
				if (i < 2)
					cmdArray[cmdArray.length-12+i] = defaultSubArgs[i];
			}
		} else {
			/*cmdArray[cmdArray.length-10] = "-sid";
			cmdArray[cmdArray.length-9] = "1000";*/
		}
		
		
		cmdArray[cmdArray.length-8] = "-quiet";
		cmdArray[cmdArray.length-7] = "-quiet";
		
		if (PMS.get().isMencoder_forcefps() && !(this instanceof MEncoderWebVideo)) {
			cmdArray[cmdArray.length-8] = "-fps";
			cmdArray[cmdArray.length-7] = "24000/1001";
		}
		cmdArray[cmdArray.length-6] = "-ofps";
		cmdArray[cmdArray.length-5] = "24000/1001";
		String frameRate = null;
		if (media != null) {
			frameRate = media.getValidFps(true);
		}
		if (frameRate != null) {
			cmdArray[cmdArray.length-5] = frameRate;
			if (PMS.get().isMencoder_forcefps() && !(this instanceof MEncoderWebVideo))
				cmdArray[cmdArray.length-7] = cmdArray[cmdArray.length-5];
		}
		/*if (media != null && media.dvdtrack > 0) {
			cmdArray[cmdArray.length-8] = "-quiet";
			cmdArray[cmdArray.length-7] = "-quiet";
			cmdArray[cmdArray.length-6] = "-quiet";
			cmdArray[cmdArray.length-5] = "-quiet";
		}*/
		if (subString != null && !PMS.get().isMencoder_disablesubs()) {
			if (vobsub) {
				// vobsub not supported in MEncoder :\
				//cmdArray[cmdArray.length-4] = "-vobsub";
				//cmdArray[cmdArray.length-3] = subString.substring(0, subString.length()-4);
			} else {
				cmdArray[cmdArray.length-4] = "-sub";
				cmdArray[cmdArray.length-3] = subString;
			}
		} else {
			cmdArray[cmdArray.length-4] = "-quiet";
			cmdArray[cmdArray.length-3] = "-quiet";
		}
		
		// set noskip and -mc 0
		if (fileName.toLowerCase().endsWith("dvr-ms")) {
			cmdArray[cmdArray.length-8] = "-vf";
			cmdArray[cmdArray.length-7] = "scale";
			cmdArray[cmdArray.length-6] = "-quiet";
			cmdArray[cmdArray.length-5] = "-quiet";
		}
		if (PMS.get().isMencoder_nooutofsync()) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +3);
			cmdArray[cmdArray.length-5] = "-mc";
			cmdArray[cmdArray.length-4] = "0";
			cmdArray[cmdArray.length-3] = "-noskip";
			
			if (PMS.get().isMencoder_intelligent_sync()) {
				if (media != null && media.codecA != null && media.codecV != null && ((media.codecA.equals("mp3") && media.codecV.equals("mpeg4"))
						|| (fileName.toLowerCase().endsWith("dvr-ms")))) {
					// correction A/V in mplayer for some xvid+mp3, and dvr-ms
					cmdArray[cmdArray.length-4] = "0.1";
					cmdArray[cmdArray.length-3] = "-quiet";
				}
				/*if (media != null && media.codecA != null && media.codecV != null && (((media.codecA.equals("dts") || media.codecA.equals("dca") || media.codecA.equals("ac3")) && media.codecV.equals("h264")) ||
						media.codecV.startsWith("RV"))) {
					// NO A/V for H264+AC3/DTS ??
					cmdArray[cmdArray.length-5] = "-quiet";
					cmdArray[cmdArray.length-4] = "-quiet";
					cmdArray[cmdArray.length-3] = "-quiet";
				}*/
			}
		}
		
		
		// force srate when sample rate < 32000 or > 48000 -> ac3's mencoder doesn't like that
		if (media != null && !pcm && !ac3 && ((media.getSampleRate() > 0 && media.getSampleRate() < 32000) || media.getSampleRate() > 48000)) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +2);
			cmdArray[cmdArray.length-4] = "-srate";
			cmdArray[cmdArray.length-3] = "48000";
		}
	
		
		cmdArray[cmdArray.length-2] = "-o";
		cmdArray[cmdArray.length-1] = pipe.getInputPipe();
		
		
		
		ProcessWrapper mkfifo_process = pipe.getPipeProcess();
		
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		pw.attachProcess(mkfifo_process);
		mkfifo_process.runInNewThread();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) { }
		pipe.deleteLater();
		
		if (pcm || ac3) {
			PipeProcess videoPipe = new PipeProcess("videoPipe" + System.currentTimeMillis(), "out", "reconnect");
			PipeProcess audioPipe = new PipeProcess("audioPipe" + System.currentTimeMillis(), "out", "reconnect");
			
			ProcessWrapper videoPipeProcess = videoPipe.getPipeProcess();
			ProcessWrapper audioPipeProcess = audioPipe.getPipeProcess();
			
			params.output_pipes[0] = videoPipe;
			params.output_pipes[1] = audioPipe;
			
			
			pw.attachProcess(videoPipeProcess);
			pw.attachProcess(audioPipeProcess);
			videoPipeProcess.runInNewThread();
			audioPipeProcess.runInNewThread();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) { }
			videoPipe.deleteLater();
			audioPipe.deleteLater();
		}
		
		pw.runInNewThread();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) { }
		return pw;
	}

	@Override
	public String mimeType() {
		return "video/mpeg";
	}

	@Override
	public String name() {
		return "MEncoder";
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

	

}
