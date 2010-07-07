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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.WorkbenchPart;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroupItem;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorGroupsStore;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.actions.ColorizeColorGroupAction;
import com.nokia.tools.s60.editor.actions.ColorizeSvgAction2;
import com.nokia.tools.s60.ide.ContributedActionsResolver;
import com.nokia.tools.s60.views.AdjustBrightnessComposite;
import com.nokia.tools.s60.views.ViewMessages;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.views.ColorResourceTableCategory;
import com.nokia.tools.screen.ui.views.IResourceSection2;
import com.nokia.tools.screen.ui.views.IResourceTableCategoryItem;
import com.nokia.tools.screen.ui.views.IResourceViewerSelectionHelper2;
import com.nokia.tools.screen.ui.views.ResourceColorBoxesLine;
import com.nokia.tools.screen.ui.views.ResourceTableCategory;
import com.nokia.tools.screen.ui.views.ResourceTableInput;
import com.nokia.tools.screen.ui.views.ResourceTableItem;
import com.nokia.tools.screen.ui.views.ResourceTableMasterGroup;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeUtil;
import com.nokia.tools.theme.ui.ThemeCategoryProvider;
import com.nokia.tools.ui.widgets.ImageLabel;

public class ThemeResourceViewerSection2 implements IResourceSection2 {

	private static final String XMLUI = "XMLUI";
	private static final String COLOURS = "COLOURS";

	private static final String sectionHeading = ViewMessages.ResourceView_Theme_Category;

