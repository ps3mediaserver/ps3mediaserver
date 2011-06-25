package net.pms.external;

import net.pms.dlna.DLNAResource;

public interface AdditionalFolderAtRoot extends ExternalListener {
	public DLNAResource getChild();
}
