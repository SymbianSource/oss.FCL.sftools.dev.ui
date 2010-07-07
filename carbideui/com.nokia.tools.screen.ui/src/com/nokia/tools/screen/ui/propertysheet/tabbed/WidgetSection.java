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
package com.nokia.tools.screen.ui.propertysheet.tabbed;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.command.CommandBuilder;
import com.nokia.tools.screen.core.ICategoryAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.ScreenEditingModelFactory;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.widget.IEnum;

public abstract class WidgetSection extends AbstractPropertySection implements
		IPropertyChangeListener, Listener {
	public static final int STANDARD_BUTTON_HEIGHT = 10;

	protected int rightMarginSpace;

	// changed to protected as some subclassed may need direct access
	protected boolean isDirty;

	private Composite parent;

	private List<Resource> resourcesToDispose = new ArrayList<Resource>();

	protected boolean suppressUIEvent;

	protected boolean suppressStackEvent;

	private WidgetTabbedPropertySheetPage rootPage;

	protected static Map<Object, Set<Control>> controlsToSync = new HashMap<Object, Set<Control>>();

	// apply values on 'enter' keystroke in text field
	protected KeyAdapter enterAdapter = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
				Event evt = new Event();
				evt.type = SWT.FocusOut;
				evt.widget = e.widget;
				handleEvent(evt);
			}
		}
	};

	protected SelectionAdapter applyChangeListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			Event evt = new Event();
			evt.type = SWT.FocusOut;
			evt.widget = e.widget;
			handleEvent(evt);
		};
	};

	protected Adapter refreshAdapter = new RefreshAdapter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		refresh();
	}

	protected void addResourceToDispose(Resource resource) {
		if (resource != null) {
			resourcesToDispose.add(resource);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public final void handleEvent(Event event) {
		if (suppressUIEvent) {
			return;
		}
		if (SWT.Modify == event.type || SWT.Selection == event.type) {
			isDirty = true;
		}

		doHandleEvent(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.ui.properties.internal.provisional.ISection#refresh()
	 */
	public final void refresh() {
		if (suppressStackEvent) {
			return;
		}
		suppressUIEvent = true;
		try {
			doRefresh();
			parent.getParent().layout();
		} finally {
			suppressUIEvent = false;
		}
	}

	public void execute(Command command) {
		CommandStack stack = (CommandStack) getPart().getAdapter(
				CommandStack.class);
		if (stack == null) {
			return;
		}
		stack.execute(command);
	}

	protected EObject doSetInput(IWorkbenchPart part, IScreenElement element) {
		return element.getWidget();
	}

	protected abstract void doHandleEvent(Event e);

	protected abstract void doRefresh();

	protected void refreshTitleBar() {
		if (rootPage != null) {
			rootPage.refreshTitleBar();
		}
	}

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		this.parent = parent;
		rootPage = (WidgetTabbedPropertySheetPage) aTabbedPropertySheetPage;
		parent.addDisposeListener(new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				for (Resource resource : resourcesToDispose) {
					resource.dispose();
				}
				resourcesToDispose.clear();
			}

		});

		// set scrollbar settings
		Composite comp = parent;
		while (comp != null && !(comp instanceof ScrolledComposite)) {
			comp.layout(true);
			comp = comp.getParent();
		}
		if (comp != null) {
			final ScrolledComposite scComp = (ScrolledComposite) comp;
			scComp.getVerticalBar().setIncrement(10);
			scComp.getHorizontalBar().setIncrement(10);
			scComp.addControlListener(new ControlListener() {
				public void controlResized(ControlEvent e) {
					scComp.getVerticalBar().setPageIncrement(
							scComp.getSize().y / 2);
					scComp.getHorizontalBar().setPageIncrement(
							scComp.getSize().x / 2);
				}

				public void controlMoved(ControlEvent e) {
				}
			});
		}
		parent.getParent().layout();
	}

	protected boolean validatePropertyValue(EStructuralFeature feature,
			Object newValue) {
		return true;
	}

	protected void applyParentAttributeSettings(CommandBuilder builder) {
	}

	protected boolean isForwardUndo() {
		return false;
	}

	protected String customizeFeatureName(EStructuralFeature feature) {
		return feature.getName();
	}

	protected void setErrorMessage(String message) {
		IStatusLineManager statusLine = getStatusLine();
		if (statusLine != null) {
			statusLine.setErrorMessage(message);
			statusLine.update(true);
		}
	}

	protected void clearErrorMessage() {
		IStatusLineManager statusLine = getStatusLine();
		if (statusLine != null) {
			statusLine.setErrorMessage(null);
			statusLine.update(false);
		}
	}

	protected IStatusLineManager getStatusLine() {
		IWorkbenchPart part = getPart().getSite().getWorkbenchWindow()
				.getActivePage().getActivePart();
		if (part instanceof IViewPart) {
			return ((IViewPart) part).getViewSite().getActionBars()
					.getStatusLineManager();
		}
		return null;
	}

	/**
	 * Get the standard label width when labels for sections line up on the left
	 * hand side of the composite. We line up to a fixed position, but if a
	 * string is wider than the fixed position, then we use that widest string.
	 * 
	 * @param parent The parent composite used to create a GC.
	 * @param labels The list of labels.
	 * @return the standard label width.
	 */
	public static int getStandardLabelWidth(Composite parent, String[] labels) {
		int standardLabelWidth = STANDARD_LABEL_WIDTH; // STANDARD_LABEL_WIDTH;
		GC gc = new GC(parent);
		int indent = gc.textExtent("XXX").x; //$NON-NLS-1$
		for (int i = 0; i < labels.length; i++) {
			int width = gc.textExtent(labels[i]).x;
			if (width + indent > standardLabelWidth) {
				standardLabelWidth = width + indent;
			}
		}
		gc.dispose();
		return standardLabelWidth;
	}

	public static int getStandardButtonHeight(Composite parent, String[] labels) {
		int standardButtonHeight = STANDARD_BUTTON_HEIGHT;
		GC gc = new GC(parent);
		for (int i = 0; i < labels.length; i++) {
			int height = gc.textExtent(labels[i]).y;
			if (height > standardButtonHeight) {
				standardButtonHeight = height;
			}
		}
		gc.dispose();
		return standardButtonHeight;
	}

	public static int getLabelWidth(Composite parent, String[] labels) {
		int standardLabelWidth = 0;
		GC gc = new GC(parent);
		// int indent = gc.textExtent("X").x; //$NON-NLS-1$
		int indent = 0;
		for (int i = 0; i < labels.length; i++) {
			int width = gc.textExtent(labels[i]).x;
			if (width + indent > standardLabelWidth) {
				standardLabelWidth = width + indent;
			}
		}
		gc.dispose();
		return standardLabelWidth;
	}

	protected static void syncWithOther(final Control control,
			final Object syncGroup) {
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				controlsToSync.get(syncGroup).remove(control);
				// syncWithOther(null, syncGroup);
			}
		});

		if (control != null && syncGroup != null) {
			Set<Control> controls = controlsToSync.get(syncGroup);
			if (controls == null) {
				controls = new HashSet<Control>();
				controlsToSync.put(syncGroup, controls);
			}
			controls.add(control);
		}

		if (syncGroup != null) {
			Set<Control> controls = controlsToSync.get(syncGroup);

			if (controls == null) {
				controls = new HashSet<Control>();
				controlsToSync.put(syncGroup, controls);
			}

			if (control != null) {
				controls.add(control);
				control.addPaintListener(new PaintListener() {
					public void paintControl(org.eclipse.swt.events.PaintEvent e) {
						Set<Control> controls = controlsToSync.get(syncGroup);

						int maxWidth = 0;
						for (Control ctrl : controls) {
							if (!ctrl.isDisposed()) {
								Point size = ctrl.computeSize(SWT.DEFAULT,
										SWT.DEFAULT);
								if (size.x > maxWidth) {
									maxWidth = size.x;
								}
							}
						}

						for (Control ctrl : controls) {
							if (!ctrl.isDisposed()) {
								Object data = ctrl.getLayoutData();
								if (data instanceof FormData) {
									FormData formData = (FormData) data;
									if (formData.width != maxWidth) {
										((FormData) data).width = maxWidth - 5;
										ctrl.getParent().layout(true);
									}
								}
								if (data instanceof GridData) {
									GridData gridData = (GridData) data;
									if (gridData.widthHint != maxWidth) {
										((GridData) data).widthHint = maxWidth - 5;
										ctrl.getParent().layout(true);
									}
								}
							}
						}
					};
				});
			}
		}
	}

	protected IEnum getIEnumValueObject(String name, Class clazz) {
		Field[] fs = clazz.getFields();
		for (Field f : fs) {
			if (Modifier.isPublic(f.getModifiers())
					&& Modifier.isStatic(f.getModifiers())
					&& Modifier.isFinal(f.getModifiers())) {
				String value;
				try {
					value = f.get(null).toString();
					if (value.equals(name)) {
						return (IEnum) f.get(null);
					}
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	protected String getIEnumDisplayName(String styleValue, Class clazz) {
		Field[] fs = clazz.getFields();
		for (Field f : fs) {
			if (Modifier.isPublic(f.getModifiers())
					&& Modifier.isStatic(f.getModifiers())
					&& Modifier.isFinal(f.getModifiers())) {
				try {
					String v = f.get(null).toString();
					if (styleValue.equals(v)) //$NON-NLS-1$
						return f.get(null).toString();
				} catch (Exception e) {
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

	protected String getIEnumValue(String name, Class clazz) {
		Field[] fs = clazz.getFields();
		for (Field f : fs) {
			if (Modifier.isPublic(f.getModifiers())
					&& Modifier.isStatic(f.getModifiers())
					&& Modifier.isFinal(f.getModifiers())) {
				String value;
				try {
					value = f.get(null).toString();
					if (value.equals(name)) {
						return f.getType().getName() + "." + f.getName(); //$NON-NLS-1$
					}
				} catch (Exception e) {
				}

			}
		}
		return ""; //$NON-NLS-1$
	}

	public static String[] listEnum(Class clazz) {
		return listEnum(clazz, false);
	}

	public static String[] listEnum(Class clazz, boolean addEmpty) {
		List<String> l = new ArrayList<String>();
		// init border style opts
		Field[] fs = clazz.getFields();
		for (Field f : fs) {
			if (Modifier.isPublic(f.getModifiers())
					&& Modifier.isStatic(f.getModifiers())
					&& Modifier.isFinal(f.getModifiers())) {
				try {
					l.add(f.get(null).toString());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		if (addEmpty) {
			l.add("");
		}
		return l.toArray(new String[l.size()]);
	}

	public EditObject createEditObject(Object object) {
		try {
			return new ScreenEditingModelFactory().createEditObject(object);
		} catch (Exception e) {
			UiPlugin.error(e);
			return null;
		}
	}

	protected void addRefreshAdapter(EObject target) {
		if (target != null && !target.eAdapters().contains(refreshAdapter)) {
			target.eAdapters().add(refreshAdapter);
		}
	}

	protected void removeRefreshAdapter(EObject target) {
		if (target != null) {
			target.eAdapters().remove(refreshAdapter);
		}
	}

	protected void addRefreshAdapter(IContentData data) {
		if (data != null) {
			ICategoryAdapter category = (ICategoryAdapter) data
					.getAdapter(ICategoryAdapter.class);
			if (category != null) {
				for (IContentData peer : category.getCategorizedPeers()) {
					addRefreshAdapter((EditObject) peer
							.getAdapter(EditObject.class));
				}
			} else {
				addRefreshAdapter((EditObject) data
						.getAdapter(EditObject.class));
			}
		}
	}

	protected void removeRefreshAdapter(IContentData data) {
		if (data != null) {
			ICategoryAdapter category = (ICategoryAdapter) data
					.getAdapter(ICategoryAdapter.class);
			if (category != null) {
				for (IContentData peer : category.getCategorizedPeers()) {
					removeRefreshAdapter((EditObject) peer
							.getAdapter(EditObject.class));
				}
			} else {
				removeRefreshAdapter((EditObject) data
						.getAdapter(EditObject.class));
			}
		}
	}

	protected void removeRefreshAdapters() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#dispose()
	 */
	@Override
	public void dispose() {
		removeRefreshAdapters();
		super.dispose();
	}

	protected class RefreshAdapter extends TypedAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
		 */
		public void notifyChanged(Notification notification) {
			// later refreshes also content, to replace the command stack
			// listener if and only if the underlying target changes are unified
			// across the system
			refreshTitleBar();
		}

	}
}