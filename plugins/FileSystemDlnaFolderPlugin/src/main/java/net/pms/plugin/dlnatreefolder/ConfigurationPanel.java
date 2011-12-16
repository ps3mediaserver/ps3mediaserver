package net.pms.plugin.dlnatreefolder;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ConfigurationPanel extends JPanel {
	private static final long serialVersionUID = -2950360909291954353L;

	private List<FolderEntry> sharedFolders    = new ArrayList<FolderEntry>();
	private JButton           bAddFolder;

	private JPanel            pNoSharedFoldersSet;

	public ConfigurationPanel() {
		this(new ArrayList<String>());
	}

	public ConfigurationPanel(List<String> folderPaths) {
		setLayout(new GridLayout());
		init();
		rebuildPanel();
	}

	public List<String> getFolders() {
		List<String> folders = new ArrayList<String>();
		for (FolderEntry fe : sharedFolders) {
			if (!folders.contains(fe.getFolderPath())) {
				folders.add(fe.getFolderPath());
			}
		}
		return folders;
	}

	public void setFolders(List<String> folderPaths) {
		sharedFolders.clear();
		rebuildPanel();
		for (String folderPath : folderPaths) {
			addFolderEntry(folderPath);
		}
	}

	private void init() {
		pNoSharedFoldersSet = new JPanel();
		pNoSharedFoldersSet.setLayout(new GridLayout());
		pNoSharedFoldersSet.add(new JLabel("No shared folders set"));

		bAddFolder = new JButton("Add Folder");
		rebuildPanel();
		bAddFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addFolderEntry("");
			}
		});
	}

	private void addFolderEntry(String folderPath) {
		if (sharedFolders.size() >= 20) {
			JOptionPane.showMessageDialog(this, "You can add a maximum of 20 folders to the list");
			return;
		}

		FolderEntry fe = new FolderEntry(folderPath);
		fe.addRemoveListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sharedFolders.remove(e.getSource());
				rebuildPanel();
			}
		});
		sharedFolders.add(fe);
		rebuildPanel();
	}

	public void rebuildPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		PanelBuilder conBuilder;
		FormLayout conLayout = new FormLayout("fill:10:grow", // columns
		        "p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, p, fill:10:grow"); // rows
		conBuilder = new PanelBuilder(conLayout);
		conBuilder.setOpaque(true);

		// Conditions
		if (sharedFolders.size() > 0) {
			// Add conditions if we've got any
			for (int i = 0; i < this.sharedFolders.size(); i++) {
				conBuilder.add(this.sharedFolders.get(i), cc.xy(1, i + 1));
			}
		} else {
			// Show the 'no shared folders set' label if there are none
			conBuilder.add(pNoSharedFoldersSet, cc.xy(1, 21, CellConstraints.CENTER, CellConstraints.CENTER));
		}

		FormLayout layout = new FormLayout("fill:10:grow", // columns
		        "fill:10:grow, 3px, p"); // raws
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		JScrollPane spConditions = new JScrollPane(conBuilder.getPanel());
		spConditions.setBorder(null);
		builder.add(spConditions, cc.xy(1, 1));

		JPanel pButton = new JPanel();
		pButton.add(bAddFolder);
		builder.add(pButton, cc.xy(1, 3));

		removeAll();
		add(builder.getPanel());
		validate();
	}
}
