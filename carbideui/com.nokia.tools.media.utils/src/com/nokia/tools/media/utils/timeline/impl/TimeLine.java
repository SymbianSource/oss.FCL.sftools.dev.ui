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
package com.nokia.tools.media.utils.timeline.impl;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.timeline.IDisplaySettings;
import com.nokia.tools.media.utils.timeline.IGridSettings;
import com.nokia.tools.media.utils.timeline.ISelectionListener;
import com.nokia.tools.media.utils.timeline.ITimeLabelProvider;
import com.nokia.tools.media.utils.timeline.ITimeLine;
import com.nokia.tools.media.utils.timeline.ITimeLineDataProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineGridLabelProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineNode;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.ITimeListener;
import com.nokia.tools.media.utils.timeline.ITimer;
import com.nokia.tools.media.utils.timeline.ITreeTimeLineDataProvider;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;

public class TimeLine extends Composite implements ITimeLine {

	public static final String GO_TO_TIME_CONTEXT = "com.nokia.tools.media.utils"
			+ '.' + "gototime_context";

	public static final String SET_CURRENT_TIME_CONTEXT = "com.nokia.tools.media.utils"
			+ '.' + "setcurrenttime_context";

	public static final String ZOOM_SET_DISPLAY_WIDTH_MENU_ITEM_CONTEXT = "com.nokia.tools.media.utils" + '.' + "setdisplaywidth_context"; //$NON-NLS-1$ //$NON-NLS-2$

	protected static final Image RESUME_IMAGE = UtilsPlugin.getImageDescriptor(
			"icons/resume_co.gif").createImage(); //$NON-NLS-1$

	protected static final Image SUSPEND_IMAGE = UtilsPlugin
			.getImageDescriptor("icons/suspend_co.gif").createImage(); //$NON-NLS-1$

	protected static final Image CANCEL_IMAGE = UtilsPlugin.getImageDescriptor(
			"icons/ch_cancel.gif").createImage();//$NON-NLS-1$

	protected static final int CURSOR_ALPHA_VALUE = 128;

	protected static final long DEFAULT_DISPLAY_WIDTH = 100;

	protected static final long DEFAULT_GRID_MAJOR_INTERVAL = 10;

	protected static final long DEFAULT_GRID_MINOR_INTERVAL = 2;

	protected static final int DEFAULT_ROW_HEIGHT = 20;

	protected static final Pattern GRID_HEADER_PATTERN = new Pattern(null, 0,
			0, 1, 1, ColorConstants.white, ColorConstants.gray);

	protected ITimeLineDataProvider dataProvider;

	protected long startTime;

	protected long endTime;

	protected long currentTime;

	protected DisplayData displayData = new DisplayData();

	protected TimeLineGridData gridData = new TimeLineGridData();

	protected Color cursorColor = IMediaConstants.DEFAULT_CURSOR_COLOR;

	protected Pattern backgroundPattern = new Pattern(null, 0, 0, 1, 1,
			IMediaConstants.BACKGROUND_COLOR, IMediaConstants.BACKGROUND_COLOR);

	protected int rowHeight = DEFAULT_ROW_HEIGHT;

	protected Composite gridHeaderComposite;

	protected ScrolledComposite scComposite;

	protected Composite topComposite;

	protected Composite bottomComposite;

	protected ToolBar topToolBar;

	protected Composite canvas;

	protected Composite labels;

	protected Slider slider;

	protected Label currentTimeLabel;

	protected List<ITimeLineRow> rows = new ArrayList<ITimeLineRow>();

	protected ITimeLineGridLabelProvider gridLabelProvider;

	protected ITimeLabelProvider timeLabelProvider;

	protected Timer timer = new Timer();

	protected Set<ITimeListener> timeListeners = new HashSet<ITimeListener>();

	protected Set<ISelectionListener> selectionListeners = new HashSet<ISelectionListener>();

	protected TimeChangedNotifierRunnable notifier = new TimeChangedNotifierRunnable();

	protected MenuManager canvasMenuManager;

	protected MenuManager gridHeaderMenuManager;

	private boolean trackMouseMoveOverGridHeader = false;

	private boolean trackMouseMoveOverCanvas = false;

	private TimeLineRow mouseDownRow;

	private IControlPoint selectedControlPoint;

	private ITimeLineRow selectedRow;

	private ITimeLineNode selectedNode;

	private ToolItem resumeButton;

	private ToolItem cancelButton;

	private ToolItem suspendButton;

	private boolean showAnimatedOnly;

	private static final Dimension DEFAULT_SIZE = new Dimension(16, 16);

	public TimeLine(Composite parent, int style) {
		super(parent, style);

		createControls(parent);

		ExecutionThread.INSTANCE.execute(notifier);
	}

	public TimeLine(Composite parent, int style,
			ITimeLineDataProvider dataProvider) {
		super(parent, style);

		createControls(parent);

		ExecutionThread.INSTANCE.execute(notifier);

		initialize(dataProvider);
	}

	public TimeLine(Composite parent, int style, long startTime, long endTime,
			long displayWidth) {
		super(parent, style);

		createControls(parent);

		ExecutionThread.INSTANCE.execute(notifier);

		initialize(startTime, endTime, displayWidth);
	}

	public TimeLine(Composite parent, int style, long startTime, long endTime,
			long displayWidth, ITimeLabelProvider timeLabelProvider,
			ITimeLineGridLabelProvider gridLabelProvider) {
		super(parent, style);

		createControls(parent);

		ExecutionThread.INSTANCE.execute(notifier);

		initialize(startTime, endTime, displayWidth, timeLabelProvider,
				gridLabelProvider);
	}

