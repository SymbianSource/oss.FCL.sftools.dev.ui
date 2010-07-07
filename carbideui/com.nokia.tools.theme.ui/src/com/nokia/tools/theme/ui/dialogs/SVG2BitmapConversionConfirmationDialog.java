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
package com.nokia.tools.theme.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.theme.ui.Activator;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

public class SVG2BitmapConversionConfirmationDialog extends TitleAreaDialog {

	public static final String CONVERSION_CONFIRMATION_DIALOG_CONTEXT = "com.nokia.tools.s60.ide.conversionConfirmation_context"; //$NON-NLS-1$

	private Rectangle bounds;

	// text for size input
	private Spinner width, height;

	private int w, h;

	private boolean maskPreserve;

	private boolean supportMask, rc;

	private List<Resource> forDispose = new ArrayList<Resource>();

	public SVG2BitmapConversionConfirmationDialog(Shell shell,
			Rectangle bounds, boolean supportsMask, boolean supportSoftmask,
			boolean rememberCheck) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.bounds = bounds;
		this.supportMask = supportsMask;
		this.rc = rememberCheck;
	}

	public int getSelectedWidth() {
		return w;
	}

	public int getSelectedHeight() {
		return h;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#
	 *      createDialogArea(org.eclipse.swt.widgets.Composite) Here we fill the
	 *      center area of the dialog
	 */
	protected Control createDialogArea(Composite parent) {
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(
						parent.getParent(),
						SVG2BitmapConversionConfirmationDialog.CONVERSION_CONFIRMATION_DIALOG_CONTEXT);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.marginHeight = 13;
		layout.marginWidth = 13;
		layout.verticalSpacing = 7;

		setTitle(Messages.SVGConversionConfirmDialog_banner_title);
		setMessage(Messages.SVGConversionConfirmDialog_banner_message);

		IBrandingManager manager = BrandingExtensionManager
				.getBrandingManager();
		if (manager != null) {
			Image titleAreaImage = manager.getBannerImageDescriptor(
					Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
							"icons/wizban/convert_edit_bmp.png")).createImage();
			setTitleImage(titleAreaImage);
			forDispose.add(titleAreaImage);
		}

		Composite spinners = new Composite(container, SWT.NONE);
		layout = new GridLayout(5, false);
		spinners.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Label l = new Label(spinners, SWT.NONE);
		l.setText(Messages.SVGConversionConfirmDialog_text2);

		width = new Spinner(spinners, SWT.BORDER);
		width.setMinimum(1);
		width.setMaximum(999);
		GridData gd = new GridData();
		gd.widthHint = 30;
		width.setLayoutData(gd);
		width.setSelection(bounds.width);

		Label x = new Label(spinners, SWT.NONE);
		x.setText(" x ");

		height = new Spinner(spinners, SWT.BORDER);
		height.setMinimum(1);
		height.setMaximum(999);
		gd = new GridData();
		gd.widthHint = 30;
		height.setLayoutData(gd);
		height.setSelection(bounds.height);

		x = new Label(spinners, SWT.NONE);
		x.setText(Messages.SVGConversionConfirmDialog_pixelsLabel);

		// preserve mask check
		final Button maskCheck = new Button(container, SWT.CHECK);
		maskCheck.setText(Messages.SVGConversionConfirmDialog_maskCheckLabel);
		maskCheck.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean state = maskCheck.getSelection();
				maskPreserve = state;
				IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
						.getPreferenceStore();
				iPreferenceStore.setValue(
						IMediaConstants.PREF_SVG_CONVERSION_PRESERVE_MASK,
						state + "");
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		maskCheck
				.setToolTipText(Messages.SVGConversionConfirmDialog_maskCheckTooltip);
		// default
		IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
				.getPreferenceStore();
		if (StringUtils.isEmpty(iPreferenceStore
				.getString(IMediaConstants.PREF_SVG_CONVERSION_PRESERVE_MASK))) {
			maskCheck.setSelection(true);
		} else {
			maskCheck
					.setSelection(iPreferenceStore
							.getBoolean(IMediaConstants.PREF_SVG_CONVERSION_PRESERVE_MASK));
		}

		if (!supportMask) {
			maskCheck.setEnabled(false);
			maskCheck.setSelection(false);
		}

		maskPreserve = maskCheck.getSelection();

		// don't ask again check
		final Button check = new Button(container, SWT.CHECK);
		check.setText(Messages.SVGConversionConfirmDialog_dontAskAgain);
		check.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

				boolean state = check.getSelection();
				if (state) {
					width.setEnabled(false);
					width.setSelection(bounds.width);
					height.setEnabled(false);
					height.setSelection(bounds.width);
				} else {
					width.setEnabled(true);
					height.setEnabled(true);
				}
				IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
						.getPreferenceStore();
				iPreferenceStore.setValue(
						IMediaConstants.PREF_SILENT_SVG_CONVERSION, state);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		check.setToolTipText(Messages.SVGConversionConfirmDialog_checkTooltip);
		check.setEnabled(rc);
		
		Composite container2 = new Composite(area, SWT.NONE);
		container2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		container2.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;

		final Label separator = new Label(container2, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return area;
	}

	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		w = width.getSelection();
		h = height.getSelection();
		super.okPressed();
	}

	public boolean isMaskPreserve() {
		return maskPreserve;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.SVGConversionConfirmDialog_title);
		IBrandingManager branding = BrandingExtensionManager
				.getBrandingManager();
		if (branding != null) {
			Image windowImage = branding.getIconImageDescriptor().createImage();
			newShell.setImage(windowImage);
			forDispose.add(windowImage);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close() {
		for (Resource x : forDispose) {
			x.dispose();
		}
		return super.close();
	}
}
