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

package com.nokia.tools.theme.s60.siscreation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.layout.LayoutException;
import com.nokia.tools.platform.theme.Component;
import com.nokia.tools.platform.theme.ComponentGroup;
import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Task;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.ThemeTag.SkinCompliance;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.s60.general.ThemeUtils;
import com.nokia.tools.theme.s60.model.S60Theme;

/**
 * The file implements the 'Skin descriptor text file' creation process. The
 * file is the updated version of the 'SkinTextFileWriter' class. The file also
 * copies the required images to specified output directory.
 */
public class ThemeTextFileWriter {

	/*
	 * --------------------------------------------------------------- The file
	 * tries to achieve the following 1. Remove general hacks - that got
	 * included from time to time in SkinTextFileWriter 2. Make it easier to
	 * read / understand. GENERAL LOGIC: 1. The class will have a property
	 * object using which its operation can be controlled (like if the skin file
	 * created is a scaleable skin or is a fixed sized skin)
	 * ---------------------------------------------------------------
	 */

	// Data member declarations
	private Map<Object, Object> properties = null;

	private S60Theme themeData = null;

	private File outputDir = null;

	private PrintWriter descFile = null;

	private String skinType = ThemeTag.SKN_VALUE_SCALABLE;

	private SkinCompliance skinCompliance = SkinCompliance.SCALEABLE_SKIN;

	// String buffer to hold the errors found during processing
	private StringBuffer error = new StringBuffer();

	// Special entities processor
	SpecialEntitiesHandler splEntitiesProcessorObj = null;

	// Processed entries list
	private Set<Object> writtenEntities = new HashSet<Object>();

	/**
	 * Writing parts, enabling that part ID is defined only once and shared in
	 * multiple frames, Purpose: optimization and 9 piece call handling
	 */
	private Set<String> writtenParts = new HashSet<String>();

	// Locale property

	/**
	 * Default Constructor
	 * 
	 * @param properties
	 *            The properties to manage the text file creation
	 * @throws ThemeException
	 */
	public ThemeTextFileWriter(Map<Object, Object> properties)
			throws ThemeException {
		this.properties = new HashMap<Object, Object>();
		this.properties.putAll(properties);

		// create a special entities object and register the current object to
		// it

		constructSpecialEntityProcessor();

	}

	/**
	 * Uses reflection to construct the special entity processor.
	 * 
	 * @throws ThemeException
	 */
	private void constructSpecialEntityProcessor() throws ThemeException {

		String specialEntityClassName = (String) properties
				.get(ThemeTag.KEY_SKIN_SPLENTITY_CLASSNAME);

		try {
			Class cls = Class.forName(specialEntityClassName);

			Class param[] = new Class[1];
			param[0] = this.getClass();

			Constructor ct = cls.getDeclaredConstructor(param);

			Object args[] = new Object[1];
			args[0] = this;
			splEntitiesProcessorObj = (SpecialEntitiesHandler) ct
					.newInstance(args);
			splEntitiesProcessorObj.initialize();
			this.properties.put(ThemeTag.KEY_ENTITY_PROP,
					splEntitiesProcessorObj.PhoneEntityAttributes);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ThemeException("Unable to create special class "
					+ e.getMessage());
		}
	}

	/**
	 * Sets the theme object
	 * 
	 * @param data
	 *            The theme object for the theme whose text file has to be
	 *            generated.
	 * @throws ThemeException
	 */
	public void setThemeObject(S60Theme data) throws ThemeException {
		if (data == null)
			throw new ThemeException("Invalid skin object");

		this.themeData = data;

		String themeDir = themeData.getThemeDir();
		File inputDir = new File(themeDir);

		if (!inputDir.exists() || !inputDir.isDirectory())
			throw new ThemeException("Invalid theme directory");

		this.properties.put(ThemeTag.KEY_INPUT_DIR, inputDir.getAbsolutePath());

		setComplianceLevel();
	}

	/**
	 * Returns the theme data object
	 */
	protected S60Theme getThemeObject() {
		return themeData;
	}

	/**
	 * Sets the directory in which the skin descriptor file has to be written
	 * 
	 * @param dir
	 *            The directory in which the skin related files has to be
	 *            written.
	 * @throws ThemeException
	 */
	public void setOutputDir(String dir) throws ThemeException {
		outputDir = new File(dir);

		if (!outputDir.exists() || !outputDir.isDirectory())
			throw new ThemeException("Invalid output directory");

		this.properties.put(ThemeTag.KEY_OUTPUT_DIR, outputDir
				.getAbsolutePath());
	}

	/**
	 * @return The properties map
	 */
	protected Map<Object, Object> getProperties() {
		return properties;
	}

