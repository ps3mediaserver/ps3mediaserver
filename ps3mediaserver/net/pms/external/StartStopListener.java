package net.pms.external;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;

public interface StartStopListener extends ExternalListener {
	public void nowPlaying(DLNAMediaInfo media, DLNAResource resource);
	public void donePlaying(DLNAMediaInfo media, DLNAResource resource);
}
