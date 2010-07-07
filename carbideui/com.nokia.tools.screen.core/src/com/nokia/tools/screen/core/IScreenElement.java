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
package com.nokia.tools.screen.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.NotificationImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.graphics.Image;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditObjectAdapter;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.media.player.IPlayer;
import com.nokia.tools.media.player.MediaPlayer;
import com.nokia.tools.widget.SComponent;
import com.nokia.tools.widget.SContainer;

/**
 * This class represents an element that is rendered on the graphical editor and
 * can be modified through the underlying the JEM object.
 * 
 */
public interface IScreenElement extends IAdaptable, Adapter {
	/**
	 * @return the text description of the widget.
	 */
	String getText();

	/**
	 * @return the image of the widget.
	 */
	Image getImage();

	/**
	 * @return the content data of the widget.
	 */
	IContentData getData();

	/**
	 * @return the widget instance.
	 */
	EObject getWidget();

	/**
	 * @return the bean
	 */
	Object getBean();

	/**
	 * @return the target element if this adapter is a delegator of the target
	 *         adapter.
	 */
	IScreenElement getTargetAdapter();

	/**
	 * Returns the parent element.
	 * 
	 * @return the parent element.
	 */
	IScreenElement getParent();

	/**
	 * Adds the child element.
	 * 
	 * @param child the child element to be added.
	 */
	void addChild(IScreenElement child);

	/**
	 * Adds the child element in the specific location.
	 * 
	 * @param index the location of the element in the current element list.
	 * @param child the child element to be added.
	 */
	void addChild(int index, IScreenElement child);

	/**
	 * Removes the child element.
	 * 
	 * @param child the child element to be removed.
	 */
	void removeChild(IScreenElement child);

	/**
	 * Returns all child elements.
	 * 
	 * @return all child elements.
	 */
	List<IScreenElement> getChildren();

	/**
	 * Adapts the model to the provided screen context. This will update the JEM
	 * object and the real widget accordingly.
	 * 
	 * @param context the screen context.
	 */
	void adaptToScreen(IScreenContext context);

	/**
	 * @return the screen context, may be null if this element has not been
	 *         adapted to screen.
	 */
	IScreenContext getContext();

	/**
	 * Called when modification to the model data happened, which can be
	 * triggered by the various property editors. The widget shall be updated
	 * according to the model changes.
	 */
	void updateWidget();

	/**
	 * Adds the widget to the parent widget using the containment feature. The
	 * child validity should be checked.<br/> <b>Note: This method suppresses
	 * the EMF notifications</b>
	 */
	void addWidgetToParent();

	/**
	 * Adds the widget to the provided parent widget using the containment
	 * feature. The child validity should be checked.
	 * 
	 * @param parent the parent widget <b>Note: This method suppresses the EMF
	 *            notifications</b>
	 */
	void addWidgetToParent(IScreenElement parent);

	/**
	 * Adds widget to the specifc location of the parent.
	 * 
	 * @param index the location for the widget.
	 * @param parent the parent widget. <b>Note: This method suppresses the EMF
	 *            notifications</b>
	 */
	void addWidgetToParent(int index, IScreenElement parent);

	/**
	 * Removes this widget from its parent.
	 */
	void removeWidgetFromParent();

	/**
	 * Removes this widget from the specific parent.
	 * 
	 * @param parent the parent widget from where this widget is to be removed.
	 */
	void removeWidgetFromParent(IScreenElement parent);

	/**
	 * @return the root element.
	 */
	IScreenElement getRoot();

	/**
	 * Finds the screen element containing the given content data.
	 * 
	 * @param data the content data to match.
	 * @return the screen element wrapping the given data.
	 */
	IScreenElement findByData(IContentData data);

	/**
	 * @return all child elements
	 */
	IScreenElement[] getAllChildren();

	/**
	 * @param runnable suppresses the notification when the runnable runs.
	 */
	void selfDispatch(Runnable runnable);

	/**
	 * @return the z-index
	 */
	int getZIndex();

	/**
	 * Compares the z-index with another element
	 * 
	 * @param another another element to compare
	 * @return the comparison result
	 */
	int compareZOrder(IScreenElement another);

