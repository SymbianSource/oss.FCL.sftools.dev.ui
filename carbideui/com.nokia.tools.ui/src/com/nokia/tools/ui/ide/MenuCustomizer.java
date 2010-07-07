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

package com.nokia.tools.ui.ide;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

public class MenuCustomizer
    implements IDEConstants {
    private List<HiddenItem> hiddenItems = new ArrayList<HiddenItem>();

    private Set<String> hiddenMenus = new HashSet<String>();

    private Map<String, Set<String>> hiddenMenuItems = new HashMap<String, Set<String>>();

    public void addHiddenMenu(String id) {
        hiddenMenus.add(id);
    }

    public void addHiddenMenuItem(String menuId, String itemId) {
        Set<String> list = hiddenMenuItems.get(menuId);
        if (list == null) {
            list = new HashSet<String>();
            hiddenMenuItems.put(menuId, list);
        }
        list.add(itemId);
    }

    /**
     * Performs additional customization.
     * 
     * @param mgr the menu manager.
     */
    protected void customizeMenu(MenuManager mgr) {
    }

    /**
     * @param mgr
     */
    protected void removePropertiesMenuItem(MenuManager mgr) {
        IContributionItem[] fileItems = mgr.getItems();
        for (int i = 0; i < fileItems.length; i++) {
            if (MENU_FILE_PROPERTIES.equals(fileItems[i].getId())) {
                mgr.remove(fileItems[i]);
                if (i > 0 && fileItems[i - 1] instanceof Separator) {
                    mgr.remove(fileItems[i - 1]);
                    if (i > 1 && fileItems[i - 2] instanceof Separator) {
                        mgr.remove(fileItems[i - 2]);
                    }
                }
            }
        }
    }

    protected void replaceAction(MenuManager mgr, String actionId, final Runnable run) {
        final IContributionItem contrib = mgr.remove(actionId);

        if (contrib instanceof ActionContributionItem) {
            final IAction about = ((ActionContributionItem) contrib).getAction();
            mgr.add(new ActionContributionItem((IAction) Proxy.newProxyInstance(about.getClass().getClassLoader(),
                new Class[] { IAction.class }, new InvocationHandler() {
                    /*
                     * (non-Javadoc)
                     * @see
                     * java.lang.reflect.InvocationHandler#invoke(java.lang.
                     * Object, java.lang.reflect.Method, java.lang.Object[])
                     */
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("runWithEvent".equals(method.getName()) || "run".equals(method.getName())) {
                            run.run();
                            return null;
                        }
                        return method.invoke(about, args);
                    }
                })));
        }
    }

    public void initializeMenus() {
        final WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        MenuManager manager = window.getMenuManager();

        for (IContributionItem item : manager.getItems()) {
            if (item instanceof MenuManager) {
                MenuManager mgr = (MenuManager) item;
                Set<String> list = hiddenMenuItems.get(item.getId());
                if (list != null) {
                    for (String itemId : list) {
                        mgr.remove(itemId);
                    }
                }
                customizeMenu(mgr);
            }
        }
    }

    public void hideExternMenus() {
        WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        MenuManager manager = window.getMenuManager();

        IContributionItem[] items = manager.getItems();
        for (int i = 0; i < items.length; i++) {
            if (hiddenMenus.contains(items[i].getId())) {
                String idBeforeThis = i > 0 ? items[i - 1].getId() : null;
                IContributionItem item = manager.remove(items[i]);
                if (item != null) {
                    hiddenItems.add(new HiddenItem(idBeforeThis, item));
                }
            }
        }
        manager.update(true);
    }

    public void restoreExternMenus() {
        WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        MenuManager manager = window.getMenuManager();
        for (Iterator<HiddenItem> i = hiddenItems.iterator(); i.hasNext();) {
            HiddenItem item = i.next();
            try {
                manager.insertAfter(item.idBeforeThis, item.item);
            } catch (Exception e) {
            }
            i.remove();
        }
        manager.update(true);
    }

    class HiddenItem {
        String idBeforeThis;

        IContributionItem item;

        HiddenItem(String idBeforeThis, IContributionItem item) {
            this.idBeforeThis = idBeforeThis;
            this.item = item;
        }
    }
}
