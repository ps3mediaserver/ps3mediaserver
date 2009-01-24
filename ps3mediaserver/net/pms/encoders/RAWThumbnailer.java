package net.pms.encoders;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.JComponent;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.InternalJavaProcessImpl;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;

public class RAWThumbnailer extends Player {
	
	public static String ID = "rawthumbs"; //$NON-NLS-1$

	protected String [] getDefaultArgs() {
		return new String [] { "-e" , "-c"}; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String[] args() {
		return getDefaultArgs();
			
	}

	@Override
	public JComponent config() {
		return null;
	}

	@Override
	public String executable() {
		return PMS.getConfiguration().getDCRawPath();
	}

	@Override
	public String id() {
		return ID; //$NON-NLS-1$
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media,
			OutputParams params) throws IOException {
		
		params.waitbeforestart = 1;
		params.minBufferSize = 1;
		params.maxBufferSize = 20;
		params.hidebuffer = true;
		
		if (media.thumb == null)
			return null;
		
		ProcessWrapper pw = new InternalJavaProcessImpl(new ByteArrayInputStream(media.thumb));
		return pw;
	}

	@Override
	public String mimeType() {
		return "image/jpeg"; //$NON-NLS-1$
	}

	@Override
	public String name() {
		return "Raws Thumbnailer"; //$NON-NLS-1$
	}

	@Override
	public int purpose() {
		return MISC_PLAYER;
	}

	@Override
	public int type() {
		return Format.IMAGE;
	}

}
