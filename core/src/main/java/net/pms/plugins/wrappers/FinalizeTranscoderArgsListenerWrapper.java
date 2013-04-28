package net.pms.plugins.wrappers;

import java.util.List;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.encoders.Player;
import net.pms.io.OutputParams;
import net.pms.plugins.FinalizeTranscoderArgsListener;

/**
 * Wraps the old style plugin {@link net.pms.external.FinalizeTranscoderArgsListener} to be used by the new plugin system
 */
@SuppressWarnings("deprecation")
public class FinalizeTranscoderArgsListenerWrapper extends BaseWrapper implements FinalizeTranscoderArgsListener {
	private net.pms.external.FinalizeTranscoderArgsListener listener;

	/**
	 * Instantiates a new finalize transcoder args listener wrapper.
	 *
	 * @param listener the listener
	 */
	public FinalizeTranscoderArgsListenerWrapper(net.pms.external.FinalizeTranscoderArgsListener listener) {
		super(listener);
		this.listener = listener;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.FinalizeTranscoderArgsListener#finalizeTranscoderArgs(net.pms.encoders.Player, java.lang.String, net.pms.dlna.DLNAResource, net.pms.dlna.DLNAMediaInfo, net.pms.io.OutputParams, java.util.List)
	 */
	@Override
	public List<String> finalizeTranscoderArgs(Player player, String filename, DLNAResource dlna, 
			DLNAMediaInfo media, OutputParams params, List<String> cmdList) {
		return listener.finalizeTranscoderArgs(player, filename, dlna, media, params, cmdList);
	}
}
