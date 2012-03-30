package net.pms.plugin.filedetail;

import java.io.IOException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.helpers.TmdbHelper;
import net.pms.medialibrary.external.FileDetailPlugin;
import net.pms.medialibrary.external.TreeEntry;

public class TmdbRatingPlugin implements FileDetailPlugin {
	private DOVideoFileInfo video;
	private String displayName;
	private static Icon ratingIcon;
	
	public TmdbRatingPlugin(){
		if(ratingIcon == null){
			URL icon = getClass().getResource("/resources/images/star-16.png");
			if(icon != null) {
				ratingIcon = new ImageIcon(icon);
			}
		}
	}

	@Override
    public boolean isFolder() {
	    return true;
    }

	@Override
    public String getName() {
	    return "Rate on TMDb";
    }

	@Override
    public Icon getTreeIcon() {
		Icon res = null;
		URL icon = getClass().getResource("/tmdb.png");
		if(icon != null) {
			res = new ImageIcon(icon);
		}
		return res;
    }

	@Override
    public JPanel getConfigurationPanel() {
	    return null;
    }

	@Override
    public void loadConfiguration(String saveFilePath) throws IOException {
	    // do nothing
	    
    }

	@Override
    public void saveConfiguration(String saveFilePath) throws IOException {
	    // do nothing
    }

	@Override
    public DLNAResource getResource() {
	    VirtualFolder vf = new VirtualFolder(displayName, null);
	    for(int i = 20 ; i >= 0; i--){
		    vf.addChild(new RatingResource(video, ((float)i) / 2));
	    }
	    return vf;
    }

	@Override
    public void setVideo(DOVideoFileInfo video) {
	    this.video = video;
    }

	@Override
    public void setDisplayName(String displayName) {
	    this.displayName = displayName;
    }

	@Override
    public MutableTreeNode getTreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(getName());
	    for(int i = 20 ; i >= 0; i--){
	    	DefaultMutableTreeNode cn = new DefaultMutableTreeNode(new TreeEntry(String.valueOf(((float)i) / 2).replace(".0", ""), ratingIcon));
	    	node.add(cn);	    	
	    }
		
	    return node;
    }

	@Override
    public boolean isAvailable() {
	    if(TmdbHelper.getSession() != null){
	    	return true;
	    }
	    return false;
    }

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public String getDescription() {
		return "Lets you rate movies on tmdb directly from your sofa";
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
