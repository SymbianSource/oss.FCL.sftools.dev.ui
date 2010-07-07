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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart;
import org.eclipse.gef.internal.ui.palette.editparts.RaisedBorder;
import org.eclipse.gef.internal.ui.palette.editparts.ToolEntryEditPart;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.PaletteEditPartFactory;
import org.eclipse.gef.ui.palette.PaletteMessages;
import org.eclipse.gef.ui.palette.editparts.PaletteEditPart;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;

/**
 * This viewer displays the theme categories in a two-level tree view. Each
 * category can be expanded and pinnned.
 */
public class ResourceViewer extends EarlyDisposablePaletteViewer {
	private static final String extensionPointId = "resourceViewerSections";
	private static final String projectId = "com.nokia.tools.screen.ui";
	protected static final Border BORDER = new MarginBorder(0, 3, 0, 3);

	protected static final Border TOOL_TIP_BORDER = new MarginBorder(0, 2, 0, 2);

	private static final int H_GAP = 4;

	private static final int LINE_LENGTH = 1000; // 20;

	private static final int MIN_LINE_LENGTH = 6;

	private IContentAdapter contentAdapter;

	private Font boldFont;

	private Font plainFont;

	private Set<ChangeListener> listeners = new HashSet<ChangeListener>();

	private List<IResourceSection> resourceSections;

	public boolean hasContent() {
		PaletteRoot root = getPaletteRoot();
		if (root.getChildren().size() > 0) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Initializes the resouce viewer with a particular theme.
	 * 
	 */
	public ResourceViewer(IContentAdapter adapter, final ResourcePage page) {
		this.setEditPartFactory(new PaletteEditPartFactory() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.gef.ui.palette.PaletteEditPartFactory#createEditPart(org.eclipse.gef.EditPart,
			 *      java.lang.Object)
			 */
			@Override
			public EditPart createEditPart(EditPart parentEditPart, Object model) {
				if (model instanceof PaletteText) {
					return new TextEditPart(((PaletteText) model));
				}
				final EditPart part = super.createEditPart(parentEditPart,
						model);
				return part;
			}

		});
		resourceSections = createResourceSections();
		reset(adapter);

		/* add copy element(s) action */
		MenuManager mmgr = new MenuManager();
		mmgr.setRemoveAllWhenShown(true);

		for (IResourceSection section : resourceSections) {
			IMenuListener listener = section.createResourceViewerMenuListener(
					this, page.getCommandStack());
			if (listener != null) {
				mmgr.addMenuListener(listener);
			}
		}
		setContextMenu(mmgr);
		FontData fd = JFaceResources.getDefaultFont().getFontData()[0];