	/**
	 * This class provides the implementation of the common functionalities
	 * defined in the {@link IScreenElement} interface and is meant to ease the
	 * the actual element implementation.
	 * 
	 */
	abstract class ScreenElementAdapter extends EditObjectAdapter implements
			IScreenElement {
		protected static final int MIN_Z_INDEX = Integer.MIN_VALUE;

		private IContentData data;

		private IScreenElement parent;

		private List<IScreenElement> children = Collections
				.synchronizedList(new ArrayList<IScreenElement>());

		private IScreenContext context;

		private IScreenElement targetElement;

		/**
		 * Constructs a screen element.
		 * 
		 * @param data the model data.
		 * @param widgetClass the widget class name.
		 */
		public ScreenElementAdapter(IContentData data) {
			this.data = data;
		}

		/**
		 * Constructs a screen element from the widget.
		 * 
		 * @param data the model data.
		 * @param widget the widget instance.
		 */
		public ScreenElementAdapter(IContentData data, EObject widget) {
			this(data);
			if (EcoreUtil.getExistingAdapter(widget, IScreenElement.class) == null) {
				widget.eAdapters().add(this);
			}
		}

		public ScreenElementAdapter(IScreenElement targetElement) {
			this.targetElement = targetElement;
		}

		public void adaptToScreen() {
			if (context == null) {
				throw new IllegalStateException("The context is null");
			}
			adaptToScreen(context);
		}

		protected abstract Object createWidget();

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#adaptToScreen(com.nokia.tools.screen.core.IScreenContext)
		 */
		public void adaptToScreen(IScreenContext context) {
			// context can be null, meaning the element is not attached to any
			// live screens
			this.context = context;
			if (getWidget() == null) {
				try {
					EObject object = new ScreenEditingModelFactory()
							.createEditObject(createWidget());
					object.eAdapters().add(this);
					selfDispatch(new Runnable() {
						public void run() {
							try {
								initWidget(ScreenElementAdapter.this.context);
							} catch (IllegalStateException e) {
							} catch (Throwable e) {
								CorePlugin.error(e);
							}
						}
					});
				} catch (Exception e) {
					CorePlugin.error(e);
				}
			}
		}

		protected void registerVMAdapters() {
			SComponent bean = (SComponent) getBean();
			Object[] vmAdapters = getVMAdapters();
			if (vmAdapters != null && vmAdapters.length > 0) {
				for (Object adapter : vmAdapters) {
					try {
						bean.registerAdapter(adapter);
					} catch (Exception e) {
						CorePlugin.error(e);
					}
				}
			}
		}

		protected void deregisterVMAdapters() {
			SComponent bean = (SComponent) getBean();
			Object[] vmAdapters = getVMAdapters();
			if (vmAdapters != null && vmAdapters.length > 0) {
				for (Object adapter : vmAdapters) {
					try {
						bean.deregisterAdapter(adapter);
					} catch (Exception e) {
						CorePlugin.error(e);
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#addChild(com.nokia.tools.screen.core.IScreenElement)
		 */
		public void addChild(IScreenElement child) {
			addChild(-1, child);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#addChild(com.nokia.tools.screen.core.IScreenElement,
		 *      int)
		 */
		public void addChild(int index, IScreenElement child) {
			if (child == this) {
				throw new IllegalArgumentException("Can't add self as child");
			}
			((ScreenElementAdapter) child).parent = this;
			if (index < 0) {
				children.add(child);
			} else {
				children.add(index, child);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#addWidgetToParent()
		 */
		public void addWidgetToParent() {
			if (parent != null && parent.getWidget() != null) {
				addWidgetToParent(parent);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#addWidgetToParent(com.nokia.tools.screen.core.IScreenElement)
		 */
		public void addWidgetToParent(IScreenElement parent) {
			addWidgetToParent(-1, parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#addWidgetToParent(int,
		 *      com.nokia.tools.screen.core.IScreenElement)
		 */
		public void addWidgetToParent(final int index,
				final IScreenElement parent) {
			SContainer parentBean = (SContainer) parent.getBean();
			SComponent bean = (SComponent) getBean();
			if (parentBean.isChildValid(bean)) {
				final List children = EditingUtil.getChildren(parent
						.getWidget());
				if (children != null && getWidget() != null) {
					parent.selfDispatch(new Runnable() {
						public void run() {
							selfDispatch(new Runnable() {
								public void run() {
									synchronized (children) {
										if (index < 0) {
											children.add(getWidget());
										} else {
											children.add(index, getWidget());
										}
									}
								}
							});
						}
					});
				}
				if (children != null && getWidget() != null) {
					registerVMAdapters();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#removeWidgetFromParent()
		 */
		public void removeWidgetFromParent() {
			if (parent != null && parent.getWidget() != null) {
				removeWidgetFromParent(parent);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#removeWidgetFromParent(com.nokia.tools.screen.core.IScreenElement)
		 */
		public void removeWidgetFromParent(IScreenElement parent) {
			final List children = EditingUtil.getChildren(parent.getWidget());
			if (children != null) {
				synchronized (children) {
					children.remove(getWidget());
					deregisterVMAdapters();
					if (getWidget() != null) {
						getWidget().eAdapters().remove(this);
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getChildren()
		 */
		public List<IScreenElement> getChildren() {
			return Collections.unmodifiableList(children);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getData()
		 */
		public IContentData getData() {
			return data;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getParent()
		 */
		public IScreenElement getParent() {
			return parent;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getRoot()
		 */
		public IScreenElement getRoot() {
			IScreenElement element = this;
			while (element.getParent() != null) {
				element = element.getParent();
			}
			return element;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getWidget()
		 */
		public EObject getWidget() {
			return (EObject) getTarget();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getBean()
		 */
		public Object getBean() {
			return EditingUtil.getBean(getWidget());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#removeChild(com.nokia.tools.screen.core.IScreenElement)
		 */
		public void removeChild(IScreenElement child) {
			if (children.remove(child)) {
				((ScreenElementAdapter) child).parent = null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#updateWidget()
		 */
		public final void updateWidget() {
			// check this, no need to run in display thread
			if (getWidget() != null) {
				selfDispatch(new Runnable() {
					public void run() {
						updateWidgetSpi();
					}
				});
			}
		}

		protected void updateWidgetSpi() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.editing.core.EditObjectAdapter#notifyChangedSpi(org.eclipse.emf.common.notify.Notification)
		 */
		protected final void notifyChangedSpi(final Notification notification) {
			if (targetElement != null) {
				if (targetElement.getWidget() != null
						&& notification.getFeature() instanceof EStructuralFeature
						&& notification.getNotifier() instanceof EObject) {
					for (Adapter adapter : targetElement.getWidget()
							.eAdapters()) {
						adapter.notifyChanged(new NotificationImpl(notification
								.getEventType(), notification.getOldValue(),
								notification.getNewValue()) {

							/*
							 * (non-Javadoc)
							 * 
							 * @see org.eclipse.emf.common.notify.impl.NotificationImpl#getFeature()
							 */
							@Override
							public Object getFeature() {
								return EditingUtil.getFeature(
										(EObject) getNotifier(),
										((EStructuralFeature) notification
												.getFeature()).getName());
							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see org.eclipse.emf.common.notify.impl.NotificationImpl#getNotifier()
							 */
							@Override
							public Object getNotifier() {
								return targetElement.getWidget();
							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see org.eclipse.emf.common.notify.impl.NotificationImpl#getPosition()
							 */
							@Override
							public int getPosition() {
								return notification.getPosition();
							}

						});
					}
				}
				// notification should be handled by the target element
				return;
			}
			IContentSynchronizer sync = null;
			if (data != null && data.getRoot() != null) {
				sync = (IContentSynchronizer) data.getRoot().getAdapter(
						IContentSynchronizer.class);
			}
			if (sync != null && !sync.isInDesignMode()) {
				sync.enterDesignMode();
			} else {
				sync = null;
			}
			try {
				if (EditingUtil.isRemovingAdapter(notification, this)) {
					deregisterVMAdapters();
				}
				handleNotification(notification);
			} finally {
				if (sync != null) {
					sync.leaveDesignMode();
				}
			}
		}

		/**
		 * Called when the widget is changed.
		 * 
		 * @param notification the event notification.
		 */
		protected void handleNotification(Notification notification) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getImage()
		 */
		public Image getImage() {
			return getData().getIcon();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getText()
		 */
		public String getText() {
			IContentData data = getData();
			if (data.getAdapter(INamingAdapter.class) != null) {
				return ((INamingAdapter) data.getAdapter(INamingAdapter.class))
						.getName();
			}
			return getData().getName();
		}

		/**
		 * Called when the widget is initialized.
		 * 
		 * @param context the current screen context.
		 */
		protected final void initWidget(final IScreenContext context) {
			selfDispatch(new Runnable() {
				public void run() {
					initWidgetSpi(context);
				}
			});
		}

		protected void initWidgetSpi(IScreenContext context) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#findByData(com.nokia.tools.content.core.IContentData)
		 */
		public IScreenElement findByData(IContentData data) {
			IScreenElement root = getRoot();
			return findByData(root, data);
		}

		public IScreenElement findByData(IScreenElement element,
				IContentData data) {
			if (element.getData() == data) {
				return element;
			}
			for (IScreenElement child : element.getChildren()) {
				if (child.getData() == data) {
					return child;
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getContext()
		 */
		public IScreenContext getContext() {
			return context;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getTargetAdapter()
		 */
		public IScreenElement getTargetAdapter() {
			return targetElement;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			if (adapter == IComponentAdapter.class) {
				IComponentAdapter customizedAdapter = CustomizationManager
						.getComponentAdapter(this);
				return customizedAdapter == null ? new ScreenComponentAdapter(
						this) : customizedAdapter;
			}
			if (adapter == IPropertyAdapter.class) {
				return IPropertyAdapter.GENERAL_ADAPTER;
			}

			if (adapter == IPlayer.class && getVMAdapters() != null) {
				for (Object vmAdapter : getVMAdapters()) {
					if (vmAdapter instanceof IPlayer) {
						return vmAdapter;
					}
					if (vmAdapter instanceof MediaPlayer) {
						return ((MediaPlayer) vmAdapter).getPlayer();
					}
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getAllChildren()
		 */
		public IScreenElement[] getAllChildren() {
			Stack<IScreenElement> stack = new Stack<IScreenElement>();
			List<IScreenElement> list = new ArrayList<IScreenElement>();
			stack.push(this);
			while (!stack.isEmpty()) {
				IScreenElement child = stack.pop();
				if (child != this) {
					list.add(child);
				}
				if (child.getChildren() != null) {
					for (IScreenElement c : child.getChildren()) {
						stack.push(c);
					}
				}
			}
			return list.toArray(new IScreenElement[list.size()]);
		}

		/**
		 * @return all adapters that will be passed to the vm
		 */
		public Object[] getVMAdapters() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#getZIndex()
		 */
		public int getZIndex() {
			return 0;
		}

		protected Stack<Integer> getStackedZIndex(IScreenElement parent) {
			IScreenElement xe = this;
			Stack<Integer> stack = new Stack<Integer>();
			while (xe != null) {
				int index = xe.getZIndex();
				if (MIN_Z_INDEX == index && !stack.isEmpty()) {
					index = 0;
				}
				stack.push(index);
				if (xe == parent) {
					break;
				}
				xe = xe.getParent();
			}
			return stack;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IScreenElement#compareZOrder(com.nokia.tools.screen.core.IScreenElement)
		 */
		public int compareZOrder(IScreenElement another) {
			// compares z-order using the stacking context
			if (another instanceof ScreenElementAdapter) {
				ScreenElementAdapter xe = (ScreenElementAdapter) another;

				Integer override = overrideZOrderComparison(another);
				if (override != null) {
					return override;
				}
				override = xe.overrideZOrderComparison(this);
				if (override != null) {
					return -override;
				}

				List<IScreenElement> ah = getHierarchy(this);
				List<IScreenElement> bh = getHierarchy(xe);
				IScreenElement commonParent = null;
				for (IScreenElement element : ah) {
					if (bh.contains(element)) {
						commonParent = element;
						break;
					}
				}
				if (commonParent != null) {
					Stack<Integer> sa = getStackedZIndex(commonParent);
					Stack<Integer> sb = xe.getStackedZIndex(commonParent);
					while (!sa.isEmpty() && !sb.isEmpty()) {
						int a = sa.pop();
						int b = sb.pop();
						if (a > b) {
							return 1;
						}
						if (a < b) {
							return -1;
						}
					}
					if (sa.isEmpty() && !sb.isEmpty()) {
						return -sb.pop();
					}
					if (sb.isEmpty() && !sa.isEmpty()) {
						return sa.pop();
					}

					return 0;
				}
			}
			return 0;
		}

		protected Integer overrideZOrderComparison(IScreenElement another) {
			return null;
		}

		private List<IScreenElement> getHierarchy(IScreenElement se) {
			List<IScreenElement> aParents = new ArrayList<IScreenElement>();
			IScreenElement element = se;
			while (element != null) {
				aParents.add(element);
				element = element.getParent();
			}
			return aParents;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "element:" + data;
		}
	}
}
