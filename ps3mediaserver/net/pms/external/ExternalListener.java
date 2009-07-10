package net.pms.external;

import javax.swing.JComponent;

public interface ExternalListener {
	
	public JComponent config();
	public String name();
	public void shutdown();

}
