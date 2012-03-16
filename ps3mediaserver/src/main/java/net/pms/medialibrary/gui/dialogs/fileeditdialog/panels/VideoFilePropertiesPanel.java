package net.pms.medialibrary.gui.dialogs.fileeditdialog.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOCertification;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DORating;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.exceptions.ConditionTypeException;
import net.pms.medialibrary.commons.interfaces.IFilePropertiesEditor;
import net.pms.medialibrary.gui.dialogs.fileeditdialog.controls.PropertyInfoEntry;
import net.pms.medialibrary.gui.shared.JHeader;

public class VideoFilePropertiesPanel extends JPanel implements IFilePropertiesEditor {
	private static final long serialVersionUID = -2983607076103804005L;
	
	private final String GENRES_NAME = Messages.getString("ML.Condition.Type.VIDEO_GENRES");
	
	private PropertyInfoEntry hName;
	private PropertyInfoEntry hOriginalName;
	private PropertyInfoEntry hSortName;
	private PropertyInfoEntry hDirector;
	private PropertyInfoEntry hImdbId;
	private PropertyInfoEntry hTmdbId;
	private PropertyInfoEntry hRating;
	private PropertyInfoEntry hRatingVoters;
	private PropertyInfoEntry hRevenue;
	private PropertyInfoEntry hYear;
	private PropertyInfoEntry hBudget;
	private PropertyInfoEntry hHomePage;
	private PropertyInfoEntry hCertification;
	private PropertyInfoEntry hCertificationReason;
	private PropertyInfoEntry hTrailer;
	private PropertyInfoEntry hTagLine;
	private JHeader hOverview;
	private JTextArea taOverview;
	private JCheckBox cbActive;
	private FileTagsPanel pGenres;
	
	private boolean isConfirmEdit;

	public VideoFilePropertiesPanel(DOVideoFileInfo fileInfo, boolean isConfirmEdit) {
		setLayout(new GridLayout());
		this.isConfirmEdit = isConfirmEdit;
		init(fileInfo);
		build();
	}
	
