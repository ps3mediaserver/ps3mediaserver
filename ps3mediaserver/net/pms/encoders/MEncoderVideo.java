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

import javax.swing.BorderFactory;
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
import net.pms.Messages;
import net.pms.network.HTTPResource;
import net.pms.newgui.FontFileFilter;
import net.pms.newgui.LooksFrame;
import net.pms.newgui.MyComboBoxModel;
import net.pms.newgui.RestrictedFileSystemView;
import net.pms.PMS;
import net.pms.util.CodecUtil;
import net.pms.util.ProcessUtil;

import org.apache.commons.lang.StringUtils;

import bsh.EvalError;
import bsh.Interpreter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import com.sun.jna.Platform;

public class MEncoderVideo extends Player {
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
	public static final String ID = "mencoder"; //$NON-NLS-1$
	protected boolean pcm;
	protected boolean mux;
	protected boolean ovccopy;
	protected boolean dvd;
	protected boolean oaccopy;
	protected boolean mpegts;
	protected boolean wmv;
	protected String overridenMainArgs[];
	//protected String defaultSubArgs [];

	public static final String DEFAULT_CODEC_CONF_SCRIPT =
		Messages.getString("MEncoderVideo.68") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.69") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.70") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.71") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.72") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.73") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.75") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.76") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.77") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.78") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.79") //$NON-NLS-1$
		+ "#\n" //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.80") //$NON-NLS-1$
		+ "container == iso :: -nosync\n" //$NON-NLS-1$
		+ "(container == avi || container == matroska) && vcodec == mpeg4 && acodec == mp3 :: -mc 0.1\n" //$NON-NLS-1$
		+ "container == flv :: -mc 0.1\n" //$NON-NLS-1$
		+ "container == mov :: -mc 0.1 -noass\n" //$NON-NLS-1$
		+ "container == rm  :: -mc 0.1\n" //$NON-NLS-1$
		+ "container == matroska && framerate == 29.97  :: -nomux -mc 0\n" //$NON-NLS-1$
		+ "\n" //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.87") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.88") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.89") //$NON-NLS-1$
		+ Messages.getString("MEncoderVideo.91"); //$NON-NLS-1$

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
			"left:pref, 3dlu, p:grow, 3dlu, right:p:grow, 3dlu, p:grow, 3dlu, right:p:grow,3dlu, p:grow, 3dlu, right:p:grow,3dlu, pref:grow", //$NON-NLS-1$
			"p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu,p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 9dlu, p, 2dlu, p, 2dlu, p , 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p"); //$NON-NLS-1$
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.EMPTY_BORDER);
		builder.setOpaque(false);

		CellConstraints cc = new CellConstraints();

		checkBox = new JCheckBox(Messages.getString("MEncoderVideo.0")); //$NON-NLS-1$
		checkBox.setContentAreaFilled(false);
		if (configuration.getSkipLoopFilterEnabled()) {
			checkBox.setSelected(true);
		}
		checkBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setSkipLoopFilterEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		JComponent cmp = builder.addSeparator(Messages.getString("MEncoderVideo.1"), cc.xyw(1, 1, 15)); //$NON-NLS-1$
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		mencodermt = new JCheckBox(Messages.getString("MEncoderVideo.35")); //$NON-NLS-1$
		mencodermt.setFont(mencodermt.getFont().deriveFont(Font.BOLD));
		mencodermt.setContentAreaFilled(false);
		if (configuration.getMencoderMT()) {
			mencodermt.setSelected(true);
		}
		mencodermt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				configuration.setMencoderMT(mencodermt.isSelected());
				if (configuration.getMencoderMT()) {
					JOptionPane.showMessageDialog(
						(JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
						Messages.getString("MEncoderVideo.31"), //$NON-NLS-1$
						"Information", //$NON-NLS-1$
						JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		mencodermt.setEnabled(Platform.isWindows() || Platform.isMac());

		builder.add(mencodermt, cc.xy(1, 3));
		builder.add(checkBox, cc.xyw(3, 3, 12));

		noskip = new JCheckBox(Messages.getString("MEncoderVideo.2")); //$NON-NLS-1$
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

		JButton button = new JButton(Messages.getString("MEncoderVideo.29")); //$NON-NLS-1$
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel codecPanel = new JPanel(new BorderLayout());
				final JTextArea textArea = new JTextArea();
				textArea.setText(configuration.getCodecSpecificConfig());
				textArea.setFont(new Font("Courier", Font.PLAIN, 12)); //$NON-NLS-1$
				JScrollPane scrollPane = new JScrollPane(textArea);
				scrollPane.setPreferredSize(new java.awt.Dimension(900, 100));

				final JTextArea textAreaDefault = new JTextArea();
				textAreaDefault.setText(DEFAULT_CODEC_CONF_SCRIPT);
				textAreaDefault.setBackground(Color.WHITE);
				textAreaDefault.setFont(new Font("Courier", Font.PLAIN, 12)); //$NON-NLS-1$
				textAreaDefault.setEditable(false);
				textAreaDefault.setEnabled(configuration.isMencoderIntelligentSync());
				JScrollPane scrollPaneDefault = new JScrollPane(textAreaDefault);
				scrollPaneDefault.setPreferredSize(new java.awt.Dimension(900, 450));

				JPanel customPanel = new JPanel(new BorderLayout());
				intelligentsync = new JCheckBox(Messages.getString("MEncoderVideo.3")); //$NON-NLS-1$
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
					DLNAMediaAudio audio = new DLNAMediaAudio();
					audio.codecA = "ac3"; //$NON-NLS-1$
					fakemedia.codecV = "mpeg4"; //$NON-NLS-1$
					fakemedia.container = "matroska"; //$NON-NLS-1$
					fakemedia.duration = "00:45:00"; //$NON-NLS-1$
					audio.nrAudioChannels = 2;
					fakemedia.width = 1280;
					fakemedia.height = 720;
					audio.sampleFrequency = "48000"; //$NON-NLS-1$
					fakemedia.frameRate = "23.976"; //$NON-NLS-1$
					fakemedia.audioCodes.add(audio);
					String result[] = getSpecificCodecOptions(newCodecparam, fakemedia, new OutputParams(configuration), "dummy.mpg", "dummy.srt", false, true); //$NON-NLS-1$ //$NON-NLS-2$
					if (result.length > 0 && result[0].startsWith("@@")) { //$NON-NLS-1$
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

		forcefps = new JCheckBox(Messages.getString("MEncoderVideo.4")); //$NON-NLS-1$
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

		yadif = new JCheckBox(Messages.getString("MEncoderVideo.26")); //$NON-NLS-1$
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

		scaler = new JCheckBox(Messages.getString("MEncoderVideo.27")); //$NON-NLS-1$
		scaler.setContentAreaFilled(false);
		scaler.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderScaler(e.getStateChange() == ItemEvent.SELECTED);
				scaleX.setEnabled(configuration.isMencoderScaler());
				scaleY.setEnabled(configuration.isMencoderScaler());
			}
		});

		builder.add(scaler, cc.xyw(3, 5, 7));

		builder.addLabel(Messages.getString("MEncoderVideo.28"), cc.xyw(10, 5, 3, CellConstraints.RIGHT, CellConstraints.CENTER)); //$NON-NLS-1$
		scaleX = new JTextField("" + configuration.getMencoderScaleX()); //$NON-NLS-1$
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
				}
			}
		});
		builder.add(scaleX, cc.xyw(13, 5, 3));

		builder.addLabel(Messages.getString("MEncoderVideo.30"), cc.xyw(10, 7, 3, CellConstraints.RIGHT, CellConstraints.CENTER)); //$NON-NLS-1$
		scaleY = new JTextField("" + configuration.getMencoderScaleY()); //$NON-NLS-1$
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



		videoremux = new JCheckBox("<html>" + Messages.getString("MEncoderVideo.38") + "</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		videoremux.setContentAreaFilled(false);
		videoremux.setFont(videoremux.getFont().deriveFont(Font.BOLD));
		if (configuration.isMencoderMuxWhenCompatible()) {
			videoremux.setSelected(true);
		}
		videoremux.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderMuxWhenCompatible((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		builder.add(videoremux, cc.xyw(1, 9, 13));

		cmp = builder.addSeparator(Messages.getString("MEncoderVideo.5"), cc.xyw(1, 19, 15)); //$NON-NLS-1$
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		builder.addLabel(Messages.getString("MEncoderVideo.6"), cc.xy(1, 21)); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.7"), cc.xyw(1, 23, 15)); //$NON-NLS-1$
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

		cmp = builder.addSeparator(Messages.getString("MEncoderVideo.8"), cc.xyw(1, 25, 15)); //$NON-NLS-1$
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		builder.addLabel(Messages.getString("MEncoderVideo.9"), cc.xy(1, 27)); //$NON-NLS-1$
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
		builder.add(defaultsubs, cc.xyw(3, 27, 8));

		builder.addLabel(Messages.getString("MEncoderVideo.10"), cc.xy(1, 29)); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.11"), cc.xy(1, 31)); //$NON-NLS-1$
		Object data[] = new Object[]{configuration.getMencoderSubCp(),
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
		builder.add(subcp, cc.xyw(3, 31, 7));

		fribidi = new JCheckBox("FriBiDi mode"); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.24"), cc.xy(1, 33)); //$NON-NLS-1$
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

		JButton fontselect = new JButton("..."); //$NON-NLS-1$
		fontselect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FontFileFilter());
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("MEncoderVideo.25")); //$NON-NLS-1$
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					defaultfont.setText(chooser.getSelectedFile().getAbsolutePath());
					configuration.setMencoderFont(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		builder.add(fontselect, cc.xyw(11, 33, 2));

		builder.addLabel(Messages.getString("MEncoderVideo.37"), cc.xyw(1, 35, 3)); //$NON-NLS-1$
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

		JButton select = new JButton("..."); //$NON-NLS-1$
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
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("FoldTab.28")); //$NON-NLS-1$
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					alternateSubFolder.setText(chooser.getSelectedFile().getAbsolutePath());
					configuration.setAlternateSubsFolder(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		builder.add(select, cc.xyw(11, 35, 2));

		builder.addLabel(Messages.getString("MEncoderVideo.12"), cc.xy(1, 39, CellConstraints.RIGHT, CellConstraints.CENTER)); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.13"), cc.xy(5, 39)); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.14"), cc.xy(9, 39)); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.15"), cc.xy(13, 39)); //$NON-NLS-1$
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


		builder.addLabel(Messages.getString("MEncoderVideo.16"), cc.xy(1, 41, CellConstraints.RIGHT, CellConstraints.CENTER)); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.17"), cc.xy(5, 41)); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.18"), cc.xy(9, 41)); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.19"), cc.xy(13, 41)); //$NON-NLS-1$
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

		builder.add(ass, cc.xy(1, 37));
		ass.setSelected(configuration.isMencoderAss());
		ass.getItemListeners()[0].itemStateChanged(null);

		fc = new JCheckBox(Messages.getString("MEncoderVideo.21")); //$NON-NLS-1$
		fc.setContentAreaFilled(false);
		fc.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderFontConfig(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		builder.add(fc, cc.xyw(3, 37, 5));
		fc.setSelected(configuration.isMencoderFontConfig());

		assdefaultstyle = new JCheckBox(Messages.getString("MEncoderVideo.36")); //$NON-NLS-1$
		assdefaultstyle.setContentAreaFilled(false);
		assdefaultstyle.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderAssDefaultStyle(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		builder.add(assdefaultstyle, cc.xyw(8, 37, 4));
		assdefaultstyle.setSelected(configuration.isMencoderAssDefaultStyle());

		subs = new JCheckBox(Messages.getString("MEncoderVideo.22")); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.92"), cc.xy(1, 45)); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.93"), cc.xyw(1, 47, 6)); //$NON-NLS-1$

		builder.addLabel(Messages.getString("MEncoderVideo.28"), cc.xy(1, 49, CellConstraints.RIGHT, CellConstraints.CENTER)); //$NON-NLS-1$
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

		builder.addLabel(Messages.getString("MEncoderVideo.30"), cc.xy(5, 49)); //$NON-NLS-1$
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
		subColor.setText("Subs color");
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
		return new String[]{"-quiet", "-oac", oaccopy ? "copy" : (pcm ? "pcm" : "lavc"), "-of", (wmv || mpegts) ? "lavf" : (pcm && avisynth()) ? "avi" : (((pcm || dts || mux) ? "rawvideo" : "mpeg")), (wmv || mpegts) ? "-lavfopts" : "-quiet", wmv ? "format=asf" : (mpegts ? "format=mpegts" : "-quiet"), "-mpegopts", "format=mpeg2:muxrate=500000:vbuf_size=1194:abuf_size=64", "-ovc", (mux || ovccopy) ? "copy" : "lavc"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$
	}

	@Override
	public String[] args() {
		String args[] = null;
		String defaultArgs[] = getDefaultArgs();
		if (overridenMainArgs != null) {
			args = new String[defaultArgs.length + overridenMainArgs.length];
			for (int i = 0; i < defaultArgs.length; i++) {
				args[i] = defaultArgs[i];
			}
			for (int i = 0; i < overridenMainArgs.length; i++) {
				if (overridenMainArgs[i].equals("-of") || overridenMainArgs[i].equals("-oac") || overridenMainArgs[i].equals("-ovc") || overridenMainArgs[i].equals("-mpegopts")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					PMS.minimal("MEncoder encoder settings: You cannot change Muxer, Muxer options, Video Codec or Audio Codec"); //$NON-NLS-1$
					overridenMainArgs[i] = "-quiet"; //$NON-NLS-1$
					if (i + 1 < overridenMainArgs.length) {
						overridenMainArgs[i + 1] = "-quiet"; //$NON-NLS-1$
					}
				}
				args[i + defaultArgs.length] = overridenMainArgs[i];
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
		if (bitrate.contains("(") && bitrate.contains(")")) { //$NON-NLS-1$ //$NON-NLS-2$
			bitrates[1] = Integer.parseInt(bitrate.substring(bitrate.indexOf("(") + 1, bitrate.indexOf(")"))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (bitrate.contains("(")) //$NON-NLS-1$
		{
			bitrate = bitrate.substring(0, bitrate.indexOf("(")).trim(); //$NON-NLS-1$
		}
		if (StringUtils.isBlank(bitrate)) {
			bitrate = "0"; //$NON-NLS-1$
		}
		bitrates[0] = (int) Double.parseDouble(bitrate);
		return bitrates;
	}

	private String addMaximumBitrateConstraints(String encodeSettings, DLNAMediaInfo media, String quality, RendererConfiguration mediaRenderer) {
		int defaultMaxBitrates[] = getVideoBitrateConfig(configuration.getMaximumBitrate());
		int rendererMaxBitrates[] = new int[2];
		if (mediaRenderer.getMaxVideoBitrate() != null) {
			rendererMaxBitrates = getVideoBitrateConfig(mediaRenderer.getMaxVideoBitrate());
		}
		if ((defaultMaxBitrates[0] == 0 && rendererMaxBitrates[0] > 0) || rendererMaxBitrates[0] < defaultMaxBitrates[0] && rendererMaxBitrates[0] > 0) {
			defaultMaxBitrates = rendererMaxBitrates;
		}
		if (defaultMaxBitrates[0] > 0 && !quality.contains("vrc_buf_size") && !quality.contains("vrc_maxrate") && !quality.contains("vbitrate")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			defaultMaxBitrates[0] = 1000 * defaultMaxBitrates[0];
			if (defaultMaxBitrates[0] > 60000) {
				defaultMaxBitrates[0] = 60000;
			}
			int bufSize = 1835;
			if (media.isHDVideo()) {
				bufSize = defaultMaxBitrates[0] / 3;
			}
			if (bufSize > 7000) {
				bufSize = 7000;
			}

			if (defaultMaxBitrates[1] > 0) {
				bufSize = defaultMaxBitrates[1] /** 1000*/
					;
			}

			if (mediaRenderer.isDefaultVBVSize() && rendererMaxBitrates[1] == 0) {
				bufSize = 1835;
			}

			//bufSize = 2000;
			encodeSettings += ":vrc_maxrate=" + defaultMaxBitrates[0] + ":vrc_buf_size=" + bufSize; //$NON-NLS-1$ //$NON-NLS-2$
		} /*else if (mediaRenderer != HTTPResource.PS3)
		encodeSettings += ":vrc_buf_size=1835"; //$NON-NLS-1$*/
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

		boolean avisynth = avisynth()/* || params.avisynth*/;

		setAudioAndSubs(fileName, media, params, configuration);

		String subString = null;
		if (params.sid != null && params.sid.getPlayableFile() != null) {
			subString = ProcessUtil.getShortFileNameIfWideChars(params.sid.getPlayableFile().getAbsolutePath());
		}

		InputFile newInput = new InputFile();
		newInput.filename = fileName;
		newInput.push = params.stdin;

		dvd = false;
		if (media != null && media.dvdtrack > 0) {
			dvd = true;
		}

		// don't honour "Switch to tsMuxeR..." if the resource is being streamed via an MEncoder entry in
		// the #--TRANSCODE--# folder
		boolean forceMencoder = !configuration.getHideTranscodeEnabled()
			&& dlna.noName 
			&& (dlna.getParent() instanceof FileTranscodeVirtualFolder);

		ovccopy = false;

		int intOCW = Integer.parseInt(configuration.getMencoderOverscanCompensationWidth());
		int intOCH = Integer.parseInt(configuration.getMencoderOverscanCompensationHeight());

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
				if (media.codecV.equals("h264")) { //$NON-NLS-1$
					params.forceType = "V_MPEG4/ISO/AVC"; //$NON-NLS-1$
				} else if (media.codecV.startsWith("mpeg2")) { //$NON-NLS-1$
					params.forceType = "V_MPEG-2"; //$NON-NLS-1$
				} else if (media.codecV.equals("vc1")) { //$NON-NLS-1$
					params.forceType = "V_MS/VFW/WVC1"; //$NON-NLS-1$
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

		String vcodec = "mpeg2video"; //$NON-NLS-1$

		wmv = false;
		if (params.mediaRenderer.isTranscodeToWMV()) {
			wmv = true;
			vcodec = "wmv2"; // http://wiki.megaframe.org/wiki/Ubuntu_XBOX_360#MEncoder not usable in streaming //$NON-NLS-1$
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

		String add = ""; //$NON-NLS-1$
		if (configuration.getMencoderDecode() == null || configuration.getMencoderDecode().indexOf("-lavdopts") == -1) { //$NON-NLS-1$
			add = " -lavdopts debug=0"; //$NON-NLS-1$
		}

		String alternativeCodec = "";//"-ac ffac3,ffdca, ";  //$NON-NLS-1$
		if (dvd) {
			alternativeCodec = ""; //$NON-NLS-1$
		}
		int channels = wmv ? 2 : configuration.getAudioChannelCount();
		if (media != null && params.aid != null) {
			channels = wmv ? 2 : CodecUtil.getRealChannelCount(configuration, params.aid);
		}
		PMS.debug("channels=" + channels);

		StringTokenizer st = new StringTokenizer(alternativeCodec + "-channels " + channels + " " + configuration.getMencoderDecode() + add, " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		overridenMainArgs = new String[st.countTokens()];
		int i = 0;
		boolean processToken = false;
		int nThreads = (dvd || fileName.toLowerCase().endsWith("dvr-ms")) ? //$NON-NLS-1$
			1 : configuration.getMencoderMaxThreads();
 
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			if (processToken) {
				token += ":threads=" + nThreads; //$NON-NLS-1$
				if (configuration.getSkipLoopFilterEnabled() && !avisynth()) {
					token += ":skiploopfilter=all"; //$NON-NLS-1$
				}
				processToken = false;
			}
			if (token.toLowerCase().contains("lavdopts")) { //$NON-NLS-1$
				processToken = true;
			}

			overridenMainArgs[i++] = token;
		}

		if (configuration.getMencoderMainSettings() != null) {
			String mainConfig = configuration.getMencoderMainSettings();
			if (params.mediaRenderer.getCustomMencoderQualitySettings() != null) {
				mainConfig = params.mediaRenderer.getCustomMencoderQualitySettings();
			}
			if (mainConfig.contains("/*")) // in case of //$NON-NLS-1$
			{
				mainConfig = mainConfig.substring(mainConfig.indexOf("/*")); //$NON-NLS-1$
			}

			// Ditlew - WDTV Live (+ other byte asking clients), CBR. This probably ought to be placed in addMaximumBitrateConstraints(..)
			int cbr_bitrate = params.mediaRenderer.getCBRVideoBitrate();
			String cbr_settings = (cbr_bitrate > 0) ? ":vrc_buf_size=1835:vrc_minrate=" + cbr_bitrate + ":vrc_maxrate=" + cbr_bitrate + ":vbitrate=" + cbr_bitrate : "";
			String encodeSettings = "-lavcopts autoaspect=1:vcodec=" + vcodec + //$NON-NLS-1$
				(wmv ? ":acodec=wmav2:abitrate=256" : (cbr_settings + ":acodec=" + (configuration.isMencoderAc3Fixed() ? "ac3_fixed" : "ac3") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				":abitrate=" + CodecUtil.getAC3Bitrate(configuration, params.aid))) + //$NON-NLS-1$
				":threads=" + (wmv ? 1 : configuration.getMencoderMaxThreads()) + ":" + mainConfig; //$NON-NLS-1$ //$NON-NLS-2$

			encodeSettings = addMaximumBitrateConstraints(encodeSettings, media, mainConfig, params.mediaRenderer);
			st = new StringTokenizer(encodeSettings, " "); //$NON-NLS-1$
			int oldc = overridenMainArgs.length;
			overridenMainArgs = Arrays.copyOf(overridenMainArgs, overridenMainArgs.length + st.countTokens());
			i = oldc;
			while (st.hasMoreTokens()) {
				overridenMainArgs[i++] = st.nextToken();
			}
		}

		boolean needAssFixPTS = false;

		StringBuilder sb = new StringBuilder();

		// Use ASS & Fontconfig flags (and therefore ASS font styles) for all subtitled files except vobsub, embedded, dvd and mp4 container with srt
		// Note: The MP4 container with SRT rule is a workaround for MEncoder r30369. If there is ever a later version of MEncoder that supports external srt subs we should use that. As of r32848 that isn't the case
		if (
			params.sid != null &&
			params.sid.type != DLNAMediaSubtitle.EMBEDDED &&
			params.sid.type != DLNAMediaSubtitle.VOBSUB &&
			!(params.sid.type == DLNAMediaSubtitle.SUBRIP && media.container.equals("mp4")) &&
			!configuration.isMencoderDisableSubs() &&
			configuration.isMencoderAss() &&
			!dvd &&
			!avisynth()
		) {
			sb.append("-ass -").append(configuration.isMencoderFontConfig() ? "" : "no").append("fontconfig "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			if (mpegts || wmv) {
				needAssFixPTS = Platform.isWindows(); // don't think the fixpts filter is in the mplayer trunk
			}
		}

		// Apply DVD/VOBsub subtitle quality
		if (params.sid != null && params.sid.type == DLNAMediaSubtitle.VOBSUB && configuration.getMencoderVobsubSubtitleQuality() != null) {
			String subtitleQuality = configuration.getMencoderVobsubSubtitleQuality();

			sb.append("-spuaa ").append(subtitleQuality).append(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		// Apply overscan compensation
		// -vf scale to original resolution seems to fix most cases of video warping
		// TODO: Integrate with Video Scaler option
		if (intOCW > 0 || intOCH > 0) {
			String scaleAppend = "";

			if (media != null && media.width > 0 && media.height > 0) {
				scaleAppend = ",scale="+media.width+":"+media.height;
			}

			sb.append("-vf softskip,expand=-").append(intOCW).append(":-").append(intOCH).append(scaleAppend).append(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		if (params.sid != null && !params.sid.is_file_utf8 && !configuration.isMencoderDisableSubs() && configuration.getMencoderSubCp() != null && configuration.getMencoderSubCp().length() > 0) {
			sb.append("-subcp ").append(configuration.getMencoderSubCp()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
			if (configuration.isMencoderSubFribidi()) {
				sb.append("-fribidi-charset ").append(configuration.getMencoderSubCp()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		if (!configuration.isMencoderDisableSubs() && !avisynth()) {
			if (configuration.getMencoderFont() != null && configuration.getMencoderFont().length() > 0) {
				sb.append("-subfont ").append(configuration.getMencoderFont()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				String font = CodecUtil.getDefaultFontPath();
				if (StringUtils.isNotBlank(font)) {
					sb.append("-subfont ").append(font).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (configuration.isMencoderAss()) {
				if (!configuration.isMencoderAssDefaultStyle() || (subString != null && params.sid.type != DLNAMediaSubtitle.ASS)) {
					String assSubColor = "ffffff00";
					if (configuration.getSubsColor() != 0) {
						assSubColor = Integer.toHexString(configuration.getSubsColor());
						if (assSubColor.length() > 2) {
							assSubColor = assSubColor.substring(2) + "00";
						}
					}
					sb.append("-ass-color ").append(assSubColor).append(" -ass-border-color 00000000 -ass-font-scale ").append(configuration.getMencoderAssScale()); //$NON-NLS-1$
					sb.append(" -ass-force-style FontName=Arial,Outline=").append(configuration.getMencoderAssOutline()).append(",Shadow=").append(configuration.getMencoderAssShadow()).append(",MarginV=").append(configuration.getMencoderAssMargin()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				}
			} else {
				sb.append("-subfont-text-scale ").append(configuration.getMencoderNoAssScale()); //$NON-NLS-1$
				sb.append(" -subfont-outline ").append(configuration.getMencoderNoAssOutline()); //$NON-NLS-1$
				sb.append(" -subfont-blur ").append(configuration.getMencoderNoAssBlur()); //$NON-NLS-1$
				int subpos = 1;
				try {
					subpos = Integer.parseInt(configuration.getMencoderNoAssSubPos());
				} catch (NumberFormatException n) {
				}
				sb.append(" -subpos ").append(100 - subpos); //$NON-NLS-1$
			}
		}

		st = new StringTokenizer(sb.toString(), " "); //$NON-NLS-1$
		int oldc = overridenMainArgs.length;
		overridenMainArgs = Arrays.copyOf(overridenMainArgs, overridenMainArgs.length + st.countTokens());
		i = oldc;
		processToken = false;
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (processToken) {
				s = "-quiet"; //$NON-NLS-1$
				processToken = false;
			}
			if ((!configuration.isMencoderAss() || dvd) && s.contains("-ass")) { //$NON-NLS-1$
				s = "-quiet"; //$NON-NLS-1$
				processToken = true;
			}
			overridenMainArgs[i++] = s;
		}

		String cmdArray[] = new String[18 + args().length];

		// Use the normal MEncoder build by default
		cmdArray[0] = executable();

		boolean isMultiCore = configuration.getNumberOfCpuCores() > 1;

		// Figure out which version of MEncoder we want to use
		if (
			(media.muxingMode != null && media.muxingMode.equals("Header stripping")) ||
			(params.sid != null && params.sid.type == DLNAMediaSubtitle.VOBSUB)
		) {
			// Use the newer version of MEncoder
			if (isMultiCore && configuration.getMencoderMT()) {
				if (new File(configuration.getMencoderAlternateMTPath()).exists()) {
					cmdArray[0] = configuration.getMencoderAlternateMTPath();
				}
			} else {
				if (new File(configuration.getMencoderAlternatePath()).exists()) {
					cmdArray[0] = configuration.getMencoderAlternatePath();
				}
			}
		} else if (isMultiCore && configuration.getMencoderMT()) {
			// Use the older MEncoder with multithreading
			if (new File(configuration.getMencoderMTPath()).exists()) {
				cmdArray[0] = configuration.getMencoderMTPath();
			}
		}

		// Choose which time to seek to
		cmdArray[1] = "-ss"; //$NON-NLS-1$
		if (params.timeseek > 0) {
			cmdArray[2] = "" + params.timeseek; //$NON-NLS-1$
		} else {
			cmdArray[2] = "0"; //$NON-NLS-1$
		}

		cmdArray[3] = "-quiet"; //$NON-NLS-1$
		if (media != null && media.dvdtrack > 0) {
			cmdArray[3] = "-dvd-device"; //$NON-NLS-1$
		}
		if (avisynth && !fileName.toLowerCase().endsWith(".iso")) { //$NON-NLS-1$
			File avsFile = FFMpegVideo.getAVSScript(fileName, params.sid, params.fromFrame, params.toFrame);
			cmdArray[4] = ProcessUtil.getShortFileNameIfWideChars(avsFile.getAbsolutePath());
		} else {
			cmdArray[4] = fileName;
			if (params.stdin != null) {
				cmdArray[4] = "-"; //$NON-NLS-1$
			}
		}
		cmdArray[5] = "-quiet"; //$NON-NLS-1$
		if (media != null && media.dvdtrack > 0) {
			cmdArray[5] = "dvd://" + media.dvdtrack; //$NON-NLS-1$
		}
		String arguments[] = args();
		for (i = 0; i < arguments.length; i++) {
			cmdArray[6 + i] = arguments[i];
			if (arguments[i].contains("format=mpeg2") && media.aspect != null && media.getValidAspect(true) != null) { //$NON-NLS-1$
				cmdArray[6 + i] += ":vaspect=" + media.getValidAspect(true); //$NON-NLS-1$
			}
		}

		cmdArray[cmdArray.length - 12] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length - 11] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length - 10] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length - 9] = "-quiet"; //$NON-NLS-1$
		if (!dts && !pcm && !avisynth() && params.aid != null && media.audioCodes.size() > 1) {
			cmdArray[cmdArray.length - 12] = "-aid"; //$NON-NLS-1$
			boolean lavf = false; // Need to add support for LAVF demuxing
			cmdArray[cmdArray.length - 11] = "" + (lavf ? params.aid.id + 1 : params.aid.id); //$NON-NLS-1$
		}

		if (subString == null && params.sid != null) {
			cmdArray[cmdArray.length - 10] = "-sid"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 9] = "" + params.sid.id; //$NON-NLS-1$
		} else if (subString != null && !avisynth()) { // Trick necessary for mencoder to skip the internal embedded track ?
			cmdArray[cmdArray.length - 10] = "-sid"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 9] = "100"; //$NON-NLS-1$
		} else if (subString == null) { // Trick necessary for mencoder to not display the internal embedded track
			cmdArray[cmdArray.length - 10] = "-subdelay"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 9] = "20000"; //$NON-NLS-1$
		}

		cmdArray[cmdArray.length - 8] = "-quiet"; //$NON-NLS-1$
		cmdArray[cmdArray.length - 7] = "-quiet"; //$NON-NLS-1$

		if (configuration.isMencoderForceFps() && !configuration.isFix25FPSAvMismatch()) {
			cmdArray[cmdArray.length - 8] = "-fps"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 7] = "24000/1001"; //$NON-NLS-1$
		}
		cmdArray[cmdArray.length - 6] = "-ofps"; //$NON-NLS-1$
		cmdArray[cmdArray.length - 5] = "24000/1001"; //$NON-NLS-1$
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
			if (params.sid.type == DLNAMediaSubtitle.VOBSUB) {
				cmdArray[cmdArray.length - 4] = "-vobsub";
				cmdArray[cmdArray.length - 3] = subString.substring(0, subString.length() - 4);
				cmdArray = Arrays.copyOf(cmdArray, cmdArray.length +2);
				cmdArray[cmdArray.length-4] = "-slang"; //$NON-NLS-1$
				cmdArray[cmdArray.length-3] = "" + params.sid.lang; //$NON-NLS-1$
			} else {
				cmdArray[cmdArray.length - 4] = "-sub"; //$NON-NLS-1$
				cmdArray[cmdArray.length - 3] = subString.replace(",", "\\,"); // commas in mencoder separates multiple subtitles files //$NON-NLS-1$ //$NON-NLS-2$
				if (params.sid.is_file_utf8 && params.sid.getPlayableFile() != null) {
					cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 1);
					cmdArray[cmdArray.length - 3] = "-utf8"; //$NON-NLS-1$
				}
			}
		} else {
			cmdArray[cmdArray.length - 4] = "-quiet"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 3] = "-quiet"; //$NON-NLS-1$
		}

		if (fileName.toLowerCase().endsWith(".evo")) { //$NON-NLS-1$
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-psprobe"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 3] = "10000"; //$NON-NLS-1$
		}

		if (needAssFixPTS) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-vf"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 3] = "ass,fixpts"; //$NON-NLS-1$
		}

		boolean deinterlace = configuration.isMencoderYadif();
		// check if the media renderer supports this resolution
		boolean mediaRendererScaler = params.mediaRenderer.isVideoRescale() && media != null && (media.width > params.mediaRenderer.getMaxVideoWidth() || (media.height > params.mediaRenderer.getMaxVideoHeight()));
		// use scaler?
		boolean scaleBool = mediaRendererScaler || (configuration.isMencoderScaler() && (configuration.getMencoderScaleX() != 0 || configuration.getMencoderScaleY() != 0));
		if ((deinterlace || scaleBool) && !avisynth()) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-vf"; //$NON-NLS-1$
			String scalerString = "scale=" + (params.mediaRenderer.getMaxVideoWidth() > 0 ? params.mediaRenderer.getMaxVideoWidth() : configuration.getMencoderScaleX()) + ":" + (params.mediaRenderer.getMaxVideoHeight() > 0 ? params.mediaRenderer.getMaxVideoHeight() : configuration.getMencoderScaleY()); //$NON-NLS-1$ //$NON-NLS-2$
			if (deinterlace) {
				scalerString = "," + scalerString; //$NON-NLS-1$
			}
			cmdArray[cmdArray.length - 3] = (deinterlace ? "yadif" : "") + (scaleBool ? scalerString : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}

		if (configuration.getMencoderMT() && !avisynth && !dvd && !(media.codecV != null && (media.codecV.equals("mpeg2video")))) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-lavdopts"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 3] = "fast"; //$NON-NLS-1$
		}

		boolean noMC0NoSkip = false;
		if (media != null) {
			String sArgs[] = getSpecificCodecOptions(configuration.getCodecSpecificConfig(), media, params, fileName, subString, configuration.isMencoderIntelligentSync(), false);
			if (sArgs != null && sArgs.length > 0) {
				boolean vfConsumed = false;
				boolean afConsumed = false;
				for (int s = 0; s < sArgs.length; s++) {
					if (sArgs[s].equals("-noass")) { //$NON-NLS-1$
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-ass")) //$NON-NLS-1$
							{
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
							}
						}
					} else if (sArgs[s].equals("-ofps")) { //$NON-NLS-1$
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-ofps")) {//$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
								cmdArray[c + 1] = "-quiet"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-fps")) { //$NON-NLS-1$
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-fps")) {//$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
								cmdArray[c + 1] = "-quiet"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-ovc")) { //$NON-NLS-1$
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-ovc")) {//$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
								cmdArray[c + 1] = "-quiet"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-channels")) { //$NON-NLS-1$
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-channels")) {//$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
								cmdArray[c + 1] = "-quiet"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-oac")) { //$NON-NLS-1$
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-oac")) {//$NON-NLS-1$
								cmdArray[c] = "-quiet"; //$NON-NLS-1$
								cmdArray[c + 1] = "-quiet"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-quality")) { //$NON-NLS-1$
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-lavcopts")) {//$NON-NLS-1$
								cmdArray[c + 1] = "autoaspect=1:vcodec=" + vcodec + //$NON-NLS-1$
									":acodec=" + (configuration.isMencoderAc3Fixed() ? "ac3_fixed" : "ac3") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
									":abitrate=" + CodecUtil.getAC3Bitrate(configuration, params.aid) + //$NON-NLS-1$
									":threads=" + configuration.getMencoderMaxThreads() + ":" + sArgs[s + 1]; //$NON-NLS-1$ //$NON-NLS-2$
								addMaximumBitrateConstraints(cmdArray[c + 1], media, cmdArray[c + 1], params.mediaRenderer);
								sArgs[s + 1] = "-quality"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-mpegopts")) { //$NON-NLS-1$
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-mpegopts")) {//$NON-NLS-1$
								cmdArray[c + 1] += ":" + sArgs[s + 1]; //$NON-NLS-1$
								sArgs[s + 1] = "-mpegopts"; //$NON-NLS-1$
								s++;
							}
						}
					} else if (sArgs[s].equals("-vf")) { //$NON-NLS-1$
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-vf")) {//$NON-NLS-1$
								cmdArray[c + 1] += "," + sArgs[s + 1]; //$NON-NLS-1$
								sArgs[s + 1] = "-vf"; //$NON-NLS-1$
								s++;
								vfConsumed = true;
							}
						}
					} else if (sArgs[s].equals("-af")) { //$NON-NLS-1$
						for (int c = 0; c < cmdArray.length; c++) {
							if (cmdArray[c] != null && cmdArray[c].equals("-af")) {//$NON-NLS-1$
								cmdArray[c + 1] += "," + sArgs[s + 1]; //$NON-NLS-1$
								sArgs[s + 1] = "-af"; //$NON-NLS-1$
								s++;
								afConsumed = true;
							}
						}
					} else if (sArgs[s].equals("-nosync")) { //$NON-NLS-1$
						noMC0NoSkip = true;
					} else if (sArgs[s].equals("-mc")) { //$NON-NLS-1$
						noMC0NoSkip = true;
					}
				}
				cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + sArgs.length);
				for (int s = 0; s < sArgs.length; s++) {
					if (sArgs[s].equals("-noass") || sArgs[s].equals("-nomux") || sArgs[s].equals("-mpegopts") || (sArgs[s].equals("-vf") & vfConsumed) || (sArgs[s].equals("-af") && afConsumed) || sArgs[s].equals("-quality") || sArgs[s].equals("-nosync") || sArgs[s].equals("-mt")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
						cmdArray[cmdArray.length - sArgs.length - 2 + s] = "-quiet"; //$NON-NLS-1$
					} else {
						cmdArray[cmdArray.length - sArgs.length - 2 + s] = sArgs[s];
					}
				}
			}
		}

		if ((pcm || dts || mux) || (configuration.isMencoderNoOutOfSync() && !noMC0NoSkip)) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 3);
			cmdArray[cmdArray.length - 5] = "-mc"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 4] = "0"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 3] = "-noskip"; //$NON-NLS-1$
			if (configuration.isFix25FPSAvMismatch()) {
				cmdArray[cmdArray.length - 4] = "0.005"; //$NON-NLS-1$
				cmdArray[cmdArray.length - 3] = "-quiet"; //$NON-NLS-1$
			}
		}

		if (params.timeend > 0) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-endpos"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 3] = "" + params.timeend; //$NON-NLS-1$
		}

		String rate = "48000";
		if (params.mediaRenderer.isXBOX()) {
			rate = "44100";
		}

		// force srate -> cause ac3's mencoder doesn't like anything other than 48khz
		if (media != null && !pcm && !dts && !mux) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 4);
			cmdArray[cmdArray.length - 6] = "-af"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 5] = "lavcresample=" + rate; //$NON-NLS-1$
			cmdArray[cmdArray.length - 4] = "-srate"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 3] = rate; //$NON-NLS-1$
		}

		// add a -cache option for piped media (e.g. rar/zip file entries):
		// https://code.google.com/p/ps3mediaserver/issues/detail?id=911
		if (params.stdin != null) {
			cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
			cmdArray[cmdArray.length - 4] = "-cache"; //$NON-NLS-1$
			cmdArray[cmdArray.length - 3] = "8192"; //$NON-NLS-1$
		}

		PipeProcess pipe = null;

		cmdArray[cmdArray.length - 2] = "-o"; //$NON-NLS-1$

		ProcessWrapperImpl pw = null;

		if (pcm || dts || mux) {

			boolean channels_filter_present = false;
			for (String s : cmdArray) {
				if (StringUtils.isNotBlank(s) && s.startsWith("channels")) { //$NON-NLS-1$
					channels_filter_present = true;
					break;
				}
			}

			if (params.avidemux) {

				pipe = new PipeProcess("mencoder" + System.currentTimeMillis(), (pcm || dts || mux) ? null : params); //$NON-NLS-1$
				params.input_pipes[0] = pipe;
				cmdArray[cmdArray.length - 1] = pipe.getInputPipe();

				if (pcm && !channels_filter_present) {
					cmdArray = Arrays.copyOf(cmdArray, cmdArray.length + 2);
					cmdArray[cmdArray.length - 2] = "-af"; //$NON-NLS-1$
					cmdArray[cmdArray.length - 1] = CodecUtil.getMixerOutput(true, configuration.getAudioChannelCount());
				}

				pw = new ProcessWrapperImpl(cmdArray, params);

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
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				videoPipe.deleteLater();
				audioPipe.deleteLater();

			} else {

				// remove the -oac switch, otherwise too many video packets errors appears again
				for (int s = 0; s < cmdArray.length; s++) {
					if (cmdArray[s].equals("-oac")) { //$NON-NLS-1$
						cmdArray[s] = "-nosound"; //$NON-NLS-1$
						cmdArray[s + 1] = "-nosound"; //$NON-NLS-1$
						break;
					}
				}

				pipe = new PipeProcess(System.currentTimeMillis() + "tsmuxerout.ts"); //$NON-NLS-1$

				TSMuxerVideo ts = new TSMuxerVideo(configuration);
				File f = new File(configuration.getTempFolder(), "pms-tsmuxer.meta"); //$NON-NLS-1$
				String cmd[] = new String[]{ts.executable(), f.getAbsolutePath(), pipe.getInputPipe()};
				pw = new ProcessWrapperImpl(cmd, params);

				PipeIPCProcess ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegvideo", System.currentTimeMillis() + "videoout", false, true); //$NON-NLS-1$ //$NON-NLS-2$

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
				if (media != null && media.audioCodes.size() > 1 && params.aid != null) {
					aid = params.aid.id + "";
				}

				PipeIPCProcess ffAudioPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegaudio01", System.currentTimeMillis() + "audioout", false, true); //$NON-NLS-1$ //$NON-NLS-2$
				StreamModifier sm = new StreamModifier();
				sm.setPcm(pcm);
				sm.setDtsembed(dts);
				sm.setNbchannels(sm.isDtsembed() ? 2 : CodecUtil.getRealChannelCount(configuration, params.aid));
				sm.setSampleFrequency(48000);
				sm.setBitspersample(16);
				String mixer = CodecUtil.getMixerOutput(!sm.isDtsembed(), sm.getNbchannels());
				// it seems the -really-quiet prevents mencoder to stop the pipe output after some time...
				// -mc 0.1 make the DTS-HD extraction works better with latest mencoder builds, and makes no impact on the regular DTS one
				String ffmpegLPCMextract[] = new String[]{configuration.getMencoderPath(), "-ss", "0", fileName, "-quiet", "-quiet", "-really-quiet", "-msglevel", "statusline=2", "-channels", "" + sm.getNbchannels(), "-ovc", "copy", "-of", "rawaudio", "-mc", dts ? "0.1" : "0", "-noskip", (aid == null) ? "-quiet" : "-aid", (aid == null) ? "-quiet" : aid, "-oac", sm.isDtsembed() ? "copy" : "pcm", (mixer != null && !channels_filter_present) ? "-af" : "-quiet", (mixer != null && !channels_filter_present) ? mixer : "-quiet", "-srate", "48000", "-o", ffAudioPipe.getInputPipe()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$ //$NON-NLS-19$ //$NON-NLS-20$ //$NON-NLS-21$ //$NON-NLS-22$ //$NON-NLS-23$ //$NON-NLS-24$ //$NON-NLS-25$
				if (!params.mediaRenderer.isMuxDTSToMpeg()) // no need to use the PCM trick when media renderer supports DTS
				{
					ffAudioPipe.setModifier(sm);
				}

				if (media != null && media.dvdtrack > 0) {
					ffmpegLPCMextract[3] = "-dvd-device"; //$NON-NLS-1$
					ffmpegLPCMextract[4] = fileName;
					ffmpegLPCMextract[5] = "dvd://" + media.dvdtrack; //$NON-NLS-1$
				} else if (params.stdin != null) {
					ffmpegLPCMextract[3] = "-"; //$NON-NLS-1$
				}
				if (fileName.toLowerCase().endsWith(".evo")) { //$NON-NLS-1$
					ffmpegLPCMextract[4] = "-psprobe"; //$NON-NLS-1$
					ffmpegLPCMextract[5] = "1000000"; //$NON-NLS-1$
				}

				if (params.timeseek > 0) {
					ffmpegLPCMextract[2] = "" + params.timeseek; //$NON-NLS-1$
				}
				OutputParams ffaudioparams = new OutputParams(configuration);
				ffaudioparams.maxBufferSize = 1;
				ffaudioparams.stdin = params.stdin;
				ProcessWrapperImpl ffAudio = new ProcessWrapperImpl(ffmpegLPCMextract, ffaudioparams);

				params.stdin = null;

				PrintWriter pwMux = new PrintWriter(f);
				pwMux.println("MUXOPT --no-pcr-on-video-pid --no-asyncio --new-audio-pes --vbr --vbv-len=500"); //$NON-NLS-1$
				String videoType = "V_MPEG-2"; //$NON-NLS-1$
				if (params.no_videoencode && params.forceType != null) {
					videoType = params.forceType;
				}
				String fps = ""; //$NON-NLS-1$
				if (params.forceFps != null) {
					fps = "fps=" + params.forceFps + ", "; //$NON-NLS-1$ //$NON-NLS-2$
				}
				String audioType = "A_LPCM"; //$NON-NLS-1$
				if (params.mediaRenderer.isMuxDTSToMpeg()) {
					audioType = "A_DTS"; //$NON-NLS-1$
				}
				if (params.lossyaudio) {
					audioType = "A_AC3"; //$NON-NLS-1$
				}
				pwMux.println(videoType + ", \"" + ffVideoPipe.getOutputPipe() + "\", " + fps + "level=4.1, insertSEI, contSPS, track=1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				pwMux.println(audioType + ", \"" + ffAudioPipe.getOutputPipe() + "\", track=2"); //$NON-NLS-1$ //$NON-NLS-2$
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
				cmdArray[cmdArray.length - 3] = "-really-quiet"; //$NON-NLS-1$
				cmdArray[cmdArray.length - 2] = "-msglevel"; //$NON-NLS-1$
				cmdArray[cmdArray.length - 1] = "statusline=2"; //$NON-NLS-1$
				cmdArray[cmdArray.length - 4] = "-"; //$NON-NLS-1$
				params.input_pipes = new PipeProcess[2];
			} else {
				pipe = new PipeProcess("mencoder" + System.currentTimeMillis(), (pcm || dts || mux) ? null : params); //$NON-NLS-1$
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
		return HTTPResource.VIDEO_TRANSCODE; //$NON-NLS-1$
	}

	@Override
	public String name() {
		return "MEncoder"; //$NON-NLS-1$
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

	private String[] getSpecificCodecOptions(String codecParam, DLNAMediaInfo media, OutputParams params, String filename, String srtFileName, boolean enable, boolean verifyOnly) {

		StringBuffer sb = new StringBuffer();

		String codecs = enable ? DEFAULT_CODEC_CONF_SCRIPT : ""; //$NON-NLS-1$
		codecs += "\n" + codecParam; //$NON-NLS-1$
		StringTokenizer stLines = new StringTokenizer(codecs, "\n"); //$NON-NLS-1$
		try {
			Interpreter interpreter = new Interpreter();
			interpreter.setStrictJava(true);
			ArrayList<String> types = CodecUtil.getPossibleCodecs();
			int rank = 1;
			if (types != null) {
				for (String type : types) {
					int r = rank++;
					interpreter.set("" + type, r); //$NON-NLS-1$
					String secondaryType = "dummy"; //$NON-NLS-1$
					if (type.equals("matroska")) { //$NON-NLS-1$
						secondaryType = "mkv";
						interpreter.set(secondaryType, r); //$NON-NLS-1$
					} else if (type.equals("rm")) { //$NON-NLS-1$
						secondaryType = "rmvb";
						interpreter.set(secondaryType, r); //$NON-NLS-1$
					} else if (type.equals("mpeg2video")) { //$NON-NLS-1$
						secondaryType = "mpeg2";
						interpreter.set(secondaryType, r); //$NON-NLS-1$
					} else if (type.equals("mpeg1video")) { //$NON-NLS-1$
						secondaryType = "mpeg1";
						interpreter.set(secondaryType, r); //$NON-NLS-1$
					}
					if (media.container != null && (media.container.equals(type) || media.container.equals(secondaryType))) {
						interpreter.set("container", r); //$NON-NLS-1$
					} else if (media.codecV != null && (media.codecV.equals(type) || media.codecV.equals(secondaryType))) {
						interpreter.set("vcodec", r); //$NON-NLS-1$
					} else if (params.aid != null && params.aid.codecA != null && params.aid.codecA.equals(type)) {
						interpreter.set("acodec", r); //$NON-NLS-1$
					}
				}
			} else {
				return null;
			}

			interpreter.set("filename", filename); //$NON-NLS-1$
			interpreter.set("audio", params.aid != null); //$NON-NLS-1$
			interpreter.set("subtitles", params.sid != null); //$NON-NLS-1$
			interpreter.set("srtfile", srtFileName); //$NON-NLS-1$
			if (params.aid != null) {
				interpreter.set("samplerate", params.aid.getSampleRate()); //$NON-NLS-1$
			}
			try {
				String framerate = media.getValidFps(false);
				if (framerate != null) {
					interpreter.set("framerate", Double.parseDouble(framerate)); //$NON-NLS-1$
				}
			} catch (NumberFormatException e) {
			}
			interpreter.set("duration", media.getDurationInSeconds()); //$NON-NLS-1$
			if (params.aid != null) {
				interpreter.set("channels", params.aid.nrAudioChannels); //$NON-NLS-1$
			}
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
							String value = line.substring(separator + 2).trim();

							if (value.length() > 0) {
								if (key.length() == 0) {
									key = "1 == 1"; //$NON-NLS-1$
								}
								Object result = interpreter.eval(key);
								if (result != null && result instanceof Boolean && ((Boolean) result).booleanValue()) {
									sb.append(" "); //$NON-NLS-1$
									sb.append(value);
								}
							}
						} catch (Throwable e) {
							PMS.info("Error while executing: " + key + " : " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
							if (verifyOnly) {
								return new String[]{"@@Error while parsing: " + e.getMessage()}; //$NON-NLS-1$
							}
						}
					} else if (verifyOnly) {
						return new String[]{"@@Malformatted line: " + line}; //$NON-NLS-1$
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
			if (arg.length() > 0) {
				args.add(arg);
			}
		}

		String definitiveArgs[] = new String[args.size()];
		args.toArray(definitiveArgs);
		return definitiveArgs;
	}
}
