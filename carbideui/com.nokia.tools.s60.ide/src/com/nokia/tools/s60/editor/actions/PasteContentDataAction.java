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

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import sun.awt.datatransfer.ClipboardTransferable;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardContentElement;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.s60.editor.commands.PasteImageCommand;
import com.nokia.tools.s60.editor.commands.ProgressMonitorWrapperCommand;
import com.nokia.tools.s60.editor.commands.Series60EditorCommand;
import com.nokia.tools.s60.editor.commands.SetThemeGraphicsCommand;
import com.nokia.tools.s60.editor.ui.dialogs.ExtendedPasteConfirmDialog;
import com.nokia.tools.s60.editor.ui.dialogs.FailureListDialog;
import com.nokia.tools.s60.editor.ui.dialogs.IFailure;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.internal.utils.PasteFolderUtils;
import com.nokia.tools.s60.internal.utils.PasteFolderUtils.PasteInfo;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.theme.content.ThemeUtil;
import com.nokia.tools.theme.core.MultiPieceManager;
import com.nokia.tools.theme.s60.S60SkinnableEntityAdapter;

/**
 * Pastes multiple elements from local clipboard with
 * ClipboardContentDescriptor.TYPE_ELEMENT_GROUP content to target theme. Does
 * not paste elements to selected items but to their 'semantic' places defined
 * by CB content. (IDs are also included).
 */
public class PasteContentDataAction extends AbstractAction {

	public static final String ID = ActionFactory.PASTE.getId() + "Element";

	private Clipboard clip = null;

	private boolean incompatibleContent = false;

	private boolean ask = true;

	private List<String> incompatibleContentList;