	/**
	 * Generates the skin text file for the set skin object.
	 * 
	 * @throws ThemeException
	 */
	public void generateDescriptorFile() throws ThemeException {

		/*
		 * Steps to be followed to generate the skin package 1. Process each
		 * Task -> Component Group -> Component -> Element (processing in the
		 * same order as mentioned above) 2. Generate the skin text file while
		 * doing step 1. 3. Copy the image from the skin folder to the temp
		 * folder. (Nice to have is to do a image comparator that removes
		 * duplicates)
		 */

		// Open the text file
		openTextFile();

		// Call the preprocessor
		splEntitiesProcessorObj.preProcess();

		// Write the general information
		writeGeneralData();

		// write the skin setting information
		splEntitiesProcessorObj.writeSkinSettings();

		// Write the element information
		writeSkinData();

		// Call the postprocessor
		splEntitiesProcessorObj.postProcess();
	}

	// Commonly used strings in the descriptor file
	static String comment = "// ";

	static String decorate = "// ======================================================================";

	static String space = " ";

	static String hexPrefix = "0x";

	static String quote = "\"";

	static String comma = ",";

	static String equal = "=";

	static char[] linebrk = { 0x0D, 0x0A };

	static String linebreak = new String(linebrk);

	static String tab = "\t";

	static char underscore = '_';

	static int hexRadix = 16;

	static int decimalRadix = 10;

	static String SKIN_IMAGE_EXT = ".bmp";

	static String SKIN_IMAGE_HARDMASK_EXT = "_mask";

	/**
	 * Writes the general information into the text file (headers, names,
	 * supported languages etc)
	 * 
	 * @throws ThemeException
	 *             when unable to successfully create the skin descriptor file
	 */
	private void writeGeneralData() throws ThemeException {

		error.delete(0, error.length());

		writeln(decorate);
		writeln(comment + themeData.getThemeName());
		writeln(decorate);
		writeln();

		// Write the skin header information
		String value = null;
		StringBuffer info = new StringBuffer();

		// PROCESS THE SKINTYPE INFORMATION
		{
			info.delete(0, info.length());

			info.append(ThemeTag.SKN_TAG_SKINTYPE);
			info.append(space);

			info.append(skinType);
			writeln(info.toString());

		}

		// WRITE THE UID INFORMATION
		{
			value = (String) themeData.getAttributeValue(ThemeTag.ATTR_UID);
			info.delete(0, info.length());

			if ((value != null) && (value.trim().length() > 0)) {
				info.append(ThemeTag.SKN_TAG_UID);
				info.append(space);
				info.append(value);
			}

			writeln(info.toString());
		}

		// WRITE THE NAME INFORMATION
		{
			info.delete(0, info.length());

			// Copy the name into a new map since we are changing the english
			// name
			// to the transfer name. This should not affect the actual name of
			// the skin
			Map<Object, Object> names = new HashMap<Object, Object>();
			names.putAll(themeData.getLangName());

			// 1 stands for english name
			// change it to transfer name that the user has selected

			Set keys = names.keySet();
			Object[] langid = keys.toArray();

			for (int i = 0; i < langid.length; i++) {
				info.delete(0, info.length());
				info.append(ThemeTag.SKN_TAG_NAME);
				info.append(space);

				String langIdStr = ((Integer) langid[i]).toString();
				info.append(langIdStr);
				info.append(space);

				value = (String) names.get(langid[i]);
				info.append(quote);
				info.append(value);
				info.append(quote);

				writeln(info.toString());
			}
		}

		// WRITE THE COPYRIGHT INFORMATION
		{
			info.delete(0, info.length());

			value = (String) themeData
					.getAttributeValue(ThemeTag.ATTR_COPYRIGHT);

			if ((value != null) && (value.trim().length() > 0)) {
				value = getSkinString(value);

				info.append(ThemeTag.SKN_TAG_COPYRIGHT);
				info.append(space);

				info.append(quote);
				info.append(value);
				info.append(quote);
			}

			writeln(info.toString());
		}

		// WRITE THE COPY PROTECTION INFORMATION
		{

			info.delete(0, info.length());
			value = (String) themeData.getAttributeValue(ThemeTag.ATTR_PROTECT);

			if (value != null
					&& value.equals(ThemeTag.ATTR_VALUE_COPY_DISALLOWED)) {
				info.append(ThemeTag.SKN_TAG_PROTECT);
				info.append(space);
				info.append(ThemeTag.SKN_VALUE_DISABLECOPY);
			}

			writeln(info.toString());
		}

		// WRITE THE FULL_MODIFY_ADAPTER PALETTE INFORMATION
		{
			info.delete(0, info.length());
			value = (String) themeData
					.getAttributeValue(ThemeTag.ATTR_DEFAULT_PALETTE);

			if (value != null && value.trim().length() > 0) {
				info.append(ThemeTag.SKN_TAG_PALETTE_SCHEME);
				info.append(equal);
				info.append(value);
			}

			writeln(info.toString());
		}

		// One empty line for beautification
		writeln();

	}

