package com.nokia.tools.screen.ui.wizards;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;

/**
 * This dialog is for deleting an existing project from the current workspace.
 * 
 *  
 * @author balasjay
 */
public class DeleteProjectDialog extends BrandedTitleAreaDialog {

	public static final String DELETE_PROJECT_DIALOG_CONTEXT = "com.nokia.tools.s60.ide.deleteProjectWizard_context"; //$NON-NLS-1$

	private String[] strSelProjects;

	Shell shell;

	private Button btnOpen;

	private List lstProjects;

	private Image labelImage;

	/**
	 * The constructor
	 */
	public DeleteProjectDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	protected Control createDialogArea(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getParent(),
				DeleteProjectDialog.DELETE_PROJECT_DIALOG_CONTEXT);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		setTitle(WizardMessages.Delete_Project_Title);
		setMessage(WizardMessages.Delete_Project_Banner_Message);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.marginHeight = 13;
		layout.marginWidth = 13;
		layout.verticalSpacing = 8;

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		updateStatus(null);


		lstProjects = new List(container, SWT.V_SCROLL | SWT.BORDER
				| SWT.H_SCROLL | SWT.MULTI);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 424;
		gd.heightHint = 202;
		gd.horizontalSpan = 3;
		gd.minimumHeight = 30;
		lstProjects.setLayoutData(gd);
		lstProjects.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				strSelProjects = lstProjects.getSelection();
				updateStates();
			}
		});
		lstProjects.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				if (lstProjects.getSelectionIndex() != -1) {
					strSelProjects = lstProjects.getSelection();
					updateStates();
					buttonPressed(IDialogConstants.OK_ID);
				}
			}

			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
			}
		});

		Composite container2 = new Composite(area, SWT.NONE);
		container2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout2 = new GridLayout();
		container2.setLayout(layout2);
		layout2.numColumns = 1;
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		layout2.verticalSpacing = 0;
		final Label separator = new Label(container2, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		separator.setLayoutData(gd);

		listOpenWorkspaceProjects();

		if (lstProjects.getItemCount() == 0) {
			lstProjects.setEnabled(false);
		} else {
			lstProjects.setEnabled(true);
		}

		return area;
	}

	/**
	 * This method lists all opened projects in the workspace
	 */
	private void listOpenWorkspaceProjects() {
		lstProjects.removeAll();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
		.getProjects();
		Arrays.sort(projects, new Comparator<IProject>() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(T, T)
			 */
			public int compare(IProject o1, IProject o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		for (IProject project : projects) {
			lstProjects.add(project.getName());
		}
	}

	protected void createButtonsForButtonBar(Composite parent) {
		btnOpen = createButton(parent, IDialogConstants.OK_ID,
				WizardMessages.Delete_Project_Button, true);
		btnOpen.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			setMessage(null);
			if(MessageDialog.openConfirm(this.getShell(), WizardMessages.Delete_Project_Title, 
					WizardMessages.Delete_Project_Confirmation_Message)== false){
				buttonId=IDialogConstants.CANCEL_ID;
			}
		}

		if (buttonId == IDialogConstants.CANCEL_ID) {
			setMessage(null);
		}

		super.buttonPressed(buttonId);
	}

	/**
	 * This method handles enabled/disabled state logic of components
	 */
	private void updateStates() {
		if (lstProjects.getItemCount() > 0) {
			lstProjects.setEnabled(true);
			if (lstProjects.getSelection().length > 0) {
				btnOpen.setEnabled(true);
			} else
				btnOpen.setEnabled(false);

		} else {
			lstProjects.setEnabled(false);
		}

	}

	/**
	 * This method updates error messages in the banner area
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
	}

	/**
	 * This method returns name of the selected project in the workspace or
	 * selected project with full path if the selection is made outside current
	 * workspace
	 * 
	 * @return project with full path
	 */
	public String[] getSelectedProjects() {
		return strSelProjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close() {
		if (labelImage != null) {
			labelImage.dispose();
		}
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.utils.BrandedTitleAreaDialog#getBannerIconDescriptor()
	 */
	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
		return null;
		//return ISharedImageDescriptor.WIZBAN_DELETE_PROJECT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.utils.BrandedTitleAreaDialog#getTitle()
	 */
	@Override
	protected String getTitle() {
		return WizardMessages.Delete_Project_Title;
	}
}
