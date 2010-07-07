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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.nokia.tools.ui.Activator;
import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;
import com.nokia.tools.ui.prefs.UIPreferences;

/**
 * Dialog based implementation of the resource page manager. Multiple pages are
 * shown as tabs.
 * 
 */
public class ResourceSelectionDialog
    extends BrandedTitleAreaDialog
    implements IResourcePageManager {

	private static Image NO_IMAGE = new Image(null, Activator
	    .getImageDescriptor("icons/x.gif").getImageData().scaledTo(ICON_SIZE,
	        ICON_SIZE));

	protected IResourceSelectionPage[] pages;

	private Composite rootArea;
	
	private Group previewArea;

	private TabFolder folder;

	private Set<Image> images = new HashSet<Image>();

	private Label previewCanvas;

	private String bannerMessage;

	private Object[] resources;

	private Button previewHoverCheckBox;

	private int previewSize = PREVIEW_SMALL_SIZE;

	protected IResourceSelectionPage selectedPage;

	private String titleMessage;

	private boolean showPreviewHoverCheckBox;

	/**
	 * Constructs a new resource selection dialog.
	 * 
	 * @param parentShell the parent shell.
	 * @param pages the resource selection pages.
	 * @param bannerMessage the banner message of the dialog.
	 */
	public ResourceSelectionDialog(Shell parentShell,
	    IResourceSelectionPage[] pages, String bannerMessage) {
		this(parentShell, pages, bannerMessage, null, true);
	}

	public ResourceSelectionDialog(Shell parentShell,
	    IResourceSelectionPage[] pages, String bannerMessage,
	    String titleMessage, boolean showPreviewHoverCheckBox) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.pages = pages;
		setManagerForPages(pages);
		this.bannerMessage = bannerMessage;
		this.titleMessage = titleMessage;
		this.showPreviewHoverCheckBox = showPreviewHoverCheckBox;
	}

	/**
	 * Associates the pages with the current manager.
	 * 
	 * @param pages
	 */
	private void setManagerForPages(IResourceSelectionPage[] pages) {
		if (null != pages) {
			for (IResourceSelectionPage page : pages) {
				page.setManager(this);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@SuppressWarnings("unchecked")
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		if (null == titleMessage)
			setTitle(Messages.ResourceSelectionDialog_Banner_Title);
		else
			setTitle(titleMessage);
		if (bannerMessage != null)
			setMessage(bannerMessage);
		else
			setMessage(Messages.ResourceSelectionDialog_Banner_Message);

		rootArea = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);

		rootArea.setLayoutData(gd);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 13;
		gl.horizontalSpacing = 7;
		gl.verticalSpacing = 7;
		gl.marginWidth = 13;
		rootArea.setLayout(gl);
		
		if(pages !=null){	

		if (pages.length > 1) {
			folder = new TabFolder(rootArea, SWT.NONE);
			gd = new GridData(GridData.FILL_BOTH);
			folder.setLayoutData(gd);

			for (IResourceSelectionPage page : pages) {
				TabItem item = new TabItem(folder, SWT.NONE);
				item.setText(page.getTitle());
				if (null != page.getIconImageDescriptor()) {
					Image themeTabImage = page.getIconImageDescriptor()
					    .createImage();
					item.setImage(themeTabImage);
					images.add(themeTabImage);
				}

				Composite mainArea = new Composite(folder, SWT.NONE);
				gd = new GridData(GridData.FILL, GridData.FILL, true, true);
				mainArea.setLayoutData(gd);
				gl = new GridLayout(1, false);
				gl.marginHeight = 9;
				gl.verticalSpacing = 7;
				gl.marginWidth = 9;
				mainArea.setLayout(gl);

				item.setControl(mainArea);
				page.createPage(mainArea);
			}

			folder.addSelectionListener(new SelectionAdapter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				@Override
				public void widgetSelected(SelectionEvent e) {
					IResourceSelectionPage page = getCurrentPage();
					resourcesSelected(page.getSelectedResources());
					page.setFocus();
					refresh();
				}

			});
			folder.setSelection(getInitialPageIndex());
		} else {
			Composite mainArea = new Composite(rootArea, SWT.NONE);
			gd = new GridData(GridData.FILL, GridData.FILL, true, true);
			mainArea.setLayoutData(gd);
			gl = new GridLayout(1, false);
			gl.marginHeight = 0;
			gl.verticalSpacing = 7;
			gl.marginWidth = 0;
			mainArea.setLayout(gl);

			pages[0].createPage(mainArea);
		}

		// Preview area
		previewArea = new Group(rootArea, SWT.NONE);
		previewArea.setText(Messages.ResourceSelectionDialog_Image_Preview);
		gd = new GridData(SWT.NONE, SWT.FILL, false, true);
		previewArea.setLayoutData(gd);		
		gl = new GridLayout();
		gl.marginWidth = 0;
		gl.marginHeight = 5;
		gl.marginLeft = 5;
		previewArea.setLayout(gl);

		previewCanvas = new Label(previewArea, SWT.CENTER);
		gd = new GridData(SWT.NONE, SWT.FILL, false, true);
		gd.widthHint = PREVIEW_SMALL_SIZE;
		previewCanvas.setLayoutData(gd);
		previewCanvas.addDisposeListener(new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				if (previewCanvas.getImage() != null) {
					previewCanvas.getImage().dispose();
				}
			}
		});
		previewCanvas.addMouseListener(new MouseAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent e) {
				if (previewCanvas.getImage() == null)
					return;
				if (previewSize == PREVIEW_SMALL_SIZE)
					previewSize = PREVIEW_LARGER_SIZE;
				else if (previewSize == PREVIEW_LARGER_SIZE)
					previewSize = PREVIEW_BIG_SIZE;
				else
					previewSize = PREVIEW_SMALL_SIZE;

				GridData gd = (GridData) previewCanvas.getLayoutData();
				gd.widthHint = previewSize;
				previewCanvas.setLayoutData(gd);

				gd = (GridData) previewArea.getLayoutData();
				Point p = previewArea.computeSize(SWT.DEFAULT, SWT.DEFAULT,
				    true);
				gd.widthHint = p.x;
				gd.heightHint = p.y;
				previewArea.setLayoutData(gd);

				resourcesSelected(getCurrentPage().getSelectedResources());
				previewArea.layout();
				previewArea.getParent().layout();
			}
		});

		createPreviewHoverCheckBox();

		Composite horSeparatorArea = new Composite(area, SWT.NONE);
		horSeparatorArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gl = new GridLayout();
		horSeparatorArea.setLayout(gl);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		final Label separator = new Label(horSeparatorArea, SWT.SEPARATOR
		    | SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		separator.setLayoutData(gd);

		applyDialogFont(dialogArea);

		initialSizes();
		initialSelection();
		
		}

		return area;
	}

	private void createPreviewHoverCheckBox() {
		if (!showPreviewHoverCheckBox())
			return;

		previewHoverCheckBox = new Button(rootArea, SWT.CHECK);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		previewHoverCheckBox.setLayoutData(gd);
		previewHoverCheckBox
		    .setText(Messages.ResourceSelectionDialog_chkPreviewHoverLabel);
		IPreferenceStore store = UIPreferences.getStore();
		previewHoverCheckBox.setSelection(store
		    .getBoolean(UIPreferences.PREF_HOVER_ENABLED_RESOURCE_PAGE));
	}

	/**
	 * @return
	 */
	private boolean showPreviewHoverCheckBox() {
		return showPreviewHoverCheckBox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#getCurrentPage()
	 */
	public IResourceSelectionPage getCurrentPage() {
		if(pages !=null){
			return pages.length > 1 ? pages[folder.getSelectionIndex()] : pages[0];
		}
		
		return null;
		
	}

	/**
	 * Finds the page index by the page id.
	 * 
	 * @param Id id of the page.
	 * @return the index of the page in the tab folder.
	 */
	private int getPageIndex(String Id) {
		for (int i = 0; i < pages.length; i++)
			if (pages[i].getId().equals(Id))
				return i;
		return 0;
	}

	/**
	 * Initializes the all control sizes.
	 */
	private void initialSizes() {
		IPreferenceStore store = UIPreferences.getStore();

		previewSize = store
		    .getInt(UIPreferences.PREF_PREVIEW_SIZE_RESOURCE_PAGE);
		if (previewSize <= 0) {
			previewSize = PREVIEW_SMALL_SIZE;
		}
		GridData gd = (GridData) previewCanvas.getLayoutData();
		gd.widthHint = previewSize;
		previewCanvas.setLayoutData(gd);

		gd = (GridData) previewArea.getLayoutData();
		Point p = previewArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		gd.widthHint = p.x;
		gd.heightHint = p.y;
		previewArea.setLayoutData(gd);

		gd = (GridData) previewArea.getParent().getLayoutData();
		gd.widthHint = store
		    .getInt(UIPreferences.PREF_LAST_WIDTH_RESOURCE_PAGE);
		gd.heightHint = store
		    .getInt(UIPreferences.PREF_LAST_HEIGHT_RESOURCE_PAGE);
		previewArea.getParent().setLayoutData(gd);

		previewArea.layout();
		previewArea.getParent().layout();
	}

	/**
	 * @return the initial page index.
	 */
	private int getInitialPageIndex() {
		IPreferenceStore store = UIPreferences.getStore();
		return getPageIndex(store
		    .getString(UIPreferences.PREF_LAST_ACTIVE_RESOURCE_PAGE));
	}

	/**
	 * Initializes the selection.
	 */
	private void initialSelection() {
		Object[] copy = resources == null ? null : new Object[resources.length];
		if (resources != null) {
			System.arraycopy(resources, 0, copy, 0, resources.length);
		}

		for (int i = 0; i < pages.length; i++) {
			Object[] rs = pages[i].init(copy);
			if (null != rs && rs.length > 0) {
				if (folder != null) {
					folder.setSelection(i);
				}
			}
		}

		resourcesSelected(getCurrentPage().getSelectedResources());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
		    false);
		createButton(parent, IDialogConstants.CANCEL_ID,
		    IDialogConstants.CANCEL_LABEL, false);
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog#close()
	 */
	@Override
	public boolean close() {
		Shell shell = this.getShell();
		IPreferenceStore store = UIPreferences.getStore();
		if(getCurrentPage() != null){
			store.setValue(UIPreferences.PREF_LAST_ACTIVE_RESOURCE_PAGE,
			    getCurrentPage().getId());
		}		
		if(previewArea !=null){
			store.setValue(UIPreferences.PREF_PREVIEW_SIZE_RESOURCE_PAGE,
			    previewSize);
			store.setValue(UIPreferences.PREF_LAST_WIDTH_RESOURCE_PAGE, previewArea
			    .getParent().getSize().x);
			store.setValue(UIPreferences.PREF_LAST_HEIGHT_RESOURCE_PAGE,
			    previewArea.getParent().getSize().y);
		}		
		store.setValue(UIPreferences.PREF_LAST_XPOS_IMAGESELECTION_DIALOG,
		    shell.getBounds().x);
		store.setValue(UIPreferences.PREF_LAST_YPOS_IMAGESELECTION_DIALOG,
		    shell.getBounds().y);
		if (showPreviewHoverCheckBox && previewHoverCheckBox !=null) {
			store.setValue(UIPreferences.PREF_HOVER_ENABLED_RESOURCE_PAGE,
			    previewHoverCheckBox.getSelection());
		}
		
		if(pages != null){
			for (IResourceSelectionPage page : pages ) {
				page.dispose();
			}	
		}
		
		for (Image image : images) {
			image.dispose();
		}
		if (previewCanvas != null && previewCanvas.getImage() != null) {
			previewCanvas.getImage().dispose();
		}
		return super.close();
	}

	/**
	 * Paints the preview for the selected element.
	 * 
	 * @param element the selected resource.
	 */
	private void repaintPreviewCanvas(Object element) {
		if (previewCanvas == null)
			return;

		Image image = createImage(element, previewSize, previewSize, true);
		if (previewCanvas.getImage() != null) {
			previewCanvas.getImage().dispose();
		}
		previewCanvas.setImage(image);
		if (image != null) {
			previewCanvas.setCursor(previewCanvas.getDisplay().getSystemCursor(
			    SWT.CURSOR_HAND));
			previewCanvas
			    .setToolTipText(Messages.ResourceSelectionDialog_Image_Tooltip_Size);
		} else {
			previewCanvas.setCursor(previewCanvas.getDisplay().getSystemCursor(
			    SWT.CURSOR_ARROW));
			previewCanvas.setToolTipText("");
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
			buttonPressed(IDialogConstants.OK_ID);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if(getCurrentPage() != null){
			resources = getCurrentPage().getSelectedResources();
		}
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		resources = null;
		super.cancelPressed();
	}

	/**
	 * @return the resources
	 */
	public Object[] getResources() {
		return resources;
	}

	/**
	 * @param resources the resources to set
	 */
	public void setResources(Object[] resources) {
		this.resources = resources;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.utils.BrandedTitleAreaDialog#getBannerIconDescriptor()
	 */
	// @Override
	protected ImageDescriptor getBannerIconDescriptor() {
		return Activator.getImageDescriptor("icons/wizban/select_image.png");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.utils.BrandedTitleAreaDialog#getTitle()
	 */
	@Override
	protected String getTitle() {		
		return this.titleMessage == null ? Messages.ResourceSelectionDialog_Title
		    : this.titleMessage;
	}

	/**
	 * Updates error messages in the banner area
	 */
	public void updateStatus(String message) {
		setErrorMessage(message);
	}

	/**
	 * Handles enabled/disabled state
	 */
	public void updateButtonState(boolean state) {
		if (getButton(IDialogConstants.OK_ID) != null) {
			getButton(IDialogConstants.OK_ID).setEnabled(state);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#isHoverEnabled(com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage)
	 */
	public boolean isHoverEnabled(IResourceSelectionPage page) {
		return showPreviewHoverCheckBox && previewHoverCheckBox.getSelection()
		    && getCurrentPage() == page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#refresh()
	 */
	public void refresh() {
		IResourceSelectionPage page = getCurrentPage();
		String error = page.checkError();
		if (error != null) {
			updateStatus(error);
			updateButtonState(false);
		} else {
			updateStatus(null);
			updateButtonState(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#resourcesOpened(java.lang.Object[])
	 */
	public void resourcesOpened(Object[] resources) {
		if (resources != null && resources.length > 0) {
			buttonPressed(IDialogConstants.OK_ID);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#resourcesSelected(java.lang.Object[])
	 */
	public void resourcesSelected(Object[] resources) {
		if (resources != null && resources.length ==1) {
			repaintPreviewCanvas(resources[0]);
		} else {
			repaintPreviewCanvas(null);
		}
		this.resources = resources;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#isResourceSelected(java.lang.Object)
	 */
	public boolean isResourceSelected(Object resource) {
		if (resources != null) {
			for (Object obj : resources) {
				if (obj.equals(resource)) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#updateLayout(org.eclipse.swt.widgets.Control)
	 */
	public void updateLayout(Control control) {
		initializeDialogUnits(control);
		if (control instanceof Button) {
			setButtonLayoutData((Button) control);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#getPages()
	 */
	public IResourceSelectionPage[] getPages() {
		return pages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#getDefaultResourceImage()
	 */
	public Image getDefaultResourceImage() {
		return NO_IMAGE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#createImage(java.lang.Object,
	 *      int, int, boolean)
	 */
	public Image createImage(Object data, int width, int height,
	    boolean keepAspectRatio) {
		for (IResourceSelectionPage page : pages) {
			Image image = page
			    .createImage(data, width, height, keepAspectRatio);
			if (image != null) {
				return image;
			}
		}
		return null;
	}

	/**
	 * Returns the initial location.
	 * 
	 * @param initialSize the initial size.
	 */
	protected Point getInitialLocation(Point initialSize) {
		Composite parent = this.getParentShell();

		Monitor monitor = this.getShell().getDisplay().getPrimaryMonitor();
		if (parent != null) {
			monitor = parent.getMonitor();
		}

		Rectangle monitorBounds = monitor.getClientArea();
		Point centerPoint;
		if (parent != null) {
			centerPoint = Geometry.centerPoint(parent.getBounds());
		} else {
			centerPoint = Geometry.centerPoint(monitorBounds);
		}

		return new Point(centerPoint.x - (initialSize.x / 2), Math.max(
		    monitorBounds.y, Math.min(centerPoint.y - (initialSize.y * 2 / 3),
		        monitorBounds.y + monitorBounds.height - initialSize.y)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#open()
	 */
	@Override
	public int open() {
		IPreferenceStore store = UIPreferences.getStore();
		int x = store
		    .getInt(UIPreferences.PREF_LAST_XPOS_IMAGESELECTION_DIALOG);
		int y = store
		    .getInt(UIPreferences.PREF_LAST_YPOS_IMAGESELECTION_DIALOG);

		Shell shell = this.getShell();
		if (shell == null || shell.isDisposed()) {
			shell = null;
			create();
			shell = this.getShell();
		}
		if (x == y && y == -9999)
			getInitialLocation(shell.getSize());
		else
			shell.setLocation(x, y);

		return super.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.dialog.IResourcePageManager#enablePreviewCheckBox(boolean)
	 */
	public void enablePreviewCheckBox(boolean enable) {
		if (showPreviewHoverCheckBox)
			previewHoverCheckBox.setEnabled(enable);
	}
}