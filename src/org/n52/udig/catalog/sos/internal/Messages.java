package org.n52.udig.catalog.sos.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.n52.udig.catalog.sos.internal.messages"; //$NON-NLS-1$
	public static String SOSDataStoreFactory_desc;
	public static String SOSDataStoreFactory_name;
	
	public static String SOSServiceExtension_protocolfailure;
	public static String SOSServiceExtension_badService;
	public static String SOSServiceExtension_nullURL;

	public static String SOSServiceImpl_broken;
	public static String SOSServiceImpl_task_name;

	public static String SOSWizardPage_wizarddescription;
	public static String SOSWizardPage_main_title;
	public static String SOSWizardPage_label_url_text;
	public static String SOSWizardPage_serverConnectionError;
	public static String SOSWizardPage_connectionProblem;
	public static String SOSWizardPage_error_invalidURL;
	public static String SOSWizardPage_label_url_tooltip;
	
	public static String SOSServiceImpl_connecting_to;
	public static String SOSServiceImpl_could_not_connect;
	
	public static String SOSParameterConfigurationPage_label_reqParams;
	
	public static String SOSPreferencePage1_show_operation_tooltipp;
	public static String SOSPreferencePage1_select_parameter_tooltipp;
	public static String SOSPreferencePage1_omit_parameter_tooltipp;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
