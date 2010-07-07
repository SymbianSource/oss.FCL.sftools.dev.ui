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
package com.nokia.tools.theme.s60;

import java.util.ArrayList;
import java.util.List;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.ContentSourceManager;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentProvider;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.Orientation;
import com.nokia.tools.platform.theme.Task;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.IScreenFactory;
import com.nokia.tools.theme.content.AbstractThemeProvider;
import com.nokia.tools.theme.content.ContentAdapter;
import com.nokia.tools.theme.content.IThemeContentProvider;
import com.nokia.tools.theme.content.ThemeContentType;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeScreenData;
import com.nokia.tools.theme.screen.ThemeElement;
import com.nokia.tools.theme.screen.ThemeTextElement;

public class S60ThemeContentProvider implements IThemeContentProvider {
	private static final IContentData[] EMPTY = new IContentData[0];

	private static final String ID_GRID_TEXT = "text9";

	private static final String ID_GRID_HIGHLIGHT = "qsn_anim_grid";

	private static final String ID_GRID_RUNNING = "qgn_indi_app_open";

	private static final String ID_GRID_DRM = "qgn_prop_drm_rights_valid_super";

	private static final String ID_GRID_MARKER = "qgn_indi_marked_grid_add";

	private static final String ID_MAIN_TEXT = "text6";

	private static final String[] IDS_VERTICAL_SCROLLBAR = {
			"qsn_cp_scroll_bg_top", "qsn_cp_scroll_bg_middle",
			"qsn_cp_scroll_bg_bottom", "qsn_cp_scroll_handle_top",
			"qsn_cp_scroll_handle_middle", "qsn_cp_scroll_handle_bottom" };

	private static final String NAME_ICONS = "ICONS";

	private static final String NAME_APPLICATION_ICONS = "Application Icons";

	private static final String NAME_CONTEXT_PANE_ICONS = "Context Pane Icons";

	private static final String NAME_GRID_VIEW = "Application Grid";

	private static final String NAME_FLAT_STATUS_PANE_PORTRAIT = "bg_status_flat_pane";

	private static final String NAME_STATUS_PANE_PORTRAIT = "status_pane_g3";

	private static final String NAME_STATUS_PANE_LANDSCAPE = "stacon_top_pane_g1";

	private static final String NAME_CONTROL_PANE_PORTRAIT = "control_pane_g3";

	private static final String NAME_CONTROL_PANE_LANDSCAPE = "stacon_bottom_pane_g1";

	private static final String NAME_IDLE_VIEW = "Idle";

	private static final String NAME_COLOURS = "COLOURS";

	private IContent content;

	private IContentData gridText;

	private IContentData mainText;

	public S60ThemeContentProvider(IContent content) {
		this.content = content;
	}

