/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.dlna;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Content;
import org.jdom.Element;

import net.pms.PMS;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class Feed extends DLNAResource {
	
	@Override
	public void resolve() {
		super.resolve();
		try {
			parse();
		} catch (Exception e) {
			PMS.error("Eror in parsing stream: " + url, e);
		}
	}
	
	
	protected String name;
	protected String url;
	protected String tempItemTitle;
	protected String tempItemLink;
	protected String tempFeedLink;
	protected String tempCategory;
	protected String tempItemThumbURL;
	
	
	public Feed(String name, String url, int type) {
		super(type);
		this.url = url;
		this.name = name;
	}
	
	@SuppressWarnings("unchecked")
	public void parse() throws Exception {
		SyndFeedInput input = new SyndFeedInput();
		byte b [] = downloadAndSendBinary(url);
		if (b != null) {
			String content = new String(b, "UTF-8");
			content = stripNonValidXMLCharacters(content);
			SyndFeed feed = input.build(new XmlReader(new ByteArrayInputStream(b)));
			name = feed.getTitle();
			if (feed.getCategories() != null && feed.getCategories().size() > 0) {
				SyndCategory category = (SyndCategory) feed.getCategories().get(0);
				tempCategory = category.getName();
			}
			List<SyndEntry> entries = feed.getEntries();
			for(SyndEntry entry:entries) {
				tempItemTitle = entry.getTitle();
				tempItemLink = entry.getLink();
				tempFeedLink = entry.getUri();
				tempItemThumbURL = null;
				
				ArrayList<Element> elements = (ArrayList<Element>) entry.getForeignMarkup();
				for(Element elt:elements) {
					if ("group".equals(elt.getName()) && "media".equals(elt.getNamespacePrefix())) {
						List<Content> subElts = elt.getContent();
						for(Content subelt:subElts) {
							if (subelt instanceof Element)
								parseElement( (Element)subelt, false);
						}
					}
					parseElement(elt, true);
				}
				List<SyndEnclosure> enclosures = entry.getEnclosures();
				for(SyndEnclosure enc:enclosures) {
					if (StringUtils.isNotBlank(enc.getUrl()))
						tempItemLink = enc.getUrl();
				}
				manageItem();
			}
		}
		lastmodified = System.currentTimeMillis();
	}
	
	@SuppressWarnings("unchecked")
	private void parseElement(Element elt, boolean parseLink) {
		if ("content".equals(elt.getName()) && "media".equals(elt.getNamespacePrefix())) {
			if (parseLink)
				tempItemLink = elt.getAttribute("url").getValue();
			List<Content> subElts = elt.getContent();
			for(Content subelt:subElts) {
				if (subelt instanceof Element)
					parseElement( (Element)subelt, false);
			}
		}
		if ("thumbnail".equals(elt.getName()) && "media".equals(elt.getNamespacePrefix())) {
			if (tempItemThumbURL == null)
				tempItemThumbURL = elt.getAttribute("url").getValue();
		}
	}

	public InputStream getInputStream() throws IOException {
		return null;
	}

	public String getName() {
		return name;
	}

	public boolean isFolder() {
		return true;
	}

	public long length() {
		return 0;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return url;
	}

	@Override
	public boolean isValid() {
		return true;
	}
	
	protected void manageItem() {
		FeedItem fi = new FeedItem(tempItemTitle, tempItemLink, tempItemThumbURL, null, specificType);
		addChild(fi);
	}

	@Override
	public boolean refreshChildren() {
		if (System.currentTimeMillis() - lastmodified > 3600000) {
			try {
				parse();
			} catch (Exception e) {
				PMS.error("Eror in parsing stream: " + url, e);
			}
			return true;
		}
		return false;
		
	}
	
	/**
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    private String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }    
}
