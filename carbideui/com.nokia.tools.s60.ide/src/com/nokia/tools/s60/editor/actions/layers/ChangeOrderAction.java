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
package com.nokia.tools.s60.editor.actions.layers;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

public class ChangeOrderAction extends BaseLayerAction {

	/* move DOWN action id */
	public static int UP = -1;

	/* move UP action id */
	public static int DOWN = 1;

	private int type;
	
	public static final String ID_UP = ChangeOrderAction.class.getSimpleName() + UP;
	public static final String ID_DOWN = ChangeOrderAction.class.getSimpleName() + DOWN;

	public ChangeOrderAction(ILayerActionsHost host, int type, boolean lazy) {
		super(null, host, lazy );		
		if (type == UP) {
			setId(ID_UP);
			setText(Messages.Layers_MoveUpAction_title);
			setToolTipText(Messages.Layers_MoveUpAction_title);
		} else if (type == DOWN) {
			setId(ID_DOWN);
			setText(Messages.Layers_MoveDownAction_title);
			setToolTipText(Messages.Layers_MoveDownAction_title);
		}	
		this.type = type;
		if (!lazy && host != null)
			setEnabled(calculateEnabled());
	}

	@Override
	public void run() {
		Object selected = getHost().getSelection().getFirstElement();
		if (selected instanceof ILayer) {
			try {
				((ILayer) selected).getParent().changeLayerOrder(
						(ILayer) selected, type);
			} catch (Exception e) {

				e.printStackTrace();
			}
		} else if (selected instanceof ILayerEffect) {
			try {
				ILayer parent = ((ILayerEffect) selected).getParent();
				List<ILayerEffect> selEff = parent.getSelectedLayerEffects();
				int pos = selEff.indexOf(selected);
				int delta = type;

				if (type == UP) {
					
					int n_index = parent.getLayerEffects().indexOf(
							selEff.get(pos + delta));
					int o_index = parent.getLayerEffects().indexOf(selected);
					((ILayerEffect) selected).getParent().changeEffectOrder(
							(ILayerEffect) selected, n_index - o_index);
				}
				if (type == DOWN) {
					
					int n_index = parent.getLayerEffects().indexOf(
							selEff.get(pos + delta));
					int o_index = parent.getLayerEffects().indexOf(selected);
					((ILayerEffect) selected).getParent().changeEffectOrder(
							(ILayerEffect) selected, n_index - o_index);
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}

	@Override
	protected boolean calculateEnabled() {		
		Object selected = getHost().getSelection().getFirstElement();
		if (selected instanceof ILayer) {
			if (type == UP) {
				IImage parent = ((ILayer) selected).getParent();
				if (parent.getLayer(0).isBackground()) {
					return ((ILayer) selected).getParent().getLayers().indexOf(
							selected) > 1;
				} else {
					return ((ILayer) selected).getParent().getLayers().indexOf(
							selected) > 0;
				}
			} else if (type == DOWN) {
				// down
				ILayer layer = (ILayer) selected;
				// IImage parent = ((ILayer) selected).getParent();
				int size = layer.getParent().getLayers().size();

				return (layer.getParent().getLayers().indexOf(layer) < size - 1 && !layer
						.isBackground());
			}
		} else if (selected instanceof ILayerEffect) {
			ILayerEffect effect = (ILayerEffect) selected;
			List<ILayerEffect> selEffList = effect.getParent()
					.getSelectedLayerEffects();
			int size = selEffList.size();
			int pos = selEffList.indexOf(effect);
			if (type == UP) {
				if (IMediaConstants.APPLY_GRAPHICS.equals(selEffList.get(0)
						.getName())
						|| IMediaConstants.APPLY_COLOR.equals(selEffList.get(0)
								.getName())) {
					// special case
					return (pos > 1);
				} else
					return (pos > 0);
			} else if (type == DOWN) {
				if (IMediaConstants.APPLY_GRAPHICS.equals(selEffList.get(0)
						.getName())
						|| IMediaConstants.APPLY_COLOR.equals(selEffList.get(0)
								.getName())) {
					// special case
					return (pos > 0 && pos < size - 1);
				} else {
					return (pos < size - 1);
				}
			}
		}
		return (false);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		if (type == UP) {
			return S60WorkspacePlugin.getImageDescriptor("icons/up.gif");
			// return ImageDescriptor.createFromFile(ActiveLayersPage.class,
			// "up.gif");
		}
		if (type == DOWN) {
			return S60WorkspacePlugin.getImageDescriptor("icons/down.gif");
			// return ImageDescriptor.createFromFile(ActiveLayersPage.class,
			// "down.gif");
		}
		return null;
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return null;
	}

}
