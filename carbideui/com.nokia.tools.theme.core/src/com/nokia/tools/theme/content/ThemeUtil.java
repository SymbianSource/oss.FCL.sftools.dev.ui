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

package com.nokia.tools.theme.content;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorPart;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentStructureAdapter;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.IThemeManager;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.PrologBackend;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.theme.core.Activator;

/**
 * A simple utility class for performing theme related operations 
 * and fetching the theme related data.
 */
public class ThemeUtil {

	public static final Object UPDATE_SCREEN_JOB_FAMILY = Color.CYAN;

	public final static String TAG_THEME_FOLDER = "folder";

	public final static String TAG_THEME_NAME = "name";

	public final static String TAG_THEME_RESOLUTION = "resolution";

	public final static String TAG_THEME_PLATFORM = "platform";

	public final static String TAG_THEME_TEMPLATE = "template";

	private static final String INVALID_DIR = ".classpath";

	/**
	 * Creation of the theme based on input parameters Created theme is saved to
	 * file system to the path: under themeRootFolder subfolder with theme name
	 * is created or if already used, unique dir is created to store the new
	 * theme contents
	 * 
	 * @param themeName
	 * @param themeRootFolder
	 * @param release
	 * @param resolution
	 * @return created S60 theme object
	 */
	public static Theme createTheme(Map<String, Object> creationData,
	    String newName, IProgressMonitor monitor) throws Exception {
		String themeName = (String) creationData.get(TAG_THEME_NAME);
		String themeRootFolder = (String) creationData.get(TAG_THEME_FOLDER);
		String release = (String) creationData.get(TAG_THEME_PLATFORM);
		String resolution = (String) creationData.get(TAG_THEME_RESOLUTION);
		String template = (String) creationData.get(TAG_THEME_TEMPLATE);

		monitor.worked(10);

		String themeDir = themeRootFolder + File.separator + themeName;


		IThemeModelDescriptor descriptor = ThemePlatform
		    .getThemeModelDescriptorById(release);

		Theme theme;
		if (null == template) {
			theme = ThemePlatform.getThemeManagerByThemeModelId(
			    descriptor.getId()).createTheme(descriptor.getId(), monitor);


			if (newName == null) {
				newName = themeName + ThemeTag.SKN_FILE_EXTN;
			}
			theme.setThemeFile(new File(themeDir + File.separator + newName));
			String[] res = resolution.split("x");
			int width = Integer.parseInt(res[0]);
			int height = Integer.parseInt(res[1]);
			theme.setDisplay(new com.nokia.tools.platform.core.Display(width,
			    height));
			DefaultTreeModel prologModel = new DefaultTreeModel(PrologBackend
			    .getRootNode(theme));
			DefaultMutableTreeNode prologNode = (DefaultMutableTreeNode) prologModel
			    .getRoot();
			// selects all tasks by default
			PrologBackend.setSelectionForAll(prologNode, true);
			PrologBackend.setTransferSelection(prologNode,
			    ThemeConstants.PROLOG);
		} else {
			File originalFile = new File(template);
			File resultingDir = new File(themeDir);

			if (newName == null) {
				newName = resultingDir.getName() + "."
				    + FileUtils.getExtension(originalFile);
			}
			theme = copyTheme(originalFile, resultingDir, descriptor
			    .getThemeDescriptor().getContainerId(), newName, monitor);
		}

		theme.setThemeName(themeName);
		theme.setPackage(themeName);

		if (!theme.getThemeFile().exists()) {
			theme.setAttribute(ThemeTag.ATTR_AUTHOR, "Author");
		}

		if (null != theme) {
			File themeFile = theme.getThemeFile();
			File dir = convertDir(themeFile.getParentFile());
			themeFile = new File(dir, themeFile.getName());
			theme.setThemeFile(themeFile);
			themeFile.createNewFile();
			theme.save(monitor);
		}
		return theme;
	}

	private static File convertDir(File dir) {
		if (dir.getName().equals(INVALID_DIR)) {
			dir = new File(dir.getParentFile(), dir.getName() + "x");
		}
		while (dir.isFile()) {
			dir = new File(dir.getParentFile(), dir.getName() + "x");
		}
		dir.mkdirs();
		return dir;
	}

