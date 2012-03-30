package net.pms.plugin.dnlatreefolder;

import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;
import net.pms.medialibrary.external.DlnaTreeFolderPlugin;

public class VideoSettingsFolderPlugin implements DlnaTreeFolderPlugin {
	private static final Logger log = LoggerFactory.getLogger(VideoSettingsFolderPlugin.class);
	private String rootFolderName = "root";

	public VideoSettingsFolderPlugin() {
	}

	@Override
	public JPanel getConfigurationPanel() {
		return null;
	}

	@Override
	public DLNAResource getDLNAResource() {
		VirtualFolder vf = new VirtualFolder(rootFolderName, null);
		VirtualFolder vfSub = new VirtualFolder(Messages.getString("PMS.8"), null);
		vf.addChild(vfSub);
		
		vf.addChild(new VirtualVideoAction(Messages.getString("PMS.3"), PMS.getConfiguration().isMencoderNoOutOfSync()) {
			public boolean enable() {
				PMS.getConfiguration().setMencoderNoOutOfSync(!PMS.getConfiguration().isMencoderNoOutOfSync());
				return PMS.getConfiguration().isMencoderNoOutOfSync();
			}
		});
		
		vf.addChild(new VirtualVideoAction(Messages.getString("PMS.14"), PMS.getConfiguration().isMencoderMuxWhenCompatible()) { 
			public boolean enable() {
				PMS.getConfiguration().setMencoderMuxWhenCompatible(!PMS.getConfiguration().isMencoderMuxWhenCompatible());
				
				return  PMS.getConfiguration().isMencoderMuxWhenCompatible();
			}
		});
		
		vf.addChild(new VirtualVideoAction("  !!-- Fix 23.976/25fps A/V Mismatch --!!", PMS.getConfiguration().isFix25FPSAvMismatch()) {
			public boolean enable() {
				PMS.getConfiguration().setMencoderForceFps(!PMS.getConfiguration().isFix25FPSAvMismatch());
				PMS.getConfiguration().setFix25FPSAvMismatch(!PMS.getConfiguration().isFix25FPSAvMismatch());
				return PMS.getConfiguration().isFix25FPSAvMismatch();
			}              
		});
		
		
		vf.addChild(new VirtualVideoAction(Messages.getString("PMS.4"), PMS.getConfiguration().isMencoderYadif()) {
			public boolean enable() {
				PMS.getConfiguration().setMencoderYadif(!PMS.getConfiguration().isMencoderYadif());
				
				return  PMS.getConfiguration().isMencoderYadif();
			}
		});
		
		vfSub.addChild(new VirtualVideoAction(Messages.getString("PMS.10"), PMS.getConfiguration().isMencoderDisableSubs()) {
			public boolean enable() {
				boolean oldValue = PMS.getConfiguration().isMencoderDisableSubs();
				boolean newValue = ! oldValue;
				PMS.getConfiguration().setMencoderDisableSubs( newValue );
				return newValue;
			}
		});
		
		vfSub.addChild(new VirtualVideoAction(Messages.getString("PMS.6"), PMS.getConfiguration().getUseSubtitles()) {
			public boolean enable() {
				boolean oldValue = PMS.getConfiguration().getUseSubtitles();
				boolean newValue = ! oldValue;
				PMS.getConfiguration().setUseSubtitles( newValue );
				return newValue;
			}
		});
		
		vfSub.addChild(new VirtualVideoAction(Messages.getString("MEncoderVideo.36"), PMS.getConfiguration().isMencoderAssDefaultStyle()) {
			public boolean enable() {
				boolean oldValue = PMS.getConfiguration().isMencoderAssDefaultStyle();
				boolean newValue = ! oldValue;
				PMS.getConfiguration().setMencoderAssDefaultStyle( newValue );
				return newValue;
			}
		});
		
		vf.addChild(new VirtualVideoAction(Messages.getString("PMS.7"), PMS.getConfiguration().getSkipLoopFilterEnabled()) {
			public boolean enable() {
				PMS.getConfiguration().setSkipLoopFilterEnabled( !PMS.getConfiguration().getSkipLoopFilterEnabled() );
				return PMS.getConfiguration().getSkipLoopFilterEnabled();
			}
		});

		vf.addChild(new VirtualVideoAction(Messages.getString("TrTab2.28"), PMS.getConfiguration().isDTSEmbedInPCM()) {
			@Override
			public boolean enable() {
				PMS.getConfiguration().setDTSEmbedInPCM(!PMS.getConfiguration().isDTSEmbedInPCM());
				return PMS.getConfiguration().isDTSEmbedInPCM();
			}
		});
		
		vf.addChild(new VirtualVideoAction(Messages.getString("PMS.27"), true) {
			public boolean enable() {
	                try {
	                    PMS.getConfiguration().save();
                    } catch (ConfigurationException e) {
	                    log.error("Failed to save configuration", e);
                    }
				return true;
			}
		});

		vf.addChild(new VirtualVideoAction(Messages.getString("LooksFrame.12"), true) {
			public boolean enable() {
				PMS.get().reset();
				return true;
			}
		});
		//vf.closeChildren(0, false);
		return vf;
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(getClass().getResource("/VideoSettingsFolder_icon.png"));
	}

	@Override
	public String getName() {
		return "Video Properties";
	}
	
	@Override
	public void setDisplayName(String name){
		rootFolderName = name;
	}

	@Override
	public void loadConfiguration(String configFilePath) throws IOException {
	}

	@Override
	public void saveConfiguration(String configFilePath) throws IOException {
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
    public MutableTreeNode getTreeNode() {
	    return null;
    }

	@Override
    public boolean isAvailable() {
	    return true;
    }

	@Override
	public int getVersion() {
		return 2;
	}

	@Override
	public String getDescription() {
		return "Shows a folder containing some commently used configurations to be changed directly from your sofa.";
	}

	@Override
	public void shutdown() {
		// do nothing
	}

	@Override
	public JComponent getGlobalConfigurationPanel() {
		return null;
	}
}
