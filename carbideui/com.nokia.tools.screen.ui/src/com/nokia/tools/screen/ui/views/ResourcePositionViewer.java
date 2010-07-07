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
package com.nokia.tools.screen.ui.views;

import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.draw2d.ScrollBar;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.ParagraphTextLayout;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

/**
 * This viewer displays the element's position on the screen.
 * 
 */
public class ResourcePositionViewer extends EarlyDisposablePaletteViewer {
	private static final int COLOR = SWT.COLOR_DARK_GRAY;
	private String selectedId = null;
	private Clickable lastSelected = null;
	private int lastScrollY = 0;
	private int tempScrollY = 0;

	private IContentData data;

	private int width = 55;

	public void clearPositionData() {
		selectedId = null;
		lastSelected = null;
		lastScrollY = 0;
		tempScrollY = 0;
	}

	public ResourcePositionViewer(final ResourcePage page) {
		addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				ISelection selection = e.getSelection();
				if (selection instanceof IStructuredSelection
						&& ((IStructuredSelection) selection).getFirstElement() instanceof EditPart) {
					EditPart selectedPart = (EditPart) ((IStructuredSelection) selection)
							.getFirstElement();
					if (selectedPart.getModel() instanceof ToolEntry) {
						Map visualPartMap = getVisualPartMap();
						for (Object obj : visualPartMap.keySet()) {
							IFigure fig = (IFigure) obj;
							EditPart part = (EditPart) visualPartMap.get(fig);
							if (part.getModel() instanceof ToolEntry) {
								// to persist the selection after Copy/Paste and
								// similar actions
								if (lastSelected != null) {
									lastSelected.getModel().setArmed(false);
									lastSelected.getModel().setPressed(false);

									IFigure labelFigure = (IFigure) ((IFigure) lastSelected)
											.getChildren().get(0);
									labelFigure.setBorder(null);
								}
								Clickable button = (Clickable) fig;
								IFigure image = (IFigure) ((IFigure) button
										.getChildren().get(0)).getChildren()
										.get(0);
								button.getModel()
										.setArmed(part == selectedPart);
								button.getModel().setPressed(
										part == selectedPart);

								if (part == selectedPart) {
									// for selection persistence
									IFigure tooltip = ((IFigure) (button
											.getChildren().get(0)))
											.getToolTip();
									if (tooltip != null)
										selectedId = ((Label) tooltip)
												.getText();
									// for scroll persistence
									if (button.getParent().getParent()
											.getParent() instanceof ScrollPane) {
										ScrollPane scrollPane = (ScrollPane) button
												.getParent().getParent()
												.getParent();
										ScrollBar scrollBar = scrollPane
												.getVerticalScrollBar();
										lastScrollY = scrollBar.getValue();
										tempScrollY = lastScrollY;
									}
								}

							}
						}
					}
				}
			}
		});

	}

	public void addMenuListeners(List<IMenuListener> listeners) {
		MenuManager mmgr = new MenuManager();
		mmgr.setRemoveAllWhenShown(true);

		for (IMenuListener listener : listeners) {
			if (listener != null) {
				mmgr.addMenuListener(listener);
			}
		}
		setContextMenu(mmgr);
	}

	/**
	 * @param data The data to set.
	 */
	public synchronized void setData(IContent content, IContentData data) {
		if (data == null || data instanceof IContent) {
			setContents(null);
			this.data = null;
			return;
		}
		this.data = data;
		setPaletteRoot(createPaletteRoot(content));
		updateFigures(content);
	}

	/**
	 * @return Returns the data.
	 */
	public IContentData getData() {
		return data;
	}

	/**
	 * @return Returns the width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width The width to set.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	protected void updateFigures(IContent content) {
		Map visualPartMap = getVisualPartMap();
		for (Object obj : visualPartMap.keySet()) {
			IFigure fig = (IFigure) obj;
			EditPart part = (EditPart) visualPartMap.get(fig);
			if (part.getModel() instanceof PaletteDrawer) {
				List children = fig.getChildren();

				ToolbarLayout drawerLayout = (ToolbarLayout) fig
						.getLayoutManager();
				drawerLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);

				IFigure f = (IFigure) children.get(0);
				if (f instanceof Clickable) {
					((Clickable) f).setEnabled(false);
					f.setBorder(null);
					// removes the default figures (icon + label + pin)
					f.remove((IFigure) f.getChildren().get(0));
					FlowPage page = new FlowPage();
					TextFlow text = new TextFlow(
							content == null ? ViewMessages.ResourceView_NoTheme
									: data.getName());
					ParagraphTextLayout l = new ParagraphTextLayout(text,
							ParagraphTextLayout.WORD_WRAP_SOFT);
					text.setLayoutManager(l);
					page.add(text);
					f.add(page, 0);

				}

				for (Object child : children) {
					IFigure childFig = (IFigure) child;
					if (childFig instanceof ScrollPane) {
						IFigure viewport = (IFigure) childFig.getChildren()
								.get(0);
						final IFigure entriesFig = (IFigure) viewport
								.getChildren().get(0);
						// for scroll persistence after Copy/Paste and similar
						// actions
						tempScrollY = lastScrollY;
						/**
						 * custom paint border that repaints whole area with the
						 * same color-pattern as in pressed pallete entry and
						 * then to fulfill IFigure painting, renders children on
						 * top of invalidated client area
						 */
						entriesFig.setBorder(new SchemeBorder(
								SchemeBorder.SCHEMES.LOWERED) {

							/*
							 * (non-Javadoc)
							 * 
							 * @see org.eclipse.draw2d.SchemeBorder#paint(org.eclipse.draw2d.Graphics,
							 *      org.eclipse.draw2d.IFigure,
							 *      org.eclipse.draw2d.geometry.Insets,
							 *      org.eclipse.swt.graphics.Color[],
							 *      org.eclipse.swt.graphics.Color[])
							 */
							@Override
							protected void paint(Graphics graphics,
									IFigure fig, Insets insets, Color[] tl,
									Color[] br) {
								org.eclipse.draw2d.geometry.Rectangle rect = getPaintRectangle(
										fig, insets);

								super.paint(graphics, fig, insets, tl, br);

								/*
								 * pattern interfearing with parent loaded
								 * patern but dot by dot works
								 */
								graphics
										.setForegroundColor(ColorConstants.white);
								for (int j = 1; j < rect.height - 2; j++)
									for (int i = 1; i < rect.width - 2; i += 2)
										graphics.drawPoint(i + rect.x
												+ (j + rect.y) % 2, j + rect.y);
								/*
								 * children repainting taken from default figure
								 * painting since border painting comes after
								 * children painting - now children repeated
								 * after border repaint
								 */
								IFigure child;
								org.eclipse.draw2d.geometry.Rectangle clip = org.eclipse.draw2d.geometry.Rectangle.SINGLETON;
								for (int i = 0; i < entriesFig.getChildren()
										.size(); i++) {
									child = (IFigure) entriesFig.getChildren()
											.get(i);
									if (child.isVisible()
											&& child.intersects(graphics
													.getClip(clip))) {
										graphics.clipRect(child.getBounds());
										child.paint(graphics);
										graphics.restoreState();
									}
								}
								if (tempScrollY != 0) { // for scroll
									// persistence
									((ScrollPane) entriesFig.getParent()
											.getParent())
											.scrollVerticalTo(ResourcePositionViewer.this.tempScrollY);
									tempScrollY = 0;

								}
							}
						});

						entriesFig.setOpaque(true);

						ToolbarLayout layout = (ToolbarLayout) entriesFig
								.getLayoutManager();

						// increases the spacing
						layout.setSpacing(10);

						for (Object entry : entriesFig.getChildren()) {
							IFigure labelFigure = (IFigure) ((IFigure) entry)
									.getChildren().get(0);
							// removes the default label figure which takes the
							// extra space
							labelFigure.remove((IFigure) labelFigure
									.getChildren().get(1));

							String dataId = ((IContentData) ((CombinedTemplateCreationEntry) ((EditPart) visualPartMap
									.get(entry)).getModel()).getTemplate())
									.getName();

							if (dataId == selectedId) {
								Clickable button = (Clickable) entry;

								button.getModel().setArmed(true);
								button.getModel().setPressed(true);
								lastSelected = button;

							}
							labelFigure
									.setToolTip(new Label(
											((IContentData) ((CombinedTemplateCreationEntry) ((EditPart) visualPartMap
													.get(entry)).getModel())
													.getTemplate()).getName()));
							labelFigure.setCursor(Display.getDefault()
									.getSystemCursor(SWT.CURSOR_HAND));
						}
					}
				}
			}
		}
	}

	/**
	 * Creates the palette root, which contains the categories and the
	 * subcategories.
	 * 
	 * @return the created palette root.
	 */
	protected PaletteRoot createPaletteRoot(IContent content) {
		PaletteRoot root = new PaletteRoot();
		PaletteContainer container = new PaletteDrawer(
				content == null ? ViewMessages.ResourceView_NoTheme : data
						.getName());

		if (content != null && data != null) {
			for (IContentData child : data.getChildren()) {
				ImageDescriptor desc = createImageDescriptor(child);
				container.add(new CombinedTemplateCreationEntry("", "", child,
						null, desc, desc));
			}
		}
		root.add(container);
		return root;
	}

	IResourceViewRectProvider rectProvider;

	protected ImageDescriptor createImageDescriptor(IContentData data) {
		if (rectProvider == null) {
			return null;
		} else {
			IContentData firstChild = data.getChildren()[0];
			IContentData screen = ScreenUtil
					.getScreenForData(firstChild, false);

			java.awt.Rectangle awtRec;
			if (screen != null) {
				awtRec = (java.awt.Rectangle) screen
						.getAttribute(ContentAttribute.BOUNDS.name());
			} else {
				awtRec = rectProvider.getResolution(data);
			}
			double scale = awtRec.width / (double) width;
			Rectangle resolutionRectangle = changeRec(awtRec, scale);

			java.awt.Rectangle dataRec = (java.awt.Rectangle) firstChild
					.getAttribute(ContentAttribute.BOUNDS.name());
			if (dataRec == null) {
				dataRec = rectProvider.getCategoryHighlightRect(data);
			}
			Rectangle rec = changeRec(dataRec, scale);
			rec.width = Math.max(2, rec.width);
			rec.height = Math.max(2, rec.height);

			Rectangle recSize = new Rectangle(0, 0,
					resolutionRectangle.width + 2,
					resolutionRectangle.height + 2);
			Rectangle border1 = new Rectangle(1, 1, resolutionRectangle.width,
					resolutionRectangle.height);

			Image image = new Image(Display.getDefault(), recSize);
			GC gc = new GC(image);
			gc
					.setBackground(Display
							.getDefault()
							.getSystemColor(
									new Boolean(
											(String) data
													.getAttribute(ContentAttribute.MODIFIED
															.name())) ? IIDEConstants.COLOR_OVERRIDE
											: COLOR));
			gc.setForeground(Display.getDefault().getSystemColor(
					SWT.COLOR_DARK_GRAY));
			gc.fillRectangle(rec);
			gc.drawRectangle(border1);
			gc.dispose();
			ImageData imageData = image.getImageData();
			image.dispose();

			ImageDescriptor descriptor = ImageDescriptor
					.createFromImageData(imageData);
			addImageDescriptorToDispose(descriptor);
			return descriptor;
		}

	}

	/**
	 * Coverts java.awt.Rectangle to org.eclipse.swt.graphics.Rectangle and
	 * change it's (x,y) to (1,1)
	 */
	private Rectangle changeRec(java.awt.Rectangle awtRec, double scale) {
		return new Rectangle(intValue(awtRec.x, scale) + 1, intValue(awtRec.y,
				scale) + 1, intValue(awtRec.width, scale), intValue(
				awtRec.height, scale));
	}

	private int intValue(int i, double scale) {
		if (scale == 0) {
			return i;
		}
		Double dob = i / scale;
		return dob.intValue();
	}
}
