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

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.UiPlugin;

public class ExternalToolsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public static final String PREFERENCES_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "external_tools_preferences_context"; 

	private Text bitmapEditorTextField;

	private Text audioPlayerTextField;

	private Text videoPlayerTextField;

	private Text soundEditorTextField;

	private Text vectorEditorTextField;

	private Button bitmapEditorBrowseButton;

	private Button soundEditorBrowseButton;

	private Button vectorEditorBrowseButton;

	private Button audioPlayerBrowseButton;

	private Button videoPlayerBrowseButton;

	private String selBitmapEditor = "";

	private String selSoundEditor = "";

	private String selVectorEditor = "";

	private String selAudioPlayer = "";

	private String selVideoPlayer = "";

	private String[] externalToolFilter = { "*.exe; *.bat; *.com; *.cmd; *.pif" };

	@Override
	protected Control createContents(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				PREFERENCES_CONTEXT);

		Composite generalComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		generalComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		generalComposite.setLayoutData(gd);

		setDescription(Messages.ExternalToolsPreferencePage_ThirdpartyEditorsDescription);
		createDescriptionLabel(generalComposite);

		Group grpEditors = new Group(generalComposite, SWT.NONE);
		grpEditors
				.setText(Messages.ExternalToolsPreferencePage_ThirdpartyEditorsInfoLabel);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = 5;
		gd.widthHint = 300;
		grpEditors.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpEditors.setLayout(layout);

		createLabel(grpEditors,
				Messages.ExternalToolsPreferencePage_bitmapEditorStringName);

		bitmapEditorTextField = createTextField(grpEditors);
		bitmapEditorTextField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				selBitmapEditor = bitmapEditorTextField.getText();
				pageChanged();
			}
		});

		bitmapEditorBrowseButton = new Button(grpEditors, SWT.PUSH);
		bitmapEditorBrowseButton
				.setText(Messages.ExternalToolsPreferencePage_bitmapBrowseButtonText);
		calculateButtonSize(bitmapEditorBrowseButton);
		bitmapEditorBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				FileDialog fd = new FileDialog(new Shell(), SWT.SYSTEM_MODAL
						| SWT.OPEN);
				fd.setFilterExtensions(externalToolFilter);
				if (selBitmapEditor != "")
					fd.setFileName(selBitmapEditor);
				String text = fd.open();
				if (text != null) {
					bitmapEditorTextField.setText(text);
				}
			}

		});

		createLabel(grpEditors,
				Messages.ExternalToolsPreferencePage_soundEditorStringName);

		soundEditorTextField = createTextField(grpEditors);
		soundEditorTextField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				selSoundEditor = soundEditorTextField.getText();
				pageChanged();
			}
		});

		soundEditorBrowseButton = new Button(grpEditors, SWT.PUSH);
		soundEditorBrowseButton
				.setText(Messages.ExternalToolsPreferencePage_soundBrowseButtonText);
		calculateButtonSize(soundEditorBrowseButton);
		soundEditorBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				FileDialog fd = new FileDialog(new Shell(), SWT.SYSTEM_MODAL
						| SWT.OPEN);
				fd.setFilterExtensions(externalToolFilter);
				if (selSoundEditor != "")
					fd.setFileName(selSoundEditor);
				String text = fd.open();
				if (text != null) {
					soundEditorTextField.setText(text);
				}
			}
		});

		createLabel(grpEditors,
				Messages.ExternalToolsPreferencePage_vectorEditorStringName);

		vectorEditorTextField = createTextField(grpEditors);
		vectorEditorTextField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				selVectorEditor = vectorEditorTextField.getText();
				pageChanged();
			}
		});

		vectorEditorBrowseButton = new Button(grpEditors, SWT.PUSH);
		vectorEditorBrowseButton
				.setText(Messages.ExternalToolsPreferencePage_vectorBrowseButtonText);
		calculateButtonSize(vectorEditorBrowseButton);
		vectorEditorBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				FileDialog fd = new FileDialog(new Shell(), SWT.SYSTEM_MODAL
						| SWT.OPEN);
				fd.setFilterExtensions(externalToolFilter);
				if (selVectorEditor != "")
					fd.setFileName(selVectorEditor);
				String text = fd.open();
				if (text != null) {
					vectorEditorTextField.setText(text);
				}
			}
		});

		Group grpPlayers = new Group(generalComposite, SWT.NONE);
		grpPlayers
				.setText(Messages.ExternalToolsPreferencePage_ThirdpartyPlayersInfoLabel);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		grpPlayers.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpPlayers.setLayout(layout);

		createLabel(grpPlayers,
				Messages.ExternalToolsPreferencePage_audioPlayerStringName);

		audioPlayerTextField = createTextField(grpPlayers);
		audioPlayerTextField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				selAudioPlayer = audioPlayerTextField.getText();
				pageChanged();
			}
		});

		audioPlayerBrowseButton = new Button(grpPlayers, SWT.PUSH);
		audioPlayerBrowseButton
				.setText(Messages.ExternalToolsPreferencePage_audioBrowseButtonText);
		calculateButtonSize(audioPlayerBrowseButton);
		audioPlayerBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				FileDialog fd = new FileDialog(new Shell(), SWT.SYSTEM_MODAL
						| SWT.OPEN);
				fd.setFilterExtensions(externalToolFilter);
				if (selAudioPlayer != "")
					fd.setFileName(selAudioPlayer);
				String text = fd.open();
				if (text != null) {
					audioPlayerTextField.setText(text);
				}
			}

		});

		createLabel(grpPlayers,
				Messages.ExternalToolsPreferencePage_videoPlayerStringName);

		videoPlayerTextField = createTextField(grpPlayers);
		videoPlayerTextField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				selVideoPlayer = videoPlayerTextField.getText();
				pageChanged();
			}
		});

		videoPlayerBrowseButton = new Button(grpPlayers, SWT.PUSH);
		videoPlayerBrowseButton
				.setText(Messages.ExternalToolsPreferencePage_videoBrowseButtonText);
		calculateButtonSize(videoPlayerBrowseButton);
		videoPlayerBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				FileDialog fd = new FileDialog(new Shell(), SWT.SYSTEM_MODAL
						| SWT.OPEN);
				fd.setFilterExtensions(externalToolFilter);
				if (selVideoPlayer != "")
					fd.setFileName(selVideoPlayer);
				String text = fd.open();
				if (text != null) {
					videoPlayerTextField.setText(text);
				}
			}

		});

		initializeValues();

		return parent;
	}

	public void init(IWorkbench workbench) {
		
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		return label;
	}

	private Text createTextField(Composite parent) {
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(data);
		return text;
	}

	private void storeValuesIntoPreferenceStore() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IMediaConstants.PREF_BITMAP_EDITOR,
				bitmapEditorTextField.getText());
		store.setValue(IMediaConstants.PREF_SOUND_EDITOR, soundEditorTextField
				.getText());
		store.setValue(IMediaConstants.PREF_VECTOR_EDITOR,
				vectorEditorTextField.getText());

		store.setValue(IMediaConstants.PREF_AUDIO_PLAYER, audioPlayerTextField
				.getText());
		store.setValue(IMediaConstants.PREF_VIDEO_PLAYER, videoPlayerTextField
				.getText());

		
		store = UiPlugin.getDefault().getPreferenceStore();
	}

	public boolean performOk() {
		storeValuesIntoPreferenceStore();
		UiPlugin.getDefault().savePluginPreferences();
		return true;
	}

	protected void performDefaults() {
		super.performDefaults();
		initializeDefaults();
	}

	private void initializeDefaults() {
		bitmapEditorTextField.setText("");
		soundEditorTextField.setText("");
		vectorEditorTextField.setText("");

		audioPlayerTextField.setText("");
		videoPlayerTextField.setText("");

	}

	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		bitmapEditorTextField.setText(store
				.getString(IMediaConstants.PREF_BITMAP_EDITOR));
		soundEditorTextField.setText(store
				.getString(IMediaConstants.PREF_SOUND_EDITOR));
		vectorEditorTextField.setText(store
				.getString(IMediaConstants.PREF_VECTOR_EDITOR));

		audioPlayerTextField.setText(store
				.getString(IMediaConstants.PREF_AUDIO_PLAYER));
		videoPlayerTextField.setText(store
				.getString(IMediaConstants.PREF_VIDEO_PLAYER));

		store = UiPlugin.getDefault().getPreferenceStore();
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return UtilsPlugin.getDefault().getPreferenceStore();
	}

	@Override
	public void setValid(boolean b) {
		
		super.setValid(b);
	}

	/**
	 * Validates the input and offers error messages
	 */
	private void pageChanged() {

		Path file1Path = new Path(selBitmapEditor);
		String file1Ext = file1Path.getFileExtension();
		Path file2Path = new Path(selSoundEditor);
		String file2Ext = file2Path.getFileExtension();
		Path file3Path = new Path(selVectorEditor);
		String file3Ext = file3Path.getFileExtension();

		Path file5Path = new Path(selAudioPlayer);
		String file5Ext = file5Path.getFileExtension();
		Path file6Path = new Path(selVideoPlayer);
		String file6Ext = file6Path.getFileExtension();

		if ((selBitmapEditor.length() != 0 && selBitmapEditor.trim().length() == 0)
				|| (selBitmapEditor.trim().length() != 0 && !FileUtils
						.isFileValidAndAccessible(file1Path))) {
			updateStatus(Messages.ExternalToolsPreferencePage_BitmapEditorNotExist);
			return;
		}

		if ((selBitmapEditor.trim().length() != 0)
				&& (file1Ext == null || !(file1Ext.equalsIgnoreCase("exe")
						|| file1Ext.equalsIgnoreCase("bat")
						|| file1Ext.equalsIgnoreCase("com")
						|| file1Ext.equalsIgnoreCase("cmd") || file1Ext
						.equalsIgnoreCase("pif")))) {
			updateStatus(Messages.ExternalToolsPreferencePage_BitmapEditorExtError);
			return;
		}

		if ((selSoundEditor.length() != 0 && selSoundEditor.trim().length() == 0)
				|| (selSoundEditor.trim().length() != 0 && !FileUtils
						.isFileValidAndAccessible(file2Path))) {
			updateStatus(Messages.ExternalToolsPreferencePage_SoundEditorNotExist);
			return;
		}

		if ((selSoundEditor.trim().length() != 0)
				&& (file2Ext == null || !(file2Ext.equalsIgnoreCase("exe")
						|| file2Ext.equalsIgnoreCase("bat")
						|| file2Ext.equalsIgnoreCase("com")
						|| file2Ext.equalsIgnoreCase("cmd") || file2Ext
						.equalsIgnoreCase("pif")))) {
			updateStatus(Messages.ExternalToolsPreferencePage_SoundEditorExtError);
			return;
		}

		if ((selVectorEditor.length() != 0 && selVectorEditor.trim().length() == 0)
				|| (selVectorEditor.trim().length() != 0 && !FileUtils
						.isFileValidAndAccessible(file3Path))) {
			updateStatus(Messages.ExternalToolsPreferencePage_VectorEditorNotExist);
			return;
		}

		if ((selVectorEditor.trim().length() != 0)
				&& (file3Ext == null || !(file3Ext.equalsIgnoreCase("exe")
						|| file3Ext.equalsIgnoreCase("bat")
						|| file3Ext.equalsIgnoreCase("com")
						|| file3Ext.equalsIgnoreCase("cmd") || file3Ext
						.equalsIgnoreCase("pif")))) {
			updateStatus(Messages.ExternalToolsPreferencePage_VectorEditorExtError);
			return;
		}

		if ((selAudioPlayer.length() != 0 && selAudioPlayer.trim().length() == 0)
				|| (selAudioPlayer.trim().length() != 0 && !FileUtils
						.isFileValidAndAccessible(file5Path))) {
			updateStatus(Messages.ExternalToolsPreferencePage_audioPlayerNotExist);
			return;
		}

		if ((selAudioPlayer.trim().length() != 0)
				&& (file5Ext == null || !(file5Ext.equalsIgnoreCase("exe")
						|| file5Ext.equalsIgnoreCase("bat")
						|| file5Ext.equalsIgnoreCase("com")
						|| file5Ext.equalsIgnoreCase("cmd") || file5Ext
						.equalsIgnoreCase("pif")))) {
			updateStatus(Messages.ExternalToolsPreferencePage_audioPlayerExtError);
			return;
		}

		if ((selVideoPlayer.length() != 0 && selVideoPlayer.trim().length() == 0)
				|| (selVideoPlayer.trim().length() != 0 && !FileUtils
						.isFileValidAndAccessible(file6Path))) {
			updateStatus(Messages.ExternalToolsPreferencePage_videoPlayerNotExist);
			return;
		}

		if ((selVideoPlayer.trim().length() != 0)
				&& (file6Ext == null || !(file6Ext.equalsIgnoreCase("exe")
						|| file6Ext.equalsIgnoreCase("bat")
						|| file6Ext.equalsIgnoreCase("com")
						|| file6Ext.equalsIgnoreCase("cmd") || file6Ext
						.equalsIgnoreCase("pif")))) {
			updateStatus(Messages.ExternalToolsPreferencePage_videoPlayerExtError);
			return;
		}

		setValid(true);
		updateStatus(null);
	}

	/**
	 * Updates error messages
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
		setValid(message == null);
	}

	private void calculateButtonSize(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int wHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point mSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(wHint, mSize.x);
		button.setLayoutData(data);
	}
}
