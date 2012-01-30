package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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

public class VideoFilePropertiesPanel extends JPanel implements IFilePropertiesEditor {
	private static final long serialVersionUID = -2983607076103804005L;
	
	private final String GENRES_NAME = Messages.getString("ML.Condition.Type.VIDEO_GENRES");
	
	private JTextField tfName;
	private JTextField tfOriginalName;
	private JTextField tfSortName;
	private JTextField tfDirector;
	private JTextField tfImdbId;
	private JTextField tfTmdbId;
	private JTextField tfRating;
	private JTextField tfRatingVoters;
	private JTextField tfRevenue;
	private JTextField tfYear;
	private JTextField tfBudget;
	private JTextField tfHomePage;
	private JTextField tfCertification;
	private JTextField tfCertificationReason;
	private JTextField tfTrailer;
	private JTextField tfTagLine;
	private JTextArea taOverview;
	private JCheckBox cbActive;
	private FileTagsPanel pGenres;

	public VideoFilePropertiesPanel(DOVideoFileInfo fileInfo) {
		setLayout(new GridLayout());
		init(fileInfo);
		build();
	}
	
	public void init(DOVideoFileInfo fileInfo) {
		tfName = new JTextField(fileInfo.getName());
		tfOriginalName = new JTextField(fileInfo.getOriginalName());
		tfYear = new JTextField(String.valueOf(fileInfo.getYear()));
		tfSortName = new JTextField(fileInfo.getSortName());
		tfDirector = new JTextField(fileInfo.getDirector());
		tfImdbId = new JTextField(fileInfo.getImdbId());
		tfHomePage = new JTextField(String.valueOf(fileInfo.getHomepageUrl()));
		tfTrailer = new JTextField(String.valueOf(fileInfo.getTrailerUrl()));
		tfTmdbId = new JTextField(String.valueOf(fileInfo.getTmdbId()));
		tfRating = new JTextField(String.valueOf(fileInfo.getRating().getRatingPercent()));
		tfRatingVoters = new JTextField(String.valueOf(fileInfo.getRating().getVotes()));
		tfCertificationReason = new JTextField(String.valueOf(fileInfo.getAgeRating().getReason()));
		tfCertification = new JTextField(String.valueOf(fileInfo.getAgeRating().getLevel()));
		tfTagLine = new JTextField(String.valueOf(fileInfo.getTagLine()));
		tfBudget = new JTextField(String.valueOf(fileInfo.getBudget()));
		taOverview = new JTextArea(String.valueOf(fileInfo.getOverview()));
		tfRevenue = new JTextField(String.valueOf(fileInfo.getRevenue()));
		cbActive = new JCheckBox(Messages.getString("ML.Condition.Header.Type.FILE_ISACTIF"));
		cbActive.setFont(cbActive.getFont().deriveFont(Font.BOLD));
		cbActive.setSelected(fileInfo.isActif());
		Map<String, List<String>> genresMap = new HashMap<String, List<String>>();
		Collections.sort(fileInfo.getGenres());
		genresMap.put(GENRES_NAME, fileInfo.getGenres());
		pGenres = new FileTagsPanel(genresMap, false);
	}

	public void build() {
		//reset the sizes of all text fields to lay them out correctly when resizing
		//otherwise, scroll bars will show up if the dialog size is being reduced
		tfName.setSize(new Dimension(10, 10));
		tfOriginalName.setSize(new Dimension(10, 10));
		tfYear.setSize(new Dimension(10, 10));
		tfSortName.setSize(new Dimension(10, 10));
		tfDirector.setSize(new Dimension(10, 10));
		tfImdbId.setSize(new Dimension(10, 10));
		tfHomePage.setSize(new Dimension(10, 10));
		tfTrailer.setSize(new Dimension(10, 10));
		tfTmdbId.setSize(new Dimension(10, 10));
		tfRating.setSize(new Dimension(10, 10));
		tfRatingVoters.setSize(new Dimension(10, 10));
		tfCertificationReason.setSize(new Dimension(10, 10));
		tfCertification.setSize(new Dimension(10, 10));
		tfTagLine.setSize(new Dimension(10, 10));
		tfBudget.setSize(new Dimension(10, 10));
		taOverview.setSize(new Dimension(10, 10));
		tfRevenue.setSize(new Dimension(10, 10));
		
		//build the panel
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("5px, 20:grow, 10px, 20:grow, 10px, 20:grow, 10px, 20:grow, 10px, d, 5px", // columns
		        "3px, d, 1px, d, 3px, d, 1px, d, 3px, d, 1px, d, 3px, d, 1px, d, 3px, d, 1px, d, 3px, d, 1px, d, 3px, d, 5px, d, 3px"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		
		//row 1
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_NAME), cc.xyw(2, 2, 3));
		builder.add(tfName, cc.xyw(2, 4, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_ORIGINALNAME), cc.xyw(6, 2, 3));
		builder.add(tfOriginalName, cc.xyw(6, 4, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_YEAR), cc.xy(10, 2));
		builder.add(tfYear, cc.xy(10, 4));
		
