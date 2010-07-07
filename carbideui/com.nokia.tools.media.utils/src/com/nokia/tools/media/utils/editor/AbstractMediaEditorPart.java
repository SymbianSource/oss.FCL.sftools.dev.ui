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
/**
 * 
 */
package com.nokia.tools.media.utils.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.editor.frameanimation.FrameAnimActionFactory;
import com.nokia.tools.media.utils.editor.frameanimation.FrameAnimationContainer;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IEffectParameter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ISelectionListener;
import com.nokia.tools.media.utils.timeline.ITimeLine;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.ITimeListener;
import com.nokia.tools.media.utils.timeline.ITreeTimeLineDataProvider;
import com.nokia.tools.media.utils.timeline.impl.TimeLine;
import com.nokia.tools.media.utils.timeline.impl.TimeLineRow;
import com.nokia.tools.media.utils.timeline.impl.TreeTimeLine;
import com.nokia.tools.media.utils.timeline.impl.TreeTimeLineViewer;

/**
 * Abstract implementation for multimedia Graphical editor part. Editor enables
 * presenting multiple heterogenous content (each of the content with multiple
 * layers) controlling preview and editing along time dimension. This class is
 * to be inherited for different animation / multimedia technologies that needs
 * to have timeline controled preview and editing. Initial implementation got
 * refactored from com.nokia.tools.s60.editor.GraphicsEditorInput.
 * 
 */
