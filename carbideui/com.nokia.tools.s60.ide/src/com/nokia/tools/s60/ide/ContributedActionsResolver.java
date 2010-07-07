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
package com.nokia.tools.s60.ide;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.WorkbenchPart;
import org.osgi.framework.Bundle;

import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.s60.editor.actions.BrowseForFileAction;
import com.nokia.tools.s60.editor.actions.SetColorAction;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.internal.utils.HideableMenuManager;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

/**
 * Provides services for contributedActions as adding to context menu etc.
 * Contributed action is {@link WorkbenchPartAction} that is, after registering,
 * available in context menus of specified Views or Editors. ContributedActions
 * are defined through Eclipse's extension point mechanism.<br/><br/>
 * ContributedAction definition example:
 * 
 * <pre>
 *  &lt;extension
 *      point=&quot;com.nokia.tools.s60.ide.contributedAction&quot;&gt;
 *    &lt;contributedAction
 *          pos=&quot;1&quot;
 *          targetGroup=&quot;%ReferenceToColor&quot;
 *          validFor=&quot;components resource&quot;
 *          workbenchPartAction=&quot;com.nokia.tools.s60.editor.actions.AddToGroupAction&quot;&gt;
 *       &lt;editor
 *             class=&quot;com.nokia.tools.s60.editor.Series60EditorPart&quot;&gt;
 *       &lt;/editor&gt;
 *    &lt;/contributedAction&gt;
 *  &lt;/extension&gt; 
 * </pre>
 * 
 * 
 * 
 * <p>
 * <b>Class Use Cases:</b>
 * </p>
 * 
 * <b>UC1:</b><br/><br/> <b>Q</b>: How do I add contributedActions to
 * context menu in my view(editor) if I DO have access to instance of my
 * view(editor) in the process of context menu creation?.<br/><br/> <b>A:</b>
 * In a place where you create context menu for your view(editor) call
 * {@link ContributedActionsResolver#getInstance()#contributeActions(IMenuManager, String, IWorkbenchPart)}.<br/><br/>
 * 
 * <b>UC2:</b><br/><br/> <b>Q</b>: How do I add contributedActions to
 * context menu in my view(editor) if I DO NOT have access to instance of my
 * view(editor) in the process of context menu creation?<br/><br/> <b>A:</b>
 * In a place where you create context menu for your view(editor) call
 * {@link ContributedActionsResolver#getInstance()#contributeActions(IMenuManager, String, ActionRegistry)}.
 * Prior to this method call, your contributedActions needs to be registered with
 * ActionRegistry the view(editor) is using. This can be done through
 * {@link ContributedActionsResolver#getInstance()#addToRegistry(IWorkbenchPart, ActionRegistry)}<br/><br/>
 * 
 * <b>UC3:</b><br/><br/> <b>Q</b>: How do I alter contributedActions before
 * they are added to context menu.<br/><br/> <b>A:</b> In that case you have
 * to:
 * <ol>
 * <li>obtain contributed actions {@link #getActions(String, IWorkbenchPart)}</li>
 * <li>alter them as you wish to</li>
 * <li>register them in ActionRegistry</li>
 * <li>Contribute them to context menu
 * {@link #contributeActions(IMenuManager, String, ActionRegistry)}</li>
 * </ol>
 * Example:
 * 
 * <pre>
 * ActionRegistry registry = new ActionRegistry();
 * //ad 1.
 * IAction[] actions = ContributedActionsResolver.getInstance().getActions(
 * 		&quot;gallery&quot;, (WorkbenchPart) EclipseUtils.getActiveSafeEditor());
 * //ad 2.
 * for (IAction action : actions) {
 * 	if (action instanceof IGalleryScreenAction) {
 * 		IGalleryScreenAction galleryAction = (IGalleryScreenAction) action;
 * 		galleryAction.setGalleryPage(GalleryPage.this);
 * 		galleryAction.setScreen(screen);
 * 	}
 * 	//ad 3.
 * 	registry.registerAction(action);
 * }
 * //ad 4.
 * ContributedActionsResolver.getInstance().contributeActions(manager, &quot;gallery&quot;,
 * 		registry);
 * 
 * </pre>
 * 
 * <br/><br/>
 * 
 * 
 * <tr>
 * <td>Contributed actions are displayed only if active editor is specified as
 * allowed editor in extension point
 * <code>com.nokia.tools.s60.ide.contributedAction</code> definition.</td>
 * <td></td>
 * <td></td>
 * </tr>
 * </table>
 */
