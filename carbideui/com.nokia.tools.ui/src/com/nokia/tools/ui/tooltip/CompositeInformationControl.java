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

package com.nokia.tools.ui.tooltip;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.ui.Activator;

public class CompositeInformationControl implements IInformationControl,
		IInformationControlExtension3, DisposeListener {

	public static int TRANSPARENCY_ALPHA = 255;

	private static Class osClass;

	protected PopupDialog fPopupDialog;

	protected Composite composite;

	protected static final int INNER_BORDER = 1;

	protected int fMaxWidth = -1;

	protected int fMaxHeight = -1;

	protected boolean disposed = false;

	private static Integer hUserWin32Lib = null;

	static {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			try {
				// use reflection to get this compiled in other os
				Class tcharClass = Thread.currentThread()
						.getContextClassLoader().loadClass(
								"org.eclipse.swt.internal.win32.TCHAR");
				Constructor ctr = tcharClass.getConstructor(new Class[] {
						int.class, String.class, boolean.class });
				Object lpLibFileName = ctr.newInstance(new Object[] { 0,
						"User32.dll", true });
				osClass = Thread.currentThread().getContextClassLoader()
						.loadClass("org.eclipse.swt.internal.win32.OS");
				Method method = osClass.getMethod("LoadLibrary",
						new Class[] { tcharClass });
				hUserWin32Lib = (Integer) method.invoke(null,
						new Object[] { lpLibFileName });
			} catch (Throwable t) {
				Activator.error(t);
			}
		}
	}

	public CompositeInformationControl(Shell parent, int shellStyle, int style) {
		this(parent, shellStyle, style, null);
	}

	public CompositeInformationControl(Shell parentShell, int shellStyle,
			final int style, String statusFieldText) {
		shellStyle = shellStyle | SWT.NO_FOCUS;
		fPopupDialog = new PopupDialog(parentShell, shellStyle, false, false,
				false, false, null, statusFieldText) {
			protected Control createDialogArea(Composite parent) {
				composite = createComposite(parent, style);

				composite.addKeyListener(new KeyListener() {

					public void keyPressed(KeyEvent e) {
						if (e.character == 0x1B) // ESC
							close();
					}

					public void keyReleased(KeyEvent e) {
					}
				});

				return composite;
			}
		};

		fPopupDialog.create();
	}

	protected Composite createComposite(Composite parent, int style) {
		composite = new Composite(parent, style);
		GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
		gd.horizontalIndent = INNER_BORDER;
		gd.verticalIndent = INNER_BORDER;
		composite.setLayoutData(gd);

		return composite;
	}

	public CompositeInformationControl(Shell parent, int style) {
		this(parent, SWT.TOOL | SWT.NO_TRIM, style);
	}

	public CompositeInformationControl(Shell parent, int style,
			String statusFieldText) {
		this(parent, SWT.TOOL | SWT.NO_TRIM, style, statusFieldText);
	}

	public CompositeInformationControl(Shell parent) {
		this(parent, SWT.NONE, null);
	}

	public Composite getComposite() {
		return composite;
	}

	/*
	 * @see IInformationControl#setInformation(String)
	 */
	public void setInformation(String content) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see IInformationControl#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		setVisible(visible, false);
	}
	
	public PopupDialog getPopupDialog() {
		return fPopupDialog;
	}

	FadeThread fadeThread;

	class FadeThread extends Thread {
		final int ACTION_OPEN = 1;

		final int ACTION_HIDE = 2;

		final int ACTION_DISPOSE = 3;

		int curAlpha = 0;

		int targetAlpha = TRANSPARENCY_ALPHA;

		int step = 15;

		int sleepTime = 5;

		int action = ACTION_OPEN;

		public FadeThread() {
			setPriority(Thread.MAX_PRIORITY);
		}

		public void run() {
			if (osClass == null) {
				return;
			}
			try {
				byte[] lpProcName = "SetLayeredWindowAttributes\0"
						.getBytes("ascii");
				Method method = osClass.getMethod("GetProcAddress",
						new Class[] { int.class, byte[].class });
				final int fun = (Integer) method.invoke(null, new Object[] {
						hUserWin32Lib, lpProcName });
				if (fun != 0) {
					while (true) {
						if (fPopupDialog == null
								|| fPopupDialog.getShell() == null
								|| fPopupDialog.getShell().isDisposed()) {
							break;
						}

						method = osClass.getMethod("CallWindowProc",
								new Class[] { int.class, int.class, int.class,
										int.class, int.class });
						method.invoke(null, new Object[] { fun,
								fPopupDialog.getShell().handle, 0x00000000,
								curAlpha, 2 });

						if (action == ACTION_OPEN) {
							action = 0;
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									if (fPopupDialog != null) {
										fPopupDialog.open();
									}
								}
							});
						}

						try {
							sleep(sleepTime);
						} catch (Exception e) {
							Activator.error(e);
						}

						if (curAlpha != targetAlpha) {
							if (curAlpha < targetAlpha) {
								curAlpha += step;
								if (curAlpha > targetAlpha) {
									curAlpha = targetAlpha;
								}
							} else if (curAlpha > targetAlpha) {
								curAlpha -= step;
								if (curAlpha < targetAlpha) {
									curAlpha = targetAlpha;
								}
							}
						} else {
							break;
						}
					}
				}
			} catch (Throwable e) {
				Activator.error(e);
			} finally {
				fadeThread = null;
				if (action == ACTION_HIDE || action == ACTION_DISPOSE) {
					final int act = action;
					action = 0;
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							if (fPopupDialog != null) {
								if (act == ACTION_HIDE) {
									if (fPopupDialog.getShell() != null
											&& !fPopupDialog.getShell()
													.isDisposed()) {
										fPopupDialog.getShell().setVisible(
												false);
									}
								}
								if (act == ACTION_DISPOSE) {
									fPopupDialog.close();
									fPopupDialog = null;
								}
							}
						}
					});
				}
			}
		}
	};

	/*
	 * @see IInformationControl#setVisible(boolean)
	 */
	public void setVisible(boolean visible, final boolean dispose) {
		if (disposed) {
			return;
		}

		if (visible) {
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				try {
					if (hUserWin32Lib != null && hUserWin32Lib != 0
							&& osClass != null) {
						byte[] lpProcName = "SetLayeredWindowAttributes\0"
								.getBytes("ascii");
						Method method = osClass.getMethod("GetProcAddress",
								new Class[] { int.class, byte[].class });
						final int fun = (Integer) method.invoke(null,
								new Object[] { hUserWin32Lib, lpProcName });
						if (fun != 0) {
							Field field = osClass.getField("GWL_EXSTYLE");
							int style = (Integer) field.get(null);
							method = osClass.getMethod("GetWindowLong",
									new Class[] { int.class, int.class });
							int wnd = (Integer) method.invoke(null,
									new Object[] {
											fPopupDialog.getShell().handle,
											style });
							method = osClass.getMethod("SetWindowLong",
									new Class[] { int.class, int.class,
											int.class });
							method.invoke(null, new Object[] {
									fPopupDialog.getShell().handle, style,
									wnd | 0x80000 });

							if (fadeThread == null) {
								fadeThread = new FadeThread();
								fadeThread.targetAlpha = TRANSPARENCY_ALPHA;
								fadeThread.action = fadeThread.ACTION_OPEN;
								fadeThread.start();
							} else {
								fadeThread.targetAlpha = TRANSPARENCY_ALPHA;
								fadeThread.action = fadeThread.ACTION_OPEN;
								fadeThread.interrupt();
							}

							return;
						}
					}
				} catch (Exception e) {
					Activator.error(e);
				}
			}
			fPopupDialog.open();
		} else {
			// if (Platform.OS_WIN32.equals(Platform.getOS())) {
			// try {
			// if (hUserWin32Lib != null && hUserWin32Lib != 0) {
			// byte[] lpProcName = "SetLayeredWindowAttributes\0"
			// .getBytes("ascii");
			// final int fun = OS.GetProcAddress(hUserWin32Lib,
			// lpProcName);
			// if (fun != 0) {
			// // fade-out routine
			// if (fadeThread == null) {
			// fadeThread = new FadeThread();
			// fadeThread.curAlpha = TRANSPARENCY_ALPHA;
			// fadeThread.targetAlpha = 0;
			// if (dispose) {
			// fadeThread.action = fadeThread.ACTION_DISPOSE;
			// } else {
			// fadeThread.action = fadeThread.ACTION_HIDE;
			// }
			// fadeThread.start();
			// } else {
			// fadeThread.targetAlpha = 0;
			// if (dispose) {
			// fadeThread.action = fadeThread.ACTION_DISPOSE;
			// } else {
			// fadeThread.action = fadeThread.ACTION_HIDE;
			// }
			// fadeThread.interrupt();
			// }
			//
			// return;
			// }
			// }
			// } catch (Exception e) {
			// // do nothing
			// }
			// }

			if (fPopupDialog != null) {
				if (fPopupDialog.getShell() != null
						&& !fPopupDialog.getShell().isDisposed()) {
					fPopupDialog.getShell().setVisible(false);
				}

				if (dispose) {
					fPopupDialog.close();
					fPopupDialog = null;
				}
			}
		}
	}

	/*
	 * @see IInformationControl#dispose()
	 */
	public void dispose() {
		if (disposed) {
			return;
		}

		if (fPopupDialog != null && fPopupDialog.getShell() != null
				&& !fPopupDialog.getShell().isDisposed()) {
			setVisible(false, true);
		} else {
			fPopupDialog.close();
			fPopupDialog = null;
		}

		disposed = true;
	}

	/*
	 * @see IInformationControl#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		fPopupDialog.getShell().setSize(width, height);
	}

	/*
	 * @see IInformationControl#setLocation(Point)
	 */
	public void setLocation(Point location) {
		fPopupDialog.getShell().setLocation(location);
	}

	/*
	 * @see IInformationControl#setSizeConstraints(int, int)
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		fMaxWidth = maxWidth;
		fMaxHeight = maxHeight;
	}

	/*
	 * @see IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		int widthHint = SWT.DEFAULT;
		int heightHint = SWT.DEFAULT;

		if (fMaxWidth > -1)
			widthHint = fMaxWidth;

		if (fMaxHeight > -1)
			heightHint = fMaxHeight;

		return fPopupDialog.getShell().computeSize(widthHint, heightHint, true);
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#computeTrim()
	 * @since 3.0
	 */
	public Rectangle computeTrim() {
		return fPopupDialog.getShell().computeTrim(0, 0, 0, 0);
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#getBounds()
	 * @since 3.0
	 */
	public Rectangle getBounds() {
		return fPopupDialog.getShell().getBounds();
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#restoresLocation()
	 * @since 3.0
	 */
	public boolean restoresLocation() {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#restoresSize()
	 * @since 3.0
	 */
	public boolean restoresSize() {
		return false;
	}

	/*
	 * @see IInformationControl#addDisposeListener(DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener) {
		fPopupDialog.getShell().addDisposeListener(listener);
	}

	/*
	 * @see IInformationControl#removeDisposeListener(DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener) {
		fPopupDialog.getShell().removeDisposeListener(listener);
	}

	/*
	 * @see IInformationControl#setForegroundColor(Color)
	 */
	public void setForegroundColor(Color foreground) {
		composite.setForeground(foreground);
	}

	/*
	 * @see IInformationControl#setBackgroundColor(Color)
	 */
	public void setBackgroundColor(Color background) {
		composite.setBackground(background);
	}

	/*
	 * @see IInformationControl#isFocusControl()
	 */
	public boolean isFocusControl() {
		return composite.isFocusControl();
	}

	/*
	 * @see IInformationControl#setFocus()
	 */
	public void setFocus() {
		fPopupDialog.getShell().forceFocus();
		composite.setFocus();
	}

	/*
	 * @see IInformationControl#addFocusListener(FocusListener)
	 */
	public void addFocusListener(FocusListener listener) {
		composite.addFocusListener(listener);
	}

	/*
	 * @see IInformationControl#removeFocusListener(FocusListener)
	 */
	public void removeFocusListener(FocusListener listener) {
		composite.removeFocusListener(listener);
	}

	public void widgetDisposed(DisposeEvent event) {
	}
}
