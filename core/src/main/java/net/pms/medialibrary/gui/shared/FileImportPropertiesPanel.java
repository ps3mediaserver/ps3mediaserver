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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOFileScannerEngineConfiguration;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.helpers.FileImportHelper;

public class FileImportPropertiesPanel extends JPanel {
	private static final long serialVersionUID = 5760812009053418040L;
	private static final Logger log = LoggerFactory.getLogger(FileImportPropertiesPanel.class);
	
	private FileType fileType;

	private List<ActionListener> activePluginsChangedListeners = new ArrayList<ActionListener>();
	
	private MultiselectJlist lActivePlugins;
	private List<FileScannerSelectorPanel> fssps;
	private Map<String, MultiselectJlist> lTagsPerEngine;

	private static boolean isHandlingPluginSelectionChange;
	
	public FileImportPropertiesPanel(DOFileImportTemplate fileImportTemplate, FileType fileType){
		this.fileType = fileType;
		
		setFileImportTemplate(fileImportTemplate);
	}

	public FileType getFileType() {
		return fileType;
	}
	
	public void setFileImportTemplate(DOFileImportTemplate template) {
		prepareComponents(template);
		placeComponents();
	}

	/**
	 * Adds notification support when a repaint is needed.
	 * This is required to redraw the dialog properly when changing template
	 * @param repaintListener
	 */
	public void addActivePluginsChangedListener(ActionListener activePluginsChangedListener) {
		if(!activePluginsChangedListeners.contains(activePluginsChangedListener)){
			activePluginsChangedListeners.add(activePluginsChangedListener);
		}
	}

	/**
	 * Initializes all UI Components. Components being initialized are the combo box containing 
	 * the templates, all file properties with their list of engines and the buttons
	 */
	private void prepareComponents(DOFileImportTemplate fileImportTemplate) {
		fssps = getSelectorPanels(fileImportTemplate, fileType);
		synchronized (this) {
			if(!isHandlingPluginSelectionChange) {
				lActivePlugins = buildActivePluginsPanel(fileImportTemplate);
			}			
		}
		lTagsPerEngine = getTagsPerEngine(fileImportTemplate);
	}
	
	/**
	 * Places the visual components correctly in the grid. Components being shown are the combo box containing 
	 * the templates, all file properties with their list of engines and the buttons
	 */
	private void placeComponents() {
		FormLayout layout = new FormLayout("3px, 120, 5px, fill:p:grow, 3px",
		        "3px, p, 5px, fill:p:grow, 3px");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		//create top panel with combo box and and buttons
		builder.add(lActivePlugins, cc.xywh(2, 2, 1, 3));
		builder.add(buildFilePropertyPreferencesPanel(), cc.xy(4, 2));
		builder.add(buildTagsPanel(), cc.xy(4, 4));
		
		//build the dialog
		setLayout(new GridLayout());
		removeAll();
		add(builder.getPanel());
	}
	

	private Component buildTagsPanel() {
		FormLayout layout = new FormLayout("5px, p, 10px, p, 10px, p, 10px, p, 10px, p, 10px, p, 10px, p, 10px, p, 10px, p, 10px, p, 5px",
		        "f:130, 5px");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(new TitledBorder("Tags"));
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		int x = 2;
		int y = 1;
		for(String engineName : lTagsPerEngine.keySet()) {
			if(x > 20) {
				break;
			}
			
			
			JScrollPane sp = new JScrollPane(lTagsPerEngine.get(engineName));
			sp.setBorder(BorderFactory.createEmptyBorder());

			JPanel p = new JPanel(new BorderLayout(0, 3));
			p.add(new JHeader(engineName), BorderLayout.NORTH);
			p.add(sp, BorderLayout.CENTER);
			
			builder.add(p, cc.xy(x, y));
			x += 2;
		}
		
		return builder.getPanel();
	}

	private JPanel buildFilePropertyPreferencesPanel() {
		FormLayout layout = new FormLayout("5px, f:p:g, 15px, f:p:g, 15px, f:p:g, 15px, f:p:g, 15px, f:p:g, 15px, f:p:g, 5px",
		        "5px, t:p, 15px, t:p, 15px, t:p, 5px");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(new TitledBorder("Properties"));
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		if (fssps != null) {
			// create grid with file types and engine names
			int index = 0;
			int x = 0;
			int y = 0;
			for (FileScannerSelectorPanel fssp : fssps) {
				if (index % 6 == 0) {
					x = 2;
					y += 2;
				}

				builder.add(fssp, cc.xy(x, y));
				x += 2;

				index++;
			}
		} else {
			log.warn("No FileSelectorPanels will be added because fpps is null. This should never happen!");
		}
		
		return builder.getPanel();
	}
	
