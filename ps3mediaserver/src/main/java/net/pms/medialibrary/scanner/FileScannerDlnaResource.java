package net.pms.medialibrary.scanner;

import java.io.IOException;
import java.io.InputStream;

import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAResource;

/**
 * This class is being used by the FileScanner. It sets the parent of the
 * RealFile being used for parsing to an instance of this class in order to make
 * the usage of mediainfo or ffmpeg configurable through the mediainfo flag
 * 
 * @author pw
 * 
 */
public class FileScannerDlnaResource extends DLNAResource {

	public FileScannerDlnaResource() {
		setDefaultRenderer(RendererConfiguration.getRendererConfigurationByUA("FileParsingRenderer"));
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getSystemName() {
		return null;
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public boolean isValid() {
		return false;
	}

}
