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
package com.nokia.tools.s60.editor.menus;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.AlignmentRetargetAction;
import org.eclipse.gef.ui.actions.CopyRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightRetargetAction;
import org.eclipse.gef.ui.actions.MatchWidthRetargetAction;
import org.eclipse.gef.ui.actions.PasteRetargetAction;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.actions.AlignmentToolsDropDownAction;
import com.nokia.tools.s60.editor.actions.BrowseForFileAction;
import com.nokia.tools.s60.editor.actions.ClearImageEditorAction;
import com.nokia.tools.s60.editor.actions.ConvertAndEditSVGInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.editor.actions.EditInSystemEditorAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction2;
import com.nokia.tools.s60.editor.actions.EditSoundInSoundEditorAction;
import com.nokia.tools.s60.editor.actions.ElevenPieceConvertAction;
import com.nokia.tools.s60.editor.actions.NinePieceConvertAction;
import com.nokia.tools.s60.editor.actions.PasteContentDataAction;
import com.nokia.tools.s60.editor.actions.SetColorAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeAction;
import com.nokia.tools.s60.editor.actions.ThreePieceConvertAction;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.ide.actions.ThemeModelDropDownAction;
import com.nokia.tools.s60.ide.actions.menu.GeneralRetargetAction;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.IScreenConstants;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.actions.PausePlayingAction;
import com.nokia.tools.screen.ui.actions.PlayAllAction;
import com.nokia.tools.screen.ui.actions.PlayRetargetAction;
import com.nokia.tools.screen.ui.actions.PlaySelectionAction;
import com.nokia.tools.screen.ui.actions.SetPlayingSpeedAction;
import com.nokia.tools.screen.ui.actions.StopPlayingAction;
import com.nokia.tools.screen.ui.editor.BaseEditorContributor;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorActionBarContributor;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorContributor;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IEmbeddedEditorDescriptor;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

/**
 * This contributor contributes the zooming and resolution change actions to the
 * main action bar and the menu.
 */
