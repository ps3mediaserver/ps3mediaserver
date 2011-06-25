package net.pms.external;

import net.pms.dlna.DLNAResource;

public interface AdditionalResourceFolderListener extends ExternalListener {
	/**
	 * Allows to add a virtual folder resource, similar to the #transcoded folder
	 * @param currentResource Parent resource
	 * @param child 
	 */
	public void addAdditionalFolder(DLNAResource currentResource, DLNAResource child);
}
