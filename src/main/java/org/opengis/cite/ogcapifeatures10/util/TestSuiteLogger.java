package org.opengis.cite.ogcapifeatures10.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logging utility class that provides simple access to the JDK Logging API. Set the
 * "java.util.logging.config.file" system property to specify the location of the desired
 * logging configuration file. A sample configuration file is available at
 * {@code src/main/config/logging.properties}.
 *
 * @see java.util.logging.LogManager LogManager
 */
public class TestSuiteLogger {

	private static final Logger LOGR = Logger.getLogger(TestSuiteLogger.class.getPackage().getName());

	/**
	 * Logs a message at the specified logging level with the given message parameters.
	 * @param level The logging {@link Level level}.
	 * @param message A String representing the content of the log message.
	 * @param params An array of message parameters.
	 */
	public static void log(Level level, String message, Object[] params) {
		if (LOGR.isLoggable(level)) {
			LOGR.log(level, message, params);
		}
	}

	/**
	 * Logs a message at the specified logging level with the given Exception object that
	 * represents a noteworthy error condition.
	 * @param level The logging {@link Level level}.
	 * @param message A String representing the content of the log message.
	 * @param except An object that indicates an exceptional situation.
	 */
	public static void log(Level level, String message, Exception except) {
		if (LOGR.isLoggable(level)) {
			LOGR.log(level, message, except);
		}
	}

	/**
	 * Logs a simple message at the specified logging level.
	 * @param level The logging {@link Level level}.
	 * @param message A String representing the content of the log message.
	 */
	public static void log(Level level, String message) {
		if (LOGR.isLoggable(level)) {
			LOGR.log(level, message);
		}
	}

	/**
	 * Indicates if the logger is enabled at a given logging level. Message levels lower
	 * than this value will be discarded.
	 * @param level The logging {@link Level level}.
	 * @return true if the logger is currently enabled for this logging level; false
	 * otherwise.
	 */
	public static boolean isLoggable(Level level) {
		return LOGR.isLoggable(level);
	}

	private TestSuiteLogger() {
	}

}
