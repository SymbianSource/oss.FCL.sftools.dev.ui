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
package com.nokia.tools.packaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.core.IPlatform;

/**
 * Interface for concrete processor to implement.
 * 
 */
public interface IPackagingProcessor {
	/**
	 * Generates the output using the provided packaging context.
	 * 
	 * @param context the packaging context that contains information required
	 *            for the processor to perform its task.
	 * @return the generated output.
	 * @throws PackagingException if the packaging process failed.
	 */
	Object process(PackagingContext context) throws PackagingException;

	/**
	 * Stub implementation of the processor interface. It provides methods for
	 * performing common functionalities among various processor
	 * implementations.
	 * 
	 */
	public abstract class AbstractPackagingProcessor implements
			IPackagingProcessor {
		protected PackagingContext context;

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.packaging.IPackagingProcessor#process(com.nokia.tools.packaging.PackagingContext)
		 */
		public Object process(PackagingContext context)
				throws PackagingException {
			this.context = context;
			return processSpi();
		}

		/**
		 * Method that does actual business logic.
		 * 
		 * @return the generated output.
		 * @throws PackagingException if the packaging process failed.
		 */
		protected abstract Object processSpi() throws PackagingException;

		/**
		 * @return the input object.
		 */
		protected String getInput() {
			return (String) context.getInput();
		}

		/**
		 * @return the output object.
		 */
		protected String getOutput() {
			return (String) context.getOutput();
		}

		/**
		 * @return the current platform.
		 */
		protected IPlatform getPlatform() {
			return (IPlatform) context.getAttribute(PackagingAttribute.platform
					.name());
		}

		/**
		 * @return the absolute SIS package file path.
		 */
		protected String getSisFile() {
			return (String) context.getAttribute(PackagingAttribute.sisFile
					.name());
		}

		/**
		 * @return the path to the temporary sis file, th
		 */
		protected String getSisTempFile() {
			return (String) context.getAttribute(PackagingAttribute.sisTempFile
					.name());
		}

		/**
		 * @return the absolute certficate file path.
		 */
		protected String getCertificateFile() {
			return (String) context
					.getAttribute(PackagingAttribute.certificateFile.name());
		}

		/**
		 * @return the absolute private key file path.
		 */
		protected String getPrivateKeyFile() {
			return (String) context
					.getAttribute(PackagingAttribute.privateKeyFile.name());
		}

		/**
		 * @return the passphrase associated with the given private key.
		 */
		protected String getPassphrase() {
			return (String) context.getAttribute(PackagingAttribute.passphrase
					.name());
		}

		/**
		 * @return the key generation algorithm.
		 */
		protected String getAlgorithm() {
			return (String) context.getAttribute(PackagingAttribute.algorithm
					.name());
		}

		/**
		 * @return the theme content.
		 */
		protected IContent getTheme() {
			return (IContent) context.getAttribute(PackagingAttribute.theme
					.name());
		}

		/**
		 * @return the theme package name.
		 */
		protected String getThemePackageName() {
			return (String) context
					.getAttribute(PackagingAttribute.themePackageName.name());
		}

		/**
		 * @return the theme name.
		 */
		protected String getThemeName() {
			return (String) context.getAttribute(PackagingAttribute.themeName
					.name());
		}

		/**
		 * @return the theme item list file.
		 */
		protected String getThemeItemListFile() {
			return (String) context
					.getAttribute(PackagingAttribute.themeItemListFile.name());
		}

		/**
		 * @return the embedded files.
		 */
		protected String[] getEmbeddedFiles() {
			return (String[]) context
					.getAttribute(PackagingAttribute.embeddedFiles.name());
		}

		/**
		 * @return the vendor
		 */
		protected String getVendor() {
			return (String) context.getAttribute(PackagingAttribute.vendor
					.name());
		}
		
		/**
		 * return the vendor icon file name
		 * @return
		 */
		protected String getVendorIcon() {
			return (String) context.getAttribute(PackagingAttribute.vendorIcon
					.name());
		}

		/**
		 * @return true if the DRM protection is enabled, false otherwise.
		 */
		protected boolean isDRM() {
			return new Boolean((String) context
					.getAttribute(PackagingAttribute.themeDRM.name()));
		}

		/**
		 * @return true if the packaging only supports bitmaps.
		 */
		protected boolean isNormalSelection() {
			return new Boolean((String) context
					.getAttribute(PackagingAttribute.themeNormalSelection
							.name()));
		}

		protected IContent checkTheme() throws PackagingException {
			IContent theme = getTheme();
			if (theme == null) {
				throw new PackagingException(
						PackagingMessages.Error_themeMissing);
			}
			return theme;
		}

		/**
		 * Checks the theme name and throws exception if the theme name is
		 * invalid.
		 * 
		 * @return the theme name.
		 * @throws PackagingException if theme name is not valid.
		 */
		protected String checkThemeName() throws PackagingException {
			String themeName = getThemeName();
			if (themeName == null) {
				throw new PackagingException(
						PackagingMessages.Error_themeNameMissing);
			}
			return themeName;
		}

		/**
		 * Checks the theme package name and throws exception if the theme name
		 * is invalid.
		 * 
		 * @return the theme package name.
		 * @throws PackagingException if theme package name is not valid.
		 */
		protected String checkThemePackageName() throws PackagingException {
			String themePackageName = getThemePackageName();
			if (themePackageName == null) {
				throw new PackagingException(
						PackagingMessages.Error_themeNameMissing);
			}
			return themePackageName;
		}
		/**
		 * Checks if the SIS file path and throws exception if the SIS file path
		 * is invalid.
		 * 
		 * @return the absolute SIS file path.
		 * @throws PackagingException if the SIS file path is not valid.
		 */
		protected String checkSisFile() throws PackagingException {
			String sisFile = getSisFile();
			if (sisFile == null) {
				throw new PackagingException(
						PackagingMessages.Error_sisFileMissing);
			}
			return sisFile;
		}

		/**
		 * Checks if the private key file path and throws exception if the SIS
		 * file path is invalid.
		 * 
		 * @return the absolute private key file path.
		 * @throws PackagingException if the private key file path is not valid.
		 */
		protected String checkPrivateKeyFile() throws PackagingException {
			String privateKeyFile = getPrivateKeyFile();
			if (privateKeyFile == null) {
				throw new PackagingException(
						PackagingMessages.Error_privateKeyFileMissing);
			}
			return privateKeyFile;
		}

		/**
		 * Checks if the certificate file path and throws exception if the SIS
		 * file path is invalid.
		 * 
		 * @return the absolute certificate file path.
		 * @throws PackagingException if the certificate file path is not valid.
		 */
		protected String checkCertificateFile() throws PackagingException {
			String certificateFile = getCertificateFile();
			if (certificateFile == null) {
				throw new PackagingException(
						PackagingMessages.Error_certificateFileMissing);
			}
			return certificateFile;
		}

		/**
		 * @return the packaging processor's working directory or the current
		 *         directory if it's not available.
		 */
		protected String getWorkingDir() {
			String dir = (String) context
					.getAttribute(PackagingAttribute.workingDir.name());
			if (dir != null) {
				File file = new File(dir);
				file.mkdir();
				return dir;
			}
			return ".";
		}

		/**
		 * Creates an empty file wit the specific name in the working directory.
		 * 
		 * @param name name of the file to be created.
		 * @throws PackagingException if file creation failed.
		 */
		protected void createEmptyFile(String name) throws PackagingException {
			File file = new File(getWorkingDir() + File.separator + name);
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new PackagingException(e);
			}
		}

