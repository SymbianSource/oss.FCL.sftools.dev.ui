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
package com.nokia.tools.theme.s60.internal.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
	static final int BUFFER = 2048;
	// Directory name for storing zipfile
	private String outputDirName = null;
	// File name of zipfile
	private String zipFileName = null;
	// outputDirName + zipFileName
	private String zipPath = null;
	// Directory name from all files to be zipped
	private String inputDirName = null;
	// Array of file names to be zipped
	private String fileNames[] = null;
	// source directory of array of files to zip
	private String sourceDirName = null;

	public void setOutputDirName(String outputDirName) {
		this.outputDirName = outputDirName;
	}

	public void setInputDirName(String inputDirName) {
		this.inputDirName = inputDirName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

	public void setZipPath(String zipPath) {
		this.zipPath = zipPath;
	}

	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}

	public void setFileNames(String[] fileNames, String sourceDirName) {
		this.sourceDirName = sourceDirName;
		this.fileNames = fileNames;
	}

	public void zipFiles() throws Exception {
		if (zipPath == null)
			zipPath = outputDirName + File.separator + zipFileName;

		ZipOutputStream out = null;
		try {
			FileOutputStream dest = new FileOutputStream(zipPath);
			out = new ZipOutputStream(new BufferedOutputStream(dest));
			// out.setMethod(ZipOutputStream.DEFLATED);
			byte data[] = new byte[BUFFER];
			// get a list of files from current directory
			if (fileNames == null) {
				File f = new File(inputDirName);
				fileNames = f.list();
			}

			for (int i = 0; i < fileNames.length; i++) {
				FileInputStream fi = new FileInputStream(new File(
						sourceDirName, fileNames[i]));
				BufferedInputStream origin = null;
				try {
					origin = new BufferedInputStream(fi, BUFFER);
					ZipEntry entry = new ZipEntry(fileNames[i]);
					out.putNextEntry(entry);
					int count;
					while ((count = origin.read(data, 0, BUFFER)) != -1) {
						out.write(data, 0, count);
					}
				} finally {
					if (origin != null) {
						origin.close();
					}
				}
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}
