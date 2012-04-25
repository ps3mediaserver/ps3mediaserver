package net.pms.plugins.wrappers;

import javax.swing.JComponent;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.plugins.StartStopListener;

public class StartStopListenerWrapper extends BaseWrapper implements StartStopListener {
	private net.pms.external.StartStopListener listener;

	public StartStopListenerWrapper(net.pms.external.StartStopListener listener) {
		super(listener);
		this.listener = listener;
	}

	@Override
	public JComponent getGlobalConfigurationPanel() {
		return listener.config();
	}

	@Override
	public void nowPlaying(DLNAMediaInfo media, DLNAResource resource) {
		listener.nowPlaying(media, resource);
	}

	@Override
	public void donePlaying(DLNAMediaInfo media, DLNAResource resource) {
		listener.donePlaying(media, resource);
	}

}
