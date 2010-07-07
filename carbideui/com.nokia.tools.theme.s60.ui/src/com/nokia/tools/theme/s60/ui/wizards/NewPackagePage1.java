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
package com.nokia.tools.theme.s60.ui.wizards;

import java.io.File;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.dialogs.FileResourceSelectionPage;
import com.nokia.tools.screen.ui.dialogs.ImageFileContentProvider;
import com.nokia.tools.screen.ui.dialogs.ThemeResourceSelectionDialog;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.wizards.AbstractNewPackagePage;
import com.nokia.tools.theme.s60.ui.Activator;
import com.nokia.tools.theme.ui.util.ThemeModelUtil;
import com.nokia.tools.ui.dialog.IResourceSelectionPage;

/**
 * This is the first page of the deployment package creation wizard. Page
 * includes basic settings and content selections for the theme.
 * 
 */
public class NewPackagePage1 extends AbstractNewPackagePage {

	private static final long MIN_UID = 0x10000000L;

	private static final long MAX_UID = 0xFFFFFFFFL;

	private static final long MID_UID = 0x7FFFFFFFL;

	protected Text txtName;

	private Text txtThemeUID, txtAuthor, txtVendor, txtCopyright;

	private Combo cboRelease;

	private Button btnUIDAutomatic, btnAllowCopy, btnDRM, btnColorRange1,
			btnColorRange2, btnDestBrowse;

	private Text txtLogo;

	private Label lblVendor, lblLogo;

	protected String selectedLogo = "";

	private Group grpColor, grpProtection;

	private String strSelThemeUID = "";

	private CheckboxTreeViewer taskViewer;

	private int TEXT_LIMIT = 255;

	private Image logoPreviewImageOrig, logoPreviewImage, expandImage,
			expandSelectionImage, collapseSelectionImage, collapseImage;

	private Composite vendorLogoComposite;

	private Label lblLogoPreview;

	Vector<Image> disposeImages = null;

	private Cursor arrowCursor, handCursor;

	private int SMALL_SIZE = 40;

	private int LARGE_SIZE = 80;

	private String vendorName, vendorLogo;

	public NewPackagePage1() {
		setDescription(WizardMessages.New_Package_Banner_Message_Page1);
	}

	public void createControl(Composite parent) {

		arrowCursor = new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW);
		handCursor = new Cursor(getShell().getDisplay(), SWT.CURSOR_HAND);
		disposeImages = new Vector<Image>();
		Composite mainComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		mainComposite.setLayout(layout);
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		layout.horizontalSpacing = 10;

		Composite platformComposite = new Composite(mainComposite, SWT.NULL);
		layout = new GridLayout();
		platformComposite.setLayout(layout);
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		platformComposite.setLayoutData(gd);

		Label lblRelease = new Label(platformComposite, SWT.NONE);
		lblRelease.setText(WizardMessages.New_Package_Release_Text);

		cboRelease = new Combo(platformComposite, SWT.READ_ONLY | SWT.BORDER
				| SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		cboRelease.setLayoutData(gd);
		cboRelease.setVisibleItemCount(10);
		cboRelease.setItems(getModelList());
		cboRelease.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				updateTree(taskViewer);
				pageChanged();
				updateVendorInformation();
				if (!getActiveModelName().equals(
						cboRelease.getItem(cboRelease.getSelectionIndex()))) {
					warn(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Different_Target_Platform_Warning_Message);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		cboRelease.addListener(SWT.Selection, this);

		Label separator = new Label(mainComposite, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);

		SashForm sash = new SashForm(mainComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		sash.setLayoutData(gd);

		Composite leftComposite = new Composite(sash, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		leftComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginRight = 4;
		layout.verticalSpacing = 7;
		leftComposite.setLayout(layout);

		Group grpInfo = new Group(leftComposite, SWT.NONE);
		grpInfo
				.setText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Group_Info_Title);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 300;
		gd.minimumHeight = 296;
		grpInfo.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpInfo.setLayout(layout);

		Label lblName = new Label(grpInfo, SWT.NONE);
		lblName
				.setText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Name_Text);

		txtName = new Text(grpInfo, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 170;
		gd.horizontalSpan = 2;
		txtName.setLayoutData(gd);
		txtName.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				pageChanged();
			}
		});
		txtName.addListener(SWT.Modify, this);
		txtName.setTextLimit(TEXT_LIMIT);

