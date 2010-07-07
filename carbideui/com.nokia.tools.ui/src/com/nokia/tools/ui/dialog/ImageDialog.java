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

package com.nokia.tools.ui.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for getting the resource /image. 
 * This class was created by refactoring ImageUtil in package com.nokia.tools.uision.property.ui.util;
 * 
 */
public class ImageDialog {
	public static final int PAGE_FILE = 1;
	public static final int PAGE_QRC = 1 << 1;

	public static String IMAGE_DIR = "images";

	protected static final int MAX_RECENT_SAVE = 21;

	protected ImageDialog() {
	}

	/**
	 * Opens the resource selection dialog to select a single resource.
	 * 
	 * @param pages bitwise or-ed pages.
	 * @return the selected resource
	 */
	public static Object selectResource(int pages, Object pageData) {
		Object[] resources = selectResources(pages, pageData);
		if (resources != null && resources.length > 0) {
			return resources[0];
		}
		return null;
	}

	/**
	 * Opens the resource selection dialog to select resource.
	 * 
	 * @param pages bitwise or-ed pages.
	 * @return the selected resources
	 */
	public static Object[] selectResources(int pages, Object pageData) {
		List<IResourceSelectionPage> list = new ArrayList<IResourceSelectionPage>();
		if ((PAGE_FILE & pages) != 0) {
			final PathHandlingConfig data = PathHandlingConfig.load();
			FileResourceSelectionPage filePage = new FileResourceSelectionPage(false) {
				protected void updateRecentList(String newPath) {
					if (data.recentPathList.indexOf(newPath) == 0 || data.usePredefined)
						return;
					if (newPath.endsWith(File.separator) && newPath.length() > 3)
						newPath = newPath.substring(0, newPath.length() - 1);
					int i = 0;
					for (String p : data.recentPathList.toArray(new String[0])) {
						if (p.equalsIgnoreCase(newPath))
							data.recentPathList.remove(i);
						i++;
					}
					data.recentPathList.add(0, newPath);
					if (data.recentPathList.size() > MAX_RECENT_SAVE)
						data.recentPathList.remove(MAX_RECENT_SAVE);
					data.saveRecentPathList();
					updatePaths();
					txtFolder.setText(txtFolder.getItem(0));
				}

				protected void updatePaths() {
					if (data.usePredefined)
						txtFolder.setItems(data.predefinedPathList.toArray(new String[0]));
					else {
						String[] allItems = data.recentPathList.toArray(new String[0]);
						if (allItems.length > 0) {
							List<String> selectedItems = new ArrayList<String>();
							int l = allItems.length;
							if (data.recentCount < allItems.length)
								l = data.recentCount;
							for (int i = 0; i < l; i++)
								selectedItems.add(allItems[i]);
							txtFolder.setItems(selectedItems.toArray(new String[0]));
						}
					}
				}
			};
			filePage.setTitle(Messages.ResourceSelectionDialog_Title);
			filePage.setContentProvider(new ImageFileContentProvider());
			list.add(filePage);
		}

		if (list.isEmpty()) {
			return null;
		}
		ResourceSelectionDialog dialog = new ResourceSelectionDialog(Display.getDefault().getActiveShell(), list
		    .toArray(new IResourceSelectionPage[list.size()]), Messages.ResourceSelectionDialog_Banner_Message,
		    Messages.ResourceSelectionDialog_Banner_Title, true);

		if (Dialog.OK == dialog.open()) {
			return dialog.getResources();
		}
		return null;
	}

}
