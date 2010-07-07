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
package com.nokia.tools.screen.ui.gef;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.SWT;

import com.nokia.tools.editing.ui.figure.FreeformElementFigure;
import com.nokia.tools.editing.ui.figure.LiveFigure;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.UiPlugin;

public class ZOrderScrollingGraphicalViewer extends ScrollingGraphicalViewer {
	public ZOrderScrollingGraphicalViewer() {
		hookZOrderSelection();
		setRootEditPart(createRootEditPart());
	}

	protected void hookZOrderSelection() {
		LightweightSystem lws = getLightweightSystem();
		try {
			final IFigure root = lws.getRootFigure();
			Field f = LightweightSystem.class.getDeclaredField("root");
			f.setAccessible(true);
			f.set(lws, Proxy.newProxyInstance(root.getClass().getClassLoader(),
					new Class[] { IFigure.class }, new InvocationHandler() {

						/*
						 * (non-Javadoc)
						 * 
						 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
						 *      java.lang.reflect.Method, java.lang.Object[])
						 */
						public Object invoke(Object proxy, Method method,
								Object[] args) throws Throwable {
							if (method.getName().equals("findFigureAt")
									&& method.getParameterTypes().length >= 2) {
								int x = (Integer) args[0];
								int y = (Integer) args[1];
								TreeSearch search = args.length > 2 ? (TreeSearch) args[2]
										: null;

								Point pt = new Point(x, y);

								List<IFigure> list = new ArrayList<IFigure>();

								for (Object obj : root.getChildren()) {
									findDescendantAt(list, (IFigure) obj, pt,
											search);
								}
								IFigure topFigure = null;
								if (!list.isEmpty()) {
									Collections.sort(list,
											new Comparator<IFigure>() {
												/*
												 * (non-Javadoc)
												 * 
												 * @see java.util.Comparator#compare(java.lang.Object,
												 *      java.lang.Object)
												 */
												public int compare(IFigure o1,
														IFigure o2) {
													EditPart e1 = (EditPart) getVisualPartMap()
															.get(o1);
													EditPart e2 = (EditPart) getVisualPartMap()
															.get(o2);
													IScreenElement s1 = JEMUtil
															.getScreenElement(e1);
													IScreenElement s2 = JEMUtil
															.getScreenElement(e2);
													if (s1 != null) {
														return s1
																.compareZOrder(s2);
													}
													if (s2 != null) {
														return -s2
																.compareZOrder(s1);
													}
													return 0;
												}

											});
									topFigure = list.get(list.size() - 1);
								}
								return topFigure;
							}
							return method.invoke(root, args);
						}

						void findDescendantAt(List<IFigure> list,
								IFigure child, Point pt, TreeSearch search) {
							if ((search == null || search.accept(child))
									&& child.containsPoint(pt)) {
								EditPart part = (EditPart) getVisualPartMap()
										.get(child);
								if (part != null
										&& JEMUtil.getScreenElement(part) != null) {
									list.add(child);
								}
							}
							List children = child.getChildren();
							if (children != null) {
								for (int i = 0; i < children.size(); i++) {
									IFigure figure = (IFigure) children.get(i);
									Point p = pt.getCopy();
									child.translateFromParent(p);
									if ((search == null || !search
											.prune(figure))
											&& (JEMUtil
													.getScreenElement(getVisualPartMap()
															.get(figure)) == null || figure
													.containsPoint(p))) {
										findDescendantAt(list, figure, p,
												search);
									}
								}
							}
						}

					}));
		} catch (Exception e) {
			UiPlugin.error(e);
		}
	}

	protected RootEditPart createRootEditPart() {
		return new ScalableFreeformRootEditPart() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.gef.editparts.ScalableFreeformRootEditPart#createScaledLayers()
			 */
			@Override
			protected ScalableFreeformLayeredPane createScaledLayers() {
				ScalableFreeformLayeredPane pane = super.createScaledLayers();

				ScalableFreeformLayeredPane layers = new ScalableFreeformLayeredPane() {
					private double getZoom() {
						return ((ScalableFreeformRootEditPart) getRoot())
								.getZoomManager().getZoom();
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.draw2d.ScalableFreeformLayeredPane#paintClientArea(org.eclipse.draw2d.Graphics)
					 */
					@Override
					protected void paintClientArea(Graphics graphics) {
						graphics.setAntialias(getZoom() == 1.0f ? SWT.OFF
								: SWT.ON);
						super.paintClientArea(graphics);
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.draw2d.FreeformLayeredPane#getFreeformExtent()
					 */
					@Override
					public Rectangle getFreeformExtent() {
						double zoom = getZoom();
						IFigure contentPane = getContentPane(this);
						if (contentPane == null) {
							return Rectangle.SINGLETON;
						}
						Rectangle bounds = contentPane.getBounds().getCopy();
						// unions other visible freeform element bounds
						for (Object obj : contentPane.getParent().getChildren()) {
							if (obj instanceof FreeformElementFigure) {
								IFigure fig = (IFigure) obj;
								if (fig.isVisible()) {
									bounds.union(fig.getBounds());
								}
							}
						}
						bounds.scale(zoom, zoom);
						
						return bounds.setLocation(0, 0);
					}

					/**
					 * Finds the top-level content pane, which is the screen
					 * figure.
					 * 
					 * @param parent the the parent figure.
					 * @return the screen figure.
					 */
					private IFigure getContentPane(IFigure parent) {
						if (parent instanceof LiveFigure) {
							return parent;
						}
						for (Object child : parent.getChildren()) {
							IFigure contentPane = getContentPane((IFigure) child);
							if (contentPane != null) {
								return contentPane;
							}
						}
						return null;
					}
				};

				// calling get(index) directly on the children will throw
				// ArrayIndexException
				Object[] children = pane.getChildren().toArray();
				if (children.length > 2) {
					layers.add((IFigure) children[0], GRID_LAYER);
					layers.add((IFigure) children[1], PRINTABLE_LAYERS);
					layers.add((IFigure) children[2], SCALED_FEEDBACK_LAYER);
				}
				return layers;
			}

		};
	}
}
