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
package com.nokia.tools.s60.editor.ui.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroupItem;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorGroupsStore;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.editor.actions.AddToGroupAction;
import com.nokia.tools.s60.editor.actions.ColorizeColorGroupAction;
import com.nokia.tools.s60.editor.actions.ColorizeSvgAction2;
import com.nokia.tools.s60.editor.actions.RemoveFromGroupAction;
import com.nokia.tools.s60.views.AdjustBrightnessComposite;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.color.ColorChangedLabelWrapper;
import com.nokia.tools.screen.ui.propertysheet.color.ColorPickerComposite;
import com.nokia.tools.screen.ui.propertysheet.color.IColorPickerListener;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;
import com.nokia.tools.ui.tooltip.CompositeInformationControl;
import com.nokia.tools.ui.tooltip.CompositeTooltip;

public class ColorizingHelper implements Observer {

	Series60EditorPart owningEditor;
	CommandStack commandStack;
	TreeViewer viewer;

	public ColorizingHelper(Series60EditorPart owningEditor,
			CommandStack commandStack, TreeViewer viewer) {
		this.owningEditor = owningEditor;
		this.commandStack = commandStack;
		this.viewer = viewer;
	}

	CompositeTooltip createTooltip() {
		final Tree tree = viewer.getTree();
		CompositeTooltip tooltip = new CompositeTooltip() {
			TreeItem item;

			@Override
			public void initControl(
					CompositeInformationControl informationControl) {
				super.initControl(informationControl);

				if (item != null && !item.isDisposed()) {
					Rectangle rect = item.getBounds(0);
					Point size = new Point(
							informationControl.getBounds().width,
							informationControl.getBounds().height);
					Point pt = tree.toDisplay(rect.x - size.x + 10, rect.y + 5); // (-6*size.x/7,0);//
					if (pt.y + size.y > tree.getDisplay().getBounds().height) {
						pt.y = tree.getDisplay().getBounds().height - size.y;
					}
					informationControl.setLocation(pt);
				}
			}

			@Override
			protected Rectangle getControlBounds() {
				if (item != null && !item.isDisposed()) {
					Point treeLoc = tree.getLocation();
					Rectangle itemBounds = item.getBounds(0);
					return new Rectangle(treeLoc.x + itemBounds.x, treeLoc.y
							+ itemBounds.y, itemBounds.width, itemBounds.height);
				} else {
					return super.getControlBounds();
				}
			}

			@Override
			public void hide() {
				super.hide();
				item = null;
			}

			@Override
			protected void mouseExit(MouseEvent e) {
				super.mouseExit(e, 250);
			}

			@Override
			protected void mouseHover(MouseEvent e) {
				if (e.widget == tree) {
					TreeItem newItem = tree.getItem(new Point(e.x, e.y));

					if (newItem != item) {
						hide();
					}

					item = newItem;

					if (item != null) {
						if (item.getData() instanceof Object[]) {
							Object[] data = (Object[]) item.getData();
							if (data[3] == AbstractAction.TYPE_COLOR
									|| data[3] == AbstractAction.TYPE_COLOR_GROUP) {
								super.mouseHover(e);
								return;
							}
						}
					}

					hide();
				}
			}

			@Override
			protected CompositeInformationControl createFocusedControl() {
				return createUnfocusedControl();
			}

			@Override
			protected CompositeInformationControl createUnfocusedControl() {
				final TreeItem currItem = item;

				RGB inputRgb = (RGB) ((Object[]) currItem.getData())[4];

				CompositeInformationControl cic = super
						.createUnfocusedControl();

				final Composite root = cic.getComposite();
				GridLayout gl = new GridLayout(1, false);
				gl.marginWidth = gl.marginHeight = 0;
				root.setLayout(gl);

				final ColorChangedLabelWrapper colorChangedLabelWrapper = new ColorChangedLabelWrapper();
				colorChangedLabelWrapper.setColorString(ColorUtil
						.asHashString(inputRgb));

				IColorPickerListener colorPickerListener = new IColorPickerListener() {
					public void selectionChanged() {
						okCloseDialog();
					}

					public void okCloseDialog() {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								hide(true);
							}
						});

						RGB newColor = colorChangedLabelWrapper
								.getColorDescriptor().getRGB();
						if (!currItem.isDisposed()) {
							viewer.getTree().select(currItem);
							handleColorChange(newColor, currItem.getData());
						}
					}
				};
				Object dataItem = currItem.getData();
				Object[] data = (Object[]) dataItem;
				String name = (String) data[0];
				if (name.length() > 5
						&& "tone".equals(name.substring(name.length() - 5, name
								.length() - 1))) {

					ColorGroup group = getColorGroups().getGroupByName(name);
					if (null != group) {
						colorChangedLabelWrapper
								.setParentColorGroup(getColorGroups()
										.getGroupByName(
												group.getParentGroupName()));
					}
					new AdjustBrightnessComposite(root, SWT.NONE,
							colorChangedLabelWrapper, colorPickerListener);
				} else {
					new ColorPickerComposite(root, SWT.NONE,
							colorChangedLabelWrapper, colorPickerListener);
				}

