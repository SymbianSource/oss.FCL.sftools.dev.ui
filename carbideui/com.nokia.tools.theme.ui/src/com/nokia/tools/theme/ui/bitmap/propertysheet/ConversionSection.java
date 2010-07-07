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
package com.nokia.tools.theme.ui.bitmap.propertysheet;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.tabbed.MultipleSelectionWidgetSection;
import com.nokia.tools.theme.ui.Activator;
import com.nokia.tools.theme.ui.dialogs.SVG2BitmapConversionConfirmationDialog;

public class ConversionSection extends MultipleSelectionWidgetSection {

	public static final Image WARNING_ICON;

	public static final Font WARNING_FONT;

	public static final Color WARNING_BACKGROUD = ColorConstants.yellow;

	static {
		WARNING_ICON = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
				"/icons/warning.gif") //$NON-NLS-1$
				.createImage();

		FontData fd = JFaceResources.getDialogFont().getFontData()[0];
		fd.setHeight(7);
		WARNING_FONT = new Font(null, fd);
	}

	protected Composite warningComposite;

	protected FormText warningText;

	@Override
	protected EObject doSetInput(IWorkbenchPart part, IScreenElement adapter) {
		EObject bitmapProperties = super.doSetInput(part, adapter);

		if (bitmapProperties == null) {
			return null;
		}

		return createEditObject(EditingUtil.getFeatureValue(bitmapProperties,
				"colorize")); //$NON-NLS-1$
	}

	@Override
	public void createControls(final Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);

		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);

		warningComposite = new Composite(composite, SWT.NONE);
		warningComposite.setLayout(new FillLayout());
		warningComposite.setVisible(false);

		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		data.height = 0;
		warningComposite.setLayoutData(data);

		composite = getWidgetFactory()
				.createFlatFormComposite(warningComposite);

		composite.setBackground(WARNING_BACKGROUD);

		warningText = getWidgetFactory().createFormText(composite, true);
		warningText.setImage("image", WARNING_ICON); //$NON-NLS-1$
		warningText.setBackground(WARNING_BACKGROUD);
		warningText.setFont(WARNING_FONT);
		warningText.setText(Messages.Conversion_Section_banner_message, true,
				false);
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		warningText.setLayoutData(data);
		warningText.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				if ("conversion_dialog" //$NON-NLS-1$
						.equalsIgnoreCase(e.getHref().toString())
						&& getFirstElement() != null) {
					ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) getFirstElement()
							.getData()
							.getAdapter(ISkinnableEntityAdapter.class);
					if (ska != null) {
						org.eclipse.draw2d.geometry.Rectangle rect = new org.eclipse.draw2d.geometry.Rectangle(
								0, 0, 100, 100);

						IImageAdapter ima = (IImageAdapter) getFirstElement()
								.getData().getAdapter(IImageAdapter.class);
						if (ima != null) {
							IImage img = ima.getImage();
							rect.width = img.getWidth();
							rect.height = img.getHeight();
						}

						if (ska.getAttributes().get(
								BitmapProperties.BITMAP_HEIGHT) != null) {
							rect.height = Integer.parseInt((String) ska
									.getAttributes().get(
											BitmapProperties.BITMAP_HEIGHT));
						}
						if (ska.getAttributes().get(
								BitmapProperties.BITMAP_WIDTH) != null) {
							rect.width = Integer.parseInt((String) ska
									.getAttributes().get(
											BitmapProperties.BITMAP_WIDTH));
						}

						/**
						 * because we don't see confirmation dialog class, must
						 * load dynamic
						 */
						SVG2BitmapConversionConfirmationDialog dialog = new SVG2BitmapConversionConfirmationDialog(
								getPart().getSite().getShell(), rect, false,
								false, false);
						if (Dialog.OK == dialog.open()) {
							// set properties to the sk.ent
							HashMap<Object, Object> map = new HashMap<Object, Object>();

							Integer height = dialog.getSelectedHeight();
							Integer width = dialog.getSelectedWidth();

							map.put(BitmapProperties.BITMAP_HEIGHT, height
									.toString());
							map.put(BitmapProperties.BITMAP_WIDTH, width
									.toString());
							ska.setAttributes(map);
						}
					}
				}
			}
		});

		Composite comp = warningComposite;
		while (comp != null && !(comp instanceof ScrolledComposite)) {
			comp.layout(true);
			comp = comp.getParent();
		}
		if (comp != null) {
			final ScrolledComposite scComp = (ScrolledComposite) comp;
			scComp.addControlListener(new ControlListener() {
				public void controlMoved(ControlEvent e) {
				}

				public void controlResized(ControlEvent e) {
					if (scComp.isDisposed() || warningText.isDisposed()) {
						return;
					}
					((FormData) warningText.getLayoutData()).width = Math.max(
							0, Math.min(warningText.computeSize(SWT.DEFAULT,
									SWT.DEFAULT).x, scComp.getSize().x - 50));
				}
			});
		}

	}

	@Override
	protected void doHandleEvent(Event e) {

	}

	@Override
	protected void doRefresh() {
		IContentData data = getFirstContent();
		if (data == null) {
			return;
		}
		ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) data
				.getAdapter(ISkinnableEntityAdapter.class);
		Map attributes = adapter.getAttributes();
		Boolean colorizeSelected = (Boolean) attributes
				.get(BitmapProperties.COLORIZE_SELECTED);
		Boolean optimizeSelected = (Boolean) attributes
				.get(BitmapProperties.IS_OPTIMIZE_SELECTED);

		boolean colourize = colorizeSelected != null && colorizeSelected;
		boolean optimize = optimizeSelected != null && optimizeSelected;

		if (colourize | optimize) {

			warningText.setText(Messages.Conversion_Section_banner_message,
					true, false);

			if (adapter.isSVG()) {
				warningText.setText(
						Messages.Conversion_Section_banner_message_svg, true,
						false);
			}

			if (warningComposite.isVisible()) {
				return;
			}

			warningComposite.setVisible(true);
			((FormData) warningComposite.getLayoutData()).height = SWT.DEFAULT;
			Composite comp = warningComposite;
			while (comp != null && !(comp instanceof ScrolledComposite)) {
				comp.layout(true);
				comp = comp.getParent();
			}
			if (comp != null) {
				Rectangle old = comp.getBounds();
				comp.setBounds(old.x, old.y, old.width + 1, old.height + 1);
				comp.setBounds(old);
				comp.layout(true);
			}
		} else {

			if (!warningComposite.isVisible()) {
				return;
			}

			warningComposite.setVisible(false);

			((FormData) warningComposite.getLayoutData()).height = 0;
			Composite comp = warningComposite;
			while (comp != null && !(comp instanceof ScrolledComposite)) {
				comp.layout(true);
				comp = comp.getParent();
			}
			if (comp != null) {
				Rectangle old = comp.getBounds();
				comp.setBounds(old.x, old.y, old.width + 1, old.height + 1);
				comp.setBounds(old);
				comp.layout(true);
			}
		}

		while (Display.getDefault().readAndDispatch())
			;
	}
}
