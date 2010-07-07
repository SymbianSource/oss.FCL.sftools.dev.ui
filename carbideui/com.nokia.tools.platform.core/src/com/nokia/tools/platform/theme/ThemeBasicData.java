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
package com.nokia.tools.platform.theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.layout.LayoutContext;
import com.nokia.tools.platform.layout.LayoutException;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.resource.util.FileUtils;

/**
 * Base Class which holds the basic information of nodes making up the Series60
 * Skin
 * 
 * 
 */


public abstract class ThemeBasicData implements Cloneable {

	// to hold the current property status
	private String currentProp = null;
//	protected List thirdPartyMap = new ArrayList();
	// Holds the parent object of a node
	private ThemeBasicData parent = null;

	// Indicates if a node is selected
	private boolean isSelected = false;

	// Indicates if a node is shown
	private boolean isShown = true;

	// Indiates if this skinnable element is skinned or not.
	private boolean isSkinned = false;


	// Holds the attributes of a node
	protected Map<Object, Object> attributes = new HashMap<Object, Object>();

	// Holds the toolbox
	protected ToolBox toolBox = null;

	protected List properties = null;

	protected List designrectangles = null;

	// holds children
	protected List<Object> children = null;

	protected ThemeBasicData link = this;

	// holds the picture
	private String fixedPicture = null;
	// holds the not resolved picture (at start it is same as fixedPicture)
	private String fixedPictureName = null;

	private boolean fixedPictureResolved = false;

	// hold if the entity has been selected for transfer to phone
	boolean isSelectedForTransfer = false;

	protected List<Object> animatedSkinnableEntityList = new ArrayList<Object>();

	private boolean executed = false;

	/**
	 * Default constructor
	 */
	public ThemeBasicData() {
		setSelected(false);
	}

	public LayoutContext getLayoutContext() {
		Theme root = (Theme) getRoot();
		if (root != null) {
			return root.getLayoutContext(getDisplay());
		}
		return null;
	}

	public LayoutContext getLayoutContext(Display display) {
		Theme root = (Theme) getRoot();
		if (root != null) {
			return root.getLayoutContext(display);
		}
		return null;
	}

	public Theme getModel() {
		Theme root = (Theme) getRoot();
		if (root != null) {
			return root.getModel();
		}
		return null;
	}

	public Display getDisplay() {
		Set<Display> displays = getDisplays();
		if (!displays.isEmpty()) {
			return displays.iterator().next();
		}
		return null;
	}

	public Set<Display> getDisplays() {
		Theme theme = (Theme) getRoot();
		Set<Display> displays = new HashSet<Display>();
		for (Display display : theme.getDisplays()) {
			if (supportsDisplay(display)) {
				displays.add(display);
			}
		}
		return displays;
	}

	public void setChildren(List<Object> l) {

		link.children = l;
	}

	public boolean getExecuted() {
		return this.executed;
	}

	public void setExecuted(boolean executed) {
		this.executed = executed;
	}

	/**
	 * Sets the name of the node
	 * 
	 * @param sName name of the node
	 */
	public void setThemeName(String name) {
		synchronized (this) {
			link.attributes.put(ThemeTag.ATTR_NAME, name);
		}
	}

	/**
	 * Returns the name of the node
	 * 
	 * @return String name of the node
	 */
	public String getThemeName() {
		return (String) link.attributes.get(ThemeTag.ATTR_NAME);
	}

	// / Get and set methods for DRM....

	public void setDRM(boolean drm) {
		synchronized (this) {
			link.attributes.put(ThemeTag.ADVANCE_DRM, (drm + ""));
		}
	}

	public boolean getDRM() {
		return new Boolean((String) link.attributes.get(ThemeTag.ADVANCE_DRM))
				.booleanValue();
	}

	public void setPixelState(boolean pixelState) {
		synchronized (this) {
			link.attributes.put(ThemeTag.BITS_PIXEL_SUPPORT, (pixelState + ""));
		}
	}

	public boolean getPixelState() {
		return new Boolean((String) link.attributes
				.get(ThemeTag.BITS_PIXEL_SUPPORT)).booleanValue();
	}

	public void setNormalTheme(boolean normalTheme) {
		synchronized (this) {
			link.attributes.put(ThemeTag.ADVANCE_NORMAL, (normalTheme + ""));
		}
	}

	public boolean getNormalTheme() {
		return new Boolean((String) link.attributes
				.get(ThemeTag.ADVANCE_NORMAL)).booleanValue();
	}

	public void setSelectedVersion(String selectedVersion) {
		synchronized (this) {
			link.attributes.put(ThemeTag.ADVANCE_VERSION,
					(selectedVersion + ""));
		}
	}

	public String getSelectedVersion() {
		return ((String) link.attributes.get(ThemeTag.ADVANCE_VERSION));
	}

	public void setSystemTheme(boolean systemSkin) {
		synchronized (this) {
			link.attributes.put(ThemeTag.ADVANCE_SYSTEM, (systemSkin + ""));
		}
	}

	public boolean getSystemTheme() {
		return new Boolean((String) link.attributes
				.get(ThemeTag.ADVANCE_SYSTEM)).booleanValue();
	}

	public void setUID(String uid) {
		synchronized (this) {
			link.attributes.put(ThemeTag.ADVANCE_UID, uid);
		}
	}

