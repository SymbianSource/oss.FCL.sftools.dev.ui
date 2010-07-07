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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.nokia.tools.media.color.ColorUtil;


public class GroupsLoader {
	public static final String COLOR_GROUPS_TAG = "grps";

	public static final String COLOR_GROUP_TAG = "grp";

	public static final String COLOR_GROUP_NAME = "name";

	public static final String COLOR_GROUP_COLOR = "rgb";

	public static final String COLOR_GROUP_ITEM_TAG = "item";

	public static final String ITEM_ID_TAG = "id";

	public static final String IMAGE_PART_OR_LAYER_TAG = "layerOrSection";

	public static final String ITEM_PART_ID_TAG = "part";
	
	public static final String PARENT_GROUP="parentGrp";
	
	public static final String CHILD_GROUP="childGrp";
	
	
	private List<ColorGroup> colorGroups = new ArrayList<ColorGroup>();
	

	public static final String DOC_PATH = "groups.xml";

	public List<ColorGroup> getColorGroups(){
		return colorGroups;
	}
	
	ColorGroups grps=null;
	
	public GroupsLoader(ColorGroups grps){
		this.grps=grps;
	}
	
	public synchronized void init() {
		InputStream in = null;
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();

			// Parse the input
			SAXParser saxParser = parserFactory.newSAXParser();

			IWorkbench wb = PlatformUI.getWorkbench();
			 IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			 IWorkbenchPage page = win.getActivePage();
			 if(page!=null){
				 IEditorPart editor = page.getActiveEditor();
				 if((editor != null && editor.getEditorInput() instanceof FileEditorInput)){
				 IFile original = ((FileEditorInput)editor.getEditorInput()).getFile();
				 IPath parentFolderLocation=original.getParent().getLocation();
				 String groupsFilePath = new String(parentFolderLocation+File.separator+DOC_PATH);
				 if(new File(groupsFilePath).exists()){
					 in = new FileInputStream(new File(groupsFilePath)); 
					 //	GroupsLoader.class.getResourceAsStream(DOC_PATH);
					 saxParser.parse(in, new GroupsHandler());
				 }
				 
				 else {
					 File file = new File(parentFolderLocation.toOSString().concat(File.separator).concat("config").concat(File.separator).concat("model").concat(File.separator).concat(DOC_PATH));
				 if(file.exists()){
					 in = new FileInputStream(file); 
					 //	GroupsLoader.class.getResourceAsStream(DOC_PATH);
					 saxParser.parse(in, new GroupsHandler());
				 }}
				 
				 }
			 }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	class GroupsHandler extends DefaultHandler {
		
		

		

		private ColorGroup colorGroup = null;

		private List<ColorGroupItem> colorGroupItem = new ArrayList<ColorGroupItem>();

		private String itemId = "";

		private String imagePartOrLayer = "";

		private String itemPartId = "";

		private String colorGroupName = "";

		private String colorGroupColor = "";
		
		private String parentGroupName="";
		
		private List<String> childrenGroups= new ArrayList<String>();

		private final int THEME_TAG_ID = 0;

		private final int COLOR_GROUPS_TAG_ID = 1;

		private final int COLOR_GROUP_TAG_ID = 2;

		private final int COLOR_GROUP_ITEM_TAG_ID = 3;

		private final int ITEM_ID_TAG_ID = 4;

		private final int IMAGE_PART_OR_LAYER_TAG_ID = 5;

		private final int ITEM_PART_ID_TAG_ID = 6;

		private final int COLOR_GROUP_NAME_TAG_ID = 7;

		private final int COLOR_GROUP_COLOR_TAG_ID = 8;
		
		private final int PARENT_GROUP_TAG_ID = 9;

		private final int CHILD_GROUP_TAG_ID = 10;

		public GroupsHandler() {			
			tags.put(COLOR_GROUPS_TAG, COLOR_GROUPS_TAG_ID);
			tags.put(COLOR_GROUP_TAG, COLOR_GROUP_TAG_ID);
			tags.put(COLOR_GROUP_ITEM_TAG, COLOR_GROUP_ITEM_TAG_ID);
			tags.put(ITEM_ID_TAG, ITEM_ID_TAG_ID);
			tags.put(IMAGE_PART_OR_LAYER_TAG, IMAGE_PART_OR_LAYER_TAG_ID);
			tags.put(ITEM_PART_ID_TAG, ITEM_PART_ID_TAG_ID);
			tags.put(COLOR_GROUP_NAME, COLOR_GROUP_NAME_TAG_ID);
			tags.put(COLOR_GROUP_COLOR, COLOR_GROUP_COLOR_TAG_ID);
			tags.put(PARENT_GROUP, PARENT_GROUP_TAG_ID );
			tags.put(CHILD_GROUP, CHILD_GROUP_TAG_ID);

		}

		Map<String, Integer> tags = new HashMap<String, Integer>();

		int currentItemTag = -1;

		public void startElement(String namespaceURI, String lName, // local
																	// name
				String qName, // qualified name
				Attributes attrs) throws SAXException {

			for (String tag : tags.keySet()) {
				if (tag.equals(qName)) {
					currentItemTag = tags.get(tag);
					break;
				}
			}
		}

		final public void characters(final char[] ch, final int start,
				final int len) {
			String text = new String(ch, start, len);
			String trimmedString = text.trim();
			boolean isEmpty = trimmedString.length() == 0 ? true : false;
			if (isEmpty) {
				return;
			}

			switch (currentItemTag) {

			case THEME_TAG_ID:			
				break;
			case COLOR_GROUPS_TAG_ID:					
				break;
			case COLOR_GROUP_TAG_ID:					
				break;
			case COLOR_GROUP_ITEM_TAG_ID:					
				break;
			case ITEM_ID_TAG_ID:
				itemId = trimmedString;
				break;
			case IMAGE_PART_OR_LAYER_TAG_ID:
				imagePartOrLayer = trimmedString;
				break;
			case ITEM_PART_ID_TAG_ID:
				itemPartId = trimmedString;
				break;
			case COLOR_GROUP_NAME_TAG_ID:
				colorGroupName = trimmedString;
				break;
			case COLOR_GROUP_COLOR_TAG_ID:
				colorGroupColor = trimmedString;
				break;
			case PARENT_GROUP_TAG_ID:
				parentGroupName=trimmedString;
				break;
			case CHILD_GROUP_TAG_ID:
				childrenGroups.add(trimmedString);
				break;
				
			default:
				break;

			}
		}

		final public void endElement(final String namespace,
				final String localname, final String type) {
			
			if (COLOR_GROUP_TAG.equals(type)) {
				
				if (colorGroupName != null && colorGroupColor != null
						&& ColorUtil.isColor(colorGroupColor)) {
					colorGroup = new ColorGroup(ColorUtil
							.getRGB(colorGroupColor), colorGroupName,grps);
					GroupsLoader.this.colorGroups.add(colorGroup); 
					colorGroup.addAllItemsToGroup(colorGroupItem);
					if(parentGroupName!=null&&parentGroupName.length()>0){
						colorGroup.setParentGroupName(parentGroupName);
					}
					if(childrenGroups.size()>0){
						for(String childGroupName:childrenGroups){
							colorGroup.addChildrenGroup(childGroupName);
						}
					}
					colorGroup = null;
					colorGroupItem= new ArrayList<ColorGroupItem>();
					parentGroupName="";
					childrenGroups= new ArrayList<String>();
				}

			} else if (COLOR_GROUP_ITEM_TAG.equals(type)) {
				// finish one groupItem
				if (!"".equals(itemId)) { // item must have id
					colorGroupItem.add(new ColorGroupItem(itemId,
							itemPartId, imagePartOrLayer));
				}

			}
		}
	}

}