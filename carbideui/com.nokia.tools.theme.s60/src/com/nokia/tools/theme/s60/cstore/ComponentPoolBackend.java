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
package com.nokia.tools.theme.s60.cstore;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.ContentSourceManager;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.project.S60DesignProjectNature;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.s60.S60ThemeProvider;
import com.nokia.tools.theme.s60.examplethemes.ExampleThemeProvider;

public class ComponentPoolBackend {

	private final static String PROPS_FILE = "exampleThemes.properties";

	private final static String PROP_THEMECOUNT = "exampleThemesCount";

	private final static String PROP_THEME = "exampleTheme.";

	public static class NamedResult {
		public Object data;

		public String elementName;

		public String themeName;
	}

	private ComponentPoolConfig cfg = null;

	private static volatile ComponentPoolBackend eInstance = null;

	private Map<URL, ThemeContent> parsedThemes;

	public static ComponentPoolBackend getInstance() {
		if (eInstance == null) {
			synchronized (ComponentPoolBackend.class) {
				if (eInstance == null)
					eInstance = new ComponentPoolBackend();

			}
		}
		return eInstance;
	}

	public synchronized static void refresh() {
		if (eInstance != null) {
			eInstance.cfg = ComponentPoolConfig.load();
			eInstance.refreshThemeContent();
		}
	}

	public synchronized static void clear() {
		if (eInstance != null) {
			eInstance.parsedThemes = null;
		}
	}

	private synchronized void initParsedThemes() {
		if (parsedThemes == null) {
			refreshThemeContent();
		}
	}

