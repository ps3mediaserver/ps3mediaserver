package net.pms.plugins.wrappers;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.plugins.StartStopListener;

/**
 * Wraps the old style plugin {@link net.pms.external.StartStopListener} to be used by the new plugin system
 */
@SuppressWarnings("deprecation")
public class StartStopListenerWrapper extends BaseWrapper implements StartStopListener {
	
	/** The listener. */
	private net.pms.external.StartStopListener listener;

	/**
	 * Instantiates a new start stop listener wrapper.
	 *
	 * @param listener the listener
	 */
	public StartStopListenerWrapper(net.pms.external.StartStopListener listener) {
		super(listener);
		this.listener = listener;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.StartStopListener#nowPlaying(net.pms.dlna.DLNAMediaInfo, net.pms.dlna.DLNAResource)
	 */
	@Override
	public void nowPlaying(DLNAMediaInfo media, DLNAResource resource) {
		listener.nowPlaying(media, resource);
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.StartStopListener#donePlaying(net.pms.dlna.DLNAMediaInfo, net.pms.dlna.DLNAResource)
	 */
	@Override
	public void donePlaying(DLNAMediaInfo media, DLNAResource resource) {
		listener.donePlaying(media, resource);
	}
}