				return cic;
			}
		};

		tooltip.setControl(tree);

		return tooltip;
	}

	void handleColorChange(RGB newColor, Object item) {
		Object[] data = (Object[]) item;
		RGB rgb = (RGB) data[4];

		ColorizeSvgAction2 action;
		ColorizeColorGroupAction colorGroupAction;
		boolean isNinePiece = false;
		boolean isMultiLayer = false;
		IContentData contentData;
		if (data[2] instanceof IContentData) {
			contentData = (IContentData) data[2];
		} else {
			contentData = findContentData(data[2]);
		}
		if (contentData != null) {
			ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) contentData
					.getAdapter(ISkinnableEntityAdapter.class);
			//if (adapter.isNinePiece() || adapter.isElevenPiece() || adapter.isThreePiece()) {
			if (adapter.isMultiPiece()) {
				isNinePiece = true;
			}
		}
		if (data[1] != null && (data[1] instanceof ILayer)
				&& (((ILayer) data[1]).getParent().getLayerCount() > 1)) {
			isMultiLayer = true;
		}

		if (data[3] == AbstractAction.TYPE_COLOR_GROUP) {

			String groupName = (String) data[0];
			ColorGroups grps = getColorGroups();
			ColorGroup grp = grps.getGroupByName(groupName);
			final List<IContentData> items = findContentDataForGroup(grp);

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
			colorGroupAction = new ColorizeColorGroupAction(sp,
					getCommandStack(), viewer);
			colorGroupAction.setColorGroups(grps);
			colorGroupAction.setColor(newColor, rgb, 255);
			if (colorGroupAction.isEnabled()) {
				colorGroupAction.setColorGroup(grp);
				colorGroupAction.run();
			}
		
			for (ColorGroup group : grps.getGroups()) {
				if (grp.getName().equals(group.getParentGroupName())) {

					RGB grpRGB = group.getGroupColor();
					float brightness = AdjustBrightnessComposite
							.getBrightnessRatio(rgb, grpRGB);
					RGB newRGBWithGivenHue = AdjustBrightnessComposite
							.getBrighterColor(newColor, brightness);
					colorizeGroup(newRGBWithGivenHue, group);
				}
			}

			return;
		}

		if (data[2] instanceof List) { // multiselection

			final List<IContentData> datas = (List<IContentData>) data[2];

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

			action = new ColorizeSvgAction2(sp, getCommandStack());

			action.setColor(newColor, rgb, 255);
			if (action.isEnabled()) {
				action.run();
			}
		} else {
			if (isNinePiece || isMultiLayer) {
				action = new ColorizeSvgAction2(new SimpleSelectionProvider(
						data[2]), getCommandStack(), (ILayer) data[1]);
			} else {
				action = new ColorizeSvgAction2(new SimpleSelectionProvider(
						data[2]), getCommandStack());
			}
			action.setColor(newColor, rgb, 255);
			if (action.isEnabled()) {
				action.run();
			}
		}
	}

	private CommandStack getCommandStack() {
		return commandStack;
	}

	IContentData findContentData(Object element) {
		return JEMUtil.getContentData(element);
	}

	ColorGroups getColorGroups() {
		if (owningEditor != null && ColorGroupsStore.isEnabled) {
			ColorGroups grps = ColorGroupsStore
					.getColorGroupsForProject(owningEditor.getProject());
			if (grps != null)
				grps.addObserver(this);
			return grps;
		} else {
			return null;
		}
	}

	List<IContentData> findContentDataForGroup(ColorGroup group) {
		List<IContentData> datas = new ArrayList<IContentData>();

		if (group != null) {
			IContent[] cnt = owningEditor.getContents();
			IContent root = ScreenUtil.getPrimaryContent(cnt);
			for (ColorGroupItem item : group.getGroupItems()) {
				IContentData data = root.findById(item.getItemId());
				datas.add(data);
			}
		}

		return datas;
	}

	void colorizeGroup(RGB newColor, ColorGroup grp) {
		if (grp != null) {
			ColorGroup referencedGroup = grp;

			RGB referencedColor = referencedGroup.getGroupColor();
			RGB changingRGB = newColor;
			final List<IContentData> items = findContentDataForGroup(referencedGroup);

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

			ColorizeColorGroupAction colorGroupAction = new ColorizeColorGroupAction(
					sp, null, null);
			colorGroupAction.setColorGroups(getColorGroups());
			colorGroupAction.setColor(changingRGB, referencedColor, 255);
			// if (colorGroupAction.isEnabled()) {
			colorGroupAction.setColorGroup(referencedGroup);
			colorGroupAction.run();
			// }
		}
	}

	/*
	 * This method is called when color is added to the group
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable arg0, Object arg1) {

		if (ColorGroups.GROUP_ADDED.equals(arg1)
				|| ColorGroups.GROUP_REMOVED.equals(arg1)
				|| ColorGroups.ITEM_ADDED.equals(arg1)
				|| ColorGroups.ITEM_REMOVED.equals(arg1)
				|| ColorGroups.NAME_CHANGED.equals(arg1)) {
			if (!viewer.getTree().isDisposed()) {
				viewer.refresh();
				viewer.expandAll();
			}
		}
	}

	public void dispose() {
		ColorGroups grps = getColorGroups();
		if (grps != null) {
			grps.deleteObserver(this);
		}
	}

	public boolean isColor(String id) {
		IContent[] cnt = owningEditor.getContents();
		IContent root = ScreenUtil.getPrimaryContent(cnt);
		IContentData data = root.findById(id);
		if (data == null) {
			return false;
		}
		if (((ISkinnableEntityAdapter) data
				.getAdapter(ISkinnableEntityAdapter.class)).isColour()) {
			return true;
		} else {
			return false;
		}
	}

	void fillContextMenu(IMenuManager manager) {

		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.getFirstElement() instanceof Object[]) {

			Object array[] = (Object[]) selection.getFirstElement();
			boolean isColor = AbstractAction.TYPE_COLOR.equals(array[3]);
			boolean isColorGroup = AbstractAction.TYPE_COLOR_GROUP
					.equals(array[3]);
			if (isColor || isColorGroup) {
				if (ColorGroupsStore.isEnabled) {
					Object defaultAction = null;
					if (isColor) {
						if (array[4] != null && array[4] instanceof RGB) {
							RGB color = (RGB) array[4];
							ColorGroups grps = getColorGroups();
							if (grps != null) {
								ColorGroup grp = grps.getGroupByRGB(color);

								final Object[] obj = (Object[]) ((IStructuredSelection) viewer
										.getSelection()).getFirstElement();
								ISelectionProvider provider = null;
								if (obj[2] instanceof List) {
									final List<IContentData> datas = (List<IContentData>) obj[2];
									provider = new ISelectionProvider() {
										public ISelection getSelection() {
											return new StructuredSelection(
													datas);
										}

										public void addSelectionChangedListener(
												ISelectionChangedListener listener) {
										}

										public void removeSelectionChangedListener(
												ISelectionChangedListener listener) {
										}

										public void setSelection(
												ISelection selection) {
										}
									};
								} else {
									provider = viewer;
								}

								AddToGroupAction action = new AddToGroupAction(
										(StructuredSelection) viewer
												.getSelection(), provider,
										getCommandStack());
								action.setMenuCreator(null);
								action.setSelectionProvider(provider);
								action.setColor(color);
								action.setGroupName(grp == null ? null : grp
										.getName());
								action.setColorGroups(grps);
								action
										.setText(Messages.Colors_Reference2Color
												+ (grp == null ? " - "
														+ Messages.Colors_CreateNewLogicalColor
														: " '" + grp.getName()
																+ "'"));
								manager.add(action);
								manager.add(new Separator());

								defaultAction = action;

								if (grp != null) {
									action = new AddToGroupAction(
											(StructuredSelection) viewer
													.getSelection(), provider,
											getCommandStack());
									action.setMenuCreator(null);
									action.setColor(color);
									action.setGroupName(null);
									action.setColorGroups(grps);
									action
											.setText(Messages.Colors_Reference2Color
													+ " - "
													+ Messages.Colors_CreateNewLogicalColor);
									manager.add(action);
									manager.add(new Separator());
								}

								for (ColorGroup group : grps.getGroups()) {
									if (group != grp) {
										action = new AddToGroupAction(
												(StructuredSelection) viewer
														.getSelection(),
												provider, getCommandStack());
										action.setMenuCreator(null);
										action.setSelectionProvider(provider);
										action.setColor(color);
										action
												.setGroupName(group == null ? null
														: group.getName());
										action.setColorGroups(grps);
										action
												.setText(Messages.Colors_Reference2Color
														+ " '"
														+ group.getName() + "'");
										manager.add(action);
									}
								}
							}
						}
					} else if (isColorGroup) {
						
						String groupName = (String) array[0];
						ColorGroups grps = getColorGroups();
						ColorGroup grp = grps.getGroupByName(groupName);

						if ((grp != null)) {
							final Object[] obj = (Object[]) ((IStructuredSelection) viewer
									.getSelection()).getFirstElement();
							ISelectionProvider provider = null;
							if (obj[2] instanceof List) {
								final List<IContentData> datas = (List<IContentData>) obj[2];
								provider = new ISelectionProvider() {
									public ISelection getSelection() {
										return new StructuredSelection(datas);
									}

									public void addSelectionChangedListener(
											ISelectionChangedListener listener) {
									}

									public void removeSelectionChangedListener(
											ISelectionChangedListener listener) {
									}

									public void setSelection(
											ISelection selection) {
									}
								};
							} else {
								provider = viewer;
							}
							
							for (ColorGroup group : grps.getGroups()) {
								if (group != grp) {
									AddToGroupAction action = new AddToGroupAction(
											(StructuredSelection) viewer
													.getSelection(),
											provider, getCommandStack());
									action.setMenuCreator(null);
									action.setSelectionProvider(provider);
									action.setColor(grp.getGroupColor());
									action
											.setGroupName(group == null ? null
													: group.getName());
									action.setColorGroups(grps);
									action
											.setText(Messages.Colors_Reference2Color
													+ " '"
													+ group.getName() + "'");
									manager.add(action);
								}
							}

							RemoveFromGroupAction action2 = new RemoveFromGroupAction(
									viewer, provider, getCommandStack());
							action2.setSelectionProvider(provider);
							action2.setColorGroups(grps);
							action2.setColorGroup(grp);
							action2.setText(Messages.Colors_UnReferenceColor
									+ (grp == null ? "" : " '" + grp.getName()
											+ "'"));
							manager.add(new Separator());
							manager.add(action2);
							manager.add(new Separator());

						}
					}
					manager.update(true);
					MenuItem[] items = ((MenuManager) manager).getMenu()
							.getItems();
					for (MenuItem item : items) {
						if (item.getData() instanceof ActionContributionItem) {
							IAction action = ((ActionContributionItem) item
									.getData()).getAction();
							if (action == defaultAction) {
								((MenuManager) manager).getMenu()
										.setDefaultItem(item);
								break;
							}
						}
					}
				}
			}
		}
	}
}
