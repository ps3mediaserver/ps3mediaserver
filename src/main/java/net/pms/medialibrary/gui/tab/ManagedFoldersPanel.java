package net.pms.medialibrary.gui.tab;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.events.FileImportDialogListener;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.gui.dialogs.FileImportTemplateDialog;
import net.pms.medialibrary.gui.shared.EButton;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class ManagedFoldersPanel extends JPanel {
	private static final long      serialVersionUID = 1558319355911044800L;
//	private static final Logger    log              = Logger.getLogger(ManagedFoldersPanel.class);

	private final int              MAX_FOLDERS      = 40;
	private JButton                bAddFolder;
	private List<ManagedFolderObj> managedFolders   = new ArrayList<ManagedFolderObj>();
	private IMediaLibraryStorage   storage;

	public ManagedFoldersPanel() {
		setLayout(new GridLayout());
		init();

		storage = MediaLibraryStorage.getInstance();
		addManagedFolders(storage.getManagedFolders());
		applyLayout();
	}

	private void addManagedFolders(List<DOManagedFile> mFolder) {
		for (DOManagedFile f : mFolder) {
			addManagedFolder(f);
		}
	}

	private void addManagedFolder(DOManagedFile f) {
		if (getManagedFolders().size() < MAX_FOLDERS) {
			JCheckBox cbWatch = new JCheckBox();
			cbWatch.setSelected(f.isWatchEnabled());
			JTextField tfFolderPath = new JTextField(f.getPath());
			JButton bBrowse = new JButton(Messages.getString("ML.ManagedFoldersPanel.bBrowse"));
			JCheckBox cbVideo = new JCheckBox();
			cbVideo.setSelected(f.isVideoEnabled());
			JCheckBox cbAudio = new JCheckBox();
			cbAudio.setSelected(f.isAudioEnabled());
			JCheckBox cbPictures = new JCheckBox();
			cbPictures.setSelected(f.isPicturesEnabled());
			JButton bScan = new JButton(Messages.getString("ML.ManagedFoldersPanel.bScan"));
			JButton bDelete = new JButton(new ImageIcon(getClass().getResource("/resources/images/tp_remove.png")));			
			JCheckBox cbSubFolders = new JCheckBox();
			cbSubFolders.setSelected(f.isSubFoldersEnabled());
			JCheckBox cbEnablePlugins = new JCheckBox();
			cbEnablePlugins.setSelected(f.isFileImportEnabled());
			
			EButton bConfigureFileImportTemplate = new EButton(Messages.getString("ML.ScanFolderDialog.bConfigure"), f.getFileImportTemplate());
			bConfigureFileImportTemplate.setEnabled(f.isFileImportEnabled());
			bConfigureFileImportTemplate.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					int templateId = 1;
					
					final EButton b = (EButton)e.getSource();
					Object uo = b.getUserObject();
					if(uo instanceof DOFileImportTemplate) {
						templateId = ((DOFileImportTemplate)uo).getId();
					}
					
					//show the dialog
					FileImportTemplateDialog vid = new FileImportTemplateDialog(SwingUtilities.getWindowAncestor(b), templateId);
					vid.setLocation(GUIHelper.getCenterDialogOnParentLocation(vid.getPreferredSize(), b));
					vid.setResizable(false);
					vid.setModal(true);
					
					vid.addFileImportDialogListener(new FileImportDialogListener() {
						
						@Override
						public void templateSaved(DOFileImportTemplate fileImportTemplate) {
							b.setUserObject(fileImportTemplate);
							storage.setManagedFolders(getManagedFolders());
						}
					});
					
					vid.pack();
					vid.setVisible(true);
					
					if(vid.isSave()) {
						templateId = vid.getFileImportTemplateId();
					}
					
					b.setUserObject(storage.getFileImportTemplate(templateId));
						
					//Store the media library folders in the db when a template changes
					//to be able to check if a template is being used when deleting one
					cleanManagedFolders();
					storage.setManagedFolders(getManagedFolders());						
				}
			});

			ManagedFolderObj obj = new ManagedFolderObj(cbWatch, tfFolderPath, cbVideo, bConfigureFileImportTemplate, cbAudio, cbPictures, bBrowse, bScan, bDelete, cbSubFolders, cbEnablePlugins, managedFolders.size());
			managedFolders.add(obj);

			obj.addRemoveListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					managedFolders.remove(((ManagedFolderObj) e.getSource()).getIndex());
					applyLayout();
				}
			});
		} else {
			JOptionPane.showMessageDialog(this, String.format(Messages.getString("ML.ManagedFoldersPanel.FolderLimitMsg"), MAX_FOLDERS));
		}
	}

	public List<DOManagedFile> getManagedFolders() {
		List<DOManagedFile> mf = new ArrayList<DOManagedFile>();
		for (ManagedFolderObj obj : managedFolders) {
			DOManagedFile f = new DOManagedFile(obj.getCbWatch().isSelected(), obj.getTfFolderPath().getText(), obj.getCbVideo().isSelected(), obj.getCbAudio()
			        .isSelected(), obj.getCbPictures().isSelected(), obj.getCbSubFolders().isSelected(), obj.getCbEnablePlugins().isSelected(), obj.getFileImportTemplate());
			if(!mf.contains(f)) {
				mf.add(f);
			}
		}

		return mf;
	}

	public void cleanManagedFolders() {
		List<DOManagedFile> mf =  getManagedFolders();

		if (mf.size() != managedFolders.size()) {
			managedFolders.clear();
			addManagedFolders(mf);
			applyLayout();
		}
	}

	private void init() {
		bAddFolder = new JButton(Messages.getString("ML.ManagedFoldersPanel.bAddFolder"));
		bAddFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DOManagedFile mf = new DOManagedFile();
				mf.setFileImportTemplate(storage.getFileImportTemplate(1));
				addManagedFolder(mf);
				applyLayout();
			}
		});
	}

	private void applyLayout() {
		FormLayout layout = new FormLayout("fill:p:grow", // columns
		        "fill:10:grow, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();

		builder.add(bAddFolder, cc.xy(1, 2, CellConstraints.CENTER, CellConstraints.BOTTOM));

		FormLayout layout2 = new FormLayout(
		        "center:p, 2px, 20:grow, 2px, p, 2px, p, 10px, center:p, 2px, center:p, 2px, center:p, 4px, p, 2px, p, 10px, p", // columns
		        "p, p, p, p, p, p, p, p, p, p," + // rows (40)
		        "p, p, p, p, p, p, p, p, p, p," + 
		        "p, p, p, p, p, p, p, p, p, p," + 
		        "p, p, p, p, p, p, p, p, p, p");
		PanelBuilder builder2 = new PanelBuilder(layout2);

		// show folders if there are any
		JPanel pManagedFolders;
		if (managedFolders.size() > 0) {
			//create labels with tooltips
			JLabel lSubFolders = new JLabel(new ImageIcon(getClass().getResource("/resources/images/subfolders-16.png")));
			lSubFolders.setToolTipText(Messages.getString("ML.ManagedFoldersPanel.lSubfolders"));
			JLabel lVideo = new JLabel(new ImageIcon(getClass().getResource("/resources/images/videofolder-16.png")));
			lVideo.setToolTipText(Messages.getString("ML.ManagedFoldersPanel.lVideo"));
			JLabel lAudio = new JLabel(new ImageIcon(getClass().getResource("/resources/images/audiofolder-16.png")));
			lAudio.setToolTipText(Messages.getString("ML.ManagedFoldersPanel.lAudio"));
			JLabel lPictures = new JLabel(new ImageIcon(getClass().getResource("/resources/images/picturesfolder-16.png")));
			lPictures.setToolTipText(Messages.getString("ML.ManagedFoldersPanel.lPictures"));

			// set headers
			builder2.add(lSubFolders, cc.xy(1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
			builder2.addLabel(Messages.getString("ML.ManagedFoldersPanel.lFolderPath"), cc.xy(3, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
			builder2.add(lVideo, cc.xy(9, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
			builder2.add(lAudio, cc.xy(11, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
			builder2.add(lPictures, cc.xy(13, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
			builder2.addLabel(Messages.getString("ML.ManagedFoldersPanel.lPlugins"), cc.xyw(15, 1, 3, CellConstraints.CENTER, CellConstraints.DEFAULT));

			int rowIndex = 2;
			for (ManagedFolderObj f : managedFolders) {
				f.setIndex(rowIndex - 2);
				builder2.add(f.getCbSubFolders(), cc.xy(1, rowIndex));
				builder2.add(f.getTfFolderPath(), cc.xy(3, rowIndex));
				builder2.add(f.getbBrowse(), cc.xy(5, rowIndex));
				builder2.add(f.getbDelete(), cc.xy(7, rowIndex));
				builder2.add(f.getCbVideo(), cc.xy(9, rowIndex));
				builder2.add(f.getCbAudio(), cc.xy(11, rowIndex));
				builder2.add(f.getCbPictures(), cc.xy(13, rowIndex));
				builder2.add(f.getCbEnablePlugins(), cc.xy(15, rowIndex));
				builder2.add(f.getbConfigureFileImportTemplate(), cc.xy(17, rowIndex));
				builder2.add(f.getbScan(), cc.xy(19, rowIndex));
				rowIndex++;
			}
			pManagedFolders = builder2.getPanel();
		} else {
			pManagedFolders = new JPanel(new GridLayout());
			pManagedFolders.add(new JLabel(Messages.getString("ML.ManagedFoldersPanel.pNoFoldersConfigures"), JLabel.CENTER));
		}

		JScrollPane sp = new JScrollPane(pManagedFolders);
		sp.setBorder(BorderFactory.createEmptyBorder());
		builder.add(sp, cc.xy(1, 1));

		removeAll();
		add(builder.getPanel());
		validate();
	}
}
