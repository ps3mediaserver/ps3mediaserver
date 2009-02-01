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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import bsh.EvalError;
import bsh.Interpreter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaLang;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.newgui.FontFileFilter;
import net.pms.newgui.LooksFrame;
import net.pms.newgui.MyComboBoxModel;
import net.pms.util.CodecUtil;
import net.pms.util.FileUtil;
import net.pms.util.ProcessUtil;

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
	private JCheckBox  yadif ;
	private JCheckBox  scaler ;
	private JTextField scaleX;
	private JTextField scaleY;
	private JCheckBox  fc ;
	private JCheckBox  ass ;
	private JCheckBox  checkBox ;
	private JCheckBox  mencodermt ;
	private JCheckBox  ac3remux ;
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
	
	private final PmsConfiguration configuration;
	
	public MEncoderVideo(PmsConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout(
                "left:pref, 3dlu, p:grow, 3dlu, right:p:grow, 3dlu, p:grow, 3dlu, right:p:grow,3dlu, p:grow, 3dlu, right:p:grow,3dlu, pref:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu,p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, p , 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
         checkBox = new JCheckBox(Messages.getString("MEncoderVideo.0")); //$NON-NLS-1$
        checkBox.setContentAreaFilled(false);
        if (PMS.getConfiguration().getSkipLoopFilterEnabled())
        	checkBox.setSelected(true);
        checkBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setSkipLoopFilterEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
        	
        });
        
        JComponent cmp = builder.addSeparator(Messages.getString("MEncoderVideo.1"),  cc.xyw(1, 1, 15)); //$NON-NLS-1$
        cmp = (JComponent) cmp.getComponent(0);
        cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
        
        mencodermt = new JCheckBox(Messages.getString("MEncoderVideo.35")); //$NON-NLS-1$
        mencodermt.setFont(mencodermt.getFont().deriveFont(Font.BOLD));
        mencodermt.setContentAreaFilled(false);
        if (PMS.getConfiguration().getMencoderMT())
        	mencodermt.setSelected(true);
        mencodermt.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setMencoderMT(e.getStateChange() == ItemEvent.SELECTED);
				if (PMS.getConfiguration().getMencoderMT())
				JOptionPane.showMessageDialog(
						(JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
	                    Messages.getString("MEncoderVideo.31"), //$NON-NLS-1$
	                    "Information", //$NON-NLS-1$
	                    JOptionPane.INFORMATION_MESSAGE);
				
			}
        	
        });
        mencodermt.setEnabled(Platform.isWindows() || Platform.isMac());
        
        builder.add(mencodermt,          cc.xy(1,  3));
       builder.add(checkBox,          cc.xyw(3,  3, 12));
       
       noskip = new JCheckBox(Messages.getString("MEncoderVideo.2")); //$NON-NLS-1$
       noskip.setContentAreaFilled(false);
       if (PMS.getConfiguration().isMencoderNoOutOfSync())
    	   noskip.setSelected(true);
       noskip.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setMencoderNoOutOfSync((e.getStateChange() == ItemEvent.SELECTED));
				//intelligentsync.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       
       builder.add(noskip,          cc.xy(1,  5));
       
      
