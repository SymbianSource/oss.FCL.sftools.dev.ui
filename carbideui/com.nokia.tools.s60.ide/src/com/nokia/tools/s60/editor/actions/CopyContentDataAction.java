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
package com.nokia.tools.s60.editor.actions;

import java.awt.datatransfer.Clipboard;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.internal.ui.palette.editparts.ToolEntryEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardContentElement;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.clipboard.JavaObjectTransferable;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.core.MultiPieceManager;


/**
 * Iplementation of Extended Copy -> Copy Element(s). Copies element content,
 * also with information where it should be placed in target theme.
 * 
 * Copies one or more nodes (IContentData) to local clipboard.
 */
public class CopyContentDataAction extends AbstractAction implements IRunnableWithProgress {

	public static final String ID = ActionFactory.COPY.getId() + "Elements";

	private Clipboard clip = null;

	private boolean syncExec;
	private boolean notEmpty;
	private boolean copySkinnedOnly = true;

	@Override
	protected void init() {
		setId(ID);
		setText("Skinned only"); 
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/copy_edit.gif"));
		setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/copy_edit.gif"));
		setToolTipText(Messages.CopyImageAction_tooltip);

		setLazyEnablementCalculation(true);
		super.init();
	}

	/**
	 * Constructor added due contributed actions functionality.
	 * 
	 * @param part
	 */
	public CopyContentDataAction(IWorkbenchPart part) {
		super(part);
	}
	
	public CopyContentDataAction(ISelectionProvider provider, Clipboard clip) {
		super(null);
		setSelectionProvider(provider);
		this.clip = clip;
	}
	
	private Object _data[] = null;
	
	private void dowork(final Object _data[]) {
		final List<ClipboardContentElement> forCopy = new ArrayList<ClipboardContentElement>();		
		for (Object _obj: _data) {
			IContentData d = getContentData(_obj);
			if (d != null) {
				ClipboardContentElement cbEl = createContentElement(d);
				forCopy.add(cbEl);
				// create childs for this content data and add them as childs of
				// cbEl
				createChilds(d, cbEl);
			}
		}			
		if (notEmpty) {
			
			// remove items with only 1 child - replace with it's child
			boolean fSingleChild = true;
			while (fSingleChild) {
				fSingleChild = false;
				for (int i = 0;i<forCopy.size();i++)  {
					ClipboardContentElement e = forCopy.get(i);
					if (e.getChildCount() == 1) {
						fSingleChild = true;
						forCopy.set(i, e.getChild(0));
					}
				}
			}
			
			ClipboardContentDescriptor descriptor = new ClipboardContentDescriptor(forCopy, ClipboardContentDescriptor.ContentType.CONTENT_ELEMENT_GROUP);
			try {
				clip.setContents(new JavaObjectTransferable(descriptor), ClipboardContentDescriptor.DummyClipOwner);
			} catch (Exception e) {
				e.printStackTrace();
			}	
		} else {
			
		}
	}
	
	private boolean shouldCopy(IContentData d) {
		if (d == null)
			return false;
			
		if (copySkinnedOnly) {
			boolean mod = "true".equalsIgnoreCase((String) d.getAttribute(ContentAttribute.MODIFIED.name()));
			return mod;
		} else {
			return true;
		}
	}