	/**
	 * Starting function to write the skin element information.
	 * 
	 * @throws ThemeException
	 */
	private void writeSkinData() throws ThemeException {

		/*
		 * This function basically processes each task in the S60 theme object
		 * In this function a for loop - loops through the entire task list -
		 * then checks if the task name is present in the special processing
		 * list - if present in the special list then it invokes the appropriate
		 * function if not present in the special list then it invokes the
		 * writeTask function
		 */

		if (!themeData.hasChildNodes())
			return;

		// Get the list of tasks and the keys from the specialTasks map
		List taskList = themeData.getChildren();

		for (int i = 0; i < taskList.size(); i++) {
			ThemeBasicData task = (ThemeBasicData) (taskList.get(i));

			boolean isSpl = writeSpecialEntry(task,
					SpecialEntitiesHandler.SpecialTasks);
			if (isSpl == false) {
				writeTask((Task) task);
			}
		}

		
		if (Boolean.parseBoolean(System
		 .getProperty(SystemProperties.includeAllElements))) {
		 processSupersetOfElements(); 
		 }

	}

	/**
	 * This method will process the superset of elements . The superset contains
	 * elements from other models (version 3.1,3.1 and 5.0 as of now ) and will
	 * write them if not already written
	 * 
	 * @throws ThemeException
	 */
	private void processSupersetOfElements() throws ThemeException {
		for (Object element : themeData.getAllElements(true)) {
			Element newElement = (Element) element;
			processSuperSetElement(newElement);
		}
	}

	private void processSuperSetElement(Element newElement)
			throws ThemeException {
		if (canProcessSuperSetElement(newElement)) {
			writeElement(newElement);
		}
	}

	private boolean canProcessSuperSetElement(Element newElement)
			throws ThemeException {
		return newElement.isSkinned() && !isSpecial(newElement)
				&& !isProcessed(newElement.getId())
				&& newElement.getSelectionForTransfer();
	}

	private boolean isSpecial(Element newElement) throws ThemeException {
		boolean isSpl = false;
		Component component = (Component) newElement.getParent();
		ComponentGroup componentGroup = componentGroup(component);
		Task task = task(componentGroup);
		if (isNull(component) || isNull(componentGroup) || isNull(task))
			return true;
		isSpl = specialTask(task) || specialComponentGroup(componentGroup)
				|| specialComponent(component) || specialElement(newElement);
		return isSpl;
	}

	private boolean isNull(Object object) {
		return object == null;
	}

	private Task task(ComponentGroup componentGroup) {
		if (null == componentGroup)
			return null;
		return (Task) componentGroup.getParent();
	}

	private ComponentGroup componentGroup(Component component) {
		if (null == component)
			return null;
		return (ComponentGroup) component.getParent();
	}

	private boolean specialElement(Element newElement) throws ThemeException {
		return newElement.getExecuted()
				|| writeSpecialEntry(newElement,
						SpecialEntitiesHandler.SpecialElements);
	}

	private boolean specialComponent(Component component) throws ThemeException {
		return component.getExecuted()
				|| writeSpecialEntry(component,
						SpecialEntitiesHandler.SpecialComp);
	}

	private boolean specialComponentGroup(ComponentGroup componentGroup)
			throws ThemeException {
		return componentGroup.getExecuted()
				|| writeSpecialEntry(componentGroup,
						SpecialEntitiesHandler.SpecialCompGroups);
	}

	private boolean specialTask(Task task) throws ThemeException {
		return task.getExecuted()
				|| writeSpecialEntry(task, SpecialEntitiesHandler.SpecialTasks);
	}

	/**
	 * Writes all the task info.
	 * 
	 * @throws ThemeException
	 */
	private void writeTask(Task task) {

		/*
		 * This function is called to process each task. For the given (input
		 * param) task - the function gets the component groups under it (the
		 * children of the given task). For each child - it checks if the child
		 * is given in the special component group processing list; if a special
		 * component group then it calls its predefined function; if not a
		 * special component group then it calls the writeComponentGroup
		 * funciton.
		 */

		if ((!task.hasChildNodes()) || (!task.getSelectionForTransfer())) {
			return;
		}

		List compGList = task.getChildren();

		for (int i = 0; i < compGList.size(); i++) {
			ThemeBasicData compG = (ThemeBasicData) (compGList.get(i));

			boolean isSpl;
			try {
				isSpl = writeSpecialEntry(compG,
						SpecialEntitiesHandler.SpecialCompGroups);

				if (isSpl == false) {
					writeComponentGroup((ComponentGroup) compG);
				}

			} catch (ThemeException e) {
				e.printStackTrace();
				error(e.getMessage());
			}
		}
	}

