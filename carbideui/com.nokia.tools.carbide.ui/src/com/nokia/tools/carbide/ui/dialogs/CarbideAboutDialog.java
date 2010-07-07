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

package com.nokia.tools.carbide.ui.dialogs;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.BrandingProperties;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.ProductProperties;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.about.AboutBundleGroupData;
import org.eclipse.ui.internal.about.AboutFeaturesButtonManager;
import org.eclipse.ui.internal.about.AboutItem;
import org.eclipse.ui.internal.about.AboutTextManager;
import org.eclipse.ui.internal.dialogs.AboutFeaturesDialog;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.nokia.tools.carbide.ui.Activator;


/**
 * Common "About" dialog for carbide family products. This shows the carbide
 * product information as well as the registration status if it's available.
 * 
 * @author onodqui
 * @version $Revision: 1.9 $ $Date: 2010/05/13 12:13:45 $
 */

public class CarbideAboutDialog extends TrayDialog {
    private final static int MAX_IMAGE_WIDTH_FOR_TEXT = 250;

    private final static int DETAILS_ID = IDialogConstants.CLIENT_ID + 1;

    private String productName;

    private IProduct product;

    private AboutBundleGroupData[] bundleGroupInfos;

    private ArrayList images = new ArrayList();

    private AboutFeaturesButtonManager buttonManager = new AboutFeaturesButtonManager();

    private StyledText text;

    private AboutTextManager aboutTextManager;

