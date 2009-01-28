package net.pms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
		if (Platform.isWindows()) {
			// get Windows Arial
			String font = getFont("C:\\Windows\\Fonts", "Arial.ttf");
			if (font == null)
				font = getFont("C:\\WINNT\\Fonts", "Arial.ttf");
			if (font == null)
				font = getFont(".\\win32\\mplayer\\", "subfont.ttf");
			return font;
		} else if (Platform.isLinux()) {
			// get Linux default font
			String font = getFont("/usr/share/fonts/truetype/ttf-bitstream-veras/", "Vera.ttf");
			if (font == null)
				font = getFont("/usr/share/fonts/truetype/ttf-dejavu/", "DejaVuSans.ttf");
			return font;
		} else if (Platform.isMac()) {
			// get default osx font
			String font = getFont("/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib/fonts/", "LucidaSansRegular.ttf");
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

}
