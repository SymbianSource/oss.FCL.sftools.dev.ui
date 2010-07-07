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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.PackagingMessages;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.layout.LayoutContext;
import com.nokia.tools.platform.theme.ILayoutContextProvider;
import com.nokia.tools.platform.theme.IThemeManager;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.s60.IThemeConstants;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.nokia.tools.theme.s60.siscreation.ThemeTextFileWriter;


public class ThemeDescriptionProcessor
    extends AbstractS60PackagingProcessor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.packaging.IPackagingProcessor.AbstractPackagingProcessor#processSpi()
	 */
	@Override
	protected Object processSpi() throws PackagingException {
		IContent theme = (IContent) context.getInput();
		if (theme == null) {
			throw new PackagingException(PackagingMessages.Error_themeMissing);
		}

		String workingDir = getWorkingDir();

		S60Theme skin = (S60Theme) ((ThemeContent) theme).getData();

		String packageName = skin.getPackage();

		context.setAttribute(PackagingAttribute.themePackageName.name(),
		    packageName);
		context.setAttribute(PackagingAttribute.themeName.name(), skin
		    .getThemeName());

		if (new Boolean((String) context
		    .getAttribute(PackagingAttribute.signPackage.name()))) {
			context.setAttribute(PackagingAttribute.sisTempFile.name(),
			    new File(workingDir, packageName + ".sis").getAbsolutePath());
		}

		generateDescriptor(skin, workingDir);
		generateItemIdFile(skin, workingDir);

		return packageName + ".txt";
	}

	protected void generateDescriptor(final S60Theme skin, String workingDir) throws PackagingException {
		ThemeTag.SkinCompliance skinCreationMode = ThemeTag.SkinCompliance.SCALEABLE_SKIN;
		// overrides the layout context handling by querying for all available
		// layouts
		skin.setLayoutContextProvider(new ILayoutContextProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.platform.theme.ILayoutContextProvider#getLayoutContext(com.nokia.tools.platform.core.Display)
			 */
			public LayoutContext getLayoutContext(Display display) {
				LayoutContext currentContext = skin.getModel()
				    .getLayoutContext(display);
				if (currentContext != null) {
					return currentContext;
				}
				IThemeModelDescriptor modelDescriptor = ThemePlatform
				    .getThemeModelDescriptorById(skin.getModelId());
				String containerId = modelDescriptor.getThemeDescriptor()
				    .getContainerId();
				for (IThemeModelDescriptor descriptor : ThemePlatform
				    .getThemeModelDescriptors()) {
					if (skin.getModelId().equalsIgnoreCase(descriptor.getId())) {
						// current model
						continue;
					}
					if (containerId.equalsIgnoreCase(descriptor
					    .getThemeDescriptor().getContainerId())) {
						// only query when the container is the same
						IThemeManager manager = ThemePlatform
						    .getThemeManagerByThemeModelId(descriptor.getId());
						if (manager != null) {
							try {
								Theme model = manager.getModel(descriptor
								    .getId(), null);
								LayoutContext context = model
								    .getLayoutContext(display);
								if (context != null) {
									return context;
								}
							} catch (Exception e) {
								S60ThemePlugin.error(e);
							}
						}
					}
				}
				return null;
			}
		});

		HashMap<Object, Object> hm = new HashMap<Object, Object>();

		hm.put(ThemeTag.KEY_SKIN_COMPLIANCE, skinCreationMode);

		if ("true".equalsIgnoreCase(skin
		    .getAttributeValue(ThemeTag.BITS_PIXEL_SUPPORT))) {
			hm.put(ThemeTag.ATTR_COLOURDEPTH, "c24");
		} else {
			hm.put(ThemeTag.ATTR_COLOURDEPTH, "c16");
		}

		// Selecting the phone model's corresponding special file processor
		String specialProcessorClassName = "com.nokia.tools.theme.s60.siscreation.SpecialEntitiesHandlerForS6050";
		hm.put(ThemeTag.KEY_SKIN_SPLENTITY_CLASSNAME, specialProcessorClassName);
		
		ThemeTextFileWriter writer = null;
		try {
			writer = new ThemeTextFileWriter(hm);
			writer.setThemeObject(skin);
			writer.setOutputDir(workingDir);
			writer.generateDescriptorFile();
		} catch (Exception e) {
			throw new PackagingException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
				}
			}
			skin.setLayoutContextProvider(null);
		}
	}

	private void generateItemIdFile(S60Theme skin, String workingDir)
	    throws PackagingException {
		List itemIdFiles = skin.getItemIdFiles();

		if (itemIdFiles == null || itemIdFiles.isEmpty()) {
			return;
		}

		List<String> ids = new ArrayList<String>();

		for (Object obj : itemIdFiles) {
			File f = new File((String) obj);
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(f));
				String text;
				while ((text = in.readLine()) != null) {
					text = text.trim();
					String[] result = text.split(" ");

					if (result.length == 4) {
						ids.add(text);
					}
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
			}
		}

		PrintWriter out = null;
		try {
			File f = new File(workingDir, ThemeTag.ITEMIDLIST);
			out = new PrintWriter(new FileWriter(f));

			for (String ss : ids) {
				out.println(ss);
			}
			context.setAttribute(PackagingAttribute.themeItemListFile.name(), f
			    .getAbsolutePath());
		} catch (Exception e) {
			throw new PackagingException(e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
			}
		}
	}

}
