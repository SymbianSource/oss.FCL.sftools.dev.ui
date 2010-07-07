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
package com.nokia.tools.theme.screen;

import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.SwingConstants;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.layout.TextLayout;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewTagConstants;
import com.nokia.tools.screen.core.IScreenContext;
import com.nokia.tools.screen.core.IScreenElementCustomizer;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeScreenElementData;
import com.nokia.tools.theme.core.Activator;
import com.nokia.tools.widget.SFont;
import com.nokia.tools.widget.theme.ThemeText;

public class ThemeTextElement extends ThemeElement {
	private String text;

	private IScreenElementCustomizer customizer;

	public ThemeTextElement(ThemeScreenElementData data) {
		super(data);
	}

	public ThemeTextElement(ThemeTextElement textElement) {
		super((ThemeData) textElement.getData());
	}

	public ThemeTextElement(ThemeData data) {
		super(data);
	}

	/**
	 * @return Returns the text.
	 */
	private String internGetText() {
		return text == null ? ((ThemeData) getData()).getData()
				.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_TEXT)
				: text;
	}

	/**
	 * @param text The text to set.
	 */
	public void setText(String text) {
		this.text = text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenElement.ScreenElementAdapter#createWidget()
	 */
	@Override
	protected Object createWidget() {
		return new ThemeText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.screen.ThemeElement#initWidgetSpi(com.nokia.tools.screen.core.IScreenContext)
	 */
	@Override
	protected void initWidgetSpi(IScreenContext context) {
		super.initWidgetSpi(context);

		ThemeBasicData element = ((ThemeData) getData()).getData();

		ThemeText text = (ThemeText) getBean();
		if (element instanceof PreviewElement) {
			Layout textLayout = null;
			try {
				textLayout = element.getLayoutForPreviewNonFrame(element
						.getDisplay(), ((PreviewElement) element)
						.getComponentInfo());
			} catch (ThemeException e) {
				Activator.error(e);
			}
			if (textLayout != null && !(textLayout instanceof TextLayout)) {
				// debugging
				return;
			}

			if (textLayout instanceof TextLayout) {
				Rectangle bounds = (Rectangle) getData().getAttribute(
						ContentAttribute.BOUNDS.name());

				TextLayout textElemLayout = (TextLayout) textLayout;
				String justification = textElemLayout.getJustification();
				int baseLine = textElemLayout.getBaseline();

				int marginL = textElemLayout.getLeftMargin();
				int marginR = textElemLayout.getRightMargin();
				int marginT = textElemLayout.getTopMargin();
				int marginB = textElemLayout.getBottomMargin();

				String typeFace = textElemLayout.getTypeFace();
				boolean ignoreBoldStyle = false;

				if (typeFace != null
						&& (typeFace.contains("Bold") || typeFace
								.contains("bold"))) {
					ignoreBoldStyle = true;
				}
				String style = (String) textElemLayout.getStyle1();
				int fStyle1 = 0;

				if (style == null || style.trim().length() == 0) {
					fStyle1 = Font.PLAIN;
				} else if (style.equalsIgnoreCase("bold") && !ignoreBoldStyle) {
					fStyle1 = Font.BOLD;
				} else {
					fStyle1 = Font.PLAIN;
				}

				style = (String) textElemLayout.getStyle2();
				int fStyle2 = 0;

				if (style == null || style.trim().length() == 0) {
					fStyle2 = Font.PLAIN;
				} else if (style.equalsIgnoreCase("italic")) {
					fStyle2 = Font.ITALIC;
				} else {
					fStyle2 = Font.PLAIN; // regular
				}

				int combinedFontStyle = Font.PLAIN;

				if (fStyle1 != Font.PLAIN && fStyle2 != Font.PLAIN) {
					combinedFontStyle = fStyle1 | fStyle2;
				} else if (fStyle1 == Font.PLAIN) {
					combinedFontStyle = fStyle2;
				} else if (fStyle2 == Font.PLAIN) {
					combinedFontStyle = fStyle1;
				}
				int fontSize = textElemLayout.getFontSize();

				int deviceSize = getDeviceSize(fontSize, typeFace);
				int margin = fontSize - deviceSize;
				fontSize = deviceSize;
				if (marginT == 0) {
					marginT = bounds.y - textElemLayout.T()
							+ Math.round(margin / 2f);
				}
				if (marginB == 0) {
					marginB = bounds.y - textElemLayout.B();
				}

				int alignment = SwingConstants.LEFT;
				if ("center".equalsIgnoreCase(justification)) {
					alignment = SwingConstants.CENTER;
				} else if ("right".equalsIgnoreCase(justification)) {
					alignment = SwingConstants.RIGHT;
				}

				text.setAlignment(alignment);
				text.setBaseline(baseLine);
				text.setMarginTop(marginT);
				text.setMarginBottom(marginB);
				text.setMarginLeft(marginL);
				text.setMarginRight(marginR);

				SFont font = new SFont(typeFace, combinedFontStyle, fontSize);
				text.setFont(font);
				text.setText(internGetText());
			}
		}
		IColorAdapter adapter = (IColorAdapter) getData().getAdapter(
				IColorAdapter.class);
		if (adapter != null) {
			text.setColor(adapter.getColor());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.screen.ThemeElement#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IScreenElementCustomizer.class) {
			return customizer == null ? (customizer = new IScreenElementCustomizer() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.screen.core.IScreenElementCustomizer#getText()
				 */
				public String getText() {
					return internGetText();
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.screen.core.IScreenElementCustomizer#setText(java.lang.String)
				 */
				public void setText(String text) {
					ThemeTextElement.this.setText(text);
				}

			})
					: customizer;
		}
		return super.getAdapter(adapter);
	}

	protected int getDeviceSize(int fontSize, String typeFace) {
		return fontSize;
	}
}