	/**
	 * Writes all the component group info.
	 * 
	 * @throws ThemeException
	 */
	private void writeComponentGroup(ComponentGroup compG) {

		/*
		 * This function is called to process each component group For the given
		 * (input param) task - the function gets the component under it (the
		 * children of the given task). For each child - it checks if the child
		 * is given in the special component processing list; if a special
		 * component then it calls its predefined function; if not a special
		 * component then it calls the writeComponent funciton.
		 */

		if ((!compG.hasChildNodes()) || (!compG.getSelectionForTransfer())) {
			return;
		}

		List compList = compG.getChildren();

		for (int i = 0; i < compList.size(); i++) {
			ThemeBasicData comp = (ThemeBasicData) (compList.get(i));
			try {
				boolean isSpl = writeSpecialEntry(comp,
						SpecialEntitiesHandler.SpecialComp);

				if (isSpl == false) {
					writeComponent((Component) comp);
				}
			} catch (ThemeException e) {
				e.printStackTrace();
				error(e.getMessage());
			}
		}
	}

	/**
	 * Writes all the component info.
	 * 
	 * @throws ThemeException
	 */
	private void writeComponent(Component comp) {

		/*
		 * This function is called to process each component For the given
		 * (input param) task - the function gets the elements under it .For
		 * each child - it checks if the child is given in the special elements
		 * processing list; if a special component group then it calls its
		 * predefined function; if not it calls the writeElement function.
		 */

		if ((!comp.hasChildNodes()) || (!comp.getSelectionForTransfer())) {
			return;
		}

		// loop thro' the compG list .
		// 1. if a compG is special call the invokeMethod
		// 2. else work on its children
		List elemList = comp.getChildren();

		for (int i = 0; i < elemList.size(); i++) {
			ThemeBasicData elem = (ThemeBasicData) (elemList.get(i));
			try {
				String masterId = (String) elem.getAttribute().get(
						ThemeTag.ATTR_MASTERID);
				if (masterId != null) {
					SkinnableEntity master = ((S60Theme) elem.getRoot())
							.getSkinnableEntity(masterId);
					ThemeGraphic actual = master.getActualThemeGraphic();
					if (actual != null) {
						((Element) elem).setActualGraphic((ThemeGraphic) actual
								.clone());
					} else {
						((Element) elem).clearThemeGraphic(true);
					}
				}

				boolean isSpl = writeSpecialEntry(elem,
						SpecialEntitiesHandler.SpecialElements);
				if (isSpl == false) {
					writeElement((Element) elem);
				}
			} catch (ThemeException e) {
				e.printStackTrace();
				error(e.getMessage());
			}
		}
	}

	/**
	 * Writes all the elem info.
	 * 
	 * @throws ThemeException
	 */
	private void writeElement(Element elem) throws ThemeException {
		/*
		 * This function processes elements that are not defined in the special
		 * entries list.
		 */

		if (elem == null)
			return;

		/*S60Theme theme = ((S60Theme) elem.getRoot());
		String version = theme.getSelectedVersion();
		if (ThemeTag.VERSION_FIVE_DOT_ZERO.equals(version)) {
			version = DevicePlatform.S60_5_0.toString();
		}
		if (version == null) {
			version = theme.getThemeDescriptor().getDefaultPlatform().getId();
		}
		if (!(ThemeTag.VERSION_THREE_DOT_ONE.equalsIgnoreCase(version))
				&& !(ThemeTag.VERSION_THREE_DOT_TWO.equalsIgnoreCase(version))
				&& !(ThemeTag.VERSION_THREE_DOT_TWO.equalsIgnoreCase(version))
				&& !(DevicePlatform.S60_5_0.getId().equalsIgnoreCase(version))
				&& !(splEntitiesProcessorObj
						.allowedEntity(elem.getIdentifier())))
			return;*/

		Element e = elem;

		if (ThemeTag.ELEMENT_FRAME.equalsIgnoreCase(e.isEntityType())) {
			writeFrameEntity(e);
			return;
		}

		// At this point the elements we have are non frame elements

		/*
		 * Below all the parts and elements are accumulated and the
		 * corresponding function is called
		 */
		List<Object> entitesList = new ArrayList<Object>();
		entitesList.add(e);

		if (e.hasChildNodes()) {

			if (!e.isConvertedFromMultipiece()) {
				entitesList.addAll(e.getChildren());
			}
		}

		// Call the corresponding function to write each entity
		for (int i = 0; i < entitesList.size(); i++) {
			SkinnableEntity nextEntity = (SkinnableEntity) entitesList.get(i);

			if (ThemeTag.ELEMENT_SOUND.equals(nextEntity.isEntityType())
					|| (ThemeTag.ELEMENT_EMBED_FILE.equals(nextEntity
							.isEntityType()))) {
				writeFileEntity(nextEntity);
			} else if (ThemeTag.ELEMENT_BMPANIM.equals(nextEntity
					.isEntityType())) {
				writeBitmapAnimEntity(nextEntity);
			} else {
				if (nextEntity.isSkinned())
					writeGraphicsEntity(nextEntity);
			}
		}
	}

