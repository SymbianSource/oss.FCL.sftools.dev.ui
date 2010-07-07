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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.IPackagingProcessor.AbstractPackagingProcessor;

/**
 * Generic-level optimizing package processor, removes file duplicates (compare on byte-level)
 * Needs instance of ThemePackageAcessor for support for particular package format.
 * @author peknijan
 *
 */
public class OptimizingPackageProcessor extends AbstractPackagingProcessor {

	private ThemePackageAccessor mediator;

	public OptimizingPackageProcessor(ThemePackageAccessor mediator) {
		this.mediator = mediator;
	}
	
	@Override
	protected Object processSpi() throws PackagingException {
		
		try {
		
		mediator.setContext(context);
		mediator.setWorkdir(getWorkingDir());
		mediator.setThemeContent(getTheme());
		
		mediator.start();
		
		List<File> packageContentFileList =  mediator.getPackageContentFileList();
		Map<File, File> fMap = new HashMap<File, File>(); 
		FileDuplicateResolver cache = new FileDuplicateResolver();
		
		List<File> obsoleteFiles = new ArrayList<File>();
		
		for (File imgFile: packageContentFileList) {
			try {
				File resolved = cache.resolveFile(imgFile);
				if (imgFile.equals(resolved)) {
					//no action needed
				} else {					
					//System.out.println(imgFile.getName() + "->" + resolved.getName());					
					//file already exists
					obsoleteFiles.add(imgFile);
					fMap.put(imgFile, resolved);
				}
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
		mediator.notifyReplaceFiles(fMap);
		mediator.notifyObsoleteFiles(obsoleteFiles);
		
		mediator.finish();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return context.getInput();
	}

}
