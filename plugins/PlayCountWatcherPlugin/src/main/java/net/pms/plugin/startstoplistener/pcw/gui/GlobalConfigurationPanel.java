/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.plugin.startstoplistener.pcw.gui;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.plugin.startstoplistener.PlayCountWatcher;
import net.pms.plugin.startstoplistener.pcw.configuration.GlobalConfiguration;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The GlobalConfigurationPanel lets configure global properties of the plugin
 */
public class GlobalConfigurationPanel extends JPanel {
	private static final long serialVersionUID = 6133803510518388833L;
	private static final Logger log = LoggerFactory.getLogger(GlobalConfigurationPanel.class);
	private GlobalConfiguration globalConfig;

	private JLabel lHeader;
	private JTextField tfPercentPlayRequired;

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
		tfPercentPlayRequired.setText(String.valueOf(globalConfig.getPercentPlayedRequired()));
	}

	/**
	 * Initializes the graphical components
	 */
	private void init() {
		lHeader = new JLabel(PlayCountWatcher.messages.getString("GlobalConfigurationPanel.1"));
		tfPercentPlayRequired = new JTextField();
	}

	/**
	 * Builds the panel
	 */
	private void build() {
		// Set basic layout
		FormLayout layout = new FormLayout("5px, p, 5px, 50, f:5px:g", //columns
				"5px, p, f:5px:g"); //rows
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.add(lHeader, cc.xy(2, 2));
		builder.add(tfPercentPlayRequired, cc.xy(4, 2));
		
		removeAll();
		add(builder.getPanel());
	}

	/**
	 * Updates the configuration to reflect the GUI
	 *
	 * @param gc the global configuration
	 */
	public void updateConfiguration(GlobalConfiguration gc) {
		String valStr = tfPercentPlayRequired.getText();
		try {
			int newVal = Integer.parseInt(valStr);
			gc.setPercentPlayedRequired(newVal);
		} catch(NumberFormatException e) {
			log.error(String.format("'%s' could not be parsed as an int", valStr));
		}
	}
}
