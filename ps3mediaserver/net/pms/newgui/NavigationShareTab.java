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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.util.KeyedComboBoxModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;

public class NavigationShareTab {
	public static final String ALL_DRIVES = Messages.getString("FoldTab.0");
	private JList FList;
	private DefaultListModel df;
	private JCheckBox hidevideosettings;
	private JCheckBox hidetranscode;
	private JCheckBox hidemedialibraryfolder;
	private JCheckBox hideextensions;
	private JCheckBox hideemptyfolders;
	private JCheckBox hideengines;
	private JButton but5;
	private JTextField seekpos;
	private JCheckBox tncheckBox;
	private JCheckBox mplayer_thumb;
	private JCheckBox dvdiso_thumb;
	private JCheckBox image_thumb;
	private JCheckBox cacheenable;
	private JCheckBox archive;
	private JComboBox sortmethod;
	private JComboBox audiothumbnail;
	private JTextField defaultThumbFolder;
	private JCheckBox iphoto;
	private JCheckBox aperture;
	private JCheckBox itunes;

	public DefaultListModel getDf() {
		return df;
	}
	private final PmsConfiguration configuration;

	NavigationShareTab(PmsConfiguration configuration) {
		this.configuration = configuration;
	}

	private void updateModel() {
		if (df.size() == 1 && df.getElementAt(0).equals(ALL_DRIVES)) {
		    configuration.setFolders("");
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < df.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				String entry = (String) df.getElementAt(i);
				// escape embedded commas. note: backslashing isn't safe as it conflicts with
				// Windows path separators:
				// http://ps3mediaserver.org/forum/viewtopic.php?f=14&t=8883&start=250#p43520
				sb.append(entry.replace(",", "&comma;"));
			}
			configuration.setFolders(sb.toString());
		}
	}

	public JComponent build() {
		FormLayout layout = new FormLayout(
			"left:pref, 50dlu, pref, 150dlu, pref, 25dlu, pref, 25dlu, pref, default:grow",
			"p, 3dlu,  p, 3dlu, p, 3dlu,  p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu,  p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, fill:default:grow");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.DLU4_BORDER);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();

		df = new DefaultListModel();
		File[] folders = PMS.get().getFoldersConf(false);
		if (folders != null && folders.length > 0) {
                    for (File file : folders) {
                        df.addElement(file.getAbsolutePath());
                    }
		} else {
			df.addElement(ALL_DRIVES);
		}
		FList = new JList();
		FList.setModel(df);
		JScrollPane pane = new JScrollPane(FList);

		JComponent cmp = builder.addSeparator(Messages.getString("FoldTab.13"), cc.xyw(1, 1, 10));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		tncheckBox = new JCheckBox(Messages.getString("NetworkTab.2"));
		tncheckBox.setContentAreaFilled(false);
		tncheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    configuration.setThumbnailsEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		if (configuration.getThumbnailsEnabled()) {
			tncheckBox.setSelected(true);
		}
		builder.add(tncheckBox, cc.xyw(1, 3, 3));

		seekpos = new JTextField("" + configuration.getThumbnailSeekPos());
		seekpos.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				try {
					int ab = Integer.parseInt(seekpos.getText());
					configuration.setThumbnailSeekPos(ab);
				} catch (NumberFormatException nfe) {
				}

			}
		});

		builder.addLabel(Messages.getString("NetworkTab.16"), cc.xyw(4, 3, 3));
		builder.add(seekpos, cc.xyw(6, 3, 2));

		mplayer_thumb = new JCheckBox(Messages.getString("FoldTab.14"));
		mplayer_thumb.setContentAreaFilled(false);
		mplayer_thumb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    configuration.setUseMplayerForVideoThumbs((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		if (configuration.isUseMplayerForVideoThumbs()) {
			mplayer_thumb.setSelected(true);
		}
		builder.add(mplayer_thumb, cc.xyw(1, 5, 3));

		dvdiso_thumb = new JCheckBox(Messages.getString("FoldTab.19"));
		dvdiso_thumb.setContentAreaFilled(false);
		dvdiso_thumb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    configuration.setDvdIsoThumbnails((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		if (configuration.isDvdIsoThumbnails()) {
			dvdiso_thumb.setSelected(true);
		}
		builder.add(dvdiso_thumb, cc.xyw(3, 5, 3));
		
		image_thumb = new JCheckBox(Messages.getString("FoldTab.21"));
		image_thumb.setContentAreaFilled(false);
		image_thumb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    configuration.setImageThumbnailsEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		if (configuration.getImageThumbnailsEnabled()) {
			image_thumb.setSelected(true);
		}
		builder.add(image_thumb, cc.xyw(1, 7, 3));

		final KeyedComboBoxModel thumbKCBM = new KeyedComboBoxModel(new Object[]{"0", "1", "2"}, new Object[]{Messages.getString("FoldTab.15"), Messages.getString("FoldTab.23"), Messages.getString("FoldTab.24")});
		audiothumbnail = new JComboBox(thumbKCBM);
		audiothumbnail.setEditable(false);

		thumbKCBM.setSelectedKey("" + configuration.getAudioThumbnailMethod());

		audiothumbnail.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {

					try {
						configuration.setAudioThumbnailMethod(Integer.parseInt((String) thumbKCBM.getSelectedKey()));
					} catch (NumberFormatException nfe) {
					}

				}
			}
		});
		builder.addLabel(Messages.getString("FoldTab.26"), cc.xyw(1, 9, 3));
		builder.add(audiothumbnail, cc.xyw(4, 9, 4));

		builder.addLabel(Messages.getString("FoldTab.27"), cc.xyw(1, 10, 3));
		defaultThumbFolder = new JTextField(configuration.getAlternateThumbFolder());
		defaultThumbFolder.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setAlternateThumbFolder(defaultThumbFolder.getText());
			}
		});
		builder.add(defaultThumbFolder, cc.xyw(4, 10, 3));

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
					defaultThumbFolder.setText(chooser.getSelectedFile().getAbsolutePath());
					configuration.setAlternateThumbFolder(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		builder.add(select, cc.xyw(7, 10, 1));

		cmp = builder.addSeparator(Messages.getString("NetworkTab.15"), cc.xyw(1, 11, 10));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		hidevideosettings = new JCheckBox(Messages.getString("FoldTab.6"));
		hidevideosettings.setContentAreaFilled(false);
		if (configuration.getHideVideoSettings()) {
			hidevideosettings.setSelected(true);
		}
		hidevideosettings.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			        configuration.setHideVideoSettings((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		hidetranscode = new JCheckBox(Messages.getString("FoldTab.33"));
		hidetranscode.setContentAreaFilled(false);
		if (configuration.getHideTranscodeEnabled()) {
			hidetranscode.setSelected(true);
		}
		hidetranscode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			        configuration.setHideTranscodeEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		hidemedialibraryfolder = new JCheckBox(Messages.getString("FoldTab.32"));
		hidemedialibraryfolder.setContentAreaFilled(false);
		if (configuration.isHideMediaLibraryFolder()) {
			hidemedialibraryfolder.setSelected(true);
		}
		hidemedialibraryfolder.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			        configuration.setHideMediaLibraryFolder((e.getStateChange() == ItemEvent.SELECTED));
			}
		});

		archive = new JCheckBox(Messages.getString("NetworkTab.1"));
		archive.setContentAreaFilled(false);
		archive.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			        configuration.setArchiveBrowsing(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		if (configuration.isArchiveBrowsing()) {
			archive.setSelected(true);
		}

		builder.add(archive, cc.xyw(1, 13, 3));

		final JButton cachereset = new JButton(Messages.getString("NetworkTab.18"));

		cacheenable = new JCheckBox(Messages.getString("NetworkTab.17"));
		cacheenable.setContentAreaFilled(false);
		cacheenable.setSelected(configuration.getUseCache());
		cacheenable.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			        configuration.setUseCache((e.getStateChange() == ItemEvent.SELECTED));
				cachereset.setEnabled(configuration.getUseCache());
				if ((LooksFrame) PMS.get().getFrame() != null) {
					((LooksFrame) PMS.get().getFrame()).getFt().setScanLibraryEnabled(configuration.getUseCache());
				}
			}
		});

		builder.add(cacheenable, cc.xy(1, 19));

		cachereset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int option = JOptionPane.showConfirmDialog(
					(Component) PMS.get().getFrame(),
					Messages.getString("NetworkTab.13") +
					Messages.getString("NetworkTab.19"),
					"Question",
					JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					PMS.get().getDatabase().init(true);
				}

			}
		});
		builder.add(cachereset, cc.xyw(4, 19, 4));

		cachereset.setEnabled(configuration.getUseCache());

		builder.add(hidevideosettings, cc.xyw(4, 13, 3));

		builder.add(hidetranscode, cc.xyw(8, 13, 3));

		builder.add(hidemedialibraryfolder, cc.xyw(8, 19, 3));

		hideextensions = new JCheckBox(Messages.getString("FoldTab.5"));
		hideextensions.setContentAreaFilled(false);
		if (configuration.isHideExtensions()) {
			hideextensions.setSelected(true);
		}
		hideextensions.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideExtensions((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(hideextensions, cc.xyw(1, 15, 3));

		hideengines = new JCheckBox(Messages.getString("FoldTab.8"));
		hideengines.setContentAreaFilled(false);
		if (configuration.isHideEngineNames()) {
			hideengines.setSelected(true);
		}
		hideengines.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideEngineNames((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(hideengines, cc.xyw(4, 15, 3));

		hideemptyfolders = new JCheckBox(Messages.getString("FoldTab.31"));
		hideemptyfolders.setContentAreaFilled(false);
		if (configuration.isHideEmptyFolders()) {
			hideemptyfolders.setSelected(true);
		}
		hideemptyfolders.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setHideEmptyFolders((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(hideemptyfolders, cc.xyw(8, 15, 3));

		itunes = new JCheckBox(Messages.getString("FoldTab.30"));
		itunes.setContentAreaFilled(false);
		if (configuration.getItunesEnabled()) {
			itunes.setSelected(true);
		}
		if (!(Platform.isMac() || Platform.isWindows())) {
			itunes.setEnabled(false);
		}
		itunes.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setItunesEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(itunes, cc.xyw(1, 17, 3));

		iphoto = new JCheckBox(Messages.getString("FoldTab.29"));
		iphoto.setContentAreaFilled(false);
		if (configuration.getIphotoEnabled()) {
			iphoto.setSelected(true);
		}
		if (!Platform.isMac()) {
			iphoto.setEnabled(false);
		}
		iphoto.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setIphotoEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(iphoto, cc.xyw(4, 17, 3));
		
		// Add Aperture selection
		aperture = new JCheckBox(Messages.getString("FoldTab.34"));
		aperture.setContentAreaFilled(false);
		if (configuration.getApertureEnabled()) {
			aperture.setSelected(true);
		}
		if (!Platform.isMac()) {
			aperture.setEnabled(false);
		}
		aperture.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setApertureEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		builder.add(aperture, cc.xyw(8, 17, 3));

		final KeyedComboBoxModel kcbm = new KeyedComboBoxModel(new Object[]{"0", "3", "1", "2"}, new Object[]{Messages.getString("FoldTab.15"), Messages.getString("FoldTab.20"), Messages.getString("FoldTab.16"), Messages.getString("FoldTab.17")});
		sortmethod = new JComboBox(kcbm);
		sortmethod.setEditable(false);

		kcbm.setSelectedKey("" + configuration.getSortMethod());

		sortmethod.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {

					try {
						configuration.setSortMethod(Integer.parseInt((String) kcbm.getSelectedKey()));
					} catch (NumberFormatException nfe) {
					}

				}
			}
		});

		builder.addLabel(Messages.getString("FoldTab.18"), cc.xyw(1, 21, 3));
		builder.add(sortmethod, cc.xyw(4, 21, 4));

		FormLayout layoutFolders = new FormLayout(
			"left:pref, left:pref, pref, pref, pref, 0:grow",
			"p, 3dlu, p, 3dlu, fill:default:grow");
		PanelBuilder builderFolder = new PanelBuilder(layoutFolders);
		builderFolder.setOpaque(true);


		cmp = builderFolder.addSeparator(Messages.getString("FoldTab.7"), cc.xyw(1, 1, 6));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		JButton but = new JButton(LooksFrame.readImageIcon("folder_new-32.png"));
		but.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JFileChooser chooser = null;
				try {
					chooser = new JFileChooser();
				} catch (Exception ee) {
					chooser = new JFileChooser(new RestrictedFileSystemView());
				}
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("FoldTab.9"));
				int returnVal = chooser.showOpenDialog((Component) e.getSource());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					((DefaultListModel) FList.getModel()).add(FList.getModel().getSize(), chooser.getSelectedFile().getAbsolutePath());
					if (FList.getModel().getElementAt(0).equals(ALL_DRIVES)) {
						((DefaultListModel) FList.getModel()).remove(0);
					}
					updateModel();
				}
			}
		});
		builderFolder.add(but, cc.xy(1, 3));
		JButton but2 = new JButton(LooksFrame.readImageIcon("button_cancel-32.png"));
		//but2.setBorder(BorderFactory.createEtchedBorder());
		but2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (FList.getSelectedIndex() > -1) {
					((DefaultListModel) FList.getModel()).remove(FList.getSelectedIndex());
					if (FList.getModel().getSize() == 0) {
						((DefaultListModel) FList.getModel()).add(0, ALL_DRIVES);
					}
					updateModel();
				}
			}
		});
		builderFolder.add(but2, cc.xy(2, 3));

		JButton but3 = new JButton(LooksFrame.readImageIcon("kdevelop_down-32.png"));
		but3.setToolTipText(Messages.getString("FoldTab.12"));
		// but3.setBorder(BorderFactory.createEmptyBorder());
		but3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel model = ((DefaultListModel) FList.getModel());
				for (int i = 0; i < model.size() - 1; i++) {
					if (FList.isSelectedIndex(i)) {
						String value = model.get(i).toString();
						model.set(i, model.get(i + 1));
						model.set(i + 1, value);
						FList.setSelectedIndex(i + 1);
						updateModel();
						break;
					}
				}
			}
		});

		builderFolder.add(but3, cc.xy(3, 3));
		JButton but4 = new JButton(LooksFrame.readImageIcon("up-32.png"));
		but4.setToolTipText(Messages.getString("FoldTab.12"));
		//  but4.setBorder(BorderFactory.createEmptyBorder());
		but4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel model = ((DefaultListModel) FList.getModel());
				for (int i = 1; i < model.size(); i++) {
					if (FList.isSelectedIndex(i)) {
						String value = model.get(i).toString();

						model.set(i, model.get(i - 1));
						model.set(i - 1, value);
						FList.setSelectedIndex(i - 1);
						updateModel();
						break;

					}
				}
			}
		});
		builderFolder.add(but4, cc.xy(4, 3));

		but5 = new JButton(LooksFrame.readImageIcon("search-32.png"));
		but5.setToolTipText(Messages.getString("FoldTab.2"));
		//but5.setBorder(BorderFactory.createEmptyBorder());
		but5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (configuration.getUseCache()) {
					if (!PMS.get().getDatabase().isScanLibraryRunning()) {
						int option = JOptionPane.showConfirmDialog(
							(Component) PMS.get().getFrame(),
							Messages.getString("FoldTab.3") +
							Messages.getString("FoldTab.4"),
							"Question",
							JOptionPane.YES_NO_OPTION);
						if (option == JOptionPane.YES_OPTION) {
							PMS.get().getDatabase().scanLibrary();
							but5.setIcon(LooksFrame.readImageIcon("viewmagfit-32.png"));
						}
					} else {
						int option = JOptionPane.showConfirmDialog(
							(Component) PMS.get().getFrame(),
							Messages.getString("FoldTab.10"),
							"Question",
							JOptionPane.YES_NO_OPTION);
						if (option == JOptionPane.YES_OPTION) {
							PMS.get().getDatabase().stopScanLibrary();
							PMS.get().getFrame().setStatusLine(null);
							but5.setIcon(LooksFrame.readImageIcon("search-32.png"));
						}
					}
				}
			}
		});

		builderFolder.add(but5, cc.xy(5, 3));
		but5.setEnabled(configuration.getUseCache());

		builderFolder.add(pane, cc.xyw(1, 5, 6));

		builder.add(builderFolder.getPanel(), cc.xyw(1, 25, 10));

		JPanel panel = builder.getPanel();
		JScrollPane scrollPane = new JScrollPane(
			panel,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}

	public void setScanLibraryEnabled(boolean enabled) {
		but5.setEnabled(enabled);
		but5.setIcon(LooksFrame.readImageIcon("search-32.png"));
	}
}
