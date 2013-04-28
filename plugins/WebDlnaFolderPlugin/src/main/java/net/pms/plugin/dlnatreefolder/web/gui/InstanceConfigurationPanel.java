package net.pms.plugin.dlnatreefolder.web.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.PMS;
import net.pms.plugin.dlnatreefolder.WebFolderPlugin;
import net.pms.plugin.dlnatreefolder.web.configuration.InstanceConfiguration;

public class InstanceConfigurationPanel extends JPanel {
	private static final long serialVersionUID = 7575898774586101894L;
	private static final Logger log = LoggerFactory.getLogger(InstanceConfigurationPanel.class);
	
	private String webFilePath;
	
	private JLabel lHeader;
	private JTextField tfWebFilePath;
	private JButton bBrowse;
	private JButton bEdit;

	public InstanceConfigurationPanel(String webFilePath) {
		setLayout(new GridLayout());
		this.webFilePath = webFilePath;
		
		init();
		build();
	}

	private void init() {
		lHeader = new JLabel(WebFolderPlugin.messages.getString("InstanceConfigurationPanel.1"));
		tfWebFilePath = new JTextField(webFilePath);
		tfWebFilePath.setPreferredSize(new Dimension(200, tfWebFilePath.getPreferredSize().height));
		bBrowse = new JButton(WebFolderPlugin.messages.getString("InstanceConfigurationPanel.2"));
		bBrowse.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File currentFolder = new File(tfWebFilePath.getText()).getParentFile();
				JFileChooser fc;
				if(currentFolder.exists()) {
					fc = new JFileChooser(currentFolder);
				} else {
					fc = new JFileChooser();
				}
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setFileFilter(new FileNameExtensionFilter("configuration files (*.conf)", "conf"));
				
				if(fc.showOpenDialog(bBrowse) == JFileChooser.APPROVE_OPTION) {
					tfWebFilePath.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		bEdit = new JButton(WebFolderPlugin.messages.getString("InstanceConfigurationPanel.3"));
		bEdit.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				File webFile = new File(tfWebFilePath.getText());
				if(webFile.exists()) {
					try {
						java.awt.Desktop.getDesktop().open(webFile);
					} catch (IOException e1) {
						log.error(String.format("Failed to open file %s in default editor", webFile), e1);
						JOptionPane.showMessageDialog(bEdit, String.format(WebFolderPlugin.messages.getString("InstanceConfigurationPanel.5"), webFile));
					}
				} else {
					JOptionPane.showMessageDialog(bEdit, WebFolderPlugin.messages.getString("InstanceConfigurationPanel.4"));
				}
			}
		});
	}

	/**
	 * Builds the panel
	 */
	private void build() {
		// Set basic layout
		FormLayout layout = new FormLayout("5px, p, 5px, f:200:g, 5px, p, 10px, p, 5px", //columns
				"5px, p, f:5px:g"); //rows
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.add(lHeader, cc.xy(2, 2));
		builder.add(tfWebFilePath, cc.xy(4, 2));
		builder.add(bBrowse, cc.xy(6, 2));
		builder.add(bEdit, cc.xy(8, 2));
		
		removeAll();
		add(builder.getPanel());
	}

	/**
	 * Updates the configuration to reflect the GUI
	 *
	 * @param gc the global configuration
	 */
	public void updateConfiguration(InstanceConfiguration gc) {
		gc.setFilePath(tfWebFilePath.getText());
	}
	
	/**
	 * Updates all graphical components to show the global configuration.<br>
	 * This is being used to roll back changes after editing properties and
	 * canceling the dialog.
	 */
	public void applyConfig() {
		PMS.getConfiguration().getProfileDirectory();
		tfWebFilePath.setText(webFilePath);
	}
}
