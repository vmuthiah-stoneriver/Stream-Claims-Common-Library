package com.client.iip.integration.claims;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.ClientServiceBaseTestCase;
import com.client.iip.integration.core.converter.IIPXStreamDateTimestampConverter;
import com.client.iip.integration.core.util.IIPXStream;
import com.thoughtworks.xstream.XStream;


/**
 * BusinessComponentBaseServiceTestCase is the base class for all the
 * service test cases. This TestCase overrides getBootstrapConfigFile
 * to configure application specific.
 *
 * @author Guru Radhakrishnan.
 *
 */
public class PolicyPullTestCase extends ClientServiceBaseTestCase {
	
	private final Logger logger =
			LoggerFactory.getLogger(PolicyPullTestCase.class);	
	
	private final static String SEARCH_ENDPOINT = "policySearchEndpoint";
	
	private final static String DETAILS_ENDPOINT = "policyRetrieveDetailsEndpoint";
	
	private final static String IMPORT_ENDPOINT = "policyImportEndpoint";
	
	private final static String LIST_ENDPOINT = "policyListUnitsEndpoint";
	
	private final static String REFRESH_ENDPOINT = "policyReImportEndpoint";
	

	/**
	 * Constructor for ValidationBaseServiceTestCase.
	 * @param name name.
	 */
	public PolicyPullTestCase(final String name) {
		super(name);
	}

	/**
	 * @return location for bootstrap
	 */	
	protected String getIIPBootstrapConfigFile() {
		return "src/test/resources/iip-test-integration-config/client-mule-bootstrap-config.xml";
	}
	
	/**
	 * Test Policy Search Service - Call going out from stream into external system 
	 * Real Time on demand Request-Response Pattern
	 */
	public void testPolicySearch() {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/PolicySearchRequest.xml"));			
			//Convert xml into Stream Object
			logger.info("PolicySearch Request Payload : " + strXML);
			Object criteria = getXStreamInstance().convert2Object(strXML);
			Object payload = sendSyncEvent(SEARCH_ENDPOINT, criteria, null, 0);
			logger.info("PolicySearch Response Payload : " + getXStreamInstance().convert2XML(payload));
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}
	
    }
	
	/**
	 * Test Policy Unit Details Service - Call going out from stream into external system 
	 * Real Time on demand Request-Response Pattern
	 */
	public void testPolicyDetails() {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/PolicyDetailsRequest.xml"));			
			//Convert xml into Stream Object
			logger.info("PolicyDetails Request Payload : " + strXML);
			Object criteria = getXStreamInstance().convert2Object(strXML);
			Object payload = sendSyncEvent(DETAILS_ENDPOINT, criteria, null, 0);
			logger.info("PolicyDetails Response Payload : " + getXStreamInstance().convert2XML(payload));
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}
	
    }
	
	/**
	 * Test Policy Import Service - Call going out from stream into external system 
	 * Real Time on demand Request-Response Pattern
	 */
	public void testPolicyImport() {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/PolicyImportRequest.xml"));			
			//Convert xml into Stream Object
			logger.info("PolicyImport Request Payload : " + strXML);
			Object criteria = getXStreamInstance().convert2Object(strXML);
			Object payload = sendSyncEvent(IMPORT_ENDPOINT, criteria, null, 0);
			logger.info("PolicyImport Response Payload : " + getXStreamInstance().convert2XML(payload));
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}
	
    }
	
	/**
	 * Test Policy List Units Service - Call going out from stream into external system 
	 * Real Time on demand Request-Response Pattern
	 */
	public void testPolicyListUnits() {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/PolicyListUnitsRequest.xml"));			
			//Convert xml into Stream Object
			logger.info("PolicyListUnits Request Payload : " + strXML);
			Object criteria = getXStreamInstance().convert2Object(strXML);
			Object payload = sendSyncEvent(LIST_ENDPOINT, criteria, null, 0);
			logger.info("PolicyListUnits Response Payload : " + getXStreamInstance().convert2XML(payload));
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}
	
    }
	
	/**
	 * Test Policy Reimport Service - Call going out from stream into external system 
	 * Real Time on demand Request-Response Pattern
	 */
	public void testPolicyRefresh() {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/PolicyReimportRequest.xml"));			
			//Convert xml into Stream Object
			logger.info("Policy Reimport Request : " + strXML);
			Object criteria = getXStreamInstance().convert2Object(strXML);
			Object payload = sendSyncEvent(REFRESH_ENDPOINT, criteria, null, 0);
			logger.info("Policy Reimport Response : " + getXStreamInstance().convert2XML(payload));
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}
	
    }	
	
	public static void main(String args[]) throws Exception{
		
		//Convert File to String
		String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/PolicyReimportRequest.xml"));			
		//Convert xml into Stream Object
		System.out.println("Policy XML to Object : " + strXML);
		IIPXStream xstream = new IIPXStream(aliasFileList);
		xstream.registerConverter(new IIPXStreamDateTimestampConverter(),
				XStream.PRIORITY_VERY_HIGH);
		xstream.addDefaultImplementation(java.util.ArrayList.class, java.util.Collection.class);
		xstream.addDefaultImplementation(java.util.HashMap.class, java.util.Map.class);
		Object payload = xstream.convert2Object(strXML);
		//Object payload = sendSyncEvent(REFRESH_ENDPOINT, criteria, null, 0);
		System.out.println("Policy Object to XML : " + xstream.convert2XML(payload));		
		
	}
	
}