	/**
	 * Writes animation data
	 * 
	 * @param nextEntity
	 *            The entity to be processed
	 * @throws ThemeException
	 */
	private void writeBitmapAnimEntity(SkinnableEntity entity)
			throws ThemeException {

		String entityId = (String) entity.getAttributeValue(ThemeTag.ATTR_ID);
		if (entityId == null) {
			return;
		}

		if (isProcessed(entityId))
			return;

		BMPAnimItemDefinition anDefObj = new BMPAnimItemDefinition(properties);
		StringBuffer animDef = anDefObj.generateDefintion(entity,
				splEntitiesProcessorObj);
		error(anDefObj.getErrorString());

		if (animDef != null && animDef.length() > 0) {
			writeln(animDef.toString());
		}

		addToProcessedList(entityId);

	}

	/**
	 * Writes graphics data
	 * 
	 * @param nextEntity
	 *            The entity to be processed
	 * @throws ThemeException
	 */
	private void writeGraphicsEntity(SkinnableEntity entity)
			throws ThemeException {
		String entityId = (String) entity.getAttributeValue(ThemeTag.ATTR_ID);
		if (entityId == null) {
			return;
		}

		/*
		 * If parent has been processed, and it is a converted element, we don't
		 * have to write its parts.
		 */
		if ((entity.getParent() != null)
				&& (entity.getCurrentProperty() != null)
				&& (entity.getCurrentProperty()
						.equals(ThemeTag.ATTR_SINGLE_BITMAP))
				&& (isProcessed(entity.getParent().getId()))) {
			return;
		}

		if (isProcessed(entityId))
			return;

		StringBuffer graphicsDef = null;

		if (ThemeTag.ELEMENT_SCALEABLE.equalsIgnoreCase(entity.isEntityType())) {
			ScaleableItemDefinition scalObj = new ScaleableItemDefinition(
					properties);
			graphicsDef = scalObj.generateDefintion(entity);
			error(scalObj.getErrorString());
		} else if (ThemeTag.ELEMENT_MORPHING.equalsIgnoreCase(entity
				.isEntityType())) {
			MorphingItemDefinition scalObj = new MorphingItemDefinition(
					properties);
			graphicsDef = scalObj.generateDefintion(entity);
			error(scalObj.getErrorString());
		}

		else if (ThemeTag.ELEMENT_FASTANIMATION.equalsIgnoreCase(entity
				.isEntityType())) {
			FastAnimationItemDefinition scalObj = new FastAnimationItemDefinition(
					properties);
			graphicsDef = scalObj.generateDefintion(entity);
			error(scalObj.getErrorString());
		} else {
			String applUid = entity.getAttributeValue(ThemeTag.ATTR_APPUID);
			if (applUid == null) {
				// Write as bitmap defintion
				BitmapItemDefinition bitmapObj = new BitmapItemDefinition(
						properties);
				graphicsDef = bitmapObj.generateDefinition(entity);
				error(bitmapObj.getErrorString());
			} else {
				// Write as appicon defintion
				ApplIconItemDefinition applIcDef = new ApplIconItemDefinition(
						properties);
				graphicsDef = applIcDef.generateDefintion(entity);
				error(applIcDef.getErrorString());
			}
		}

		if (graphicsDef != null && graphicsDef.length() > 0) {
			writeln(graphicsDef.toString());
		}

		addToProcessedList(entityId);
	}

	/**
	 * Write the frame data into the descriptor file
	 * 
	 * @param itemId
	 *            The itemid of the frame
	 * @param parts
	 *            A map containing 9 entries. Each entry will have the align tag
	 *            as its key and the corresponding bitmap source string
	 *            (generated using makeBitmapSourceString) as its value
	 * @throws SkinAppException
	 *             when unable to successfully create the skin descriptor file
	 */
	public void writeFrameString(String itemId, Map parts, String themeDir)
			throws ThemeException {

		// Check if the id has already been processed
		if (writtenEntities.contains(itemId)) // already processed
			return;

		StringBuffer frameData = new StringBuffer();

		frameData.append(ThemeTag.SKN_TAG_FRAME);
		frameData.append(space);
		frameData.append(ThemeTag.SKN_TAG_IID);
		frameData.append(equal);
		frameData.append(makeItemId(itemId));
		frameData.append(linebreak);

		for (int i = 0; i < ThemeUtils.partsOrder.length; i++) {
			String partBmpData = (String) parts.get(ThemeUtils.partsOrder[i]);
			frameData.append(tab);

			frameData.append(partBmpData);
			frameData.append(linebreak);
		}

		frameData.append(ThemeTag.SKN_TAG_END);
		frameData.append(linebreak);
		writeln(frameData.toString());

		// write the id into the writtenentities set
		writtenEntities.add(itemId);
	}

