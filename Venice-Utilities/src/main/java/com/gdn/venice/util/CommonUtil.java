package com.gdn.venice.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceEnvironment;
import com.gdn.venice.exception.VeniceInternalException;

/**
 * 
 * @author yauritux
 * 
 */
public class CommonUtil {
	
	public static VeniceEnvironment veniceEnv = VeniceEnvironment.DEVELOPMENT; //default to development
	
	private static Map<String, Logger > loggers = new HashMap<String, Logger>();
	
	public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> c) {
		List<T> r = new ArrayList<T>(c.size());
		for (Object o : c) {
			r.add(clazz.cast(o));
		}
		return r;
	}
	
	public static Logger getLogger(String key) {
		Logger logger = null;
		if ((!loggers.containsKey(key)) || (loggers.get(key) == null)) {
			Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
			logger = loggerFactory.getLog4JLogger(key);
			loggers.put(key, logger);
		} else {
			logger = loggers.get(key);
		}
		return logger;
		
	}

	public static void logException(VeniceInternalException exception
			, Logger logger, LoggerLevel level) {
		
		switch (veniceEnv) {
		case TESTING:
			return; //do nothing in testing environment
		default: //continue to the next line for both "DEVELOPMENT" and "PRODUCTION"
		}
		
		switch (level) {
		case TRACE:
			logger.trace(exception.getMessage(), exception);
			break;
		case DEBUG:
			logger.debug(exception.getMessage(), exception);
			break;
		case INFO:
			logger.info(exception.getMessage(), exception);
			break;
		case WARN:
			logger.warn(exception.getMessage(), exception);
			break;
		default:
			logger.error(exception.getMessage(), exception);
		}
	}
	
	public static VeniceInternalException logAndReturnException(VeniceInternalException exception
			, Logger logger, LoggerLevel level) {
		logException(exception, logger, level);
		return exception;
	}
	
	public static void logTrace(Logger logger, String message) {
		if (logger.isTraceEnabled()) logger.trace(message);
	}
	
	//public static void logDebug(Logger logger, String message) {
	public static void logDebug(String key, String message) {
	    switch (veniceEnv) {
	    case TESTING: 
	    	return; //do nothing in testing environment
	    default: //continue to the next line for both "DEVELOPMENT" and "PRODUCTION"
	    }
		if (getLogger(key).isDebugEnabled()) getLogger(key).debug(message);
	}
	
	public static void logError(String key, String message) {
	    switch (veniceEnv) {
	    case TESTING: 
	    	return; //do nothing in testing environment
	    default: //continue to the next line for both "DEVELOPMENT" and "PRODUCTION"
	    }
		getLogger(key).error(message);
	}
	
	public static void logError(String key, Object message) {
	    switch (veniceEnv) {
	    case TESTING: 
	    	return; //do nothing in testing environment
	    default: //continue to the next line for both "DEVELOPMENT" and "PRODUCTION"
	    }
		getLogger(key).error(message);
	}
	
	public static void logInfo(Logger logger, String message) {
		if (logger.isInfoEnabled()) logger.info(message);
	}
}
