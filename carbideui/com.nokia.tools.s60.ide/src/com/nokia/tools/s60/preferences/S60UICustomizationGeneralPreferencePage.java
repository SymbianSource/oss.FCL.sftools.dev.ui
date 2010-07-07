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
package com.nokia.tools.s60.preferences;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.AbstractContentSourceManager;
import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.ContentSourceManager;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.core.IScreenFactory;
import com.nokia.tools.screen.ui.IScreenConstants;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

public class S60UICustomizationGeneralPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {

	public static final String PREFERENCES_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "preferences_context"; 

	// check 'ask on SVG to bitmap conversion'
	private Button svgConversionCheckbox;

	private Button silent9PieceReplaceCheck;

	private Button silentSinglePieceReplaceCheck;

	private Button autoSaveBeforePackagingCheck;

	private Button hidePreviewNotAvailableCheck;

	private Button showFloatingToolbarCheck;

	private Button showScreenAnimationCheck;

	private Button zoomGlobalRadio, zoomIndividualRadio;

	private List<IContent> contents;

	private Map<IContent, Button[]> galleryCheckboxMap = new HashMap<IContent, Button[]>();

	@Override
	protected Control createContents(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				PREFERENCES_CONTEXT);

		contents = new ArrayList<IContent>();
		for (String type : AbstractContentSourceManager.getContentTypes()) {
			if (ScreenUtil.isPrimaryContent(type)) {
				try {
					contents.addAll(ContentSourceManager.getGlobalInstance()
							.getRootContents(type, null));
				} catch (Exception e) {
					S60WorkspacePlugin.error(e);
				}
			}
		}

		Composite generalComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		generalComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		generalComposite.setLayoutData(gd);

		setDescription(Messages.S60UICustomizationGeneralPreferencePage_generalPreferencesLabel);
		createDescriptionLabel(generalComposite);

		TabFolder folder = new TabFolder(generalComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalIndent = 5;
		folder.setLayoutData(gd);

		TabItem appearanceItem = new TabItem(folder, SWT.NONE);
		appearanceItem
				.setText(Messages.S60UICustomizationGeneralPreferencePage_appearanceLabel);
		TabItem confirmationItem = new TabItem(folder, SWT.NONE);
		confirmationItem
				.setText(Messages.S60UICustomizationGeneralPreferencePage_confirmationLabel);

		TabItem displayItem = new TabItem(folder, SWT.NONE);
		displayItem
				.setText(Messages.S60UICustomizationGeneralPreferencePage_displayLabel);

		Composite appearanceComposite = createComposite(folder, 1);
		appearanceItem.setControl(appearanceComposite);
		Composite confirmationComposite = createComposite(folder, 1);
		confirmationItem.setControl(confirmationComposite);
		Composite displayComposite = createComposite(folder, 1);
		displayItem.setControl(displayComposite);

		createAppearanceControl(appearanceComposite);
		createConfirmationControl(confirmationComposite);
		createDisplayControl(displayComposite);

		initializeValues();
		return parent;
	}

	private void createConfirmationControl(Composite confirmationComposite) {
		Group grpConversions = new Group(confirmationComposite, SWT.NONE);
		grpConversions
				.setText(Messages.S60UICustomizationGeneralPreferencePage_conversionConfirmationInfoLabel);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		grpConversions.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpConversions.setLayout(layout);

		svgConversionCheckbox = new Button(grpConversions, SWT.CHECK);
		svgConversionCheckbox
				.setText(Messages.S60UICustomizationGeneralPreferencePage_silentSVGCheck);

		silent9PieceReplaceCheck = new Button(grpConversions, SWT.CHECK);
		silent9PieceReplaceCheck
				.setText(Messages.S60UICustomizationGeneralPreferencePage_silent9PieceReplaceCheck);

		silentSinglePieceReplaceCheck = new Button(grpConversions, SWT.CHECK);
		silentSinglePieceReplaceCheck
				.setText(Messages.S60UICustomizationGeneralPreferencePage_silent9PieceFillPartsCheck);

		autoSaveBeforePackagingCheck = new Button(confirmationComposite,
				SWT.CHECK);
		autoSaveBeforePackagingCheck
				.setText(Messages.S60UICustomizationGeneralPreferencePage_autoSaveBeforePackagingCheck);
	}

