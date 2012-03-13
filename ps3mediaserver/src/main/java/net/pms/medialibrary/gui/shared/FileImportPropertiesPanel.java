package net.pms.medialibrary.gui.shared;

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

import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
	private List<MultiselectJlist> lTagsPerEngine;

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
		if(activePluginsChangedListeners.contains(activePluginsChangedListener)){
			activePluginsChangedListeners.remove(activePluginsChangedListener);
		}
		activePluginsChangedListeners.add(activePluginsChangedListener);
	}

	/**
	 * Initializes all UI Components. Components being initialized are the combo box containing 
	 * the templates, all file properties with their list of engines and the buttons
	 */
	private void prepareComponents(DOFileImportTemplate fileImportTemplate) {
		fssps = getSelectorPanels(fileImportTemplate, fileType);
		if(!isHandlingPluginSelectionChange) {
			lActivePlugins = buildActivePluginsPanel(fileImportTemplate);
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
		FormLayout layout = new FormLayout("p, p, p, p, p, p, p, p, p, p, p, p",
		        "p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(new TitledBorder("Tags"));
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		int x = 1;
		int y = 1;
		for(MultiselectJlist l : lTagsPerEngine) {
			if(y > 12) {
				break;
			}
			
			builder.add(l, cc.xy(x, y));
			x ++;
		}
		
		return builder.getPanel();
	}

	private JPanel buildFilePropertyPreferencesPanel() {
		FormLayout layout = new FormLayout("5px, f:p:g, 15px, f:p:g, 15px, f:p:g, 15px, f:p:g, 15px, f:p:g, 5px",
		        "5px, t:p, 15px, t:p, 15px, t:p, 15px, t:p, 15px, t:p, 5px");
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
				if (index % 4 == 0) {
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
	
	private List<MultiselectJlist> getTagsPerEngine(DOFileImportTemplate fileImportTemplate) {
		List<MultiselectJlist> res = new ArrayList<MultiselectJlist>();
		
		Map<String, List<String>> availableTags = FileImportHelper.getTagNamesPerEngine(fileType, fileImportTemplate);
		Map<String, List<String>> enabledTagsForEngines = fileImportTemplate.getEnabledTags().get(fileType);
		
		for(String engineName : availableTags.keySet()) {
			List<String> currentTags = availableTags.get(engineName);
			Collections.sort(currentTags);
			
			MultiselectJlist newList = new MultiselectJlist(currentTags);
			newList.setBackground(getBackground());
			newList.setCellRenderer(new ActiveEnginesListCellRenderer());
			newList.setBorder(new TitledBorder(engineName));
			
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
			
			res.add(newList);
		}
		
		return res;
	}

	private MultiselectJlist buildActivePluginsPanel(DOFileImportTemplate template) {		
		//initialize the active plugins list view
		MultiselectJlist l = new MultiselectJlist(FileImportHelper.getAvailableEngineNames(fileType));
		l.setCellRenderer(new ActiveEnginesListCellRenderer());
		l.setBorder(new TitledBorder("Active plugins"));
		l.setBackground(getBackground());
		List<String> activeEngines = template.getEnabledEngines().get(fileType);
		if(activeEngines != null) {
			//collect the indexes to select in a List<Integer>
			List<Integer> engineIndexesToSelect = new ArrayList<Integer>();
			ListModel model = l.getModel();
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
			l.setSelectedIndices(indexesToSelect);
		}
		
		l.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				synchronized (this) {
					if(isHandlingPluginSelectionChange) return;
					isHandlingPluginSelectionChange = true;					
				}
				
				for(ActionListener l : activePluginsChangedListeners) {
					l.actionPerformed(new ActionEvent(this, 0, "SelectionChanged"));
				}
				
				isHandlingPluginSelectionChange = false;
			}
		});
		
		return l;
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
		for(MultiselectJlist l : lTagsPerEngine) {
			Object[] vals = l.getSelectedValues();
			if(vals != null && vals.length > 0) {
				List<String> tagNames = new ArrayList<String>();
				for(int i = 0; i < vals.length; i++) {
					tagNames.add(vals[i].toString());
				}
				res.put(((TitledBorder)l.getBorder()).getTitle(), tagNames);
			}
		}
		return res;
	}
}
