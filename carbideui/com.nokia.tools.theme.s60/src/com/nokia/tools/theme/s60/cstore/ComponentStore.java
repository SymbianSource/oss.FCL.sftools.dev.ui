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
package com.nokia.tools.theme.s60.cstore;

import java.awt.datatransfer.Clipboard;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.transform.OutputKeys;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;

import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.content.core.ContentSourceManager;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.resource.util.XmlUtil;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.theme.content.ContentAdapter;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.ecore.ThemeModelFactory;
import com.nokia.tools.theme.s60.IThemeConstants;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.S60ThemeProvider;
import com.nokia.tools.theme.s60.editing.EditableAnimatedEntity;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.editing.EditableEntityImageFactory;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * Component Store allows storing and retrieving arbitrary number of theme
 * elements. Element are stored using theme model infrastructure
 */
public class ComponentStore {

	/**
	 * ID prefix used for element's id to distinguish between theme element and
	 * store elements.
	 */
	public static final String ID_PREFIX = "__id_";
	public static final String ID_PREFIX_MULTILAYER = "__id_ml_";
	public static final String ID_PREFIX_BMPANIM = "__id_ba_";

	public static final String PROP_CONTENT_ADDED = "CONTENT_ADD";
	public static final String PROP_CONTENT_REMOVED = "CONTENT_REMOVE";
	public static final String PROP_CONTENT_CHANGE = "CONTENT_CHANGE";

	public static ComponentStore SINGLETON;

	/**
	 * true if given element id represents id of ComponentStore user-defined
	 * element.
	 * 
	 * @param id
	 * @return
	 */
	public static boolean isComponentStoreElement(String id) {
		return id != null && id.startsWith(ID_PREFIX);
	}

	public static boolean isMultilayerElement(String id) {
		return id != null && id.startsWith(ID_PREFIX_MULTILAYER);
	}

	public static boolean isAnimatedElement(String id) {
		return id != null && id.startsWith(ID_PREFIX_BMPANIM);
	}

	/**
	 * Stored element represents element in component store
	 */
	public static class StoredElement {

		public StoredElement() {
		}

		public StoredElement(String id, String name, int orderId,
				List<String> tags, IContentData data) {
			link = data;
			this.id = id == null ? data.getId() : id;
			this.name = name == null ? data.getName() : name;
			this.tags = tags;
			if (tags == null)
				this.tags = new ArrayList<String>();
			naturalOrderId = orderId;
		}

		public int naturalOrderId;
		public String id;
		public String name;
		public List<String> tags;
		public IContentData link;
		public Object metadata;
	}

	/* template element id for nine piece */
	private static final String ID_NINEPIECE = "S60_2_6%qsn_fr_list";
	/* template element id for normal element */
	private static final String ID_SINGLE = "qgn_menu_appsgrid_cxt";
	/* template element id for normal element */
	private static final String ID_BMPANIM = "qgn_note_batt_charging_anim";

	/* location relative to eclipse installation folder */
	private static final String STORE_LOCATION = "ComponentStore/store.tdf";

	private PropertyChangeSupport propSup = new PropertyChangeSupport(this);

	private boolean silent = false;