	public void init(DOVideoFileInfo fileInfo) {
		hName = new PropertyInfoEntry(fileInfo.getName(), ConditionType.VIDEO_NAME, isConfirmEdit);
		hOriginalName = new PropertyInfoEntry(fileInfo.getOriginalName(), ConditionType.VIDEO_ORIGINALNAME, isConfirmEdit);
		hYear = new PropertyInfoEntry(String.valueOf(fileInfo.getYear()), ConditionType.VIDEO_YEAR, isConfirmEdit);
		hSortName = new PropertyInfoEntry(fileInfo.getSortName(), ConditionType.VIDEO_SORTNAME, isConfirmEdit);
		hDirector = new PropertyInfoEntry(fileInfo.getDirector(), ConditionType.VIDEO_DIRECTOR, isConfirmEdit);
		hImdbId = new PropertyInfoEntry(fileInfo.getImdbId(), ConditionType.VIDEO_IMDBID, isConfirmEdit);
		hHomePage = new PropertyInfoEntry(fileInfo.getHomepageUrl(), ConditionType.VIDEO_HOMEPAGEURL, isConfirmEdit);
		hTrailer = new PropertyInfoEntry(fileInfo.getTrailerUrl(), ConditionType.VIDEO_TRAILERURL, isConfirmEdit);
		hTmdbId = new PropertyInfoEntry(String.valueOf(fileInfo.getTmdbId()), ConditionType.VIDEO_TMDBID, isConfirmEdit);
		hRating = new PropertyInfoEntry(String.valueOf(fileInfo.getRating().getRatingPercent()), ConditionType.VIDEO_RATINGPERCENT, isConfirmEdit);
		hRatingVoters = new PropertyInfoEntry(String.valueOf(fileInfo.getRating().getVotes()), ConditionType.VIDEO_RATINGVOTERS, isConfirmEdit);
		hCertificationReason = new PropertyInfoEntry(fileInfo.getAgeRating().getReason(), ConditionType.VIDEO_CERTIFICATIONREASON, isConfirmEdit);
		hCertification = new PropertyInfoEntry(fileInfo.getAgeRating().getLevel(), ConditionType.VIDEO_CERTIFICATION, isConfirmEdit);
		hTagLine = new PropertyInfoEntry(fileInfo.getTagLine(), ConditionType.VIDEO_TAGLINE, isConfirmEdit);
		hBudget = new PropertyInfoEntry(String.valueOf(fileInfo.getBudget()), ConditionType.VIDEO_BUDGET, isConfirmEdit);
		hRevenue  = new PropertyInfoEntry(String.valueOf(fileInfo.getRevenue()), ConditionType.VIDEO_REVENUE, isConfirmEdit);
		
		hOverview = new JHeader(ConditionType.VIDEO_OVERVIEW, isConfirmEdit);
		taOverview = new JTextArea(String.valueOf(fileInfo.getOverview()));
		taOverview.setLineWrap(true);
		taOverview.setWrapStyleWord(true);
		taOverview.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				hOverview.setSelected(true);
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				hOverview.setSelected(true);
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				hOverview.setSelected(true);
			}
		});

		cbActive = new JCheckBox(Messages.getString("ML.Condition.Header.Type.FILE_ISACTIF"));
		cbActive.setFont(cbActive.getFont().deriveFont(Font.BOLD));
		cbActive.setSelected(fileInfo.isActive());		
		if(isConfirmEdit) {
			cbActive.setSelected(false);
			cbActive.setEnabled(false);
		}
		
		Map<String, List<String>> genresMap = new HashMap<String, List<String>>();
		Collections.sort(fileInfo.getGenres());
		genresMap.put(GENRES_NAME, fileInfo.getGenres());
		pGenres = new FileTagsPanel(genresMap, false);
	}

	public void build() {
		//reset the sizes of all text fields to lay them out correctly when resizing
		//otherwise, scroll bars will show up if the dialog size is being reduced
		taOverview.setSize(new Dimension(10, 10));
		
		//build the panel
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("5px, 20:grow, 10px, 20:grow, 10px, 20:grow, 10px, 20:grow, 10px, d, 5px", // columns
		        "3px, d, 3px, d, 3px, d, 3px, d, 3px, d, 3px, d, 3px, d, 5px, d, 3px"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		
		//row 1
		builder.add(hName, cc.xyw(2, 2, 3));
		builder.add(hOriginalName, cc.xyw(6, 2, 3));
		builder.add(hYear, cc.xy(10, 2));
		
		//row 2
		builder.add(hSortName, cc.xyw(2, 4, 3));
		builder.add(hDirector, cc.xyw(6, 4, 3));
		builder.add(hImdbId, cc.xy(10, 4));
		
		//row 3
		builder.add(hHomePage, cc.xyw(2, 6, 3));
		builder.add(hTrailer, cc.xyw(6, 6, 3));
		builder.add(hTmdbId, cc.xy(10, 6));
		
		//row 4		
		builder.add(hRating, cc.xy(2, 8));
		builder.add(hRatingVoters, cc.xy(4, 8));
		builder.add(hCertificationReason, cc.xyw(6, 8, 3));
		builder.add(hCertification, cc.xy(10, 8));

		//row 5
		builder.add(hTagLine, cc.xyw(2, 10, 7));
		builder.add(hBudget, cc.xy(10, 10));

		//row 6+7
		JPanel pOverviewHeader = new JPanel(new GridLayout());
		pOverviewHeader.setAlignmentY(LEFT_ALIGNMENT);
		pOverviewHeader.add(hOverview);
		
		JScrollPane spOverview = new JScrollPane(taOverview);
		spOverview.setBorder(new JTextField().getBorder());

		JPanel pOverview = new JPanel(new BorderLayout(0, 1));
		pOverview.add(pOverviewHeader, BorderLayout.NORTH);
		pOverview.add(spOverview, BorderLayout.CENTER);
		
		builder.add(pOverview, cc.xywh(2, 12, 7, 3));
		
		builder.add(hRevenue, cc.xy(10, 12));
		builder.add(cbActive, cc.xy(10, 14));
		
		//row 8
		builder.add(pGenres, cc.xyw(2, 16, 9));

		JPanel p = builder.getPanel();
		JScrollPane sp = new JScrollPane(p);
		sp.setBorder(BorderFactory.createEmptyBorder());
		
		removeAll();
		add(sp);
	}

	@Override
	public void updateFileInfo(DOFileInfo fileInfo) throws ConditionTypeException {
		if(!(fileInfo instanceof DOVideoFileInfo)) {
			return;
		}
		
		//try to parse all numerical values first to avoid modifying the fileinfo
		//if a value can't be set
		int year;
		int tmdbId;
		int budget;
		int revenue;
		int rating;
		int voters;
		
		try { year = Integer.parseInt(hYear.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_YEAR, hYear.getText());
		}
		try { tmdbId = Integer.parseInt(hTmdbId.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_TMDBID, hTmdbId.getText());
		}
		try { budget = Integer.parseInt(hBudget.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_BUDGET, hBudget.getText());
		}
		try { revenue = Integer.parseInt(hRevenue.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_REVENUE, hRevenue.getText());
		}
		try { rating = Integer.parseInt(hRating.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_RATINGPERCENT, hRating.getText());
		}
		try { voters = Integer.parseInt(hRatingVoters.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_RATINGVOTERS, hRatingVoters.getText());
		}

		DOVideoFileInfo fiVideo = (DOVideoFileInfo) fileInfo;
		fiVideo.setActive(cbActive.isSelected());
		
		fiVideo.setYear(year);
		fiVideo.setTmdbId(tmdbId);
		fiVideo.setBudget(budget);
		fiVideo.setRevenue(revenue);
		fiVideo.setRating(new DORating(rating, voters));		
		
		fiVideo.setName(hName.getText().trim());
		fiVideo.setOriginalName(hOriginalName.getText().trim());
		fiVideo.setSortName(hSortName.getText().trim());
		fiVideo.setDirector(hDirector.getText().trim());
		fiVideo.setImdbId(hImdbId.getText().trim());
		fiVideo.setHomepageUrl(hHomePage.getText().trim());
		fiVideo.setTrailerUrl(hTrailer.getText().trim());
		fiVideo.setAgeRating(new DOCertification(hCertification.getText().trim(), hCertificationReason.getText().trim()));
		fiVideo.setTagLine(hTagLine.getText().trim());
		fiVideo.setOverview(taOverview.getText().trim());
		
		Map<String, List<String>> tags = pGenres.getTags();
		if (tags.keySet().contains(GENRES_NAME)) {
			fiVideo.setGenres(tags.get(GENRES_NAME));
		}
	}

	@Override
	public List<ConditionType> getPropertiesToUpdate() {
		List<ConditionType> res = new ArrayList<ConditionType>();
		res.add(ConditionType.FILE_CONTAINS_TAG);
		res.add(ConditionType.VIDEO_CONTAINS_GENRE);
		
		if(hName.isSelected()) {
			res.add(ConditionType.VIDEO_NAME);
		}
		if(hOriginalName.isSelected()) {
			res.add(ConditionType.VIDEO_ORIGINALNAME);
		}
		if(hSortName.isSelected()) {
			res.add(ConditionType.VIDEO_SORTNAME);
		}
		if(hDirector.isSelected()) {
			res.add(ConditionType.VIDEO_DIRECTOR);
		}
		if(hImdbId.isSelected()) {
			res.add(ConditionType.VIDEO_IMDBID);
		}
		if(hTmdbId.isSelected()) {
			res.add(ConditionType.VIDEO_TMDBID);
		}
		if(hRating.isSelected()) {
			res.add(ConditionType.VIDEO_RATINGPERCENT);
		}
		if(hRatingVoters.isSelected()) {
			res.add(ConditionType.VIDEO_RATINGVOTERS);
		}
		if(hRevenue.isSelected()) {
			res.add(ConditionType.VIDEO_REVENUE);
		}
		if(hYear.isSelected()) {
			res.add(ConditionType.VIDEO_YEAR);
		}
		if(hBudget.isSelected()) {
			res.add(ConditionType.VIDEO_BUDGET);
		}
		if(hHomePage.isSelected()) {
			res.add(ConditionType.VIDEO_HOMEPAGEURL);
		}
		if(hCertification.isSelected()) {
			res.add(ConditionType.VIDEO_CERTIFICATION);
		}
		if(hCertificationReason.isSelected()) {
			res.add(ConditionType.VIDEO_CERTIFICATIONREASON);
		}
		if(hTrailer.isSelected()) {
			res.add(ConditionType.VIDEO_TRAILERURL);
		}
		if(hTagLine.isSelected()) {
			res.add(ConditionType.VIDEO_TAGLINE);
		}
		if(hOverview.isSelected()) {
			res.add(ConditionType.VIDEO_OVERVIEW);
		}
		return res;
	}
}
