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
/*
 * File Name S60Theme.java Description File contains the class to hold a
 * complete information of the skin design for the Series 60 Skins Creation
 * Application project. 
 */

package com.nokia.tools.theme.s60.model;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.platform.theme.ILayeredImageCompositor;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.s60.general.PlatformSupportInfo;
import com.nokia.tools.theme.s60.model.tpi.DefinedIcons;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconLoadException;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconManager;
import com.nokia.tools.theme.s60.parser.ThemeWriter;


public class S60Theme
    extends Theme {

	private static final String ICONS_XML = "icons.xml";
	
	/**
	 * Represents the list of Third Party Icons.
	 */
	
	private DefinedIcons themeSpecificThirdPartyIcons = new DefinedIcons();
	
	private static final String[] PROPERTIES = { BitmapProperties.COLOR,
	    BitmapProperties.COLORIZE, BitmapProperties.COLORIZE_SELECTED,
	    BitmapProperties.DITHER_SELECTED,
	    BitmapProperties.IS_OPTIMIZE_SELECTED,
	    BitmapProperties.OPTIMIZE_SELECTION };

	// hold the default animation information
	private S60ThemeAnimation skinAnimation = null;

	public S60Theme(String themeId) {
		super(themeId);
	}

	public S60Theme(Theme model) {
		super(model);
	}

	/**
	 * Method to set the skinAnimationobject
	 * 
	 * @param skinAnimation ThemeAnimation object which holds the animation
	 *            information
	 */
	public void setSkinAnimation(S60ThemeAnimation skinAnimation)
	    throws ThemeException {
		if (this.skinAnimation == null) {
			this.skinAnimation = skinAnimation;
			return;
		}
		this.skinAnimation.update(skinAnimation);
		return;
	}

	public S60ThemeAnimation getSkinAnimation() {
		return this.skinAnimation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.Theme#save(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void save(IProgressMonitor monitor) throws ThemeException {
		ThemeWriter writer = new ThemeWriter(this, monitor);
		writer.init();
		try {
			writer.createXMLFromS60Skin();
		} catch (ThemeException e) {
			throw e;
		} catch (Exception e) {
			throw new ThemeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.Theme#createGraphic(com.nokia.tools.platform.theme.SkinnableEntity)
	 */
	@Override
	public ThemeGraphic createGraphic(SkinnableEntity entity) {
		if ((entity.getToolBox() != null)
		    && (entity.getToolBox().multipleLayersSupport)) {
			return new MorphedGraphic(entity);
		}
		return super.createGraphic(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.Theme#getLayeredImageCompositor(com.nokia.tools.platform.theme.ThemeGraphic)
	 */
	@Override
	protected ILayeredImageCompositor getLayeredImageCompositor(
	    ThemeGraphic graphic) {
		return new S60LayeredImageCompositor(graphic);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.Theme#supportsPlatform(com.nokia.tools.platform.core.IPlatform,
	 *      java.lang.String)
	 */
	@Override
	public boolean supportsPlatform(IPlatform platform, String skinId) {
		return PlatformSupportInfo.isPlatformSupported(skinId, platform);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.Theme#saveAs(java.lang.String,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void saveAs(String newFolderName, IProgressMonitor monitor)
	    throws ThemeException {
		File srcDir = getThemeFile().getParentFile();
		File[] srcFiles = srcDir.listFiles(new FileFilter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.io.FileFilter#accept(java.io.File)
			 */
			public boolean accept(File pathname) {
				return pathname.isFile()
				    && !pathname.getName().toLowerCase().endsWith(
				        ThemeTag.SKN_FILE_EXTN);
			}
		});

		monitor.worked(10);

		File projectFolder = new File(newFolderName);
		String themeName = getThemeName();
		if (themeName == null) {
			themeName = projectFolder.getName();
		}
		File tgtDir = new File(projectFolder, themeName);
		if (!tgtDir.exists()) {
			tgtDir.mkdirs();
		}

		// copy existing actual image files to the new directory
		for (File f : srcFiles) {
			try {
				FileUtils.copyFile(f, new File(tgtDir, f.getName()));
			} catch (IOException e) {
				throw new ThemeException(e);
			}
			monitor.worked(10);
		}
		setThemeFile(new File(tgtDir, themeName + ThemeTag.SKN_FILE_EXTN));

		setThemeName(themeName);
		setPackage(themeName);
		save(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.Theme#merge(com.nokia.tools.platform.theme.SkinnableEntity,
	 *      com.nokia.tools.platform.theme.SkinnableEntity)
	 */
	@Override
	protected void merge(SkinnableEntity oldEntity, SkinnableEntity newEntity) {
		Map<Object, Object> attributes = oldEntity.getAttribute();
		Map<Object, Object> map = new HashMap<Object, Object>();

		for (String property : PROPERTIES) {
			Object value = attributes.get(property);
			if (value != null) {
				map.put(property, value);
			}
		}
		if (!map.isEmpty()) {
			newEntity.setAttribute(map);
		}
	}

	public boolean containsThemeSpecificThirdPartyIcons() {
		String iconsDotXmlPath = themeSpecificThirdPartyIconsPath();
		return new File(iconsDotXmlPath).exists();
	}

	protected void performPlatformSpecificMerge() throws ThemeException{
		try{
			ThirdPartyIconManager.loadThemeSpecificIcons(this);
			ThirdPartyIconManager.loadToolSpecificThirdPartyIcons(this);
			refreshElementList();
		}
		catch(ThirdPartyIconLoadException e){
			throw new ThemeException(e);
		}
	}
	
	public String themeSpecificThirdPartyIconsPath() {
		return getThemeDir() + File.separatorChar + ICONS_XML;
	}

	public URL themeSpecificThirdPartyIconsUrl() {
		File iconsDotXml = new File(themeSpecificThirdPartyIconsPath());
		if (iconsDotXml.exists()) {
			try {
				return iconsDotXml.toURI().toURL();
			} catch (MalformedURLException e) {
				return null;
			}
		}
		return null;
	}

	public DefinedIcons getThemeSpecificThirdPartyIcons(){
		return themeSpecificThirdPartyIcons;
	}
	
	public void setThemeSpecificThirdPartyIcons(DefinedIcons themeSpecificThirdPartyIcons){
		if(themeSpecificThirdPartyIcons != null){
			this.themeSpecificThirdPartyIcons = themeSpecificThirdPartyIcons;
		}
	}
	
}