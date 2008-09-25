/**********************************************************************************
Copyright (C) 2008
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under
the terms of the GNU General Public License version 2 as published by the
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.

Author: Carsten Priess
Created: 15.01.2008
 *********************************************************************************/
package org.n52.udig.catalog.internal.sos.ui;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geotools.data.ows.OperationType;
import org.n52.oxf.owsCommon.capabilities.IDiscreteValueDomain;
import org.n52.oxf.owsCommon.capabilities.Parameter;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.dataStore.SOSCapabilities;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.ParameterConfiguration;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;
import org.n52.udig.catalog.sos.internal.Messages;

/**
 * This class offers a way to preconfigure SOS (pl.) being accessed by this
 * plugin..
 * 
 * Enable this via plugin.xml
 * 
 * <page category="org.n52.udig.catalog.internal.sos.ui.SOSPreferencePage"
 * class="org.n52.udig.catalog.internal.sos.ui.SOSPreferencePage1"
 * id="org.n52.udig.catalog.internal.sos.ui.SOSPreferencePage1" name="SOS
 * Connection Properties"> </page>
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class SOSPreferencePage1 extends PreferencePage implements
		IWorkbenchPreferencePage, SelectionListener {

	private Button add;
	private Button remove;
	private Combo urlCombo;
	// private Combo opCombo;
	// private Combo paramCombo;
	private Composite configurationBlock;
	private Composite composite;
	private SOSCapabilities caps;
	private Button omitTag;
	private Button showTag;
	// private Composite layoutBlock;
	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSPreferencePage1.class);
	private final String placeholderString1 = "                                   ";
//	private final String placeholderString2 = "                                                                              ";
	private final String placeholderString3 = "                                                                 ";
	private Tree tree;
	private Tree valueTree;
	// private Text description;

	Composite valueBlock;
	Composite btBlock;

	/**
	 * 
	 */
	public SOSPreferencePage1() {
		super();
	}

	/**
	 * @param title
	 */
	public SOSPreferencePage1(final String title) {
		super(title);
	}

	/**
	 * @param title
	 * @param image
	 */
	public SOSPreferencePage1(final String title, final ImageDescriptor image) {
		super(title, image);
	}

	private void updateCombo() {
		if (urlCombo != null) {
			urlCombo.removeAll();
			urlCombo.add(" ");
		}
		for (final String s : SOSConfigurationRegistry.getInstance()
				.getConfiguredSOSs()) {
			urlCombo.add(s);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		composite = new Group(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		final Label label = new Label(composite, SWT.NONE);
		label.setText("Select your SOS URL:");
		new Label(composite, SWT.NONE).setText("");
		new Label(composite, SWT.NONE).setText("");

		urlCombo = new Combo(composite, SWT.BORDER);
		urlCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		urlCombo.setVisibleItemCount(10);
		urlCombo.addSelectionListener(this);
		urlCombo.add("");
		for (final String s : SOSConfigurationRegistry.getInstance()
				.getConfiguredSOSs()) {
			urlCombo.add(s);
		}

		// buttons
		// new Label(composite, SWT.NONE).setText("");

		// final Composite buttonBlock = new Group(composite, SWT.NULL);
		// buttonBlock.setLayout(new GridLayout(2, false));

		add = new Button(composite, SWT.PUSH);
		add.setText("Add");
		add.setToolTipText("Adds a new SOS");
		add.addSelectionListener(this);

		remove = new Button(composite, SWT.PUSH);
		remove.setText("Remove");
		remove.setToolTipText("Remove selected SOS from configuration");
		remove.addSelectionListener(this);

		// Configuration Group
		// new Label(composite, SWT.NONE).setText("");

		// layoutBlock= new Group(parent, SWT.NULL);
		// layoutBlock.setLayout(new GridLayout(2, false));
		// layoutBlock.setVisible(true);

		configurationBlock = new Group(parent, SWT.NONE);
		configurationBlock.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		configurationBlock.setVisible(false);

		try {
			final Label lab1 = new Label(configurationBlock, SWT.NONE);
			lab1.setText("Select your parameters to configure");
			new Label(configurationBlock, SWT.NONE).setText("");
			tree = new Tree(configurationBlock, SWT.SINGLE | SWT.BORDER);
			// tree.setVisible(false);
			// tree.setEnabled(false);
			tree.setItemCount(10);
			tree.getItem(0).setText(placeholderString3);
			tree.addSelectionListener(this);

			valueBlock = new Composite(configurationBlock, SWT.None);
			valueBlock.setLayout(new GridLayout(1, false));
			valueBlock.setVisible(false);

			btBlock = new Composite(valueBlock, SWT.None);
			btBlock.setLayout(new GridLayout(2, false));
			btBlock.setVisible(true);

			final Label labShowOp = new Label(btBlock, SWT.NONE);
			labShowOp.setText("Show Operation:");
			showTag = new Button(btBlock, SWT.CHECK);
			showTag.addSelectionListener(this);
			showTag
					.setToolTipText(Messages.SOSPreferencePage1_show_operation_tooltipp);

			final Label lab3 = new Label(btBlock, SWT.NONE);
			lab3.setText("Omit Parameter:");
			omitTag = new Button(btBlock, SWT.CHECK);
			omitTag.addSelectionListener(this);
			omitTag
					.setToolTipText(Messages.SOSPreferencePage1_omit_parameter_tooltipp);

			valueTree = new Tree(valueBlock, SWT.CHECK | SWT.BORDER);
			valueTree.setItemCount(6);
			valueTree.getItem(0).setText(placeholderString1);
			valueTree.addSelectionListener(this);

			// description = new Text(configurationBlock, SWT.READ_ONLY);
			// description.setText("aaaaaaaaaaaaa");

			// tree.getItem(0).setText("Required");
			// tree.getItem(1).setText("Optional");

			// final Label lab1 = new Label(configurationBlock, SWT.NONE);
			// lab1.setText("Select Operation");
			// opCombo = new Combo(configurationBlock, SWT.BORDER);
			// opCombo.addSelectionListener(this);
			// opCombo.add(placeholderString1);
			//
			// final Label labShowOp = new Label(configurationBlock, SWT.NONE);
			// labShowOp.setText("Show Operation");
			// showTag = new Button(configurationBlock, SWT.CHECK);
			// showTag.addSelectionListener(this);
			// showTag.setToolTipText(Messages.SOSPreferencePage1_show_operation_tooltipp);
			//
			// final Label lab2 = new Label(configurationBlock, SWT.NONE);
			// lab2.setText("Select Parameter");
			// paramCombo = new Combo(configurationBlock, SWT.BORDER);
			// paramCombo.addSelectionListener(this);
			// paramCombo.add(placeholderString1);
			// paramCombo.setToolTipText(Messages.SOSPreferencePage1_select_parameter_tooltipp);
			//
			// final Label lab3 = new Label(configurationBlock, SWT.NONE);
			// lab3.setText("Omit Parameter");
			// omitTag = new Button( configurationBlock, SWT.CHECK );
			// omitTag.addSelectionListener(this);
			// omitTag.setToolTipText(Messages.SOSPreferencePage1_omit_parameter_tooltipp);

		} catch (final Exception e) {
			LOGGER.warn("Error getting capabilities", e);
		}

		// parameterList = new org.eclipse.swt.widgets.List(layoutBlock,
		// SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		// parameterList.addSelectionListener(this);
		// parameterList.add(placeholderString2);
		// parameterList.add(placeholderString2);
		// parameterList.add(placeholderString2);
		// parameterList.add(placeholderString2);
		// parameterList.add(placeholderString2);
		// parameterList.add(placeholderString2);

		// ScrollBar sbh = new ScrollBar(parameterList, SWT.HORIZONTAL);

		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(final IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	public void widgetDefaultSelected(final SelectionEvent e) {
	}

	private String identifySelectedOperation() {
		if (tree.getSelection() != null && tree.getSelection()[0] != null) {
			final TreeItem selectedItem = tree.getSelection()[0];
			// identify the operation we are in
			TreeItem currentOperationItem = selectedItem;
			while (currentOperationItem.getParentItem() != null) {
				currentOperationItem = currentOperationItem.getParentItem();
			}
			return currentOperationItem.getText();
		} else {
			return null;
		}
	}

	private String identifySelectedParameter() {
		if (tree.getSelection() != null && tree.getSelection()[0] != null) {
			if (tree.getSelection()[0].getItemCount() == 0) {
				return tree.getSelection()[0].getText();
			}
		}

		return null;
		// if itemCount == 0 we have a parameterid
	}

	public void widgetSelected(final SelectionEvent e) {
		// treelistener
		if (e.widget instanceof Tree) {
			final Tree t = (Tree) e.widget;
			if (t.equals(tree)) {
				if (tree.getSelection() != null
						&& tree.getSelection()[0] != null) {

					final TreeItem selectedItem = tree.getSelection()[0];
					showTag.setSelection(SOSConfigurationRegistry.getInstance()
							.getShowOperation(urlCombo.getText(),
									identifySelectedOperation()));
					showTag.setEnabled(true);
					valueBlock.setVisible(true);

					// if itemCount == 0 we may have a parameterid
					if (selectedItem.getItemCount() == 0
							&& getParametersAsStrings(
									identifySelectedOperation()).contains(
									identifySelectedParameter())) {
						omitTag.setEnabled(true);
						try {
							omitTag.setSelection(SOSConfigurationRegistry
									.getInstance().getOmitParameter(
											urlCombo.getText(),
											identifySelectedOperation(),
											identifySelectedParameter()));
						} catch (final NoSuchElementException nsee1) {
							omitTag.setSelection(false);
						}
						valueTree.removeAll();

						final List<org.n52.oxf.owsCommon.capabilities.Parameter> p = getParameters(identifySelectedOperation());
						IDiscreteValueDomain<String> idvd;

						for (final Parameter parm : p) {
							if (parm.getServiceSidedName().equals(
									selectedItem.getText())) {
								if (parm.getValueDomain() instanceof IDiscreteValueDomain) {
									final String[] selectedValueItems = SOSConfigurationRegistry
											.getInstance()
											.getParameter(
													urlCombo.getText(),
													identifySelectedOperation(),
													parm.getServiceSidedName());
									final List<String> l = new LinkedList<String>();
									for (final String a : selectedValueItems) {
										l.add(a);
									}

									idvd = (IDiscreteValueDomain<String>) parm
											.getValueDomain();
									valueTree.setItemCount(idvd
											.getPossibleValues().size());
									int i = 0;
									for (final Object s : idvd
											.getPossibleValues()) {

										if (l.contains(String.valueOf(s))) {
											valueTree.getItem(i).setChecked(
													true);
										} else {
											valueTree.getItem(i).setChecked(
													false);
										}
										valueTree.getItem(i++).setText(
												String.valueOf(s));
									}
								}
							}
						}
					} else {
						omitTag.setEnabled(false);
						valueTree.removeAll();
					}
				} else {
					showTag.setEnabled(false);
					valueBlock.setVisible(false);
				}
			}

			if (t.equals(valueTree)) {
				if (valueTree.getItemCount() > 0) {
					final List<String> l = new LinkedList<String>();

					for (final TreeItem titem : valueTree.getItems()) {
						if (titem.getChecked() == true) {
							l.add(titem.getText());
						}
					}

					final String[] values = new String[l.size()];
					int j = 0;
					// XXX where is the difference: checked and selection?
					for (final String s : l) {
						values[j++] = s;
					}
					SOSConfigurationRegistry.getInstance().setParameter(
							urlCombo.getText(), identifySelectedOperation(),
							identifySelectedParameter(), values);
				}
			}
		}
		// buttonlistener
		if (e.widget instanceof Button) {
			final Button b = (Button) e.widget;
			// addButton
			if (b.equals(add)) {
				if (urlCombo.getText() != null && urlCombo.getText() != "") {
					SOSConfigurationRegistry.getInstance().addNewSOS(
							urlCombo.getText());
					updateCombo();
				}
			}
			// removeButton
			else if (b.equals(remove)) {
				if (urlCombo.getText() != null && urlCombo.getText() != "") {
					SOSConfigurationRegistry.getInstance().removeSOS(
							(urlCombo.getText()));
					updateCombo();
				}

			} else if (b.equals(showTag)) {
				SOSConfigurationRegistry.getInstance().setShowOperation(
						urlCombo.getText(), identifySelectedOperation(),
						b.getSelection());
			}

			else if (b.equals(omitTag)) {
				SOSConfigurationRegistry.getInstance().setOmitParameter(
						urlCombo.getText(), identifySelectedOperation(),
						identifySelectedParameter(), b.getSelection());
			}

		}
		// ComboBoxes
		if (e.widget instanceof Combo) {
			final Combo c = (Combo) e.widget;
			// urlCombo
			if (c.equals(urlCombo)) {
				urlCombohasChanged(c);
			}
			// // operationsCombo
			// else if (c.equals(opCombo)){
			// opCombohasChanged(c);
			// }
			// else if (c.equals(paramCombo)){
			// paramCombohasChanged(c);
			// }
		}

		// if (e.widget instanceof org.eclipse.swt.widgets.List) {
		// final org.eclipse.swt.widgets.List l = (org.eclipse.swt.widgets.List)
		// e.widget;
		// if (l.equals(parameterList)){
		// SOSConfigurationRegistry.getInstance().setParameter(urlCombo.getText(),
		// opCombo.getText(), paramCombo.getText(),l.getSelection());
		// }
		//
		// }
		// TODO save when ok or apply is pressed
		SOSConfigurationRegistry.getInstance().save();
	}

	/**
	 * Called by widgetSelected to initiate all needed actions on an url
	 * Selectionevent
	 * 
	 * @param c
	 */
	private void urlCombohasChanged(final Combo c) {
		if (urlCombo.getText() == null || urlCombo.getText().trim().equals("")) {
			configurationBlock.setVisible(false);
		} else {
			try {
				// opCombo.removeAll();
				final Map<String, Serializable> params = new HashMap<String, Serializable>(
						1);
				params.put(SOSDataStoreFactory.URL_SERVICE.key, new URL(
						urlCombo.getText()));
				caps = SOSDataStoreFactory.getInstance().getCapabilities(
						SOSDataStoreFactory.workOnParams(params));

				populateTree(tree);

				configurationBlock.setVisible(true);
			} catch (final Exception ee) {
				ee.printStackTrace();
				configurationBlock.setVisible(false);
			}
		}
	}

	private void populateTree(final Tree base) {
		base.setItemCount(caps.getOperations().getAllOperations().size());
		int i = 0;
		for (final OperationType op : caps.getOperations().getAllOperations()) {
			populateOperation(((SOSOperationType) op).getId(), base.getItem(i));
			// base.getItem(i++).
			i++;
		}
	}

	private void populateOperation(final String opID, final TreeItem item) {
		final ParameterConfiguration pc = ((SOSOperationType) caps
				.getOperations().getOperationTypeByName(opID))
				.getNewPreconfiguredConfiguration();
		item.setText(opID);
		item.setItemCount(2);
		final List<String> reqParams = pc.getRequiredParametersAsStrings();
		item.getItem(0).setText("Required");
		item.getItem(0).setItemCount(reqParams.size());
		int i = 0;
		for (final String s : reqParams) {
			item.getItem(0).getItem(i++).setText(s);
		}

		final List<String> optionalParams = pc.getOptionalParametersAsStrings();
		item.getItem(1).setText("Optional");
		item.getItem(1).setItemCount(optionalParams.size());
		i = 0;
		for (final String s : optionalParams) {
			item.getItem(1).getItem(i++).setText(s);
		}

		// base.getItem(i).setText();
	}

	private List<org.n52.oxf.owsCommon.capabilities.Parameter> getParameters(
			final String opName) {
		final ParameterConfiguration pc = ((SOSOperationType) caps
				.getOperations().getOperationTypeByName(opName))
				.getNewPreconfiguredConfiguration();
		final List<org.n52.oxf.owsCommon.capabilities.Parameter> pall = new LinkedList<org.n52.oxf.owsCommon.capabilities.Parameter>();
		pall.addAll(pc.getRequiredParameters());
		pall.addAll(pc.getOptionalParameters());
		return pall;
	}

	private List<String> getParametersAsStrings(final String opName) {
		final ParameterConfiguration pc = ((SOSOperationType) caps
				.getOperations().getOperationTypeByName(opName))
				.getNewPreconfiguredConfiguration();
		final List<String> pall = new LinkedList<String>();
		final SOSOperationType op = (SOSOperationType) caps.getOperations()
				.getOperationTypeByName(opName);
		for (final Parameter p : pc.getRequiredParameters()) {
			pall.add(p.getServiceSidedName());
		}
		for (final Parameter p : pc.getOptionalParameters()) {
			pall.add(p.getServiceSidedName());
		}

		return pall;
	}

	public static void main(final String[] args) {
		PropertyConfigurator.configure(GeneralConfigurationRegistry
				.getInstance().getLog4jPropertiesFilename());
		final Display display = new Display();
		final Shell shell = new Shell(display);

		final SOSPreferencePage1 page = new SOSPreferencePage1();
		shell.setLayout(new GridLayout());

		final Control c = page.createContents(shell);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	public void modifyText(final ModifyEvent e) {
		// Combo c = (Combo)e.widget;
		// if (c.equals(urlCombo)){
		// updateCombo();
		// }
	}
}