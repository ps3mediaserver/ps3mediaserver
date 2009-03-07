package net.pms.dlna;

import java.io.File;


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
}
