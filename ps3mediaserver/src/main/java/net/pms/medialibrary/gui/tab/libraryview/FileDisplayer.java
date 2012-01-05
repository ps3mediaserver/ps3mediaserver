package net.pms.medialibrary.gui.tab.libraryview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.SortOption;
import net.pms.medialibrary.gui.shared.FilterEditor;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class FileDisplayer extends JPanel {
	private static final long serialVersionUID = -1722661396066035647L;
	private ImageIcon iUp = new ImageIcon(getClass().getResource("/resources/images/uparrow-16.png"));
	private ImageIcon iDown = new ImageIcon(getClass().getResource("/resources/images/downarrow-16.png"));
	private int filterHeight = 132;
	private int dividerWidth;
	
	private FileType fileType = FileType.UNKNOWN;
	private JLabel lExpandFilter;
	private JPanel pFilter;
	private JSplitPane sp;
	private FilterEditor filterEditor;
	private FileDisplayTable tFiles;
	
	public FileDisplayer(FileType fileType) {
		super(new GridLayout(1, 1));
		this.fileType = fileType;
		init();
	}
	
	private void init(){
		lExpandFilter = new JLabel(iDown);
		lExpandFilter.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(lExpandFilter.getIcon().equals(iUp)){
					lExpandFilter.setIcon(iDown);
					//TODO: here's a bug with the filter height becoming smaller
					pFilter.setSize(pFilter.getWidth(), filterHeight);
					pFilter.getComponent(1).setVisible(true);
					pFilter.getComponent(2).setVisible(true);
					sp.setDividerSize(dividerWidth);
				}else {
					lExpandFilter.setIcon(iUp);
					filterHeight = pFilter.getHeight();
					pFilter.setSize(pFilter.getWidth(), 30);
					pFilter.getComponent(1).setVisible(false);
					pFilter.getComponent(2).setVisible(false);
					sp.setDividerSize(0);
				}
				sp.setDividerLocation(pFilter.getSize().height);
			}
		});
		
		pFilter = new JPanel(new BorderLayout());
		pFilter.addComponentListener(new ComponentAdapter() {			
			@Override
			public void componentResized(ComponentEvent e) {
				repositionExpandFilterLabel();
			}
		});
		pFilter.setBorder(BorderFactory.createTitledBorder(Messages.getString("ML.FileDisplayer.FilterHeader")));
		pFilter.add(lExpandFilter);
		
		filterEditor = new FilterEditor(new DOFilter(), fileType);
		pFilter.add(filterEditor, BorderLayout.CENTER);
		
		JButton bApply = new JButton(Messages.getString("ML.FileDisplayer.bApply"));
		bApply.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				applyFilter();
			}
		});
		if(bApply.getPreferredSize().width < 60){
			bApply.setPreferredSize(new Dimension(60, bApply.getPreferredSize().height));
		}
		pFilter.add(bApply, BorderLayout.EAST);
		
		tFiles = new FileDisplayTable(fileType);
		sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pFilter, tFiles);
		dividerWidth = sp.getDividerSize();
		add(sp);
		sp.setDividerLocation(filterHeight);
		
		repositionExpandFilterLabel();
		applyFilter();
	}

	@SuppressWarnings("unchecked")
	private void applyFilter() {
		DOFilter filter;
		try {
			filter = filterEditor.getFilter();
			filter.validate();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			return;
		}
		
		List<DOFileInfo> files = new ArrayList<DOFileInfo>();
		if(fileType == FileType.VIDEO){
			List<DOVideoFileInfo> vFiles = MediaLibraryStorage.getInstance().getVideoFileInfo(filter, true, ConditionType.VIDEO_NAME, 0, SortOption.FileProperty, false);
			files = (List<DOFileInfo>)(List<?>)vFiles;
		} else if(fileType == FileType.FILE){
			files = MediaLibraryStorage.getInstance().getFileInfo(filter, true, ConditionType.FILE_FILENAME, 0, SortOption.FileProperty);
		}
//		else if(fileType == FileType.AUDIO){
//			List<DOVideoFileInfo> aFiles = MediaLibraryStorage.getInstance().getAudioFileInfo(filter, true, ConditionType.VIDEO_NAME, 0);
//			files = (List<DOFileInfo>)(List<?>)aFiles;
//		} else if(fileType == FileType.PICTURES){
//			List<DOVideoFileInfo> vFiles = MediaLibraryStorage.getInstance().getPicturesFileInfo(filter, true, ConditionType.VIDEO_NAME, 0);
//			files = (List<DOFileInfo>)(List<?>)vFiles;
//		}
		tFiles.setContent(files);
	}
	
	private void repositionExpandFilterLabel(){
		lExpandFilter.setBounds(pFilter.getWidth() - lExpandFilter.getWidth() - 7, 2, lExpandFilter.getPreferredSize().width, lExpandFilter.getPreferredSize().height);
	}

	public void setFilter(DOFilter filter) {
		filterEditor.setFilter(filter);
		applyFilter();
	}
}
