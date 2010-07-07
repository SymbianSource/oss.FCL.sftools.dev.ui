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

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.layout.LayoutContext;
import com.nokia.tools.platform.layout.LayoutInfo;
import com.nokia.tools.platform.layout.LayoutManager;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.platform.theme.preview.ThemePreview;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.SimpleCache;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.resource.util.DebugHelper.SilentRunnable;

public abstract class Theme
    extends ThemeBasicData {

	private static final String DISPLAY_FORMAT = "{0}x{1}{2}";

	// holds the skin file
	private File themeFile;

	// holds the directory of the skin's xml file
	private String tempDir;

	
	private ThemePreview themePreview;

	// holds the list of langugage names that the skin has
	private Map<Integer, String> skinNames = new HashMap<Integer, String>();

	// holds the filenames which are to be considered to get the Item Ids for
	// the theme's phone model
	private List<String> itemIdFiles;

	private HashMap<String, HashMap<Object, Object>> skinSettingsMap;

	private HashMap<Object, Object> dimensionsMap;

	private ThemeCache previewCache = new ThemeCache(
	    ThemeConstants.PREVIEW_CACHE_NAME);

	private ThemeCache imageCache = new ThemeCache(
	    ThemeConstants.IMAGE_CACHE_NAME);

	private ThemeCache backgroundLayerCache = new ThemeCache(
	    ThemeConstants.BACKGROUND_LAYER_CACHE_NAME);

	private String modelId;

	private Theme model;

	private ThemeLayoutContext layout;

	private Map<String, String> soundFormats;

	// stores this here because it's used in many places
	private Map<String, Element> elementMap;

	private Map<String, ExtraEntityWrapper> extraEntities = new HashMap<String, ExtraEntityWrapper>();

	private Map<String, Object> runtimeAttr = new HashMap<String, Object>();

	private Set<Display> displays;

	private Map<Display, Map<String, Set<String>>> backgroundDependencies = Collections
	    .synchronizedMap(new HashMap<Display, Map<String, Set<String>>>());

	private ILayoutContextProvider layoutContextProvider;

	public Theme(String modelId) {
		if (modelId == null) {
			throw new NullPointerException();
		}
		this.modelId = modelId;
		model = this;
	}

	public Theme(Theme model) {
		try {
			setModel(model);
		} catch (ThemeException e) {
			// should never happen
		}
	}

	public Theme getModel() {
		return model;
	}

	public boolean isModel() {
		return model == this;
	}

	public String getModelId() {
		return modelId;
	}

	public String getDefaultModelId() {
		IThemeDescriptor descriptor = getThemeDescriptor();
		if (descriptor == null) {
			return modelId;
		}
		IThemeModelDescriptor modelDescriptor = ThemePlatform
		    .getDefaultThemeModelDescriptor(descriptor.getContainerId());
		return modelDescriptor == null ? null : modelDescriptor.getId();
	}

	public String[] getAllModelIds() {
		Set<String> ids = new LinkedHashSet<String>();
		ids.add(getModelId().toLowerCase());
		for (ExtraEntityWrapper wrapper : extraEntities.values()) {
			ids.add(wrapper.modelId.toLowerCase());
		}
		ids.add(getDefaultModelId().toLowerCase());
		return ids.toArray(new String[ids.size()]);
	}

	public IThemeDescriptor getThemeDescriptor() {
		IThemeModelDescriptor desc = ThemePlatform
		    .getThemeModelDescriptorById(modelId);
		return desc == null ? null : desc.getThemeDescriptor();
	}

	public String getThemeId() {
		IThemeDescriptor desc = getThemeDescriptor();
		return desc == null ? null : desc.getId();
	}

	public void setModel(Theme model) throws ThemeException {
		if (model == null) {
			throw new NullPointerException();
		}
		if (isModel()) {
			throw new IllegalArgumentException(
			    "Can't switch model to another one");
		}
		if (this.model == model) {
			return;
		}
		String oldModelId = this.modelId;
		this.model = model;
		this.modelId = model.getModelId();

		if (oldModelId != null) {
			merge(oldModelId, model);
			getLayout().setDisplay(getDefaultDisplay());
		}
	}

	/**
	 * @return the layoutContextProvider
	 */
	public ILayoutContextProvider getLayoutContextProvider() {
		return layoutContextProvider;
	}

	/**
	 * @param layoutContextProvider the layoutContextProvider to set
	 */
	public void setLayoutContextProvider(
	    ILayoutContextProvider layoutContextProvider) {
		this.layoutContextProvider = layoutContextProvider;
	}

	private synchronized ThemeLayoutContext getLayout() {
		if (layout == null) {
			layout = new ThemeLayoutContext(getThemeId());
		}
		return layout;
	}

	public LayoutContext getLayoutContext() {
		return getLayout().getLayoutContext();
	}

	public LayoutContext getLayoutContext(Display display) {
		if (layoutContextProvider != null) {
			LayoutContext context = layoutContextProvider
			    .getLayoutContext(display);
			if (context != null) {
				return context;
			}
		}
		return getLayout().getLayoutContext(display);
	}

	public void setDevice(IDevice device) {
		getLayout().setDevice(device);
	}

	public void setDisplay(Display display) {
		getLayout().setDisplay(display);
	}

	public Display getDisplay() {
		return getLayout().getDisplay();
	}

	public Display getDefaultDisplay() {
		return ThemePlatform.getThemeDescriptorById(getThemeId())
		    .getDefaultDevice().getDisplay();
	}

	public boolean supportsDisplay(Display display) {
		return getDisplays().contains(display);
	}

	public synchronized Set<Display> getDisplays() {
		if (displays == null) {
			displays = new HashSet<Display>();
			for (IDevice device : ThemePlatform
			    .getDevicesByThemeId(getThemeId())) {
				displays.add(device.getDisplay());
			}
		}
		return displays;
	}

	/**
	 * @return the soundFormats
	 */
	public Map<String, String> getSoundFormats() {
		return soundFormats;
	}

	/**
	 * @param soundFormats the soundFormats to set
	 */
	public void setSoundFormats(Map<String, String> soundFormats) {
		this.soundFormats = soundFormats;
	}

	public Set<String> getSoundExtensions() {
		Set<String> set = new HashSet<String>(soundFormats.size());
		if (soundFormats != null) {
			for (String extension : soundFormats.values()) {
				set.add(extension);
			}
		}
		return set;
	}

	/**
	 * Returns the temp directory
	 */

	public String getTempDir() throws ThemeException {
		if (tempDir == null) {
			tempDir = FileUtils.generateUniqueDirectory("ThemeTool");

			if (tempDir == null)
				tempDir = FileUtils.generateUniqueDirectory("ThemeTool");
		}

		
		FileUtils.addForCleanup(new File(tempDir));

		return tempDir;
	}

	/**
	 * @return Returns the themeFile.
	 */
	public File getThemeFile() {
		return themeFile;
	}

	/**
	 * @param themeFile The themeFile to set.
	 */
	public void setThemeFile(File themeFile) {
		this.themeFile = themeFile;
	}

	/**
	 * Method to return the directory of the skin's xml file
	 * 
	 * @return String Directory of the skin's xml file
	 */
	public String getThemeDir() {
		if (getThemeFile() != null) {
			return getThemeFile().getParentFile().getAbsolutePath();
		}
		return null;
	}

	public void setSkinSettings(
	    HashMap<String, HashMap<Object, Object>> lineColours) {
		this.skinSettingsMap = lineColours;
	}

	public HashMap<String, HashMap<Object, Object>> getSkinSettings() {
		return this.skinSettingsMap;
	}

	public void setDimensions(HashMap<Object, Object> dimensionsMap) {
		this.dimensionsMap = dimensionsMap;
	}

	public HashMap getDimensions() {
		return this.dimensionsMap;
	}

	/**
	 * Method to return the file name of the skin image
	 * 
	 * @return ThemeGraphic
	 */
	public RenderedImage getBackGround() {
		SkinnableEntity se = getSkinnableEntity(ThemeTag.ELEMENT_BACKGROUND_NAME);

		if (!isModel()) {
			try {
				ThemeGraphic tg = se.getActualThemeGraphic();
				if (tg != null)
					return ThemeappJaiUtil.getElementImage(se);
			} catch (ThemeException e) {
				PlatformCorePlugin.error(e);
			}
		}
		return null;
	}

	/**
	 * Sets the name of the node (for english language)
	 * 
	 * @param sName name of the node
	 */
	public void setThemeName(String name) {
		synchronized (this) {
			attributes.put(ThemeTag.ATTR_ENG_NAME, name);
			setLangName(1, name);
		}
	}

	/**
	 * Returns the name of the node
	 * 
	 * @return String name of the node
	 */
	public String getThemeName() {
		return (String) skinNames.get(new Integer(1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#isChildValid(java.lang.Object)
	 */
	@Override
	public boolean isChildValid(Object obj) {
		return obj instanceof Task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#addChild(int,
	 *      java.lang.Object)
	 */
	public void addChild(int position, Object objTask) throws ThemeException {
		try {
			Task task = (Task) objTask;
			String name = task.getThemeName();

			Task existingTask = null;
			boolean duplicate = false;

			String taskId = null;
			String existingTaskId = null;

			if (link.children != null) {
				for (int i = 0; i < link.children.size(); i++) {
					existingTask = (Task) link.children.get(i);
					if (task.getAttribute().containsKey(
					    ThemeTag.ATTR_CHILD_NAME)) {
						taskId = task
						    .getAttributeValue(ThemeTag.ATTR_CHILD_NAME);
						existingTaskId = existingTask
						    .getAttributeValue(ThemeTag.ATTR_CHILD_NAME);

						if (existingTaskId != null) {
							if (existingTaskId.equalsIgnoreCase(taskId)) {
								return;
							}
						}
					} else {
						if ((existingTask.getThemeName())
						    .equalsIgnoreCase(name)) {
							duplicate = true;
							break;
						}
					}
				}
			}

			if (!duplicate) {
				synchronized (this) {
					task.setParent(this);
					if (link.children == null)
						link.children = new ArrayList<Object>();
					if ((position < 0) || (position > link.children.size()))
						link.children.add(task);
					else
						link.children.add(position, task);
				}
			} else {
				if ((task.getAttributeValue("overwrite") != null)
				    && (task.getAttributeValue("overwrite").equals("true")))
					existingTask.overWrite(task);
				else
					existingTask.merge(task);
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
			throw new ThemeException("Adding task to S60 failed : "
			    + e.getMessage());
		}
	}

	/**
	 * Method to get all the elements related to a Skin
	 * 
	 * @return List list of elements
	 */
	public synchronized Map<String, Element> getAllElements() {
		// model elements are cached too because there are significant amount of
		// hits for default graphics
		// if (isModel()) {
		// return getAllElements0();
		// }

		/*
		 * temp fix for component store - we need to know actual list of
		 * elements, not cached one
		 */
		if (runtimeAttr.get("ComponentStore") != null)
			return getAllElements0();

		if (elementMap == null) {
			elementMap = getAllElements0();
		}
		return elementMap;
	}

	public Collection getAllElements(boolean includeExtra) {
		Map<String, Element> elements = getAllElements();
		if (!includeExtra || extraEntities.isEmpty()) {
			return elements.values();
		}
		List newElements = new ArrayList(elements.values());
		if (includeExtra) {
			for (ExtraEntityWrapper wrapper : extraEntities.values()) {
				newElements.add(wrapper.entity);
			}
		}
		return newElements;
	}

	public synchronized void refreshElementList() {
		elementMap = null;
		getAllElements();
	}

	protected synchronized Map<String, Element> getAllElements0() {
		Map<String, Element> elementMap = new LinkedHashMap<String, Element>();

		// for loop for all the tasks
		List tasks = getChildren();

		if (tasks != null) {
			for (int i = 0; i < tasks.size(); i++) {
				// for loop for getting the component group for each task
				List componentGroups = ((ThemeBasicData) tasks.get(i))
				    .getChildren();

				if (componentGroups != null) {
					for (int j = 0; j < componentGroups.size(); j++) {
						// for loop for getting the component of each group
						List components = ((ThemeBasicData) componentGroups
						    .get(j)).getChildren();

						if (components != null) {
							for (int k = 0; k < components.size(); k++) {
								// for loop for getting the elements
								List<Object> elements = ((ThemeBasicData) components
								    .get(k)).getChildren();
								if (elements != null) {
									for (Object obj : elements) {
										Element element = (Element) obj;
										if (element.isLink()) {
											continue;
										}
										String id = element
										    .getAttributeValue(ThemeTag.ATTR_ID);
										if (id != null) {											
											elementMap.put(id.toLowerCase(),
											    element);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return elementMap;
	}

	/**
	 * Method to get all the tasks related to a Skin
	 * 
	 * @return List list of tasks
	 */
	private List getAllTasks() {
		List taskList = getChildren();
		return taskList;
	}

	/**
	 * Method to get all the componentGroups related to a Skin
	 * 
	 * @return List list of componentGroups
	 */
	private List getAllComponentGroups() {
		List<Object> componentGroupsList = new ArrayList<Object>();

		// for loop for all the tasks
		List tasks = getChildren();
		if (tasks != null) {
			for (int i = 0; i < tasks.size(); i++) {
				// for loop for getting the component group for each task
				List<Object> componentGroups = ((ThemeBasicData) tasks.get(i))
				    .getChildren();
				if (componentGroups != null) {
					componentGroupsList.addAll(componentGroups);
				}
			}
		}
		return componentGroupsList;
	}

	/**
	 * Method to get all the components related to a Skin
	 * 
	 * @return List list of components
	 */
	public List getAllComponents() {
		List<Object> componentsList = new ArrayList<Object>();

		// for loop for all the tasks
		List tasks = getChildren();
		if (tasks != null) {
			for (int i = 0; i < tasks.size(); i++) {
				// for loop for getting the component group for each task
				List componentGroups = ((ThemeBasicData) tasks.get(i))
				    .getChildren();
				if (componentGroups != null) {
					for (int j = 0; j < componentGroups.size(); j++) {
						// for loop for getting the component of each group
						List<Object> components = ((ThemeBasicData) componentGroups
						    .get(j)).getChildren();
						if (components != null) {
							componentsList.addAll(components);
						}
					}
				}
			}
		}
		return componentsList;
	}

	/**
	 * Method to get the Task with the specified name
	 * 
	 * @return ThemeBasicData task object with the specified name
	 */
	public ThemeBasicData getTask(String entityName) {
		return getTask(entityName, false);
	}

	/**
	 * Method to get the Task with the specified name
	 * 
	 * @return ThemeBasicData task object with the specified name
	 */
	public ThemeBasicData getTask(String entityName, boolean canBeNull) {
		ThemeBasicData sbd = null;
		ThemeBasicData ret = null;
		List tasks = getAllTasks();
		if (tasks != null) {
			for (int i = 0; i < tasks.size(); i++) {
				sbd = (ThemeBasicData) tasks.get(i);
				// If it is a link ignore it since we are concerned only about
				// the actual entity
				if (sbd.isLink()) {
					continue;
				}

				if ((sbd.getThemeName()).equalsIgnoreCase(entityName)) {
					return sbd;
				}
			}
		}
		if (canBeNull) {
			return ret;
		} else {
			return sbd;
		}
	}

	/**
	 * Method to get the ComponentGroup with the specified name
	 * 
	 * @return ThemeBasicData ComponentGroup object with the specified name
	 */
	public ThemeBasicData getComponentGroup(String entityName) {
		return getComponentGroup(entityName, false);
	}

	/**
	 * Method to get the ComponentGroup with the specified name
	 * 
	 * @return ThemeBasicData ComponentGroup object with the specified name
	 */
	public ThemeBasicData getComponentGroup(String entityName, boolean canBeNull) {
		ThemeBasicData sbd = null;
		ThemeBasicData ret = null;
		List componentGroups = getAllComponentGroups();
		if (componentGroups != null) {
			for (int i = 0; i < componentGroups.size(); i++) {
				sbd = (ThemeBasicData) componentGroups.get(i);
				// If it is a link ignore it since we are concerned only about
				// the actual entity
				if (sbd.isLink()) {
					continue;
				}

				if ((sbd.getThemeName()).equalsIgnoreCase(entityName)) {
					return sbd;
				}
			}
		}
		if (canBeNull) {
			return ret;
		} else {
			return sbd;
		}
	}

	/**
	 * Method to get the Component with the specified name
	 * 
	 * @return ThemeBasicData Component object with the specified name
	 */
	public ThemeBasicData getComponent(String entityName) {
		return getComponent(entityName, false);
	}

	/**
	 * Method to get the Component with the specified name
	 * 
	 * @return ThemeBasicData Component object with the specified name
	 */
	public ThemeBasicData getComponent(String entityName, boolean canBeNull) {
		ThemeBasicData sbd = null;
		ThemeBasicData ret = null;
		List components = getAllComponents();

		if (components != null) {
			for (int i = 0; i < components.size(); i++) {
				sbd = (ThemeBasicData) components.get(i);
				// If it is a link ignore it since we are concerned only about
				// the actual entity
				if (sbd.isLink()) {
					continue;
				}

				if ((sbd.getThemeName()).equalsIgnoreCase(entityName)) {
					return sbd;
				}
			}
		}
		if (canBeNull) {
			return ret;
		} else {
			return sbd;
		}
	}

	/**
	 * Method to get an Element with its Id
	 * 
	 * @param elementId String holding an element's id
	 * @return Element object
	 */
	public Element getElementWithId(String elementId) {
		return getElementWithId(elementId, false);
	}

	public Element getElementWithId(String elementId, boolean includeExtra) {
		if (elementId == null) {
			return null;
		}

		Map<String, Element> elementMap = getAllElements();
		if (!includeExtra) {
			return elementMap.get(elementId.toLowerCase());
		}
		for (ExtraEntityWrapper wrapper : extraEntities.values()) {
			if (wrapper.entity instanceof Element
			    && elementId.equalsIgnoreCase(wrapper.entity.getIdentifier())) {
				return (Element) wrapper.entity;
			}
		}
		return null;
	}

	/**
	 * Method to get an Element with its Name
	 * 
	 * @param elementId String holding an element's Name
	 * @return Element object
	 */
	public Element getElementWithName(String elementName) {
		return getElementWithName(elementName, false);
	}

	public Element getElementWithName(String elementName, boolean includeExtra) {
		if (elementName == null) {
			return null;
		}

		// no optimization here because this is called less frequently
		Map<String, Element> elementMap = getAllElements();
		for (Element element : elementMap.values()) {
			if (elementName.equalsIgnoreCase(element.getThemeName())) {
				return element;
			}
		}
		if (includeExtra) {
			for (ExtraEntityWrapper wrapper : extraEntities.values()) {
				if (wrapper.entity instanceof Element
				    && elementName.equalsIgnoreCase(wrapper.entity
				        .getThemeName())) {
					return (Element) wrapper.entity;
				}
			}
		}
		return null;
	}

	/**
	 * Method to get an Element with its App UId
	 * 
	 * @param elementUId String holding an element's application UId
	 * @return Element object
	 */
	public Element getElementWithAppUId(String elementAppUId) {
		if (elementAppUId == null) {
			return null;
		}

		Map<String, Element> elementMap = getAllElements();
		for (Element element : elementMap.values()) {
			if (elementAppUId.equalsIgnoreCase(element
			    .getAttributeValue(ThemeTag.ATTR_APPUID))) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Method to get an Element with its Major and Minor Id
	 * 
	 * @param elementMajorId String holding an element's Major Id
	 * @param elementMinorId String holding an element's Minor Id
	 * @return Element object
	 */
	public Element getElementWithMajorMinorId(String elementMajorId,
	    String elementMinorId) {
		if ((elementMajorId == null) || (elementMinorId == null)) {
			return null;
		}

		Map<String, Element> elementMap = getAllElements();
		for (Element element : elementMap.values()) {
			if ((elementMajorId.equalsIgnoreCase(element
			    .getAttributeValue(ThemeTag.ATTR_MAJORID)))
			    && (elementMinorId.equalsIgnoreCase(element
			        .getAttributeValue(ThemeTag.ATTR_MINORID)))) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Method to get an Element/Part with the specified identifier Equivalent to
	 * call getElement (s) followed by getPart(s)
	 * 
	 * @param s String holding an element's/part's identifier
	 * @return ThemeBasicData object
	 */

	public SkinnableEntity getSkinnableEntity(String s) {
		return getSkinnableEntity(s, false);
	}

	public SkinnableEntity getSkinnableEntity(String s, boolean includeExtra) {
		return getSkinnableEntity(s, includeExtra, true);
	}

	public SkinnableEntity getSkinnableEntity(String s, boolean includeExtra,
	    boolean searchName) {
		if (s == null || s.trim().length() <= 0) {
			return null;
		}
		String identifier = getTaggedInfo(s, ThemeTag.TAG_ID_START);
		if (identifier == null) {
			identifier = getTaggedInfo(s, ThemeTag.TAG_NAME_START);
		}

		if (identifier != null) {
			s = identifier;
		}

		SkinnableEntity entity = getElementWithId(s, includeExtra);

		if (entity == null)
			entity = getPartWithId(s, includeExtra);

		if (entity == null && searchName)
			entity = getElementWithName(s, includeExtra);

		return entity;
	}

	public SkinnableEntity getSkinnableEntityById(String s) {
		return getSkinnableEntity(s, false, false);
	}

	/**
	 * Function to set the name of the skin corresponding to a language
	 * 
	 * @param langId The number that represents the language id
	 * @param name The name in the language specified by langId
	 */
	public void setLangName(int langId, String name) {
		synchronized (this) {
			skinNames.put(langId, name);
		}
	}

	/**
	 * Function to set the name of the skin corresponding to a language
	 * 
	 * @param names A map containing the langid (as key) and corresponding name
	 *            (as value)
	 */
	public void setLangName(Map<Integer, String> names) {
		synchronized (this) {
			skinNames.putAll(names);
		}
	}

	/**
	 * Function that returns all the names associated with the skin
	 * 
	 * @return A map with key as the language id and value as the name of the
	 *         skin in the particular language
	 */
	public Map<Integer, String> getLangName() {
		return skinNames;
	}

	/**
	 * Method to set the package
	 * 
	 * @param packageName String holding the name of the package
	 */
	public void setPackage(String packageName) {

		// Remove all spaces. Currently the batch files and bmconv does not
		// support names with spaces
		// String temp = packageName.trim().replace(' ', '_');

		String temp;
		StringBuffer newStr = new StringBuffer();

		StringTokenizer st = new StringTokenizer(packageName.trim(), " ");

		while (st.hasMoreTokens()) {

			int len = newStr.length();

			temp = st.nextToken();

			newStr.append(temp);
			newStr.setCharAt(len, Character.toUpperCase(temp.charAt(0)));
		}

		synchronized (this) {
			attributes.put(ThemeTag.ATTR_PACKAGE, newStr.toString());
		}
	}

	/**
	 * Method to get the package
	 * 
	 * @return String name of the package
	 */
	public String getPackage() {
		// String pkgName = (String) getAttributeValue(ThemeTag.ATTR_PACKAGE);
		//
		// if (pkgName == null) {
		//
		// if (getThemeName() != null)
		// setPackage(getThemeName());
		// return (String) getAttributeValue(ThemeTag.ATTR_PACKAGE);
		// }
		//
		// return pkgName;
		// the name1 will be used inside .pkg and file name can be the same.

		// we're using this because the above routine doesn't work when the name
		// contains the characters that are illegal to the command line tools
		return "themepackage";
	}

	/**
	 * Method to clone the object
	 * 
	 * @return Object cloned object
	 */
	public Object clone() {

		Theme skinObj = (Theme) super.clone();

		// never clones the runtime attributes, shared reference and makes the
		// all themes to be dynamic and thus destroys the performance
		skinObj.runtimeAttr = new HashMap<String, Object>();
		skinObj.model = isModel() ? this : model;
		// don't clone the element cache
		skinObj.elementMap = null;
		clonePreview(skinObj);

		// clone the extraentities Map
		HashMap m = new HashMap<Object, Object>(this.extraEntities);
		m = (HashMap) m.clone();
		skinObj.extraEntities = (Map) m;
		
		// clone the skinNames Map
		m = new HashMap<Object, Object>(this.skinNames);
		m = (HashMap) m.clone();
		skinObj.skinNames = (Map) m;

		// clone the attributes Map
		m = new HashMap<Object, Object>(this.attributes);
		m = (HashMap) m.clone();
		skinObj.attributes = (Map) m;

		if (dimensionsMap != null) {
			m = new HashMap<Object, Object>(this.dimensionsMap);
			m = (HashMap) m.clone();
			skinObj.dimensionsMap = (HashMap) m;
		}
		if (skinSettingsMap != null) {
			m = new HashMap<Object, Object>(this.skinSettingsMap);
			m = (HashMap) m.clone();
			Iterator it = this.skinSettingsMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next().toString();
				HashMap<Object, Object> temp = this.skinSettingsMap.get(key);
				HashMap m1 = new HashMap<Object, Object>(temp);
				m1 = (HashMap) m1.clone();
				m.put(key, m1);
			}

			skinObj.skinSettingsMap = (HashMap) m;
		}

		// clone the toolBox
		if (skinObj.toolBox != null)
			skinObj.toolBox = (ToolBox) (this.toolBox.clone());

		// clone the chilren List
		cloneChildren(skinObj);

		skinObj.clear();
		return skinObj;
	}

	protected void clear() {
		if (!isModel()) {
			backgroundDependencies = null;
		}
		if (imageCache != null) {
			imageCache = new ThemeCache(ThemeConstants.IMAGE_CACHE_NAME);
		}

		if (previewCache != null) {
			previewCache = new ThemeCache(ThemeConstants.PREVIEW_CACHE_NAME);
		}

		if (backgroundLayerCache != null) {
			backgroundLayerCache = new ThemeCache(
			    ThemeConstants.BACKGROUND_LAYER_CACHE_NAME);
		}

		clearThemeGraphics();
		// layout/elements shouldn't be cloned
		layout = null;
		elementMap = null;
		displays = null;
	}

	protected void cloneChildren(Theme skinObj) {
		if (this.children != null) {
			List l = (List) this.getChildren();
			ArrayList<Object> a = new ArrayList<Object>(l);

			a = (ArrayList) a.clone();

			for (int i = 0; i < a.size(); i++) {
				ThemeBasicData s = (ThemeBasicData) a.get(i);
				s = (ThemeBasicData) s.clone();
				// clone the parent
				s.setParent(skinObj);
				a.set(i, s);
			}
			skinObj.children = (List<Object>) a;
		}
		try {
			skinObj.updateLinks();
		} catch (Exception e) {
			// System.out.println("update links in clone failed : " +
			// e.getMessage());
			// Debug.out(this,"update links in clone failed : " +
			// e.getMessage());
		}
	}

	protected void clonePreview(Theme skinObj) {
		ThemePreview themePreview = (ThemePreview) ((ThemeBasicData) this.themePreview)
		    .clone();
		themePreview.setTheme(skinObj);

		((Theme) skinObj).clearThemePreview();
		((Theme) skinObj).setThemePreview(themePreview);
		themePreview.intializePreviewController();
	}

	/**
	 * Method to get a Part with its Id
	 * 
	 * @param partId String holding a part's id
	 * @return ThemeBasicData object
	 */
	public Part getPartWithId(String partId) {
		return getPartWithId(partId, false);
	}

	public Part getPartWithId(String partId, boolean includeExtra) {
		if (partId == null)
			return null;

		Collection elementList = getAllElements(includeExtra);
		if (elementList != null) {
			for (Object obj : elementList) {
				if (obj instanceof Element) {
					Element elem = (Element) obj;
					List partList = elem.getChildren();
					if (partList != null) {
						for (int j = 0; j < partList.size(); j++) {
							Part part = (Part) partList.get(j);
							// If it is a link ignore it since we are concerned
							// only
							// about the actual entity
							if (part.isLink()) {
								continue;
							}

							String sId = part
							    .getAttributeValue(ThemeTag.ATTR_ID);
							if ((sId != null) && partId.equalsIgnoreCase(sId)) {
								return part;
							}
						}
					}
				} else if (obj instanceof Part) {
					// from extra entities
					Part part = (Part) obj;
					if (part.isLink()) {
						continue;
					}
					String sId = part.getAttributeValue(ThemeTag.ATTR_ID);
					if ((sId != null) && partId.equalsIgnoreCase(sId)) {
						return part;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Method to update the links on the S60 object
	 * 
	 * @param skinDetails Theme in which link has to be updated
	 * @return Theme updated Theme object
	 * @throws ThemeAppParserException If not able to successfully set the links
	 */
	private void updateLinks() throws ThemeException {

		// Debug.out( this , "inside links updation.................");

		ThemeBasicData link = null;
		// for loop for all the tasks
		List tasks = getChildren();

		if (tasks != null) {

			for (int i = 0; i < tasks.size(); i++) {
				ThemeBasicData task = (ThemeBasicData) tasks.get(i);
				if (task.isLink()) {
					link = task.getLink();
					link = getTask(link.getThemeName());
					if (link == null) {
						throw new ThemeException(
						    "The link is not found for the task : "
						        + link.getThemeName());
					}
					task.setLink(link);
					link = null;
					continue;
				}

				// for loop for getting the component group for each task
				List componentGroups = task.getChildren();
				if (componentGroups != null) {
					for (int j = 0; j < componentGroups.size(); j++) {
						ThemeBasicData compG = (ThemeBasicData) componentGroups
						    .get(j);
						if (compG.isLink()) {
							link = compG.getLink();
							link = getComponentGroup(link.getThemeName());
							if (link == null) {
								throw new ThemeException(
								    "The link is not found for the componentGroup : "
								        + link.getThemeName());
							}
							compG.setLink(link);
							link = null;
							continue;
						}

						// for loop for getting the component of each group
						List components = compG.getChildren();
						if (components != null) {
							for (int k = 0; k < components.size(); k++) {
								ThemeBasicData comp = (ThemeBasicData) components
								    .get(k);
								if (comp.isLink()) {
									link = comp.getLink();
									link = getComponent(link.getThemeName());
									if (link == null) {
										throw new ThemeException(
										    "The link is not found for the component : "
										        + link.getThemeName());
									}
									comp.setLink(link);
									link = null;
									continue;
								}

								// for loop for getting the elements
								List elements = comp.getChildren();
								if (elements != null) {
									for (int l = 0; l < elements.size(); l++) {
										ThemeBasicData elem = (ThemeBasicData) elements
										    .get(l);
										if (elem.isLink()) {
											link = elem.getLink();
											String identifier = link
											    .getAttributeValue(ThemeTag.ATTR_ID);
											identifier = (identifier == null) ? link
											    .getAttributeValue(ThemeTag.ATTR_NAME)
											    : identifier;

											if (identifier != null) {
												link = getSkinnableEntity(identifier);
												if (link == null) {
													throw new ThemeException(
													    "The link is not found for the element : "
													        + identifier);
												}

												if (!(link instanceof Element)) {
													throw new ThemeException(
													    "The link found for the element : "
													        + identifier
													        + " is not of type Element");
												}
												elem.setLink(link);
												link = null;
												continue;
											}
										}

										// for loop for getting the parts
										List parts = elem.getChildren();

										if (parts != null) {
											for (int m = 0; m < parts.size(); m++) {
												ThemeBasicData part = (ThemeBasicData) parts
												    .get(m);
												if (part.isLink()) {
													link = part.getLink();
													String identifier = link
													    .getAttributeValue(ThemeTag.ATTR_ID);
													if (identifier != null) {
														link = getSkinnableEntity(identifier);

														if (link == null) {
															throw new ThemeException(
															    "The link is not found for the part : "
															        + identifier);
														}
														if (!(link instanceof Part)) {
															throw new ThemeException(
															    "The link found for the part : "
															        + identifier
															        + " is not of type Part");
														}
														part.setLink(link);
														link = null;
														continue;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Method to return the Color for a given colourGroup
	 * 
	 * @param colourGroup String holds the colour group
	 * @return Color object
	 * @throws ThemeAppParserException If getting colour fails
	 */
	public Color getRGBColour(String colourGroup) throws ThemeException {
		return getRGBColour(colourGroup, 0);
	}

	/**
	 * Method to return the Color for a given colourGroup
	 * 
	 * @param colourGroup String holds the colour group
	 * @return Color object
	 * @return listType 0-calls getImage, 1-calls getPreviewImage 2-calls
	 *         getEditedImage
	 * @throws ThemeAppParserException If getting colour fails
	 */
	public Color getRGBColour(String colourGroup, int listType)
	    throws ThemeException {

		if (colourGroup == null) {

			return null;
		}

		colourGroup = colourGroup.toLowerCase();

		if (colourGroup.equalsIgnoreCase(ThemeTag.COLOUR_WHITE)) {

			return new Color(255, 255, 255);
		}

		SkinnableEntity se = getSkinnableEntity(colourGroup);

		if (se != null) {

			ThemeGraphic tg = null;

			switch (listType) {
				case 1:
				case 2:
					tg = se.getPreviewThemeGraphic();
					break;
				default:
					tg = se.getThemeGraphic();
			}

			if (tg != null) {
				List ilList = tg.getImageLayers();
				if (ilList != null) {
					ImageLayer il = (ImageLayer) ilList.get(0);
					String colourValue = il
					    .getAttribute(ThemeTag.ATTR_COLOUR_RGB);
					colourValue = (colourValue == null) ? il
					    .getAttribute(ThemeTag.ATTR_COLOUR_IDX) : colourValue;

					if (colourValue == null) {
						return null;
					}

					try {

						if (colourValue.startsWith("0x")
						    || colourValue.startsWith("0X")) {
							return ColorUtil.toColor(colourValue);
						} else {
							return Color.decode(colourValue);
						}

					} catch (Exception e) {
						throw new ThemeException(
						    "Failed in hex to RGB conversion : "
						        + e.getMessage());
					}
				}
			}
		}

		// }

		return null;
	}

	/*
	 * Method to set the itemId files @param itemIdFiles List holding the itemId
	 * files
	 */
	public void setItemIdFiles(List<String> idFiles) {
		/*
		 * if there r multiple phone models... and each is having its own list
		 * of item id files, maintain it in a single list at the S60 obj
		 */
		if (this.itemIdFiles == null)
			this.itemIdFiles = new ArrayList<String>();
		if (idFiles != null)
			this.itemIdFiles.addAll(idFiles);
	}

	/*
	 * Method to get the itemId files @return List holds the itemId files
	 */
	public List getItemIdFiles() {
		return this.itemIdFiles;
	}

	/*
	 * Method to clear the Skin Preview This method is used in clone()
	 */
	private void clearThemePreview() {
		this.themePreview = null;
	}

	public void clearAllCaches() {
		getPreviewCache().clearElementCache();
		getPreviewCache().clearPreviewScreenCache();
		getBackgroundLayerCache().clearElementCache();
		getBackgroundLayerCache().clearPreviewScreenCache();
		getImageCache().clearElementCache();
		getImageCache().clearPreviewScreenCache();

	}

	public void setThemePreview(ThemePreview themePreview) {
		if (this.themePreview == null) {
			themePreview.setParent(this);
			this.themePreview = themePreview;
		}
	}

	public ThemePreview getThemePreview() {
		return this.themePreview;
	}

	/**
	 * @return Returns the imageCache.
	 */
	public ThemeCache getImageCache() {
		return imageCache;
	}

	/**
	 * @param imageCache The imageCache to set.
	 */
	public void setImageCache(ThemeCache imageCache) {
		this.imageCache = imageCache;
	}

	/**
	 * @return Returns the previewCache.
	 */
	public ThemeCache getPreviewCache() {
		return previewCache;
	}

	/**
	 * @param previewCache The previewCache to set.
	 */
	public void setPreviewCache(ThemeCache previewCache) {
		this.previewCache = previewCache;
	}

	public ThemeCache getBackgroundLayerCache() {
		return backgroundLayerCache;
	}
	
	private void clearThemeGraphics() {
		for (Object obj : getAllElements().values()) {
			if (obj instanceof Element) {
				Element element = (Element) obj;
				element.clearThemeGraphic();
				List<Object> parts = element.getChildren();
				if (parts != null) {
					for (Object part : parts) {
						((Part) part).clearThemeGraphic();
					}
				}
			}
		}
	}

	public abstract void save(IProgressMonitor monitor) throws ThemeException;

	public abstract void saveAs(String newFileName, IProgressMonitor monitor)
	    throws ThemeException;

	public void dispose() {
		// there are cached images generated by the entity images.
		SimpleCache.clear(this);
	}

	public ThemeGraphic createGraphic(SkinnableEntity entity) {
		String entityType = entity.isEntityType();
		if (ThemeTag.ELEMENT_COLOUR.equals(entityType)) {
			return new ColourGraphic(entity);
		}
		if (ThemeTag.ELEMENT_SOUND.equals(entityType)
		    || ThemeTag.ELEMENT_EMBED_FILE.equals(entityType)) {
			return new SoundGraphic(entity);
		}

		return new ThemeGraphic(entity);
	}

	protected abstract ILayeredImageCompositor getLayeredImageCompositor(
	    ThemeGraphic graphic);

	public boolean supportsPlatform(IPlatform platform, String skinId) {
		return true;
	}

	public void computeAllElementLayout(final Set<Display> displays,
	    IProgressMonitor monitor) {
		Display themeDisplay = getDisplay();
		ISafeRunnable job = new SilentRunnable() {

			public void run() {
				for (Object object : getAllElements().values()) {
					if (object instanceof SkinnableEntity) {
						SkinnableEntity entity = (SkinnableEntity) object;
						for (Display display : displays) {
							setDisplay(display);
							try {
								if (entity.supportsDisplay(display)) {
									getLayoutContext(display).calculate(
									    entity.getComponentInfo(display));
								}
							} catch (Exception e) {
								PlatformCorePlugin.error(
								    "Layout computation failed for "
								        + entity.getIdentifier() + "@"
								        + display, e);
							}
							try {
								if (entity.getLayoutInfo() == null) {
									PlatformCorePlugin
									    .error("Layout computation failed for "
									        + entity.getIdentifier() + "@"
									        + display);
								}
							} catch (Exception e) {
								PlatformCorePlugin.error(
								    "Layout computation failed for "
								        + entity.getIdentifier() + "@"
								        + display, e);
							}
						}
					}
				}
			}
		};
		DebugHelper.debugTime("entity layout computation <" + getThemeId()
		    + ">", job);
		if (monitor != null) {
			monitor.worked(100);
		}
		setDisplay(themeDisplay);
	}

	public void computeAllSwappedElementLayout(final Set<Display> displays,
	    IProgressMonitor monitor) {
		Display themeDisplay = getDisplay();
		ISafeRunnable job = new SilentRunnable() {

			public void run() {
				for (Object object : getAllElements().values()) {
					if (object instanceof SkinnableEntity) {
						SkinnableEntity entity = (SkinnableEntity) object;
						for (Display display : displays) {
							setDisplay(display);

							// swapped elements
							PreviewImage image = getThemePreview()
							    .getPreviewImageForElem(entity, true);
							PreviewElement element = image == null ? null
							    : image.getPreviewElement(display, entity
							        .getIdentifier());
							if (element != null) {
								try {
									if (entity.getLayoutInfoForPreview(display,
									    element.getComponentInfo(), image) == null) {
										PlatformCorePlugin
										    .error("Layout computation failed for "
										        + element.getID()
										        + "$"
										        + element.getCompName()
										        + "$"
										        + element.getParent()
										            .getThemeName()
										        + "@"
										        + display);
									}
								} catch (Exception e) {
									PlatformCorePlugin
									    .error(
									        "Layout computation failed for "
									            + element.getID()
									            + "$"
									            + element.getCompName()
									            + "$"
									            + element.getParent()
									                .getThemeName() + "@"
									            + display, e);
								}
							}
							// after swapping, the preview screen content
							// changes and thus the default layouts need to be
							// calculated again
							try {
								if (entity.getLayoutInfo() == null) {
									PlatformCorePlugin
									    .error("Layout computation failed for "
									        + entity.getIdentifier() + "@"
									        + display);
								}
							} catch (Exception e) {
								PlatformCorePlugin.error(
								    "Layout computation failed for "
								        + entity.getIdentifier() + "@"
								        + display, e);
							}
							try {
								if (entity.supportsDisplay(display)) {
									if (entity.getLayoutInfo(display,
									    Integer.MIN_VALUE, entity
									        .getDefaultLocId(display), null) == null) {
										PlatformCorePlugin
										    .error("Layout computation failed for "
										        + entity.getIdentifier()
										        + "@"
										        + display);
									}
								}
							} catch (Exception e) {
								PlatformCorePlugin.error(
								    "Layout computation failed for "
								        + entity.getIdentifier() + "@"
								        + display, e);
							}
						}
					}
				}
			}
		};
		DebugHelper.debugTime("entity layout computation <" + getThemeId()
		    + ">", job);
		if (monitor != null) {
			monitor.worked(100);
		}
		setDisplay(themeDisplay);
	}

	public void computeAllPreviewElementLayout(final Set<Display> displays,
	    IProgressMonitor monitor) {
		Display themeDisplay = getDisplay();

		ISafeRunnable job = new SilentRunnable() {

			public void run() {
				for (Object obj : getThemePreview().getChildren(true)) {
					PreviewImage screen = (PreviewImage) obj;
					for (Display display : displays) {
						setDisplay(display);
						if (!screen.supportsDisplay(display)) {
							continue;
						}
						for (Object child : screen.getChildren(true)) {
							PreviewElement element = (PreviewElement) child;
							if (!element.supportsDisplay(display)) {
								continue;
							}
							ComponentInfo component = element
							    .getComponentInfo();
							int type = element.getPreviewElementType();
							if ((type == ThemeConstants.ELEMENT_TYPE_IMAGEFILE && component
							    .getName() == null)
							    || type == ThemeConstants.ELEMENT_TYPE_SOUND) {
								continue;
							}
							// tries to calculate the parts layout directly,
							// refactor
							// later to make simple and coherent way of fetching
							// component info and layouts for all theme elements
							SkinnableEntity entity = element
							    .getSkinnableEntity();
							if (entity != null) {
								try {
									if (entity.getLayoutInfoForPreview(display,
									    element.getComponentInfo(), screen) == null) {
										PlatformCorePlugin
										    .error("Layout computation failed for "
										        + element.getID()
										        + "$"
										        + element.getCompName()
										        + "$"
										        + element.getParent()
										            .getThemeName()
										        + "@"
										        + display);
									}
								} catch (Exception e) {
									PlatformCorePlugin
									    .error(
									        "Layout computation failed for "
									            + element.getID()
									            + "$"
									            + element.getCompName()
									            + "$"
									            + element.getParent()
									                .getThemeName() + "@"
									            + display, e);
								}
								if (ThemeTag.ELEMENT_FRAME.equals(entity
								    .isEntityType())) {
									List children = entity.getChildren();
									if (children != null) {
										for (Object c : children) {
											Part part = (Part) c;
											try {
												if (part.getLayoutInfo(display,
												    element.getLocId()) == null) {
													PlatformCorePlugin
													    .error("Layout computation failed for parts "
													        + element.getID()
													        + "$"
													        + element
													            .getCompName()
													        + "$"
													        + element
													            .getParent()
													            .getThemeName()
													        + "@" + display);
												}
											} catch (Exception e) {
												PlatformCorePlugin.error(
												    "Layout computation failed for parts "
												        + element.getID()
												        + "$"
												        + element.getCompName()
												        + "$"
												        + element.getParent()
												            .getThemeName()
												        + "@" + display, e);
											}
										}
									}
								}
							} else {
								try {
									if (element.getLayoutForPreviewNonFrame(
									    display, component) == null) {
										PlatformCorePlugin
										    .error("Layout computation failed for "
										        + element.getID()
										        + "$"
										        + element.getCompName()
										        + "$"
										        + element.getParent()
										            .getThemeName()
										        + "@"
										        + display);
									}
								} catch (Exception e) {
									PlatformCorePlugin
									    .error(
									        "Layout computation failed for "
									            + element.getID()
									            + "$"
									            + element.getCompName()
									            + "$"
									            + element.getParent()
									                .getThemeName() + "@"
									            + display, e);
								}
							}
						}
					}
				}
			}
		};
		DebugHelper.debugTime("preview layout computation <" + getThemeId()
		    + "> ", job);
		if (monitor != null) {
			monitor.worked(100);
		}
		setDisplay(themeDisplay);
	}

	public void setRuntimeAttribute(String key, Object value) {
		runtimeAttr.put(key, value);
	}

	public Object getRuntimeAttribute(String key) {
		return runtimeAttr.get(key);
	}

	public String getFileName(String fileName, Display display) {
		if (StringUtils.isEmpty(fileName)) {
			return null;
		}

		if (display == null) {
			display = getDisplay();
		}

		String[] names = fileName.split("\\|");
		if (names.length == 1) {
			File f1 = new File(fileName);
			if (f1.isAbsolute() && f1.exists())
				return fileName;

			fileName = f1.getName();

			if (isModel()) {
				// first checks the display specific file if this is model
				if (display == null) {
					display = getDisplay();
				}

				String displayId = display.format(DISPLAY_FORMAT);

				File override = new File(getThemeDir() + File.separator
				    + displayId + File.separator + fileName);
				if (override.exists()) {
					return override.getAbsolutePath();
				}
			}

			File file = new File(getThemeDir() + File.separator + fileName);
			if (file.exists()) {
				return file.getAbsolutePath();
			}
			if (isModel()) {
				return null;
			}
			return getModel().getFileName(fileName, display);
		}
		for (String name : names) {
			name = name.trim();
			name = getFileName(name, display);
			if (name != null) {
				return name;
			}
		}
		return null;
	}

	/**
	 * returns all multi-layer elements
	 * 
	 * @param theme
	 * @return
	 */
	public List<SkinnableEntity> getElementsMultiLayer() {
		List<SkinnableEntity> forUpdate = new ArrayList<SkinnableEntity>();
		for (Object o : getAllElements().values()) {
			if (o instanceof SkinnableEntity) {
				SkinnableEntity e = (SkinnableEntity) o;
				if (e.getToolBox().multipleLayersSupport) {
					forUpdate.add(e);
				}
			}
		}
		return forUpdate;
	}

	public Map<String, Set<String>> getBackgroundDependency() {
		Map<Display, Map<String, Set<String>>> backgroundDependencies = getModel().backgroundDependencies;
		synchronized (backgroundDependencies) {
			Display display = getDisplay();
			Map<String, Set<String>> backgroundDependency = backgroundDependencies
			    .get(getDisplay());

			if (backgroundDependency == null) {
				backgroundDependency = new HashMap<String, Set<String>>();
				backgroundDependencies.put(display, backgroundDependency);

				/*
				 * following method returns list of elements, that comprises to
				 * this element's BG layer. valid only for multilayer elements:
				 * theme.getThemePreview().getDependentsForBackgroundLayer(<element_id>,
				 * null); i.e. 'qsn_bg_slice_list_A' = [qsn_bg_screen,
				 * qsn_bg_area_main]
				 */
				List<SkinnableEntity> bgels = getElementsMultiLayer();
				for (SkinnableEntity ent : bgels) {
					Set<String> bgContributors = getThemePreview()
					    .getDependentsForBackgroundLayer(ent.getId());
					for (String bgcon : bgContributors) {
						Set<String> l = backgroundDependency.get(bgcon);
						if (l == null) {
							l = new HashSet<String>();
							backgroundDependency.put(bgcon, l);
						}
						l.add(ent.getId());
					}
				}
			}
			return backgroundDependency;
		}
	}

	protected synchronized void merge(String oldModelId, Theme model) throws ThemeException {
		Map<String, SkinnableEntity> oldEntities = new HashMap<String, SkinnableEntity>();
		for (Object obj : getAllElements().values()) {
			if (obj instanceof SkinnableEntity) {
				SkinnableEntity entity = (SkinnableEntity) obj;
				oldEntities.put(entity.getIdentifier(), entity);
				// By default, set all skinned entities selected for packaging
				if (entity.isSkinned())
					entity.isSelectedForTransfer = true;
			}
		}

		elementMap = null;
		model.cloneChildren(this);
		model.clonePreview(this);
		clear();

		performPlatformSpecificMerge();
		performPlatformSpecificMerge(oldEntities);
		
		
		for (Object obj : getAllElements().values()) {
			if (obj instanceof SkinnableEntity) {
				SkinnableEntity entity = (SkinnableEntity) obj;
				String id = entity.getIdentifier();
				SkinnableEntity oldEntity = oldEntities.remove(id);
				ExtraEntityWrapper extraEntity = extraEntities.remove(id);
				if (oldEntity == null && extraEntity != null) {
					oldEntity = extraEntity.entity;
				}
				if (oldEntity != null) {

					boolean modelHasElementTypeId = false;
					if (entity.getModel().getElementWithId(id) != null) {
						if (entity.getModel().getElementWithId(id)
								.getCurrentProperty() != null) {
							entity.setCurrentProperty(entity.getModel()
									.getElementWithId(id).getCurrentProperty());
							modelHasElementTypeId = true;
						}
					}
					if (!modelHasElementTypeId)
						entity.setCurrentProperty(oldEntity
								.getCurrentProperty());
				}

				if (oldEntity != null && oldEntity.isAnyChildDone()) {
					// sets true always
					if (!isModel()) entity.setSkinned(true);
					// transfers the skinned graphics
					ThemeGraphic tg = oldEntity.getActualThemeGraphic();
					if (tg != null) {
						entity.setActualGraphic((ThemeGraphic) tg.clone());
					}
					entity.setCurrentProperty(oldEntity.getCurrentProperty());
					// the rest things that a theme element need to transfer
					merge(oldEntity, entity);

					// parts
					List children = oldEntity.getChildren();
					if (children != null && !children.isEmpty()) {
						for (Object child : children) {
							if (child instanceof SkinnableEntity) {
								SkinnableEntity part = (SkinnableEntity) child;
								SkinnableEntity newPart = null;
								// do a lookup in theme?
								// for now just compare the new model's children
								List newChildren = entity.getChildren();
								if (newChildren != null) {
									for (Object newChild : newChildren) {
										if (newChild instanceof SkinnableEntity
										    && part.getIdentifier().equals(
										        ((SkinnableEntity) newChild)
										            .getIdentifier())) {
											newPart = (SkinnableEntity) newChild;
											break;
										}
									}
								} else {
									PlatformCorePlugin
									    .warn("Merging problem: the entity: "
									        + entity.getIdentifier()
									        + " doesn't have children in "
									        + getModelId()
									        + " but the old model has: "
									        + oldModelId);
								}

								if (newPart != null) {
									tg = part.getActualThemeGraphic();
									if (tg != null) {
										newPart
										    .setActualGraphic((ThemeGraphic) tg
										        .clone());
									}
								}
							}
						}
					}
				}
			}
		}

		// stores the extra entities
		if (!isModel()) {
			for (SkinnableEntity entity : oldEntities.values()) {
				extraEntities.put(entity.getIdentifier(), new ExtraEntityWrapper(
				   entity, oldModelId));
			}
		}
		
		oldEntities = null;
		clearOtherModelLayouts();
	}
	
	/**
	 * Gallery and Layout Manager caches layout values. These layout values are no 
	 * longer necessary when switching models. This utility method helps in removing 
	 * unwanted layout content (esp., LayoutXMLData belonging to LayoutManager.class group.
	 * 
	 * Note that currently, we do not keep any layouts. We clear all layouts and let them load
	 * again after this flow is over.
	 */
	private void clearOtherModelLayouts() {
		//Use this variable to filter current model (layoutset descriptor id) and remove all other model layout infos.
		//Currently, we will remove all layouts.
		String currentModelId = "";
		
		ArrayList<LayoutInfo> layoutInfoListToRemove = new ArrayList<LayoutInfo>();
		
		Map<Object, Object> groupData = SimpleCache.getGroupData(LayoutManager.class);
		
		//Cache map is cleared in following scenarios
		//1. install/uninstall operation on any plugin. 
		//2. On model change
		 
		//on installing any plugin and setting new model, groupData will be null.
		if (groupData != null) {
			Set<Entry<Object, Object>> allKeys = groupData.entrySet();
			for (Entry<Object, Object> key : allKeys) {
				if ((key != null) && (key.getKey() != null)
						&& (key.getKey() instanceof LayoutInfo)) {
					LayoutInfo currentLayoutInfo = (LayoutInfo) key.getKey();
					String foundModelId = currentLayoutInfo.getLayoutSet()
							.getDescriptor().getId();

					if (!currentModelId.equalsIgnoreCase(foundModelId)) {
						layoutInfoListToRemove.add(currentLayoutInfo);
					}
				}
			}
		}
		
		for (LayoutInfo layoutInfoToRemove: layoutInfoListToRemove) {
			SimpleCache.clear(LayoutManager.class, layoutInfoToRemove);
		}
		
	}

	protected void performPlatformSpecificMerge() throws ThemeException{
		// In this base class, there will be no implementation for this as 
		// this is supposed to be implemented in sub-classes with specific merges they would need.
		// This method has not been put as abstract as we want a do nothing implementation common to all 
		// platforms which do not need specific implementation.
	}
	
	protected void performPlatformSpecificMerge(Map<String, SkinnableEntity> oldEntities) throws ThemeException{
		// In this base class, there will be no implementation for this as 
		// this is supposed to be implemented in sub-classes with specific merges they would need.
		// This method has not been put as abstract as we want a do nothing implementation common to all 
		// platforms which do not need specific implementation.
	}

	protected void merge(SkinnableEntity oldEntity, SkinnableEntity newEntity)
	    throws ThemeException {
	}

	public void validateSkinned() {
		
		for (Object obj : getAllElements().values()) {
			ThemeBasicData tbd = (ThemeBasicData) obj;
			if (tbd.isShown())
				tbd.setSkinned(tbd.isSkinned());
		}
	}

	/**
	 * Sets draw lines property
	 * 
	 * @param thme
	 * @param newValue
	 */
	public void setDrawLines(boolean newValue) {
		/*
		 * map structure: 'property_list_separator_lines' = { values=True,False,
		 * mandatory=false, default=True, name=Draw Lines,
		 * id=property_list_separator_lines }
		 */
		HashMap<String, HashMap<Object, Object>> settings = getSkinSettings();
		HashMap<Object, Object> drawLines = settings
		    .get("property_list_separator_lines");
		
		drawLines.put("value", Boolean.toString(newValue));
	}

	public boolean isDrawLines() {
		HashMap<String, HashMap<Object, Object>> settings = getSkinSettings();
		HashMap drawLines = settings.get("property_list_separator_lines");
		if (drawLines.get("value") == null) {
			try {
				return Boolean.parseBoolean((String) drawLines.get("default"));
			} catch (Exception e) {
				return true;
			}
		}
		return Boolean.parseBoolean((String) drawLines.get("value"));
	}

	public static boolean supportsDisplay(String value, Display display) {
		if (display == null || StringUtils.isEmpty(value)) {
			return true;
		}
		String[] displays = value.split(",");

		Set<String> tokens = new HashSet<String>(displays.length);
		for (String token : displays) {
			token = token.trim();
			if (token.length() > 0) {
				tokens.add(token);
			}
		}

		// checks the explicit unsupported displays first
		for (Iterator<String> i = tokens.iterator(); i.hasNext();) {
			String token = i.next();

			if (token.startsWith("!")) {
				token = token.substring(1);
				if (Display.parseOrientation(token) == display.getOrientation()) {
					return false;
				}

				Display d = Display.valueOf(token);
				if (display.equals(d)) {
					return false;
				}
				i.remove();
			}
		}

		// then checks the supported displays
		if (tokens.isEmpty()) {
			return true;
		}

		for (String token : tokens) {
			if (Display.parseOrientation(token) == display.getOrientation()) {
				return true;
			}

			Display d = Display.valueOf(token);
			if (display.equals(d)) {
				return true;
			}
		}
		return false;
	}


	class ExtraEntityWrapper {

		SkinnableEntity entity;

		String modelId;

		ExtraEntityWrapper(SkinnableEntity entity, String modelId) {
			this.entity = entity;
			this.modelId = modelId;
		}
	}
}
