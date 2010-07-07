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
package com.nokia.tools.s60.editor.ui.dialogs;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.svg.ColorGroupsStore;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.s60.editor.ui.views.Messages;

public class LayerPropertiesDialog extends Dialog {

	public static final int CHECK_COL_SIZE = 20;

	private static final String EFFECT_ENABLED_COL = "enabled"; 

	private static final String EFFECT_NAME_COL = "name"; 

	public static final String[] EFFECT_TABLE_COLUMNS = new String[] {
			EFFECT_ENABLED_COL, EFFECT_NAME_COL };

	private ILayer layer;

	private List TABLE_COLUMNS_LIST = Arrays.asList(EFFECT_TABLE_COLUMNS);

	private TableViewer effectsViewer;

	Text layerName;

	private IProject project;

	public LayerPropertiesDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite control = (Composite) super.createDialogArea(parent);

		Composite root = new Composite(control, SWT.NONE);
		root.setLayout(new GridLayout(2, false));

		Label lblLayername = new Label(root, SWT.NONE);
		lblLayername.setText(Messages.LayerPropertiesDialog_label);

		layerName = new Text(root, SWT.BORDER);
		layerName.setTextLimit(ILayer.LAYER_NAME_LIMIT);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 150;
		layerName.setLayoutData(gd);
		layerName.forceFocus();
		layerName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				// check if layer name is valid
				String name = layerName.getText();
				if (!StringUtils.isEmpty(name)) {
					if (layer.getParent().getLayer(name) != null) {
						getButton(IDialogConstants.OK_ID).setEnabled(false);
					} else {
						getButton(IDialogConstants.OK_ID).setEnabled(true);
					}
					getButton(IDialogConstants.OK_ID).setEnabled(
							!(IMediaConstants.BackgroundLayer
									.equalsIgnoreCase(name)));
				} else {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
			}
		});

		// effects list
		// createEffectsGroup(root);

		return control;
	}

	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.CLOSE_ID == buttonId) {
			closePressed();
		} else if (IDialogConstants.OK_ID == buttonId) {
			try {
				String oldName = layer.getName();

				layer.setName(layerName.getText());
				if (project != null) {
					if (ColorGroupsStore.isEnabled) {
						ColorGroupsStore.changeLayerNameIfPresent(project,
								layer.getParent().getId(), oldName, layerName
										.getText());
					}
				}
			} catch (Exception e) {
			}
			super.buttonPressed(buttonId);
		} else {
			super.buttonPressed(buttonId);
		}
	}

	protected void closePressed() {
		setReturnCode(OK);
		close();
	}

	public class EffectsCellModifier implements ICellModifier {

		public boolean canModify(Object element, String property) {
			int columnIndex = TABLE_COLUMNS_LIST.indexOf(property);
			boolean returnValue;
			switch (columnIndex) {
			case 0:
				ILayerEffect effect = (ILayerEffect) element;
				boolean enabled = false;
				for (Iterator iter = effect.getParent()
						.getAvailableLayerEffects().iterator(); iter.hasNext();) {
					ILayerEffect availableEffect = (ILayerEffect) iter.next();
					if (availableEffect.getName().equals(effect.getName())) {
						enabled = true;
						break;
					}
				}
				returnValue = enabled;
				break;
			case 1: 
				returnValue = false;
				break;
			default:
				returnValue = false;
			}
			return returnValue;
		}

		public Object getValue(Object element, String property) {
			// Find the index of the column
			int columnIndex = TABLE_COLUMNS_LIST.indexOf(property);
			Object result = null;
			ILayerEffect effect = (ILayerEffect) element;

			switch (columnIndex) {
			case 0: // enabled column
				result = new Boolean(effect.isSelected());
				break;
			case 1: // name column
				result = effect.getName();
				break;
			default:
				result = "";
			}
			return result;
		}

		public void modify(Object element, String property, Object value) {
			// Find the index of the column
			int columnIndex = TABLE_COLUMNS_LIST.indexOf(property);

			TableItem item = (TableItem) element;
			ILayerEffect effect = (ILayerEffect) item.getData();

			switch (columnIndex) {
			case 0: // Enabled column
				effect.setSelected(((Boolean) value).booleanValue());
				break;
			case 1: // Name column not modifiable
				break;
			default:
			}
			// refresh
			effectsViewer.refresh();
		}

	}

	public void setLayer(ILayer layer) {
		this.layer = layer;

		if (getShell() != null) {
			getShell().setText(Messages.LayerPropertiesDialog_title);
		}

		layerName.setText(layer.getName());
		layerName.setSelection(0, layer.getName().length());
		// effectsViewer.setInput(layer.getLayerEffects());

		// layout
		getContents().pack(true);
		initializeBounds();

		// updateTableColumnSizes();
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	

}
