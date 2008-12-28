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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
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
import net.pms.newgui.FontFileFilter;
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
	private JTextField defaultaudiosubs;
	private JTextField defaultfont;
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
                "left:pref, 3dlu, p:grow, 3dlu, right:p:grow, 3dlu, p:grow, 3dlu, right:p:grow,3dlu, p:grow, 3dlu, right:p:grow,3dlu, pref:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, p , 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
         checkBox = new JCheckBox(Messages.getString("MEncoderVideo.0")); //$NON-NLS-1$
        checkBox.setContentAreaFilled(false);
        if (PMS.get().isSkiploopfilter())
        	checkBox.setSelected(true);
        checkBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setSkipLoopFilter(e.getStateChange() == ItemEvent.SELECTED);
			}
        	
        });
        
        builder.addSeparator(Messages.getString("MEncoderVideo.1"),  cc.xyw(1, 1, 15)); //$NON-NLS-1$
       builder.add(checkBox,          cc.xyw(1,  3, 15));
       
       noskip = new JCheckBox(Messages.getString("MEncoderVideo.2")); //$NON-NLS-1$
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
       
       intelligentsync = new JCheckBox(Messages.getString("MEncoderVideo.3")); //$NON-NLS-1$
       intelligentsync.setContentAreaFilled(false);
       if (PMS.get().isMencoder_intelligent_sync())
    	   intelligentsync.setSelected(true);
       intelligentsync.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_intelligent_sync(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       if (!PMS.get().isMencoder_nooutofsync())
    	   intelligentsync.setEnabled(false);
       builder.add(intelligentsync,          cc.xyw(3,  5, 12));
       
       forcefps = new JCheckBox(Messages.getString("MEncoderVideo.4")); //$NON-NLS-1$
       forcefps.setContentAreaFilled(false);
       if (PMS.get().isMencoder_forcefps())
    	   forcefps.setSelected(true);
       forcefps.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_forcefps(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       
       builder.add(forcefps,          cc.xyw(1,  7, 15));
       
       
  
       builder.addSeparator(Messages.getString("MEncoderVideo.5"), cc.xyw(1, 9, 15)); //$NON-NLS-1$
       builder.addLabel(Messages.getString("MEncoderVideo.6"), cc.xy(1, 11)); //$NON-NLS-1$
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
       
      
       
       builder.addLabel(Messages.getString("MEncoderVideo.7"), cc.xyw(1, 13, 15)); //$NON-NLS-1$
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
       builder.add(langs, cc.xyw(3, 13, 8));
       
       builder.addSeparator(Messages.getString("MEncoderVideo.8"),  cc.xyw(1, 15, 15)); //$NON-NLS-1$
       
        
     
       
       builder.addLabel(Messages.getString("MEncoderVideo.9"), cc.xy(1, 17)); //$NON-NLS-1$
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
       builder.add(defaultsubs, cc.xyw(3, 17, 8));
       
       builder.addLabel(Messages.getString("MEncoderVideo.10"), cc.xy(1, 19)); //$NON-NLS-1$
       defaultaudiosubs = new JTextField(PMS.get().getMencoder_audiosublangs());
       defaultaudiosubs.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_audiosublangs(defaultaudiosubs.getText());
   		}
       	   
          });
       builder.add(defaultaudiosubs, cc.xyw(3, 19, 8));
       
       builder.addLabel(Messages.getString("MEncoderVideo.11"), cc.xy(1, 21)); //$NON-NLS-1$
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
    		   "cp1250  /* Windows - Eastern Europe */", //$NON-NLS-1$
    		   "cp1251  /* Windows - Cyrillic */", //$NON-NLS-1$
    		   "cp1252  /* Windows - Western Europe */", //$NON-NLS-1$
    		   "cp1253  /* Windows - Greek */", //$NON-NLS-1$
    		   "cp1254  /* Windows - Turkish */", //$NON-NLS-1$
    		   "cp1255  /* Windows - Hebrew */", //$NON-NLS-1$
    		   "cp1256  /* Windows - Arabic */", //$NON-NLS-1$
    		   "cp1257  /* Windows - Baltic */", //$NON-NLS-1$
    		   "cp1258  /* Windows - Vietnamese */", //$NON-NLS-1$
    		   "ISO-8859-1 /* Western Europe */", //$NON-NLS-1$
    		   "ISO-8859-2 /* Western and Central Europe */", //$NON-NLS-1$
    		   "ISO-8859-3 /* Western Europe and South European */", //$NON-NLS-1$
    		   "ISO-8859-4 /* Western Europe and Baltic countries */", //$NON-NLS-1$
    		   "ISO-8859-5 /* Cyrillic alphabet */", //$NON-NLS-1$
    		   "ISO-8859-6 /* Arabic */", //$NON-NLS-1$
    		   "ISO-8859-7 /* Greek */", //$NON-NLS-1$
    		   "ISO-8859-8 /* Hebrew */", //$NON-NLS-1$
    		   "ISO-8859-9 /* Western Europe with amended Turkish */", //$NON-NLS-1$
    		   "ISO-8859-10 /* Western Europe with Nordic languages */", //$NON-NLS-1$
    		   "ISO-8859-11 /* Thai */", //$NON-NLS-1$
    		   "ISO-8859-13 /* Baltic languages plus Polish */", //$NON-NLS-1$
    		   "ISO-8859-14 /* Celtic languages */", //$NON-NLS-1$
    		   "ISO-8859-15 /* Added the Euro sign */", //$NON-NLS-1$
    		   "ISO-8859-16 /* Central European languages */", //$NON-NLS-1$
    		   "cp932   /* Japanese */", //$NON-NLS-1$
    		   "cp936   /* Chinese */", //$NON-NLS-1$
    		   "cp949   /* Korean */", //$NON-NLS-1$
    		   "cp950   /* Big5, Taiwanese, Cantonese */"}; //$NON-NLS-1$
       MyComboBoxModel cbm = new MyComboBoxModel(data);
       
       subcp = new JComboBox(cbm);
       subcp.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String s = (String) e.getItem();
					if (s.indexOf("/*") > -1) { //$NON-NLS-1$
						s = s.substring(0, s.indexOf("/*")).trim(); //$NON-NLS-1$
					}
					PMS.get().setMencoder_subcp(s);
				}
			}
       	
       });
       subcp.setEditable(true);
       builder.add(subcp, cc.xyw(3, 21,7));
       
       fribidi = new JCheckBox("FriBiDi mode"); //$NON-NLS-1$
       fribidi.setContentAreaFilled(false);
       if (PMS.get().isMencoder_subfribidi())
    	   fribidi.setSelected(true);
       fribidi.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_subfribidi(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       builder.add(fribidi, cc.xyw(11, 21, 4));
       
       builder.addLabel(Messages.getString("MEncoderVideo.24"), cc.xy(1, 23)); //$NON-NLS-1$
       defaultfont = new JTextField(PMS.get().getMencoder_font());
       defaultfont.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			PMS.get().setMencoder_font(defaultfont.getText());
   		}
       	   
          });
       builder.add(defaultfont, cc.xyw(3, 23, 8));
       
       JButton fontselect = new JButton("..."); //$NON-NLS-1$
       fontselect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FontFileFilter());
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("MEncoderVideo.25")); //$NON-NLS-1$
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	defaultfont.setText(chooser.getSelectedFile().getAbsolutePath());
			    	PMS.get().setMencoder_font(chooser.getSelectedFile().getAbsolutePath());
			    }
			}
 		  
 	  });
 	  builder.add(fontselect,          cc.xyw(11,  23, 2));
       
       builder.addLabel(Messages.getString("MEncoderVideo.12"), cc.xy(1, 27)); //$NON-NLS-1$
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
       
       builder.addLabel(Messages.getString("MEncoderVideo.13"), cc.xy(5, 27)); //$NON-NLS-1$
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
       
       builder.addLabel(Messages.getString("MEncoderVideo.14"), cc.xy(9, 27)); //$NON-NLS-1$
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
       
       builder.addLabel(Messages.getString("MEncoderVideo.15"), cc.xy(13, 27)); //$NON-NLS-1$
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
 builder.add(mencoder_ass_scale, cc.xy(3, 27));
       
       builder.add(mencoder_ass_outline, cc.xy(7, 27));
       
       builder.add(mencoder_ass_shadow, cc.xy(11, 27));
      
       builder.add(mencoder_ass_margin, cc.xy(15, 27));
       
       
       builder.addLabel(Messages.getString("MEncoderVideo.16"), cc.xy(1, 29)); //$NON-NLS-1$
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
       
       builder.addLabel(Messages.getString("MEncoderVideo.17"), cc.xy(5, 29)); //$NON-NLS-1$
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
       
       builder.addLabel(Messages.getString("MEncoderVideo.18"), cc.xy(9, 29)); //$NON-NLS-1$
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
       
       builder.addLabel(Messages.getString("MEncoderVideo.19"), cc.xy(13, 29)); //$NON-NLS-1$
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
 builder.add(mencoder_noass_scale, cc.xy(3, 29));
       
       builder.add(mencoder_noass_outline, cc.xy(7, 29));
       
       builder.add(mencoder_noass_blur, cc.xy(11, 29));
      
       builder.add(mencoder_noass_subpos, cc.xy(15, 29));
       
       
       ass = new JCheckBox(Messages.getString("MEncoderVideo.20")); //$NON-NLS-1$
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
       
       builder.add(ass,          cc.xy(1,  25));
       ass.setSelected(PMS.get().isMencoder_ass());
      ass.getItemListeners()[0].itemStateChanged(null);
     
      fc = new JCheckBox(Messages.getString("MEncoderVideo.21")); //$NON-NLS-1$
      fc.setContentAreaFilled(false);
      fc.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_fontconfig(e.getStateChange() == ItemEvent.SELECTED);
			}
      	
      });
      
      builder.add(fc,          cc.xyw(3,  25, 12));
      fc.setSelected(PMS.get().isMencoder_fontconfig());
       
       subs = new JCheckBox(Messages.getString("MEncoderVideo.22")); //$NON-NLS-1$
       subs.setContentAreaFilled(false);
       if (PMS.get().isUsesubs())
    	   subs.setSelected(true);
       subs.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setUsesubs(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       builder.add(subs, cc.xyw(1, 31, 15));
       
       JTextArea decodeTips = new JTextArea(Messages.getString("MEncoderVideo.23")); //$NON-NLS-1$
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
	
	public static final String ID = "mencoder"; //$NON-NLS-1$
	
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
		return new String [] { "-quiet",/* pcm?"-quiet":"-af", pcm?"channels=6:6:0:0:1:2:2:3:3:5:4:1:5:5":"-quiet", */ /* pcm?"-format":"-quiet", pcm?"s24le":"", */ "-oac", pcm?"pcm":"lavc", "-of", (pcm||ac3)?"avi":"mpeg", /*"-lavfopts", "format=mpegts", */"-mpegopts", "format=mpeg2:muxrate=500000:vbuf_size=1194:abuf_size=64", "-ovc", ovccopy?"copy":"lavc" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
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
				if (overridenMainArgs[i].equals("-of") || overridenMainArgs[i].equals("-oac") || overridenMainArgs[i].equals("-ovc") || overridenMainArgs[i].equals("-mpegopts")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					PMS.minimal("MEncoder encoder settings: You cannot change Muxer, Muxer options, Video Codec or Audio Codec"); //$NON-NLS-1$
					overridenMainArgs[i] = "-quiet"; //$NON-NLS-1$
					if (i + 1 < overridenMainArgs.length)
						overridenMainArgs[i+1] = "-quiet"; //$NON-NLS-1$
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
		
		String add = ""; //$NON-NLS-1$
		if (PMS.get().getMencoder_decode() == null || PMS.get().getMencoder_decode().indexOf("-lavdopts") == -1) { //$NON-NLS-1$
			add = " -lavdopts debug=0"; //$NON-NLS-1$
		}
		
		String alternativeCodec = "";//"-ac ffac3,ffdca, ";  //$NON-NLS-1$
		if (dvd)
			alternativeCodec = ""; //$NON-NLS-1$
		StringTokenizer st = new StringTokenizer(alternativeCodec + "-channels " + PMS.get().getAudiochannels() + " " + PMS.get().getMencoder_decode() + add, " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		overridenMainArgs = new String [st.countTokens()];
		int i = 0;
		boolean next = false;
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			if (next) {
				int nbcores = PMS.get().getNbcores();
				if (dvd || fileName.toLowerCase().endsWith("dvr-ms")) //$NON-NLS-1$
					nbcores = 1;
				token += ":threads=" + nbcores; //$NON-NLS-1$
				if (PMS.get().isSkiploopfilter() && !avisynth())
					token += ":skiploopfilter=all"; //$NON-NLS-1$
				next = false;
			} 
			if (token.toLowerCase().contains("lavdopts")) { //$NON-NLS-1$
				next = true;
			}
			
			overridenMainArgs[i++] = token;
		}
		//}
		if (PMS.get().getMencoderMainSettings() != null) {
			String encodeSettings = "-lavcopts autoaspect=1:vcodec=mpeg2video:acodec=ac3:abitrate=" + PMS.get().getAudiobitrate() + ":threads=" + PMS.get().getNbcores() + ":" + PMS.get().getMencoderMainSettings(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String m = PMS.get().getMaximumbitrate();
			int bufs = 0;
			if (m.contains("(") && m.contains(")")) { //$NON-NLS-1$ //$NON-NLS-2$
				bufs = Integer.parseInt(m.substring(m.indexOf("(")+1, m.indexOf(")"))); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (m.contains("(")) //$NON-NLS-1$
				m = m.substring(0, m.indexOf("(")).trim(); //$NON-NLS-1$
			
			int mb = Integer.parseInt(m);
			if (mb > 0 && !PMS.get().getMencoderMainSettings().contains("vrc_buf_size") && !PMS.get().getMencoderMainSettings().contains("vrc_maxrate") && !PMS.get().getMencoderMainSettings().contains("vbitrate")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
				encodeSettings += ":vrc_maxrate=" + mb + ":vrc_buf_size=" + bufSize; //$NON-NLS-1$ //$NON-NLS-2$
			}
			st = new StringTokenizer(encodeSettings, " "); //$NON-NLS-1$
			int oldc = overridenMainArgs.length;
			overridenMainArgs = Arrays.copyOf(overridenMainArgs, overridenMainArgs.length + st.countTokens());
			i = oldc;
			while (st.hasMoreTokens()) {
				overridenMainArgs[i++] = st.nextToken();
			}
		}

		
		StringBuffer sb = new StringBuffer();
		if (!PMS.get().isMencoder_disablesubs() && PMS.get().isMencoder_ass() && !dvd)
			sb.append("-ass -" + (PMS.get().isMencoder_fontconfig()?"":"no") + "fontconfig "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (!PMS.get().isMencoder_disablesubs() && PMS.get().getMencoder_subcp() != null && PMS.get().getMencoder_subcp().length() >  0) {
			sb.append("-subcp " +PMS.get().getMencoder_subcp() + " "); //$NON-NLS-1$ //$NON-NLS-2$
			if (PMS.get().isMencoder_subfribidi()) {
				sb.append("-fribidi-charset " +PMS.get().getMencoder_subcp() + " "); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (PMS.get().getMencoder_audiolangs() != null && PMS.get().getMencoder_audiolangs().length() >  0)
			sb.append("-alang " +PMS.get().getMencoder_audiolangs() + " "); //$NON-NLS-1$ //$NON-NLS-2$
		if (!PMS.get().isMencoder_disablesubs()) {
			if (PMS.get().getMencoder_font() != null && PMS.get().getMencoder_font().length() > 0) {
				sb.append("-font " + PMS.get().getMencoder_font() + " "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (PMS.get().isMencoder_ass()) {
				sb.append("-ass-color ffffff00 -ass-border-color 00000000 -ass-font-scale " + PMS.get().getMencoder_ass_scale()); //$NON-NLS-1$
				sb.append(" -ass-force-style FontName=Arial,Outline=" + PMS.get().getMencoder_ass_outline() + ",Shadow=" + PMS.get().getMencoder_ass_shadow() + ",MarginV=" + PMS.get().getMencoder_ass_margin() + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			} else {
				sb.append("-subfont-text-scale " + PMS.get().getMencoder_noass_scale()); //$NON-NLS-1$
				sb.append(" -subfont-outline " + PMS.get().getMencoder_noass_outline()); //$NON-NLS-1$
				sb.append(" -subfont-blur " + PMS.get().getMencoder_noass_blur()); //$NON-NLS-1$
				int subpos = 1;
				try {
					subpos = Integer.parseInt(PMS.get().getMencoder_noass_subpos());
				} catch (NumberFormatException n) {}
				sb.append(" -subpos " + (100-subpos)); //$NON-NLS-1$
			}
		}
		
		st = new StringTokenizer(sb.toString(), " "); //$NON-NLS-1$
		int oldc = overridenMainArgs.length;
		overridenMainArgs = Arrays.copyOf(overridenMainArgs, overridenMainArgs.length + st.countTokens());
		i = oldc;
		next = false;
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (next) {
				s = "-quiet"; //$NON-NLS-1$
				next =false;
			}
			if ((!PMS.get().isMencoder_ass() || dvd) && s.contains("-ass")) { //$NON-NLS-1$
				s = "-quiet"; //$NON-NLS-1$
				
				next = true;
			}
			overridenMainArgs[i++] = s;
		}
		
		sb = new StringBuffer();
		if (!PMS.get().isMencoder_disablesubs() && PMS.get().getMencoder_sublangs() != null && PMS.get().getMencoder_sublangs().length() >  0) {
			sb.append("-slang " +PMS.get().getMencoder_sublangs() + " "); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			int maxid = 1000;
			if (media != null && media.maxsubid > 0)
				maxid = media.maxsubid+1;
			sb.append("-sid " + maxid + " "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		String subs = sb.toString().trim();
		if (subs != null && subs.length() > 0) {
			
			st = new StringTokenizer(subs, " "); //$NON-NLS-1$
			defaultSubArgs = new String [st.countTokens()];
			i = 0;
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				defaultSubArgs[i++] = s;
			}
		}
		
		boolean avisynth = avisynth()/* || params.avisynth*/;
		
		PipeProcess pipe = new PipeProcess("mencoder" + System.currentTimeMillis()); //$NON-NLS-1$
		params.input_pipes [0] = pipe;
		
		boolean vobsub = false;
		String subString = null;
		if (!avisynth && PMS.get().isUsesubs()) {
			String woExt = fileName.substring(0, fileName.length()-4);
			File srtFile = new File(woExt + ".srt"); //$NON-NLS-1$
			if (srtFile.exists()) {
				subString=srtFile.getAbsolutePath();
			}
			File assFile = new File(woExt + ".ass"); //$NON-NLS-1$
			if (assFile.exists()) {
				subString=assFile.getAbsolutePath();
			}
			File subFile = new File(woExt + ".sub"); //$NON-NLS-1$
			if (subFile.exists()) {
				subString=subFile.getAbsolutePath();
			}
			File idxFile = new File(woExt + ".idx"); //$NON-NLS-1$
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
		cmdArray[1] = "-ss"; //$NON-NLS-1$
		if (params.timeseek > 0) {
			cmdArray[2] = "" + params.timeseek; //$NON-NLS-1$
		} else {
			cmdArray[2] = "0"; //$NON-NLS-1$
		}
		cmdArray[3] = "-quiet"; //$NON-NLS-1$
		if (media != null&& media.dvdtrack > 0) {
			cmdArray[3] = "-dvd-device"; //$NON-NLS-1$
		}
		if (avisynth && !fileName.toLowerCase().endsWith(".iso")) { //$NON-NLS-1$
			File avsFile = FFMpegVideo.getAVSScript(fileName, params.fromFrame, params.toFrame);
			cmdArray[4] = avsFile.getAbsolutePath();
		} else
			cmdArray[4] = fileName;
		cmdArray[5] = "-quiet"; //$NON-NLS-1$
		if (media != null&& media.dvdtrack > 0) {
			cmdArray[5] = "dvd://" + media.dvdtrack; //$NON-NLS-1$
		}
		String arguments [] = args();
		for(i=0;i<arguments.length;i++) {
			cmdArray[6+i] = arguments[i];
			if (params.timeseek > 0 && arguments[i].contains("format=mpegts")) { //$NON-NLS-1$
				cmdArray[6+i] += ":preload=" + params.timeseek; //$NON-NLS-1$
				params.timeseek = 0; 
			}
			if (arguments[i].contains("format=mpeg2") && media.aspect != null && media.getValidAspect(true) != null) { //$NON-NLS-1$
				cmdArray[6+i] += ":vaspect=" + media.getValidAspect(true); //$NON-NLS-1$
			}
		}
		
		
		cmdArray[cmdArray.length-12] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length-11] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length-10] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length-9] = "-quiet"; //$NON-NLS-1$
		if (params.aid > -1 || params.sid > -1) {
			if (params.aid > -1) {
				cmdArray[cmdArray.length-12] = "-aid"; //$NON-NLS-1$
				cmdArray[cmdArray.length-11] = "" + params.aid; //$NON-NLS-1$
			}
			cmdArray[cmdArray.length-10] = "-sid"; //$NON-NLS-1$
			if (params.sid > -1) {
				cmdArray[cmdArray.length-9] = "" + params.sid; //$NON-NLS-1$
			} else {
				int maxid = 1000;
				if (media != null)
					maxid = media.maxsubid+1;
				cmdArray[cmdArray.length-9] = maxid + ""; //$NON-NLS-1$
			}
		} else if (subString == null) {
			for(i=0;i<defaultSubArgs.length;i++) {
				if (i < 2)
					cmdArray[cmdArray.length-12+i] = defaultSubArgs[i];
			}
		} else if (subString != null && media != null) {
			cmdArray[cmdArray.length-10] = "-sid"; //$NON-NLS-1$
			int maxid = 1000;
			if (media != null)
				maxid = media.maxsubid+1;
			cmdArray[cmdArray.length-9] = ""+ maxid; //$NON-NLS-1$
		}
		if (params.aid == -1 && params.sid == -1) {
			int as [] = media.getAudioSubLangIds();
			if (media != null && as != null) {
				cmdArray[cmdArray.length-12] = "-aid"; //$NON-NLS-1$
				cmdArray[cmdArray.length-11] = "" + as[0]; //$NON-NLS-1$
				cmdArray[cmdArray.length-10] = "-sid"; //$NON-NLS-1$
				cmdArray[cmdArray.length-9] = "" + as[1]; //$NON-NLS-1$
			}
		}
		
		cmdArray[cmdArray.length-8] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length-7] = "-quiet"; //$NON-NLS-1$
		
		if (PMS.get().isMencoder_forcefps() && !(this instanceof MEncoderWebVideo)) {
			cmdArray[cmdArray.length-8] = "-fps"; //$NON-NLS-1$
			cmdArray[cmdArray.length-7] = "24000/1001"; //$NON-NLS-1$
		}
		cmdArray[cmdArray.length-6] = "-ofps"; //$NON-NLS-1$
		cmdArray[cmdArray.length-5] = "24000/1001"; //$NON-NLS-1$
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
				cmdArray[cmdArray.length-4] = "-sub"; //$NON-NLS-1$
				cmdArray[cmdArray.length-3] = subString;
			}
		} else {
			cmdArray[cmdArray.length-4] = "-quiet"; //$NON-NLS-1$
			cmdArray[cmdArray.length-3] = "-quiet"; //$NON-NLS-1$
		}
		
		// set noskip and -mc 0
		if (fileName.toLowerCase().endsWith("dvr-ms")) { //$NON-NLS-1$
			cmdArray[cmdArray.length-8] = "-vf"; //$NON-NLS-1$
			cmdArray[cmdArray.length-7] = "scale"; //$NON-NLS-1$
			cmdArray[cmdArray.length-6] = "-quiet"; //$NON-NLS-1$
			cmdArray[cmdArray.length-5] = "-quiet"; //$NON-NLS-1$
		}
		if (PMS.get().isMencoder_nooutofsync()) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +3);
			cmdArray[cmdArray.length-5] = "-mc"; //$NON-NLS-1$
			cmdArray[cmdArray.length-4] = "0"; //$NON-NLS-1$
			cmdArray[cmdArray.length-3] = "-noskip"; //$NON-NLS-1$
			
			if (PMS.get().isMencoder_intelligent_sync()) {
				if (media != null && media.codecA != null && media.codecV != null && ((media.codecA.equals("mp3") && media.codecV.equals("mpeg4")) //$NON-NLS-1$ //$NON-NLS-2$
						|| (fileName.toLowerCase().endsWith("dvr-ms")))) { //$NON-NLS-1$
					// correction A/V in mplayer for some xvid+mp3, and dvr-ms
					cmdArray[cmdArray.length-4] = "0.1"; //$NON-NLS-1$
					cmdArray[cmdArray.length-3] = "-quiet"; //$NON-NLS-1$
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
		if (media != null && !pcm && !ac3 && ((media.getSampleRate() > 0 && media.getSampleRate() < 32000) || media.getSampleRate() >= 48000)) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +2);
			cmdArray[cmdArray.length-4] = "-srate"; //$NON-NLS-1$
			cmdArray[cmdArray.length-3] = "48000"; //$NON-NLS-1$
		}
	
		
		cmdArray[cmdArray.length-2] = "-o"; //$NON-NLS-1$
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
			PipeProcess videoPipe = new PipeProcess("videoPipe" + System.currentTimeMillis(), "out", "reconnect"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			PipeProcess audioPipe = new PipeProcess("audioPipe" + System.currentTimeMillis(), "out", "reconnect"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
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
		return "video/mpeg"; //$NON-NLS-1$
	}

	@Override
	public String name() {
		return "MEncoder"; //$NON-NLS-1$
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

	

}