public class EditorActionBarContributor extends BaseEditorContributor implements
		ZoomListener {

	private static final String EDIT_MENU_ID = "edit";

	private static final String TOOLS_MENU_ID = "Tools";

	private static final String ALIGNMENT_ID = "alignment";

	/**
	 * Parent cool bar is not editor specific, in order to keep it alive after
	 * closing the last editor of different type while another editor is still
	 * using the same coolbar, we keep a global reference count only for parent
	 * coolbar.
	 */
	private static int refCount;

	private boolean embeddedActionAdded;

	private MenuManager viewMenu;

	private ComboContributionItem[] items;

	private IToolBarManager[] toolBars;

	private CustomZoomComboContributionItem zoom;

	private List<IEmbeddedEditorActionBarContributor> contributors = new ArrayList<IEmbeddedEditorActionBarContributor>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.BaseEditorContributor#init(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void init(IActionBars bars) {
		super.init(bars);
		refCount++;

		for (IEmbeddedEditorDescriptor descriptor : ExtensionManager
				.getEmbeddedEditorDescriptors()) {
			IEmbeddedEditorContributor contributor = descriptor
					.getContributor();
			if (contributor != null) {
				try {
					IEmbeddedEditorActionBarContributor contrib = descriptor
							.getContributor().getActionBarContributor();
					contrib.init(getActionBars(), getPage());
					contrib.setParentEditorClass(Series60EditorPart.class);
					contributors.add(contrib);
				} catch (Exception e) {
					S60WorkspacePlugin.error(e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.BaseEditorContributor#contributeToParentCoolBar()
	 */
	protected void contributeToParentCoolBar() {
		if (!coolBarItems.isEmpty()) {
			return;
		}
		// Create zooming items
		String[] zoomStrings = new String[] { ZoomManager.FIT_ALL,
				ZoomManager.FIT_HEIGHT, ZoomManager.FIT_WIDTH };
		zoom = new CustomZoomComboContributionItem(getPage(), zoomStrings);
		IToolBarManager zoomToolBar = contributeToolBarToCoolBar(zoom);
		zoomToolBar.add(new Separator());
		zoomToolBar.add(getAction(GEFActionConstants.ZOOM_IN));
		zoomToolBar.add(getAction(GEFActionConstants.ZOOM_OUT));

		IToolBarManager modelToolBar = contributeToolBarToCoolBar(new ThemeModelDropDownAction());

		// Resolution tools
		ResolutionComboContributionItem resolution = new ResolutionComboContributionItem();
		IToolBarManager resolutionToolBar = contributeToolBarToCoolBar(resolution);
		resolutionToolBar.add(new Separator());
		Action orientationAction = resolution.getOrientationAction();
		resolutionToolBar.add(orientationAction);

		items = new ComboContributionItem[] { resolution };
		toolBars = new IToolBarManager[] { modelToolBar };

		IToolBarManager playToolbar = contributeToolBarToCoolBar(getAction(PlayAllAction.ID));
		playToolbar.add(getAction(PlaySelectionAction.ID));
		playToolbar.add(getAction(PausePlayingAction.ID));
		playToolbar.add(getAction(StopPlayingAction.ID));
		playToolbar.add(new Separator());
		playToolbar.add(getAction(SetPlayingSpeedAction.ID));
	}

	private void contributeEmbeddedActions() {
		if (embeddedActionAdded) {
			return;
		}
		// Alignment and sizing tools
		AlignmentToolsDropDownAction alignmentToolsDropDownAction = new AlignmentToolsDropDownAction(
				getActionRegistry());
		IToolBarManager alignmentToolBar = contributeToolBarToCoolBar(alignmentToolsDropDownAction);
		alignmentToolBar.add(new Separator());
		alignmentToolBar.add(getAction(GEFActionConstants.MATCH_WIDTH));
		alignmentToolBar.add(getAction(GEFActionConstants.MATCH_HEIGHT));

		embeddedActionAdded = true;

		
		parentCoolBarManager.update(true);
		viewMenu.update(true);
	}

	private void removeEmbeddedActions() {
		if (!embeddedActionAdded) {
			return;
		}

		parentCoolBarManager.remove(AlignmentToolsDropDownAction.ID);
		embeddedActionAdded = false;
		//in order to make sure that coolItems controlled by
		//parentCoolBarManager are not disposed when we call an
		//update, we have to check for the empty condition here.
		if(!coolBarItems.isEmpty())
			parentCoolBarManager.update(true);
		viewMenu.update(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#buildActions()
	 */
	@Override
	protected void buildActions() {
		addRetargetAction(new CopyRetargetAction());
		addRetargetAction(new PasteRetargetAction());
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());

		addRetargetAction(new AlignmentRetargetAction(PositionConstants.LEFT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.CENTER));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.RIGHT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.TOP));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.MIDDLE));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.BOTTOM));
		addRetargetAction(new MatchWidthRetargetAction());
		addRetargetAction(new MatchHeightRetargetAction());

		IAction act = new EditImageInBitmapEditorAction(
				(ISelectionProvider) null, null);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		act = new BrowseForFileAction((ISelectionProvider) null, null);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		act = new EditImageInSVGEditorAction((ISelectionProvider) null, null);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		act = new EditSoundInSoundEditorAction((ISelectionProvider) null, null);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		act = new EditMaskAction((ISelectionProvider) null, null);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		act = new ConvertAndEditSVGInBitmapEditorAction(
				(ISelectionProvider) null, null);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));
		
		act = new EditInSystemEditorAction(
				(ISelectionProvider) null, null);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));				

		act = new NinePieceConvertAction((ISelectionProvider) null, null, 1);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		act = new NinePieceConvertAction((ISelectionProvider) null, null, 9);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));
		//11 pic support
		act = new ElevenPieceConvertAction((ISelectionProvider) null, null, 1);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		act = new ElevenPieceConvertAction((ISelectionProvider) null, null, 11);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		act = new ClearImageEditorAction((ISelectionProvider) null, null);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		act = new SetColorAction((ISelectionProvider) null, null);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		act = new SetStretchModeAction((IWorkbenchPart) null,
				IMediaConstants.STRETCHMODE_STRETCH);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText(), IAction.AS_CHECK_BOX));

		act = new SetStretchModeAction((IWorkbenchPart) null,
				IMediaConstants.STRETCHMODE_ASPECT);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText(), IAction.AS_CHECK_BOX));

		act = new EditMaskAction2((ISelectionProvider) null, null);
		addRetargetAction(new GeneralRetargetAction(act.getId(), act.getText(),
				act.getToolTipText()));

		addRetargetAction(new PlayRetargetAction(new PlayAllAction(
				(IWorkbenchPart) null)));

		addRetargetAction(new PlayRetargetAction(new PlaySelectionAction(
				(IWorkbenchPart) null)));

		addRetargetAction(new PlayRetargetAction(new PausePlayingAction(
				(IWorkbenchPart) null)));

		addRetargetAction(new PlayRetargetAction(new StopPlayingAction(
				(IWorkbenchPart) null)));

		addRetargetAction(new PlayRetargetAction(new SetPlayingSpeedAction(
				(IWorkbenchPart) null)));

		addRetargetAction(new GeneralRetargetAction(new PasteContentDataAction(
				(ISelectionProvider) null, null, null)));
	}

	private void contributeEmbeddedMenus(IEditorPart editor) {
		if (viewMenu.find(ALIGNMENT_ID) != null) {
			return;
		}

		viewMenu.add(new Separator());
		MenuManager alignMenu = new MenuManager("&Alignment Tools",
				ALIGNMENT_ID);
		alignMenu.add(getAction(GEFActionConstants.ALIGN_LEFT));
		alignMenu.add(getAction(GEFActionConstants.ALIGN_CENTER));
		alignMenu.add(getAction(GEFActionConstants.ALIGN_RIGHT));
		alignMenu.add(new Separator());
		alignMenu.add(getAction(GEFActionConstants.ALIGN_TOP));
		alignMenu.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
		alignMenu.add(getAction(GEFActionConstants.ALIGN_BOTTOM));
		viewMenu.add(alignMenu);
		viewMenu.add(new Separator());
		viewMenu.add(getAction(GEFActionConstants.MATCH_WIDTH));
		viewMenu.add(getAction(GEFActionConstants.MATCH_HEIGHT));
	}

	private void removeEmbeddedMenus() {
		for (IContributionItem item : viewMenu.getItems()) {
			if (!GEFActionConstants.ZOOM_IN.equals(item.getId())
					&& !GEFActionConstants.ZOOM_OUT.equals(item.getId())) {
				viewMenu.remove(item);
			}
		}
	} /*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
		 */

	public void contributeToMenu(IMenuManager menubar) {
		viewMenu = new MenuManager("&View", "view");
		viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
		viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));

		IMenuManager editMenu = menubar.findMenuUsingPath(EDIT_MENU_ID);

		editMenu.insertAfter(ActionFactory.PASTE.getId(),
				getAction(PasteContentDataAction.ID));

		editMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				// refresh action label
				GeneralRetargetAction retarget = (GeneralRetargetAction) getAction(PasteContentDataAction.ID);
				if (retarget != null && retarget.isHandled()) {
					((WorkbenchPartAction) retarget.getActionHandler())
							.update();
					retarget.setText(retarget.getActionHandler().getText());
					retarget
							.setEnabled(retarget.getActionHandler().isEnabled());
				}
			}
		});

		editMenu.add(new Separator());
		IAction action = getAction(BrowseForFileAction.ID);
		action.setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/select_file.gif"));
		action.setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/select_file.gif"));
		editMenu.add(action);
		editMenu.add(new Separator());
		editMenu.add(getAction(EditImageInBitmapEditorAction.ID));
		editMenu.add(getAction(EditImageInSVGEditorAction.ID));
		editMenu.add(getAction(ConvertAndEditSVGInBitmapEditorAction.ID));
		editMenu.add(getAction(EditSoundInSoundEditorAction.ID));
		editMenu.add(getAction(EditInSystemEditorAction.ID));
		editMenu.add(new Separator());
		editMenu.add(getAction(EditMaskAction.ID));
		editMenu.add(getAction(EditMaskAction2.ID));
		editMenu.add(new Separator());
		action = getAction(ClearImageEditorAction.ID);
		action.setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/clear_co.gif"));
		action.setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/clear_co.gif"));
		editMenu.add(action);
		editMenu.add(new Separator());
		editMenu.add(getAction(NinePieceConvertAction.ID_NINE));
		editMenu.add(getAction(NinePieceConvertAction.ID_SINGLE));
		
		editMenu.add(getAction(ElevenPieceConvertAction.ID_ELEVEN));
		editMenu.add(getAction(ElevenPieceConvertAction.ID_SINGLE));

		/*editMenu.add(getAction(ThreePieceConvertAction.ID_THREE));
		editMenu.add(getAction(ThreePieceConvertAction.ID_SINGLE));*/

		MenuManager menu = new MenuManager(Messages.submenu_StretchMode_label);
		menu.add(getAction(SetStretchModeAction.ID_Stretch));
		menu.add(getAction(SetStretchModeAction.ID_Aspect));
		editMenu.add(new Separator());
		editMenu.add(menu);

		try {
			menubar.insertBefore(TOOLS_MENU_ID, viewMenu);
		} catch (Exception e) {
			
			menubar.insertAfter(EDIT_MENU_ID, viewMenu);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#declareGlobalActionKeys()
	 */
	@Override
	protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.PRINT.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.menus.BaseEditorContributor#refresh(boolean)
	 */
	protected void refresh(int state) {
		if (items != null) {
			for (ComboContributionItem item : items) {
				item.update();
			}
		}
		if (toolBars != null) {
			for (IToolBarManager toolBar : toolBars) {
				toolBar.update(true);
			}
		}
		if (isEditorActive()) {
			if (getContentEditor() != null) {
				ZoomManager manager = (ZoomManager) getContentEditor()
						.getAdapter(ZoomManager.class);
				if (manager != null) {
					manager.removeZoomListener(this);
					manager.addZoomListener(this);
				}
				zoom.setZoomManager(manager);
			}
		} else {
			zoom.setZoomManager(null);
		}
		IEditorPart editor = getContentEditor();
		if (STATE_ACTIVATE == state) {
			IScreenElement element = ScreenUtil.getScreen(editor);
			if (element != null && element.getData() != null
					&& element.getData().getRoot() != null) {
				if (!ScreenUtil.isPrimaryContent(element.getData())) {
					contributeEmbeddedActions();
				} else {
					removeEmbeddedActions();
				}
			}
		} else if (STATE_DEACTIVATE == state) {
			removeEmbeddedActions();
		}
		if (editor != null) {
			setActiveEditor(editor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.ZoomListener#zoomChanged(double)
	 */
	public void zoomChanged(double zoom) {
		IPreferenceStore store = UiPlugin.getDefault().getPreferenceStore();
		store.setValue(IScreenConstants.PREF_ZOOMING_FACTOR, zoom);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IEditorPart editor) {
		super.setActiveEditor(editor);
		boolean showEmbeddedMenu = false;
		if (editor != null) {
			IContentAdapter adapter = (IContentAdapter) editor
					.getAdapter(IContentAdapter.class);
			if (adapter != null) {
				for (IContent content : adapter.getContents()) {
					if (!ScreenUtil.isPrimaryContent(content)) {
						showEmbeddedMenu = true;
						break;
					}
				}
			}
		}
		if (showEmbeddedMenu) {
			contributeEmbeddedMenus(editor);
		} else {
			removeEmbeddedMenus();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.menus.BaseEditorContributor#getContentEditor()
	 */
	@Override
	protected IEditorPart getContentEditor() {
		IEditorPart part = super.getContentEditor();
		if (part != null
				&& (part instanceof Series60EditorPart || part
						.getAdapter(Series60EditorPart.class) != null)) {
			return part;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.BaseEditorContributor#dispose()
	 */
	@Override
	public void dispose() {
		refCount--;
		if (refCount < 0) {
			refCount = 0;
		}
		for (IEditorActionBarContributor contributor : contributors) {
			contributor.dispose();
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.BaseEditorContributor#canDisposeCoolBarItems()
	 */
	@Override
	protected boolean canDisposeCoolBarItems() {
		return refCount <= 0;
	}
}
