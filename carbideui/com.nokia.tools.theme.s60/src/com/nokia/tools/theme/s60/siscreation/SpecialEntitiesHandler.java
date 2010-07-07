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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.theme.IThemeManager;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.general.ThemeAppBundle;
import com.nokia.tools.theme.s60.internal.core.Messages;
import com.nokia.tools.theme.s60.model.tpi.DefinedIcons;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIcon;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconManager;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconType;

/**
 *         The file provides the frame work for the special entity
 *         processing done while generating the descriptor file. The frame work
 *         can be extended to support different descriptor files for different
 *         S60 versions. Framework: For each S60 version, a new class has to be
 *         implemented that extends for SpecialEntitiesHandler. The new class
 *         will then be used to implement all the special processing (any other
 *         processing other than the default processing defined in
 *         themetextfilewriter class) Additionally the specialised classes must
 *         also mentioned the default attributes used by the item defintion
 *         classes (if they have to be differentiated between S60 versions). For
 *         example refer to FrameItemDefinition class (setEntityAttribute
 *         function)
 */
public class SpecialEntitiesHandler {

	// Reference to descriptor file handler object
	ThemeTextFileWriter descRef = null;

	// Data structures that hold the information of elements to be processed
	// seperately
	static Map<Object, Object> SpecialTasks = new HashMap<Object, Object>();

	static Map<Object, Object> SpecialCompGroups = new HashMap<Object, Object>();

	static Map<Object, Object> SpecialComp = new HashMap<Object, Object>();

	static Map<Object, Object> SpecialElements = new HashMap<Object, Object>();

	static Map<Object, Object> SpecialParts = new HashMap<Object, Object>();

	static Map<Object, Object> correctedId = new HashMap<Object, Object>();

	static Set CompulsoryElements = new HashSet();

	Map<Object, Object> PhoneEntityAttributes = new HashMap<Object, Object>();
	
	protected DefinedIcons _3rdIconsModel = null;

	// Definitions for special entry elements
	protected List<Object> allowedEntitiesList;

	// Definitions for default attributes / parameters that is supposed to be
	// used

	/**
	 * Constructor
	 * 
	 * @param descRef The reference to the skin descriptor file object
	 * @throws ThemeException
	 */
	SpecialEntitiesHandler(ThemeTextFileWriter descRef)
			throws ThemeException {
		this.descRef = descRef;
		this._3rdIconsModel = load3rdPartyIconsModel();
	}

	/**
	 * Initializes the data structures of the respective special entities class.
	 * The idea is to provide one call that can initialize the static data
	 * structures of the special entities class.
	 */
	public void initialize() {
	}

