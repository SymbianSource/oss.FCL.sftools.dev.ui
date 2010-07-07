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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.theme.s60.ui.Activator;

/**
 */
public class DistributeAnimateTimeAction extends AbstractAction {
	private final static ImageDescriptor CASCADE_IMAGE_DESCRIPTOR = Activator
			.getImageDescriptor("icons/Cascade.png"); 

	public static final String HLP_CTX = "com.nokia.tools.s60.ide" + '.' + "distributetime_context"; 

	public static final String ID = "Distribute_Animate_Time"; 

	private long selectedTime = 0;

	public void setSelectedTime(long time) {
		selectedTime = time;
	}

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.DistributeAnimateTimeAction_name); 
		setLazyEnablementCalculation(true);
		setImageDescriptor(CASCADE_IMAGE_DESCRIPTOR);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				DistributeAnimateTimeAction.HLP_CTX);
	}

	public DistributeAnimateTimeAction(IWorkbenchPart part) {
		super(part);
	}

	public DistributeAnimateTimeAction(ISelectionProvider provider,
			CommandStack stack) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
	}

	@Override
	public void doRun(Object sel) {
		if (sel instanceof IAnimatedImage) {
			final IAnimatedImage curImage = (IAnimatedImage) sel;

			IInputValidator validator = new IInputValidator() {
				public String isValid(String newText) {
					long animTime = 0;
					try {
						animTime = Long.parseLong(newText);
					} catch (NumberFormatException ex) {
						return Messages.NotANumber;
					} catch (Exception ex) {
						return ex.getMessage();
					}
					if (animTime < IAnimationFrame.MINIMUM_ANIMATE_TIME) {
						return Messages.SetAnimateTimeAction_AnimateTimeMustBe
								+ IAnimationFrame.MINIMUM_ANIMATE_TIME;
					}
					return null;
				};
			};

			final IAnimationFrame[] frames = curImage.getAnimationFrames();
			String defaultTime = "" 
					+ curImage.getDefaultAnimateTime();

			if (selectedTime == 0) {
				InputDialog dialog = new InputDialog(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						Messages.DistributeAnimateTimeAction_name,
						Messages.SetAnimateTimeAction_EnterTime, ""
								+ defaultTime, validator);
				if (dialog.open() == Dialog.OK) {
					try {
						selectedTime = Long.parseLong(dialog.getValue());
					} catch (Exception e) {
					}
				}
			}

			if (selectedTime > 0) {
				try {
					final long newAnimateTime = selectedTime;
					execute(new Command() {
						long oldDefaultTime;

						Map<IAnimationFrame, Long> oldAnimateTimes = new HashMap<IAnimationFrame, Long>();

						@Override
						public boolean canExecute() {
							return true;
						}

						@Override
						public boolean canUndo() {
							return true;
						}

						@Override
						public void execute() {
							for (IAnimationFrame image : frames) {
								oldAnimateTimes.put(image, image
										.getAnimateTime());
							}
							oldDefaultTime = curImage.getDefaultAnimateTime();
							redo();
						}

						@Override
						public void undo() {
							curImage.setDefaultAnimateTime(oldDefaultTime);
							for (IAnimationFrame image : frames) {
								Long oldAnimateTime = oldAnimateTimes
										.get(image);
								if (oldAnimateTime != null) {
									image.setAnimateTime(oldAnimateTime);
								}
							}
						}

						@Override
						public void redo() {
							curImage.setDefaultAnimateTime(newAnimateTime);
							for (IAnimationFrame image : frames) {
								image.setAnimateTime(newAnimateTime);
							}
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see org.eclipse.gef.commands.Command#getLabel()
						 */
						public String getLabel() {
							return getText();
						}
					}, null);

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		if (sel instanceof IAnimatedImage) {
			return true;
		}

		return false;
	}
}
