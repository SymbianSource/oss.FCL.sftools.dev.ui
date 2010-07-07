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
package com.nokia.tools.theme.s60.model.tpi;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.IThemeManager;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeUtil;
import com.nokia.tools.theme.s60.IThemeConstants;
import com.nokia.tools.theme.s60.S60ThemeContent;
import com.nokia.tools.theme.s60.S60ThemeProvider;
import com.nokia.tools.theme.s60.model.S60Theme;

/**
 * This class provides all the Helper methods which will perform the 
 * publishing of the changes done to the Third Party Icons [Tool specific and Theme Specific]
 * to the themes/models for tool specific icons and for the specific theme for the Theme specific TPIs.
 */
public class ThirdPartyIconChangesPublisher {
	
	public static void refresh3rdPartyIcons(DefinedIcons oldModel, DefinedIcons newModel, boolean themeSpecific, S60Theme theme, boolean releaseModels)
			throws Exception {
		List<String> oldThirdPartyIconsIds = thirdPartyIconIds(oldModel);

		// releases the model, may need finer controls later
		if(releaseModels)
			releaseTheme();

		List<String> thirdPartyIconsIds = thirdPartyIconIds(newModel);

		try {
			if(!themeSpecific){
				IEditorReference openEditorsReferences[] = openEditorsReferences();
				refreshInEditorsToolSpecificTPIChanges(newModel, oldThirdPartyIconsIds, thirdPartyIconsIds, openEditorsReferences);
				refreshToolSpecificTPIChangesInModels(newModel, oldThirdPartyIconsIds, thirdPartyIconsIds);
			}
			else{
				refreshInEditorsThemeSpecificTPIChanges(newModel, oldThirdPartyIconsIds, thirdPartyIconsIds, theme);
			}
		} catch (Exception e1) {
			throw e1;
		}
	}

	/**
	 * Will perform the refresh in all the open Editors and all the associated 
	 * Themes and their model for Tool specific TPI changes.
	 * Note [Important]: This method should not be called for Theme specific TPI changes.
	 */
	private static void refreshInEditorsToolSpecificTPIChanges(DefinedIcons newModel,	List<String> oldThirdPartyIconsIds,
			                                List<String> thirdPartyIconsIds, IEditorReference[] openEditorsReferences)
			throws ThemeException, MalformedURLException {

		for (IEditorReference editorReference : openEditorsReferences) {
			IEditorPart editor = editorReference.getEditor(false);
			if (canProcessEditor(editor)) {
				
				IContentAdapter adapter = (IContentAdapter) editor.getAdapter(IContentAdapter.class);
				if (adapter != null) {
					String findId = oldThirdPartyIconsIds.size() > 0 ? oldThirdPartyIconsIds.get(0)
							: thirdPartyIconsIds.size() > 0 ? thirdPartyIconsIds.get(0)	: null;
					if (findId != null) {
						IContent[] content = adapter.getContents(S60ThemeProvider.CONTENT_TYPE);
						if(content == null || content.length == 0){
							continue;
						}
						if(!(content[0].getRoot() instanceof S60ThemeContent)){
							continue;
						}
						ThemeContent root = (ThemeContent) content[0].getRoot();
						S60Theme theme = (S60Theme) root.getData();

					
						try {
							ThirdPartyIconManager.loadToolSpecificThirdPartyIcons(theme);
						} catch (ThirdPartyIconLoadException e) {
							throw new ThemeException(e);
						}
						theme.refreshElementList();

						Element findElem = theme.getElementWithId(findId);
						if (findElem != null) {
							performThemeDataRefresh(newModel,
									oldThirdPartyIconsIds, thirdPartyIconsIds,
									adapter, root, theme, findElem);
						}
					}
				}
			}
		}
	}
	
