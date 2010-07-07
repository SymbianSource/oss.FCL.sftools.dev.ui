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
package com.nokia.tools.theme.s60.internal.utilities;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.Part;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.s60.S60ThemeContent;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.nokia.tools.theme.s60.parser.ThemeWriter;

/**
 * Although a lof of the methods mentioned should be in the coresponding classes
 * for the reason of pertining the Theme studio source code unchanged routines
 * and methods for accessing theme studio data structures are implemented as
 * utilitiy class
 */
public class TSDataUtilities {
	private static List<String> bgElementIds;
	private static List<String> defaultStretchElementIds;

	static {
		bgElementIds = new ArrayList<String>();
		defaultStretchElementIds = new ArrayList<String>();

		InputStream is = null;
		try {
			is = FileUtils.getURL(S60ThemePlugin.getDefault(),
					"data/config/specialCases.properties").openStream();
			Properties p = new Properties();
			p.load(is);
			StringTokenizer bgEls = new StringTokenizer(p
					.getProperty("bgElements"), " ,");
			StringTokenizer stretchEls = new StringTokenizer(p
					.getProperty("defaultStretch"), " ,");
			while (bgEls.hasMoreTokens())
				bgElementIds.add(bgEls.nextToken());
			while (stretchEls.hasMoreTokens())
				defaultStretchElementIds.add(stretchEls.nextToken());
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		} finally {
			FileUtils.close(is);
		}
	}

	public static boolean isBackgroundElementId(String id) {
		return bgElementIds.contains(id);
	}

	public static boolean isTopOrNestedBackgroundElement(IContentData element) {
		if (!(element instanceof ThemeData)) {
			return false;
		}
		if (element.getId() == null) {
			return false;
		}

		if (isBackgroundElementId(element.getId())) {
			return true;
		}
		// content data may be created without parent
		Map<String, Set<String>> backgroundDependency = ((Theme) ((ThemeData) element)
				.getData().getRoot()).getBackgroundDependency();
		for (String bgId : bgElementIds) {
			Set<String> elems = backgroundDependency.get(bgId);
			if (elems != null && elems.contains(element.getId())) {
				return true;
			}
		}
		return false;
	}

	public static String getDefaultStretchMode(String elementId) {
		boolean stretch = defaultStretchElementIds.contains(elementId);
		return stretch ? IMediaConstants.STRETCHMODE_STRETCH
				: IMediaConstants.STRETCHMODE_ASPECT;
	}

	/**
	 * Saves colorized images as bitmaps. returns list of elements that were
	 * resolved
	 * 
	 * @param theme
	 */
	public static List<Element> resolveColorize(S60ThemeContent theme) {

		// remeber list of elements that were colorized, and EMF widgets needs
		// to be updated also
		List<Element> colorizedElements = new ArrayList<Element>();

		S60Theme skin2 = (S60Theme) ((S60ThemeContent) theme).getData();
		try {
			for (Element el : skin2.getAllElements().values()) {
				/* nine piece handle */
				if (el.getChildren() != null && el.getChildren().size() == 9) {
					if (ThemeTag.ATTR_9_PIECE.equals(el.getCurrentProperty())) {
						/*
						 * distributed colours settings to childs and process
						 * them
						 */
						String eProps[] = { BitmapProperties.COLORIZE,
								BitmapProperties.COLORIZE_SELECTED,
								BitmapProperties.OPTIMIZE_SELECTION,
								BitmapProperties.IS_OPTIMIZE_SELECTED,
								BitmapProperties.DITHER_SELECTED,
								BitmapProperties.COLOR };
						Map<Object, Object> xProps = new HashMap<Object, Object>();
						for (String k : eProps) {
							xProps.put(k, el.getAttribute().get(k));
						}
						for (Object child : el.getChildren()) {
							Part p = (Part) child;
							p.setAttribute(xProps);
						}
						for (Object child : el.getChildren()) {
							Part p = (Part) child;
							p.setAttribute(xProps);

							Boolean colourize = (Boolean) p.getAttribute().get(
									BitmapProperties.COLORIZE_SELECTED);
							Boolean optimize = (Boolean) p.getAttribute().get(
									BitmapProperties.IS_OPTIMIZE_SELECTED);
							if ((colourize != null && colourize)
									|| (optimize != null && optimize)) {
								ThemeWriter.saveAdjustedImage(p);

								// add to list
								if (!colorizedElements.contains(el))
									colorizedElements.add(el);

							}
							// clean attrs
							p.getAttribute().remove(
									BitmapProperties.IS_OPTIMIZE_SELECTED);
							p.getAttribute().remove(BitmapProperties.COLOR);
							p.getAttribute().remove(BitmapProperties.COLORIZE);
							p.getAttribute().remove(
									BitmapProperties.COLORIZE_SELECTED);
							p.getAttribute().remove(
									BitmapProperties.OPTIMIZE_SELECTION);
							p.getAttribute().remove(
									BitmapProperties.DITHER_SELECTED);
						}

						// clean attrs
						el.getAttribute().remove(
								BitmapProperties.IS_OPTIMIZE_SELECTED);
						el.getAttribute().remove(BitmapProperties.COLOR);
						el.getAttribute().remove(BitmapProperties.COLORIZE);
						el.getAttribute().remove(
								BitmapProperties.COLORIZE_SELECTED);
						el.getAttribute().remove(
								BitmapProperties.OPTIMIZE_SELECTION);
						el.getAttribute().remove(
								BitmapProperties.DITHER_SELECTED);

						continue;
					}
				}

				Boolean colourize = (Boolean) el.getAttribute().get(
						BitmapProperties.COLORIZE_SELECTED);
				Boolean optimize = (Boolean) el.getAttribute().get(
						BitmapProperties.IS_OPTIMIZE_SELECTED);

				if ((colourize != null && colourize)
						|| (optimize != null && optimize)) {
					ThemeWriter.saveAdjustedImage(el);
					// add to list
					if (!colorizedElements.contains(el))
						colorizedElements.add(el);
				}

				// clean attrs
				el.getAttribute().remove(BitmapProperties.IS_OPTIMIZE_SELECTED);
				el.getAttribute().remove(BitmapProperties.COLOR);
				el.getAttribute().remove(BitmapProperties.COLORIZE);
				el.getAttribute().remove(BitmapProperties.COLORIZE_SELECTED);
				el.getAttribute().remove(BitmapProperties.OPTIMIZE_SELECTION);
				el.getAttribute().remove(BitmapProperties.DITHER_SELECTED);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return colorizedElements;

	}
}
