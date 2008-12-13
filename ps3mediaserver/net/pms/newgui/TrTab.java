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
package net.pms.newgui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import net.pms.PMS;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TrTab {
	
	public JCheckBox getCheckBox() {
		return checkBox;
	}

	public static final String DISABLED = "[disabled] ";
	public static HashMap<String, String> engines = new HashMap<String, String>();
	
	static {
		engines.put("tsmuxer", "tsMuxer");
		engines.put("avsffmpeg", "AviSynth/FFMpeg");
		engines.put("avsmencoder", "AviSynth/MEncoder");
		engines.put("mencoder", "MEncoder only");
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
	//private JTextField substyle;
	private JTextField langs;
	private JTextField defaultsubs;
	private JComboBox subcp;
	private JTextField abitrate;
	private JTextField maxbitrate;
	private JList list;
	private JCheckBox  checkBox ;
	private JCheckBox  noskip ;
	private JCheckBox  forcefps ;
	public JCheckBox getNoskip() {
		return noskip;
	}

	private JCheckBox  fc ;
	private JCheckBox  ass ;
	private JCheckBox  disableSubs ;
	private JCheckBox  forcePCM ;
	private JCheckBox convertfps;
	private JCheckBox tsmuxerforcefps;
	private JCheckBox tsmuxerforcepcm;
	private JCheckBox tsmuxerforceac3;
	private JCheckBox  subs ;
	private JCheckBox fribidi;
	public JCheckBox getSubs() {
		return subs;
	}

	private JComboBox channels;
	private JComboBox vq ;
	private JTextField ffmpeg;
	private DefaultListModel model;
	private JTextArea textArea;
	
	public JComponent build() {
		FormLayout mainlayout = new FormLayout(
				"left:pref, pref, 7dlu, pref, pref, 0:grow",
				"p, 3dlu,p, 3dlu, p, 3dlu, 0:grow" );
		PanelBuilder builder = new PanelBuilder(mainlayout);
        builder.setBorder(Borders.DLU4_BORDER);
		
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        JLabel jl1 = builder.addLabel("You will find here the list of transcoding engines. The top one will appears and replace any unsupported video on their original folder", cc.xyw(2, 1, 5));
        JLabel jl2 = builder.addLabel("the others will be accessible in a special sub folder called \"#Transcoded#\", added with several languages options [Mencoder only]", cc.xyw(2, 3, 5));
        jl1.setFont(jl1.getFont().deriveFont(Font.BOLD));
        jl2.setFont(jl2.getFont().deriveFont(Font.BOLD));
        builder.add(buildLeft(), cc.xy(2, 5));
        builder.add(buildRightTabbedPane(), cc.xyw(4, 5, 3));
        
        return builder.getPanel();
	}
	
	private void updateEngineModel() {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> newengines = new ArrayList<String>();
		for(int i=0;i<model.size();i++) {
			String engine = (String) model.getElementAt(i);
			if (!engine.startsWith(DISABLED)) {
				Iterator<Entry<String, String>> it = engines.entrySet().iterator();
		        while (it.hasNext()) {
		        	Entry<String, String> entry = it.next();
		        	if (entry.getValue().equals(engine)) {
		        		engine = entry.getKey();
		        		newengines.add(engine);
						if (sb.length() > 0)
							sb.append(",");
						sb.append(engine);
		        	}
		        }
		        
			}
		}
		PMS.get().getFrame().setReloadable(true);
		PMS.get().setEngines(sb.toString());
		PMS.get().setEnginesAsList(newengines);
	}
	
	public JComponent buildRightTabbedPane() {
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.addTab("Common Player Settings", buildCommon());
		tabbedPane.addTab("Common Encoder Settings", buildMEncoder());
		tabbedPane.addTab("MEncoder only Player", buildMEncoderDec());
		tabbedPane.addTab("AviSynth Player", buildAviSynth());
		tabbedPane.addTab("TsMuxer Player", buildTsMuxer());
		tabbedPane.addTab("FFmpeg Player", buildFFmpeg());
		return tabbedPane;
	}
	
	public JComponent buildLeft() {
		FormLayout layout = new FormLayout(
                "left:pref, pref, pref, pref, 0:grow",
                "top:pref, 3dlu, p, 3dlu, p, 20dlu, p, 3dlu, p, 3dlu, p");
         PanelBuilder builder = new PanelBuilder(layout);
       // builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(false);
        
        model = new DefaultListModel();
        
        if (PMS.get().getEnginesAsList() != null)
        	for(String engine:PMS.get().getEnginesAsList()) {
        		model.addElement(engines.get(engine));
        	}
        Iterator<String> it = engines.values().iterator();
        while (it.hasNext()) {
        	String engine = it.next();
        	if (!model.contains(engine)) {
        		model.addElement(DISABLED + engine);
        	}
        }
      
        
        list = new JList(model);
        list.setCellRenderer(new ListCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(150,250 ));
         pane.setBorder(BorderFactory.createEtchedBorder());
        
        CellConstraints cc = new CellConstraints();
      
       
       JButton but = new JButton(LooksFrame.readImageIcon("kdevelop_down-32.png"));
       but.setToolTipText("Sort the transcoding engines list. First one will appears in the original video folder");
       but.setBorder(BorderFactory.createEmptyBorder());
       but.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			for(int i=0;i<model.size()-1;i++) {
				if (list.isSelectedIndex(i)) {
					String value = model.get(i).toString();
					model.set(i, model.get(i+1));
					model.set(i+1, value);
					list.setSelectedIndex(i+1);
					updateEngineModel();
					break;
				}
			}
		}   	   
       });
       
       
       
       builder.add(but,          cc.xy(2,  1));
       JButton but2 = new JButton(LooksFrame.readImageIcon("up-32.png"));
       but2.setToolTipText("Sort the transcoding engines list. First one will appears in the original video folder");
       but2.setBorder(BorderFactory.createEmptyBorder());
       but2.addActionListener(new ActionListener() {
   		public void actionPerformed(ActionEvent e) {
   			for(int i=1;i<model.size();i++) {
   				if (list.isSelectedIndex(i)) {
   					String value = model.get(i).toString();
   					
   					model.set(i, model.get(i-1));
   					model.set(i-1, value);
   					list.setSelectedIndex(i-1);
   					updateEngineModel();
   					break;

   				}
   			}
   		}   	   
          });
       builder.add(but2,          cc.xy(3,  1));
       JButton but3 = new JButton(LooksFrame.readImageIcon("connect_no-32.png"));
       but3.setToolTipText("Enable/disable a transcoding engine");
       but3.setBorder(BorderFactory.createEmptyBorder());
       but3.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			for(int i=0;i<model.size();i++) {
				if (list.isSelectedIndex(i)) {
					String line = model.get(i).toString();
					if (line.startsWith(DISABLED))
						line = line.substring(DISABLED.length());
					else
						line = DISABLED + line;
					model.set(i, line);
					updateEngineModel();
					break;
				}
			}
		}
    	   
       });
       builder.add(but3,          cc.xy(4,  1));
       
       builder.add(pane,          cc.xyw(2,  3, 4));
       
    //   
       
        return builder.getPanel();
	}
	
	public JComponent buildMEncoder() {
		FormLayout layout = new FormLayout(
				"left:pref, 2dlu, pref:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p , 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 20dlu, p, 3dlu, p, 3dlu, p");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator("Video encoder settings with following engines: Mencoder/AviSynth/FFmpeg",  cc.xyw(1, 1, 3));
        
        forcePCM = new JCheckBox("DTS/FLAC -> LPCM remux (You need an HDMI receiver for streaming PCM 5.1 ! Average Bitrate = 4.6Mbps)");
        forcePCM.setContentAreaFilled(false);
       /* if (!PMS.get().isWindows())
        	forcePCM.setEnabled(false);
        else {*/
	        if (PMS.get().isMencoder_usepcm())
	        	forcePCM.setSelected(true);
	        forcePCM.addItemListener(new ItemListener() {
	
				public void itemStateChanged(ItemEvent e) {
					PMS.get().setMencoder_usepcm(e.getStateChange() == ItemEvent.SELECTED);
				}
	        	
	        });
       // }
       
        builder.add(forcePCM, cc.xyw(1, 3, 3));
        
        abitrate = new JTextField("" + PMS.get().getAudiobitrate());
        abitrate.addKeyListener(new KeyListener() {

 		@Override
 		public void keyPressed(KeyEvent e) {}
 		@Override
 		public void keyTyped(KeyEvent e) {}
 		@Override
 		public void keyReleased(KeyEvent e) {
 			try {
 				int ab = Integer.parseInt(abitrate.getText());
 				PMS.get().setAudiobitrate(ab);
 			} catch (NumberFormatException nfe) {
 			}
 		}

        });
        
       builder.addLabel("AC3 Audio bitrate (in Kbits/s):", cc.xy(1, 5));
       builder.add(abitrate, cc.xy(3, 5));
       
       builder.addLabel("Maximum bandwidth in Mbits/s (0 means no limit):", cc.xy(1, 9));
       
       maxbitrate = new JTextField("" + PMS.get().getMaximumbitrate());
       maxbitrate.addKeyListener(new KeyListener() {

		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {
			PMS.get().setMaximumbitrate(maxbitrate.getText());
		}

       });
       builder.add(maxbitrate, cc.xy(3, 9));
      
       builder.addLabel("Mpeg2 Video quality settings (Some presets are available in the drop-down, you can also edit values, but be careful):", cc.xyw(1, 13, 3));
       
        Object data [] = new Object [] { PMS.get().getMencoderMainSettings(),
    		   "keyint=1:vqscale=1:vqmin=2  /* Best Quality */",
    		   "keyint=1:vqscale=1:vqmin=1  /* Lossless Quality, Crazy Bitrate */",
    		   "keyint=3:vqscale=2:vqmin=3  /* Good quality */",
    		   "keyint=5:vqscale=3:vqmin=5  /* Medium quality, Low-end CPU or HD Wifi Transcoding */"};
       MyComboBoxModel cbm = new MyComboBoxModel(data);
       
       vq = new JComboBox(cbm);
       vq.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String s = (String) e.getItem();
					if (s.indexOf("/*") > -1) {
						s = s.substring(0, s.indexOf("/*")).trim();
					}
					PMS.get().setMencoder_main(s);
				}
			}
       	
       });
       vq.setEditable(true);
       builder.add(vq,          cc.xyw(1,  15, 3));
       
      String help1 = "Encoder Tips:\n\nThe video is automatically transcoded and muxed to a MPEG-PS / AC3 audio (highly compatible on PS3)";
      help1 += "\nYou can play with the vqscale, vqmin and keyint parameters to achieve good, even almost lossless transcoding quality.";
      help1 += "\nDrawback to this is the VBR bitrate who can peaks sometimes above your max network capacity.";
      help1 += "\nThat's why you can also set the bandwith if you're on WiFi, CPL, etc. However, the transcoding quality";
     help1 += "\nis a balance between network speed and cpu power: the more quality you will put in a constrained bitrate,";
   help1 += 	"\nthe more your cpu will suffer! Also, don't expect to fit a 1080p action movie in the purest quality in 15Mbps :p";
   
      
       JTextArea decodeTips = new JTextArea(help1);
       decodeTips.setEditable(false);
       decodeTips.setBorder(BorderFactory.createEtchedBorder());
       decodeTips.setBackground(new Color(255, 255, 192));
       builder.add(decodeTips, cc.xyw(1, 17, 3));
       
       
        return builder.getPanel();
	}
	
	public JComponent buildMEncoderDec() {
		FormLayout layout = new FormLayout(
                "left:pref, 3dlu, p:grow, 3dlu, right:p:grow, 3dlu, p:grow, 3dlu, right:p:grow,3dlu, p:grow, 3dlu, right:p:grow,3dlu, pref:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, p , 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
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
       
       noskip = new JCheckBox("A/V sync correction <- You can disable it in #Video Settings#, just in case");
       noskip.setContentAreaFilled(false);
       if (PMS.get().isMencoder_nooutofsync())
    	   noskip.setSelected(true);
       noskip.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_nooutofsync(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       
       builder.add(noskip,          cc.xyw(1,  5, 15));
       
      
       
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
       builder.add(defaultsubs, cc.xyw(3, 17, 13));
       
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
    		   "cp1252  /* Occidental */",
    		   "cp1255  /* Hebrew */",
    		   "cp1256  /* Arabic */"};
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
       builder.add(subcp, cc.xy(3, 21));
       
       fribidi = new JCheckBox("FriBiDi mode");
       fribidi.setContentAreaFilled(false);
       if (PMS.get().isMencoder_subfribidi())
    	   fribidi.setSelected(true);
       fribidi.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_subfribidi(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       builder.add(fribidi, cc.xyw(5, 21, 10));
       
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
     
      fc = new JCheckBox("Fontconfig / Embedded fonts");
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
       
      
       disableSubs.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMencoder_disablesubs(e.getStateChange() == ItemEvent.SELECTED);
				
				subs.setEnabled(!PMS.get().isMencoder_disablesubs());
				defaultsubs.setEnabled(!PMS.get().isMencoder_disablesubs());
				subcp.setEnabled(!PMS.get().isMencoder_disablesubs());
				ass.setEnabled(!PMS.get().isMencoder_disablesubs());
				
				fribidi.setEnabled(!PMS.get().isMencoder_disablesubs());
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
	
	
	public JComponent buildFFmpeg() {
		FormLayout layout = new FormLayout(
                "left:pref, 0:grow",
                "p, 3dlu, p, 3dlu");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        
       
       builder.addSeparator("Video decoder settings for FFmpeg engine only (PREFER MENCODER)",  cc.xyw(2, 1, 1));
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
	
	public JComponent buildAviSynth() {
		FormLayout layout = new FormLayout(
                "left:pref, 0:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        
        builder.addSeparator("Video decoder settings for AviSynth engine only",  cc.xyw(2, 1, 1));
        
        convertfps = new JCheckBox("Enable AviSynth variable framerate change into a constant framerate (convertfps=true)");
        convertfps.setContentAreaFilled(false);
        if (PMS.get().isAvisynth_convertfps())
        	convertfps.setSelected(true);
        convertfps.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.get().setAvisynth_convertfps(e.getStateChange() == ItemEvent.SELECTED);
 			}
        	
        });
        builder.add(convertfps, cc.xy(2, 3));
        
        String clip = PMS.get().getAvisynth_script();
        if (clip == null)
        	clip = "";
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(clip, PMS.AVS_SEPARATOR);
        int i=0;
        while (st.hasMoreTokens()) {
        	if (i> 0)
        		sb.append("\n");
        	sb.append(st.nextToken());
        	i++;
        }
        textArea = new JTextArea(sb.toString());
        textArea.addKeyListener(new KeyListener() {

       		@Override
       		public void keyPressed(KeyEvent e) {}
       		@Override
       		public void keyTyped(KeyEvent e) {}
       		@Override
       		public void keyReleased(KeyEvent e) {
       			StringBuffer sb = new StringBuffer();
       			StringTokenizer st = new StringTokenizer(textArea.getText(), "\n");
       	        int i=0;
       	        while (st.hasMoreTokens()) {
       	        	if (i> 0)
       	        		sb.append(PMS.AVS_SEPARATOR);
       	        	sb.append(st.nextToken());
       	        	i++;
       	        }
       	        PMS.get().setAvisynth_script(sb.toString());
       		}
           	   
              });
        
       /* JTextField firstLine = new JTextField("clip=DirectShowSource(<mymovie>, <convertfps>)");
        firstLine.setEditable(false);
        builder.add(firstLine, cc.xy(2, 5));
        
        JTextField secondLine = new JTextField("clip=TextSub(clip, <mysubs>)");
        secondLine.setEditable(false);
        builder.add(secondLine, cc.xy(2, 7));*/
        
        JTextArea firstLine = new JTextArea("clip=DirectShowSource(<mymovie>, <convertfps>)\nclip=TextSub(clip, <mysubs>) <- depends on subtitles existence");
        firstLine.setEditable(false);
        builder.add(firstLine, cc.xy(2, 5));
        
        builder.addLabel("These 2 first lines are generated. You can now change the AviSynth script using the 'clip' variable.", cc.xy(2, 7));
        JScrollPane pane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setPreferredSize(new Dimension(500,250));
        builder.add(pane, cc.xy(2, 9));
        
        
        return builder.getPanel();
	}
	
	public JComponent buildTsMuxer() {
		FormLayout layout = new FormLayout(
                "left:pref, 0:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
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
        
        tsmuxerforcepcm = new JCheckBox("Force PCM remuxing with DTS/FLAC audio (TESTING, no guaranty whatsoever, working only with few TS/M2TS so far)");
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
        
        tsmuxerforceac3 = new JCheckBox("Force AC3 remuxing with all files (TESTING, no guaranty whatsoever, working only with few TS/M2TS so far)");
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
	
	public JComponent buildCommon() {
		FormLayout layout = new FormLayout(
				"left:pref, 2dlu, pref:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        
        builder.addSeparator("Video decoder settings, apply to following decoders: Mencoder/AviSynth/FFmpeg",  cc.xyw(1, 1, 3));
        
        channels = new JComboBox(new Object [] {"2 channels Stereo", "6 channels 5.1" /*, "8 channels 7.1" */ }); // 7.1 not supported by Mplayer :\
        channels.setEditable(false);
        if (PMS.get().getAudiochannels() == 2)
     	   channels.setSelectedIndex(0);
        else
     	   channels.setSelectedIndex(1);
        channels.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.get().setAudiochannels(Integer.parseInt(e.getItem().toString().substring(0, 1)));
 			}
        	
        });
        
        builder.addLabel("Number of audio channels:", cc.xy(1, 3));
        builder.add(channels, cc.xy(3, 3));
       
        disableSubs = new JCheckBox("Definitely disable subtitles");
        disableSubs.setContentAreaFilled(false);
      
        builder.add(disableSubs,          cc.xy(1,  5));
       
       
        return builder.getPanel();
	}
}
