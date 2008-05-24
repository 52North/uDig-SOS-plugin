/**
 *
 */
package org.n52.udig.catalog.internal.sos.ui;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geotools.data.ows.OperationType;
import org.n52.oxf.owsCommon.capabilities.IDiscreteValueDomain;
import org.n52.oxf.owsCommon.capabilities.Parameter;
import org.n52.udig.catalog.internal.sos.SOSPlugin;
import org.n52.udig.catalog.internal.sos.dataStore.SOSCapabilities;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;

/**
 * @author 52n
 *
 */
public class SOSPreferencePage1 extends PreferencePage implements
		IWorkbenchPreferencePage, SelectionListener {

	private Button add;
	private Button remove;
	private Combo urlCombo;
	private Combo opCombo;
	private Combo paramCombo;
	private Composite configurationBlock;
	private Composite composite;
	private SOSCapabilities caps;
	private Button omitTag;
	private Button showTag;
	private Composite layoutBlock;
	private org.eclipse.swt.widgets.List parameterList;
	private final String placeholderString1 = "                                   ";
	private final String placeholderString2 = "                                                                              ";

	/**
	 *
	 */
	public SOSPreferencePage1() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param title
	 */
	public SOSPreferencePage1(final String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param title
	 * @param image
	 */
	public SOSPreferencePage1(final String title, final ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}


	private void updateCombo(){
		if (urlCombo != null){
			urlCombo.removeAll();
			urlCombo.add(" ");
		}
		for (final String s:SOSConfigurationRegistry.getInstance().getConfiguredSOSs()){
			urlCombo.add(s);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		composite = new Group(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
		final Label label = new Label(composite, SWT.NONE);
		label.setText("SOS");
		urlCombo = new Combo(composite, SWT.BORDER);
		urlCombo.setVisibleItemCount(15);
		urlCombo.addSelectionListener(this);
		urlCombo.add("");
		for (final String s:SOSConfigurationRegistry.getInstance().getConfiguredSOSs()){
			urlCombo.add(s);
		}

		// buttons
		new Label(composite, SWT.NONE).setText("");

		final Composite buttonBlock = new Group(composite, SWT.NULL);
		buttonBlock.setLayout(new GridLayout(2, false));

		add = new Button(buttonBlock,SWT.PUSH);
		add.setText("Add");
		add.setToolTipText("Adds a new SOS");
		add.addSelectionListener(this);

		remove = new Button(buttonBlock,SWT.PUSH);
		remove.setText("Remove");
		remove.setToolTipText("Remove selected SOS from configuration");
		remove.addSelectionListener(this);

		// Configuration Group
		new Label(composite, SWT.NONE).setText("");

		layoutBlock= new Group(composite, SWT.NULL);
		layoutBlock.setLayout(new GridLayout(2, false));
		layoutBlock.setVisible(false);

		configurationBlock = new Group(layoutBlock, SWT.NONE);
		configurationBlock.setLayout(new GridLayout(2, false));

		try {
			final Label lab1 = new Label(configurationBlock, SWT.NONE);
			lab1.setText("Select Operation");
			opCombo = new Combo(configurationBlock, SWT.BORDER);
			opCombo.addSelectionListener(this);
			opCombo.add(placeholderString1);

			final Label labShowOp = new Label(configurationBlock, SWT.NONE);
			labShowOp.setText("Show Operation");
			showTag = new Button(configurationBlock, SWT.CHECK);
			showTag.addSelectionListener(this);
			//TODO addtooltip

			final Label lab2 = new Label(configurationBlock, SWT.NONE);
			lab2.setText("Select Parameter");
			paramCombo = new Combo(configurationBlock, SWT.BORDER);
			paramCombo.addSelectionListener(this);
			paramCombo.add(placeholderString1);
			//TODO addtooltip

			final Label lab3 = new Label(configurationBlock, SWT.NONE);
			lab3.setText("Omit Parameter");
			omitTag = new Button( configurationBlock, SWT.CHECK );
			omitTag.addSelectionListener(this);

		} catch (final Exception e) {
			SOSPlugin.log( "Error getting capabilities", e);
		}

		parameterList = new org.eclipse.swt.widgets.List(layoutBlock, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		parameterList.addSelectionListener(this);
		parameterList.add(placeholderString2);
		parameterList.add(placeholderString2);
		parameterList.add(placeholderString2);
		parameterList.add(placeholderString2);
		parameterList.add(placeholderString2);
		parameterList.add(placeholderString2);

//		ScrollBar sbh = new ScrollBar(parameterList, SWT.HORIZONTAL);

		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(final IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	public void widgetDefaultSelected(final SelectionEvent e) {
	}

	public void widgetSelected(final SelectionEvent e) {
		// buttonlistener
		if (e.widget instanceof Button) {
			final Button b = (Button) e.widget;
			//addButton
			if(b.equals(add)){
	        	if (urlCombo.getText()!= null && urlCombo.getText() != ""){
	        		SOSConfigurationRegistry.getInstance().addNewSOS(urlCombo.getText());
	        		updateCombo();
	        	}
	        }
        	// removeButton
			else if(b.equals(remove)){
	        		if (urlCombo.getText()!= null && urlCombo.getText() != ""){
	            		SOSConfigurationRegistry.getInstance().removeSOS((urlCombo.getText()));
	            		updateCombo();
	            	}

	        	}
			else if(b.equals(showTag)){
				SOSConfigurationRegistry.getInstance().setShowOperation(urlCombo.getText(), opCombo.getText(), b.getSelection());
			}

			else if(b.equals(omitTag)){
				SOSConfigurationRegistry.getInstance().setOmitParameter(urlCombo.getText(), opCombo.getText(), paramCombo.getText(),b.getSelection());
			}

		}
		// ComboBoxes
		if (e.widget instanceof Combo) {
			final Combo c = (Combo) e.widget;
			// urlCombo
			if (c.equals(urlCombo)){
				urlCombohasChanged(c);
			}
			// operationsCombo
			else if (c.equals(opCombo)){
				opCombohasChanged(c);
			}
			else if (c.equals(paramCombo)){
				paramCombohasChanged(c);
			}
		}

		if (e.widget instanceof org.eclipse.swt.widgets.List) {
			final org.eclipse.swt.widgets.List l = (org.eclipse.swt.widgets.List) e.widget;
			if (l.equals(parameterList)){
				SOSConfigurationRegistry.getInstance().setParameter(urlCombo.getText(), opCombo.getText(), paramCombo.getText(),l.getSelection());
			}

		}
        //TODO save when ok or apply is pressed
        SOSConfigurationRegistry.getInstance().save();
	}

	/**
	 * Called by widgetSelected to initiate all needed actions on an url Selectionevent
	 * @param c
	 */
	private void urlCombohasChanged(final Combo c){
		if (urlCombo.getText()== null || urlCombo.getText().trim().equals("")){
			layoutBlock.setVisible(false);
		} else{
			try {
				opCombo.removeAll();
				final Map<String, Serializable> params = new HashMap<String, Serializable>(1);
				params.put(SOSDataStoreFactory.URL_CAPS.key, new URL(urlCombo.getText()));
				caps = SOSDataStoreFactory.getInstance().getCapabilities(SOSDataStoreFactory.workOnParams(params));

//				new SOSCapabilities(new SOSAdapter(
//
//							SOSDataStoreFactory.serviceVersion), SOSDataStoreFactory.workOnParams(params));
				for (final OperationType op:caps.getOperations().getAllOperations()){
					opCombo.add(((SOSOperationType)op).getId());
				}
				layoutBlock.setVisible(true);
			} catch (final Exception ee) {
				ee.printStackTrace();
			}
		}
	}

	private void opCombohasChanged(final Combo c){
		paramCombo.removeAll();
		parameterList.removeAll();
		showTag.setSelection(SOSConfigurationRegistry.getInstance().getShowOperation(urlCombo.getText(), opCombo.getText()));
		for (final Parameter parm:getParameters()){
			paramCombo.add(parm.getServiceSidedName());
		}
	}

	private void paramCombohasChanged(final Combo c){
//		parameterList = new org.eclipse.swt.widgets.List(layoutBlock, SWT.MULTI);
//		parameterList.addSelectionListener(this);
		parameterList.removeAll();
		final List<org.n52.oxf.owsCommon.capabilities.Parameter> p = getParameters();
		IDiscreteValueDomain<String> idvd;

		for (final Parameter parm:p){
			if (parm.getServiceSidedName().equals(c.getText())){
				if (parm.getValueDomain() instanceof IDiscreteValueDomain) {
					idvd = (IDiscreteValueDomain<String>) parm.getValueDomain();
					for (final Object s:idvd.getPossibleValues()){
						parameterList.add(String.valueOf(s));
					}
				}
			}
		}

		parameterList.setSelection(SOSConfigurationRegistry.getInstance().getParameter(urlCombo.getText(), opCombo.getText(), paramCombo.getText()));

		try {
			omitTag.setSelection(SOSConfigurationRegistry.getInstance().getOmitParameter(urlCombo.getText(), opCombo.getText(), paramCombo.getText()));
		} catch (final NoSuchElementException nsee1) {
			omitTag.setSelection(false);
		}

	}

	private List<org.n52.oxf.owsCommon.capabilities.Parameter> getParameters(){
		final List<org.n52.oxf.owsCommon.capabilities.Parameter> pall = new LinkedList<org.n52.oxf.owsCommon.capabilities.Parameter>();
		final SOSOperationType op = (SOSOperationType)caps.getOperations().getOperationTypeByName(opCombo.getText());
		pall.addAll(op.getParameterConfiguration().getRequiredParameters());
		pall.addAll(op.getParameterConfiguration().getOptionalParameters());
		return pall;
	}

	public static void main(final String[] args) {
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
//		Combo c = (Combo)e.widget;
//		if (c.equals(urlCombo)){
//			updateCombo();
//		}
	}
}