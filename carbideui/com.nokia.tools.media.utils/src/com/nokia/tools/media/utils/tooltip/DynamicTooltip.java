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
package com.nokia.tools.media.utils.tooltip;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.framework.Bundle;

import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.ui.tooltip.CompositeInformationControl;
import com.nokia.tools.ui.tooltip.CompositeTooltip;

public class DynamicTooltip extends CompositeTooltip {
	public static final String PREF_DYNAMIC_TOOLTIP = "dynamicTooltip";

	public static boolean IS_ENABLED = true;
	
	static {
		IS_ENABLED = "true".equalsIgnoreCase(UtilsPlugin.getDefault()
				.getPreferenceStore().getString(PREF_DYNAMIC_TOOLTIP));
	}

	public static final String EXTENSION_POINT = "com.nokia.tools.media.utils.dynamicTooltip";

	public static final String ELEMENT_TOOLBAR_ITEM = "tooltip-item";

	public static final String ELEMENT_SECTION = "tooltip-section";

	public static final String ELEMENT_ENABLEMENT = "enablement";

	public static final String ATTRIBUTE_ICON = "icon";

	public static final String ATTRIBUTE_DISABLED_ICON = "disabledIcon";

	public static final String ATTRIBUTE_HOVER_ICON = "hoverIcon";

	public static final String ATTRIBUTE_LABEL = "label";

	public static final String ATTRIBUTE_UICLASS = "uiClass";

	public static final String ATTRIBUTE_CONTEXT = "context";

	public static final String ATTRIBUTE_TOOLTIP = "tooltip";

	public static final String ATTRIBUTE_TOOLTIP_STYLE = "tooltipStyle";
	
	public static final String ATTRIBUTE_TARGET = "target";

	public static final String ATTRIBUTE_ACTION = "action";
	
	public static final String ATTRIBUTE_ACTION_PROVIDER = "actionProvider";

	public static final String ATTRIBUTE_PRIORITY = "priority";

	public static final String ATTRIBUTE_FOCUSED = "focused";

	public static final String ATTRIBUTE_UNFOCUSED = "unfocused";

	public static final String ATTRIBUTE_UICONTAINER_ID = "uiContainerId";

	public static final String ATTRIBUTE_FILTER = "filter";

	protected static List<ToolbarItemContribution> toolbarContributions = new ArrayList<ToolbarItemContribution>();

	protected static List<SectionContribution> sectionContributions = new ArrayList<SectionContribution>();

	protected static ImageDescriptor ARROW_IMAGE = UtilsPlugin
			.getImageDescriptor("icons/arrowR.png");

	public enum EStyle {
		HORIZONTAL_HORIZONTAL, HORIZONTAL_VERTICAL, VERTICAL_HORIZONTAL, VERTICAL_VERTICAL, MENU
	}

	protected EStyle style;

	static {
		initializeData();
	}

	protected Object selection;

	protected Object uiContainer;

	protected String uiContainerId;

	protected Object context;

	protected List<DynamicTooltip> children = new ArrayList<DynamicTooltip>();

	public DynamicTooltip(Object selection, Object uiContainer, Object context,
			EStyle style) {
		super();
		this.selection = selection;
		this.uiContainer = uiContainer;
		this.context = context;
		this.style = style;
		if (uiContainer != null) {
			this.uiContainerId = uiContainer.getClass().getName();
		}
	}

	public void update(Object selection, Object uiContainer, Object context,
			EStyle style) {
		this.selection = selection;
		this.uiContainer = uiContainer;
		this.context = context;
		this.style = style;

		if (informationControl != null) {
			// tooltip is visible, refresh needed
			Composite composite = ((CompositeInformationControl) informationControl)
					.getComposite();
			if (composite != null) {
				Control[] children = composite.getChildren();
				for (Control control : children) {
					control.dispose();
				}

				children = composite.getParent().getChildren();
				for (Control control : children) {
					if (control != composite) {
						control.dispose();
					}
				}

				addContributedControls(composite, focused);

				layout();
			}
		}
	}

	protected void layout() {
		if (informationControl != null) {
			Composite composite = ((CompositeInformationControl) informationControl)
					.getComposite();
			if (composite != null) {
				composite.getShell().layout(true, true);
				Point prefferedSize = informationControl.computeSizeHint();
				Point size = getSize(prefferedSize.x, prefferedSize.y);
				informationControl.setSize(size.x, size.y);
				composite.getShell().redraw();
				composite.getShell().update();
			}
		}
	}

	@Override
	protected CompositeInformationControl createFocusedControl() {
		informationControl = new ExtendedCompositeInformationControl(
				getShell(), SWT.PRIMARY_MODAL | SWT.RESIZE | SWT.TOOL,
				SWT.NONE, null);

		addContributedControls(((CompositeInformationControl) informationControl).getComposite(), true);

		return (CompositeInformationControl) informationControl;
	}

	@Override
	protected CompositeInformationControl createUnfocusedControl() {
		informationControl = new ExtendedCompositeInformationControl(
				getShell(), SWT.NO_TRIM | SWT.TOOL, SWT.NONE, null);

		addContributedControls(((CompositeInformationControl) informationControl).getComposite(), false);

		return (CompositeInformationControl) informationControl;
	}