//       if (!PMS.get().isMencoder_nooutofsync())
//    	   intelligentsync.setEnabled(false);
      // builder.add(intelligentsync,          cc.xyw(3,  5, 7));
       
       JButton button = new JButton(Messages.getString("MEncoderVideo.29")); //$NON-NLS-1$
       button.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			JPanel codecPanel = new JPanel(new BorderLayout());
			final JTextArea textArea = new JTextArea();
			textArea.setText(PMS.getConfiguration().getCodecSpecificConfig());
			textArea.setFont(new Font("Courier", Font.PLAIN, 12)); //$NON-NLS-1$
			JScrollPane scrollPane = new JScrollPane(textArea);		
			scrollPane.setPreferredSize(new java.awt.Dimension(900, 100));
			
			final JTextArea textAreaDefault = new JTextArea();
			textAreaDefault.setText(DEFAULT_CODEC_CONF_SCRIPT);
			textAreaDefault.setBackground(Color.WHITE);
			textAreaDefault.setFont(new Font("Courier", Font.PLAIN, 12)); //$NON-NLS-1$
			textAreaDefault.setEditable(false);
			textAreaDefault.setEnabled(PMS.getConfiguration().isMencoderIntelligentSync());
			JScrollPane scrollPaneDefault = new JScrollPane(textAreaDefault);		
			scrollPaneDefault.setPreferredSize(new java.awt.Dimension(900, 450));
			
			JPanel customPanel = new JPanel(new BorderLayout());
			 intelligentsync = new JCheckBox(Messages.getString("MEncoderVideo.3")); //$NON-NLS-1$
		       intelligentsync.setContentAreaFilled(false);
		       if (PMS.getConfiguration().isMencoderIntelligentSync())
		    	   intelligentsync.setSelected(true);
		       intelligentsync.addItemListener(new ItemListener() {

					public void itemStateChanged(ItemEvent e) {
						PMS.getConfiguration().setMencoderIntelligentSync((e.getStateChange() == ItemEvent.SELECTED));
						textAreaDefault.setEnabled(PMS.getConfiguration().isMencoderIntelligentSync());
						
					}
		       	
		       });
		       
		       JLabel label = new JLabel(Messages.getString("MEncoderVideo.33")); //$NON-NLS-1$
		       customPanel.add(label, BorderLayout.NORTH);
		       customPanel.add(scrollPane, BorderLayout.SOUTH);
		       
			codecPanel.add(intelligentsync, BorderLayout.NORTH);
			codecPanel.add(scrollPaneDefault, BorderLayout.CENTER);
			codecPanel.add(customPanel, BorderLayout.SOUTH);
			while (JOptionPane.showOptionDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
					codecPanel, Messages.getString("MEncoderVideo.34"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) { //$NON-NLS-1$
				String newCodecparam = textArea.getText();
				DLNAMediaInfo fakemedia = new DLNAMediaInfo();
				fakemedia.codecA = "ac3"; //$NON-NLS-1$
				fakemedia.codecV = "mpeg4"; //$NON-NLS-1$
				fakemedia.container = "matroska"; //$NON-NLS-1$
				fakemedia.duration = "00:45:00"; //$NON-NLS-1$
				fakemedia.nrAudioChannels = 2;
				fakemedia.width = 1280;
				fakemedia.height = 720;
				fakemedia.sampleFrequency = "48000"; //$NON-NLS-1$
				fakemedia.frameRate = "23.976"; //$NON-NLS-1$
				String result [] = getSpecificCodecOptions(newCodecparam, fakemedia, "dummy.mpg", "dummy.srt", false, true); //$NON-NLS-1$ //$NON-NLS-2$
				if (result.length > 0 && result[0].startsWith("@@")) { //$NON-NLS-1$
					String errorMessage = result[0].substring(2);
					JOptionPane.showMessageDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())), errorMessage);
					
				} else {
					PMS.getConfiguration().setCodecSpecificConfig(newCodecparam);
					break;
				}
			}
			
		}
    	   
       });
       builder.add(button,          cc.xyw(1,  11, 2));
       
       forcefps = new JCheckBox(Messages.getString("MEncoderVideo.4")); //$NON-NLS-1$
       forcefps.setContentAreaFilled(false);
       if (configuration.isMencoderForceFps())
    	   forcefps.setSelected(true);
       forcefps.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderForceFps(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       
       builder.add(forcefps,          cc.xyw(1,  7, 2));
       
       yadif = new JCheckBox(Messages.getString("MEncoderVideo.26")); //$NON-NLS-1$
       yadif.setContentAreaFilled(false);
       if (configuration.isMencoderYadif())
    	   yadif.setSelected(true);
       yadif.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderYadif(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       
       builder.add(yadif,          cc.xyw(3,  7, 7));
       
       scaler = new JCheckBox(Messages.getString("MEncoderVideo.27")); //$NON-NLS-1$
       scaler.setContentAreaFilled(false);
       scaler.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderScaler(e.getStateChange() == ItemEvent.SELECTED);
				scaleX.setEnabled(configuration.isMencoderScaler());
				scaleY.setEnabled(configuration.isMencoderScaler());
			}
       	
       });
       
       builder.add(scaler,          cc.xyw(3,  5,7));
       
       builder.addLabel(Messages.getString("MEncoderVideo.28"), cc.xyw(10, 5, 3, CellConstraints.RIGHT, CellConstraints.CENTER)); //$NON-NLS-1$
       scaleX = new JTextField("" + configuration.getMencoderScaleX()); //$NON-NLS-1$
       scaleX.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			try {
   				configuration.setMencoderScaleX(Integer.parseInt(scaleX.getText()));
   			} catch (NumberFormatException nfe) {}
   		}
       	   
          });
       builder.add(scaleX, cc.xyw(13, 5, 3));
       
       builder.addLabel(Messages.getString("MEncoderVideo.30"), cc.xyw(10, 7, 3, CellConstraints.RIGHT, CellConstraints.CENTER)); //$NON-NLS-1$
       scaleY = new JTextField("" + configuration.getMencoderScaleY()); //$NON-NLS-1$
       scaleY.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			try {
   				configuration.setMencoderScaleY(Integer.parseInt(scaleY.getText()));
   			} catch (NumberFormatException nfe) {}
   		}
       	   
          });
       builder.add(scaleY, cc.xyw(13, 7, 3));
       
       if (configuration.isMencoderScaler())
    	   scaler.setSelected(true);
       else {
    	   scaleX.setEnabled(false);
    	   scaleY.setEnabled(false);
       }
       
       ac3remux = new JCheckBox(Messages.getString("MEncoderVideo.32")); //$NON-NLS-1$
       ac3remux.setContentAreaFilled(false);
       if (PMS.getConfiguration().isMencoderRemuxAC3())
    	   ac3remux.setSelected(true);
       ac3remux.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setMencoderRemuxAC3((e.getStateChange() == ItemEvent.SELECTED));
			}
       	
       });
       
       builder.add(ac3remux, cc.xyw(1, 9,15));
  
       cmp = builder.addSeparator(Messages.getString("MEncoderVideo.5"), cc.xyw(1, 19, 15)); //$NON-NLS-1$
       cmp = (JComponent) cmp.getComponent(0);
       cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
       
       builder.addLabel(Messages.getString("MEncoderVideo.6"), cc.xy(1, 21)); //$NON-NLS-1$
       decode = new JTextField(configuration.getMencoderDecode());
       decode.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderDecode(decode.getText());
   		}
       	   
          });
       builder.add(decode, cc.xyw(3, 21, 13));
       
      
       
       builder.addLabel(Messages.getString("MEncoderVideo.7"), cc.xyw(1, 23, 15)); //$NON-NLS-1$
       langs = new JTextField(configuration.getMencoderAudioLanguages());
       langs.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderAudioLanguages(langs.getText());
   		}
       	   
          });
       builder.add(langs, cc.xyw(3, 23, 8));
       
       cmp = builder.addSeparator(Messages.getString("MEncoderVideo.8"),  cc.xyw(1, 25, 15)); //$NON-NLS-1$
       cmp = (JComponent) cmp.getComponent(0);
       cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
        
     
       
       builder.addLabel(Messages.getString("MEncoderVideo.9"), cc.xy(1, 27)); //$NON-NLS-1$
       defaultsubs = new JTextField(configuration.getMencoderSubLanguages());
       defaultsubs.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderSubLanguages(defaultsubs.getText());
   		}
       	   
          });
       builder.add(defaultsubs, cc.xyw(3, 27, 8));
       
       builder.addLabel(Messages.getString("MEncoderVideo.10"), cc.xy(1, 29)); //$NON-NLS-1$
       defaultaudiosubs = new JTextField(configuration.getMencoderAudioSubLanguages());
       defaultaudiosubs.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderAudioSubLanguages(defaultaudiosubs.getText());
   		}
       	   
          });
       builder.add(defaultaudiosubs, cc.xyw(3, 29, 8));
       
       builder.addLabel(Messages.getString("MEncoderVideo.11"), cc.xy(1,31)); //$NON-NLS-1$
       /*subcp = new JTextField(configuration.getMencoderSubCp());
       subcp.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoder_subcp(subcp.getText());
   		}
       	   
          });*/
       Object data [] = new Object [] { configuration.getMencoderSubCp(),
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
					configuration.setMencoderSubCp(s);
				}
			}
       	
       });
       subcp.setEditable(true);
       builder.add(subcp, cc.xyw(3, 31,7));
       
       fribidi = new JCheckBox("FriBiDi mode"); //$NON-NLS-1$
       fribidi.setContentAreaFilled(false);
       if (configuration.isMencoderSubFribidi())
    	   fribidi.setSelected(true);
       fribidi.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderSubFribidi(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       });
       builder.add(fribidi, cc.xyw(11, 31, 4));
       
       builder.addLabel(Messages.getString("MEncoderVideo.24"), cc.xy(1, 33)); //$NON-NLS-1$
       defaultfont = new JTextField(configuration.getMencoderFont());
       defaultfont.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderFont(defaultfont.getText());
   		}
       	   
          });
       builder.add(defaultfont, cc.xyw(3, 33, 8));
       
       JButton fontselect = new JButton("..."); //$NON-NLS-1$
       fontselect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FontFileFilter());
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("MEncoderVideo.25")); //$NON-NLS-1$
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	defaultfont.setText(chooser.getSelectedFile().getAbsolutePath());
			    	configuration.setMencoderFont(chooser.getSelectedFile().getAbsolutePath());
			    }
			}
 		  
 	  });
 	  builder.add(fontselect,          cc.xyw(11,  33, 2));
       
       builder.addLabel(Messages.getString("MEncoderVideo.12"), cc.xy(1, 37, CellConstraints.RIGHT, CellConstraints.CENTER)); //$NON-NLS-1$
       mencoder_ass_scale = new JTextField(configuration.getMencoderAssScale());
       mencoder_ass_scale.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderAssScale(mencoder_ass_scale.getText());
   		}
       	   
          });
       
       builder.addLabel(Messages.getString("MEncoderVideo.13"), cc.xy(5, 37)); //$NON-NLS-1$
       mencoder_ass_outline = new JTextField(configuration.getMencoderAssOutline());
       mencoder_ass_outline.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderAssOutline(mencoder_ass_outline.getText());
   		}
       	   
          });
       
       builder.addLabel(Messages.getString("MEncoderVideo.14"), cc.xy(9,37)); //$NON-NLS-1$
       mencoder_ass_shadow = new JTextField(configuration.getMencoderAssShadow());
       mencoder_ass_shadow.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderAssShadow(mencoder_ass_shadow.getText());
   		}
       	   
          });
       
       builder.addLabel(Messages.getString("MEncoderVideo.15"), cc.xy(13, 37)); //$NON-NLS-1$
       mencoder_ass_margin = new JTextField(configuration.getMencoderAssMargin());
       mencoder_ass_margin.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderAssMargin(mencoder_ass_margin.getText());
   		}
       	   
          });
 builder.add(mencoder_ass_scale, cc.xy(3, 37));
       
       builder.add(mencoder_ass_outline, cc.xy(7, 37));
       
       builder.add(mencoder_ass_shadow, cc.xy(11, 37));
      
       builder.add(mencoder_ass_margin, cc.xy(15, 37));
       
       
       builder.addLabel(Messages.getString("MEncoderVideo.16"), cc.xy(1, 39, CellConstraints.RIGHT, CellConstraints.CENTER)); //$NON-NLS-1$
       mencoder_noass_scale = new JTextField(configuration.getMencoderNoAssScale());
       mencoder_noass_scale.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderNoAssScale(mencoder_noass_scale.getText());
   		}
       	   
          });
       
       builder.addLabel(Messages.getString("MEncoderVideo.17"), cc.xy(5, 39)); //$NON-NLS-1$
       mencoder_noass_outline = new JTextField(configuration.getMencoderNoAssOutline());
       mencoder_noass_outline.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderNoAssOutline(mencoder_noass_outline.getText());
   		}
       	   
          });
       
       builder.addLabel(Messages.getString("MEncoderVideo.18"), cc.xy(9, 39)); //$NON-NLS-1$
       mencoder_noass_blur = new JTextField(configuration.getMencoderNoAssBlur());
       mencoder_noass_blur.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderNoAssBlur(mencoder_noass_blur.getText());
   		}
       	   
          });
       
       builder.addLabel(Messages.getString("MEncoderVideo.19"), cc.xy(13, 39)); //$NON-NLS-1$
       mencoder_noass_subpos = new JTextField(configuration.getMencoderNoAssSubPos());
       mencoder_noass_subpos.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setMencoderNoAssSubPos(mencoder_noass_subpos.getText());
   		}
       	   
          });
 builder.add(mencoder_noass_scale, cc.xy(3, 39));
       
       builder.add(mencoder_noass_outline, cc.xy(7, 39));
       
       builder.add(mencoder_noass_blur, cc.xy(11, 39));
      
       builder.add(mencoder_noass_subpos, cc.xy(15,39));
       
       
       ass = new JCheckBox(Messages.getString("MEncoderVideo.20")); //$NON-NLS-1$
       ass.setContentAreaFilled(false);
       ass.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e != null) {
					configuration.setMencoderAss(e.getStateChange() == ItemEvent.SELECTED);
				}
				
				mencoder_ass_scale.setEnabled(configuration.isMencoderAss());
				mencoder_ass_outline.setEnabled(configuration.isMencoderAss());
				mencoder_ass_shadow.setEnabled(configuration.isMencoderAss());
				mencoder_ass_margin.setEnabled(configuration.isMencoderAss());
				mencoder_noass_scale.setEnabled(!configuration.isMencoderAss());
				mencoder_noass_outline.setEnabled(!configuration.isMencoderAss());
				mencoder_noass_blur.setEnabled(!configuration.isMencoderAss());
				mencoder_noass_subpos.setEnabled(!configuration.isMencoderAss());
			}
       	
       });
       
       builder.add(ass,          cc.xy(1, 35));
       ass.setSelected(configuration.isMencoderAss());
      ass.getItemListeners()[0].itemStateChanged(null);
     
      fc = new JCheckBox(Messages.getString("MEncoderVideo.21")); //$NON-NLS-1$
      fc.setContentAreaFilled(false);
      fc.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderFontConfig(e.getStateChange() == ItemEvent.SELECTED);
			}
      	
      });
      
      builder.add(fc,          cc.xyw(3,  35, 12));
      fc.setSelected(configuration.isMencoderFontConfig());
       
       subs = new JCheckBox(Messages.getString("MEncoderVideo.22")); //$NON-NLS-1$
       subs.setContentAreaFilled(false);
       if (PMS.getConfiguration().getUseSubtitles())
    	   subs.setSelected(true);
       subs.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setUseSubtitles((e.getStateChange() == ItemEvent.SELECTED));
			}
       	
       });
       builder.add(subs, cc.xyw(1, 41, 15));
       
       JTextArea decodeTips = new JTextArea(Messages.getString("MEncoderVideo.23")); //$NON-NLS-1$
       decodeTips.setEditable(false);
       decodeTips.setBorder(BorderFactory.createEtchedBorder());
       decodeTips.setBackground(new Color(255, 255, 192));
       builder.add(decodeTips, cc.xyw(1, 43, 15));
       
      
      JCheckBox disableSubs = ((LooksFrame) PMS.get().getFrame()).getTr().getDisableSubs();
      disableSubs.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderDisableSubs(e.getStateChange() == ItemEvent.SELECTED);
				
				subs.setEnabled(!configuration.isMencoderDisableSubs());
				defaultsubs.setEnabled(!configuration.isMencoderDisableSubs());
				subcp.setEnabled(!configuration.isMencoderDisableSubs());
				ass.setEnabled(!configuration.isMencoderDisableSubs());
				
				fribidi.setEnabled(!configuration.isMencoderDisableSubs());
				fc.setEnabled(!configuration.isMencoderDisableSubs());
				mencoder_ass_scale.setEnabled(!configuration.isMencoderDisableSubs());
				mencoder_ass_outline.setEnabled(!configuration.isMencoderDisableSubs());
				mencoder_ass_shadow.setEnabled(!configuration.isMencoderDisableSubs());
				mencoder_ass_margin.setEnabled(!configuration.isMencoderDisableSubs());
				mencoder_noass_scale.setEnabled(!configuration.isMencoderDisableSubs());
				mencoder_noass_outline.setEnabled(!configuration.isMencoderDisableSubs());
				mencoder_noass_blur.setEnabled(!configuration.isMencoderDisableSubs());
				mencoder_noass_subpos.setEnabled(!configuration.isMencoderDisableSubs());
				
				if (!configuration.isMencoderDisableSubs())
				 ass.getItemListeners()[0].itemStateChanged(null);
			}
      	
      });
     if (configuration.isMencoderDisableSubs())
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
	protected boolean oaccopy;
	protected boolean avi;

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
	
	
	protected String [] getDefaultArgs() {
		return new String [] { "-quiet", "-oac", oaccopy?"copy":(pcm?"pcm":"lavc"), "-of", (pcm||ac3||avi)?"avi":"mpeg", "-mpegopts", "format=mpeg2:muxrate=500000:vbuf_size=1194:abuf_size=64", "-ovc", ovccopy?"copy":"lavc" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$
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
		return configuration.getMencoderPath();
	}
	
	private String addMaximumBitrateConstraints(String encodeSettings, DLNAMediaInfo media, String quality) {
		String m = "" + PMS.getConfiguration().getMaximumBitrate(); //$NON-NLS-1$
		int bufs = 0;
		if (m.contains("(") && m.contains(")")) { //$NON-NLS-1$ //$NON-NLS-2$
			bufs = Integer.parseInt(m.substring(m.indexOf("(")+1, m.indexOf(")"))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (m.contains("(")) //$NON-NLS-1$
			m = m.substring(0, m.indexOf("(")).trim(); //$NON-NLS-1$
		if (StringUtils.isBlank(m))
			m = "0"; //$NON-NLS-1$
		
		int mb = Integer.parseInt(m);
		if (mb > 0 && !quality.contains("vrc_buf_size") && !quality.contains("vrc_maxrate") && !quality.contains("vbitrate")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		return encodeSettings;
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media, OutputParams params)
			throws IOException {
		
		String vcodec = "mpeg2video"; //$NON-NLS-1$
		/*
		avi = false;
		if (params.mediaRenderer ==HTTPResource.XBOX) {
			avi = true;
			vcodec = "mpeg4:vbitrate=20000000"; // http://wiki.megaframe.org/wiki/Ubuntu_XBOX_360#MEncoder
		}
		*/
		oaccopy = false;
		if (configuration.isMencoderRemuxAC3()) {
			if (media != null && media.codecA != null && media.codecA.equals("ac3") && media.audioCodes != null && media.audioCodes.size() <= 1 && !avisynth()) //$NON-NLS-1$
				oaccopy = true;
			
			if (params.aid > -1 && media != null && media.audioCodes != null && media.audioCodes.size() > 0) {
				for(DLNAMediaLang lang:media.audioCodes) {
					if (lang.id == params.aid && lang.format != null && lang.format.equals("ac3")) //$NON-NLS-1$
						oaccopy = true;
				}
			}
		}
		dvd = false;
		if (media != null && media.dvdtrack > 0)
			dvd = true;
		
		boolean lossless = false; // really need to revamp the media audio code
		if (configuration.isMencoderUsePcm() && params.aid > -1 && media != null && media.audioCodes != null && media.audioCodes.size() > 0) {
			for(DLNAMediaLang lang:media.audioCodes) {
				if (lang.id == params.aid && lang.format != null && media.isLossless(lang.format)) //$NON-NLS-1$
					lossless = true;
			}
		}
		
		if (lossless || (media.losslessaudio && configuration.isMencoderUsePcm()) || (configuration.isTsmuxerPreremuxPcm() && params.losslessaudio)) { //$NON-NLS-1$
			pcm = true;
			ac3 = false;
			params.losslessaudio = true;
			params.forceFps = media.getValidFps(false);
			if (params.no_videoencode)
				ovccopy = true;
		} else if (configuration.isTsmuxerPreremuxAc3() && params.lossyaudio) {
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
		if (configuration.getMencoderDecode() == null || configuration.getMencoderDecode().indexOf("-lavdopts") == -1) { //$NON-NLS-1$
			add = " -lavdopts debug=0"; //$NON-NLS-1$
		}
		
		String alternativeCodec = "";//"-ac ffac3,ffdca, ";  //$NON-NLS-1$
		if (dvd)
			alternativeCodec = ""; //$NON-NLS-1$
		StringTokenizer st = new StringTokenizer(alternativeCodec + "-channels " + PMS.getConfiguration().getAudioChannelCount() + " " + configuration.getMencoderDecode() + add, " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		overridenMainArgs = new String [st.countTokens()];
		int i = 0;
		boolean next = false;
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			if (next) {
				int nbcores = PMS.getConfiguration().getNumberOfCpuCores();
				if (dvd || fileName.toLowerCase().endsWith("dvr-ms")) //$NON-NLS-1$
					nbcores = 1;
				token += ":threads=" + nbcores; //$NON-NLS-1$
				if (PMS.getConfiguration().getSkipLoopFilterEnabled() && !avisynth())
					token += ":skiploopfilter=all"; //$NON-NLS-1$
				next = false;
			} 
			if (token.toLowerCase().contains("lavdopts")) { //$NON-NLS-1$
				next = true;
			}
			
			overridenMainArgs[i++] = token;
		}
		//}
		if (PMS.getConfiguration().getMencoderMainSettings() != null) {
			String encodeSettings = "-lavcopts autoaspect=1:vcodec=" + vcodec + ":acodec=ac3:abitrate=" + PMS.getConfiguration().getAudioBitrate() + ":threads=" + PMS.getConfiguration().getNumberOfCpuCores() + ":" + PMS.getConfiguration().getMencoderMainSettings(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			encodeSettings = addMaximumBitrateConstraints(encodeSettings, media, PMS.getConfiguration().getMencoderMainSettings());
			st = new StringTokenizer(encodeSettings, " "); //$NON-NLS-1$
			int oldc = overridenMainArgs.length;
			overridenMainArgs = Arrays.copyOf(overridenMainArgs, overridenMainArgs.length + st.countTokens());
			i = oldc;
			while (st.hasMoreTokens()) {
				overridenMainArgs[i++] = st.nextToken();
			}
		}

		
		StringBuffer sb = new StringBuffer();
		if (!configuration.isMencoderDisableSubs() && configuration.isMencoderAss() && !dvd && !avisynth())
			sb.append("-ass -" + (configuration.isMencoderFontConfig()?"":"no") + "fontconfig "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (!configuration.isMencoderDisableSubs() && configuration.getMencoderSubCp() != null && configuration.getMencoderSubCp().length() >  0) {
			sb.append("-subcp " +configuration.getMencoderSubCp() + " "); //$NON-NLS-1$ //$NON-NLS-2$
			if (configuration.isMencoderSubFribidi()) {
				sb.append("-fribidi-charset " +configuration.getMencoderSubCp() + " "); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (configuration.getMencoderAudioLanguages() != null && configuration.getMencoderAudioLanguages().length() >  0)
			sb.append("-alang " +configuration.getMencoderAudioLanguages() + " "); //$NON-NLS-1$ //$NON-NLS-2$
		if (!configuration.isMencoderDisableSubs() && !avisynth()) {
			if (configuration.getMencoderFont() != null && configuration.getMencoderFont().length() > 0) {
				sb.append("-subfont " + configuration.getMencoderFont() + " "); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				String font = CodecUtil.getDefaultFontPath();
				if (StringUtils.isNotBlank(font))
					sb.append("-subfont " + font + " "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (configuration.isMencoderAss()) {
				sb.append("-ass-color ffffff00 -ass-border-color 00000000 -ass-font-scale " + configuration.getMencoderAssScale()); //$NON-NLS-1$
				sb.append(" -ass-force-style " + (configuration.isMencoderFontConfig()?"FontName=Arial,":"") + "Outline=" + configuration.getMencoderAssOutline() + ",Shadow=" + configuration.getMencoderAssShadow() + ",MarginV=" + configuration.getMencoderAssMargin() + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			} else {
				sb.append("-subfont-text-scale " + configuration.getMencoderNoAssScale()); //$NON-NLS-1$
				sb.append(" -subfont-outline " + configuration.getMencoderNoAssOutline()); //$NON-NLS-1$
				sb.append(" -subfont-blur " + configuration.getMencoderNoAssBlur()); //$NON-NLS-1$
				int subpos = 1;
				try {
					subpos = Integer.parseInt(configuration.getMencoderNoAssSubPos());
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
			if ((!configuration.isMencoderAss() || dvd) && s.contains("-ass")) { //$NON-NLS-1$
				s = "-quiet"; //$NON-NLS-1$
				
				next = true;
			}
			overridenMainArgs[i++] = s;
		}
		
		sb = new StringBuffer();
		if (!configuration.isMencoderDisableSubs() && configuration.getMencoderSubLanguages() != null && configuration.getMencoderSubLanguages().length() >  0) {
			sb.append("-slang " +configuration.getMencoderSubLanguages() + " "); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			/*int maxid = 1000;
			if (media != null && media.maxsubid > 0)
				maxid = media.maxsubid+1;*/
			//sb.append("-sid " + maxid + " "); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("-subdelay 20000"); //$NON-NLS-1$
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
		
		PipeProcess pipe = new PipeProcess("mencoder" + System.currentTimeMillis(), (pcm || ac3)?null:params); //$NON-NLS-1$
		params.input_pipes [0] = pipe;
		
		boolean vobsub = false;
		String subString = null;
		if (!avisynth && PMS.getConfiguration().getUseSubtitles()) {
			File srtFile = FileUtil.isFileExists(fileName, "srt"); //$NON-NLS-1$
			if (srtFile != null) {
				subString=srtFile.getAbsolutePath();
			}
			File assFile = FileUtil.isFileExists(fileName, "ass"); //$NON-NLS-1$
			if (assFile != null) {
				subString=assFile.getAbsolutePath();
			}
			File subFile = FileUtil.isFileExists(fileName, "sub"); //$NON-NLS-1$
			if (subFile != null) {
				subString=subFile.getAbsolutePath();
			}
			File smiFile = FileUtil.isFileExists(fileName, "smi"); //$NON-NLS-1$
			if (smiFile != null) {
				subString=smiFile.getAbsolutePath();
			}
			File idxFile = FileUtil.isFileExists(fileName, "idx"); //$NON-NLS-1$
			if (idxFile != null) {
				vobsub = true;
			}
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
			cmdArray[4] = ProcessUtil.getShortFileNameIfWideChars(avsFile.getAbsolutePath());
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
		
		boolean nosubfound = false;
		
		cmdArray[cmdArray.length-12] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length-11] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length-10] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length-9] = "-quiet"; //$NON-NLS-1$
		if (configuration.getUseSubtitles()) {
			if (params.aid > -1 || params.sid > -1) {
				if (params.aid > -1) {
					cmdArray[cmdArray.length-12] = "-aid"; //$NON-NLS-1$
					cmdArray[cmdArray.length-11] = "" + params.aid; //$NON-NLS-1$
				}
				cmdArray[cmdArray.length-10] = "-sid"; //$NON-NLS-1$
				if (params.sid > -1) {
					cmdArray[cmdArray.length-9] = "" + params.sid; //$NON-NLS-1$
				} else {
					/*int maxid = 1000;
					if (media != null)
						maxid = media.maxsubid+1;*/
					//cmdArray[cmdArray.length-9] = maxid + ""; //$NON-NLS-1$
					cmdArray[cmdArray.length-9] = "20000"; //$NON-NLS-1$
					cmdArray[cmdArray.length-10] = "-subdelay"; //$NON-NLS-1$
				}
			} else if (subString == null) {
				nosubfound = true;
				for(i=0;i<defaultSubArgs.length;i++) {
					if (i < 2)
						cmdArray[cmdArray.length-12+i] = defaultSubArgs[i];
				}
			} else if (subString != null && !avisynth()) {
				cmdArray[cmdArray.length-10] = "-sid"; //$NON-NLS-1$
				int maxid = 100;
				/*if (media != null)
					maxid = media.maxsubid+1;*/
				cmdArray[cmdArray.length-9] = ""+ maxid; //$NON-NLS-1$
			}
			if (params.aid == -1 && params.sid == -1 && subString == null) {
				int as [] = media.getAudioSubLangIds();
				if (media != null && as != null) {
					cmdArray[cmdArray.length-12] = "-aid"; //$NON-NLS-1$
					cmdArray[cmdArray.length-11] = "" + as[0]; //$NON-NLS-1$
					cmdArray[cmdArray.length-10] = "-sid"; //$NON-NLS-1$
					cmdArray[cmdArray.length-9] = "" + as[1]; //$NON-NLS-1$
				}
			}
		} else {
			//cmdArray[cmdArray.length-10] = "-sid"; //$NON-NLS-1$
			//int maxid = 100;
			/*if (media != null)
				maxid = media.maxsubid+1;*/
			//cmdArray[cmdArray.length-9] = ""+ maxid; //$NON-NLS-1$
			cmdArray[cmdArray.length-9] = "20000"; //$NON-NLS-1$
			cmdArray[cmdArray.length-10] = "-subdelay"; //$NON-NLS-1$
		}
		
		// disable ass if there are no subs (not safe ?)
		if (nosubfound && media != null && (media.subtitlesCodes == null || media.subtitlesCodes.size() == 0)) {
			for(int c=0;c<cmdArray.length;c++) {
				if (cmdArray[c] != null && cmdArray[c].equals("-ass")) //$NON-NLS-1$
					cmdArray[c] = "-quiet"; //$NON-NLS-1$
			}
		}
		
		cmdArray[cmdArray.length-8] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length-7] = "-quiet"; //$NON-NLS-1$
		
		if (configuration.isMencoderForceFps()) {
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
			if (configuration.isMencoderForceFps())
				cmdArray[cmdArray.length-7] = cmdArray[cmdArray.length-5];
		}
		/*if (media != null && media.dvdtrack > 0) {
			cmdArray[cmdArray.length-8] = "-quiet";
			cmdArray[cmdArray.length-7] = "-quiet";
			cmdArray[cmdArray.length-6] = "-quiet";
			cmdArray[cmdArray.length-5] = "-quiet";
		}*/
		if (subString != null && !configuration.isMencoderDisableSubs()) {
			if (vobsub) {
				// vobsub not supported in MEncoder :\
				//cmdArray[cmdArray.length-4] = "-vobsub";
				//cmdArray[cmdArray.length-3] = subString.substring(0, subString.length()-4);
				cmdArray[cmdArray.length-4] = "-quiet"; //$NON-NLS-1$
				cmdArray[cmdArray.length-3] = "-quiet"; //$NON-NLS-1$
			} else {
				cmdArray[cmdArray.length-4] = "-sub"; //$NON-NLS-1$
				cmdArray[cmdArray.length-3] = ProcessUtil.getShortFileNameIfWideChars(subString).replace(",", "\\,"); // comas in mencoder separates multiple subtitles files //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			cmdArray[cmdArray.length-4] = "-quiet"; //$NON-NLS-1$
			cmdArray[cmdArray.length-3] = "-quiet"; //$NON-NLS-1$
		}
		
		if (fileName.toLowerCase().endsWith("evo")) { //$NON-NLS-1$
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +2);
			cmdArray[cmdArray.length-4] = "-psprobe"; //$NON-NLS-1$
			cmdArray[cmdArray.length-3] = "10000"; //$NON-NLS-1$
		}
		
		
		/*if (fileName.toLowerCase().endsWith("flv") || fileName.toLowerCase().endsWith("mov") || fileName.toLowerCase().endsWith("evo") || fileName.toLowerCase().endsWith("rmvb") || fileName.toLowerCase().endsWith("rm")) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +2);
			cmdArray[cmdArray.length-4] = "-vf"; //$NON-NLS-1$
			cmdArray[cmdArray.length-3] = "fixpts"; //$NON-NLS-1$
		}*/
		
		boolean deinterlace = configuration.isMencoderYadif();
		boolean scaler = configuration.isMencoderScaler() && (configuration.getMencoderScaleX() != 0 || configuration.getMencoderScaleY() != 0);
		if ((deinterlace || scaler) && !avisynth()) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +2);
			cmdArray[cmdArray.length-4] = "-vf"; //$NON-NLS-1$
			String scalerString = "scale=" + configuration.getMencoderScaleX() + ":" + configuration.getMencoderScaleY(); //$NON-NLS-1$ //$NON-NLS-2$
			if (deinterlace)
				scalerString = "," + scalerString; //$NON-NLS-1$
			cmdArray[cmdArray.length-3] = (deinterlace?"yadif":"") + (scaler?scalerString:""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		/*
		if (params.mediaRenderer == HTTPResource.XBOX) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +4);
			cmdArray[cmdArray.length-6] = "-forceidx"; //$NON-NLS-1$
			cmdArray[cmdArray.length-5] = "-noodml"; //$NON-NLS-1$
			cmdArray[cmdArray.length-4] = "-ffourcc"; //$NON-NLS-1$
			cmdArray[cmdArray.length-3] = "DX50"; //$NON-NLS-1$
		}
		*/
		if (configuration.getMencoderMT()) {
			if (setCmdToMencoderMT(configuration, cmdArray, media)) {
				cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +2);
				cmdArray[cmdArray.length-4] = "-lavdopts"; //$NON-NLS-1$
				cmdArray[cmdArray.length-3] = "fast"; //$NON-NLS-1$
			}
		}
		
		boolean noMC0NoSkip = false;
		if (media != null) {
			String sArgs [] = getSpecificCodecOptions(PMS.getConfiguration().getCodecSpecificConfig(), media, fileName, subString, PMS.getConfiguration().isMencoderIntelligentSync(), false);
			if (sArgs !=null && sArgs.length > 0) {
				boolean vfConsumed = false;
				boolean afConsumed = false;
				for(int s=0;s<sArgs.length;s++) {
					if (sArgs[s].equals("-noass")) { //$NON-NLS-1$
						for(int c=0;c<cmdArray.length;c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-ass")) //$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
						}
					} else if (sArgs[s].equals("-ofps")) { //$NON-NLS-1$
						for(int c=0;c<cmdArray.length;c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-ofps")) {//$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
								cmdArray[c+1] = "-quiet"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-fps")) { //$NON-NLS-1$
						for(int c=0;c<cmdArray.length;c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-fps")) {//$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
								cmdArray[c+1] = "-quiet"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-ovc")) { //$NON-NLS-1$
						for(int c=0;c<cmdArray.length;c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-ovc")) {//$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
								cmdArray[c+1] = "-quiet"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-channels")) { //$NON-NLS-1$
						for(int c=0;c<cmdArray.length;c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-channels")) {//$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
								cmdArray[c+1] = "-quiet"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-oac")) { //$NON-NLS-1$
						for(int c=0;c<cmdArray.length;c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-oac")) {//$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
								cmdArray[c+1] = "-quiet"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-quality")) { //$NON-NLS-1$
						for(int c=0;c<cmdArray.length;c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-lavcopts")) {//$NON-NLS-1$
								cmdArray[c+1] = "autoaspect=1:vcodec=" + vcodec + ":acodec=ac3:abitrate=" + PMS.getConfiguration().getAudioBitrate() + ":threads=" + PMS.getConfiguration().getNumberOfCpuCores() + ":" + sArgs[s+1]; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								addMaximumBitrateConstraints(cmdArray[c+1], media, cmdArray[c+1]);
								sArgs[s+1] = "-quality"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-mpegopts")) { //$NON-NLS-1$
						for(int c=0;c<cmdArray.length;c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-mpegopts")) {//$NON-NLS-1$
								cmdArray[c+1] += ":" + sArgs[s+1]; //$NON-NLS-1$
								sArgs[s+1] = "-mpegopts"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-vf")) { //$NON-NLS-1$
						for(int c=0;c<cmdArray.length;c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-vf")) {//$NON-NLS-1$
								cmdArray[c+1] += "," + sArgs[s+1]; //$NON-NLS-1$
								sArgs[s+1] = "-vf"; //$NON-NLS-1$
								s++;
								vfConsumed = true;
							}
						}
					} else if (sArgs[s].equals("-af")) { //$NON-NLS-1$
						for(int c=0;c<cmdArray.length;c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-af")) {//$NON-NLS-1$
								cmdArray[c+1] += "," + sArgs[s+1]; //$NON-NLS-1$
								sArgs[s+1] = "-af"; //$NON-NLS-1$
								s++;
								afConsumed = true;
							}
						}
					} else if (sArgs[s].equals("-nosync")) { //$NON-NLS-1$
						noMC0NoSkip = true;
					} else if (sArgs[s].equals("-mc")) { //$NON-NLS-1$
						noMC0NoSkip = true;
					} else if (sArgs[s].equals("-mt") && !avisynth) { //$NON-NLS-1$
						setCmdToMencoderMT(configuration, cmdArray, null);
					}
				}
				cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +sArgs.length);
				for(int s=0;s<sArgs.length;s++) {
					if (sArgs[s].equals("-noass") || sArgs[s].equals("-mpegopts") || (sArgs[s].equals("-vf") & vfConsumed) || (sArgs[s].equals("-af") && afConsumed) || sArgs[s].equals("-quality") || sArgs[s].equals("-nosync") || sArgs[s].equals("-mt")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
						cmdArray[cmdArray.length-sArgs.length-2+s] = "-quiet"; //$NON-NLS-1$
					} else
						cmdArray[cmdArray.length-sArgs.length-2+s] = sArgs[s];
				}
			}
		}
		
		if ((pcm || ac3) || (PMS.getConfiguration().isMencoderNoOutOfSync() && !noMC0NoSkip)) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +3);
			cmdArray[cmdArray.length-5] = "-mc"; //$NON-NLS-1$
			cmdArray[cmdArray.length-4] = "0"; //$NON-NLS-1$
			cmdArray[cmdArray.length-3] = "-noskip"; //$NON-NLS-1$
		}
		
		
		// force srate -> cause ac3's mencoder doesn't like anything other than 48khz
		if (media != null && !pcm && !ac3 ) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +4);
			cmdArray[cmdArray.length-6] = "-af"; //$NON-NLS-1$
			cmdArray[cmdArray.length-5] = "lavcresample=48000"; //$NON-NLS-1$
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
	
	private boolean setCmdToMencoderMT(PmsConfiguration configuration, String cmdArray [], DLNAMediaInfo media) {
		boolean set = configuration.getNumberOfCpuCores() > 1;
		if (media != null && (media.width < 1280 || media.codecV == null || !media.codecV.equals("h264"))) //$NON-NLS-1$
				set = false;
		if (set) {
			String mencoderMTPath = PMS.getConfiguration().getMencoderMTPath();
			if (new File(mencoderMTPath).exists()) {
				cmdArray[0] = mencoderMTPath;
				return true;
			}
		}
		return false;
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

	private String [] getSpecificCodecOptions(String codecParam, DLNAMediaInfo media, String filename, String srtFileName, boolean enable, boolean verifyOnly) {
		
		StringBuffer sb = new StringBuffer();
		
		String codecs = enable?DEFAULT_CODEC_CONF_SCRIPT:""; //$NON-NLS-1$
		codecs += "\n" + codecParam; //$NON-NLS-1$
		StringTokenizer stLines = new StringTokenizer(codecs, "\n"); //$NON-NLS-1$
		try {
			Interpreter interpreter = new Interpreter();
			interpreter.setStrictJava(true);
			ArrayList<String> types = CodecUtil.getPossibleCodecs();
			int rank = 1;
			if (types != null) {
				for(String type:types) {
					int r = rank++;
					interpreter.set("" + type, r); //$NON-NLS-1$
					if (media.container != null && media.container.equals(type)) {
						interpreter.set("container", r); //$NON-NLS-1$
						if (type.equals("matroska")) //$NON-NLS-1$
							interpreter.set("mkv", r); //$NON-NLS-1$
						else if (type.equals("rm")) //$NON-NLS-1$
							interpreter.set("rmvb", r); //$NON-NLS-1$
					} else if (media.codecV != null && media.codecV.equals(type)) {
						interpreter.set("vcodec", r); //$NON-NLS-1$
						if (type.equals("mpeg2video")) //$NON-NLS-1$
							interpreter.set("mpeg2", r); //$NON-NLS-1$
					} else if (media.codecA != null && media.codecA.equals(type)) {
						interpreter.set("acodec", r); //$NON-NLS-1$
					}
				}
			} else
				return null;
				
			interpreter.set("filename", filename); //$NON-NLS-1$
			interpreter.set("srtfile", srtFileName); //$NON-NLS-1$
			interpreter.set("samplerate", media.getSampleRate()); //$NON-NLS-1$
			try {
				String framerate = media.getValidFps(false);
				if (framerate != null)
					interpreter.set("framerate", Double.parseDouble(framerate)); //$NON-NLS-1$
			} catch (NumberFormatException e) {}
			interpreter.set("duration", media.getDurationInSeconds()); //$NON-NLS-1$
			interpreter.set("channels", media.nrAudioChannels); //$NON-NLS-1$
			interpreter.set("height", media.height); //$NON-NLS-1$
			interpreter.set("width", media.width); //$NON-NLS-1$
			while (stLines.hasMoreTokens()) {
				String line = stLines.nextToken();
				if (!line.startsWith("#") && line.trim().length() > 0) { //$NON-NLS-1$
					int separator = line.indexOf("::"); //$NON-NLS-1$
					if (separator > -1) {
						String key = null;
						try {
							key = line.substring(0, separator).trim();
							String value = line.substring(separator+2).trim();
							
							if (value.length() > 0) {
								if (key.length() == 0)
									key = "1 == 1"; //$NON-NLS-1$
								Object result = interpreter.eval(key);
								if (result != null && result instanceof Boolean && ((Boolean)result).booleanValue()) {
									sb.append(" "); //$NON-NLS-1$
									sb.append(value);
								}
							}
						} catch (Throwable e) {
							PMS.info("Error while executing: " + key + " : " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
							if (verifyOnly) {
								return new String [] { "@@Error while parsing: " + e.getMessage() }; //$NON-NLS-1$
							}
						}
					} else if (verifyOnly) {
						return new String [] { "@@Malformatted line: " + line }; //$NON-NLS-1$
					}
				}
			}
		} catch (EvalError e) {
			PMS.info("BeanShell error: " + e.getMessage()); //$NON-NLS-1$
		}
		String completeLine = sb.toString();
		
		ArrayList<String> args = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(completeLine, " "); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String arg = st.nextToken().trim();
			if (arg.length() > 0)
				args.add(arg);
		}
		
		String definitiveArgs [] = new String [args.size()];
		args.toArray(definitiveArgs);
		return definitiveArgs;
	}
	
	public static final String DEFAULT_CODEC_CONF_SCRIPT = 
		  Messages.getString("MEncoderVideo.68")  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.69")  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.70")  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.71") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.72")  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.73")  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.75")  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.76")  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.77")  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.78")  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.79")  //$NON-NLS-1$
		+ "#\n"  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.80") //$NON-NLS-1$
		+ "container == iso :: -nosync\n"  //$NON-NLS-1$
		+ "(container == avi || container == matroska) && vcodec == mpeg4 && acodec == mp3 :: -mc 0.1\n"  //$NON-NLS-1$
		+ "container == flv :: -mc 0.1\n"  //$NON-NLS-1$
		+ "container == mov :: -mc 0.1 -noass\n"  //$NON-NLS-1$
		+ "container == rm  :: -mc 0.1\n" //$NON-NLS-1$
		+ "\n"  //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.87") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.88") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.89") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.91"); //$NON-NLS-1$

}
