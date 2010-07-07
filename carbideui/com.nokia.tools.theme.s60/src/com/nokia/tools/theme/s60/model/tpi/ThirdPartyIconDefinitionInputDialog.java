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
package com.nokia.tools.theme.s60.model.tpi;

import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.tools.screen.ui.branding.ISharedImageDescriptor;

public class ThirdPartyIconDefinitionInputDialog extends Dialog {

	private Text appUID_text;
	private Text majorID_text;
	private Text minorID_text;
	private Text name_text;
	private boolean applicationIcon = true; // A boolean indicating if the created TPI is an application or a non-application icon.
	private ThirdPartyIconType thirdPartyIconType;
	private ThirdPartyIcon thirdPartyIcon;
	private Button appRadioButton, nonAppRadioButton;
	private static final int ICON_NAME_MAXLEN = 48;
	private String title;
	private Label nameErrorLabel, appUIDErrorLabel, majorIDErrorLabel, minorIDErrorLabel;
	private static Image errorImage = null; 
	
	public ThirdPartyIconDefinitionInputDialog(Shell parent, ThirdPartyIconType thirdPartyIconType, String title, ThirdPartyIcon thirdPartyIcon) {
		super(parent);
		setShellStyle( SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.thirdPartyIconType = thirdPartyIconType;
		this.thirdPartyIcon = thirdPartyIcon;
		this.title = title;
	}


	private boolean checkHexIds(String s) {
		if (s == null)
			return false;
		return Pattern.matches("0x[0-9a-fA-F]{1,8}", s);
	}

	private boolean checkAlphaNumeric(String s) {
		if (s == null)
			return false;
		return Pattern.matches("[0-9a-zA-Z ]{1,48}", s);
	}

	
	protected Control createContents(Composite parent) {
		// create the overall composite
		Shell shell = parent.getShell();
		if(shell != null){
			shell.setText(title);
		}
		if(errorImage == null){
			errorImage = ISharedImageDescriptor.ICON16_ERROR.createImage(true);
		}
		Control contents = super.createContents(parent);
		if(thirdPartyIcon == null)
			getButton(OK).setEnabled(false);
		
		if(thirdPartyIcon != null){
			String tpiName = thirdPartyIcon.getName();
			String appUID = thirdPartyIcon.getAppUid();
			String majorID = thirdPartyIcon.getMajorId();
			String minorID = thirdPartyIcon.getMinorId();
			
			name_text.setText(tpiName == null?"Custom Icon":tpiName);
			
			if(appUID == null || appUID.trim().length() < 2 || !appUID.substring(0, 2).equals("0x") ){
				appRadioButton.setSelection(false);
				nonAppRadioButton.setSelection(true);
				appUID_text.setEnabled(false);
				majorID_text.setEnabled(true);
				minorID_text.setEnabled(true);
				applicationIcon = false;
			}
			else{
				appUID_text.setText(appUID.trim());
				applicationIcon = true;
			}
			
			majorID_text.setText(majorID==null?"0x00000000":majorID.trim());
			minorID_text.setText(minorID==null?"0x0000":minorID.trim());
		}
		validateAndUpdateDisplay();
		return contents;
		
	}
	
	
	protected Control createDialogArea(final Composite parent) {

		Composite parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayout(new GridLayout(3, false));

		Label empty_label = new Label(parentComposite, SWT.NONE);
		GridData empty_data = new GridData();
		empty_label.setLayoutData(empty_data);
		empty_data.horizontalSpan = 3;

		Label name_label = new Label(parentComposite, SWT.NONE);
		name_label.setText("Name :");
		GridData name_data = new GridData();
		name_label.setLayoutData(name_data);

		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateAndUpdateDisplay();
			}
		};
		
		name_text = new Text(parentComposite, SWT.BORDER);
		name_text.setTextLimit(ICON_NAME_MAXLEN);
		name_data = new GridData(GridData.FILL_HORIZONTAL);
		name_data.grabExcessHorizontalSpace = true;
		name_text.setLayoutData(name_data);
		name_text.setText("Custom Icon");
		name_text.addModifyListener(modifyListener);
		nameErrorLabel = new Label(parentComposite, SWT.NONE);
		nameErrorLabel.setImage(errorImage);
		nameErrorLabel.setVisible(false);
		nameErrorLabel.setToolTipText("Not a valid alphanumeric value");
		
		

		Label appUID_Label = new Label(parentComposite, SWT.NONE);
		appUID_Label.setText("Application UID :");

		GridData appUID_data = new GridData();
		appUID_Label.setLayoutData(appUID_data);

		appUID_text = new Text(parentComposite, SWT.BORDER);
		appUID_text.setText("0x00000000");
		appUID_text.setTextLimit(10);
		appUID_data = new GridData(GridData.FILL_HORIZONTAL);
		appUID_text.setLayoutData(appUID_data);
		appUID_text.setToolTipText("Must start with \"0x\"");
		appUID_text.addModifyListener(modifyListener);

		appUIDErrorLabel = new Label(parentComposite, SWT.NONE);
		appUIDErrorLabel.setImage(errorImage);
		appUIDErrorLabel.setVisible(false);
		appUIDErrorLabel.setToolTipText("Not a valid hexadecimal value");

		