final public class ContributedActionsResolver {

	/** Extension point name for contributedActions in context menus. */
	public static final String EXTENSION_POINT = S60WorkspacePlugin.getDefault().getBundle().getSymbolicName()+ ".contributedAction"; 

	/**
	 * XML element name as defined in extension point definitions. Defines whole
	 * contributedAction.
	 */
	private static final String CONTRIBUTEDACTIONS_ELEMENT = "contributedAction"; 

	/**
	 * XML attribute name of {@link #CONTRIBUTEDACTIONS_ELEMENT}. Defines name
	 * of the class of contributedAction.
	 */	
	private static final String CLASS_ATTRIBUTE = "workbenchPartAction"; 

	/**
	 * XML attribute name of {@link #CONTRIBUTEDACTIONS_ELEMENT}. Defines
	 * position of contributedAction in context menu.
	 */	
	private static final String POS_ATTRIBUTE = "pos"; 

	/**
	 * XML attribute name of {@link #CONTRIBUTEDACTIONS_ELEMENT}. Defines
	 * allowed WorkBenchParts. In this part, contributed actions are visible in
	 * context menu.
	 */
	private static final String VALIDFOR_ATTRIBUTE = "validFor"; 

	/**
	 * XML attribute name of {@link #CONTRIBUTEDACTIONS_ELEMENT}. Defines group
	 * name where contributedAction belongs. Altogether with submenu paths.
	 */	
	private static final String TARGETGROUP_ATTRIBUTE = "targetGroup"; 

	/**
	 * XML element. Child of {@link #CONTRIBUTEDACTIONS_ELEMENT}. Defines
	 * allowed editors for this action and context.
	 */
	private static final String EDITOR_ELEMENT = "editor"; 

	/**
	 * XML attribute name of {@link #EDITOR_ELEMENT}. Defines fully-classified
	 * name of allowed editor.
	 */
	private static final String EDITOR_ELEMENT_CLASS_ATTRIBUTE = "class"; 

	/**
	 * Singleton instance.
	 */
	private static final ContributedActionsResolver contributedActionsResolverInstance = new ContributedActionsResolver();
	
	/**
	 * Loaded contributedActions.
	 */
	private Map<Class<WorkbenchPartAction>, ActionInfo> contributedActions = new HashMap<Class<WorkbenchPartAction>, ActionInfo>();

	/**
	 * Class constructor.
	 */
	private ContributedActionsResolver() {
		initialize();
	}

	/**
	 * Gets singleton instance.
	 * 
	 * @return ContributedActionsResolver instance.
	 * 
	 */
	public static final ContributedActionsResolver getInstance() {
		return contributedActionsResolverInstance; 
	}
	
