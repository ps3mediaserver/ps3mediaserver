package net.pms.plugin.filedetail.tmdbrater.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.savvasdalkitsis.jtmdb.Session;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.plugin.filedetail.TmdbHelper;
import net.pms.plugin.filedetail.tmdbrater.configuration.GlobalConfiguration;

public class GlobalConfigurationPanel extends JPanel {
	private static final long serialVersionUID = -5311911715182147203L;
	private Session session;
	
	private JLabel lHeader;
	private JLabel lUserName;
	private JButton bLogIn;

	/**
	 * Instantiates a new global configuration panel.
	 *
	 * @param globalConfig the global configuration
	 */
	public GlobalConfigurationPanel(GlobalConfiguration globalConfig) {
		setLayout(new GridLayout());
		session = new Session(globalConfig.getUserName(), globalConfig.getSession());
		
		init();
		build();
	}

	public void applyConfig() {	
		String userNameStr = session == null ? null : session.getUserName();
		if(userNameStr == null || userNameStr.equals("")) {
			userNameStr = "none";
		}
		lUserName.setText(userNameStr);
	}

	private void init() {
		lHeader= new JLabel("Logged in user:");
		lUserName = new JLabel();
		bLogIn = new JButton("Authenticate");
		bLogIn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				session =  TmdbHelper.createSession(bLogIn);
				applyConfig();
			}
		});
	}

	private void build() {
		// Set basic layout
		FormLayout layout = new FormLayout("5px, p, 5px, p, 5px, p, f:5px:g", //columns
				"5px, p, f:5px:g"); //rows
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.add(lHeader, cc.xy(2, 2));
		builder.add(lUserName, cc.xy(4, 2));
		builder.add(bLogIn, cc.xy(6, 2));
		
		removeAll();
		add(builder.getPanel());
	}

	public void updateConfiguration(GlobalConfiguration gc) {
		if(session != null) {
			gc.setSession(session.getSession());
			gc.setUserName(session.getUserName());
		}
	}
}
