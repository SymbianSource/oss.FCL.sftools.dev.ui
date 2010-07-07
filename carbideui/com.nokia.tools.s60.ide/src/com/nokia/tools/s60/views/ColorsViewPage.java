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
package com.nokia.tools.s60.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentDelta;
import com.nokia.tools.content.core.IContentListener;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroupItem;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorGroupsStore;
import com.nokia.tools.s60.editor.actions.ColorizeColorGroupAction;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.SearchViewPage.ElementTableItem;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.propertysheet.color.ColorChangedLabelWrapper;
import com.nokia.tools.screen.ui.propertysheet.color.ColorPickerComposite;
import com.nokia.tools.screen.ui.propertysheet.color.DraggedColorObject;
import com.nokia.tools.screen.ui.propertysheet.color.IColorPickerListener;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;
import com.nokia.tools.ui.tooltip.CompositeInformationControl;
import com.nokia.tools.ui.tooltip.CompositeTooltip;

public class ColorsViewPage extends Page implements ISelectionListener,
		IContentListener, Observer {

	private static final Image CopyGroupImage = UtilsPlugin.getImageDescriptor(
			"icons/Add.png").createImage();

	private static final Image DeleteGroupImage = UtilsPlugin
			.getImageDescriptor("icons/Remove.png").createImage();

	// private static final Pattern BG_PATTERN = new Pattern(null, 0, 0, 1, 1,
	// ColorConstants.white, ColorConstants.lightGray);

	private static final Font ItalicFont;

	private static final int rowHeight = 18;

	private static final int QuadrupleColumnWidth = 14;

	private static final Color fgTextColor = ColorConstants.black;

	// private static final Color bgColor = ColorConstants.menuBackground;

	private static final int colorBoxSizeOuter = 12;

	private static final int colorBoxSizeMiddle = 10;

	private static final int colorBoxSizeInner = 8;

	private static final int colorBoxRoundAngle = 2;

	protected static final int EDITCOLUMN = 0;

	protected static final int COPYCOLUMN = 6;

	protected static final int DELETECOLUMN = 7;

	protected static final int FIRST_QUADRUPLE_COLUMN = 1;

	protected static final int LAST_QUADRUPLE_COLUMN = 4;

	private Composite table;

	private List<ColorQuadruple> inputs;

	private Composite composite;

	private ScrolledComposite scrolledComposite;

	private IEditorPart sourceEditor;

	static {
		FontRegistry fr = new FontRegistry();
		FontData[] fd = fr.defaultFont().getFontData();
		fd[0].setStyle(SWT.ITALIC);
		ItalicFont = new Font(null, fd);
	}

	public IEditorPart getSourceEditor() {
		return sourceEditor;
	}

	public ColorsViewPage(IEditorPart sourceEd) {
		this.sourceEditor = sourceEd;
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService().addSelectionListener(this);
		ColorGroups grps = getColorGroups();
		if (grps != null) {
			grps.addObserver(this);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService().removeSelectionListener(this);
		ColorGroups grps = getColorGroups();
		if (grps != null) {
			grps.deleteObserver(this);
		}
	}

	protected CommandStack getCommandStack() {
		return (CommandStack) sourceEditor.getAdapter(CommandStack.class);
	}

	@Override
	public void createControl(Composite parent) {
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL
				| SWT.V_SCROLL);

		composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		GridLayout gl = new GridLayout();
		gl.marginWidth = gl.marginHeight = 0;
		gl.marginBottom = 2;
		composite.setLayout(gl);

		createCompositeArea(composite);

		scrolledComposite.setContent(composite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.getHorizontalBar().setIncrement(60);
		scrolledComposite.getVerticalBar().setIncrement(40);
		scrolledComposite.getHorizontalBar().setPageIncrement(
				scrolledComposite.getHorizontalBar().getIncrement() * 2);
		scrolledComposite.getVerticalBar().setPageIncrement(
				scrolledComposite.getVerticalBar().getIncrement() * 2);

		Point minSize = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledComposite.setMinSize(minSize);

		initializeIconViewActions();
	}

	private void initializeIconViewActions() {
		IActionBars bars = getSite().getActionBars();
		IToolBarManager tbm = bars.getToolBarManager();

		tbm.removeAll();

		tbm.add(new AddNewColorGroupAction());
	}

	@Override
	public Control getControl() {
		return scrolledComposite;
	}

	@Override
	public void setFocus() {
	}

	public void fillTable() {
		for (ColorQuadruple input : inputs) {
			if (!table.isDisposed()) {
				final Composite labelComp = new Composite(table, SWT.NONE);
				GridLayout gl = new GridLayout();
				gl.marginWidth = gl.marginHeight = gl.horizontalSpacing = gl.verticalSpacing = 0;
				labelComp.setLayout(gl);
				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.grabExcessHorizontalSpace = true;
				gd.horizontalSpan = 3;
				gd.verticalIndent = 5;
				labelComp.setLayoutData(gd);
				labelComp.setBackground(labelComp.getParent().getBackground());

				final Text label = new Text(labelComp, SWT.NONE);
				label.setEditable(false);
				label.setData(input);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.grabExcessHorizontalSpace = true;
				gd.grabExcessVerticalSpace = true;
				label.setLayoutData(gd);
				label.setFont(ItalicFont);

				label.setText(input.getQuadrupleName());

				label.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent e) {
						if (e.button == 1) {
							label.setFont(null);
							label.setEditable(true);
							labelComp.setBackground(ColorConstants.white);
						}
					}
				});
				
				label.addMouseTrackListener(new MouseTrackListener() {
					public void mouseEnter(MouseEvent e) {
						
						
					}

					public void mouseExit(MouseEvent e) {
						label.setFont(ItalicFont);
						label.setEditable(false);
						// this will call the focus lost event
						label.setEnabled(false);
						if (!label.isDisposed()) {
							label.setEnabled(true);
							labelComp.setBackground(labelComp.getParent()
									.getBackground());
						}
					}

					public void mouseHover(MouseEvent e) {
						
						
					}
				});
				
				label.addTraverseListener(new TraverseListener() {
					public void keyTraversed(TraverseEvent e) {
						if (e.detail == SWT.TRAVERSE_ESCAPE
								|| e.detail == SWT.TRAVERSE_RETURN) {
							ColorQuadruple quad = (ColorQuadruple) label
									.getData();

							if (e.detail == SWT.TRAVERSE_ESCAPE) {
								label.setText(quad.getQuadrupleName());
							} else if (e.detail == SWT.TRAVERSE_RETURN) {
								//the focus lost will happen
								// after this
								// if (!updateName(quad, label.getText())) {
								// label.setText(quad.getQuadrupleName());
								// }
							}

							label.setFont(ItalicFont);
							label.setEditable(false);
							// this will call the focus lost event
							label.setEnabled(false);
							if (!label.isDisposed()) {
								label.setEnabled(true);
								labelComp.setBackground(labelComp.getParent()
										.getBackground());
							}
						}
					}
				});

				label.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						label.setFont(ItalicFont);
						label.setEditable(false);
						label.setEnabled(false);
						label.setEnabled(true);
						labelComp.setBackground(labelComp.getParent()
								.getBackground());
						ColorQuadruple quad = (ColorQuadruple) label.getData();
						if (!updateName(quad, label.getText())) {
							label.setText(quad.getQuadrupleName());
						}
					}

					@Override
					public void focusGained(FocusEvent e) {
						label.setFont(null);
						label.setEditable(true);
						labelComp.setBackground(ColorConstants.white);
					}
				});

				final Canvas quadrupleComposite = new Canvas(table, SWT.BORDER);
				quadrupleComposite.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						GC gc = e.gc;
						gc.setBackground(ColorConstants.white);
						// gc.setBackgroundPattern(BG_PATTERN);
						gc.fillRectangle(0, 0, quadrupleComposite.getSize().x,
								quadrupleComposite.getSize().y);
					}
				});
				gl = new GridLayout(4, false);
				gl.marginWidth = 2;
				gl.marginHeight = gl.horizontalSpacing = gl.verticalSpacing = 0;
				quadrupleComposite.setLayout(gl);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.heightHint = rowHeight;
				gd.verticalAlignment = SWT.CENTER;
				quadrupleComposite.setLayoutData(gd);

				Canvas q1 = new Canvas(quadrupleComposite, SWT.NONE);
				q1.setData(input);
				gd = new GridData();
				gd.grabExcessVerticalSpace = true;
				gd.widthHint = QuadrupleColumnWidth;
				gd.heightHint = rowHeight;
				gd.verticalAlignment = SWT.CENTER;
				q1.setLayoutData(gd);
				q1.setBackground(q1.getParent().getBackground());
				addPaintListener(0, q1);
				addDragSource(0, q1);
				addQuadrupleMouseListener(0, q1);
				createTooltip(0, q1);

				Canvas q2 = new Canvas(quadrupleComposite, SWT.NONE);
				q2.setData(input);
				gd = new GridData();
				gd.grabExcessVerticalSpace = true;
				gd.widthHint = QuadrupleColumnWidth;
				gd.heightHint = rowHeight;
				gd.verticalAlignment = SWT.CENTER;
				gd.horizontalIndent = 6;
				q2.setLayoutData(gd);
				q2.setBackground(q2.getParent().getBackground());
				addPaintListener(1, q2);
				addDragSource(1, q2);
				addQuadrupleMouseListener(1, q2);
				createTooltip(1, q2);

				Canvas q3 = new Canvas(quadrupleComposite, SWT.NONE);
				q3.setData(input);
				gd = new GridData();
				gd.grabExcessVerticalSpace = true;
				gd.widthHint = QuadrupleColumnWidth;
				gd.heightHint = rowHeight;
				gd.verticalAlignment = SWT.CENTER;
				q3.setLayoutData(gd);
				q3.setBackground(q3.getParent().getBackground());
				addPaintListener(2, q3);
				addDragSource(2, q3);
				addQuadrupleMouseListener(2, q3);
				createTooltip(2, q3);

				Canvas q4 = new Canvas(quadrupleComposite, SWT.NONE);
				q4.setData(input);
				gd = new GridData();
				gd.grabExcessVerticalSpace = true;
				gd.widthHint = QuadrupleColumnWidth;
				gd.heightHint = rowHeight;
				gd.verticalAlignment = SWT.CENTER;
				q4.setLayoutData(gd);
				q4.setBackground(q4.getParent().getBackground());
				addPaintListener(3, q4);
				addDragSource(3, q4);
				addQuadrupleMouseListener(3, q4);
				createTooltip(3, q4);

				final Button b1 = new Button(table, SWT.FLAT);
				b1.setData(input);
				gd = new GridData();
				gd.grabExcessHorizontalSpace = false;
				gd.heightHint = 20;
				gd.widthHint = 20;
				gd.verticalAlignment = SWT.CENTER;
				b1.setLayoutData(gd);
				b1.setBackground(b1.getParent().getBackground());
				b1.setImage(CopyGroupImage);
				b1.setToolTipText("Copy Color Group as a new Group");

				b1.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						CommandStack stck = (CommandStack) sourceEditor
								.getAdapter(CommandStack.class);

						Command cmd = new Command() {
							@Override
							public boolean canExecute() {
								return true;
							}

							@Override
							public boolean canUndo() {
								return false;
							}

							@Override
							public void execute() {
								ColorQuadruple quad = (ColorQuadruple) b1
										.getData();
								ColorQuadruple newQuad = new ColorQuadruple(
										"copy of " + quad.getQuadrupleName());
								ColorGroups grps = getColorGroups();

								int idx = 1;
								while (grps.getGroupByName(newQuad
										.getQuadrupleName()) != null) {
									idx++;
									newQuad = new ColorQuadruple("copy (" + idx
											+ ") of " + quad.getQuadrupleName());
								}

								for (int i = 0; i < quad.getRgbList().size(); i++) {
									RGB rgb = quad.getRgbList().get(i);
									// (add group notifies this observer about
									// adding
									// group)
									if (i == 0) {
										if (grps.addGroup(newQuad
												.getQuadrupleName(), rgb) == true) {
											// newQuad.addRGB(rgb, false);
											// inputs.add(newQuad);
										}
									} else {
										String newName = newQuad
												.getQuadrupleName()
												+ " tone" + i;
										if (grps.addGroup(newName, rgb) == true) {
											// newQuad.addRGB(rgb, false);
											ColorGroup added = grps
													.getGroupByName(newName);
											added.setParentGroupName(newQuad
													.getQuadrupleName());
										}
									}
								}
							}
						};

						if (stck != null) {
							stck.execute(cmd);
						} else {
							cmd.execute();
						}
					}
				});

				final Button b2 = new Button(table, SWT.FLAT);
				b2.setData(input);
				gd = new GridData();
				gd.grabExcessHorizontalSpace = false;
				gd.heightHint = 20;
				gd.widthHint = 20;
				gd.verticalAlignment = SWT.CENTER;
				b2.setLayoutData(gd);
				b2.setBackground(b2.getParent().getBackground());
				b2.setImage(DeleteGroupImage);
				b2.setToolTipText("Delete Color Group");

				b2.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						CommandStack stck = (CommandStack) sourceEditor
								.getAdapter(CommandStack.class);

						Command cmd = new Command() {
							@Override
							public boolean canExecute() {
								return true;
							}

							@Override
							public boolean canUndo() {
								return false;
							}

							@Override
							public void execute() {
								ColorQuadruple quad = (ColorQuadruple) b2
										.getData();
								IBrandingManager branding = BrandingExtensionManager
										.getBrandingManager();
								Image image = null;
								if (branding != null) {
									image = branding.getIconImageDescriptor()
											.createImage();
								}
								MessageDialog dialog = new MessageDialog(
										PlatformUI.getWorkbench().getDisplay()
												.getActiveShell(),
										ViewMessages.RefColors_Delete_MsgBox_Title,
										image,
										MessageFormat
												.format(
														ViewMessages.RefColors_Delete_MsgBox_Message,
														new Object[] { quad
																.getQuadrupleName() }),
										3, new String[] {
												IDialogConstants.YES_LABEL,
												IDialogConstants.NO_LABEL }, 1);
								dialog.create();
								image.dispose();
								if (dialog.open() == 0) {
									inputs.remove(quad);
									ColorGroups grps = getColorGroups();
									grps.removeGroup(quad.getQuadrupleName());
									for (int i = 1; i <= 3; i++) {
										// (remove group notifies this observer
										// about group
										// removal
										grps.removeGroup(quad
												.getQuadrupleName()
												+ " tone" + i);
									}

								}
							}
						};

						if (stck != null) {
							stck.execute(cmd);
						} else {
							cmd.execute();
						}
					}
				});
			}
		}
	}

	private boolean updateName(final ColorQuadruple quad, final String newName) {
		String oldName = quad.getQuadrupleName();
		if (oldName.equals(newName)) {
			return false;
		}
		for (ColorGroup cg : getColorGroups().getGroups()) {
			if (cg.getName().equals(newName)) {
								
				MessageDialog.openError(getControl().getShell(),
						ViewMessages.RefColors_Duplicate_Name_Title, NLS.bind(
								ViewMessages.RefColors_Duplicate_Name_Message,
								newName));
				return false;
			}
		}
		CommandStack stck = (CommandStack) sourceEditor
				.getAdapter(CommandStack.class);

		Command cmd = new Command() {
			String oldName;

			@Override
			public boolean canExecute() {
				return true;
			}

			@Override
			public boolean canUndo() {
				return oldName != null;
			}

			@Override
			public void execute() {
				redo();
			}

			@Override
			public String getLabel() {
				return "Set color group name";
			}

			@Override
			public void undo() {
				if (!oldName.equals(newName)) {
					ColorGroups grps = getColorGroups();
					if (grps.modifyGroupName(newName, oldName)) {
						quad.setQuadrupleName(oldName);
						for (int i = 1; i < quad.getRgbList().size(); i++) {
							grps.modifyGroupName(newName + " tone" + i, quad
									.getGroupName(i - 1));
						}
					}
				}
			}

			@Override
			public void redo() {
				oldName = quad.getQuadrupleName();
				if (!oldName.equals(newName)) {
					ColorGroups grps = getColorGroups();
					if (grps.modifyGroupName(oldName, newName)) {
						quad.setQuadrupleName(newName);
						for (int i = 1; i < quad.getRgbList().size(); i++) {
							grps.modifyGroupName(oldName + " tone" + i, newName
									+ " tone" + i);
						}
					}
				}
			}
		};

		if (stck != null) {
			stck.execute(cmd);
		} else {
			cmd.execute();
		}
		return true;
	}

	private void addPaintListener(final int qIndex, Control control) {
		PaintListener paintListener = new PaintListener() {
			public void paintControl(org.eclipse.swt.events.PaintEvent event) {
				GC gc = event.gc;
				gc.setForeground(fgTextColor);
				// gc.setBackground(bgColor);
				gc.setBackground(ColorConstants.white);
				// gc.setBackgroundPattern(BG_PATTERN);
				gc.fillRectangle(0, 0, table.getSize().x, table.getSize().y);

				ColorQuadruple paintedQuadruple = (ColorQuadruple) event.widget
						.getData();
				Object[] array = paintedQuadruple.getRgbList().toArray();
				RGB[] rgbArray = new RGB[array.length];
				for (int i = 0; i < array.length; i++) {
					rgbArray[i] = (RGB) array[i];
				}

				Rectangle bounds = new Rectangle(0, 0, ((Control) event.widget)
						.getSize().x, ((Control) event.widget).getSize().y);

				if (qIndex == 0) {
					RGB rgb = null;
					if (paintedQuadruple.getRgbList().size() > 0) {
						rgb = paintedQuadruple.getRgbList().get(0);
						if (paintedQuadruple.isLinked(0)) {
							drawOval(gc, bounds, rgb);
						} else {
							drawBox(gc, bounds, rgb);
						}
					} else {
						drawUnusedBox(gc, bounds, rgb);
					}
				} else if (qIndex == 1) {
					RGB rgb = null;
					if (paintedQuadruple.getRgbList().size() > 1) {
						rgb = paintedQuadruple.getRgbList().get(1);
						if (paintedQuadruple.isLinked(1)) {
							drawOval(gc, bounds, rgb);
						} else {
							drawBox(gc, bounds, rgb);
						}
					} else {
						drawUnusedBox(gc, bounds, rgb);
					}
				} else if (qIndex == 2) {
					RGB rgb = null;
					if (paintedQuadruple.getRgbList().size() > 2) {
						rgb = paintedQuadruple.getRgbList().get(2);
						if (paintedQuadruple.isLinked(2)) {
							drawOval(gc, bounds, rgb);
						} else {
							drawBox(gc, bounds, rgb);
						}
					} else {
						drawUnusedBox(gc, bounds, rgb);
					}
				} else if (qIndex == 3) {
					RGB rgb;
					if (paintedQuadruple.getRgbList().size() > 3) {
						rgb = paintedQuadruple.getRgbList().get(3);
						if (paintedQuadruple.isLinked(3)) {
							drawOval(gc, bounds, rgb);
						} else {
							drawBox(gc, bounds, rgb);
						}
					} else {
						rgb = new RGB(240, 240, 240);
						drawUnusedBox(gc, bounds, rgb);
					}
				}
			}
		};

		control.addPaintListener(paintListener);
	}

	private void addDragSource(final int qIndex, Control control) {
		int operations = DND.DROP_COPY;
		DragSource dragSource = new DragSource(control, operations);
		Transfer[] types = new Transfer[] { LocalSelectionTransfer
				.getInstance() };
		dragSource.setTransfer(types);
		dragSource.addDragListener(new DragSourceListener() {

			Object draggedData = null;

			private int draggedDetail;

			public void dragFinished(DragSourceEvent event) {

			}

			public void dragSetData(DragSourceEvent event) {
				if (LocalSelectionTransfer.getInstance().isSupportedType(
						event.dataType)
						&& draggedData != null) {
					event.data = draggedData;
					event.detail = draggedDetail;
				}

			}

			/**
			 * Select colorgroup for dragging
			 * 
			 * @param event
			 */
			public void dragStart(DragSourceEvent event) {
				DragSource ds = (DragSource) event.widget;
				Control t = ds.getControl();

				ColorQuadruple quad = (ColorQuadruple) t.getData();
				DraggedColorObject object = new DraggedColorObject();

				if (qIndex >= quad.getRgbList().size() || qIndex == -1) {
					
					event.doit = false;
					return;
				}
				object.setName(quad.getGroupName(qIndex));
				object.setColor(quad.getRgbList().get(qIndex));
				object.setGrps(getColorGroups());
				draggedData = object;
				draggedDetail = DND.DROP_COPY;
				ISelection selection = new StructuredSelection(draggedData);
				LocalSelectionTransfer.getInstance().setSelection(selection);
			}
		});
	}

	private void addQuadrupleMouseListener(final int qIndex, Control control) {
		final Menu menu = new Menu(control);
		control.setMenu(menu);
		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent event) {
				final ColorQuadruple quad = (ColorQuadruple) event.widget
						.getData();

				// show popup menu over the ColorGroup
				if (event.button == 3) {
					MenuItem[] items = menu.getItems();
					for (int i = 0; i < items.length; i++) {
						items[i].dispose();
					}

					final String colorGroupName = ColorsViewPage.this
							.getColorGroupName(quad, qIndex);
					final ColorGroups colorGroups = ColorsViewPage.this
							.getColorGroups();
					final ColorGroup colorGroup = colorGroups
							.getGroupByName(colorGroupName);
					List<String> groupIDs = new ArrayList<String>();
					if (colorGroup != null) {
						List<ColorGroupItem> groupItems = colorGroup
								.getGroupItems();
						for (int i = 0; i < groupItems.size(); i++) {
							ColorGroupItem cgi = groupItems.get(i);
							groupIDs.add(cgi.getItemId());
						}
					}

					class MenuItemSelectionAction implements SelectionListener {

						List<String> groupIDs;

						MenuItemSelectionAction(List<String> groupIDs) {
							this.groupIDs = groupIDs;
						}

						public void widgetDefaultSelected(SelectionEvent e) {
						}

						public void widgetSelected(SelectionEvent e) {
							try {
								SearchView sv = (SearchView) PlatformUI
										.getWorkbench()
										.getActiveWorkbenchWindow()
										.getActivePage()
										.showView(SearchView.ID);
								IPage currentPage = sv.getCurrentPage();
								if (currentPage instanceof SearchViewPage) {
									SearchViewPage svp = (SearchViewPage) currentPage;
									
									// ColorGroup's color
									// which was selected
									ArrayList<ElementTableItem> allAllowedItems = svp.container
											.getAllAllowedItems();
									ArrayList<ElementTableItem> dataToFilter = new ArrayList<ElementTableItem>();
									for (int i = 0; i < allAllowedItems.size(); i++) {
										ElementTableItem eti = allAllowedItems
												.get(i);
										if (groupIDs.contains(eti
												.getProperty("id"))) {
											dataToFilter.add(eti);
										}
									}
									ArrayList<ElementTableItem> filteredInput = svp
											.filterInput(dataToFilter, "");
									svp.tableViewer.setInput(filteredInput);
								}
							} catch (PartInitException ex) {
								ex.printStackTrace();
							}
						}
					}

					if (colorGroup != null && !colorGroup.isEmpty()) {
						MenuItem mi = new MenuItem(menu, SWT.PUSH);
						mi.setText("Search linked elements");
						mi.addSelectionListener(new MenuItemSelectionAction(
								groupIDs));
					}

					if (colorGroup != null && colorGroupName != null
							&& qIndex > 0) {
						if (menu.getItemCount() > 0) {
							new MenuItem(menu, SWT.SEPARATOR);
						}

						new ActionContributionItem(new RemoveToneAction(quad,
								colorGroups, colorGroupName)).fill(menu, -1);

					}

					if (menu.getItemCount() > 0) {
						menu.setVisible(true);
					}
				}
			}
		};

		control.addMouseListener(adapter);
	}

	public void createCompositeArea(Composite parent) {
		// ColorEditorComposite c= new ColorEditorComposite(parent,SWT.NONE);

		inputs = createInput();

		table = new Composite(parent, SWT.NONE);

		GridLayout gl = new GridLayout(3, false);
		gl.horizontalSpacing = 2;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 2;
		table.setLayout(gl);

		GridData gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);
		table.setBackground(ColorConstants.menuBackground);
		// table.setBackground(ColorConstants.white);

		fillTable();
	}

	private void drawOval(GC gc, Rectangle bounds, RGB rgb) {

		Color color = new Color(null, rgb);
		gc.setBackground(fgTextColor);
		// gc.setAntialias(SWT.ON);
		gc.fillOval(bounds.x + 1, bounds.y + bounds.height / 5,
				colorBoxSizeOuter, colorBoxSizeOuter);

		gc.setBackground(ColorConstants.white);

		gc.fillOval(bounds.x + 2, bounds.y + bounds.height / 5 + 1,
				colorBoxSizeMiddle, colorBoxSizeMiddle);

		gc.setBackground(color);

		gc.fillOval(bounds.x + 3, bounds.y + bounds.height / 5 + 2,
				colorBoxSizeInner, colorBoxSizeInner);
		color.dispose();

	}

	private void drawBox(GC gc, Rectangle bounds, RGB rgb) {

		Color color = new Color(null, rgb);
		gc.setBackground(fgTextColor);

		gc.fillRoundRectangle(bounds.x + 1, bounds.y + bounds.height / 5,
				colorBoxSizeOuter, colorBoxSizeOuter, colorBoxRoundAngle,
				colorBoxRoundAngle);

		gc.setBackground(ColorConstants.white);
		gc.fillRectangle(bounds.x + 2, bounds.y + bounds.height / 5 + 1,
				colorBoxSizeMiddle, colorBoxSizeMiddle);

		gc.setBackground(color);
		gc.fillRectangle(bounds.x + 3, bounds.y + bounds.height / 5 + 2,
				colorBoxSizeInner, colorBoxSizeInner);

		color.dispose();
	}

	private void drawUnusedBox(GC gc, Rectangle bounds, RGB rgb) {

		Color color = new Color(null, new RGB(200, 200, 200));// ColorConstants.lightGray;
		gc.setBackground(color);

		gc.fillRoundRectangle(bounds.x + 1, bounds.y + bounds.height / 5,
				colorBoxSizeOuter, colorBoxSizeOuter, colorBoxRoundAngle,
				colorBoxRoundAngle);
		color.dispose();
	}

	/**
	 * Fill all Quadruples from the existing logical color groups. It's a method
	 * for converting from color_groups and quadruples
	 * 
	 * @return
	 */
	private List<ColorQuadruple> createInput() {
		List<ColorQuadruple> quads = new ArrayList<ColorQuadruple>();

		List<String> grpNames = new ArrayList<String>();
		ColorGroups grps = getColorGroups();
		if (grps != null) {
			for (ColorGroup grp : grps.getGroups()) {
				String grpName = grp.getName();
				if (grp.getName().endsWith("tone1")) {
					grpName = grpName.substring(0, grpName.length() - 6);
				} else if (grp.getName().endsWith("tone2")) {
					grpName = grpName.substring(0, grpName.length() - 6);
				} else if (grp.getName().endsWith("tone3")) {
					grpName = grpName.substring(0, grpName.length() - 6);
				} else {
				
				}

				if (grpNames.contains(grpName)) {
					for (ColorQuadruple quad : quads) {
						if (quad.getQuadrupleName().equals(grpName)) {
							quad.addRGB(grp.getGroupColor(), !grp.isEmpty(),
									grp.getName());
						}
					}
				} else {
					ColorQuadruple quad = new ColorQuadruple(grpName);
					quad.addRGB(grp.getGroupColor(), !grp.isEmpty(), grp
							.getName());
					grpNames.add(grpName);
					quads.add(quad);
				}

			}
		}

		return quads;
	}

	/**
	 * 
	 * Container for 4 coulours in which belongs to the same colour group
	 */
	class ColorQuadruple {
		List<RGB> rgbList = new ArrayList<RGB>();

		List<String> groupNames = new ArrayList<String>();

		int selectedIndex = 0;

		String quadrupleName = "";

		public ColorQuadruple(String quadrupleName) {
			this.quadrupleName = quadrupleName;
		}

		Map<Integer, Boolean> rgbLinkage = new HashMap<Integer, Boolean>();

		public String getGroupName(int index) {
			if (index < groupNames.size() && index >= 0)
				return groupNames.get(index);
			return quadrupleName + " tone" + (index + 1);
		}

		public boolean isLinked(int i) {
			if (rgbLinkage.get(i) != null && rgbLinkage.get(i)) {
				return true;
			} else {
				return false;
			}
		}

		public void addRGB(RGB rgb, boolean isLinked, String color) {
			this.rgbList.add(rgb);
			this.groupNames.add(color);
			this.rgbLinkage.put(rgbList.size() - 1, isLinked);
		}

		public void changeRGB(int index, RGB newRGB) {
			int position = index;// rgbList.indexOf(oldRGB);
			if (position != -1) {
				rgbList.set(position, newRGB);
			}

			boolean isLinked = rgbLinkage.get(index);
			rgbLinkage.remove(index);
			rgbLinkage.put(index, isLinked);
		}

		public List<RGB> getRgbList() {
			return rgbList;
		}

		/*
		 * public void setRgbList(List<RGB> rgbList) { this.rgbList = rgbList; }
		 */

		public String getQuadrupleName() {
			return quadrupleName;
		}

		public void setQuadrupleName(String quadrupleName) {
			this.quadrupleName = quadrupleName;
		}

		public int getSelectedRGBIndex() {
			return selectedIndex;
		}

		public void setSelectedRGBIndex(int selectedIndex) {
			this.selectedIndex = selectedIndex;
		}

	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			if (this.inputs.size() == 0) {
				inputs = createInput();
				update(null, null);
			}
		}
	}

	public void contentModified(IContentDelta delta) {
	}

	public void rootContentChanged(IContent content) {
	}

	private CompositeTooltip createTooltip(final int qIndex,
			final Control control) {
		CompositeTooltip tooltip = new CompositeTooltip() {
			@Override
			protected void mouseExit(MouseEvent e) {
				super.mouseExit(e, 250);
			}

			@Override
			protected Point getLocation() {
				Point location = control.getParent().toDisplay(
						getControlLocation().x + 2 * getControlSize().x / 3,
						getControlLocation().y + 2 * getControlSize().y / 3);

				return location;
			}

			@Override
			protected CompositeInformationControl createFocusedControl() {
				return createUnfocusedControl();
			}

			@Override
			protected CompositeInformationControl createUnfocusedControl() {
				final Object itemsData = control.getData();
				if (itemsData instanceof ColorQuadruple) {
					final ColorQuadruple quad = (ColorQuadruple) itemsData;
					quad.setSelectedRGBIndex(qIndex);
					RGB tempRGB;
					if (quad.getSelectedRGBIndex() > quad.rgbList.size() - 1) {
						tempRGB = quad.getRgbList().get(0);
					} else {
						tempRGB = quad.getRgbList().get(
								quad.getSelectedRGBIndex());
					}
					final RGB inputRgb = tempRGB;

					CompositeInformationControl cic = super
							.createUnfocusedControl();

					final Composite root = cic.getComposite();
					GridLayout gl = new GridLayout(1, false);
					gl.marginWidth = gl.marginHeight = 0;
					root.setLayout(gl);

					final ColorChangedLabelWrapper colorChangedLabelWrapper = new ColorChangedLabelWrapper();
					colorChangedLabelWrapper.setColorString(ColorUtil
							.asHashString(inputRgb));

					IColorPickerListener colorPickerListener = new IColorPickerListener() {
						public void selectionChanged() {
							okCloseDialog();
						}

						public void okCloseDialog() {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									hide(true);
								}
							});

							RGB newColor = colorChangedLabelWrapper
									.getColorDescriptor().getRGB();

							if (quad.getSelectedRGBIndex() == 0) {
								RGB formerLeadColor = quad.getRgbList().get(0);
								quad.changeRGB(0, newColor);
								colorizeGroup(sourceEditor, newColor,
										getColorGroups().getGroupByName(
												quad.getQuadrupleName()));

								for (int i = 1; i < quad.getRgbList().size(); i++) {
									RGB rgb = quad.getRgbList().get(i);
									float brightnessRatio = AdjustBrightnessComposite
											.getBrightnessRatio(
													formerLeadColor, rgb);
									RGB newRGBWithGivenHue = AdjustBrightnessComposite
											.getBrighterColor(newColor,
													brightnessRatio);
									quad.changeRGB(i, newRGBWithGivenHue);
									colorizeGroup(sourceEditor,
											newRGBWithGivenHue,
											getColorGroups().getGroupByName(
													quad.getGroupName(i)));
								}
							} else if (quad.getSelectedRGBIndex() >= quad
									.getRgbList().size()) {
								// add new
								ColorGroups grps = getColorGroups();
								String newName = quad.getQuadrupleName()
										+ " tone" + quad.getRgbList().size();
								if (grps.addGroup(newName, newColor) == true) {
									quad.addRGB(newColor, false, newName);
									ColorGroup added = grps
											.getGroupByName(newName);
									added.setParentGroupName(quad
											.getQuadrupleName());
								}
							} else {
								quad.changeRGB(quad.getSelectedRGBIndex(),
										newColor);
								colorizeGroup(
										sourceEditor,
										newColor,
										getColorGroups()
												.getGroupByName(
														quad
																.getGroupName(quad
																		.getSelectedRGBIndex())));
							}

						}
					};
					if (quad.getSelectedRGBIndex() == 0) {
						new ColorPickerComposite(root, SWT.NONE,
								colorChangedLabelWrapper, colorPickerListener);
					} else if (quad.getSelectedRGBIndex() > 0
							&& quad.getSelectedRGBIndex() < 4) {
						ColorGroup group = getColorGroups().getGroupByName(
								quad.getQuadrupleName());
						colorChangedLabelWrapper.setParentColorGroup(group);
						new AdjustBrightnessComposite(root, SWT.NONE,
								colorChangedLabelWrapper, colorPickerListener);
					}

					return cic;

				} else {
					return null;
				}
			}
		};

		tooltip.setControl(control);

		return tooltip;
	}

	private List<IContentData> findContentDataForGroup(IEditorPart editor,
			ColorGroup group) {
		List<IContentData> datas = new ArrayList<IContentData>();

		IContentAdapter adapter = (IContentAdapter) editor
				.getAdapter(IContentAdapter.class);
		if (adapter != null) {
			IContent[] cnt = adapter.getContents();
			IContent root = ScreenUtil.getPrimaryContent(cnt);
			for (ColorGroupItem item : group.getGroupItems()) {
				IContentData data = root.findById(item.getItemId());
				if (data == null) {
					data = root.findByName(item.getItemId());
				}
				datas.add(data);
			}
		}

		return datas;
	}

	private void colorizeGroup(IEditorPart editor, RGB newColor, ColorGroup grp) {
		if (grp != null) {
			ColorGroup referencedGroup = grp;

			RGB referencedColor = referencedGroup.getGroupColor();
			RGB changingRGB = newColor;
			final List<IContentData> items = findContentDataForGroup(editor,
					referencedGroup);

			ISelectionProvider sp = new ISelectionProvider() {
				public ISelection getSelection() {
					return new StructuredSelection(items);
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

			ColorizeColorGroupAction colorGroupAction = new ColorizeColorGroupAction(
					sp, null, null);
			colorGroupAction.setColorGroups(getColorGroups());
			colorGroupAction.setColor(changingRGB, referencedColor, 255);
			colorGroupAction.setColorGroup(referencedGroup);
			colorGroupAction.run();
		}
	}

	private ColorGroups getColorGroups() {
		if (sourceEditor != null && ColorGroupsStore.isEnabled) {
			IFile original = ((FileEditorInput) sourceEditor.getEditorInput())
					.getFile();
			return ColorGroupsStore.getColorGroupsForProject(original
					.getProject());
		} else {
			return null;
		}
	}

	public void update(Observable arg0, Object arg1) {
		
		// cause DragSource panic
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!table.isDisposed()) {
					table.setRedraw(false);
					for (Control child : table.getChildren()) {
						child.dispose();
					}

					inputs = createInput();
					fillTable();

					table.layout(true, true);

					Point minSize = composite.computeSize(SWT.DEFAULT,
							SWT.DEFAULT);
					scrolledComposite.setMinSize(minSize);

					table.setRedraw(true);

					scrolledComposite.redraw();
					table.redraw();
				}
			}
		});
	}

	private String getColorGroupName(ColorQuadruple colorQuad, int index) {
		String baseName = colorQuad.getQuadrupleName(); 
		if (index == 0) {
			return baseName;
		} else if (index > 0) {
			return colorQuad.getGroupName(index);
		}
		return "NO COLOR GROUP";
	}

	class RemoveToneAction extends Action {
		ColorQuadruple quad;

		ColorGroups colorGroups;

		String tone;

		RemoveToneAction(ColorQuadruple quad, ColorGroups groups, String tone) {
			super();
			setText("Remove");
			this.quad = quad;
			this.colorGroups = groups;
			this.tone = tone;
		}

		@Override
		public void run() {
			CommandStack stck = (CommandStack) sourceEditor
					.getAdapter(CommandStack.class);
			Command cmd = new Command(getText()) {
				@Override
				public boolean canExecute() {
					return true;
				}

				@Override
				public boolean canUndo() {
					return false;
				}

				@Override
				public void execute() {
					List<ColorGroup> groups = new ArrayList<ColorGroup>();
					for (int i = 1; i <= 3; i++) {
						ColorGroup grp = colorGroups.getGroupByName(quad
								.getQuadrupleName()
								+ " tone" + i);
						if (grp != null) {
							if (!tone.equals(grp.getName())) {
								groups.add(grp);
							} else {
								colorGroups.removeGroup(grp);
							}
						}
					}

					int idx = 1;
					for (ColorGroup grp : groups) {
						String newName = quad.getQuadrupleName() + " tone"
								+ idx;
						if (!newName.equals(grp.getName())) {
							grp.setName(newName);
						}
						idx++;
					}
					update(null, null);
				}
			};
			if (stck != null) {
				stck.execute(cmd);
			} else {
				cmd.execute();
			}
		}
	}

	class AddNewColorGroupAction extends Action {
		public AddNewColorGroupAction() {
			super();
			setToolTipText("Create a new Color Group");
			setImageDescriptor(S60WorkspacePlugin
					.getImageDescriptor("icons/new_color_group.gif"));
		}

		@Override
		public void run() {
			Command cmd = new Command(getText()) {
				ColorGroup newGroup;

				@Override
				public boolean canExecute() {
					return true;
				}

				@Override
				public boolean canUndo() {
					return newGroup != null;
				}

				@Override
				public void redo() {
					newGroup = getColorGroups().getNewGroup(
							new RGB(255, 255, 255));
					getColorGroups().addGroup(newGroup);
					update(null, null);
				}

				@Override
				public void execute() {
					redo();
				}

				@Override
				public void undo() {
					getColorGroups().removeGroup(newGroup);
					update(null, null);
				}
			};
			CommandStack stck = (CommandStack) sourceEditor
					.getAdapter(CommandStack.class);
			if (stck != null) {
				stck.execute(cmd);
			} else {
				cmd.execute();
			}
		}
	}
}