	/**
	 * Loads all contributedActions from extension point definitions.
	 */
	@SuppressWarnings("unchecked")
	synchronized private void initialize() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT).getConfigurationElements();
		for (IConfigurationElement element : elements) {
			if (CONTRIBUTEDACTIONS_ELEMENT.equalsIgnoreCase(element.getName())) {
				String _clazz = element.getAttribute(CLASS_ATTRIBUTE);
				Bundle declaringPlugin = Platform.getBundle(element
						.getNamespaceIdentifier());
				try {
					Class<WorkbenchPartAction> clazz = declaringPlugin.loadClass(_clazz);
					ActionInfo actionInfo = contributedActions.get(clazz);
					if (actionInfo == null) {
						actionInfo = new ActionInfo(clazz);
						contributedActions.put(clazz, actionInfo);						
					} 
					actionInfo.addActionContext(element);
				} catch (ClassNotFoundException e) {
					S60WorkspacePlugin.error(
							"Error loading contributed action", e);
				}
			}
		}
	}

	/**
	 * Creates contributedActions and register them with ActionRegistry.
	 * 
	 * @param workbenchPart
	 *            WorkBenchPart for which contributedActions are created.
	 * @param actionRegistry
	 *            ActionRegistry where contributedActions are registered.
	 */
	public synchronized void addToRegistry(final IWorkbenchPart workbenchPart,
			final ActionRegistry actionRegistry) {
		for (ActionInfo info : contributedActions.values()) {
			Object[] params = { workbenchPart };
			try {
				WorkbenchPartAction act = info.actionClass.getConstructor(
						new Class[] { IWorkbenchPart.class }).newInstance(
						params);
				info.workBenchPartActionId = act.getId();
				actionRegistry.registerAction(act);
			} catch (IllegalArgumentException e) {
				S60WorkspacePlugin.error(e);
			} catch (SecurityException e) {
				S60WorkspacePlugin.error(e);
			} catch (InstantiationException e) {
				S60WorkspacePlugin.error(e);
			} catch (IllegalAccessException e) {
				S60WorkspacePlugin.error(e);
			} catch (InvocationTargetException e) {
				S60WorkspacePlugin.error(e);
			} catch (NoSuchMethodException e) {
				S60WorkspacePlugin.error(e);
			}
		}
	}

	/**
	 * Creates actions allowed in specified uiContext.
	 * 
	 * 
	 * @param uiContext
	 *            Only actions with this context will be returned.<br/>uiContext
	 *            examples: editor, outline, components, layers, gallery etc..
	 *            <br/>Cannot be null.
	 * @param workbenchPart
	 *            WorkBenchPart for which contributedActions are created.<br/>Cannot
	 *            be null.
	 * @return actions that are allowed for specified uiContext.
	 */
	public IAction[] getActions(final String uiContext, final IWorkbenchPart workbenchPart) {
		if (uiContext == null)
			throw new IllegalArgumentException("uiContext cannot be null!");		
		if (workbenchPart == null)
			throw new IllegalArgumentException("IWorkbenchPart cannot be null!");		
		List<IAction> toRet = new ArrayList<IAction>();
		for (ActionInfo info : contributedActions.values()) {
			if (info.isActionAllowed(uiContext)) {
				Object[] params = { workbenchPart };
				try {
					WorkbenchPartAction act = null;

					if (workbenchPart != null) {
						act = info.actionClass.getConstructor(
								new Class[] { IWorkbenchPart.class })
								.newInstance(params);
						info.workBenchPartActionId = act.getId();
					}
					if (act == null)
						continue;

					toRet.add(act);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return toRet.toArray(new IAction[0]);
	}

	/**
	 * Creates contributedActions and adds them to IMenuManager.
	 * ContributedActions are added to IMenuManager only if they are allowed in
	 * specified uiContext.
	 * 
	 * @param manager
	 *            IMenuManager we are adding contributedActions to. <br/>Cannot
	 *            be null.
	 * @param uiContext
	 *            Only actions with this context will be added to
	 *            IMenuManager(filtering criteria).<br/>uiContext examples:
	 *            editor, outline, components, layers, gallery etc.. <br/>Cannot
	 *            be null.
	 * @param workbenchPart
	 *            WorkBenchPart for which contributedActions are created.<br/>Cannot
	 *            be null.
	 */
	public void contributeActions(final IMenuManager manager, final String uiContext,
			final IWorkbenchPart workbenchPart) {
		if (manager == null)
			throw new IllegalArgumentException("IMenuManager cannot be null!");
		if (uiContext == null)
			throw new IllegalArgumentException("uiContext cannot be null!");		
		if (workbenchPart == null)
			throw new IllegalArgumentException("IWorkbenchPart cannot be null!");

		final Map<String, Map<WorkbenchPartAction, ActionInfo>> result = new HashMap<String, Map<WorkbenchPartAction, ActionInfo>>();
		for (ActionInfo info : contributedActions.values()) {
			if (info.isActionAllowed(uiContext)) {
				final Object[] params = { workbenchPart };
				try {
					WorkbenchPartAction workbenchPartAction = null;
					workbenchPartAction = info.actionClass.getConstructor(
								new Class[] { IWorkbenchPart.class })
								.newInstance(params);
					info.workBenchPartActionId = workbenchPartAction.getId();
					addToResult(result, workbenchPartAction, info, uiContext);
				} catch (IllegalArgumentException e) {
					S60WorkspacePlugin.error(e);
				} catch (SecurityException e) {
					S60WorkspacePlugin.error(e);
				} catch (InstantiationException e) {
					S60WorkspacePlugin.error(e);
				} catch (IllegalAccessException e) {
					S60WorkspacePlugin.error(e);
				} catch (InvocationTargetException e) {
					S60WorkspacePlugin.error(e);
				} catch (NoSuchMethodException e) {
					S60WorkspacePlugin.error(e);
				}
			}
		}
		addToMenuManager(manager, result, uiContext);
	}	
	
	/**
	 * Adds contributedActions from ActionRegistry to IMenuManager.
	 * ContributedActions are added to IMenuManager only if they are allowed in
	 * specified uiContext.
	 * 
	 * @see #addToRegistry(IWorkbenchPart, ActionRegistry)
	 * 
	 * 
	 * @param manager
	 *            IMenuManager we are adding contributedActions to. <br/>Cannot be null.
	 * @param uiContext
	 *            Only actions with this context will be added to
	 *            IMenuManager(filtering criteria).<br/>uiContext examples:
	 *            editor, outline, components, layers, gallery etc..
	 *            <br/>Cannot be null.
	 * @param actionRegistry
	 *            ActionRegistry where contributedActions are registered.
	 *            <br/>Cannot be null.
	 */
	public void contributeActions(final IMenuManager manager, final String uiContext,
			final ActionRegistry actionRegistry) {
		if (manager == null)
			throw new IllegalArgumentException("IMenuManager cannot be null!");
		if (uiContext == null)
			throw new IllegalArgumentException("uiContext cannot be null!");
		if (actionRegistry == null)
			throw new IllegalArgumentException("IWorkbenchPart cannot be null!");		
		
		
		final Map<String, Map<WorkbenchPartAction, ActionInfo>> result = new HashMap<String, Map<WorkbenchPartAction, ActionInfo>>();
		for (ActionInfo info : contributedActions.values()) {
			
			if (info.workBenchPartActionId == null)
				continue;			
			if (info.isActionAllowed(uiContext)) {
					WorkbenchPartAction workbenchPartAction = (WorkbenchPartAction) actionRegistry.getAction(info.workBenchPartActionId);
					
					if (workbenchPartAction == null)
						continue;
					addToResult(result, workbenchPartAction, info, uiContext);
			}
		}
		addToMenuManager(manager, result, uiContext);
	}

	/**
	 * Adds enabled actions to result Map 
	 */
	private void addToResult(final Map<String, Map<WorkbenchPartAction, ActionInfo>> result, final WorkbenchPartAction workbenchPartAction, final ActionInfo info, final String uiContext) {
		if (workbenchPartAction.isEnabled()) {
			final String group = info.getTargetGroupForContext(uiContext);
			if (result.get(group) == null) {
				result.put(group,new HashMap<WorkbenchPartAction, ActionInfo>());
			}
			Map<WorkbenchPartAction, ActionInfo> vMap = result.get(group);
			vMap.put(workbenchPartAction, info);
		}		
	}

	/**
	 * Sort actions and put them into menu.
	 */
	private void addToMenuManager(final IMenuManager manager, final Map<String, Map<WorkbenchPartAction, ActionInfo>> result, final String uiContext) {
		for (String grooup : result.keySet()) {
			final Map<WorkbenchPartAction, ActionInfo> actions = result
					.get(grooup);
			WorkbenchPartAction[] instances = actions.keySet().toArray(
					new WorkbenchPartAction[actions.size()]);
			Arrays.sort(instances, new Comparator<WorkbenchPartAction>() {
				public int compare(WorkbenchPartAction o1,
						WorkbenchPartAction o2) {
					int positionForContext1 = actions.get(o1).getPositionForContext(uiContext);
					int positionForContext2 = actions.get(o2).getPositionForContext(uiContext);
					return positionForContext1 > positionForContext2 ? 1 : -1;
				}
			});
			// add to group grooup in menu manager
			for (WorkbenchPartAction pa : instances) {
				if (ContributedActionsResolver.ActionInfo.WorkBenchPartInfo.DEFAULT_TARGET_GROUP.equals(grooup)) {
					if (pa instanceof IContributionItem) {
						manager.add((IContributionItem) pa);
					} else {
						manager.add(pa);
					}
				} else {
					createMenu(manager, grooup, pa);
				}
			}
		}		
	}	
	
	/**
	 * Method adds to menu action by given path.
	 * 
	 * @param manager
	 *            main context menu
	 * @param groupName
	 *            specifies path to menu ,where should be action added.
	 * @param action
	 */
	private void createMenu(IContributionManager manager, String groupName,
			IAction action) {
		String[] path = groupName.trim().split("\\\\");
		MenuManager menu = (MenuManager) manager;
		
		//Fix for Reference color so that it does not appear for the icons.
		if(groupName.contains(Messages.Colors_Reference2Color)){
			if(menu.find(BrowseForFileAction.ID) != null)
			return;
		}
		
		if (path.length > 0) {
			for (int i = 1; i < path.length; i++) {
				IContributionItem hMenu = menu.find(path[i]);
				if (hMenu == null) {
					hMenu = new HideableMenuManager(path[i]);
				}
				menu.add(hMenu);
				createGroup(menu, path[i], hMenu);
//					if (action.getMenuCreator()!=null){
//							Menu subM = action.getMenuCreator().getMenu(menu.getMenu());
//							
//							}
				menu = (MenuManager) hMenu;
			}
		}
		// Create Group
		createGroup(menu, groupName, action);
	}
	
	/**
	 * Method adds to menu group action.
	 * 
	 * @param manager
	 *            where should be action added.
	 * @param groupName
	 * @param menu
	 */
	private void createGroup(MenuManager manager, String groupName,
			IContributionItem menu) {
		IContributionItem find = manager.find(groupName);
		if (find == null || !(find instanceof GroupMarker)) {
//			manager.f
//			menu.
//			new MenuItem(menu,SWT.SEPARATOR);
			manager.add(new GroupMarker(groupName));
		}
		manager.appendToGroup(groupName, menu);
	}

	/**
	 * Method adds to menu group action.
	 * 
	 * @param manager
	 *            where should be action added.
	 * @param groupName
	 * @param action
	 */
	private void createGroup(IContributionManager manager, String groupName,
			IAction action) {
		IContributionItem find = manager.find(groupName);
		if (find == null || !(find instanceof GroupMarker)) {
			manager.add(new GroupMarker(groupName));
		}
		manager.appendToGroup(groupName, action);
	}

	
	/**
	 * Stores information related to single contributedAction.
	 * 
	
	 */ 
	private static class ActionInfo {

		/**
		 * Class constructor
		 * 
		 * @param clazz
		 *            fully-qualified contributedAction class name
		 */
		ActionInfo(final Class<WorkbenchPartAction> clazz) {
			this.validContexts = new ArrayList<ContributedActionsResolver.ActionInfo.WorkBenchPartInfo>();
			this.actionClass = clazz;
		}

		/** fully-qualified contributedAction class name */
		private Class<WorkbenchPartAction> actionClass;
		
		/** WorkBenchPartAction ID {@link WorkbenchPartAction#getId() }*/
		private String workBenchPartActionId;
		
		/**
		 * All {@link WorkbenchPart}s in which this contributedAction is
		 * allowed to be shown in context menus.
		 */
		final List<WorkBenchPartInfo> validContexts;

		
		/**
		 * Adds context for this action.
		 * 
		 * @param parentElement @{@link ContributedActionsResolver#CONTRIBUTEDACTIONS_ELEMENT}
		 */
		void addActionContext(IConfigurationElement parentElement) {
			final String contextValidFor = parentElement.getAttribute(VALIDFOR_ATTRIBUTE);
			final String targetGroup = parentElement.getAttribute(TARGETGROUP_ATTRIBUTE); 
			final String position = parentElement.getAttribute(POS_ATTRIBUTE);			
			final List<String> allowedEditors = findAllowedEditors(parentElement);
			String[] contexts = contextValidFor.split(" ");
			for (String context : contexts) {
				WorkBenchPartInfo part = new WorkBenchPartInfo(context, targetGroup, position, allowedEditors);
				if (!validContexts.contains(part)) {
					validContexts.add(part);
				} else {
					WorkBenchPartInfo workBenchPart = validContexts.get(validContexts.indexOf(part));
					try {
						workBenchPart.mergeInformation(part);
					} catch (Exception e) {
						StringBuilder message = new StringBuilder();
						message.append("For contributedAction ");
						message.append(this.actionClass.getName());						
						message.append(" collides position or targetGroup in ");
						message.append(workBenchPart.getContext() + " context(");
						message.append(VALIDFOR_ATTRIBUTE);
						message.append(" attribute). Positions[");
						message.append(workBenchPart.getPosition() + "," + part.getPosition());
						message.append("] TargetGroups[");
						message.append(workBenchPart.getTargetGroup() + ",");
						message.append(part.getTargetGroup() + "]." + "Check action definitions in plugin.xml files.");						
						S60WorkspacePlugin.warn(message.toString(), e);
					}
				}
			}
		}
		
		/**
		 * Finds allowed editors for this contributedAction in extension
		 * section.
		 * 
		 * @param parentElement
		 *            that has {@link #EDITOR_ELEMENT}s as its children
		 *            
		 * @return allowed editors
		 */
		private ArrayList<String> findAllowedEditors(final IConfigurationElement parentElement) {
			ArrayList<String> allowedEditors = new ArrayList<String>();
			IConfigurationElement[] contributedActionsChildren = parentElement.getChildren();
			for (IConfigurationElement contributedActionsChild : contributedActionsChildren) {
				if (EDITOR_ELEMENT.equals(contributedActionsChild.getName())) {
					String classAttribute = contributedActionsChild
							.getAttribute(EDITOR_ELEMENT_CLASS_ATTRIBUTE);
					allowedEditors.add(classAttribute);
				}
			}
			return allowedEditors;
		}

		/**
	
		 * 
		 * @param uiContext
		 *            context(WorkBenchPart) where context menu is being
		 *            created.
		 * @return <br/>true - action is allowed<br/>false - action is not
		 *         allowed
		 */
		public boolean isActionAllowed(final String uiContext) {
			boolean isActionAllowed = false;
			WorkBenchPartInfo requestedPart = createWorkBenchPartInfo(uiContext);
			if (validContexts.contains(requestedPart)) {
				WorkBenchPartInfo workBenchPart = validContexts.get(validContexts.indexOf(requestedPart));
				isActionAllowed = workBenchPart.isAllowedForActiveEditor();
			}
			return isActionAllowed;
		}
		
		/**
		 * Finds target group(for current contributedAction and context) where
		 * this action will be placed.
		 * 
		 * @param uiContext
		 *            WorkBenchPart context
		 */
		public String getTargetGroupForContext(final String uiContext) {
			WorkBenchPartInfo requestedPart = createWorkBenchPartInfo(uiContext);
			WorkBenchPartInfo workBenchPart = validContexts.get(validContexts.indexOf(requestedPart));
			return workBenchPart.targetGroup;
		}
		
		/**
		 * Finds position in context menu(for current contributedAction and
		 * context) where this action will be placed.
		 * 
		 * @param uiContext
		 *            WorkBenchPart context
		 */		
		public int getPositionForContext(final String uiContext) {
			WorkBenchPartInfo requestedPart = createWorkBenchPartInfo(uiContext);
			WorkBenchPartInfo workBenchPart = validContexts.get(validContexts.indexOf(requestedPart));
			return workBenchPart.position;
		}		
		
		/**
		 * Creates new WorkBenchPartInfo.
		 * 
		 * @param context -
		 *            its context
		 * @return new instance
		 * 
		 */
		private WorkBenchPartInfo createWorkBenchPartInfo(final String context) {
			return new WorkBenchPartInfo(context, null, null, Arrays.asList(""));
		}
			
		
		/**
		 * Represents single entry from entries specified in {@link #VALIDFOR_ATTRIBUTE}
		 * 
		
		 *
		 */
		private static class WorkBenchPartInfo {
			
			/** If there is no targetGroup, this is chosen as default one. */
			public static final String DEFAULT_TARGET_GROUP = "-";
			
			/** 
			 * ContributedAction has to be visible in this context(WorkBenchPart). 
			 * 
			 * @see ContributedActionsResolver#VALIDFOR_ATTRIBUTE 
			 */
			private String context;
			
			/**
			 * Group where this action belongs in contextMenu. Also defines path
			 * through context submenus
			 */
			private String targetGroup;
			
			/** Position in context menu */
			private int position;
			
			/**
			 * Editors from which one editor has to be active for action to be
			 * allowed.
			 */
			final List<String> allowedEditors;

			
			/**
			 * Class constructor.
			 * 
			 * @param context
			 *            {@link WorkbenchPart} where this action should be
			 *            visible in context menu.
			 * @param targetGroup
			 *            Group where this action belongs in contextMenu. Also
			 *            defines path through context submenus
			 * @param position
			 * @param allowedEditors List of allowed aditors for this context
			 */
			WorkBenchPartInfo(final String context, final String targetGroup, final String position, final List<String> allowedEditors) {
				this.context = context.trim();
				if (StringUtils.isEmpty(targetGroup)) {
					this.targetGroup = DEFAULT_TARGET_GROUP; 
				} else {
					this.targetGroup =  targetGroup.trim();
				}
				this.allowedEditors = allowedEditors;
				if (position != null) {
					this.position = Integer.parseInt(position);
				} else {
					this.position = Integer.MAX_VALUE;				
				}
			}			
			
			/**
			 * Checks if this action is allowed for currently active editor. If
			 * the action in not allowed, action MUST NOT BE visible in menu.
			 * Allowed editors are defined in
			 * <code>com.nokia.tools.s60.ide.contributedAction: contributedAction/editor@class</code>
			 * extension point contributions.<br/> <b>true</b> - action is
			 * allowed, should be visible in menu.<br/> <b>false</b> - action
			 * is not allowed, must not be visible in menu<br/>
			 */
			boolean isAllowedForActiveEditor() {
				boolean isActive = false;
				final IEditorPart activeEditor = EclipseUtils.getActiveSafeEditor();
				if (activeEditor != null) {
					final String activeEditorClassName = activeEditor.getClass().getName();
					isActive = allowedEditors.contains(activeEditorClassName);
				}
				return isActive;
			}
			
			/**
			 * Gets context in which contributionAction has to be visible.
			 * 
			 * @return contributionAction context
			 */
			String getContext() {
				return context;
			}
			
			/**
			 * Gets group where this action belongs in contextMenu including
			 * submenu path.
			 * 
			 * @return target group for contributedAction
			 */
			String getTargetGroup() {
				return targetGroup;
			}
			
			/**
			 * Gets desired position in context menu.
			 * 
			 * @return position in context menu.
			 */
			int getPosition() {
				return position;
			}

			/**
			 * If there are two actions with same context but they differentiate
			 * in target group and position, former target group and position
			 * takes precedence.
			 * 
			 * @param part
			 *            Part to merge.
			 * @throws Exception
			 *             Thrown, if there is targetGroup or position
			 *             collision.
			 */
			void mergeInformation(WorkBenchPartInfo part) throws Exception {
				for (String allowedEditor : part.allowedEditors) {
					if (!allowedEditors.contains(allowedEditor)) {
						allowedEditors.add(allowedEditor);
						if (this.position != part.getPosition() || !this.targetGroup.equals(part.getTargetGroup())) {
							throw new Exception("Position or targetGroup collision" );
						}
					}
				}
			}			
			
			/**
			 * Two WorkBenchPartInfo objects are equal if and only if their
			 * contexts are the same.
			 */
			@Override
			public boolean equals(Object obj) {
				if (obj == null || !(obj instanceof WorkBenchPartInfo)) {
					return false;
				}
				WorkBenchPartInfo other = (WorkBenchPartInfo) obj;
				if (context.equals(other.context)) {
					return true;
				} else {
					return false;
				}
			}
			
			@Override
			public int hashCode() {
				return context.hashCode();
			}
		}

	}	
	
}