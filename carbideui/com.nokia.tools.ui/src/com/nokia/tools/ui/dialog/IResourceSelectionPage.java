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
package com.nokia.tools.ui.dialog;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Interface for the resource select page. A resource selection page can be used
 * to select single or multiple resources, typically it features a preview or
 * play control that is handled by the resource page manager.
 * 
 */
public interface IResourceSelectionPage {
	/**
	 * Sets the resource page manager.
	 * 
	 * @param manager the resource page manager.
	 */
	void setManager(IResourcePageManager manager);

	/**
	 * @return the resource page manager.
	 */
	IResourcePageManager getManager();

	/**
	 * Creates the page control.
	 * 
	 * @param parent the parent composite.
	 * @return the created control.
	 */
	Control createPage(Composite parent);

	/**
	 * Called when the focus is gained on this page.
	 */
	void setFocus();

	/**
	 * Called when the page is disposed.
	 */
	void dispose();

	/**
	 * Creates a new image.
	 * 
	 * @param data the resource data.
	 * @param width width of the image.
	 * @param height height of the image.
	 * @param keepAspectRatio true for keeping aspect ratio, false for
	 *            stretching.
	 * @return the newly created image.
	 */
	Image createImage(Object data, int width, int height,
			boolean keepAspectRatio);

	/**
	 * Sets the icon image descriptor.
	 * 
	 * @param descriptor the icon image descriptor.
	 */
	void setIconImageDescriptor(ImageDescriptor descriptor);

	/**
	 * @return the icon image descriptor.
	 */
	ImageDescriptor getIconImageDescriptor();

	/**
	 * Sets the id of the page.
	 * 
	 * @param id if of the page.
	 */
	void setId(String id);

	/**
	 * @return the id of the page.
	 */
	String getId();

	/**
	 * Sets the title of the page.
	 * 
	 * @param title title of the page.
	 */
	void setTitle(String title);

	/**
	 * @return the title of the page.
	 */
	String getTitle();

	/**
	 * Initializes the page with the provided resources.
	 * 
	 * @param resources the initial resources.
	 * @return the selected resources.
	 */
	Object[] init(Object[] resources);

	/**
	 * @return the selected resources.
	 */
	Object[] getSelectedResources();

	/**
	 * Called when the error checking is performed by the manager, this shall
	 * return non-null strings if there is error on the page.
	 * 
	 * @return the error string.
	 */
	String checkError();

	/**
	 * Stub implementation of the resource selection page for clients to extend.
	 */
	abstract class Adapter implements IResourceSelectionPage,
			ISelectionChangedListener, IOpenListener {
		private IResourcePageManager manager;
		private ImageDescriptor descriptor;
		private String id;
		private String title;

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#getManager()
		 */
		public IResourcePageManager getManager() {
			return manager;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#setManager(com.nokia.tools.screen.ui.dialogs.IResourcePageManager)
		 */
		public void setManager(IResourcePageManager manager) {
			this.manager = manager;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#getIconImageDescriptor()
		 */
		public ImageDescriptor getIconImageDescriptor() {
			return descriptor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#setIconImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
		 */
		public void setIconImageDescriptor(ImageDescriptor descriptor) {
			this.descriptor = descriptor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#getId()
		 */
		public String getId() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#setId(java.lang.String)
		 */
		public void setId(String id) {
			this.id = id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#getTitle()
		 */
		public String getTitle() {
			return title;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#setTitle(java.lang.String)
		 */
		public void setTitle(String title) {
			this.title = title;
		}

		/**
		 * Calculates the index of row by the given mouse location.
		 * 
		 * @param table the table control.
		 * @param posY y coordinate of the mouse location.
		 * @return the index of the row that is under the point.
		 */
		protected int calculateIndex(Table table, int posY) {
			if (table.getItemCount() == 0) {
				return -1;
			}
			int scrollPosY = table.getVerticalBar().getSelection();
			int itemHeight = table.getItemHeight();
			int headerHeight = table.getHeaderHeight();
			int index = (int) (((posY - headerHeight) + (scrollPosY * itemHeight)) / itemHeight);
			if (index >= table.getItemCount())
				index = table.getSelectionIndex();
			return index;
		}

		/**
		 * Handles the mouse move events. This should be called by the
		 * table-based implementation upon mouse movements.
		 * 
		 * @param table the table control.
		 * @param e the mouse event.
		 */
		protected void handleMouseMove(Table table, MouseEvent e) {
			if (getManager().isHoverEnabled(this)) {
				int index;
				if (e.widget == table) {
					int posY = e.y;
					index = calculateIndex(table, posY);
				} else {
					index = table.getSelectionIndex();
				}
				if (index < 0) {
					return;
				}
				TableItem element = table.getItem(index);
				if (!getManager().isResourceSelected(element.getData())) {
					getManager().resourcesSelected(
							new Object[] { element.getData() });
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection) {
				getManager().resourcesSelected(
						((IStructuredSelection) selection).toArray());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IOpenListener#open(org.eclipse.jface.viewers.OpenEvent)
		 */
		public void open(OpenEvent event) {
			ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection) {
				getManager().resourcesOpened(
						((IStructuredSelection) selection).toArray());
			}
		}
	}

}