	protected void createControls(Composite parent) {
		addDisposeListener(new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				onDispose(e);
			}

		});
		GridLayout gridLayout = new GridLayout(1, false);
		this.setLayout(gridLayout);

		Composite timeLineComposite = new Composite(this, SWT.BORDER);
		GridLayout timeLineCompositeLayout = new GridLayout(1, false);
		timeLineCompositeLayout.marginWidth = 0;
		timeLineCompositeLayout.marginHeight = 0;
		timeLineCompositeLayout.verticalSpacing = 0;
		timeLineCompositeLayout.horizontalSpacing = 0;
		timeLineComposite.setLayout(timeLineCompositeLayout);
		timeLineComposite.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, true, true));

		topComposite = createTopComposite(timeLineComposite);

		topToolBar = createTopToolBar(topComposite);

		gridHeaderComposite = createGridHeader(topComposite);

		scComposite = new ScrolledComposite(timeLineComposite, SWT.V_SCROLL);
		GridLayout scCompositeLayout = new GridLayout(1, false);
		scCompositeLayout.marginHeight = 0;
		scCompositeLayout.verticalSpacing = 0;
		scCompositeLayout.marginWidth = 0;
		scCompositeLayout.horizontalSpacing = 0;
		scComposite.setLayout(scCompositeLayout);
		scComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true));

		Composite composite = new Composite(scComposite, SWT.NONE);
		GridLayout compositeLayout = new GridLayout(2, false);
		compositeLayout.marginHeight = 0;
		compositeLayout.verticalSpacing = 0;
		compositeLayout.marginWidth = 0;
		compositeLayout.horizontalSpacing = 0;
		composite.setLayout(compositeLayout);
		composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true));

		labels = createLabelsArea(composite);
		canvas = createTimeLineCanvas(composite);

		scComposite.setContent(composite);
		scComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scComposite.setExpandHorizontal(true);
		scComposite.setExpandVertical(true);

		scComposite.getVerticalBar().setIncrement(DEFAULT_SIZE.height);
		scComposite.getVerticalBar().setPageIncrement(
				2 * scComposite.getVerticalBar().getIncrement());

		bottomComposite = createBottomComposite(this);
		currentTimeLabel = createCurrentTimeLabel(bottomComposite);

		createPopupMenus();
	}

	protected void onDispose(DisposeEvent e) {
		if (backgroundPattern != null) {
			backgroundPattern.dispose();
		}
	}

	protected Composite createTopComposite(final Composite parent) {
		final Composite topComposite = new Composite(parent, SWT.NONE);

		GridLayout topCompositeLayout = new GridLayout(3, false);
		topCompositeLayout.marginWidth = 0;
		topCompositeLayout.marginHeight = 0;
		topCompositeLayout.verticalSpacing = 0;
		topCompositeLayout.horizontalSpacing = 0;
		topComposite.setLayout(topCompositeLayout);

		topComposite.setLayoutData(new GridData(GridData.FILL,
				GridData.BEGINNING, true, false));

		return topComposite;
	}

	protected void createPopupMenus() {
		gridHeaderMenuManager = new MenuManager("#GridHeaderPopupMenu"); //$NON-NLS-1$
		gridHeaderMenuManager.setRemoveAllWhenShown(true);

		Menu gridHeaderMenu = gridHeaderMenuManager
				.createContextMenu(gridHeaderComposite);
		gridHeaderComposite.setMenu(gridHeaderMenu);

		final IAction setCurrentTimeAction = new Action(Messages
				.getString("TimeLine.MenuItem_SetCurrentTime")) { //$NON-NLS-1$
			@Override
			public void run() {
				IInputValidator validator = new IInputValidator() {
					public String isValid(String newText) {
						try {
							if (timeLabelProvider != null) {
								timeLabelProvider.parse(newText);
							} else {
								try {
									Long.parseLong(newText);
								} catch (NumberFormatException ex) {
									throw new NumberFormatException(
											Messages
													.getString("TimeLine.Error_NotANumber")); //$NON-NLS-1$
								}
							}
						} catch (Exception ex) {
							return ex.getMessage();
						}
						return null;
					};
				};

				String defaultValue;

				if (timeLabelProvider != null) {
					defaultValue = timeLabelProvider.getLabel(getCurrentTime());
				} else {
					defaultValue = ((Long) getCurrentTime()).toString();
				}

				InputDialog dialog = new InputDialog(
						gridHeaderComposite.getShell(),
						Messages
								.getString("TimeLine.DialogTitle_SetCurrentTime"), Messages.getString("TimeLine.TextInput_EnterTime"), //$NON-NLS-1$ //$NON-NLS-2$
						defaultValue, validator);

				if (dialog.open() == Dialog.OK) {
					try {
						long time = 0;
						if (timeLabelProvider != null) {
							time = timeLabelProvider.parse(dialog.getValue());
						} else {
							time = Long.parseLong(dialog.getValue());
						}
						setCurrentTime(time);
						if (getCurrentTime() < displayData
								.getDisplayStartTime()
								|| getCurrentTime() > displayData
										.getDisplayEndTime()) {
							getDisplayData().setDisplayStartTime(
									Math.max(0, getCurrentTime()
											- getDisplayData()
													.getDisplayWidthInTime()
											/ 2));
						}
						repaint();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		};

		PlatformUI.getWorkbench().getHelpSystem().setHelp(setCurrentTimeAction,
				TimeLine.SET_CURRENT_TIME_CONTEXT);

		final IAction zoomInAction = new Action(Messages
				.getString("TimeLine.MenuItem_ZoomIn")) { //$NON-NLS-1$
			@Override
			public void run() {
				zoomIn();
			}
		};

		final IAction zoomOutAction = new Action(Messages
				.getString("TimeLine.MenuItem_ZoomOut")) { //$NON-NLS-1$
			@Override
			public void run() {
				zoomOut();
			}
		};

		final IAction setDisplayWidthAction = new Action(Messages
				.getString("TimeLine.MenuItem_SetDisplayWidth")) { //$NON-NLS-1$
			@Override
			public void run() {
				IInputValidator validator = new IInputValidator() {
					public String isValid(String newText) {
						try {
							if (timeLabelProvider != null) {
								timeLabelProvider.parse(newText);
							} else {
								try {
									Long.parseLong(newText);
								} catch (NumberFormatException ex) {
									throw new NumberFormatException(
											Messages
													.getString("TimeLine.Error_NotANumber")); //$NON-NLS-1$
								}
							}
						} catch (Exception ex) {
							return ex.getMessage();
						}
						return null;
					};
				};

				String defaultValue;

				if (timeLabelProvider != null) {
					defaultValue = timeLabelProvider.getLabel(getDisplayData()
							.getDisplayWidthInTime());
				} else {
					defaultValue = ((Long) getDisplayData()
							.getDisplayWidthInTime()).toString();
				}

				InputDialog dialog = new InputDialogWithContext(
						gridHeaderComposite.getShell(),
						Messages
								.getString("TimeLine.DialogTitle_SetDisplayWidth"), //$NON-NLS-1$
						Messages.getString("TimeLine.TextInput_EnterTime"), defaultValue, validator); //$NON-NLS-1$

				if (dialog.open() == Dialog.OK) {
					try {
						long time = 0;
						if (timeLabelProvider != null) {
							time = timeLabelProvider.parse(dialog.getValue());
						} else {
							time = Long.parseLong(dialog.getValue());
						}
						getDisplayData().setDisplayWidthInTime(time);
						repaint();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		};

		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				setDisplayWidthAction,
				TimeLine.ZOOM_SET_DISPLAY_WIDTH_MENU_ITEM_CONTEXT);

		final IAction shitem = new Action(Messages
				.getString("TimeLine.MenuItem_ShowAnimatedOnly"), SWT.CHECK) { //$NON-NLS-1$
			@Override
			public void run() {
				showAnimatedOnly = isChecked();
				if (TimeLine.this instanceof TreeTimeLine) {
					dataProvider.setShowAnimatedOnly(showAnimatedOnly);
					((TreeTimeLine) TimeLine.this)
							.setTreeContentProvider(((ITreeTimeLineDataProvider) dataProvider)
									.getTreeContentProvider());
					((TreeTimeLine) TimeLine.this).setInput(dataProvider
							.getInput());
					((TreeTimeLine) TimeLine.this).getTreeViewer().expandAll();
					((TreeTimeLine) TimeLine.this).synchronizeRowsWithTree();
				} else {
					initialize(dataProvider);
				}
			}
		};

		shitem.setChecked(showAnimatedOnly);

		gridHeaderMenuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(setCurrentTimeAction);
				manager.add(new Separator());

				MenuManager zoomMenuManager = new MenuManager(Messages
						.getString("TimeLine.MenuItem_Zoom")); //$NON-NLS-1$
				zoomMenuManager.add(zoomInAction);
				zoomMenuManager.add(zoomOutAction);
				zoomMenuManager.add(new Separator());
				zoomMenuManager.add(setDisplayWidthAction);

				manager.add(zoomMenuManager);

				manager.add(new Separator());

				manager.add(shitem);
				manager.updateAll(true);
			}
		});

		canvasMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		canvasMenuManager.setRemoveAllWhenShown(true);
		canvasMenuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				if (getSelectedNode() != null) {
					((TimeLineNode) getSelectedNode())
							.contributeToContextMenu(manager);
				}

				if (getSelectedRow() != null) {
					((TimeLineRow) getSelectedRow())
							.contributeToContextMenu(manager);
				}
			}
		});

		Menu canvasMenu = canvasMenuManager.createContextMenu(canvas);
		canvas.setMenu(canvasMenu);
	}

	public MenuManager getContextMenuManager() {
		return canvasMenuManager;
	}

	public MenuManager getGridHeaderContextMenuManager() {
		return gridHeaderMenuManager;
	}

	protected Composite createGridHeader(Composite parent) {
		Composite gridHeaderComposite = new Composite(parent, SWT.NO_BACKGROUND);
		gridHeaderComposite.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, true, false));

		((GridData) gridHeaderComposite.getLayoutData()).heightHint = gridData.gridHeaderHeight;
		((GridData) gridHeaderComposite.getLayoutData()).minimumHeight = gridData.gridHeaderHeight;

		gridHeaderComposite.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paintGridHeader(e.gc);
			}
		});

		gridHeaderComposite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 1) {
					trackMouseMoveOverGridHeader = false;
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					trackMouseMoveOverGridHeader = true;
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				int xPos = e.x - getGridHeaderArea().x + 1;
				long newTime = getTimeForPoint(new Point(xPos, 0));
				setCurrentTime(newTime);
				if (getCurrentTime() > displayData.getDisplayEndTime()) {
					displayData.setDisplayStartTime(displayData
							.getDisplayStartTime()
							- displayData.getDisplayEndTime()
							+ getCurrentTime());
				}
				repaint();
			}
		});

		gridHeaderComposite.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (trackMouseMoveOverGridHeader) {
					int xPos = e.x - getGridHeaderArea().x + 1;
					long newTime = getTimeForPoint(new Point(xPos, 0));

					if (getSelectedRow() != null) {
						TimeLineRow row = ((TimeLineRow) getSelectedRow());
						xPos += getNodesArea().x;
						int yPos = Arrays.asList(getVisibleRows()).indexOf(row)
								* getRowHeight() + (getRowHeight() / 2);
						TimeLineNode node = ((TimeLineRow) getSelectedRow())
								.findNodeAtPoint(xPos, yPos);
						if (node != null) {
							IControlPoint cp = node.findControlPointAtPoint(
									xPos - 1, yPos);
							if (cp != null) {
								newTime = cp.getTime();
								setSelectedRow(row);
								setSelectedNode(node);
								setSelectedControlPoint(cp);
								notifySelectionListeners();
							}
						}
					}

					setCurrentTime(newTime);
					if (getCurrentTime() > displayData.getDisplayEndTime()) {
						displayData.setDisplayStartTime(displayData
								.getDisplayStartTime()
								- displayData.getDisplayEndTime()
								+ getCurrentTime());
					} else if (getCurrentTime() < displayData
							.getDisplayStartTime()) {
						displayData.setDisplayStartTime(displayData
								.getDisplayStartTime()
								- displayData.getDisplayStartTime()
								+ getCurrentTime());
					}
					repaint();
				}
			}
		});

		gridHeaderComposite.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.KEYPAD_SUBTRACT && e.stateMask == 0) {
					zoomOut();
				}
				if (e.keyCode == SWT.KEYPAD_ADD && e.stateMask == 0) {
					zoomIn();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});

		return gridHeaderComposite;
	}

	protected Composite createTimeLineCanvas(Composite parent) {
		Composite canvas = new Canvas(parent, SWT.NO_BACKGROUND);
		canvas.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				try {
					paintCanvas(e.gc);
				} catch (Exception er) {
					er.printStackTrace();
				}
			}
		});

		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				int xPos = e.x - getGridArea().x;
				int yPos = e.y - getGridArea().y;

				long time = getTimeForPoint(new Point(xPos, 0));

				int rowNumber = yPos / getRowHeight();

				if (rowNumber >= getVisibleRows().length) {
					return;
				}

				mouseDownRow = (TimeLineRow) getVisibleRows()[rowNumber];

				mouseDownRow.mouseDoubleClick(e.button, e.x, e.y, time);

				repaint();
			}

			@Override
			public void mouseUp(MouseEvent e) {
				trackMouseMoveOverCanvas = false;

				if (mouseDownRow != null) {
					int xPos = e.x - getGridArea().x;
					long time = getTimeForPoint(new Point(xPos, 0));

					mouseDownRow.mouseUp(e.button, e.x, e.y, time);

					mouseDownRow = null;
				}

				repaint();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				trackMouseMoveOverCanvas = true;

				int xPos = e.x - getGridArea().x;
				int yPos = e.y - getGridArea().y;

				long time = getTimeForPoint(new Point(xPos, 0));

				int rowNumber = yPos / getRowHeight();

				if (rowNumber >= getVisibleRows().length) {
					return;
				}

				// clear selection fields
				selectedControlPoint = null;
				selectedNode = null;
				selectedRow = null;

				mouseDownRow = (TimeLineRow) getVisibleRows()[rowNumber];

				mouseDownRow.mouseDown(e.button, e.x, e.y, time);

				notifySelectionListeners();

				repaint();
			}
		});

		canvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (trackMouseMoveOverCanvas) {
					if (mouseDownRow != null) {
						int xPos = e.x - getGridArea().x;

						long time = getTimeForPoint(new Point(xPos, 0));

						time = Math.max(time, startTime);

						mouseDownRow.mouseMove(e.x, e.y, time);

						if (time > displayData.getDisplayEndTime()) {
							displayData.setDisplayStartTime(displayData
									.getDisplayStartTime()
									- displayData.getDisplayEndTime() + time);
						} else if (time < displayData.getDisplayStartTime()) {
							displayData.setDisplayStartTime(displayData
									.getDisplayStartTime()
									- displayData.getDisplayStartTime() + time);
						}

						repaint();
					}
				}
			};
		});

		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.KEYPAD_SUBTRACT && e.stateMask == 0) {
					zoomOut();
				}
				if (e.keyCode == SWT.KEYPAD_ADD && e.stateMask == 0) {
					zoomIn();
				}

				ITimeLineRow row = getSelectedRow();

				if (row == null) {
					return;
				}

				((TimeLineRow) row).keyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				ITimeLineRow row = getSelectedRow();

				if (row == null) {
					return;
				}

				((TimeLineRow) row).keyReleased(e);
			}
		});

		return canvas;
	}

	protected Composite createLabelsArea(Composite parent) {
		Composite labels = new Composite(parent, SWT.NONE);
		labels.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false,
				true));
		labels.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paintLabels(e.gc);
			};
		});

		return labels;
	}

	protected Composite createBottomComposite(final Composite parent) {
		final Composite comp = new Composite(parent, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		compLayout.marginWidth = 0;
		compLayout.marginHeight = 0;
		compLayout.verticalSpacing = 0;
		compLayout.horizontalSpacing = 0;
		comp.setLayout(compLayout);
		comp.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING,
				true, false));

		final Composite spacer = new Composite(comp, SWT.NONE);
		RowLayout spacerRl = new RowLayout(SWT.HORIZONTAL);
		spacerRl.marginTop = 0;
		spacerRl.marginBottom = 0;
		spacer.setLayout(spacerRl);
		spacer.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				int spacerPefWidth = spacer.computeSize(SWT.DEFAULT,
						SWT.DEFAULT).x;
				int labelsWidth = labels.getSize().x;
				int toolbarWidth = getTopToolBar().getSize().x;
				int maxWidth = Math.max(toolbarWidth, Math.max(spacerPefWidth,
						labelsWidth));
				if (maxWidth > labelsWidth) {
					GridData gd = (GridData) labels.getLayoutData();
					gd.widthHint = maxWidth;
					labels.getParent().layout(true, true);
					getTopToolBar().getParent().layout(true, true);
					repaintAll();
				}

				GridData gd = new GridData(maxWidth, SWT.DEFAULT);
				spacer.setLayoutData(gd);
				parent.layout(true, true);
			}
		});

		slider = createSlider(comp);

		return spacer;
	}

	protected Slider createSlider(final Composite parent) {

		final Slider slider = new Slider(parent, SWT.HORIZONTAL);

		slider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int newDisplayStart = slider.getSelection();
				displayData.setDisplayStartTime(newDisplayStart);
				repaint();
			}
		});
		slider.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true,
				true));
		return slider;
	}

	protected Label createCurrentTimeLabel(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText("CT: N/A"); //$NON-NLS-1$
		Point prefferedSize = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		label.setLayoutData(new RowData(prefferedSize.x, prefferedSize.y));
		label.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				RowData rd = (RowData) label.getLayoutData();
				Point prefferedSize = label.computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				Point currentSize = new Point(rd.width, rd.height);
				if (currentSize.x < prefferedSize.x) {
					rd.width = prefferedSize.x;
					parent.layout(true, true);
				}
			}
		});
		return label;
	}

	protected ToolBar createTopToolBar(final Composite parent) {
		final Composite tbComposite = new Composite(parent, SWT.NONE);
		GridLayout tbCompositeLayout = new GridLayout(2, false);
		tbCompositeLayout.marginWidth = 0;
		tbCompositeLayout.marginHeight = 0;
		tbCompositeLayout.verticalSpacing = 0;
		tbCompositeLayout.horizontalSpacing = 0;
		tbComposite.setLayout(tbCompositeLayout);

		final ToolBar tb = new ToolBar(tbComposite, SWT.FLAT);
		tb.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Point computed = tb.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				if (getBottomComposite() != null) {
					computed.x = Math.max(computed.x, getBottomComposite()
							.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
				}
				if (labels.getSize().x < computed.x) {
					GridData gd = (GridData) labels.getLayoutData();
					gd.widthHint = computed.x;
					labels.getParent().layout();
				} else {
					computed.x = labels.getSize().x;
				}

				GridData gd = new GridData(computed.x, computed.y);
				tbComposite.setLayoutData(gd);
				parent.layout();
			}
		});

		resumeButton = new ToolItem(tb, SWT.NONE);
		resumeButton.setImage(RESUME_IMAGE);
		resumeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dataProvider != null) {
					getTimer().setSpeed(
							dataProvider.getClockTimePerIncrement(),
							dataProvider.getClockIncrement());
				}
				getTimer().start();
			}
		});

		suspendButton = new ToolItem(tb, SWT.FLAT);
		suspendButton.setImage(SUSPEND_IMAGE);
		suspendButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getTimer().stop();
			}
		});
		suspendButton.setEnabled(false);

		cancelButton = new ToolItem(tb, SWT.FLAT);
		cancelButton.setImage(CANCEL_IMAGE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getTimer().stop();
				getTimer().setTime(getStartTime());
				if (getCurrentTime() < displayData.getDisplayStartTime()
						|| getCurrentTime() > displayData.getDisplayEndTime()) {
					displayData.setDisplayStartTime(getCurrentTime());
					repaint();
				}
			}
		});
		cancelButton.setEnabled(true);

		// currentTimeLabel = createCurrentTimeLabel(tbComposite);

		return tb;
	}

	// protected ToolBar createBottomToolBar(Composite parent) {
	// ToolBar tb = new ToolBar(parent, SWT.FLAT);
	//
	// return tb;
	// }

	public void initialize(ITimeLineDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		dataProvider.setShowAnimatedOnly(showAnimatedOnly);

		initialize(dataProvider.getStartTime(), dataProvider.getEndTime(),
				dataProvider.getDisplayWidth(), dataProvider
						.getTimeLabelProvider(), dataProvider
						.getGridLabelProvider());

		displayData.setDisplayStartTime(dataProvider.getDisplayStart());
		setCurrentTime(dataProvider.getInitialTime());

		gridData.setShowGrid(dataProvider.getShowGrid());
		gridData.setShowGridHeader(dataProvider.getShowGridHeader());
		gridData.setGridInterval(dataProvider.getMajorGridInterval(),
				dataProvider.getMinorGridInterval());

		getTimer().setSpeed(dataProvider.getClockTimePerIncrement(),
				dataProvider.getClockIncrement());

		if (dataProvider.getInput() instanceof ITimeLineRow[]) {
			setRows((ITimeLineRow[]) dataProvider.getInput());
		}
	}

	public void initialize(long startTime, long endTime, long displayWidth) {
		initialize(startTime, endTime, displayWidth, null, null);
	}

	public void initialize(long startTime, long endTime, long displayWidth,
			ITimeLabelProvider timeLabelProvider,
			ITimeLineGridLabelProvider gridLabelProvider) {

		this.startTime = startTime;
		this.endTime = endTime;
		this.displayData.setDisplayStartTime(startTime);
		this.displayData.setDisplayWidthInTime(displayWidth);

		setTimeLabelProvider(timeLabelProvider);
		setGridLabelProvider(gridLabelProvider);

		setCurrentTime(startTime);
	}

	public ITimeLineRow[] getRows() {
		return rows.toArray(new TimeLineRow[0]);
	}

	protected ITimeLineRow[] getVisibleRows() {
		List<ITimeLineRow> visibleRows = new ArrayList<ITimeLineRow>();
		synchronized (rows) {
			for (ITimeLineRow row : rows) {
				if (row.isVisible()) {
					visibleRows.add(row);
				}
			}
		}
		return visibleRows.toArray(new TimeLineRow[visibleRows.size()]);
	};

	public void setRows(ITimeLineRow[] rows) {
		synchronized (rows) {

			removeAllRows();

			for (ITimeLineRow row : rows) {
				this.rows.add(row);
				((TimeLineRow) row).setTimeLine(this);
			}
		}
		updateVerticalScrollbar();
	}

	public void addRow(ITimeLineRow row) {
		synchronized (rows) {
			this.rows.add(row);
			((TimeLineRow) row).setTimeLine(this);
			updateVerticalScrollbar();
		}
	}

	public void addRow(ITimeLineRow row, int index) {
		synchronized (rows) {
			this.rows.add(index, row);
			((TimeLineRow) row).setTimeLine(this);
			updateVerticalScrollbar();
		}
	}

	public void removeRow(ITimeLineRow row) {
		synchronized (rows) {
			this.rows.remove(row);
			((TimeLineRow) row).setTimeLine(null);
			updateVerticalScrollbar();
		}
	}

	public void removeAllRows() {
		synchronized (rows) {
			for (ITimeLineRow row : rows) {
				((TimeLineRow) row).setTimeLine(null);
			}
			this.rows.clear();
			updateVerticalScrollbar();
		}
	}

	protected void updateVerticalScrollbar() {
		Runnable runnable = new Runnable() {
			public void run() {
				if (!canvas.isDisposed()) {
					GridData canvasGD = (GridData) canvas.getLayoutData();
					canvasGD.minimumHeight = getVisibleRows().length
							* rowHeight;
					canvas.setLayoutData(canvasGD);
					scComposite.setMinSize(scComposite.getContent()
							.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
				}
			}
		};
		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(runnable);
		} else {
			runnable.run();
		}
	}

	protected Rectangle getGridHeaderArea() {

		// int x1 = labels.getSize().x + gridData.leftGridMargins;
		int x1 = gridData.leftGridMargins;
		int y1 = 0;
		int x2 = scComposite.getSize().x - 1;
		int y2 = topComposite.getSize().y - 1;

		return new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
	}

	protected Rectangle getGridArea() {
		int x1 = gridData.leftGridMargins;
		int y1 = 0;
		int x2 = canvas.getSize().x - 1;
		int y2 = canvas.getSize().y - 1;

		return new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
	}

	protected Rectangle getNodesArea() {
		int x1 = gridData.leftGridMargins;
		int y1 = 0;
		int x2 = canvas.getSize().x - gridData.rightGridMargins;
		int y2 = canvas.getSize().y - 1;

		return new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
	}

	protected void paintLabels(GC gc) {
		int x1 = 0;
		int y1 = getNodesArea().y;
		int x2 = labels.getSize().x - 1;
		int y2 = getNodesArea().y + rowHeight - 1;

		synchronized (rows) {
			for (ITimeLineRow row : rows) {

				if (!row.isVisible()) {
					continue;
				}

				String text = row.getLabel();

				if (text == null) {
					y1 += rowHeight;
					y2 += rowHeight;

					continue;
				}

				Point textExtent = gc.textExtent(text);

				Rectangle bounds = new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1
						+ 1);

				gc.drawText(text, x1, (bounds.height - textExtent.y) / 2
						+ bounds.y);

				y1 += rowHeight;
				y2 += rowHeight;
			}
		}
	}

	protected void paintCanvas(GC gc) {

		Image image = new Image(null, canvas.getSize().x, canvas.getSize().y);

		GC imageGC = null;

		try {
			imageGC = new GC(image);

			imageGC.setBackground(canvas.getBackground());

			if (backgroundPattern != null) {
				imageGC.setBackgroundPattern(backgroundPattern);
			}

			imageGC.fillRectangle(0, 0, image.getImageData().width, image
					.getImageData().height);

			synchronized (rows) {
				paintRowsBackground(imageGC);
				try {
					paintGridHeader();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					paintGrid(imageGC);
				} catch (Exception e) {
					e.printStackTrace();
				}
				paintRows(imageGC);
				paintLimits(imageGC);
				paintCursor(imageGC);
			}

			gc.drawImage(image, 0, 0);

		} finally {
			image.dispose();

			if (imageGC != null) {
				imageGC.dispose();
			}
		}
	}

	private void paintLimits(GC gridGC) {

		Point point = getPointForTime(getNodesArea(), getStartTime());

		if (point != null) {

			int x1 = point.x;
			int y1 = 0;
			int x2 = point.x;
			int y2 = y1 + getGridArea().height - 1;

			gridGC.setForeground(IMediaConstants.DEFAULT_GRID_FG_COLOR);
			gridGC.setLineStyle(SWT.LINE_SOLID);
			gridGC.drawLine(x1, y1, x2, y2);
		}

		point = getPointForTime(getNodesArea(), getEndTime());
		if (point != null) {

			int x1 = point.x;
			int y1 = 0;
			int x2 = point.x;
			int y2 = y1 + getGridArea().height - 1;

			gridGC.setForeground(IMediaConstants.DEFAULT_GRID_FG_COLOR);
			gridGC.setLineStyle(SWT.LINE_SOLID);
			gridGC.drawLine(x1, y1, x2, y2);
		}
	}

	private void paintCursor(GC gridGC) {

		Point point = getPointForTime(getNodesArea(), getCurrentTime());

		if (point == null) {
			// not visible on current display area
			return;
		}

		int x1 = point.x;
		int y1 = 0;
		int x2 = point.x;
		int y2 = y1 + getGridArea().height - 1;

		gridGC.setForeground(cursorColor);
		gridGC.setLineStyle(SWT.LINE_SOLID);
		gridGC.drawLine(x1, y1, x2, y2);
	}

	private void paintRowsBackground(GC gc) {
		Rectangle bounds = getNodesArea();
		bounds.height = rowHeight;

		synchronized (rows) {
			for (ITimeLineRow row : rows) {

				if (!row.isVisible()) {
					continue;
				}

				((TimeLineRow) row).paintBackground(gc, bounds);

				bounds.y += rowHeight;
			}
		}
	}

	private void paintGridHeader() {
		GC gridHeaderGC = new GC(gridHeaderComposite);
		try {
			paintGridHeader(gridHeaderGC);
		} finally {
			gridHeaderGC.dispose();
		}
	}

	private void paintGridHeader(GC gc) {
		// allow grid label provider to update grid data before startig paint
		// grid header
		if (gridLabelProvider != null) {
			gridLabelProvider.getLabel(getStartTime(), gridData);
		}

		if (gridData.gridHeaderHeight < 1) {
			return;
		}

		Image image = new Image(null, gridHeaderComposite.getSize().x,
				gridHeaderComposite.getSize().y);

		GC imageGC = null;

		try {
			imageGC = new GC(image);

			imageGC.setBackground(gridHeaderComposite.getBackground());
			imageGC.fillRectangle(0, 0, image.getImageData().width, image
					.getImageData().height);

			if (gridData.showGridHeader) {

				if (gridLabelProvider != null
						&& gridLabelProvider.getBackground() != null) {
					imageGC.setBackground(gridLabelProvider.getBackground());
					imageGC.setBackgroundPattern(GRID_HEADER_PATTERN);
					imageGC.fillRectangle(getGridHeaderArea().x
							- gridData.leftGridMargins, getGridHeaderArea().y,
							getGridHeaderArea().width
									+ gridData.leftGridMargins
									+ gridData.rightGridMargins,
							getGridHeaderArea().height);
				}

				long begin = displayData.getDisplayStartTime()
						/ gridData.getMajorGridInterval()
						* gridData.getMajorGridInterval();
				long end = (displayData.getDisplayEndTime()
						/ gridData.getMajorGridInterval() + 1)
						* gridData.getMajorGridInterval();

				for (long i = begin; i <= end; i += gridData
						.getMajorGridInterval()) {

					Point point = getPointForTime(getNodesArea(), i);

					if (point == null) {
						// not visible on current display area
						continue;
					}

					int x1 = getGridHeaderArea().x + point.x
							- gridData.leftGridMargins;
					int y1 = getGridHeaderArea().y;
					int x2 = x1;
					int y2 = y1 + getGridHeaderArea().height - 1;

					// paint label
					if (gridLabelProvider != null) {
						try {
							String text = gridLabelProvider.getLabel(i,
									gridData);
							if (text != null && text.length() > 0) {
								Point textExtent = imageGC.textExtent(text);
								imageGC.drawText(text, x1 - (textExtent.x - 1)
										/ 2, y1 + (y2 - y1 - textExtent.y) / 2,
										true);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					y1 = y2 - 6;

					imageGC.setForeground(gridData.gridColor);
					imageGC.setLineStyle(SWT.LINE_SOLID);
					imageGC.drawLine(x1, y1, x2, y2);
					imageGC.drawLine(x1, getGridHeaderArea().y, x2,
							getGridHeaderArea().y + 6);
				}

				begin = displayData.getDisplayStartTime()
						/ gridData.getMinorGridInterval()
						* gridData.getMinorGridInterval();
				end = (displayData.getDisplayEndTime()
						/ gridData.getMinorGridInterval() + 1)
						* gridData.getMinorGridInterval();

				for (long i = begin; i <= end; i += gridData
						.getMinorGridInterval()) {

					Point point = getPointForTime(getNodesArea(), i);

					if (point == null) {
						// not visible on current display area
						continue;
					}

					int x1 = getGridHeaderArea().x + point.x
							- gridData.leftGridMargins;
					int y1 = getGridHeaderArea().y;
					int x2 = x1;
					int y2 = y1 + getGridHeaderArea().height - 1;

					// paint label
					if (gridLabelProvider != null) {
						try {
							String text = gridLabelProvider.getLabel(i,
									gridData);
							if (text != null && text.length() > 0) {
								Point textExtent = imageGC.textExtent(text);
								imageGC.drawText(text, x1 - (textExtent.x - 1)
										/ 2, y1 + (y2 - y1 - textExtent.y) / 2,
										true);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					y1 = y2 - 4;

					imageGC.setForeground(gridData.gridColor);
					imageGC.setLineStyle(SWT.LINE_SOLID);
					imageGC.drawLine(x1, y1, x2, y2);
					imageGC.drawLine(x1, getGridHeaderArea().y, x2,
							getGridHeaderArea().y + 4);
				}

				imageGC.setLineStyle(SWT.LINE_SOLID);
				imageGC.drawLine(getGridHeaderArea().x, getGridHeaderArea().y
						+ getGridHeaderArea().height - 1, getGridHeaderArea().x
						+ getGridHeaderArea().width - 1, getGridHeaderArea().y
						+ getGridHeaderArea().height - 1);

				// paint cursor header
				Point point = getPointForTime(getNodesArea(), getCurrentTime());
				if (point != null) {
					imageGC.setBackground(cursorColor);
					imageGC.setAlpha(CURSOR_ALPHA_VALUE);
					imageGC.fillRectangle(getGridHeaderArea().x + point.x - 1
							- gridData.leftGridMargins, getGridHeaderArea().y,
							3, getGridHeaderArea().height - 1);
				}
			}

			gc.drawImage(image, 0, 0);
		} finally {
			image.dispose();

			if (imageGC != null) {
				imageGC.dispose();
			}
		}
	}

	private void paintGrid(GC gc) {
		if (!gridData.showGrid) {
			return;
		}

		long begin = displayData.getDisplayStartTime()
				/ gridData.getMajorGridInterval()
				* gridData.getMajorGridInterval();
		long end = (displayData.getDisplayEndTime()
				/ gridData.getMajorGridInterval() + 1)
				* gridData.getMajorGridInterval();

		for (long i = begin; i <= end; i += gridData.getMajorGridInterval()) {

			Point point = getPointForTime(getNodesArea(), i);

			if (point == null) {
				// not visible on current display area
				continue;
			}

			int x1 = point.x;
			int y1 = getGridArea().y;
			int x2 = point.x;
			int y2 = y1 + getGridArea().height - 1;

			gc.setForeground(gridData.gridColor);
			gc.setLineStyle(SWT.LINE_DOT);
			gc.drawLine(x1, y1, x2, y2);
		}
	}

	protected void paintRows(GC gc) {
		Rectangle bounds = getNodesArea();
		bounds.height = rowHeight;

		synchronized (rows) {
			for (ITimeLineRow row : rows) {

				if (!row.isVisible()) {
					continue;
				}

				((TimeLineRow) row).paint(gc, bounds);

				bounds.y += rowHeight;
			}
		}
	}

	long getTimeForPoint(Point point) {

		long displayWidthInTime = displayData.getDisplayWidthInTime();
		long displayWidthInPixels = getNodesArea().width;

		double oneTimePeriodPixel = ((double) displayWidthInPixels)
				/ ((double) displayWidthInTime);

		long time = (long) (point.x / oneTimePeriodPixel)
				+ displayData.getDisplayStartTime();

		return time;
	}

	Point getPointForTime(Rectangle bounds, long time) {
		if (time < displayData.getDisplayStartTime()
				|| time > displayData.getDisplayEndTime()) {
			return null;
		}

		long displayWidthInTime = displayData.getDisplayWidthInTime();
		long displayWidthInPixels = bounds.width;

		long relativeTime = time - displayData.getDisplayStartTime();

		double onePixelTime = ((double) displayWidthInTime)
				/ ((double) displayWidthInPixels);

		int pixels = (int) (relativeTime / onePixelTime);

		return new Point(bounds.x + pixels, bounds.y + bounds.height / 2);
	}

	int drawCnt = 0;

	public void repaint() {
		if (drawCnt > 0) {
			return;
		}
		drawCnt++;
		Runnable run = new Runnable() {
			public void run() {
				drawCnt = 0;
				updateButtons();
				updateSlider();
				if (!canvas.isDisposed()) {
					canvas.redraw();
				}
			}
		};
		if (Display.getCurrent() != null) {
			run.run();
		} else {
			Display.getDefault().asyncExec(run);
		}
	}

	public void repaintAll() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!isDisposed()) {
					updateButtons();
					updateSlider();
					redraw(0, 0, getSize().x - 1, getSize().y - 1, true);
				}
			}
		});
	}

	protected void updateButtons() {
		if (!resumeButton.isDisposed()) {
			resumeButton.setEnabled(!((Timer) getTimer()).isRunning());
		}
		if (!suspendButton.isDisposed()) {
			suspendButton.setEnabled(((Timer) getTimer()).isRunning());
		}
		if (!cancelButton.isDisposed()) {
			cancelButton.setEnabled(((Timer) getTimer()).isRunning()
					|| getCurrentTime() > getStartTime());
		}
	}

	protected void updateSlider() {
		if (slider != null && !slider.isDisposed()) {

			long endTime = getEndTime();

			int min = (int) startTime;
			int max = (int) (Math.max(Math.max(displayData.getDisplayEndTime()
					- displayData.getDisplayWidthInTime(), endTime
					- displayData.getDisplayWidthInTime() / 4 * 3),
					getCurrentTime() - displayData.getDisplayWidthInTime() / 4
							* 3));

			slider.setMinimum(min);
			slider.setMaximum(max + slider.getThumb());
			slider.setIncrement((int) displayData.getDisplayWidthInTime() / 4);
			slider.setPageIncrement((int) displayData.getDisplayWidthInTime());

			slider.setEnabled(displayData.getDisplayStartTime() < max
					|| displayData.getDisplayStartTime() > min);

			if (slider.getSelection() != displayData.getDisplayStartTime()) {
				slider.setSelection((int) displayData.getDisplayStartTime());
			}

			slider.redraw();
		}
	}

	public void setBackgroundPattern(Pattern backgroundPattern) {
		if (this.backgroundPattern != null) {
			this.backgroundPattern.dispose();
		}
		this.backgroundPattern = backgroundPattern;
	}

	public long getEndTime() {
		if (endTime == 0) {
			// calculate end time from row nodes
			synchronized (rows) {
				long calcTime = 0;
				for (ITimeLineRow row : rows) {
					ITimeLineNode[] nodes = row.getNodes();
					for (ITimeLineNode node : nodes) {
						calcTime = Math.max(calcTime, node.getEndTime());
					}
				}
				return calcTime;
			}
		} else {
			return endTime;
		}
	}

	public long getStartTime() {
		return startTime;
	}

	public int getRowHeight() {
		return rowHeight;
	}

	public void setRowHeight(int lineHeight) {
		this.rowHeight = lineHeight;
	}

	public void setGridLabelProvider(ITimeLineGridLabelProvider provider) {
		this.gridLabelProvider = provider;
	}

	public long getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(long currentTime) {

		if (currentTime < 0) {
			currentTime = 0;
		}

		// long endTime = getEndTime();
		// if (currentTime > endTime) {
		// currentTime = endTime;
		// }
		//		
		this.currentTime = currentTime;

		if (getCurrentControlPoint() != null
				&& getCurrentControlPoint().getTime() != currentTime) {
			setSelectedControlPoint(null);
		}

		if (currentTimeLabel != null) {
			if (timeLabelProvider != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (!currentTimeLabel.isDisposed()) {
							currentTimeLabel.setText(timeLabelProvider
									.getLabel(TimeLine.this.getCurrentTime()));
							currentTimeLabel.redraw();
						}
					};
				});
			}
		}

		synchronized (rows) {
			for (ITimeLineRow row : rows) {
				((TimeLineRow) row).notifyTimeListeners(this.currentTime);
			}
		}

		notifyTimeListeners();
	}

	public void setCursorColor(Color cursorColor) {
		this.cursorColor = cursorColor;
	}

	public ITimer getTimer() {
		return timer;
	}

	@Override
	public void dispose() {
		timer.destroy();
		notifier.terminate();
		super.dispose();
	}

	public void addTimeListener(ITimeListener listener) {
		synchronized (timeListeners) {
			timeListeners.add(listener);
		}
	}

	public void removeTimeListener(ITimeListener listener) {
		synchronized (timeListeners) {
			timeListeners.remove(listener);
		}
	}

	protected void notifyTimeListeners() {
		notifier.notifyListeners(getCurrentTime());
	}

	public void setTimeLabelProvider(ITimeLabelProvider timeLabelProvider) {
		this.timeLabelProvider = timeLabelProvider;
	}

	public class Timer implements ITimer {

		private TimerThread timerThread = new TimerThread();

		public Timer() {
			timerThread.start();
		}

		public void start() {
			if (getCurrentTime() < displayData.getDisplayStartTime()
					|| getCurrentTime() > displayData.getDisplayEndTime()) {
				displayData.setDisplayStartTime(getCurrentTime());
				repaint();
			}
			timerThread.startTimer();
		}

		public void stop() {
			timerThread.stopTimer();
		}

		public boolean isRunning() {
			return !timerThread.stop;
		}

		public void setTime(long time) {
			setCurrentTime(time);
			repaint();
		}

		public void setSpeed(long timeInMilis, long timeIncrement) {
			timerThread.speed = timeInMilis;
			timerThread.increment = timeIncrement;
		}

		protected void destroy() {
			timerThread.terminate();
		}

		public void setAutorepeat(boolean autorepeat) {
			timerThread.setAutorepeat(autorepeat);
		}
	}

	private class TimerThread extends Thread {

		private static final int DEFAULT_SLEEP_TIME = 100;

		private static final int STOP_SLEEP_TIME = 100;

		long speed = 1000;

		long increment = 1;

		boolean stop = true;

		boolean terminate = false;

		boolean autorepeat = true;

		private TimerThread() {
			setName("TimerThread"); //$NON-NLS-1$
			setDaemon(true);
			setPriority(Thread.MAX_PRIORITY);
		}

		@Override
		public void run() {
			long sleepTime;
			while (!terminate) {
				try {
					if (!stop) {
						sleepTime = speed == 0 ? DEFAULT_SLEEP_TIME : speed;
						sleep(sleepTime);

						long newCurTime = getCurrentTime() + increment;
						long endTime = getEndTime();

						if (getCurrentTime() >= endTime) {
							if (autorepeat) {
								setCurrentTime(getStartTime());
							} else {
								stopTimer();
							}
						} else {
							setCurrentTime(Math.min(newCurTime, endTime));
						}

						if (getCurrentTime() < displayData
								.getDisplayStartTime()
								|| getCurrentTime() > displayData
										.getDisplayEndTime()) {
							displayData.setDisplayStartTime(getCurrentTime());
						}

						repaint();
					} else {
						sleep(STOP_SLEEP_TIME);
					}
				} catch (InterruptedException e) {
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void terminate() {
			terminate = true;

			timer.timerThread.interrupt();
		}

		public void startTimer() {
			if (speed == 0 || increment == 0) {
				return;
			}

			stop = false;

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					updateButtons();
				};
			});
		}

		public void stopTimer() {
			stop = true;

			timer.timerThread.interrupt();

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					updateButtons();
				};
			});
		}

		public void setAutorepeat(boolean autorepeat) {
			this.autorepeat = autorepeat;
		}
	}

	public class TimeLineGridData implements IGridSettings {

		protected boolean showGrid = true;

		protected boolean showGridHeader = true;

		protected long gridMajorInterval;

		protected long gridMinorInterval;

		protected int gridHeaderHeight = 26;

		protected int leftGridMargins = 10;

		protected int rightGridMargins = 10;

		protected Color gridColor = IMediaConstants.DEFAULT_GRID_COLOR;

		public long getMajorGridInterval() {
			if (gridMajorInterval == 0) {
				return DEFAULT_GRID_MAJOR_INTERVAL;
			}
			return gridMajorInterval;
		}

		public void setGridMinorInterval(long minorTime) {
			gridMinorInterval = minorTime;
		}

		public void setGridMajorInterval(long majorTime) {
			gridMajorInterval = majorTime;
		}

		public long getMinorGridInterval() {
			if (gridMinorInterval == 0) {
				return DEFAULT_GRID_MINOR_INTERVAL;
			}
			return gridMinorInterval;
		}

		public IDisplaySettings getDisplayData() {
			return displayData;
		}

		public void setShowGrid(boolean showGrid) {
			this.showGrid = showGrid;
		}

		public void setShowGridHeader(boolean showGridHeader) {
			this.showGridHeader = showGridHeader;
		}

		public void setGridInterval(long majorTime, long minorTime) {
			setGridMajorInterval(majorTime);
			setGridMinorInterval(minorTime);
		}

		public void setGridMargins(int left, int right, int headerHeight) {
			this.leftGridMargins = left;
			this.rightGridMargins = right;
			this.gridHeaderHeight = headerHeight;

			((GridData) gridHeaderComposite.getLayoutData()).heightHint = this.gridHeaderHeight;
			((GridData) gridHeaderComposite.getLayoutData()).minimumHeight = this.gridHeaderHeight;
		}

		public Color getGridColor() {
			return gridColor;
		}

		public void setGridColor(Color gridColor) {
			this.gridColor = gridColor;
		}

		public boolean isShowGrid() {
			return showGrid;
		}

		public boolean isShowGridHeader() {
			return showGridHeader;
		}

	}

	public class DisplayData implements IDisplaySettings {

		private long displayStartTime;

		private long displayWidthInTime;

		public DisplayData() {
		}

		public long getDisplayStartTime() {
			return displayStartTime;
		}

		public void setDisplayStartTime(long startTime) {
			displayStartTime = startTime;
		}

		public long getDisplayEndTime() {
			return getDisplayStartTime() + getDisplayWidthInTime();
		}

		public long getDisplayWidthInTime() {
			if (displayWidthInTime == 0) {
				return DEFAULT_DISPLAY_WIDTH;
			}
			return displayWidthInTime;
		}

		public void setDisplayWidthInTime(long width) {
			displayWidthInTime = width;
		}

		public long getDisplayWidthInPixels() {
			if (canvas != null && !canvas.isDisposed()) {
				return canvas.getSize().x;
			} else {
				return 0;
			}
		}
	}

	private class TimeChangedNotifierRunnable implements Runnable {

		boolean terminate = false;

		Object mutex = new Object();

		long actualTime;

		public void run() {
			Thread.currentThread().setName("TimeChangedNotifierThread"); //$NON-NLS-1$
			long time = 0;
			while (!terminate) {
				synchronized (mutex) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (!terminate) {
					while (actualTime != -1) {
						time = actualTime;
						actualTime = -1;
						synchronized (timeListeners) {
							for (ITimeListener listener : timeListeners) {
								try {
									listener.timeChanged(time);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}

		public void terminate() {
			synchronized (mutex) {
				terminate = true;
				mutex.notify();
			}
		}

		public void notifyListeners(long newTime) {
			synchronized (mutex) {
				actualTime = newTime;
				mutex.notify();
			}
		}
	}

	public IDisplaySettings getDisplayData() {
		return displayData;
	}

	public IGridSettings getGridData() {
		return gridData;
	}

	public IControlPoint getSelectedControlPoint() {
		return this.selectedControlPoint;
	}

	void setSelectedControlPoint(IControlPoint controlPoint) {
		this.selectedControlPoint = controlPoint;
	}

	void setSelectedRow(ITimeLineRow row) {
		this.selectedRow = row;
	}

	public ITimeLineRow getSelectedRow() {
		return selectedRow;
	}

	void setSelectedNode(ITimeLineNode node) {
		this.selectedNode = node;
	}

	public ITimeLineNode getSelectedNode() {
		return selectedNode;
	}

	public void addSelectionListener(ISelectionListener listener) {
		synchronized (selectionListeners) {
			selectionListeners.add(listener);
		}
	}

	public void removeSelectionListener(ISelectionListener listener) {
		synchronized (selectionListeners) {
			selectionListeners.remove(listener);
		}
	}

	public void notifySelectionListeners() {
		if (selectionListeners.size() == 0) {
			return;
		}

		List<Object> selection = new ArrayList<Object>();
		if (selectedControlPoint != null) {
			selection.add(selectedControlPoint);
		}
		if (selectedNode != null) {
			selection.add(selectedNode);
		}
		if (selectedRow != null) {
			selection.add(selectedRow);
		}

		final IStructuredSelection ssel = new StructuredSelection(selection);

		ExecutionThread.INSTANCE.execute(new Runnable() {
			public void run() {
				synchronized (selectionListeners) {
					for (ISelectionListener listener : selectionListeners) {

						listener.selectionChanged(ssel);
					}
				}
			};
		});
	}

	public ToolBar getTopToolBar() {
		return topToolBar;
	}

	public Composite getBottomComposite() {
		return bottomComposite;
	}

	public ITimeLabelProvider getTimeLabelProvider() {
		return timeLabelProvider;
	}

	class InputDialogWithContext extends InputDialog {

		public InputDialogWithContext(Shell parentShell, String dialogTitle,
				String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue,
					validator);

		}

		@Override
		protected Control createDialogArea(Composite parent) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
					TimeLine.ZOOM_SET_DISPLAY_WIDTH_MENU_ITEM_CONTEXT);
			return super.createDialogArea(parent);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.timeline.ITimeLine#getViewer()
	 */
	public Viewer getViewer() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.timeline.ITimeLine#getCurrentControlPoint()
	 */
	public IControlPoint getCurrentControlPoint() {
		return selectedControlPoint;
	}

	public ITimeLineDataProvider getDataProvider() {
		return dataProvider;
	}

	public void zoomIn() {
		if (dataProvider.getZoomProvider() != null) {
			dataProvider.getZoomProvider().zoomIn(displayData);
		} else {
			displayData.setDisplayWidthInTime(displayData
					.getDisplayWidthInTime() / 2);
		}
		repaint();
	}

	public void zoomOut() {
		if (dataProvider.getZoomProvider() != null) {
			dataProvider.getZoomProvider().zoomOut(displayData);
		} else {
			displayData.setDisplayWidthInTime(displayData
					.getDisplayWidthInTime() * 2);
		}
		repaint();
	}
}