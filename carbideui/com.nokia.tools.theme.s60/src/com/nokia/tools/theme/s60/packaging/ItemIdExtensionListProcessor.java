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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.s60.model.S60Theme;

/**
 * 
 * This class creates id extension txt file. which needs to be
 *         passed to aknskinDesccompiler.exe along with skin descriptor file
 */
public class ItemIdExtensionListProcessor extends AbstractS60PackagingProcessor {
	private Document xml;

	private FileWriter fileWriter = null;

	private PrintWriter iconidfile = null;

	File file = null;

	final MessageFormat FORMAT = new MessageFormat(
			"IID \"KAknsIID{0}\" {1} {2}");

	@Override
	protected Object processSpi() throws PackagingException {
		String fileName = getWorkingDir() + "\\thirdpartyiconid.txt";
		try {

			file = new File(fileName);
			fileWriter = new FileWriter(file);
			iconidfile = new PrintWriter(fileWriter);

			IContent theme = (IContent) context
					.getAttribute(PackagingAttribute.theme.name());

			S60Theme skin = (S60Theme) ((ThemeContent) theme).getData();

			List<ThemeBasicData> icons = extractCustomIcons(context, skin);

			boolean isIdFileEmpty = true;
			for (ThemeBasicData icon : icons) {
				iconidfile.println(new String(FORMAT.format(new Object[] {
						icon.getId().replaceAll("_", ""),
						icon.getAttributeValue(ThemeTag.ATTR_MAJORID),
						icon.getAttributeValue(ThemeTag.ATTR_MINORID) })));

				isIdFileEmpty = false;
			}
			if (!isIdFileEmpty)
				context.setAttribute(PackagingAttribute.themeItemListFile
						.name(), fileName);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fileWriter.close();
				iconidfile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return context.getInput();
	}

	private List<ThemeBasicData> extractCustomIcons(PackagingContext context,
			ThemeBasicData themeData) {
		List<ThemeBasicData> toRet = new ArrayList<ThemeBasicData>();

		if (!themeData.hasChildNodes())
			return toRet;

		// Get the list of tasks and the keys from the specialTasks map
		List children = themeData.getChildren();

		for (int i = 0; i < children.size(); i++) {
			ThemeBasicData item = (ThemeBasicData) (children.get(i));
			if (item instanceof SkinnableEntity) {
				SkinnableEntity entity = (SkinnableEntity) item;
				String majorId = entity
						.getAttributeValue(ThemeTag.ATTR_MAJORID);
				String minorId = entity
						.getAttributeValue(ThemeTag.ATTR_MINORID);

				if (majorId != null && minorId != null) {
					toRet.add(entity);
				}

				String colorGroupId = entity
						.getAttributeValue(ThemeTag.ATTR_COLOUR_GROUP_ID);
				String colorGroupMajorId = entity
						.getAttributeValue(ThemeTag.ATTR_COLOUR_GROUP_MAJOR_ID);
				String colorGroupMinorId = entity
						.getAttributeValue(ThemeTag.ATTR_COLOUR_GROUP_MINOR_ID);
				if (colorGroupId != null && colorGroupMajorId != null
						&& colorGroupMinorId != null) {
					Element tbd = new Element(colorGroupId);
					tbd.setAttribute(ThemeTag.ATTR_ID, colorGroupId);
					tbd.setAttribute(ThemeTag.ATTR_MAJORID, colorGroupMajorId);
					tbd.setAttribute(ThemeTag.ATTR_MINORID, colorGroupMinorId);
					toRet.add(tbd);
				}
			}

			toRet.addAll(extractCustomIcons(context, item));
		}

		return toRet;
	}
}
