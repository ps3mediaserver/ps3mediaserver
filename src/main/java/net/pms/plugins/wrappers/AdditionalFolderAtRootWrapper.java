package net.pms.plugins.wrappers;

import net.pms.external.AdditionalFolderAtRoot;

public class AdditionalFolderAtRootWrapper extends BaseWrapper {
	private AdditionalFolderAtRoot folder;
	
	public AdditionalFolderAtRootWrapper(AdditionalFolderAtRoot folder) {
		super(folder);
		this.folder = folder;
	}

	public AdditionalFolderAtRoot getFolder() {
		return folder;
	}
}
