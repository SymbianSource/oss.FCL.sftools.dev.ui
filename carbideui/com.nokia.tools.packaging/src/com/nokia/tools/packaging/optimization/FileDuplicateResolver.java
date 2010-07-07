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
package com.nokia.tools.packaging.optimization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * file cache, that's capable of detecting binary identical files.
 * 
 * 
 *
 */
public class FileDuplicateResolver {
	
	//key = filesize, value = map of file entries, key = file, value = file name
	Map<Integer,Map<File, String>> fileTable = new HashMap<Integer, Map<File,String>>();

	/**
	 * returns resolved file name
	 * @param inputFile
	 * @return
	 * @throws IOException 
	 */
	public synchronized File resolveFile(File inputFile) throws IOException {
		if (inputFile == null)
			return null;
		if (!inputFile.exists())
			return inputFile;
		Map<File, String> sameSizes = fileTable.get(new Integer((int) inputFile.length()));
		if (sameSizes == null) {
			sameSizes = new HashMap<File, String>();
			sameSizes.put(inputFile, inputFile.getName());
			fileTable.put(new Integer((int) inputFile.length()), sameSizes);			
			return inputFile;			
		} else {
			for (File absPath: sameSizes.keySet()) {				
				if (compareByContent(inputFile, absPath)) {
					return absPath;
				}
			}
			//not found
			sameSizes.put(inputFile, inputFile.getName());
			return inputFile;
		}
	}
	


	public synchronized void clear() {
		fileTable.clear();
	}
	
	/**
	 * true if files are the same.
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean compareByContent(File a, File b) {
		FileInputStream ia = null;
		FileInputStream ib = null;
		try {
			if (a.length() != b.length())
				return false;
			byte[] buf_a = new byte[32000];
			byte[] buf_b = new byte[32000];
			
			 ia = new FileInputStream(a);
			 ib = new FileInputStream(b);
			
			int readed = 0;
			while ((readed = ia.read(buf_a)) > 0) {
				ib.read(buf_b);
				if (!Arrays.equals(buf_a, buf_b))
					return false;
			}
			return true;
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			try {
				ia.close();
			} catch (Exception e) {
				
			}
			try {
				ib.close();
			} catch (Exception e) {
				
			}			
		}
		return false;
	}
}
