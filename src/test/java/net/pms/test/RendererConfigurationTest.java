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
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;


/**
 * Test the RendererConfiguration class
 */
public class RendererConfigurationTest {
	private final Map<String, String> testCases = new HashMap<String, String>();

    @Before
    public void setUp() {
        // Silence all log messages from the PMS code that is being tested
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset(); 

    	// Cases that are too generic should not match anything
    	testCases.put("User-Agent: UPnP/1.0 DLNADOC/1.50", null);
    	testCases.put("User-Agent: Unknown Renderer", null);

    	// From PS3.conf:
    	testCases.put("User-Agent: PLAYSTATION 3", "Playstation 3");
    	testCases.put("X-AV-Client-Info: av=5.0; cn=\"Sony Computer Entertainment Inc.\"; mn=\"PLAYSTATION 3\"; mv=\"1.0\"", "Playstation 3");

    	// From AirPlayer.conf:
    	testCases.put("User-Agent: AirPlayer/1.0.09 CFNetwork/485.13.9 Darwin/11.0.0", "AirPlayer");
    	testCases.put("User-Agent: Lavf52.54.0", "AirPlayer");

    	// From Philips.conf:
    	testCases.put("User-Agent: Allegro-Software-WebClient/4.61 DLNADOC/1.00", "Philips Aurea");

    	// From Realtek.conf:
    	// FIXME: Actual conflict here! Popcorn Hour is returned...
    	//testCases.put("User-Agent: POSIX UPnP/1.0 Intel MicroStack/1.0.2718, RealtekMediaCenter, DLNADOC/1.50", "Realtek");
    	testCases.put("User-Agent: RealtekVOD neon/0.27.2", "Realtek");

    	// From iPad-iPhone.conf:
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
     * Test the RendererConfiguration class and the consistency of the
     * renderer .conf files it reads. This is done by feeding it known
     * headers and checking whether it recognizes the correct renderer.
     */
    @Test
    public void testKnownHeaders() {
    	// Test all header test cases
    	Set<Entry<String, String>> set = testCases.entrySet();
    	Iterator<Entry<String, String>> i = set.iterator();
    	
    	while (i.hasNext()) {
    		Entry<String, String> entry = (Entry<String, String>) i.next();
   	    	testHeader(entry.getKey(), entry.getValue());
    	}
    }
    
    /**
     * Test one particular header line to see if it returns the correct
     * renderer. Set the correct renderer name to <code>null</code> to
     * require that nothing matches at all.
     *
     * @param headerLine The header line to recognize.
     * @param correctRendererName The name of the renderer. 
     */
    private void testHeader(String headerLine, String correctRendererName) {
    	if (correctRendererName != null) {
    		// Header is supposed to match a particular renderer
	    	if (headerLine != null && headerLine.toLowerCase().startsWith("user-agent")) {
	    		// Match by User-Agent
				RendererConfiguration rc = RendererConfiguration.getRendererConfigurationByUA(headerLine);
				assertNotNull("No renderer recognized for header \"" + headerLine + "\"", rc);
				assertEquals("Expected renderer \"" + correctRendererName + "\", "
						+ "instead renderer \"" + rc.getRendererName() + "\" was returned for header \""
						+ headerLine + "\"", correctRendererName, rc.getRendererName());
	    	} else {
	    		// Match by additional header
				RendererConfiguration rc = RendererConfiguration.getRendererConfigurationByUAAHH(headerLine);
				assertNotNull("No renderer recognized for header \"" + headerLine + "\"", rc);
				assertEquals("Expected renderer \"" + correctRendererName + "\" to be recognized, "
						+ "instead renderer \"" + rc.getRendererName() + "\" was returned for header \""
						+ headerLine + "\"", correctRendererName, rc.getRendererName());
	    	}
    	} else {
    		// Header is supposed to match no renderer at all
	    	if (headerLine != null && headerLine.toLowerCase().startsWith("user-agent")) {
	    		// Match by User-Agent
				RendererConfiguration rc = RendererConfiguration.getRendererConfigurationByUA(headerLine);
				assertEquals("Expected no matching renderer to be found for header \"" + headerLine
						+ "\", instead renderer \"" + (rc != null ? rc.getRendererName() : "")
						+ "\" was recognized.", null,
						rc);
	    	} else {
	    		// Match by additional header
				RendererConfiguration rc = RendererConfiguration.getRendererConfigurationByUAAHH(headerLine);
				assertEquals("Expected no matching renderer to be found for header \"" + headerLine
						+ "\", instead renderer \"" + rc.getRendererName() + "\" was recognized.", null,
						rc);
	    	}
    	}
    }
}
