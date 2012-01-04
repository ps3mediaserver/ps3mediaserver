package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.Font;
import java.awt.GridLayout;

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
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;

public class VideoFilePropertiesPanel extends JPanel {
	private static final long serialVersionUID = -2983607076103804005L;
	
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

	public VideoFilePropertiesPanel(DOVideoFileInfo fileInfo) {
		build(fileInfo);
	}

	private void build(DOVideoFileInfo fileInfo) {
		setLayout(new GridLayout());
		
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("5px, 20:grow, 10px, 20:grow, 10px, 20:grow, 10px, 20:grow, 10px, p, 5px", // columns
		        "3px, p, 1px, p, 3px, p, 1px, p, 3px, p, 1px, p, 3px, p, 1px, p, 3px, p, 1px, p, 3px, p, 1px, p, 3px, p, 1px, p, 3px, fill:p:grow, 3px"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		
		//row 1
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_NAME), cc.xyw(2, 2, 3));
		tfName = new JTextField(fileInfo.getName());
		builder.add(tfName, cc.xyw(2, 4, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_ORIGINALNAME), cc.xyw(6, 2, 3));
		tfOriginalName = new JTextField(fileInfo.getOriginalName());
		builder.add(tfOriginalName, cc.xyw(6, 4, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_YEAR), cc.xy(10, 2));
		tfYear = new JTextField(String.valueOf(fileInfo.getYear()));
		builder.add(tfYear, cc.xy(10, 4));
		
		//row 2
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_SORTNAME), cc.xyw(2, 6, 3));
		tfSortName = new JTextField(fileInfo.getSortName());
		builder.add(tfSortName, cc.xyw(2, 8, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_DIRECTOR), cc.xy(6, 6));
		tfDirector = new JTextField(fileInfo.getDirector());
		builder.add(tfDirector, cc.xyw(6, 8, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_IMDBID), cc.xy(10, 6));
		tfImdbId = new JTextField(fileInfo.getImdbId());
		builder.add(tfImdbId, cc.xy(10, 8));
		
		//row 3
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_HOMEPAGEURL), cc.xyw(2, 12, 3));
		tfHomePage = new JTextField(String.valueOf(fileInfo.getHomepageUrl()));
		builder.add(tfHomePage, cc.xyw(2, 14, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_TRAILERURL), cc.xyw(6, 12, 3));
		tfTrailer = new JTextField(String.valueOf(fileInfo.getTrailerUrl()));
		builder.add(tfTrailer, cc.xyw(6, 14, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_TMDBID), cc.xy(10, 12));
		tfTmdbId = new JTextField(String.valueOf(fileInfo.getTmdbId()));
		builder.add(tfTmdbId, cc.xy(10, 14));
		
		//row 4		
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_RATINGPERCENT), cc.xy(2, 16));
		tfRating = new JTextField(String.valueOf(fileInfo.getRating().getRatingPercent()));
		builder.add(tfRating, cc.xy(2, 18));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_RATINGVOTERS), cc.xy(4, 16));
		tfRatingVoters = new JTextField(String.valueOf(fileInfo.getRating().getVotes()));
		builder.add(tfRatingVoters, cc.xy(4, 18));
		
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_CERTIFICATIONREASON), cc.xyw(6, 16, 3));
		tfCertificationReason = new JTextField(String.valueOf(fileInfo.getAgeRating().getReason()));
		builder.add(tfCertificationReason, cc.xyw(6, 18, 3));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_CERTIFICATION), cc.xy(10, 16));
		tfCertification = new JTextField(String.valueOf(fileInfo.getAgeRating().getLevel()));
		builder.add(tfCertification, cc.xy(10, 18));
		
		//row 5
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_TAGLINE), cc.xyw(2, 20, 7));
		tfTagLine = new JTextField(String.valueOf(fileInfo.getTagLine()));
		builder.add(tfTagLine, cc.xyw(2, 22, 7));
		
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_BUDGET), cc.xy(10, 20));
		tfBudget = new JTextField(String.valueOf(fileInfo.getBudget()));
		builder.add(tfBudget, cc.xy(10, 22));

		//row 6
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_OVERVIEW), cc.xyw(2, 24, 7));
		taOverview = new JTextArea(String.valueOf(fileInfo.getOverview()));
		taOverview.setLineWrap(true);
		taOverview.setWrapStyleWord(true);
		JScrollPane spOverview = new JScrollPane(taOverview);
		spOverview.setBorder(tfBudget.getBorder());
		builder.add(spOverview, cc.xywh(2, 26, 7, 3));
		
		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_REVENUE), cc.xy(10, 24));
		tfRevenue = new JTextField(String.valueOf(fileInfo.getRevenue()));
		builder.add(tfRevenue, cc.xy(10, 26, CellConstraints.DEFAULT, CellConstraints.TOP));
		
		cbActive = new JCheckBox(Messages.getString("ML.Condition.Header.Type.FILE_ISACTIF"));
		cbActive.setSelected(fileInfo.isActif());
		cbActive.setFont(cbActive.getFont().deriveFont(Font.BOLD));
		builder.add(cbActive, cc.xy(10, 28, CellConstraints.DEFAULT, CellConstraints.TOP));

		JScrollPane sp = new JScrollPane(builder.getPanel());
		sp.setBorder(BorderFactory.createEmptyBorder());
		
		add(sp);
	}
}