	/**
	 * Generates the id from the image name. The function removes all the
	 * underscores and replaces the first character after the underscore to
	 * upper case letter.
	 * 
	 * @param imageName
	 *            The string containing the image name
	 * @return The generated id
	 */
	private String makeItemId(String str) {

		if (str == null)
			return null;

		int dot = str.indexOf('.');
		if (dot != -1) {
			str = str.substring(0, dot);
		}

		// while processing the item id - the version prefix should not be
		// packed (meaning the underscores
		// must not be removed from the version prefix string)
		// the id may be prefixed with version prefix delimited by % symbol (so
		// the version prefixed id
		// will look like <version_prefix>%<id> -- eg S60_2_6%qsn_fr_list

		str.trim();
		String versionDelimiter = null;
		int versionDelimiterPos = str.indexOf('%');
		if (versionDelimiterPos != -1) {
			versionDelimiter = str.substring(0, versionDelimiterPos + 1);
			str = str.substring(versionDelimiterPos + 1);

		}

		String temp;
		StringBuffer newStr = new StringBuffer();

		StringTokenizer st = new StringTokenizer(str, "_");

		while (st.hasMoreTokens()) {

			int len = newStr.length();

			temp = st.nextToken();

			newStr.append(temp);
			newStr.setCharAt(len, Character.toUpperCase(temp.charAt(0)));
		}

		// if the versionDelimiter was processed earlier then it should be put
		// back (prepended) to the id again
		if (versionDelimiter != null) {
			newStr.insert(0, versionDelimiter);
		}

		return newStr.toString();
	}

	/**
	 * Writes the frame info.
	 * 
	 * @throws ThemeException
	 */
	private void writeFrameEntity(Element frame) throws ThemeException {
		String entityId = (String) frame.getAttributeValue(ThemeTag.ATTR_ID);
		if (entityId == null) {
			return;
		}

		if (isProcessed(entityId))
			return;

		FrameItemDefinition frDefObj = new FrameItemDefinition(properties);
		StringBuffer frameDef = frDefObj.generateDefintion(frame, writtenParts);
		error(frDefObj.getErrorString());

		boolean wasWriten = false;
		if (frameDef != null && frameDef.length() > 0) {
			writeln(frameDef.toString());
			wasWriten = true;
		}

		addToProcessedList(entityId);

		// Also all the part ids need to be added to the processed list
		List partsList = frame.getChildren();
		if (null != partsList) {
			for (int i = 0; i < partsList.size(); i++) {
				SkinnableEntity nextPart = (SkinnableEntity) partsList.get(i);
				String partId = (String) nextPart
						.getAttributeValue(ThemeTag.ATTR_ID);
				addToProcessedList(partId);
				if (wasWriten) {
					writtenParts.add(partId);
				}
			}
		}

	}

	/**
	 * Writes the sound element data
	 * 
	 * @param e
	 *            The sound element
	 * @throws ThemeException
	 */
	private void writeFileEntity(SkinnableEntity e) throws ThemeException {

		String id = (String) e.getAttributeValue(ThemeTag.ATTR_ID);
		if (id == null)
			return;

		ThemeGraphic graphic = e.getPhoneThemeGraphic();

		if (graphic == null) {
			return;
		}

		String file = (String) graphic.getAttribute(ThemeTag.FILE_NAME);
		String fileName = file;
		int lastIndex = file.lastIndexOf(File.separator);
		if (lastIndex > -1)
			fileName = file.substring(lastIndex + 1);

		if (file != null)
			file = FileUtils.makeAbsolutePath(themeData.getThemeDir(), file);
		else
			return;

		// Copy the sound file to the output directory

		try {
			FileUtils.copyFile(file, FileUtils.makeAbsolutePath(outputDir
					.getAbsolutePath(), fileName));
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		}

		if (e.isEntityType().equals(ThemeTag.ELEMENT_SOUND)) {
			writeSoundString(id, "\"" + fileName + "\"");
		}
	}

