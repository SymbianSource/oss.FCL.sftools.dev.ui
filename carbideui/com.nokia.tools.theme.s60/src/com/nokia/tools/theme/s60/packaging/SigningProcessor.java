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
import com.nokia.tools.packaging.PackagingConstants;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.PackagingMessages;
import com.nokia.tools.theme.s60.IThemeConstants;

/**
 * This processor generates the signed SIS package with the provided
 * certificate/private key.<br/> Mandatory attributes:
 * <ul>
 * <li>{@link PackagingAttribute#input} - the original sis file
 * <li>{@link PackagingAttribute#platform}
 * <li>{@link PackagingAttribute#sisFile}
 * <li>{@link PackagingAttribute#certificateFile}
 * <li>{@link PackagingAttribute#privateKeyFile}
 * </ul>
 * Optional attributes:
 * <ul>
 * <li>{@link PackagingAttribute#passphrase}
 * <li>{@link PackagingAttribute#algorithm}
 * <li>{@link PackagingAttribute#keepInputAfterSigning} - default "false"
 * </ul>
 * Output: the signed sis file
 */
public class SigningProcessor extends AbstractS60PackagingProcessor {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.packaging.IPackagingProcessor.AbstractPackagingProcessor#processSpi()
	 */
	@Override
	protected Object processSpi() throws PackagingException {
		boolean signPackage = new Boolean((String) context
				.getAttribute(PackagingAttribute.signPackage.name()));
		if (!signPackage) {
			return getInput();
		}

		if (!Platform.OS_WIN32.equals(Platform.getOS())) {
			throw new PackagingException(PackagingMessages.Error_osNotSupported);
		}

		String algorithm = getAlgorithm();
		String certificateFile = checkCertificateFile();
		String privateKeyFile = checkPrivateKeyFile();
		String passphrase = getPassphrase();
		String input = getInput();
		String sisFile = checkSisFile();

		if (input == null) {
			throw new PackagingException(PackagingMessages.Error_sisFileMissing);
		}

		String tempOut = input + "x";

		List<String> list = new ArrayList<String>();
		if (algorithm != null) {
			if (PackagingConstants.ALGORITHM_DSA.equalsIgnoreCase(algorithm)) {
				list.add("-cd");
			} else if (PackagingConstants.ALGORITHM_RSA
					.equalsIgnoreCase(algorithm)) {
				list.add("-cr");
			}
		}
		list.add("-s");
		list.add(input);
		list.add(tempOut);
		list.add(certificateFile);
		list.add(privateKeyFile);
		if (passphrase != null) {
			list.add(passphrase);
		}

		String exe = null;
		String modelID = null;
		String secondaryModelID = null;

        modelID = this.context.getAttribute(PackagingAttribute.primaryModelId.name()).toString();
        secondaryModelID = this.context.getAttribute(PackagingAttribute.secondaryModelId.name()).toString();
        URL packagingExecutablePath = PackagingExecutableProvider.getPackagingExecutablePath(modelID, PackagingExecutableType.SIS_SIGNER, false);
        if(packagingExecutablePath == null && secondaryModelID != null){
        	packagingExecutablePath = PackagingExecutableProvider.getPackagingExecutablePath(secondaryModelID, PackagingExecutableType.SIS_SIGNER, true);
        }
        if(packagingExecutablePath != null){
        	exe = PackagingExecutableType.SIS_SIGNER.name() + ".exe";
        	copyExecutableFile(packagingExecutablePath, exe);
        }

		if(exe == null){
			exe = "signsis.exe";
			
		}
		exec(exe, list);

		if (Boolean.valueOf((String) context
				.getAttribute(PackagingAttribute.keepInputAfterSigning.name()))) {
			copy(tempOut, sisFile);
			new File(tempOut).delete();
		} else {
			// keeps the unsigned sis file
			File unsigned = new File(input);
			String name = unsigned.getName();
			String prefix = "", suffix = ".sis";
			int index = name.lastIndexOf(".");
			if (index >= 0) {
				prefix = name.substring(0, index);
				suffix = name.substring(index);
			}
			if (prefix.length() == 0) {
				// safeguard for future
				prefix = "package";
			}
			unsigned.renameTo(new File(unsigned.getParentFile(), prefix
					+ "-unsigned" + suffix));
			// renames the sisx to sis file
			if (new File(tempOut).renameTo(new File(input))) {
				copy(input, sisFile);
			} else {
				copy(tempOut, sisFile);
				new File(tempOut).delete();
			}
		}

		return sisFile;
	}
}
