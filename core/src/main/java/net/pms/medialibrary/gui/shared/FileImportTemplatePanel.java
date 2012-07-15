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
package net.pms.medialibrary.gui.shared;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOFileScannerEngineConfiguration;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class FileImportTemplatePanel extends JPanel {
	private static final long serialVersionUID = -4447691905844747495L;
	
	private List<ActionListener> repaintListeners = new ArrayList<ActionListener>();
	
	private JComboBox cbTemplate;
	private JButton bNewTemplate;
	private JButton bDeleteTemplate;
	private JTabbedPane tpProperties;
	
	private FileImportPropertiesPanel fippsVideo;
	private FileImportPropertiesPanel fippsAudio;
	private FileImportPropertiesPanel fippsPictures;
	
	public FileImportTemplatePanel(int fileImportTemplateId) {
		prepareComponents(fileImportTemplateId);
	}

	/**
	 * Adds notification support when a repaint is needed.
	 * This is required to redraw the dialog properly when changing template
	 * @param repaintListener
	 */
	public void addRepaintListener(ActionListener repaintListener) {
		if(repaintListeners.contains(repaintListener)){
			repaintListeners.remove(repaintListener);
		}
		repaintListeners.add(repaintListener);
	}
	
	/**
	 * returns the ID of the template having been set. If an error occurs the id=1 
	 * corresponding to the default template is being returned
	 * @return set template id
	 */
	public int getFileImportTemplateId() {
		//the ID of the default template is 1. Keep it as a fail safe if something goes wrong
		//while retrieving the ID from the panel
		int templateId = 1;
		
		//try to get the real selected template id
		if(cbTemplate.getSelectedItem() != null && cbTemplate.getSelectedItem() instanceof DOFileImportTemplate) {
			templateId = ((DOFileImportTemplate)cbTemplate.getSelectedItem()).getId();
		}
		
		return templateId;
	}

	/**
	 * Sets the 'normal' gui state be enabling the delete template button,
	 * making the templates combo box non-editable and selecting the saved template
	 * @param template
	 */
	public void templateSaved(DOFileImportTemplate template) {		
		if(cbTemplate.isEditable()){
			//restore UI after editing
			bDeleteTemplate.setEnabled(true);
			bNewTemplate.setEnabled(true);

			//refresh all templates
			cbTemplate = getTemplatesComboBox();
			
			fippsAudio.setFileImportTemplate(template);
			fippsPictures.setFileImportTemplate(template);
			fippsVideo.setFileImportTemplate(template);
			
			placeComponents();
			
			//select saved template
			cbTemplate.setSelectedItem(getTemplateInComboBoxById(template.getId()));
		}
		checkDeleteButtonState(template.getId());
	}

	/**
	 * Either return a new template if a new one has been created
	 * or return the already existing one.
	 * If a new template is being returned, its ID will be 0
	 * @return the configured template
	 */
	public DOFileImportTemplate getDisplayedTemplate() {
		DOFileImportTemplate displayedTemplate = null;
		
		if (cbTemplate.isEditable()) {
			// it's a new template
			displayedTemplate = new DOFileImportTemplate();
			
			
			//get the entered name
			String newName = "";
			if(cbTemplate.getEditor().getItem() != null) {
				newName = cbTemplate.getEditor().getItem().toString();
			}
			displayedTemplate.setName(newName);
		} else if (cbTemplate.getSelectedItem() instanceof DOFileImportTemplate) {
			//take the selected template
			displayedTemplate = (DOFileImportTemplate) cbTemplate.getSelectedItem();
		}
		
		//merge the configured engines for the different file types
		List<DOFileScannerEngineConfiguration> configuredEngines = new ArrayList<DOFileScannerEngineConfiguration>();
		configuredEngines.addAll(fippsAudio.getConfiguredEngines());
		configuredEngines.addAll(fippsVideo.getConfiguredEngines());
		configuredEngines.addAll(fippsPictures.getConfiguredEngines());
 		displayedTemplate.setEngineConfigurations(configuredEngines);
 		
		//merge the active engines for the different file types
		Map<FileType, List<String>> activeEngines = new HashMap<FileType, List<String>>();
		activeEngines.putAll(fippsAudio.getActiveEngines());
		activeEngines.putAll(fippsVideo.getActiveEngines());
		activeEngines.putAll(fippsPictures.getActiveEngines());		
 		displayedTemplate.setEnabledEngines(activeEngines);
 		
 		//merge configured tags per engine
 		Map<FileType, Map<String, List<String>>> tagsPerEngine = new HashMap<FileType, Map<String, List<String>>>();
 		tagsPerEngine.put(FileType.AUDIO, fippsAudio.getActiveTags());
 		tagsPerEngine.put(FileType.VIDEO, fippsVideo.getActiveTags());
 		tagsPerEngine.put(FileType.PICTURES, fippsPictures.getActiveTags());
 		displayedTemplate.setEnabledTags(tagsPerEngine);
		
		return displayedTemplate;
	}
	
	private JComboBox getTemplatesComboBox() {		
		//load the available templates from db
		List<DOFileImportTemplate> importTemplates = MediaLibraryStorage.getInstance().getFileImportTemplates();
		
		//create combo box
		JComboBox cbNew = new JComboBox();
		
		//sort the entries by their displayed name
		Collections.sort(importTemplates, new Comparator<DOFileImportTemplate>() {
			@Override
			public int compare(DOFileImportTemplate o1, DOFileImportTemplate o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		//add the loaded templates as combo box items
		for(DOFileImportTemplate importTemplate : importTemplates) {
			cbNew.addItem(importTemplate);
		}
		
		//attach listener to update shown file properties according to template
		cbNew.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				if(cb.getSelectedItem() instanceof DOFileImportTemplate) {
					DOFileImportTemplate selectedTemplate = (DOFileImportTemplate) cb.getSelectedItem();
					int selectedTab = tpProperties.getSelectedIndex();
					updateTabbedPane(selectedTemplate);
					tpProperties.setSelectedIndex(selectedTab);
					
					placeComponents();
					
					checkDeleteButtonState(selectedTemplate.getId());
				}
				cb.requestFocus();
			}
		});
		
		return cbNew;
	}
	
	
	private void updateTabbedPane(DOFileImportTemplate template) {
		fippsAudio.setFileImportTemplate(template);
		fippsVideo.setFileImportTemplate(template);
		fippsPictures.setFileImportTemplate(template);
		
		tpProperties = buildTabbedPane();
	}

	/**
	 * Initializes all UI Components. Components being initialized are the combo box containing 
	 * the templates, all file properties with their list of engines and the buttons	 * 
	 * @param allowEdit 
	 */
	private void prepareComponents(int fileImportTemplateId) {		
		//create and populate template combo box with items of type DOFileImportTemplate
		cbTemplate = getTemplatesComboBox();
		
		//select the correct template in the combo box
		DOFileImportTemplate activeTemplate = getTemplateInComboBoxById(fileImportTemplateId);

		
		//create the new template button
		bNewTemplate = new JButton(new ImageIcon(getClass().getResource("/resources/images/tp_add.png")));
		bNewTemplate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cbTemplate.setEditable(true);
				cbTemplate.removeAllItems();
				cbTemplate.requestFocus();
				
				bDeleteTemplate.setEnabled(false);
				bNewTemplate.setEnabled(false);
			}
		});

		//create the delete template button
		bDeleteTemplate = new JButton(new ImageIcon(getClass().getResource("/resources/images/tp_remove.png")));
		bDeleteTemplate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedTemplate();
			}
		});
		
		fippsAudio = new FileImportPropertiesPanel(activeTemplate, FileType.AUDIO);
		fippsAudio.addActivePluginsChangedListener(getActivePluginsChangedListener());
		fippsVideo = new FileImportPropertiesPanel(activeTemplate, FileType.VIDEO);
		fippsVideo.addActivePluginsChangedListener(getActivePluginsChangedListener());
		fippsPictures = new FileImportPropertiesPanel(activeTemplate, FileType.PICTURES);
		fippsPictures.addActivePluginsChangedListener(getActivePluginsChangedListener());

		//create and update list of FileScannerSelectorPanel
		if(activeTemplate != null) {
			updateTabbedPane(activeTemplate);
		}
		
		//select configured template after all components have been initialized
		//this will also trigger the rest of the UI initialization
		cbTemplate.setSelectedItem(activeTemplate);
	}
	
	private ActionListener getActivePluginsChangedListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {				
				DOFileImportTemplate tmpTemplate = getDisplayedTemplate();
				
				fippsAudio.setFileImportTemplate(tmpTemplate);
				fippsVideo.setFileImportTemplate(tmpTemplate);
				fippsPictures.setFileImportTemplate(tmpTemplate);
				
				for(ActionListener l : repaintListeners) {
					l.actionPerformed(new ActionEvent(this, 0, "Repaint"));
				}
			}
		};
	}

	private void checkDeleteButtonState(int templateId)  {
		//disable the delete button if it wont be possible to delete the template
		if(MediaLibraryStorage.getInstance().isFileImportTemplateInUse(templateId)) {
			bDeleteTemplate.setEnabled(false);
		} else {
			bDeleteTemplate.setEnabled(true);
		}
	}
	

	/**
	 * Places the visual components correctly in the grid. Components being shown are the combo box containing 
	 * the templates, all file properties with their list of engines and the buttons
	 */
	private void placeComponents() {
		FormLayout layout = new FormLayout("fill:p:grow, 5px, p, 5px, p, 5px, p",
		        "p, 5px, fill:p:grow");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		//create top panel with combo box and and buttons
		builder.addLabel(Messages.getString("ML.FileImportConfigurationPanel.lTemplate"), cc.xy(1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		builder.add(cbTemplate, cc.xy(3, 1));
		builder.add(bNewTemplate, cc.xy(5, 1));
		builder.add(bDeleteTemplate, cc.xy(7, 1));
		builder.add(tpProperties, cc.xyw(1, 3, 7));
		
		//build the dialog
		setLayout(new GridLayout());
		removeAll();
		add(builder.getPanel());
		
		for(ActionListener l : repaintListeners) {
			l.actionPerformed(new ActionEvent(this, 0, "Repaint"));
		}
	}

	private JTabbedPane buildTabbedPane() {
		JTabbedPane tp = new JTabbedPane();
		tp.addTab(Messages.getString("ML.FileType.VIDEO"), new ImageIcon(getClass().getResource("/resources/images/videofolder-16.png")), fippsVideo);
		tp.addTab(Messages.getString("ML.FileType.AUDIO"), new ImageIcon(getClass().getResource("/resources/images/audiofolder-16.png")), fippsAudio);
		tp.addTab(Messages.getString("ML.FileType.PICTURES"), new ImageIcon(getClass().getResource("/resources/images/picturesfolder-16.png")), fippsPictures);
		
		return tp;
	}
	
	/**
	 * Deletes the currently selected DOFileImportTemplate in the combo box. It's being deleted from the DB as 
	 * well as removed from the list of available DOFileImportTemplate in the combo box
	 * 
	 */
	private void deleteSelectedTemplate() {
		if(cbTemplate.getSelectedItem() != null && cbTemplate.getSelectedItem() instanceof DOFileImportTemplate) {
			DOFileImportTemplate template = (DOFileImportTemplate) cbTemplate.getSelectedItem();
			
			//don't allow to delete the default template
			if(template.getId() == 1) {
				JOptionPane.showMessageDialog(this, Messages.getString("ML.FileImportConfigurationPanel.Msg.NoDeleteDefaultTemplate"));
				return;
			}
			
			//don't allow to delete a template which is being used by other managed folders
			if(MediaLibraryStorage.getInstance().isFileImportTemplateInUse(template.getId())) {
				JOptionPane.showMessageDialog(this, String.format(Messages.getString("ML.FileImportConfigurationPanel.Msg.DeleteTemplateInUse"), template.getName()));
				return;
			} 
			
			//ask for delete confirmation before deleting
			if(JOptionPane.showConfirmDialog(this, String.format(Messages.getString("ML.FileImportConfigurationPanel.Msg.ConfirmDeleteTemplate"), template.getName())) == JOptionPane.NO_OPTION) {
				return;
			}
			
			//delete the template from DB and combo box
			MediaLibraryStorage.getInstance().deleteFileImportTemplate(template.getId());
			cbTemplate.removeItem(template);
			
			//set the default template selected when a template is being deleted
			cbTemplate.setSelectedItem(getTemplateInComboBoxById(1));
		}		
	}
	
	/**
	 * Returns the DOFileImportTemplate with the given id if it is contained in the combo box elements, null otherwise
	 * @param templateId the id of the template to look for
	 * @return the DOFileImportTemplate with the given id or null if not found
	 */
	private DOFileImportTemplate getTemplateInComboBoxById(int templateId){
		DOFileImportTemplate res = null;
		
		for(int i = 0; i < cbTemplate.getItemCount(); i++){
			Object currrentItem = cbTemplate.getItemAt(i);
			if(currrentItem instanceof DOFileImportTemplate){
				if(((DOFileImportTemplate)currrentItem).getId() == templateId){
					res = (DOFileImportTemplate)currrentItem;
					break;
				}
			}
		}
		
		return res;
	}
}
