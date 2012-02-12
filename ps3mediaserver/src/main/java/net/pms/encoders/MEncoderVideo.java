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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
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

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.FileTranscodeVirtualFolder;
import net.pms.dlna.InputFile;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeIPCProcess;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.io.StreamModifier;
import net.pms.network.HTTPResource;
import net.pms.newgui.FontFileFilter;
import net.pms.newgui.LooksFrame;
import net.pms.newgui.MyComboBoxModel;
import net.pms.newgui.RestrictedFileSystemView;
import net.pms.util.CodecUtil;
import net.pms.util.ProcessUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.EvalError;
import bsh.Interpreter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;

public class MEncoderVideo extends Player {
	private static final Logger logger = LoggerFactory.getLogger(MEncoderVideo.class);

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
	private JTextField forcedsub;
	private JTextField forcedtags;
	private JTextField defaultaudiosubs;
	private JTextField defaultfont;
	private JComboBox subcp;
	private JTextField subq;
	private JCheckBox forcefps;
	private JCheckBox yadif;
	private JCheckBox scaler;
	private JTextField scaleX;
	private JTextField scaleY;
	private JCheckBox assdefaultstyle;
	private JCheckBox fc;
	private JCheckBox ass;
	private JCheckBox checkBox;
	private JCheckBox mencodermt;
	private JCheckBox videoremux;
	private JCheckBox noskip;
	private JCheckBox intelligentsync;
	private JTextField alternateSubFolder;
	private JButton subColor;
	private JTextField ocw;
	private JTextField och;
	private JCheckBox subs;
	private JCheckBox fribidi;
	private final PmsConfiguration configuration;
	public static final int MENCODER_MAX_THREADS = 8;
	protected boolean dts;
	public static final String ID = "mencoder";
	protected boolean pcm;
	protected boolean mux;
	protected boolean ovccopy;
	protected boolean dvd;
	protected boolean oaccopy;
	protected boolean mpegts;
	protected boolean wmv;
	protected String overriddenMainArgs[];

	public static final String DEFAULT_CODEC_CONF_SCRIPT =
		Messages.getString("MEncoderVideo.68")
		+ Messages.getString("MEncoderVideo.69")
		+ Messages.getString("MEncoderVideo.70")
		+ Messages.getString("MEncoderVideo.71")
		+ Messages.getString("MEncoderVideo.72")
		+ Messages.getString("MEncoderVideo.73")
		+ Messages.getString("MEncoderVideo.75")
		+ Messages.getString("MEncoderVideo.76")
		+ Messages.getString("MEncoderVideo.77")
		+ Messages.getString("MEncoderVideo.78")
		+ Messages.getString("MEncoderVideo.79")
		+ "#\n"
		+ Messages.getString("MEncoderVideo.80")
		+ "container == iso :: -nosync\n"
		+ "(container == avi || container == matroska) && vcodec == mpeg4 && acodec == mp3 :: -mc 0.1\n"
		+ "container == flv :: -mc 0.1\n"
		+ "container == mov :: -mc 0.1 -noass\n"
		+ "container == rm  :: -mc 0.1\n"
		+ "container == matroska && framerate == 29.97  :: -nomux -mc 0\n"
		+ "container == mp4 && vcodec == h264 :: -mc 0.1 -noass\n"
		+ "\n"
		+ Messages.getString("MEncoderVideo.87")
		+ Messages.getString("MEncoderVideo.88")
		+ Messages.getString("MEncoderVideo.89")
		+ Messages.getString("MEncoderVideo.91");

	public JCheckBox getCheckBox() {
		return checkBox;
	}

	public JCheckBox getNoskip() {
		return noskip;
	}

	public JCheckBox getSubs() {
		return subs;
	}

