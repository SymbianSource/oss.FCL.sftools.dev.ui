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
package com.nokia.tools.s60.editor.menus;

import org.eclipse.jface.action.Action;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.core.Orientation;
import com.nokia.tools.s60.editor.actions.ChangeResolutionAction;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.ide.actions.ActionMessages;

/**
 */
public class ResolutionComboContributionItem extends
		ContentComboContributionItem {
	/**
	 * Action id for the resolution change combo.
	 */
	public static final String ID = ResolutionComboContributionItem.class
			.getPackage().getName()
			+ ".resolution";
	private static final String DISPLAY_FORMAT = " {0} x {1}{2}";

	private Action orientationAction;

	public ResolutionComboContributionItem() {
		super(ID);
	}

	public Action getOrientationAction() {
		if (orientationAction == null) {
			orientationAction = new OrientationAction();
		}
		return orientationAction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.menus.ComboContributionItem#getItems()
	 */
	@Override
	protected String[] getItems() {
		return getAllResolutions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.menus.ComboContributionItem#getNewItem()
	 */
	@Override
	protected String getNewItem() {
		Display res = getContentResolution();
		if (res == null) {
			return null;
		}
		return res.format(DISPLAY_FORMAT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.menus.ComboContributionItem#itemSelected(java.lang.String)
	 */
	@Override
	protected void itemSelected(String item) {
		setCurrentResolution(Display.valueOf(item));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.menus.ComboContributionItem#comboDisabled()
	 */
	@Override
	protected void comboDisabled() {
		super.comboDisabled();
		orientationAction.setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.menus.ComboContributionItem#comboEnabled()
	 */
	@Override
	protected void comboEnabled() {
		super.comboEnabled();

		boolean isEnabled = false;
		Display resolution = Display.valueOf(getSelectedItem());
		IDevice[] devices = (IDevice[]) getContent()
				.getAdapter(IDevice[].class);
		for (int i = 0; i < devices.length; i++) {
			Display display = devices[i].getDisplay();
			if (resolution.getWidth() == display.getHeight()
					&& resolution.getHeight() == display.getWidth()
					&& resolution.getWidth() != resolution.getHeight()
					&& resolution.getType().equals(display.getType())) {
				isEnabled = true;
				break;
			}
		}

		orientationAction.setEnabled(isEnabled);
	}

	/**
	 * @return Current resolution
	 */
	protected Display getContentResolution() {
		IContent theme = getContent();
		if (theme == null) {
			return null;
		}
		return (Display) theme.getAttribute(ContentAttribute.DISPLAY.name());
	}

	/**
	 * @return All supported resolutions
	 */
	protected String[] getAllResolutions() {
		IContent content = getContent();
		if (content == null) {
			return new String[0];
		}

		IDevice[] devices = (IDevice[]) getContent()
				.getAdapter(IDevice[].class);
		if (devices == null) {
			return new String[0];
		}
		String[] resolutions = new String[devices.length];
		for (int i = 0; i < devices.length; i++) {
			Display display = devices[i].getDisplay();
			resolutions[i] = display.format(DISPLAY_FORMAT);
		}
		return resolutions;
	}

	/**
	 * Applies the new resolution to the underlying content.
	 * 
	 * @param width width of the new resolution.
	 * @param height height of the new resolution.
	 */
	protected void setCurrentResolution(Display display) {
		IContent theme = getContent();
		if (theme != null) {
			
			ChangeResolutionAction action = new ChangeResolutionAction(
					getEditorPart(), display);
			if (action.isEnabled()) {
				action.run();
			}
		}
	}

	/**
	 * Toggles current orientation
	 */
	private void toggleOrientation() {
		Display display = Display.valueOf(getSelectedItem());
		Display newDisplay = (Display) display.clone();
		newDisplay.setWidth(display.getHeight());
		newDisplay.setHeight(display.getWidth());
		newDisplay
				.setOrientation(display.getOrientation() == Orientation.PORTRAIT ? Orientation.LANDSCAPE
						: Orientation.PORTRAIT);
		setSelectedItem(newDisplay.format(DISPLAY_FORMAT));
		setCurrentResolution(newDisplay);
	}

	class OrientationAction extends Action {
		public OrientationAction() {
			setEnabled(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			toggleOrientation();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#setEnabled(boolean)
		 */
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			updateOrientationState();
		}

		/**
		 * Updates images and tooltips based on current orientation
		 */
		public void updateOrientationState() {
			if (isEnabled()) {
				Display resolution = Display.valueOf(getSelectedItem());
				if (resolution.getWidth() < resolution.getHeight()) {
					setToolTipText(ActionMessages.LandscapeOrientationAction_tooltip);
					setImageDescriptor(S60WorkspacePlugin
							.getImageDescriptor("icons/etool16/portrait_landscape16x16.png"));
					setDisabledImageDescriptor(S60WorkspacePlugin
							.getImageDescriptor("icons/dtool16/portrait_landscape16x16.png"));
				} else {
					setToolTipText(ActionMessages.PortraitOrientationAction_tooltip);
					setImageDescriptor(S60WorkspacePlugin
							.getImageDescriptor("icons/etool16/landscape_portrait16x16.png"));
					setDisabledImageDescriptor(S60WorkspacePlugin
							.getImageDescriptor("icons/dtool16/landscape_portrait16x16.png"));
				}
			} else {
				setImageDescriptor(S60WorkspacePlugin
						.getImageDescriptor("icons/etool16/portrait_landscape16x16.png"));
				setDisabledImageDescriptor(S60WorkspacePlugin
						.getImageDescriptor("icons/dtool16/portrait_landscape16x16.png"));
			}
		}
	}
}
