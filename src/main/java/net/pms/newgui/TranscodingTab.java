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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.encoders.Player;
import net.pms.encoders.PlayerFactory;
import net.pms.util.FormLayoutUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Locale;

public class TranscodingTab {
	private static final Logger LOGGER = LoggerFactory.getLogger(TranscodingTab.class);
	private static final String COMMON_COL_SPEC = "$lcgap, left:pref, 2dlu, pref:grow, $lcgap";
	private static final String COMMON_ROW_SPEC = "5*(pref, 2dlu), pref, 18dlu, pref, 9dlu:grow, 2*(pref, 2dlu), default, 5*(pref, 2dlu), pref, 9dlu, 2*(pref, 2dlu), pref, 9dlu, 3*(pref, 2dlu), pref, $lgap, default";
	private static final String EMPTY_COL_SPEC = "left:pref, 2dlu, pref:grow";
	private static final String EMPTY_ROW_SPEC = "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p , 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 20dlu, p, 3dlu, p, 3dlu, p";
	private static final String LEFT_COL_SPEC = "left:pref, pref, pref, pref, 0:grow";
	private static final String LEFT_ROW_SPEC = "fill:10:grow, 3dlu, p, 3dlu, p, 3dlu, p";
	private static final String MAIN_COL_SPEC = "left:pref, pref, 7dlu, pref, pref, fill:10:grow";
	private static final String MAIN_ROW_SPEC = "fill:10:grow";

	private final PmsConfiguration configuration;
	private ComponentOrientation orientation;

	TranscodingTab(PmsConfiguration configuration) {
		this.configuration = configuration;
		// Apply the orientation for the locale
		Locale locale = new Locale(configuration.getLanguage());
		orientation = ComponentOrientation.getOrientation(locale);
	}
	private JCheckBox disableSubs;

	public JCheckBox getDisableSubs() {
		return disableSubs;
	}
	private JTextField forcetranscode;
	private JTextField notranscode;
	private JTextField maxbuffer;
	private JComboBox nbcores;
	private DefaultMutableTreeNode parent[];
	private JPanel tabbedPanel;
	private CardLayout cl;
	private JTextField abitrate;
	private JTree tree;
	private JCheckBox forcePCM;
	private JCheckBox forceDTSinPCM;
	private JComboBox channels;
	private JComboBox vq;
	private JCheckBox ac3remux;
	private JCheckBox mpeg2remux;
	private JCheckBox chapter_support;
	private JTextField chapter_interval;
	private JCheckBox videoHWacceleration;
	private JTextArea decodeTips;
	private JTextField langs;
	private JTextField defaultsubs;
	private JTextField forcedsub;
	private JTextField forcedtags;
	private JTextField defaultaudiosubs;
	private JComboBox subcp;
	private JCheckBox fribidi;
	private JTextField defaultfont;
	private JButton fontSelectButton;
	private JTextField alternateSubFolder;
	private JButton folderSelectButton;
	private JTextField mencoder_ass_scale;
	private JTextField mencoder_ass_outline;
	private JTextField mencoder_ass_shadow;
	private JTextField mencoder_ass_margin;
	private JTextField mencoder_noass_scale;
	private JTextField mencoder_noass_outline;
	private JTextField mencoder_noass_blur;
	private JTextField mencoder_noass_subpos;
	private JCheckBox ass;
	private JCheckBox fc;
	private JCheckBox assdefaultstyle;
	private JButton subColor;
	private JCheckBox subs;
	private JTextField subq;
	private JTextField ocw;
	private JTextField och;
	private static final int MAX_CORES = 32;

	private void updateEngineModel() {
		ArrayList<String> engines = new ArrayList<String>();
		Object root = tree.getModel().getRoot();
		for (int i = 0; i < tree.getModel().getChildCount(root); i++) {
			Object firstChild = tree.getModel().getChild(root, i);
			if (!tree.getModel().isLeaf(firstChild)) {
				for (int j = 0; j < tree.getModel().getChildCount(firstChild); j++) {
					Object secondChild = tree.getModel().getChild(firstChild, j);
					if (secondChild instanceof TreeNodeSettings) {
						TreeNodeSettings tns = (TreeNodeSettings) secondChild;
						if (tns.isEnable() && tns.getPlayer() != null) {
							engines.add(tns.getPlayer().id());
						}
					}
				}
			}
		}
		configuration.setEnginesAsList(engines);
	}

	private void handleCardComponentChange(Component component) {
		tabbedPanel.setPreferredSize(component.getPreferredSize());
		tabbedPanel.getParent().invalidate();
		tabbedPanel.getParent().validate();
		tabbedPanel.getParent().repaint();
	}

