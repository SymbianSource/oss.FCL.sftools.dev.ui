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
package com.nokia.tools.s60.editor.dnd;

import java.awt.Color;
import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.clipboard.FileTransferable;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor.ContentType;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.editor.actions.AddToGroupAction;
import com.nokia.tools.s60.editor.actions.CopyGraphicsAction;
import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.s60.editor.actions.PasteGraphicsAction;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.color.DraggedColorObject;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;

/**
 * Generic drop target listeners, capable accepting LocalSelectionTransfer with
 * ClipboardContentElement or file list content or FileTransfer.
 * 
 * It implements TransferDropTargetListener because of isage in GEF Viewer where
 * is needed. In other context, transfer object passed in constructor doesn't
 * affect anything.
 */
public abstract class S60BaseDropListener implements TransferDropTargetListener {

	private CommandStack stack;

	private Transfer transfer;

	public S60BaseDropListener(Transfer t, CommandStack stack) {
		this.stack = stack;
		this.transfer = t;
	}

	public Transfer getTransfer() {
		return transfer;
	}

	public boolean isEnabled(DropTargetEvent event) {
		return true;
	}

	/**
	 * returns element that is tarrget of drop operation
	 * 
	 * @param evt
	 * @return
	 */
	protected Object getSelectedElement(DropTargetEvent evt) {

		DropTarget dt = (DropTarget) evt.widget;
		if (dt.getControl() instanceof Tree) {
			TreeItem treeItem = null;
			treeItem = (TreeItem) evt.item;
			if (treeItem == null) {
				return null;
			} else {
				return treeItem.getData();
			}
		} else {
			ISelectionProvider provider = getSelectionProviderForGenericCommand(evt);
			if (provider != null) {
				return ((IStructuredSelection) provider.getSelection())
						.getFirstElement();
			}
		}
		return null;
	}

	/**
	 * Unify drop data to clipboard with FileTransferable content if possible,
	 * other null;
	 */
	public static Clipboard unifyDropData(Object dropData) {
		/* ifile comes from package explorer / resource explorer */
		if (dropData instanceof IFile) {
			IFile file = (IFile) dropData;
			dropData = file.getLocation().toFile().getAbsolutePath();
		}

		if (dropData instanceof String) {
			dropData = new String[] { (String) dropData };
		}

		if (dropData instanceof String[]) {
			String paths[] = (String[]) dropData;
			Clipboard clip = new Clipboard("");
			List<File> files = new ArrayList<File>();
			for (String path : paths) {
				if (ClipboardHelper.stringRepresentsFile(path)) {
					files.add(new File(path));
				}
			}
			FileTransferable ft = new FileTransferable(files);
			clip.setContents(ft, ft);
			dropData = clip;
		}
		return (Clipboard) (dropData instanceof Clipboard ? dropData : null);
	}

	private DraggedColorObject dCO = null;