	static {
		try {
			SINGLETON = new ComponentStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void refresh() {
		SINGLETON.refreshContent();
	}

	private void refreshContent() {
		storeContent.clear();
		try {
			
			File location = new File(FileUtils.getFile(Platform
					.getInstallLocation().getURL()), STORE_LOCATION);
			ContentSourceManager mgr = ContentSourceManager.getGlobalInstance();
			if (!location.canRead()) {
				return;
			}
			List<IContent> c = mgr.getRootContents(
					S60ThemeProvider.CONTENT_TYPE, location,
					new NullProgressMonitor());
			storeTheme = (ThemeContent) c.get(0);
			Theme theme = (Theme) storeTheme.getData();

			// mark as component store theme for proper saving of custom
			// elements
			theme.setRuntimeAttribute("ComponentStore", Boolean.TRUE);

			for (Object el : theme.getAllElements().values()) {
				SkinnableEntity sk = (SkinnableEntity) el;
				if (sk.getId().startsWith(ID_PREFIX)) {
					storeContent.add(createStoreContent(sk));
				}
			}

			/* sort store content */
			Collections.sort(storeContent, new Comparator<StoredElement>() {
				public int compare(StoredElement o1, StoredElement o2) {
					int a = o1.naturalOrderId;
					int b = o2.naturalOrderId;
					return a < b ? -1 : a == b ? 0 : 1;
				}
			});

		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContentException e) {
			e.printStackTrace();
		}
		dirty = false;
	}

	/* theme that is used as storage from component store elements */
	private ThemeContent storeTheme;

	/* list of store content */
	private List<StoredElement> storeContent = new ArrayList<StoredElement>();

	private boolean dirty;

	private ComponentStore() {

		/* check if there is directory and theme */
		File location = new File(FileUtils.getFile(Platform
				.getInstallLocation().getURL()), STORE_LOCATION);

		synchronized (ID_PREFIX) {
			if (!location.canRead()) {
				File locAddr = location.getParentFile();
				if (!locAddr.exists()) {
					locAddr.mkdirs();
				}
				InputStream in = null;
				try {
					in = getClass().getResourceAsStream("store_template.tdf");

					// creates store using the default model to avoid
					// initializing/parsing unnecessary models
					Document doc = XmlUtil.parse(in);

					IThemeModelDescriptor descriptor = ThemePlatform
							.getDefaultThemeModelDescriptor(IThemeConstants.THEME_CONTAINER_ID);
					if (descriptor != null) {
						String modelId = descriptor.getId();
						IDevice device = descriptor.getThemeDescriptor()
								.getDefaultDevice();
						if (device != null) {
							Display display = device.getDisplay();
							org.w3c.dom.Element root = doc.getDocumentElement();
							root.setAttribute(ThemeTag.LAYOUT_WIDTH, Integer
									.toString(display.getWidth()));
							root.setAttribute(ThemeTag.LAYOUT_HEIGHT, Integer
									.toString(display.getHeight()));
						}
						if (modelId != null) {
							org.w3c.dom.Element element = (org.w3c.dom.Element) XPathAPI
									.selectSingleNode(doc, "//skin/phone/model");
							element.setAttribute(ThemeTag.ATTR_ID, modelId);
						}
					}
					Map<String, String> props = new HashMap<String, String>(1);
					props.put(OutputKeys.DOCTYPE_SYSTEM,
							"http://abc.com/skindata.dtd");
					XmlUtil.write(doc, location, props);
				} catch (Exception e) {
					S60ThemePlugin.error(e);
				} finally {
					FileUtils.close(in);
				}
			}
		}

		refreshContent();
	}

	private String makeString(String s) {
		return "~".equals(s) ? "" : s;
	}

	private StoredElement createStoreContent(SkinnableEntity sk) {

		StoredElement element = new StoredElement();
		String dataStr = sk.getAttributeValue(ThemeTag.ATTR_NAME);
		dataStr = dataStr.replace("||", "|~|");
		StringTokenizer tok = new StringTokenizer(dataStr, "|");
		element.id = makeString(tok.nextToken());
		element.naturalOrderId = Integer.parseInt(tok.nextToken());
		if (tok.hasMoreTokens())
			element.name = makeString(tok.nextToken());
		boolean mask = false, softmask = false;
		if (tok.hasMoreTokens())
			mask = "true".equalsIgnoreCase(tok.nextToken());
		if (tok.hasMoreTokens())
			softmask = "true".equalsIgnoreCase(tok.nextToken());

		sk.getToolBox().Mask = mask;
		sk.getToolBox().SoftMask = softmask;

		element.tags = new ArrayList<String>();
		while (tok.hasMoreTokens())
			element.tags.add(makeString(tok.nextToken()));
		element.link = storeTheme.findByData(sk);
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#getContents()
	 */
	public List<StoredElement> getContents() {
		return storeContent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#getMatches(java.lang.String)
	 */
	public List<StoredElement> getMatches(String id) {
		List<StoredElement> matches = new ArrayList<StoredElement>();
		for (StoredElement s : storeContent) {
			if (id.equalsIgnoreCase(s.id) || id.startsWith(s.id)
					|| s.id.startsWith(id) || s.id.indexOf(id) != -1)
				matches.add(s);
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#getFilteredContent(java.util.List)
	 */
	public List<StoredElement> getFilteredContent(List<String> filterTags) {
		List<StoredElement> matches = new ArrayList<StoredElement>();
		for (StoredElement s : storeContent) {
			for (String t : s.tags)
				for (String a : filterTags)
					if (t.equals(a))
						if (!matches.contains(s))
							matches.add(s);
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#getAvailableTags()
	 */
	public List<String> getAvailableTags() {
		List<String> matches = new ArrayList<String>();
		for (StoredElement s : storeContent) {
			for (String t : s.tags)
				if (!matches.contains(t))
					matches.add(t);
		}
		return matches;
	}

	private int getMaxNaturalOrder() {
		int i = 0;
		for (StoredElement e : storeContent)
			if (e.naturalOrderId > i)
				i = e.naturalOrderId;
		return i + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#addElements(java.util.List,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void addElements(List<IContentData> elements,
			IProgressMonitor monitor) throws Exception {
		silent = true;
		List<StoredElement> added = new ArrayList<StoredElement>();
		try {
			for (IContentData a : elements) {
				added.add(addElement(a));
				if (monitor != null)
					monitor.worked(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		silent = false;
		propSup.firePropertyChange(PROP_CONTENT_ADDED, null, added);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#addElement(com.nokia.tools.content.core.IContentData)
	 */
	public StoredElement addElement(IContentData element) throws Exception {

		if (((ISkinnableEntityAdapter) element
				.getAdapter(ISkinnableEntityAdapter.class)).isMultiPiece()) {
			return addNinePiece(element);
		}

		if (((IImageAdapter) element.getAdapter(IImageAdapter.class))
				.getImage() instanceof EditableAnimatedEntity) {
			return addAnimatedIcon(element);
		}

		SkinnableEntity template = ((ThemeData) storeTheme.findById(ID_SINGLE))
				.getSkinnableEntity();
		SkinnableEntity newEnt = (SkinnableEntity) template.clone();

		String elementId = element.getId();
		String elementName = element.getName();
		if (element.getAdapter(INamingAdapter.class) != null)
			elementName = ((INamingAdapter) element
					.getAdapter(INamingAdapter.class)).getName();

		// add tags
		List<String> tags = new ArrayList<String>();
		IContentData component = element.getParent();
		IContentData componentGroup = component.getParent();
		tags.add(component.getName());
		if (componentGroup != null) {
			String newTag = componentGroup.getName();
			if (!tags.contains(newTag))
				tags.add(newTag);
		}

		// encode name string
		IToolBoxAdapter tb = (IToolBoxAdapter) element
				.getAdapter(IToolBoxAdapter.class);
		String nameStr = encodeInfo(elementId, elementName, tags,
				getMaxNaturalOrder(), tb.supportMask(), tb.supportSoftMask());
		newEnt.setAttribute(ThemeTag.ATTR_NAME, nameStr);
		SkinnableEntity elementData = ((ThemeData) element)
				.getSkinnableEntity();
		EditableEntityImage eei = (EditableEntityImage) EditableEntityImageFactory
				.getInstance().createEntityImage(elementData, null, 0, 0);

		ThemeGraphic eeiGraphics = eei.getSavedThemeGraphics(true);
		eeiGraphics.setAttribute(ThemeTag.ATTR_STATUS,
				ThemeTag.ATTR_VALUE_ACTUAL);
		newEnt.setActualGraphic(eeiGraphics);

		String idPrefix = elementData.getToolBox().multipleLayersSupport ? ID_PREFIX_MULTILAYER
				: ID_PREFIX;

		newEnt.setAttribute(ThemeTag.ATTR_ID, idPrefix + uniqueId());
		// handled by the resource change
		// template.getParent().addChild(newEnt);

		internalAddToTree(newEnt);
		StoredElement stelement = createStoreContent(newEnt);
		storeContent.add(stelement);

		dirty = true;

		if (!silent)
			propSup.firePropertyChange(PROP_CONTENT_ADDED, null, stelement);

		return stelement;
	}

	private StoredElement addAnimatedIcon(IContentData element)
			throws Exception {
		// switch template element to 9-piece mode
		ThemeData tdTemplate = ((ThemeData) storeTheme.findById(ID_BMPANIM));
		ISkinnableEntityAdapter tdAdapter = (ISkinnableEntityAdapter) tdTemplate
				.getAdapter(ISkinnableEntityAdapter.class);

		Element template = (Element) tdTemplate.getSkinnableEntity();
		Element newElement = (Element) template.clone();

		// invoke copy/paste from source element to newly created element
		IImageAdapter elementAdapter = (IImageAdapter) element
				.getAdapter(IImageAdapter.class);
		EditableAnimatedEntity elementEntity = (EditableAnimatedEntity) elementAdapter
				.getImage();
		ThemeGraphic imageData = elementEntity.getThemeGraphics(true);
		imageData
				.setAttribute(ThemeTag.ATTR_STATUS, ThemeTag.ATTR_VALUE_ACTUAL);
		newElement.setActualGraphic(imageData);

		try {
			EditObject resource = new ThemeModelFactory()
					.createEditObject(newElement);
			((ThemeData) tdTemplate.getParent()).getResource().getChildren()
					.add(resource);
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
		// newElement has proper content now
		String elementId = element.getId();
		String elementName = element.getName();
		if (element.getAdapter(INamingAdapter.class) != null)
			elementName = ((INamingAdapter) element
					.getAdapter(INamingAdapter.class)).getName();

		// add tags
		List<String> tags = new ArrayList<String>();
		IContentData component = element.getParent();
		IContentData componentGroup = component.getParent();
		tags.add(component.getName());
		if (componentGroup != null)
			tags.add(componentGroup.getName());

		// encode name string
		String nameStr = encodeInfo(elementId, elementName, tags,
				getMaxNaturalOrder(), true, true);
		newElement.setAttribute(ThemeTag.ATTR_NAME, nameStr);
		newElement.setAttribute(ThemeTag.ATTR_ID, ID_PREFIX_BMPANIM
				+ uniqueId());
		template.getParent().addChild(newElement);

		/* copy/paste graphics another time to force element-specific file names */
		elementEntity = new EditableAnimatedEntity(newElement);
		imageData = elementEntity.getSavedThemeGraphics(true);
		imageData
				.setAttribute(ThemeTag.ATTR_STATUS, ThemeTag.ATTR_VALUE_ACTUAL);
		newElement.setActualGraphic(imageData);

		StoredElement stelement = createStoreContent(newElement);
		storeContent.add(stelement);

		dirty = true;

		if (!silent)
			propSup.firePropertyChange(PROP_CONTENT_ADDED, null, stelement);

		return stelement;
	}

	private StoredElement addNinePiece(IContentData element) throws Exception {
		// switch template element to 9-piece mode
		ThemeData tdTemplate = ((ThemeData) storeTheme.findById(ID_NINEPIECE));
		ISkinnableEntityAdapter tdAdapter = (ISkinnableEntityAdapter) tdTemplate
				.getAdapter(ISkinnableEntityAdapter.class);
		//tdAdapter.setNinePieceBitmap();
		tdAdapter.setMultiPieceBitmap();

		Element template = (Element) tdTemplate.getSkinnableEntity();
		Element newElement = (Element) template.clone();

		
		ISkinnableEntityAdapter elementAdapter = (ISkinnableEntityAdapter) element
				.getAdapter(ISkinnableEntityAdapter.class);
		Clipboard clip = new Clipboard("");
		elementAdapter.copyImageToClipboard(clip);

		ThemeData newElementNode = null;
		try {
			EditObject resource = new ThemeModelFactory()
					.createEditObject(newElement);
			((ThemeData) tdTemplate.getParent()).getResource().getChildren()
					.add(resource);
			newElementNode = ContentAdapter.getContentData(resource);
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}

		((ISkinnableEntityAdapter) newElementNode
				.getAdapter(ISkinnableEntityAdapter.class)).paste(
				ClipboardHelper.getSupportedClipboardContent(clip), null);

		// newElement has proper content now
		String elementId = element.getId();
		String elementName = element.getName();
		if (element.getAdapter(INamingAdapter.class) != null)
			elementName = ((INamingAdapter) element
					.getAdapter(INamingAdapter.class)).getName();

		// add tags
		List<String> tags = new ArrayList<String>();
		IContentData component = element.getParent();
		IContentData componentGroup = component.getParent();
		tags.add(component.getName());
		if (componentGroup != null)
			tags.add(componentGroup.getName());

		// encode name string
		IToolBoxAdapter tb = (IToolBoxAdapter) element
				.getAdapter(IToolBoxAdapter.class);
		String nameStr = encodeInfo(elementId, elementName, tags,
				getMaxNaturalOrder(), tb.supportMask(), tb.supportSoftMask());
		newElement.setAttribute(ThemeTag.ATTR_NAME, nameStr);
		newElement.setAttribute(ThemeTag.ATTR_ID, ID_PREFIX + uniqueId());
		template.getParent().addChild(newElement);

		StoredElement stelement = createStoreContent(newElement);
		storeContent.add(stelement);

		dirty = true;

		if (!silent)
			propSup.firePropertyChange(PROP_CONTENT_ADDED, null, stelement);

		return stelement;
	}

	private void internalAddToTree(SkinnableEntity newEnt) {
		ThemeData template = ((ThemeData) storeTheme.findById(ID_SINGLE));
		try {
			EditObject resource = new ThemeModelFactory()
					.createEditObject(newEnt);
			((ThemeData) template.getParent()).getResource().getChildren().add(
					resource);
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
	}

	/**
	 * string format: <id>|<orderNo>|<name>|<tag1>|<tag2>|<tag3>|...
	 * 
	 * @param elementId
	 * @param elementName
	 * @param tags
	 * @param order
	 * @return
	 */
	private String encodeInfo(String elementId, String elementName,
			List<String> tags, int order, boolean mask, boolean softmask) {
		StringBuffer result = new StringBuffer();
		if (tags == null)
			tags = new ArrayList<String>();

		tags.add(0, Boolean.toString(softmask));
		tags.add(0, Boolean.toString(mask));
		tags.add(0, elementName);
		tags.add(0, Integer.toString(order));
		tags.add(0, elementId);
		Iterator<String> i = tags.iterator();
		while (i.hasNext()) {
			result.append(i.next());
			if (i.hasNext())
				result.append('|');
		}
		return result.toString();
	}

	private String uniqueId() {
		return "" + System.currentTimeMillis() + ""
				+ (int) (Math.random() * 1000000);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#addElements(java.util.List,
	 *      org.eclipse.core.runtime.IProgressMonitor, java.lang.String)
	 */
	public void addElements(List<File> image, IProgressMonitor monitor,
			String... _tags) throws Exception {
		silent = true;
		List<StoredElement> added = new ArrayList<StoredElement>();
		try {
			for (File a : image) {
				added.add(addElement(a, null, null, _tags));
				if (monitor != null)
					monitor.worked(1);
				System.out.println("Added: " + a + ", filter tags = " + _tags);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		silent = false;
		propSup.firePropertyChange(PROP_CONTENT_ADDED, null, added);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#deleteElements(java.util.List)
	 */
	public void deleteElements(List<StoredElement> els) {
		silent = true;
		List<StoredElement> trueRemoved = new ArrayList<StoredElement>();
		try {
			for (StoredElement l : els)
				trueRemoved.add(deleteElement(l));
		} catch (Exception e) {
			e.printStackTrace();
		}
		silent = false;
		propSup.firePropertyChange(PROP_CONTENT_REMOVED, null, trueRemoved);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#deleteElement(com.nokia.tools.theme.s60.cstore.ComponentStore.StoredElement)
	 */
	public StoredElement deleteElement(StoredElement els) {
		if (!storeContent.contains(els))
			return null;

		IContentData node = els.link;

		EditObject resource = ((ThemeData) node).getResource();
		resource.getParent().getChildren().remove(resource);

		storeContent.remove(els);

		dirty = true;

		if (!silent)
			propSup.firePropertyChange(PROP_CONTENT_REMOVED, null, els);

		return els;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#addElement(java.io.File,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public StoredElement addElement(File image, String id, String name,
			String... _tags) throws Exception {
		ThemeData templateData = ((ThemeData) storeTheme.findById(ID_SINGLE));
		SkinnableEntity template = templateData.getSkinnableEntity();
		SkinnableEntity newEnt = (SkinnableEntity) template.clone();

		if (StringUtils.isEmpty(id))
			id = FileUtils.getBaseName(image);
		if (StringUtils.isEmpty(name))
			name = id;
		List<String> tags = new ArrayList<String>();
		if (_tags != null)
			tags.addAll(Arrays.asList(_tags));
		String nameStr = encodeInfo(id, name, tags, getMaxNaturalOrder(), true,
				true);

		ThemeData newData = null;
		try {
			EditObject resource = new ThemeModelFactory()
					.createEditObject(newEnt);
			((ThemeData) templateData.getParent()).getResource().getChildren()
					.add(resource);
			newData = ContentAdapter.getContentData(resource);
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
		((ISkinnableEntityAdapter) newData
				.getAdapter(ISkinnableEntityAdapter.class)).paste(image, null);

		newEnt.setAttribute(ThemeTag.ATTR_NAME, nameStr);
		newEnt.setAttribute(ThemeTag.ATTR_ID, ID_PREFIX + uniqueId());
		template.getParent().addChild(newEnt);

		StoredElement stElement = createStoreContent(newEnt);
		storeContent.add(stElement);

		dirty = true;

		if (!silent)
			propSup.firePropertyChange(PROP_CONTENT_ADDED, null, stElement);

		return stElement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#saveStore()
	 */
	public void saveStore() {
		if (dirty)
			try {
				((S60Theme) storeTheme.getData())
						.save(new NullProgressMonitor());
				dirty = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propSup.addPropertyChangeListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.cstore.IComponentStore#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propSup.removePropertyChangeListener(l);
	}
}