	public List<ResourceTableInput> getPaletteDrawers(
			IContentAdapter contentAdapter) {
		List<ResourceTableInput> listToReturn = new ArrayList<ResourceTableInput>();
		ResourceTableInput input = null;
		if (contentAdapter != null) {
			// IContent themeContent =
			// ScreenUtil.getPrimaryContent(contentAdapter
			// .getContents());
			IContent[] themeContents = contentAdapter.getContents();
			IContent themeContent = null;
			for (IContent content : themeContents) {
				if (ScreenUtil.isPrimaryContent(content)
						&& !XMLUI.equals(content.getType())) {
					themeContent = content;
					break;
				}
			}

			if (themeContent != null) {
				IContentData[] elements = ThemeCategoryProvider
						.getRootLevelElements(themeContent);
				input = new ResourceTableInput();
				input.setType(themeContent.getType());
				// input.setName("===== System Theme =====");

				for (IContentData element : elements) {
					ResourceTableMasterGroup grp = new ResourceTableMasterGroup();
					grp.setEmphasizeCategories(false);
					grp.setMasterGroupName(element.getName());
					grp.setAssociatedContentData(element);
					input.addGroup(grp);

					if (((ThemeData) element).getData().isEntityType().equals(
							ThemeTag.ELEMENT_COLOUR)/* element.getName().equalsIgnoreCase(COLOURS) */) {
						grp.setEmphasizeCategories(true);
						List<RGB> colorList = new ArrayList<RGB>();
						List<RGB> colorLinkedList = new ArrayList<RGB>();
						List<RGB> colorNotLinkedList = new ArrayList<RGB>();
						for (IContentData child : ThemeUtil
								.getValidChildren(element)) {
							IContentData[] children = ThemeUtil
									.getValidChildren(child);
							if (children.length > 0) {
								for (IContentData subchild : ThemeUtil
										.getValidChildren(children[0])) {
									RGB colorRGB = null;
									IColorAdapter colorAdapter = (IColorAdapter) subchild
											.getAdapter(IColorAdapter.class);

									if (colorAdapter != null) {
										colorRGB = ColorUtil.toRGB(colorAdapter
												.getColor());

									}
									if (!colorList.contains(colorRGB)) {
										colorList.add(colorRGB);
									}
									// handle logical colors
									if (!colorNotLinkedList.contains(colorRGB)) {

										if (colorGroupsContainElementWithRGB(
												subchild.getId(), colorRGB,
												themeContent)) {
											if (!colorLinkedList
													.contains(colorRGB)) {
												colorLinkedList.add(colorRGB);
											}

										} else {
											colorNotLinkedList.add(colorRGB);
											if (colorLinkedList
													.contains(colorRGB)) {
												colorLinkedList
														.remove(colorRGB);
											}
										}

									} else {
										continue;
									}

								}
							}
						}

						Object[] colorsArray = colorList.toArray();
						for (int i = 0; i <= colorsArray.length / 14
								&& !((colorsArray.length % 14 == 0) && i == colorsArray.length / 14); i++) {
							IResourceTableCategoryItem cat = new ResourceColorBoxesLine(
									grp);
							cat.setName("");
							((ResourceColorBoxesLine) cat)
									.setLinkedRGBList(colorLinkedList);
							for (int j = 0; j < 14
									&& colorsArray.length > i * 14 + j; j++) {
								RGB rgb = (RGB) colorsArray[i * 14 + j];
								((ResourceColorBoxesLine) cat).addRGB(rgb);
							}
							grp.addResourceTableCagetory(cat);
						}

					}
					for (IContentData child : ThemeUtil
							.getValidChildren(element)) {
						boolean isColorCategory = false;
						boolean isColorCategoryWithAllElementsLinked = false;
						boolean wasCheckedForLinkage = false;

						RGB colorRGB = null;
						if (grp.getMasterGroupName().equalsIgnoreCase(COLOURS)) {
							isColorCategory = true;
							IContentData[] children = ThemeUtil
									.getValidChildren(child);
							if (children.length > 0) {
								for (IContentData subchild : ThemeUtil
										.getValidChildren(children[0])) {
									IColorAdapter colorAdapter = (IColorAdapter) subchild
											.getAdapter(IColorAdapter.class);

									if (colorAdapter != null) {
										if (colorRGB == null) {
											colorRGB = ColorUtil
													.toRGB(colorAdapter
															.getColor());

											if (colorGroupsContainElementWithRGB(
													subchild.getId(), colorRGB,
													themeContent)) {
												isColorCategoryWithAllElementsLinked = true;
											}
											wasCheckedForLinkage = true;
										} else if (!colorRGB
												.equals(ColorUtil
														.toRGB(colorAdapter
																.getColor()))) {
											wasCheckedForLinkage = true;
											colorRGB = null;
											isColorCategoryWithAllElementsLinked = false;
											break;
										} else {
											if (wasCheckedForLinkage
													&& isColorCategoryWithAllElementsLinked) {
												if (!colorGroupsContainElementWithRGB(
														subchild.getId(),
														colorRGB, themeContent)) {
													isColorCategoryWithAllElementsLinked = false;
												}
											}
										}

									}
								}
							}
						}
						IResourceTableCategoryItem cat = null;
						if (isColorCategory) {

							cat = new ColorResourceTableCategory(grp, colorRGB);
							IContentData[] children = ThemeUtil
									.getValidChildren(child);
							if (children.length > 0) {

								((ColorResourceTableCategory) cat)
										.setAssociatedContentData(children[0]);

							}
							if (isColorCategoryWithAllElementsLinked) {
								((ColorResourceTableCategory) cat)
										.setLinked(true);
							}
							boolean isModifiedAll = true;
							boolean isModifiedSomething = false;
							if (children.length > 0) {
								for (IContentData subchild : ThemeUtil
										.getValidChildren(children[0])) {
									String booleanValue = (String) subchild
											.getAttribute(ContentAttribute.MODIFIED
													.name());
									if (new Boolean(booleanValue)) {
										isModifiedSomething = true;
									} else {
										isModifiedAll = false;
									}

								}
							}
							if (isModifiedAll) {
								((ColorResourceTableCategory) cat)
										.setSkinned(true);
							} else if (isModifiedSomething) {
								((ColorResourceTableCategory) cat)
										.setHalfSkinned(true);
							}

						} else {
							cat = new ResourceTableCategory(grp);
							((ResourceTableCategory) cat)
									.setAssociatedContentData(child);
						}
						cat.setName(child.getName());
						grp.addResourceTableCagetory(cat);

						if (!(cat instanceof ColorResourceTableCategory)) {
							// resolve is skinned for non color categories
							boolean isInCategoryModifiedAll = true;
							boolean isInCategoryModifiedSomething = false;

							for (IContentData subchild : ThemeUtil
									.getValidChildren(child)) {

								ResourceTableItem item = new ResourceTableItem(
										(ResourceTableCategory) cat);
								item.setName(subchild.getName());
								item.setAssociatedContentData(subchild);

								boolean isModifiedAll = true;
								boolean isModifiedSomething = false;
								for (IContentData data : ThemeUtil
										.getValidChildren(subchild)) {
									String booleanValue = (String) data
											.getAttribute(ContentAttribute.MODIFIED
													.name());
									if (new Boolean(booleanValue)) {
										isModifiedSomething = true;
										isInCategoryModifiedSomething = true;
									} else {
										isModifiedAll = false;
									}

								}

								if (isModifiedAll) {
									item.setSkinned(true);
								} else if (isModifiedSomething) {
									item.setHalfSkinned(true);
									isInCategoryModifiedAll = false;
								} else {
									isInCategoryModifiedAll = false;
								}

								((ResourceTableCategory) cat).addItem(item);

							}

							if (isInCategoryModifiedAll) {
								((ResourceTableCategory) cat).setSkinned(true);
							} else if (isInCategoryModifiedSomething) {
								((ResourceTableCategory) cat)
										.setHalfSkinned(true);
							}
						}

					}
				}
			}
		}
		if (input != null) {
			listToReturn.add(input);
		}
		return listToReturn;
	}

