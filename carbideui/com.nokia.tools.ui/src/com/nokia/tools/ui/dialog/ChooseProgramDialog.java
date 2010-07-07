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
package com.nokia.tools.ui.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.nokia.tools.media.image.ProgramDescriptor;
import com.nokia.tools.media.image.RegQueryUtil;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;
import com.nokia.tools.ui.prefs.UIPreferences;

public class ChooseProgramDialog extends BrandedTitleAreaDialog {

	class ProgramLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		private final List<Image> imagesToDispose = new ArrayList<Image>();

		@Override
		public void dispose() {
			super.dispose();
			for (final Image image : imagesToDispose) {
				image.dispose();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java
		 * .lang.Object, int)
		 */
		public Image getColumnImage(final Object element, final int columnIndex) {
			if (columnIndex == 0) {
				return ((ProgramDescriptor) element).getIcon();
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.
		 * lang.Object, int)
		 */
		public String getColumnText(final Object element, final int columnIndex) {
			switch (columnIndex) {
			case 0:
				return ((ProgramDescriptor) element).getName();
			default:
				return null;
			}

		}

	}

	public static boolean chooseProgramReqd(final String fileType) {
		if (SWT.getPlatform().equals("win32")) {
			final ProgramDescriptor[] pgms = RegQueryUtil
					.getRecommendedPrograms(fileType);
			if ((pgms != null) && (pgms.length > 0)) {
				for (final ProgramDescriptor pgm : pgms) {
					pgm.dispose();

				}
				return true;
			}
		}
		return false;
	}

	private final String bannerMessage;

	private Button btnBrowse, checkAlways;

	private final String[] externalToolFilter = { "*.exe; *.bat; *.com; *.cmd; *.pif" };
	
	private final String fileExtn;
	private String initialProgram = null;

	private List<ProgramDescriptor> prgmDesList = new ArrayList<ProgramDescriptor>();

	private boolean rememberDecision;

	private Group groupArea;

	private ProgramDescriptor selectedProgram = null;

	private String selectedProgramPath = null;

	private final boolean showAlwaysUseMessage;

	private TableViewer tableViewer;

	private final String titleMessage;	

	private final static List<String> EXE_TYPES = new ArrayList<String>(5);

	static {
		EXE_TYPES.add("exe");
		EXE_TYPES.add("bat");
		EXE_TYPES.add("com");
		EXE_TYPES.add("pif");
		EXE_TYPES.add("cmd");
	}
	

	public ChooseProgramDialog(final Shell parentShell,
			final String bannerMessage, final String titleMessage,
			final boolean showAlwaysUseMessage, final Program defaultProgram,
			final String fileType) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.bannerMessage = bannerMessage;
		this.titleMessage = titleMessage;
		this.showAlwaysUseMessage = showAlwaysUseMessage;
		this.fileExtn = fileType;

	}

	private ProgramDescriptor addProgram(final String program) {
		if (isValidProgram(program)) {
			final File prgFile = new File(program);

			final String initialProgramName = prgFile.getName();
			final ProgramDescriptor newPgm = new ProgramDescriptor(
			    initialProgramName, program, null);
			if (!prgmDesList.contains(newPgm)) {
				prgmDesList.add(newPgm);

			}
			return newPgm;
		}

		return null;
	}

