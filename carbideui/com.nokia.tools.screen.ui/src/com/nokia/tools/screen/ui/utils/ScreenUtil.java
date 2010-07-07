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
package com.nokia.tools.screen.ui.utils;

import java.awt.Rectangle;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.AbstractContentSourceManager;
import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.ContentSourceManager;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.screen.core.ICategoryAdapter;
import com.nokia.tools.screen.core.IScreenContext;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.IScreenFactory;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;
import com.nokia.tools.screen.ui.extension.ExtensionManager;

public class ScreenUtil {
	private static final MessageFormat RECTANGLE_FORMAT = new MessageFormat(
			"{0},{1},{2},{3}");

	private ScreenUtil() {
	}

	public static boolean isPrimaryContentInput(IEditorInput input) {
		if (ExtensionManager.getEmbeddedEditorDescriptor(input) != null) {
			return false;
		}
		// filters out the inputs that are not handled by any content providers
		for (String type : AbstractContentSourceManager.getContentTypes()) {
			try {
				if (!ContentSourceManager.getGlobalInstance().getRootContents(
						type, input, null).isEmpty()) {
					return true;
				}
			} catch (Exception e) {
				UiPlugin.error(e);
			}
		}
		return false;
	}

	@Deprecated
	public static boolean isPrimaryContent(String contentType) {
		

		
		if ("DummyData".equalsIgnoreCase(contentType)) {
			return false;
		}
		return ExtensionManager
				.getEmbeddedEditorDescriptorByContentType(contentType) == null;
	}

	public static boolean isPrimaryContent(IContentData data) {
		if (data == null || data.getRoot() == null) {
			return false;
		}
		return isPrimaryContent(data.getRoot().getType());
	}

	public static IContent getPrimaryContent(IContent[] contents) {
		if (contents != null) {
			for (IContent content : contents) {
				if (isPrimaryContent(content)) {
					return content;
				}
			}
		}
		return null;
	}

	public static IContent getSecondaryContent(IContent[] contents) {
		if (contents != null) {
			for (IContent content : contents) {
				if (!isPrimaryContent(content)) {
					return content;
				}
			}
		}
		return null;
	}

	public static IEditorPart getEditor(IScreenElement element) {
		IScreenContext context = element.getContext();
		if (context != null) {
			DefaultEditDomain domain = (DefaultEditDomain) context
					.getAdapter(EditDomain.class);
			if (domain != null) {
				return domain.getEditorPart();
			}
		}
		// not on screen, try open editor to see if project matches
		IProject project = (IProject) element.getData().getRoot().getAttribute(
				ContentAttribute.PROJECT.name());
		if (project == null) {
			return null;
		}
		for (IEditorReference reference : PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences()) {
			IEditorPart editor = reference.getEditor(false);
			if (editor != null
					&& editor.getEditorInput() instanceof IFileEditorInput
					&& ((IFileEditorInput) editor.getEditorInput()).getFile()
							.getProject().equals(project)) {
				return editor;
			}
		}
		return null;
	}

	/**
	 * @return the current screen element in the active editor.
	 */
	public static IScreenElement getActiveScreen() {
		return getScreen(EclipseUtils.getActiveSafeEditor());
	}

	public static EditPartViewer getActiveViewer(IEditorPart editor) {
		EditPartViewer viewer = (EditPartViewer) editor
				.getAdapter(EditPartViewer.class);
		if (viewer == null) {
			IEditorPart part = (IEditorPart) editor
					.getAdapter(ScreenEditorPart.class);
			if (part != null) {
				viewer = (EditPartViewer) part.getAdapter(EditPartViewer.class);
			}
		}
		return viewer;
	}

	/**
	 * Returns the top-level screen element that is displayed in the current
	 * editor.
	 * 
	 * @param editor
	 *            the editor in where the screen element is displayed
	 * @return the screen element.
	 */
	public static IScreenElement getScreen(IEditorPart editor) {
		if (editor == null) {
			return null;
		}
		return getScreen(getActiveViewer(editor));
	}

	/**
	 * Returns the top-level screen element that is displayed in the current
	 * editor.
	 * 
	 * @param viewer
	 *            the editpart viewer.
	 * @return the screen element.
	 */
	public static IScreenElement getScreen(EditPartViewer viewer) {
		if (viewer != null) {
			List parts = viewer.getRootEditPart().getChildren();
			if (!parts.isEmpty()) {
				parts = ((EditPart) parts.get(0)).getChildren();
				if (!parts.isEmpty()) {
					return JEMUtil.getScreenElement(parts.get(0));
				}
			}
		}
		return null;
	}

	public static IScreenElement[] getSupportedElements(IEditorPart editor,
			int type) {
		return getSupportedElements(getScreen(editor), type);
	}

	public static IScreenElement[] getSupportedElements(EditPartViewer viewer,
			int type) {
		return getSupportedElements(getScreen(viewer), type);
	}

	public static IScreenElement[] getSupportedElements(IScreenElement screen,
			int type) {
		List<IScreenElement> elements = new ArrayList<IScreenElement>();
		if (screen != null) {
			for (IScreenElement child : screen.getAllChildren()) {
				IComponentAdapter adapter = (IComponentAdapter) child
						.getAdapter(IComponentAdapter.class);
				if (adapter.supports(type, null)) {
					elements.add(child);
				}
			}
		}
		return (IScreenElement[]) elements.toArray(new IScreenElement[elements
				.size()]);
	}

	public static IContentData getScreenContent(EditPartViewer viewer) {
		IScreenElement element = getScreen(viewer);
		if (element != null) {
			return element.getData();
		}
		return null;
	}

