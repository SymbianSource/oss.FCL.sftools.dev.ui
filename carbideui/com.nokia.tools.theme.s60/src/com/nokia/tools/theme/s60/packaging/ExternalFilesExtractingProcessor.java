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
/**
 * 
 */
package com.nokia.tools.theme.s60.packaging;

import java.util.List;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.PackagingMessages;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.s60.model.S60Theme;

/**
 * Extract info about files to be embedded and add them to packaging context.
 * Initialy this was done for screensaver.
 */
public class ExternalFilesExtractingProcessor extends
		AbstractS60PackagingProcessor {

	/* (non-Javadoc)
	 * @see com.nokia.tools.packaging.IPackagingProcessor.AbstractPackagingProcessor#processSpi()
	 */
	@Override
	protected Object processSpi() throws PackagingException {
		IContent theme = (IContent) context.getInput();
		if (theme == null) {
			throw new PackagingException(PackagingMessages.Error_themeMissing);
		}

		S60Theme skin = (S60Theme) ((ThemeContent) theme).getData();

		extractEmbeddedFiles(context, skin, getWorkingDir());
		
		return theme;
	}

	/**
	 * traverses through components, task, elements and if file is specified as to be embeded
	 * add it to context
	 * @param context
	 * @param skin
	 * @param workingDir
	 */
	private void extractEmbeddedFiles(PackagingContext context, ThemeBasicData themeData, String workingDir){
    	
    	if (!themeData.hasChildNodes())
    		return;
    	
    	//Get the list of tasks and the keys from the specialTasks map
        List children = themeData.getChildren();

        for (int i = 0; i < children.size() ; i++) {
        	ThemeBasicData item = (ThemeBasicData)(children.get(i));
        	if( item instanceof SkinnableEntity ){
            	SkinnableEntity entity = (SkinnableEntity) item;
        		if(entity.isEntityType().equalsIgnoreCase(ThemeTag.ELEMENT_EMBED_FILE)
        				&& entity.getSkinnedStatus() == ThemeTag.SKN_ATTR_STATUS_DONE 
        				&& entity.getSelectionForTransfer() ){
        		   	try {
						String file = (String)entity.getThemeGraphic().getAttribute(ThemeTag.FILE_NAME);
						String[] embeddedFiles = (String[])context.getAttribute(PackagingAttribute.embeddedFiles.name());
						String[] newEmbeddedFiles = null;
						if(embeddedFiles != null && embeddedFiles.length > 0){
							newEmbeddedFiles = new String[embeddedFiles.length + 1];
							int count=0;
							for(String embeddedFile: embeddedFiles){
								newEmbeddedFiles[count++] = embeddedFile;
							}
							newEmbeddedFiles[count] = file;
						}
						else{
							newEmbeddedFiles = new String[] { file };
						}
						context.setAttribute(PackagingAttribute.embeddedFiles.name(),
								new String[] { file });
					} catch (ThemeException e) {
						e.printStackTrace();
					}
        		}
        	}
        	extractEmbeddedFiles(context, item, workingDir );
        }
	}

}