	public void dragOver(DropTargetEvent event) {

		// set to none default, later enabled
		event.detail = DND.DROP_NONE;

		Object dropData = null;
		try {
			dropData = getDropData(event);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// for dropping colors
		if (dropData instanceof DraggedColorObject
				&& getSelectedElement(event) != null) {
			ISkinnableEntityAdapter adapter = getSkinnableEntityAdapter(event);
			if (adapter != null) {

				if (adapter.isColour()) {

					event.detail = DND.DROP_COPY;
					dCO = (DraggedColorObject) dropData;
					return;
				}
			}
		}// end for dropping colors

		if (dropData instanceof ClipboardContentDescriptor) {
			// disable for layers view
			if ((event.item != null)
					&& (event.item.getData() instanceof Object[])) {
				Object[] obj = (Object[]) event.item.getData();
				if (obj[1] instanceof ILayer) {
					return;
				}
			}
			event.detail = DND.DROP_COPY;
			highlightDragOver(((DropTarget) event.widget).getControl());
			return;
		}

		dropData = unifyDropData(dropData);

		if (dropData instanceof Clipboard) {
			// clip = drop data
			// dropCandidate = object where we are dropping
			Clipboard clip = (Clipboard) dropData;
			if (DNDUtil.draggedObjectCanBeDropped(getSelectedElement(event),
					clip)) {
				event.detail = DND.DROP_COPY;
				highlightDragOver(((DropTarget) event.widget).getControl());
			}
		}
	}

	public void drop(DropTargetEvent event) {

		ISkinnableEntityAdapter adapter = getSkinnableEntityAdapter(event);

		if (adapter != null) {

			Object dropData = getDropData(event);

			if (dropData instanceof DraggedColorObject) {
				ISelectionProvider provider = null;
				DraggedColorObject obj = (DraggedColorObject) dropData;
				// this if is because of multiselection in LayersView
				if (event.item instanceof TreeItem) {
					TreeItem trItem = (TreeItem) event.item;
					Object data = trItem.getData();
					if (data instanceof Object[]) {
						Object[] object = (Object[]) data;
						
						if (object[2] instanceof List) {
							final List<IContentData> datas = (List<IContentData>) object[2];
							provider = new ISelectionProvider() {
								public ISelection getSelection() {
									return new StructuredSelection(datas);
								}

								public void addSelectionChangedListener(
										ISelectionChangedListener listener) {
								}

								public void removeSelectionChangedListener(
										ISelectionChangedListener listener) {
								}

								public void setSelection(ISelection selection) {
								}
							};
						}
					}
				}
				if (null == provider) {
					provider = new SimpleSelectionProvider(adapter
							.getContentData());
				}
				IColorAdapter colorAdapter = (IColorAdapter) adapter
						.getContentData().getAdapter(IColorAdapter.class);
				if (colorAdapter != null) {
					Color oldColor = colorAdapter.getColor();

					AddToGroupAction action = new AddToGroupAction(
							new StructuredSelection(adapter.getContentData()),
							provider, null);

					
					action.setColor(ColorUtil.getRGB(ColorUtil
							.asHashString(oldColor)));
					action.setGroupName(obj.getName());
					action.setColorGroups(obj.getGrps());
					action.run();
				}
			} else if (dropData instanceof ClipboardContentDescriptor) {
				ClipboardContentDescriptor cDesc = (ClipboardContentDescriptor) dropData;
				if (cDesc.getType() == ContentType.CONTENT_ELEMENT) {
					IContentData source = JEMUtil.getContentData(cDesc
							.getContent());

					Clipboard clip = new Clipboard("");
					AbstractAction copy = new CopyGraphicsAction(
							new SimpleSelectionProvider(source), clip);
					if (copy.isEnabled())
						copy.run();
					
					//See if color depth is different. Dont paste layer info from source if they are different. In that case
					//only paste the image.
					ISkinnableEntityAdapter ska =  (ISkinnableEntityAdapter) source.getAdapter(ISkinnableEntityAdapter.class);	
					boolean isColorDepthDifferent = hasDifferentColorDepth(
							adapter, ska);												

					AbstractAction paste = new PasteGraphicsAction(
							getSelectionProviderForGenericCommand(event),
							stack, clip);
					if (paste.isEnabled() && !isColorDepthDifferent)
						paste.run();
					else {
						
						copy = new CopyImageAction(new SimpleSelectionProvider(
								source), clip);
						if (copy.isEnabled())
							copy.run();
						paste = new PasteImageAction(
								getSelectionProviderForGenericCommand(event),
								stack, clip);
						if (paste.isEnabled())
							paste.run();
					}
				}
			} else {

				dropData = unifyDropData(dropData);

				if (dropData instanceof Clipboard) {

					AbstractAction executeAction = null;

					// this is because of layers view - we need to distinguis
					// proper layer and layer mask or layer image node
					if ((event.item != null)
							&& (event.item.getData() instanceof Object[])) {
						Object[] obj = (Object[]) event.item.getData();

						if (obj[1] instanceof IImage
								&& obj.length > 3
								&& AbstractAction.TYPE_PART
										.equalsIgnoreCase((String) obj[3])) {
							// this node is "PART:[part label here]" node of
							// nine-piece part
							obj[1] = ((IImage) obj[1]).getLayer(0);
						}

						if (obj[1] instanceof ILayer) {
							executeAction = new PasteImageAction(
									new SimpleSelectionProvider(obj), stack,
									(Clipboard) dropData);
						}
					}

					if (executeAction == null)
						executeAction = new PasteImageAction(
								new SimpleSelectionProvider(adapter
										.getContentData()), stack,
								(Clipboard) dropData);

					try {
						executeAction.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		removeHighlight(((DropTarget) event.widget).getControl());
	}

	/**
	 * Checks if color depth information is same for source and target. 
	 * @param target
	 * @param source
	 * @return true if colordepth is different
	 */
	private boolean hasDifferentColorDepth(ISkinnableEntityAdapter target,
			ISkinnableEntityAdapter source) {						
		String sourceColorDepth = source.getColorDepth(0);
		String targetColorDepth = target.getColorDepth(0);					
		
		boolean isColorDepthDifferent = false;
		
		if (!sourceColorDepth.equals(targetColorDepth)) {
			//Color depth is different. PasteImageAction must be called.
			isColorDepthDifferent = true;
		}
		return isColorDepthDifferent;
	}

	public void dragEnter(DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT || event.detail == DND.DROP_MOVE) {
			if ((event.operations & DND.DROP_COPY) != 0
					|| (event.operations & DND.DROP_MOVE) != 0) {
				event.detail = DND.DROP_COPY;
			} else {
				event.detail = DND.DROP_NONE;
			}
		}
	}

	public static Object getDropData(DropTargetEvent evt) {
		Transfer t = null;

		if (FileTransfer.getInstance().isSupportedType(evt.currentDataType))
			t = FileTransfer.getInstance();
		else if (TextTransfer.getInstance()
				.isSupportedType(evt.currentDataType))
			t = TextTransfer.getInstance();
		else if (LocalSelectionTransfer.getInstance().isSupportedType(
				evt.currentDataType))
			t = LocalSelectionTransfer.getInstance();

		if (t.isSupportedType(evt.currentDataType)) {

			if (t instanceof LocalSelectionTransfer) {

				// this is just for dropping colors
				LocalSelectionTransfer tempT = (LocalSelectionTransfer) t;

				StructuredSelection selection = (StructuredSelection) tempT
						.getSelection();
				if (selection != null && !selection.isEmpty()) {
					Object data = selection.getFirstElement();
					if (data instanceof DraggedColorObject) {
						return data;
					}
				}
				// end for dropping color

				Object data = evt.data;
				if (data == null) {
					data = ((LocalSelectionTransfer) t)
							.nativeToJava(evt.currentDataType);
				}
				if (data != null) {
					Object imageData = ((IStructuredSelection) data)
							.getFirstElement();
					return imageData;
				}
			}

			if (t instanceof FileTransfer) {
				Object data = evt.data;
				if (data == null) {
					data = ((FileTransfer) t).nativeToJava(evt.currentDataType);
				}
				return (String[]) data;
			}

			if (t instanceof TextTransfer) {
				Object data = evt.data;
				if (data == null) {
					data = ((TextTransfer) t).nativeToJava(evt.currentDataType);
				}
				String text = (String) data;
				return text;
			}

		}

		return null;
	}

	public void dragOperationChanged(DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT) {
			if ((event.operations & DND.DROP_COPY) != 0
					|| (event.operations & DND.DROP_MOVE) != 0) {
				// ok
			} else {
				event.detail = DND.DROP_NONE;
			}
		}
	}

	public void dropAccept(DropTargetEvent event) {
	}

	public void dragLeave(DropTargetEvent event) {
		removeHighlight(((DropTarget) event.widget).getControl());
	}


	protected ISkinnableEntityAdapter getSkinnableEntityAdapter(
			DropTargetEvent event) {
		return null;
	}

	protected IWorkbenchPart getWorkbenchPart() {
		return null;
	}

	protected EditPart getSourceEditPart(DropTargetEvent event) {
		return null;
	}

	protected ISelectionProvider getSelectionProviderForGenericCommand(
			DropTargetEvent event) {
		return null;
	}

	protected void highlightDragOver(Control control) {
	}

	protected void removeHighlight(Control control) {
	}

}