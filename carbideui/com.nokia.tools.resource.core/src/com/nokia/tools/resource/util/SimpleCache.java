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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class provides a simple global place for caching various contents under
 * certain groups. The cached data is indexed by the provided key, which can be
 * used later to lookup for the cached data. The cache shall be cleared when
 * they are not needed anymore.
 *
 */
public class SimpleCache {
	private static Map<Object, Map<Object, Object>> cache = new HashMap<Object, Map<Object, Object>>();

	/**
	 * No instantion.
	 */
	private SimpleCache() {
	}

	/**
	 * Caches the data.
	 * 
	 * @param group the cache group.
	 * @param key key of the cached data.
	 * @param value the actual data.
	 * @param weakReference true if object should be stored as weak ref
	 */
	public static void cache(Object group, Object key, Object value,
			boolean weakReference) {
		synchronized (cache) {
			if (DebugHelper.debugCaching()) {
				if (!cache.containsKey(group)) {
					DebugHelper.debug(SimpleCache.class, "Adding new group 0x"
							+ Integer.toHexString(group.hashCode()) + ": "
							+ group);
				}
			}
			Map<Object, Object> objects = cache.get(group);
			if (objects == null) {
				objects = new HashMap<Object, Object>();
				cache.put(group, objects);
			}

			if (weakReference) {
				value = new WeakReference(value);
			}

			objects.put(key, value);
		}
	}

	/**
	 * Caches the data.
	 * 
	 * @param group the cache group.
	 * @param key key of the cached data.
	 * @param value the actual data.
	 */
	public static void cache(Object group, Object key, Object value) {
		cache(group, key, value, false);
	}

	/**
	 * Clears a particular key.
	 * 
	 * @param group the cache group.
	 * @param key key of the data to be cleared.
	 */
	public static void clear(Object group, Object key) {
		synchronized (cache) {
			Map<Object, Object> objects = cache.get(group);
			if (objects != null) {
				objects.remove(key);
			}
		}
	}

	/**
	 * Clears the all cached data of the given group.
	 * 
	 * @param group the cache group to be cleared.
	 */
	public static void clear(Object group) {
		synchronized (cache) {
			cache.remove(group);
			if (DebugHelper.debugCaching()) {
				if (!cache.containsKey(group)) {
					DebugHelper.debug(SimpleCache.class, "Removed group 0x"
							+ Integer.toHexString(group.hashCode()) + ": "
							+ group);
					for (Object key : cache.keySet()) {
						DebugHelper.debug(SimpleCache.class,
								"\t\tRemaining key 0x"
										+ Integer.toHexString(key.hashCode())
										+ ": " + key);
					}
				}
			}
		}
	}

	/**
	 * Clears entire cache.
	 */
	public static void clearAll() {
		synchronized (cache) {
			cache.clear();
		}
	}

	/**
	 * Returns the cached data.
	 * 
	 * @param group the cache group.
	 * @param key key of the cached data to be retrieved.
	 * @return the cached data or null if the data is not in the cache.
	 */
	public static Object getData(Object group, Object key) {
		synchronized (cache) {
			Map<Object, Object> objects = cache.get(group);
			if (objects == null) {
				return null;
			}
			Object value = objects.get(key);
			return value instanceof WeakReference ? ((WeakReference) value)
					.get() : value;
		}
	}

	/**
	 * Returns true if the specific group exists in the cache.
	 * 
	 * @param group the group object.
	 * @return true if the group exists, false otherwise.
	 */
	public static boolean hasGroup(Object group) {
		synchronized (cache) {
			Map<Object, Object> objects = cache.get(group);
			if (objects == null) {
				return false;
			}
			return !objects.isEmpty();
		}
	}

	/**
	 * @return all group objects.
	 */
	public static List<Object> getGroups() {
		synchronized (cache) {
			return new ArrayList<Object>(cache.keySet());
		}
	}

	/**
	 * Returns the cached data of the specific group.
	 * 
	 * @param group the group object.
	 * @return the cached data of the specific group.
	 */
	public static Map<Object, Object> getGroupData(Object group) {
		synchronized (cache) {
			return cache.get(group);
		}
	}

	/**
	 * Returns the cached data. This will go through all cached data to find the
	 * corresponding one.
	 * 
	 * @param key the key to the cached data.
	 * @return the cached data or null if the data is not in the cache.
	 */
	public static Object getData(Object key) {
		synchronized (cache) {
			for (Map<Object, Object> objects : cache.values()) {
				Object value = objects.get(key);
				if (value != null) {
					return value instanceof WeakReference ? ((WeakReference) value)
							.get()
							: value;
				}
			}
			return null;
		}
	}

	/**
	 * @return the lock object.
	 */
	public static Object getMutex() {
		return cache;
	}
}