	private boolean isValidProgram(String program) {
		if ((program != null) && (program.trim().length() > 0)) {
			final File prgFile = new File(program);
			if (FileUtils.isFileValidAndAccessible(prgFile)) {
				final String file1Ext = FileUtils.getExtension(prgFile);
				if (EXE_TYPES.contains(file1Ext)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		prgmDesList = new ArrayList<ProgramDescriptor>(Arrays
				.asList(RegQueryUtil.getRecommendedPrograms(fileExtn)));
		final ProgramDescriptor initialPgm = addProgram(initialProgram);

		final Composite area = (Composite) super.createDialogArea(parent);
		setTitle(getTitle());
		if (bannerMessage != null) {
			setMessage(bannerMessage);
		} else {
			setMessage(Messages.Choose_Program_To_Open);
		}
		Composite rootArea = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		rootArea.setLayoutData(gd);
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = 13;
		gl.marginWidth = 13;
		gl.verticalSpacing = 7;
		rootArea.setLayout(gl);

		groupArea = new Group(rootArea, SWT.NONE);
		groupArea.setText(Messages.Program);
		gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		groupArea.setLayoutData(gd);
		gl = new GridLayout(2, false);
		gl.marginHeight = 9;
		gl.marginWidth = 9;
		gl.verticalSpacing = 7;
		groupArea.setLayout(gl);

		tableViewer = new TableViewer(groupArea, SWT.FULL_SELECTION
				| SWT.BORDER | SWT.V_SCROLL);

		final Table items = tableViewer.getTable();
		items.setLinesVisible(false);
		items.setHeaderVisible(false);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumHeight = 50;
		gd.minimumWidth = 100;
		gd.widthHint = 250;
		gd.heightHint = 100;
		gd.horizontalSpan = 2;
		items.setLayoutData(gd);

		tableViewer.setLabelProvider(new ProgramLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		IPreferenceStore oldStore = UIPreferences.getStore();
		String allEditors = oldStore.getString(fileExtn);
		if (!StringUtils.isEmpty(allEditors)) {
			String[] editors = allEditors.split(",");
			for (String editor : editors) {
				addProgram(editor);
			}
		}
		tableViewer.setInput(prgmDesList);

		if (initialPgm != null) {
			tableViewer.setSelection(new StructuredSelection(initialPgm));
		} else {
			tableViewer.setSelection(new StructuredSelection(tableViewer
					.getElementAt(0)));
		}

		if (showAlwaysUseMessage) {
			checkAlways = new Button(groupArea, SWT.CHECK);
			checkAlways
					.setText("Always use the selected program to open this kind of file");
			gd = new GridData(SWT.FILL, SWT.NONE, true, false);
			gd.horizontalSpan = 2;
			checkAlways.setLayoutData(gd);
		}

		btnBrowse = new Button(groupArea, SWT.NONE);
		btnBrowse.setText("&Browse...");
		initializeDialogUnits(btnBrowse);
		setButtonLayoutData(btnBrowse);
		gd = (GridData) btnBrowse.getLayoutData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.RIGHT;
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent se) {
				final FileDialog fd = new FileDialog(new Shell(),
						SWT.SYSTEM_MODAL | SWT.OPEN);
				fd.setFilterExtensions(externalToolFilter);
				if (initialProgram != "") {
					fd.setFileName(initialProgram);
				}
				final String text = fd.open();
				if (text != null) {
					final ProgramDescriptor des = addProgram(text);
					if (des != null) {
						selectedProgram = des;
						tableViewer.setInput(prgmDesList);
						tableViewer.setSelection(new StructuredSelection(des));

					}
					IPreferenceStore store = UIPreferences.getStore();
					String temp = store.getString(fileExtn);
					if(!StringUtils.isEmpty(temp)) {
						if(!temp.contains(des.getFullPath())) {
							String allEditors = temp + ',' + des.getFullPath();
							store.setValue(fileExtn,allEditors);
						}
					} else {
						store.setValue(fileExtn,des.getFullPath());
					}
				}
			}

		});

		Composite separatorComposite = new Composite(area, SWT.NONE);
		separatorComposite
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout3 = new GridLayout();
		separatorComposite.setLayout(layout3);
		layout3.numColumns = 1;
		layout3.marginHeight = 0;
		layout3.marginWidth = 0;
		layout3.verticalSpacing = 0;

		final Label separator = new Label(separatorComposite, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		separator.setLayoutData(gd);

		return area;

	}

	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
		
		return null;
	}

	public ProgramDescriptor getSelectedProgram() {
		final ISelection selection = tableViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			selectedProgram = (ProgramDescriptor) ((IStructuredSelection) selection)
					.toArray()[0];
		}
		return selectedProgram;
	}

	public String getSelectedProgramPath() {
		return selectedProgramPath;
	}

	@Override
	protected String getTitle() {
		return this.titleMessage == null ? Messages.Choose_Program
				: this.titleMessage;
	}

	public boolean isRememberDecision() {
		return rememberDecision;
	}

	@Override
	protected void okPressed() {
		selectedProgramPath = setSelectedProgramPath();
		if (showAlwaysUseMessage) {
			rememberDecision = checkAlways.getSelection();
		}
		super.okPressed();
	}

	public void setInitialProgram(final String initialProgram) {
		this.initialProgram = initialProgram;
	}

	public String setSelectedProgramPath() {
		String path = null;
		if (getSelectedProgram() != null) {
			path = getSelectedProgram().getFullPath();
		}
		return path;
	}

}