public abstract class AbstractMediaEditorPart extends EditorPart implements
		IPropertyChangeListener, ISelectionListener, ITimeListener,
		ICheckStateListener, PropertyChangeListener, ISelectionProvider {

	/** min. height for animation-controls composite in bottom part fo editor */
	private static final int MINIMUM_ANIM_CONTROLS_HEIGHT = 140;

	private ActionRegistry actionRegistry = new ActionRegistry();

	// commmand stack for inner undo/redo in this editor
	private CommandStack commandStack = new CommandStack();

	// element we are editing
	private IImage element;

	/*
	 * list of IImagePreviews, currently editor supports only one element at
	 * time
	 */
	protected List<AbstractFramePreview> imagePreviews = new ArrayList<AbstractFramePreview>();

	// timeline
	private TreeTimeLineViewer timeLineViewer;

	// currently selected display attrs. in timeline
	protected TimingModel timeModel = TimingModel.RealTime;
	protected TimeSpan timeSpan;

	private SashForm sashForm;
	private Composite animationControls;
	private boolean animationControlsVisible;

	private IStructuredSelection _lastSelection;

	private Map<Object, BackupData> displayData = new HashMap<Object, BackupData>();

	private static class BackupData {
		private Long oldDisplayStart;
		private Long oldDisplayWidth;
		private Long oldCurrentTime;
	}

	protected List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	protected TreeTimeLineViewer getTimelineViewer() {
		return timeLineViewer;
	}

	protected TimingModel getTimingModel() {
		return timeModel;
	}

	public CommandStack getCommandStack() {
		return commandStack;
	}

	public ITimeLine getTimeline() {
		if (null == getTimelineViewer())
			return null;
		return getTimelineViewer().getTimeLine();
	}

	public IImage getActiveImage() {
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (element instanceof IAnimatedImage) {
			sashForm = new SashForm(parent, SWT.VERTICAL) {
				boolean first = true;

				protected void checkSubclass() {
				};

				@Override
				public void layout(boolean changed, boolean all) {
					super.layout(changed, all);
					if (!first) {
						return;
					}

					Control[] children = sashForm.getChildren();
					Sash lastSash = null;
					for (Control control : children) {
						if (control instanceof Sash) {
							lastSash = (Sash) control;
						}
					}
					if (lastSash == null) {
						return;
					}
					first = false;
					lastSash.addPaintListener(new PaintListener() {
						public void paintControl(PaintEvent e2) {
							GC graphics = e2.gc;

							Rectangle area = new Rectangle(0, 0,
									((Control) e2.widget).getSize().x,
									((Control) e2.widget).getSize().y);

							int centerY = area.height / 2;

							graphics
									.setForeground(ColorConstants.buttonLightest);

							graphics.drawLine(area.x /* + H_GAP */,
									centerY - 1, area.x + area.width /*- H_GAP */
											- 1, centerY - 1);
							graphics.drawLine(area.x /* + H_GAP */,
									centerY - 1, area.x + area.width /*- H_GAP */
											- 1, centerY - 1);

							graphics.setForeground(ColorConstants.buttonDarker);

							graphics.drawLine(area.x /* + H_GAP */, centerY,
									area.x + area.width /*- H_GAP */- 1,
									centerY);
							graphics.drawLine(area.x /* + H_GAP */, centerY,
									area.x + area.width /*- H_GAP */- 1,
									centerY);
						};
					});

					lastSash.addMouseListener(new MouseAdapter() {
						public void mouseDoubleClick(MouseEvent e) {
							if (e.button == 1) {
								animationControlsVisible = !animationControlsVisible;
								UtilsPlugin
										.getDefault()
										.getPreferenceStore()
										.setValue(
												IMediaConstants.PREF_ANIMATION_CONTROL_VISIBLE,
												animationControlsVisible);
								updateSashWeights();
							}
						};
					});
				};
			};
		} else {
			sashForm = new SashForm(parent, SWT.VERTICAL);
		}

		createPreviewComposite(sashForm);
		createTimeLine(sashForm);
		if (element instanceof IAnimatedImage) {
			contributeToCombo(((TimeLine) getTimeline()).getTopToolBar());
		}

		if (element instanceof IAnimatedImage) {
			sashForm.setWeights(new int[] { 40, 12, 0 });
		} else {
			sashForm.setWeights(new int[] { 40, 12 });
		}

		if (element != null)
			refresh(element);

	}

	protected abstract void createPreviewComposite(Composite parent);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		sashForm.setFocus();
	}

	private void updateSashWeights() {
		if (!(element instanceof IAnimatedImage)) {
			return;
		}

		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					updateSashWeights();
				};
			});
			return;
		}

		int[] weights = sashForm.getWeights();
		int sumWeights = 0;
		for (int i : weights) {
			sumWeights += i;
		}

		int parentHeight = sashForm.getParent().getSize().y
				- sashForm.SASH_WIDTH * weights.length;
		int timeLineHeight = ((TimeLine) getTimeline()).getSize().y;
		int animControlsHeight;
		if (!animationControlsVisible) {
			animControlsHeight = 20; // SashForm drag limit... :-(((((((
		} else if (animationControls.getChildren().length == 0) {
			animControlsHeight = MINIMUM_ANIM_CONTROLS_HEIGHT;
		} else {
			animControlsHeight = Math.max(animationControls.getChildren()[0]
					.computeSize(SWT.DEFAULT, SWT.DEFAULT).y,
					MINIMUM_ANIM_CONTROLS_HEIGHT);
		}
		int topHeight = Math.max(0, parentHeight - timeLineHeight
				- animControlsHeight);

		sashForm.setWeights(new int[] { topHeight, timeLineHeight,
				animControlsHeight });
	}

	private void createTimeLineMenu() {
		Tree timeLineTree = ((TreeTimeLine) getTimeline()).getTreeViewer()
				.getTree();
		MenuManager timeLineTreeMenuManager = new MenuManager();
		timeLineTreeMenuManager.setRemoveAllWhenShown(true);
		Menu menu = timeLineTreeMenuManager.createContextMenu(timeLineTree);
		timeLineTree.setMenu(menu);
		contributeTimeLineMenu(timeLineTreeMenuManager);
	}

	protected void contributeTimeLineMenu(MenuManager menuManager) {

	}

	private void createTimeModelCombo(final TimeLine tl) {
		final ToolBar tb = tl.getTopToolBar();
		final Composite bottom = tl.getBottomComposite();

		final Label sceneLabel = new Label(bottom, SWT.NONE);
		sceneLabel.setText(getTimeModelInfo(timeModel));

		contributeToTimeModelCombo(tb, sceneLabel);
	}

	protected abstract void contributeToTimeModelCombo(final ToolBar tb,
			final Label sceneLabel);
	
	protected abstract void contributeToCombo(final ToolBar tb);

	private void createTimeLine(final Composite parent) {

		if (element instanceof IAnimatedImage) {
			timeLineViewer = new TreeTimeLineViewer(parent, SWT.NONE);
		} else {
			timeLineViewer = new TreeTimeLineViewer(parent, SWT.CHECK);
			((CheckboxTreeViewer) timeLineViewer.getTimeLine().getTreeViewer())
					.addCheckStateListener(this);
		}

		createTimeLineMenu();

		createTimeModelCombo(((TimeLine) getTimeline()));
		
		getTimeline().addTimeListener(this);
		getTimeline().addSelectionListener(this);

		getSite().setSelectionProvider(this);

		if (element instanceof IAnimatedImage) {
			// create container for row's effects
			animationControls = new Composite(parent, SWT.NONE);
			animationControls.setLayout(new FillLayout());

			if (UtilsPlugin.getDefault().getPreferenceStore().contains(
					IMediaConstants.PREF_ANIMATION_CONTROL_VISIBLE)) {
				animationControlsVisible = UtilsPlugin.getDefault()
						.getPreferenceStore().getBoolean(
								IMediaConstants.PREF_ANIMATION_CONTROL_VISIBLE);
			} else {
				UtilsPlugin.getDefault().getPreferenceStore().setDefault(
						IMediaConstants.PREF_ANIMATION_CONTROL_VISIBLE, true);
			}

			animationControlsVisible = true;
		}

		// initialize sash weight on first paint
		((TimeLine) getTimeline()).addPaintListener(new PaintListener() {
			boolean first = true;

			public void paintControl(PaintEvent e) {
				if (first) {
					updateSashWeights();
					first = false;
				}
			};
		});
	}

	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element = event.getElement();
		if (element instanceof ILayer) {
			((ILayer) element).setAnimatedInPreview(event.getChecked());
			for (ILayerEffect effect : ((ILayer) element)
					.getSelectedLayerEffects()) {
				((CheckboxTreeViewer) timeLineViewer.getTimeLine()
						.getTreeViewer())
						.setChecked(effect, event.getChecked());
				((CheckboxTreeViewer) timeLineViewer.getTimeLine()
						.getTreeViewer())
						.setGrayed(effect, !event.getChecked());
				timeLineViewer.getTimeLine().getTreeViewer().refresh(effect);
			}
		} else if (element instanceof ILayerEffect) {
			if (!((ILayerEffect) element).getParent().isAnimatedInPreview()) {
				((CheckboxTreeViewer) timeLineViewer.getTimeLine()
						.getTreeViewer()).setChecked(element, false);
			} else {
				((ILayerEffect) element).setAnimatedInPreview(event
						.getChecked());
			}
		}

		
		imagePreviews.get(0).refreshPreviewImage(timeModel,
				getTimeline().getCurrentTime());
	}

	/*
	 * for each row: 1. find all atributes, that are animated in given time
	 * model 2. set CPMFilterWraper properties to display CP's only from this
	 * @param img @param timeModel @param span
	 */
	private void updateControlPointsFiltering(IImage img,
			TimingModel timeModel, TimeSpan span) {
		img.updateControlPointsFiltering(timeModel, span);
	}

	/*
	 * clear timeline - related model nodes from model.
	 */
	private void clearTimeLineNodes(IImage img) {
		img.clearTimeLineNodes();
	}

	/**
	 * Refreshes UI state with respect to active object
	 * 
	 * @param selection
	 */
	protected void refresh(IImage selection) {
		refreshTimeLine(selection, false, null);
	}

	protected void refreshTimeLine(IImage img) {
		refreshTimeLine(img, true, null);
	}

	protected void refreshTimeLine(IImage img, boolean preserveDisplayData,
			BackupData backupData) {
		ITreeTimeLineDataProvider provider = null;

		// need to remove old timeline model nodes
		clearTimeLineNodes(img);

		// set proper filter data to control point model to show only params and
		// CP's in current timing model
		updateControlPointsFiltering(img, timeModel, timeSpan);

		if (timeModel == TimingModel.RealTime) {
			provider = img.getRealTimeTimeModelTimeLineProvider();
		} else if (timeModel == TimingModel.Relative) {
			provider = img.getRelativeTimeModelTimeLineProvider(timeSpan);
		}

		if (provider != null) {
			if (preserveDisplayData && backupData == null) {
				backupData = new BackupData();
				backupData.oldDisplayStart = timeLineViewer.getTimeLine()
						.getDisplayData().getDisplayStartTime();
				backupData.oldDisplayWidth = timeLineViewer.getTimeLine()
						.getDisplayData().getDisplayWidthInTime();
				backupData.oldCurrentTime = timeLineViewer.getTimeLine()
						.getCurrentTime();
			}
			timeLineViewer.getTimeLine().initialize(provider);
			timeLineViewer.expandAll();

			if (preserveDisplayData && backupData != null) {
				if (backupData.oldDisplayStart != null) {
					timeLineViewer.getTimeLine().getDisplayData()
							.setDisplayStartTime(backupData.oldDisplayStart);
				}
				if (backupData.oldDisplayWidth != null) {
					timeLineViewer.getTimeLine().getDisplayData()
							.setDisplayWidthInTime(backupData.oldDisplayWidth);
				}
				if (backupData.oldCurrentTime != null) {
					long newTime = backupData.oldCurrentTime;
					timeLineViewer.getTimeLine().setCurrentTime(newTime);
				}
			}

			if (_lastSelection != null) {
				StructuredSelection s = (StructuredSelection) _lastSelection;
				Iterator it = s.iterator();
				while (it.hasNext()) {
					Object o = it.next();
					if (o instanceof ILayerEffect || o instanceof ILayer)
						timeLineViewer.getTimeLine().getTreeViewer()
								.setSelection(new StructuredSelection(o));
				}
			} else {
				// select first timeline row
				timeLineViewer.getTimeLine().getTreeViewer().getTree()
						.setFocus();
				ITimeLineRow[] row = timeLineViewer.getTimeLine().getRows();
				if (row.length > 0) {
					// no need, it will checked in the timeline
					timeLineViewer.getTimeLine().setSelectedRow(row[0]);
				}
			}

			if (img instanceof IAnimatedImage) {
				// show animation frames
				if (animationControls != null) {
					animationControls.setVisible(true);
				}
			} else {
				refreshAnimationControls(_lastSelection, true);
			}
		}
	}

	protected void setImage(IImage imageInput) {
		element = imageInput;
		element.addPropertyListener(this);

		// add listeners to parameters
		for (ILayer l : element.getLayers()) {
			for (ILayerEffect e : l.getLayerEffects()) {
				for (IEffectParameter p : e.getParameters()) {
					p.removePropertyChangeListener(this);
					p.addPropertyListener(this);
				}
			}
		}


		element.startAnimation();

	}

	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
		imagePreviews.get(0).invalidateCache();
		imagePreviews.get(0).refreshPreviewImage(timeModel,
				getTimeline().getCurrentTime());

	}

	// property changes from IImage
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof IImage) {

			if (IImage.PROPERTY_STATE.equals(evt.getPropertyName())
					|| IImage.PROPERTY_ANIMATION_STATE.equals(evt
							.getPropertyName())) {
				((TimeLine) getTimeline()).repaintAll();
			} else if (IImage.PROPERTY_STRUCTURE_CHANGE.equals(evt
					.getPropertyName())) {
				refreshTimeLine(getActiveImage());
				// add listeners to possible newly created params:
				// add listeners to parameters
				for (ILayer l : getActiveImage().getLayers()) {
					for (ILayerEffect e : l.getLayerEffects()) {
						for (IEffectParameter p : e.getParameters()) {
							p.removePropertyChangeListener(this);
							p.addPropertyListener(this);
						}
					}
				}
			}
		}
		if (evt.getSource() instanceof IEffectParameter) {
			if (IEffectParameter.PROPERTY_TIMING.equals(evt.getPropertyName())) {
				IEffectParameter parem = (IEffectParameter) evt.getSource();
				if (parem.isAnimated()) {
					TimingModel mdl = parem.getTimingModel();
					TimeSpan span = parem.getTimeSpan();
					refreshTimeLine(mdl, span, getActiveImage());
				} else {
					refreshTimeLine(getActiveImage());
				}
				if (_lastSelection != null)
					refreshAnimationControls(_lastSelection, true);
				timeLineViewer.getTimeLine().getTreeViewer().setSelection(
						new StructuredSelection(parem.getParent()), true);
				((TreeTimeLine) timeLineViewer.getTimeLine())
						.setRowSelected(parem.getParent());
			}
			if (IEffectParameter.PROPERTY_ANIMATED
					.equals(evt.getPropertyName())) {
				IEffectParameter parem = (IEffectParameter) evt.getSource();
				refreshTimeLine(getActiveImage());
				if (_lastSelection != null)
					refreshAnimationControls(_lastSelection, true);
				timeLineViewer.getTimeLine().getTreeViewer().setSelection(
						new StructuredSelection(parem.getParent()), true);
				((TreeTimeLine) timeLineViewer.getTimeLine())
						.setRowSelected(parem.getParent());
			}
		}

		if (evt.getSource() instanceof IAnimatedImage) {
			refreshTimeLine(getActiveImage());
		}
	}

	
	protected String getTimeModelInfo(TimingModel model) {
		return "";
	}

	protected void refreshTimeLine(TimingModel mdl, final TimeSpan timeSpan,
			IImage img) {
		if (mdl == null)
			mdl = TimingModel.RealTime;

		String key = "" + this.timeModel + ":" + this.timeSpan;
		BackupData bd = new BackupData();
		bd.oldCurrentTime = timeLineViewer.getTimeLine().getCurrentTime();
		bd.oldDisplayStart = timeLineViewer.getTimeLine().getDisplayData()
				.getDisplayStartTime();
		bd.oldDisplayWidth = timeLineViewer.getTimeLine().getDisplayData()
				.getDisplayWidthInTime();
		displayData.put(key, bd);

		this.timeModel = mdl;
		this.timeSpan = timeSpan;

		key = "" + mdl + ":" + timeSpan;
		bd = displayData.get(key);
		refreshTimeLine(img, bd != null, bd);

		try {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					Label sceneLabel = (Label) timeLineViewer.getTimeLine()
							.getBottomComposite().getChildren()[1];
					// update scene label text
					sceneLabel.setText(getTimeModelInfo(timeModel));
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		throw new RuntimeException("not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (getTimeline() != null) {
			((TimeLine) getTimeline()).dispose();
		}
		if (imagePreviews != null) {
			for (AbstractFramePreview preview : imagePreviews) {
				preview.dispose();
			}
		}

		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.timeline.ITimeListener#timeChanged(long)
	 */
	public synchronized void timeChanged(long newTime) {
		
		imagePreviews.get(0).refreshPreviewImage(timeModel, newTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.timeline.ISelectionListener#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(final IStructuredSelection selection) {
		// called when timeline selection changed - row, row_node, control
		// point.

		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					selectionChanged(selection);
				}
			});
			return;
		}

		refreshAnimationControls(selection, false);

		List<ISelectionChangedListener> toNotify = new ArrayList<ISelectionChangedListener>();

		synchronized (listeners) {
			toNotify.addAll(listeners);
		}

		for (ISelectionChangedListener listener : toNotify) {
			try {
				listener.selectionChanged(new SelectionChangedEvent(this,
						getSelection()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * fill's contents of anim. row controls with adequate composite for
	 * selected row
	 * 
	 * @param sel
	 */
	protected void refreshAnimationControls(final IStructuredSelection sel,
			final boolean onProperty) {

		if (sel != null)
			_lastSelection = sel;

		if (animationControls == null) {
			return;
		}

		IAnimatedImage anim = null;
		ILayerEffect eff = null;

		if (sel != null) {
			Iterator it = sel.iterator();
			while (it.hasNext()) {
				Object o = it.next();
				if (o instanceof TimeLineRow) {
					// found
					Object source = ((TimeLineRow) o).getSource();
					if (source instanceof ILayerEffect) {
						eff = (ILayerEffect) source;
						break;
					} else if (source instanceof IAnimatedImage) {
						anim = (IAnimatedImage) source;
						break;
					}
				}
			}
		}

		if (eff == null && anim == null) {
			clearComposite(animationControls);
		} else if (anim != null) {
			final IAnimatedImage selectedAnim = anim;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (animationControls.getChildren() != null)
						if (animationControls.getChildren().length > 0) {
							Composite cont = (Composite) animationControls
									.getChildren()[0];

							if (cont.getChildren().length > 0) {

								Control animCont = cont.getChildren()[0];

								if (animCont instanceof FrameAnimationContainer) {
									if (((FrameAnimationContainer) animCont)
											.getImage() == selectedAnim) {
										return; // selection didn't change
									}
								}
							}
						}

					// clear
					clearComposite(animationControls);

					// update anim controls composite
					_updateAnimControlCompositeForAnimatedImage(selectedAnim,
							animationControls);

					animationControls.layout();
					animationControls.redraw();
				}
			});
		}
	}

	private void _updateAnimControlCompositeForAnimatedImage(
			final IAnimatedImage image, final Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		// composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		FrameAnimationContainer controls = new FrameAnimationContainer(this,
				composite, SWT.NONE);
		controls.setActionFactory(getFrameAnimationEditingActionFactory());
		controls.selectionChanged(image, getTimeline());
		controls.addPropertyChangedListener(this);

		updateSashWeights();
	}

	/**
	 * Extension place provide custom frame manipulation actions, and
	 * contribution If editing includes frame animation method enables
	 * 
	 * @return
	 */
	protected abstract FrameAnimActionFactory getFrameAnimationEditingActionFactory();

	public ActionRegistry getActionRegistry() {
		return actionRegistry;
	}

	private void clearComposite(final Composite r) {
		if (animationControls == null) {
			return;
		}

		if (Display.getCurrent() == null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					clearComposite(r);
				}
			});
			return;
		}
		org.eclipse.swt.widgets.Control cntrl[] = r.getChildren();
		if (cntrl != null)
			for (int i = 0; i < cntrl.length; i++) {
				try {
					cntrl[i].dispose();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}

	protected Composite getRootControl() {
		return sashForm;
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
			getTimeline().addSelectionListener(this);
		}
	}

	public ISelection getSelection() {
		if (element instanceof IAnimatedImage) {
			if (((TreeTimeLine) getTimeline()).getSelectedControlPoint() != null) {
				return new StructuredSelection(((TreeTimeLine) getTimeline())
						.getSelectedControlPoint());
			}
		}

		return ((TreeTimeLine) getTimeline()).getTreeViewer().getSelection();
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public void setSelection(ISelection selection) {
		throw new UnsupportedOperationException();
	}

}
