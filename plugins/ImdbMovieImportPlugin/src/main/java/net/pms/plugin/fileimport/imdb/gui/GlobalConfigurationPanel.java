package net.pms.plugin.fileimport.imdb.gui;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.newgui.components.ComboBoxItem;
import net.pms.plugin.fileimport.ImdbMovieImportPlugin;
import net.pms.plugin.fileimport.imdb.configuration.GlobalConfiguration;
import net.pms.plugin.fileimport.imdb.configuration.GlobalConfiguration.PlotType;

/**
 * The GlobalConfigurationPanel lets configure global properties of the plugin
 */
public class GlobalConfigurationPanel extends JPanel {
	private static final long serialVersionUID = 6338437685202972227L;
	private static final Logger log = LoggerFactory.getLogger(GlobalConfigurationPanel.class);
	private final GlobalConfiguration globalConfig;
	private JTextField tfCoverWidth;
	private JTextField tfReceiveTimeout;
	private JComboBox cbPlotType;
	private JCheckBox cbUseRottenTomatoes;

	/**
	 * Instantiates a new global configuration panel.
	 *
	 * @param globalConfig the global configuration
	 */
	public GlobalConfigurationPanel(GlobalConfiguration globalConfig) {
		setLayout(new GridLayout());
		this.globalConfig = globalConfig;
		init();
		build();
	}

	/**
	 * Initializes the graphical components
	 */
	private void init() {
		tfCoverWidth = new JTextField();
		tfReceiveTimeout = new JTextField();
		
		cbUseRottenTomatoes = new JCheckBox(ImdbMovieImportPlugin.messages.getString("GlobalConfigurationPanel.1"));

		@SuppressWarnings("unchecked")
		ComboBoxItem<PlotType>[] cbItems = new ComboBoxItem[PlotType.values().length];		
		int i = 0;
		for(PlotType plotType : PlotType.values()) {
			cbItems[i++] = new ComboBoxItem<PlotType>(ImdbMovieImportPlugin.messages.getString("PlotType." + plotType), plotType);
		}
		cbPlotType = new JComboBox(cbItems);
	}

	/**
	 * Builds the panel
	 */
	private void build() {
		// Set basic layout
		FormLayout layout = new FormLayout("5px, p, 5px, f:p:g, 5px", //columns
				"5px, p, 5px, p, 5px, p, 5px, p, 5px"); //rows
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.addLabel(ImdbMovieImportPlugin.messages.getString("GlobalConfigurationPanel.2"), cc.xy(2, 2, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		builder.add(tfCoverWidth, cc.xy(4, 2));

		builder.addLabel(ImdbMovieImportPlugin.messages.getString("GlobalConfigurationPanel.3"), cc.xy(2, 4, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		builder.add(cbPlotType, cc.xy(4, 4));

		builder.addLabel(ImdbMovieImportPlugin.messages.getString("GlobalConfigurationPanel.4"), cc.xy(2, 6, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		builder.add(tfReceiveTimeout, cc.xy(4, 6));

		builder.add(cbUseRottenTomatoes, cc.xyw(2, 8, 3));

		JScrollPane sp = new JScrollPane(builder.getPanel());
		sp.setBorder(BorderFactory.createEmptyBorder());
		
		add(sp);
	}
	
	/**
	 * Updates all graphical components to show the global configuration.<br>
	 * This is being used to roll back changes after editing properties and
	 * canceling the dialog.
	 */
	public void applyConfig() {
		cbUseRottenTomatoes.setSelected(globalConfig.isUseRottenTomatoes());
		cbPlotType.setSelectedItem(new ComboBoxItem<PlotType>(ImdbMovieImportPlugin.messages.getString("PlotType." + globalConfig.getPlotType()), globalConfig.getPlotType()));
		tfCoverWidth.setText(String.valueOf(globalConfig.getCoverWidth()));
		tfReceiveTimeout.setText(String.valueOf(globalConfig.getReceiveTimeoutSec()));
	}

	/**
	 * Updates the configuration to reflect the GUI
	 *
	 * @param gc the global configuration
	 */
	@SuppressWarnings("unchecked")
	public void updateConfiguration(GlobalConfiguration gc) {
		gc.setUseRottenTomatoes(cbUseRottenTomatoes.isSelected());
		gc.setPlotType(((ComboBoxItem<PlotType>)cbPlotType.getSelectedItem()).getValue());
		
		int coverWidth = globalConfig.getCoverWidth();
		try {
			coverWidth = Integer.parseInt(tfCoverWidth.getText());
		} catch (NumberFormatException ex) {
			log.error(String.format("Failed to parse cover width '%s' as an integer", tfCoverWidth.getText()));
		}
		gc.setCoverWidth(coverWidth);
		
		int receiveTimeout = globalConfig.getReceiveTimeoutSec();
		try {
			receiveTimeout = Integer.parseInt(tfReceiveTimeout.getText());
		} catch (NumberFormatException ex) {
			log.error(String.format("Failed to parse receive timeout '%s' as an integer", tfReceiveTimeout.getText()));
		}
		gc.setReceiveTimeoutMs(receiveTimeout);
	}
}
