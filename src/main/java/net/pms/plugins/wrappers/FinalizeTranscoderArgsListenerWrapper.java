package net.pms.plugins.wrappers;

import java.util.List;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.encoders.Player;
import net.pms.io.OutputParams;
import net.pms.plugins.FinalizeTranscoderArgsListener;

public class FinalizeTranscoderArgsListenerWrapper extends BaseWrapper implements FinalizeTranscoderArgsListener {
	private net.pms.external.FinalizeTranscoderArgsListener listener;
	
	public FinalizeTranscoderArgsListenerWrapper(net.pms.external.FinalizeTranscoderArgsListener listener) {
		super(listener);
		this.listener = listener;
	}

	@Override
	public List<String> finalizeTranscoderArgs(Player player, String filename,
			DLNAResource dlna, DLNAMediaInfo media, OutputParams params,
			List<String> cmdList) {
		return listener.finalizeTranscoderArgs(player, filename, dlna, media, params, cmdList);
	}
}
