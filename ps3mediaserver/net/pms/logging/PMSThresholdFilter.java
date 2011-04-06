/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2010  A.Brochard
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
package net.pms.logging;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Special Logback Filter that filters according to the level set in the
 * PmsConfiguration.
 * 
 * The threshold level is taken from {@link PmsConfiguration#getLoggingLevel()},
 * and is an integer between 0 and 3. These levels are used historically be PMS
 * and have the following mappings:
 * 
 * <table border="1">
 * <tr><th>value</th><th>PMS</th><th>SLF4J</th></tr>
 * <tr><td>0</td><td>DEBUG</td><td>TRACE</td></tr>
 * <tr><td>1</td><td>INFO</td><td>DEBUG</td></tr>
 * <tr><td>2</td><td>TRACE</td><td>INFO</td></tr>
 * <tr><td>3</td><td>--</td><td>OFF</td></tr>
 * </table>
 * All logging events below the threshold are denied, all others are accepted.
 * 
 * @author thomas@innot.de
 * 
 */
public class PMSThresholdFilter extends Filter<ILoggingEvent> {

	// The old PMS logging levels.
	// Only used to convert from the old levels that might be used in some
	// pms.conf files to the SLF4J levels.
	private static final int PMS_DEBUG = 0;
	private static final int PMS_INFO = 1;
	private static final int PMS_MINIMAL = 2;

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (!isStarted()) {
			return FilterReply.NEUTRAL;
		}

		// Don't cache the configuration and/or logging level as
		// they might be changed anytime via the GUI (in future
		// versions of the GUI)
		int pmslevel = PMS.getConfiguration().getLoggingLevel();

		// Convert old PMSConfiguration Level to SLF4J level
		Level level;
		switch (pmslevel) {
		case PMS_DEBUG:
			level = Level.TRACE;
			break;
		case PMS_INFO:
			level = Level.DEBUG;
			break;
		case PMS_MINIMAL:
			level = Level.INFO;
			break;
		default:
			level = Level.OFF;
		}
		if (event.getLevel().isGreaterOrEqual(level)) {
			return FilterReply.ACCEPT;
		} else {
			return FilterReply.DENY;
		}
	}

}
