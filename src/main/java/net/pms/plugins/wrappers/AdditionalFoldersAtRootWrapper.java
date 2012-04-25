package net.pms.plugins.wrappers;

import net.pms.external.AdditionalFoldersAtRoot;

public class AdditionalFoldersAtRootWrapper extends BaseWrapper {
	private AdditionalFoldersAtRoot folders;
	
	public AdditionalFoldersAtRootWrapper(AdditionalFoldersAtRoot folders) {
		super(folders);
		this.folders = folders;
	}

	public AdditionalFoldersAtRoot getFolders() {
		return folders;
	}
}
