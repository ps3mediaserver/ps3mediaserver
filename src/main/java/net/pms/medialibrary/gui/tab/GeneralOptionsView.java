package net.pms.medialibrary.gui.tab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.MediaLibraryConfiguration;
import net.pms.medialibrary.commons.dataobjects.OmitPrefixesConfiguration;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.MediaLibraryConstants.MetaDataKeys;
import net.pms.medialibrary.commons.enumarations.ScanState;
import net.pms.medialibrary.commons.exceptions.InitialisationException;
import net.pms.medialibrary.commons.exceptions.ScanStateException;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.commons.helpers.TmdbHelper;
import net.pms.medialibrary.commons.interfaces.IFileScannerEventListener;
import net.pms.medialibrary.commons.interfaces.ILibraryManagerEventListener;
import net.pms.medialibrary.gui.dialogs.ScanFolderDialog;
import net.pms.medialibrary.library.LibraryManager;
import net.pms.medialibrary.scanner.FileScanner;
import net.pms.medialibrary.storage.MediaLibraryStorage;

import com.github.savvasdalkitsis.jtmdb.Session;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class GeneralOptionsView extends JPanel {
	private static final Logger log = LoggerFactory.getLogger(GeneralOptionsView.class);
	private static final long         serialVersionUID = -3580997978960568334L;

	private LibraryManager            libraryManager;
	private MediaLibraryConfiguration libConfig;
	private ScanState                 scanState;
	private JLabel                    lVideoCount;
	private JButton                   bClearVideo;
	private JLabel                    lAudioCount;
	private JButton                   bClearAudio;
	private JLabel                    lPicturesCount;
	private JButton                   bClearPictures;
	private JLabel                    lScanState;
	private JButton                   bStartPauseScan;
	private JButton                   bStopScan;
	private JButton                   bScanFolder;
	private JTextField                tfPictureFolderPathValue;
	private JCheckBox                 cbEnableMediaLibrary;
	private JLabel                    lOmitPrefix;
	private JTextField                tfOmitPrefix;
	private JCheckBox                 cbOmitFiltering;
	private JCheckBox                 cbOmitSorting;
	private JButton                   bApply;
	private ManagedFoldersPanel       pManagedFolders;

	private JComponent                pOptions;

	private JLabel lTmdbUser;

	public GeneralOptionsView() {
		libConfig = MediaLibraryConfiguration.getInstance();

		try {
			this.libraryManager = LibraryManager.getInstance();
		} catch (InitialisationException ex) {
			log.error("Failed to get LibraryManager", ex);
			return;
		}

		setLayout(new BorderLayout());
		add(buildUseMediaLibrary(), BorderLayout.NORTH);
		pOptions = build();
		add(pOptions, BorderLayout.CENTER);

		this.scanState = libraryManager.getScanState().getScanState();
		updateScanState();
		pOptions.setVisible(cbEnableMediaLibrary.isSelected());
	}

	private JComponent buildUseMediaLibrary() {
		FormLayout layout = new FormLayout("10:grow", "p, 7px, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		CellConstraints cc = new CellConstraints();

		// Header
		JComponent sGeneral = builder.addSeparator(Messages.getString("ML.GeneralOptionsView.sGeneral"), cc.xy(1, 1));
		sGeneral = (JComponent) sGeneral.getComponent(0);
		sGeneral.setFont(sGeneral.getFont().deriveFont(Font.BOLD));

		// Enable
		cbEnableMediaLibrary = new JCheckBox(Messages.getString("ML.GeneralOptionsView.cbEnableMediaLibrary"));
		builder.add(cbEnableMediaLibrary, cc.xy(1, 3, CellConstraints.LEFT, CellConstraints.CENTER));

		cbEnableMediaLibrary.setSelected(libConfig.isMediaLibraryEnabled());

		cbEnableMediaLibrary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pOptions.setVisible(cbEnableMediaLibrary.isSelected());
				libConfig.setMediaLibraryEnabled(cbEnableMediaLibrary.isSelected());
			}
		});

		return builder.getPanel();
	}

	private JComponent build() {
		this.libraryManager.addFileScannerEventListener(new IFileScannerEventListener() {

			@Override
			public void scanStateChanged(ScanState state) {
				scanState = state;
				updateScanState();
			}

			@Override
			public void itemInserted(FileType type) {
				switch (type) {
					case VIDEO:
						int currVal = Integer.parseInt(lVideoCount.getText());
						lVideoCount.setText(String.valueOf(++currVal));
						break;
					case AUDIO:
						currVal = Integer.parseInt(lAudioCount.getText());
						lAudioCount.setText(String.valueOf(++currVal));
						break;
					case PICTURES:
						currVal = Integer.parseInt(lPicturesCount.getText());
						lPicturesCount.setText(String.valueOf(++currVal));
						break;
				}
			}
		});
		this.libraryManager.addLibraryManagerEventListener(new ILibraryManagerEventListener() {

			@Override
			public void itemCountChanged(int itemCount, FileType type) {
				switch (type) {
					case VIDEO:
						lVideoCount.setText(String.valueOf(itemCount));
						break;
					case AUDIO:
						lAudioCount.setText(String.valueOf(itemCount));
						break;
					case PICTURES:
						lPicturesCount.setText(String.valueOf(itemCount));
						break;
				}
			}
		});

		bApply = new JButton(Messages.getString("ML.ManagedFoldersPanel.bApply"));
		bApply.setToolTipText(Messages.getString("ML.ManagedFoldersPanel.bApply.ToolTipText"));
		bApply.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				pManagedFolders.cleanManagedFolders();
				MediaLibraryStorage.getInstance().setManagedFolders(pManagedFolders.getManagedFolders());

				libConfig.setPictureSaveFolderPath(tfPictureFolderPathValue.getText());

				OmitPrefixesConfiguration omitCfg = new OmitPrefixesConfiguration();
				omitCfg.setFiltering(cbOmitFiltering.isSelected());
				omitCfg.setSorting(cbOmitSorting.isSelected());
				omitCfg.setPrefixes(Arrays.asList(tfOmitPrefix.getText().trim().split(" ")));
				libConfig.setOmitPrefixesConfiguration(omitCfg);
			}
		});

		FormLayout layout = new FormLayout("3px, 10:grow, 3px", "3px, p, 7px, p, 7px, p, 7px, fill:p:grow, 3px, p, 3px");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		CellConstraints cc = new CellConstraints();

		builder.add(buildGeneral(), cc.xy(2, 2));
		builder.add(buildLibrary(), cc.xy(2, 4));
		builder.add(buildScanner(), cc.xy(2, 6));
		builder.add(buildFolderManager(), cc.xy(2, 8));
		builder.add(bApply, cc.xy(2, 10, CellConstraints.RIGHT, CellConstraints.CENTER));
		
		JScrollPane sp = new JScrollPane(builder.getPanel());
		sp.setBorder(BorderFactory.createEmptyBorder());

		return sp;
	}

	private Component buildFolderManager() {
		FormLayout layout = new FormLayout("fill:p:grow", "p, fill:150:grow");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		CellConstraints cc = new CellConstraints();

		JComponent sManageFolders = builder.addSeparator(Messages.getString("ML.GeneralOptionsView.sManageFolders"), cc.xy(1, 1));
		sManageFolders = (JComponent) sManageFolders.getComponent(0);
		sManageFolders.setFont(sManageFolders.getFont().deriveFont(Font.BOLD));

		pManagedFolders = new ManagedFoldersPanel();
		builder.add(pManagedFolders, cc.xy(1, 2));

		return builder.getPanel();
	}

	private Component buildScanner() {
		FormLayout layout = new FormLayout("p, 5px, 100px, 3dlu, p, 3dlu, p, 3dlu, fill:10:grow", "p, 3dlu,  p, 3dlu, p, 3dlu, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		CellConstraints cc = new CellConstraints();

		JComponent sScanner = builder.addSeparator(Messages.getString("ML.GeneralOptionsView.sScanner"), cc.xyw(1, 1, 9));
		sScanner = (JComponent) sScanner.getComponent(0);
		sScanner.setFont(sScanner.getFont().deriveFont(Font.BOLD));

		JLabel lScanState = builder.addLabel(Messages.getString("ML.GeneralOptionsView.lScanState"), cc.xy(1, 3));
		this.lScanState = new JLabel(Messages.getString("ML.ScanState.IDLE"));
		builder.add(this.lScanState, cc.xy(3, 3));
		this.bStartPauseScan = new JButton(Messages.getString("ML.GeneralOptionsView.bPause"));
		lScanState.setPreferredSize(new Dimension(lScanState.getPreferredSize().width, bStartPauseScan.getPreferredSize().height));
		this.bStartPauseScan.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (scanState == ScanState.RUNNING) {
					try {
						libraryManager.pauseScan();
						bStartPauseScan.setText(Messages.getString("ML.GeneralOptionsView.bResume"));
					} catch (ScanStateException ex) {
						if(log.isInfoEnabled()) log.info("Unable to pause scan when its state is " + ex.getCurrentState() + ". It can only be used when in state " + ex.getExpectedState());
					}
				} else if (scanState == ScanState.PAUSED) {
					try {
						libraryManager.unPauseScan();
						bStartPauseScan.setText(Messages.getString("ML.GeneralOptionsView.bPause"));
					} catch (ScanStateException ex) {
						if(log.isInfoEnabled()) log.info("Unable to pause scan when its state is " + ex.getCurrentState() + ". It can only be used when in state " + ex.getExpectedState());
					}
				}
			}
		});
		builder.add(this.bStartPauseScan, cc.xy(5, 3));
		this.bStopScan = new JButton(Messages.getString("ML.GeneralOptionsView.bStopScan"));
		this.bStopScan.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				libraryManager.stopScan();
			}

		});
		builder.add(this.bStopScan, cc.xy(7, 3));

		this.bScanFolder = new JButton(Messages.getString("ML.GeneralOptionsView.bScanFolder"));
		this.bScanFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser fc = new JFileChooser(MediaLibraryStorage.getInstance().getMetaDataValue(MetaDataKeys.LAST_SCAN_FOLDER_PATH.toString()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				if (fc.showOpenDialog(bScanFolder.getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
					ScanFolderDialog d = new ScanFolderDialog(fc.getSelectedFile().getAbsolutePath());
					d.setModal(true);
					d.setResizable(false);
					d.pack();
					d.setLocation(GUIHelper.getCenterDialogOnParentLocation(d.getSize(), bScanFolder));

					d.setVisible(true);
					if (d.isDoImport()) {
						try {
							File f = new File(d.getManagedFolder().getPath());
							MediaLibraryStorage.getInstance().setMetaDataValue(MetaDataKeys.LAST_SCAN_FOLDER_PATH.toString(), f.getParent());
							FileScanner.getInstance().scan(d.getManagedFolder());
						} catch (InitialisationException ex) {
							log.error("Failed to get instance of FileScanner", ex);
						}
					}
				}
			}

		});
		builder.add(this.bScanFolder, cc.xyw(1, 5, 3));
		return builder.getPanel();
	}

	private Component buildGeneral() {
		FormLayout layout = new FormLayout("r:p, 3px, fill:10:grow, 3px, p, 3px, p, 3px, p, 3px, p", 
				"p, 3px, p, 3px, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		CellConstraints cc = new CellConstraints();

		//header
		JLabel lPictureFolderPathTitle = new JLabel(Messages.getString("ML.GeneralOptionsView.lPictureFolderPathTitle"));
		builder.add(lPictureFolderPathTitle, cc.xy(1, 1));

		//picture save folder path
		tfPictureFolderPathValue = new JTextField();
		tfPictureFolderPathValue.setEditable(false);
		builder.add(tfPictureFolderPathValue, cc.xyw(3, 1, 7));

		JButton bBrowsePictureFolderPath = new JButton(Messages.getString("ML.GeneralOptionsView.bBrowsePictureFolderPath"));
		bBrowsePictureFolderPath.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser chooser = null;
				File f = new File(tfPictureFolderPathValue.getText());
				if (f.isDirectory()) {
					chooser = new JFileChooser(f.getAbsoluteFile());
				} else {
					chooser = new JFileChooser();
				}

				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("ML.General.FolderChooser.Title"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String folderPath = chooser.getSelectedFile().getAbsolutePath();
					tfPictureFolderPathValue.setText(folderPath);
				}
			}
		});
		builder.add(bBrowsePictureFolderPath, cc.xy(11, 1));

		//prefixes to ignore
		lOmitPrefix = new JLabel(Messages.getString("ML.General.OmitPrefixes.Heading"));
		builder.add(lOmitPrefix, cc.xy(1, 3));

		tfOmitPrefix = new JTextField();
		builder.add(tfOmitPrefix, cc.xyw(3, 3, 3));

		builder.addLabel(Messages.getString("ML.General.OmitPrefixes.When"), cc.xy(7, 3));

		cbOmitSorting = new JCheckBox(Messages.getString("ML.General.OmitPrefixes.Sort"));
		builder.add(cbOmitSorting, cc.xy(9, 3));

		cbOmitFiltering = new JCheckBox(Messages.getString("ML.General.OmitPrefixes.Filter"));
		builder.add(cbOmitFiltering, cc.xy(11, 3));
		
		//tmdb account
		builder.addLabel(Messages.getString("ML.GeneralOptionsView.TmdbHeader"), cc.xy(1, 5));
		lTmdbUser = new JLabel();
		builder.add(lTmdbUser, cc.xy(3, 5));
		JButton bSetTmdbUser = new JButton(Messages.getString("ML.GeneralOptionsView.ButtonSetUser"));
		bSetTmdbUser.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Session s = TmdbHelper.createSession(lTmdbUser.getTopLevelAncestor());
				setSession(s);
			}
		});
		builder.add(bSetTmdbUser, cc.xy(5, 5));

		//set initial values
		tfPictureFolderPathValue.setText(libConfig.getPictureSaveFolderPath());

		OmitPrefixesConfiguration omitCfg = libConfig.getOmitPrefixesConfiguration();
		String prefixes = "";
		for (String k : omitCfg.getPrefixes()) {
			prefixes += k + " ";
		}
		prefixes = prefixes.trim();

		tfOmitPrefix.setText(prefixes);
		cbOmitSorting.setSelected(omitCfg.isSorting());
		cbOmitFiltering.setSelected(omitCfg.isFiltering());
		
		setSession(TmdbHelper.getSession());

		return builder.getPanel();
	}
	
	private void setSession(Session session){
		if(session != null){
			lTmdbUser.setText(" " + session.getUserName());
			lTmdbUser.setFont(lTmdbUser.getFont().deriveFont(Font.PLAIN));
		} else {
			lTmdbUser.setText(" " + Messages.getString("ML.GeneralOptionsView.NoTmdbUser"));
			lTmdbUser.setFont(lTmdbUser.getFont().deriveFont(Font.ITALIC));
		}
	}

	private JComponent buildLibrary() {
		FormLayout layout = new FormLayout("p, right:p, 5px, 40px, 3dlu, p, 3dlu, fill:0:grow, p", "p, p, p, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		CellConstraints cc = new CellConstraints();

		JComponent sManageLibrary = builder.addSeparator(Messages.getString("ML.GeneralOptionsView.sManageLibrary"), cc.xyw(1, 1, 9));
		sManageLibrary = (JComponent) sManageLibrary.getComponent(0);
		sManageLibrary.setFont(sManageLibrary.getFont().deriveFont(Font.BOLD));

		builder.addLabel(Messages.getString("ML.GeneralOptionsView.lVideos"), cc.xy(2, 2));
		this.lVideoCount = new JLabel(String.valueOf(this.libraryManager.getVideoCount()));
		builder.add(this.lVideoCount, cc.xy(4, 2));
		this.bClearVideo = new JButton(Messages.getString("ML.GeneralOptionsView.bClear"));
		this.bClearVideo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				switch (JOptionPane.showConfirmDialog(bClearVideo.getTopLevelAncestor(), Messages.getString("ML.GeneralOptionsView.DeleteAllVideosMsg"))) {
					case JOptionPane.YES_OPTION:
						libraryManager.clearVideo();
						break;
				}
			}

		});
		builder.add(this.bClearVideo, cc.xy(6, 2));

		JButton bResetLibrary = new JButton(Messages.getString("ML.GeneralOptionsView.bResetLibrary"));
		bResetLibrary.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (JOptionPane.showConfirmDialog(bClearVideo.getTopLevelAncestor(), String.format(Messages.getString("ML.GeneralOptionsView.ResetDBMsg"), System
				        .getProperty("line.separator"))) == JOptionPane.YES_OPTION) {
					try {
						// reset the storage
						libraryManager.resetStorage();

						// update the configuration fields that have to be
						MediaLibraryConfiguration config = MediaLibraryConfiguration.getInstance();
						cbEnableMediaLibrary.setSelected(config.isMediaLibraryEnabled());
						tfPictureFolderPathValue.setText(config.getPictureSaveFolderPath());
						
						OmitPrefixesConfiguration omitCfg = libConfig.getOmitPrefixesConfiguration();
						String prefixes = "";
						for (String k : omitCfg.getPrefixes()) {
							prefixes += k + " ";
						}
						prefixes = prefixes.trim();
						tfOmitPrefix.setText(prefixes);
						cbOmitFiltering.setSelected(omitCfg.isFiltering());
						cbOmitSorting.setSelected(omitCfg.isSorting());
						net.pms.PMS.get().getFrame().setStatusLine(Messages.getString("ML.GeneralOptionsView.ResetDBDoneMsg"));
					} catch (Exception ex) {
						log.error("Failed to reset data base", ex);
					}
				}
			}
		});
		builder.add(bResetLibrary, cc.xy(9, 2));

		builder.addLabel(Messages.getString("ML.GeneralOptionsView.lTracks"), cc.xy(2, 3));
		this.lAudioCount = new JLabel(String.valueOf(this.libraryManager.getAudioCount()));
		builder.add(this.lAudioCount, cc.xy(4, 3));
		this.bClearAudio = new JButton(Messages.getString("ML.GeneralOptionsView.bClear"));
		this.bClearAudio.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				switch (JOptionPane.showConfirmDialog(bClearAudio.getTopLevelAncestor(), Messages.getString("ML.GeneralOptionsView.DeleteAllTracksMsg"))) {
					case JOptionPane.YES_OPTION:
						libraryManager.clearAudio();
						break;
				}
			}

		});
		builder.add(this.bClearAudio, cc.xy(6, 3));

		JButton bCleanLibrary = new JButton(Messages.getString("ML.GeneralOptionsView.bClearLibrary"));
		bCleanLibrary.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (JOptionPane.showConfirmDialog(bClearVideo.getTopLevelAncestor(), String.format(Messages.getString("ML.GeneralOptionsView.CleanDBMsg"), System
				        .getProperty("line.separator"))) == JOptionPane.YES_OPTION) {
					try {
						// clean the storage
						libraryManager.cleanStorage();
					} catch (Exception ex) {
						log.error("Failed the library", ex);
					}
				}
			}
		});
		builder.add(bCleanLibrary, cc.xy(9, 3));

		builder.addLabel(Messages.getString("ML.GeneralOptionsView.lPictures"), cc.xy(2, 4));
		this.lPicturesCount = new JLabel(String.valueOf(this.libraryManager.getPictureCount()));
		builder.add(this.lPicturesCount, cc.xy(4, 4));
		this.bClearPictures = new JButton(Messages.getString("ML.GeneralOptionsView.bClear"));
		this.bClearPictures.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				switch (JOptionPane.showConfirmDialog(bClearPictures.getTopLevelAncestor(), Messages.getString("ML.GeneralOptionsView.DeleteAllPicturesMsg"))) {
					case JOptionPane.YES_OPTION:
						libraryManager.clearAudio();
						break;
				}
			}

		});
		builder.add(this.bClearPictures, cc.xy(6, 4));

		return builder.getPanel();
	}

	private void updateScanState() {
		this.lScanState.setText(Messages.getString("ML.ScanState." + this.scanState));
		if (this.scanState == ScanState.PAUSED) {
			this.bStartPauseScan.setText(Messages.getString("ML.GeneralOptionsView.bResume"));
			this.bStartPauseScan.setVisible(true);
			this.bStopScan.setVisible(true);
			this.bStartPauseScan.setEnabled(true);
			this.bStopScan.setEnabled(true);
		} else if (this.scanState == ScanState.PAUSING || this.scanState == ScanState.STARTING || this.scanState == ScanState.STOPPING) {
			this.bStartPauseScan.setEnabled(false);
			this.bStopScan.setEnabled(false);
		} else if (this.scanState == ScanState.RUNNING) {
			this.bStartPauseScan.setText(Messages.getString("ML.GeneralOptionsView.bPause"));
			this.bStartPauseScan.setVisible(true);
			this.bStopScan.setVisible(true);
			this.bStartPauseScan.setEnabled(true);
			this.bStopScan.setEnabled(true);
		} else if (this.scanState == ScanState.IDLE) {
			this.bStartPauseScan.setVisible(false);
			this.bStopScan.setVisible(false);
		}
	}
}
