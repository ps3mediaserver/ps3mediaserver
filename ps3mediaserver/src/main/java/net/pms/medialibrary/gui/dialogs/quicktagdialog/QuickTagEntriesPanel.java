package net.pms.medialibrary.gui.dialogs.quicktagdialog;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOQuickTagEntry;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.gui.shared.JHeader;

public class QuickTagEntriesPanel extends JPanel{
	private static final long serialVersionUID = -3273657476659025528L;
	private List<QuickTagEntryPanel> quickTagEntryPanels = new ArrayList<QuickTagEntryPanel>();
	private FileType fileType;
	private boolean isInitializing;

	public QuickTagEntriesPanel(List<DOQuickTagEntry> quickTagEntries, FileType fileType) {
		isInitializing = true;
		
		setLayout(new GridLayout());
		if(quickTagEntries == null) {
			quickTagEntries = new ArrayList<DOQuickTagEntry>();
		}
		if(quickTagEntries.size() == 0) {
			quickTagEntries.add(new DOQuickTagEntry());
		}

		this.fileType = fileType;
		for(DOQuickTagEntry entry : quickTagEntries) {
			addQuickTagEntry(entry);
		}
		
		isInitializing = false;
		
		refresh();
	}

	public void addQuickTagEntry(DOQuickTagEntry quickTagEntry) {
		QuickTagEntryPanel newEntry = new QuickTagEntryPanel(quickTagEntry, fileType);
		newEntry.addDeleteListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				quickTagEntryPanels.remove(e.getSource());
				refresh();
			}
		});
		
		quickTagEntryPanels.add(newEntry);
		
		refresh();
	}

	private void refresh() {
		if(isInitializing) {
			return;
		}
		
		//create the rows string
		String rowsString = "5px, p, 5px, ";
		if(quickTagEntryPanels.size() > 0) {
			for(int i = 0; i < quickTagEntryPanels.size(); i++) {
				rowsString += "p, 3px, ";
			}
			rowsString = rowsString.substring(0, rowsString.length() - 5);
		}
		rowsString += "5px";
		
		FormLayout layout = new FormLayout(
				"5px, 135, 5px, 135, 5px, f:210:g, 5px, p, 2px, p, 2px, p, 5px, p, 5px",
				rowsString);
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		//add the headers
		int rowIndex = 2;
		builder.add(new JHeader(Messages.getString("ML.QuickTagDialog.Header.Name")), cc.xy(2, rowIndex, CellConstraints.CENTER, CellConstraints.DEFAULT));
		builder.add(new JHeader(Messages.getString("ML.QuickTagDialog.Header.TagName")), cc.xy(4, rowIndex, CellConstraints.CENTER, CellConstraints.DEFAULT));
		builder.add(new JHeader(Messages.getString("ML.QuickTagDialog.Header.TagValue")), cc.xy(6, rowIndex, CellConstraints.CENTER, CellConstraints.DEFAULT));
		builder.add(new JHeader(Messages.getString("ML.QuickTagDialog.Header.HotKey")), cc.xyw(8, rowIndex, 5, CellConstraints.CENTER, CellConstraints.DEFAULT));
		
		//add the entries
		for(QuickTagEntryPanel pEntry : quickTagEntryPanels) {
			rowIndex += 2;
			
			builder.add(pEntry.getTfName(), cc.xy(2, rowIndex));
			builder.add(pEntry.getCbTagName(), cc.xy(4, rowIndex));
			builder.add(pEntry.getCbTagValue(), cc.xy(6, rowIndex));
			builder.add(pEntry.getCbKeyCombination(), cc.xy(8, rowIndex));
			builder.addLabel("+", cc.xy(10, rowIndex));
			builder.add(pEntry.getCbVirtualKey(), cc.xy(12, rowIndex));
			builder.add(pEntry.getbDelete(), cc.xy(14, rowIndex));
		}
		
		removeAll();
		add(builder.getPanel());
		
		validate();
		repaint();
	}
}