	private void createAppearanceControl(Composite appearanceComposite) {

		if (!contents.isEmpty()) {
			if (contents.size() > 1) {
				CTabFolder subFolder = new CTabFolder(appearanceComposite,
						SWT.NONE);
				GridData gd = new GridData(GridData.FILL_BOTH);
				subFolder.setLayoutData(gd);
				subFolder.setBorderVisible(true);
				subFolder.setSimple(false);
				for (IContent content : contents) {
					IPlatform platform = (IPlatform) content
							.getAttribute(ContentAttribute.PLATFORM.name());
					CTabItem screensItem = new CTabItem(subFolder, SWT.NONE);

					screensItem
							.setText(Messages.S60UICustomizationGeneralPreferencePage_seriesTabText
									+ " "
									+ (platform.toString()).substring(1, 3));
					Composite galleryComposite = createComposite(subFolder, 1);
					GridLayout layout = new GridLayout();
					layout.verticalSpacing = 7;
					galleryComposite.setLayout(layout);
					galleryComposite.setLayoutData(new GridData(
							GridData.FILL_BOTH));
					screensItem.setControl(galleryComposite);

					createGalleryControl(content, galleryComposite);
				}
				subFolder.setSelection(0);
			} else {
				for (IContent content : contents)
					createGalleryControl(content, appearanceComposite);
			}
		}
	}

	private void createGalleryControl(IContent content,
			Composite galleryComposite) {
		Group grpGallery = new Group(galleryComposite, SWT.NONE);
		grpGallery
				.setText(Messages.S60UICustomizationGeneralPreferencePage_galleryLabel);

		GridData gd = new GridData(GridData.FILL_BOTH);
		grpGallery.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpGallery.setLayout(layout);

		Set<String> screens = new TreeSet<String>(new Comparator<String>() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(T, T)
			 */
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		IScreenFactory factory = (IScreenFactory) content
				.getAdapter(IScreenFactory.class);

		for (IContentData child : factory.getScreens()) {
			//this check ensures that this screen is available in the gallery.
			
			IScreenAdapter adapter = (IScreenAdapter) child
					.getAdapter(IScreenAdapter.class);
			if (adapter != null && adapter.isModelScreen())
				screens.add(child.getName());
		}

		final Button[] galleryCheckboxes = new Button[screens.size()];
		int i = 0;
		for (String screen : screens) {
			galleryCheckboxes[i] = new Button(grpGallery, SWT.CHECK);
			galleryCheckboxes[i++].setText(screen);
		}
		galleryCheckboxMap.put(content, galleryCheckboxes);

		Composite buttonComposite = new Composite(grpGallery, SWT.NULL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.RIGHT;
		gd.verticalAlignment = SWT.BOTTOM;
		buttonComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		buttonComposite.setLayout(layout);

		Button clearAllButton = new Button(buttonComposite, SWT.NONE);
		clearAllButton
				.setText(Messages.S60UICustomizationGeneralPreferencePage_clearAllLabel);
		calculateButtonSize(clearAllButton);
		clearAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				for (Button btn : galleryCheckboxes) {
					btn.setSelection(false);
				}
			}
		});