	/**
	 * Write the sound data into the descriptor file
	 * 
	 * @param id
	 *            - The element id of the sound element
	 * @param soundSource
	 *            - The soundSource data generated using //makeBmpSourceString
	 *            function (with itemid as null there)
	 * @throws SkinAppException
	 *             when unable to successfully create the skin descriptor file
	 */
	private void writeSoundString(String id, String soundSource)
			throws ThemeException {

		// Check if the id has already been processed
		if (writtenEntities.contains(id)) // already processed
			return;

		StringBuffer soundData = new StringBuffer();
		soundData.append(ThemeTag.SKN_BINARY_SOUND);
		soundData.append(space);

		soundData.append(ThemeTag.SKN_TAG_IID);
		soundData.append(equal);
		soundData.append(id);
		soundData.append(space);

		soundData.append(soundSource);
		writeln(soundData.toString());

		// write the id into the writtenentities set
		writtenEntities.add(id);
	}

	/**
	 * Checks if the given entity needs to be given special processing code.
	 * 
	 * @param curEntity
	 *            The current task / compGroup / comp / element / part
	 * @param splMap
	 *            The reference to the map containing the special entries for
	 *            the given entity type (SpecialTasks, SpecialCompGroups,
	 *            SpecialComp, SpecialElements, SpecialParts)
	 * @return true if it has been processed with a special function
	 * @throws ThemeException
	 */
	private boolean writeSpecialEntry(ThemeBasicData curEntity, Map splMap)
			throws ThemeException {
		/*
		 * This function is called to check if the given entiy is a special
		 * data. If the given enity needs to be processed differently - then it
		 * calls its predefined function and returns true; if not it returns
		 * false.
		 */

		String curEntityIdentifier = curEntity.getIdentifier();

		if (splMap.containsKey(curEntityIdentifier)) {
			// This is a special entity - so invoke the special function for it
			String function = (String) splMap.get(curEntityIdentifier);

			if (function == null)
				return true;

			try {

				invokeMethod(function, curEntity);
			} catch (Exception e) {

				/* indication that element is not subject to special processing: */
				if (e instanceof ThemeException
						&& "FALSE".equals(e.getMessage()))
					return false;

				e.printStackTrace();
				throw new ThemeException(
						"Unable to finish writing special entry for "
								+ curEntityIdentifier);

			}
			curEntity.setExecuted(true);
			return true;
		}

		return false;
	}

	/**
	 * The function uses reflection to call the functions specified by the
	 * parameter
	 * 
	 * @param funcToInvoke
	 *            String holding the function name
	 * @throws ThemeException
	 *             when unable to successfully create the skin descriptor file
	 */
	private void invokeMethod(String funcToInvoke, ThemeBasicData sbd)
			throws ThemeException {

		Class splProcessorClass = splEntitiesProcessorObj.getClass();
		System.out.println("Class name :" + splProcessorClass.getName());

		Class[] classArray = new Class[1];
		classArray[0] = sbd.getClass();

		Object[] objArray = new Object[1];
		objArray[0] = sbd;

		try {
			// Do not use getDeclaredMethod since declaredmethod looks only into
			// the current class
			// and not into its super class. The disadvantage with getMethod is
			// that it looks for
			// only publicly accessible API's - so while coding all special
			// entity processing method
			// must have 'public' acess specifier.
			Method m = splProcessorClass.getMethod(funcToInvoke, classArray);
			m.invoke(splEntitiesProcessorObj, objArray);
		} catch (Exception e) {
			// Layout exceptions does not matter while packaging. As such
			// layout data is used only for preview. Hence, it is safe
			// to ignore layout exception.

			if (!(e.getCause() instanceof LayoutException)) {

				/*
				 * indication that element is not subject to special processing
				 * forwarding:
				 */
				if (e instanceof InvocationTargetException)
					e = (Exception) ((InvocationTargetException) e)
							.getTargetException();

				if (!(e.getCause() instanceof LayoutException)) {
					if (e instanceof ThemeException
							&& "FALSE".equals(e.getMessage()))
						throw (ThemeException) e;

					e.printStackTrace();
					throw new ThemeException("Failed to invoke spl method :"
							+ e.getMessage());
				}
			}
		}
	}

	/**
	 * Opens the text file for editing
	 */
	private void openTextFile() throws ThemeException {

		StringBuffer fileNameBuff = new StringBuffer();
		fileNameBuff.append(outputDir.getAbsolutePath());
		fileNameBuff.append(File.separator);
		fileNameBuff.append(themeData.getPackage());
		fileNameBuff.append(".txt");

		String textFileName = fileNameBuff.toString();

		try {
			// descFile = new PrintWriter(new BufferedWriter(new
			// FileWriter(textFileName)));
			/*
			 * text file should be written as unicode low endian
			 */
			descFile = new PrintWriter(textFileName, "UTF-16LE");
			descFile.append('\uFEFF'); // byte order marker

		} catch (IOException e) {
			e.printStackTrace();
			throw new ThemeException(
					"Unable to create skin descriptor file. Check access writes/space in "
							+ fileNameBuff);

		}
	}

