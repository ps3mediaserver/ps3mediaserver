package net.pms.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import net.pms.PMS;
import net.pms.network.HTTPResource;

public class CoverUtil extends HTTPResource {
	
	private static CoverUtil instance;
	
	private static byte[] lock = null;
	static {
		lock = new byte[0];
	}
	
	public static CoverUtil get() {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new CoverUtil();
				}
			}
		}
		return instance;
	}
	
	private CoverUtil() {
		covers = new HashMap<String, byte[]>();
	}
	
	private HashMap<String, byte []> covers;
	
	public static final int AUDIO_DISCOGS = 0;
	public static final int AUDIO_AMAZON = 1;
	
	public synchronized byte [] getThumbnailFromArtistAlbum(int backend, String...infos) throws IOException {
		if (infos.length >=2 && StringUtils.isNotBlank(infos[0]) && StringUtils.isNotBlank(infos[1])) {
			String artist = URLEncoder.encode(infos[0] , "UTF-8");
			String album = URLEncoder.encode(infos[1] , "UTF-8");
			if (covers.get(artist+album) != null) {
				byte data [] = covers.get(artist+album);
				if (data.length == 0)
					return null;
				else return data;
			}
			if (backend == AUDIO_DISCOGS) {
				String url = "http://www.discogs.com/advanced_search?artist=" + artist + "&release_title=" + album + "&btn=Search+Releases";
				byte data [] = downloadAndSendBinary(url);
				if (data != null) {
					try {
						String html = new String(data, "UTF-8");
						int firstItem = html.indexOf("<li style=\"background:");
						if (firstItem > -1) {
							String detailUrl = html.substring(html.indexOf("<a href=\"/", firstItem)+10, html.indexOf("\"><em>", firstItem));
							data = downloadAndSendBinary("http://www.discogs.com/" + detailUrl);
							html = new String(data, "UTF-8");
							firstItem = html.indexOf("<a href=\"/viewimages?");
							if (firstItem > -1) {
								String imageUrl = html.substring(html.indexOf("<img src=\"", firstItem)+10, html.indexOf("\" border", firstItem));
								data = downloadAndSendBinary(imageUrl);
								if (data != null)
									covers.put(artist+album, data);
								else
									covers.put(artist+album, new byte[0]);
								return data;
							}
						}
					} catch (Exception e) {
						PMS.error("Error while retrieving cover for " + artist+album, e);
					}
				}
			} else if (backend == AUDIO_AMAZON) {
				String url = "http://www.amazon.com/gp/search/ref=sr_adv_m_pop/?search-alias=popular&unfiltered=1&field-keywords=&field-artist=" + artist + "&field-title=" + album + "&field-label=&field-binding=&sort=relevancerank&Adv-Srch-Music-Album-Submit.x=35&Adv-Srch-Music-Album-Submit.y=13";
				byte data [] = downloadAndSendBinary(url);
				if (data != null) {
					try {
						String html = new String(data, "UTF-8");
						int firstItem = html.indexOf("class=\"imageColumn\"");
						if (firstItem > -1) {
							int imageUrlPos = html.indexOf("src=\"", firstItem)+5;
							String imageUrl = html.substring(imageUrlPos, html.indexOf("\" class", imageUrlPos));
							data = downloadAndSendBinary(imageUrl);
							if (data != null)
								covers.put(artist+album, data);
							else
								covers.put(artist+album, new byte[0]);
							return data;
						}
					} catch (Exception e) {
						PMS.error("Error while retrieving cover for " + artist+album, e);
					}
				}
			}
			covers.put(artist+album, new byte[0]);
		}
		return null;
	}

}
