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

package com.nokia.tools.packaging.commandline.commands;

import java.io.File;
import java.io.IOException;

import com.nokia.tools.packaging.commandline.PackagerMessages;
import com.nokia.tools.packaging.commandline.util.CMDProgressMonitor;
import com.nokia.tools.packaging.commandline.util.ProcessorException;
import com.nokia.tools.resource.util.FileUtils;

/**
 * @author surmathe This class is responsible for converting TPF to TDF. Depends
 *         on media utils
 * 
 */
public class TPF2TDFCommand implements ICommand {

	private File archive = null;

	private File destination = null;

	private CMDProgressMonitor progressBar = null;

	public TPF2TDFCommand(File archive, File destination,
			CMDProgressMonitor progressBar) {
		this.progressBar = progressBar;
		this.archive = archive;
		this.destination = destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.cmdpackaging.commands.Command#execute()
	 */
	public void execute() throws ProcessorException {
		progressBar.setTaskName("Converting tpf to tdf");
		try {
			FileUtils.deleteDirectory(destination, progressBar);
			FileUtils.unzip(archive, destination);
		} catch (IOException e) {
			throw new ProcessorException(PackagerMessages.Packager_Error_tpf2tdf);
		}
		finally{
		
		}
	}

	public File getOutPut() {
		return destination;
	}

}
