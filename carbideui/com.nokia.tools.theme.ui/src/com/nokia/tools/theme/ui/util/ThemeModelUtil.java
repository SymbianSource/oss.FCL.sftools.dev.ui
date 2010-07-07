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
package com.nokia.tools.theme.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.extension.PluginEntry;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

/**
 * This class consists exclusively of useful static methods.
 * 
 * 
 */
public class ThemeModelUtil {
	public static final String ENTRY_URL_PROTOCOL = "bundleentry";

	/**
	 * Gets the list of all available theme model descriptors and returns a list
	 * of <code>IThemeModelDescriptor</code>.
	 * 
	 * @return <code>IThemeModelDescriptor</code>
	 */
	public static List<IThemeModelDescriptor> getAllThemeModelDescriptor() {
		final IContent content = getContent();
		if (content != null) {
			String themeId = (String) content
					.getAttribute(ContentAttribute.THEME_ID.name());
			String modelId = (String) content
					.getAttribute(ContentAttribute.MODEL.name());
			IThemeDescriptor desc = ThemePlatform
					.getThemeDescriptorById(themeId);
			if (desc != null) {
				String containerId = desc.getContainerId();
				List<IThemeModelDescriptor> items = new ArrayList<IThemeModelDescriptor>();
				boolean isModelEditor = isModelEditor();
				Set<String> modelIds = new HashSet<String>();
				for (IThemeModelDescriptor modelDesc : ThemePlatform
						.getThemeModelDescriptorsByContainer(containerId)) {
					if ((!isModelEditor || modelId.equalsIgnoreCase(modelDesc
							.getId()))
							&& !modelIds.contains(modelDesc.getId())
							&& !isFromPluginProject(modelDesc)) {
						items.add(modelDesc);
						modelIds.add(modelDesc.getId());
					}
				}
				Collections.sort(items,
						new Comparator<IThemeModelDescriptor>() {
							public int compare(IThemeModelDescriptor o1,
									IThemeModelDescriptor o2) {
								return o1.getName().compareToIgnoreCase(
										o2.getName());
							}
						});
				return items;
			}
		}
		return Collections.emptyList();
	}

	public static IContent getContent() {
		IWorkbenchPart part = getEditorPart();
		if (part == null) {
			return null;
		}
		IContentAdapter adapter = (IContentAdapter) part
				.getAdapter(IContentAdapter.class);
		if (adapter == null) {
			return null;
		}
		return ScreenUtil.getPrimaryContent(adapter.getContents());
	}

	public static IEditorPart getEditorPart() {
		return EclipseUtils.getActiveSafeEditor();
	}

	private static boolean isModelEditor() {
		IEditorPart editor = getEditorPart();
		return editor != null
				&& editor.getEditorInput() instanceof IFileEditorInput
				&& PluginEntry.PLUGIN_XML.equals(((IFileEditorInput) editor
						.getEditorInput()).getFile().getName());
	}

	private static boolean isFromPluginProject(IThemeModelDescriptor descriptor) {
		return !descriptor.getModelPath().toExternalForm().startsWith(
				ThemeModelUtil.ENTRY_URL_PROTOCOL);
	}

}