	protected void addContributedControls(final Composite composite,
			boolean focusState) {
		Composite toolBarComposite = null;
		Composite sectionsComposite = null;

		children.clear();
		
		if (style == EStyle.MENU) {
			GridLayout gl = new GridLayout(2, false);
			gl.marginHeight = gl.marginWidth = gl.horizontalSpacing = 0;
			composite.setLayout(gl);
			toolBarComposite = new Composite(composite, SWT.NONE);
			gl = new GridLayout(1, false);
			// gl.marginHeight = gl.marginWidth = 0;
			toolBarComposite.setLayout(gl);
			toolBarComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
					true, false));
		}

		// horizontal toolbar
		if (style == EStyle.HORIZONTAL_HORIZONTAL || style == EStyle.HORIZONTAL_VERTICAL) {
			GridLayout gl = new GridLayout(1, false);
			gl.marginHeight = gl.marginWidth = gl.verticalSpacing = 0;
			composite.setLayout(gl);
			toolBarComposite = new Composite(composite, SWT.NONE);
			gl = new GridLayout(1, false);
			gl.marginHeight = gl.marginWidth = 0;
			toolBarComposite.setLayout(gl);
			toolBarComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
					true, false));
		}

		// vertical toolbar
		if (style == EStyle.VERTICAL_VERTICAL || style == EStyle.VERTICAL_HORIZONTAL) {
			GridLayout gl = new GridLayout(2, false);
			gl.marginHeight = gl.marginWidth = gl.horizontalSpacing = 0;
			composite.setLayout(gl);
			toolBarComposite = new Composite(composite, SWT.NONE);
			gl = new GridLayout(1, false);
			gl.marginHeight = gl.marginWidth = 0;
			toolBarComposite.setLayout(gl);
			toolBarComposite.setLayoutData(new GridData(SWT.NONE, SWT.FILL,
					false, true));
		}
		
		sectionsComposite = new Composite(composite, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = gl.marginWidth = 0;
		sectionsComposite.setLayout(gl);
		sectionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));

		toolBarComposite.setBackground(composite.getBackground());
		sectionsComposite.setBackground(composite.getBackground());

		Set<ToolbarItemContribution> toolbarContributions = new TreeSet<ToolbarItemContribution>();

		toolbarContributions.addAll(collectToolbarContributions(focusState));

		List<SectionContribution> sectionContributions = collectSectionContributions(focusState);
		for (SectionContribution contribution : sectionContributions) {
			if (contribution.uiClass != null) {
				try {
					IDynamicTooltipUIContribution ui = contribution.uiClass
							.newInstance();
					ui.setTooltip(this);
					ui.setUIContainer(uiContainer);
					ui.setSelection(selection);
					ui.setContext(context);
					configureUIContribution(ui);
					ToolbarItemContribution[] tcs = ui.contributeToolbar();
					if (tcs != null) {
						for (ToolbarItemContribution tc : tcs) {
							toolbarContributions.add(tc);
						}
					}
					ui.createControls(sectionsComposite, focusState);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (sectionsComposite.getChildren().length == 0) {
			sectionsComposite.dispose();
		}

		if (toolbarContributions.size() > 0) {
			GridLayout layout = (GridLayout) toolBarComposite.getLayout();
			layout.horizontalSpacing = 1;

			if (EStyle.HORIZONTAL_HORIZONTAL == style || EStyle.HORIZONTAL_VERTICAL == style
					|| EStyle.VERTICAL_HORIZONTAL == style || EStyle.VERTICAL_VERTICAL == style) {

				CoolBar cb = null;
				if (EStyle.HORIZONTAL_HORIZONTAL == style || EStyle.HORIZONTAL_VERTICAL == style) {
					cb = new CoolBar(toolBarComposite, SWT.FLAT | SWT.HORIZONTAL);
					cb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				} else {
					cb = new CoolBar(toolBarComposite, SWT.FLAT | SWT.VERTICAL);
					cb.setLayoutData(new GridData(GridData.FILL_VERTICAL));
				}
							
				Map<String, ToolBarManager> linkMap = new LinkedHashMap<String, ToolBarManager>(); 
								
				final Map<ContributionItem, ToolbarItemContribution> map = new HashMap<ContributionItem, ToolbarItemContribution>();
				for (final ToolbarItemContribution contribution : toolbarContributions) {
					ToolBarManager toolBar = linkMap.get(contribution.getTarget()); 
					if (toolBar == null) {
						if (EStyle.HORIZONTAL_HORIZONTAL == style || EStyle.HORIZONTAL_VERTICAL == style) {
							toolBar = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
						} else {
							toolBar = new ToolBarManager(SWT.FLAT | SWT.VERTICAL);
						}
						linkMap.put(contribution.getTarget(), toolBar);
					}
					
					if (contribution.getAction() == null) {
						Action action = new Action(contribution.getLabel()) {
							@Override
							public void run() {
								// do nothing
							}
						};
						action.setImageDescriptor(contribution.getIcon());
						action.setDisabledImageDescriptor(contribution
								.getDisabledIcon());
						action.setHoverImageDescriptor(contribution
								.getHoverIcon());

						ActionContributionItem item = new ActionContributionItem(
								action);
						map.put(item, contribution);
						toolBar.add(item);
					} else {
						IAction action = contribution.getAction();
						if (contribution.getAction() instanceof IDynamicTooltipToolbarAction) {
							((IDynamicTooltipToolbarAction) action)
									.setTooltip(DynamicTooltip.this);
							((IDynamicTooltipToolbarAction) action)
									.setUIContainer(uiContainer);
							((IDynamicTooltipToolbarAction) action)
									.setSelection(selection);
						}
						if (action instanceof SeparatorAction) {
							Separator separator = new Separator();
							map.put(separator, contribution);
							toolBar.add(separator);
						} else {
							ActionContributionItem item = new ActionContributionItem(
									action);
							map.put(item, contribution);
							toolBar.add(item);
						}
					}
				}
				
				Point preffered = new Point(0, 0);

				for (Map.Entry<String, ToolBarManager> entry : linkMap.entrySet()) {
					CoolItem ci = new CoolItem(cb, SWT.NONE);
					ToolBar tb = entry.getValue().createControl(cb);
					ci.setControl(tb);
					
					Point tbSize = tb.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				    Point cbSize = ci.computeSize(tbSize.x, tbSize.y);
					ci.setSize(cbSize);
					ci.setPreferredSize(cbSize);
					ci.setMinimumSize(tbSize);
					if (EStyle.HORIZONTAL_HORIZONTAL == style || EStyle.HORIZONTAL_VERTICAL == style) {
						preffered.x += cbSize.x;
						preffered.y = Math.max(preffered.y, cbSize.y);
					} else {
						preffered.x = Math.max(preffered.x, cbSize.x);
						preffered.y += cbSize.y;
					}
					
					for (int i = 0; i < tb.getItems().length; i++) {
						ToolItem item = tb.getItems()[i];
						ContributionItem actionItem = (ContributionItem) item
								.getData();
						if (actionItem instanceof ActionContributionItem) {
							createChildTooltip(map.get(actionItem), item);
						}
					}
				}
				
				cb.setLocked(false);
				cb.setLayoutData(new GridData(preffered.x, preffered.y));
				
			} else if (EStyle.MENU == style) {
				for (final ToolbarItemContribution contribution : toolbarContributions) {
					Composite itemComposite = null;

					itemComposite = createMenuComposite(toolBarComposite);

					if (contribution.getAction() instanceof SeparatorAction) {
						itemComposite.setEnabled(false);

						final Canvas img = new Canvas(itemComposite, SWT.NONE);
						GridData gd = new GridData(SWT.FILL, SWT.CENTER, true,
								false);
						gd.heightHint = 5;
						gd.horizontalSpan = 3;
						img.setLayoutData(gd);
						img.setBackground(itemComposite.getBackground());
						img.addPaintListener(new PaintListener() {
							public void paintControl(PaintEvent e) {
								GC gc = e.gc;
								gc.setForeground(ColorConstants.black);
								e.gc.drawLine(0, 2, img.getSize().x - 1, 2);
							}
						});

						continue;
					}

					final Canvas img = new Canvas(itemComposite, SWT.NONE);
					img.setBackground(itemComposite.getBackground());

					final Label label = new Label(itemComposite, SWT.NONE);
					label.setBackground(itemComposite.getBackground());
					label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
							true, false));
					label
							.setText(contribution.getLabel() != null ? contribution
									.getLabel()
									: "");
					label.setEnabled(!contribution.isDisabled());

					final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent event) {
							if (IAction.TEXT.equals(event.getProperty())) {
								label
										.setText(contribution.getLabel() != null ? contribution
												.getLabel()
												: "");
								layout();
							}
							if (IAction.ENABLED.equals(event.getProperty())) {
								label.setEnabled(!contribution.isDisabled());
							}
						}
					};

					contribution
							.addPropertyChangeListener(propertyChangeListener);

					label.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							contribution
									.removePropertyChangeListener(propertyChangeListener);
						}
					});

					if (contribution.tooltip != null) {
						final Canvas arrow = new Canvas(itemComposite, SWT.NONE);
						arrow.setBackground(itemComposite.getBackground());
						arrow.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
								false, false));
						final Image image = ARROW_IMAGE.createImage();
						arrow.addDisposeListener(new DisposeListener() {
							public void widgetDisposed(DisposeEvent e) {
								image.dispose();
							}
						});

						((GridData) arrow.getLayoutData()).widthHint = image
								.getImageData().width;
						((GridData) arrow.getLayoutData()).heightHint = image
								.getImageData().height;
						arrow.addPaintListener(new PaintListener() {
							public void paintControl(PaintEvent e) {
								e.gc.drawImage(image, 0, 0);
							}
						});
					}

					img.setToolTipText(contribution.getLabel());

					final IPropertyChangeListener labelChangeListener = new IPropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent event) {
							if (IAction.TEXT.equals(event.getProperty())) {
								img.setToolTipText(contribution.getLabel());
							}
						}
					};

					contribution.addPropertyChangeListener(labelChangeListener);

					img.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							contribution
									.removePropertyChangeListener(labelChangeListener);
						}
					});

					final Image[] image = new Image[] { contribution
							.isDisabled() ? contribution.getDisabledIcon()
							.createImage() : contribution.getIcon()
							.createImage() };
					final Image[] hoverImage = new Image[] { (contribution
							.isDisabled() || contribution.getHoverIcon() == null) ? image[0]
							: contribution.getHoverIcon().createImage() };

					final IPropertyChangeListener imageChangeListener = new IPropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent event) {
							if (IAction.IMAGE.equals(event.getProperty())
									|| IAction.ENABLED.equals(event
											.getProperty())) {
								if (image[0] != null) {
									image[0].dispose();
								}
								if (hoverImage[0] != null) {
									hoverImage[0].dispose();
								}

								image[0] = contribution.isDisabled() ? contribution
										.getDisabledIcon().createImage()
										: contribution.getIcon().createImage();
								hoverImage[0] = (contribution.isDisabled() || contribution
										.getHoverIcon() == null) ? image[0]
										: contribution.getHoverIcon()
												.createImage();
								img.redraw();
							}
						}
					};

					contribution.addPropertyChangeListener(imageChangeListener);

					img.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							contribution
									.removePropertyChangeListener(imageChangeListener);
							if (image[0] != null) {
								image[0].dispose();
							}
							if (hoverImage[0] != null) {
								hoverImage[0].dispose();
							}
						}
					});

					img.setLayoutData(new GridData(
							image[0].getImageData().width + 4, image[0]
									.getImageData().height + 4));

					img.addPaintListener(new PaintListener() {
						public void paintControl(PaintEvent e) {
							e.gc.drawImage(image[0], 2, 2);
						}
					});

					if (EStyle.MENU == style) {
						DynamicTooltip tooltip = createChildTooltip(
								contribution, itemComposite);
						registerOnClickAction(contribution, itemComposite,
								tooltip);
						for (Control control : itemComposite.getChildren()) {
							registerOnClickAction(contribution, control,
									tooltip);
						}
					}
				}
			}
		}
		
		// set proper number of columns for horizontal sections alignment
		if ((style == EStyle.HORIZONTAL_HORIZONTAL || style == EStyle.VERTICAL_HORIZONTAL) && !sectionsComposite.isDisposed()) {
			((GridLayout) sectionsComposite.getLayout()).numColumns = sectionsComposite.getChildren().length;
		}

		if (toolBarComposite.getChildren().length == 0) {
			toolBarComposite.dispose();
		}

		if (sectionsComposite.isDisposed() && toolBarComposite.isDisposed()) {
			composite.dispose();
		}
	}

	public boolean hasContributions() {
		return hasContributions(false) || hasContributions(true);
	}

	public boolean hasContributions(boolean focusState) {
		return (collectToolbarContributions(focusState).size() > 0 || collectSectionContributions(
				focusState).size() > 0);
	}

	public int getContributionsCount() {
		return Math.max(getContributionsCount(false),
				getContributionsCount(true));
	}

	public int getContributionsCount(boolean focusState) {
		return collectToolbarContributions(focusState).size()
				+ collectSectionContributions(focusState).size();
	}

	protected Composite createMenuComposite(Composite parent) {
		Composite itemComposite = new Composite(parent, SWT.NONE);
		itemComposite.setBackground(parent.getBackground());
		GridLayout gl = new GridLayout(3, false);
		gl.marginWidth = gl.marginHeight = 0;
		itemComposite.setLayout(gl);
		itemComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false));

		final Composite _itemComposite = itemComposite;
		final Listener mouseMove = new Listener() {
			public void handleEvent(Event event) {
				if ((!_itemComposite.isDisposed())
						&& event.widget instanceof Control) {
					Control control = (Control) event.widget;
					if (control.getShell() == _itemComposite.getShell()) {
						Point loc = _itemComposite.getLocation();
						loc = _itemComposite.getParent().toDisplay(loc);
						Rectangle rect = new Rectangle(loc.x, loc.y,
								_itemComposite.getSize().x, _itemComposite
										.getSize().y);
						Color newColor = null;
						if (rect.contains(control.toDisplay(event.x, event.y))) {
							// test if event is from control or it children
							while (control != null && control != _itemComposite) {
								control = control.getParent();
							}
							if (control == null) {
								return;
							}
							newColor = ColorConstants.lightGray;
						} else {
							newColor = _itemComposite.getParent()
									.getBackground();
						}

						boolean redraw = false;
						if (!newColor.equals(_itemComposite.getBackground())) {
							_itemComposite.setBackground(newColor);
							redraw = true;
						}
						Control[] children = _itemComposite.getChildren();
						for (Control child : children) {
							if (!newColor.equals(child.getBackground())) {
								child.setBackground(newColor);
								redraw = true;
							}
						}
						if (redraw) {
							_itemComposite.redraw();
						}
					}
				}
			}
		};

		itemComposite.getDisplay().addFilter(SWT.MouseMove, mouseMove);

		itemComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				_itemComposite.getDisplay().removeFilter(SWT.MouseMove,
						mouseMove);
			}
		});

		return itemComposite;
	}

	protected void registerOnClickAction(
			final ToolbarItemContribution contribution, final Control control,
			final DynamicTooltip tooltip) {
		if (contribution.actionId != null) {
			control.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (e.button == 1) {
						IAction action;
						try {
							action = contribution.getAction();
							if (action != null) {
								if (action instanceof IDynamicTooltipToolbarAction) {
									((IDynamicTooltipToolbarAction) action)
											.setTooltip(DynamicTooltip.this);
									((IDynamicTooltipToolbarAction) action)
											.setUIContainer(uiContainer);
									((IDynamicTooltipToolbarAction) action)
											.setSelection(selection);
								}
								if (action.isEnabled()) {
									action.run();
								}
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			});
		}

		if (tooltip != null) {
			control.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (e.button == 1) {
						tooltip.show();
					}
				}
			});

			if (tooltip.control != control) {
				control.addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseHover(MouseEvent e) {
						tooltip.show();
					}
				});
			}
		}
	}

	@Override
	public void hide(boolean parents) {
		for (DynamicTooltip child : children) {
			try {
				child.hide();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		children.clear();
		
		super.hide(parents);
	}

	protected DynamicTooltip createChildTooltip(
			ToolbarItemContribution contribution, final ToolItem item) {
		if (contribution.tooltip != null) {
			EStyle targetStyle = getStyle();
			if (contribution.tooltipStyle != null) {
				targetStyle = EStyle.valueOf(contribution.tooltipStyle);
				if (targetStyle == null) {
					targetStyle = getStyle();
				}
			}

			final DynamicTooltip tooltip = new DynamicTooltip(selection,
					uiContainer, contribution.tooltip, targetStyle) {

				@Override
				protected Rectangle getControlBounds() {
					Rectangle toolbarBounds = super.getControlBounds();
					Rectangle itemBounds = item.getBounds();
					return new Rectangle(toolbarBounds.x + itemBounds.x,
							toolbarBounds.y + itemBounds.y, itemBounds.width,
							itemBounds.height);
				}

				@Override
				protected boolean isControlEnabled() {
					return item.isEnabled();
				}

				@Override
				protected void configureUIContribution(
						IDynamicTooltipUIContribution ui) {
					DynamicTooltip.this.configureUIContribution(ui);
				}
			};

			tooltip.setControl(item.getParent());

			tooltip.setParent(this);

			children.add(tooltip);

			return tooltip;
		}

		return null;
	}

	protected DynamicTooltip createChildTooltip(
			ToolbarItemContribution contribution, Control control) {
		if (contribution.tooltip != null) {
			EStyle targetStyle = getStyle();
			if (contribution.tooltipStyle != null) {
				targetStyle = EStyle.valueOf(contribution.tooltipStyle);
				if (targetStyle == null) {
					targetStyle = getStyle();
				}
			}

			final DynamicTooltip tooltip = new DynamicTooltip(selection,
					uiContainer, contribution.tooltip, targetStyle) {

				@Override
				protected void configureUIContribution(
						IDynamicTooltipUIContribution ui) {
					DynamicTooltip.this.configureUIContribution(ui);
				}
			};

			tooltip.setControl(control);

			tooltip.setParent(this);

			children.add(tooltip);

			return tooltip;
		}

		return null;
	}

	protected void configureUIContribution(IDynamicTooltipUIContribution ui) {
	}

	public List<ToolbarItemContribution> collectToolbarContributions(
			boolean focusState) {
		if (!IS_ENABLED) {
			return new ArrayList<ToolbarItemContribution>();
		}

		Set<ToolbarItemContribution> toRet = new TreeSet<ToolbarItemContribution>();

		for (ToolbarItemContribution contribution : toolbarContributions) {
			for (Enablement enablement : contribution.enablements) {
				boolean enabled = true;
				if (context == null && enablement.context != null) {
					enabled = false;
					continue;
				}
				if (context != null && enablement.context != null) {
					enabled = context.equals(enablement.context);
					if (!enabled) {
						continue;
					}
				}
				if (focusState == true && enablement.focused != null
						&& Boolean.FALSE.equals(enablement.focused)) {
					enabled = false;
					continue;
				}
				if (focusState == false && enablement.unfocused != null
						&& Boolean.FALSE.equals(enablement.unfocused)) {
					enabled = false;
					continue;
				}
				if (enablement.uiContainerId != null
						&& !enablement.uiContainerId.equals(uiContainerId)) {
					enabled = false;
					continue;
				}
				if (enablement.filter != null
						&& !enablement.filter.accept(this, selection,
								uiContainerId, context, focusState)) {
					enabled = false;
				}

				if (enabled) {
					try {
						toRet.add((ToolbarItemContribution) contribution
								.clone());
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		return new ArrayList<ToolbarItemContribution>(toRet);
	}

	public List<SectionContribution> collectSectionContributions(
			boolean focusState) {

		if (!IS_ENABLED) {
			return new ArrayList<SectionContribution>();
		}

		Set<SectionContribution> toRet = new TreeSet<SectionContribution>();

		for (SectionContribution contribution : sectionContributions) {
			for (Enablement enablement : contribution.enablements) {
				boolean enabled = true;
				if (context == null && enablement.context != null) {
					enabled = false;
					continue;
				}
				if (context != null && enablement.context != null) {
					enabled = context.equals(enablement.context);
					if (!enabled) {
						continue;
					}
				}
				if (focusState == true && enablement.focused != null
						&& Boolean.FALSE.equals(enablement.focused)) {
					enabled = false;
					continue;
				}
				if (focusState == false && enablement.unfocused != null
						&& Boolean.FALSE.equals(enablement.unfocused)) {
					enabled = false;
					continue;
				}
				if (enablement.uiContainerId != null
						&& !enablement.uiContainerId.equals(uiContainerId)) {
					enabled = false;
					continue;
				}
				if (enablement.filter != null
						&& !enablement.filter.accept(this, selection,
								uiContainerId, context, focusState)) {
					enabled = false;
				}

				if (enabled) {
					try {
						toRet.add((SectionContribution) contribution.clone());
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		return new ArrayList<SectionContribution>(toRet);
	}

	@SuppressWarnings("unchecked")
	protected static void initializeData() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT).getConfigurationElements();
		for (IConfigurationElement element : elements) {
			if (ELEMENT_TOOLBAR_ITEM.equalsIgnoreCase(element.getName())) {
				ToolbarItemContribution def = new ToolbarItemContribution();

				String contributorName = element.getContributor().getName();

				String iconPath = element.getAttribute(ATTRIBUTE_ICON);
				if (iconPath != null) {
					URL url = FileLocator.find(Platform.getBundle(contributorName), new Path(iconPath), null);
					if (url != null) {
						def.icon = ImageDescriptor.createFromURL(url);
					} else {
						def.icon = ImageDescriptor.getMissingImageDescriptor();
					}
				}

				String disabledIconPath = element
						.getAttribute(ATTRIBUTE_DISABLED_ICON);
				if (disabledIconPath != null) {
					URL url = FileLocator.find(Platform.getBundle(contributorName), new Path(disabledIconPath), null);
					if (url != null) {
						def.disabledIcon = ImageDescriptor.createFromURL(url);
					}
				}

				String hoverIconPath = element
						.getAttribute(ATTRIBUTE_HOVER_ICON);
				if (hoverIconPath != null) {
					URL url = FileLocator.find(Platform.getBundle(contributorName), new Path(hoverIconPath), null);
					if (url != null) {
						def.hoverIcon = ImageDescriptor.createFromURL(url);
					}
				}

				def.label = element.getAttribute(ATTRIBUTE_LABEL);

				String priority = element.getAttribute(ATTRIBUTE_PRIORITY);
				if (priority != null && priority.length() > 0) {
					try {
						def.priority = new Integer(priority);
					} catch (NumberFormatException nfe) {
						System.err.println("Invalid definition:\n"
								+ element.toString());
						System.err
								.println("Specified priority is not a number!");
					}
				}

				def.tooltip = element
						.getAttribute(ATTRIBUTE_TOOLTIP);

				def.tooltipStyle = element.getAttribute(ATTRIBUTE_TOOLTIP_STYLE);
				
				def.target = element
						.getAttribute(ATTRIBUTE_TARGET);

				Bundle contributor = Platform.getBundle(contributorName);

				def.actionId = element
					.getAttribute(ATTRIBUTE_ACTION);
				
				def.contributor = contributor;
				
				String actionProviderClassName = element
						.getAttribute(ATTRIBUTE_ACTION_PROVIDER);

				if (actionProviderClassName != null) {
					try {
						Class actionProviderClass = contributor
								.loadClass(actionProviderClassName);

						if (ITooltipActionProvider.class.isAssignableFrom(actionProviderClass)) {
							def.actionProvider = (ITooltipActionProvider) actionProviderClass.newInstance();
						} else {
							System.err.println("Invalid definition:\n"
									+ element.toString());
							System.err
									.println("actionProvider class must implement interface "
											+ ITooltipActionProvider.class.getName());
						}
					} catch (ClassNotFoundException cnfe) {
						System.err.println("Invalid definition:\n"
								+ element.toString());
						System.err.println("actionProvider class not found: "
								+ actionProviderClassName);
					} catch (Throwable t) {
						System.err.println("Invalid definition:\n"
								+ element.toString());
						t.printStackTrace();
					}
				}

				def.enablements = parseEnablements(element);

				toolbarContributions.add(def);
			}

			if (ELEMENT_SECTION.equalsIgnoreCase(element.getName())) {
				SectionContribution def = new SectionContribution();

				String priority = element.getAttribute(ATTRIBUTE_PRIORITY);
				if (priority != null && priority.length() > 0) {
					try {
						def.priority = new Integer(priority);
					} catch (NumberFormatException nfe) {
						System.err.println("Invalid definition:\n"
								+ element.toString());
						System.err
								.println("Specified priority is not a number!");
					}
				}

				String uiClassName = element.getAttribute(ATTRIBUTE_UICLASS);
				Bundle contributor = Platform.getBundle(element
						.getContributor().getName());
				try {
					Class uiClass = contributor.loadClass(uiClassName);

					if (IDynamicTooltipUIContribution.class
							.isAssignableFrom(uiClass)) {
						def.uiClass = (Class<IDynamicTooltipUIContribution>) uiClass;
					} else {
						System.err.println("Invalid definition:\n"
								+ element.toString());
						System.err
								.println("UI class must implement interface "
										+ IDynamicTooltipUIContribution.class
												.getName());
					}
				} catch (ClassNotFoundException cnfe) {
					System.err.println("Invalid definition:\n"
							+ element.toString());
					System.err.println("UI class not found: " + uiClassName);
				}

				def.enablements = parseEnablements(element);

				sectionContributions.add(def);
			}
		}
	}

	protected static List<Enablement> parseEnablements(
			IConfigurationElement element) {
		List<Enablement> enablements = new ArrayList<Enablement>();

		for (IConfigurationElement child : element.getChildren()) {
			if (ELEMENT_ENABLEMENT.equalsIgnoreCase(child.getName())) {
				Enablement enablement = new Enablement();

				String focused = child.getAttribute(ATTRIBUTE_FOCUSED);
				if (focused != null && focused.length() > 0) {
					enablement.focused = new Boolean(focused);
				}

				String unfocused = child.getAttribute(ATTRIBUTE_UNFOCUSED);
				if (unfocused != null && unfocused.length() > 0) {
					enablement.unfocused = new Boolean(unfocused);
				}

				enablement.uiContainerId = child
						.getAttribute(ATTRIBUTE_UICONTAINER_ID);

				enablement.context = child.getAttribute(ATTRIBUTE_CONTEXT);

				String filterClassName = child.getAttribute(ATTRIBUTE_FILTER);
				if (filterClassName != null) {
					Bundle contributor = Platform.getBundle(element
							.getContributor().getName());
					try {
						Class filterClass = contributor
								.loadClass(filterClassName);

						if (IDynamicTooltipContributionFilter.class
								.isAssignableFrom(filterClass)) {
							try {
								enablement.filter = (IDynamicTooltipContributionFilter) filterClass
										.newInstance();
							} catch (Exception e) {
								System.err.println("Invalid definition:\n"
										+ child.toString());
								System.err
										.println("filter class can't be instantiated!");
								e.printStackTrace();
							}
						} else {
							System.err.println("Invalid definition:\n"
									+ child.toString());
							System.err
									.println("filter class must implement interface "
											+ IDynamicTooltipContributionFilter.class
													.getName());
						}
					} catch (ClassNotFoundException cnfe) {
						System.err.println("Invalid definition:\n"
								+ child.toString());
						System.err.println("filter class not found: "
								+ filterClassName);
					}
				}

				enablements.add(enablement);
			}
		}

		return enablements;
	}

	public static class ToolbarItemContribution implements
			Comparable<ToolbarItemContribution>, IPropertyChangeListener,
			Cloneable {

		Bundle contributor;

		ImageDescriptor icon;

		ImageDescriptor disabledIcon;

		ImageDescriptor hoverIcon;

		String label;

		Integer priority;

		IAction action;
		
		String actionId;

		ITooltipActionProvider actionProvider;

		String tooltip;

		String tooltipStyle;
		
		String target;

		List<Enablement> enablements = new ArrayList<Enablement>();

		boolean disabled;

		@Override
		public Object clone() throws CloneNotSupportedException {
			ToolbarItemContribution clone = (ToolbarItemContribution) super
					.clone();
			return clone;
		}

		public int compareTo(ToolbarItemContribution o) {
			// items without specified priority place at the end
			if (priority == null) {
				return -1;
			}

			if (o.priority == null) {
				return 1;
			}

			int diff = priority - o.priority;

			if (diff == 0) {
				diff = -1;
			}
			return diff;
		}

		public ImageDescriptor getIcon() {
			if (icon != null) {
				return icon;
			}

			if (getAction() != null
					&& getAction().getImageDescriptor() != null) {
				return getAction().getImageDescriptor();
			}

			return ImageDescriptor.getMissingImageDescriptor();
		}

		public void setIcon(ImageDescriptor icon) {
			PropertyChangeEvent event = new PropertyChangeEvent(this,
					IAction.IMAGE, this.icon, icon);

			this.icon = icon;

			propertyChange(event);
		}

		public ImageDescriptor getDisabledIcon() {
			if (disabledIcon != null) {
				return disabledIcon;
			}

			if (getAction() != null
					&& getAction().getDisabledImageDescriptor() != null) {
				return getAction().getDisabledImageDescriptor();
			}

			return ImageDescriptor
					.createWithFlags(getIcon(), SWT.IMAGE_DISABLE);
		}

		public void setDisabledIcon(ImageDescriptor disabledIcon) {
			PropertyChangeEvent event = new PropertyChangeEvent(this,
					IAction.IMAGE, this.disabledIcon, disabledIcon);

			this.disabledIcon = disabledIcon;

			propertyChange(event);
		}

		public ImageDescriptor getHoverIcon() {
			if (hoverIcon != null) {
				return hoverIcon;
			}

			if (getAction() != null
					&& getAction().getHoverImageDescriptor() != null) {
				return getAction().getHoverImageDescriptor();
			}

			return hoverIcon;
		}

		public void setHoverIcon(ImageDescriptor hoverIcon) {
			PropertyChangeEvent event = new PropertyChangeEvent(this,
					IAction.IMAGE, this.hoverIcon, hoverIcon);

			this.hoverIcon = hoverIcon;

			propertyChange(event);
		}

		public List<Enablement> getEnablements() {
			return enablements;
		}

		public void setEnablements(List<Enablement> enablements) {
			this.enablements = enablements;
		}

		public String getLabel() {
			if (label != null) {
				return label;
			}

			if (getAction() != null
					&& getAction().getText() != null) {
				return getAction().getText();
			}

			return label;
		}

		public void setLabel(String label) {
			PropertyChangeEvent event = new PropertyChangeEvent(this,
					IAction.TEXT, this.label, label);

			this.label = label;

			propertyChange(event);
		}

		public ITooltipActionProvider getActionProvider() {
			return actionProvider;
		}

		public void setOnClickActionClass(ITooltipActionProvider actionProvider) {
			this.actionProvider = actionProvider;
			setAction(null);
		}

		public Integer getPriority() {
			return priority;
		}

		public void setPriority(Integer priority) {
			this.priority = priority;
		}

		public IAction getAction() {
			if (action == null && actionId != null) {
				try {
					if (actionProvider != null) {
						setAction(actionProvider.getAction(contributor, actionId));
					}
					else {
						setAction(new DefaultTooltipActionProvider().getAction(contributor, actionId));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return action;
		}

		public void setAction(IAction action) {
			if (this.action != null && this.listeners.size() > 0) {
				this.action.removePropertyChangeListener(this);
			}

			this.action = action;

			if (this.action != null && this.listeners.size() > 0) {
				this.action.addPropertyChangeListener(this);
			}
		}

		public String getTooltip() {
			return tooltip;
		}

		public void setTooltip(String targetContext) {
			this.tooltip = targetContext;
		}

		public boolean isDisabled() {
			if (disabled
					|| (getAction() != null && !getAction()
							.isEnabled())) {
				return true;
			}

			return false;
		}

		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
		}

		List<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();

		public void addPropertyChangeListener(IPropertyChangeListener listener) {
			synchronized (listeners) {
				if (!listeners.contains(listener)) {
					listeners.add(listener);

					// add listener on action on demand
					if (listeners.size() == 1 && getAction() != null) {
						getAction().addPropertyChangeListener(this);
					}
				}
			}
		}

		public void removePropertyChangeListener(
				IPropertyChangeListener listener) {
			synchronized (listeners) {
				listeners.remove(listener);

				// remove listener on action if no listeners registered
				if (listeners.size() == 0 && getAction() != null) {
					getAction().removePropertyChangeListener(this);
				}
			}
		}

		public void propertyChange(PropertyChangeEvent event) {
			synchronized (listeners) {
				for (IPropertyChangeListener listener : listeners) {
					listener.propertyChange(event);
				}
			}
		}

		public void dispose() {
			if (getAction() != null) {
				getAction().removePropertyChangeListener(this);
			}
		}

		public Bundle getContributor() {
			return contributor;
		}

		public void setContributor(Bundle contributor) {
			this.contributor = contributor;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}
	}

	public static class SectionContribution implements
			Comparable<SectionContribution>, Cloneable {

		Integer priority;

		Class<IDynamicTooltipUIContribution> uiClass;

		List<Enablement> enablements = new ArrayList<Enablement>();

		@Override
		public Object clone() throws CloneNotSupportedException {
			SectionContribution clone = (SectionContribution) super.clone();
			return clone;
		}

		public int compareTo(SectionContribution o) {
			// items without specified priority place at the end
			if (priority == null) {
				return -1;
			}

			if (o.priority == null) {
				return 1;
			}

			int diff = priority - o.priority;

			if (diff == 0) {
				diff = -1;
			}
			return diff;
		}

		public List<Enablement> getEnablements() {
			return enablements;
		}

		public void setEnablements(List<Enablement> enablements) {
			this.enablements = enablements;
		}

		public Integer getPriority() {
			return priority;
		}

		public void setPriority(Integer priority) {
			this.priority = priority;
		}

		public Class<IDynamicTooltipUIContribution> getUiClass() {
			return uiClass;
		}

		public void setUiClass(Class<IDynamicTooltipUIContribution> uiClass) {
			this.uiClass = uiClass;
		}
	}

	public static class Enablement {
		Boolean focused;

		Boolean unfocused;

		String uiContainerId;

		String context;

		IDynamicTooltipContributionFilter filter;

		public IDynamicTooltipContributionFilter getFilter() {
			return filter;
		}

		public void setFilter(IDynamicTooltipContributionFilter filter) {
			this.filter = filter;
		}

		public Boolean getFocused() {
			return focused;
		}

		public void setFocused(Boolean focused) {
			this.focused = focused;
		}

		public String getUiContainerId() {
			return uiContainerId;
		}

		public void setUiContainerId(String uiContainerId) {
			this.uiContainerId = uiContainerId;
		}

		public Boolean getUnfocused() {
			return unfocused;
		}

		public void setUnfocused(Boolean unfocused) {
			this.unfocused = unfocused;
		}

		public String getContext() {
			return context;
		}

		public void setContext(String context) {
			this.context = context;
		}
	}

	public EStyle getStyle() {
		return style;
	}

	public void setTitle(String title) {
		if (informationControl != null) {
			((ExtendedCompositeInformationControl) informationControl)
					.setTitleText(title);
		}
	}

}