	private Map<String, MultiselectJlist> getTagsPerEngine(DOFileImportTemplate fileImportTemplate) {
		Map<String, MultiselectJlist> res = new HashMap<String, MultiselectJlist>();
		
		Map<String, List<String>> availableTags = FileImportHelper.getTagNamesPerEngine(fileType, fileImportTemplate);
		Map<String, List<String>> enabledTagsForEngines = fileImportTemplate.getEnabledTags().get(fileType);
		
		for(String engineName : availableTags.keySet()) {
			List<String> currentTags = availableTags.get(engineName);
			Collections.sort(currentTags);
			
			MultiselectJlist newList = new MultiselectJlist(currentTags);
			newList.setBackground(getBackground());
			newList.setCellRenderer(new ActiveEnginesListCellRenderer());
			
			List<String> enabledTags = enabledTagsForEngines.get(engineName);
			if(enabledTags != null && enabledTags.size() > 0) {
				ListModel model = newList.getModel();
				List<Integer> tagIndexesToSelect = new ArrayList<Integer>();
				for(int i = 0; i < model.getSize(); i++) {
					Object element = model.getElementAt(i);
					if(enabledTags.contains(element)) {
						tagIndexesToSelect.add(i);
					}
				}

				//convert the List<Integer> to int[]
				int[] indexesToSelect = new int[tagIndexesToSelect.size()];
				for(int i = 0; i < tagIndexesToSelect.size(); i++) {
					indexesToSelect[i] = tagIndexesToSelect.get(i);
				}
				newList.setSelectedIndices(indexesToSelect);
			}
			res.put(engineName, newList);
		}
		
		return res;
	}

	private MultiselectJlist buildActivePluginsPanel(DOFileImportTemplate template) {		
		//initialize the active plugins list view
		final MultiselectJlist newList = new MultiselectJlist(FileImportHelper.getAvailableEngineNames(fileType));
		newList.setCellRenderer(new ActiveEnginesListCellRenderer());
		newList.setBorder(new TitledBorder(Messages.getString("ML.FileImportPropertiesPanel.title.ActivePlugins")));
		newList.setBackground(getBackground());
		List<String> activeEngines = template.getEnabledEngines().get(fileType);
		if(activeEngines != null) {
			//collect the indexes to select in a List<Integer>
			List<Integer> engineIndexesToSelect = new ArrayList<Integer>();
			ListModel model = newList.getModel();
			for(String engineName : activeEngines) {
				for(int i = 0; i < model.getSize(); i++){
					String currentEngine = model.getElementAt(i).toString();
					if(currentEngine.equals(engineName)) {
						engineIndexesToSelect.add(i);
						break;
					}
				}
			}
			
			//convert the List<Integer to int[]
			int[] indexesToSelect = new int[engineIndexesToSelect.size()];
			for(int i = 0; i < engineIndexesToSelect.size(); i++) {
				indexesToSelect[i] = engineIndexesToSelect.get(i);
			}
			newList.setSelectedIndices(indexesToSelect);
		}
		
		newList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				synchronized (this) {
					if(isHandlingPluginSelectionChange) return;
					isHandlingPluginSelectionChange = true;					
				}
				
				try {
					ListModel model = newList.getModel();
					for(ActionListener l : activePluginsChangedListeners) {
						for(int i = e.getFirstIndex(); i < e.getLastIndex(); i++) {
							l.actionPerformed(new ActionEvent(this, newList.isSelectedIndex(i) ? 0 : 1, model.getElementAt(i).toString()));
						}
					}
				} 
				finally {
					isHandlingPluginSelectionChange = false;
				}
			}
		});
		
		return newList;
	}

	/**
	 * Creates the list of FileScannerSelectorPanel consisting of a FileProperty and a list of engine names
	 * @param template
	 * @param video 
	 * @return list of FileScannerSelectorPanel
	 */
	private List<FileScannerSelectorPanel> getSelectorPanels(DOFileImportTemplate template, FileType fileType){

		//get the list of available plugins and create a map containing all available engines for a file property
		List<DOFileScannerEngineConfiguration> filePropertyEngineNames = FileImportHelper.getFilePropertyEngines(template);
		
		//add all file properties with engine names
		String filePropertyPrefix = fileType.toString() + "_";
		ArrayList<FileScannerSelectorPanel> newFssps = new ArrayList<FileScannerSelectorPanel>();
		for(DOFileScannerEngineConfiguration engine : filePropertyEngineNames) {
			if(engine.getFileProperty().toString().startsWith(filePropertyPrefix)) {
				newFssps.add(new FileScannerSelectorPanel(engine));
			}
		}
		
		//sort the panels by localized name
		Collections.sort(newFssps, new Comparator<FileScannerSelectorPanel>() {
			@Override
			public int compare(FileScannerSelectorPanel o1, FileScannerSelectorPanel o2) {
				return o1.getLocalizedFilePropertyName().compareTo(o2.getLocalizedFilePropertyName());
			}
		});
		
		return newFssps;
	}

	public List<DOFileScannerEngineConfiguration> getConfiguredEngines() {
		List<DOFileScannerEngineConfiguration> res = new ArrayList<DOFileScannerEngineConfiguration>();
		for(FileScannerSelectorPanel p : fssps) {
			res.add(p.getEngine());
		}
		return res;
	}

	public Map<FileType, List<String>> getActiveEngines() {
		Map<FileType, List<String>> res = new HashMap<FileType, List<String>>();
 		for(Object o : lActivePlugins.getSelectedValues()) {
 			if(res.containsKey(fileType)) {
 				res.get(fileType).add(o.toString());
 			} else{
 				List<String> newEngines = new ArrayList<String>();
 				newEngines.add(o.toString());
 				res.put(fileType, newEngines);
 			}
		}
		return res;
	}

	public Map<String, List<String>> getActiveTags() {
		Map<String, List<String>> res = new HashMap<String, List<String>>();
		for(String engineName : lTagsPerEngine.keySet()) {
			Object[] vals = lTagsPerEngine.get(engineName).getSelectedValues();
			if(vals != null && vals.length > 0) {
				List<String> tagNames = new ArrayList<String>();
				for(int i = 0; i < vals.length; i++) {
					tagNames.add(vals[i].toString());
				}
				res.put(engineName, tagNames);
			}
		}
		return res;
	}
}
