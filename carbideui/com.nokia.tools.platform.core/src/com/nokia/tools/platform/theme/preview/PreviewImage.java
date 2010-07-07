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
package com.nokia.tools.platform.theme.preview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.StringUtils;

/**
 * 		   To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PreviewImage
    extends ThemeBasicData {

	private Set<Display> displays;

	public PreviewImage(String name) {
		this.setAttribute(PreviewTagConstants.ATTR_PREVIEW_IMAGE_NAME, name);
	}

	/**
	 * @return
	 */
	public String getName() {
		return (String) getAttributeValue(PreviewTagConstants.ATTR_PREVIEW_IMAGE_NAME);
	}

	/**
	 * This API is to be used in gallery subsystem to show the name of the
	 * preview image. This is specially meant for the landscape preview as it
	 * trims the " landscape" portion from the name.
	 */
	public String getThemeName() {
		return getName();
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		this.setAttribute(PreviewTagConstants.ATTR_PREVIEW_IMAGE_NAME, string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#isChildValid(java.lang.Object)
	 */
	@Override
	public boolean isChildValid(Object obj) {
		return obj instanceof PreviewElement || obj instanceof PreviewRefer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#addChild(int,
	 *      java.lang.Object)
	 */
	public synchronized void addChild(int position, Object objPreviewElement) {
		((ThemeBasicData) objPreviewElement).setParent(this);
		if (getLink().getChildren() == null) {
			getLink().setChildren(new ArrayList<Object>());
		}
		if (position < 0) {
			getLink().getChildren().add(objPreviewElement);
		} else {
			getLink().getChildren().add(position, objPreviewElement);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#getDisplay()
	 */
	public Display getDisplay() {
		return ThemePreview.getDefaultDisplay(this);
	}

	public synchronized Set<Display> getDisplays() {
		if (displays == null) {
			displays = ThemePreview.getDisplays(this);
		}
		return displays;
	}

	/**
	 * THis method returns the children of a preview screen including the
	 * indirect children through referals
	 */
	public synchronized List<ThemeBasicData> getChildren(
	    boolean includeReferedChildren) {
		List<ThemeBasicData> allChildren = new ArrayList<ThemeBasicData>();
		List<ThemeBasicData> initialList = getChildren();
		if (!includeReferedChildren) {
			return initialList;
		}

		if (initialList != null) {
			for (ThemeBasicData tbd : initialList) {
				Theme root = (Theme) tbd.getRoot();
				if (tbd instanceof PreviewRefer) {
					String referedScreenName = ((PreviewRefer) tbd)
					    .getReferedScreenName();
					PreviewImage referedImage = (PreviewImage) root
					    .getThemePreview().getPreviewImageByName(
					        referedScreenName, getDisplays());
					if (referedImage != null) {
						List<ThemeBasicData> referedList = referedImage
						    .getChildren(true);
						for (ThemeBasicData data : referedList) {
							// adds cloned with proper parent info
							PreviewElement cloned = (PreviewElement) data
							    .clone();
							cloned.setScreen(this);
							// gets the proper link to the nested element
							cloned.setLink(data.getLink() == data ? data : data
							    .getLink());
							allChildren.add(cloned);
						}
					}
				} else {
					allChildren.add(tbd);
				}
			}
		}

		return allChildren;
	}
	
	/**
	 * THis method returns the children of a preview screen including the
	 * indirect children through referals
	 */
	public synchronized List<ThemeBasicData> getChildrenWithNoClone(
	    boolean includeReferedChildren) {
		List<ThemeBasicData> allChildren = new ArrayList<ThemeBasicData>();
		List<ThemeBasicData> initialList = getChildren();
		if (!includeReferedChildren) {
			return initialList;
		}

		if (initialList != null) {
			for (ThemeBasicData tbd : initialList) {
				Theme root = (Theme) tbd.getRoot();
				if (tbd instanceof PreviewRefer) {
					String referedScreenName = ((PreviewRefer) tbd)
					    .getReferedScreenName();
					PreviewImage referedImage = (PreviewImage) root
					    .getThemePreview().getPreviewImageByName(
					        referedScreenName, getDisplays());
					if (referedImage != null) {
						List<ThemeBasicData> referedList = referedImage
						    .getChildren(true);
						for (ThemeBasicData data : referedList) {
							// adds cloned with proper parent info
							/*PreviewElement cloned = (PreviewElement) data
							    .clone();
							cloned.setScreen(this);*/
							// gets the proper link to the nested element
							/*cloned.setLink(data.getLink() == data ? data : data
							    .getLink());
							allChildren.add(cloned);*/
							allChildren.add(data);
						}
					}
				} else {
					allChildren.add(tbd);
				}
			}
		}

		return allChildren;
	}

	public void setRoot(Theme theme) {
		setParent(theme);

		List<ThemeBasicData> initialList = getChildren();
		if (initialList != null) {
			for (ThemeBasicData tbd : initialList) {
				tbd.setParent(theme);
				if (tbd instanceof PreviewRefer) {
					String referedScreenName = ((PreviewRefer) tbd)
					    .getReferedScreenName();
					PreviewImage referedImage = (PreviewImage) theme
					    .getThemePreview().getPreviewImageByName(
					        referedScreenName, getDisplays());
					referedImage.setRoot(theme);
				}
			}
		}
	}

	public PreviewElement getElement(ThemeBasicData data) {
		if (data.isEntityType().equals(ThemeTag.ELEMENT_COLOUR)) {
			return getColorElement(data.getId());
		}
		return getSkinElement(data.getId());
	}
	
	public PreviewElement getElement(ThemeBasicData data, boolean canClonePreviewElement) {
		if (data.isEntityType().equals(ThemeTag.ELEMENT_COLOUR)) {
			return getColorElement(data.getId());
		}
		return getSkinElement(data.getId(), canClonePreviewElement);
	}

	private PreviewElement getSkinElement(String skinId) {
		if (StringUtils.isEmpty(skinId)) {
			return null;
		}

		List<ThemeBasicData> childrenList = getChildren(true);
		for (ThemeBasicData tbd : childrenList) {
			if (skinId.equalsIgnoreCase(((PreviewElement) tbd).getID())) {
				return (PreviewElement) tbd;
			}
		}
		return null;
	}
	
	private PreviewElement getSkinElement(String skinId, boolean canClonePreviewElement) {
		if (StringUtils.isEmpty(skinId)) {
			return null;
		}

		List<ThemeBasicData> childrenList = null;
		if (canClonePreviewElement)
			childrenList = getChildren(true);
		else
			childrenList = getChildrenWithNoClone(true);
		for (ThemeBasicData tbd : childrenList) {
			if (skinId.equalsIgnoreCase(((PreviewElement) tbd).getID())) {
				return (PreviewElement) tbd;
			}
		}
		return null;
	}

	private PreviewElement getColorElement(String colorId) {
		if (StringUtils.isEmpty(colorId)) {
			return null;
		}

		List<ThemeBasicData> childrenList = getChildren(true);
		for (ThemeBasicData tbd : childrenList) {
			String currColorId = tbd
			    .getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_COLORID);
			if (colorId.equalsIgnoreCase(currColorId)) {
				return (PreviewElement) tbd;
			}
		}
		return null;
	}

	public boolean isPreview() {
		return new Boolean(getAttributeValue(ThemeTag.ATTR_PREVIEW));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#supportsDisplay(com.nokia.tools.platform.core.Display)
	 */
	public boolean supportsDisplay(Display display) {
		return getDisplays().contains(display);
	}

	public List<PreviewElement> getPreviewElementsForColor(String colourId) {
		if (StringUtils.isEmpty(colourId)) {
			return Collections.EMPTY_LIST;
		}
		List<PreviewElement> result = new ArrayList<PreviewElement>();
		for (ThemeBasicData child : getChildren(true)) {
			PreviewElement element = (PreviewElement) child;
			if (element.getPreviewElementType() == ThemeConstants.ELEMENT_TYPE_SOUND) {
				continue;
			}
			String colorgroup = element
			    .getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_COLORID);

			if (colourId.equals(colorgroup)) {
				result.add(element);
			}
		}
		return result;
	}

	public PreviewElement getPreviewElement(Display display, String compName,
	    int varietyIndex) {
		return getPreviewElement(display, null, compName == null ? ""
		    : compName, Integer.toString(varietyIndex));
	}

	public PreviewElement getPreviewElement(Display display,
	    String skinElementName) {
		return getPreviewElement(display, skinElementName, null, null);
	}
	
	public PreviewElement getPreviewElement(Display display,
		    String skinElementName, String compName, String varietyId, boolean canClonePreviewElement) {
		if (display == null) {
			display = getRoot().getDisplay();
		}
		List<ThemeBasicData> childrenList = null;
		if (canClonePreviewElement)
			childrenList = getChildren(true);
		else
			childrenList = getChildrenWithNoClone(true);
		for (ThemeBasicData tbd : childrenList) {
			PreviewElement element = (PreviewElement) tbd;

			int type = element.getPreviewElementType();
			if (type == ThemeConstants.ELEMENT_TYPE_IMAGEFILE
			    || type == ThemeConstants.ELEMENT_TYPE_SOUND) {
				continue;
			}

			if (compName == null) {
				if (skinElementName != null
				    && skinElementName.equalsIgnoreCase(element.getID())
				    && element.supportsDisplay(display)) {
					return element;
				}
			} else {
				ComponentInfo pi = element.getComponentInfo();
				if (!StringUtils.isEmpty(varietyId)) {
					if (compName.equalsIgnoreCase(pi.getName())
					    && varietyId.equals(pi.getVariety())
					    && element.supportsDisplay(display)) {
						return element;
					}
				} else {
					if (compName.equalsIgnoreCase(pi.getName())
					    && element.supportsDisplay(display)) {
						return element;
					}
				}
			}
		}

		return null;
		/*return getPreviewElement(display,
			    skinElementName, compName, varietyId, canClonePreviewElement);*/
	}

	private PreviewElement getPreviewElement(Display display,
	    String skinElementName, String compName, String varietyId) {
		return getPreviewElement(display,
			    skinElementName, compName, varietyId, true);
	}

	public PreviewElement getPreviewElementWithSplMask(String splMaskId)
	    throws ThemeException {
		if (StringUtils.isEmpty(splMaskId)) {
			return null;
		}

		List<ThemeBasicData> childrenList = getChildren(true);
		for (ThemeBasicData tbd : childrenList) {
			PreviewElement element = (PreviewElement) tbd;
			int type = element.getPreviewElementType();
			if (type == ThemeConstants.ELEMENT_TYPE_IMAGEFILE
			    || type == ThemeConstants.ELEMENT_TYPE_SOUND) {
				continue;
			}

			if (splMaskId.equalsIgnoreCase(element.getSplMask())) {
				return element;
			}
		}
		return null;
	}

	public String getLocID(String compName, int varietyId) {
		if (StringUtils.isEmpty(compName)) {
			return null;
		}

		List<ThemeBasicData> childrenList = getChildren(true);
		for (ThemeBasicData tbd : childrenList) {
			PreviewElement element = (PreviewElement) tbd;
			int type = element.getPreviewElementType();
			if (type == ThemeConstants.ELEMENT_TYPE_IMAGEFILE
			    || type == ThemeConstants.ELEMENT_TYPE_SOUND)
				continue;

			ComponentInfo pi = element.getComponentInfo();

			if (compName.equalsIgnoreCase(pi.getName())
			    && Integer.toString(varietyId).equals(pi.getVariety())) {
				return element.getLocId();
			}
		}
		return null;
	}

	public String getSupportedDisplays() {
		return (String) attributes.get(PreviewTagConstants.ATTR_DISPLAY);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof PreviewImage) {
			PreviewImage otherPreviewImage = (PreviewImage) other;
			String name = getName();
			String otherName = otherPreviewImage.getName();
			if (null != name && null != otherName)
				return name.equals(otherName);
		}
		return false;
	}

}
