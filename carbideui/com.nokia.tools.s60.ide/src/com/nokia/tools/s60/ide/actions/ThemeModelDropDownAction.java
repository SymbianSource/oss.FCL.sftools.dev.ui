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
package com.nokia.tools.s60.ide.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.framework.internal.core.Constants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.extension.PluginEntry;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.s60.editor.actions.ChangeThemeModelAction;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

public class ThemeModelDropDownAction extends Action implements IMenuCreator {
	private Menu menu;

	private String currentId;

	/**
	 * Action id for the model change combo.
	 */
	public static final String ID = ThemeModelDropDownAction.class.getPackage()
			.getName()
			+ ".thememodel";

	public ThemeModelDropDownAction() {
		setToolTipText("Switch Between Platform Graphics");
		setMenuCreator(this);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/platform/platform_base.png"));
		setId(ID);
	}

	public void dispose() {
		if (menu != null) {
			menu.dispose();
		}
	}

	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		String curModel = getContent().getAttribute(
				ContentAttribute.MODEL.name()).toString();
		for (IThemeModelDescriptor desc : getItems()) {
			ChangeThemeModelAction action = new ChangeThemeModelAction(
					getEditorPart(), this, desc.getId());
			action.setText(desc.getName());
			if (desc.getId().equalsIgnoreCase(curModel)) {
				action.setChecked(true);
				ActionContributionItem item = new ActionContributionItem(action);
				item.fill(menu, -1);
				
			
			} else {
				action.setChecked(false);
				ActionContributionItem item = new ActionContributionItem(action);
				item.fill(menu, -1);
			}
		}
		return menu;
	}

	protected List<IThemeModelDescriptor> getItems() {
		final IContent content = getContent();
		if (content != null) {
			String themeId = (String) content
					.getAttribute(ContentAttribute.THEME_ID.name());
			String modelId = (String) content
					.getAttribute(ContentAttribute.MODEL.name());
			IThemeDescriptor desc = ThemePlatform
					.getThemeDescriptorById(themeId);
			if (desc != null) {
				String containerId = desc.getContainerId();
				List<IThemeModelDescriptor> items = new ArrayList<IThemeModelDescriptor>();
				boolean isModelEditor = isModelEditor();
				Set<String> modelIds = new HashSet<String>();
				for (IThemeModelDescriptor modelDesc : ThemePlatform
						.getThemeModelDescriptorsByContainer(containerId)) {
					if ((!isModelEditor || modelId.equalsIgnoreCase(modelDesc
							.getId()))
							&& !modelIds.contains(modelDesc.getId())
							&& !isFromPluginProject(modelDesc)) {
						items.add(modelDesc);
						modelIds.add(modelDesc.getId());
					}
				}
				Collections.sort(items,
						new Comparator<IThemeModelDescriptor>() {

							/*
							 * (non-Javadoc)
							 * 
							 * @see java.util.Comparator#compare(java.lang.Object,
							 *      java.lang.Object)
							 */
							public int compare(IThemeModelDescriptor o1,
									IThemeModelDescriptor o2) {
								return o1.getName().compareToIgnoreCase(
										o2.getName());
							}
						});
				return items;
			}
		}
		return Collections.EMPTY_LIST;
	}

	private boolean isFromPluginProject(IThemeModelDescriptor descriptor) {
		return !descriptor.getModelPath().toExternalForm().startsWith(
				Constants.OSGI_ENTRY_URL_PROTOCOL);
	}

	private boolean isModelEditor() {
		
		
		IEditorPart editor = getEditorPart();
		return editor != null
				&& editor.getEditorInput() instanceof IFileEditorInput
				&& PluginEntry.PLUGIN_XML.equals(((IFileEditorInput) editor
						.getEditorInput()).getFile().getName());
	}

	public IContent getContent() {
		IWorkbenchPart part = getEditorPart();
		if (part == null) {
			return null;
		}
		IContentAdapter adapter = (IContentAdapter) part
				.getAdapter(IContentAdapter.class);
		if (adapter == null) {
			return null;
		}
		return ScreenUtil.getPrimaryContent(adapter.getContents());
	}

	public IEditorPart getEditorPart() {
		return EclipseUtils.getActiveSafeEditor();
	}

	public Menu getMenu(Menu parent) {
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		IContent content = getContent();
		if (content != null) {
			String modelId = (String) content
					.getAttribute(ContentAttribute.MODEL.name());
			if (modelId != null) {
				IThemeModelDescriptor desc = ThemePlatform
						.getThemeModelDescriptorById(modelId);
				if (desc != null) {
					String themeId = desc.getThemeId();
					currentId = themeId;
					String name = desc.getName();
					
					setImageDescriptor(getCustomImageDescriptor(themeId, name));
					setToolTipText(name);
				}
			}
		}
	}

	
	public ImageDescriptor getCustomImageDescriptor(String id, String text) {
		FontData fd = JFaceResources.getDefaultFont().getFontData()[0];
		Font bigFont = new Font(Display.getDefault(), fd.getName(), 9, SWT.BOLD);
		Font smallFont = new Font(Display.getDefault(), fd.getName(), 5,
				SWT.NORMAL);
		Font normalFont = new Font(Display.getDefault(), fd.getName(), 7,
				SWT.NORMAL);
		ImageDescriptor desc = S60WorkspacePlugin
				.getImageDescriptor("icons/platform/platform_base.png");

		Image bannerImage = null;
		GC imageGC = null;
		int logoSpace;
		Color color1 = new Color(Display.getDefault(), 128, 132, 135);
		Color color2 = new Color(Display.getDefault(), 183, 83, 176);
		Color color3 = new Color(Display.getDefault(), 143, 0, 96);

		try {
			bannerImage = desc.createImage();
			int imageWidth = bannerImage.getBounds().width;
			imageGC = new GC(bannerImage);
			if (id.contains("60")) {
				logoSpace = FigureUtilities.getTextWidth("S60", bigFont) + 2;
				imageGC.setForeground(color1);
				imageGC.setFont(bigFont);
				imageGC.drawString("S60", 1, 1, true);
			} else {
				logoSpace = 0;
			}
			imageGC.setForeground(ColorConstants.black);
			imageGC.setFont(normalFont);
			text = filtrateText(text);
			int freeSpace = imageWidth - logoSpace - 2;
			int textWidth = FigureUtilities.getTextWidth(text, normalFont);

			if (textWidth > freeSpace) {
				text = ScreenUtil
						.toShorterWithDots(text, freeSpace, normalFont);
				textWidth = FigureUtilities.getTextWidth(text, normalFont);
			}
			int x = (int) (((freeSpace) - textWidth) / 2) + logoSpace;
			imageGC.drawString(text, x, 3, true);
			ImageData data = bannerImage.getImageData();
			data.transparentPixel = new java.awt.Color(192, 192, 192).getRGB();
			return ImageDescriptor.createFromImageData(data);
		} finally {
			if (imageGC != null) {
				imageGC.dispose();
			}
			if (bannerImage != null) {
				bannerImage.dispose();
			}
			if (bigFont != null) {
				bigFont.dispose();
			}
			if (normalFont != null) {
				normalFont.dispose();
			}
			if (smallFont != null) {
				smallFont.dispose();
			}
			if (color1 != null) {
				color1.dispose();
			}
			if (color2 != null) {
				color2.dispose();
			}
			if (color3 != null) {
				color3.dispose();
			}
		}
	}

	private String filtrateText(String text) {
		text = text.replace("S60 ", "");
		text = text.replace("Series 40 ", "");
		text = text.replace(" Edition", "");
		text = text.replace(" BaseGraphics", "");
		return text;
	}
}
