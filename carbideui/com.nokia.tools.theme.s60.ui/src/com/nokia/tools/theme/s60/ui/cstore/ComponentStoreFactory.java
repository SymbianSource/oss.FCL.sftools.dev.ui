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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.nokia.tools.theme.s60.cstore.ComponentPoolBackend;
import com.nokia.tools.theme.s60.cstore.ComponentStore;

public class ComponentStoreFactory {

	public static IComponentStore getComponentStore() {
		IComponentStore p = (IComponentStore) Proxy.newProxyInstance(Thread
				.currentThread().getContextClassLoader(),
				new Class[] { IComponentStore.class }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						ComponentStore c = (ComponentStore) ComponentStore.SINGLETON;
						return c.getClass().getMethod(method.getName(),
								method.getParameterTypes()).invoke(c, args);
					}
				});
		return p;
	}

	public static IComponentPool getComponentPool() {
		IComponentPool p = (IComponentPool) Proxy.newProxyInstance(Thread
				.currentThread().getContextClassLoader(),
				new Class[] { IComponentPool.class }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						ComponentPoolBackend c = (ComponentPoolBackend) ComponentPoolBackend
								.getInstance();
						return c.getClass().getMethod(method.getName(),
								method.getParameterTypes()).invoke(c, args);
					}
				});
		return p;
	}

	public static void refreshComponentPool() {
		ComponentPoolBackend.refresh();
	}

}