		Label lblThemeUID = new Label(grpInfo, SWT.NONE);
		lblThemeUID.setText(WizardMessages.New_Package_ThemeUID_Text);

		Composite themeUIDComposite = new Composite(grpInfo, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 2;
		themeUIDComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		themeUIDComposite.setLayout(layout);

		txtThemeUID = new Text(themeUIDComposite, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 80;
		txtThemeUID.setLayoutData(gd);
		txtThemeUID.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				strSelThemeUID = txtThemeUID.getText();
				pageChanged();
			}
		});
		txtThemeUID.addListener(SWT.Modify, this);
		txtThemeUID.setTextLimit(TEXT_LIMIT);

		btnUIDAutomatic = new Button(themeUIDComposite, SWT.CHECK);
		btnUIDAutomatic
				.setText(WizardMessages.New_Package_ThemeUIDAutomatic_Text);
		btnUIDAutomatic.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				txtThemeUID.setText("");
				txtThemeUID.setFocus();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});

		Label lblAuthor = new Label(grpInfo, SWT.NONE);
		lblAuthor.setText(WizardMessages.New_Package_Author_Text);

		txtAuthor = new Text(grpInfo, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 170;
		gd.horizontalSpan = 2;
		txtAuthor.setLayoutData(gd);
		txtAuthor.addListener(SWT.Modify, this);
		txtAuthor.setTextLimit(TEXT_LIMIT);

		lblVendor = new Label(grpInfo, SWT.NONE);
		lblVendor.setText(WizardMessages.New_Package_Vendor_Text);

		txtVendor = new Text(grpInfo, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 170;
		gd.horizontalSpan = 2;
		txtVendor.setLayoutData(gd);
		txtVendor.addListener(SWT.Modify, this);
		txtVendor.setTextLimit(TEXT_LIMIT);

		lblLogo = new Label(grpInfo, SWT.NONE);
		lblLogo.setText(WizardMessages.New_Package_VendorLogo_Text);

		txtLogo = new Text(grpInfo, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 170;
		gd.horizontalSpan = 2;
		txtLogo.setLayoutData(gd);
		txtLogo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				selectedLogo = txtLogo.getText();
				pageChanged();
				setPreviewImage(selectedLogo);
			}
		});
		txtLogo.addListener(SWT.Modify, this);

		// Dummy label to fill a column
		new Label(grpInfo, SWT.NONE);

		vendorLogoComposite = new Composite(grpInfo, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		vendorLogoComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		vendorLogoComposite.setLayout(layout);

		lblLogoPreview = new Label(vendorLogoComposite, SWT.BORDER);
		gd = new GridData();
		gd.verticalSpan = 2;
		gd.widthHint = SMALL_SIZE;
		gd.heightHint = SMALL_SIZE;
		lblLogoPreview.setLayoutData(gd);
		lblLogoPreview.addMouseListener(new MouseAdapter() {

			public void mouseDown(MouseEvent e) {
				togglePreviewSize(SMALL_SIZE, LARGE_SIZE);
			}
		});

		// Dummy label to fill a column
		Label l = new Label(vendorLogoComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		l.setLayoutData(gd);

		class OpenLogo implements SelectionListener {

			public void widgetSelected(SelectionEvent event) {
				FileResourceSelectionPage page = new FileResourceSelectionPage() {

					@Override
					public String checkError() {
						String str = super.checkError();
						if (null == str)
							return null;
						if (str
								.equals(com.nokia.tools.screen.ui.dialogs.WizardMessages.ResourceSelectionDialog_No_File_Error))
							return com.nokia.tools.screen.ui.dialogs.WizardMessages.ResourceSelectionDialog_No_JPGFile_Error;
						else if (str
								.equals(com.nokia.tools.screen.ui.dialogs.WizardMessages.ResourceSelectionDialog_No_images_Error))
							return com.nokia.tools.screen.ui.dialogs.WizardMessages.ResourceSelectionDialog_No_JpgImages_Error;
						return str;
					}
				};
				page.setContentProvider(new ImageFileContentProvider() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * com.nokia.tools.screen.ui.dialogs.ImageFileContentProvider
					 * #acceptFile(java.io.File)
					 */
					@Override
					protected boolean acceptFile(File file) {
						return file.isFile()
								&& file.canRead()
								&& (file.getName().toLowerCase().endsWith(
										".jpg") || file.getName().toLowerCase()
										.endsWith(".jpeg"));
					}
				});

				ThemeResourceSelectionDialog<String> rd = new ThemeResourceSelectionDialog<String>(
						getShell(),
						new IResourceSelectionPage[] { page },
						com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_ResourceSelectionDialog_Banner_Message);
				if (txtLogo.getText().trim().length() > 0
						&& new File(txtLogo.getText().trim()).exists()) {
					rd.setResources(new Object[] { new File(txtLogo.getText()
							.trim()) });
				} else
					rd.create();
				if (Dialog.OK == rd.open()) {
					Object[] resources = rd.getResources();
					if (resources != null && resources.length > 0) {
						txtLogo.setText(new File(resources[0].toString())
								.getAbsolutePath());
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}

		btnDestBrowse = new Button(vendorLogoComposite, SWT.NONE);
		initializeDialogUnits(btnDestBrowse);
		setButtonLayoutData(btnDestBrowse);
		btnDestBrowse
				.setText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_btnDestSelect_Text);
		btnDestBrowse.addSelectionListener(new OpenLogo());

		// Dummy label to fill a column
		new Label(vendorLogoComposite, SWT.NONE);

		Label lblCopyright = new Label(grpInfo, SWT.NONE);
		lblCopyright
				.setText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Copyright_Text);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		lblCopyright.setLayoutData(gd);

		txtCopyright = new Text(grpInfo, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL);
		gd.widthHint = 170;
		gd.heightHint = 34;
		gd.horizontalSpan = 2;
		gd.minimumHeight = 20;
		txtCopyright.setLayoutData(gd);
		txtCopyright.addListener(SWT.Modify, this);
		txtCopyright.setTextLimit(TEXT_LIMIT);

		Composite groupComposite = new Composite(leftComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		groupComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 10;
		groupComposite.setLayout(layout);

		grpProtection = new Group(groupComposite, SWT.NONE);
		grpProtection
				.setText(WizardMessages.New_Package_Group_Protection_Title);
		gd = new GridData();
		grpProtection.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpProtection.setLayout(layout);

		btnAllowCopy = new Button(grpProtection, SWT.CHECK);
		btnAllowCopy.setText(WizardMessages.New_Package_chkAllowCopying_Text);
		btnAllowCopy.addListener(SWT.Selection, this);

		btnDRM = new Button(grpProtection, SWT.CHECK);
		btnDRM.setText(WizardMessages.New_Package_chkDRM_Text);
		btnDRM.addListener(SWT.Selection, this);

		grpColor = new Group(groupComposite, SWT.NONE);
		grpColor.setText(WizardMessages.New_Package_Group_Color_Title);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		grpColor.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpColor.setLayout(layout);
		grpColor.setVisible(false);

		btnColorRange1 = new Button(grpColor, SWT.RADIO);
		btnColorRange1
				.setText(WizardMessages.New_Package_ColorRange_Radio1_Text);
		btnColorRange1.addListener(SWT.Selection, this);

		btnColorRange2 = new Button(grpColor, SWT.RADIO | SWT.LEFT);
		btnColorRange2
				.setText(WizardMessages.New_Package_ColorRange_Radio2_Text);
		btnColorRange2.addListener(SWT.Selection, this);

		Composite rightComposite = new Composite(sash, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		rightComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 4;
		layout.verticalSpacing = 6;
		rightComposite.setLayout(layout);

		Group grpContent = new Group(rightComposite, SWT.NONE);
		grpContent
				.setText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Group_Content_Title);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 295;
		gd.minimumHeight = 291;
		grpContent.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpContent.setLayout(layout);

		Composite contentComposite = new Composite(grpContent, SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		contentComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		contentComposite.setLayout(layout);

		Composite treeComposite = new Composite(contentComposite, SWT.NULL);
		FillLayout layout2 = new FillLayout();
		treeComposite.setLayout(layout2);
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 225;
		treeComposite.setLayoutData(gd);

		taskViewer = createTaskViewer(treeComposite);

		for (IContent content : getContents()) {
			content.setAttribute(ContentAttribute.PACKAGING.name(), "true");
		}

		final Label sep = new Label(contentComposite, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		sep.setLayoutData(gd);

		Composite buttonComposite = new Composite(contentComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = SWT.RIGHT;
		buttonComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		buttonComposite.setLayout(layout);

		Button expandButton = new Button(buttonComposite, SWT.FLAT | SWT.PUSH);
		expandButton
				.setToolTipText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Expand_Tooltip);
		expandImage = Activator.getImageDescriptor("icons/expand_all.png")
				.createImage();
		expandButton.setImage(expandImage);
		expandButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				int curLevel = expansionLevel();
				taskViewer.expandToLevel(curLevel + 1);
				updateTree(taskViewer);
			}
		});

		Button expandSelectionButton = new Button(buttonComposite, SWT.FLAT
				| SWT.PUSH);
		expandSelectionButton
				.setToolTipText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Expand_Selection_Tooltip);
		expandSelectionImage = Activator.getImageDescriptor("icons/expand.png")
				.createImage();
		expandSelectionButton.setImage(expandSelectionImage);
		expandSelectionButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				TreeItem[] item = taskViewer.getTree().getSelection();
				if (item.length > 0) {
					TreePath p = getTreePathFromTreeItem(item[item.length - 1]);
					int curLevel = expansionLevelForSelection(p);
					taskViewer.expandToLevel(p, curLevel + 1);
					updateTree(taskViewer);
				}
			}
		});

		Button collapseSelectionButton = new Button(buttonComposite, SWT.FLAT
				| SWT.PUSH);
		collapseSelectionButton
				.setToolTipText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Collapse_Selection_Tooltip);
		collapseSelectionImage = Activator.getImageDescriptor(
				"icons/collapse.png").createImage();
		collapseSelectionButton.setImage(collapseSelectionImage);
		collapseSelectionButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				TreeItem[] item = taskViewer.getTree().getSelection();
				if (item.length > 0) {
					TreePath p = getTreePathFromTreeItem(item[item.length - 1]);
					int curLevel = expansionLevelForSelection(p);
					int segments = p.getSegmentCount();
					taskViewer.collapseToLevel(p, -1);
					if (curLevel > 0)
						if (curLevel >= segments)
							taskViewer.expandToLevel(p, curLevel - segments);
						else
							taskViewer.expandToLevel(p, curLevel - 1);
					else
						taskViewer.expandToLevel(p, 0);
					updateTree(taskViewer);
				}
			}
		});

		Button collapseButton = new Button(buttonComposite, SWT.FLAT | SWT.PUSH);
		collapseButton
				.setToolTipText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Collapse_Tooltip);
		collapseImage = Activator.getImageDescriptor("icons/collapse_all.png")
				.createImage();
		collapseButton.setImage(collapseImage);
		collapseButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				int curLevel = expansionLevel();
				taskViewer.collapseAll();
				if (curLevel > 0)
					taskViewer.expandToLevel(curLevel - 1);
				else
					taskViewer.expandToLevel(0);
				updateTree(taskViewer);
			}
		});

		sash.setWeights(new int[] { 55, 45 });

		fillAttributes();
		updateTree(taskViewer);

		if (txtThemeUID.getText().trim().length() > 0) {
			btnUIDAutomatic.setSelection(false);
		} else {
			btnUIDAutomatic.setSelection(true);
		}

		updateVendorInformation();

		pageChanged();
		updateModel();
		setControl(mainComposite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getShell(),
				getHelpContextId());
	}

	private void updateVendorInformation() {
		if (null == vendorName)
			vendorName = txtVendor.getText();
		if (!txtVendor.getText().equals(""))
			vendorName = txtVendor.getText();
		if (null == vendorLogo)
			vendorLogo = txtLogo.getText();
		if (!txtLogo.getText().equals(""))
			vendorLogo = txtLogo.getText();

		lblVendor.setEnabled(true);
		txtVendor.setText(vendorName);
		txtVendor.setEnabled(true);
		lblLogo.setEnabled(true);
		txtLogo.setText(vendorLogo);
		txtLogo.setEnabled(true);
		btnDestBrowse.setEnabled(true);
		lblLogoPreview.setImage(logoPreviewImage);
		lblLogoPreview.setToolTipText("");
	}

	private int expansionLevel() {
		int count = 0;
		for (TreePath p : taskViewer.getExpandedTreePaths()) {
			int segmentCount = p.getSegmentCount();
			if (segmentCount > count)
				count = segmentCount;
		}
		return count + 1;
	}

	private int expansionLevelForSelection(TreePath path) {
		int count = 0;

		for (TreePath allExpanded : taskViewer.getExpandedTreePaths()) {
			for (int i = 0; i < allExpanded.getSegmentCount(); i++) {
				if (allExpanded.getSegment(i).equals(path.getLastSegment())) {
					int segmentCount = allExpanded.getSegmentCount();
					if (segmentCount > count)
						count = segmentCount;
				}
			}
		}
		return count;
	}

	private TreePath getTreePathFromTreeItem(TreeItem item) {
		LinkedList<Object> segments = new LinkedList<Object>();
		while (item != null) {
			Object segment = item.getData();
			Assert.isNotNull(segment);
			segments.addFirst(segment);
			item = item.getParentItem();
		}
		return new TreePath(segments.toArray());
	}

	/**
	 * Returns URL for information web page
	 */
	

	List<IThemeModelDescriptor> modelDescList;

	private String[] getModelList() {
		modelDescList = ThemeModelUtil.getAllThemeModelDescriptor();
		String[] modelList = new String[modelDescList.size()];
		int i = 0;
		for (IThemeModelDescriptor themeModelDescriptor : modelDescList) {
			modelList[i++] = getDisplayName(themeModelDescriptor.getName());
		}
		return modelList;
	}

	private String getDisplayName(String modelName) {
		modelName = modelName.replace("BaseGraphics", "");
		modelName = modelName.trim();
		modelName = modelName.replace("FP", "Feature Pack ");
		return modelName;
	}

	private String getActiveModelName() {
		final IContent content = getPrimaryContent();
		if (content != null) {
			String modelId = (String) content
					.getAttribute(ContentAttribute.MODEL.name());
			IThemeModelDescriptor themeModelDescriptor = ThemePlatform
					.getThemeModelDescriptorById(modelId);
			return getDisplayName(themeModelDescriptor.getName());
		}
		return null;
	}

	/**
	 * return the platform that is selected currently for packaging target to be
	 * accessed from other pages
	 * 
	 * @return
	 */
	protected IPlatform[] getSelectedPlatforms() {
		if (cboRelease.getSelectionIndex() < 0)
			return new IPlatform[0];
		return new IPlatform[] { getPlatform(cboRelease.getText()) };
	}

	private String getPrimaryModelID() {
		return modelDescList.get(cboRelease.getSelectionIndex()).getId();
	}

	private String getSecondaryModelId() {
		String primaryModelId = getPrimaryModelID();
		Object platformId = ModelPlatformMapping.getInstance().get(
				primaryModelId);
		return (platformId == null) ? null : platformId.toString();
	}

	/**
	 * by the platform name (displayed name is different from platform name)
	 * returns the platform
	 * 
	 * @param platformName
	 */
	private IPlatform getPlatform(String platformName) {
		String platformId = modelDescList.get(cboRelease.getSelectionIndex())
				.getThemeDescriptor().getDefaultPlatform().getId();
		return DevicePlatform.getPlatformById(platformId);
	}

	/**
	 * Fill attributes in the wizard page
	 */
	private void fillAttributes() {
		String[] attributes = getAttributes();

		if (attributes[0] != null)
			txtName.setText(attributes[0]);

		if (attributes[1] != null)
			txtThemeUID.setText(attributes[1]);

		if (attributes[2] != null)
			txtAuthor.setText(attributes[2]);

		if (attributes[3] != null)
			txtVendor.setText(attributes[3]);

		if (attributes[4] != null)
			txtLogo.setText(attributes[4]);

		if (attributes[5] != null)
			txtCopyright.setText(attributes[5]);

		if (attributes[6].equalsIgnoreCase("false"))
			btnAllowCopy.setSelection(false);
		else
			btnAllowCopy.setSelection(true);

		String[] releases = cboRelease.getItems();
		if (attributes[7] != null) {
			String activeModelName = getActiveModelName();
			for (String release : releases) {
				if (activeModelName.equals(release))
					cboRelease.select(cboRelease.indexOf(activeModelName));
			}
		}
		if (cboRelease.getSelectionIndex() < 0) {
			cboRelease.select(releases.length - 1);
		}

		if (attributes[8] != null) {
			if (!attributes[8].equalsIgnoreCase("true"))
				btnColorRange1.setSelection(true);
			else
				btnColorRange2.setSelection(true);
		} else
			btnColorRange1.setSelection(true);

		if (attributes[9] != null) {
			if (attributes[9].equalsIgnoreCase("true"))
				btnDRM.setSelection(true);
			else
				btnDRM.setSelection(false);
		}
	}

	/**
	 * Gets attributes from current theme
	 */
	private String[] getAttributes() {
		String[] attributes = new String[10];
		IContent theme = getPrimaryContent();
		attributes[0] = (String) theme
				.getAttribute(ContentAttribute.APPLICATION_NAME.name());
		attributes[1] = (String) theme.getAttribute(ContentAttribute.APP_UID
				.name());
		attributes[2] = (String) theme.getAttribute(ContentAttribute.AUTHOR
				.name());
		attributes[3] = (String) theme
				.getAttribute(ContentAttribute.VENDOR_NAME.name());
		attributes[4] = (String) theme
				.getAttribute(ContentAttribute.VENDOR_LOGO.name());
		attributes[5] = (String) theme.getAttribute(ContentAttribute.COPYRIGHT
				.name());
		attributes[6] = (String) theme
				.getAttribute(ContentAttribute.ALLOW_COPYING.name());
		attributes[7] = ((IPlatform) theme
				.getAttribute(ContentAttribute.PLATFORM.name())).getName();
		attributes[8] = (String) theme
				.getAttribute(ContentAttribute.BITS_24PIXEL_SUPPORT.name());
		attributes[9] = (String) theme
				.getAttribute(ContentAttribute.DRM_PROTECTION.name());
		return attributes;
	}

	private void setPreviewImage(String source) {
		if (source != null) {
			try {
				logoPreviewImageOrig = new Image(getShell().getDisplay(),
						source);
			} catch (Exception e) {
				logoPreviewImageOrig = null;
			}
			if (logoPreviewImageOrig != null) {
				GridData gd = (GridData) lblLogoPreview.getLayoutData();
				togglePreviewSize(gd.heightHint, gd.heightHint);
				disposeImages.addElement(logoPreviewImage);
				return;
			}
		}
		lblLogoPreview.setImage(null);
		updatePreview(SMALL_SIZE);
	}

	private void togglePreviewSize(int a, int b) {
		if (logoPreviewImageOrig == null)
			return;
		GridData gd = (GridData) lblLogoPreview.getLayoutData();
		if (gd.heightHint == a)
			updatePreview(b);
		else
			updatePreview(a);
	}

	private void updatePreview(int newH) {
		int newW;
		if (logoPreviewImageOrig == null) {
			newW = newH;
			lblLogoPreview.setCursor(arrowCursor);
			lblLogoPreview.setToolTipText("");
		} else {
			int w = logoPreviewImageOrig.getBounds().width;
			int h = logoPreviewImageOrig.getBounds().height;
			newW = (int) ((float) newH / h * w);
			logoPreviewImage = new Image(null, logoPreviewImageOrig
					.getImageData().scaledTo(newW, newH));
			lblLogoPreview.setImage(logoPreviewImage);
			lblLogoPreview.setCursor(handCursor);
			if (newH > SMALL_SIZE)
				lblLogoPreview
						.setToolTipText(WizardMessages.New_Package_VendorLogo_Tooltip_Small);
			else
				lblLogoPreview
						.setToolTipText(WizardMessages.New_Package_VendorLogo_Tooltip_Big);
		}
		GridData gd = (GridData) lblLogoPreview.getLayoutData();
		gd.widthHint = newW;
		gd.heightHint = newH;
		lblLogoPreview.setLayoutData(gd);

		gd = (GridData) vendorLogoComposite.getLayoutData();
		Point p = vendorLogoComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT,
				true);
		gd.widthHint = p.x;
		gd.heightHint = p.y;
		vendorLogoComposite.setLayoutData(gd);
		vendorLogoComposite.layout();
		vendorLogoComposite.getParent().layout();
	}

	public boolean canFlipToNextPage() {
		return isPageComplete() && getNextPage() != null;
	}

	/**
	 * Handles enabled/disabled state logic of components
	 */
	private void updateStates() {
		TreeItem themeItem = getThemeItem();
		if (themeItem != null && themeItem.getChecked()
				|| taskViewer.getCheckedElements().length == 0) {
			grpColor.setEnabled(true);
			btnColorRange1.setEnabled(true);
			btnColorRange2.setEnabled(true);
			grpProtection.setEnabled(true);
			btnAllowCopy.setEnabled(true);
			btnDRM.setEnabled(true);
		} else {
			grpColor.setEnabled(false);
			btnColorRange1.setEnabled(false);
			btnColorRange2.setEnabled(false);
			grpProtection.setEnabled(false);
			btnAllowCopy.setEnabled(false);
			btnDRM.setEnabled(false);
		}

		if (btnUIDAutomatic.getSelection() == true) {
			txtThemeUID.setEnabled(false);
		} else {
			txtThemeUID.setEnabled(true);
		}
	}

	/**
	 * Validates the input and offers error messages
	 */
	protected void pageChanged() {
		updateStates();

		warn(null);
		error(null);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus nameStatus = workspace.validateName(txtName.getText().trim(),
				IResource.PROJECT);

		if (txtName.getText().trim().length() == 0) {
			error(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Name_Not_Exist_Error);
			return;
		}

		if (!nameStatus.isOK()) {
			error(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Wrong_Char_Error);
			return;
		}

		if ((strSelThemeUID = strSelThemeUID.trim()).length() > 0) {
			if (!strSelThemeUID.startsWith("0x")) {
				error(WizardMessages.New_Package_ThemeUID_Content_Error);
				return;
			}
			long value = 0;
			try {
				value = Long.parseLong(strSelThemeUID.substring(2), 16);
			} catch (Exception e) {
				error(WizardMessages.New_Package_ThemeUID_Content_Error);
				return;
			}

			if (value < MIN_UID || value > MAX_UID) {
				error(null);
				error(MessageFormat.format(
						WizardMessages.New_Package_ThemeUID_Out_Range_Error,
						new Object[] { Integer.toHexString((int) MIN_UID),
								Integer.toHexString((int) MAX_UID) }));
				return;
			}
			if (value <= MID_UID) {
				warn(null);
				warn(MessageFormat
						.format(
								WizardMessages.New_Package_ThemeUID_Protected_Range_Warning,
								new Object[] {
										Integer.toHexString((int) MIN_UID),
										Integer.toHexString((int) MID_UID) }));
			}
		} else {
			if (!btnUIDAutomatic.getSelection()) {
				error(WizardMessages.New_Package_ThemeUID_Not_Exist_Error);
				return;
			}
		}

		if (selectedLogo.trim().length() > 0) {
			Path path = new Path(selectedLogo);
			String ext = path.getFileExtension();

			if (ext == null || (!ext.equalsIgnoreCase("jpg"))) {
				error(WizardMessages.New_Package_VendorLogo_Extension_Error);
				return;
			}

			if (!FileUtils.isFileValidAndAccessible(path)) {
				error(WizardMessages.New_Package_VendorLogo_Not_Exist_Error);
				return;
			}
		}

		if (taskViewer.getCheckedElements().length == 0) {
			error(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_No_Content_Checked_Error);
			return;
		}

		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		if (arrowCursor != null) {
			arrowCursor.dispose();
		}
		if (handCursor != null) {
			handCursor.dispose();
		}
		for (Image image : disposeImages) {
			image.dispose();
		}
		if (logoPreviewImageOrig != null) {
			logoPreviewImageOrig.dispose();
		}
		if (logoPreviewImage != null) {
			logoPreviewImage.dispose();
		}
		if (expandImage != null) {
			expandImage.dispose();
		}
		if (expandSelectionImage != null) {
			expandSelectionImage.dispose();
		}
		if (collapseSelectionImage != null) {
			collapseSelectionImage.dispose();
		}
		if (collapseImage != null) {
			collapseImage.dispose();
		}
		super.dispose();
	}

	/**
	 * Updates error messages in the banner area
	 */
	private void error(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/**
	 * Updates warning message in the banner area
	 * 
	 * @param message
	 */
	private void warn(String message) {
		setMessage(message, WARNING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.AbstractNewPackagePage#updateModel()
	 */
	@Override
	protected void updateModel() {
		if (!isPageComplete())
			return;
		IContent theme = getPrimaryContent();
		PackagingContext context = getContext();
		if (txtName.getText().trim().length() > 0) {
			theme.setAttribute(ContentAttribute.APPLICATION_NAME.name(),
					txtName.getText().trim());
			context.setAttribute(PackagingAttribute.themeName.name(), txtName
					.getText().trim());
		}
		theme.setAttribute(ContentAttribute.APP_UID.name(), txtThemeUID
				.getText().trim().length() > 0 ? txtThemeUID.getText().trim()
				: null);
		theme.setAttribute(ContentAttribute.AUTHOR.name(), txtAuthor.getText());
		theme.setAttribute(ContentAttribute.VENDOR_NAME.name(), txtVendor
				.getText());
		theme.setAttribute(ContentAttribute.VENDOR_LOGO.name(), txtLogo
				.getText());
		theme.setAttribute(ContentAttribute.COPYRIGHT.name(), txtCopyright
				.getText());
		theme.setAttribute(ContentAttribute.ALLOW_COPYING.name(), new Boolean(
				btnAllowCopy.getSelection()).toString());
		theme.setAttribute(ContentAttribute.DRM_PROTECTION.name(), new Boolean(
				btnDRM.getSelection()).toString());
		context.setAttribute(PackagingAttribute.themeDRM.name(), new Boolean(
				btnDRM.getSelection()).toString());
		theme.setAttribute(ContentAttribute.BITS_24PIXEL_SUPPORT.name(),
				new Boolean(btnColorRange2.getSelection()).toString());
		context.setAttribute(PackagingAttribute.vendor.name(), txtVendor
				.getText().trim());
		context.setAttribute(PackagingAttribute.vendorIcon.name(), txtLogo
				.getText().trim());
		context.setAttribute(PackagingAttribute.primaryModelId.name(),
				getPrimaryModelID());
		context.setAttribute(PackagingAttribute.secondaryModelId.name(),
				(getSecondaryModelId() == null) ? getPrimaryModelID()
						: getSecondaryModelId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#performHelp()
	 */
	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp(
				getHelpContextId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.AbstractNewPackagePage#performFinish()
	 */
	public boolean performFinish() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.s60.wizards.AbstractNewPackagePage#handleEvent(org.eclipse
	 * .swt.widgets.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		if (event.widget == txtName) {
			TreeItem themeItem = getThemeItem();
			taskViewer.update(themeItem.getData(), null);
		}
	}

	private TreeItem getThemeItem() {
		for (TreeItem item : taskViewer.getTree().getItems()) {
			IContent content = (IContent) item.getData();
			if (ScreenUtil.isPrimaryContent(content)) {
				return item;
			}
		}
		return null;
	}
}