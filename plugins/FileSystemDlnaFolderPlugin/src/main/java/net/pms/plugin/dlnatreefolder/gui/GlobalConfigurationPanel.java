package net.pms.plugin.dlnatreefolder.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.newgui.RestrictedFileSystemView;
import net.pms.plugin.dlnatreefolder.FileSystemFolderPlugin;
import net.pms.plugin.dlnatreefolder.configuration.GlobalConfiguration;
import net.pms.util.KeyedComboBoxModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class GlobalConfigurationPanel extends JPanel {
	private static final long serialVersionUID = 6133803510518388833L;
	private static final Logger log = LoggerFactory.getLogger(GlobalConfigurationPanel.class);

	private JCheckBox cbHideTranscode;
	private JCheckBox cbHideExtensions;
	private JCheckBox cbHideEmptyFolders;
	private JCheckBox cbGenerateThumbs;
	private JCheckBox cbDvdIsoThumbs;
	private JCheckBox cbImageThumbs;
	private JComboBox cbAudioThumbs;
	private JComboBox cbSortMethod;
	private JCheckBox cbBrowseArchives;
	private JCheckBox cbHideEngineNames;

	private JTextField tfSeekPos;
	private JTextField tfDefaultThumbFolder;

	private JButton bBrowseAlternateThumbFolder;
	
	private final GlobalConfiguration globalConfig;


	public GlobalConfigurationPanel(GlobalConfiguration globalConfig) {
		setLayout(new GridLayout());
		this.globalConfig = globalConfig;
		init();
		build();
	}
	
	public void applyConfig() {		
		cbGenerateThumbs.setSelected(globalConfig.isThumbnailGenerationEnabled());
		cbDvdIsoThumbs.setSelected(globalConfig.isDvdIsoThumbnailsEnabled());
		cbImageThumbs.setSelected(globalConfig.getImageThumbnailsEnabled());
		((KeyedComboBoxModel) cbAudioThumbs.getModel()).setSelectedKey(String.valueOf(globalConfig.getAudioThumbnailMethod()));
		tfDefaultThumbFolder.setText(globalConfig.getAlternateThumbFolder());
		cbBrowseArchives.setSelected(globalConfig.isBrowseArchives());
		cbHideTranscode.setSelected(globalConfig.isHideTranscodeEnabled());
		cbHideEngineNames.setSelected(globalConfig.isHideEngineNames());
		cbHideExtensions.setSelected(globalConfig.isHideExtensions());
		cbHideEmptyFolders.setSelected(globalConfig.isHideEmptyFolders());
		((KeyedComboBoxModel) cbSortMethod.getModel()).setSelectedKey(String.valueOf(globalConfig.getSortMethod()));
	}

	private void init() {
		// Thumbnails
		
		// Generate thumbnails
		cbGenerateThumbs = new JCheckBox(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.1"));
		cbGenerateThumbs.setContentAreaFilled(false);

		//ThumbnailSeekPos
		tfSeekPos = new JTextField(String.valueOf(globalConfig.getThumbnailSeekPosSec()));
		tfSeekPos.setPreferredSize(new Dimension(60, tfSeekPos.getPreferredSize().height));

		// DvdIsoThumbnails
		cbDvdIsoThumbs = new JCheckBox(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.3"));

		// ImageThumbnailsEnabled
		cbImageThumbs = new JCheckBox(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.4"));

		// AudioThumbnailMethod
		KeyedComboBoxModel thumbKCBM = new KeyedComboBoxModel(new Object[]{"0", "1", "2"}, new Object[]{ 
				FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.5"), 
				FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.6"), 
				FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.7")});
		cbAudioThumbs = new JComboBox(thumbKCBM);
		cbAudioThumbs.setEditable(false);

		// AlternateThumbFolder
		tfDefaultThumbFolder = new JTextField();

		// AlternateThumbFolder: select
		bBrowseAlternateThumbFolder = new JButton("...");
		bBrowseAlternateThumbFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = null;
				try {
					chooser = new JFileChooser();
				} catch (Exception ee) {
					chooser = new JFileChooser(new RestrictedFileSystemView());
				}
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showDialog((Component) e.getSource(), FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.8"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					tfDefaultThumbFolder.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		//Navigation & parsing
		cbBrowseArchives = new JCheckBox(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.22"));
		cbHideTranscode = new JCheckBox(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.9"));
		cbHideEngineNames = new JCheckBox(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.23"));
		cbHideExtensions = new JCheckBox(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.10"));
		cbHideEmptyFolders = new JCheckBox(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.11"));

		// sort method
		final KeyedComboBoxModel kcbm = new KeyedComboBoxModel(
			new Object[]{
				"0", // alphabetical
				"4", // natural sort
				"3", // ASCIIbetical
				"1", // newest first
				"2"  // oldest first
			},
			new Object[]{
				FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.12"),
				FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.13"),
				FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.14"),
				FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.15"),
				FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.16")
			}
		);
		cbSortMethod = new JComboBox(kcbm);
		cbSortMethod.setEditable(false);
		kcbm.setSelectedKey(String.valueOf(globalConfig.getSortMethod()));
	}

	private void build() {
		// Set basic layout
		FormLayout layout = new FormLayout("5px, p, 5px, f:p:g, 5px", //columns
				"5px, p, 5px, p, 5px, p, 5px, p, 5px, p, 5px, p, 5px, p, 5px, p, 5px, p, 5px, p, f:5px:g"); //rows
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();

		//thumbnails
		JComponent cmp = builder.addSeparator(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.17"), cc.xyw(2, 2, 3));
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		//generate thumbnails
		builder.add(cbGenerateThumbs, cc.xy(2, 4));
		
		//thumbnail seek pos
		JPanel pSeekPos = new JPanel(new BorderLayout());
		pSeekPos.add(new JLabel(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.18")), BorderLayout.LINE_START);
		pSeekPos.add(tfSeekPos, BorderLayout.CENTER);
		builder.add(pSeekPos, cc.xy(4, 4));

		builder.add(cbDvdIsoThumbs, cc.xy(2, 6));
		builder.add(cbImageThumbs, cc.xy(4, 6));

		//audio thumbs
		builder.addLabel(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.19"), cc.xy(2, 8));
		builder.add(cbAudioThumbs, cc.xy(4, 8));

		//alternate cover folder
		JPanel pAlternateCoverFolder = new JPanel(new BorderLayout());
		pAlternateCoverFolder.add(tfDefaultThumbFolder, BorderLayout.CENTER);
		pAlternateCoverFolder.add(bBrowseAlternateThumbFolder, BorderLayout.EAST);
		builder.addLabel(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.20"), cc.xy(2, 10));
		builder.add(pAlternateCoverFolder, cc.xy(4, 10));
		
		//navigation/parsing
		cmp = builder.addSeparator(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.21"), cc.xyw(2, 12, 3));
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		builder.add(cbBrowseArchives, cc.xy(2, 14));
		builder.add(cbHideTranscode, cc.xy(4, 14));
		builder.add(cbHideExtensions, cc.xy(2, 16));
		builder.add(cbHideEngineNames, cc.xy(4, 16));
		builder.add(cbHideEmptyFolders, cc.xyw(2, 18, 3));
		
		builder.addLabel(FileSystemFolderPlugin.messages.getString("GlobalConfigurationPanel.24"), cc.xy(2, 20));
		builder.add(cbSortMethod, cc.xy(4, 20));
		
		JScrollPane sp = new JScrollPane(builder.getPanel());
		sp.setBorder(BorderFactory.createEmptyBorder());
		
		add(sp);
	}

	public void updateConfiguration(GlobalConfiguration gc) {
		gc.setAlternateThumbFolder(tfDefaultThumbFolder.getText());
		gc.setAudioThumbnailMethod(Integer.parseInt((String) ((KeyedComboBoxModel)cbAudioThumbs.getModel()).getSelectedKey()));
		gc.setBrowseArchives(cbBrowseArchives.isSelected());
		gc.setDvdIsoThumbnailsEnabled(cbDvdIsoThumbs.isSelected());
		gc.setHideEmptyFolders(cbHideEmptyFolders.isSelected());
		gc.setHideEngineNames(cbHideEngineNames.isSelected());
		gc.setHideExtensions(cbHideExtensions.isSelected());
		gc.setHideTranscodeEnabled(cbHideTranscode.isSelected());
		gc.setImageThumbnailsEnabled(cbImageThumbs.isSelected());
		gc.setSortMethod(Integer.parseInt((String) ((KeyedComboBoxModel)cbSortMethod.getModel()).getSelectedKey()));
		gc.setThumbnailGenerationEnabled(cbGenerateThumbs.isSelected());
		
		int seekPos = 30;
		try {
			seekPos = Integer.parseInt(tfSeekPos.getText());
		} catch(NumberFormatException ex) {
			log.error(String.format("Failed to parse thumbnail seek position '%s' as an integer", tfSeekPos.getText()));
		}
		gc.setThumbnailSeekPosSec(seekPos);
	}
}