		Button selectAllButton = new Button(buttonComposite, SWT.NONE);
		selectAllButton
				.setText(Messages.S60UICustomizationGeneralPreferencePage_selectAllLabel);
		calculateButtonSize(selectAllButton);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				for (Button btn : galleryCheckboxes) {
					btn.setSelection(true);
				}
			}
		});
	}

	private void createDisplayControl(Composite displayComposite) {
		Group grpZoom = new Group(displayComposite, SWT.NONE);
		grpZoom
				.setText(Messages.S60UICustomizationGeneralPreferencePage_zoomLabel);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		grpZoom.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpZoom.setLayout(layout);

		zoomGlobalRadio = new Button(grpZoom, SWT.RADIO);
		zoomGlobalRadio
				.setText(Messages.S60UICustomizationGeneralPreferencePage_zoomGlobalText);

		zoomIndividualRadio = new Button(grpZoom, SWT.RADIO);
		zoomIndividualRadio
				.setText(Messages.S60UICustomizationGeneralPreferencePage_zoomIndividualText);

		hidePreviewNotAvailableCheck = new Button(displayComposite, SWT.CHECK);
		hidePreviewNotAvailableCheck
				.setText(Messages.S60UICustomizationGeneralPreferencePage_HidePreviewNotAvailableCheck);

		showScreenAnimationCheck = new Button(displayComposite, SWT.CHECK);
		showScreenAnimationCheck
				.setText(Messages.S60UICustomizationGeneralPreferencePage_ShowScreenAnimationCheck);

		showFloatingToolbarCheck = new Button(displayComposite, SWT.CHECK);
		showFloatingToolbarCheck
				.setText(Messages.S60UICustomizationGeneralPreferencePage_ShowEditorTooltipCheck);
	}

	public void init(IWorkbench workbench) {
		
	}

	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);

		// GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.verticalSpacing = 7;
		composite.setLayout(layout);

		// GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}

	private void storeValuesIntoPreferenceStore() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IMediaConstants.PREF_SILENT_SVG_CONVERSION,
				svgConversionCheckbox.getSelection());
		store.setValue(IMediaConstants.PREF_NINE_PIECE_2SINGLE_ASK,
				this.silent9PieceReplaceCheck.getSelection());
		store.setValue(IMediaConstants.PREF_SINGLE_PIECE_2NINE_ASK,
				this.silentSinglePieceReplaceCheck.getSelection());

		
		store = S60WorkspacePlugin.getDefault().getPreferenceStore();
		store.setValue(IS60IDEConstants.PREF_NO_PREVIEW_HIDE_MESSAGEBOX,
				this.hidePreviewNotAvailableCheck.getSelection());
		store.setValue(IS60IDEConstants.PREF_SHOW_EDITOR_TOOLTIP,
				this.showFloatingToolbarCheck.getSelection());

		for (IContent content : contents) {
			StringBuilder sb = new StringBuilder();
			for (Button btn : galleryCheckboxMap.get(content)) {
				if (btn.getSelection()) {
					sb.append(btn.getText() + ",");
				}
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			store.setValue(IS60IDEConstants.PREF_GALLERY_SCREENS + "."
					+ content.getType(), sb.toString());
		}

		store = UiPlugin.getDefault().getPreferenceStore();
		store.setValue(IScreenConstants.PREF_SAVE_BEFORE_PACKAGING_ASK,
				this.autoSaveBeforePackagingCheck.getSelection());
		store.setValue(IScreenConstants.PREF_ZOOMING_GLOBAL, zoomGlobalRadio
				.getSelection());
		store.setValue(IScreenConstants.PREF_AUTO_ANIMATION_DISABLED,
				!showScreenAnimationCheck.getSelection());
	}

	public boolean performOk() {
		storeValuesIntoPreferenceStore();
		S60WorkspacePlugin.getDefault().savePluginPreferences();
		return true;
	}

	protected void performDefaults() {
		super.performDefaults();
		initializeDefaults();
	}

	private void initializeDefaults() {
		IPreferenceStore store = getPreferenceStore();
		svgConversionCheckbox.setSelection(store
				.getDefaultBoolean(IMediaConstants.PREF_SILENT_SVG_CONVERSION));
		silent9PieceReplaceCheck
				.setSelection(store
						.getDefaultBoolean(IMediaConstants.PREF_NINE_PIECE_2SINGLE_ASK));
		silentSinglePieceReplaceCheck
				.setSelection(store
						.getDefaultBoolean(IMediaConstants.PREF_SINGLE_PIECE_2NINE_ASK));

		store = S60WorkspacePlugin.getDefault().getPreferenceStore();
		hidePreviewNotAvailableCheck
				.setSelection(store
						.getDefaultBoolean(IS60IDEConstants.PREF_NO_PREVIEW_HIDE_MESSAGEBOX));
		showFloatingToolbarCheck.setSelection(store
				.getDefaultBoolean(IS60IDEConstants.PREF_SHOW_EDITOR_TOOLTIP));

		for (IContent content : contents) {
			String val = store
					.getDefaultString(IS60IDEConstants.PREF_GALLERY_SCREENS
							+ "." + content.getType());
			updateGalleryCheckboxes(content, val);
		}

		store = UiPlugin.getDefault().getPreferenceStore();
		autoSaveBeforePackagingCheck
				.setSelection(store
						.getDefaultBoolean(IScreenConstants.PREF_SAVE_BEFORE_PACKAGING_ASK));
		zoomGlobalRadio.setSelection(store
				.getDefaultBoolean(IScreenConstants.PREF_ZOOMING_GLOBAL));
		zoomIndividualRadio.setSelection(!zoomGlobalRadio.getSelection());
		showScreenAnimationCheck
				.setSelection(!store
						.getDefaultBoolean(IScreenConstants.PREF_AUTO_ANIMATION_DISABLED));
	}

	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		svgConversionCheckbox.setSelection(store
				.getBoolean(IMediaConstants.PREF_SILENT_SVG_CONVERSION));
		silent9PieceReplaceCheck.setSelection(store
				.getBoolean(IMediaConstants.PREF_NINE_PIECE_2SINGLE_ASK));
		silentSinglePieceReplaceCheck.setSelection(store
				.getBoolean(IMediaConstants.PREF_SINGLE_PIECE_2NINE_ASK));

		store = S60WorkspacePlugin.getDefault().getPreferenceStore();
		hidePreviewNotAvailableCheck.setSelection(store
				.getBoolean(IS60IDEConstants.PREF_NO_PREVIEW_HIDE_MESSAGEBOX));
		showFloatingToolbarCheck.setSelection(store
				.getBoolean(IS60IDEConstants.PREF_SHOW_EDITOR_TOOLTIP));

		for (IContent content : contents) {
			String val = store.getString(IS60IDEConstants.PREF_GALLERY_SCREENS
					+ "." + content.getType());
			updateGalleryCheckboxes(content, val);
		}

		store = UiPlugin.getDefault().getPreferenceStore();
		autoSaveBeforePackagingCheck.setSelection(store
				.getBoolean(IScreenConstants.PREF_SAVE_BEFORE_PACKAGING_ASK));
		zoomGlobalRadio.setSelection(store
				.getBoolean(IScreenConstants.PREF_ZOOMING_GLOBAL));
		zoomIndividualRadio.setSelection(!zoomGlobalRadio.getSelection());
		showScreenAnimationCheck.setSelection(!store
				.getBoolean(IScreenConstants.PREF_AUTO_ANIMATION_DISABLED));
	}

	private void updateGalleryCheckboxes(IContent content, String val) {
		if (val != null) {
			String[] screens = val.split(",");
			for (Button btn : galleryCheckboxMap.get(content)) {
				boolean selected = false;
				for (String screen : screens) {
					if (screen.equals(btn.getText())) {
						selected = true;
						break;
					}
				}
				btn.setSelection(selected);
			}
		}
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return UtilsPlugin.getDefault().getPreferenceStore();
	}

	protected void calculateButtonSize(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int wHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point mSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(wHint, mSize.x);
		button.setLayoutData(data);
	}
}
