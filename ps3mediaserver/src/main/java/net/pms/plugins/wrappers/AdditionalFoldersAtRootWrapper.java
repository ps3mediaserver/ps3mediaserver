package net.pms.plugins.wrappers;

import net.pms.external.AdditionalFoldersAtRoot;
import net.pms.plugins.Plugin;

/**
 * Wraps the old style plugin {@link net.pms.external.AdditionalFoldersAtRoot} to be used by the new plugin system
 */
@SuppressWarnings("deprecation")
public class AdditionalFoldersAtRootWrapper extends BaseWrapper implements Plugin {
	private AdditionalFoldersAtRoot folders;

	/**
	 * Instantiates a new additional folders at root wrapper.
	 *
	 * @param folders the additional folders
	 */
	public AdditionalFoldersAtRootWrapper(AdditionalFoldersAtRoot folders) {
		super(folders);
		this.folders = folders;
	}

	/**
	 * Gets the folders.
	 *
	 * @return the folders
	 */
	public AdditionalFoldersAtRoot getFolders() {
		return folders;
	}
}
