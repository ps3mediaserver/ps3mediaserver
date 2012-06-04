package net.pms.plugin.webservice;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.pms.plugin.webservice.configuration.GlobalConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class GlobalConfigurationPanel extends JPanel {
	private static final long serialVersionUID = 8993757672399673524L;
	private static final Logger log = LoggerFactory.getLogger(GlobalConfigurationPanel.class);
	private GlobalConfiguration globalConfig;

	private JLabel lHeader;
	private JTextField tfPort;
	private JLabel lRequireRestart;

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
	 * Updates all graphical components to show the global configuration.<br>
	 * This is being used to roll back changes after editing properties and
	 * canceling the dialog.
	 */
	public void applyConfig() {
		tfPort.setText(String.valueOf(globalConfig.getPort()));
	}

	/**
	 * Initializes the graphical components
	 */
	private void init() {
		lHeader = new JLabel(WebServicePlugin.messages.getString("GlobalConfigurationPanel.1"));
		tfPort = new JTextField();
		lRequireRestart = new JLabel(WebServicePlugin.messages.getString("GlobalConfigurationPanel.2"));
	}

	/**
	 * Builds the panel
	 */
	private void build() {
		// Set basic layout
		FormLayout layout = new FormLayout("5px, p, 5px, 70, 5px, p, f:5px:g", //columns
				"5px, p, f:5px:g"); //rows
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.add(lHeader, cc.xy(2, 2));
		builder.add(tfPort, cc.xy(4, 2));
		builder.add(lRequireRestart, cc.xy(6, 2));
		
		removeAll();
		add(builder.getPanel());
	}

	/**
	 * Updates the configuration to reflect the GUI
	 *
	 * @param gc the global configuration
	 */
	public void updateConfiguration(GlobalConfiguration gc) {
		String valStr = tfPort.getText();
		try {
			int newVal = Integer.parseInt(valStr);
			gc.setPort(newVal);
		} catch(NumberFormatException e) {
			log.error(String.format("'%s' could not be parsed as an int", valStr));
		}
	}

}