	private List<File> list = null;

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.PasteImageAction_name + " Element(s) ");
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/paste_edit.gif"));
		setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/paste_edit.gif"));
		setToolTipText(Messages.PasteImageAction_tooltip);
	}

	/**
	 * Constructor added due contributed actions functionality.
	 * 
	 * @param part
	 */
	public PasteContentDataAction(IWorkbenchPart part) {
		super(part);
	}

	public PasteContentDataAction(IWorkbenchPart part, CommandStack stack,
			Clipboard clip) {
		super(part);
		this.stack = stack;
		this.clip = clip;
	}

	public PasteContentDataAction(ISelectionProvider provider,
			CommandStack stack, Clipboard clip) {
		super(null);
		setSelectionProvider(provider);
		this.clip = clip;
		this.stack = stack;
	}

	@Override
	public void doRun(Object sel) {
		IContentData data = getContentData(sel);

		if (data == null) {
			IWorkbenchPart editor = null;
			if (getWorkbenchPart() != null) {
				editor = getWorkbenchPart();
			} else {
				editor = getActiveEditorPart();
			}
			if (editor instanceof IContentAdapter) {
				IContentAdapter adapter = (IContentAdapter) editor;
				IContent[] contents = adapter.getContents();
				data = ScreenUtil.getPrimaryContent(contents);
			}
		}

		final IContent root = data.getRoot();
		if (data != null) {
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);
			incompatibleContentList = new ArrayList<String>();
			if (skAdapter != null) {
				if (ClipboardHelper.clipboardContainsFolderWithImagesData(clip)
						|| ClipboardHelper.clipboardContainsMultipleImagesData(
								clip, null)) {
					List<ClipboardContentElement> _list = null;
					PasteInfo pasteInfo = null;
					if (ClipboardHelper
							.clipboardContainsFolderWithImagesData(clip)) {
						File[] files = null;
						try {
							if (Toolkit.getDefaultToolkit()
									.getSystemClipboard()
									.isDataFlavorAvailable(
											DataFlavor.javaFileListFlavor)) {
								List list = (List) Toolkit.getDefaultToolkit()
										.getSystemClipboard().getData(
												DataFlavor.javaFileListFlavor);
								files = (File[]) list.toArray(new File[0]);
							} else if (Toolkit.getDefaultToolkit()
									.getSystemClipboard()
									.isDataFlavorAvailable(
											DataFlavor.stringFlavor)) {
								String file = (String) Toolkit
										.getDefaultToolkit()
										.getSystemClipboard().getData(
												DataFlavor.stringFlavor);
								files = new File[] { new File(file) };
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (files != null) {
							if (skAdapter instanceof S60SkinnableEntityAdapter) {
								try {
									pasteInfo = PasteFolderUtils
											.createPasteInfo(root, files, data);
								} catch (Exception e) {
							
									e.printStackTrace();
								}
							}
							
							// "Paste image(s) into Theme"
							else {
								
								Object idOfElement = null;
								list = new ArrayList<File>();
								
								Transferable transfer = clip.getContents(null);
								try {
									if(!(transfer instanceof ClipboardTransferable))
									idOfElement = transfer
											.getTransferData(DataFlavor.imageFlavor);
									list = (List<File>) transfer
											.getTransferData(DataFlavor.javaFileListFlavor);
								} catch (UnsupportedFlavorException e1) {
									
									e1.printStackTrace();
								} catch (IOException e1) {
									
									e1.printStackTrace();
								}

								if (idOfElement == null)
								{
									try {
										pasteInfo = PasteFolderUtils
												.createPasteInfo(root, files,
														data);
									} catch (Exception e) {
										
										e.printStackTrace();
									}
								}

							}

							_list = new ArrayList<ClipboardContentElement>();
							for (Map.Entry<IContentData, List<File>> entry : pasteInfo
									.getResolvedFiles().entrySet()) {
								if (entry.getValue().size() > 0) {
									ClipboardContentElement cce = PasteFolderUtils
											.createContentElement(entry
													.getKey(), entry.getValue()
													.toArray(new File[0]));
									if (cce != null) {
										_list.add(cce);
									}
								}
							}
						}
					} else {
						List<File> files = (List<File>) ClipboardHelper
								.getClipboardContent(
										ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE
												| ClipboardHelper.CONTENT_TYPE_MULTIPLE_IMAGES,
										clip);
						pasteInfo = PasteFolderUtils.createPasteInfo(root,
								files.toArray(new File[0]), null);
						_list = new ArrayList<ClipboardContentElement>();
						for (Map.Entry<IContentData, List<File>> entry : pasteInfo
								.getResolvedFiles().entrySet()) {

							if (entry.getValue().size() > 0) {
								ClipboardContentElement cce = PasteFolderUtils
										.createContentElement(entry.getKey(),
												entry.getValue().toArray(
														new File[0]));
								if (cce != null) {
									_list.add(cce);
								}
							}
						}

					}

					if (_list != null && pasteInfo != null) {
						try {
							processClipboardContentElements(root, _list,
									pasteInfo);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return;
					}
				}

				for (DataFlavor fl : clip.getAvailableDataFlavors()) {
					if (fl.getMimeType().startsWith(
							DataFlavor.javaJVMLocalObjectMimeType)) {
						try {
							Object content = clip.getData(fl);

							if (content instanceof ClipboardContentDescriptor) {
								ClipboardContentDescriptor cdd = (ClipboardContentDescriptor) content;
								List<ClipboardContentElement> _list = (List<ClipboardContentElement>) cdd
										.getContent();

								processClipboardContentElements(root, _list,
										null);

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						return;
					}
				}
			}
		}
	}

	protected void processClipboardContentElements(final IContent root,
			List<ClipboardContentElement> _list, PasteInfo pasteInfo) {
		List<ClipboardContentElement> _list2 = new ArrayList<ClipboardContentElement>();
		for (ClipboardContentElement c : _list) {
			if (c.getChildCount() > 0)
				for (int i = 0; i < c.getChildCount(); i++)
					_list2.add(c.getChild(i));
			else
				_list2.add(c);
		}

		if (ask) {
			ExtendedPasteConfirmDialog dlg = new ExtendedPasteConfirmDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), _list2, root);
			if (pasteInfo != null) {
				dlg.setShowFiles(true);
				List<File> unresolvedFiles = pasteInfo.getUnresolvedFiles();
				if (unresolvedFiles.size() > 0) {
					Collections.sort(unresolvedFiles);
					dlg.setDisplayError(true);
					String errorTitle = "" + unresolvedFiles.size()
							+ " UNRESOLVED FILE"
							+ (unresolvedFiles.size() == 1 ? "" : "S") + "!";

					dlg.setErrorTitle(errorTitle);
					StringBuffer message = new StringBuffer();
					for (int i = 0; i < unresolvedFiles.size(); i++) {
						message.append(unresolvedFiles.get(i).getAbsolutePath()
								+ "\n");
					}
					dlg.setErrorMessage(message.toString());
				}
			}
			if (dlg.open() == Window.OK)
				_list = dlg.getSelectedContentData();
			else
				return;
		}
		if (_list.size() == 0)
			return;
		final CompoundCommand commandList = new CompoundCommand(
				"Paste Element(s)");

		// expand clipboard content
		Stack<ClipboardContentElement> stack = new Stack<ClipboardContentElement>();
		stack.addAll(_list);
		_list.clear();
		while (!stack.isEmpty()) {
			ClipboardContentElement el = stack.pop();
			if (el.getContent() != null)
				_list.add(el);
			if (el.getChildCount() > 0)
				for (int i = 0; i < el.getChildCount(); i++)
					stack.push(el.getChild(i));
		}

		final List<ClipboardContentElement> list = _list;

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
					new IRunnableWithProgress() {
						/**
						 * This method finds if the memory has known types of multipiece data.
						 * @return
						 */
						private boolean isKnownType(ClipboardContentElement cData) {
							boolean isKnownType = false;
							if ((cData != null) && (cData.getMetadata(2) instanceof String))
								isKnownType = MultiPieceManager.isKnownCopyElementType((String)cData.getMetadata(2));
							return isKnownType;
						}
						
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							monitor.beginTask("Pasting", list.size() * 11);
							// user had selected
							// these to copy
							// into current
							// theme
							for (ClipboardContentElement cData : list) {
								if (monitor.isCanceled()) {
									break;
								}

								monitor.worked(1);

								if (Display.getCurrent() != null) {
									while (Display.getCurrent()
											.readAndDispatch())
										;
								}
								IContentData ourCData = root
										.findById((String) cData.getMetadata());
								if (ourCData != null) {
									
									monitor.subTask("Checking "
											+ ourCData.getName() + "...");
									ISkinnableEntityAdapter skOur = (ISkinnableEntityAdapter) ourCData
											.getAdapter(ISkinnableEntityAdapter.class);
									if (skOur != null
											&& cData.getContent() != null) {
										Object elementContent = cData
												.getContent();
										// initialize the widget
										EditObject widget = (EditObject) ourCData
												.getAdapter(EditObject.class);
										
										if (skOur.isColour()) {
											
											IColorAdapter icla = (IColorAdapter) skOur
													.getAdapter(IColorAdapter.class);
											Color color = icla
													.getColourFromGraphics(elementContent);
											Command cc = icla
													.getApplyColorCommand(
															color, true);

											/*
											 * create command over widget
											 */
											commandList
													.add(new ProgressMonitorWrapperCommand(
															cc,
															monitor,
															"Pasting "
																	+ ourCData
																			.getName()
																	+ "...", 10));
										} else if (isKnownType(cData)) {
/*												IMediaConstants.NINE_PIECE_COPY_INFO
												.equals(cData.getMetadata(2))||IMediaConstants.THREE_PIECE_COPY_INFO
													.equals(cData.getMetadata(2))) {
*/											if (skOur.supportsMultiPiece()) {
												ThemeUtil
														.markAsChanged(elementContent);
												PasteImageCommand cmd = new PasteImageCommand(
														ourCData, null,
														elementContent);
												cmd
														.setCollectFailedCommands(true);
												cmd.setShowErrorMessages(false);

												commandList
														.add(new ProgressMonitorWrapperCommand(
																cmd,
																monitor,
																"Pasting "
																		+ ourCData
																				.getName()
																		+ "...",
																10));
												Command stretchModeCommand = skOur
														.getApplyStretchModeCommand(skOur
																.getStretchMode());
												if (stretchModeCommand
														.canExecute()) {
													commandList
															.add(stretchModeCommand);
												}
											}
										} else {
											IPasteTargetAdapter pTarget = getPasteTargetAdapter(ourCData);
											if (pTarget != null) {
												if (!pTarget.isPasteAvailable(
														cData, null)) {
													if (pTarget
															.isPasteAvailable(
																	elementContent,
																	null)) {

														PasteImageCommand cmd = new PasteImageCommand(
																ourCData, null,
																elementContent);

														cmd
																.setCollectFailedCommands(true);
														cmd
																.setShowErrorMessages(false);

														commandList
																.add(new ProgressMonitorWrapperCommand(
																		cmd,
																		monitor,
																		"Pasting "
																				+ ourCData
																						.getName()
																				+ "...",
																		10));
														if (skOur
																.getStretchMode() != null)
															commandList
																	.add(skOur
																			.getApplyStretchModeCommand(skOur
																					.getStretchMode()));
														continue;

													} else {

														incompatibleContent = true;
														incompatibleContentList
																.add((String) cData
																		.getMetadata(1));
														continue;
													}
												}
											}

											elementContent = skOur
													.getClone(elementContent);
											ThemeUtil
													.markAsChanged(elementContent);
											SetThemeGraphicsCommand cmd = new SetThemeGraphicsCommand(
													ourCData,
													skOur.getThemeGraphics(),
													elementContent, null);
											cmd.setCollectFailedCommands(true);
											cmd.setShowErrorMessages(false);

											commandList
													.add(new ProgressMonitorWrapperCommand(
															cmd,
															monitor,
															"Pasting "
																	+ ourCData
																			.getName()
																	+ "...", 10));
										}
									}
								}
							}

							/*
							 * this must be executed in ui thread
							 */
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									try {
										execute(commandList, null);
										List<IFailure> failedCommands = new ArrayList<IFailure>();
										if (incompatibleContent) {
											for (String incompatibleId : incompatibleContentList) {
												final String id = incompatibleId;
												IFailure f = new IFailure() {
													public String getDetail() {
														return Messages.PasteContentDataAction_incompatibleContentDetail;
													}

													public String getMessage() {
														return Messages.PasteContentDataAction_incompatibleContent;
													}

													public ESeverity getSeverity() {
														return ESeverity.WARN;
													}

													public String getSource() {
														return id;
													}
												};
												failedCommands.add(0, f);
											}
										}
										failedCommands
												.addAll(Series60EditorCommand
														.getFailedCommands());
										if (failedCommands.size() > 0) {
											// show failures
											FailureListDialog fd = new FailureListDialog(
													Display.getCurrent()
															.getActiveShell(),
													Messages.PasteContentDataAction_pasteFailed);
											fd.setFailures(failedCommands);
											fd.open();
										}
									} finally {
										Series60EditorCommand
												.clearFailedCommands();

									}
								}
							});

						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {

		if (clip == null)
			clip = ClipboardHelper.APPLICATION_CLIPBOARD;

		if (ClipboardHelper.clipboardContainsFolderWithImagesData(clip)) {
			setText(Messages.PasteImageAction_name + " Image(s) into Theme");
			return true;
		}

		if (ClipboardHelper.clipboardContainsMultipleImagesData(clip, null)) {
			setText(Messages.PasteImageAction_name + " Image(s) into Theme");
			return true;
		}

		setText(Messages.PasteImageAction_name + " Element(s) into Theme");

		IContentData data = getContentData(sel);

		if (data == null) {
			IWorkbenchPart editor = null;
			if (getWorkbenchPart() != null) {
				editor = getWorkbenchPart();
			} else {
				editor = getActiveEditorPart();
			}
			if (editor instanceof IContentAdapter) {
				IContentAdapter adapter = (IContentAdapter) editor;
				IContent[] contents = adapter.getContents();
				data = ScreenUtil.getPrimaryContent(contents);
			}
		}

		if (data != null) {

			
			// ClipboardContentDescriptor
			for (DataFlavor fl : clip.getAvailableDataFlavors()) {
				if (fl.getMimeType().startsWith(
						DataFlavor.javaJVMLocalObjectMimeType)) {
					try {
						Object content = clip.getData(fl);
						if (content instanceof ClipboardContentDescriptor) {
							ClipboardContentDescriptor cdd = (ClipboardContentDescriptor) content;
							if (cdd.getType() == ClipboardContentDescriptor.ContentType.CONTENT_ELEMENT
									|| cdd.getType() == ClipboardContentDescriptor.ContentType.CONTENT_ELEMENT_GROUP) {
								return true;
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return false;
	}

	public void setAsk(boolean b) {
		this.ask = b;
	}
}
