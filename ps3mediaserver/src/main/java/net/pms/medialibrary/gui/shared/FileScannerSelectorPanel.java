package net.pms.medialibrary.gui.shared;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileScannerEngineConfiguration;
import net.pms.medialibrary.commons.enumarations.FileProperty;

public class FileScannerSelectorPanel extends JPanel {
	private static final long serialVersionUID = -7986992235501250777L;
	
	private JHeader lFileType;
	private ReorderableJList lEngineNames;
	
	private DOFileScannerEngineConfiguration engine;

	public FileScannerSelectorPanel(DOFileScannerEngineConfiguration engine){
		setLayout(new GridLayout());
		
		this.engine = engine;
		init();
	}

	public Map<FileProperty, List<String>> getFilePropertyEngineNames() {
		return null;
	}
	
	public String getLocalizedFilePropertyName() {
		return Messages.getString("ML.FileProperty." + engine.getFileProperty().toString());
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getEngineNames() {
		return (List<String>) Collections.list(((DefaultListModel)lEngineNames.getModel()).elements());
	}
	
	private void init() {
		FormLayout layout = new FormLayout("f:p:g",
		        "p, 2px, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		lFileType = new JHeader(getLocalizedFilePropertyName(), true);
		lFileType.setSelected(engine.isEnabled() && engine.getEngineNames().size() > 0);
		lFileType.setEnabled(engine.getEngineNames().size() > 0);
		lFileType.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				lEngineNames.setEnabled(lFileType.isSelected());
			}
		});
		builder.add(lFileType, cc.xy(1, 1));

		lEngineNames = new ReorderableJList();
		lEngineNames.setBorder(new LineBorder(Color.lightGray));
		lEngineNames.setSelectionBackground(lEngineNames.getBackground());
		lEngineNames.setSelectionForeground(lEngineNames.getForeground());
		lEngineNames.setEnabled(lFileType.isSelected());
		DefaultListModel defModel = new DefaultListModel();
		lEngineNames.setModel(defModel);
		
		for(String engineName : engine.getEngineNames()) {
			defModel.addElement(engineName);			
		}
		builder.add(lEngineNames, cc.xy(1, 3));
		
		add(builder.getPanel());
	}

	public DOFileScannerEngineConfiguration getEngine() {
		return new DOFileScannerEngineConfiguration(lFileType.isSelected(), getEngineNames(), engine.getFileProperty());
	}
}
