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
package com.nokia.tools.theme.ui.propertysheet.tabbed;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.tabbed.SingleSelectionWidgetSection;

public class MediaFileSection extends SingleSelectionWidgetSection {
	private Text path;
	private Button browse;
	private String lastAppliedValue;

	@Override
	protected void doHandleEvent(Event e) {

		if (e.type == SWT.FocusOut) {
			String text = path.getText().trim();

			if (text != null && text.equals(lastAppliedValue))
				return;
			lastAppliedValue = text;

			if (StringUtils.isEmpty(text)) {
				// set to bean property
				applyAttributeSetting(IMediaConstants.PROPERTY_MEDIA_FILE, ""); //$NON-NLS-1$
			} else {
				// check valid & set
				try {
					File path = new File(text);
					if (FileUtils.isFileValidAndAccessible(path)) {
						IContentData data = getContent();
						// check that this is valid image type for this element
						ISkinnableEntityAdapter editingHelper = (ISkinnableEntityAdapter) data
								.getAdapter(ISkinnableEntityAdapter.class);
						IMediaFileAdapter adapter = (IMediaFileAdapter) data
								.getAdapter(IMediaFileAdapter.class);
						String fileExt = FileUtils.getExtension(path);
						if (fileExt.indexOf('.') == -1)
							fileExt = '.' + fileExt;
						for (String ext : editingHelper
								.getSupportedFileExtensions()) {
							if (ext.indexOf('.') == -1)
								ext = '.' + ext;
							if (ext.equalsIgnoreCase(fileExt)) {
								execute(adapter.getApplyMediaFileCommand(text));
								doRefresh();
								return;
							}
						}
					}
				} catch (Exception er) {
					er.printStackTrace();
				}

				// error - invalid file
				INamingAdapter nameFetch = (INamingAdapter) getContent()
						.getAdapter(INamingAdapter.class);
				MessageDialog.openError(path.getShell(),
						(nameFetch != null ? nameFetch.getName() + " - " : "")
								+ Messages.MediaFile_err_title,
						Messages.MediaFile_err_msg);
				// set actual correct value
				doRefresh();

			}
		}
	}

	@Override
	protected void doRefresh() {
		IMediaFileAdapter adapter = (IMediaFileAdapter) getContent()
				.getAdapter(IMediaFileAdapter.class);
		if (adapter == null) {
			return;
		}
		String s = adapter.getFileName(true);
		if (s != null) {
			if (new File(s).exists())
				s = new File(s).getName();

			path.setText(s);
		} else {
			path.setText(""); //$NON-NLS-1$
		}

		path.update();
		path.getParent().redraw();

		lastAppliedValue = path.getText();
	}

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite root = getWidgetFactory().createComposite(parent);

		GridLayout lay = new GridLayout(3, false);
		lay.marginWidth = lay.marginHeight = 0;
		lay.marginLeft = 10;
		root.setLayout(lay);

		Label label = getWidgetFactory().createLabel(root,
				Messages.MediaFile_file_label);
		GridData gd = new GridData();
		gd.widthHint = 78;
		label.setLayoutData(gd);

		path = getWidgetFactory().createText(root, ""); //$NON-NLS-1$
		path.addListener(SWT.Modify, this);
		path.setEditable(false);

		gd = new GridData();
		gd.widthHint = 195;
		path.setLayoutData(gd);

		browse = getWidgetFactory().createButton(root,
				Messages.MediaFile_browse_label, SWT.FLAT);
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browsePressed(e);
			}
		});
		parent.pack();
	}


	protected void browsePressed(SelectionEvent e) {
		IContentData data = getContent();
		if (data != null) {
			INamingAdapter nameFetch = (INamingAdapter) data
					.getAdapter(INamingAdapter.class);
			ISkinnableEntityAdapter editingHelper = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);
			FileDialog fileDialog = new FileDialog(Display.getCurrent()
					.getActiveShell(), SWT.OPEN);
			fileDialog.setText(nameFetch.getName()
					+ " - " + Messages.MediaFile_dialog_label); //$NON-NLS-1$

			fileDialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toOSString());
			String exts = ""; //$NON-NLS-1$
			for (String ext : editingHelper.getSupportedFileExtensions()) {
				exts += ("*" + ext + ";"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (0 != exts.length())
				exts = exts.substring(0, exts.length() - 1);

			fileDialog.setFilterExtensions(new String[] { exts.toLowerCase() });

			if (fileDialog.open() != null) {
				String separator = ""; //$NON-NLS-1$
				int length = fileDialog.getFilterPath().trim().length();
				if (length > 0
						&& fileDialog.getFilterPath().charAt(length - 1) != File.separatorChar)
					separator = File.separator;
				String path = new Path(fileDialog.getFilterPath() + separator
						+ fileDialog.getFileName()).toOSString();
				// set path to widget property
				this.path.setText(path);

				// fire update event
				Event evt = new Event();
				evt.type = SWT.FocusOut;
				evt.widget = e.widget;
				doHandleEvent(evt);
			}
		}
	}

}
