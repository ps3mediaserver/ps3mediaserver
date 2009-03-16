package net.pms.dlna;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import net.pms.PMS;


public class DLNAMediaSubtitle  extends DLNAMediaLang {
	
	public static int SUBRIP = 1;
	public static int MICRODVD = 2;
	public static int SAMI = 3;
	public static int ASS = 4;
	public static int VOBSUB = 5;
	public static int EMBEDDED = 6;
	
	public static String subExtensions[] = new String [] {"srt", "sub", "smi", "ass", "idx" };
	
	public int type;
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
		if (type == 1)
			return "SubRip";
		if (type == 2)
			return "MicroDVD";
		if (type == 3)
			return "Sami";
		if (type == 4)
			return "ASS/SSA";
		if (type == 5)
			return "VobSub";
		if (type == 6)
			return "Embedded";
		return "-";
	}

	public String toString() {
		return "Sub: " + getSubType() + " / lang: " + lang + " / ID: " + id + " / FILE: " + (file!=null?file.getAbsolutePath():"-");
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
				if (b1 == 255 && b2 == 254)
					is_file_unicode = 1;
				else if (b1 == 254 && b2 == 255) 
					is_file_unicode = 2;
				else if (b1 == 239 && b2 == 187 && b3 == 191)
					is_file_utf8 = true;
				
				//Mplayer doesn't handle UTF-16 encoded subs
				if (is_file_unicode > 0) {
					is_file_utf8 = true;
					utf8_file = new File(PMS.getConfiguration().getTempFolder(), "utf8_" + file.getName());
					if (!utf8_file.exists()) {
						InputStreamReader r = new InputStreamReader(new FileInputStream(file), is_file_unicode==1?"UTF-16":"UTF-16BE");
						OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(utf8_file), "UTF-8");
						int c;
						while((c=r.read()) != -1) {
							osw.write(c);
						}
						osw.close();
						r.close();
					}
				}
			} catch (IOException e) {
				PMS.error(null, e);
			} finally {
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {}
			}
		}
	}
}
