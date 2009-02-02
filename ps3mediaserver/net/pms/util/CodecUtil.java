package net.pms.util;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.sun.jna.Platform;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;

public class CodecUtil {
	
	private static ArrayList<String> codecs;
	
	public static ArrayList<String> getPossibleCodecs() {
		if (codecs == null) {
			codecs = new ArrayList<String>();
			InputStream is = CodecUtil.class.getClassLoader().getResourceAsStream("resources/ffmpeg_formats.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			try {
				while ((line=br.readLine()) != null) {
					if (line.contains(" "))
						codecs.add(line.substring(0, line.indexOf(" ")));
					else
						codecs.add(line);
				}
				br.close();
				codecs.add("iso");
			} catch (IOException e) {
				PMS.error("Error while retrieving codec list", e);
			}
		}
		return codecs;
	}
	
	public static int getAC3Bitrate(PmsConfiguration configuration, DLNAMediaInfo media) {
		int defaultBitrate = configuration.getAudioBitrate();
		if (media != null && defaultBitrate >= 384) {
			if (media.nrAudioChannels == 2) {
				defaultBitrate = 384;
			} else if (media.nrAudioChannels == 1) {
				defaultBitrate = 192;
			}
		}
		return defaultBitrate;
	}
	
	public static String getDefaultFontPath() {
		String font = getFontPathHack("Arial");
		if (font != null)
			return font;
		// in case of hack not working
		if (Platform.isWindows()) {
			// get Windows Arial
			font = getFont("C:\\Windows\\Fonts", "Arial.ttf");
			if (font == null)
				font = getFont("C:\\WINNT\\Fonts", "Arial.ttf");
			if (font == null)
				font = getFont("D:\\Windows\\Fonts", "Arial.ttf");
			if (font == null)
				font = getFont(".\\win32\\mplayer\\", "subfont.ttf");
			return font;
		} else if (Platform.isLinux()) {
			// get Linux default font
			font = getFont("/usr/share/fonts/truetype/msttcorefonts/", "Arial.ttf");
			if (font == null)
				font = getFont("/usr/share/fonts/truetype/ttf-bitstream-veras/", "Vera.ttf");
			if (font == null)
				font = getFont("/usr/share/fonts/truetype/ttf-dejavu/", "DejaVuSans.ttf");
			return font;
		} else if (Platform.isMac()) {
			// get default osx font
			font = getFont("/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib/fonts/", "LucidaSansRegular.ttf");
			return font;
		}
		return null;
	}
	
	private static String getFont(String path, String name) {
		File f = new File(path, name);
		if (f.exists())
			return f.getAbsolutePath();
		return null;
	}
	
	private static String getFontPathHack(String fontName) {
		// Ugly hack to retrieve default font path if run on Sun JRE
		Font f = new Font(fontName, Font.PLAIN, 12);
		f.getFamily();
		f.getAttributes();
		Object font2Dhandle = getValue(f, "font2DHandle");
		if (font2Dhandle != null) {
			Object font2D = getValue(font2Dhandle, "font2D");
			if (font2D != null) {
				Object platName = getValue(font2D, "platName");
				if (platName != null)
					return platName.toString();
			}
		}
		return null;
	}

	private static Object getValue(Object obj, String field) {
		try {
			Field f = null;
			try {
				f = obj.getClass().getDeclaredField(field);
			} catch (NoSuchFieldException nsfe) {
				try {
					if (obj.getClass().getSuperclass() != null)
						f = obj.getClass().getSuperclass().getDeclaredField(field);
				} catch (NoSuchFieldException nsfe2) {
					if (obj.getClass().getSuperclass().getSuperclass() != null)
						f = obj.getClass().getSuperclass().getSuperclass().getDeclaredField(field);
				}
			}
			f.setAccessible(true);
			return f.get(obj);
		} catch (Exception e) {}
			return null;
		}

}
