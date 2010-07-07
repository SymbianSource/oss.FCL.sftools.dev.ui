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

import com.nokia.tools.packaging.commandline.CmdParameters;
import com.nokia.tools.packaging.commandline.PackagerMessages;
import com.nokia.tools.packaging.commandline.util.CMDProgressMonitor;
import com.nokia.tools.packaging.commandline.util.CommandLineException;
import com.nokia.tools.packaging.commandline.util.CommandParser;
import com.nokia.tools.packaging.commandline.util.InvalidParameterException;
import com.nokia.tools.packaging.commandline.util.MissingParameterException;
import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.core.IPlatform;

/**
 * Validater perfoms validations on command input perameters
 * 
 * @author Bhanu
 * 
 */
public class ValidaterCommand implements ICommand {

	private static final long MIN_UID = 0x10000000L;

	private static final long MAX_UID = 0xFFFFFFFFL;

	private static final long MID_UID = 0x7FFFFFFFL;

	private static final int MaxLen = 256;

	private CommandParser commandParser;

	private CmdParameters cmdParameters;

	private CMDProgressMonitor progressBar = null;

	public ValidaterCommand(CommandParser commandParser,
			CmdParameters cmdParameters, CMDProgressMonitor progressBar) {
		this.progressBar = progressBar;
		this.commandParser = commandParser;
		this.cmdParameters = cmdParameters;
	}

	public void execute() throws CommandLineException {
		progressBar.setTaskName("Validating input perameters");
		performBasicValidation();
		performAdvanceValidation();
		// progressBar.addWorked(10);
	}

	/**
	 * performBasicValidation assigns flag values to corresponding Packager
	 * members
	 * 
	 * @param cmdParameters
	 * @throws MissingParameterException
	 */
	public void performBasicValidation() throws MissingParameterException {
		cmdParameters.setStrInputPath(commandParser
				.getFlagValue(PackagerMessages.Packager_Flag_Input));
		cmdParameters.setStrOutputPath(commandParser
				.getFlagValue(PackagerMessages.Packager_Flag_Output));
		cmdParameters.setStrSelThemeUID(commandParser
				.getFlagValue(PackagerMessages.Packager_Flag_ID));
		cmdParameters.setStrVer(commandParser
				.getFlagValue(PackagerMessages.Packager_Flag_Version));
		cmdParameters.setStrNotice(commandParser
				.getFlagValue(PackagerMessages.Packager_Flag_Notice));
		cmdParameters.setStrThemeName(commandParser
				.getFlagValue(PackagerMessages.Packager_Flag_Themename));
		cmdParameters.setStrCopyAllowed(commandParser
				.getFlagValue(PackagerMessages.Packager_Flag_Copy));

	}