	public JComponent build() {
		String colSpec = FormLayoutUtil.getColSpec(MAIN_COL_SPEC, orientation);
		FormLayout mainlayout = new FormLayout(colSpec, MAIN_ROW_SPEC);
		PanelBuilder builder = new PanelBuilder(mainlayout);
		builder.setBorder(Borders.DLU4_BORDER);

		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		builder.add(buildRightTabbedPanel(), FormLayoutUtil.flip(cc.xyw(4, 1, 3), colSpec, orientation));
		builder.add(buildLeft(), FormLayoutUtil.flip(cc.xy(2, 1), colSpec, orientation));

		JPanel panel = builder.getPanel();
		
		// Apply the orientation to the panel and all components in it
		panel.applyComponentOrientation(orientation);

		return panel;
	}

	private JComponent buildRightTabbedPanel() {
		cl = new CardLayout();
		tabbedPanel = new JPanel(cl);
		tabbedPanel.setBorder(BorderFactory.createEmptyBorder());
		JScrollPane scrollPane = new JScrollPane(tabbedPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return scrollPane;
	}

	public JComponent buildLeft() {
		String colSpec = FormLayoutUtil.getColSpec(LEFT_COL_SPEC, orientation);
		FormLayout layout = new FormLayout(colSpec, LEFT_ROW_SPEC);
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.EMPTY_BORDER);
		builder.setOpaque(false);

		CellConstraints cc = new CellConstraints();

		JButton but = new JButton(LooksFrame.readImageIcon("kdevelop_down-32.png"));
		but.setToolTipText(Messages.getString("TrTab2.6"));
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreePath path = tree.getSelectionModel().getSelectionPath();
				if (path != null && path.getLastPathComponent() instanceof TreeNodeSettings) {
					TreeNodeSettings node = ((TreeNodeSettings) path.getLastPathComponent());
					if (node.getPlayer() != null) {
						DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();   // get the tree model
						//now get the index of the selected node in the DefaultTreeModel
						int index = dtm.getIndexOfChild(node.getParent(), node);
						// if selected node is first, return (can't move it up)
						if (index < node.getParent().getChildCount() - 1) {
							dtm.insertNodeInto(node, (DefaultMutableTreeNode) node.getParent(), index + 1);   // move the node
							dtm.reload();
							for (int i = 0; i < tree.getRowCount(); i++) {
								tree.expandRow(i);
							}
							tree.getSelectionModel().setSelectionPath(new TreePath(node.getPath()));
							updateEngineModel();
						}
					}
				}
			}
		});
		builder.add(but, FormLayoutUtil.flip(cc.xy(2, 3), colSpec, orientation));

		JButton but2 = new JButton(LooksFrame.readImageIcon("up-32.png"));
		but2.setToolTipText(Messages.getString("TrTab2.6"));
		but2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreePath path = tree.getSelectionModel().getSelectionPath();
				if (path != null && path.getLastPathComponent() instanceof TreeNodeSettings) {
					TreeNodeSettings node = ((TreeNodeSettings) path.getLastPathComponent());
					if (node.getPlayer() != null) {
						DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();   // get the tree model
						//now get the index of the selected node in the DefaultTreeModel
						int index = dtm.getIndexOfChild(node.getParent(), node);
						// if selected node is first, return (can't move it up)
						if (index != 0) {
							dtm.insertNodeInto(node, (DefaultMutableTreeNode) node.getParent(), index - 1);   // move the node
							dtm.reload();
							for (int i = 0; i < tree.getRowCount(); i++) {
								tree.expandRow(i);
							}
							tree.getSelectionModel().setSelectionPath(new TreePath(node.getPath()));
							updateEngineModel();
						}
					}
				}
			}
		});
		builder.add(but2, FormLayoutUtil.flip(cc.xy(3, 3), colSpec, orientation));

		JButton but3 = new JButton(LooksFrame.readImageIcon("connect_no-32.png"));
		but3.setToolTipText(Messages.getString("TrTab2.0"));
		but3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreePath path = tree.getSelectionModel().getSelectionPath();
				if (path != null && path.getLastPathComponent() instanceof TreeNodeSettings && ((TreeNodeSettings) path.getLastPathComponent()).getPlayer() != null) {
					((TreeNodeSettings) path.getLastPathComponent()).setEnable(!((TreeNodeSettings) path.getLastPathComponent()).isEnable());
					updateEngineModel();
					tree.updateUI();
				}
			}
		});
		builder.add(but3, FormLayoutUtil.flip(cc.xy(4, 3), colSpec, orientation));

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(Messages.getString("TrTab2.11"));
		TreeNodeSettings commonEnc = new TreeNodeSettings(Messages.getString("TrTab2.5"), null, buildCommon());
		commonEnc.getConfigPanel().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				handleCardComponentChange(e.getComponent());
			}
		});
		tabbedPanel.add(commonEnc.id(), commonEnc.getConfigPanel());
		root.add(commonEnc);

		parent = new DefaultMutableTreeNode[5];
		parent[0] = new DefaultMutableTreeNode(Messages.getString("TrTab2.14"));
		parent[1] = new DefaultMutableTreeNode(Messages.getString("TrTab2.15"));
		parent[2] = new DefaultMutableTreeNode(Messages.getString("TrTab2.16"));
		parent[3] = new DefaultMutableTreeNode(Messages.getString("TrTab2.17"));
		parent[4] = new DefaultMutableTreeNode(Messages.getString("TrTab2.18"));
		root.add(parent[0]);
		root.add(parent[1]);
		root.add(parent[2]);
		root.add(parent[3]);
		root.add(parent[4]);

		tree = new JTree(new DefaultTreeModel(root)) {
			private static final long serialVersionUID = -6703434752606636290L;
		};
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getNewLeadSelectionPath() != null && e.getNewLeadSelectionPath().getLastPathComponent() instanceof TreeNodeSettings) {
					TreeNodeSettings tns = (TreeNodeSettings) e.getNewLeadSelectionPath().getLastPathComponent();
					cl.show(tabbedPanel, tns.id());
				}
			}
		});

		tree.setCellRenderer(new TreeRenderer());
		JScrollPane pane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		builder.add(pane, FormLayoutUtil.flip(cc.xyw(2, 1, 4), colSpec, orientation));

		builder.addLabel(Messages.getString("TrTab2.19"), FormLayoutUtil.flip(cc.xyw(2, 5, 4), colSpec, orientation));
		builder.addLabel(Messages.getString("TrTab2.20"), FormLayoutUtil.flip(cc.xyw(2, 7, 4), colSpec, orientation));

		JPanel panel = builder.getPanel();

		// Apply the orientation to the panel and all components in it
		panel.applyComponentOrientation(orientation);

		return panel;
	}

	public void addEngines() {
		ArrayList<Player> disPlayers = new ArrayList<Player>();
		ArrayList<Player> ordPlayers = new ArrayList<Player>();
		PMS r = PMS.get();

		for (String id : configuration.getEnginesAsList(r.getRegistry())) {
			//boolean matched = false;
			for (Player p : PlayerFactory.getAllPlayers()) {
				if (p.id().equals(id)) {
					ordPlayers.add(p);
					//matched = true;
				}
			}
		}

		for (Player p : PlayerFactory.getAllPlayers()) {
			if (!ordPlayers.contains(p)) {
				ordPlayers.add(p);
				disPlayers.add(p);
			}
		}

		for (Player p : ordPlayers) {
			TreeNodeSettings en = new TreeNodeSettings(p.name(), p, null);
			if (disPlayers.contains(p)) {
				en.setEnable(false);
			}
			JComponent jc = en.getConfigPanel();
			if (jc == null) {
				jc = buildEmpty();
			}
			jc.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent e) {
					handleCardComponentChange(e.getComponent());
				}
			});
			tabbedPanel.add(en.id(), jc);
			parent[p.purpose()].add(en);
		}

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}


		tree.setSelectionRow(0);
	}

	public JComponent buildEmpty() {
		String colSpec = FormLayoutUtil.getColSpec(EMPTY_COL_SPEC, orientation);
		FormLayout layout = new FormLayout(colSpec, EMPTY_ROW_SPEC);
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.EMPTY_BORDER);
		builder.setOpaque(false);

		CellConstraints cc = new CellConstraints();

		builder.addSeparator(Messages.getString("TrTab2.1"), FormLayoutUtil.flip(cc.xyw(1, 1, 3), colSpec, orientation));

		JPanel panel = builder.getPanel();

		// Apply the orientation to the panel and all components in it
		panel.applyComponentOrientation(orientation);

		return panel;
	}

	public JComponent buildCommon() {
		String colSpec = FormLayoutUtil.getColSpec(COMMON_COL_SPEC, orientation);
		FormLayout layout = new FormLayout(colSpec, COMMON_ROW_SPEC);
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.EMPTY_BORDER);
		builder.setOpaque(false);

		CellConstraints cc = new CellConstraints();

		JComponent cmp = builder.addSeparator(Messages.getString("NetworkTab.5"), FormLayoutUtil.flip(cc.xyw(2, 1, 3), colSpec, orientation));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
		
		builder.addLabel(Messages.getString("NetworkTab.6").replaceAll("MAX_BUFFER_SIZE", configuration.getMaxMemoryBufferSizeStr()), FormLayoutUtil.flip(cc.xy(2, 3), colSpec, orientation));
		maxbuffer = new JTextField("" + configuration.getMaxMemoryBufferSize());
		maxbuffer.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					int ab = Integer.parseInt(maxbuffer.getText());
					configuration.setMaxMemoryBufferSize(ab);
				} catch (NumberFormatException nfe) {
					LOGGER.debug("Could not parse max memory buffer size from \"" + maxbuffer.getText() + "\"");
				}
			}
		});
		builder.add(maxbuffer, FormLayoutUtil.flip(cc.xy(4, 3), colSpec, orientation));
		
		builder.addLabel(Messages.getString("NetworkTab.7") + Runtime.getRuntime().availableProcessors() + ")", FormLayoutUtil.flip(cc.xy(2, 5), colSpec, orientation));

		String[] guiCores = new String[MAX_CORES];
		for (int i = 0; i < MAX_CORES; i++) {
			guiCores[i] = Integer.toString(i + 1);
		}
		nbcores = new JComboBox(guiCores);
		nbcores.setEditable(false);
		int nbConfCores = configuration.getNumberOfCpuCores();
		if (nbConfCores > 0 && nbConfCores <= MAX_CORES) {
			nbcores.setSelectedItem(Integer.toString(nbConfCores));
		} else {
			nbcores.setSelectedIndex(0);
		}

		nbcores.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			        configuration.setNumberOfCpuCores(Integer.parseInt(e.getItem().toString()));
			}
		});
		builder.add(nbcores, FormLayoutUtil.flip(cc.xy(4, 5), colSpec, orientation));

		chapter_support = new JCheckBox(Messages.getString("TrTab2.52"));
		chapter_support.setContentAreaFilled(false);
		chapter_support.setSelected(configuration.isChapterSupport());

		chapter_support.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setChapterSupport((e.getStateChange() == ItemEvent.SELECTED));
				chapter_interval.setEnabled(configuration.isChapterSupport());
			}
		});
		builder.add(chapter_support, FormLayoutUtil.flip(cc.xy(2, 7), colSpec, orientation));
		
		chapter_interval = new JTextField("" + configuration.getChapterInterval());
		chapter_interval.setEnabled(configuration.isChapterSupport());
		chapter_interval.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					int ab = Integer.parseInt(chapter_interval.getText());
					configuration.setChapterInterval(ab);
				} catch (NumberFormatException nfe) {
					LOGGER.debug("Could not parse chapter interval from \"" + chapter_interval.getText() + "\"");
				}
			}
		});
		builder.add(chapter_interval, FormLayoutUtil.flip(cc.xy(4, 7), colSpec, orientation));

		disableSubs = new JCheckBox(Messages.getString("TrTab2.51"),configuration.isDisableSubtitles());
		disableSubs.setContentAreaFilled(false);
 		disableSubs.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setDisableSubtitles((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(disableSubs, FormLayoutUtil.flip(cc.xy(2, 11), colSpec, orientation));

		JTabbedPane setupTabbedPanel = new JTabbedPane();
		setupTabbedPanel.addTab(Messages.getString("TrTab2.66"), buildVideoSetupPanel());
		setupTabbedPanel.addTab(Messages.getString("TrTab2.67"), buildAudioSetupPanel());
		setupTabbedPanel.addTab(Messages.getString("MEncoderVideo.8"), buildSubtitlesSetupPanel());
		
		builder.add(setupTabbedPanel, FormLayoutUtil.flip(cc.xywh(1, 14, 5, 31), colSpec, orientation));

		JPanel panel = builder.getPanel();
		panel.applyComponentOrientation(orientation);

		return panel;
	}
	
	private JComponent buildVideoSetupPanel() {
		String colSpec = FormLayoutUtil.getColSpec("$lcgap, left:pref, 2dlu, pref:grow, $lcgap", orientation);
		FormLayout layout = new FormLayout(colSpec, "$lgap, pref, 2dlu, pref, 6dlu, 2*(10dlu), 2*(pref, 2dlu), pref, 20dlu, 3*(pref, 2dlu), pref");
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		
		videoHWacceleration = new JCheckBox(Messages.getString("TrTab2.69"));
		videoHWacceleration.setSelected(configuration.isVideoHardwareAcceleration());
		videoHWacceleration.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setVideoHardwareAcceleration((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(videoHWacceleration, FormLayoutUtil.flip(cc.xy(2, 2), colSpec, orientation));
		videoHWacceleration.setEnabled(false); // TODO When any transcoder will implement it change to true or delete the line.

		builder.add(new JLabel(Messages.getString("TrTab2.32")), FormLayoutUtil.flip(cc.xy(2, 4), colSpec, orientation));

		Object data[] = new Object[] {
			configuration.getMencoderMainSettings(),                                                /* default */
			String.format("keyint=5:vqscale=1:vqmin=2  /* %s */", Messages.getString("TrTab2.60")), /* great */
			String.format("keyint=5:vqscale=1:vqmin=1  /* %s */", Messages.getString("TrTab2.61")), /* lossless */
			String.format("keyint=5:vqscale=2:vqmin=3  /* %s */", Messages.getString("TrTab2.62")), /* good (wired) */
			String.format("keyint=25:vqmax=5:vqmin=2  /* %s */",  Messages.getString("TrTab2.63")), /* good (wireless) */
			String.format("keyint=25:vqmax=7:vqmin=2  /* %s */",  Messages.getString("TrTab2.64")), /* medium (wireless) */
			String.format("keyint=25:vqmax=8:vqmin=3  /* %s */",  Messages.getString("TrTab2.65"))  /* low */
		};

		MyComboBoxModel cbm = new MyComboBoxModel(data);
		vq = new JComboBox(cbm);
		vq.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String s = (String) e.getItem();
					if (s.indexOf("/*") > -1) {
						s = s.substring(0, s.indexOf("/*")).trim();
					}
					configuration.setMencoderMainSettings(s);
				}
			}
		});
		vq.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				vq.getItemListeners()[0].itemStateChanged(new ItemEvent(vq, 0, vq.getEditor().getItem(), ItemEvent.SELECTED));
			}
		});
		vq.setEditable(true);
		builder.add(vq, FormLayoutUtil.flip(cc.xy(4, 4), colSpec, orientation));

		mpeg2remux = new JCheckBox(Messages.getString("MEncoderVideo.39") + (Platform.isWindows() ? Messages.getString("TrTab2.21") : ""));
		mpeg2remux.setContentAreaFilled(false);
		if (configuration.isMencoderRemuxMPEG2()) {
			mpeg2remux.setSelected(true);
		}
		mpeg2remux.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderRemuxMPEG2((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(mpeg2remux, FormLayoutUtil.flip(cc.xyw(2, 6, 3), colSpec, orientation));
		
		JComponent cmp = builder.addSeparator(Messages.getString("TrTab2.7"), FormLayoutUtil.flip(cc.xyw(2, 8, 3), colSpec, orientation));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		builder.add(new JLabel(Messages.getString("TrTab2.8")), FormLayoutUtil.flip(cc.xy(2, 10), colSpec, orientation));
		notranscode = new JTextField(configuration.getNoTranscode());
		notranscode.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setNoTranscode(notranscode.getText());
			}
		});
		builder.add(notranscode, FormLayoutUtil.flip(cc.xy(4, 10), colSpec, orientation));

		builder.addLabel(Messages.getString("TrTab2.9"), FormLayoutUtil.flip(cc.xy(2, 12), colSpec, orientation));
		forcetranscode = new JTextField(configuration.getForceTranscode());
		forcetranscode.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setForceTranscode(forcetranscode.getText());
			}
		});
		builder.add(forcetranscode, FormLayoutUtil.flip(cc.xy(4, 12), colSpec, orientation));

		String help1 = Messages.getString("TrTab2.39");
		help1 += Messages.getString("TrTab2.40");
		help1 += Messages.getString("TrTab2.41");
		help1 += Messages.getString("TrTab2.42");
		help1 += Messages.getString("TrTab2.43");
		help1 += Messages.getString("TrTab2.44");
		decodeTips = new JTextArea();
		decodeTips.setEditable(false);
		decodeTips.setBorder(BorderFactory.createEtchedBorder());
		decodeTips.setBackground(new Color(255, 255, 192));
		decodeTips.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(171, 173, 179)), BorderFactory.createEmptyBorder(3, 5, 3, 5)));
		decodeTips.setText(help1);
		builder.add(decodeTips, FormLayoutUtil.flip(cc.xywh(2, 14, 3, 1), colSpec, orientation));
				
		JPanel panel = builder.getPanel();
		panel.applyComponentOrientation(orientation);

		return panel;
	}
	
	private JComponent buildAudioSetupPanel() {
		String colSpec = FormLayoutUtil.getColSpec("$lcgap, left:pref, 2dlu, pref:grow, $lcgap", orientation);
		FormLayout layout = new FormLayout(colSpec, "$lgap, pref, 9dlu, 4*(pref, 2dlu), pref, 12dlu, 3*(pref, 2dlu), pref:grow");
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
	
		JComponent cmp = builder.addSeparator(Messages.getString("TrTab2.3"), FormLayoutUtil.flip(cc.xyw(2, 2, 3), colSpec, orientation));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
		
		builder.addLabel(Messages.getString("TrTab2.50"), FormLayoutUtil.flip(cc.xy(2, 4), colSpec, orientation));

		channels = new JComboBox(new Object[]{Messages.getString("TrTab2.55"),  Messages.getString("TrTab2.56") /*, "8 channels 7.1" */}); // 7.1 not supported by Mplayer :\
		channels.setEditable(false);
		if (configuration.getAudioChannelCount() == 2) {
			channels.setSelectedIndex(0);
		} else {
			channels.setSelectedIndex(1);
		}
		channels.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setAudioChannelCount(Integer.parseInt(e.getItem().toString().substring(0, 1)));
			}
		});
		builder.add(channels, FormLayoutUtil.flip(cc.xy(4, 4), colSpec, orientation));

		forcePCM = new JCheckBox(Messages.getString("TrTab2.27"));
		forcePCM.setContentAreaFilled(false);
		if (configuration.isMencoderUsePcm()) {
			forcePCM.setSelected(true);
		}
		forcePCM.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderUsePcm(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(forcePCM, FormLayoutUtil.flip(cc.xy(2, 6), colSpec, orientation));

		ac3remux = new JCheckBox(Messages.getString("MEncoderVideo.32") + (Platform.isWindows() ? Messages.getString("TrTab2.21") : ""));
		ac3remux.setContentAreaFilled(false);
		if (configuration.isRemuxAC3()) {
			ac3remux.setSelected(true);
		}
		ac3remux.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setRemuxAC3((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(ac3remux, FormLayoutUtil.flip(cc.xyw(2, 8, 3), colSpec, orientation));

		forceDTSinPCM = new JCheckBox(Messages.getString("TrTab2.28") + (Platform.isWindows() ? Messages.getString("TrTab2.21") : ""));
		forceDTSinPCM.setContentAreaFilled(false);
		if (configuration.isDTSEmbedInPCM()) {
			forceDTSinPCM.setSelected(true);
		}
		forceDTSinPCM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				configuration.setDTSEmbedInPCM(forceDTSinPCM.isSelected());
				if (configuration.isDTSEmbedInPCM()) {
					JOptionPane.showMessageDialog(
							(JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
							Messages.getString("TrTab2.10"),
							"Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		builder.add(forceDTSinPCM, FormLayoutUtil.flip(cc.xyw(2, 10, 3), colSpec, orientation));

		builder.addLabel(Messages.getString("TrTab2.29"), FormLayoutUtil.flip(cc.xy(2, 12), colSpec, orientation));
		abitrate = new JTextField("" + configuration.getAudioBitrate());
		abitrate.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					int ab = Integer.parseInt(abitrate.getText());
					configuration.setAudioBitrate(ab);
				} catch (NumberFormatException nfe) {
					LOGGER.debug("Could not parse audio bitrate from \"" + abitrate.getText() + "\"");
				}
			}
		});
		builder.add(abitrate, FormLayoutUtil.flip(cc.xy(4, 12), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.7"), FormLayoutUtil.flip(cc.xy(2, 14), colSpec, orientation));
		langs = new JTextField(configuration.getMencoderAudioLanguages());
		langs.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAudioLanguages(langs.getText());
			}
		});
		builder.add(langs, FormLayoutUtil.flip(cc.xy(4, 14), colSpec, orientation));

		JPanel panel = builder.getPanel();
		panel.applyComponentOrientation(orientation);

	return panel;
}
	
	private JComponent buildSubtitlesSetupPanel() {
		String colSpec = FormLayoutUtil.getColSpec("$lcgap, left:pref, 3dlu, pref:grow, 3dlu, right:pref:grow, 3dlu, pref:grow, 3dlu, right:pref:grow, 3dlu, pref:grow, 3dlu, right:pref:grow, 3dlu, pref:grow, $lcgap", orientation);
		FormLayout layout = new FormLayout(colSpec, "$lgap, 17*(pref, 2dlu), pref");
		final PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();

		builder.addLabel(Messages.getString("MEncoderVideo.9"), FormLayoutUtil.flip(cc.xy(2, 2), colSpec, orientation));
		defaultsubs = new JTextField(configuration.getMencoderSubLanguages());
		defaultsubs.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderSubLanguages(defaultsubs.getText());
			}
		});
		builder.add(defaultsubs, FormLayoutUtil.flip(cc.xywh(4, 2, 2, 1), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.94"), FormLayoutUtil.flip(cc.xy(6, 2), colSpec, orientation));
		forcedsub = new JTextField(configuration.getMencoderForcedSubLanguage());
		forcedsub.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderForcedSubLanguage(forcedsub.getText());
			}
		});
		builder.add(forcedsub, FormLayoutUtil.flip(cc.xy(8, 2), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.95"), FormLayoutUtil.flip(cc.xy(10, 2), colSpec, orientation));
		forcedtags = new JTextField(configuration.getMencoderForcedSubTags());
		forcedtags.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderForcedSubTags(forcedtags.getText());
			}
		});
		builder.add(forcedtags, FormLayoutUtil.flip(cc.xywh(12, 2, 5, 1), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.10"), FormLayoutUtil.flip(cc.xy(2, 4), colSpec, orientation));
		defaultaudiosubs = new JTextField(configuration.getMencoderAudioSubLanguages());
		defaultaudiosubs.addKeyListener(new KeyAdapter() {
			@Override
		public void keyReleased(KeyEvent e) {
				configuration.setMencoderAudioSubLanguages(defaultaudiosubs.getText());
			}
		});
		builder.add(defaultaudiosubs, FormLayoutUtil.flip(cc.xywh(4, 4, 8, 1), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.11"), FormLayoutUtil.flip(cc.xy(2, 6), colSpec, orientation));
		Object data[] = new Object[]{
			configuration.getMencoderSubCp(),
			Messages.getString("MEncoderVideo.129"),
			Messages.getString("MEncoderVideo.130"),
			Messages.getString("MEncoderVideo.131"),
			Messages.getString("MEncoderVideo.132"),
			Messages.getString("MEncoderVideo.96"),
			Messages.getString("MEncoderVideo.97"),
			Messages.getString("MEncoderVideo.98"),
			Messages.getString("MEncoderVideo.99"),
			Messages.getString("MEncoderVideo.100"),
			Messages.getString("MEncoderVideo.101"),
			Messages.getString("MEncoderVideo.102"),
			Messages.getString("MEncoderVideo.103"),
			Messages.getString("MEncoderVideo.104"),
			Messages.getString("MEncoderVideo.105"),
			Messages.getString("MEncoderVideo.106"),
			Messages.getString("MEncoderVideo.107"),
			Messages.getString("MEncoderVideo.108"),
			Messages.getString("MEncoderVideo.109"),
			Messages.getString("MEncoderVideo.110"),
			Messages.getString("MEncoderVideo.111"),
			Messages.getString("MEncoderVideo.112"),
			Messages.getString("MEncoderVideo.113"),
			Messages.getString("MEncoderVideo.114"),
			Messages.getString("MEncoderVideo.115"),
			Messages.getString("MEncoderVideo.116"),
			Messages.getString("MEncoderVideo.117"),
			Messages.getString("MEncoderVideo.118"),			
			Messages.getString("MEncoderVideo.119"),
			Messages.getString("MEncoderVideo.120"),
			Messages.getString("MEncoderVideo.121"),
			Messages.getString("MEncoderVideo.122"),
			Messages.getString("MEncoderVideo.123"),
			Messages.getString("MEncoderVideo.124")
		};

		MyComboBoxModel cbm = new MyComboBoxModel(data);
		subcp = new JComboBox(cbm);
		subcp.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String s = (String) e.getItem();
					int offset = s.indexOf("/*");
					
					if (offset > -1) {
						s = s.substring(0, offset).trim();
					}
					
					configuration.setMencoderSubCp(s);
				}
			}
		});
		subcp.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				subcp.getItemListeners()[0].itemStateChanged(new ItemEvent(subcp, 0, subcp.getEditor().getItem(), ItemEvent.SELECTED));
			}
		});

		subcp.setEditable(true);
		builder.add(subcp, FormLayoutUtil.flip(cc.xywh(4, 6, 7, 1), colSpec, orientation));

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
		builder.add(fribidi, FormLayoutUtil.flip(cc.xywh(12, 6, 4, 1), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.24"), FormLayoutUtil.flip(cc.xy(2, 8), colSpec, orientation));
		defaultfont = new JTextField(configuration.getMencoderFont());
		defaultfont.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderFont(defaultfont.getText());
			}
		});
		builder.add(defaultfont, FormLayoutUtil.flip(cc.xywh(4, 8, 8, 1), colSpec, orientation));

		fontSelectButton = new JButton("...");
		fontSelectButton.addActionListener(new ActionListener() {
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
		builder.add(fontSelectButton, FormLayoutUtil.flip(cc.xy(12, 8), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.37"), FormLayoutUtil.flip(cc.xy(2, 10), colSpec, orientation));
		alternateSubFolder = new JTextField(configuration.getAlternateSubsFolder());
		alternateSubFolder.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setAlternateSubsFolder(alternateSubFolder.getText());
			}
		});
		builder.add(alternateSubFolder, FormLayoutUtil.flip(cc.xywh(4, 10, 8, 1), colSpec, orientation));

		folderSelectButton = new JButton("...");
		folderSelectButton.addActionListener(new ActionListener() {
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
		builder.add(folderSelectButton, FormLayoutUtil.flip(cc.xy(12, 10), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.12"), FormLayoutUtil.flip(cc.xy(2, 12, CellConstraints.RIGHT, CellConstraints.CENTER), colSpec, orientation));
		mencoder_ass_scale = new JTextField(configuration.getMencoderAssScale());
		mencoder_ass_scale.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAssScale(mencoder_ass_scale.getText());
			}
		});
		builder.add(mencoder_ass_scale, FormLayoutUtil.flip(cc.xy(4, 12), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.13"), FormLayoutUtil.flip(cc.xy(6, 12), colSpec, orientation));
		mencoder_ass_outline = new JTextField(configuration.getMencoderAssOutline());
		mencoder_ass_outline.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAssOutline(mencoder_ass_outline.getText());
			}
		});
		builder.add(mencoder_ass_outline, FormLayoutUtil.flip(cc.xy(8, 12), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.14"), FormLayoutUtil.flip(cc.xy(10, 12), colSpec, orientation));
		mencoder_ass_shadow = new JTextField(configuration.getMencoderAssShadow());
		mencoder_ass_shadow.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAssShadow(mencoder_ass_shadow.getText());
			}
		});
		builder.add(mencoder_ass_shadow, FormLayoutUtil.flip(cc.xy(12, 12), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.15"), FormLayoutUtil.flip(cc.xy(14, 12), colSpec, orientation));
		mencoder_ass_margin = new JTextField(configuration.getMencoderAssMargin());
		mencoder_ass_margin.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderAssMargin(mencoder_ass_margin.getText());
			}
		});
		builder.add(mencoder_ass_margin, FormLayoutUtil.flip(cc.xy(16, 12), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.16"), FormLayoutUtil.flip(cc.xy(2, 14, CellConstraints.RIGHT, CellConstraints.CENTER), colSpec, orientation));
		mencoder_noass_scale = new JTextField(configuration.getMencoderNoAssScale());
		mencoder_noass_scale.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderNoAssScale(mencoder_noass_scale.getText());
			}
		});
		builder.add(mencoder_noass_scale, FormLayoutUtil.flip(cc.xy(4, 14), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.17"), FormLayoutUtil.flip(cc.xy(6, 14), colSpec, orientation));
		mencoder_noass_outline = new JTextField(configuration.getMencoderNoAssOutline());
		mencoder_noass_outline.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderNoAssOutline(mencoder_noass_outline.getText());
			}
		});
		builder.add(mencoder_noass_outline, FormLayoutUtil.flip(cc.xy(8, 14), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.18"), FormLayoutUtil.flip(cc.xy(10, 14), colSpec, orientation));
		mencoder_noass_blur = new JTextField(configuration.getMencoderNoAssBlur());
		mencoder_noass_blur.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderNoAssBlur(mencoder_noass_blur.getText());
			}
		});
		builder.add(mencoder_noass_blur,  FormLayoutUtil.flip(cc.xy(12, 14), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.19"), FormLayoutUtil.flip(cc.xy(14, 14), colSpec, orientation));
		mencoder_noass_subpos = new JTextField(configuration.getMencoderNoAssSubPos());
		mencoder_noass_subpos.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderNoAssSubPos(mencoder_noass_subpos.getText());
			}
		});
		builder.add(mencoder_noass_subpos, FormLayoutUtil.flip(cc.xy(16, 14), colSpec, orientation));

		ass = new JCheckBox(Messages.getString("MEncoderVideo.20"));
		ass.setContentAreaFilled(false);
		ass.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e != null) {
					configuration.setMencoderAss(e.getStateChange() == ItemEvent.SELECTED);
				}
			}
		});
		builder.add(ass, FormLayoutUtil.flip(cc.xy(2, 16), colSpec, orientation));
		ass.setSelected(configuration.isMencoderAss());
		ass.getItemListeners()[0].itemStateChanged(null);

		fc = new JCheckBox(Messages.getString("MEncoderVideo.21"));
		fc.setContentAreaFilled(false);
		fc.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderFontConfig(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(fc, FormLayoutUtil.flip(cc.xyw(4, 16, 5), colSpec, orientation));

		assdefaultstyle = new JCheckBox(Messages.getString("MEncoderVideo.36"));
		assdefaultstyle.setContentAreaFilled(false);
		assdefaultstyle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setMencoderAssDefaultStyle(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(assdefaultstyle, FormLayoutUtil.flip(cc.xyw(9, 16, 4), colSpec, orientation));
		assdefaultstyle.setSelected(configuration.isMencoderAssDefaultStyle());

		subColor = new JButton();
		subColor.setText(Messages.getString("MEncoderVideo.31"));
		subColor.setBackground(new Color(configuration.getSubsColor()));
		subColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(
						SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame()),
					Messages.getString("MEncoderVideo.125"),
					subColor.getBackground()
				);

				if (newColor != null) {
					subColor.setBackground(newColor);
					configuration.setSubsColor(newColor.getRGB());
				}
			}
		});
		builder.add(subColor, FormLayoutUtil.flip(cc.xyw(13, 16, 4), colSpec, orientation));

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
		builder.add(subs, FormLayoutUtil.flip(cc.xyw(2, 22, 15), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.92"), FormLayoutUtil.flip(cc.xy(2, 24), colSpec, orientation));
		subq = new JTextField(configuration.getMencoderVobsubSubtitleQuality());
		subq.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderVobsubSubtitleQuality(subq.getText());
			}
		});
		builder.add(subq, FormLayoutUtil.flip(cc.xy(4, 24), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.93"), cc.xyw(2, 26, 6));

		builder.addLabel(Messages.getString("MEncoderVideo.28") + "% ", FormLayoutUtil.flip(cc.xy(2, 28, CellConstraints.RIGHT, CellConstraints.CENTER), colSpec, orientation));
		ocw = new JTextField(configuration.getMencoderOverscanCompensationWidth());
		ocw.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderOverscanCompensationWidth(ocw.getText());
			}
		});
		builder.add(ocw, FormLayoutUtil.flip(cc.xy(4, 28), colSpec, orientation));

		builder.addLabel(Messages.getString("MEncoderVideo.30") + "% ", FormLayoutUtil.flip(cc.xy(6, 28), colSpec, orientation));
		och = new JTextField(configuration.getMencoderOverscanCompensationHeight());
		och.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setMencoderOverscanCompensationHeight(och.getText());
			}
		});
		builder.add(och, FormLayoutUtil.flip(cc.xy(8, 28), colSpec, orientation));
		
		final JPanel panel = builder.getPanel();
		
		boolean enable = !configuration.isDisableSubtitles();
	    for (Component component : panel.getComponents()) {
	    	component.setEnabled(enable);
	    }

		disableSubs.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean disable = e.getStateChange() != ItemEvent.SELECTED;
			    for (Component component : panel.getComponents()) {
			    	component.setEnabled(disable);
			    }
			}
		});
		
		panel.applyComponentOrientation(orientation);

		return panel;
	}
}
