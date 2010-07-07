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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.theme.s60.internal.utilities.Zip;


public class PipProcessor extends AbstractS60PackagingProcessor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.packaging.IPackagingProcessor.AbstractPackagingProcessor#processSpi()
	 */
	@Override
	protected Object processSpi() throws PackagingException {
		if (!isDRM()) {
			return getInput();
		}

		String workingDir = getWorkingDir();
		String packageName = checkThemePackageName();
		String zipName = packageName + ".pip";

		Zip zip = new Zip();
		zip.setOutputDirName(workingDir);
		zip.setZipFileName(zipName);

		try {
			String[] fileNames = readFileNames(workingDir, packageName);
			if (fileNames != null) {
				zip.setFileNames(fileNames, workingDir);
				zip.zipFiles();

				return zipName;
			}
			return null;
		} catch (Exception e) {
			throw new PackagingException(e);
		}
	}

	public String[] readFileNames(String sourceDir, String skinName)
			throws IOException {
		File fileList = new File(sourceDir);
		SkinFilter skinFilter = new SkinFilter(skinName);
		String strFiles[] = fileList.list(skinFilter);
		int fileCount = strFiles.length;
		int soundFileCount = 0;

		File fileName = new File(sourceDir, skinName + ".txt");

		BufferedReader br = null;
		try {
			FileInputStream is = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(is, "UTF-16LE");
			br = new BufferedReader(isr);
			String strLine = br.readLine();

			String strFileName = "";
			Vector<String> vctFileList = new Vector<String>();
			while (strLine != null) {
				if (strLine.startsWith("SOUND")) {
					int firstIndex = strLine.indexOf('"') + 1;
					if (firstIndex > 0) {
						int secondIndex = strLine.indexOf('"', firstIndex);
						if (secondIndex > 1)
							strFileName = strLine.substring(firstIndex,
									secondIndex); // name
						// between
						// two
						// quotes
						if (strFileName.indexOf('.') > 0) // if name contains
															// . it
							// has extension for
							// file
							vctFileList.add(strFileName);
					}
				}
				strLine = br.readLine();
			}
			soundFileCount = vctFileList.size();

			if (soundFileCount == 0 && fileCount == 0) {
				return null;
			}
			if (soundFileCount == 0) {
				return strFiles;
			}
			if (fileCount == 0) {
				strFiles = new String[soundFileCount];
				vctFileList.copyInto(strFiles);
				return strFiles;
			}
			String strAllFile[] = new String[fileCount + soundFileCount];
			for (int index = 0; index < fileCount; index++) {
				strAllFile[index] = strFiles[index];
			}
			for (int index = 0; index < soundFileCount; index++) {
				strAllFile[index + fileCount] = (String) vctFileList
						.elementAt(index);
			}
			return strAllFile;
		} finally {
			if (br != null) {
				br.close();
			}
		}

	}

	class SkinFilter implements FilenameFilter {
		String skinName = "";

		public SkinFilter(String skinName) {
			this.skinName = skinName;
		}

		public boolean accept(File dir, String name) {
			String strDRMFiles[] = { skinName + ".sis", skinName + ".skn",
					"datafiles.def" };
			int fileCount = strDRMFiles.length;
			for (int index = 0; index < fileCount; index++) {
				if (name.equalsIgnoreCase(strDRMFiles[index]))
					return true;
			}
			return false;
		}
	}
}
