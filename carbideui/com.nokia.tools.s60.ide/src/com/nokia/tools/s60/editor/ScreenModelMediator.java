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
package com.nokia.tools.s60.editor;

import java.awt.Dimension;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.nokia.tools.content.core.AbstractContentSourceManager;
import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentDelta;
import com.nokia.tools.content.core.IContentListener;
import com.nokia.tools.content.core.IContentService;
import com.nokia.tools.content.core.IContentSourceManager;
import com.nokia.tools.content.core.project.S60DesignProjectNature;
import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.utils.ThreadUtils;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.IInvokable;
import com.nokia.tools.resource.util.DebugHelper.SilentRunnable;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.ResourceView2;
import com.nokia.tools.screen.core.CustomizationManager;
import com.nokia.tools.screen.core.IContentSynchronizer;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.core.IScreenContext;
import com.nokia.tools.screen.core.IScreenCustomizer;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.IScreenFactory;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.core.ScreenEditingModelFactory;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorCustomizer;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorPart;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IEmbeddedEditorDescriptor;
import com.nokia.tools.screen.ui.gallery.IGalleryAdapter;
import com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.views.ResourceViewPart;
import com.nokia.tools.screen.ui.views.ViewIDs;

/**
 * This class mediates the model and the editor. All model related actions are
 * performed here.
 * 
 */