    /**
     * Create an instance of the AboutDialog for the given window.
     * 
     * @param parentShell The parent of the dialog.
     */
    public CarbideAboutDialog(Shell parentShell) {
        super(parentShell);
        setHelpAvailable(false);

        product = Platform.getProduct();
        if (product != null) {
            productName = product.getName();
        }
        if (productName == null) {
            productName = WorkbenchMessages.AboutDialog_defaultProductName;
        }

        // create a descriptive object for each BundleGroup
        IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
        LinkedList groups = new LinkedList();
        if (providers != null) {
            for (int i = 0; i < providers.length; ++i) {
                IBundleGroup[] bundleGroups = providers[i].getBundleGroups();
                for (int j = 0; j < bundleGroups.length; ++j) {
                    groups.add(new AboutBundleGroupData(bundleGroups[j]));
                }
            }
        }
        bundleGroupInfos = (AboutBundleGroupData[]) groups.toArray(new AboutBundleGroupData[0]);
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
            case DETAILS_ID:
                BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
                    public void run() {
                        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        CarbideInstallationDialog dialog = new CarbideInstallationDialog(getShell(), workbenchWindow);
                        dialog.setModalParent(CarbideAboutDialog.this);
                        dialog.open();
                    }
                });
                break;
            default:
                super.buttonPressed(buttonId);
                break;
        }
    }

    public boolean close() {
        // dispose all images
        for (int i = 0; i < images.size(); ++i) {
            Image image = (Image) images.get(i);
            image.dispose();
        }

        return super.close();
    }

    /*
     * (non-Javadoc) Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(NLS.bind(WorkbenchMessages.AboutDialog_shellTitle, productName));
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IWorkbenchHelpContextIds.ABOUT_DIALOG);
    }

    /**
     * Add buttons to the dialog's button bar. Subclasses should override.
     * 
     * @param parent the button bar composite
     */
    protected void createButtonsForButtonBar(Composite parent) {
        parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton(parent, DETAILS_ID, WorkbenchMessages.AboutDialog_DetailsButton, false);

        Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        Button b = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        b.setFocus();
    }

    /**
     * Creates and returns the contents of the upper part of the dialog (above
     * the button bar). Subclasses should overide.
     * 
     * @param parent the parent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) {
        // brand the about box if there is product info
        Image aboutImage = null;
        AboutItem item = null;
        if (product != null) {
            ImageDescriptor imageDescriptor = ProductProperties.getAboutImage(product);
            if (imageDescriptor != null) {
                aboutImage = imageDescriptor.createImage();
            }

            // if the about image is small enough, then show the text
            if (aboutImage == null || aboutImage.getBounds().width <= MAX_IMAGE_WIDTH_FOR_TEXT) {
                String aboutText = ProductProperties.getAboutText(product);
                if (aboutText != null) {
                    item = AboutTextManager.scan(aboutText);
                }
            }

            if (aboutImage != null) {
                images.add(aboutImage);
            }
        }

        // create a composite which is the parent of the top area and the bottom
        // button bar, this allows there to be a second child of this composite
        // with
        // a banner background on top but not have on the bottom
        Composite workArea = new Composite(parent, SWT.NONE);
        GridLayout workLayout = new GridLayout();
        workLayout.marginHeight = 0;
        workLayout.marginWidth = 0;
        workLayout.verticalSpacing = 0;
        workLayout.horizontalSpacing = 0;
        workArea.setLayout(workLayout);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        // page group
        Color background = JFaceColors.getBannerBackground(parent.getDisplay());
        Color foreground = JFaceColors.getBannerForeground(parent.getDisplay());
        Composite top = (Composite) super.createDialogArea(workArea);

        // override any layout inherited from createDialogArea
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        top.setLayout(layout);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
        top.setBackground(background);
        top.setForeground(foreground);

        // the image & text
        final Composite topContainer = new Composite(top, SWT.NONE);
        topContainer.setBackground(background);
        topContainer.setForeground(foreground);

        layout = new GridLayout();
        layout.numColumns = (aboutImage == null || item == null ? 1 : 2);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        topContainer.setLayout(layout);

        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        topContainer.setLayoutData(data);

        GC gc = new GC(parent);
        // arbitrary default
        int topContainerHeightHint = 100;
        try {
            // default height enough for 6 lines of text
            topContainerHeightHint = Math.max(topContainerHeightHint, gc.getFontMetrics().getHeight() * 6);
        } finally {
            gc.dispose();
        }

        // image on left side of dialog
        if (aboutImage != null) {
            Label imageLabel = new Label(topContainer, SWT.NONE);
            imageLabel.setBackground(background);
            imageLabel.setForeground(foreground);

            data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.verticalAlignment = GridData.BEGINNING;
            data.grabExcessHorizontalSpace = false;
            imageLabel.setLayoutData(data);
            imageLabel.setImage(aboutImage);
            // topContainerHeightHint = Math.max(topContainerHeightHint,
            // aboutImage.getBounds().height);
        }

        // data = new GridData();
        // data.horizontalAlignment = GridData.FILL;
        // data.verticalAlignment = GridData.FILL;
        // data.grabExcessHorizontalSpace = true;
        // data.grabExcessVerticalSpace = true;
        // data.heightHint = topContainerHeightHint;
        // topContainer.setLayoutData(data);

        if (item != null) {
            final int minWidth = 400; // This value should really be calculated
            // from the computeSize(SWT.DEFAULT,
            // SWT.DEFAULT) of all the
            // children in infoArea excluding the
            // wrapped styled text
            // There is no easy way to do this.
            final ScrolledComposite scroller = new ScrolledComposite(topContainer, SWT.V_SCROLL | SWT.H_SCROLL);
            data = new GridData(GridData.FILL_BOTH);
            data.widthHint = minWidth;
            scroller.setLayoutData(data);

            final Composite textComposite = new Composite(scroller, SWT.NONE);
            textComposite.setBackground(background);

            layout = new GridLayout();
            layout.numColumns = 1;
            layout.marginRight = 17;
            layout.marginTop = 0;
            textComposite.setLayout(layout);
            data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.verticalAlignment = GridData.BEGINNING;
            data.grabExcessHorizontalSpace = true;
            textComposite.setLayoutData(data);

            // Image on the right
            Label textImageLabel = new Label(textComposite, SWT.NONE);
            URL url = BrandingProperties.getUrl(product.getProperty("aboutTextImage"), product.getDefiningBundle());
            Image textImage = ImageDescriptor.createFromURL(url).createImage();
            images.add(textImage);
            textImageLabel.setImage(textImage);
            textImageLabel.setBackground(background);
            textImageLabel.setForeground(foreground);
            data = new GridData();
            data.horizontalAlignment = SWT.RIGHT;
            data.verticalIndent = 7;
            textImageLabel.setLayoutData(data);

            // Version information on the right
            Label versionLabel = new Label(textComposite, SWT.NONE);
            data = new GridData();
            data.horizontalAlignment = SWT.RIGHT;
            data.verticalIndent = 8;
            versionLabel.setText(product.getProperty("versionText"));
            versionLabel.setBackground(background);
            versionLabel.setForeground(foreground);
            versionLabel.setLayoutData(data);

            String buildId = product.getProperty("buildId");
            if (buildId != null && !buildId.startsWith("@")) {
                // don't show build id if it's not resolved, @buildId@
                Label buildIdLabel = new Label(textComposite, SWT.NONE);
                data = new GridData();
                data.horizontalAlignment = SWT.RIGHT;
                data.verticalIndent = 8;
                buildIdLabel.setText(Messages.BuildId_Label + " " + buildId);
                buildIdLabel.setBackground(background);
                buildIdLabel.setForeground(foreground);
                buildIdLabel.setLayoutData(data);
            }           

            text = new StyledText(textComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
            text.setCaret(null);
            text.setFont(parent.getFont());
            text.setText(item.getText());
            text.setCursor(null);
            text.setBackground(background);
            text.setForeground(foreground);

            aboutTextManager = new AboutTextManager(text);
            aboutTextManager.setItem(item);

            createTextMenu();

            GridData gd = new GridData();
            gd.verticalAlignment = GridData.BEGINNING;
            gd.horizontalAlignment = GridData.FILL;
            gd.grabExcessHorizontalSpace = true;
            text.setLayoutData(gd);
            
            // Adjust the scrollbar increments
            scroller.getHorizontalBar().setIncrement(20);
            scroller.getVerticalBar().setIncrement(20);

            final boolean[] inresize = new boolean[1]; // flag to stop
            // unneccesary
            // recursion
            textComposite.addControlListener(new ControlAdapter() {
                public void controlResized(ControlEvent e) {
                    if (inresize[0])
                        return;
                    inresize[0] = true;
                    // required because of bugzilla report 4579
                    textComposite.layout(true);
                    // required because you want to change the height that the
                    // scrollbar will scroll over when the width changes.
                    int width = textComposite.getClientArea().width;
                    Point p = textComposite.computeSize(width, SWT.DEFAULT);
                    scroller.setMinSize(minWidth, p.y);
                    inresize[0] = false;
                }
            });

            scroller.setExpandHorizontal(true);
            scroller.setExpandVertical(true);
            Point p = textComposite.computeSize(minWidth, SWT.DEFAULT);
            textComposite.setSize(p.x, p.y);
            scroller.setMinWidth(minWidth);
            scroller.setMinHeight(p.y);

            scroller.setContent(textComposite);
        }

        // horizontal bar
        Label bar = new Label(workArea, SWT.HORIZONTAL | SWT.SEPARATOR);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        bar.setLayoutData(data);
        String[] s = new String[1];
        URL url = Platform.getBundle(Activator.PLUGIN_ID).getEntry("doc/data.html");
        try {
			s[0] = FileLocator.toFileURL(url).toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[][] index = { { Messages.Oss_Liense_Info.indexOf("Click"), Messages.Oss_Liense_Info.length()-Messages.Oss_Liense_Info.indexOf("Click")-1 } };
		AboutItem licenceItem = new AboutItem(Messages.Oss_Liense_Info, index, s);

		final Composite licenceTextComposite = new Composite(workArea, SWT.NONE);
		licenceTextComposite.setBackground(background);

		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginRight = 17;
		layout.marginTop = 0;
		licenceTextComposite.setLayout(layout);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
		licenceTextComposite.setLayoutData(data);

		StyledText licenceText = new StyledText(licenceTextComposite, SWT.MULTI
				| SWT.WRAP | SWT.READ_ONLY);
		licenceText.setCaret(null);
		licenceText.setText(licenceItem.getText());
		licenceText.setCursor(null);
		aboutTextManager = new AboutTextManager(licenceText);
		aboutTextManager.setItem(licenceItem);

        bar = new Label(workArea, SWT.HORIZONTAL | SWT.SEPARATOR);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        bar.setLayoutData(data);
        AboutItem ossItem = AboutTextManager.scan(Messages.Oss_Download_Info);
        
        final Composite ossTextComposite= new Composite(workArea, SWT.NONE);
        ossTextComposite.setBackground(background);

        layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginRight = 17;
        layout.marginTop = 0;
        ossTextComposite.setLayout(layout);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.BEGINNING;
        data.grabExcessHorizontalSpace = true;
        ossTextComposite.setLayoutData(data);
        
        StyledText ossText = new StyledText(ossTextComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        ossText.setCaret(null);
        ossText.setText(licenceItem.getText());
        ossText.setCursor(null);
        aboutTextManager = new AboutTextManager(ossText);
        aboutTextManager.setItem(ossItem);        
        
        bar = new Label(workArea, SWT.HORIZONTAL | SWT.SEPARATOR);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        bar.setLayoutData(data);

        // add image buttons for bundle groups that have them
        Composite bottom = (Composite) super.createDialogArea(workArea);
        // override any layout inherited from createDialogArea
        layout = new GridLayout();
        bottom.setLayout(layout);
        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;

        bottom.setLayoutData(data);

        createFeatureImageButtonRow(bottom);

        // spacer
        bar = new Label(bottom, SWT.NONE);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        bar.setLayoutData(data);

        return workArea;
    }

    /**
     * Create the context menu for the text widget.
     * 
     * @since 3.4
     */
    private void createTextMenu() {
        final MenuManager textManager = new MenuManager();
        textManager.add(new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(),
            null, IWorkbenchCommandConstants.EDIT_COPY, CommandContributionItem.STYLE_PUSH)));
        textManager.add(new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(),
            null, IWorkbenchCommandConstants.EDIT_SELECT_ALL, CommandContributionItem.STYLE_PUSH)));
        text.setMenu(textManager.createContextMenu(text));
        text.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                textManager.dispose();
            }
        });

    }

    private void createFeatureImageButtonRow(Composite parent) {
        Composite featureContainer = new Composite(parent, SWT.NONE);
        RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = true;
        featureContainer.setLayout(rowLayout);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        featureContainer.setLayoutData(data);

        for (int i = 0; i < bundleGroupInfos.length; i++) {
            createFeatureButton(featureContainer, bundleGroupInfos[i]);
        }
    }

    private Button createFeatureButton(Composite parent, final AboutBundleGroupData info) {
        if (!buttonManager.add(info)) {
            return null;
        }

        ImageDescriptor desc = info.getFeatureImage();
        Image featureImage = null;

        Button button = new Button(parent, SWT.FLAT | SWT.PUSH);
        button.setData(info);
        featureImage = desc.createImage();
        images.add(featureImage);
        button.setImage(featureImage);
        button.setToolTipText(info.getProviderName());

        button.getAccessible().addAccessibleListener(new AccessibleAdapter() {
            /*
             * (non-Javadoc)
             * @see
             * org.eclipse.swt.accessibility.AccessibleAdapter#getName(org.eclipse
             * .swt.accessibility.AccessibleEvent)
             */
            public void getName(AccessibleEvent e) {
                e.result = info.getProviderName();
            }
        });
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                AboutBundleGroupData[] groupInfos = buttonManager.getRelatedInfos(info);
                AboutBundleGroupData selection = (AboutBundleGroupData) event.widget.getData();

                AboutFeaturesDialog d = new AboutFeaturesDialog(getShell(), productName, groupInfos, selection);
                d.open();
            }
        });

        return button;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    protected boolean isResizable() {
        return true;
    }
}
