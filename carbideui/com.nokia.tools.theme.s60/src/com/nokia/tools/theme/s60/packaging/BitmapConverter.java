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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.PackagingMessages;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.s60.S60ThemeContent;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.nokia.tools.theme.s60.packaging.util.SymbianUtil;

/**
 * This processor converts the bitmaps to be used in the target phone. If the
 * attribute {@link PackagingAttribute#themeNormalSelection} equals to "true",
 * then this processor will do nothing.<br/> Mandatory attributes:
 * <ul>
 * <li>{@link PackagingAttribute#input} - name of the content.
 * </ul>
 * Output: name of the content.
 */
public class BitmapConverter extends AbstractS60PackagingProcessor {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.packaging.IPackagingProcessor.AbstractPackagingProcessor#processSpi()
	 */
	@Override
	protected Object processSpi() throws PackagingException {
		if (!isNormalSelection()) {
			return getInput();
		}
		if (!Platform.OS_WIN32.equals(Platform.getOS())
				&& !Platform.OS_MACOSX.equals(Platform.getOS())) {
			throw new PackagingException(PackagingMessages.Error_osNotSupported);
		}

		String input = getInput();
		if (input == null) {
			throw new PackagingException(
					PackagingMessages.Error_bitmapNameMissing);
		}

		File workingDir = new File(getWorkingDir());
		File[] bitmaps = workingDir.listFiles(new FilenameFilter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.io.FilenameFilter#accept(java.io.File,
			 *      java.lang.String)
			 */
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".bmp");
			}
		});

		// there has to be at least one bitmap file available, otherwise bmcov
		// will fail. This can happen when there is only sound file edited in
		// the theme.
		if (bitmaps == null || bitmaps.length == 0) {
			// creates a dummy .mbm file to satisfy makesis
			createEmptyFile(input + ".mbm");
			return input;
		}

		String origFile = getWorkingDir() + File.separator + input
				+ "_MBMList.txt";
		String newFile = getWorkingDir() + File.separator + input
				+ "_MBMList_mod.txt";
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			in = new BufferedReader(new FileReader(origFile));
			out = new PrintWriter(new FileWriter(newFile));
			out.println("/p\"" + SymbianUtil.getRuntimeDir() + File.separator
					+ "thirdpartybitmap.pal\"");
			String line = null;
			while ((line = in.readLine()) != null) {
				out.println(line);
			}
			out.flush();
		} catch (IOException e) {
			throw new PackagingException(e);
		} finally {
			FileUtils.close(in);
			FileUtils.close(out);
		}

		List<String> list = new ArrayList<String>(1);
		list.add(newFile);

		String exe=null;
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
            S60Theme theme = (S60Theme) ((S60ThemeContent) getTheme()).getData();
            String modelID = this.context.getAttribute(PackagingAttribute.primaryModelId.name()).toString();
            String secondaryModelID = this.context.getAttribute(PackagingAttribute.secondaryModelId.name()).toString();
            URL packagingExecutablePath = PackagingExecutableProvider.getPackagingExecutablePath(modelID, PackagingExecutableType.BITMAP_CONVERTER, false);
            if(packagingExecutablePath == null && secondaryModelID != null){
            	packagingExecutablePath = PackagingExecutableProvider.getPackagingExecutablePath(secondaryModelID, PackagingExecutableType.BITMAP_CONVERTER, true);
            }
            if(packagingExecutablePath != null){
            	exe = PackagingExecutableType.BITMAP_CONVERTER.name() + ".exe";
            	copyExecutableFile(packagingExecutablePath, exe);
            }
            else{
            	exe = "BMCONV.EXE";
            }

		} else {
			exe = "bmconv";
		}

		exec(exe, list);
		return input;
	}

}
