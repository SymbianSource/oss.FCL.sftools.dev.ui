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

package com.nokia.tools.ui.color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class EyeDropperControl {
	public static void fire() {
		final Shell shell = new Shell(Display.getDefault(), SWT.NO_BACKGROUND);
		final GC screenGC = new GC(Display.getDefault());
		shell.setLayout(null);
		shell.setBounds(Display.getDefault().getBounds());
		GC shellGC = new GC(shell);
		final Image bg = new Image(Display.getDefault(), shell.getSize().x,
		    shell.getSize().y);
		shellGC.copyArea(bg, 0, 0);
		shellGC.dispose();
		shell.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				Image image = new Image(e.display, 1, 1);
				screenGC.copyArea(image, e.x, e.y);

				int pixel = image.getImageData().getPixel(0, 0);
				RGB pickedRGB = image.getImageData().palette.getRGB(pixel);
				image.dispose();

				GC gc = new GC(shell);
				int x = e.x + 20;
				int y = e.y;
				gc.drawImage(bg, 0, 0);
				gc.drawRectangle(x, y + 10, 20, 20);
				Color color = new Color(e.display, pickedRGB);
				gc.setBackground(color);
				gc.fillRectangle(x + 1, y + 11, 19, 19);
				gc.dispose();
				color.dispose();
			}
		});

		shell.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				Image image = new Image(e.display, 1, 1);
				screenGC.copyArea(image, e.x, e.y);

				int pixel = image.getImageData().getPixel(0, 0);
				RGB pickedRGB = image.getImageData().palette.getRGB(pixel);
				image.dispose();
				shell.dispose();
				GenericColorPickerUtil.capturedRGB = pickedRGB;
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		shell.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					shell.dispose();
				}
			}

		});

		shell.addDisposeListener(new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				screenGC.dispose();
				bg.dispose();
			}

		});

		shell.open();
		while (!shell.isDisposed()) {
			if (!Display.getDefault().readAndDispatch())
				Display.getDefault().sleep();
		}
	}

	public static class GenericColorPickerUtil {
		public static RGB capturedRGB;
	}

}
