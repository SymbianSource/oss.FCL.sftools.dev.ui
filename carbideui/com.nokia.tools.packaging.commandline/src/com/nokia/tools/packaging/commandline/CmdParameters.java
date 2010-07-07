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

import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.core.IPlatform;

public class CmdParameters {
	private String strInputPath = "";

	private String strOutputPath = "";

	private String strSelThemeUID = "";

	private String strVer = "";

	private String strNotice = "";

	private String strThemeName = "";

	private String strcopyAllowed = "";

	private File tdfFile = null;

	private File outPut = null;

	private File tdfFilePath;

	private IPlatform platform = DevicePlatform.SF_2;

	boolean isTpf = false;

	/**
	 * to get outPut directory
	 * 
	 * @return outPut
	 */
	public File getOutPut() {
		return outPut;
	}

	/**
	 * sets outPut folder
	 * 
	 * @param outPutFolder
	 */
	public void setOutPut(File outPutFolder) {
		this.outPut = outPutFolder;
	}

	/**
	 * to get tdf/tpf path
	 * 
	 * @return strInputPath
	 */
	public String getStrInputPath() {
		return strInputPath;
	}

	/**
	 * to get tdf/tpf path
	 * 
	 * @param strInputPath
	 */
	public void setStrInputPath(String strInputPath) {
		this.strInputPath = strInputPath;
	}

	/**
	 * to get copy right notice
	 * 
	 * @return strNotice
	 */
	public String getStrNotice() {
		return strNotice;
	}

	/**
	 * to set copy right notice
	 * 
	 * @param strNotice
	 */
	public void setStrNotice(String strNotice) {
		this.strNotice = strNotice;
	}

	/**
	 * get output directory path
	 * 
	 * @return strOutputPath
	 */
	public String getStrOutputPath() {
		return strOutputPath;
	}

	/**
	 * to set output directory path
	 * 
	 * @param strOutputPath
	 */
	public void setStrOutputPath(String strOutputPath) {
		this.strOutputPath = strOutputPath;
	}

	/**
	 * to get Theme ID
	 * 
	 * @return strSelThemeUID
	 */
	public String getStrSelThemeUID() {
		return strSelThemeUID;
	}

	/**
	 * to set Theme ID
	 * 
	 * @param strSelThemeUID
	 */
	public void setStrSelThemeUID(String strSelThemeUID) {
		this.strSelThemeUID = strSelThemeUID;
	}

	/**
	 * to get target platform version
	 * 
	 * @return strVer
	 */
	public String getStrVer() {
		return strVer;
	}

	/**
	 * to set target platform version
	 * 
	 * @param strVer
	 */
	public void setStrVer(String strVer) {
		this.strVer = strVer;
	}

	/**
	 * to get tdf file
	 * 
	 * @return tdfFile
	 */
	public File getTdfFile() {
		return tdfFile;
	}

	/**
	 * to set tdf file
	 * 
	 * @param tdfFile
	 */
	public void setTdfFile(File tdfFile) {
		this.tdfFile = tdfFile;
	}

	/**
	 * get whether its tdf ot not
	 * 
	 * @return isTpf
	 */
	public boolean isTpf() {
		return isTpf;
	}

	/**
	 * set isTdf
	 * 
	 * @param isTpf
	 */
	public void setTpf(boolean isTpf) {
		this.isTpf = isTpf;
	}

	/**
	 * return the target platform
	 * 
	 * @return
	 */
	public IPlatform getPlatform() {
		return platform;
	}

	/**
	 * sets the target platform
	 * 
	 * @param platform
	 */
	public void setPlatform(IPlatform platform) {
		this.platform = platform;
	}

	/**
	 * 
	 * @return
	 */
	public String getStrThemeName() {
		return strThemeName;
	}

	/**
	 * 
	 * @param strThemeName
	 */
	public void setStrThemeName(String strThemeName) {
		this.strThemeName = strThemeName;
	}

	/**
	 * 
	 * @return
	 */
	public String getStrCopyAllowed() {
		return strcopyAllowed;
	}

	/**
	 * 
	 * @param copyAllowed
	 */
	public void setStrCopyAllowed(String copyAllowed) {
		this.strcopyAllowed = copyAllowed;
	}

	public File getTdfFilePath() {
		return tdfFilePath;
	}

	public void setTdfFilePath(File tdfFilePath) {
		this.tdfFilePath = tdfFilePath;
	}
}
