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

package com.nokia.tools.theme.s60.ui.propertysheet.tabbed;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.extension.PlatformExtensionManager;
import com.nokia.tools.screen.ui.propertysheet.tabbed.SingleSelectionWidgetSection;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.s60.general.PlatformSupportInfo;
import com.nokia.tools.theme.s60.general.PlatformSuppportInfoProvider;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIcon;
import com.nokia.tools.theme.s60.ui.Activator;
import com.nokia.tools.theme.ui.propertysheet.tabbed.PlatformTooltip;

public class PlatformSupportSection extends SingleSelectionWidgetSection {

	private static final Image ADDITIONAL_INFO_IMAGE = Activator
			.getImageDescriptor("icons/platform/info.PNG").createImage();
	
	private static final Image THIRD_PARTY_ICONS_IMAGE = Activator
			.getImageDescriptor("icons/platform/3PI.PNG").createImage();

	private static final String PLATFORM_TOOLTIP = "tooltip";

	private static Map<String, Image[]> images = new HashMap<String, Image[]>();

	private static Image constructImageWithInfoFlag(Image originalImage) {
		int imageWidth = originalImage.getImageData().width;
		int imageHeight = originalImage.getImageData().height;
		Image bitmap = new Image(null, imageWidth, imageHeight);
		GC gc = new GC(bitmap);
		try {
			gc.drawImage(originalImage, 0, 0);
			gc.drawImage(ADDITIONAL_INFO_IMAGE, imageWidth
					- ADDITIONAL_INFO_IMAGE.getImageData().width - 1, 1);
		} finally {
			gc.dispose();
		}

		return bitmap;
	}

	private Composite platformComposite;

	static {
		for (IPlatform platform : PlatformExtensionManager.getPlatforms()) {
			if (images.get(platform.getId()) == null) {
				Image platformImage = PlatformSuppportInfoProvider
						.getImage(platform);
				Image[] pImages = new Image[2];
				pImages[0] = platformImage;
				pImages[1] = constructImageWithInfoFlag(platformImage);
				images.put(platform.getId(), pImages);
			}
		}
	}
	
	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		this.parentComposite = parent;

	}

	private Composite mainComposite, parentComposite;

	private void updatePlatformSection() {
		Composite parent = this.parentComposite;
		if (platformComposite != null) {
			mainComposite.dispose();
		}
		mainComposite = getWidgetFactory().createFlatFormComposite(parent);
		int leftCoordinate = getStandardLabelWidth(mainComposite,
				new String[] { Messages.Label_SupportedPlatforms });

		CLabel label = getWidgetFactory().createCLabel(mainComposite,
				Messages.Label_SupportedPlatforms);
		platformComposite = getWidgetFactory().createComposite(mainComposite);

		RowLayout layout = new RowLayout();
		layout.wrap = false;
		layout.spacing = 0;
		layout.marginLeft = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginRight = 0;
		layout.marginWidth = 0;

		platformComposite.setLayout(layout);

		ThemeData data = (ThemeData) getContent();
		
		if(data.getSkinnableEntity() instanceof ThirdPartyIcon){
			Label imageLabel = getWidgetFactory().createLabel(
					platformComposite, null);
			imageLabel.setImage(THIRD_PARTY_ICONS_IMAGE);

			PlatformTooltip tooltip = (PlatformTooltip) imageLabel
					.getData(PLATFORM_TOOLTIP);
			if (tooltip == null) {
				tooltip = new PlatformTooltip();
				imageLabel.setData(PLATFORM_TOOLTIP, tooltip);
				tooltip.setControl(imageLabel);
			}
			tooltip.setMinimumSize(SWT.DEFAULT, SWT.DEFAULT);
			tooltip.setMinimumUnfocusedSize(SWT.DEFAULT,
					SWT.DEFAULT);
			tooltip.setMaximumUnfocusedSize(SWT.DEFAULT,
					SWT.DEFAULT);
			
			tooltip.setContent("Third Party Icon");
			tooltip.showGainFocusMessage(false);

		} else {
			for (IPlatform platform : PlatformExtensionManager.getPlatforms()) {

				String help = PlatformSupportInfo.getHelp(data.getId(),
						platform);
				boolean info = help != null && help.length() > 0;
				Image image = null;

				if (data.supportsPlatform(platform)) {
					if (info) {
						if (images.get(platform.getId()) != null) {
							image = images.get(platform.getId())[1];
						}
						if (image == null) {
							image = PlatformSuppportInfoProvider
									.getImage(platform);
							images.put(platform.getId(), new Image[] { image,
									constructImageWithInfoFlag(image) });
						}
					} else {
						if (images.get(platform.getId()) != null) {
							image = images.get(platform.getId())[0];
						}
						if (image == null) {
							Image pImage = PlatformSuppportInfoProvider
									.getImage(platform);
							image = constructImageWithInfoFlag(pImage);
							images.put(platform.getId(), new Image[] { pImage,
									image });
						}
					}
					Label imageLabel = getWidgetFactory().createLabel(
							platformComposite, null);
					imageLabel.setImage(image);

					PlatformTooltip tooltip = (PlatformTooltip) imageLabel
							.getData(PLATFORM_TOOLTIP);
					if (tooltip == null) {
						tooltip = new PlatformTooltip();
						imageLabel.setData(PLATFORM_TOOLTIP, tooltip);
						tooltip.setControl(imageLabel);
					}
					if (info) {
						tooltip.setMinimumSize(200, 100);
						tooltip.setMinimumUnfocusedSize(200, SWT.DEFAULT);
						tooltip.setMaximumUnfocusedSize(SWT.DEFAULT, 70);
					} else {
						tooltip.setMinimumSize(SWT.DEFAULT, SWT.DEFAULT);
						tooltip.setMinimumUnfocusedSize(SWT.DEFAULT,
								SWT.DEFAULT);
						tooltip.setMaximumUnfocusedSize(SWT.DEFAULT,
								SWT.DEFAULT);
					}

					tooltip.setContent(info ? help : platform.getName());
					tooltip.showGainFocusMessage(info);
				}
			}
		}



		FormData rData = new FormData();
		rData.left = new FormAttachment(0, leftCoordinate);
		rData.right = new FormAttachment(100, 
				- ITabbedPropertyConstants.HSPACE);
		platformComposite.setLayoutData(rData);

		rData = new FormData();
		rData.left = new FormAttachment(0, 0);
		rData.right = new FormAttachment(platformComposite,
				-ITabbedPropertyConstants.HSPACE);
		rData.top = new FormAttachment(platformComposite, 0, SWT.CENTER);
		label.setLayoutData(rData);
		parent.layout();
		parent.getParent().pack();
	}

	@Override
	protected void doHandleEvent(Event e) {
	}

	@Override
	protected void doRefresh() {
		updatePlatformSection();
	}
}
