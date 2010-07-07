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
import java.io.IOException;

import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.PackagingMessages;
import com.nokia.tools.packaging.IPackager.Packager;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.theme.s60.packaging.pkg.Header;
import com.nokia.tools.theme.s60.packaging.pkg.PackageFile;
import com.nokia.tools.theme.s60.packaging.pkg.Signature;

/**
 * This processor generates the symbian package description file.<br/>
 * Mandatory attributes:
 * <ul>
 * <li>{@link PackagingAttribute#themeName} - name of the theme
 * </ul>
 * Optional attributes:
 * <ul>
 * <li>{@link PackagingAttribute#screenSaverFile}
 * <li>{@link PackagingAttribute#screenSaverUid}
 * <li>{@link PackagingAttribute#privateKeyFile}
 * <li>{@link PackagingAttribute#certificateFile}
 * </ul>
 * Output: the symbian package description file.
 */
public class ThemePackagingProcessor extends AbstractS60PackagingProcessor {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.packaging.IPackagingProcessor.AbstractPackagingProcessor#processSpi()
	 */
	@Override
	protected Object processSpi() throws PackagingException {
		String input = getInput();
		if (input == null) {
			throw new PackagingException(
					PackagingMessages.Error_themeNameMissing);
		}
		String workingDir = getWorkingDir();
		String privateKeyFile = getPrivateKeyFile();
		String certificateFile = getCertificateFile();
		String passphrase = getPassphrase();
		IPlatform platform = getPlatform();
		String themeName = checkThemeName();

		String output = getOutput();
		if (output == null) {
			output = input + ".pkg";
		}
		if (passphrase != null && passphrase.length() == 0) {
			passphrase = null;
		}

		File file = new File(workingDir, output);
		PackageFile pkg = new PackageFile();
		try {
			pkg.load(file);
		} catch (IOException e) {
			throw new PackagingException(e);
		}

		Header header = (Header) pkg.getStatement(Header.class);
		if (header != null) {
			header.setName(themeName);
			pkg.setStatement(header);
		}
		addEmbeddedFiles(pkg);

		// finally checks if there are statements contributed by the embedded
		// contents
		Object[] stmts = (Object[]) context
				.getAttribute(PackagingAttribute.packageStatements.name());
		if (stmts != null) {
			for (Object stmt : stmts) {
				pkg.addStatement(stmt);
			}
		}

		try {
			pkg.save(file);
		} catch (IOException e) {
			throw new PackagingException(e);
		}

		return output;
	}
}