	/**
	 * validates the command parameter values
	 * 
	 * Path to tpf or tdf file
	 * 
	 * Output path (optional, default: folder_for_tpf+packagingOutput)
	 * 
	 * Theme ID (optional)
	 * 
	 * Target version (2.0 – 5.0) (optional, default 5.0)
	 * 
	 * Author, (optional)
	 * 
	 * Copyright notice(optional),
	 * 
	 */
	public void performAdvanceValidation() throws InvalidParameterException {

		File inputFile;
		String tempTdfFilePath;

		// validate input file path
		if ((cmdParameters.getStrInputPath() == null)
				|| (cmdParameters.getStrInputPath().trim().length() == 0)) {
			throw new InvalidParameterException(
					PackagerMessages.Packager_Error_input);

		} else {
			inputFile = new File(cmdParameters.getStrInputPath());
			String inputFileExt = getExtension(inputFile);

			if (!(PackagerMessages.Packager_TPF.equals(inputFileExt))
					&& !(PackagerMessages.Packager_TDF.equals(inputFileExt))) {
				throw new InvalidParameterException(cmdParameters
						.getStrInputPath()
						+ " " + PackagerMessages.Packager_Error_invalid_input);
			}

			if ((!inputFile.exists())) {
				throw new InvalidParameterException(cmdParameters
						.getStrInputPath()
						+ " " + PackagerMessages.Packager_Error_FileNotExist);
			}

			// Convert tpf to tdf file
			if (PackagerMessages.Packager_TPF.equals(inputFileExt)) {
				tempTdfFilePath = System.getProperty("java.io.tmpdir")
						+ File.separator + stripExtension(inputFile);
		
				String tempTdfFile = tempTdfFilePath + "/" + stripExtension(inputFile) + ".tdf";
		
				if(!new File(tempTdfFile).exists()){
					// 	The case when the tpf file is packaged such that the TDF file path is {theme name}\{theme name}\{theme name}.tdf
					// instead of {theme name}\{theme name}.tdf
			
					tempTdfFilePath += File.separator + stripExtension(inputFile);
					tempTdfFile = tempTdfFilePath + "/" + stripExtension(inputFile) + ".tdf";
				}
		
				cmdParameters.setTdfFilePath(new File(tempTdfFilePath));

				cmdParameters.setTdfFile(new File(tempTdfFile));
				cmdParameters.setTpf(true);
			} else if (PackagerMessages.Packager_TDF.equals(inputFileExt)) {
				cmdParameters.setTdfFile(new File(cmdParameters
						.getStrInputPath()));
			}
		}

		// validate output directory path
		if ((cmdParameters.getStrOutputPath() == null)
				|| (cmdParameters.getStrOutputPath().trim().length() == 0)) {
			cmdParameters.setStrOutputPath(inputFile.getParent()
					+ PackagerMessages.Packager_FileSeperator
					+ PackagerMessages.Packager_DefaultOutFolder);
		}
		cmdParameters.setOutPut(new File(cmdParameters.getStrOutputPath()));

		if (!cmdParameters.getOutPut().exists()) {
			try {
				if (!cmdParameters.getOutPut().mkdir())
					throw (new Exception());
				// tempFolder.deleteOnExit();
			} catch (Exception e) {
				// tempFolder.deleteOnExit();
				throw new InvalidParameterException(
						PackagerMessages.Packager_Error_OutFolderNotValid);

			}
		}
		// validate ThemeUID
		if ((cmdParameters.getStrSelThemeUID() != null)
				&& (cmdParameters.getStrSelThemeUID().trim()).length() > 0) {
			if (!cmdParameters.getStrSelThemeUID().toLowerCase().startsWith(
					PackagerMessages.Packager_HexStartsWith)) {
				throw new InvalidParameterException(
						PackagerMessages.Packager_Error_UIDNotHex);
			}
			long value = 0;
			try {
				value = Long.parseLong(cmdParameters.getStrSelThemeUID()
						.substring(2), 16);
			} catch (Exception e) {
				throw new InvalidParameterException(
						PackagerMessages.Packager_Error_UIDNotHex);
			}

			if (value < MIN_UID || value > MAX_UID) {
				throw new InvalidParameterException(
						PackagerMessages.Packager_Error_UIDRange + " "
								+ PackagerMessages.Packager_HexStartsWith
								+ Integer.toHexString((int) MIN_UID) + " - "
								+ PackagerMessages.Packager_HexStartsWith
								+ Integer.toHexString((int) MAX_UID));
			}
			if (value <= MID_UID) {

				PackagerMessages
						.printMessageOnConsole(PackagerMessages.Packager_Commandline
								+ PackagerMessages.Packager_Warning
								+ " "
								+ PackagerMessages.Packager_Error_UIDProtectedRange
								+ " "
								+ PackagerMessages.Packager_HexStartsWith
								+ Integer.toHexString((int) MIN_UID)
								+ " - "
								+ PackagerMessages.Packager_HexStartsWith
								+ Integer.toHexString((int) MID_UID));
			}
		}
		// validate target platform vertsion
		if ((cmdParameters.getStrVer() == null)
				|| (cmdParameters.getStrVer().trim()).length() == 0) {
			cmdParameters.setPlatform(DevicePlatform.SF_2);
		}
		if ("5.0".equals(cmdParameters.getStrVer().trim())) {
			cmdParameters.setPlatform(DevicePlatform.S60_5_0);
		}
		else if("Symbian^2".equalsIgnoreCase(cmdParameters.getStrVer().trim())){
			cmdParameters.setPlatform(DevicePlatform.SF_2);
		}
		else {
			IPlatform platform = DevicePlatform.getPlatformById(cmdParameters.getStrVer().trim());
			if(platform != null){
				cmdParameters.setPlatform(platform);
			}
			else 
				throw new InvalidParameterException(
						PackagerMessages.Packager_Error_InvalidTarget);
		}

		// validate Copyright
		if (cmdParameters.getStrNotice().length() > MaxLen) {
			throw new InvalidParameterException(
					PackagerMessages.Packager_Error_CopyrightMaxLen);
		}
		// validate Theme name
		if (cmdParameters.getStrThemeName().length() > MaxLen) {
			throw new InvalidParameterException(
					PackagerMessages.Packager_Error_Themename);
		} else if (cmdParameters.getStrThemeName().trim().length() != 0) {
			File file = null;
			try {
				file = new File(System.getProperty("java.io.tmpdir")
						+ File.separator + cmdParameters.getStrThemeName()
						+ ".txt");
				file.createNewFile();
			} catch (Exception e) {
				throw new InvalidParameterException(
						PackagerMessages.Packager_Error_Themename_Illegal);
			}
			file.deleteOnExit();
		}
		// validate copy protection
		if ("2".equalsIgnoreCase(cmdParameters.getStrCopyAllowed())) {
			cmdParameters.setStrCopyAllowed("false");
		} else if ("1".equalsIgnoreCase(cmdParameters.getStrCopyAllowed())) {
			cmdParameters.setStrCopyAllowed("true");
		} else {
			throw new InvalidParameterException(
					PackagerMessages.Packager_Error_Copy);
		}

	}

	/**
	 * extracts the file name with out extension
	 * 
	 * @param f
	 * @return
	 */
	private String stripExtension(File f) {
		// String fileName = "";
		// String s = f.getName();
		// StringTokenizer strToken = new StringTokenizer(s, ".");
		//
		// while (strToken.hasMoreTokens()) {
		// fileName = strToken.nextToken();
		// }
		//
		// return fileName;
		String fileName = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			fileName = s.substring(0, i).toLowerCase();
		}
		return fileName;
	}

	private static String getExtension(String filename) {
		if (filename == null)
			return null;
		int i = filename.lastIndexOf('.');
		if (i != -1 && i < filename.length()) {
			return filename.substring(++i);
		}
		return null;
	}

	private static String getExtension(File f) {
		return getExtension(f.getName());
	}
}