	@Override
	public void run() {
		
		if (clip == null)
			clip = ClipboardHelper.APPLICATION_CLIPBOARD;

		ISelection sel = getSelection();
		if (sel instanceof IStructuredSelection) {

			final Object _data[] = ((IStructuredSelection)sel).toArray();
			
			if (syncExec) {
				dowork(_data);
			} else {
				try {
					this._data = _data;
					PlatformUI.getWorkbench().getProgressService().busyCursorWhile(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}					
		}				
	}
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		dowork(_data);
	}
	
	private void createChilds(IContentData d, ClipboardContentElement cbEl) {		
		
		if (shouldCopy(d)) {
			if (d.getChildren() == null)
				return;
			for (IContentData cd: d.getChildren()) {							
				
				if (shouldCopy(cd)) {
					cbEl.addChild(createContentElement(cd));
					createChilds(cd, cbEl.getChild(cbEl.getChildCount() - 1));
				}
			}
		}
	}

	private ClipboardContentElement createContentElement(IContentData elData) {
		if (elData 
				!= null) {
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) elData
				.getAdapter(ISkinnableEntityAdapter.class);
			if (skAdapter != null) {
				try {
					Object themeGraphic = null;
					INamingAdapter nma = (INamingAdapter) skAdapter.getAdapter(INamingAdapter.class);
					String elName = nma == null ? elData.getName() : nma.getName();
					IImageAdapter imgAdapter = (IImageAdapter) skAdapter.getAdapter(IImageAdapter.class);
					
					if (!skAdapter.isMultiPiece()) {	
						if (skAdapter.isColour())
							
							themeGraphic = skAdapter.getThemeGraphics();
						else
							themeGraphic = skAdapter.getThemeGraphics(true);
						
						if (themeGraphic == null) {
							// intermediate node - create dummy node
							return new ClipboardContentElement(null, 1, elData.getId(), getGroupName(elData));
						}
						themeGraphic = skAdapter.getClone(themeGraphic);						
						ClipboardContentElement cbElement = null;
						
						String caption = getGroupName(elData.getParent()) + "\\" + elName;
						
						if (imgAdapter != null) {
							// must add into content element metadata about
							// supported animations and timings
							// target theme might not support it for given
							// element
							IImage img = imgAdapter.getImage(true);
							cbElement = new ClipboardContentElement(themeGraphic, 1, elData.getId(), caption, img.isAnimated(), img.isAnimatedFor(TimingModel.RealTime), img.isAnimatedFor(TimingModel.Relative));
						} else {
							cbElement = new ClipboardContentElement(themeGraphic, 1, elData.getId(), caption);
						}
						notEmpty = true;
						return cbElement;
					} else {
// element is nine piece
						try {
							List<IImage> parts = imgAdapter.getImage(true).getPartInstances();
							List<Object> themeGraphics = new ArrayList<Object>();					
							for (IImage p: parts) {
								themeGraphics.add(skAdapter.getEditedThemeGraphics(p, true));
							}
							notEmpty = true;
							int partCount = 0;
							if (parts != null) partCount = parts.size();
							return new ClipboardContentElement(themeGraphics, 1, elData.getId(), elName, 
									MultiPieceManager.getCopyElementInfo(partCount));
									
						} catch (Exception e) {
							e.printStackTrace();
						}
					}										
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}					
		}
		return null;
	}

	/**
	 * returns full 'path' name to given group/category.
	 * 
	 * @param elData
	 * @return
	 */
	private Object getGroupName(IContentData elData) {
		StringBuffer result = new StringBuffer();
		String origName = elData.getName();
		while (elData.getParent() != null) {
			result.insert(0, elData.getName());
			if (elData.getParent().getParent() != null)
				result.insert(0, "\\");
			elData = elData.getParent();
			if (elData.getName().equals(origName))
				elData = elData.getParent();
		}
		return result.toString();
	}

	@Override
	protected boolean calculateEnabled() {

		ISelection sel = getSelection();		
		if (sel instanceof IStructuredSelection) {
			
			/* code for palette viewer */
			Object first = ((IStructuredSelection)sel).getFirstElement();
			if (first instanceof ToolEntryEditPart) {
				Object model = ((ToolEntryEditPart)first).getModel();
				if (model instanceof CombinedTemplateCreationEntry) {
					Object template = ((CombinedTemplateCreationEntry)model).getTemplate();
					if (template instanceof IContentData) {
						if (shouldCopy((IContentData) template))
							return true;
					}
				}
			}			
			Object data[] = ((IStructuredSelection)sel).toArray();
			for (Object element: data) {
				IContentData elData = getContentData(element);
				if (elData != null) {
					if (elData.getChildren() != null && elData.getChildren().length > 0) {
						if (shouldCopy(elData))
							return true;
					} else {
						ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) elData
							.getAdapter(ISkinnableEntityAdapter.class);					
						if (skAdapter != null && skAdapter.isCopyAllowed()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	protected void doRun(Object element) {}
	
	public void setSyncExec(boolean b) {
		this.syncExec = b;
	}
	
	/**
	 * when true (default), only skinned elements in category will be copied, if
	 * source data contains category
	 * 
	 * @param skinnedOnly
	 */
	public void setCopySkinnedOnly(boolean skinnedOnly) {
		this.copySkinnedOnly = skinnedOnly;
	}
	
	
}
