package net.pms.plugin.dlnatreefolder.itunes.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.plugin.dlnatreefolder.iTunesFolderPlugin;
import net.pms.plugin.dlnatreefolder.itunes.configuration.InstanceConfiguration;

public class InstanceConfigurationPanel extends JPanel{
	private static final long serialVersionUID = -1617763456027028861L;
	
	private InstanceConfiguration globalConfig;
	
	private JLabel lHeader;
	private JButton bBrowse;
	private JTextField tfiTinesFilePath;

	public InstanceConfigurationPanel(InstanceConfiguration globalConfig) {
		setLayout(new GridLayout());
		this.globalConfig = globalConfig;
		init();
		build();
	}
	
	/**
	 * Updates all graphical components to show the global configuration.<br>
	 * This is being used to roll back changes after editing properties and
	 * canceling the dialog.
	 */
	public void applyConfig() {
		tfiTinesFilePath.setText(globalConfig.getiTunesFilePath());
	}

	/**
	 * Initializes the graphical components
	 */
	private void init() {
		lHeader = new JLabel(iTunesFolderPlugin.messages.getString("GlobalConfigurationPanel.1"));
		tfiTinesFilePath = new JTextField();
		bBrowse = new JButton(iTunesFolderPlugin.messages.getString("GlobalConfigurationPanel.2"));
		bBrowse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File currentFolder = new File(tfiTinesFilePath.getText()).getParentFile();
				JFileChooser fc;
				if(currentFolder.exists()) {
					fc = new JFileChooser(currentFolder);
				} else {
					fc = new JFileChooser();
				}
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setFileFilter(new FileNameExtensionFilter("xml files (*.xml)", "xml"));
				
				if(fc.showOpenDialog(bBrowse) == JFileChooser.APPROVE_OPTION) {
					tfiTinesFilePath.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
	}

	/**
	 * Builds the panel
	 */
	private void build() {
		// Set basic layout
		FormLayout layout = new FormLayout("5px, p, 5px, f:200:g, 5px, p, 5px", //columns
				"5px, p, f:5px:g"); //rows
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.add(lHeader, cc.xy(2, 2));
		builder.add(tfiTinesFilePath, cc.xy(4, 2));
		builder.add(bBrowse, cc.xy(6, 2));
		
		removeAll();
		add(builder.getPanel());
	}

	/**
	 * Updates the configuration to reflect the GUI
	 *
	 * @param gc the global configuration
	 */
	public void updateConfiguration(InstanceConfiguration gc) {
		gc.setiTunesFilePath(tfiTinesFilePath.getText());
	}
}
