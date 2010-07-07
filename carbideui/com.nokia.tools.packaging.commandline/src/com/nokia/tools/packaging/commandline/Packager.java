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

package com.nokia.tools.packaging.commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.nokia.tools.packaging.commandline.commands.TDF2SkinDescCommand;
import com.nokia.tools.packaging.commandline.commands.TPF2TDFCommand;
import com.nokia.tools.packaging.commandline.commands.ValidaterCommand;
import com.nokia.tools.packaging.commandline.util.CMDProgressMonitor;
import com.nokia.tools.packaging.commandline.util.CommandLineException;
import com.nokia.tools.packaging.commandline.util.CommandParser;
import com.nokia.tools.resource.util.FileUtils;

/**
 * Packager is Main class which sequences the flow of method calls.
 * 
 * @author Bhanu
 */
public class Packager {
	public static final String PLUGIN_ID = "com.nokia.tools.packaging.commandline";

	private static CmdParameters cmdParameters;

	private static CommandParser commandParser;

	private static ValidaterCommand validater;

	private TPF2TDFCommand tpf2tdfCommand = null;

	private TDF2SkinDescCommand tdf2SkinDescCommand = null;

	private static CMDProgressMonitor progressBar = null;
	
	public static boolean exit=true;
	
	public static void exitAfterRun(boolean exitVm){
		exit = exitVm;
	}
	

	public static void main(String args[]) {
		PackagerMessages
		.printMessageOnConsole(PackagerMessages.Packager_Process_Started);
		cmdParameters = new CmdParameters();
		progressBar = new CMDProgressMonitor();
		Packager packager = new Packager();

		try {
			commandParser = new CommandParser();
			commandParser.buildParameterList(args, progressBar);
			packager.validate(progressBar);
			if (cmdParameters.isTpf()) {
				packager.tpf2Tdf();
			}
			packager.generateSkinDescriptor();
			progressBar.addWorked(CMDProgressMonitor.TOTAL - progressBar.getWorked());
			progressBar.progress(progressBar.getWorked());
			PackagerMessages
					.printMessageOnConsole(PackagerMessages.Packager_Commandline
							+ "\n" + PackagerMessages.Packager_Successful);
		} catch (CommandLineException e) {
			if (progressBar.getWorked() > 0)
				PackagerMessages
						.printMessageOnConsole(PackagerMessages.Packager_Commandline
								+ "");
			progressBar.setCanceled(true);
			if (e.getMessage().startsWith(PackagerMessages.Packager_Usage)) {
				PackagerMessages
						.printMessageOnConsole(PackagerMessages.Packager_Commandline
								+ e.getMessage());
			} else {
				PackagerMessages
						.printMessageOnConsole(PackagerMessages.Packager_Commandline
								+ PackagerMessages.Packager_Error
								+ " "
								+ e.getMessage());
				PackagerMessages
						.printMessageOnConsole(PackagerMessages.Packager_Commandline
								+ PackagerMessages.Packager_UnSuccessful);
			}

		} catch (Throwable exception) {
			// this block catch all the exceptions just to make sure the tool
			// doesnt hang in between. Mostly this happens in case if Carbide.ui
			// packager hits some fileNotfound in between
			// Need to analyse the set of most probable exceptions and
			// replace this block accordingly
			if (progressBar.getWorked() > 0)
				PackagerMessages
						.printMessageOnConsole(PackagerMessages.Packager_Commandline
								+ "");
			progressBar.setCanceled(true);
			exception.printStackTrace();
			PackagerMessages
					.printMessageOnConsole(PackagerMessages.Packager_Commandline
							+ PackagerMessages.Packager_Error
							+ " "
							+ PackagerMessages.Packager_Error_packaging);
			PackagerMessages
					.printMessageOnConsole(PackagerMessages.Packager_Commandline
							+ PackagerMessages.Packager_UnSuccessful);
		} finally {
			if(exit){
				System.exit(0);
			}			
			else{
			 exit=true;
			}
		}

	}

	private void validate(CMDProgressMonitor progressBar)
			throws CommandLineException {
		validater = new ValidaterCommand(commandParser, cmdParameters,
				progressBar);
		validater.execute();
	}

	/**
	 * tpf2Tdf convers tpf file to tdf file and sets the path
	 * for the correct TDF file.
	 * 
	 */
	private void tpf2Tdf() throws CommandLineException {
		tpf2tdfCommand = new TPF2TDFCommand(new File(cmdParameters
				.getStrInputPath()), cmdParameters.getTdfFilePath(),
				progressBar);
		tpf2tdfCommand.execute();
		File[] tdfFiles = searchTDFFiles(cmdParameters.getTdfFilePath());
		
		if(tdfFiles == null || tdfFiles.length == 0){
			throw new CommandLineException(PackagerMessages.Packager_Error_NO_TDF_FILE_PRESENT);
		}
		else if(tdfFiles.length > 1){
			throw new CommandLineException(PackagerMessages.Packager_Error_MULTIPLE_TDF_FILES_PRESENT);
		}
		cmdParameters.setTdfFile(tdfFiles[0]);
	}

	private File[] searchTDFFiles(File tdfFilePath) {
		List<File> tdfFiles = new ArrayList<File>();
		locateAndAddTDFFiles(tdfFiles, tdfFilePath);
		return (File[])tdfFiles.toArray(new File[0]);
	}

	private void locateAndAddTDFFiles(List<File> tdfFileList, File path){
		File[] children = path.listFiles();
		if(children != null && children.length >0){
			for(File child: children){
				if(child.isDirectory()){
					locateAndAddTDFFiles(tdfFileList, child);
				}
				else{
					String extension = FileUtils.getExtension(child);
					if(extension != null && extension.toLowerCase().equals(PackagerMessages.Packager_TDF)){
						tdfFileList.add(child);
					}
				}
			}
		}
	}

	/**
	 * skinDescription generates skin description file that will be used by
	 * successive commands.
	 * 
	 */
	private void generateSkinDescriptor() throws CommandLineException {
		tdf2SkinDescCommand = new TDF2SkinDescCommand(cmdParameters,
				progressBar);
		tdf2SkinDescCommand.execute();
	}
}