		//row 2
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_SORTNAME), cc.xyw(2, 6, 3));
		builder.add(tfSortName, cc.xyw(2, 8, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_DIRECTOR), cc.xy(6, 6));
		builder.add(tfDirector, cc.xyw(6, 8, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_IMDBID), cc.xy(10, 6));
		builder.add(tfImdbId, cc.xy(10, 8));
		
		//row 3
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_HOMEPAGEURL), cc.xyw(2, 10, 3));
		builder.add(tfHomePage, cc.xyw(2, 12, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_TRAILERURL), cc.xyw(6, 10, 3));
		builder.add(tfTrailer, cc.xyw(6, 12, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_TMDBID), cc.xy(10, 10));
		builder.add(tfTmdbId, cc.xy(10, 12));
		
		//row 4		
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_RATINGPERCENT), cc.xy(2, 14));
		builder.add(tfRating, cc.xy(2, 16));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_RATINGVOTERS), cc.xy(4, 14));
		builder.add(tfRatingVoters, cc.xy(4, 16));
		
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_CERTIFICATIONREASON), cc.xyw(6, 14, 3));
		builder.add(tfCertificationReason, cc.xyw(6, 16, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_CERTIFICATION), cc.xy(10, 14));
		builder.add(tfCertification, cc.xy(10, 16));
		
		//row 5
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_TAGLINE), cc.xyw(2, 18, 7));
		builder.add(tfTagLine, cc.xyw(2, 20, 7));
		
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_BUDGET), cc.xy(10, 18));
		builder.add(tfBudget, cc.xy(10, 20));

		//row 6
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_OVERVIEW), cc.xyw(2, 22, 7));
		taOverview.setLineWrap(true);
		taOverview.setWrapStyleWord(true);
		JScrollPane spOverview = new JScrollPane(taOverview);
		spOverview.setBorder(tfBudget.getBorder());
		builder.add(spOverview, cc.xywh(2, 24, 7, 3));
		
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_REVENUE), cc.xy(10, 22));
		builder.add(tfRevenue, cc.xy(10, 24, CellConstraints.DEFAULT, CellConstraints.TOP));
		
		builder.add(cbActive, cc.xy(10, 26, CellConstraints.DEFAULT, CellConstraints.TOP));

		builder.add(pGenres, cc.xyw(2, 28, 9));

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
		
		try { year = Integer.parseInt(tfYear.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_YEAR, tfYear.getText());
		}
		try { tmdbId = Integer.parseInt(tfTmdbId.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_TMDBID, tfTmdbId.getText());
		}
		try { budget = Integer.parseInt(tfBudget.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_BUDGET, tfBudget.getText());
		}
		try { revenue = Integer.parseInt(tfRevenue.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_REVENUE, tfRevenue.getText());
		}
		try { rating = Integer.parseInt(tfRating.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_RATINGPERCENT, tfRating.getText());
		}
		try { voters = Integer.parseInt(tfRatingVoters.getText().trim()); } 
		catch(NumberFormatException ex) {
			throw new ConditionTypeException(ConditionType.VIDEO_RATINGVOTERS, tfRatingVoters.getText());
		}

		DOVideoFileInfo fiVideo = (DOVideoFileInfo) fileInfo;
		fiVideo.setActif(cbActive.isSelected());
		
		fiVideo.setYear(year);
		fiVideo.setTmdbId(tmdbId);
		fiVideo.setBudget(budget);
		fiVideo.setRevenue(revenue);
		fiVideo.setRating(new DORating(rating, voters));		
		
		fiVideo.setName(tfName.getText().trim());
		fiVideo.setOriginalName(tfOriginalName.getText().trim());
		fiVideo.setSortName(tfSortName.getText().trim());
		fiVideo.setDirector(tfDirector.getText().trim());
		fiVideo.setImdbId(tfImdbId.getText().trim());
		fiVideo.setHomepageUrl(tfHomePage.getText().trim());
		fiVideo.setTrailerUrl(tfTrailer.getText().trim());
		fiVideo.setAgeRating(new DOCertification(tfCertification.getText().trim(), tfCertificationReason.getText().trim()));
		fiVideo.setTagLine(tfTagLine.getText().trim());
		fiVideo.setOverview(taOverview.getText().trim());
		
		Map<String, List<String>> tags = pGenres.getTags();
		if (tags.keySet().contains(GENRES_NAME)) {
			fiVideo.setGenres(tags.get(GENRES_NAME));
		}
	}
}
