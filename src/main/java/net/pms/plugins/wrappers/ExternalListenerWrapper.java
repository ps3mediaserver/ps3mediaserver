package net.pms.plugins.wrappers;

import net.pms.external.ExternalListener;

/**
 * Wraps the old style plugin {@link net.pms.external.ExternalListener} to be used by the new plugin system
 */
@SuppressWarnings("deprecation")
public class ExternalListenerWrapper extends BaseWrapper {

	/**
	 * Instantiates a new external listener wrapper.
	 *
	 * @param listener the listener
	 */
	public ExternalListenerWrapper(ExternalListener listener) {
		super(listener);
	}
}
