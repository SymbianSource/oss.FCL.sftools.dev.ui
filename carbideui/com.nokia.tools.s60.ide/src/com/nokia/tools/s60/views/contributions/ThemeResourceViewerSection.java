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
package com.nokia.tools.s60.views.contributions;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.internal.ui.palette.editparts.ToolEntryEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.editor.actions.AdjustThemeListAction;
import com.nokia.tools.s60.editor.actions.CopyContentDataAction;
import com.nokia.tools.s60.editor.actions.PasteContentDataAction;
import com.nokia.tools.s60.editor.actions.SkinBySimilarAction;
import com.nokia.tools.s60.internal.utils.HideableMenuManager;
import com.nokia.tools.s60.views.ViewMessages;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.views.IResourceSection;
import com.nokia.tools.screen.ui.views.IResourceViewRectProvider;
import com.nokia.tools.screen.ui.views.IResourceViewerSelectionHelper;
import com.nokia.tools.theme.content.ThemeUtil;
import com.nokia.tools.theme.s60.ui.cstore.ComponentStoreFactory;
import com.nokia.tools.theme.ui.ThemeCategoryProvider;

public class ThemeResourceViewerSection implements IResourceSection,
		IResourceViewRectProvider {

	private static final String XMLUI = "XMLUI";
	private static final String sectionHeading = ViewMessages.ResourceView_Theme_Category;

	public List<PaletteDrawer> getPaletteDrawers(IContentAdapter contentAdapter) {
		List<PaletteDrawer> drawers = new ArrayList<PaletteDrawer>();
		if (contentAdapter != null) {
			// IContent themeContent =
			// ScreenUtil.getPrimaryContent(contentAdapter
			// .getContents());
			IContent[] themeContents = contentAdapter.getContents();
			IContent themeContent = null;
			for (IContent content : themeContents) {
				if (ScreenUtil.isPrimaryContent(content)
						) {
					themeContent = content;
					break;
				}
			}
			if (themeContent != null) {
				IContentData[] elements = ThemeCategoryProvider
						.getRootLevelElements(themeContent);
				for (IContentData element : elements) {
					PaletteDrawer drawer = new PaletteDrawer(element.getName(),
							element.getImageDescriptor(24, 24));
					for (IContentData child : element.getChildren()) {
						ImageDescriptor desc = child.getImageDescriptor(16, 16);
						drawer.add(new CombinedTemplateCreationEntry(child
								.getName(), ThemeUtil.toTitleCase(element
								.getName()), child, null, desc, desc));
					}
					drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
					drawers.add(drawer);
				}
			}
		}
		return drawers;
	}

	public String getSectionHeading() {

		return sectionHeading;
	}

	public IResourceViewerSelectionHelper getSelectionHelper(IContentData data) {
		if (data instanceof IContentData) {
			if (ScreenUtil.isPrimaryContent(data)
					&& !XMLUI.equals(data.getRoot().getType())) {
				return new ThemeResourceViewerSelectionHelper();
			}
		}
		return null;
	}

	public IMenuListener createResourceViewerMenuListener(
			final ISelectionProvider selectionProvider, final CommandStack stack) {
		return new IMenuListener() {
			public void menuAboutToShow(IMenuManager mmgr) {
				AbstractAction action = new CopyContentDataAction(
						selectionProvider, null);
				action.update();
				// mmgr.add(action);

				HideableMenuManager cpCpy = new HideableMenuManager(action
						.getText());
				try {
					action.setText("Skinned Only");
					cpCpy.add(action);

					action = new CopyContentDataAction(selectionProvider, null);
					((CopyContentDataAction) action).setCopySkinnedOnly(false);
					action.update();
					action.setText("All");
					cpCpy.add(action);

				} catch (Exception e) {
					e.printStackTrace();
				}

				// if (cpSubmenu.isEnabled()) {
				mmgr.add(new Separator());
				mmgr.add(cpCpy);
				// }

				action = new PasteContentDataAction(selectionProvider, stack,
						null);
				action.update();
				if( action.isEnabled() )
					mmgr.add(action);

				// skin by similar actions
				{
					IStructuredSelection sel = (IStructuredSelection) selectionProvider
							.getSelection();
					if (sel.getFirstElement() instanceof ToolEntryEditPart) {
						ToolEntryEditPart tEditP = (ToolEntryEditPart) sel
								.getFirstElement();
						CombinedTemplateCreationEntry entry = (CombinedTemplateCreationEntry) tEditP
								.getModel();
						IContentData cData = (IContentData) entry.getTemplate();

						String lbl = ViewMessages
								.bind(
										com.nokia.tools.screen.ui.views.ViewMessages.ResourceView_SkinBySubmenu,
										cData.getName());
						HideableMenuManager cpSubmenu = new HideableMenuManager(
								lbl);
						try {
							for (String name : ComponentStoreFactory
									.getComponentPool().getThemeNames()) {
								if (ComponentStoreFactory
										.getComponentPool()
										.isSkinned(cData.getId(),
												cData.getParent().getId(), name)) {
									action = new SkinBySimilarAction(
											selectionProvider, null, name);
									cpSubmenu.add(action);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (cpSubmenu.isEnabled()) {
							mmgr.add(new Separator());
							Action customize = new AdjustThemeListAction(selectionProvider);
							if( customize.isEnabled() ){
								cpSubmenu.add(new Separator());
								cpSubmenu.add(customize);
							}
							mmgr.add(cpSubmenu);
						}
					}
				}

			}
		};
	}

	public IMenuListener createResourcePositionViewerMenuListener(
			final ISelectionProvider selectionProvider, final CommandStack stack) {
		return new IMenuListener() {
			public void menuAboutToShow(IMenuManager mmgr) {

				AbstractAction action = new CopyContentDataAction(
						selectionProvider, null);
				action.update();
				// mmgr.add(action);

				HideableMenuManager cpCpy = new HideableMenuManager(action
						.getText());
				try {
					action.setText("Skinned Only");
					cpCpy.add(action);

					action = new CopyContentDataAction(selectionProvider, null);
					((CopyContentDataAction) action).setCopySkinnedOnly(false);
					action.update();
					action.setText("All");
					cpCpy.add(action);

				} catch (Exception e) {
					e.printStackTrace();
				}
				;

				// if (cpSubmenu.isEnabled()) {
				mmgr.add(new Separator());
				mmgr.add(cpCpy);
				// }

				action = new PasteContentDataAction(selectionProvider, stack,
						null);
				action.update();
				if( action.isEnabled() )
					mmgr.add(action);

				// // skin by similar actions
				// {
				// IStructuredSelection sel = (IStructuredSelection)
				// selectionProvider
				// .getSelection();
				// if (sel.getFirstElement() instanceof ToolEntryEditPart) {
				// ToolEntryEditPart tEditP = (ToolEntryEditPart) sel
				// .getFirstElement();
				// CombinedTemplateCreationEntry entry =
				// (CombinedTemplateCreationEntry) tEditP
				// .getModel();
				// IContentData cData = (IContentData) entry.getTemplate();
				//
				// String lbl = ViewMessages
				// .bind(
				// com.nokia.tools.screen.ui.views.ViewMessages.ResourceView_SkinBySubmenu,
				// cData.getName());
				// HideableMenuManager cpSubmenu = new HideableMenuManager(
				// lbl);
				// try {
				// for (String name : ComponentStoreFactory
				// .getComponentPool().getThemeNames()) {
				// if (ComponentStoreFactory
				// .getComponentPool()
				// .isSkinned(cData.getId(),
				// cData.getParent().getId(), name)) {
				// action = new SkinBySimilarAction(
				// selectionProvider, null, name);
				// cpSubmenu.add(action);
				// }
				// }
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				//
				// if (cpSubmenu.isEnabled()) {
				// mmgr.add(new Separator());
				// mmgr.add(cpSubmenu);
				// }
				// }
				// }

				// skin by similar actions
				{
					IStructuredSelection sel = (IStructuredSelection) selectionProvider
							.getSelection();
					if (sel.getFirstElement() instanceof ToolEntryEditPart) {
						ToolEntryEditPart tEditP = (ToolEntryEditPart) sel
								.getFirstElement();
						CombinedTemplateCreationEntry entry = (CombinedTemplateCreationEntry) tEditP
								.getModel();
						IContentData cData = (IContentData) entry.getTemplate();

						String lbl = ViewMessages
								.bind(
										com.nokia.tools.screen.ui.views.ViewMessages.ResourceView_SkinBySubmenu,
										cData.getName());
						HideableMenuManager cpSubmenu = new HideableMenuManager(
								lbl);
						try {
							for (String name : ComponentStoreFactory
									.getComponentPool().getThemeNames()) {
								if (ComponentStoreFactory
										.getComponentPool()
										.isSkinned(cData.getId(),
												cData.getParent().getId(), name)) {
									action = new SkinBySimilarAction(
											selectionProvider, null, name);
									cpSubmenu.add(action);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (cpSubmenu.isEnabled()) {
							mmgr.add(new Separator());
							mmgr.add(cpSubmenu);
						}
					}
				}
			}
		};
	}

	class ThemeResourceViewerSelectionHelper implements
			IResourceViewerSelectionHelper {
		public boolean supportsPositionViewer() {
			return true;
		}

		public void executeSelect(IContentData data) {
			// do nothing

		}

		public IContentData findTaskContaining(IContentData item) {
			IContent content = item.getRoot();
			if (ScreenUtil.isPrimaryContent(content)) {
				for (IContentData child : content.getChildren()) {
					if (child.getClass().getName().endsWith("ThemeTaskData")) {
						if (child.findById(item.getId()) != null)
							return child;
					}
				}
			}
			return null;
		}

		public IContentData findGroupContaining(IContentData item,
				IContentData task) {

			if (task != null) {
				IContent content = task.getRoot();
				if (ScreenUtil.isPrimaryContent(content)) {
					for (IContentData child : task.getChildren()) {
						if (child.getClass().getName().endsWith(
								"ThemeComponentGroupData")) {
							if (child.findById(item.getId()) != null)
								return child;
						}
					}
				}
			}
			return null;
		}

		public IContentData findMinorGroup(IContentData item,
				IContentData task, IContentData compGroup) {
			if (task == compGroup) {
				return task;
			}
			for (IContentData child : compGroup.getChildren()) {
				if (child.findById(item.getId()) != null)
					return child;
			}
			return null;
		}

	}

	public Rectangle getCategoryHighlightRect(IContentData cat) {
		return ThemeCategoryProvider.getCategoryHighlightRect(cat);
	}

	public Rectangle getResolution(IContentData data) {
		return ThemeCategoryProvider.getResolution(data);
	}

	/**
	 * boolean returns whether the section supports given type
	 */
	public boolean supportsContent(IContent content) {
		return ScreenUtil.isPrimaryContent(content);
	}
}