	public List<ImageLabel> getNavigationControlItems(Composite parent,
			IContentAdapter adapter) {
		List<ImageLabel> ials = new ArrayList<ImageLabel>();
		if (adapter != null) {

			IContent[] themeContents = adapter.getContents();
			IContent themeContent = null;
			for (IContent content : themeContents) {
				if (ScreenUtil.isPrimaryContent(content)
						&& !XMLUI.equals(content.getType())) {
					themeContent = content;
					break;
				}
			}
			if (themeContent != null) {
				Composite thumbComposite = new Composite(parent, SWT.NONE);
				GridLayout gl = new GridLayout();
				thumbComposite.setLayout(gl);
				gl.marginHeight = 1;
				gl.marginWidth = 1;
				thumbComposite.setBackground(ColorConstants.listBackground);

				ImageLabel ial = new ImageLabel(thumbComposite, SWT.NONE);
				ial.setUnselectedBackground(ColorConstants.listBackground);
				ial.setFillBackground(true);
				ial.setSelectedTextColor(ColorConstants.white);
				ial.setUnselectedTextColor(ColorConstants.menuForeground);
				ImageDescriptor descriptor = null;

				if (themeContent.getType().equals("S60THEME")) {
					descriptor = UiPlugin
							.getImageDescriptor("/icons/resview/theme.png");
					ial.setText("Theme");
				} else {
					descriptor = UiPlugin
							.getImageDescriptor("/icons/resview/battery.ico");
				}
				ial.setImageDescriptor(descriptor);
				ial.setData(themeContent.getType());
				ials.add(ial);
			}
		}

		return ials;
	}

	private IEditorPart getEditorPart() {
		return EclipseUtils.getActiveSafeEditor();
	}