		/**
		 * Copies the source file to the target file.
		 * 
		 * @param src the source file.
		 * @param tgt the target file.
		 * @throws PackagingException if copying failed.
		 */
		protected void copy(String src, String tgt) throws PackagingException {
			if (new File(src).equals(new File(tgt))) {
				return;
			}
			InputStream in = null;
			OutputStream out = null;
			try {
				in = new FileInputStream(src);
				out = new FileOutputStream(tgt);
				byte[] buf = new byte[16384];
				int read = 0;
				while ((read = in.read(buf)) > 0) {
					out.write(buf, 0, read);
					out.flush();
				}
			} catch (Exception e) {
				throw new PackagingException(e);
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (Exception e) {
				}
				try {
					if (out != null) {
						out.close();
					}
				} catch (Exception e) {
				}
			}
		}

		/**
		 * Returns the name of the source file.
		 * 
		 * @param src the source file from where the name is to be retrieved.
		 * @return the name of the source file.
		 */
		protected String getFileName(String src) {
			return new File(src).getName();
		}

		/**
		 * Copies the given file to the working directory.
		 * 
		 * @param src the source file to be copied.
		 * @throws PackagingException if copying failed.
		 */
		protected void copyToWorkingDir(String src) throws PackagingException {
			copy(src, getWorkingDir() + File.separator + getFileName(src));
		}
	}
}