	public MEncoderVideo(PmsConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout(
			"left:pref, 3dlu, p:grow, 3dlu, right:p:grow, 3dlu, p:grow, 3dlu, right:p:grow,3dlu, p:grow, 3dlu, right:p:grow,3dlu, pref:grow",
			"p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu,p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 9dlu, p, 2dlu, p, 2dlu, p , 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.EMPTY_BORDER);
		builder.setOpaque(false);

		CellConstraints cc = new CellConstraints();

		checkBox = new JCheckBox(Messages.getString("MEncoderVideo.0"));
		checkBox.setContentAreaFilled(false);
		if (configuration.getSkipLoopFilterEnabled()) {
			checkBox.setSelected(true);
		}
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setSkipLoopFilterEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		JComponent cmp = builder.addSeparator(Messages.getString("MEncoderVideo.1"), cc.xyw(1, 1, 15));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		mencodermt = new JCheckBox(Messages.getString("MEncoderVideo.35"));
		mencodermt.setContentAreaFilled(false);
		if (configuration.getMencoderMT()) {
			mencodermt.setSelected(true);
		}
		mencodermt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				configuration.setMencoderMT(mencodermt.isSelected());
			}
		});
		mencodermt.setEnabled(Platform.isWindows() || Platform.isMac());

		builder.add(mencodermt, cc.xy(1, 3));
		builder.add(checkBox, cc.xyw(3, 3, 12));

		noskip = new JCheckBox(Messages.getString("MEncoderVideo.2"));
		noskip.setContentAreaFilled(false);
		if (configuration.isMencoderNoOutOfSync()) {
			noskip.setSelected(true);
		}
		noskip.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderNoOutOfSync((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		builder.add(noskip, cc.xy(1, 5));

		JButton button = new JButton(Messages.getString("MEncoderVideo.29"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel codecPanel = new JPanel(new BorderLayout());
				final JTextArea textArea = new JTextArea();
				textArea.setText(configuration.getCodecSpecificConfig());
				textArea.setFont(new Font("Courier", Font.PLAIN, 12));
				JScrollPane scrollPane = new JScrollPane(textArea);
				scrollPane.setPreferredSize(new java.awt.Dimension(900, 100));

				final JTextArea textAreaDefault = new JTextArea();
				textAreaDefault.setText(DEFAULT_CODEC_CONF_SCRIPT);
				textAreaDefault.setBackground(Color.WHITE);
				textAreaDefault.setFont(new Font("Courier", Font.PLAIN, 12));
				textAreaDefault.setEditable(false);
				textAreaDefault.setEnabled(configuration.isMencoderIntelligentSync());
				JScrollPane scrollPaneDefault = new JScrollPane(textAreaDefault);
				scrollPaneDefault.setPreferredSize(new java.awt.Dimension(900, 450));

				JPanel customPanel = new JPanel(new BorderLayout());
				intelligentsync = new JCheckBox(Messages.getString("MEncoderVideo.3"));
				intelligentsync.setContentAreaFilled(false);
				if (configuration.isMencoderIntelligentSync()) {
					intelligentsync.setSelected(true);
				}
				intelligentsync.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						configuration.setMencoderIntelligentSync((e.getStateChange() == ItemEvent.SELECTED));
						textAreaDefault.setEnabled(configuration.isMencoderIntelligentSync());

					}
				});

				JLabel label = new JLabel(Messages.getString("MEncoderVideo.33"));
				customPanel.add(label, BorderLayout.NORTH);
				customPanel.add(scrollPane, BorderLayout.SOUTH);

				codecPanel.add(intelligentsync, BorderLayout.NORTH);
				codecPanel.add(scrollPaneDefault, BorderLayout.CENTER);
				codecPanel.add(customPanel, BorderLayout.SOUTH);
				while (JOptionPane.showOptionDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
					codecPanel, Messages.getString("MEncoderVideo.34"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
					String newCodecparam = textArea.getText();
					DLNAMediaInfo fakemedia = new DLNAMediaInfo();
					DLNAMediaAudio audio = new DLNAMediaAudio();
					audio.setCodecA("ac3");
					fakemedia.setCodecV("mpeg4");
					fakemedia.setContainer("matroska");
					fakemedia.setDuration(45d*60);
					audio.setNrAudioChannels(2);
					fakemedia.setWidth(1280);
					fakemedia.setHeight(720);
					audio.setSampleFrequency("48000");
					fakemedia.setFrameRate("23.976");
					fakemedia.getAudioCodes().add(audio);
					String result[] = getSpecificCodecOptions(newCodecparam, fakemedia, new OutputParams(configuration), "dummy.mpg", "dummy.srt", false, true);
					if (result.length > 0 && result[0].startsWith("@@")) {
						String errorMessage = result[0].substring(2);
						JOptionPane.showMessageDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())), errorMessage);

					} else {
						configuration.setCodecSpecificConfig(newCodecparam);
						break;
					}
				}
			}
		});
		builder.add(button, cc.xyw(1, 11, 2));

		forcefps = new JCheckBox(Messages.getString("MEncoderVideo.4"));
		forcefps.setContentAreaFilled(false);
		if (configuration.isMencoderForceFps()) {
			forcefps.setSelected(true);
		}
		forcefps.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderForceFps(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		builder.add(forcefps, cc.xyw(1, 7, 2));

		yadif = new JCheckBox(Messages.getString("MEncoderVideo.26"));
		yadif.setContentAreaFilled(false);
		if (configuration.isMencoderYadif()) {
			yadif.setSelected(true);
		}
		yadif.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderYadif(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		builder.add(yadif, cc.xyw(3, 7, 7));

		scaler = new JCheckBox(Messages.getString("MEncoderVideo.27"));
		scaler.setContentAreaFilled(false);
		scaler.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderScaler(e.getStateChange() == ItemEvent.SELECTED);
				scaleX.setEnabled(configuration.isMencoderScaler());
				scaleY.setEnabled(configuration.isMencoderScaler());
			}
		});

		builder.add(scaler, cc.xyw(3, 5, 7));

		builder.addLabel(Messages.getString("MEncoderVideo.28"), cc.xyw(10, 5, 3, CellConstraints.RIGHT, CellConstraints.CENTER));
		scaleX = new JTextField("" + configuration.getMencoderScaleX());
		scaleX.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				try {
					configuration.setMencoderScaleX(Integer.parseInt(scaleX.getText()));
				} catch (NumberFormatException nfe) {
					logger.debug("Could not parse scaleX from \"" + scaleX.getText() + "\"");
				}
			}
		});
		builder.add(scaleX, cc.xyw(13, 5, 3));

		builder.addLabel(Messages.getString("MEncoderVideo.30"), cc.xyw(10, 7, 3, CellConstraints.RIGHT, CellConstraints.CENTER));
		scaleY = new JTextField("" + configuration.getMencoderScaleY());
		scaleY.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				try {
					configuration.setMencoderScaleY(Integer.parseInt(scaleY.getText()));
				} catch (NumberFormatException nfe) {
					logger.debug("Could not parse scaleY from \"" + scaleY.getText() + "\"");
				}
			}
		});
		builder.add(scaleY, cc.xyw(13, 7, 3));

		if (configuration.isMencoderScaler()) {
			scaler.setSelected(true);
		} else {
			scaleX.setEnabled(false);
			scaleY.setEnabled(false);
		}



		videoremux = new JCheckBox("<html>" + Messages.getString("MEncoderVideo.38") + "</html>");
		videoremux.setContentAreaFilled(false);
		if (configuration.isMencoderMuxWhenCompatible()) {
			videoremux.setSelected(true);
		}
		videoremux.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderMuxWhenCompatible((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		builder.add(videoremux, cc.xyw(1, 9, 13));

		cmp = builder.addSeparator(Messages.getString("MEncoderVideo.5"), cc.xyw(1, 19, 15));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		builder.addLabel(Messages.getString("MEncoderVideo.6"), cc.xy(1, 21));
		decode = new JTextField(configuration.getMencoderDecode());
		decode.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderDecode(decode.getText());
			}
		});
		builder.add(decode, cc.xyw(3, 21, 13));

		builder.addLabel(Messages.getString("MEncoderVideo.7"), cc.xyw(1, 23, 15));
		langs = new JTextField(configuration.getMencoderAudioLanguages());
		langs.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAudioLanguages(langs.getText());
			}
		});
		builder.add(langs, cc.xyw(3, 23, 8));

		cmp = builder.addSeparator(Messages.getString("MEncoderVideo.8"), cc.xyw(1, 25, 15));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		builder.addLabel(Messages.getString("MEncoderVideo.9"), cc.xy(1, 27));
		defaultsubs = new JTextField(configuration.getMencoderSubLanguages());
		defaultsubs.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderSubLanguages(defaultsubs.getText());
			}
		});

		builder.addLabel(Messages.getString("MEncoderVideo.94"), cc.xy(5, 27));
		forcedsub = new JTextField(configuration.getMencoderForcedSubLanguage());
		forcedsub.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderForcedSubLanguage(forcedsub.getText());
			}
		});

		builder.addLabel(Messages.getString("MEncoderVideo.95"), cc.xy(9, 27));
		forcedtags = new JTextField(configuration.getMencoderForcedSubTags());
		forcedtags.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderForcedSubTags(forcedtags.getText());
			}
		});
		builder.add(defaultsubs, cc.xyw(3, 27, 2));

		builder.add(forcedsub, cc.xy(7, 27));

		builder.add(forcedtags, cc.xyw(11, 27, 5));

		builder.addLabel(Messages.getString("MEncoderVideo.10"), cc.xy(1, 29));
		defaultaudiosubs = new JTextField(configuration.getMencoderAudioSubLanguages());
		defaultaudiosubs.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAudioSubLanguages(defaultaudiosubs.getText());
			}
		});
		builder.add(defaultaudiosubs, cc.xyw(3, 29, 8));

		builder.addLabel(Messages.getString("MEncoderVideo.11"), cc.xy(1, 31));
		Object data[] = new Object[]{
			configuration.getMencoderSubCp(),
			"cp1250  /* Windows - Eastern Europe */",
			"cp1251  /* Windows - Cyrillic */",
			"cp1252  /* Windows - Western Europe */",
			"cp1253  /* Windows - Greek */",
			"cp1254  /* Windows - Turkish */",
			"cp1255  /* Windows - Hebrew */",
			"cp1256  /* Windows - Arabic */",
			"cp1257  /* Windows - Baltic */",
			"cp1258  /* Windows - Vietnamese */",
			"ISO-8859-1 /* Western Europe */",
			"ISO-8859-2 /* Western and Central Europe */",
			"ISO-8859-3 /* Western Europe and South European */",
			"ISO-8859-4 /* Western Europe and Baltic countries */",
			"ISO-8859-5 /* Cyrillic alphabet */",
			"ISO-8859-6 /* Arabic */",
			"ISO-8859-7 /* Greek */",
			"ISO-8859-8 /* Hebrew */",
			"ISO-8859-9 /* Western Europe with amended Turkish */",
			"ISO-8859-10 /* Western Europe with Nordic languages */",
			"ISO-8859-11 /* Thai */",
			"ISO-8859-13 /* Baltic languages plus Polish */",
			"ISO-8859-14 /* Celtic languages */",
			"ISO-8859-15 /* Added the Euro sign */",
			"ISO-8859-16 /* Central European languages */",
			"cp932   /* Japanese */",
			"cp936   /* Chinese */",
			"cp949   /* Korean */",
			"cp950   /* Big5, Taiwanese, Cantonese */", 
			"UTF-8   /* Unicode */"
		};
		MyComboBoxModel cbm = new MyComboBoxModel(data);

		subcp = new JComboBox(cbm);
		subcp.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String s = (String) e.getItem();
					if (s.indexOf("/*") > -1) {
						s = s.substring(0, s.indexOf("/*")).trim();
					}
					configuration.setMencoderSubCp(s);
				}
			}
		});
		subcp.setEditable(true);
		builder.add(subcp, cc.xyw(3, 31, 7));

		fribidi = new JCheckBox(Messages.getString("MEncoderVideo.23"));
		fribidi.setContentAreaFilled(false);
		if (configuration.isMencoderSubFribidi()) {
			fribidi.setSelected(true);
		}
		fribidi.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderSubFribidi(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(fribidi, cc.xyw(11, 31, 4));

		builder.addLabel(Messages.getString("MEncoderVideo.24"), cc.xy(1, 33));
		defaultfont = new JTextField(configuration.getMencoderFont());
		defaultfont.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderFont(defaultfont.getText());
			}
		});
		builder.add(defaultfont, cc.xyw(3, 33, 8));

		JButton fontselect = new JButton("...");
		fontselect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FontFileFilter());
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("MEncoderVideo.25"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					defaultfont.setText(chooser.getSelectedFile().getAbsolutePath());
					configuration.setMencoderFont(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		builder.add(fontselect, cc.xyw(11, 33, 2));

		builder.addLabel(Messages.getString("MEncoderVideo.37"), cc.xyw(1, 35, 3));
		alternateSubFolder = new JTextField(configuration.getAlternateSubsFolder());
		alternateSubFolder.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setAlternateSubsFolder(alternateSubFolder.getText());
			}
		});
		builder.add(alternateSubFolder, cc.xyw(3, 35, 8));

		JButton select = new JButton("...");
		select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = null;
				try {
					chooser = new JFileChooser();
				} catch (Exception ee) {
					chooser = new JFileChooser(new RestrictedFileSystemView());
				}
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("FoldTab.28"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					alternateSubFolder.setText(chooser.getSelectedFile().getAbsolutePath());
					configuration.setAlternateSubsFolder(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		builder.add(select, cc.xyw(11, 35, 2));

		builder.addLabel(Messages.getString("MEncoderVideo.12"), cc.xy(1, 39, CellConstraints.RIGHT, CellConstraints.CENTER));
		mencoder_ass_scale = new JTextField(configuration.getMencoderAssScale());
		mencoder_ass_scale.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAssScale(mencoder_ass_scale.getText());
			}
		});

		builder.addLabel(Messages.getString("MEncoderVideo.13"), cc.xy(5, 39));
		mencoder_ass_outline = new JTextField(configuration.getMencoderAssOutline());
		mencoder_ass_outline.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAssOutline(mencoder_ass_outline.getText());
			}
		});

		builder.addLabel(Messages.getString("MEncoderVideo.14"), cc.xy(9, 39));
		mencoder_ass_shadow = new JTextField(configuration.getMencoderAssShadow());
		mencoder_ass_shadow.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAssShadow(mencoder_ass_shadow.getText());
			}
		});

		builder.addLabel(Messages.getString("MEncoderVideo.15"), cc.xy(13, 39));
		mencoder_ass_margin = new JTextField(configuration.getMencoderAssMargin());
		mencoder_ass_margin.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAssMargin(mencoder_ass_margin.getText());
			}
		});
		builder.add(mencoder_ass_scale, cc.xy(3, 39));

		builder.add(mencoder_ass_outline, cc.xy(7, 39));

		builder.add(mencoder_ass_shadow, cc.xy(11, 39));

		builder.add(mencoder_ass_margin, cc.xy(15, 39));


		builder.addLabel(Messages.getString("MEncoderVideo.16"), cc.xy(1, 41, CellConstraints.RIGHT, CellConstraints.CENTER));
		mencoder_noass_scale = new JTextField(configuration.getMencoderNoAssScale());
		mencoder_noass_scale.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderNoAssScale(mencoder_noass_scale.getText());
			}
		});

		builder.addLabel(Messages.getString("MEncoderVideo.17"), cc.xy(5, 41));
		mencoder_noass_outline = new JTextField(configuration.getMencoderNoAssOutline());
		mencoder_noass_outline.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderNoAssOutline(mencoder_noass_outline.getText());
			}
		});

		builder.addLabel(Messages.getString("MEncoderVideo.18"), cc.xy(9, 41));
		mencoder_noass_blur = new JTextField(configuration.getMencoderNoAssBlur());
		mencoder_noass_blur.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderNoAssBlur(mencoder_noass_blur.getText());
			}
		});

		builder.addLabel(Messages.getString("MEncoderVideo.19"), cc.xy(13, 41));
		mencoder_noass_subpos = new JTextField(configuration.getMencoderNoAssSubPos());
		mencoder_noass_subpos.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderNoAssSubPos(mencoder_noass_subpos.getText());
			}
		});
		builder.add(mencoder_noass_scale, cc.xy(3, 41));

		builder.add(mencoder_noass_outline, cc.xy(7, 41));

		builder.add(mencoder_noass_blur, cc.xy(11, 41));

		builder.add(mencoder_noass_subpos, cc.xy(15, 41));


		ass = new JCheckBox(Messages.getString("MEncoderVideo.20"));
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

		builder.add(ass, cc.xy(1, 37));
		ass.setSelected(configuration.isMencoderAss());
		ass.getItemListeners()[0].itemStateChanged(null);

		fc = new JCheckBox(Messages.getString("MEncoderVideo.21"));
		fc.setContentAreaFilled(false);
		fc.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderFontConfig(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		builder.add(fc, cc.xyw(3, 37, 5));
		fc.setSelected(configuration.isMencoderFontConfig());

		assdefaultstyle = new JCheckBox(Messages.getString("MEncoderVideo.36"));
		assdefaultstyle.setContentAreaFilled(false);
		assdefaultstyle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderAssDefaultStyle(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		builder.add(assdefaultstyle, cc.xyw(8, 37, 4));
		assdefaultstyle.setSelected(configuration.isMencoderAssDefaultStyle());

		subs = new JCheckBox(Messages.getString("MEncoderVideo.22"));
		subs.setContentAreaFilled(false);
		if (configuration.getUseSubtitles()) {
			subs.setSelected(true);
		}
		subs.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setUseSubtitles((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(subs, cc.xyw(1, 43, 15));

		builder.addLabel(Messages.getString("MEncoderVideo.92"), cc.xy(1, 45));
		subq = new JTextField(configuration.getMencoderVobsubSubtitleQuality());
		subq.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderVobsubSubtitleQuality(subq.getText());
			}
		});
		builder.add(subq, cc.xyw(3, 45, 1));

		builder.addLabel(Messages.getString("MEncoderVideo.93"), cc.xyw(1, 47, 6));

		builder.addLabel(Messages.getString("MEncoderVideo.28"), cc.xy(1, 49, CellConstraints.RIGHT, CellConstraints.CENTER));
		ocw = new JTextField(configuration.getMencoderOverscanCompensationWidth());
		ocw.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderOverscanCompensationWidth(ocw.getText());
			}
		});
		builder.add(ocw, cc.xyw(3, 49, 1));

		builder.addLabel(Messages.getString("MEncoderVideo.30"), cc.xy(5, 49));
		och = new JTextField(configuration.getMencoderOverscanCompensationHeight());
		och.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderOverscanCompensationHeight(och.getText());
			}
		});
		builder.add(och, cc.xyw(7, 49, 1));

		subColor = new JButton();
		subColor.setText(Messages.getString("MEncoderVideo.31"));
		subColor.setBackground(new Color(configuration.getSubsColor()));
		subColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(
					(JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
					"Choose Subtitles Color",
					subColor.getBackground());
				if (newColor != null) {
					subColor.setBackground(newColor);
					configuration.setSubsColor(newColor.getRGB());
				}
			}
		});
		builder.add(subColor, cc.xyw(12, 37, 4));

		JCheckBox disableSubs = ((LooksFrame) PMS.get().getFrame()).getTr().getDisableSubs();
		disableSubs.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderDisableSubs(e.getStateChange() == ItemEvent.SELECTED);

				subs.setEnabled(!configuration.isMencoderDisableSubs());
				subq.setEnabled(!configuration.isMencoderDisableSubs());
				defaultsubs.setEnabled(!configuration.isMencoderDisableSubs());
				subcp.setEnabled(!configuration.isMencoderDisableSubs());
				ass.setEnabled(!configuration.isMencoderDisableSubs());
				assdefaultstyle.setEnabled(!configuration.isMencoderDisableSubs());
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

				if (!configuration.isMencoderDisableSubs()) {
					ass.getItemListeners()[0].itemStateChanged(null);
				}
			}
		});
		if (configuration.isMencoderDisableSubs()) {
			disableSubs.setSelected(true);
		}

		return builder.getPanel();
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
	public boolean avisynth() {
		return false;
	}

	@Override
	public boolean isTimeSeekable() {
		return true;
	}

	protected String[] getDefaultArgs() {
		return new String[]{
			"-quiet",
			"-oac", oaccopy ? "copy" : (pcm ? "pcm" : "lavc"),
			"-of", (wmv || mpegts) ? "lavf" : (pcm && avisynth()) ? "avi" : (((pcm || dts || mux) ? "rawvideo" : "mpeg")),
			(wmv || mpegts) ? "-lavfopts" : "-quiet",
			wmv ? "format=asf" : (mpegts ? "format=mpegts" : "-quiet"),
			"-mpegopts", "format=mpeg2:muxrate=500000:vbuf_size=1194:abuf_size=64",
			"-ovc", (mux || ovccopy) ? "copy" : "lavc"
		};
	}

	@Override
	public String[] args() {
		String args[] = null;
		String defaultArgs[] = getDefaultArgs();
		if (overriddenMainArgs != null) {
			args = new String[defaultArgs.length + overriddenMainArgs.length];
			for (int i = 0; i < defaultArgs.length; i++) {
				args[i] = defaultArgs[i];
			}
			for (int i = 0; i < overriddenMainArgs.length; i++) {
				if ("-of".equals(overriddenMainArgs[i]) || "-oac".equals(overriddenMainArgs[i]) || "-ovc".equals(overriddenMainArgs[i]) || "-mpegopts".equals(overriddenMainArgs[i])) {
					logger.info("MEncoder encoder settings: You cannot change Muxer, Muxer options, Video Codec or Audio Codec");
					overriddenMainArgs[i] = "-quiet";
					if (i + 1 < overriddenMainArgs.length) {
						overriddenMainArgs[i + 1] = "-quiet";
					}
				}
				args[i + defaultArgs.length] = overriddenMainArgs[i];
			}
		} else {
			args = defaultArgs;
		}
		return args;

	}

	@Override
	public String executable() {
		return configuration.getMencoderPath();
	}

	private int[] getVideoBitrateConfig(String bitrate) {
		int bitrates[] = new int[2];
		if (bitrate.contains("(") && bitrate.contains(")")) {
			bitrates[1] = Integer.parseInt(bitrate.substring(bitrate.indexOf("(") + 1, bitrate.indexOf(")")));
		}
		if (bitrate.contains("("))
		{
			bitrate = bitrate.substring(0, bitrate.indexOf("(")).trim();
		}
		if (StringUtils.isBlank(bitrate)) {
			bitrate = "0";
		}
		bitrates[0] = (int) Double.parseDouble(bitrate);
		return bitrates;
	}

	/**
	 * Note: This is not exact, the bitrate can go above this but it is generally pretty good.
	 * @return The maximum bitrate the video should be along with the buffer size using MEncoder vars
	 */
	private String addMaximumBitrateConstraints(String encodeSettings, DLNAMediaInfo media, String quality, RendererConfiguration mediaRenderer, String audioType) {
		int defaultMaxBitrates[] = getVideoBitrateConfig(configuration.getMaximumBitrate());
		int rendererMaxBitrates[] = new int[2];
		if (mediaRenderer.getMaxVideoBitrate() != null) {
			rendererMaxBitrates = getVideoBitrateConfig(mediaRenderer.getMaxVideoBitrate());
		}
		if ((defaultMaxBitrates[0] == 0 && rendererMaxBitrates[0] > 0) || rendererMaxBitrates[0] < defaultMaxBitrates[0] && rendererMaxBitrates[0] > 0) {
			defaultMaxBitrates = rendererMaxBitrates;
		}
		if (defaultMaxBitrates[0] > 0 && !quality.contains("vrc_buf_size") && !quality.contains("vrc_maxrate") && !quality.contains("vbitrate")) {
			// Convert value from Mb to Kb
			defaultMaxBitrates[0] = 1000 * defaultMaxBitrates[0];

			// Halve it since it seems to send up to 1 second of video in advance
			defaultMaxBitrates[0] = defaultMaxBitrates[0] / 2;

			int bufSize = 1835;
			if (media.isHDVideo()) {
				bufSize = defaultMaxBitrates[0] / 3;
			}
			if (bufSize > 7000) {
				bufSize = 7000;
			}

			if (defaultMaxBitrates[1] > 0) {
				bufSize = defaultMaxBitrates[1];
			}

			if (mediaRenderer.isDefaultVBVSize() && rendererMaxBitrates[1] == 0) {
				bufSize = 1835;
			}

			// Make room for audio
			// If audio is PCM, subtract 4600kb/s
			if ("pcm".equals(audioType)) {
				defaultMaxBitrates[0] = defaultMaxBitrates[0] - 4600;
			}
			// If audio is DTS, subtract 1510kb/s
			else if ("dts".equals(audioType)) {
				defaultMaxBitrates[0] = defaultMaxBitrates[0] - 1510;
			}
			// If audio is AC3, subtract 640kb/s to be safe
			else if ("ac3".equals(audioType)) {
				defaultMaxBitrates[0] = defaultMaxBitrates[0] - 640;
			}

			// Round down to the nearest Mb
			defaultMaxBitrates[0] = defaultMaxBitrates[0] / 1000 * 1000;

			encodeSettings += ":vrc_maxrate=" + defaultMaxBitrates[0] + ":vrc_buf_size=" + bufSize;
		}
		return encodeSettings;
	}

	@Override
	public ProcessWrapper launchTranscode(
		String fileName,
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		params.manageFastStart();

		boolean avisynth = avisynth();

		setAudioAndSubs(fileName, media, params, configuration);

		String subString = null;
		if (params.sid != null && params.sid.getPlayableFile() != null) {
			subString = ProcessUtil.getShortFileNameIfWideChars(params.sid.getPlayableFile().getAbsolutePath());
		}

		InputFile newInput = new InputFile();
		newInput.setFilename(fileName);
		newInput.setPush(params.stdin);

		dvd = false;
		if (media != null && media.getDvdtrack() > 0) {
			dvd = true;
		}

		// don't honour "Switch to tsMuxeR..." if the resource is being streamed via an MEncoder entry in
		// the #--TRANSCODE--# folder
		boolean forceMencoder = !configuration.getHideTranscodeEnabled()
			&& dlna.isNoName()
			&& (dlna.getParent() instanceof FileTranscodeVirtualFolder);

		ovccopy = false;

		int intOCW = 0;
		int intOCH = 0;

		try {
			intOCW = Integer.parseInt(configuration.getMencoderOverscanCompensationWidth());
		} catch (NumberFormatException e) {
			logger.error("Cannot parse configured MEncoder overscan compensation width: \""
					+ configuration.getMencoderOverscanCompensationWidth() + "\"");
		}

		try {
			intOCH = Integer.parseInt(configuration.getMencoderOverscanCompensationHeight());
		} catch (NumberFormatException e) {
			logger.error("Cannot parse configured MEncoder overscan compensation height: \""
					+ configuration.getMencoderOverscanCompensationHeight() + "\"");
		}

		if (
			!forceMencoder &&
			params.sid == null &&
			!dvd &&
			!avisynth() &&
			media != null && (
				media.isVideoPS3Compatible(newInput) ||
				!params.mediaRenderer.isH264Level41Limited()
			) &&
			media.isMuxable(params.mediaRenderer) &&
			configuration.isMencoderMuxWhenCompatible() &&
			params.mediaRenderer.isMuxH264MpegTS() && (
				intOCW == 0 &&
				intOCH == 0
			)
		) {
			String sArgs[] = getSpecificCodecOptions(configuration.getCodecSpecificConfig(), media, params, fileName, subString, configuration.isMencoderIntelligentSync(), false);
			boolean nomux = false;
			for (String s : sArgs) {
				if (s.equals("-nomux")) {
					nomux = true;
				}
			}
			if (!nomux) {
				TSMuxerVideo tv = new TSMuxerVideo(configuration);
				params.forceFps = media.getValidFps(false);
				if (media.getCodecV().equals("h264")) {
					params.forceType = "V_MPEG4/ISO/AVC";
				} else if (media.getCodecV().startsWith("mpeg2")) {
					params.forceType = "V_MPEG-2";
				} else if (media.getCodecV().equals("vc1")) {
					params.forceType = "V_MS/VFW/WVC1";
				}
				return tv.launchTranscode(fileName, dlna, media, params);
			}
		} else if (params.sid == null && dvd && configuration.isMencoderRemuxMPEG2() && params.mediaRenderer.isMpeg2Supported()) {
			String sArgs[] = getSpecificCodecOptions(configuration.getCodecSpecificConfig(), media, params, fileName, subString, configuration.isMencoderIntelligentSync(), false);
			boolean nomux = false;
			for (String s : sArgs) {
				if (s.equals("-nomux")) {
					nomux = true;
				}
			}
			if (!nomux) {
				ovccopy = true;
			}
		}

		String vcodec = "mpeg2video";

		wmv = false;
		if (params.mediaRenderer.isTranscodeToWMV()) {
			wmv = true;
			vcodec = "wmv2"; // http://wiki.megaframe.org/wiki/Ubuntu_XBOX_360#MEncoder not usable in streaming
		}

		mpegts = false;
		if (params.mediaRenderer.isTranscodeToMPEGTSAC3()) {
			mpegts = true;
		}

		oaccopy = false;
		if (configuration.isRemuxAC3() && params.aid != null && params.aid.isAC3() && !avisynth() && params.mediaRenderer.isTranscodeToAC3()) {
			oaccopy = true;
		}

		dts = configuration.isDTSEmbedInPCM() && (!dvd || configuration.isMencoderRemuxMPEG2()) && params.aid != null && params.aid.isDTS() && !avisynth() && params.mediaRenderer.isDTSPlayable();
		pcm = configuration.isMencoderUsePcm() && (!dvd || configuration.isMencoderRemuxMPEG2()) && (params.aid != null && (params.aid.isDTS() || params.aid.isLossless())) && params.mediaRenderer.isMuxLPCMToMpeg();

		if (dts || pcm) {
			if (dts) {
				oaccopy = true;
			}
			params.losslessaudio = true;
			params.forceFps = media.getValidFps(false);
		}

		// mpeg2 remux still buggy with mencoder :\
		if (!pcm && !dts && !mux && ovccopy) {
			ovccopy = false;
		}

		if (pcm && avisynth()) {
			params.avidemux = true;
		}

		String add = "";
		if (params.mediaRenderer.getCustomMencoderOptions() != null) {
			if (params.mediaRenderer.getCustomMencoderOptions().indexOf("-lavdopts") == -1) {
				add = " -lavdopts debug=0";
			}
		} else if (configuration.getMencoderDecode() == null || configuration.getMencoderDecode().indexOf("-lavdopts") == -1) {
			add = " -lavdopts debug=0";
		}

		String alternativeCodec = "";//"-ac ffac3,ffdca, "; 
		if (dvd) {
			alternativeCodec = "";
		}
		int channels = wmv ? 2 : configuration.getAudioChannelCount();
		if (media != null && params.aid != null) {
			channels = wmv ? 2 : CodecUtil.getRealChannelCount(configuration, params.aid);
		}
		logger.trace("channels=" + channels);

		StringTokenizer st = new StringTokenizer(alternativeCodec + "-channels " + channels + " " + ((params.mediaRenderer.getCustomMencoderOptions()!=null && !(params.mediaRenderer.getCustomMencoderOptions().indexOf("expand=") != -1 && dvd))?(params.mediaRenderer.getCustomMencoderOptions()):configuration.getMencoderDecode()) + add, " ");
		overriddenMainArgs = new String[st.countTokens()];
		int i = 0;
		boolean processToken = false;
		int nThreads = (dvd || fileName.toLowerCase().endsWith("dvr-ms")) ?
			1 : configuration.getMencoderMaxThreads();

		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			if (processToken) {
				token += ":threads=" + nThreads;
				if (configuration.getSkipLoopFilterEnabled() && !avisynth()) {
					token += ":skiploopfilter=all";
				}
				processToken = false;
			}
			if (token.toLowerCase().contains("lavdopts")) {
				processToken = true;
			}

			overriddenMainArgs[i++] = token;
		}

		if (configuration.getMencoderMainSettings() != null) {
			String mainConfig = configuration.getMencoderMainSettings();
			String customSettings = params.mediaRenderer.getCustomMencoderQualitySettings();

			// Custom settings in PMS may override the settings of the saved configuration
			if (customSettings != null && !"".equals(customSettings.trim())) {
				mainConfig = customSettings;
			}

			if (mainConfig.contains("/*")) // in case of
			{
				mainConfig = mainConfig.substring(mainConfig.indexOf("/*"));
			}

			// Ditlew - WDTV Live (+ other byte asking clients), CBR. This probably ought to be placed in addMaximumBitrateConstraints(..)
			int cbr_bitrate = params.mediaRenderer.getCBRVideoBitrate();
			String cbr_settings = (cbr_bitrate > 0) ? ":vrc_buf_size=1835:vrc_minrate=" + cbr_bitrate + ":vrc_maxrate=" + cbr_bitrate + ":vbitrate=" + cbr_bitrate : "";
			String encodeSettings = "-lavcopts autoaspect=1:vcodec=" + vcodec +
				(wmv ? ":acodec=wmav2:abitrate=448" : (cbr_settings + ":acodec=" + (configuration.isMencoderAc3Fixed() ? "ac3_fixed" : "ac3") +
				":abitrate=" + CodecUtil.getAC3Bitrate(configuration, params.aid))) +
				":threads=" + (wmv ? 1 : configuration.getMencoderMaxThreads()) +
				("".equals(mainConfig) ? "" : ":" + mainConfig);

			String audioType = "ac3";
			if (dts) {
				audioType = "dts";
			} else if (pcm) {
				audioType = "pcm";
			}

			encodeSettings = addMaximumBitrateConstraints(encodeSettings, media, mainConfig, params.mediaRenderer, audioType);
			st = new StringTokenizer(encodeSettings, " ");
			int oldc = overriddenMainArgs.length;
			overriddenMainArgs = Arrays.copyOf(overriddenMainArgs, overriddenMainArgs.length + st.countTokens());
			i = oldc;
			while (st.hasMoreTokens()) {
				overriddenMainArgs[i++] = st.nextToken();
			}
		}

		boolean needAssFixPTS = false;

		boolean foundNoassParam = false;
		if (media != null) {
			String sArgs [] = getSpecificCodecOptions(configuration.getCodecSpecificConfig(), media, params, fileName, subString, configuration.isMencoderIntelligentSync(), false);
			for(String s:sArgs) {
				if (s.equals("-noass")) {
					foundNoassParam = true;
				}
			}
		}

		StringBuilder sb = new StringBuilder();

		// Use ASS & Fontconfig flags (and therefore ASS font styles) for all subtitled files except vobsub, embedded, dvd and mp4 container with srt
		// Note: The MP4 container with SRT rule is a workaround for MEncoder r30369. If there is ever a later version of MEncoder that supports external srt subs we should use that. As of r32848 that isn't the case
		if (
			params.sid != null &&
			params.sid.getType() != DLNAMediaSubtitle.EMBEDDED &&
			params.sid.getType() != DLNAMediaSubtitle.VOBSUB &&
			!(params.sid.getType() == DLNAMediaSubtitle.SUBRIP && media.getContainer().equals("mp4")) &&
			!configuration.isMencoderDisableSubs() &&
			configuration.isMencoderAss() &&
			!foundNoassParam &&
			!dvd &&
			!avisynth()
		) {
			sb.append("-ass -").append(configuration.isMencoderFontConfig() ? "" : "no").append("fontconfig ");
			if (mpegts || wmv) {
				needAssFixPTS = Platform.isWindows(); // don't think the fixpts filter is in the mplayer trunk
			}
		}

		// Apply DVD/VOBsub subtitle quality
		if (params.sid != null && params.sid.getType() == DLNAMediaSubtitle.VOBSUB && configuration.getMencoderVobsubSubtitleQuality() != null) {
			String subtitleQuality = configuration.getMencoderVobsubSubtitleQuality();

			sb.append("-spuaa ").append(subtitleQuality).append(" ");
		}

		// Apply overscan compensation
		// -vf scale to original resolution seems to fix most cases of video warping
		// TODO: Integrate with Video Scaler option
		if (intOCW > 0 || intOCH > 0) {
			String scaleAppend = "";

			if (media != null && media.getWidth() > 0 && media.getHeight() > 0) {
				scaleAppend = ",scale=" + media.getWidth() + ":" + media.getHeight();
			}

			sb.append("-vf softskip,expand=-").append(intOCW).append(":-").append(intOCH).append(scaleAppend).append(",harddup ");
		}

		if (params.sid != null && !params.sid.isFileUtf8() && !configuration.isMencoderDisableSubs() && configuration.getMencoderSubCp() != null && configuration.getMencoderSubCp().length() > 0) {
			sb.append("-subcp ").append(configuration.getMencoderSubCp()).append(" ");
			if (configuration.isMencoderSubFribidi()) {
				sb.append("-fribidi-charset ").append(configuration.getMencoderSubCp()).append(" ");
			}
		}

		if (!configuration.isMencoderDisableSubs() && !avisynth()) {
			if (configuration.getMencoderFont() != null && configuration.getMencoderFont().length() > 0) {
				sb.append("-subfont ").append(configuration.getMencoderFont()).append(" ");
			} else {
				String font = CodecUtil.getDefaultFontPath();
				if (StringUtils.isNotBlank(font)) {
					sb.append("-subfont ").append(font).append(" ");
				}
			}
			if (configuration.isMencoderAss() && !foundNoassParam) {
				if (!configuration.isMencoderAssDefaultStyle() || (subString != null && params.sid.getType() != DLNAMediaSubtitle.ASS)) {
					String assSubColor = "ffffff00";
					if (configuration.getSubsColor() != 0) {
						assSubColor = Integer.toHexString(configuration.getSubsColor());
						if (assSubColor.length() > 2) {
							assSubColor = assSubColor.substring(2) + "00";
						}
					}
					sb.append("-ass-color ").append(assSubColor).append(" -ass-border-color 00000000 -ass-font-scale ").append(configuration.getMencoderAssScale());
					sb.append(" -ass-force-style FontName=Arial,Outline=").append(configuration.getMencoderAssOutline()).append(",Shadow=").append(configuration.getMencoderAssShadow()).append(",MarginV=").append(configuration.getMencoderAssMargin()).append(" ");
				}
			} else {
				sb.append("-subfont-text-scale ").append(configuration.getMencoderNoAssScale());
				sb.append(" -subfont-outline ").append(configuration.getMencoderNoAssOutline());
				sb.append(" -subfont-blur ").append(configuration.getMencoderNoAssBlur());
				int subpos = 1;
				try {
					subpos = Integer.parseInt(configuration.getMencoderNoAssSubPos());
				} catch (NumberFormatException n) {
					logger.debug("Could not parse subpos from \"" + configuration.getMencoderNoAssSubPos() + "\"");
				}
				sb.append(" -subpos ").append(100 - subpos);
			}
		}

		st = new StringTokenizer(sb.toString(), " ");
		int oldc = overriddenMainArgs.length;
		overriddenMainArgs = Arrays.copyOf(overriddenMainArgs, overriddenMainArgs.length + st.countTokens());
		i = oldc;
		processToken = false;
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (processToken) {
				s = "-quiet";
				processToken = false;
			}
			if ((!configuration.isMencoderAss() || dvd) && s.contains("-ass")) {
				s = "-quiet";
				processToken = true;
			}
			overriddenMainArgs[i++] = s;
		}

		String cmdArray[] = new String[18 + args().length];

		cmdArray[0] = executable();

		// Choose which time to seek to
		cmdArray[1] = "-ss";
		if (params.timeseek > 0) {
			cmdArray[2] = "" + params.timeseek;
		} else {
			cmdArray[2] = "0";
		}

		cmdArray[3] = "-quiet";
		if (media != null && media.getDvdtrack() > 0) {
			cmdArray[3] = "-dvd-device";
		}
		if (avisynth && !fileName.toLowerCase().endsWith(".iso")) {
			File avsFile = FFMpegVideo.getAVSScript(fileName, params.sid, params.fromFrame, params.toFrame);
			cmdArray[4] = ProcessUtil.getShortFileNameIfWideChars(avsFile.getAbsolutePath());
		} else {
			cmdArray[4] = fileName;
			if (params.stdin != null) {
				cmdArray[4] = "-";
			}
		}
		cmdArray[5] = "-quiet";
		if (media != null && media.getDvdtrack() > 0) {
			cmdArray[5] = "dvd://" + media.getDvdtrack();
		}
		String arguments[] = args();
		for (i = 0; i < arguments.length; i++) {
			cmdArray[6 + i] = arguments[i];
			if (arguments[i].contains("format=mpeg2") && media.getAspect() != null && media.getValidAspect(true) != null) {
				cmdArray[6 + i] += ":vaspect=" + media.getValidAspect(true);
			}
		}

		cmdArray[cmdArray.length - 12] = "-quiet";
		cmdArray[cmdArray.length - 11] = "-quiet";
		cmdArray[cmdArray.length - 10] = "-quiet";
		cmdArray[cmdArray.length - 9] = "-quiet";
		if (!dts && !pcm && !avisynth() && params.aid != null && media.getAudioCodes().size() > 1) {
			cmdArray[cmdArray.length - 12] = "-aid";
			boolean lavf = false; // Need to add support for LAVF demuxing
			cmdArray[cmdArray.length - 11] = "" + (lavf ? params.aid.getId() + 1 : params.aid.getId());
		}

		if (subString == null && params.sid != null) {
			cmdArray[cmdArray.length - 10] = "-sid";
			cmdArray[cmdArray.length - 9] = "" + params.sid.getId();
		} else if (subString != null && !avisynth()) { // Trick necessary for mencoder to skip the internal embedded track ?
			cmdArray[cmdArray.length - 10] = "-sid";
			cmdArray[cmdArray.length - 9] = "100";
		} else if (subString == null) { // Trick necessary for MEncoder to not display the internal embedded track
			cmdArray[cmdArray.length - 10] = "-subdelay";
			cmdArray[cmdArray.length - 9] = "20000";
		}

		cmdArray[cmdArray.length - 8] = "-quiet";
		cmdArray[cmdArray.length - 7] = "-quiet";

		if (configuration.isMencoderForceFps() && !configuration.isFix25FPSAvMismatch()) {
			cmdArray[cmdArray.length - 8] = "-fps";
			cmdArray[cmdArray.length - 7] = "24000/1001";
		}
		cmdArray[cmdArray.length - 6] = "-ofps";
		cmdArray[cmdArray.length - 5] = "24000/1001";
		String frameRate = null;
		if (media != null) {
			frameRate = media.getValidFps(true);
		}
		if (frameRate != null) {
			cmdArray[cmdArray.length - 5] = frameRate;
			if (configuration.isMencoderForceFps()) {
				if (configuration.isFix25FPSAvMismatch()) {
					cmdArray[cmdArray.length - 8] = "-mc";
					cmdArray[cmdArray.length - 7] = "0.005";
					cmdArray[cmdArray.length - 5] = "25";
				} else {
					cmdArray[cmdArray.length - 7] = cmdArray[cmdArray.length - 5];
				}
			}
		}

		if (subString != null && !configuration.isMencoderDisableSubs() && !avisynth()) {
			if (params.sid.getType() == DLNAMediaSubtitle.VOBSUB) {
				cmdArray[cmdArray.length - 4] = "-vobsub";
				cmdArray[cmdArray.length - 3] = subString.substring(0, subString.length() - 4);
				cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
				cmdArray[cmdArray.length - 4] = "-slang";
				cmdArray[cmdArray.length - 3] = "" + params.sid.getLang();
			} else {
				cmdArray[cmdArray.length - 4] = "-sub";
				cmdArray[cmdArray.length - 3] = subString.replace(",", "\\,"); // commas in mencoder separates multiple subtitles files
				if (params.sid.isFileUtf8() && params.sid.getPlayableFile() != null) {
					cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 1);
					cmdArray[cmdArray.length - 3] = "-utf8";
				}
			}
		} else {
			cmdArray[cmdArray.length - 4] = "-quiet";
			cmdArray[cmdArray.length - 3] = "-quiet";
		}

		if (fileName.toLowerCase().endsWith(".evo")) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-psprobe";
			cmdArray[cmdArray.length - 3] = "10000";
		}

		if (needAssFixPTS) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-vf";
			cmdArray[cmdArray.length - 3] = "ass,fixpts";
		}

		boolean deinterlace = configuration.isMencoderYadif();
		// check if the media renderer supports this resolution
		boolean mediaRendererScaler = params.mediaRenderer.isVideoRescale() && media != null && (media.getWidth() > params.mediaRenderer.getMaxVideoWidth() || (media.getHeight() > params.mediaRenderer.getMaxVideoHeight()));
		// use scaler?
		boolean scaleBool = mediaRendererScaler || (configuration.isMencoderScaler() && (configuration.getMencoderScaleX() != 0 || configuration.getMencoderScaleY() != 0));
		if ((deinterlace || scaleBool) && !avisynth()) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-vf";
			String scalerString = "scale=" + (params.mediaRenderer.getMaxVideoWidth() > 0 ? params.mediaRenderer.getMaxVideoWidth() : configuration.getMencoderScaleX()) + ":" + (params.mediaRenderer.getMaxVideoHeight() > 0 ? params.mediaRenderer.getMaxVideoHeight() : configuration.getMencoderScaleY());
			if (deinterlace) {
				scalerString = "," + scalerString;
			}
			cmdArray[cmdArray.length - 3] = (deinterlace ? "yadif" : "") + (scaleBool ? scalerString : "");
		}

		if (configuration.getMencoderMT() && !avisynth && !dvd && !(media.getCodecV() != null && (media.getCodecV().equals("mpeg2video")))) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-lavdopts";
			cmdArray[cmdArray.length - 3] = "fast";
		}

		boolean noMC0NoSkip = false;
		if (media != null) {
			String sArgs[] = getSpecificCodecOptions(configuration.getCodecSpecificConfig(), media, params, fileName, subString, configuration.isMencoderIntelligentSync(), false);
			if (sArgs != null && sArgs.length > 0) {
				boolean vfConsumed = false;
				boolean afConsumed = false;
				for (int s = 0; s < sArgs.length; s++) {
					if (sArgs[s].equals("-noass")) {
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-ass"))
							{
								cmdArray[c] = "-quiet";
							}
						}
					} else if (sArgs[s].equals("-ofps")) {
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-ofps")) {
								cmdArray[c] = "-quiet";
								cmdArray[c + 1] = "-quiet";
								s++;
							}
						}
					} else if (sArgs[s].equals("-fps")) {
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-fps")) {
								cmdArray[c] = "-quiet";
								cmdArray[c + 1] = "-quiet";
								s++;
							}
						}
					} else if (sArgs[s].equals("-ovc")) {
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-ovc")) {
								cmdArray[c] = "-quiet";
								cmdArray[c + 1] = "-quiet";
								s++;
							}
						}
					} else if (sArgs[s].equals("-channels")) {
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-channels")) {
								cmdArray[c] = "-quiet";
								cmdArray[c + 1] = "-quiet";
								s++;
							}
						}
					} else if (sArgs[s].equals("-oac")) {
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-oac")) {
								cmdArray[c] = "-quiet";
								cmdArray[c + 1] = "-quiet";
								s++;
							}
						}
					} else if (sArgs[s].equals("-quality")) {
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-lavcopts")) {
								cmdArray[c + 1] = "autoaspect=1:vcodec=" + vcodec +
									":acodec=" + (configuration.isMencoderAc3Fixed() ? "ac3_fixed" : "ac3") +
									":abitrate=" + CodecUtil.getAC3Bitrate(configuration, params.aid) +
									":threads=" + configuration.getMencoderMaxThreads() + ":" + sArgs[s + 1];
								addMaximumBitrateConstraints(cmdArray[c + 1], media, cmdArray[c + 1], params.mediaRenderer, "");
								sArgs[s + 1] = "-quality";
								s++;
							}
						}
					} else if (sArgs[s].equals("-mpegopts")) {
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-mpegopts")) {
								cmdArray[c + 1] += ":" + sArgs[s + 1];
								sArgs[s + 1] = "-mpegopts";
								s++;
							}
						}
					} else if (sArgs[s].equals("-vf")) {
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-vf")) {
								cmdArray[c + 1] += "," + sArgs[s + 1];
								sArgs[s + 1] = "-vf";
								s++;
								vfConsumed = true;
							}
						}
					} else if (sArgs[s].equals("-af")) {
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-af")) {
								cmdArray[c + 1] += "," + sArgs[s + 1];
								sArgs[s + 1] = "-af";
								s++;
								afConsumed = true;
							}
						}
					} else if (sArgs[s].equals("-nosync")) {
						noMC0NoSkip = true;
					} else if (sArgs[s].equals("-mc")) {
						noMC0NoSkip = true;
					}
				}
				cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + sArgs.length);
				for (int s = 0; s < sArgs.length; s++) {
					if (sArgs[s].equals("-noass") || sArgs[s].equals("-nomux") || sArgs[s].equals("-mpegopts") || (sArgs[s].equals("-vf") & vfConsumed) || (sArgs[s].equals("-af") && afConsumed) || sArgs[s].equals("-quality") || sArgs[s].equals("-nosync") || sArgs[s].equals("-mt")) {
						cmdArray[cmdArray.length - sArgs.length - 2 + s] = "-quiet";
					} else {
						cmdArray[cmdArray.length - sArgs.length - 2 + s] = sArgs[s];
					}
				}
			}
		}

		if ((pcm || dts || mux) || (configuration.isMencoderNoOutOfSync() && !noMC0NoSkip)) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 3);
			cmdArray[cmdArray.length - 5] = "-mc";
			cmdArray[cmdArray.length - 4] = "0";
			cmdArray[cmdArray.length - 3] = "-noskip";
			if (configuration.isFix25FPSAvMismatch()) {
				cmdArray[cmdArray.length - 4] = "0.005";
				cmdArray[cmdArray.length - 3] = "-quiet";
			}
		}

		if (params.timeend > 0) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-endpos";
			cmdArray[cmdArray.length - 3] = "" + params.timeend;
		}

		String rate = "48000";
		if (params.mediaRenderer.isXBOX()) {
			rate = "44100";
		}

		// force srate -> cause ac3's mencoder doesn't like anything other than 48khz
		if (media != null && !pcm && !dts && !mux) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 4);
			cmdArray[cmdArray.length - 6] = "-af";
			cmdArray[cmdArray.length - 5] = "lavcresample=" + rate;
			cmdArray[cmdArray.length - 4] = "-srate";
			cmdArray[cmdArray.length - 3] = rate;
		}

		// add a -cache option for piped media (e.g. rar/zip file entries):
		// https://code.google.com/p/ps3mediaserver/issues/detail?id=911
		if (params.stdin != null) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-cache";
			cmdArray[cmdArray.length - 3] = "8192";
		}

		PipeProcess pipe = null;

		cmdArray[cmdArray.length - 2] = "-o";

		ProcessWrapperImpl pw = null;

		if (pcm || dts || mux) {
			boolean channels_filter_present = false;
			for (String s : cmdArray) {
				if (StringUtils.isNotBlank(s) && s.startsWith("channels")) {
					channels_filter_present = true;
					break;
				}
			}

			if (params.avidemux) {
				pipe = new PipeProcess("mencoder" + System.currentTimeMillis(), (pcm || dts || mux) ? null : params);
				params.input_pipes[0] = pipe;
				cmdArray[cmdArray.length - 1] = pipe.getInputPipe();

				if (pcm && !channels_filter_present) {
					cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
					cmdArray[cmdArray.length - 2] = "-af";
					cmdArray[cmdArray.length - 1] = CodecUtil.getMixerOutput(true, configuration.getAudioChannelCount());
				}

				pw = new ProcessWrapperImpl(cmdArray, params);

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
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				videoPipe.deleteLater();
				audioPipe.deleteLater();
			} else {
				// remove the -oac switch, otherwise too many video packets errors appears again
				for (int s = 0; s < cmdArray.length; s++) {
					if (cmdArray[s].equals("-oac")) {
						cmdArray[s] = "-nosound";
						cmdArray[s + 1] = "-nosound";
						break;
					}
				}

				pipe = new PipeProcess(System.currentTimeMillis() + "tsmuxerout.ts");

				TSMuxerVideo ts = new TSMuxerVideo(configuration);
				File f = new File(configuration.getTempFolder(), "pms-tsmuxer.meta");
				String cmd[] = new String[]{ts.executable(), f.getAbsolutePath(), pipe.getInputPipe()};
				pw = new ProcessWrapperImpl(cmd, params);

				PipeIPCProcess ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegvideo", System.currentTimeMillis() + "videoout", false, true);

				cmdArray[cmdArray.length - 1] = ffVideoPipe.getInputPipe();

				OutputParams ffparams = new OutputParams(configuration);
				ffparams.maxBufferSize = 1;
				ffparams.stdin = params.stdin;
				ProcessWrapperImpl ffVideo = new ProcessWrapperImpl(cmdArray, ffparams);

				ProcessWrapper ff_video_pipe_process = ffVideoPipe.getPipeProcess();
				pw.attachProcess(ff_video_pipe_process);
				ff_video_pipe_process.runInNewThread();
				ffVideoPipe.deleteLater();

				pw.attachProcess(ffVideo);
				ffVideo.runInNewThread();

				String aid = null;
				if (media != null && media.getAudioCodes().size() > 1 && params.aid != null) {
					aid = params.aid.getId() + "";
				}

				PipeIPCProcess ffAudioPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegaudio01", System.currentTimeMillis() + "audioout", false, true);
				StreamModifier sm = new StreamModifier();
				sm.setPcm(pcm);
				sm.setDtsembed(dts);
				sm.setNbchannels(sm.isDtsembed() ? 2 : CodecUtil.getRealChannelCount(configuration, params.aid));
				sm.setSampleFrequency(48000);
				sm.setBitspersample(16);
				String mixer = CodecUtil.getMixerOutput(!sm.isDtsembed(), sm.getNbchannels());

				// it seems the -really-quiet prevents mencoder to stop the pipe output after some time...
				// -mc 0.1 make the DTS-HD extraction works better with latest mencoder builds, and makes no impact on the regular DTS one
				String ffmpegLPCMextract[] = new String[]{
					executable(), 
					"-ss", "0",
					fileName,
					"-really-quiet",
					"-msglevel", "statusline=2",
					"-channels", "" + sm.getNbchannels(),
					"-ovc", "copy",
					"-of", "rawaudio",
					"-mc", dts ? "0.1" : "0",
					"-noskip",
					(aid == null) ? "-quiet" : "-aid", (aid == null) ? "-quiet" : aid,
					"-oac", sm.isDtsembed() ? "copy" : "pcm",
					(mixer != null && !channels_filter_present) ? "-af" : "-quiet", (mixer != null && !channels_filter_present) ? mixer : "-quiet",
					"-srate", "48000",
					"-o", ffAudioPipe.getInputPipe()
				};

				if (!params.mediaRenderer.isMuxDTSToMpeg()) // no need to use the PCM trick when media renderer supports DTS
				{
					ffAudioPipe.setModifier(sm);
				}

				if (media != null && media.getDvdtrack() > 0) {
					ffmpegLPCMextract[3] = "-dvd-device";
					ffmpegLPCMextract[4] = fileName;
					ffmpegLPCMextract[5] = "dvd://" + media.getDvdtrack();
				} else if (params.stdin != null) {
					ffmpegLPCMextract[3] = "-";
				}
				if (fileName.toLowerCase().endsWith(".evo")) {
					ffmpegLPCMextract[4] = "-psprobe";
					ffmpegLPCMextract[5] = "1000000";
				}

				if (params.timeseek > 0) {
					ffmpegLPCMextract[2] = "" + params.timeseek;
				}
				OutputParams ffaudioparams = new OutputParams(configuration);
				ffaudioparams.maxBufferSize = 1;
				ffaudioparams.stdin = params.stdin;
				ProcessWrapperImpl ffAudio = new ProcessWrapperImpl(ffmpegLPCMextract, ffaudioparams);

				params.stdin = null;

				PrintWriter pwMux = new PrintWriter(f);
				pwMux.println("MUXOPT --no-pcr-on-video-pid --no-asyncio --new-audio-pes --vbr --vbv-len=500");
				String videoType = "V_MPEG-2";
				if (params.no_videoencode && params.forceType != null) {
					videoType = params.forceType;
				}
				String fps = "";
				if (params.forceFps != null) {
					fps = "fps=" + params.forceFps + ", ";
				}
				String audioType = "A_LPCM";
				if (params.mediaRenderer.isMuxDTSToMpeg()) {
					audioType = "A_DTS";
				}
				if (params.lossyaudio) {
					audioType = "A_AC3";
				}
				pwMux.println(videoType + ", \"" + ffVideoPipe.getOutputPipe() + "\", " + fps + "level=4.1, insertSEI, contSPS, track=1");
				pwMux.println(audioType + ", \"" + ffAudioPipe.getOutputPipe() + "\", track=2");
				pwMux.close();

				ProcessWrapper pipe_process = pipe.getPipeProcess();
				pw.attachProcess(pipe_process);
				pipe_process.runInNewThread();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				pipe.deleteLater();
				params.input_pipes[0] = pipe;

				ProcessWrapper ff_pipe_process = ffAudioPipe.getPipeProcess();
				pw.attachProcess(ff_pipe_process);
				ff_pipe_process.runInNewThread();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				ffAudioPipe.deleteLater();
				pw.attachProcess(ffAudio);
				ffAudio.runInNewThread();

			}

		} else {
			boolean directpipe = Platform.isMac() || Platform.isFreeBSD();
			if (directpipe) {
				cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 3);
				cmdArray[cmdArray.length - 3] = "-really-quiet";
				cmdArray[cmdArray.length - 2] = "-msglevel";
				cmdArray[cmdArray.length - 1] = "statusline=2";
				cmdArray[cmdArray.length - 4] = "-";
				params.input_pipes = new PipeProcess[2];
			} else {
				pipe = new PipeProcess("mencoder" + System.currentTimeMillis(), (pcm || dts || mux) ? null : params);
				params.input_pipes[0] = pipe;
				cmdArray[cmdArray.length - 1] = pipe.getInputPipe();
			}

			cmdArray = finalizeTranscoderArgs(
				this,
				fileName,
				dlna,
				media,
				params,
				cmdArray
			);

			pw = new ProcessWrapperImpl(cmdArray, params);

			if (!directpipe) {
				ProcessWrapper mkfifo_process = pipe.getPipeProcess();
				pw.attachProcess(mkfifo_process);
				mkfifo_process.runInNewThread();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				pipe.deleteLater();
			}

		}

		pw.runInNewThread();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		return pw;
	}

	@Override
	public String mimeType() {
		return HTTPResource.VIDEO_TRANSCODE;
	}

	@Override
	public String name() {
		return "MEncoder";
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

	private String[] getSpecificCodecOptions(String codecParam, DLNAMediaInfo media, OutputParams params, String filename, String srtFileName, boolean enable, boolean verifyOnly) {

		StringBuilder sb = new StringBuilder();

		String codecs = enable ? DEFAULT_CODEC_CONF_SCRIPT : "";
		codecs += "\n" + codecParam;
		StringTokenizer stLines = new StringTokenizer(codecs, "\n");
		try {
			Interpreter interpreter = new Interpreter();
			interpreter.setStrictJava(true);
			ArrayList<String> types = CodecUtil.getPossibleCodecs();
			int rank = 1;
			if (types != null) {
				for (String type : types) {
					int r = rank++;
					interpreter.set("" + type, r);
					String secondaryType = "dummy";
					if ("matroska".equals(type)) {
						secondaryType = "mkv";
						interpreter.set(secondaryType, r);
					} else if ("rm".equals(type)) {
						secondaryType = "rmvb";
						interpreter.set(secondaryType, r);
					} else if ("mpeg2video".equals(type)) {
						secondaryType = "mpeg2";
						interpreter.set(secondaryType, r);
					} else if ("mpeg1video".equals(type)) {
						secondaryType = "mpeg1";
						interpreter.set(secondaryType, r);
					}
					if (media.getContainer() != null && (media.getContainer().equals(type) || media.getContainer().equals(secondaryType))) {
						interpreter.set("container", r);
					} else if (media.getCodecV() != null && (media.getCodecV().equals(type) || media.getCodecV().equals(secondaryType))) {
						interpreter.set("vcodec", r);
					} else if (params.aid != null && params.aid.getCodecA() != null && params.aid.getCodecA().equals(type)) {
						interpreter.set("acodec", r);
					}
				}
			} else {
				return null;
			}

			interpreter.set("filename", filename);
			interpreter.set("audio", params.aid != null);
			interpreter.set("subtitles", params.sid != null);
			interpreter.set("srtfile", srtFileName);
			if (params.aid != null) {
				interpreter.set("samplerate", params.aid.getSampleRate());
			}
			String framerate = media.getValidFps(false);
			try {
				if (framerate != null) {
					interpreter.set("framerate", Double.parseDouble(framerate));
				}
			} catch (NumberFormatException e) {
				logger.debug("Could not parse framerate from \"" + framerate + "\"");
			}
			interpreter.set("duration", media.getDurationInSeconds());
			if (params.aid != null) {
				interpreter.set("channels", params.aid.getNrAudioChannels());
			}
			interpreter.set("height", media.getHeight());
			interpreter.set("width", media.getWidth());
			while (stLines.hasMoreTokens()) {
				String line = stLines.nextToken();
				if (!line.startsWith("#") && line.trim().length() > 0) {
					int separator = line.indexOf("::");
					if (separator > -1) {
						String key = null;
						try {
							key = line.substring(0, separator).trim();
							String value = line.substring(separator + 2).trim();

							if (value.length() > 0) {
								if (key.length() == 0) {
									key = "1 == 1";
								}
								Object result = interpreter.eval(key);
								if (result != null && result instanceof Boolean && ((Boolean) result).booleanValue()) {
									sb.append(" ");
									sb.append(value);
								}
							}
						} catch (Throwable e) {
							logger.debug("Error while executing: " + key + " : " + e.getMessage());
							if (verifyOnly) {
								return new String[]{"@@Error while parsing: " + e.getMessage()};
							}
						}
					} else if (verifyOnly) {
						return new String[]{"@@Malformatted line: " + line};
					}
				}
			}
		} catch (EvalError e) {
			logger.debug("BeanShell error: " + e.getMessage());
		}
		String completeLine = sb.toString();

		ArrayList<String> args = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(completeLine, " ");
		while (st.hasMoreTokens()) {
			String arg = st.nextToken().trim();
			if (arg.length() > 0) {
				args.add(arg);
			}
		}

		String definitiveArgs[] = new String[args.size()];
		args.toArray(definitiveArgs);
		return definitiveArgs;
	}
}
