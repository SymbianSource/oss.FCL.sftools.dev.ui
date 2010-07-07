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
package com.nokia.tools.theme.s60.packaging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.optimization.ThemePackageAccessor;
import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.s60.S60ThemePlugin;

public class S60ThemePackageAccessor extends ThemePackageAccessor.Stub {

	public static final String DESC_FILE = "themepackage.txt";
	public static final String ENCODING = "UTF-16LE";

	Map<String, String> themePackageDescriptors = new HashMap<String, String>();

	public void finish() {
		for (String name : themePackageDescriptors.keySet()) {
			String xmlTheme = workDir + File.separator + name;
			PrintWriter writer = null;
			try {
				String content = themePackageDescriptors.get(name);
				writer = new PrintWriter(xmlTheme, ENCODING);
				// no need to write the byte order marker because it's already
				// in the string
				writer.write(content);
			} catch (FileNotFoundException e) {
				S60ThemePlugin.error(e);
			} catch (IOException e) {
				S60ThemePlugin.error(e);
			} finally {
				FileUtils.close(writer);
			}
		}
	}

	public List<File> getPackageContentFileList() {
		File workFile = new File(workDir);
		List<File> files = new ArrayList<File>();
		for (File f : workFile.listFiles()) {
			if (f.isFile()) {
				if (!f.getName().toLowerCase().endsWith(".txt")
						&& !f.getName().toLowerCase().endsWith(".dat"))
					files.add(f);
			}
		}
		return files;
	}

	public void notifyObsoleteFiles(List<File> obsoleteFiles) {
		for (File s : obsoleteFiles)
			try {
				if (optimizeAllowed(s))
					s.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	protected boolean optimizeAllowed(File s) {
		if (s.getName().indexOf("_mask_soft") != -1)
			return false;
		if (s.getName().indexOf("_mask") != -1)
			return false;

		return true;
	}

	public void notifyReplaceFiles(Map<File, File> map) {
		for (String name : themePackageDescriptors.keySet()) {
			String content = themePackageDescriptors.get(name);
			for (File s : map.keySet())
				try {
					if (!optimizeAllowed(s))
						continue;

					File r = map.get(s);

					boolean replaced = false;

					String test1 = " " + s.getName();
					String test1r = " " + r.getName();
					if (content.indexOf(test1) > 0) {
						content = content.replace(test1, test1r);
						replaced = true;
					} else { 
						test1 = test1.toLowerCase();
						if (content.indexOf(test1) > 0) {
							content = content.replace(test1, test1r);
							replaced = true;
						}
					}

					if (!replaced) {
						test1 = "=" + s.getName();
						test1r = "=" + r.getName();
						if (content.indexOf(test1) > 0) {
							content = content.replace(test1, test1r);
						} else if (test1.startsWith("=S60")
								|| test1.startsWith("=LAY_AH")) {
							test1 = test1.toLowerCase();
							if (content.indexOf(test1) > 0) {
								content = content.replace(test1, test1r);
							}
						}
					}
				} catch (Exception e) {
					S60ThemePlugin.error(e);
				}
			themePackageDescriptors.put(name, content);
		}
	}

	public void start() {

		List<String> themeDescNames = new ArrayList<String>();
		themeDescNames.add(DESC_FILE);

		if (themeDescNames.size() == 0)
			return;

		for (String name : themeDescNames) {
			String xmlTheme = workDir + File.separator + name;
			try {
				byte data[] = FileUtils.readBytes(new File(xmlTheme));
				themePackageDescriptors.put(name, new String(data, ENCODING));
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}

	}

}