public class ScreenModelMediator implements IGalleryScreenProvider,
		IContentListener, Runnable, IPropertyChangeListener, IAdaptable {
	/**
	 * Total number of work slices
	 */
	public static final int TOTAL_WORK = 1100;
	/**
	 * Total number of work slices used for generating screen elements
	 */
	public static final int SCREEN_BUILD_WORK = 1000;
	/**
	 * Total number of work slices used for doing other work, this is equivalent
	 * to {@link #TOTAL_WORK} - {@link #SCREEN_BUILD_WORK}.
	 */
	public static final int OTHER_WORK = TOTAL_WORK - SCREEN_BUILD_WORK;

	private static final long MONITOR_INTERVAL = 5 * 1000L;

	protected Series60EditorPart editor;
	private Set<IContent> activeContents;
	protected Map<IContent, IEditorPart> editors;
	private IContent activeContent;

	private Map<IContentData, GraphicalViewer> viewers;
	private List<IGalleryScreen> galleryScreens;
	private IContent contentNotification;

	private Thread thread;
	private Object galleryLock = new Object();
	private ScreenContext screenContext;
	private boolean suppressPreferenceChangeEvents;
	private volatile boolean isDisposed;

	/**
	 * Creates a model mediator for the specific editor.
	 * 
	 * @param editor the screen editor.
	 */
	public ScreenModelMediator(Series60EditorPart editor) {
		this.editor = editor;
		viewers = new HashMap<IContentData, GraphicalViewer>(32);
		activeContents = new HashSet<IContent>();
		editors = new HashMap<IContent, IEditorPart>();
		S60WorkspacePlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(this);
		start();
	}

	private synchronized Set<IContent> replaceContent(IContent newContent) {
		Set<IContent> oldContents = new HashSet<IContent>(getActiveContents()
				.size());
		IFile file = (IFile) newContent.getAdapter(IFile.class);
		for (Iterator<IContent> i = getActiveContents().iterator(); i.hasNext();) {
			IContent content = (IContent) i.next();
			IFile f = (IFile) content.getAdapter(IFile.class);
			if (f != null && f.equals(file)) {
				i.remove();
				oldContents.add(content);
				contentRemoved(content);
			}
		}
		for (Iterator<IContentData> i = viewers.keySet().iterator(); i
				.hasNext();) {
			IFile f = (IFile) i.next().getRoot().getAdapter(IFile.class);
			if (f != null && f.equals(file)) {
				i.remove();
			}
		}
		IEditorPart editor = null;
		for (Iterator<IContent> i = editors.keySet().iterator(); i.hasNext();) {
			IContent c = i.next();
			if (c != null && c.getRoot() != null) {
				IFile f = (IFile) c.getRoot().getAdapter(IFile.class);
				if (f != null && f.equals(file)) {
					editor = editors.get(c);
					i.remove();
				}
			}
		}
		addContent(newContent);
		if (editor != null) {
			editors.put(newContent, editor);
		}
		return oldContents;
	}

	public static IEmbeddedEditorDescriptor getEmbeddedEditorDescriptor(
			IEditorInput input) {
		String extension = input.getName().toLowerCase();
		if (extension.contains(".")) {
			extension = extension.substring(extension.lastIndexOf("."));
		}
		return ExtensionManager
				.getEmbeddedEditorDescriptorByExtension(extension);
	}

	private IContentSourceManager getContentSourceManager()
			throws ContentException {
		try {
			return S60DesignProjectNature
					.getUIDesignData(((IFileEditorInput) editor
							.getEditorInput()).getFile().getProject());
		} catch (CoreException e) {
			throw new ContentException(e);
		}
	}

	public void initialize(IEditorInput input, IProgressMonitor monitor)
			throws ContentException, IOException {
		IContent providedContent = (IContent) input.getAdapter(IContent.class);
		if (providedContent != null) {
			addContent(providedContent);
			activeContent = providedContent;
			return;
		}
		IContentSourceManager manager = getContentSourceManager();

		if (!ScreenUtil.isPrimaryContentInput(input)) {
			// opens the embedded editor in standalone mode, need to load
			// primary content but no need to load other embedded contents
			IEmbeddedEditorDescriptor desc = getEmbeddedEditorDescriptor(input);
			if (desc == null) {
				throw new ContentException(
						MessageFormat
								.format(
										EditorMessages.Editor_Error_MissingEmbeddedEditorDescriptor,
										new Object[] { input.getName() }));
			}
			IEmbeddedEditorPart embeddedEditor = editor.createEmbeddedEditor(
					desc, input);
			List<IContent> embeddedContents = manager.getRootContents(desc
					.getContentType(), embeddedEditor, monitor);
			if (!embeddedContents.isEmpty()) {
				activeContent = embeddedContents.get(0);
				editors.put(activeContent, embeddedEditor);
				addContent(activeContent);
			}

			// tries to find the primary content
			for (String type : AbstractContentSourceManager.getContentTypes()) {
				if (!ScreenUtil.isPrimaryContent(type)) {
					// ignores secondary
					continue;
				}
				List<IContent> contents = manager
						.getRootContents(type, monitor);
				if (!contents.isEmpty()) {
					IContent content = contents.get(0);
					editors.put(content, editor);
					addContent(content);
					break;
				}
			}
		} else {
			// opens the primary editor in the managed mode, load all embedded
			// contents checks if the contents have been loaded already in the current
			// session, e.g. by the open project action
			List<IContent> contents = null;
			try {
				contents = (List<IContent>) editor.getProject()
						.getSessionProperty(IS60IDEConstants.CONTENTS_NAME);
				editor.getProject().setSessionProperty(
						IS60IDEConstants.CONTENTS_NAME, null);
			} catch (CoreException e) {
				throw new ContentException(e);
			}

			
			boolean foundPrimaryContent = false;
			if (contents == null) {
				contents = new ArrayList<IContent>();

				// first checks the input, user may want to open a particular
				// theme in a navigator in case there are multiple themes
				for (String type : AbstractContentSourceManager
						.getContentTypes()) {
					contents.addAll(manager.getRootContents(type, input,
							monitor));
				}

				for (IContent content : contents) {
					if (ScreenUtil.isPrimaryContent(content)) {
						foundPrimaryContent = true;
						editors.put(content, editor);
						addContent(content);
						break;
					}
				}

				contents.clear();

				// then checks all available contents in the project
				for (String type : AbstractContentSourceManager
						.getContentTypes()) {
					contents.addAll(manager.getRootContents(type, monitor));
				}
			}

			for (IContent content : contents) {
				IEmbeddedEditorDescriptor descriptor = ExtensionManager
						.getEmbeddedEditorDescriptorByContentType(content
								.getType());
				if (descriptor != null) {
					IContent resolved = null;
					IFile file = (IFile) content.getAdapter(IFile.class);
					if (editor.getEditorInput() instanceof IFileEditorInput) {
						if (file.equals(((IFileEditorInput) editor
								.getEditorInput()).getFile())) {
							continue;
						}
					}
					// for embedded content we need to create editor parts
					IEditorInput embeddedInput = new FileEditorInput(file);
					IEmbeddedEditorCustomizer customizer = descriptor
							.getEmbeddedEditorCustomizer();
					if (!customizer.isDefaultForContent(content)) {
						// no need to create editor
						resolved = content;
					} else {
						IEmbeddedEditorPart embeddedEditor = editor
								.createEmbeddedEditor(descriptor, embeddedInput);
						List<IContent> embeddedContents = manager
								.getRootContents(descriptor.getContentType(),
										embeddedEditor, monitor);
						if (!embeddedContents.isEmpty()) {
							resolved = embeddedContents.get(0);
							editors.put(resolved, embeddedEditor);
							if (activeContent == null
									&& embeddedInput.equals(input)) {
								activeContent = resolved;
							}
						}
					}
					if (resolved != null) {
						addContent(resolved);
					}
				} else if (ScreenUtil.isPrimaryContent(content)) {
					if (!foundPrimaryContent) {
						foundPrimaryContent = true;

						editors.put(content, editor);
						addContent(content);
					}
				} else {
					// dummy contents
					addContent(content);
				}
			}
		}
	}

	public IContent getPrimaryContent() {
		for (IContent content : getActiveContents()) {
			if (ScreenUtil.isPrimaryContent(content.getType())) {
				return content;
			}
		}
		return null;
	}

	/**
	 * Rebuilds the model that is related to given content.
	 * 
	 * @param content the content from where to rebuild the model.
	 * @return the new viewer.
	 */
	public void rebuildModel(IProgressMonitor monitor) throws ContentException {
		clearGalleryScreens();
		// disposeViewers();

		screenContext = createScreenContext(getPrimaryContent());
	}

	public GraphicalViewer getDefaultViewer(IProgressMonitor monitor) {
		if (getGalleryScreens().isEmpty()) {
			return null;
		}

		IContentData data = getGalleryScreens().get(0).getData();
		String defaultName = S60WorkspacePlugin.getDefault()
				.getPreferenceStore().getString(
						IS60IDEConstants.PREF_SCREEN_DEFAULT + "."
								+ data.getRoot().getType());
		IGalleryScreen defaultScreen = null;
		for (IGalleryScreen screen : getGalleryScreens()) {
			if (defaultScreen == null) {
				defaultScreen = screen;
			}
			if ((ScreenUtil.isPrimaryContent(getActiveContent()) && screen
					.getName().equals(defaultName))) {
				return screen.getViewer(monitor);
			}
		}
		return defaultScreen == null ? null : defaultScreen.getViewer(monitor);
	}

	/**
	 * Creates a new screen context based on the information specified in the
	 * given theme.
	 * 
	 * @param screen the screen from where the screen context is obtained.
	 * @return the new screen themeContext.
	 */
	protected ScreenContext createScreenContext(IContentData screen) {
		com.nokia.tools.platform.core.Display display = screen == null ? null
				: (com.nokia.tools.platform.core.Display) screen
						.getAttribute(ContentAttribute.DISPLAY.name());
		if (display == null) {
			display = new com.nokia.tools.platform.core.Display(352, 416);
		}
		return new ScreenContext(display);
	}

	/**
	 * Builds the top-level editpart's model.
	 * 
	 * @param screenData the content from where the model information is
	 *            retrieved.
	 * @return the top-level editpart's model.
	 */
	protected EditDiagram buildModel(IContentData screenData,
			IProgressMonitor monitor) throws ContentException {
		boolean isTaskOwner = false;
		if (monitor == null) {
			monitor = ThreadUtils.displaySynched(editor.getProgressMonitor());
			isTaskOwner = true;
			monitor.beginTask(EditorMessages.Editor_Task_BuildingModel,
					TOTAL_WORK);
		} else {
			monitor = ThreadUtils.displaySynched(monitor);
		}
		monitor.subTask(EditorMessages.Editor_Task_CreatingScreenElements);

		try {
			final EditDiagram model = new ScreenEditingModelFactory()
					.createDiagram();

			if (monitor.isCanceled()) {
				throw new IllegalStateException();
			}

			monitor.worked(10);

			com.nokia.tools.platform.core.Display display = (com.nokia.tools.platform.core.Display) screenData
					.getAttribute(ContentAttribute.DISPLAY.name());
			ScreenContext context = screenContext;
			if (display != null && !screenContext.getDisplay().equals(display)) {
				context = createScreenContext(screenData);
			}

			final IScreenElement screen = createScreen(context, screenData);

			if (monitor.isCanceled()) {
				throw new IllegalStateException();
			}

			if (screen != null) {
				screen.adaptToScreen(context);
				// this also needs to be synchronized and run in the display
				// thread
				screen.selfDispatch(new Runnable() {
					public void run() {
						model.getEditObjects().add(
								(EditObject) screen.getWidget());
					}
				});
			}

			if (monitor.isCanceled()) {
				throw new IllegalStateException();
			}

			monitor.worked(10);

			if (monitor.isCanceled()) {
				throw new IllegalStateException();
			}

			monitor.worked(10);

			int numberOfWorks = 0;
			if (screen != null) {
				numberOfWorks++;
			}
			if (!ScreenUtil.isPrimaryContent(screenData)) {
				numberOfWorks++;
			}
			int totalWork = numberOfWorks == 0 ? SCREEN_BUILD_WORK - 30
					: (SCREEN_BUILD_WORK - 30) / numberOfWorks;
			Counter counter = new Counter();

			if (screen != null) {
				countScreenElements(counter, screen);
				int slice = totalWork / counter.count;
				if (monitor.isCanceled()) {
					throw new IllegalStateException();
				}
				// builds the screen by adding the children to their parents

				buildScreen(screen, context, monitor, slice);
			}

			if (!ScreenUtil.isPrimaryContent(screenData)) {
				// then populates the embedded screen
				boolean isCustomized = false;
				IScreenCustomizer defaultCustomizer = null;
				for (IScreenCustomizer customizer : CustomizationManager
						.getCustomizers()) {
					if (customizer.isDefault()) {
						defaultCustomizer = customizer;
					} else {
						if (customizer.customizeScreen(editor, screen, monitor)) {
							isCustomized = true;
						}
					}
				}
				if (!isCustomized && defaultCustomizer != null) {
					defaultCustomizer.customizeScreen(editor, screen, monitor);
				}
			}

			if (monitor.isCanceled()) {
				throw new IllegalStateException();
			}

			return model;
		} catch (IllegalStateException e) {
			throw e;
		} catch (Throwable e) {
			throw new ContentException(e);
		} finally {
			if (isTaskOwner) {
				monitor.done();
			}
		}
	}

	/**
	 * Counts the total number of screen elements and stores the count into the
	 * counter object.
	 * 
	 * @param counter the counter object holding the current screen element
	 *            count.
	 * @param element the starting element.
	 */
	private void countScreenElements(Counter counter, IScreenElement element) {
		counter.count++;
		for (IScreenElement child : element.getChildren()) {
			countScreenElements(counter, child);
		}
	}

	/**
	 * Builds the screen elements recursively.
	 * 
	 * @param screen the top-level screen element.
	 * @param context the current screen context.
	 * @param addToParent true if the child element should be added to the
	 *            parent, false otherwise. <b>Note: A single screen element can
	 *            be added to only one parent, otherwise multiple instances will
	 *            be created in the target vm.</b>
	 */
	protected void buildScreen(IScreenElement screen, IScreenContext context,
			IProgressMonitor monitor, int slice) {
		if (monitor == null) {
			monitor = ThreadUtils.displaySynched(editor.getProgressMonitor());
		}
		if (monitor.isCanceled()) {
			throw new IllegalStateException();
		}

		screen.adaptToScreen(context);

		if (monitor != null) {
			monitor.worked(slice);
		}

		if (monitor.isCanceled()) {
			throw new IllegalStateException();
		}

		screen.addWidgetToParent();


		if (monitor.isCanceled()) {
			throw new IllegalStateException();
		}

		for (IScreenElement element : screen.getChildren()) {
			buildScreen(element, context, monitor, slice);
		}
	}

	protected IContentData getDefaultScreen(IContent content) {
		IContentData defaultScreen = null;
		IScreenFactory factory = (IScreenFactory) content
				.getAdapter(IScreenFactory.class);
		for (IContentData child : factory.getScreens()) {
			IScreenAdapter adapter = (IScreenAdapter) child
					.getAdapter(IScreenAdapter.class);
			if (defaultScreen == null) {
				defaultScreen = child;
			}
			if (S60WorkspacePlugin.getDefault().getPreferenceStore().getString(
					IS60IDEConstants.PREF_SCREEN_DEFAULT + "."
							+ child.getRoot().getType())
					.equals(child.getName())
					&& content.getAttribute(ContentAttribute.DISPLAY.name())
							.equals(
									child.getAttribute(ContentAttribute.DISPLAY
											.name()))) {
				return child;
			}
		}
		return defaultScreen;
	}

	/**
	 * Creates the screen element tree.
	 * 
	 * @param screenContext the current screen context.
	 * @param data the content data.
	 * @return the screen element tree.
	 */
	public IScreenElement createScreen(IScreenContext context, IContentData data) {
		IScreenAdapter adapter = null;
		if (data instanceof IContent) {
			IScreenAdapter defaultAdapter = null;
			IScreenFactory factory = (IScreenFactory) data
					.getAdapter(IScreenFactory.class);
			for (IContentData child : factory.getScreens()) {
				defaultAdapter = (IScreenAdapter) child
						.getAdapter(IScreenAdapter.class);
				break;
			}
			if (adapter == null) {
				adapter = defaultAdapter;
			}
		} else {
			adapter = (IScreenAdapter) data.getAdapter(IScreenAdapter.class);
		}
		if (adapter == null) {
			return null;
		}
		IScreenElement screenElement = adapter.buildScreen(context);
		return screenElement;
	}

	public IEditorPart getEditor(IContentData data) {
		return editors.get(data.getRoot());
	}

	public IContent getActiveContent() {
		return activeContent;
	}

	public Set<IContent> getActiveContents() {
		return activeContents;
	}

	public IContent getContent(EditPartViewer viewer) {
		for (IContentData data : viewers.keySet()) {
			if (viewers.get(data) == viewer) {
				return data.getRoot();
			}
		}
		return null;
	}

	public IEditorPart getEditor(EditPartViewer viewer) {
		for (IContentData data : viewers.keySet()) {
			if (viewers.get(data) == viewer) {
				return editors.get(data.getRoot());
			}
		}
		return null;
	}

	public Collection<IEditorPart> getEditors() {
		return editors.values();
	}

	public void refreshViewers() {
		for (EditPartViewer viewer : viewers.values()) {
			viewer.deselectAll();
		}
	}

	public GraphicalViewer getDefaultViewer(IContent content,
			IProgressMonitor monitor) {
		IScreenFactory factory = (IScreenFactory) content
				.getAdapter(IScreenFactory.class);
		for (IContentData screen : factory.getScreens()) {
			GraphicalViewer viewer = getViewer(screen, false, monitor);
			if (viewer != null) {
				return viewer;
			}
		}
		return null;
	}

	public GraphicalViewer getViewer(IEditorInput input,
			IProgressMonitor monitor) {
		boolean editorExists = false;
		for (IContent content : editors.keySet()) {
			IEditorPart editor = editors.get(content);
			if (editor.getEditorInput().equals(input)) {
				editorExists = true;
				GraphicalViewer viewer = getDefaultViewer(content, monitor);
				if (viewer != null) {
					return viewer;
				}
			}
		}
		if (!editorExists && getActiveContent() != null
				&& ScreenUtil.isPrimaryContent(getActiveContent())) {
			IContent loadedContent = null;
			for (Iterator<IContent> i = getActiveContents().iterator(); i
					.hasNext();) {
				IContent activeContent = i.next();
				if (!ScreenUtil.isPrimaryContent(activeContent)
						&& (((IFileEditorInput) input).getFile()
								.equals(activeContent.getAdapter(IFile.class)))) {
					// creates new embedded editor
					IEmbeddedEditorDescriptor descriptor = ExtensionManager
							.getEmbeddedEditorDescriptorByContentType(activeContent
									.getType());
					IEmbeddedEditorPart embeddedEditor = editor
							.createEmbeddedEditor(descriptor, input);
					try {
						List<IContent> contents = getContentSourceManager()
								.getRootContents(descriptor.getContentType(),
										embeddedEditor, monitor);
						if (!contents.isEmpty()) {
							loadedContent = contents.get(0);
							editors.put(loadedContent, embeddedEditor);
							i.remove();
							contentRemoved(activeContent);
							break;
						}
					} catch (Exception e) {
						S60WorkspacePlugin.error(e);
					}
				}
			}
			if (loadedContent != null) {
				addContent(loadedContent);
				return getDefaultViewer(loadedContent, monitor);
			}
		}
		return null;
	}

	public boolean isEmbeddedEditorDirty() {
		for (IEditorPart editor : editors.values()) {
			if (editor instanceof IEmbeddedEditorPart && editor.isDirty()) {
				return true;
			}
		}
		return false;
	}

	public IContent extractContent(EditPartViewer viewer) {
		if (viewer != null) {
			EditPart contents = viewer.getContents();
			if (contents != null) {
				EditDiagram comp = (EditDiagram) contents.getModel();
				if (comp != null) {
					List screens = comp.getEditObjects();
					if (!screens.isEmpty()) {
						IScreenElement element = JEMUtil
								.getScreenElement((EObject) screens.get(0));
						if (element != null) {
							return element.getData().getRoot();
						}
					}
				}
			}
		}
		return null;
	}

	public IContentData getScreenByViewer(EditPartViewer viewer) {
		for (IContentData data : viewers.keySet()) {
			EditPartViewer v = viewers.get(data);
			if (v == viewer) {
				return data;
			}
		}
		return null;
	}

	/**
	 * Creates the editpart viewer for the given screen. If the viewer has been
	 * created already, this will return immediately.
	 * 
	 * @param screenData the screen content data.
	 * @return the editpart viewer associated with the given screen.
	 */
	public GraphicalViewer getViewer(IContentData screenData,
			boolean disposeOldViewers, IProgressMonitor monitor) {
		return getViewer0(screenData, disposeOldViewers, monitor);
	}

	/**
	 * Creates the editpart viewer for the given screen. If the viewer has been
	 * created already, this will return immediately.
	 * 
	 * @param screenData the screen content data.
	 * @return the editpart viewer associated with the given screen.
	 */
	private GraphicalViewer getViewer0(final IContentData screenData,
			final boolean disposeOldViewers, final IProgressMonitor monitor) {
		GraphicalViewer viewer = viewers.get(screenData);
		if (viewer == null) {
			synchronized(screenData){
				ISafeRunnable job = new SilentRunnable() {
					public void run() throws Exception {
						try {
							final EditDiagram model = buildModel(screenData,
									monitor);
							GraphicalViewer viewer = (GraphicalViewer) ThreadUtils
									.syncDisplayExec(new IInvokable.Adapter() {
										public Object invoke() {
											if (editor == null) {
												return null;
											}
											GraphicalViewer viewer = editor
													.createGraphicalViewer(model);
											if (!ScreenUtil
													.isPrimaryContent(screenData)) {
												if (disposeOldViewers) {
													IEditorPart embedded = getEditor(viewer);
													if (embedded instanceof IEmbeddedEditorPart) {
														((IEmbeddedEditorPart) embedded)
																.disposeViewers();
													}
												}
												editor
														.createEmbeddedViewerControl(
																(IEmbeddedEditorPart) getEditor(screenData),
																viewer);
											} else {
												editor.createViewerControl(viewer);
											}
											return viewer;
										}
									});
							viewers.put(screenData, viewer);
						} catch (IllegalStateException e) {
						}
					}
				};
				if (DebugHelper.debugPerformance()) {
					DebugHelper.debugTime(this, "creating viewer: "
							+ screenData.getName(), job);
				} else {
					try {
						job.run();
					} catch (Exception e) {
						S60WorkspacePlugin.error(e);
					}
				}
			}
		}
		return viewers.get(screenData);
	}

	private List<IGalleryScreen> createGalleryScreens(IContent content) {
		List<IGalleryScreen> screens = new ArrayList<IGalleryScreen>();
		IScreenFactory factory = (IScreenFactory) content
				.getAdapter(IScreenFactory.class);
		if (factory != null) {
			for (IContentData data : factory.getScreens()) {
				IScreenAdapter adapter = (IScreenAdapter) data
						.getAdapter(IScreenAdapter.class);
				if (adapter != null && adapter.isModelScreen()) {
					screens.add(new GalleryScreen(data));
				}
			}
		}
		return screens;
	}

	private void sortGalleryScreens(List<IGalleryScreen> screens) {
		Collections.sort(screens, new Comparator<IGalleryScreen>() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(T, T)
			 */
			public int compare(IGalleryScreen o1, IGalleryScreen o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.views.IGalleryScreenProvider#getGalleryScreens()
	 */
	public List<IGalleryScreen> getGalleryScreens() {
		synchronized (galleryLock) {
			if (galleryScreens == null) {
				galleryScreens = new ArrayList<IGalleryScreen>();
				for (IContent content : getActiveContents()) {
					galleryScreens.addAll(createGalleryScreens(content));
				}
				sortGalleryScreens(galleryScreens);
			}
			return galleryScreens;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.views.IGalleryScreenProvider#getSize()
	 */
	public Dimension getSize() {
		com.nokia.tools.platform.core.Display display = (com.nokia.tools.platform.core.Display) screenContext
				.getDisplay();
		return new Dimension(display.getWidth(), display.getHeight());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		editor.selectionChanged(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.views.IGalleryScreenProvider#screenCreated(com.nokia.tools.s60.views.IGalleryScreenProvider.IGalleryScreen)
	 */
	public void screenCreated(IGalleryScreen screen) {
		updateGalleryPreferences(screen, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.views.IGalleryScreenProvider#screenDisposed(com.nokia.tools.s60.views.IGalleryScreenProvider.IGalleryScreen)
	 */
	public void screenDisposed(IGalleryScreen screen) {
		updateGalleryPreferences(screen, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider#galleryDisposed()
	 */
	public void galleryDisposed() {
		if (isDisposed()) {
			disposeContents();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider#isDisposed()
	 */
	public boolean isDisposed() {
		return isDisposed;
	}

	protected boolean isGalleryPersistable() {
		return ScreenUtil.isPrimaryContent(getActiveContent());
	}

	/**
	 * Updates the preferences when user adds/removes gallery screens.
	 * 
	 * @param newScreen the new screen to be added. 
	 */
	protected void updateGalleryPreferences(IGalleryScreen screenEffected,
			boolean isDelete) {
		if (!isGalleryPersistable()) {
			return;
		}

		IPreferenceStore store = S60WorkspacePlugin.getDefault()
				.getPreferenceStore();
		String prefName = IS60IDEConstants.PREF_GALLERY_SCREENS + "."
				+ screenEffected.getData().getRoot().getType();
		String screens = store.getString(prefName);
		StringBuffer sb = new StringBuffer();
		if (screens != null) {
			String[] strs = screens.split(",");
			for (String str : strs) {
				if (isDelete && screenEffected != null
						&& str.equals(screenEffected.getName())) {
					continue;
				}
				sb.append(str + ",");
			}
		}
		if (!isDelete && screenEffected != null) {
			sb.append(screenEffected.getName() + ",");
		}
		if (sb.length() > 0) {
			// removes the trailing ","
			sb.deleteCharAt(sb.length() - 1);
		}
		synchronized (this) {
			suppressPreferenceChangeEvents = true;
			try {
				store.setValue(prefName, sb.toString());
				if (store.needsSaving()
						&& store instanceof IPersistentPreferenceStore) {
					try {
						((IPersistentPreferenceStore) store).save();
					} catch (IOException e) {
						S60WorkspacePlugin.error(e);
					}
				}
			} finally {
				suppressPreferenceChangeEvents = false;
			}
		}
	}

	/**
	 * Saves the contents managed by the associ editor.
	 */
	public synchronized void saveContents(IProgressMonitor monitor)
			throws ContentException, IOException {
		for (IContent activeContent : getActiveContents()) {
			IContentSynchronizer sync = (IContentSynchronizer) activeContent
					.getAdapter(IContentSynchronizer.class);
			if (sync != null) {
				sync.enterDesignMode();
			}
			try {
				activeContent.save(monitor);
			} finally {
				if (sync != null) {
					sync.leaveDesignMode();
				}
			}
		}

		
		for (IContent activeContent : getActiveContents()) {
			if ("XMLUI".equals(activeContent.getType())) {
				List<IContent> contents = getContentSourceManager()
						.getRootContents("DummyData", new NullProgressMonitor());
				if (contents.size() > 0) {
					contents.get(0).save(monitor);
				}
				break;
			}
		}

		for (IContent content : editors.keySet()) {
			IContentSynchronizer sync = (IContentSynchronizer) content
					.getAdapter(IContentSynchronizer.class);
			if (sync != null) {
				sync.enterDesignMode();
			}
			try {
				IEditorPart editor = editors.get(content);
				if (editor instanceof IEmbeddedEditorPart) {
					editor.doSave(monitor);
				}
			} finally {
				if (sync != null) {
					sync.leaveDesignMode();
				}
			}
		}
	}

	public void dispose() {
		try {
			stop();
		} catch (Throwable e) {
			S60WorkspacePlugin.error(e);
		}
		try {
			
			disposeViewers();
		} catch (Throwable e) {
			S60WorkspacePlugin.error(e);
		}
		try {
			disposeContents();
		} catch (Throwable e) {
			S60WorkspacePlugin.error(e);
		} finally {
			isDisposed = true;
		}
		try {
			disposeEditors();
		} catch (Throwable e) {
			S60WorkspacePlugin.error(e);
		}
		editor = null;
		S60WorkspacePlugin.getDefault().getPreferenceStore()
				.removePropertyChangeListener(this);
	}

	public void disposeViewer(final GraphicalViewer viewer) {
		if (viewer == null) {
			return;
		}
		for (Iterator<GraphicalViewer> i = viewers.values().iterator(); i
				.hasNext();) {
			if (i.next() == viewer) {
				i.remove();
			}
		}
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				SelectionSynchronizer synchronizer = (SelectionSynchronizer) editor
						.getAdapter(SelectionSynchronizer.class);
				if (synchronizer != null) {
					synchronizer.removeViewer(viewer);
				}
				MenuManager manager = viewer.getContextMenu();
				if (manager != null) {
					manager.dispose();
				}
				Control control = viewer.getControl();
				if (control != null && !control.isDisposed()
						&& control.getParent() != null) {
					control.getParent().dispose();
				}
			}
		});
		removeAdapters((EObject) viewer.getContents().getModel());
	}

	protected void removeAdapters(EObject object) {
		EList<Adapter> adapters = object.eAdapters();
		while (!adapters.isEmpty()) {
			adapters.remove(adapters.size() - 1);
		}
		if (object instanceof EditDiagram) {
			for (EObject child : ((EditDiagram) object).getEditObjects()) {
				removeAdapters(child);
			}
		} else if (object instanceof EditObject) {
			for (EObject child : ((EditObject) object).getChildren()) {
				removeAdapters(child);
			}
		}
	}

	public void disposeEmbeddedViewers() {
		for (Object data : viewers.keySet().toArray()) {
			if (!ScreenUtil.isPrimaryContent((IContentData) data)) {
				disposeViewer(viewers.get((IContentData) data));
			}
		}
	}

	public void disposeViewers() {
		for (Object viewer : viewers.values().toArray()) {
			disposeViewer((GraphicalViewer) viewer);
		}
		viewers.clear();
	}

	public void disposeContents() {
		for (IContent activeContent : getActiveContents()) {
			contentRemoved(activeContent);
			activeContent.dispose();
		}
		getActiveContents().clear();
	}

	public void disposeOldContents() {
		try {
			
			disposeViewers();
			disposeContents();
			disposeEditors();
		} catch (Throwable e) {
			S60WorkspacePlugin.error(e);
		} finally {
		}
	}

	public void disposeEditors() {
		for (IEditorPart editor : editors.values()) {
			if (editor instanceof IEmbeddedEditorPart) {
				editor.removePropertyListener(ScreenModelMediator.this.editor);
				IContentService adapter = (IContentService) editor
						.getAdapter(IContentService.class);
				if (adapter != null) {
					adapter.removeContentListener(this);
				}
				editor.dispose();
			}
		}
		editors.clear();
	}

	public void clearGalleryScreens() {
		galleryScreens = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentListener#contentChanged(com.nokia.tools.content.core.IContent)
	 */
	public void rootContentChanged(IContent content) {
		contentNotification = content;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (!suppressPreferenceChangeEvents
				&& (IS60IDEConstants.PREF_GALLERY_SCREENS + "." + getActiveContent()
						.getType()).equals(event.getProperty())) {
			editor.refreshGallery();
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this, "ScreenMediator");
			thread.start();
		}
	}

	public void stop() {
		if (thread != null) {
			Thread tmp = thread;
			thread = null;
			tmp.interrupt();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (Thread.currentThread() == thread) {
			if (contentNotification != null) {
				IContent newContent = null;
				IProgressMonitor monitor = ThreadUtils.displaySynched(editor
						.getProgressMonitor());
				try {
					List<IContent> contents = getContentSourceManager()
							.getRootContents(contentNotification.getType(),
									contentNotification, monitor);
					contentNotification = null;
					if (!contents.isEmpty()) {
						newContent = contents.get(0);
					}
				} catch (Exception e) {
					S60WorkspacePlugin.error(e);
				}
				if (newContent == null) {
					continue;
				}
				monitor.beginTask(EditorMessages.Editor_Task_SyncingViewer,
						SCREEN_BUILD_WORK);

				Set<IContent> oldContents = replaceContent(newContent);

				// replacing gallery screens
				synchronized (galleryLock) {
					if (galleryScreens != null) {
						for (Iterator<IGalleryScreen> i = galleryScreens
								.iterator(); i.hasNext();) {
							IGalleryScreen screen = i.next();
							if (oldContents
									.contains(screen.getData().getRoot())) {
								i.remove();
							}
						}
						galleryScreens.addAll(createGalleryScreens(newContent));
						sortGalleryScreens(galleryScreens);
					}
				}

				GraphicalViewer viewer = null;
				IScreenFactory factory = (IScreenFactory) newContent
						.getAdapter(IScreenFactory.class);
				for (IContentData data : factory.getScreens()) {
					viewer = getViewer(data, true, monitor);
					break;
				}
				if (viewer != null) {
					final GraphicalViewer newViewer = viewer;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							editor.refreshViewer(newViewer);
						}
					});
				}
				monitor.done();
			}
			try {
				Thread.sleep(MONITOR_INTERVAL);
			} catch (InterruptedException e) {
			}
		}
	}
	/**
	* A simple structure for storing the counter value 
	*/
	
	class Counter {
		int count;
	}

	// needed for IconView & properties
	public IScreenContext getContext() {
		return screenContext;
	}

	class GalleryScreen implements IGalleryScreen {
		IContentData data;
		Control control;

		GalleryScreen(IContentData data) {
			this.data = data;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.s60.views.IGalleryScreenProvider.IGalleryScreen#setControl(org.eclipse.swt.widgets.Control)
		 */
		public void setControl(Control control) {
			this.control = control;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.s60.views.IGalleryScreenProvider.IGalleryScreen#getControl()
		 */
		public Control getControl() {
			return control;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.s60.views.IGalleryScreenProvider.IGalleryScreen#getName()
		 */
		public String getName() {
			return data.getName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.s60.views.IGalleryScreenProvider.IGalleryScreen#isDefault()
		 */
		public boolean isDefault() {
			if (!ScreenUtil.isPrimaryContent(data)) {
				if (data.getRoot() == getActiveContent()) {
					// embedded editor opened directly so we show the
					// gallery
					return true;
				}
				if (ScreenUtil.isPrimaryContent(getActiveContent())) {
					IEmbeddedEditorDescriptor descriptor = ExtensionManager
							.getEmbeddedEditorDescriptorByContentType(data
									.getRoot().getType());
					IEmbeddedEditorCustomizer customizer = descriptor
							.getEmbeddedEditorCustomizer();
					return customizer != null
							&& customizer.isDefaultForContent(data.getRoot());
				}
			}
			String[] galleryScreens = S60WorkspacePlugin.getDefault()
					.getPreferenceStore().getString(
							IS60IDEConstants.PREF_GALLERY_SCREENS + "."
									+ data.getRoot().getType()).split(",");
			for (String screen : galleryScreens) {
				if (screen.trim().equals(data.getName())) {
					return true;
				}
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.s60.views.IGalleryScreenProvider.IGalleryScreen#isActive()
		 */
		public boolean isActive() {
			if(editor!=null){
				return viewers.get(data) == editor.getEditPartViewer();
			}else
				return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.s60.views.IGalleryScreenProvider.IGalleryScreen#getViewer(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public GraphicalViewer getViewer(IProgressMonitor monitor) {
			return ScreenModelMediator.this.getViewer(data, false, monitor);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.s60.views.IGalleryScreenProvider.IGalleryScreen#getData()
		 */
		public IContentData getData() {
			return data;
		}
	}

	class ScreenContext implements IScreenContext {
		com.nokia.tools.platform.core.Display display;

		ScreenContext(com.nokia.tools.platform.core.Display display) {
			this.display = display;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenContext#getDisplay()
		 */
		public com.nokia.tools.platform.core.Display getDisplay() {
			return display;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			return editor.getAdapter(adapter);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentListener#contentModified(com.nokia.tools.content.core.IContentDelta)
	 */
	public void contentModified(IContentDelta delta) {
		if (0 != (delta.getType())) {
			IContentData screen = delta.getAddedContent().get(0);
			IScreenFactory factory = (IScreenFactory) screen.getRoot()
					.getAdapter(IScreenFactory.class);
			screen = factory.getScreenForData(screen, false);
			if (null != screen) {
				IProgressMonitor monitor = editor.getProgressMonitor();
				
				monitor.beginTask(EditorMessages.Editor_Task_BuildingModel,
						TOTAL_WORK);
				if (null != getViewer(screen, false, monitor)) {

					IScreenAdapter screenAdapter = (IScreenAdapter) screen
							.getAdapter(IScreenAdapter.class);
					screenAdapter.updateScreen(delta, monitor);
				}
				monitor.done();
			}
		}
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IScreenContext.class)
			return getContext();
		return null;
	}

	/**
	 * 
	 * @param content
	 */
	private void contentRemoved(IContent content) {
		IContentService notificationService = (IContentService) content
				.getAdapter(IContentService.class);
		if (null != notificationService) {
			notificationService.removeContentListener(this);
		}
	}

	/**
	 * Helper method grouping routines that happens when content added to
	 * mediator
	 * 
	 * @param content
	 */
	private void addContent(IContent content) {
		activeContents.add(content);
		IContentService notificationService = (IContentService) content
				.getAdapter(IContentService.class);
		if (null != notificationService) {
			notificationService.addContentListener(this);
		}
		if (activeContent == null) {
			activeContent = getPrimaryContent();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider#addScreen(java.lang.String,
	 *      java.lang.String)
	 */
	public void addScreen(String themeIdentifier, String screenName) {

		/*
		 * note: themeIdentifier is appUid + _ + themeUid
		 */

		try {
			if (getContentSourceManager() == null) {
				return;
			}
			IContentSourceManager manager = getContentSourceManager();
			List<IContent> contents = new ArrayList<IContent>();
			for (String type : AbstractContentSourceManager.getContentTypes()) {
				if (!ScreenUtil.isPrimaryContent(type)) {
					contents.addAll(manager.getRootContents(type, null));
				}
			}

			IContent content = null;

			for (IContent cntnt : contents) {

				Object app_uid = cntnt.getAttribute(ContentAttribute.APP_UID
						.name());
				Object theme_uid = cntnt
						.getAttribute(ContentAttribute.THEME_UID.name());
				String themeName = cntnt.getName();
				String cntntThemeIdentifier = app_uid + "_" + theme_uid;

				if (screenName.equals(themeName)
						&& themeIdentifier.equals(cntntThemeIdentifier)) {

					IContent resolved = null;
					IEmbeddedEditorDescriptor descriptor = ExtensionManager
							.getEmbeddedEditorDescriptorByContentType(cntnt
									.getType());
					if (descriptor != null) {

						IFile file = (IFile) cntnt.getAdapter(IFile.class);
						if (editor.getEditorInput() instanceof IFileEditorInput) {
							if (file.equals(((IFileEditorInput) editor
									.getEditorInput()).getFile())) {
								continue;
							}
						}

						IEditorInput embeddedInput = new FileEditorInput(file);
						IEmbeddedEditorCustomizer customizer = descriptor
								.getEmbeddedEditorCustomizer();
						if (!customizer.isDefaultForContent(cntnt)) {
							resolved = cntnt;
						} else {
							IEmbeddedEditorPart embeddedEditor = editor
									.createEmbeddedEditor(descriptor,
											embeddedInput);
							List<IContent> embeddedContents = manager
									.getRootContents(descriptor
											.getContentType(), embeddedEditor,
											null);
							if (!embeddedContents.isEmpty()) {
								resolved = embeddedContents.get(0);
								editors.put(resolved, embeddedEditor);
							}
						}
					} else {
						resolved = cntnt;
						editors.put(resolved, editor);
					}
					if (resolved != null) {
						addContent(resolved);
						content = resolved;
					}

				}
			}

			synchronized (galleryLock) {
				if (content != null) {
					galleryScreens.addAll(createGalleryScreens(content));
					sortGalleryScreens(galleryScreens);
				} else {
					S60WorkspacePlugin
							.error("ScreenModelMediator.addScreen(): Cannot localize screen: "
									+ themeIdentifier);
				}
			}

			IGalleryAdapter adapter = (IGalleryAdapter) editor
					.getAdapter(IGalleryAdapter.class);

			adapter.notifyGalleryChanged();

			ResourceViewPart rv = (ResourceViewPart) PlatformUI.getWorkbench()
					.getWorkbenchWindows()[0].getActivePage().findView(
					ViewIDs.RESOURCE_VIEW_ID);
			if (rv != null)
				rv.refresh();
			ResourceView2 rv2 = (ResourceView2) PlatformUI.getWorkbench()
					.getWorkbenchWindows()[0].getActivePage().findView(
					ViewIDs.RESOURCE_VIEW2_ID);

			if (rv2 != null)
				rv2.refresh();

		} catch (ContentException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	public void removeScreen(String themeIdentifier, String screenName) {

		try {
			List<IContent> contentForRemoval = new ArrayList<IContent>();
			ArrayList<IContentData> screensForRemoval = new ArrayList<IContentData>();
			for (IContent cntnt : activeContents) {

				Object uid = cntnt
						.getAttribute(ContentAttribute.APP_UID.name());
				String themeName = cntnt.getName();
				Object themeUid = cntnt.getAttribute(ContentAttribute.THEME_UID
						.name());
				String cntThemeIdent = uid + "_" + themeUid;

				if (screenName.equals(themeName)
						&& themeIdentifier.equals(cntThemeIdent)) {
					contentForRemoval.add(cntnt);

					IContentData[] children = cntnt.getAllChildren();
					for (int i = 0; i < children.length; i++) {
						
						if (children[i].getName().equalsIgnoreCase(screenName)) {
							screensForRemoval.add(children[i]);
						}
					}
				}
			}
			for (IContent content : contentForRemoval) {
				for (IContentData screenForRemoval : screensForRemoval) {
					for (IGalleryScreen gScreen : galleryScreens) {
						if (gScreen.getData() == screenForRemoval) {
							galleryScreens.remove(gScreen);
							break;
						}
					}
				}

				IGalleryAdapter adapter = (IGalleryAdapter) editor
						.getAdapter(IGalleryAdapter.class);

				activeContents.remove(content);
				contentRemoved(content);
				content.dispose();

				adapter.notifyGalleryChanged();

				
				if (editors.get(content) instanceof IEmbeddedEditorPart) {
					editors.get(content).removePropertyListener(ScreenModelMediator.this.editor);
					IContentService adapter1 = (IContentService) editors.get(content)
							.getAdapter(IContentService.class);
					if (adapter1 != null) {
						adapter1.removeContentListener(this);
					}
					editors.get(content).dispose();
				}
				


				GraphicalViewer viewerToShowInEditor = null;
				
				for (IContentData scr : screensForRemoval) {
					GraphicalViewer graphicalViewer = viewers.get(scr);
					disposeViewer(graphicalViewer);
				}
				
				for (GraphicalViewer viewer : viewers.values()) {
					 viewerToShowInEditor = viewer;
					break;
				}
				
				editor.refreshViewer(viewerToShowInEditor);

				ResourceViewPart rv = (ResourceViewPart) PlatformUI
						.getWorkbench().getWorkbenchWindows()[0]
						.getActivePage().findView(ViewIDs.RESOURCE_VIEW_ID);
				if (rv != null)
					rv.refresh();

				ResourceView2 rv2 = (ResourceView2) PlatformUI.getWorkbench()
						.getWorkbenchWindows()[0].getActivePage().findView(
						ViewIDs.RESOURCE_VIEW2_ID);

				if (rv2 != null)
					rv2.refresh();

				
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	public IGalleryScreen addScreen(final IContentData screenData) {
		contentModified(new IContentDelta() {
			public List<IContentData> getAddedContent() {
				List<IContentData> added = new ArrayList<IContentData>();
				added.add(screenData);
				return added;
			}

			public int getType() {
				return IContentDelta.ADDED;
			}

			public List<String> getAffectedElementIDs() {
				return null;
			}

			public List<IContentData> getRemovedContent() {
				return null;
			}
		});

		IGalleryScreen galleryScreen = new GalleryScreen(screenData);

		galleryScreens.add(new GalleryScreen(screenData));
		sortGalleryScreens(galleryScreens);

		IGalleryAdapter adapter = (IGalleryAdapter) editor
				.getAdapter(IGalleryAdapter.class);

		adapter.notifyGalleryChanged();

		return galleryScreen;
	}
}