	private static void refreshToolSpecificTPIChangesInModels(DefinedIcons newModel, List<String> oldThirdPartyIconsIds,
            							List<String> thirdPartyIconsIds) throws ThemeException, MalformedURLException {
		List<Theme> allLoadedModels = ThemeUtil.getAllLoadedModels();
		for(Theme model: allLoadedModels){
			if(model instanceof S60Theme){
				S60Theme s60Theme = (S60Theme)model;
				
				try {
					ThirdPartyIconManager.loadToolSpecificThirdPartyIcons(s60Theme);
				} catch (ThirdPartyIconLoadException e) {
					throw new ThemeException(e);
				}
				s60Theme.refreshElementList();
				String findId = oldThirdPartyIconsIds.size() > 0 ? oldThirdPartyIconsIds.get(0)
						: thirdPartyIconsIds.size() > 0 ? thirdPartyIconsIds.get(0)	: null;
				Element findElem = s60Theme.getElementWithId(findId);
				if (findElem != null) {
					performThemeDataRefresh(newModel,
							oldThirdPartyIconsIds, thirdPartyIconsIds,
							s60Theme, findElem);
				}
			}
		}
		
	}
	
	private static void performThemeDataRefresh(DefinedIcons newModel,
			List<String> oldThirdPartyIconsIds,
			List<String> thirdPartyIconsIds, S60Theme theme, Element findElem) throws ThemeException{
		

		ThemeBasicData iconsGroup = findElem.getParent();

		List<Object> toRemove = new ArrayList<Object>();
		for (Object child : iconsGroup.getChildren()) {
			String id = ((ThemeBasicData) child).getId();
			if (oldThirdPartyIconsIds.contains(id)
					&& !thirdPartyIconsIds.contains(id)) {
				toRemove.add(child);
			}
		}

		for (Object child : toRemove) {
			iconsGroup.removeChild(child);
		}

		// update appuid and name
		for (Iterator<ThirdPartyIcon> iter = newModel.iterator(); iter.hasNext();) {
			ThirdPartyIcon iconDef = iter.next();
			ThemeBasicData child = iconsGroup.getChild(iconDef.getId());
			if (child != null) {
				ThemeBasicData childInComponentsView = (ThemeBasicData) child;
				childInComponentsView.setAttribute(ThemeTag.ATTR_APPUID, iconDef.getAppUid());
				childInComponentsView.setAttribute(ThemeTag.ATTR_NAME, iconDef.getName());
				childInComponentsView.setAttribute(ThemeTag.ATTR_MAJORID,iconDef.getMajorId());
				childInComponentsView.setAttribute(ThemeTag.ATTR_MINORID,iconDef.getMinorId());
				
				if(childInComponentsView instanceof ThirdPartyIcon){
					ThirdPartyIcon tempTPIIcon = (ThirdPartyIcon)childInComponentsView;
					tempTPIIcon.updateThirdPartyIconProperties(iconDef);
				}
			}
		}

		theme.refreshElementList();
	}

	private static void performThemeDataRefresh(DefinedIcons newModel,
			List<String> oldThirdPartyIconsIds,
			List<String> thirdPartyIconsIds, IContentAdapter adapter,
			ThemeContent root, S60Theme theme, Element findElem)
			throws ThemeException {
		ThemeBasicData iconsGroup = findElem.getParent();
		ThemeData iconsGroupData = (ThemeData) root.findByData(iconsGroup);

		List<Object> toRemove = new ArrayList<Object>();
		for (Object child : iconsGroup.getChildren()) {
			String id = ((ThemeBasicData) child).getId();
			if (oldThirdPartyIconsIds.contains(id)
					&& !thirdPartyIconsIds.contains(id)) {
				toRemove.add(child);
			}
		}

		for (Object child : toRemove) {
			if(child instanceof ThemeBasicData){
				((ThemeBasicData) child).setSkinned(false);
			}
			iconsGroup.removeChild(child);
		}

		// update appuid and name
		for (Iterator<ThirdPartyIcon> iter = newModel.iterator(); iter.hasNext();) {
			ThirdPartyIcon iconDef = iter.next();
			ThemeBasicData child = iconsGroup.getChild(iconDef.getId());
			if (child != null) {
				ThemeBasicData childInComponentsView = (ThemeBasicData) child;

				String oldName = childInComponentsView.getName();
				String newName = iconDef.getName();
				childInComponentsView.setAttribute(ThemeTag.ATTR_APPUID, iconDef.getAppUid());
				childInComponentsView.setAttribute(ThemeTag.ATTR_NAME, iconDef.getName());
				childInComponentsView.setAttribute(ThemeTag.ATTR_MAJORID,iconDef.getMajorId());
				childInComponentsView.setAttribute(ThemeTag.ATTR_MINORID,iconDef.getMinorId());
				
				if(childInComponentsView instanceof ThirdPartyIcon){
					ThirdPartyIcon tempTPIIcon = (ThirdPartyIcon)childInComponentsView;
					tempTPIIcon.updateThirdPartyIconProperties(iconDef);
				}
				
				updateUIModelOnNameChange(root,	iconsGroupData,	childInComponentsView, 
										  oldName, newName);
			}
		}

		theme.refreshElementList();

		root.getProvider().updateTree(iconsGroupData, iconsGroup);
		// reorder 3rd party icons
		for (String id : thirdPartyIconsIds) {
			IContentData data = iconsGroupData.findById(id);
			if (data != null) {
				// remove
				iconsGroupData.removeChild(data);
				// add with correct order
				iconsGroupData.addChild(data);
			}
		}
		if(adapter != null){
			adapter.fireContentModified(new String[] { iconsGroupData.getId() });
		}
	}


