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
package com.nokia.tools.theme.editing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import com.nokia.tools.media.utils.svg2svgt.SVGTUtil;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.XmlUtil;

public class SVG2SVTConverter {
	private static String getAppropriateFileName(String fileName,
			File outputFolder) {
		String localFile = fileName;
		int length = fileName.length();
		boolean isConversionDone = false;
		long l = 0;
		String[] convertedFileNames = outputFolder.list();
		if (convertedFileNames == null) {
			return null;
		}
		for (int i = 0; i < convertedFileNames.length; i++) {
			if (convertedFileNames[i].contains(fileName.substring(0, fileName
					.lastIndexOf("."))) //$NON-NLS-1$
					&& convertedFileNames[i].length() >= length) {
				File f = new File(convertedFileNames[i]);
				if (f.lastModified() >= l) {
					l = f.lastModified();
					localFile = convertedFileNames[i];
				}
				isConversionDone = true;
			}
		}
		if (localFile.equals(fileName) && !isConversionDone)
			return null;
		return localFile;
	}

	@SuppressWarnings("unchecked")
	public static String convertToSVGT(String identifier, Map dimensions,
			String fileName) throws Exception {
		if (!fileName.endsWith(ThemeTag.SVG_FILE_EXTN)) {
			return fileName;
		}

		String dimension = dimensions != null
				&& dimensions.containsKey(identifier) ? SVGTUtil.DIMENSION_NORMAL
				: "100%";
		String tempDir = FileUtils.getTemporaryDirectory() + File.separator
				+ "svgt" + System.currentTimeMillis();
		String svgtFilePath = tempDir + File.separator
				+ new File(fileName).getName();
		File file = new File(svgtFilePath);
		file.getParentFile().mkdirs();
		FileUtils.addForCleanup(file.getParentFile());

		Map<String, String> options = new HashMap<String, String>(1);
		options.put(SVGTUtil.OPT_DIMENSION, dimension);
		Document doc = XmlUtil.parse(new File(fileName));
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			SVGTUtil.convertToSVGT(doc, out, options);
		} finally {
			FileUtils.close(out);
		}

		String newFileName = getAppropriateFileName(new File(fileName)
				.getName().toString(), new File(tempDir));
		if (newFileName != null) {
			return tempDir + File.separator + newFileName;
		}

		return fileName;
	}
}
