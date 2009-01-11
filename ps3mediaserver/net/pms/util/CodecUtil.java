package net.pms.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.pms.PMS;

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

}
