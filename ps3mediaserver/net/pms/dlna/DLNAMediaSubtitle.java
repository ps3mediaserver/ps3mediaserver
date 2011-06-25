package net.pms.dlna;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import net.pms.PMS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DLNAMediaSubtitle extends DLNAMediaLang implements Cloneable {
	public static final Logger logger = LoggerFactory.getLogger(DLNAMediaSubtitle.class);
	public static final int SUBRIP = 1;
	public static final int TEXT = 2;
	public static final int MICRODVD = 3;
	public static final int SAMI = 4;
	public static final int ASS = 5;
	public static final int VOBSUB = 6;
	public static final int EMBEDDED = 7;
	public static String subExtensions[] = new String[]{"srt", "txt", "sub", "smi", "ass", "idx"};
	public int type;
	public String flavor;
	public File file;
	private File utf8_file;
	public boolean is_file_utf8;

	public File getPlayableFile() {
		if (utf8_file != null) {
			return utf8_file;
		}
		return file;
	}

	public String getSubType() {
		switch (type) {
			case SUBRIP:
				return "SubRip";
			case TEXT:
				return "Text File";
			case MICRODVD:
				return "MicroDVD";
			case SAMI:
				return "Sami";
			case ASS:
				return "ASS/SSA";
			case VOBSUB:
				return "VobSub";
			case EMBEDDED:
				return "Embedded";
		}
		return "-";
	}

	public String toString() {
		return "Sub: " + getSubType() + " / lang: " + lang + " / ID: " + id + " / FILE: " + (file != null ? file.getAbsolutePath() : "-");
	}

	public void checkUnicode() {
		if (file != null && file.exists() && file.length() > 3) {
			FileInputStream fis = null;
			try {
				int is_file_unicode = 0;

				fis = new FileInputStream(file);
				int b1 = fis.read();
				int b2 = fis.read();
				int b3 = fis.read();
				if (b1 == 255 && b2 == 254) {
					is_file_unicode = 1;
				} else if (b1 == 254 && b2 == 255) {
					is_file_unicode = 2;
				} else if (b1 == 239 && b2 == 187 && b3 == 191) {
					is_file_utf8 = true;
				}

				// MPlayer doesn't handle UTF-16 encoded subs
				if (is_file_unicode > 0) {
					is_file_utf8 = true;
					utf8_file = new File(PMS.getConfiguration().getTempFolder(), "utf8_" + file.getName());
					if (!utf8_file.exists()) {
						InputStreamReader r = new InputStreamReader(new FileInputStream(file), is_file_unicode == 1 ? "UTF-16" : "UTF-16BE");
						OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(utf8_file), "UTF-8");
						int c;
						while ((c = r.read()) != -1) {
							osw.write(c);
						}
						osw.close();
						r.close();
					}
				}
			} catch (IOException e) {
				logger.error(null, e);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
