/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008-2013 A. Brochard.
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

package net.pms.util;

import org.junit.Before;
import org.junit.Test;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

import static org.fest.assertions.Assertions.assertThat;

public class StringUtilTest {
	/**
	 * Set up testing conditions before running the tests.
	 */
	@Before
	public final void setUp() {
		// Silence all log messages from the PMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.reset();
	}

	@Test
	public void testAbbreviate() {
		assertThat(StringUtil.abbreviate(null)).isNull();
		assertThat(StringUtil.abbreviate("ABC").equals("ABC")).isTrue();
		assertThat(StringUtil.abbreviate("A B C").equals("ABC")).isTrue();
		assertThat(StringUtil.abbreviate("tsMuxeR").equals("TMR")).isTrue();
		assertThat(StringUtil.abbreviate("foo bar baz").equals("FBB")).isTrue();
		assertThat(StringUtil.abbreviate("Foo Bar Baz").equals("FBB")).isTrue();
		assertThat(StringUtil.abbreviate(" foo  Bar  Baz ").equals("FBB")).isTrue();
		assertThat(StringUtil.abbreviate("Foo-Bar-Baz").equals("FBB")).isTrue();
	}
}