	public String getUID() {
		return (String) link.attributes.get(ThemeTag.ADVANCE_UID);
	}

	public void setPublicKey(String publicKey) {
		synchronized (this) {
			link.attributes.put(ThemeTag.ADVANCE_PUBLICKEY, publicKey);
		}
	}

	public String getPublicKey() {
		return (String) link.attributes.get(ThemeTag.ADVANCE_PUBLICKEY);
	}

	public void setPrivateKey(String privateKey) {
		synchronized (this) {
			link.attributes.put(ThemeTag.ADVANCE_PRIVATEKEY, privateKey);
		}
	}

	public String getPrivateKey() {
		return (String) link.attributes.get(ThemeTag.ADVANCE_PRIVATEKEY);
	}

	public String getId() {
		return (String) link.attributes.get(ThemeTag.ATTR_ID);
	}

	/**
	 * Sets the trasferSelection status
	 * 
	 * @param status The booleans status of selection
	 */
	public void setSelectionForTransfer(boolean status) {
		setSelectionForTransfer(status, true, true);
	}

	/**
	 * Sets the selection status for transfer
	 * 
	 * @param status The booleans status of selection
	 * @param isChildSelected true if the childrens are also selected
	 * @param isParentSelected true if the child should set its parent as well
	 */
	public void setSelectionForTransfer(boolean status,
			boolean isChildSelected, boolean isParentSelected) {

		if ((isParentSelected == true) && (status == true)) {
			ThemeBasicData parent = getParent();

			if (parent != null) {
				parent.setSelectionForTransfer(true, false, true);
			}
		}
		isSelectedForTransfer = status;

		if (isChildSelected) {
			// if a entity is selected it means all its children are also
			// selected
			// List children =link.getChildren();
			List children = getChildren();

			if (children != null) {
				for (int i = 0; i < children.size(); i++) {
					ThemeBasicData sbd = (ThemeBasicData) children.get(i);
					sbd.setSelectionForTransfer(status, true, false);
				}
			}
		}
	}

	/**
	 * Method to set the link
	 * 
	 * @param sbd ThemeBasicData the link
	 */
	public void setLink(ThemeBasicData sbd) {
		link = sbd;
	}

	/**
	 * Method to get the link
	 * 
	 * @return ThemeBasicData the link
	 */
	public ThemeBasicData getLink() {
		return link;
	}

	/**
	 * Method to get the status of Link
	 * 
	 * @return boolean true if linked else false
	 */
	public boolean isLink() {
		if (this != link)
			return true;
		return false;
	}

	public SkinnableEntity getSkinnableEntity() {
		return null;
	}

	/**
	 * Fetches the transferselection status
	 */
	public boolean getSelectionForTransfer() {
		// return link.isSelectedForTransfer;
		return isSelectedForTransfer;
	}

	/**
	 * Sets the show status
	 * 
	 * @param status The boolean status of show
	 */
	public void setShow(boolean status) {
		this.isShown = status;
	}

	/**
	 * Returns the status of show
	 * 
	 * @return The show status
	 */
	public boolean isShown() {
		return this.isShown;
	}

	// /**
	// * getting the sound sattus
	// */
	// boolean isSoundElement = false;
	// public boolean getSoundStatus(ThemeBasicData sbdSound){
	//        
	// if (new
	// Boolean(sbdSound.getAttributeValue(ThemeTag.ELEMENT_SOUND)).booleanValue())
	// {
	// isSoundElement = true;
	// return isSoundElement;
	// }
	// else {
	// while (sbdSound.getParent() != null){
	// sbdSound = sbdSound.getParent();
	// getSoundStatus(sbdSound);
	// }
	// }
	// return isSoundElement;
	// }

	/*
	 * API for checking satus of Entity at any level ( Element / component /
	 * componentgroup )
	 */

	// public boolean isEntityType(String attr){
	// return isEntityType(attr, this);
	// }
	//     
	// public boolean isEntityType(String attr, ThemeBasicData sbd){
	// boolean entityType = false;
	// if( sbd == null) {
	// return true;
	// }
	// if(new Boolean(sbd.getAttributeValue(attr)).booleanValue()){
	// entityType = true ;
	// return entityType;
	// }else {
	// while (sbd.getParent() != null){
	// sbd = sbd.getParent();
	// entityType=isEntityType(attr, sbd);
	// if(entityType) return true;
	// }
	// }
	// return false;
	// }
	public String isEntityType() {
		return isEntityType(this);
	}

	public String isEntityType(ThemeBasicData sbd) {
		String entityType = "image";
		if (sbd == null) {
			return entityType;
		}
		if (sbd.getAttributeValue(ThemeTag.ATTR_ENTITY_TYPE) != null) {
			entityType = sbd.getAttributeValue(ThemeTag.ATTR_ENTITY_TYPE);
			return entityType;
		} else {
			while (sbd.getParent() != null) {
				sbd = sbd.getParent();
				entityType = isEntityType(sbd);
				if (entityType != null)
					return entityType;
			}
		}
		return entityType;
	}