	public static IContent getContentRoot(EditPartViewer viewer) {
		IContentData data = getScreenContent(viewer);
		if (data != null) {
			return data.getRoot();
		}
		return null;
	}

	/**
	 * Screen element matching include matching element and categorized peers
	 * 
	 * @return true if element or categorized peers is "hit" with search tokens
	 */
	protected static boolean isMatching(IScreenElement element, Map searchTokens) {
		if (isMatching(element.getData(), searchTokens)) {
			return true;
		}
		ICategoryAdapter category = (ICategoryAdapter) element.getData()
				.getAdapter(ICategoryAdapter.class);
		if (null != category) {
			IContentData[] peers = category.getCategorizedPeers();
			if (null != peers) {
				for (IContentData data : peers) {
					if (isMatching(data, searchTokens))
						return true;
				}
			}
		}
		return false;
	}

	protected static boolean isMatching(IContentData element, Map searchTokens) {
		boolean match = false;

		for (Object ob : searchTokens.keySet()) {
			if (null == element.getAttribute((String) ob))
				continue;
			if (searchTokens.get(ob) == null) {
			
				return false;
			}
			if (!searchTokens.get(ob).equals(element.getAttribute((String) ob)))
				return false;
			else
				match = true;
		}
		return match;
	}

	public static EditPart findPartForMarkerAttrs(EditPart root, Map attributes) {
		if (null == root)
			return null;
		for (Object child : root.getChildren()) {
			Object model = ((EditPart) child).getModel();
			if (model instanceof EObject) {
				IScreenElement screenElement = JEMUtil.getScreenElement(model);
				if (null != screenElement) {
					
					if (isMatching(screenElement, attributes))
						return (EditPart) child;
				}
			}
			EditPart ret = findPartForMarkerAttrs((EditPart) child, attributes);
			if (null != ret)
				return ret;

		}
		return null;
	}

	public static List<EditPart> findPartsForData(EditPart root,
			IContentData[] data) {
		List<EditPart> parts = new ArrayList<EditPart>(root.getChildren()
				.size());
		if (data != null && data.length > 0) {
			// filters out duplicates
			Set<IContentData> set = new HashSet<IContentData>();
			for (IContentData cd : data) {
				set.add(cd);
			}
			findPartsForData(root, set, parts);
		}
		return parts;
	}

	private static void findPartsForData(EditPart part, Set<IContentData> data,
			List<EditPart> parts) {
		IScreenElement element = JEMUtil.getScreenElement(part);
		if (element != null) {
			HashMap<String, String> map = new HashMap<String, String>();
			for (IContentData cd : data) {
				map.put(ContentAttribute.ID.name(), cd.getId());
				map.put(ContentAttribute.NAME.name(), cd.getName());
				if (isMatching(element, map)) {
					parts.add(part);
				}
			}
		}
		for (Object child : part.getChildren()) {
			findPartsForData((EditPart) child, data, parts);
		}
	}

	public static EditPart findPartForData(EditPart root, IContentData data) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(ContentAttribute.ID.name(), data.getId());
		map.put(ContentAttribute.NAME.name(), data.getName());
		return findPartForMarkerAttrs(root, map);
	}

	/**
	 * find screen and access screen factory to create new one for the element
	 * in case screen doesnt exist. Similar method to findScreenThatContainsData
	 * with difference in case that screen is forced to be created if there is
	 * no screen existing IMPORTANT: usage constrained to editor, do not expose
	 * to other packages
	 * 
	 * @param element
	 *            the element to search for
	 * @param createIfNotFound
	 *            true will force adapting new element to the screen, false may
	 *            do nothing.<br/><b>Note: the behavior is implementation
	 *            dependant, using true only when necessary to avoid performance
	 *            hit.</b>
	 * @return
	 */
	public static IContentData getScreenForData(IContentData element,
			boolean createIfNotFound) {
		IScreenFactory screenFactory = (IScreenFactory) element.getRoot()
				.getAdapter(IScreenFactory.class);
		if (null != screenFactory)
			return screenFactory.getScreenForData(element, createIfNotFound);
		System.out.println("Missing screen for :" + element);
		return null;

	}

	public static Point getIconSize() {
		return Display.getCurrent().getIconSizes()[0];
	}

	/**
	 * Cuts the given string to fit in the given space with the given font and
	 * adds dots in the end.
	 * 
	 * @param text
	 *            text to be modified.
	 * @param space
	 *            space to be filled with the string.
	 * @param font
	 *            font to be used in calculating cutting.
	 * @return the shortened string with dots.
	 */
	public static String toShorterWithDots(String text, int space, Font font) {
		String dots = "...";
		int dotWidth = FigureUtilities.getTextWidth(dots, font);
		for (int i = 0; i < text.length(); i++) {
			int textWidth = FigureUtilities.getTextWidth(text.substring(0, i),
					font)
					+ dotWidth;
			if (textWidth > space) {
				text = text.substring(0, i - 1);
				break;
			}
		}
		return text + dots;
	}

	public static Rectangle parseRectangle(String text) {
		try {
			Object[] values = RECTANGLE_FORMAT.parse(text);
			int x = Integer.parseInt(((String) values[0]).trim());
			int y = Integer.parseInt(((String) values[1]).trim());
			int width = Integer.parseInt(((String) values[2]).trim());
			int height = Integer.parseInt(((String) values[3]).trim());
			return new Rectangle(x, y, width, height);
		} catch (Exception e) {
			return new Rectangle();
		}
	}

	public static String formatRectangle(Rectangle rect) {
		if (rect == null) {
			return "";
		}
		return RECTANGLE_FORMAT.format(new Object[] { rect.x, rect.y,
				rect.width, rect.height });
	}
}