	private boolean colorGroupsContainElementWithRGB(String elementId,
			RGB colorsRGB, IContent themeContent) {
		IEditorPart activeEd = getEditorPart();
		if (activeEd instanceof Series60EditorPart) {
			IFile original = ((FileEditorInput) activeEd.getEditorInput())
					.getFile();
			ColorGroups grps = ColorGroupsStore
					.getColorGroupsForProject(original.getProject());
			if (grps != null && grps.getGroupByRGB(colorsRGB) != null) {
				ColorGroup colorGrp = grps.getGroupByRGB(colorsRGB);
				if (colorGrp.containsItemWithIdAndLayerName(elementId, null)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public String getSectionHeading() {

		return sectionHeading;
	}

	public IResourceViewerSelectionHelper2 getSelectionHelper(IContentData data) {
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

				WorkbenchPart activePart = (WorkbenchPart) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().getActivePart();
				ContributedActionsResolver.getInstance().contributeActions(
						mmgr, "resource", activePart);

			}
		};
	}

	class ThemeResourceViewerSelectionHelper implements
			IResourceViewerSelectionHelper2 {
		public boolean supportsPositionViewer() {
			return true;
		}

		private String CHANGE_COLOR = "CHANGE_COLOR";

		public void executeSelect(IEditorPart editor, Object data, String type,
				Object argument) {
			if (data instanceof ColorResourceTableCategory
					&& type == CHANGE_COLOR && argument != null
					&& argument instanceof RGB) {
				ColorResourceTableCategory cat = (ColorResourceTableCategory) data;
				RGB newRGB = (RGB) argument;
				RGB oldRGB = cat.getRGB();
				if (!cat.isLinked()) {

					cat.setRGB(newRGB);
					IContentData[] dataArray = ThemeUtil.getValidChildren(cat
							.getAssociatedContentData());

					final List<IContentData> datas = new ArrayList<IContentData>();

					for (int i = 0; i < dataArray.length; i++) {
						datas.add(dataArray[i]);
					}
					colorize(editor, datas, oldRGB, newRGB);
				} else {
					ColorGroups grps = getColorGroups(editor);

					colorizeGroup(editor, newRGB, grps.getGroupByRGB(oldRGB));
				}
			} else if (data instanceof ResourceColorBoxesLine
					&& argument != null && argument instanceof RGB) {
				ResourceColorBoxesLine line = (ResourceColorBoxesLine) data;
				RGB rgb = line.getSelectedRGB();
				RGB newRGB = (RGB) argument;
				if (!line.getLinkedRGBList().contains(rgb)) {
					line.getRGBList().set(line.getRGBList().indexOf(rgb),
							newRGB);
					ResourceTableMasterGroup grp = line.getParent();
					final List<IContentData> datas = new ArrayList<IContentData>();

					for (IContentData child : ThemeUtil.getValidChildren(grp
							.getAssociatedContentData())) {
						for (IContentData subchild : ThemeUtil
								.getValidChildren(ThemeUtil
										.getValidChildren(child)[0])) {
							IColorAdapter colorAdapter = (IColorAdapter) subchild
									.getAdapter(IColorAdapter.class);

							if (colorAdapter != null) {
								RGB colorRGB = ColorUtil.toRGB(colorAdapter
										.getColor());
								if (colorRGB.equals(rgb)) {
									datas.add(subchild);
								}

							}

						}
					}
					colorize(editor, datas, rgb, newRGB);
				} else {
					ColorGroups grps = getColorGroups(editor);

					ColorGroup grp = grps.getGroupByRGB(rgb);
					colorizeGroup(editor, newRGB, grp);
					if (null == grp)
						return;

					// colorize also the children
				}

			}
		}

		private List<IContentData> findContentDataForGroup(IEditorPart editor,
				ColorGroup group) {
			List<IContentData> datas = new ArrayList<IContentData>();

			if (editor instanceof Series60EditorPart) {
				IContent[] cnt = ((Series60EditorPart) editor).getContents();
				IContent root = ScreenUtil.getPrimaryContent(cnt);
				for (ColorGroupItem item : group.getGroupItems()) {
					IContentData data = root.findById(item.getItemId());
					if (data == null) {
						data = root.findByName(item.getItemId());
					}
					datas.add(data);
				}
			}

			return datas;
		}

		private void colorizeGroup(IEditorPart editor, RGB newColor,
				ColorGroup grp) {

			ColorGroup referencedGroup = grp;
			RGB referencedColor = referencedGroup.getGroupColor();
			RGB changingRGB = newColor;
			final List<IContentData> items = findContentDataForGroup(editor,
					referencedGroup);

			// final List<ColorGroupItem> items= ((ColorGroup)
			// data[2]).getGroupItems();
			ISelectionProvider sp = new ISelectionProvider() {
				public ISelection getSelection() {
					return new StructuredSelection(items);
				}

				public void addSelectionChangedListener(
						ISelectionChangedListener listener) {
				}

				public void removeSelectionChangedListener(
						ISelectionChangedListener listener) {
				}

				public void setSelection(ISelection selection) {
				}
			};

			ColorGroups grps = getColorGroups(editor);
			ColorizeColorGroupAction colorGroupAction = new ColorizeColorGroupAction(
					sp, null, null);
			colorGroupAction.setColorGroups(grps);
			colorGroupAction.setColor(changingRGB, referencedColor, 255);
			// if (colorGroupAction.isEnabled()) {
			colorGroupAction.setColorGroup(referencedGroup);
			colorGroupAction.run();
			// }

			for (ColorGroup group : grps.getGroups()) {
				if (referencedGroup.getName()
						.equals(group.getParentGroupName())) {

					RGB grpRGB = group.getGroupColor();
					float brightness = AdjustBrightnessComposite
							.getBrightnessRatio(referencedColor, grpRGB);
					RGB newRGBWithGivenHue = AdjustBrightnessComposite
							.getBrighterColor(changingRGB, brightness);
					colorizeGroup(editor, newRGBWithGivenHue, group);
				}
			}
		}

		private ColorGroups getColorGroups(IEditorPart sourceEditor) {
			if (sourceEditor != null && ColorGroupsStore.isEnabled) {
				IFile original = ((FileEditorInput) sourceEditor
						.getEditorInput()).getFile();
				return ColorGroupsStore.getColorGroupsForProject(original
						.getProject());
			} else {
				return null;
			}
		}

		private void colorize(IEditorPart editor,
				final List<IContentData> datas, RGB oldRGB, RGB newRGB) {

			ColorizeSvgAction2 action;

			for (IContentData contentData : datas) {
				contentData.getAdapter(EditObject.class);
			}

			ISelectionProvider sp = new ISelectionProvider() {
				public ISelection getSelection() {
					return new StructuredSelection(datas);
				}

				public void addSelectionChangedListener(
						ISelectionChangedListener listener) {
				}

				public void removeSelectionChangedListener(
						ISelectionChangedListener listener) {
				}

				public void setSelection(ISelection selection) {
				}
			};

			action = new ColorizeSvgAction2(sp, getCommandStack(editor));

			action.setColor(newRGB, oldRGB, 255);
			if (action.isEnabled()) {
				action.run();
			}
		}

		private CommandStack getCommandStack(IEditorPart editor) {
			return (CommandStack) editor.getAdapter(CommandStack.class);
		}

		public ResourceTableCategory getCategoryToPin(
				List<ResourceTableInput> inputs, IContentData dataToPin) {
			ResourceTableInput input = null;
			if (!ThemeResourceViewerSection2.this.supportsContent(dataToPin
					.getRoot())) {
				return null;
			}

			for (ResourceTableInput usedInput : inputs) {
				if (usedInput.getType().equals(dataToPin.getRoot().getType())) {
					input = usedInput;
					break;
				}
			}

			if (dataToPin != null) {

				IContentData minorGroup = dataToPin.getParent();
				IContentData compGroup = minorGroup.getParent();

				// selects appropriate elements
				if (compGroup != null && minorGroup != null && input != null) {

					for (ResourceTableMasterGroup group : input.getGroups()) {
						for (IResourceTableCategoryItem cat : group
								.getCategories()) {
							if (cat instanceof ColorResourceTableCategory) {
								IContentData catData = ((ColorResourceTableCategory) cat)
										.getAssociatedContentData();
								if (catData != null) {
									if (catData.hasChildren()) {
										for (IContentData subChild : ThemeUtil
												.getValidChildren(catData)) {
											if (subChild == dataToPin) {
												return (ResourceTableCategory) cat;
											}

										}
									}
								}
							} else if (cat instanceof ResourceTableCategory) {
								for (ResourceTableItem item : ((ResourceTableCategory) cat)
										.getItems()) {
									IContentData itemData = item
											.getAssociatedContentData();
									if (itemData.hasChildren()) {
										for (IContentData subChild : ThemeUtil
												.getValidChildren(itemData)) {
											if (subChild == dataToPin) {
												return (ResourceTableCategory) cat;

											}
										}
									}
								}
							}

						}
					}
				}
			}
			return null;
		}

	}

	/**
	 * boolean returns whether the section supports given type
	 */
	public boolean supportsContent(IContent content) {
		return (ScreenUtil.isPrimaryContent(content)
				&& !XMLUI
				.equals(content.getType()));
	}

	public List<ResourceTableMasterGroup> filterInput(
			List<ResourceTableInput> tempInputs, ImageLabel imageAboveLabel) {
		return new ArrayList<ResourceTableMasterGroup>();
	}
}
