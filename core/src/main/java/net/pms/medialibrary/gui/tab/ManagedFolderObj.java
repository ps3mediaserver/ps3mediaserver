/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
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
package net.pms.medialibrary.gui.tab;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.exceptions.InitialisationException;
import net.pms.medialibrary.gui.shared.EButton;
import net.pms.medialibrary.library.LibraryManager;

public class ManagedFolderObj {
	private List<ActionListener> removeListeners = new ArrayList<ActionListener>();
	private JCheckBox            cbWatch;
	private JTextField           tfFolderPath;
	private JCheckBox            cbVideo;
	private JCheckBox            cbAudio;
	private JCheckBox            cbPictures;
	private JButton              bBrowse;
	private JButton              bScan;
	private JButton              bDelete;
	private JCheckBox            cbSubFolders;
	private EButton            	 bConfigureFileImportTemplate;
	private int                  index;
	private JCheckBox 			 cbEnablePlugins;

	public ManagedFolderObj(JCheckBox cbWatch, JTextField tfFolderPath, JCheckBox cbVideo, EButton bConfigureFileImportTemplate, JCheckBox cbAudio, JCheckBox cbPictures, JButton bBrowse, JButton bScan,
	        JButton bDelete, JCheckBox cbSubFolders, JCheckBox cbEnablePlugins, int index) {
		setCbWatch(cbWatch);
		setTfFolderPath(tfFolderPath);
		setCbVideo(cbVideo);
		setbConfigureVideo(bConfigureFileImportTemplate);
		setCbAudio(cbAudio);
		setCbPictures(cbPictures);
		setbBrowse(bBrowse);
		setbScan(bScan);
		setbDelete(bDelete);
		setCbSubFolders(cbSubFolders);
		setIndex(index);
		setCbEnablePlugins(cbEnablePlugins);
	}

	@Override
	public String toString() {
		return String.format("File=%s, Watch=%s, Subfolders=%s, Video=%s, Audio=%s, Pictures=%s", tfFolderPath.getText(), cbWatch.isSelected(), cbSubFolders
		        .isSelected(), cbVideo.isSelected(), cbAudio.isSelected(), cbPictures.isSelected());
	}

	public void addRemoveListener(ActionListener l) {
		removeListeners.add(l);
	}

	public void setCbWatch(JCheckBox cbWatch) {
		this.cbWatch = cbWatch;
	}

	public JCheckBox getCbWatch() {
		return cbWatch;
	}

	public void setTfFolderPath(JTextField folderPath) {
		this.tfFolderPath = folderPath;
		if (folderPath != null) {
			folderPath.addCaretListener(new CaretListener() {

				@Override
				public void caretUpdate(CaretEvent e) {
					updateTfFolderPathToolTip();
				}
			});

			folderPath.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					updateTfFolderPathToolTip();
				}
			});
		}
	}

	public DOManagedFile getManagedFolder() {
		return new DOManagedFile(cbWatch.isSelected(), tfFolderPath.getText(), cbVideo.isSelected(), cbAudio.isSelected(), cbPictures.isSelected(),
		        cbSubFolders.isSelected(), cbEnablePlugins.isSelected(), (DOFileImportTemplate) bConfigureFileImportTemplate.getUserObject());
	}

	public JTextField getTfFolderPath() {
		return tfFolderPath;
	}

	public void setCbVideo(JCheckBox ccbVideo) {
		this.cbVideo = ccbVideo;
	}

	public JCheckBox getCbVideo() {
		return cbVideo;
	}

	public void setCbAudio(JCheckBox cbAudio) {
		this.cbAudio = cbAudio;
	}

	public JCheckBox getCbAudio() {
		return cbAudio;
	}

	public void setCbPictures(JCheckBox cbPictures) {
		this.cbPictures = cbPictures;
	}

	public JCheckBox getCbPictures() {
		return cbPictures;
	}

	public void setbBrowse(JButton bBrowse) {
		this.bBrowse = bBrowse;

		if (bBrowse != null) bBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = null;
				File f = new File(tfFolderPath.getText());
				if (f.isDirectory()) {
					chooser = new JFileChooser(f.getAbsoluteFile());
				} else {
					chooser = new JFileChooser();
				}

				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("FoldTab.28"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String folderPath = chooser.getSelectedFile().getAbsolutePath();
					tfFolderPath.setText(folderPath);
				}
			}
		});
	}

	public JButton getbBrowse() {
		return bBrowse;
	}

	public void setbScan(JButton bScan) {
		this.bScan = bScan;
		if (bScan != null) {
			bScan.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					scanFolder();
				}
			});
		}
	}

	private void scanFolder() {
		File f = new File(tfFolderPath.getText());
		if (f.isDirectory()) {
			try {
				LibraryManager.getInstance().scanFolder(getManagedFolder());
			} catch (InitialisationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(tfFolderPath.getTopLevelAncestor(), String.format(Messages.getString("ML.Messages.FolderDoesNotExist"), tfFolderPath.getText()));
		}
	}

	public JButton getbScan() {
		return bScan;
	}

	public void setbDelete(JButton bDelete) {
		this.bDelete = bDelete;
		if (bDelete != null) {
			bDelete.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					fireRemove();
				}
			});
		}
	}

	public JButton getbDelete() {
		return bDelete;
	}

	public void setCbSubFolders(JCheckBox cbSubFolders) {
		this.cbSubFolders = cbSubFolders;
	}

	public JCheckBox getCbSubFolders() {
		return cbSubFolders;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setbConfigureVideo(EButton bConfigureVideo) {
		this.bConfigureFileImportTemplate = bConfigureVideo;
	}

	public EButton getbConfigureFileImportTemplate() {
		return bConfigureFileImportTemplate;
	}

	public DOFileImportTemplate getFileImportTemplate() {
		return bConfigureFileImportTemplate.getUserObject() instanceof DOFileImportTemplate ? (DOFileImportTemplate)bConfigureFileImportTemplate.getUserObject() : null;
	}

	public JCheckBox getCbEnablePlugins() {
		return cbEnablePlugins;
	}

	public void setCbEnablePlugins(JCheckBox cbEnablePlugins) {
		this.cbEnablePlugins = cbEnablePlugins;
		cbEnablePlugins.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(((JCheckBox)e.getSource()).isSelected()) {
					bConfigureFileImportTemplate.setEnabled(true);
				} else {
					bConfigureFileImportTemplate.setEnabled(false);
				}
			}
		});
	}

	private void updateTfFolderPathToolTip() {
		if (!tfFolderPath.getText().equals("") && tfFolderPath.getPreferredSize().width > tfFolderPath.getSize().width) {
			tfFolderPath.setToolTipText(tfFolderPath.getText());
		} else {
			tfFolderPath.setToolTipText(null);
		}
	}

	private void fireRemove() {
		for (ActionListener l : removeListeners) {
			l.actionPerformed(new ActionEvent(this, 753, "Delete"));
		}
	}
}
