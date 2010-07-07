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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingConstants;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.PackagingMessages;
import com.nokia.tools.packaging.PackagingPlugin;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.theme.s60.packaging.pkg.Header;
import com.nokia.tools.theme.s60.packaging.pkg.LocalizedVendor;
import com.nokia.tools.theme.s60.packaging.pkg.PackageFile;
import com.nokia.tools.theme.s60.packaging.pkg.Vendor;
import com.nokia.tools.theme.s60.packaging.pkg.VendorLogo;

/**
 * This processor creates the SIS package using the symbian package description
 * file.<br/> Mandatory attributes:	
 * <ul>
 * <li>{@link PackagingAttribute#input} - the symbian package description file
 * <li>{@link PackagingAttribute#platform}
 * <li>{@link PackagingAttribute#sisFile}
 * </ul>
 * Optional attributes:
 * <ul>
 * <li>{@link PackagingAttribute#passphrase}
 * </ul>
 * Output: the SIS package.
 */
public class SisPackagingProcessor extends AbstractS60PackagingProcessor {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.packaging.IPackagingProcessor.AbstractPackagingProcessor#processSpi()
	 */
	@Override
	protected Object processSpi() throws PackagingException {
		if (PackagingPlugin.getDefault().getPreferenceStore().getBoolean(
				PackagingConstants.PREF_BREAK_PACKAGING_BEFORE_MAKESIS)) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(PlatformUI.getWorkbench()
							.getDisplay().getActiveShell(),
							"Packaging - Makesis paused",
							"Press OK to continue");
				}
			});
		}

		if (!Platform.OS_WIN32.equals(Platform.getOS())
				&& !Platform.OS_MACOSX.equals(Platform.getOS())) {
			throw new PackagingException(PackagingMessages.Error_osNotSupported);
		}

		String input = getInput();
		String sisFile = getSisFile();
		String sisTempFile = getSisTempFile();
		String passphrase = getPassphrase();
		String vendor = getVendor();
		String vendorIcon = getVendorIcon();
		String workingDir = getWorkingDir();

		if (sisFile == null && sisTempFile == null) {
			throw new PackagingException(PackagingMessages.Error_sisFileMissing);
		}
		if (input == null) {
			throw new PackagingException(
					PackagingMessages.Error_symbianPackageFileMissing);
		}

		if (!StringUtils.isEmpty(vendor)) {
			try {
				// adds vendor information
				File pkgFile = new File(workingDir + File.separator + input);
				PackageFile file = new PackageFile();
				file.load(pkgFile);
				Vendor v = (Vendor) file.getStatement(Vendor.class);
				LocalizedVendor lv = (LocalizedVendor) file
						.getStatement(LocalizedVendor.class);
				VendorLogo vl = (VendorLogo) file
						.getStatement(VendorLogo.class);
				Header header = getPackageHeader(file);
				
				if (v == null) {
					v = new Vendor(vendor);
					if(null == header)
						file.addStatement(v);
					else
						file.insertStatementAfter(header, v);
				} else {
					file.removeStatement(v);
					v.setName(vendor);
					file.insertStatementAfter(header, v);
				}
				if (lv == null) {
					lv = new LocalizedVendor(vendor);
					if(null == header)
						file.addStatement(lv);
					else
						file.insertStatementAfter(header, lv);
				} else {
					file.removeStatement(lv);
					lv.setName(vendor);
					file.insertStatementAfter(header, lv);
				}
				if (vl == null) {
					if (!vendorIcon.equals("")) {
						vl = new VendorLogo(vendorIcon);
						file.insertStatementAfter(v, vl);
					}
				} else {
					file.removeStatement(vl);
					vl.setName(vendorIcon);
					file.insertStatementAfter(lv, vl);
				}

				file.save(pkgFile);
			} catch (Exception e) {
				throw new PackagingException(e);
			}
		}

		String name = input;
		int index = input.lastIndexOf('.');
		if (index >= 0) {
			name = input.substring(0, index);
		}
		name += ".sis";

		List<String> list = new ArrayList<String>();

		list.add(input);

		if (sisTempFile != null) {
			list.add(sisTempFile);
		} else {
			list.add(name);
		}
		String exe = null;
		String modelID = null;
		String secondaryModelID = null;

        modelID = this.context.getAttribute(PackagingAttribute.primaryModelId.name()).toString();
        secondaryModelID = this.context.getAttribute(PackagingAttribute.secondaryModelId.name()).toString();
        URL packagingExecutablePath = PackagingExecutableProvider.getPackagingExecutablePath(modelID, PackagingExecutableType.SIS_CREATOR, false);
        
        if(packagingExecutablePath == null && secondaryModelID != null){
        	packagingExecutablePath = PackagingExecutableProvider.getPackagingExecutablePath(secondaryModelID, PackagingExecutableType.SIS_CREATOR, true);
        }
        	
        if(packagingExecutablePath != null){
        	exe = PackagingExecutableType.SIS_CREATOR.name() + ".exe";
        	copyExecutableFile(packagingExecutablePath, exe);
        }

		
		if(exe == null){
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				exe = "makesis.exe";
			} else {
				exe = "makesis";
			}
		}
		exec(exe, list);

		if (sisTempFile == null) {
			copy(new File(getWorkingDir(), name).getAbsolutePath(), sisFile);
		}
		return sisTempFile == null ? sisFile : sisTempFile;
	}

	private Header getPackageHeader(PackageFile file) {
		
		return (Header)file.getStatement(Header.class);
	}
}
