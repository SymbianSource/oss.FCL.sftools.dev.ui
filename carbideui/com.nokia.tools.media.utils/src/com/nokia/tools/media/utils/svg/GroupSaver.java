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
package com.nokia.tools.media.utils.svg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.resource.util.FileUtils;

public class GroupSaver {

	public void save(List<ColorGroup> colorGroups, String filePath) {

		if (colorGroups != null) {

			String groupsFilePath = filePath + File.separator
					+ GroupsLoader.DOC_PATH;
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new FileWriter(groupsFilePath));
				out.write(startTagWrap(GroupsLoader.COLOR_GROUPS_TAG) + "\n");
				for (ColorGroup group : colorGroups) {
					/*
					 * save empty groups too
					 */
					// if(group.isEmpty()){
					// continue;
					// }
					out
							.write(startTagWrap(GroupsLoader.COLOR_GROUP_TAG)
									+ "\n");
					out.write(startTagWrap(GroupsLoader.COLOR_GROUP_NAME)
							+ group.getName()
							+ endTagWrap(GroupsLoader.COLOR_GROUP_NAME) + "\n");
					out
							.write(startTagWrap(GroupsLoader.COLOR_GROUP_COLOR)
									+ "\n"
									+ ColorUtil.asHashString(group
											.getGroupColor())
									+ endTagWrap(GroupsLoader.COLOR_GROUP_COLOR)
									+ "\n");
					for (ColorGroupItem item : group.getGroupItems()) {
						out
								.write(startTagWrap(GroupsLoader.COLOR_GROUP_ITEM_TAG)
										+ "\n");
						out.write(startTagWrap(GroupsLoader.ITEM_ID_TAG)
								+ item.getItemId()
								+ endTagWrap(GroupsLoader.ITEM_ID_TAG) + "\n");
						if (item.getItemPartId() != null
								&& !"".equals(item.getItemPartId())) {
							out
									.write(startTagWrap(GroupsLoader.ITEM_PART_ID_TAG)
											+ item.getItemPartId()
											+ endTagWrap(GroupsLoader.ITEM_PART_ID_TAG)
											+ "\n");
						}
						if (item.getImagePartOrLayer() != null
								&& !"".equals(item.getImagePartOrLayer())) {
							out
									.write(startTagWrap(GroupsLoader.IMAGE_PART_OR_LAYER_TAG)
											+ item.getImagePartOrLayer()
											+ endTagWrap(GroupsLoader.IMAGE_PART_OR_LAYER_TAG)
											+ "\n");
						}
						out.write(endTagWrap(GroupsLoader.COLOR_GROUP_ITEM_TAG)
								+ "\n");
					}
					if (group.hasChildrenGroup()) {
						for (String groupName : group.getChildrenGroups()) {
							out.write(startTagWrap(GroupsLoader.CHILD_GROUP));
							out.write(groupName);
							out.write(endTagWrap(GroupsLoader.CHILD_GROUP)
									+ "\n");
						}
					}

					if (group.hasParent()) {
						out.write(startTagWrap(GroupsLoader.PARENT_GROUP));
						out.write(group.getParentGroupName());
						out.write(endTagWrap(GroupsLoader.PARENT_GROUP) + "\n");
					}

					out.write(endTagWrap(GroupsLoader.COLOR_GROUP_TAG) + "\n");
				}
				out.write(endTagWrap(GroupsLoader.COLOR_GROUPS_TAG) + "\n");
				out.flush();
			} catch (IOException e) {
				UtilsPlugin.error(e);
			} finally {
				FileUtils.close(out);
			}
		}

	}

	private String startTagWrap(String startTagToWrap) {
		return "<" + startTagToWrap + ">";
	}

	private String endTagWrap(String endTagToWrap) {
		return "</" + endTagToWrap + ">";
	}

}