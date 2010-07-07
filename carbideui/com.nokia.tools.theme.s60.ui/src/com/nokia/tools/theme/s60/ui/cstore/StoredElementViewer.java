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
package com.nokia.tools.theme.s60.ui.cstore;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.image.RenderedImageDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor.ContentType;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.theme.s60.cstore.ComponentStore;
import com.nokia.tools.theme.s60.cstore.ComponentStore.StoredElement;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;

/**
 * Capable of showing list of StoredElements representing element in component
 * store
 */
public class StoredElementViewer extends ScrolledComposite implements
		ISelectionProvider {

	protected static int ELEMENT_SIZE_SMALL = 30;

	protected static int ELEMENT_SIZE_BIG = 45;

	public static interface ActionProvider {

		public void fillElementContextMenu(IMenuManager manager,
				Object element, List<Object> multiSelection);

	}

	public static interface ActionProvider2 extends ActionProvider {

		public void processDoubleClick(Object element,
				List<Object> multiSelection);

	}

	public enum DisplaySizeMode {
		SmallIcon, BigIcon, List, Specified
	}

	public enum ModifyOperation {
		Changed, Removed
	}

	public boolean dragInProgress = false;

	protected List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	protected int elementWidth = 0;

	protected int lastWidth = 0;

	protected int currentCols = 0;

	protected Composite root;

	protected ISelection _selected;

	protected ActionProvider provider;

	protected List<StoredElement> contentList = new ArrayList<StoredElement>();

	protected DisplaySizeMode sizeMode = DisplaySizeMode.BigIcon;

	protected DisplaySizeMode lastSizeMode = DisplaySizeMode.BigIcon;

	protected ISelection currentSelection;

	protected int specifiedWidth, specifiedHeight;

	protected Control selected;

	private List<Object> selection = new ArrayList<Object>();

	private Map<Object, Label> labelMap = new HashMap<Object, Label>();

	private Thread activeRefreshThread;

	public StoredElementViewer(Composite parent, int style,
			ActionProvider actionProvider) {
		super(parent, style | SWT.H_SCROLL | SWT.V_SCROLL);

		this.provider = actionProvider;

		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				layoutSize();
			}
		});
		root = new Composite(this, SWT.None);
		setExpandHorizontal(true);
		setExpandVertical(true);
		setContent(root);
	}

	public synchronized void setContent(List<StoredElement> list,
			int specifiedWidth, int specifiedHeight) {

		if (sizeMode != DisplaySizeMode.Specified)
			lastSizeMode = sizeMode;
		sizeMode = DisplaySizeMode.Specified;
		this.specifiedHeight = specifiedHeight;
		this.specifiedWidth = specifiedWidth;

		contentList.clear();
		contentList.addAll(list);

		startRefreshContent();
	}

	public synchronized void addDeltaContent(List<StoredElement> list) {

		contentList.addAll(list);

		if (contentList.size() == list.size())
			startRefreshContent();
		else {
			try {
				for (final StoredElement sElement : list) {
					internalAddElement(sElement);
				}
				// force refresh layout na scroll area
				currentCols = 0;
				layoutSize();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void startRefreshContent() {
		activeRefreshThread = new Thread(new Runnable() {
			public void run() {
				synchronized(StoredElementViewer.class){
					refreshContent();
				}
			}
		});
		activeRefreshThread.start();
	}

	public synchronized void setContent(List<StoredElement> list) {

		if (sizeMode == DisplaySizeMode.Specified)
			sizeMode = lastSizeMode;

		contentList.clear();
		contentList.addAll(list);

		startRefreshContent();
	}

	protected void refreshContent() {

		List<StoredElement> imgs = contentList;

		try {

			disposeLabels();
			layoutRoot();

			elementWidth = 0;
			lastWidth = 0;

			for (int i = 0; i < imgs.size(); i++) {
				StoredElement sElement = imgs.get(i);
				if (activeRefreshThread != Thread.currentThread()) {
					// more recent update in progress
					return;
				}
				internalAddElement(sElement);
			}
			if (lastWidth > elementWidth)
				elementWidth = lastWidth;

			// force refresh layout na scroll area
			currentCols = 0;
			layoutSize();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void disposeLabels() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					for (Control c : root.getChildren()) {
						if (c instanceof Label
								&& ((Label) c).getImage() != null)
							((Label) c).getImage().dispose();
						c.dispose();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					labelMap.clear();
				}
			}
		});
	}

	public void clearContent() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				clear();
			}
		});
	}

	private RenderedImageDescriptor createImage(RenderedImage _img,
			Label element) {

		if (_img == null)
			_img = (RenderedImage) element.getData("BASEIMG");

		final int MARGIN = _img.getWidth() < 20 || _img.getHeight() < 20 ? 8
				: 4;

		BufferedImage bi = new BufferedImage(_img.getWidth() + MARGIN, _img
				.getHeight()
				+ MARGIN, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) bi.getGraphics();

		/* paint bg grid */
		Color c1 = new Color(235, 235, 235);
		Color c2 = new Color(210, 210, 210);
		int edge = 10;
		for (int x = 0; x < bi.getWidth(); x += edge)
			for (int y = 0; y < bi.getHeight(); y += edge) {
				g2.setColor((x / edge + y / edge) % 2 == 0 ? c1 : c2);
				g2.fillRect(x, y, edge, edge);
			}

		/* code for white bg - disabled */

		/*
		 * g2.setColor(Color.WHITE); g2.fillRect(0, 0, bi.getWidth(),
		 * bi.getHeight());
		 */

		g2.drawRenderedImage(_img, AffineTransform.getTranslateInstance(
				MARGIN / 2, MARGIN / 2));

		g2.setColor(selection.contains(element.getData()) ? Color.blue
				: Color.GRAY);
		g2.drawRect(0, 0, bi.getWidth() - 1, bi.getHeight() - 1);
		if (selection.contains(element.getData())) {
			g2.drawRect(1, 1, bi.getWidth() - 3, bi.getHeight() - 3);
		}

		g2.dispose();
		final RenderedImageDescriptor desc = new RenderedImageDescriptor(bi);
		return desc;
	}

	private void internalAddElement(final StoredElement element) {
		if (element == null)
			return;

		/* determine item size */
		int _width = sizeMode == DisplaySizeMode.SmallIcon ? ELEMENT_SIZE_SMALL
				: ELEMENT_SIZE_BIG;
		int _height = sizeMode == DisplaySizeMode.SmallIcon ? ELEMENT_SIZE_SMALL
				: ELEMENT_SIZE_BIG;
		if (sizeMode == DisplaySizeMode.Specified) {
			_width = specifiedWidth;
			_height = specifiedHeight;
		}
		final int width = _width;
		final int height = _height;

		IContentData cData = (IContentData) element.link;
		if (cData == null)
			return;

		IImageAdapter adapter = (IImageAdapter) cData
				.getAdapter(IImageAdapter.class);

		if (adapter != null) {
			EditableEntityImage ei = (EditableEntityImage) adapter.getImage(
					width, height);
			if (ei != null) {

				/*
				 * for nine pieces, image must be rendered in orig dimensions
				 * and then scaled down.
				 */
				RenderedImage __img = null;
				//if (ei.isNinePiece()
				if (ei.isMultiPiece()
						&& !ComponentStore.isComponentStoreElement(ei.getId())) {
					ei = (EditableEntityImage) adapter.getImage();
					__img = CoreImage.create(ei.getAggregateImage())
							.stretch(width, height, CoreImage.STRETCH)
							.getAwt();
				} else {
					__img = ei.getAggregateImage();
				}

				final int MARGIN = __img.getWidth() < 20
						|| __img.getHeight() < 20 ? 8 : 4;
				final RenderedImage _img = __img;

				final boolean setImage = true;

				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						final Label lbl = new Label(root, SWT.NONE);

						lbl.setData("BASEIMG", _img);
						lbl.setData(element);
						labelMap.put(element, lbl);

						if (setImage) {
							final RenderedImageDescriptor desc = createImage(
									_img, lbl);
							lbl.setImage(desc.createImage());
						}
						lbl.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseDoubleClick(MouseEvent e) {
								doDoubleClick(lbl, element);
							}

							public void mouseUp(MouseEvent e) {
								if (e.button == 1
										|| (e.button == 3 && !selection
												.contains(lbl.getData()))) {
									if ((e.stateMask & SWT.SHIFT) == 0) {
										for (int j = selection.size() - 1; j >= 0; j--) {
											Object x = selection.get(j);
											Label l = labelMap.get(x);
											if (l != null) {
												try {
													l.getImage().dispose();
												} catch (Exception ex) {
													
												}
												selection.remove(j);
												l.setImage(createImage(null, l)
														.createImage());
											}
										}
										selection.clear();
									}
									selection.add(lbl.getData());
									try {
										lbl.getImage().dispose();
									} catch (Exception ex) {
										
									}
									lbl.setImage(createImage(_img, lbl)
											.createImage());
								}
							}
						});

						String label = element.name;
						lbl.setToolTipText(label);

						/* element menu */
						MenuManager mmgr = new MenuManager();
						lbl.setMenu(mmgr.createContextMenu(lbl));
						mmgr.setRemoveAllWhenShown(true);
						mmgr.addMenuListener(new IMenuListener() {
							public void menuAboutToShow(IMenuManager manager) {
								doFillElementContextMenu(manager, lbl, element);
							}
						});

						// add drag support for this ele,ent
						DragSource dSource = new DragSource(lbl, DND.DROP_COPY);
						dSource
								.setTransfer(new Transfer[] { LocalSelectionTransfer
										.getTransfer() });

						dSource.addDragListener(new DragSourceListener() {
							public void dragFinished(DragSourceEvent event) {
								dragInProgress = false;
							}

							public void dragSetData(DragSourceEvent event) {
								if (LocalSelectionTransfer.getTransfer()
										.isSupportedType(event.dataType)) {
									ClipboardContentDescriptor cde = new ClipboardContentDescriptor(
											element.link,
											ContentType.CONTENT_ELEMENT);
									// indicate drag from this to prevent drop
									// on self
									event.data = ComponentStoreWidget.DRAG_FROM_POOL;
									StructuredSelection ssel = new StructuredSelection(
											new Object[] { cde });
									LocalSelectionTransfer.getTransfer()
											.setSelection(ssel);
									LocalSelectionTransfer.getTransfer()
											.setSelectionSetTime(
													System.currentTimeMillis());
								}
							}

							public void dragStart(DragSourceEvent event) {
								event.doit = dragInProgress = true;
							}
						});

						root.layout(new Control[] { lbl });
					}
				});

				// remember width for compute layout columns count later
				if (width + MARGIN > lastWidth)
					lastWidth = width + MARGIN;

			}
		}
	}

	protected void doFillElementContextMenu(IMenuManager manager, Label lbl,
			StoredElement element) {
		if (provider != null)
			provider.fillElementContextMenu(manager, element, selection);
	}

	protected void doDoubleClick(Label lbl, StoredElement element) {
		if (provider instanceof ActionProvider2)
			((ActionProvider2) provider).processDoubleClick(element, selection);
	}

	protected void layoutRoot() {
		if (Display.getCurrent() == null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					layoutRoot();
				}
			});
		} else {
			root.layout(true);
		}
	}

	protected void layoutSize() {
		if (elementWidth == 0)
			return;

		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					layoutSize();
				}
			});
			return;
		}

		// compute layout
		int clientWidth = getSize().x;
		int count = Math.max(1, clientWidth / (elementWidth + 8));

		if (count != currentCols) {
			root.setLayout(new GridLayout(count, true));
			setMinSize(root.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			root.layout(true);
			currentCols = count;
		}
	}

	protected void clear() {
		contentList.clear();
		selection.clear();

		// get images to controls
		disposeLabels();

		root.layout(true);
		elementWidth = 0;
		currentCols = 0;
		setMinSize(0, 0);
	}

	@Override
	public void dispose() {
		try {
			clear();
		} catch (Exception e) {
		}
		super.dispose();
	}

	public void notifyModified(ModifyOperation op, List<StoredElement> affected) {
		// refresh appropriately given elements
		if (op == ModifyOperation.Removed) {
			for (StoredElement se : affected) {
				for (Control c : root.getChildren()) {
					if (c.getData() == se) {
						Label lbl = (Label) c;
						if (lbl.getImage() != null)
							lbl.getImage().dispose();
						labelMap.remove(lbl.getData());
						lbl.dispose();
						contentList.remove(se);
						root.layout(true);
					}
				}
			}
		}
	}

	public Composite getContentComposite() {
		return root;
	}

	/* selection provider impl */

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public ISelection getSelection() {
		return currentSelection;
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public void setSelection(ISelection selection) {
	}

}