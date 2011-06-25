package net.pms.external;

import java.util.Iterator;
import net.pms.dlna.DLNAResource;

public interface AdditionalFoldersAtRoot extends ExternalListener {
	public Iterator<DLNAResource> getChildren();
}
