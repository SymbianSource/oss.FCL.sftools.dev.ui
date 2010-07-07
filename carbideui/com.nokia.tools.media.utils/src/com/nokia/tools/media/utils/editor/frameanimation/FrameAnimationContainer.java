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

import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.ImageAdapter;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.editor.AbstractMediaEditorPart;
import com.nokia.tools.media.utils.editor.Messages;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.timeline.ISelectionListener;
import com.nokia.tools.media.utils.timeline.ITimeLine;
import com.nokia.tools.media.utils.timeline.ITimeLineNode;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.media.utils.timeline.cp.IControlPointListener;
import com.nokia.tools.media.utils.timeline.impl.ExecutionThread;
import com.nokia.tools.media.utils.timeline.impl.TimeLine;
import com.nokia.tools.media.utils.timeline.impl.TimeLineNode;
import com.nokia.tools.media.utils.timeline.impl.TimeLineRow;

public class FrameAnimationContainer extends Composite implements
		ISelectionListener, IControlPointListener, PropertyChangeListener,
		CommandStackListener {

	public static final String ACTION_ID_EDIT_SVG_IMAGE = "EditSVGImage"; //$NON-NLS-1$

	public static final String ACTION_ID_EDIT_BITMAP_IMAGE = "EditBitmapImage"; //$NON-NLS-1$

	private final static Image NEW_IMAGE = UtilsPlugin.getImageDescriptor(
			"icons/New.png").createImage(); //$NON-NLS-1$

	private final static Image DELETE_IMAGE = UtilsPlugin.getImageDescriptor(
			"icons/Remove.png").createImage(); //$NON-NLS-1$

	private final static Image MOVE_TO_BEGIN_IMAGE = UtilsPlugin
			.getImageDescriptor("icons/Previous.png").createImage(); //$NON-NLS-1$

	private final static Image MOVE_TO_LEFT_IMAGE = UtilsPlugin
			.getImageDescriptor("icons/AXP_left.png").createImage(); //$NON-NLS-1$

	private final static Image MOVE_TO_RIGHT_IMAGE = UtilsPlugin
			.getImageDescriptor("icons/AXP_Right.png").createImage(); //$NON-NLS-1$

	private final static Image MOVE_TO_END_IMAGE = UtilsPlugin
			.getImageDescriptor("icons/Next.png").createImage(); //$NON-NLS-1$

	private final static Image CASCADE_IMAGE = UtilsPlugin.getImageDescriptor(
			"icons/Cascade.png").createImage(); //$NON-NLS-1$

	private final static String IMAGE_ADDED = "IMAGE_ADDED"; //$NON-NLS-1$

	private final static String IMAGE_MOVED = "IMAGE_MOVED"; //$NON-NLS-1$

	private final static String IMAGE_CHANGED = "IMAGE_CHANGED"; //$NON-NLS-1$

	private final static String IMAGE_REMOVED = "IMAGE_REMOVED"; //$NON-NLS-1$

	ScrolledComposite sc = null;

	Composite c = null;

	private Button undoButton = null;

	private Button redoButton = null;

	private Button addButton = null;

	private Button removeButton = null;

	private Button moveToBegin = null;

	private Button moveToLeft = null;

	private Button moveToEnd = null;

	private Button moveToRight = null;

	private List<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();

	/*
	 * selected Animation Frame
	 */
	private IAnimationFrame selectedAnimationFrame = null;

	private Button selectedAnimationFrameButton = null;

	private IAnimatedImage curImage;

	private ITimeLine timeLine;

	private ITimeLineNode curNode;

	private IMenuListener timeLineMenuListener = null;

	private AbstractMediaEditorPart graphicsEditor;

	public FrameAnimActionFactory getActionFactory() {
		return actionFactory;
	}

	public void setActionFactory(FrameAnimActionFactory factory) {
		actionFactory = factory;
	}

	private FrameAnimActionFactory actionFactory = null;

	public FrameAnimationContainer(AbstractMediaEditorPart graphicsEditor,
			Composite parent, int style) {
		super(parent, style);

		this.graphicsEditor = graphicsEditor;

		setLayoutData(new GridData(GridData.FILL_BOTH));

		setLayout(new GridLayout(1, false));

		// create SCROLLING COMPOSITE
		// set its layout
		sc = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// sc.setSize(400, 400);
		sc.setLayout(new GridLayout());

		// create scrolled composite'contents which will be scrolled
		c = new Composite(sc, SWT.NONE);
		RowLayout rl = new RowLayout();
		rl.wrap = false;
		c.setLayout(rl);

		// SET CONTENTS WHICH WILL BE SCROLLED BY SCROLLED COMPOSITE
		sc.setContent(c);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite bC = new Composite(this, SWT.NONE);
		RowLayout bcRl = new RowLayout(SWT.HORIZONTAL);
		bcRl.wrap = false;
		bC.setLayout(bcRl);

		undoButton = new Button(bC, SWT.FLAT);
		undoButton.setEnabled(false);
		undoButton.setToolTipText(graphicsEditor.getActionRegistry().getAction(
				ActionFactory.UNDO.getId()).getToolTipText());
		undoButton.setImage(graphicsEditor.getActionRegistry().getAction(
				ActionFactory.UNDO.getId()).getDisabledImageDescriptor()
				.createImage());
		// listener for adding new image to the animation list
		undoButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				UndoAction undoAction = (UndoAction) FrameAnimationContainer.this.graphicsEditor
						.getActionRegistry().getAction(
								ActionFactory.UNDO.getId());
				if (undoAction.isEnabled()) {
					undoAction.run();
				}
			}
		});

		redoButton = new Button(bC, SWT.FLAT);
		redoButton.setEnabled(false);
		redoButton.setToolTipText(graphicsEditor.getActionRegistry().getAction(
				ActionFactory.REDO.getId()).getToolTipText());
		redoButton.setImage(graphicsEditor.getActionRegistry().getAction(
				ActionFactory.REDO.getId()).getDisabledImageDescriptor()
				.createImage());
		// listener for redo operation
		redoButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				RedoAction redoAction = (RedoAction) FrameAnimationContainer.this.graphicsEditor
						.getActionRegistry().getAction(
								ActionFactory.REDO.getId());
				if (redoAction.isEnabled()) {
					redoAction.run();
				}
			}
		});

		Label separator = new Label(bC, SWT.SEPARATOR);

		addButton = new Button(bC, SWT.FLAT);
		addButton.setToolTipText(Messages.AddFrameToolTip);
		addButton.setImage(NEW_IMAGE);
		// listener for adding new image to the animation list
		addButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {

				getDisplay().asyncExec(new Runnable() {

					public void run() {
						ISelectionProvider provider = new ISelectionProvider() {
							public void addSelectionChangedListener(
									ISelectionChangedListener listener) {
							};

							public void removeSelectionChangedListener(
									ISelectionChangedListener listener) {
							};

							public ISelection getSelection() {
								return new StructuredSelection(curImage);
							};

							public void setSelection(ISelection selection) {
							};
						};

						Action newFrameAction = getActionFactory()
								.getNewFrameAction(provider);

						if (newFrameAction.isEnabled()) {
							newFrameAction.run();
						}
					}
				});
			}
		});

		removeButton = new Button(bC, SWT.FLAT);
		removeButton.setToolTipText(Messages.DeleteFrameToolTip);
		removeButton.setImage(DELETE_IMAGE);
		// listener for image delete operation
		removeButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// test if selected image has
				// remove selected image from animation'images'list
				if (selectedAnimationFrameButton != null) {
					final IAnimationFrame sourceImg = (IAnimationFrame) selectedAnimationFrameButton
							.getData(IAnimationFrame.class.getName());

					ISelectionProvider provider = new ISelectionProvider() {
						public ISelection getSelection() {
							return new StructuredSelection(sourceImg);
						}

						public void setSelection(ISelection selection) {
						}

						public void addSelectionChangedListener(
								ISelectionChangedListener listener) {
						}

						public void removeSelectionChangedListener(
								ISelectionChangedListener listener) {
						}
					};

					Action removeFrameAction = getActionFactory()
							.getRemoveFrameAction(provider);

					if (removeFrameAction.isEnabled()) {
						removeFrameAction.run();
					}
				}
			}
		});

		separator.setLayoutData(new RowData(SWT.DEFAULT, removeButton
				.computeSize(SWT.DEFAULT, SWT.DEFAULT).y));

		separator = new Label(bC, SWT.SEPARATOR);
		separator.setLayoutData(new RowData(SWT.DEFAULT, removeButton
				.computeSize(SWT.DEFAULT, SWT.DEFAULT).y));

		moveToBegin = new Button(bC, SWT.FLAT);
		moveToBegin.setToolTipText(Messages.MoveFrameToBeginToolTip);
		moveToBegin.setImage(MOVE_TO_BEGIN_IMAGE);
		// listener for image delete operation
		moveToBegin.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// test if selected image has
				// remove selected image from animation'images'list
				if (selectedAnimationFrameButton != null) {

					final IAnimationFrame sourceImg = (IAnimationFrame) selectedAnimationFrameButton
							.getData(IAnimationFrame.class.getName());

					Command command = new Command(
							Messages.AnimationImageContainer_moveFrameToBegin) {
						int oldSeqNo;

						public boolean canExecute() {
							return curImage != null && sourceImg != null;
						};

						public boolean canUndo() {
							return true;
						};

						public void redo() {
							oldSeqNo = sourceImg.getSeqNo();
							if (curImage != null && oldSeqNo > 0) {
								curImage.moveAnimationFrame(sourceImg, 0);
								notityPropertyChangedListeners(IMAGE_MOVED,
										null, sourceImg);
							}
						};

						public void undo() {
							if (curImage != null) {
								if (sourceImg.getSeqNo() < oldSeqNo) {
									curImage.moveAnimationFrame(sourceImg,
											oldSeqNo + 1);
								} else {
									curImage.moveAnimationFrame(sourceImg,
											oldSeqNo);
								}
								notityPropertyChangedListeners(IMAGE_MOVED,
										null, sourceImg);
							}
						};

						public void execute() {
							redo();
						};
					};

					FrameAnimationContainer.this.graphicsEditor
							.getCommandStack().execute(command);
				}
			}
		});

		moveToLeft = new Button(bC, SWT.FLAT);
		moveToLeft.setToolTipText(Messages.MoveFrameToLeftToolTip);
		moveToLeft.setImage(MOVE_TO_LEFT_IMAGE);
		// listener for image delete operation
		moveToLeft.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// test if selected image has
				// remove selected image from animation'images'list
				if (selectedAnimationFrameButton != null) {

					final IAnimationFrame sourceImg = (IAnimationFrame) selectedAnimationFrameButton
							.getData(IAnimationFrame.class.getName());

					Command command = new Command(
							Messages.AnimationImageContainer_moveFrameToLeft) {
						boolean moved;

						public boolean canExecute() {
							return curImage != null && sourceImg != null;
						};

						public boolean canUndo() {
							return moved;
						};

						public void redo() {
							if (curImage != null && sourceImg.getSeqNo() > 0) {
								curImage.moveAnimationFrame(sourceImg,
										sourceImg.getSeqNo() - 1);
								notityPropertyChangedListeners(IMAGE_MOVED,
										null, sourceImg);
								moved = true;
							}
						};

						public void undo() {
							if (curImage != null
									&& sourceImg.getSeqNo() < curImage
											.getAnimationFrames().length - 1) {
								curImage.moveAnimationFrame(sourceImg,
										sourceImg.getSeqNo() + 2);
								notityPropertyChangedListeners(IMAGE_MOVED,
										null, sourceImg);
							}
						};

						public void execute() {
							redo();
						};
					};

					FrameAnimationContainer.this.graphicsEditor
							.getCommandStack().execute(command);
				}
			}
		});

		moveToRight = new Button(bC, SWT.FLAT);
		moveToRight.setToolTipText(Messages.MoveFrameToRightToolTip);
		moveToRight.setImage(MOVE_TO_RIGHT_IMAGE);
		// listener for image delete operation
		moveToRight.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// test if selected image has
				// remove selected image from animation'images'list
				if (selectedAnimationFrameButton != null) {

					final IAnimationFrame sourceImg = (IAnimationFrame) selectedAnimationFrameButton
							.getData(IAnimationFrame.class.getName());

					Command command = new Command(
							Messages.AnimationImageContainer_moveFrameToRight) {
						boolean moved;

						public boolean canExecute() {
							return curImage != null && sourceImg != null;
						};

						public boolean canUndo() {
							return moved;
						};

						public void redo() {
							if (curImage != null
									&& sourceImg.getSeqNo() < curImage
											.getAnimationFrames().length - 1) {
								curImage.moveAnimationFrame(sourceImg,
										sourceImg.getSeqNo() + 2);
								notityPropertyChangedListeners(IMAGE_MOVED,
										null, sourceImg);
								moved = true;
							}
						};

						public void undo() {
							if (curImage != null && sourceImg.getSeqNo() > 0) {
								curImage.moveAnimationFrame(sourceImg,
										sourceImg.getSeqNo() - 1);
								notityPropertyChangedListeners(IMAGE_MOVED,
										null, sourceImg);
							}
						};

						public void execute() {
							redo();
						};
					};

					FrameAnimationContainer.this.graphicsEditor
							.getCommandStack().execute(command);
				}
			}
		});

		moveToEnd = new Button(bC, SWT.FLAT);
		moveToEnd.setToolTipText(Messages.MoveFrameToEndToolTip);
		moveToEnd.setImage(MOVE_TO_END_IMAGE);
		// listener for image delete operation
		moveToEnd.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// test if selected image has
				// remove selected image from animation'images'list
				if (selectedAnimationFrameButton != null) {

					final IAnimationFrame sourceImg = (IAnimationFrame) selectedAnimationFrameButton
							.getData(IAnimationFrame.class.getName());

					Command command = new Command(
							Messages.AnimationImageContainer_moveFrameToEnd) {
						int oldSeqNo;

						public boolean canExecute() {
							return curImage != null && sourceImg != null;
						};

						public boolean canUndo() {
							return true;
						};

						public void redo() {
							oldSeqNo = sourceImg.getSeqNo();
							if (curImage != null
									&& sourceImg.getSeqNo() < curImage
											.getAnimationFrames().length - 1) {
								curImage.moveAnimationFrame(sourceImg, curImage
										.getAnimationFrames().length + 1);
								notityPropertyChangedListeners(IMAGE_MOVED,
										null, sourceImg);
							}
						};

						public void undo() {
							if (curImage != null) {
								if (sourceImg.getSeqNo() < oldSeqNo) {
									curImage.moveAnimationFrame(sourceImg,
											oldSeqNo + 1);
								} else {
									curImage.moveAnimationFrame(sourceImg,
											oldSeqNo);
								}
								notityPropertyChangedListeners(IMAGE_MOVED,
										null, sourceImg);
							}
						};

						public void execute() {
							redo();
						};
					};

					FrameAnimationContainer.this.graphicsEditor
							.getCommandStack().execute(command);
				}
			}
		});

		Label separator2 = new Label(bC, SWT.SEPARATOR);
		separator2.setLayoutData(new RowData(SWT.DEFAULT, removeButton
				.computeSize(SWT.DEFAULT, SWT.DEFAULT).y));

		Button setAnimateTime = new Button(bC, SWT.FLAT);
		setAnimateTime.setToolTipText(Messages.DistributeAnimateTimeToolTip);
		setAnimateTime.setImage(CASCADE_IMAGE);
		// listener for image delete operation
		setAnimateTime.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				ISelectionProvider provider = new ISelectionProvider() {
					public ISelection getSelection() {
						return new StructuredSelection(curImage);
					}

					public void setSelection(ISelection selection) {
					}

					public void addSelectionChangedListener(
							ISelectionChangedListener listener) {
					}

					public void removeSelectionChangedListener(
							ISelectionChangedListener listener) {
					}
				};

				Action distributeAnimateTimeAction = getActionFactory()
						.getDistributeAnimationTimeAction(provider);
				distributeAnimateTimeAction.run();
				if (timeLine != null) {
					timeLine.repaint();
				}
			}
		});

		updateButtonsState(false);

		this.graphicsEditor.getCommandStack().addCommandStackListener(this);
	}

	private void updateButtonsState(boolean updateLayerView) {
		if (selectedAnimationFrameButton != null
				&& !selectedAnimationFrameButton.isDisposed()) {
			IAnimationFrame frame = (IAnimationFrame) selectedAnimationFrameButton
					.getData(IAnimationFrame.class.getName());
			if (updateLayerView) {
				/* notify ActiveLayersPage about the change */
				IViewPart lView = PlatformUI
						.getWorkbench()
						.getActiveWorkbenchWindow()
						.getActivePage()
						.findView(
								"com.nokia.tools.s60.editor.ui.views.LayersView");
				if (lView != null) {
					if (lView instanceof org.eclipse.ui.ISelectionListener) {
						((org.eclipse.ui.ISelectionListener) lView)
								.selectionChanged(graphicsEditor,
										new StructuredSelection(frame));
					}
				}
			}
			if (curImage != null) {
				if (frame.getSeqNo() < curImage.getAnimationFrames().length - 1) {
					moveToEnd.setEnabled(true);
					moveToRight.setEnabled(true);
				} else {
					moveToEnd.setEnabled(false);
					moveToRight.setEnabled(false);
				}

				if (frame.getSeqNo() > 0) {
					moveToBegin.setEnabled(true);
					moveToLeft.setEnabled(true);
				} else {
					moveToBegin.setEnabled(false);
					moveToLeft.setEnabled(false);
				}
			} else {
				moveToBegin.setEnabled(false);
				moveToLeft.setEnabled(false);
				moveToEnd.setEnabled(false);
				moveToRight.setEnabled(false);
			}
		} else {
			moveToBegin.setEnabled(false);
			moveToLeft.setEnabled(false);
			moveToEnd.setEnabled(false);
			moveToRight.setEnabled(false);
		}
	}

	/**
	 * Add frame to animation.
	 * 
	 * @param animationFrame
	 */
	private void addAnimationFrame(IAnimationFrame animationFrame) {
		// create Widget for image containing
		// add this image container to animation dialog
		final Button b = new Button(c, SWT.PUSH | SWT.FLAT);
		b.setData(IAnimationFrame.class.getName(), animationFrame);
		b.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				selectedAnimationFrameButton = b;
				selectedAnimationFrame = (IAnimationFrame) b
						.getData(IAnimationFrame.class.getName());
				b.setFocus();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				selectedAnimationFrameButton = b;
				selectedAnimationFrame = (IAnimationFrame) b
						.getData(IAnimationFrame.class.getName());
				b.setFocus();
				ISelectionProvider prov = new ISelectionProvider() {
					public void addSelectionChangedListener(
							ISelectionChangedListener listener) {
						
					}

					public ISelection getSelection() {
						return new StructuredSelection(selectedAnimationFrame);
					}

					public void removeSelectionChangedListener(
							ISelectionChangedListener listener) {
						

					}

					public void setSelection(ISelection selection) {
						

					}
				};

				ActionContributionItem action = null;
				if (selectedAnimationFrame.isSvg()) {
					action = (ActionContributionItem) getActionFactory()
							.getPopupMenuContribution(prov).find(
									ACTION_ID_EDIT_SVG_IMAGE);
				} else {
					action = (ActionContributionItem) getActionFactory()
							.getPopupMenuContribution(prov).find(
									ACTION_ID_EDIT_BITMAP_IMAGE);
				}

				if (action != null) {
					if (action.getAction().isEnabled()) {
						action.getAction().run();
					}
				}
			}
		});

		b.addFocusListener(new FocusAdapter() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {
				selectedAnimationFrameButton = b;
				selectedAnimationFrame = (IAnimationFrame) b
						.getData(IAnimationFrame.class.getName());
				if (timeLine != null) {
					IAnimationFrame frame = (IAnimationFrame) b
							.getData(IAnimationFrame.class.getName());

					if (curNode != null
							&& ((ITimeLineRow) curNode.getRow()).getSource() == curImage) {

						if (curNode.getRow().getTimeLine()
								.getCurrentControlPoint() != frame) {
							((TimeLineNode) curNode)
									.selectControlPoint((IControlPoint) frame);
							timeLine.repaint();
						}
					}
				}
				updateButtonsState(true);
			};
		});

		// set image selection listener
		b.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				selectedAnimationFrameButton = b;
				selectedAnimationFrame = (IAnimationFrame) b
						.getData(IAnimationFrame.class.getName());
				updateButtonsState(false);
			}

		});

		registerKeyListener(b);

		registerContextMenu(b);

		// set image's widget LayoutData
		RenderedImage awtImage = animationFrame.getImage();

		ImageAdapter vpAdaptor = new ImageAdapter(animationFrame.getWidth(),
				animationFrame.getHeight());
		Graphics2D vpG = (Graphics2D) vpAdaptor.getGraphics();
		if (awtImage != null) {
			vpG.setColor(java.awt.Color.WHITE);
			vpG.fillRect(0, 0, animationFrame.getWidth(), animationFrame
					.getHeight());
			vpG.drawRenderedImage(awtImage, CoreImage.TRANSFORM_ORIGIN);
		} else {
			vpG.setColor(java.awt.Color.WHITE);
			vpG.fillRect(0, 0, animationFrame.getWidth(), animationFrame
					.getHeight());
		}
		vpG.dispose();

		Image image = vpAdaptor.toSwtImage();

		b.setImage(image);

		RowData rd = new RowData(b.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, b
				.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		b.setLayoutData(rd);

		// set image drag/drop capability
		new AnimationImageDragSource(b);
		new AnimationImageDropTarget(this, b);
	}

	private void registerContextMenu(final Button b) {
		final IAnimationFrame animationFrame = (IAnimationFrame) b
				.getData(IAnimationFrame.class.getName());

		final ISelectionProvider provider = new ISelectionProvider() {
			public ISelection getSelection() {
				return new StructuredSelection(animationFrame);
			}

			public void setSelection(ISelection selection) {
			}

			public void addSelectionChangedListener(
					ISelectionChangedListener listener) {
			}

			public void removeSelectionChangedListener(
					ISelectionChangedListener listener) {
			}
		};

		MenuManager mManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		mManager.setRemoveAllWhenShown(true);

		mManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(graphicsEditor.getActionRegistry().getAction(
						ActionFactory.UNDO.getId()));
				manager.add(graphicsEditor.getActionRegistry().getAction(
						ActionFactory.REDO.getId()));
				manager.add(new Separator());
				contributeToPopupMenu(manager, provider);

			};
		});

		Menu menu = mManager.createContextMenu(b);
		b.setMenu(menu);
	}

	protected void contributeToPopupMenu(IMenuManager manager,
			ISelectionProvider provider) {
		if (actionFactory != null) {
			IContributionManager subManager = actionFactory
					.getPopupMenuContribution(provider);
			IContributionItem[] items = subManager.getItems();
			for (IContributionItem item : items) {
				manager.add(item);
			}
		}
	}

	private void registerTimeLineContextMenu() {
		if (timeLineMenuListener != null) {
			((TimeLine) timeLine).getContextMenuManager().removeMenuListener(
					timeLineMenuListener);
		}

		timeLineMenuListener = createTimeLineMenuListener((TimeLine) timeLine);

		((TimeLine) timeLine).getContextMenuManager().addMenuListener(
				timeLineMenuListener);
	}

	@Override
	public void dispose() {
		if (timeLineMenuListener != null) {
			((TimeLine) timeLine).getContextMenuManager().removeMenuListener(
					timeLineMenuListener);
		}

		if (this.curImage != null) {
			this.curImage.getControlPointModel().removeControlPointListener(
					this);
		}

		Image image = undoButton.getImage();
		if (image != null && !image.isDisposed()) {
			image.dispose();
		}
		image = redoButton.getImage();
		if (image != null && !image.isDisposed()) {
			image.dispose();
		}

		graphicsEditor.getCommandStack().removeCommandStackListener(this);

		super.dispose();
	}

	
	private IMenuListener createTimeLineMenuListener(final ITimeLine timeLine) {

		return new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				if (isDisposed()) {
					((TimeLine) timeLine).getContextMenuManager()
							.removeMenuListener(this);
					return;
				}

				Separator separator = new Separator("frameMenuItems"); //$NON-NLS-1$
				if (manager.getItems().length > 0) {
					manager.insertBefore(manager.getItems()[0].getId(),
							separator);
				} else {
					manager.add(separator);
				}

				if (curNode != null && curNode.getRow().getSource() == curImage) {

					final IAnimationFrame animationFrame = (IAnimationFrame) curNode
							.getRow().getTimeLine().getCurrentControlPoint();

					if (animationFrame == null) {

						ISelectionProvider provider = new ISelectionProvider() {
							public ISelection getSelection() {
								return new StructuredSelection(curImage);
							}

							public void setSelection(ISelection selection) {
							}

							public void addSelectionChangedListener(
									ISelectionChangedListener listener) {
							}

							public void removeSelectionChangedListener(
									ISelectionChangedListener listener) {
							}
						};

						Action distributeAnimateTimeAction = getActionFactory()
								.getDistributeAnimationTimeAction(provider);

						manager.insertAfter(separator.getId(),
								distributeAnimateTimeAction);
					} else {

						ISelectionProvider provider = new ISelectionProvider() {
							public ISelection getSelection() {
								return new StructuredSelection(animationFrame);
							}

							public void setSelection(ISelection selection) {
							}

							public void addSelectionChangedListener(
									ISelectionChangedListener listener) {
							}

							public void removeSelectionChangedListener(
									ISelectionChangedListener listener) {
							}
						};

						MenuManager mManager = new MenuManager(
								Messages.AnimationImageContainer_FrameMenuItem);

						mManager.add(graphicsEditor.getActionRegistry()
								.getAction(ActionFactory.UNDO.getId()));
						mManager.add(graphicsEditor.getActionRegistry()
								.getAction(ActionFactory.REDO.getId()));
						mManager.add(new Separator());
						contributeToPopupMenu(mManager, provider);

						manager.insertAfter(separator.getId(), mManager);
					}
				}
			}
		};
	}

	private void registerKeyListener(final Button b) {
		b.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				final IAnimationFrame animationFrame = (IAnimationFrame) b
						.getData(IAnimationFrame.class.getName());

				ISelectionProvider provider = new ISelectionProvider() {
					public ISelection getSelection() {
						return new StructuredSelection(animationFrame);
					}

					public void setSelection(ISelection selection) {
					}

					public void addSelectionChangedListener(
							ISelectionChangedListener listener) {
					}

					public void removeSelectionChangedListener(
							ISelectionChangedListener listener) {
					}
				};

				if (e.keyCode == 99 && e.stateMask == 262144) {
					// CTRL+C
					try {
						Action copyAction = getActionFactory()
								.getCopyFrameAction(provider);
						if (copyAction.isEnabled()) {
							copyAction.run();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}

				} else if (e.keyCode == 118 && e.stateMask == 262144) {
					Action pasteAction = getActionFactory()
							.getPasteFrameAction(provider);
					if (pasteAction.isEnabled()) {
						pasteAction.run();
					}
				} else if (e.keyCode == SWT.DEL && e.stateMask == 0) {
					// Del
					Action removeAction = getActionFactory()
							.getRemoveFrameAction(provider);
					if (removeAction.isEnabled()) {
						removeAction.run();
					}
				} else if (e.keyCode == SWT.ARROW_LEFT && e.stateMask == 0) {
					IAnimationFrame[] frames = curImage.getAnimationFrames();
					for (int idx = 0; idx < frames.length; idx++) {
						if (frames[idx] == selectedAnimationFrame) {
							int leftIdx = Math.max(0, idx - 1);
							setFocus(frames[leftIdx]);
							break;
						}
					}
				} else if (e.keyCode == SWT.ARROW_RIGHT && e.stateMask == 0) {
					IAnimationFrame[] frames = curImage.getAnimationFrames();
					for (int idx = 0; idx < frames.length; idx++) {
						if (frames[idx] == selectedAnimationFrame) {
							int rightIdx = Math.min(frames.length - 1, idx + 1);
							setFocus(frames[rightIdx]);
							break;
						}
					}
				}

			}
		});
	}

	/**
	 * Adds initial animation images to the dialog. Use it to initialize
	 * animation dialog.
	 */
	public void setInitialImages(List<IAnimationFrame> frameList) {
		Control[] controls = c.getChildren();
		for (Control control : controls) {
			if (control instanceof Button) {
				if (((Button) control).getImage() != null) {
					((Button) control).getImage().dispose();
				}
				control.dispose();
			}
		}
		for (int i = 0; i < frameList.size(); i++) {
			IAnimationFrame fileAnimImage = frameList.get(i);
			addAnimationFrame(fileAnimImage);
		}
		sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		c.layout();

		controls = c.getChildren();
		if (controls.length > 0) {		
			if (controls[0] instanceof Button) {
				sc.getHorizontalBar().setIncrement(
						controls[0].getBounds().width + 3);
				sc.getVerticalBar()
						.setIncrement(controls[0].getBounds().height + 3);
				sc.getHorizontalBar().setPageIncrement(
						2 * sc.getHorizontalBar().getIncrement());
				sc.getVerticalBar().setPageIncrement(
						2 * sc.getVerticalBar().getIncrement());
			}
		}
	}

	/**
	 * Returns current animation images List ordered by how they occure in the
	 * animation
	 */
	public List<IAnimationFrame> getAnimationFrames() {
		List<IAnimationFrame> animationFrames = new ArrayList<IAnimationFrame>();
		Control[] buttons = c.getChildren();
		for (int i = 0; i < buttons.length; i++) {
			IAnimationFrame frame = (IAnimationFrame) ((Button) buttons[i])
					.getData(IAnimationFrame.class.getName());
			animationFrames.add(frame);
		}
		return animationFrames;

	}

	public void selectionChanged(IAnimatedImage image, ITimeLine timeLine) {
		if (this.timeLine != timeLine) {
			if (this.timeLine != null) {
				this.timeLine.removeSelectionListener(this);
			}

			this.timeLine = timeLine;

			if (this.timeLine != null) {
				this.timeLine.addSelectionListener(this);
				registerTimeLineContextMenu();
			}
		}

		if (this.curImage != image) {
			refresh(image);
		}
	}

	synchronized public void refresh(IAnimatedImage image) {
		if (isDisposed()) {
			return;
		}

		Point oldOrigin = null;
		IAnimationFrame focusedImage = selectedAnimationFrame;

		if (this.curImage != image) {
			if (this.curImage != null) {
				this.curImage.removePropertyChangeListener(this);
				this.curImage.getControlPointModel()
						.removeControlPointListener(this);
			}

			this.curImage = image;

			if (this.curImage != null) {
				this.curImage.addPropertyListener(this);
				this.curImage.getControlPointModel().addControlPointListener(
						this);
			}

			oldOrigin = new Point(0, 0);
		} else {
			oldOrigin = sc.getOrigin();
		}

		List<IAnimationFrame> animList = new ArrayList<IAnimationFrame>();

		if (this.curImage != null) {
			IAnimationFrame[] animationFrames = this.curImage
					.getAnimationFrames();

			for (int i = 0; i < animationFrames.length; i++) {
				animList.add((IAnimationFrame) animationFrames[i]);
			}
		}

		if (focusedImage != null && !animList.contains(focusedImage)) {
			// frame removed, update focus...
			int seqNo = focusedImage.getSeqNo();
			int newFocus = Math.min(seqNo, animList.size() - 1);
			if (newFocus >= 0) {
				focusedImage = animList.get(newFocus);
			} else {
				focusedImage = null;
			}
		}

		setInitialImages(animList);

		sc.setOrigin(oldOrigin);

		setFocus(focusedImage);

		redraw();
	}

	synchronized private void setFocus(IAnimationFrame focusedFrame) {
		if (focusedFrame == null) {
			selectedAnimationFrameButton = null;
			selectedAnimationFrame = null;
			return;
		}

		if (c.isDisposed()) {
			return;
		}

		Control[] buttons = c.getChildren();
		for (Control control : buttons) {
			if (!control.isDisposed()) {
				IAnimationFrame frame = (IAnimationFrame) ((Button) control)
						.getData(IAnimationFrame.class.getName());
				if (frame == focusedFrame) {
					Rectangle intersection = control.getBounds().intersection(
							new Rectangle(sc.getOrigin().x, sc.getOrigin().y,
									sc.getClientArea().width, sc
											.getClientArea().height));
					if (intersection.width == 0) {
						sc.setOrigin(control.getLocation().x, sc.getOrigin().y);
					}
					control.setFocus();
					selectedAnimationFrameButton = (Button) control;
					selectedAnimationFrame = (IAnimationFrame) control
							.getData(IAnimationFrame.class.getName());
					return;
				}
			}
		}

		if (timeLine != null) {
			if (curNode != null
					&& ((TimeLineRow) curNode.getRow()).getSource() == curImage) {

				if (curNode.getRow().getTimeLine().getCurrentControlPoint() != focusedFrame) {
					((TimeLineNode) curNode)
							.selectControlPoint((IControlPoint) focusedFrame);
					timeLine.repaint();
				}
			}
		}

		selectedAnimationFrameButton = null;
		selectedAnimationFrame = null;
	}

	public void addPropertyChangedListener(IPropertyChangeListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removePropertyChangedListener(IPropertyChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected void notityPropertyChangedListeners(final String propertyName,
			final Object oldValue, final Object newValue) {
		if (listeners.size() == 0) {
			return;
		}

		ExecutionThread.INSTANCE.execute(new Runnable() {
			public void run() {
				synchronized (listeners) {
					for (IPropertyChangeListener listener : listeners) {
						listener
								.propertyChange(new org.eclipse.jface.util.PropertyChangeEvent(
										this, propertyName, oldValue, newValue));
					}
				}
			};
		});
	}

	public IAnimatedImage getImage() {
		return curImage;
	}

	public void selectionChanged(final IStructuredSelection selection) {
		final Object first = selection.getFirstElement();
		if (first instanceof IAnimationFrame) {
			if (Display.getCurrent() != null) {
				setFocus((IAnimationFrame) first);
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setFocus((IAnimationFrame) first);
					}
				});
			}
			curNode = (TimeLineNode) selection.toArray()[1];
		} else if (first instanceof TimeLineNode) {
			curNode = (TimeLineNode) first;
		} else {
			curNode = null;
		}
	}

	public void controlPointCreated(IControlPoint point) {
		refresh(curImage);
		setFocus((IAnimationFrame) point);
		notityPropertyChangedListeners(IMAGE_ADDED, null, point);
	}

	public void controlPointMoved(IControlPoint point) {
		refresh(curImage);
		notityPropertyChangedListeners(IMAGE_MOVED, null, point);
	}

	public void controlPointRemoved(IControlPoint point) {
		refresh(curImage);
		notityPropertyChangedListeners(IMAGE_REMOVED, null, point);
	}

	public void controlPointSelected(IControlPoint point) {
	}

	public void propertyChange(PropertyChangeEvent evt) {
		refresh(curImage);
		notityPropertyChangedListeners(IMAGE_CHANGED, null, curImage);
	}

	public void commandStackChanged(EventObject event) {
		if (undoButton == null || redoButton == null || undoButton.isDisposed()
				|| redoButton.isDisposed()) {
			return;
		}

		if (graphicsEditor.getCommandStack().canUndo()) {
			undoButton.setEnabled(true);
			Image oldImage = undoButton.getImage();
			UndoAction undoAction = (UndoAction) graphicsEditor
					.getActionRegistry().getAction(ActionFactory.UNDO.getId());
			undoAction.update();
			undoButton.setImage(undoAction.getImageDescriptor().createImage());
			undoButton.setToolTipText(undoAction.getText());
			if (oldImage != null && !oldImage.isDisposed()) {
				oldImage.dispose();
			}
		} else {
			undoButton.setEnabled(false);
			Image oldImage = undoButton.getImage();
			undoButton.setImage(graphicsEditor.getActionRegistry().getAction(
					ActionFactory.UNDO.getId()).getDisabledImageDescriptor()
					.createImage());
			undoButton.setToolTipText(null);
			if (oldImage != null && !oldImage.isDisposed()) {
				oldImage.dispose();
			}
		}

		if (graphicsEditor.getCommandStack().canRedo()) {
			redoButton.setEnabled(true);
			Image oldImage = redoButton.getImage();
			RedoAction redoAction = (RedoAction) graphicsEditor
					.getActionRegistry().getAction(ActionFactory.REDO.getId());
			redoAction.update();
			redoButton.setImage(redoAction.getImageDescriptor().createImage());
			redoButton.setToolTipText(redoAction.getText());
			if (oldImage != null && !oldImage.isDisposed()) {
				oldImage.dispose();
			}
		} else {
			redoButton.setEnabled(false);
			Image oldImage = redoButton.getImage();
			redoButton.setImage(graphicsEditor.getActionRegistry().getAction(
					ActionFactory.REDO.getId()).getDisabledImageDescriptor()
					.createImage());
			redoButton.setToolTipText(null);
			if (oldImage != null && !oldImage.isDisposed()) {
				oldImage.dispose();
			}
		}
	}

	protected AbstractMediaEditorPart getGraphicEditor() {
		return graphicsEditor;
	}

}
