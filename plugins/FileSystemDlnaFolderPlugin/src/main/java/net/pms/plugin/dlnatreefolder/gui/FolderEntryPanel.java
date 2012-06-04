package net.pms.plugin.dlnatreefolder.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.pms.plugin.dlnatreefolder.FileSystemFolderPlugin;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FolderEntryPanel extends JPanel {
	private static final long    serialVersionUID = 436739054166843859L;

	private JTextField           tfFolderPath;
	private JButton              bBrowse;
	private JButton              bRemove;

	private List<ActionListener> removeListeners  = new ArrayList<ActionListener>();

	public FolderEntryPanel() {
		this("");
	}

	public FolderEntryPanel(String folderPath) {
		setLayout(new GridLayout());

		init();
		rebuildPanel();

		setFolderPath(folderPath);
	}

	public String getFolderPath() {
		return tfFolderPath.getText();
	}

	public void setFolderPath(String path) {
		tfFolderPath.setText(path);
	}

	private void init() {
		tfFolderPath = new JTextField();

		bBrowse = new JButton(FileSystemFolderPlugin.messages.getString("FolderEntry.1"));
		bBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				if (fc.showOpenDialog(getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
					tfFolderPath.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});

		bRemove = new JButton(new ImageIcon(getClass().getResource("/resources/images/tp_remove.png")));
		bRemove.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireRemoveListener();
			}
		});
	}

	public void addRemoveListener(ActionListener l) {
		removeListeners.add(l);
	}

	private void fireRemoveListener() {
		for (ActionListener l : removeListeners) {
			l.actionPerformed(new ActionEvent(this, 0, ""));
		}
	}

	private void rebuildPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("p, 3px, fill:10:grow, 3px, p, 3px, p", // columns
		        "p"); // raws
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.addLabel(FileSystemFolderPlugin.messages.getString("FolderEntry.2"), cc.xy(1, 1));
		builder.add(tfFolderPath, cc.xy(3, 1));
		builder.add(bBrowse, cc.xy(5, 1));
		builder.add(bRemove, cc.xy(7, 1));

		removeAll();
		add(builder.getPanel());
	}
}
