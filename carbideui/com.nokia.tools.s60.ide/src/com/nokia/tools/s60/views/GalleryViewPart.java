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
package com.nokia.tools.s60.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.print.PrintGraphicalViewerOperation;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSite;

import com.nokia.tools.screen.ui.gallery.IGalleryAdapter;
import com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider;
import com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider.IGalleryScreen;

/**
 * This viewpart is used for displaying the preview gallery. When the gallery is
 * not available, an empty message page is displayed.
 */
public class GalleryViewPart extends PageBookView {
	public static final String ID = "com.nokia.tools.s60.views.Gallery";

	public static final String GALLERY_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "gallery_context"; 

	private static final int PRINT_SPACING = 20;

	private IWorkbenchPart currentPart;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
	 */
	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage(ViewMessages.GalleryView_defaultText);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(book,
				GalleryViewPart.GALLERY_CONTEXT);

		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		IGalleryAdapter adapter = (IGalleryAdapter) part
				.getAdapter(IGalleryAdapter.class);
		GalleryPage page = new GalleryPage(adapter);
		PageSite site = new PageSite(getViewSite());
		site.getActionBars().setGlobalActionHandler(
				ActionFactory.PRINT.getId(), new GalleryPrintAction(this));
		page.init(site);
		page.createControl(getPageBook());
		return new PageRec(part, page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.ui.part.PageBookView.PageRec)
	 */
	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		pageRecord.page.dispose();
		pageRecord.dispose();
		if (part == currentPart) {
			currentPart = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
	 */
	@Override
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page = getSite().getPage();
		if (page != null)
			return page.getActiveEditor();

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		
		// the graphics editor is open, later we'll synchronize all parts
		// with the current active editor
		return (part.getAdapter(IGalleryAdapter.class) != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#showPageRec(org.eclipse.ui.part.PageBookView.PageRec)
	 */
	@Override
	protected void showPageRec(PageRec pageRec) {
		IPageSite pageSite = getPageSite(pageRec.page);
		ISelectionProvider provider = pageSite.getSelectionProvider();
		if (provider == null && (pageRec.page instanceof GalleryPage)) {
			pageSite.setSelectionProvider((GalleryPage) pageRec.page);
		}

		if (pageRec.part != currentPart) {
			if (pageRec.page instanceof GalleryPage) {
				IGalleryScreenProvider screenProvider = (IGalleryScreenProvider) pageRec.part
						.getAdapter(IGalleryScreenProvider.class);
				if (screenProvider != null) {
					((GalleryPage) pageRec.page).galleryChanged(screenProvider,
							false);
				}
			}
			currentPart = pageRec.part;
		}

		super.showPageRec(pageRec);
	}

	protected GraphicalViewer createPrinterGraphicalViewer() {
		GraphicalViewer viewer = new ScrollingGraphicalViewer();
		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
		return viewer;
	}

	protected Control fitToPrinter(GraphicalViewer viewer, Rectangle clip) {
		PrinterGraphicalEditPart part = new PrinterGraphicalEditPart(clip);
		viewer.setContents(part);

		Composite composite = new Composite(getPageBook(), SWT.BORDER);
		StackLayout stack = new StackLayout();
		stack.topControl = viewer.createControl(composite);
		composite.setLayout(stack);

		composite.setSize(clip.width, clip.height);
		return composite;
	}

	class PrinterGraphicalEditPart extends AbstractGraphicalEditPart {
		List<IFigure> figures;
		PrinterLayout layout;

		PrinterGraphicalEditPart(Rectangle clip) {
			figures = new ArrayList<IFigure>();
			int width = 0, height = 0;
			for (IGalleryScreen screen : ((GalleryPage) getCurrentPage())
					.getScreens()) {
				if (screen.getControl() != null
						&& !screen.getControl().isDisposed()) {
					EditPartViewer v = screen.getViewer(null);
					IFigure fig = (IFigure) ((GraphicalEditPart) v
							.getContents()).getFigure().getChildren().get(0);
					figures.add(fig);
					if (width == 0 || height == 0) {
						width = fig.getBounds().width;
						height = fig.getBounds().height;
					}
				}
			}
			layout = new PrinterLayout(clip, width, height);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
		 */
		@Override
		protected IFigure createFigure() {
			Figure fig = new Figure();
			fig.setLayoutManager(layout);

			for (final IFigure f : figures) {
				IFigure wrapped = new Figure() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
					 */
					@Override
					public void paint(Graphics graphics) {
						Rectangle bounds = getBounds().getCopy();
						graphics.translate(-f.getBounds().x + bounds.x, -f
								.getBounds().y
								+ bounds.y);
						f.paint(graphics);
					}
				};
				wrapped.setSize(f.getBounds().getSize());
				fig.add(wrapped);
			}

			fig.setSize(fig.getPreferredSize());

			return fig;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
		 */
		@Override
		protected void createEditPolicies() {
		}
	}

	class PrinterLayout extends AbstractLayout {
		Rectangle clip;
		int width;
		int height;

		PrinterLayout(Rectangle clip, int width, int height) {
			this.clip = clip;
			this.width = width;
			this.height = height;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.draw2d.AbstractLayout#calculatePreferredSize(org.eclipse.draw2d.IFigure,
		 *      int, int)
		 */
		@Override
		protected Dimension calculatePreferredSize(IFigure container,
				int wHint, int hHint) {
			if (width <= 0 || height <= 0) {
				return Dimension.SINGLETON;
			}
			int numberOfChildren = container.getChildren().size();
			int cols = clip.width / width;
			int rows = clip.height / (height + PRINT_SPACING);
			int totalPerPage = cols * rows;
			int numberOfPages = 1;
			if (totalPerPage > 0) {
				numberOfPages = numberOfChildren / totalPerPage;
				if (numberOfChildren % totalPerPage != 0) {
					numberOfPages++;
				}
			}
			return new Dimension(clip.width, clip.height * numberOfPages);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.draw2d.LayoutManager#layout(org.eclipse.draw2d.IFigure)
		 */
		public void layout(IFigure container) {
			if (width <= 0 || height <= 0) {
				return;
			}
			List children = container.getChildren();

			int cols = (clip.width - 2 * PRINT_SPACING) / (width + PRINT_SPACING);
			int rows = (clip.height - 2 * PRINT_SPACING) / (height + PRINT_SPACING);

			int index = 0;
			int currentPage = 0;
			while (index < children.size()) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols && index < children.size(); j++) {
						int x = PRINT_SPACING + j * (width + PRINT_SPACING);
						int y = currentPage * clip.height + PRINT_SPACING + i
								* (height + PRINT_SPACING);
						((IFigure) children.get(index++))
								.setBounds(new Rectangle(x, y, width, height));
					}
				}
				currentPage++;
			}
		}
	}

	class GalleryPrintAction extends PrintAction {
		GalleryPrintAction(IWorkbenchPart part) {
			super(part);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.gef.ui.actions.PrintAction#calculateEnabled()
		 */
		@Override
		protected boolean calculateEnabled() {
			return super.calculateEnabled()
					&& getCurrentPage() instanceof GalleryPage;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.gef.ui.actions.PrintAction#run()
		 */
		@Override
		public void run() {
			PrintDialog dialog = new PrintDialog(getSite().getShell(), SWT.NULL);
			PrinterData data = dialog.open();

			if (data != null) {
				GraphicalViewer viewer = createPrinterGraphicalViewer();
				PrintGraphicalViewerOperation op = new PrintGraphicalViewerOperation(
						new Printer(data), viewer) {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.draw2d.PrintFigureOperation#printPages()
					 */
					@Override
					protected void printPages() {
						Graphics pg = getFreshPrinterGraphics();
						double dpiScale = (double) getPrinter().getDPI().x
								/ Display.getCurrent().getDPI().x;
						pg.scale(dpiScale);
						Rectangle rect = new Rectangle();
						pg.getClip(rect);
						Control control = fitToPrinter(getViewer(), rect);
						try {
							super.printPages();
						} finally {
							control.dispose();
						}
					}
				};
				op.run(getWorkbenchPart().getTitle());
			}
		}
	}
}
