/*
* Copyright (c) 2006-2010 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
*/

package com.nokia.tools.resource.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;

import com.nokia.tools.resource.core.Activator;

/**
 * Debug related utilities that are aware of the active debug options
 * (.options).
 * 
 */
public class DebugHelper {
	private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");

	/**
	 * @return true if <code>deepclean</code> is enabled.
	 */
	public static boolean debugDeepclean() {
		return debugEnabled("/debug/deepclean");
	}

	/**
	 * @return true if <code>performance</code> is enabled.
	 */
	public static boolean debugPerformance() {
		return debugEnabled("/debug/performance");
	}

	/**
	 * @return true if <code>parser</code> is enabled.
	 */
	public static boolean debugParser() {
		return debugEnabled("/debug/parser");
	}

	/**
	 * @return true if <code>animation</code> is enabled.
	 */
	public static boolean debugAnimation() {
		return debugEnabled("/debug/animation");
	}

	/**
	 * @return true if <code>caching</code> is enabled.
	 */
	public static boolean debugCaching() {
		return debugEnabled("/debug/caching");
	}

	/**
	 * @return true if <code>cleanresource</code> is enabled.
	 */
	public static boolean debugCleanResource() {
		return debugEnabled("/debug/cleanresource");
	}

	/**
	 * @return true if <code>ui</code> is enabled.
	 */
	public static boolean debugUi() {
		return debugEnabled("/debug/ui");
	}

	/**
	 * @return true if <code>batch</code> is enabled.
	 */
	public static boolean debugBatch() {
		return debugEnabled("/debug/batch");
	}

	/**
	 * @return true if <code>communication</code> is enabled.
	 */
	public static boolean debugCommunication() {
		return debugEnabled("/debug/communication");
	}

	/**
	 * @return true if <code>liveWindow</code> is enabled.
	 */
	public static boolean debugLiveWindow() {
		return debugEnabled("/debug/liveWindow");
	}

	/**
	 * @return true if <code>jni</code> is enabled.
	 */
	public static boolean debugJni() {
		return debugEnabled("/debug/jni");
	}

	/**
	 * Checks if the given debug option is enabled or not.
	 * 
	 * @param option the option name.
	 * @return true if the option is enabled, false otherwise.
	 */
	public static boolean debugEnabled(String option) {
		return Boolean.valueOf(Platform.getDebugOption(Activator.PLUGIN_ID
		    + option));
	}

	/**
	 * Prints out the debug message to the console.
	 * 
	 * @param message the message to be printed.
	 */
	public static void debug(String message) {
		debug(null, message);
	}

	/**
	 * Prints out the debug message to the console, the message is identified by
	 * the object's class.
	 * 
	 * @param object the object from where the class information is obtained,
	 *            can be null.
	 * @param message the debug message to be printed.
	 */
	public static void debug(Object object, String message) {
		debug(object == null ? null : object.getClass(), message);
	}

	/**
	 * Prints out the debug message to the console, the message is identified by
	 * the given class.
	 * 
	 * @param clazz the class that the message is associated with.
	 * @param message the debug message to be printed.
	 */
	public static void debug(Class clazz, String message) {
		System.out.println("[" + FORMAT.format(new Date()) + "]"
		    + (clazz == null ? "" : "<" + clazz.getSimpleName() + ">") + ": "
		    + message);
	}

	/**
	 * Measures the time spent (in milliseconds) for running the given job.
	 * 
	 * @param message the debug message.
	 * @param job the job to be measured.
	 */
	public static void debugTime(String message, ISafeRunnable job) {
		debugTime(null, message, job);
	}

	/**
	 * Measures the time spent (in milliseconds) for running the given job.
	 * 
	 * @param object the object from where the class information is obtained,
	 *            can be null.
	 * @param message the debug message.
	 * @param job the job to be measured.
	 */
	public static void debugTime(Object object, String message,
	    ISafeRunnable job) {
		debugTime(object == null ? null : object.getClass(), message, job);
	}

	/**
	 * Measures the time spent (in milliseconds) for running the given job.
	 * 
	 * @param clazz the class that this message is associated with.
	 * @param message the debug message.
	 * @param job the job to be measured.
	 */
	public static void debugTime(Class clazz, String message, ISafeRunnable job) {
		long start = System.currentTimeMillis();
		SafeRunner.run(job);
		debug(clazz, "time spent for " + message + " is "
		    + (System.currentTimeMillis() - start) + " millis.");
	}

	/**
	 * This runnable catches exceptions to the system log.
	 * 
	 */
	public static abstract class SilentRunnable
	    implements ISafeRunnable {
		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.
		 * Throwable)
		 */
		public void handleException(Throwable exception) {
			Activator.error(exception);
		}
	}
}
