package net.pms.dlna;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import net.pms.PMS;
import net.pms.formats.Format;

public class PlaylistFolder extends DLNAResource {
	
	private File playlistfile;
	public File getPlaylistfile() {
		return playlistfile;
	}

	private boolean valid = true;
	
	public PlaylistFolder(File f) {
		playlistfile = f;
		lastmodified = playlistfile.lastModified();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return playlistfile.getName();
	}

	@Override
	public String getSystemName() {
		return playlistfile.getName();
	}

	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public long lastModified() {
		return playlistfile.lastModified();
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public void resolve() {
		if (playlistfile.length() < 10000000) {
			ArrayList<String> entries = new ArrayList<String>();
			try {
				BufferedReader br = new BufferedReader(new FileReader(playlistfile));
				String line = null;
				boolean pls = false;
				while ((line=br.readLine()) !=null) {
					if (!line.startsWith("#")) {
						if (line.equals("[playlist]"))
							pls = true;
						if (!pls) {
							entries.add(line);
						} else {
							if (line.startsWith("File")) {
								line = line.substring(line.indexOf("=")+1);
								entries.add(line);
							}
						}
					}
				}
				br.close();
			} catch (IOException e) {
				PMS.error(null, e);
			}
			for(String entry:entries) {
				if (!entry.toLowerCase().startsWith("http://") && !entry.toLowerCase().startsWith("mms://")) {
					File en1= new File(playlistfile.getParentFile(), entry);
					File en2= new File(entry);
					if (en1.exists()) {
						addChild(new RealFile(en1));
						valid = true;
					} else {
						if (en2.exists()) {
							addChild(new RealFile(en2));
							valid = true;
						}
					}
				}
			}
			if (PMS.getConfiguration().getUseCache()) {
				if (!PMS.get().getDatabase().isDataExists(playlistfile.getAbsolutePath(), playlistfile.lastModified())) {
					PMS.get().getDatabase().insertData(playlistfile.getAbsolutePath(), playlistfile.lastModified(), Format.PLAYLIST, null);
				}
			}
			for(DLNAResource r:children) {
				r.resolve();
			}
		}
	}

}