	/**
	 * Will perform the refresh for the Theme specific TPI changes.
	 * It will also refresh the Editor containing the Theme only and not other theme. 
	 * Note [Important]: This method should not be called for Tool specific TPI changes.
	 */
	private static void refreshInEditorsThemeSpecificTPIChanges(DefinedIcons newModel, List<String> oldThirdPartyIconsIds,
			                    List<String> thirdPartyIconsIds, S60Theme theme) throws ThemeException, MalformedURLException {

		for (IEditorReference editorReference : openEditorsReferences()) {
			IEditorPart editor = editorReference.getEditor(false);
			if (canProcessEditor(editor)) {
				
				IContentAdapter adapter = (IContentAdapter) editor.getAdapter(IContentAdapter.class);
				if (adapter != null) {
					String findId = oldThirdPartyIconsIds.size() > 0 ? oldThirdPartyIconsIds.get(0)
							: thirdPartyIconsIds.size() > 0 ? thirdPartyIconsIds.get(0)	: null;
					if (findId != null) {
						IContent[] content = adapter.getContents(S60ThemeProvider.CONTENT_TYPE);
						
						if(content == null || content.length == 0){
							continue;
						}
						if(!(content[0].getRoot() instanceof S60ThemeContent)){
							continue;
						}

						ThemeContent root = (ThemeContent) content[0].getRoot();
						if(theme == root.getData()){
							try {
								ThirdPartyIconManager.loadThemeSpecificIcons(theme);
							} catch (ThirdPartyIconLoadException e) {
								throw new ThemeException(e);
							}
							theme.refreshElementList();

							Element findElem = theme.getElementWithId(findId);
							if (findElem != null) {
								performThemeDataRefresh(newModel,
										oldThirdPartyIconsIds, thirdPartyIconsIds,
										adapter, root, theme, findElem);
							}
						}
					}
				}
			}
		}
	}
	
	
	
	
	private static void updateUIModelOnNameChange(ThemeContent root,
			ThemeData iconsGroupData, ThemeBasicData childInComponentsView,
			String oldName, String newName) {
		if (!oldName.equals(newName)) {
			ThemeData childToRename = root.findByData(childInComponentsView);
			iconsGroupData.getResource().getChildren().remove(
					childToRename.getResource());
			iconsGroupData.getResource().getChildren().add(
					childToRename.getResource());
		}
	}

	private static boolean canProcessEditor(IEditorPart editor) {
		return editor != null && 
		       !editor.getClass().getSimpleName().equals("PluginEditorPart");
	}

	private static IEditorReference[] openEditorsReferences() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getEditorReferences();
	}


	private static void releaseTheme() {
		IThemeManager manager = ThemePlatform
				.getThemeManagerByContainerId(IThemeConstants.THEME_CONTAINER_ID);
		for (IThemeDescriptor descriptor : ThemePlatform.getThemeDescriptors()) {
			if (IThemeConstants.THEME_CONTAINER_ID.equalsIgnoreCase(descriptor
					.getContainerId())) {
				manager.releaseTheme(descriptor.getId());
			}
		}
	}

	private static List<String> thirdPartyIconIds(DefinedIcons model) {
		List<String> thirdPartyIconsIds = new ArrayList<String>();
		for (Iterator<ThirdPartyIcon> iter = model.iterator(); iter.hasNext();) {
			ThirdPartyIcon icon = iter.next();
			thirdPartyIconsIds.add(icon.getId());
		}
		return thirdPartyIconsIds;
	}
	
}