	protected void initAllowedEntities(String allowedEntitiesFilePath) {
		// To load the allowed entities into a list for further use.
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(allowedEntitiesFilePath));
			String str;
			allowedEntitiesList = new ArrayList<Object>();
			while ((str = br.readLine()) != null) {
				allowedEntitiesList.add(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Writes the skin specific settings values for example to enable or to
	 * disable line drawings
	 */
	public void writeSkinSettings() throws ThemeException {
	}

	/**
	 * This is the first skin creation function call. Hence all version specific
	 * process settings can be done here.
	 * 
	 * @throws ThemeException
	 */
	public void preProcess() throws ThemeException {
	}

	/**
	 * Revert to old state (the state before preprocess was called)
	 * 
	 * @throws ThemeException
	 */
	public void postProcess() throws ThemeException {

	}

	/**
	 * This is the method that is to be overridden by al the subclassses. It
	 * returns a boolean depending upon whether the id is present in the allowed
	 * list or not.
	 */

	public boolean allowedEntity(String id) {
		boolean isContained = false;
		
		if(allowedEntitiesList!=null){
			for (int i = 0; i < allowedEntitiesList.size(); i++) {
			if (allowedEntitiesList.get(i) != null
					&& allowedEntitiesList.get(i) instanceof String
					&& allowedEntitiesList.get(i).toString() != null
					&& allowedEntitiesList.get(i).toString().equalsIgnoreCase(
							id)) {
				isContained = true;
				break;
			}
			}
		}
		if (!isContained && _3rdIconsModel != null) {
			for (int i = 0; i < _3rdIconsModel.size(); i++) {
				ThirdPartyIcon ic = _3rdIconsModel.get(i);
				if (id.equals(ic.getId())) {
					isContained = true;
					break;
				}
			}
		}
		return isContained;
	}

	/**
	 * This is the method that is overridden by all the subclasses. It returns a
	 * Map containing the identifier string as it is or corrected.
	 */
	public String returnCorrectedId(String id) {
		return id;
	}

	/**
	 * Validates if the scaleable (layer) definition is a valid definition
	 * 
	 * @param tgrp The theme graphics object containing the new layer data that
	 *        needs to be checked
	 * @param entityType The entity type of the item
	 * @param error Error string (if any - useful when the return value is
	 *        false)
	 * @return True if the data is valid and can be saved
	 */
	public static boolean validateScaleableDefintion(SkinnableEntity sEntity,
			StringBuffer error) {

		/*
		 * In future if need arises the function can be extended to support
		 * different return values based on the platform version.
		 */

		/*
		 * Steps followed 1. Create a dummy entity 2. Make a 'fake call' (images
		 * are not processed) to individual scaleableitem classes 3. Check the
		 * output and process for the output layer value.
		 */
		Map<Object, Object> properties = new HashMap<Object, Object>();
		properties.put(ThemeTag.KEY_FAKE_CALL, new Boolean(true));

		StringBuffer graphicsDef = null;
		StringBuffer defnError = new StringBuffer();

		String entityType = sEntity.isEntityType();
		System.out.println("DEBUG: SPL_ENT_HAND: currEntity = "
				+ sEntity.getIdentifier() + " entityType : "
				+ sEntity.isEntityType());

		if (ThemeTag.ELEMENT_SCALEABLE.equalsIgnoreCase(entityType)) {
			ScaleableItemDefinition scalObj = null;
			try {
				scalObj = new ScaleableItemDefinition(properties);
				graphicsDef = scalObj.generateDefintion(sEntity);
			} catch (ThemeException e) {
				
				e.printStackTrace();
			}
			defnError.append(scalObj.getErrorString());
		} else if (ThemeTag.ELEMENT_MORPHING.equalsIgnoreCase(entityType)) {
			MorphingItemDefinition scalObj = null;
			try {
				scalObj = new MorphingItemDefinition(properties);
				graphicsDef = scalObj.generateDefintion(sEntity);
			} catch (ThemeException e) {
				
				e.printStackTrace();
			}
			defnError.append(scalObj.getErrorString());
		} else if (ThemeTag.ELEMENT_FASTANIMATION.equalsIgnoreCase(entityType)) {
			FastAnimationItemDefinition scalObj = null;
			try {
				scalObj = new FastAnimationItemDefinition(properties);
				graphicsDef = scalObj.generateDefintion(sEntity);

			} catch (ThemeException e) {
				
				e.printStackTrace();
			}
			defnError.append(scalObj.getErrorString());
		}

		// Get the OUTPUT string from the scaleable item definition

		int outputStackPos = Integer.MAX_VALUE;

		if (graphicsDef != null && graphicsDef.length() >= 0) {
			String outputPattern = "OUTPUT=";
			String FSLASH = "/";

			int outPos = graphicsDef.indexOf(outputPattern);
			if (outPos != -1) {
				int nextFslash = graphicsDef.indexOf(FSLASH, outPos);
				String finalOutputLayer = graphicsDef.substring(outPos
						+ outputPattern.length(), nextFslash);
				outputStackPos = Integer.valueOf(finalOutputLayer);
			} else { // check if refiid is there in the data
				String refIidPatern = " REFIID=";
				int refiidPos = graphicsDef.indexOf(refIidPatern);
				if (refiidPos != -1)
					outputStackPos = 0;
			}
		}

		// Process and check if the outputStackPos is usable or not
		int maxAllowed = 0;
		ResourceBundle propBundle = ThemeAppBundle.getPropBundle();
		if (ThemeTag.ELEMENT_SCALEABLE.equalsIgnoreCase(entityType)) {
			maxAllowed = Integer.parseInt(propBundle
					.getString("SCALEABLE_DEF_MAX_LAYER_LIMIT"));
			error.replace(0, error.length(),
					Messages.Scaleable_Def_Layer_Limit_Msg);
		} else if (ThemeTag.ELEMENT_MORPHING.equalsIgnoreCase(entityType)) {
			maxAllowed = Integer.parseInt(propBundle
					.getString("MORPHING_DEF_MAX_LAYER_LIMIT"));
			error.replace(0, error.length(),
					Messages.Morphing_Def_Layer_Limit_Msg);
		} else if (ThemeTag.ELEMENT_FASTANIMATION.equalsIgnoreCase(entityType)) {
			maxAllowed = Integer.parseInt(propBundle
					.getString("FASTANIM_DEF_MAX_LAYER_LIMIT"));
			error.replace(0, error.length(),
					Messages.FastAnim_Def_Layer_Limit_Msg);
		} else {
			// not a scealable item
			return true;
		}

		if (outputStackPos < maxAllowed) {
			return true;
		} else {
			return false;
		}
	}

	protected static void clearAndAddSpecialJobs() {
		SpecialTasks.clear();
		SpecialCompGroups.clear();
		SpecialComp.clear();
		SpecialElements.clear();
		SpecialParts.clear();
		correctedId.clear();
	}
	
	protected DefinedIcons load3rdPartyIconsModel() {
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			db.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
						String systemId) throws SAXException, IOException {
					return new InputSource(IThemeDescriptor.class
							.getResourceAsStream(IThemeManager.DTD_FOLDER
									+ new File(systemId).getName()));
				}
			});
			
			URL url = get3rdPartyIconDefinitionUrl();
			if (url != null) {
				Document xml = db.parse(url.openStream());
				
				DefinedIcons model = ThirdPartyIconManager.loadThirdParyIcons(xml, ThirdPartyIconType.TOOL_SPECIFIC);
				return model;
			}
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
		return null;
	}
	
	protected URL get3rdPartyIconDefinitionUrl() throws IOException {
		return null;
	}
}