	protected void copyFile(String sourceName, String newName) {
		// // Computing New File Name with the new childId
		String destFileName = null;
		
		if (!newName.endsWith(ThemeTag.FILE_TYPE_BMP))
			newName = newName + ThemeTag.FILE_TYPE_BMP;
		if (((Theme) (this.getRoot())).getThemeDir() != null) {
			destFileName = ((Theme) (this.getRoot())).getThemeDir()
					+ File.separator + newName;
		}

		// Copying from source to destination
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			String dir = ((Theme) (this.getRoot())).getThemeDir();
			File fileDir = new File(dir);
			fileDir.mkdir();
			File newFile = new File(destFileName);
			newFile.createNewFile();

			fis = new FileInputStream(sourceName);
			fos = new FileOutputStream(destFileName);
			int i = 0;

			while ((i = fis.read()) != -1) {
				fos.write(i);
			}
			fos.flush();
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		} finally {
			FileUtils.close(fis);
			FileUtils.close(fos);
		}
	}

	/**
	 * Sets the fix status
	 * 
	 * @param status The boolean status of fix
	 */
	public void setFix(boolean status) {
		synchronized (this) {
			link.setAttribute(ThemeTag.ATTR_FIX, String.valueOf(status));
			if (link.children != null) {
				for (int i = 0; i < link.children.size(); i++) {
					ThemeBasicData s = (ThemeBasicData) link.children.get(i);
					s.setFix(status);
				}
			}
		}
	}

	/**
	 * Method to set the Map holding the Attribute name,value pair
	 * 
	 * @param mapAttribute Map with the Attribute name,value pair
	 */
	public void setAttribute(Map<Object, Object> mapAttribute) {
		synchronized (this) {
			if (mapAttribute.containsKey(ThemeTag.ATTR_SHOW)) {

				if (((String) mapAttribute.get(ThemeTag.ATTR_SHOW))
						.equals(ThemeTag.ATTR_SHOW_FALSE)) {
					this.setShow(false);
				}
			}

			if (mapAttribute.containsKey(ThemeTag.ATTR_MASTERID)) {
				this.setShow(false);
			}

			if (mapAttribute.containsKey(ThemeTag.ATTR_PICTURE)) {
				link.fixedPicture = (String) mapAttribute
						.get(ThemeTag.ATTR_PICTURE);
				link.fixedPictureName = link.fixedPicture;
				mapAttribute.remove(ThemeTag.ATTR_PICTURE);
			}
			link.attributes.putAll(mapAttribute);
		}

	}

	public void setBitmapProperties(BitmapProperties properties) {
		setAttribute(properties.getAttributes());
	}

	public BitmapProperties getBitmapProperties() {
		return new BitmapProperties(getAttribute());
	}

	/**
	 * Method to get the Attributes for a node
	 */
	public Map<Object, Object> getAttribute() {
		return link.attributes;
	}

	/**
	 * Method to get the value of a Attribute
	 * 
	 * @param attributeName name of the Attribute
	 * @return String value of the Attribute
	 */
	public String getAttributeValue(String attributeName) {

		// this check is needed becos...
		// the loc_id and the previewscreen information for a linked entity
		// will be available only in its attr map (this.attrMap).
		if (link != this) {

			if (attributeName.equalsIgnoreCase(ThemeTag.ATTR_LOC_ID)
					|| attributeName
							.equalsIgnoreCase(ThemeTag.ATTR_PREVIEWSCREEN)) {

				try {
					return (String) this.attributes.get(attributeName);
				} catch (Exception e) {
					PlatformCorePlugin.error(e);
				}

			}
		}
		return (String) link.attributes.get(attributeName);
	}

	/**
	 * Method to set the value of a Attribute
	 * 
	 * @param attributeName name of the Attribute
	 * @param value value of the Attribute
	 */

	public void setAttribute(String attributeName, String value) {
		synchronized (this) {
			link.attributes.put(attributeName, value);
		}
	}

	/**
	 * Method to set the name attribute
	 * 
	 * @param name
	 */
	public void setName(String name) {
		setAttribute(ThemeTag.ATTR_NAME, name);
	}

	/**
	 * Method to get the name attribute
	 * 
	 * @param name
	 */
	public String getName() {
		return getAttributeValue(ThemeTag.ATTR_NAME);
	}

	/**
	 * Sets the parent of the node
	 * 
	 * @param parent ThemeBasicData object holding the parent of the node
	 */
	public void setParent(ThemeBasicData parent) {
		this.parent = parent;
	}

	/**
	 * Returns the parent of the node
	 * 
	 * @return ThemeBasicData object holding the parent of the node
	 */
	public ThemeBasicData getParent() {
		return this.parent;
	}

	/**
	 * Method to set the ToolBox for a Node
	 * 
	 * @param toolBox ToolBox object to be set to the Node
	 */
	public void forcedSetToolBox() {

		if (link.toolBox == null) {
			link.toolBox = new ToolBox();
		}

		if (link.getParent() != null) {
			ToolBox parentToolBox = link.getParent().getForcedToolBox();
			link.toolBox.update(parentToolBox);
		}
	}

	/**
	 * Method to set the ToolBox for a Node
	 * 
	 * @param toolBox ToolBox object to be set to the Node
	 */
	public void setToolBox(ToolBox toolBox) {
		link.toolBox = toolBox;
	}

	public void setProperties(List list) {
		link.properties = list;
	}

	public void setDesignRectangles(List list) {
		link.designrectangles = list;
	}

	public List getProperties() {
		return getProperties(link);
	}

	public List getDesignRectangles() {
		return getDesignRectangles(link);
	}

	public List getProperties(ThemeBasicData sbd) {
		List prop = null;
		if (sbd == null) {
			return null;
		}

		if (sbd.properties != null) {
			return sbd.properties;
		} else {

			while (sbd.getParent() != null) {
				sbd = sbd.getParent();
				prop = getProperties(sbd);
				if (prop != null)
					return prop;
			}
		}
		return null;
	}

	public List getDesignRectangles(ThemeBasicData sbd) {
		List prop = null;
		if (sbd == null) {
			return null;
		}

		if (sbd.designrectangles != null) {
			return sbd.designrectangles;
		} else {

			while (sbd.getParent() != null) {
				sbd = sbd.getParent();
				prop = getDesignRectangles(sbd);
				if (prop != null)
					return prop;
			}
		}
		return null;
	}

	/**
	 * Returns the ToolBox of a Node
	 * 
	 * @return ToolBox ToolBox object of a Node
	 */
	public ToolBox getToolBox() {
		return link.toolBox;
	}

	/**
	 * Helper method for setForcedToolBox
	 * 
	 * @return ToolBox ToolBox object of a Node
	 */
	private ToolBox getForcedToolBox() {

		if (link.toolBox == null) {

			link.forcedSetToolBox();
		}

		return link.toolBox;
	}

	public String getPreviewHint() {
		ThemeBasicData current = this;
		while (current != null) {
			String hint = current.getAttributeValue(ThemeTag.ATTR_PREVIEW_HINT);
			if (hint != null) {
				return hint;
			}
			current = current.getParent();
		}
		return null;
	}

	/**
	 * Method to add a child to a node
	 * 
	 * @param obj Object of the Child node
	 */
	public void addChild(ThemeBasicData obj) throws ThemeException {
		addChild(-1, obj);
	}

	public abstract void addChild(int position, Object obj)
			throws ThemeException;

	public abstract boolean isChildValid(Object obj);

	/**
	 * Method to return the Children of a node
	 * 
	 * @return List list containing the children of the node
	 */
	public List getChildren() {
		// to get the parts for an element in which it is to be layered
		if (link instanceof Element) {
			link.children = link.getLayeredChildren();
		}
		return link.children;
	}

	/*
	 * this method returns the children whose isShown attribute is same as
	 * isShown
	 */
	public List getChildren(boolean isShown) {
		// to get the parts for an element in which it is to be layered
		if (link instanceof Element) {
			link.children = link.getLayeredChildren();
		}

		List<Object> newList = null;
		if (link.children != null && link.children.size() > 0) {
			newList = new ArrayList<Object>();
			for (int i = 0; i < link.children.size(); i++) {
				if (((ThemeBasicData) link.children.get(i)).isShown == isShown)
					newList.add(link.children.get(i));
			}
		}

		return newList;
	}

	/**
	 * Method to return the Child of a node with the given name
	 * 
	 * @return ThemeBasicData child of the node with the given name
	 */
	public ThemeBasicData getChild(String childIdentifier) {
		if (link.children != null) {

			for (int i = 0; i < link.children.size(); i++) {
				ThemeBasicData sbd = (ThemeBasicData) (link.children.get(i));

				String id = sbd.getAttributeValue(ThemeTag.ATTR_ID);

				if (id != null) {
					if (id.equalsIgnoreCase(childIdentifier)) {
						return sbd;
					}
				}

				if ((sbd.getThemeName()).equalsIgnoreCase(childIdentifier)) {
					return sbd;
				}
			}
		}

		return null;
	}

	public ThemeBasicData getChildForPreview(String childIdentifier) {
		if (link.children != null) {

			for (int i = 0; i < link.children.size(); i++) {
				ThemeBasicData sbd = (ThemeBasicData) (link.children.get(i));

				String identifier = sbd.getIdentifier();

				if (identifier != null) {
					if (identifier.equalsIgnoreCase(childIdentifier)) {
						return sbd;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Method to return the index of Child position
	 * 
	 * @param childName String holds the name of the child for which the index
	 *            is needed
	 * @return int index of the child position
	 */
	public int getChildIndex(String childName) {

		if (link.children != null) {

			for (int i = 0; i < link.children.size(); i++) {
				ThemeBasicData sbd = (ThemeBasicData) (link.children.get(i));

				if ((sbd.getThemeName()).equalsIgnoreCase(childName)) {
					return i;
				}
			}
		}

		return ThemeTag.CHILD_NOT_FOUND;
	}

	/**
	 * Method to return the Child of a node with the given name
	 * 
	 * @return ThemeBasicData child of the node with the given name
	 */
	public ThemeBasicData getEntity(String taggedString) throws ThemeException {

		String value = null;
		value = getTaggedInfo(taggedString, ThemeTag.TAG_SKIN_START);
		if (!value.equalsIgnoreCase(this.getThemeName())) {
			throw new ThemeException(" The Skins are not same");
		}

		ThemeBasicData sbd = null;
		ThemeBasicData parent = null;
		value = getTaggedInfo(taggedString, ThemeTag.TAG_TASK_START);
		if (link.children != null) {
			sbd = getChild(value);
			if (sbd != null) {
				parent = sbd;
			}
		}

		value = getTaggedInfo(taggedString, ThemeTag.TAG_COMPONENTGROUP_START);
		if (parent.children != null) {
			sbd = parent.getChild(value);
			if (sbd != null) {
				parent = sbd;
			}
		}

		value = getTaggedInfo(taggedString, ThemeTag.TAG_COMPONENT_START);
		if (parent.children != null) {
			sbd = parent.getChild(value);
			if (sbd != null) {
				parent = sbd;
			}
		}

		value = getTaggedInfo(taggedString, ThemeTag.TAG_ELEMENT_START);
		if (parent.children != null) {
			sbd = parent.getChild(value);
			if (sbd != null) {
				parent = sbd;
			}
		}

		value = getTaggedInfo(taggedString, ThemeTag.TAG_PART_START);
		if (parent.children != null) {
			sbd = parent.getChild(value);
			if (sbd != null) {
				return sbd;
			}
		}

		return parent;
	}

	/**
	 * Method to merge the data of an Node with an existing one
	 * 
	 * @param duplicateNode ThemeBasicData object whose details needs to be
	 *            added to the existing object
	 */
	public void merge(ThemeBasicData duplicateNode) throws ThemeException {

		synchronized (this) {
			duplicateNode.getAttribute().remove(ThemeTag.ATTR_NAME);
			link.setAttribute(duplicateNode.getAttribute());

			if (link.toolBox != null) {
				link.toolBox.update(duplicateNode.getToolBox());
			}

			if (duplicateNode.children != null) {
				for (int i = 0; i < duplicateNode.children.size(); i++) {

					link.addChild((ThemeBasicData) duplicateNode.children
							.get(i));
				}
			}
		}
	}

	/**
	 * Method to merge the data of an Node with an existing one
	 * 
	 * @param duplicateNode ThemeBasicData object whose details needs to be
	 *            added to the existing object
	 */
	public void overWrite(ThemeBasicData duplicateNode) throws ThemeException {

		synchronized (this) {
			// duplicateNode.getAttribute().remove(ThemeTag.ATTR_NAME);
			// link.setAttribute(duplicateNode.getAttribute());

			link.removeAll();
			if (link.toolBox != null) {
				link.toolBox.update(duplicateNode.getToolBox());
			}
			if (duplicateNode.children != null) {
				for (int i = 0; i < duplicateNode.children.size(); i++) {

					link.addChild((ThemeBasicData) duplicateNode.children
							.get(i));
				}
			}
		}
	}

	/**
	 * Removes a child from the list of Children for a Node
	 * 
	 * @param skinObject Object to be removed from the list of ComponentGroups
	 */
	public void removeChild(Object skinObject) throws ThemeException {

		if ((link.children != null) && (link.children.contains(skinObject))) {
			List list = ((ThemeBasicData) skinObject).getChildren();

			synchronized (this) {

				/*
				 * commented by sirokkam do not remove skinObject children,
				 * because this cause problems with UNDO operations...
				 */
				// while ((list != null) && (list.size() != 0)) {
				// ((ThemeBasicData) skinObject).removeChild(list.get(0));
				// }
				if (link.children.remove(skinObject)) {
					// Debug.out(this,"success" + this.toString());
				} else {
					// Debug.out(this,"remove failed");
					throw new ThemeException("Removal of child failed");
				}
			}
		}
	}

	/**
	 * Remove all the children and initialize the toolbox for a node
	 * 
	 * @return none
	 */
	public void removeAll() throws ThemeException {

		if (link.children != null) {
			// Debug.out(this,"remove" + this.getName());
			synchronized (this) {
				while (link.children.size() != 0) {
					link.removeChild(link.children.get(0));
				}
			}
		}
		if (link.toolBox != null) {
			// link.toolBox = null;
			link.toolBox = new ToolBox();
		}
	}

	/**
	 * Method to check if a node has any children.Returns true if it has no
	 * children, else false. This method is overridden in the derived class if
	 * needed
	 * 
	 * @return boolean true if it has children , else false
	 */
	public boolean hasChildNodes() {
		if (link.children == null) {
			return false;
		} else if (link.children.size() > 0) {
			return true;
		}

		return false;
	}

	/*
	 * @return String representation of the object
	 */
	public String toString() {

		StringBuffer data = new StringBuffer();

		data.append("\nName :");
		data.append(getThemeName());

		data.append("\nATTRIBUTES\n\t");
		data.append(attributes);

		return data.toString();
	}

	/**
	 * Method to clone the object
	 * 
	 * @return Object cloned object
	 */
	public Object clone() {
		try {
			ThemeBasicData skinObj = (ThemeBasicData) super.clone();

			if ((skinObj instanceof Part) || (skinObj instanceof Theme)
					|| (skinObj instanceof PreviewElement)) {

			} else {
				// clone the attributes Map
				HashMap<Object, Object> m = new HashMap<Object, Object>(
						this.attributes);
				m = (HashMap) m.clone();
				skinObj.attributes = (Map<Object, Object>) m;

				// clone the toolBox
				if (skinObj.toolBox != null)
					skinObj.toolBox = (ToolBox) (this.toolBox.clone());

				// clone the chilren List
				if (this.children != null) {
					List<Object> l = (List<Object>) this.getChildren();
					ArrayList<Object> a = new ArrayList<Object>(l);

					a = (ArrayList) a.clone();

					for (int i = 0; i < a.size(); i++) {
						if (a.get(i) instanceof String) {
							// for the PreviewImage the children is a list of
							// Strings. in which case
							// cloning is not needed
						} else {
							ThemeBasicData s = (ThemeBasicData) a.get(i);
							if (s instanceof Element) {
								s = (Element) s.clone();
							}
							if (s instanceof Part) {
								s = (Part) s.clone();
								s.setParent(skinObj);
							} else {
								s = (ThemeBasicData) s.clone();
							}

							// clone the parent
							s.parent = skinObj;
							a.set(i, s);
						}
					}
					skinObj.children = (List<Object>) a;
				}
			}

			if (!isLink()) {
				skinObj.link = skinObj;
			}

			return (Object) skinObj;
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	/**
	 * Get the root
	 * 
	 * @return ThemeBasicData root
	 */
	
	public ThemeBasicData getRoot() {
		try {
			ThemeBasicData parent = getParent();
			if (parent != null) {
				return parent.getRoot();
			}
			return this; 
		}
		catch (Throwable t) {
			System.out.println("DEBUG: Memory Overflow.");
		}
		return this;

	}

	// public Theme getRoot()
	// {
	// ThemeBasicData parent = getParent();
	// while (parent != null) {
	//			
	// parent = parent.getParent();
	//			
	// if (parent instanceof Theme)
	// return (Theme) parent;
	// }
	// return null;
	// }


	public void setSkinned(boolean status) {
		ThemeBasicData tbd = this;
		//Those that are of frame type should not have its parts with skinned status.		
		if ((this instanceof Part)) {
			//Assumption here is that all those parts that are not frames must go for 
			//packaging. A slightly better way to do this is to let the multipiece manager
			//decide who goes for packaging and who does not. To be refactored so.
			if (!this.getParent().isEntityType().equals(ThemeTag.ELEMENT_FRAME))
				this.isSkinned = status;
			tbd = this.getParent();	
			 
		}
		if (!(tbd instanceof Element))
			return;
		tbd.setSkinnedStatus(status);
	}

	public boolean isSkinned() {
		return isSkinned;
	}

	private void setSkinnedStatus(boolean status) {
		this.isSkinned = status;
		
		ThemeBasicData parent = this.getParent();
		if (parent == null)
			return;
		if (status) {

			List l = parent.getChildren();
			if ((l == null) || (l.size() == 0)) {
				return;
			}
			int lsize = l.size();
			boolean childDoneStatus = true;
			for (int i = 0; i < lsize; i++) {
				ThemeBasicData s = (ThemeBasicData) l.get(i);
				if ((s != null) && (s.isShown)) {
					if ((!s.isSkinned) && (s.isSelected)) {
						childDoneStatus = false;
						break;
					}
				}
			}
			if (childDoneStatus) {
				parent.setSkinnedStatus(true);
			}
		} else {
			parent.setSkinnedStatus(false);
		}
	}

	/*
	 * public void setSkinned(boolean status) { this.isSkinned = status;
	 * ThemeBasicData parent = this.getParent(); boolean childStatus = true;
	 * if(! status) { parent.isSkinned = status; }else if((parent.getChildren() !=
	 * null) && (parent.getChildren().size() > 0)) { for(int i = 0; i <
	 * parent.getChildren().size() ; i++) { ThemeBasicData s = (ThemeBasicData)
	 * parent.getChildren().get(i); if(! s.isSkinned){ childStatus = false;
	 * break; } } parent.isSkinned = childStatus; } if(parent.getParent() !=
	 * null) { parent.setSkinned(childStatus); } }
	 */

	public void setSelected(boolean status) {
		this.isSelected = status;
		
	}

	public void setSelected1(boolean status) {
		this.isSelected = status;
		if (!status)
			return;
		if (this.getParent() != null) {
			ThemeBasicData tbd = (ThemeBasicData) this.getParent();
			for (int i = 0; i < tbd.getChildren().size(); i++) {
				if ((((ThemeBasicData) tbd.getChildren().get(i)).isSelected)
						|| (((ThemeBasicData) tbd.getChildren().get(i)).isSkinned)) {
					tbd.setSelected1(true);
				}
			}
		}
	}

	public String getSkinnedStatus() {
		if (this.isSkinned)
			return ThemeTag.SKN_ATTR_STATUS_DONE;
		else if (this.isSelected)
			return ThemeTag.SKN_ATTR_STATUS_SELECTED;
		else
			return ThemeTag.SKN_ATTR_STATUS_NOT_SELECTED;
	}

	public boolean isAnyChildDone() {

		if (this.isSkinned)
			return true;

		// check colorize properties
		Object test = attributes.get(BitmapProperties.COLORIZE_SELECTED);
		if (test != null) {
			if (test instanceof Boolean) {
				if ((Boolean) test)
					return true;
			} else if (test instanceof String) {
				if ("true".equals(test))
					return true;
			}
		}
		test = attributes.get(BitmapProperties.IS_OPTIMIZE_SELECTED);
		if (test != null) {
			if (test instanceof Boolean) {
				if ((Boolean) test)
					return true;
			} else if (test instanceof String) {
				if ("true".equals(test))
					return true;
			}
		}

		List l = this.getChildren();

		if (l == null)
			return false;
		int lsize = l.size();
		if (lsize == 0)
			return false;

		if (this instanceof Element) {
			if (ThemeTag.ATTR_SINGLE_BITMAP.equals(getCurrentProperty())) {
				// dont check childrens - PARTs
				return false;
			}
		}

		boolean childStatus = false;
		for (int i = 0; i < lsize; i++) {
			ThemeBasicData s = (ThemeBasicData) l.get(i);
			childStatus = s.isAnyChildDone();
			if (childStatus)
				break;
		}
		return childStatus;
	}

	/**
	 * Method to return the children in the order of layering
	 * 
	 * @return List children in the layered order
	 */
	public List<Object> getLayeredChildren() {

		List<Object> layeredList = null;

		String layer = null;

		// the layer values of the children are put in an integer array.
		// if the layer value is not available... the LAYOUT_LAYER_MIN is
		// assigned.

		// the mapping to a child in the "children" and its layer value in the
		// "layerArray" is available.

		if (link.hasChildNodes()) {

			List childList = link.children;
			int[] layerArray = new int[childList.size()];

			for (int i = 0; i < childList.size(); i++) {

				ThemeBasicData sbd = (ThemeBasicData) childList.get(i);
				layer = sbd.getAttributeValue(ThemeTag.LAYOUT_LAYER);
				if (layer != null) {
					layerArray[i] = Integer.parseInt(layer);
				} else
					layerArray[i] = ThemeTag.LAYOUT_LAYER_MIN;
			}

			// loop from min layer value to max layer value. ***the upper
			// bound***
			// includes the max also. the min =0 and max = 8

			// for every layer value, loop thro "layerArray" and if the
			// layervalue
			// and the value at the layerArray at a position matches, pick it
			// and add it to the layeredList

			for (int i = ThemeTag.LAYOUT_LAYER_MIN; i < childList.size(); i++) {

				for (int j = 0; j < layerArray.length; j++) {
					if (i == layerArray[j]) {
						ThemeBasicData sbd = (ThemeBasicData) childList.get(j);
						if (layeredList == null) {
							layeredList = new ArrayList<Object>();
						}
						layeredList.add(sbd);

						if (layeredList.size() == childList.size()) {
							// System.out.println(" CHILDREN :::: " +
							// layeredList);
							return layeredList;
						}
					}
				}
			}
		}

		return layeredList;
	}

	/**
	 * Method to set the picture for a node
	 * 
	 * @param picture String holds the info of the pictured
	 */
	private void resolveFixedPicture(String picture) throws ThemeException {
		if (picture == null) {
			return;
		}

		Theme s60 = (Theme) link.getRoot();

		String skinDir = s60.getThemeDir();
		String dir = s60.getModel().getThemeDir();

		String absPath = null;
		File f = null;
		try {
			absPath = FileUtils.makeAbsolutePath(skinDir, picture);
			f = new File(absPath);
			if (f.exists()) {
				link.setFixedPictureResolved(true);
				link.fixedPicture = absPath;
			} else if (dir != null) {
				absPath = FileUtils.makeAbsolutePath(dir, picture);
				f = new File(absPath);

				if (f.exists()) {
					link.setFixedPictureResolved(true);
					link.fixedPicture = absPath;
				} else
					throw new ThemeException(absPath + " : File not found ");
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
	}

	/**
	 * Method to get the picture name for a node
	 * 
	 * @return String file name of the picture
	 */
	public String getFixedPictureName() {
		return link.fixedPictureName;
	}

	/**
	 * Method to get the picture for a node
	 * 
	 * @return String absolute path of the picture
	 */
	public String getFixedPicture() {
		if (link.fixedPicture != null) {

			try {
				if (!isFixedPictureResolved()) {
					link.resolveFixedPicture(link.fixedPicture);
				}
			} catch (ThemeException e) {
				PlatformCorePlugin.error(e);
			}
			return link.fixedPicture;
		} else {

			link.fixedPicture = getForcedPicture();
			link.setFixedPictureResolved(true);
		}
		return link.fixedPicture;
	}

	/**
	 * Helper method to get the picture for a Node
	 */
	private String getForcedPicture() {

		ThemeBasicData parent = link.getParent();
		if (parent != null) {
			String parentPicture = parent.getFixedPicture();
			if (parentPicture == null) {
				parent.getForcedPicture();
			} else
				return parentPicture;
		}

		return null;

	}

	public void setFixedPictureResolved(boolean resolveStatus) {
		link.fixedPictureResolved = resolveStatus;
	}

	private boolean isFixedPictureResolved() {
		return link.fixedPictureResolved;
	}

	/**
	 * Method to return the identifier of the Entity
	 * 
	 * @return String identifier - id if its available , else name
	 */
	public String getIdentifier() {

		String identifier = link.getAttributeValue(ThemeTag.ATTR_ID);
		identifier = (identifier == null) ? link.getThemeName() : identifier;
		return identifier;
	}

	Map map_default_phone = null;

	/**
	 * To set the property as per the phonemodel name
	 */
	public void setDefaultProperties(HashMap map) {
		map_default_phone = map;
	}

	/**
	 * 
	 */
	public Map getDefaultProperties() {
		return map_default_phone;
	}

	/**
	 * @param, map containg attributes of third party icon.
	 */
//	public void setThirdPartyIcon(List list) {
//		// if(list != null)
////		if(!thirdPartyMap.containsAll(list)){
////			thirdPartyMap.addAll(list);
////		}
//	}

	/**
	 * @return, map.
	 */
//	public List getThirdPartyIcon() {
////		return thirdPartyMap;
//	}

	/*
	 * Check for 3rd party icon based on element id
	 */

//	public boolean isThirdPartyIcon(String id) {
//		for (int i = 0; i < thirdPartyMap.size(); i++) {
//			Map tmp = (Map) thirdPartyMap.get(i);
//			if (tmp.containsValue(id))
//				return true;
//		}
//		return false;
//	}

	/**
	 * for setting the property
	 */
	public void setCurrentProperty(String property) {
		link.currentProp = property;
	}

	/**
	 * for getting the property
	 */
	public String getCurrentProperty() {
		return link.currentProp;
	}

	public boolean supportDrafts() {
		return false;
	}

	public boolean supportsPlatform(IPlatform platform) {
		return ((Theme) getRoot()).supportsPlatform(platform, getIdentifier());
	}

	public Layout getLayoutForPreviewNonFrame(Display display,
			ComponentInfo component) throws ThemeException {
		try {
			getLayoutContext(display).calculate(component);
			return component.getLayout();
		} catch (Exception e) {
			throw new ThemeException(e);
		}
	}

	public ComponentInfo getComponentInfo(Display display, String skinId) {
		return ThemePlatform.getIdMappingsHandler(
				((Theme) getRoot()).getThemeId()).getComponentInfo(this,
				display, skinId);
	}

	public Layout getLayoutInfo(Display display, String skinId)
			throws ThemeException {
		ComponentInfo component = getComponentInfo(display, skinId);
		try {
			getLayoutContext(display).calculate(component);
		} catch (LayoutException e) {
			throw new ThemeException(e);
		}
		return component.getLayout();
	}

	public boolean supportsDisplay(Display display) {
		return true;
	}

	public boolean isSameTarget(ThemeBasicData data) {
		return this == data || getLink() == data
				|| (getLink() != null && getLink() == data.getLink());
	}

	public Object getImageCacheGroup() {
		return getRoot();
	}

	public String getImageCacheKey(int width, int height) {
		return getImageCacheKey() + width + "x" + height;
	}

	public String getImageCacheKey() {
		return getIdentifier() + "!";
	}

	/**
	 * Method to get a specific information from a tagged string
	 * 
	 * @param taggedString String holding an entity's collective information
	 * @param tag String holds the specific tag for which the value is needed
	 * @return String value of the tag
	 */
	public static String getTaggedInfo(String taggedString, String tag) {

		int i = taggedString.indexOf(tag);
		String temp = null;

		if (i != -1) {
			temp = taggedString.substring(i + tag.length());

			StringBuffer sb = new StringBuffer(tag);
			sb.insert(1, "/");

			String s[] = temp.split(sb.toString());

			temp = s[0];
		}
		return temp;
	}

	public int getChildIndex(ThemeBasicData child) {
		if (link.children != null) {

			for (int i = 0; i < link.children.size(); i++) {
				ThemeBasicData sbd = (ThemeBasicData) (link.children.get(i));

				if ((sbd == child)) {
					return i;
				}
			}
		}

		return ThemeTag.CHILD_NOT_FOUND;
	}
	
	protected void copyProperties(ThemeBasicData themeBasicData){
		currentProp = themeBasicData.currentProp;
		parent = themeBasicData.parent;
		isSelected = themeBasicData.isSelected;
		isShown = themeBasicData.isShown;
		isSkinned = themeBasicData.isSkinned;
		attributes = themeBasicData.attributes;
		toolBox = themeBasicData.toolBox;
		properties = themeBasicData.properties;
		designrectangles = themeBasicData.designrectangles;
		children = themeBasicData.children;
		link = themeBasicData.link;
		fixedPicture = themeBasicData.fixedPicture;
		fixedPictureName = themeBasicData.fixedPictureName;
		fixedPictureResolved = themeBasicData.fixedPictureResolved;
		isSelectedForTransfer = themeBasicData.isSelectedForTransfer;
		animatedSkinnableEntityList = themeBasicData.animatedSkinnableEntityList;
		executed = themeBasicData.executed;
	}
	
	/**
	 * Multipiece elements can be switched to single piece. When such switching happens,
	 * their current property is made as ThemeTag.ATTR_SINGLE_BITMAP. Originally, a multi
	 * piece element is identified by how many designrectangle tags exist in defaultdesign.xml.
	 * 
	 * @return true if the element was originally a multipiece element and has now been switched to single piece. 
	 */
	public boolean isConvertedFromMultipiece() {		
		boolean isSingleBitmapNow = (getCurrentProperty().equals(ThemeTag.ATTR_SINGLE_BITMAP));
		boolean originallyMultiPiece = (getDesignRectangles().size() > 1);
		boolean isConvertedElement = (originallyMultiPiece && isSingleBitmapNow);
		return isConvertedElement;
	}
}