	/**
	 * Writes a line of data into the descriptor file
	 * 
	 * @param data
	 *            The data to be written into the descriptor file
	 * @throws ThemeException
	 *             when unable to successfully create the skin descriptor file
	 */
	protected void writeln(String data) throws ThemeException {
		if (data != null && data.length() > 0)
			descFile.println(data);
		flush();
	}

	/**
	 * Writes a blank line
	 * 
	 * @throws ThemeException
	 *             when unable to successfully create the skin descriptor file
	 */
	protected void writeln() throws ThemeException {
		descFile.println();
		flush();
	}

	/**
	 * Flushes the stream (Used as a helper function)
	 * 
	 * @throws ThemeException
	 *             when unable to successfully create the skin descriptor file
	 */
	protected void flush() throws ThemeException {
		descFile.flush();
	}

	/**
	 * Closes the file descriptor
	 * 
	 * @throws ThemeException
	 *             when unable to successfully create the skin descriptor file
	 */
	public void close() throws ThemeException {
		descFile.close();
	}

	/**
	 * Processes the string to confirm to series 60 skin writing format. String
	 * is defined as sequence of characters (excluding linebreak) enclosed in
	 * double quotation marks (character 34).Any characters between “<” and “>”
	 * characters are interpreted as a single number that is replaced by the
	 * Unicode character of the same value. “<” and “>” are reserved for Unicode
	 * escapes, and the enclosed number is interpreted as a single Unicode
	 * character value. Double quotation marks are reserved for string
	 * termination and must not appear inside the string. If any of these
	 * reserved characters need to be included in the string itself, it must be
	 * properly escaped (i.e. the corresponding numeric Unicode character value
	 * must be used). Example: ”String with <34>Unicode escapes<0x22>”
	 * 
	 * @param value
	 *            The input string
	 * @return The processed string that could be written into the text file.
	 * @throws ThemeException
	 */
	protected String getSkinString(String value) throws ThemeException {

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '<':
				sb.append("<60>");
				break;
			case '>':
				sb.append("<62>");
				break;
			case '"':
				sb.append("<34>");
				break;
			case '\r':
				if (i < value.length() - 1 && value.charAt(i + 1) == '\n') {
					i++;
				}
				sb.append(" ");
				break;
			case '\n':
				sb.append(" ");
				break;
			default:
				sb.append(c);
			}
		}

		return sb.toString();
	}

	/**
	 * Sets the error string
	 * 
	 * @param errStr
	 *            Error string
	 */
	protected void error(String errStr) {
		if (errStr != null && errStr.trim().length() > 0)
			error.append(errStr).append("\n");
	}

	/*
	 * Returns the error data
	 */
	protected String getErrors() {
		if (error != null)
			return error.toString();
		else
			return null;
	}

	/**
	 * Returns true if the element id has already been processed.
	 * 
	 * @param id
	 *            The identfier to check
	 */
	protected boolean isProcessed(String id) {
		return writtenEntities.contains(id);
	}

	/**
	 * Add the given id to the processed list
	 * 
	 * @param id
	 *            The id to be added.
	 */
	protected void addToProcessedList(String id) {
		writtenEntities.add(id);
	}

	/**
	 * Sets the skintype information and the compliance level value
	 */
	private void setComplianceLevel() {
		SkinCompliance compl = (SkinCompliance) properties
				.get(ThemeTag.KEY_SKIN_COMPLIANCE);

		// IF not found in properties then check skin type
		if (compl == null) {
			String skinType = (String) themeData
					.getAttributeValue(ThemeTag.ATTR_SKINTYPE);
			if (ThemeTag.SKN_VALUE_SCALABLE.equalsIgnoreCase(skinType)) {
				compl = SkinCompliance.SCALEABLE_SKIN;
			} else {
				compl = SkinCompliance.BITMAP_SKIN;
			}
		}

		if (compl == SkinCompliance.SCALEABLE_SKIN) {
			skinType = ThemeTag.SKN_VALUE_SCALABLE;
			skinCompliance = SkinCompliance.SCALEABLE_SKIN;
		} else {
			skinType = ThemeTag.SKN_VALUE_NORMAL;
			skinCompliance = SkinCompliance.BITMAP_SKIN;
		}

		properties.put(ThemeTag.KEY_SKIN_COMPLIANCE, skinCompliance);
		properties.put(ThemeTag.KEY_SKIN_SKINTYPE, skinType);
	}
}
