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
package com.nokia.tools.editing.jfc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.ui.figure.LiveFigure;
import com.nokia.tools.media.image.CoreImage;

/**
 *
 */
public class JFCFigure extends LiveFigure {
	private JPanel panel;

	public JFCFigure(GraphicalViewer viewer, boolean isLive) {
		super(viewer, isLive);
	}

	public JFCFigure(GraphicalViewer viewer) {
		super(viewer);
	}

	protected Container getRoot() {
		return panel == null ? null : (panel.getParent() == null ? panel
				: panel.getParent());
	}

	protected Object getOffscreenComponent() {
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.figure.LiveFigure#initFigure()
	 */
	protected void initFigure() {
		if (shouldInitFigure()) {
			Component[] components = null;
			if (panel == null) {
				EditPart part = (EditPart) getViewer().getVisualPartMap().get(
						this);
				if (part != null) {
					Component component = (Component) EditingUtil
							.getBean((EObject) part.getModel());
					if (component != null) {
						components = new Component[] { component };
					}
				}
			} else {
				components = panel.getComponents();
			}
			final Rectangle bounds = getBounds();

			dispose();

			panel = new JPanel() {
				public void validate() {
					if (!isDisplayable()) {
						validateTree();
					} else {
						super.validate();
					}
				}
			};
			panel.setOpaque(false);
			Frame f = null;
			if (isLive()) {
				f = (Frame) createLiveComponent();
			}
			final Frame frame = f;
			final Component[] children = components;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					panel.setLayout(new BorderLayout());
					if (frame != null) {
						// ignore awt events
						frame.setEnabled(false);
						frame.setBounds(bounds.x, bounds.y, bounds.width,
								bounds.height);
						frame.add(panel);
						frame.validate();
					} else {
						// setComponentZOrder()
						// panel.addNotify();
						panel.setDoubleBuffered(false);
					}
					if (children != null) {
						for (Component component : children) {
							panel.add(component);
						}
					}

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							revalidateRoot();
						}
					});
				}
			});
		}
	}

	protected Object createLiveComponent() {
		createCanvas();
		if (getCanvas() != null && !getCanvas().isDisposed()) {
			return SWT_AWT.new_Frame(getCanvas());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.figure.LiveFigure#paintLive()
	 */
	protected void paintLive() {
		final Container root = getRoot();
		if (root != null) {
			// let the live mode repaint
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					root.repaint();
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.figure.LiveFigure#runImageCaptureJob(java.lang.Runnable)
	 */
	protected void runImageCaptureJob(Runnable job) {
		if (panel != null) {
			SwingUtilities.invokeLater(job);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.figure.LiveFigure#captureImage()
	 */
	protected Image captureImage() {
		if (panel == null) {
			return null;
		}
		if ((bounds.width <= 0) || (bounds.height <= 0)) return null;
		panel.setSize(bounds.width, bounds.height);
		panel.invalidate();
		panel.validate();

		// paints the offscreen figure anyway to make thumbnail
		// work
		final BufferedImage image = new BufferedImage(bounds.width,
				bounds.height, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics g = image.getGraphics();
		panel.printAll(g);
		g.dispose();

		return CoreImage.create().init(image).getSwt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.figure.LiveFigure#dispose()
	 */
	public void dispose() {
		super.dispose();

		Container root = getRoot();
		panel = null;
		if (root instanceof Frame) {
			final Frame f = (Frame) root;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					f.dispose();
				}
			});
		}
	}
}
