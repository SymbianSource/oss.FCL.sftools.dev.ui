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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.graphics.RGB;

public class ColorGroups extends Observable implements Observer {
	public static final Integer GROUP_ADDED = 0;

	public static final Integer GROUP_REMOVED = 1;

	public static final Integer ITEM_ADDED = 2;

	public static final Integer ITEM_REMOVED = 3;

	public static final Integer NAME_CHANGED = 4;

	private List<ColorGroup> colorGroups = new ArrayList<ColorGroup>();

	private String themeName = "";

	public ColorGroups(String name) {
		if (name != null) {
			themeName = name;
		}
	}

	public boolean addGroup(ColorGroup group) {
		if (group.getName() == null || group.getName().equals("")) {
			return false; // group name cannot be null or empty
		}
		for (ColorGroup existingGroup : colorGroups) {
			if (group.getName().equals(existingGroup.getName())) {
				return true; // return true as group exists
			}

		}
		colorGroups.add(group);

		setChanged();
		notifyObservers(ColorGroups.GROUP_ADDED);
		return true;
		// saveGroups();

	}

	public boolean addGroup(String name, RGB rgb) {
		if (rgb != null) {
			ColorGroup grp = new ColorGroup(rgb, name, this);
			// setChanged();
			// notifyObservers();
			return addGroup(grp);

		}
		return false;
	}

	public boolean modifyGroupName(String oldName, String newName) {
		if (oldName == null || newName == null) {
			return false;
		}
		if ("".equals(oldName) || "".equals(newName)) {
			return false;
		}
		if (this.getGroupByName(newName) != null) {
			return false;
		}

		for (ColorGroup existingGroup : colorGroups) {
			if (existingGroup.getName().equals(oldName)) {
				existingGroup.setName(newName);
				// change a group name, shall update the parent names in the
				// child groups otherwise the black brightness will appear, this
				// logic needs to be fixed, referring by reference will be
				// better than referring by name.

				// empty list!
				for (String childName : existingGroup.getChildrenGroups()) {
					ColorGroup cg = getGroupByName(childName);
					if (cg != null) {
						cg.setParentGroupName(newName);
					}
				}

				for (ColorGroup cg : colorGroups) {
					if (cg.getName().startsWith(oldName)) {
						String tonePart = cg.getName().substring(
								oldName.length());
						if (tonePart.length() == 6 && tonePart.contains("tone")) {
							// a child
							cg.setParentGroupName(newName);
						}
					}
				}

				return true;
			}
		}

		return false;
	}

	private String getNewGroupName() {
		int count = 1;
		for (ColorGroup cg : colorGroups) {
			// don't count the tones
			if (cg.getParentGroupName() == null) {
				count++;
			}
		}
		String correctId = "color" + count;
		while (getGroupByName(correctId) != null) {
			correctId = "color" + (++count);
		}
		return correctId;
	}

	public ColorGroup getNewGroup(RGB color) {
		ColorGroup group = new ColorGroup(color, getNewGroupName(), this);
		return group;
	}

	public void removeGroup(String name) {
		for (ColorGroup existingGroup : colorGroups) {
			if (existingGroup.getName().equals(name)) {
				colorGroups.remove(existingGroup);
				setChanged();
				notifyObservers(ColorGroups.GROUP_REMOVED);
				return;
			}
		}
	}

	public void removeGroup(ColorGroup group) {
		colorGroups.remove(group);
		// saveGroups();

	}

	public ColorGroup getGroupByName(String groupName) {
		ColorGroup group = null;
		for (ColorGroup grp : getGroups()) {
			if (grp.getName().equals(groupName)) {
				group = grp;
				break;
			}
		}
		return group;
	}

	public List<ColorGroup> getGroups() {
		return colorGroups;
	}

	public List<ColorGroup> getParentGroups() {
		List<ColorGroup> groups = new ArrayList<ColorGroup>();
		for (ColorGroup grp : getGroups()) {
			if (!grp.hasParent()) {
				groups.add(grp);
			}
		}
		return groups;
	}

	public ColorGroup getGroupByColorHue(RGB rgb) {
		ColorGroup group = null;
		float[] hsbInput = rgb.getHSB();
		int hue = (int) (hsbInput[0]);

		for (ColorGroup grp : getGroups()) {
			RGB grpColor = grp.getGroupColor();
			float[] hsb = grpColor.getHSB();
			int extractedHue = (int) (hsb[0]);
			if (hue == extractedHue) {
				group = grp;
				break;
			}
		}
		return group;
	}

	
	public ColorGroup getGroupByRGB(RGB color) {
		ColorGroup group = null;
		for (ColorGroup grp : getGroups()) {
			if (grp.getGroupColor().equals(color)) {
				group = grp;
				break;
			}
		}
		return group;
	}

	public List<ColorGroup> getGroupsByRGB(RGB color) {
		List<ColorGroup> cgs = new ArrayList<ColorGroup>();
		for (ColorGroup grp : getGroups()) {
			if (grp.getGroupColor().equals(color)) {
				cgs.add(grp);
			}
		}
		return cgs;
	}

	public String getThemeName() {
		return this.themeName;
	}

	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}

	public void update(Observable arg0, Object arg1) {
		setChanged();
		notifyObservers(arg1);
	}

	public ColorGroup getGroupByItemId(String id) {
		if (id != null) {
			for (ColorGroup grp : getGroups()) {
				for (ColorGroupItem item : grp.getGroupItems()) {
					if (id.equals(item.getItemId())) {
						return grp;
					}
				}
			}
		}
		return null;
	}

	public ColorGroup getGroupByItemId(String id, String layerName) {
		if (layerName == null) {
			layerName = "";
		}
		for (ColorGroup grp : getGroups()) {
			for (ColorGroupItem item : grp.getGroupItems()) {
				if (id.equals(item.getItemId())) {
					String part = item.getImagePartOrLayer();
					if (part == null) {
						part = "";
					}
					if (part.equals(layerName)) {
						return grp;
					}
				}
			}
		}
		return null;
	}

}
