package net.pms.plugins.wrappers;

import net.pms.external.AdditionalFolderAtRoot;
import net.pms.plugins.Plugin;

/**
 * Wraps the old style plugin {@link net.pms.external.AdditionalFolderAtRoot} to be used by the new plugin system
 */
@SuppressWarnings("deprecation")
public class AdditionalFolderAtRootWrapper extends BaseWrapper implements Plugin {
	private AdditionalFolderAtRoot folder;

	/**
	 * Instantiates a new wrapper.
	 *
	 * @param folder the additional folder at root
	 */
	public AdditionalFolderAtRootWrapper(AdditionalFolderAtRoot folder) {
		super(folder);
		this.folder = folder;
	}

	/**
	 * Gets the folder.
	 *
	 * @return the additional folder at root
	 */
	public AdditionalFolderAtRoot getFolder() {
		return folder;
	}
}
