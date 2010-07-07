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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.PackagingMessages;
import com.nokia.tools.theme.s60.packaging.util.SymbianUtil;

/**
 * This processor converts the multimedia contents to the format that can used
 * in the phone. If the attribute
 * {@link PackagingAttribute#themeNormalSelection} is not set to "true", then
 * this processor will do nothing.<br/>
 * Mandatory attributes:
 * <ul>
 * <li>{@link PackagingAttribute#input} - name of the content
 * <li>{@link PackagingAttribute#platform}
 * </ul>
 * Optional attributes:
 * <ul>
 * <li>{@link PackagingAttribute#output} - optional output file name. The
 * default is <code>input.mif</code>.
 * </ul>
 * Output: name of the content.
 */
public class MifConverter extends AbstractS60PackagingProcessor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.s60.packaging.ThemePackagingProcessor#processSpi()
	 */
	@Override
	protected Object processSpi() throws PackagingException {
		if (isNormalSelection()) {
			return getInput();
		}
		if (!Platform.OS_WIN32.equals(Platform.getOS())) {
			throw new PackagingException(PackagingMessages.Error_osNotSupported);
		}

		String input = getInput();
		String output = getOutput();

		if (input == null) {
			throw new PackagingException(PackagingMessages.Error_mifNameMissing);
		}
		if (output == null) {
			output = input + ".mif";
		}

		createEmptyFile(input + ".mbm");
		String mifListFile = input + "_MIFList.txt";

		String runtimeDir = SymbianUtil.getRuntimeDir();
		List<String> list = new ArrayList<String>();
		list.add(output);
		list.add("/B" + runtimeDir);
		list.add("/P" + runtimeDir + File.separator + "thirdpartybitmap.pal");
		list.add("/F" + mifListFile);
		list.add("/H" + input + ".mbg");

		// /Vsvgtbinencode_version specifies the format version of the generated
		// SVGT binary code by svgtbinencode.exe.
		// svgtbinencode_version may be one of the following values:
		// 1 - Compatible with S60 Edition 3, feature pack 1 and following
		// editions and feature packs.
		// 3 - Compatible with S60 Edition 3, feature pack 2 and following
		// editions and feature packs.
		// (Default with S60 Edition 3, feature pack 2.)
		// Includes some performance improvements compared to version 1.
		// Note, not compatible with S60 Edition 3, feature pack 1.

		String exe = null;
		String modelID = null;
		String secondaryModelID = null;

		Boolean primaryModelIDExecutableCopied = null; // Null means copied none
														// [neither from primary
														// model Id or secondary
														// model ID.
		// If copied from primary model ID then it will be set to Boolean.TRUE
		// else set to Boolean.FALSE.
		modelID = this.context.getAttribute(
				PackagingAttribute.primaryModelId.name()).toString();// theme.getModelId();
		secondaryModelID = this.context.getAttribute(
				PackagingAttribute.secondaryModelId.name()).toString();// theme.getModelId();
		URL packagingExecutablePath = PackagingExecutableProvider
				.getPackagingExecutablePath(modelID,
						PackagingExecutableType.MIF_CONVERTER, false);
		if (packagingExecutablePath == null && secondaryModelID != null) {
			primaryModelIDExecutableCopied = Boolean.FALSE;
			packagingExecutablePath = PackagingExecutableProvider
					.getPackagingExecutablePath(secondaryModelID,
							PackagingExecutableType.MIF_CONVERTER, true);
		}
		if (packagingExecutablePath != null) {
			if (primaryModelIDExecutableCopied == null) {
				primaryModelIDExecutableCopied = Boolean.TRUE;
			}
			exe = PackagingExecutableType.MIF_CONVERTER.name() + ".exe";
			copyExecutableFile(packagingExecutablePath, exe);
		}

		/*
		 * MIF Converter internally makes use of Bitmap converter [bmconv.exe]
		 * and SVT Bin Encoder [SVGTBinencode.exe] and hence we need to even
		 * copy these execs if any defined for the platform and with the names
		 * as mentioned above.
		 */

		// Copying bmconv.exe

		packagingExecutablePath = PackagingExecutableProvider
				.getPackagingExecutablePath(modelID,
						PackagingExecutableType.BITMAP_CONVERTER, false);
		if (packagingExecutablePath == null && secondaryModelID != null) {
			packagingExecutablePath = PackagingExecutableProvider
					.getPackagingExecutablePath(secondaryModelID,
							PackagingExecutableType.BITMAP_CONVERTER, true);
		}
		if (packagingExecutablePath != null) {
			String bmconv = "BMCONV.EXE";
			copyExecutableFile(packagingExecutablePath, bmconv);
		}

		// Copying the SVGTBINENCODE.exe

		packagingExecutablePath = PackagingExecutableProvider
				.getPackagingExecutablePath(modelID,
						PackagingExecutableType.SVGT_BIN_ENCODE, false);
		if (packagingExecutablePath == null && secondaryModelID != null) {
			packagingExecutablePath = PackagingExecutableProvider
					.getPackagingExecutablePath(secondaryModelID,
							PackagingExecutableType.SVGT_BIN_ENCODE, true);
		}
		if (packagingExecutablePath != null) {
			String svgtbinencode = "SVGTBINENCODE.exe";
			copyExecutableFile(packagingExecutablePath, svgtbinencode);
		}

		if (exe == null) {
			exe = "mifconv.exe";
		}

		list.add("/S" + runtimeDir);
		list.add("/V1");
		
		// Call the method for passing the plugin specific parameter for
		// executing the .exes.
		// Check primaryModelIDExecutableCopied value. If null, we need not make
		// call for gettting updated list of parameters.
		// If primaryModelIDExecutableCopied == Boolean.FALSE, we need to use
		// secondaryModelID for the fetch, else modelID.

		if (primaryModelIDExecutableCopied == Boolean.FALSE) {
			list = PackagingExecutableProvider
					.getUpdatedParameterListForExecutable(secondaryModelID,
							PackagingExecutableType.MIF_CONVERTER, context,
							list);
		} else if (primaryModelIDExecutableCopied == Boolean.TRUE) {
			list = PackagingExecutableProvider
					.getUpdatedParameterListForExecutable(modelID,
							PackagingExecutableType.MIF_CONVERTER, context,
							list);
		}
		
		exec(exe, list);

		// Some of the mifconv.exe deletes the mbm if not needed
		// but this file (even empty) is needed for the sis packaging.
		// Hence we create here an empty file if its not already present.
		createEmptyFile(input + ".mbm");
		// if no mif is created then, create empty mif file needed for sis
		// packaging
		// case occurs when only multimedia elements are package since they
		// doesn't go as part of
		// hence doesn't results in creating mif entry hence no mif file.
		createEmptyFile(input + ".mif");
		return input;
	}
}
