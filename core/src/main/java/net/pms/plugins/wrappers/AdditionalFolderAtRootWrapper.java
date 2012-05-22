package net.pms.plugins.wrappers;

import net.pms.external.AdditionalFolderAtRoot;

/**
 * Wraps the old style plugin {@link net.pms.external.AdditionalFolderAtRoot} to be used by the new plugin system
 */
@SuppressWarnings("deprecation")
public class AdditionalFolderAtRootWrapper extends BaseWrapper {
	private AdditionalFolderAtRoot folder;

	/**
	 * Instantiates a new additional folder at root wrapper.
	 *
	 * @param folder the additional folder
	 */
	public AdditionalFolderAtRootWrapper(AdditionalFolderAtRoot folder) {
		super(folder);
		this.folder = folder;
	}

	/**
	 * Gets the folder.
	 *
	 * @return the folder
	 */
	public AdditionalFolderAtRoot getFolder() {
		return folder;
	}
}
