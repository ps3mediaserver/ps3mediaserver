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
package net.pms.medialibrary.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.events.FileImportDialogListener;
import net.pms.medialibrary.gui.shared.FileImportTemplatePanel;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class FileImportTemplateDialog extends JDialog {
	private static final long serialVersionUID = 1590810981218310481L;
	private final int MIN_BUTTON_WIDTH = 60;
	
	private JButton bOk;
	private JButton bSave;
	private JButton bCancel;	
	private FileImportTemplatePanel pTemplateFileProperties;
	
	private List<FileImportDialogListener> fileImportDialogListeners = new ArrayList<FileImportDialogListener>();
	
	private boolean save;

	public FileImportTemplateDialog(Window owner, int fileImportTemplateId){
		super(owner);
		initComponents(fileImportTemplateId);
		placeComponents();
	}

	public FileImportTemplateDialog(JDialog owner, int fileImportTemplateId){
		super(owner);
		initComponents(fileImportTemplateId);
		placeComponents();
	}
	
	public int getTemplateId() {
		return pTemplateFileProperties.getFileImportTemplateId();
	}
	
	public DOFileImportTemplate getTemplate() {
		return pTemplateFileProperties.getDisplayedTemplate();
	}
	
	public void addFileImportDialogListener(FileImportDialogListener listener) {
		if(!fileImportDialogListeners.contains(listener)) {
			fileImportDialogListeners.add(listener);
		}
	}
	
	private void initComponents(int fileImportTemplateId) {
		setIconImage(new ImageIcon(FileImportTemplateDialog.class.getResource("/resources/images/icon-32.png")).getImage());
		setTitle(Messages.getString("ML.FileImportConfigurationDialog.title"));
		
		pTemplateFileProperties = new FileImportTemplatePanel(fileImportTemplateId);
		pTemplateFileProperties.addRepaintListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pack();
			}
		});

		bOk = new JButton(Messages.getString("ML.FileImportConfigurationDialog.bOk"));
		if(bOk.getPreferredSize().width < MIN_BUTTON_WIDTH) bOk.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bOk.getPreferredSize().height));
		bOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				DOFileImportTemplate template = saveConfiguration();
				if(template != null) {
					setVisible(false);
				}
			}
		});
		
		bSave = new JButton(Messages.getString("ML.FileImportConfigurationDialog.bSave"));
		if(bSave.getPreferredSize().width < MIN_BUTTON_WIDTH) bSave.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bSave.getPreferredSize().height));
		bSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				saveConfiguration();
			}
		});
		
		bCancel = new JButton(Messages.getString("ML.FileImportConfigurationDialog.bCancel"));
		if(bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		bCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}

	private void placeComponents() {
		FormLayout layout = new FormLayout("3px, p, 3px",
		        "3px, p, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.add(pTemplateFileProperties, cc.xy(2, 2, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		
		//add buttons
		JPanel bPanel = new JPanel();
		bPanel.setAlignmentX(CENTER_ALIGNMENT);
		bPanel.add(bOk);
		bPanel.add(bSave);
		bPanel.add(bCancel);
		builder.add(bPanel, cc.xy(2, 3));
		
		//build the dialog
		setLayout(new GridLayout());
		setContentPane(builder.getPanel());
	}
	
	private DOFileImportTemplate saveConfiguration() {
		DOFileImportTemplate template = pTemplateFileProperties.getDisplayedTemplate();
		if(template.getName() == null || template.getName().equals("")) {
			JOptionPane.showMessageDialog(this, Messages.getString("ML.FileImportConfigurationPanel.Msg.EnterTemplateName"));
			return null;
		}
		
		// insert or update into db. A new template has the ID=0
		if (template.getId() > 0) {
			MediaLibraryStorage.getInstance().updateFileImportTemplate(template);
		} else {
			MediaLibraryStorage.getInstance().insertFileImportTemplate(template);
		}

		save = true;
		
		for(FileImportDialogListener l : fileImportDialogListeners) {
			l.templateSaved(template);
		}

		pTemplateFileProperties.templateSaved(template);
		
		return template;
	}

	public boolean isSave() {
		return save;
	}
}