	private synchronized IContentData getText(ThemeContentType type) {
		if (ThemeContentType.MAIN_TEXT == type) {
			if (mainText == null
					|| ContentAdapter.getContentData(((ThemeData) mainText)
							.getResource()) != mainText) {
				mainText = content.findById(ID_MAIN_TEXT);
			}
			return mainText;
		}
		if (ThemeContentType.GRID_TEXT == type) {
			if (gridText == null
					|| ContentAdapter.getContentData(((ThemeData) gridText)
							.getResource()) != gridText) {
				gridText = content.findById(ID_GRID_TEXT);
			}
			return gridText;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.IThemeContentProvider#createScreenElement(com.nokia.tools.content.core.IContentData,
	 *      com.nokia.tools.theme.content.ThemeContentType)
	 */
	public IScreenElement createScreenElement(IContentData data,
			ThemeContentType type) {
		if (!(data instanceof ThemeData)) {
			return null;
		}
		if (ThemeContentType.GRID_TEXT == type) {
			IContentData gridText = getText(ThemeContentType.GRID_TEXT);
			ThemeTextElement element = new S60ThemeTextElement(
					(ThemeData) gridText);
			element.setText((String) data.getAttribute(ContentAttribute.TEXT
					.name()));
			return element;
		}
		if (ThemeContentType.MAIN_TEXT == type) {
			IContentData mainText = getText(ThemeContentType.MAIN_TEXT);
			ThemeTextElement element = new S60ThemeTextElement(
					(ThemeData) mainText);
			element.setText((String) data.getAttribute(ContentAttribute.TEXT
					.name()));
			return element;
		}
		IContentProvider provider = ContentSourceManager
				.getGlobalContentProvider(S60ThemeProvider.CONTENT_TYPE);
		if (provider instanceof AbstractThemeProvider) {
			return (ThemeElement) ((AbstractThemeProvider) provider)
					.createScreenElement(data);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.IThemeContentProvider#getContentData(com.nokia.tools.theme.content.ThemeContentType)
	 */
	public IContentData[] getContentData(ThemeContentType type) {

		if (ThemeContentType.THEME_COLOR_GROUPS == type) {
			List<IContentData> data = new ArrayList<IContentData>();
			for (IContentData task : content.getChildren()) {
				ThemeData td = (ThemeData) task;
				if (td.getData() instanceof Task
						&& NAME_COLOURS.equalsIgnoreCase(task.getName())) {
					for (IContentData componentGroup : task.getChildren()) {
						data.add(componentGroup);
					}
				}
			}
			return data.toArray(new IContentData[data.size()]);
		}

		if (ThemeContentType.THEME_COLOR_COMPONENTS == type) {
			List<IContentData> data = new ArrayList<IContentData>();
			for (IContentData task : content.getChildren()) {
				ThemeData td = (ThemeData) task;
				if (td.getData() instanceof Task
						&& NAME_COLOURS.equalsIgnoreCase(task.getName())) {
					for (IContentData componentGroup : task.getChildren()) {
						for (IContentData component : componentGroup
								.getChildren()) {
							for (IContentData element : component.getChildren()) {
								data.add(element);
							}
						}
					}
				}
			}
			return data.toArray(new IContentData[data.size()]);
		}

		if (ThemeContentType.APPLICATION_ICON == type) {
			List<IContentData> data = new ArrayList<IContentData>();
			for (IContentData task : content.getChildren()) {
				ThemeData td = (ThemeData) task;
				if (td.getData() instanceof Task
						&& NAME_ICONS.equalsIgnoreCase(task.getName())) {
					for (IContentData componentGroup : task.getChildren()) {
						if (NAME_APPLICATION_ICONS
								.equalsIgnoreCase(componentGroup.getName())) {
							for (IContentData component : componentGroup
									.getChildren()) {
								if (NAME_CONTEXT_PANE_ICONS
										.equalsIgnoreCase(component.getName())) {
									for (IContentData element : component
											.getChildren()) {
										data.add(element);
									}
								}
							}
						}
					}
				}
			}
			return data.toArray(new IContentData[data.size()]);
		}
		if (ThemeContentType.GRID_HIGHLIGHT == type) {
			IContentData data = content.findById(ID_GRID_HIGHLIGHT);
			return data == null ? EMPTY : new IContentData[] { data };
		}
		if (ThemeContentType.GRID_DRM == type) {
			IContentData data = content.findById(ID_GRID_DRM);
			return data == null ? EMPTY : new IContentData[] { data };
		}
		if (ThemeContentType.GRID_MARKER == type) {
			IContentData data = content.findById(ID_GRID_MARKER);
			return data == null ? EMPTY : new IContentData[] { data };
		}
		if (ThemeContentType.GRID_RUNNING == type) {
			IContentData data = content.findById(ID_GRID_RUNNING);
			return data == null ? EMPTY : new IContentData[] { data };
		}
		if (ThemeContentType.MAIN_TEXT == type) {
			IContentData data = getText(ThemeContentType.MAIN_TEXT);
			return data == null ? EMPTY : new IContentData[] { data };
		}
		if (ThemeContentType.GRID_TEXT == type) {
			IContentData data = getText(ThemeContentType.GRID_TEXT);
			return data == null ? EMPTY : new IContentData[] { data };
		}
		if (ThemeContentType.VERTICAL_SCROLLBAR == type) {
			List<IContentData> list = new ArrayList<IContentData>();
			for (String id : IDS_VERTICAL_SCROLLBAR) {
				IContentData data = content.findById(id);
				if (data != null) {
					list.add(data);
				}
			}
			return list.toArray(new IContentData[list.size()]);
		}

		if (ThemeContentType.GRID_VIEW == type) {
			return getScreen(NAME_GRID_VIEW);
		} else if (ThemeContentType.VIEW == type) {
			return getScreen(NAME_IDLE_VIEW);
		}

		Display display = (Display) content
				.getAttribute(ContentAttribute.DISPLAY.name());
		Orientation orientation = display == null ? Orientation.PORTRAIT
				: display.getOrientation();
		if (orientation != Orientation.LANDSCAPE) {
			// covering square case
			orientation = Orientation.PORTRAIT;
		}

		if (ThemeContentType.THIN_STATUS_PANE == type) {
			IContentData[] views = getContentData(ThemeContentType.GRID_VIEW);
			if (views.length > 0) {
				for (IContentData child : ((ThemeScreenData) views[0])
						.findAllElements()) {
					if (Orientation.PORTRAIT == orientation
							&& NAME_FLAT_STATUS_PANE_PORTRAIT
									.equalsIgnoreCase(child.getName())
							&& ((ThemeData) child).supportsDisplay(display)) {
						return new IContentData[] { child };
					}
					if (Orientation.LANDSCAPE == orientation
							&& NAME_STATUS_PANE_LANDSCAPE
									.equalsIgnoreCase(child.getName())
							&& ((ThemeData) child).supportsDisplay(display)) {
						return new IContentData[] { child };
					}
				}
			}
			return EMPTY;
		}
		if (ThemeContentType.STATUS_PANE == type) {
			IContentData[] views = getContentData(ThemeContentType.VIEW);
			if (views.length > 0) {
				for (IContentData child : ((ThemeScreenData) views[0])
						.findAllElements()) {
					if (Orientation.PORTRAIT == orientation
							&& NAME_STATUS_PANE_PORTRAIT.equalsIgnoreCase(child
									.getName())
							&& ((ThemeData) child).supportsDisplay(display)) {
						return new IContentData[] { child };
					}
					if (Orientation.LANDSCAPE == orientation
							&& NAME_STATUS_PANE_LANDSCAPE
									.equalsIgnoreCase(child.getName())
							&& ((ThemeData) child).supportsDisplay(display)) {
						return new IContentData[] { child };
					}
				}
			}
			return EMPTY;
		}
		if (ThemeContentType.CONTROL_PANE == type) {
			IContentData[] views = getContentData(ThemeContentType.GRID_VIEW);
			if (views.length > 0) {
				for (IContentData child : ((ThemeScreenData) views[0])
						.findAllElements()) {
					if (Orientation.PORTRAIT == orientation
							&& NAME_CONTROL_PANE_PORTRAIT
									.equalsIgnoreCase(child.getName())
							&& ((ThemeData) child).supportsDisplay(display)) {
						return new IContentData[] { child };
					}
					if (Orientation.LANDSCAPE == orientation
							&& NAME_CONTROL_PANE_LANDSCAPE
									.equalsIgnoreCase(child.getName())
							&& ((ThemeData) child).supportsDisplay(display)) {
						return new IContentData[] { child };
					}
				}
			}
			return EMPTY;
		}

		return EMPTY;
	}

	private IContentData[] getScreen(String screenName) {
		Display display = (Display) content
				.getAttribute(ContentAttribute.DISPLAY.name());
		Orientation orientation = display == null ? Orientation.PORTRAIT
				: display.getOrientation();
		if (orientation != Orientation.LANDSCAPE) {
			orientation = Orientation.PORTRAIT;
		}
		IScreenFactory factory = (IScreenFactory) content
				.getAdapter(IScreenFactory.class);
		for (IContentData screen : factory.getScreens()) {
			if (Orientation.PORTRAIT == orientation
					&& ((ThemeData) screen).supportsDisplay(display)
					&& screenName.equalsIgnoreCase(screen.getName())) {
				return new IContentData[] { screen };
			}
			if (Orientation.LANDSCAPE == orientation
					&& ((ThemeData) screen).supportsDisplay(display)
					&& screenName.equalsIgnoreCase(screen.getName())) {
				return new IContentData[] { screen };
			}
		}
		return EMPTY;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IContent.class))
			return content;
		return null;
	}
}
