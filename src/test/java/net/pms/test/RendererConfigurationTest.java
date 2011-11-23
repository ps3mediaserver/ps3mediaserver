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

package net.pms.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.pms.configuration.RendererConfiguration;

import org.junit.Before;
import org.junit.Test;

/**
 * Test the RendererConfiguration class 
 */
public class RendererConfigurationTest {
	private final Map<String, String> testCases = new HashMap<String, String>();

    @Before
    public void setUp() {
    	// From PS3.conf:
    	// # PlayStation 3 uses the following strings:
    	// #
    	// # User-Agent: PLAYSTATION 3
    	// # ---
    	// # User-Agent: UPnP/1.0
    	// # X-AV-Client-Info: av=5.0; cn="Sony Computer Entertainment Inc."; mn="PLAYSTATION 3"; mv="1.0";
    	// # ---
    	// # User-Agent: UPnP/1.0 DLNADOC/1.50
    	// # X-AV-Client-Info: av=5.0; cn="Sony Computer Entertainment Inc."; mn="PLAYSTATION 3"; mv="1.0";
    	testCases.put("User-Agent: PLAYSTATION 3", "Playstation 3");
    	testCases.put("X-AV-Client-Info: av=5.0; cn=\"Sony Computer Entertainment Inc.\"; mn=\"PLAYSTATION 3\"; mv=\"1.0\"", "Playstation 3");

    	// From AirPlayer.conf:
    	// # User-Agent: AirPlayer/1.0.09 CFNetwork/485.13.9 Darwin/11.0.0
    	// # User-Agent: Lavf52.54.0
    	testCases.put("User-Agent: AirPlayer/1.0.09 CFNetwork/485.13.9 Darwin/11.0.0", "AirPlayer");
    	testCases.put("User-Agent: Lavf52.54.0", "AirPlayer");

    	// From Philips.conf:
    	// # Renderer name string:  Allegro-Software-WebClient/4.61 DLNADOC/1.00
    	testCases.put("User-Agent: Allegro-Software-WebClient/4.61 DLNADOC/1.00", "Philips Aurea");

    	// From Realtek.conf:
    	// #[New I/O server worker #1-2] TRACE 22:46:50.077 Media renderer was not recognized. HTTP User agent :POSIX UPnP/1.0 Intel MicroStack/1.0.2718, RealtekMediaCenter, DLNADOC/1.50
    	// #[New I/O server worker #1-2] TRACE 23:01:12.406 Media renderer was not recognized. HTTP User agent :RealtekVOD neon/0.27.2
    	// FIXME: Actual conflict here! Popcorn Hour is returned...
    	//testCases.put("User-Agent: POSIX UPnP/1.0 Intel MicroStack/1.0.2718, RealtekMediaCenter, DLNADOC/1.50", "Realtek");
    	testCases.put("User-Agent: RealtekVOD neon/0.27.2", "Realtek");

    	// From iPad-iPhone.conf:
    	// # User-Agent: 8player lite 2.2.3 (iPad; iPhone OS 5.0.1; nl_NL)
    	// # User agent: yxplayer2%20lite/1.2.7 CFNetwork/485.13.9 Darwin/11.0.0
    	// # User-Agent: MPlayer 1.0rc4-4.2.1
    	// # User-Agent: NSPlayer/4.1.0.3856
    	testCases.put("User-Agent: 8player lite 2.2.3 (iPad; iPhone OS 5.0.1; nl_NL)", "iPad / iPhone");
    	testCases.put("User-Agent: yxplayer2%20lite/1.2.7 CFNetwork/485.13.9 Darwin/11.0.0", "iPad / iPhone");
    	testCases.put("User-Agent: MPlayer 1.0rc4-4.2.1", "iPad / iPhone");
    	testCases.put("User-Agent: NSPlayer/4.1.0.3856", "iPad / iPhone");

    	// From XBMC.conf:
    	testCases.put("User-Agent: XBMC/10.0 r35648 (Mac OS X; 11.2.0 x86_64; http://www.xbmc.org)", "XBMC");
    	testCases.put("User-Agent: Platinum/0.5.3.0, DLNADOC/1.50", "XBMC");

    	// Initialize the RendererConfiguration
    	RendererConfiguration.loadRendererConfigurations();
    }

    /**
     * Test the RendererConfiguration class for recognizing headers.
     *
     * From PS3.conf:
     */
    @Test
    public void testKnownHeaders() {
    	// Test all headers
    	Set<Entry<String, String>> set = testCases.entrySet();
    	Iterator<Entry<String, String>> i = set.iterator();
    	
    	while (i.hasNext()) {
    		Entry<String, String> entry = (Entry<String, String>) i.next();
   	    	testHeader(entry.getKey(), entry.getValue());
    	}
    }
    
    /**
     * Test one particular header line to see if it returns the correct
     * renderer.
     *
     * @param headerLine The header line to recognize.
     * @param correctRendererName The name of the renderer.
     */
    private void testHeader(String headerLine, String correctRendererName) {
    	if (headerLine != null && headerLine.toLowerCase().startsWith("user-agent")) {
			RendererConfiguration rc = RendererConfiguration.getRendererConfigurationByUA(headerLine);
			assertNotNull("No renderer recognized for header \"" + headerLine + "\"", rc);
			assertEquals("Expected renderer \"" + correctRendererName + "\", "
					+ "instead renderer \"" + rc.getRendererName() + "\" was returned for header \""
					+ headerLine + "\"", correctRendererName, rc.getRendererName());
    	} else {
			RendererConfiguration rc = RendererConfiguration.getRendererConfigurationByUAAHH(headerLine);
			assertNotNull("No renderer recognized for header \"" + headerLine + "\"", rc);
			assertEquals("Expected renderer \"" + correctRendererName + "\" to be recognized, "
					+ "instead renderer \"" + rc.getRendererName() + "\" was returned for header \""
					+ headerLine + "\"", correctRendererName, rc.getRendererName());
    	}
    }
}
