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
package com.nokia.tools.media.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.resource.util.IInvokable;

/**
 * This class provides static methods for some common thread related operations.
 * 
 */
public class ThreadUtils {
	/**
	 * No instantiation.
	 */
	private ThreadUtils() {
	}

	/**
	 * Returns a monitor that is safe to use in non-display thread.
	 * 
	 * @param monitor the actual monitor to be wrapped.
	 * @return the monitor that is display-thread safe.
	 */
	public static IProgressMonitor displaySynched(final IProgressMonitor monitor) {
		if (monitor == null) {
			return null;
		}
		return (IProgressMonitor) Proxy.newProxyInstance(monitor.getClass()
				.getClassLoader(), new Class[] { IProgressMonitor.class },
				new InvocationHandler() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
					 *      java.lang.reflect.Method, java.lang.Object[])
					 */
					public Object invoke(Object proxy, final Method method,
							final Object[] args) throws Throwable {
						String name = method.getName();
						if ("beginTask".equals(name) || "subTask".equals(name)
								|| "worked".equals(name) || "done".equals(name)) {
							Display.getDefault().syncExec(new Runnable() {

								/*
								 * (non-Javadoc)
								 * 
								 * @see java.lang.Runnable#run()
								 */
								public void run() {
									try {
										method.invoke(monitor, args);
									} catch (Throwable e) {
										UtilsPlugin.error(e);
									}
								}
							});
							return null;
						} else {
							return method.invoke(monitor, args);
						}
					}
				});
	}

	/**
	 * Invokes the given {@link invokable} in the display thread. Some JEM
	 * method calls have to be invoked in the display thread. When there is
	 * invocation error, the {@link IInvokable#handleException(Throwable)}
	 * method will be called.
	 * 
	 * @param invokable the invokable to be invoked.
	 * @return the result from the invocation.
	 */
	public static Object syncDisplayExec(final IInvokable invokable) {
		final Object[] result = new Object[1];
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					result[0] = invokable.invoke();
				} catch (Throwable e) {
					invokable.handleException(e);
				}
			}
		});
		return result[0];
	}
}
