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
package com.nokia.tools.media.utils.editor.frameanimation;

import java.awt.datatransfer.Clipboard;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.clipboard.PasteHelper;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;

public class AnimationImageDropTarget implements DropTargetListener {
	private FrameAnimationContainer cont;

	private IAnimatedImage img;

	private Button dropTargetButton = null;

	private int operations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT;

	private DropTarget target = null;

	private Transfer[] types = new Transfer[] { LocalSelectionTransfer.getInstance(), FileTransfer.getInstance()};

	public AnimationImageDropTarget(FrameAnimationContainer cont, Button b) {
		this.cont = cont;
		this.img = cont.getImage();
		dropTargetButton = b;
		target = new DropTarget(b, operations);
		target.setTransfer(types);
		target.addDropListener(this);
	}

	public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_NONE;
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) 
			event.detail = DND.DROP_COPY;
		if (LocalSelectionTransfer.getInstance().isSupportedType(event.currentDataType)) {
			event.detail = DND.DROP_MOVE;
			if (LocalSelectionTransfer.getInstance().getSelection() instanceof IStructuredSelection) {
				IStructuredSelection s = (IStructuredSelection) LocalSelectionTransfer.getInstance().getSelection();
				Object file = s.getFirstElement();
				if (file instanceof IFile) {					
					event.detail = DND.DROP_COPY;
				}
			}
		}						
	}

	public void dragLeave(DropTargetEvent event) {
	}

	public void dragOperationChanged(DropTargetEvent event) {
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
			return (String[])data;			
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

	public void dragOver(DropTargetEvent event) {
		Point scStart = cont.sc.toDisplay(cont.sc.getClientArea().x, cont.sc
				.getClientArea().y);

		Point scEnd = cont.sc.toDisplay(cont.sc.getClientArea().x
				+ cont.sc.getClientArea().width - 1, cont.sc.getClientArea().y
				+ cont.sc.getClientArea().height - 1);

		if (event.x - 5 <= scStart.x) {
			Point origin = cont.sc.getOrigin();
			origin.x -= 5;
			origin.x = Math.max(0, origin.x);
			cont.sc.setOrigin(origin);
		} else if (event.x + 5 >= scEnd.x) {
			Point origin = cont.sc.getOrigin();
			origin.x += 5;
			origin.x = Math.min(cont.c.getSize().x - 1, origin.x);
			cont.sc.setOrigin(origin);
		}
		
		Object dropData = getDropData(event);		
		if (dropData instanceof String[]) {
			event.detail = DND.DROP_NONE;
			//FileTransfer			
			String paths[] = (String[]) dropData;
			if (PasteHelper.isParameterUsableAsImage(paths[0]) && paths.length <= 2) {
				event.detail = DND.DROP_COPY;
			}			
		}
	}

	public void drop(final DropTargetEvent event) {				
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {				
				
				boolean pasteOp = false;
				Object pasteParam = null;
				
				if (event.data instanceof IStructuredSelection) {
					IStructuredSelection s = (IStructuredSelection) event.data;
					Object file = s.getFirstElement();
					if (file instanceof IFile) {
						IFile f = (IFile) file;
						pasteOp = true;
						pasteParam = f.getLocation().toFile();		
						//System.out.println(pasteParam + " = " + ((File)pasteParam).exists());
						event.detail = DND.DROP_COPY;
					}
				}
				
				if (event.data instanceof String[]) {			
					pasteOp = true;
					pasteParam = Arrays.asList((String[])event.data);
					event.detail = DND.DROP_COPY;
				}							
				
				if (event.detail == DND.DROP_COPY && !pasteOp) {
					
					Button sourceAnimationbutton = (Button) ((IStructuredSelection) LocalSelectionTransfer
							.getInstance().getSelection()).getFirstElement();
					IAnimationFrame sourceImg = (IAnimationFrame) sourceAnimationbutton
							.getData(IAnimationFrame.class.getName());
					
					//perform copy					
					Clipboard clip = new Clipboard("");
					sourceImg.copyImageToClipboard(clip);
					pasteOp = true;
					pasteParam = ClipboardHelper.getClipboardContent(ClipboardHelper.CONTENT_TYPE_IMAGE_WITH_MASK, clip);

				}
				
				Command command = null;
				final Object pasteParamFinal = pasteParam;
				
				final IAnimationFrame targetImg = (IAnimationFrame) dropTargetButton
				.getData(IAnimationFrame.class.getName());
				
				if (pasteOp && pasteParam != null) {										
					command = new Command(com.nokia.tools.media.utils.editor.Messages
							.AnimationImageContainer_pasteImg) {						

						Object undoData;
						
						public boolean canExecute() {
							return true;
						};

						public boolean canUndo() {
							return true;
						};

						public void redo() {
							if (undoData == null) {
								Clipboard clip = new Clipboard("");
								targetImg.copyImageToClipboard(clip);
								undoData = ClipboardHelper.getClipboardContent(ClipboardHelper.CONTENT_TYPE_IMAGE_WITH_MASK, clip);
							}
							try {
								targetImg.paste(pasteParamFinal, null);
							} catch (Exception e) {
								
								e.printStackTrace();
							}
						};

						public void undo() {
							try {
								targetImg.paste(undoData, null);
							} catch (Exception e) {
								
								e.printStackTrace();
							}
						};

						public void execute() {
							redo();
						};
					};
				} else {
					
					Button sourceAnimationbutton = (Button) ((IStructuredSelection) LocalSelectionTransfer
							.getInstance().getSelection())
							.getFirstElement();
					final IAnimationFrame sourceImg = (IAnimationFrame) sourceAnimationbutton
							.getData(IAnimationFrame.class.getName());					
					if (sourceImg == targetImg)
						return;
					
					if (sourceImg != null && targetImg != null) {
				
					command = new Command(com.nokia.tools.media.utils.editor.Messages
							.AnimationImageContainer_moveFrame) {
						int oldSeqNo;

						public boolean canExecute() {
							return sourceImg != null;
						};

						public boolean canUndo() {
							return sourceImg != null;
						};

						public void redo() {
							oldSeqNo = sourceImg.getSeqNo();

							if (sourceImg.getSeqNo() < targetImg.getSeqNo()) {
								img.moveAnimationFrame(sourceImg, targetImg
										.getSeqNo() + 1);
							} else {
								img.moveAnimationFrame(sourceImg, targetImg
										.getSeqNo());
							}

							cont.notityPropertyChangedListeners("IMAGE_MOVED",
									null, sourceImg);

							cont.refresh(img);
							cont.redraw();
						};

						public void undo() {
							if (sourceImg.getSeqNo() < oldSeqNo) {
								img.moveAnimationFrame(sourceImg, oldSeqNo + 1);
							} else {
								img.moveAnimationFrame(sourceImg, oldSeqNo);
							}
						};

						public void execute() {
							redo();
						};
					};
				}
				}
				
				if (command != null)
					cont.getGraphicEditor().getCommandStack().execute(command);
			
			}
		});
	}

	public void dropAccept(DropTargetEvent event) {						
	}

}
