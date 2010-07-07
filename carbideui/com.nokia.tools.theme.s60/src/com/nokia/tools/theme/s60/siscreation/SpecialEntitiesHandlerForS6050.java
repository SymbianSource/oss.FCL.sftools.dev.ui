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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nokia.tools.platform.theme.Component;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Task;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.general.ThemeAppBundle;
import com.nokia.tools.theme.s60.model.S60Theme;

class SpecialEntitiesHandlerForS6050 extends SpecialEntitiesHandler {
	
	
    protected static void clearAndAddSpecialJobs(){
		SpecialEntitiesHandler.clearAndAddSpecialJobs();

		//Special task entries    	
		SpecialTasks.put("COLOURS","writeTaskColoursData");
		
		//Special component groups
		SpecialComp.put("Context Pane Icons", "writeApplIconData");
    }
	
	//Definitions for default attributes / parameters that is supposed to be used
	private void clearAndAddSpecialPhoneProperties(){
		PhoneEntityAttributes.clear();
		
		PhoneEntityAttributes.put("frame_tl", ThemeTag.SKN_TAG_STRETCH);
		PhoneEntityAttributes.put("frame_tr", ThemeTag.SKN_TAG_STRETCH);
		PhoneEntityAttributes.put("frame_bl", ThemeTag.SKN_TAG_STRETCH);
		PhoneEntityAttributes.put("frame_br", ThemeTag.SKN_TAG_STRETCH);
		PhoneEntityAttributes.put("frame_t", ThemeTag.SKN_TAG_STRETCH);
		PhoneEntityAttributes.put("frame_b", ThemeTag.SKN_TAG_STRETCH);
		PhoneEntityAttributes.put("frame_l", ThemeTag.SKN_TAG_STRETCH);
		PhoneEntityAttributes.put("frame_r", ThemeTag.SKN_TAG_STRETCH);
		PhoneEntityAttributes.put("frame_center", ThemeTag.SKN_TAG_STRETCH);
		
	}
	
    /**
     * Constructor
     * @param descRef The reference to the skin descriptor file object
     * @throws ThemeException
     */
    SpecialEntitiesHandlerForS6050 (ThemeTextFileWriter descRef) throws ThemeException {
    	super(descRef); 
    }
	
	/**
	 * Initializes the data structures of the respective special entities class.
	 */
	public void initialize() {
		clearAndAddSpecialJobs();
		clearAndAddSpecialPhoneProperties();
		initAllowedEntities(ThemeAppBundle.getPropBundle().getString(
				ThemeConstants.ALLOWED_ENTITIES_PATH_50));
	}
    
	/**
	 * This method returns the corrected id.
	 */
	public String returnCorrectedId(String id){
		if(correctedId.containsKey(id)){
			return correctedId.get(id).toString();
		} else{
			return id;
		}
	}
    
	
	/**
	 * Writes the skin specific settings values
	 * for example to enable or to disable line drawings
	 */
	public void writeSkinSettings () throws ThemeException {
		S60Theme skinData = descRef.getThemeObject(); 
		HashMap<String, HashMap<Object, Object>> skinSettingsMap = skinData.getSkinSettings();
		
		if (skinSettingsMap == null)
			return;
		
		String[] idStrings = (String[])skinSettingsMap.keySet().toArray(new String[1]);
		for (int i=0; i< idStrings.length; i++){
			
			//The value of each of the settings key will give a hashmap containing
			//all the attributes associated with the key element (xml attributes)
			String nextSettingId = (String)idStrings[i];
			HashMap nextSetting = (HashMap)skinSettingsMap.get(nextSettingId);
			if (nextSetting == null)
				continue;
			
			//The data has to be written into the text file if
			//1. The user has explicitly set value for it
			//2. If it is a mandatory attribute.
			boolean isMandatory = new Boolean((String)nextSetting.get(ThemeTag.ATTR_MANDATORY)).booleanValue();
			String value = (String)nextSetting.get(ThemeTag.ATTR_VALUE);
			
			if (value == null && isMandatory == true){
				value = (String)nextSetting.get(ThemeTag.DEFAULT);
			}
			
			if (value != null){
				String nextSettingIid = BasicItemDefinition.makeItemId(nextSettingId);
				StringBuffer nextSettingDefn = new StringBuffer ();
				nextSettingDefn.append(BasicItemDefinition.STRING).append(BasicItemDefinition.SPACE);
				nextSettingDefn.append(BasicItemDefinition.IID).append(BasicItemDefinition.EQUAL);
				nextSettingDefn.append(nextSettingIid).append(BasicItemDefinition.SPACE);
				nextSettingDefn.append("\"").append(value).append("\"");
				
				descRef.writeln(nextSettingDefn.toString());
			}
		}
	}
	
	/**
	 * Writes the colour task into the text file
	 */
	public void writeTaskColoursData(Task colourTask) throws ThemeException {
		if (colourTask == null)
			return;

		if (!colourTask.getSelectionForTransfer())
			return;

		Map properties = descRef.getProperties();
		ColourItemDefinition cItDef = new ColourItemDefinition(properties);
		StringBuffer colourDef = cItDef.generateDefintion(colourTask);

		if (colourDef != null) {
			descRef.writeln(colourDef.toString());
		}
	}
	
	/**
	 * Writes the icon details into the file
	 * 
	 * @param cntxIconComp
	 * @throws ThemeException
	 */
	public void writeApplIconData(Component cntxIconComp) throws ThemeException {
		if (!cntxIconComp.getSelectionForTransfer())
			return;

		Map properties = descRef.getProperties();

		List cntxIcons = cntxIconComp.getChildren();
		for (int i = 0; i < cntxIcons.size(); i++) {
			SkinnableEntity nextCxtIcon = (SkinnableEntity) cntxIcons.get(i);
			if (!nextCxtIcon.getSelectionForTransfer()) {
				continue;
			}

			String masterId = (String) nextCxtIcon.getAttribute().get(
					ThemeTag.ATTR_MASTERID);
			if (masterId != null) {
				SkinnableEntity master = ((S60Theme) nextCxtIcon.getRoot())
						.getSkinnableEntity(masterId);
				ThemeGraphic actual = master.getActualThemeGraphic();
				if (actual != null) {
					nextCxtIcon.setActualGraphic((ThemeGraphic) actual.clone());
				} else {
					nextCxtIcon.clearThemeGraphic(true);
				}
			}

			/*
			 * If the icon element does not
			 * have an uid then we have to call the bitmap definition class to
			 * generate the bitmap item defintion for the corresponding icon.
			 */
			StringBuffer iconDef = null;

			String applUid = nextCxtIcon
					.getAttributeValue(ThemeTag.ATTR_APPUID);
			if (applUid == null) {
				// Write as bitmap defintion
				BitmapItemDefinition bitmapObj = new BitmapItemDefinition(
						properties);
				iconDef = bitmapObj.generateDefinition(nextCxtIcon);
				descRef.error(bitmapObj.getErrorString());
			} else {
				// Write as appicon defintion
				ApplIconItemDefinition applIcDef = new ApplIconItemDefinition(
						properties);
				iconDef = applIcDef.generateDefintion(nextCxtIcon);
				descRef.error(applIcDef.getErrorString());
			}

			if (iconDef != null) {
				descRef.writeln(iconDef.toString());
			}
		}

		// No need to write into processed list -- since application icons are
		// supposed
		// to be grouped in one place -- so that they can be handled seperately
	}

	
 }