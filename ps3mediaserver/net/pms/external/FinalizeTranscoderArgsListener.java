package net.pms.external;

import java.util.List;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.io.OutputParams;

public interface FinalizeTranscoderArgsListener extends ExternalListener {
    public List<String> finalizeTranscoderArgs(
        String engine,
        String filename,
        DLNAResource dlna,
        DLNAMediaInfo media,
        OutputParams params,
        List<String> cmdList
    );
}