		boldFont = new Font(Display.getDefault(), fd.getName(), fd.getHeight(),
				SWT.BOLD);
		plainFont = new Font(Display.getDefault(), fd.getName(),
				fd.getHeight(), SWT.NORMAL);
		addResourceToDispose(boldFont);
		addResourceToDispose(plainFont);
	}

	/**
	 * Resets to the original state.
	 */
	public void reset(IContentAdapter adapter) {
		this.contentAdapter = adapter;
		final PaletteRoot root = createPaletteRoot2(getPaletteRoot());
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				setPaletteRoot(root);
			}

		});
		for (Object child : root.getChildren()) {
			if (child instanceof PaletteDrawer) {
				final EditPart part = (EditPart) getEditPartRegistry().get(
						child);
				if (part instanceof DrawerEditPart) {
					((DrawerEditPart) part).getDrawerFigure()
							.getCollapseToggle().addChangeListener(
									new ChangeListener() {

										/*
										 * (non-Javadoc)
										 * 
										 * @see org.eclipse.draw2d.ChangeListener#handleStateChanged(org.eclipse.draw2d.ChangeEvent)
										 */
										public void handleStateChanged(
												ChangeEvent event) {
											for (ChangeListener listener : listeners) {
												event = new ChangeEvent(part,
														event.getPropertyName());
												listener
														.handleStateChanged(event);
											}
										}
									});
				}
			}
		}
		if (getControl() != null) {
			updateFonts();
		}
	}

	protected PaletteRoot createPaletteRoot2(PaletteRoot oldRoot) {
		PaletteRoot root = new PaletteRoot();
		try {
			for (IResourceSection section : resourceSections) {
				List<PaletteDrawer> drawers = section
						.getPaletteDrawers(contentAdapter);
				if (drawers != null && drawers.size() > 0) {
					root.add(new PaletteText(section.getSectionHeading(), 7,
							SWT.NORMAL));
					for (PaletteDrawer drawer : drawers) {
						root.add(drawer);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return root;
	}

	private void updateFonts() {
		for (Object obj : getEditPartRegistry().values()) {
			if (obj instanceof GraphicalEditPart) {
				GraphicalEditPart editPart = (GraphicalEditPart) obj;
				editPart.getFigure()
						.setFont(
								editPart.getChildren().isEmpty() ? plainFont
										: boldFont);
			}
		}
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.palette.PaletteViewer#hookControl()
	 */
	@Override
	protected void hookControl() {

		updateFonts();

		super.hookControl();
	}

	static class PaletteText extends PaletteEntry {

		/** Type identifier * */
		public static final Object PALETTE_TYPE_TEXT = "$Palette Text";//$NON-NLS-1$

		private String label;

		private int fHeight;

		private int fStyle;

		/**
		 * Constructor
		 * 
		 * @param id This Separator's unique ID
		 */
		public PaletteText(String label, int fHeight, int fStyle) {
			super(PaletteMessages.NEW_SEPARATOR_LABEL, "", PALETTE_TYPE_TEXT);//$NON-NLS-1$
			this.label = label;
			this.fHeight = fHeight;
			this.fStyle = fStyle;
			setId("");
		}

		@Override
		public String getLabel() {
			return label;
		}
	}

	class TextEditPart extends PaletteEditPart {
		public TextEditPart(PaletteText separator) {
			super(separator);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
		 */
		@Override
		protected IFigure createFigure() {
			TitleLabel label = new TitleLabel(((PaletteText) getModel())
					.getLabel(), ((PaletteText) getModel()).fHeight,
					((PaletteText) getModel()).fStyle, true);
			label.setBorder(new RaisedBorder(1, 6, 1, 6));
			return label;
		}
	}

	class TitleLabel extends Label {

		private boolean horizontal;
		private Font titleFont;

		public TitleLabel(String label, int fHeight, int fStyle,
				boolean isHorizontal) {
			super(label);
			horizontal = isHorizontal;
			setLabelAlignment(PositionConstants.CENTER);
			setBorder(BORDER);
			Label tooltip = new Label(getText());
			tooltip.setBorder(TOOL_TIP_BORDER);
			setToolTip(tooltip);
			setForegroundColor(ColorConstants.listForeground);
			FontData fd = JFaceResources.getDefaultFont().getFontData()[0];
			titleFont = new Font(Display.getDefault(), fd.getName(), fHeight,
					fStyle);
			addResourceToDispose(titleFont);
		}

		@Override
		public IFigure getToolTip() {
			if (isTextTruncated())
				return super.getToolTip();
			return null;
		}

		@Override
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);
			org.eclipse.draw2d.geometry.Rectangle area = getBounds()
					.getCropped(super.getInsets());
			org.eclipse.draw2d.geometry.Rectangle textBounds = getTextBounds();
			// We reduce the width by 1 because FigureUtilities grows it by 1
			// unnecessarily
			textBounds.width--;
			setFont(titleFont);
			if (hasFocus())
				graphics.drawFocus(bounds.getResized(-1, -1).intersect(
						textBounds.getExpanded(getInsets())));

			int lineWidth = Math.min(
					(area.width - textBounds.width - H_GAP * 2) / 2,
					LINE_LENGTH);

			if (lineWidth >= MIN_LINE_LENGTH) {
				int centerY = area.y + area.height / 2;
				graphics.setForegroundColor(ColorConstants.buttonLightest);
				graphics.drawLine(textBounds.x - H_GAP - lineWidth,
						centerY - 3, textBounds.x - H_GAP, centerY - 3);
				graphics.drawLine(textBounds.x - H_GAP - lineWidth,
						centerY + 2, textBounds.x - H_GAP, centerY + 2);
				graphics.drawLine(textBounds.right() + H_GAP, centerY - 3,
						textBounds.right() + H_GAP + lineWidth, centerY - 3);
				graphics.drawLine(textBounds.right() + H_GAP, centerY + 2,
						textBounds.right() + H_GAP + lineWidth, centerY + 2);
				graphics.setForegroundColor(ColorConstants.buttonDarker);
				graphics.drawLine(textBounds.x - H_GAP - lineWidth,
						centerY + 3, textBounds.x - H_GAP, centerY + 3);
				graphics.drawLine(textBounds.x - H_GAP - lineWidth,
						centerY - 2, textBounds.x - H_GAP, centerY - 2);
				graphics.drawLine(textBounds.right() + H_GAP, centerY - 2,
						textBounds.right() + H_GAP + lineWidth, centerY - 2);
				graphics.drawLine(textBounds.right() + H_GAP, centerY + 3,
						textBounds.right() + H_GAP + lineWidth, centerY + 3);
				if (horizontal) {
					graphics.drawPoint(textBounds.x - H_GAP, centerY + 2);
					graphics.drawPoint(textBounds.x - H_GAP, centerY - 3);
					graphics.drawPoint(textBounds.right() + H_GAP + lineWidth,
							centerY - 3);
					graphics.drawPoint(textBounds.right() + H_GAP + lineWidth,
							centerY + 2);
					graphics.setForegroundColor(ColorConstants.buttonLightest);
					graphics.drawPoint(textBounds.x - H_GAP - lineWidth,
							centerY - 2);
					graphics.drawPoint(textBounds.x - H_GAP - lineWidth,
							centerY + 3);
					graphics.drawPoint(textBounds.right() + H_GAP, centerY - 2);
					graphics.drawPoint(textBounds.right() + H_GAP, centerY + 3);
				} else {
					graphics.drawPoint(textBounds.x - H_GAP - lineWidth,
							centerY + 2);
					graphics.drawPoint(textBounds.x - H_GAP - lineWidth,
							centerY - 3);
					graphics.drawPoint(textBounds.right() + H_GAP, centerY - 3);
					graphics.drawPoint(textBounds.right() + H_GAP, centerY + 2);
					graphics.setForegroundColor(ColorConstants.buttonLightest);
					graphics.drawPoint(textBounds.x - H_GAP, centerY - 2);
					graphics.drawPoint(textBounds.x - H_GAP, centerY + 3);
					graphics.drawPoint(textBounds.right() + H_GAP + lineWidth,
							centerY - 2);
					graphics.drawPoint(textBounds.right() + H_GAP + lineWidth,
							centerY + 3);
				}
			}
		}
	}

	public static List<IResourceSection> createResourceSections() {
		List<IResourceSection> sections = new ArrayList<IResourceSection>();
		IExtensionPoint extension = Platform.getExtensionRegistry()
				.getExtensionPoint(projectId, extensionPointId);
		IExtension[] extensions = extension.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				try {
					Object execExt = configElements[j]
							.createExecutableExtension("class");
					if (execExt instanceof IResourceSection) {
						sections.add((IResourceSection) execExt);
					}
				} catch (CoreException e) {
				}
			}
		}
		return sections;
	}

	public IResourceViewerSelectionHelper getResourceSelectionHelper(
			IContentData data) {
		IResourceViewerSelectionHelper helper = null;
		if (data != null) {
			for (IResourceSection section : this.resourceSections) {
				helper = section.getSelectionHelper(data);
				if (helper != null) {
					return helper;
				}
			}
		}
		return helper;
	}

	public IResourceSection getResourceSection(IContentData data) {
		IResourceSection sectionToReturn = null;
		if (data != null) {
			for (IResourceSection section : this.resourceSections) {
				if (section.supportsContent(data.getRoot())) {
					sectionToReturn = section;
					return sectionToReturn;
				}
			}
		}
		return sectionToReturn;
	}

	public void appendSelection(EditPart editpart) {
		if (editpart instanceof ToolEntryEditPart) {
			getSelectionManager().appendSelection(editpart);
		}
	}

	/**
	 * @return the resourceSections
	 */
	public List<IResourceSection> getResourceSections() {
		return resourceSections;
	}

}
