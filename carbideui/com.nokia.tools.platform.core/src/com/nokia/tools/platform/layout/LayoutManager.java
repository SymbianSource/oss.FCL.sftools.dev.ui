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
package com.nokia.tools.platform.layout;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Adler32;

import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.SimpleCache;

public class LayoutManager {
	private static final File CACHE_FILE = new File(FileUtils
			.getConfigDir(PlatformCorePlugin.getDefault()), PlatformCorePlugin
			.getDefault().getBundle().getSymbolicName()
			+ File.separator + "layout.cache");
	private static boolean isCacheDirty;

	static {
		CACHE_FILE.getParentFile().mkdirs();
	}

	private LayoutManager() {
	}

	public synchronized static void markCacheDirty() {
		isCacheDirty = true;
	}

	/**
	 * Releases all layout related data, useful in debugging the memory leak and
	 * is called when the debug/deepClean is turned on.
	 */
	public synchronized static void release() {
		SimpleCache.clear(LayoutManager.class);
		LayoutSet.release();
	}

	public synchronized static void loadCache() {
		if (CACHE_FILE.isFile() && CACHE_FILE.canRead()) {
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(new BufferedInputStream(
						new FileInputStream(CACHE_FILE), FileUtils.BUF_SIZE));
				LayoutCache cache = (LayoutCache) in.readObject();
				if (DebugHelper.debugCaching()) {
					DebugHelper.debug(LayoutManager.class, "Loaded "
							+ cache.getComponents().size()
							+ " components from layout cache.");
				}
				cache.fillSystemCache();
			} catch (Throwable e) {
				PlatformCorePlugin.error(e);
				CACHE_FILE.delete();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (Exception e) {
				}
			}
		}
	}

	public synchronized static void storeCache() {
		if (!isCacheDirty) {
			return;
		}

		LayoutCache cache = new LayoutCache();
		Map<Object, Object> data = SimpleCache
				.getGroupData(LayoutManager.class);
		if (data != null) {
			synchronized (data) {
				for (Object key : data.keySet()) {
					if (key instanceof ComponentInfo) {
						cache.addComponent((ComponentInfo) key);
					}
				}
			}
		}

		ObjectOutputStream out = null;
		try {
			CACHE_FILE.getParentFile().mkdirs();
			out = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(CACHE_FILE), FileUtils.BUF_SIZE));
			out.writeObject(cache);
			out.flush();
			if (DebugHelper.debugCaching()) {
				DebugHelper.debug(LayoutManager.class, "Stored "
						+ cache.getComponents().size()
						+ " components into layout cache.");
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
			CACHE_FILE.delete();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
			}
		}
	}

	static class LayoutCache implements Serializable {
		private static final long serialVersionUID = 1L;

		Map<String, Long> fileMap = new HashMap<String, Long>();
		List<ComponentInfo> components = new ArrayList<ComponentInfo>();

		void addComponent(ComponentInfo component) {
			if (component.getLayout() == null) {
				return;
			}
			LayoutContext context = component.getContext();
			if (context == null) {
				return;
			}
			for (URL url : getPaths(context)) {
				updateFileChecksum(fileMap, url);
			}
			components.add(component);
		}

		List<ComponentInfo> getComponents() {
			return components;
		}

		long updateFileChecksum(Map<String, Long> map, URL url) {
			String path = FileUtils.getAbsoluteBundlePath(url);
			Long checksum = map.get(path);
			if (checksum == null) {
				checksum = checksum(url);
				map.put(path, checksum);
			}
			return checksum;
		}

		void fillSystemCache() {
			Set<LayoutContext> verifiedContexts = new HashSet<LayoutContext>();
			Set<String> verifiedPaths = new HashSet<String>();
			Map<String, Long> newFileMap = new HashMap<String, Long>(fileMap
					.size());
			for (ComponentInfo component : components) {
				LayoutContext context = component.getContext();
				if (context == null || context.getLayoutSet() == null) {
					// layout data not available in the current installation
					continue;
				}

				boolean isClean = true;
				if(!verifiedContexts.contains(context)){
					for (URL url : getPaths(context)) {
						String path = FileUtils.getAbsoluteBundlePath(url);
						if(!verifiedPaths.contains(path)){
							if (fileMap.get(path) == null) {
								// new file
								if (DebugHelper.debugCaching()) {
									DebugHelper.debug(LayoutManager.class, "New file:"
											+ url + ", invalidate layout cache.");
								}
		
								markCacheDirty();
								isClean = false;
								break;
							}
		
							updateFileChecksum(newFileMap, url);
		
							if (!fileMap.get(path).equals(newFileMap.get(path))) {
								// file changed
								if (DebugHelper.debugCaching()) {
									DebugHelper.debug(LayoutManager.class,
											"File changed:" + url
													+ ", invalidate layout cache.");
								}
								markCacheDirty();
								isClean = false;
								break;
							}
							verifiedPaths.add(path);
						}
					}
					verifiedContexts.add(context);
				}
				if (isClean) {
					SimpleCache
							.cache(LayoutManager.class, component, component);
				}
			}
		}

		long checksum(URL url) {
			Adler32 adler = new Adler32();

			long checksum = 0;
			InputStream in = null;
			try {
				in = new BufferedInputStream(url.openStream(),
						FileUtils.BUF_SIZE);
				byte[] buf = new byte[FileUtils.BUF_SIZE];
				int read;
				while ((read = in.read(buf)) > 0) {
					adler.update(buf, 0, read);
				}
				checksum = adler.getValue();
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (Exception e) {
				}
			}
			return checksum;
		}

		List<URL> getPaths(LayoutContext context) {
			List<URL> urls = new ArrayList<URL>();
			URL fontPath = context.getLayoutSet().getDescriptor()
					.getFontDescriptor().getFontPath();
			if (fontPath != null) {
				urls.add(fontPath);
			}
			for (LayoutInfo info : context.getLayouts()) {
				URL[] componentPaths = info.getVariantDescriptor()
						.getComponentPaths();
				URL[] attributePaths = info.getVariantDescriptor()
						.getAttributePaths();
				if (componentPaths != null) {
					urls.addAll(Arrays.asList(componentPaths));
				}
				if (attributePaths != null) {
					urls.addAll(Arrays.asList(attributePaths));
				}
			}
			return urls;
		}
	}
}
