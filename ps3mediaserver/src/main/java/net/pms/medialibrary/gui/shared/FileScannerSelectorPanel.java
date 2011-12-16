package net.pms.medialibrary.gui.shared;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.FileProperty;

public class FileScannerSelectorPanel extends JPanel {
	private static final long serialVersionUID = -7986992235501250777L;
	
	private JLabel lFileType;
	private ReorderableJList lEngineNames;
	
	private FileProperty fileProperty;

	public FileScannerSelectorPanel(FileProperty fileProperty, List<String> engineNames){
		setLayout(new GridLayout());
		
		this.fileProperty = fileProperty == null ? FileProperty.UNKNOWN : fileProperty;
		init(engineNames);
	}

	public Map<FileProperty, List<String>> getFilePropertyEngineNames() {
		return null;
	}
	
	public String getLocalizedFilePropertyName() {
		return Messages.getString("ML.FileProperty." + fileProperty.toString());
	}
	
	public FileProperty getFileProperty() {
		return fileProperty;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getEngineNames() {
		return (List<String>) Collections.list(((DefaultListModel)lEngineNames.getModel()).elements());
	}
	
	private void init(List<String> engineNames) {
		FormLayout layout = new FormLayout("p, 4px, p",
		        "p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		lFileType = new JLabel(getLocalizedFilePropertyName());
		builder.add(lFileType, cc.xy(1, 1));
		
		lEngineNames = new ReorderableJList();
		lEngineNames.setSelectionBackground(lEngineNames.getBackground());
		lEngineNames.setSelectionForeground(lEngineNames.getForeground());
		DefaultListModel defModel = new DefaultListModel();
		lEngineNames.setModel(defModel);
		
		for(String engineName : engineNames) {
			defModel.addElement(engineName);			
		}
		builder.add(lEngineNames, cc.xy(3, 1));
		
		add(builder.getPanel());
	}

	public Component getNameLabel() {
		return lFileType;
	}

	public Component getEnginesList() {
		return lEngineNames;
	}
}