	public static Theme openThemeFromFile(File file, String containerId,
	    IProgressMonitor monitor) throws ThemeException {
		IThemeManager manager = ThemePlatform
		    .getThemeManagerByContainerId(containerId);
		return manager.openTheme(file, monitor);
	}

	public static Theme copyTheme(File originalThemeFile, File dir,
	    String containerId, String newName, IProgressMonitor monitor)
	    throws ThemeException {
		File dstDir = convertDir(dir);
		try {
			FileUtils.copyDir(new Path(originalThemeFile.getAbsolutePath())
			    .removeLastSegments(1).toFile(), dstDir);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String oldName = originalThemeFile.getName();
		boolean renamed = new File(dstDir, oldName).renameTo(new File(dstDir,
		    newName));
		if (!renamed) {
			Activator.error("Rename file: " + oldName + " to " + newName
			    + " failed.");
		}

		monitor.worked(10);

		return openThemeFromFile(new File(new Path(dstDir.getAbsolutePath())
		    .append(new Path(renamed ? newName : oldName).lastSegment())
		    .toString()), containerId, monitor);
	}

	public static Theme getDefaultModel(String containerId,
	    IProgressMonitor monitor) {
		IThemeModelDescriptor descriptor = ThemePlatform
		    .getDefaultThemeModelDescriptor(containerId);
		if (descriptor != null) {
			try {
				return (Theme) ThemePlatform.getThemeManagerByContainerId(
				    containerId).getModel(descriptor.getId(), monitor);
			} catch (ThemeException e) {
				Activator.error(e);
			}
		}
		return null;
	}

	public static List<Theme> getAllLoadedModels(){
		List<Theme> list = new ArrayList<Theme>();
		IThemeModelDescriptor[] themeModelDescriptors = ThemePlatform.getThemeModelDescriptors();
		for(IThemeModelDescriptor themeModelDescriptor: themeModelDescriptors){
			IThemeManager themeManager = ThemePlatform.getThemeManagerByThemeModelId(themeModelDescriptor.getId());
			if(themeManager != null){
				Theme model = themeManager.getLoadedModel(themeModelDescriptor.getId());
				if(model != null){
					list.add(model);
				}
			}
		}
		return list;
	}
	
	public static String getDefaultUrl(String containerId, String fileName) {
		if (fileName == null) {
			return null;
		}
		try {
			File baseDir = new File(getDefaultModel(containerId, null)
			    .getThemeDir());
			return FileUtils.toURL(new File(baseDir, fileName)).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return fileName;
		}
	}

	public static Rectangle getLayoutRect(Object component) {

		Rectangle highLightRect = new Rectangle(0, 0, 0, 0);
		double minLeftOffset = Integer.MAX_VALUE;
		double minTopOffset = Integer.MAX_VALUE;
		double maxRightOffset = Integer.MIN_VALUE;
		double maxBottomOffset = Integer.MIN_VALUE;

		Element element;
		ThemeBasicData comp = (ThemeBasicData) component;

		List elementInfo = comp.getChildren();

		Layout layoutInfo[] = new Layout[elementInfo.size()];

		for (int i = 0; i < elementInfo.size(); i++) {
			element = (Element) elementInfo.get(i);
			try {
				if (element.isShown())
					layoutInfo[i] = element.getLayoutInfo();
			} catch (ThemeException ex) {
				ex.printStackTrace();
				minLeftOffset = minTopOffset = 0;
				maxBottomOffset = maxRightOffset = 1;
			}

			if ((layoutInfo[i] != null) && (element.isShown())) {
				minLeftOffset = Math.min(minLeftOffset, layoutInfo[i].L());
				minTopOffset = Math.min(minTopOffset, layoutInfo[i].T());
				maxRightOffset = Math.max(maxRightOffset,
				    (layoutInfo[i].W() + layoutInfo[i].L()));
				maxBottomOffset = Math.max(maxBottomOffset,
				    (layoutInfo[i].H() + layoutInfo[i].T()));
			} else {
				if (element.hasChildNodes()) {
					Rectangle elementRect = null;
					try {
						elementRect = Element.getElementLayout(element);
					} catch (Exception e) {
					}
					minLeftOffset = Math.min(minLeftOffset, (int) elementRect
					    .getX());
					minTopOffset = Math.min(minTopOffset, (int) elementRect
					    .getY());
					maxRightOffset = Math.max(maxRightOffset,
					    (int) (minLeftOffset + elementRect.getWidth()));
					maxBottomOffset = Math.max(maxBottomOffset,
					    (int) (minTopOffset + elementRect.getHeight()));
				}
			}

		}
		highLightRect.x = (int) minLeftOffset;
		highLightRect.y = (int) minTopOffset;
		highLightRect.width = (int) (maxRightOffset - minLeftOffset);
		highLightRect.height = (int) (maxBottomOffset - minTopOffset);
		return highLightRect;
	}

	public static Rectangle getResolution(Object theme) {
		Theme theme1 = (Theme) theme;
		com.nokia.tools.platform.core.Display disp = theme1.getDisplay();
		return new Rectangle(disp.getWidth(), disp.getHeight());
	}

	/**
	 * @param themeGraphics ThemeGraphics runtime object or list of theme
	 *            graphics
	 */
	public static void markAsChanged(Object themeGraphics) {
		if (themeGraphics instanceof ThemeGraphic) {
			ThemeGraphic gr = (ThemeGraphic) themeGraphics;
			for (Object l : gr.getImageLayers()) {
				ImageLayer layer = (ImageLayer) l;
				if (layer.hasImage())
					layer.setAttribute(ThemeTag.ATTR_TMP_IMAGE, "true");
				if (layer.getAttribute(ThemeTag.ATTR_SOFTMASK) != null
				    || layer.getAttribute(ThemeTag.ATTR_HARDMASK) != null)
					layer.setAttribute(ThemeTag.ATTR_TMP_MASK_IMAGE, "true");
			}
		} else if (themeGraphics instanceof List) {
			for (Object content : ((List) themeGraphics)) {
				markAsChanged(content);
			}
		}
	}

	/**
	 * Changes the each first letter of a word to upper case and rest of the
	 * strings to lower case
	 * 
	 * @param name name to be modified.
	 * @return the string in the title case.
	 */
	public static String toTitleCase(String name) {
		if (name == null) {
			return null;
		}
		String result = name.substring(0, 1).toUpperCase();
		for (int i = 1; i < name.length(); i++) {
			if (name.substring(i - 1, i).contains(" ")
			    && (name.length() > i + 4)
			    && !name.substring(i, i + 4).equalsIgnoreCase("and "))
				result = result + name.substring(i, i + 1).toUpperCase();
			else
				result = result + name.substring(i, i + 1).toLowerCase();
		}
		return result;
	}

	/*
	 * Note that this method uses the eclipseutils safe editor. So the String
	 * returned would be based on the active editor opened. @return String the
	 * type of the theme
	 */
	public String getCurrentActiveThemeName() {
		IContent content = (IContent) EclipseUtils.getActiveSafeEditor()
		    .getAdapter(IContent.class);

		if (content instanceof ThemeData) {
			ThemeData themeData = (ThemeData) content;
			return themeData.getRoot().getType();

		}
		return "";
	}

	public static Theme getCurrentActiveTheme() {
		IEditorPart activeSafeEditor = EclipseUtils.getActiveSafeEditor();
		if (null == activeSafeEditor)
			return null;
		IContent content = (IContent) activeSafeEditor
		    .getAdapter(IContent.class);

		if (content instanceof ThemeData) {
			ThemeData themeData = (ThemeData) content;
			return (Theme) themeData.getData().getRoot();

		}
		return null;
	}

	/**
	 * Returns only the valid children.
	 * 
	 * @param element the element from where the children are queried.
	 * @return the valid children.
	 */
	public static IContentData[] getValidChildren(IContentData element) {
		IContentStructureAdapter adapter = (IContentStructureAdapter) element
		    .getAdapter(IContentStructureAdapter.class);
		return adapter == null ? element.getChildren() : adapter.getChildren();
	}
}
