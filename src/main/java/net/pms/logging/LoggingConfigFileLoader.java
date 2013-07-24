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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.util.PropertiesUtil;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.replace;

/**
 * Simple loader for logback configuration files.
 * 
 * @author thomas@innot.de
 */
public class LoggingConfigFileLoader {
	private static String filepath = null;
	private static HashMap<String, String> logFilePaths = new HashMap<String, String>(); // key: appender name, value: log file path
	private static final PmsConfiguration configuration = PMS.getConfiguration();

	/**
	 * Gets the full path of a successfully loaded Logback configuration file.
	 * 
	 * If the configuration file could not be loaded the string
	 * <code>internal defaults</code> is returned.
	 * 
	 * @return pathname or <code>null</code>
	 */
	public static String getConfigFilePath() {
		if (filepath != null) {
			return filepath;
		} else {
			return "internal defaults";
		}
	}

	/**
	 * Loads the (optional) Logback configuration file.
	 *
	 * <p>
	 * It loads the file defined in the {@code project.logback} property
	 * (use {@code [PROFILE_DIR]} to specify profile folder) and (re-)initializes Logback with this file.
	 * </p>
	 *
	 * <p>
	 * If failed (file not found or unreadable) it tries to load {@code logback.xml} from the current directory.
	 * </p>
	 *
	 * <p>
	 * If running headless, then the alternative config file defined in {@code project.logback.headless} is tried.
	 * </p>
	 *
	 * <p>
	 * If no config file worked, then nothing is loaded and
	 * Logback will use the {@code logback.xml} file on the classpath as a default. If
	 * this doesn't exist then a basic console appender is used as fallback.
	 * </p>
	 *
	 * <strong>Note:</strong> Any error messages generated while parsing the
	 * config file are dumped only to {@code stdout}.
	 */
	public static void load() {
		// Note: Do not use any logging method in this method!
		// Any status output needs to go to the console.

		File logFile = null;

		if (PMS.isHeadless()) {
			final String logFilePath = replace(PropertiesUtil.getProjectProperties().get("project.logback.headless"), "[PROFILE_DIR]", configuration.getProfileDirectory());
			if (isNotBlank(logFilePath)) {
				logFile = new File(logFilePath);
			}
		} else {
			final String logFilePath = replace(PropertiesUtil.getProjectProperties().get("project.logback"), "[PROFILE_DIR]", configuration.getProfileDirectory());
			if (isNotBlank(logFilePath)) {
				logFile = new File(logFilePath);
			}
		}

		if (logFile == null || !logFile.canRead()) {
			// Now try configs from the app folder.
			if (PMS.isHeadless()) {
				logFile = new File("logback.headless.xml");
			} else {
				logFile = new File("logback.xml");
			}
		}

		if (!logFile.canRead()) {
			// No problem, the internal logback.xml is used.
			return;
		}

		// Now get logback to actually use the config file

		ILoggerFactory ilf = LoggerFactory.getILoggerFactory();
		if (!(ilf instanceof LoggerContext)) {
			// Not using LogBack.
			// Can't configure the logger, so just exit
			return;
		}

		LoggerContext lc = (LoggerContext) ilf;

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(lc);
			// the context was probably already configured by
			// default configuration rules
			lc.reset();
			configurator.doConfigure(logFile);

			// Save the filepath after loading the file
			filepath = logFile.getAbsolutePath();
		} catch (JoranException je) {
			// StatusPrinter will handle this
			je.printStackTrace();
		}

		for (Logger logger : lc.getLoggerList()) {
			Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders();

			while (it.hasNext()) {
				Appender<ILoggingEvent> ap = it.next();

				if (ap instanceof FileAppender) {
					FileAppender<ILoggingEvent> fa = (FileAppender<ILoggingEvent>) ap;
					logFilePaths.put(fa.getName(), fa.getFile());
				}
			}
		}

		StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
	}

	public static HashMap<String, String> getLogFilePaths() {
		return logFilePaths;
	}
}
