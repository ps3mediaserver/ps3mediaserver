package net.pms.medialibrary.gui.dialogs;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.gui.shared.FileImportTemplatePanel;
import net.pms.medialibrary.storage.MediaLibraryStorage;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ScanFolderDialog extends JDialog {
	private static final long serialVersionUID = 4321886059086383731L;
	private final int MIN_BUTTON_WIDTH = 60;
	private ScanFolderDialog instance;
	private JTextField tfFolderPath;
	private JButton bBrowseFolderPath;
	private JCheckBox cbScanVideo;
	private JCheckBox cbScanAudio;
	private JCheckBox cbScanPictures;
	private JCheckBox cbScanSubFolders;
	private JCheckBox cbUseFileImportPlugins;
	private FileImportTemplatePanel pFileImport;

	private JButton bImport;
	private JButton bCancel;
	private JButton bSave;

	private boolean doImport = false;

	public ScanFolderDialog() {
		this("");
	}

	public void setDoImport(boolean doImport) {
		this.doImport = doImport;
	}

	public ScanFolderDialog(String folderPath) {
		setIconImage(new ImageIcon(
				FileImportTemplateDialog.class
						.getResource("/resources/images/icon-32.png"))
				.getImage());
		setTitle(Messages.getString("ML.ScanFolderDialog.Title"));

		build(folderPath);
		instance = this;
	}

	public boolean isDoImport() {
		return doImport;
	}

	public DOManagedFile getManagedFolder() {
		DOManagedFile f = new DOManagedFile();
		f.setPath(tfFolderPath.getText());
		f.setVideoEnabled(cbScanVideo.isSelected());
		f.setAudioEnabled(cbScanAudio.isSelected());
		f.setPicturesEnabled(cbScanPictures.isSelected());
		f.setSubFoldersEnabled(cbScanSubFolders.isSelected());
		f.setFileImportTemplate(pFileImport.getDisplayedTemplate());
		f.setWatchEnabled(false);

		return f;
	}

	private void build(String folderPath) {
		FormLayout layout = new FormLayout(
				"5px, p, 5px, fill:p:grow, 5px, p, 5px, p, 5px",
				"p, 2px,  p, 5px,  p, 2px,  p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();

		// line 1
		builder.addLabel(Messages.getString("ML.ScanFolderDialog.lFolderPath"),
				cc.xy(2, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		tfFolderPath = new JTextField(folderPath);
		tfFolderPath.setMinimumSize(new Dimension(300, tfFolderPath
				.getPreferredSize().height));
		builder.add(tfFolderPath, cc.xy(4, 1));
		bBrowseFolderPath = new JButton(
				Messages.getString("ML.ScanFolderDialog.bBrowse"));
		bBrowseFolderPath.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser fc = new JFileChooser(bBrowseFolderPath.getText());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					tfFolderPath
							.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}

		});
		builder.add(bBrowseFolderPath, cc.xy(6, 1));
		cbScanSubFolders = new JCheckBox(
				Messages.getString("ML.ScanFolderDialog.cbScanSubFolders"));
		cbScanSubFolders.setSelected(true);
		builder.add(cbScanSubFolders, cc.xy(8, 1));

		// line 2
		builder.addLabel(Messages.getString("ML.ScanFolderDialog.lLookFor"),
				cc.xy(2, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));

		JPanel pFileTypes = new JPanel(new FlowLayout());
		cbScanVideo = new JCheckBox(
				Messages.getString("ML.ScanFolderDialog.cbScanVideo"), true);
		pFileTypes.add(cbScanVideo);

		cbScanAudio = new JCheckBox(
				Messages.getString("ML.ScanFolderDialog.cbScanAudio"), true);
		pFileTypes.add(cbScanAudio);

		cbScanPictures = new JCheckBox(
				Messages.getString("ML.ScanFolderDialog.cbScanPictures"), true);
		pFileTypes.add(cbScanPictures);

		builder.add(pFileTypes,
				cc.xy(4, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));

		cbUseFileImportPlugins = new JCheckBox(
				Messages.getString("ML.ScanFolderDialog.cbUsePlugins"));
		cbUseFileImportPlugins.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (cbUseFileImportPlugins.isSelected()) {
					pFileImport.setVisible(true);
				} else {
					pFileImport.setVisible(false);
				}
				pack();
			}
		});
		builder.add(cbUseFileImportPlugins,
				cc.xy(8, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));

		// file import
		pFileImport = new FileImportTemplatePanel(1);
		pFileImport.setVisible(false);
		pFileImport.addRepaintListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				pack();
			}
		});
		builder.add(pFileImport, cc.xyw(2, 5, 7));

		// buttons
		bImport = new JButton(Messages.getString("ML.ScanFolderDialog.bImport"));
		if (bImport.getPreferredSize().width < MIN_BUTTON_WIDTH)
			bImport.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bImport
					.getPreferredSize().height));
		bImport.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				save();
				
				File selectedFolder = new File(tfFolderPath.getText());
				if (selectedFolder.isDirectory()) {
					setDoImport(true);
					instance.setVisible(false);
				} else {
					JOptionPane.showMessageDialog(
							null,
							Messages.getString("ML.ScanFolderDialog.InvalidPathMsg"),
							Messages.getString("ML.ScanFolderDialog.InvalidPathTitle"),
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		bCancel = new JButton(Messages.getString("ML.ScanFolderDialog.bCancel"));
		if (bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH)
			bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel
					.getPreferredSize().height));
		bCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setDoImport(false);
				instance.setVisible(false);
			}
		});
		bSave = new JButton(Messages.getString("ML.ScanFolderDialog.bSave"));
		if (bSave.getPreferredSize().width < MIN_BUTTON_WIDTH)
			bSave.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bSave
					.getPreferredSize().height));
		bSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				save();
			}
		});

		JPanel bPanel = new JPanel();
		bPanel.setAlignmentX(CENTER_ALIGNMENT);
		bPanel.add(bImport);
		bPanel.add(bSave);
		bPanel.add(bCancel);
		builder.add(bPanel, cc.xyw(2, 7, 7));

		getContentPane().add(builder.getPanel());
		pack();
	}

	private DOFileImportTemplate save() {
		DOFileImportTemplate template = pFileImport.getDisplayedTemplate();
		if(template.getName() == null || template.getName().equals("")) {
			JOptionPane.showMessageDialog(this, Messages.getString("ML.FileImportConfigurationPanel.Msg.EnterTemplateName"));
			return null;
		}
		
		// insert or update into db. A new template has the ID=0
		if (template.getId() > 0) {
			MediaLibraryStorage.getInstance().updateFileImportTemplate(template);
		} else {
			MediaLibraryStorage.getInstance().insertFileImportTemplate(template);
		}

		pFileImport.templateSaved(template);
		
		return template;
	}
}