	private synchronized void refreshThemeContent() {
			if (parsedThemes == null) {
				parsedThemes = new ConcurrentHashMap<URL, ThemeContent>();
			}
		
		List<URL> newThemeList = new ArrayList<URL>();
		try {
			if (cfg.addExampleThemes) {
				String exampleThemesContributor = ExampleThemeProvider
						.getContributorName();
				if (exampleThemesContributor != null) {

					URL propUrl = Platform.getBundle(exampleThemesContributor)
							.getEntry(PROPS_FILE);
					InputStream in = propUrl.openStream();
					Properties props = new Properties();
					props.load(in);
					in.close();

					int count = Integer.parseInt(props
							.getProperty(PROP_THEMECOUNT));
					String themeFolderLocation = FileLocator.toFileURL(
							FileLocator.find(Platform
									.getBundle(exampleThemesContributor),
									new Path("/"), null)).toString();
					for (int i = 1; i <= count; i++) {
						try {
							String themePath = props
									.getProperty(PROP_THEME + i).trim();
							if(!themePath.equals("")){
							newThemeList.add(getUrl(themeFolderLocation
									+ themePath));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			// add user list
			if (cfg.addCustomThemes) {
				for (String path : cfg.userThemeList) {
					newThemeList.add(new File(path).toURI().toURL());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (cfg.addWorkspaceTheme) {
			newThemeList.addAll(listOpenWorkspaceProjects());
		}

		List<URL> processed = new ArrayList<URL>();

		// update parsed theme list
		for (URL url : newThemeList) {
			processed.add(url);
			if (parsedThemes.get(url) == null) {
				try {
					ContentSourceManager mgr = ContentSourceManager
							.getGlobalInstance();
					List<IContent> c = mgr.getRootContents(
							S60ThemeProvider.CONTENT_TYPE,
							new File(url.toURI()), new NullProgressMonitor());
					if (!c.isEmpty()) {
						parsedThemes.put(url, (ThemeContent) c.get(0));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		Iterator<URL> it = parsedThemes.keySet().iterator();
		while (it.hasNext()) {
			if (!processed.contains(it.next()))
				it.remove();
		}
	}

	private URL getThemeUrl(final ThemeContent tc)
			throws MalformedURLException, URISyntaxException {
		initParsedThemes();
		for (Entry<URL, ThemeContent> entry : parsedThemes.entrySet()) {
			if (entry.getValue() == tc) {
				return entry.getKey();
			}
		}

		final Object[] ref = new Object[1];

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				for (IEditorReference r : PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getEditorReferences()) {
					if (r.getEditor(false) instanceof ScreenEditorPart) {
						IContent editorContent = (IContent) r.getEditor(false)
								.getAdapter(IContent.class);
						if (tc == editorContent) {
							FileEditorInput input = (FileEditorInput) r
									.getEditor(false).getEditorInput();
							try {
								ref[0] = input.getFile().getLocation().toFile()
										.toURI().toURL();
								break;
							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});

		return ((URL) ref[0]);
	}

	private IEditorPart findActiveEditor() {
		final List<IEditorPart> result = new ArrayList<IEditorPart>(1);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IEditorPart part = EclipseUtils.getActiveSafeEditor();
				if (part == null)
					result.add(null);
				else if (part instanceof ScreenEditorPart)
					result.add(part);
				else
					result.add((IEditorPart) part
							.getAdapter(ScreenEditorPart.class));
			}
		});
		return result.get(0);
	}

	private Object findActivetheme() {
		IEditorPart part = findActiveEditor();
		return (Object) (part != null ? part.getAdapter(IContent.class) : null);
	}

	/**
	 * This method lists all non opened projects in the workspace
	 * 
	 * @return
	 */
	private List<URL> listOpenWorkspaceProjects() {
		List<URL> lstProjects = new ArrayList<URL>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();

		for (IProject project : projects) {
			try {
				if (project.isOpen()
						&& project.hasNature(S60DesignProjectNature.NATURE_ID)) {
					IFile file = project.getFolder(project.getName()).getFile(
							project.getName() + ".tdf");
					lstProjects.add(file.getLocationURI().toURL());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return lstProjects;
	}

	/**
	 * This method lists all non opened projects in the workspace
	 * 
	 * @return
	 */
	private List<ThemeContent> getOpenThemes() {
		final List<ThemeContent> lstProjects = new ArrayList<ThemeContent>();
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				for (IEditorReference r : PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getEditorReferences()) {
					if (r.getEditor(false) instanceof ScreenEditorPart) {
						if (r.getEditor(false).getAdapter(IContent.class) instanceof ThemeContent) {
							ThemeContent tc = (ThemeContent) r.getEditor(false)
									.getAdapter(IContent.class);
							if (tc != null)
								lstProjects.add(tc);
						}
					}
				}
			}
		});
		return lstProjects;
	}

	private List<ThemeContent> getParsedThemes() {
		initParsedThemes();
		List<ThemeContent> lstProjects = new ArrayList<ThemeContent>();
		lstProjects.addAll(parsedThemes.values());

		try {
			// replace theme parsed from workspace with open
			List<ThemeContent> openThemes = getOpenThemes();
			for (ThemeContent tc : openThemes) {
				URL tcUrl = getThemeUrl(tc);
				ThemeContent old = parsedThemes.get(tcUrl);
				if (old != null) {
					lstProjects.remove(old);
					lstProjects.add(tc);
				} else if (cfg.addOpenThemes)
					lstProjects.add(tc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// remove current editor from the list
		lstProjects.remove(findActivetheme());

		return lstProjects;
	}

	private ComponentPoolBackend() {
		cfg = ComponentPoolConfig.load();
		refreshThemeContent();
	}

	private static URL getUrl(String path) throws MalformedURLException {
		return new URL(path.replace(" ", "%20"));
	}

	private String getThemeName(ThemeContent t) {
		initParsedThemes();
		for (Entry<URL, ThemeContent> entry : parsedThemes.entrySet()) {
			if (entry.getValue() == t) {
				Path path = new Path(entry.getKey().getPath());
				for (String segment : path.makeAbsolute().segments()) {
					if (segment.equals("ExampleThemes")) {
						return path.lastSegment().replace("%20", " ")
								+ " (Example Theme)";
					}
				}
				return path.lastSegment().replace("%20", " ");
			}
		}
		for (IEditorReference r : PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences()) {
			if (r.getEditor(false) instanceof ScreenEditorPart) {
				IContent tc = (IContent) r.getEditor(false).getAdapter(
						IContent.class);
				if (tc == t) {
					FileEditorInput input = (FileEditorInput) r
							.getEditor(false).getEditorInput();
					return input.getFile().getLocation().lastSegment();
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.s60.ui.cstore.IComponentPool#getElementListFromPool
	 * (java.lang.String, java.lang.String)
	 */
	public NamedResult[] getElementListFromPool(String id, String parentId)
			throws ThemeException {
		List<ThemeContent> themes = getParsedThemes();
		NamedResult se[] = new NamedResult[themes.size()];
		int i = 0;
		for (ThemeContent t : getParsedThemes()) {
			IContentData d = null;
			if (parentId != null) {
				IContentData cd = t.findById(parentId);
				if (cd != null) {
					d = cd.findById(id);
				}
			}

			if (d == null)
				d = t.findById(id);
			if (d != null) {
				Object modified = d.getAttribute(ContentAttribute.MODIFIED
						.name());
				if ("true".equalsIgnoreCase(modified.toString())) {

					// filter out background-only elements
					IImageAdapter imgAd = (IImageAdapter) d
							.getAdapter(IImageAdapter.class);
					IImage test = imgAd.getImage();
					if (test != null && test.getLayerCount() > 0)
						if (test.getLayer(0).isBackground()
								&& test.getLayerCount() == 1)
							continue;

					NamedResult n = new NamedResult();
					n.data = d;
					n.elementName = d.getId();
					n.themeName = getThemeName(t);
					se[i++] = n;
				}
			}
		}
		return se;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.s60.ui.cstore.IComponentPool#isSkinned(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	public boolean isSkinned(String elementId, String parentElementId,
			String sourceThemeName) {
		for (ThemeContent t : getParsedThemes()) {
			String name = getThemeName(t);
			if (name.equals(sourceThemeName)) {
				ThemeContent tContent = t;
				IContentData d = null;
				if (parentElementId != null) {
					IContentData cd = tContent.findById(parentElementId);
					if (cd != null) {
						d = cd.findById(elementId);
					}
				}

				if (d == null)
					d = tContent.findById(elementId);

				if (d != null) {
					return "true".equalsIgnoreCase((String) d
							.getAttribute(ContentAttribute.MODIFIED.name()));
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.ui.cstore.IComponentPool#getThemeCount()
	 */
	public int getThemeCount() {
		return getParsedThemes().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.s60.ui.cstore.IComponentPool#getComponentFromPool
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	public IContentData getComponentFromPool(String elementId,
			String parentElementId, String sourceThemeName) {
		for (ThemeContent t : getParsedThemes()) {
			String name = getThemeName(t);
			if (name.equals(sourceThemeName)) {
				ThemeContent tContent = t;
				IContentData d = null;
				if (parentElementId != null) {
					IContentData cd = tContent.findById(parentElementId);
					if (cd != null) {
						d = cd.findById(elementId);
					}
				}

				if (d == null)
					d = tContent.findById(elementId);
				return d;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.ui.cstore.IComponentPool#getThemeNames()
	 */
	public String[] getThemeNames() {
		List<ThemeContent> themes = getParsedThemes();
		String n[] = new String[themes.size()];
		int i = 0;
		for (ThemeContent t : themes) {
			n[i++] = getThemeName(t);
		}
		return n;
	}

	public static boolean initialized() {
		return eInstance != null;
	}

}
