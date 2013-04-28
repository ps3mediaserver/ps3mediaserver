package net.pms.external;

import net.pms.dlna.DLNAResource;

import java.util.Iterator;

/**
 * Classes implementing this interface and packaged as pms plugins will show 0-n
 * additional folders at the root level when the DLNA tree is being browsed on
 * the renderer
 * 
 * @see net.pms.plugins.DlnaTreeFolderPlugin
 */
@Deprecated
public interface AdditionalFoldersAtRoot extends ExternalListener {
	/**
	 * Gets the list of DLNAResource that will be added to the root folder. If
	 * they are functional, they will show up after the default folders.
	 * 
	 * @return an iterator containing DLNAResources that will be added to the
	 *         root folder
	 */
	public Iterator<DLNAResource> getChildren();
}