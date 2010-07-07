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
package com.nokia.tools.theme.s60.ui.animation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.layers.IEffectParameter;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.theme.s60.effects.EffectParameter;
import com.nokia.tools.theme.s60.ui.Activator;
import com.nokia.tools.theme.s60.ui.Messages;
import com.nokia.tools.theme.s60.ui.animation.presets.AnimationPresetControl2;
import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;

/**
 * Dialog for setting timing model properties
 * 
 */
public class AnimationPropertiesDialog extends BrandedTitleAreaDialog {

	public static final String HLP_CTX = "com.nokia.tools.theme.s60.ui.timingDialog_context"; //$NON-NLS-1$

	private TimingModelComposite animDialogComposite;

	private AnimationPresetControl2 animPresetComposite;

	private ILayerEffect effect;

	private IEffectParameter param;

	public AnimationPropertiesDialog(Shell parentShell, ILayerEffect effect,
			String paramName) {
		super(parentShell);
		setShellStyle(getShellStyle());
		this.effect = effect;
		this.param = effect.getParameter(paramName);
		((EffectParameter) param).updateAnimModels();
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getParent(),
				AnimationPropertiesDialog.HLP_CTX);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.marginHeight = 13;
		layout.marginWidth = 13;
		layout.verticalSpacing = 7;

		setTitle(Messages.animationTimeDialog_banner_title);
		setMessage(Messages.animationTimeDialog_banner_message);

		/* tabs */
		TabFolder tabs = new TabFolder(container, SWT.HORIZONTAL);
		// GridData gd = new GridData();
		// gd.horizontalIndent = 5;
		// gd.verticalIndent = 5;
		// tabs.setLayoutData(gd);

		TabItem item = new TabItem(tabs, SWT.NULL);
		animDialogComposite = new TimingModelComposite(tabs, SWT.NONE);
		item.setText(Messages.animationTimeDialog_timingTab);
		item.setControl(animDialogComposite);
		animDialogComposite
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		animDialogComposite.setData(effect, param);

		item = new TabItem(tabs, SWT.NULL);
		item.setText(Messages.animationTimeDialog_presetsTab);
		animPresetComposite = new AnimationPresetControl2(tabs, 0, effect,
				param.getName());
		item.setControl(animPresetComposite);

		Composite separatorCcontainer = new Composite(area, SWT.NONE);
		separatorCcontainer
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		separatorCcontainer.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;

		final Label separator = new Label(separatorCcontainer, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void okPressed() {
		animDialogComposite.commitChanges();
		super.okPressed();
	}

	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}

	@Override
	protected String getTitle() {
		return Messages.animationTimeDialog_title;
	}

	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
		return Activator
				.getImageDescriptor("icons/wizban/animation_timing.png");
	}

	public double[][] getResultData() {
		return animPresetComposite.getResultData();
	}

	public int getAnimTime() {
		return animPresetComposite.getAnimTime();
	}

}