		Label majorID_label = new Label(parentComposite, SWT.NONE);
		majorID_label.setText("Major ID :");
		GridData majorID_data = new GridData();
		majorID_label.setLayoutData(majorID_data);

		majorID_text = new Text(parentComposite, SWT.BORDER);
		majorID_text.setTextLimit(10);
		majorID_text.setEnabled(false);
		majorID_data = new GridData(GridData.FILL_HORIZONTAL);
		majorID_text.setLayoutData(majorID_data);
		majorID_text.setText("0x00000000");
		majorID_text.addModifyListener(modifyListener);

		majorIDErrorLabel = new Label(parentComposite, SWT.NONE);
		majorIDErrorLabel.setImage(errorImage);
		majorIDErrorLabel.setVisible(false);
		majorIDErrorLabel.setToolTipText("Not a valid hexadecimal value");
		
		

		Label minorID_label = new Label(parentComposite, SWT.NONE);
		minorID_label.setText("Minor ID :");
		GridData minorID_data = new GridData();
		minorID_label.setLayoutData(minorID_data);

		minorID_text = new Text(parentComposite, SWT.BORDER);
		minorID_text.setTextLimit(6);
		minorID_text.setText("0x0000");
		minorID_text.setEnabled(false);
		minorID_data = new GridData(GridData.FILL_HORIZONTAL);
		minorID_text.setLayoutData(minorID_data);
		minorID_text.addModifyListener(modifyListener);

		minorIDErrorLabel = new Label(parentComposite, SWT.NONE);
		minorIDErrorLabel.setImage(errorImage);
		minorIDErrorLabel.setVisible(false);
		minorIDErrorLabel.setToolTipText("Not a valid hexadecimal value");

		

		Label empty_label2 = new Label(parentComposite, SWT.NONE);
		GridData empty_data2 = new GridData();
		empty_label2.setLayoutData(empty_data);
		empty_data2.horizontalSpan = 2;

		appRadioButton = new Button(parentComposite, SWT.RADIO);
		appRadioButton.setText("Application");
		appRadioButton.setSelection(true);
		appRadioButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				majorID_text.setEnabled(false);
				minorID_text.setEnabled(false);
				appUID_text.setEnabled(true);
				applicationIcon = true;
				validateAndUpdateDisplay();
			}

		});

		nonAppRadioButton = new Button(parentComposite, SWT.RADIO);
		nonAppRadioButton.setText("Non Application");
		nonAppRadioButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				appUID_text.setEnabled(false);
				majorID_text.setEnabled(true);
				minorID_text.setEnabled(true);
				applicationIcon = false;
				validateAndUpdateDisplay();
			}
		});

		

		Label empty_label3 = new Label(parentComposite, SWT.NONE);
		GridData empty_data3 = new GridData();
		empty_label3.setLayoutData(empty_data);
		empty_data3.horizontalSpan = 2;
		return parentComposite;
	}
	
	public boolean isApplicationIcon() {
		return applicationIcon;
	}


	protected void okPressed() {
		if(thirdPartyIcon == null)
			thirdPartyIcon =  new ThirdPartyIcon(applicationIcon?appUID_text.getText().trim():null,
										name_text.getText().trim(),
										applicationIcon?null: majorID_text.getText().trim(),
										applicationIcon?null: minorID_text.getText().trim(),
										thirdPartyIconType);
		else{
			thirdPartyIcon.setAppUid(applicationIcon?appUID_text.getText().trim():null);
			thirdPartyIcon.setName(name_text.getText().trim());
			thirdPartyIcon.setMajorId(applicationIcon?null: majorID_text.getText().trim());
			thirdPartyIcon.setMinorId(applicationIcon?null: minorID_text.getText().trim());
		}
		super.okPressed();
	}
	
	public ThirdPartyIcon getThirdPartyIcon(){
		return thirdPartyIcon;
	}
	
	private void validateAndUpdateDisplay(){
		nameErrorLabel.setVisible(false);
		appUIDErrorLabel.setVisible(false);
		majorIDErrorLabel.setVisible(false);
		minorIDErrorLabel.setVisible(false);

		boolean enabled = true;
		if(!checkAlphaNumeric(name_text.getText())){
			nameErrorLabel.setVisible(true);
			enabled = false;
		}

		if(applicationIcon){
			if(!checkHexIds(appUID_text.getText())){
				appUIDErrorLabel.setVisible(true);
				enabled = false;
			}
			else{
				appUIDErrorLabel.setVisible(false);
			}
		}
		else{
			if(!checkHexIds(majorID_text.getText())){
				majorIDErrorLabel.setVisible(true);
				enabled = false;
			}
			else{
				majorIDErrorLabel.setVisible(false);
			}
			
			if(!checkHexIds(minorID_text.getText())){
				minorIDErrorLabel.setVisible(true);
				enabled = false;
			}
			else{
				minorIDErrorLabel.setVisible(false);
			}
		}	
		getButton(OK).setEnabled(enabled);
	}
}
