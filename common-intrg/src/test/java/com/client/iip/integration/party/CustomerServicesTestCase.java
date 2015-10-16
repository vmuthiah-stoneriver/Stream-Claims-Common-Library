package com.client.iip.integration.party;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.ClientServiceBaseTestCase;

/**
 * Test case for Party interfaces.
 * 
 * @author Guru Radhakrishnan
 *
 */
public class CustomerServicesTestCase extends ClientServiceBaseTestCase {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final static String CUSTOMER_DETAILS_ENDPOINT = "vm://customerDetails?connector=vmSync";
	
	private final static String CUSTOMER_UPDATE_ENDPOINT = "vm://customerUpdate?connector=vmSync";
	
	private final static String CUSTOMER_DUPCHECK_ENDPOINT = "vm://customerDupCheck?connector=vmSync";


	/**
	 * Constructor for CustomerUpdateTestCase.
	 * 
	 * @param name name.
	 */
	public CustomerServicesTestCase(String name) {
		super(name);
		logger.debug("------------------------------ Process started ------------------------------");
		logger.debug("Inside CustomerServicesTestCase constructor. Test case initiated.");
	}
	
	/**
	 * @return location for bootstrap
	 */	
	protected String getIIPBootstrapConfigFile() {
		return "src/test/resources/iip-test-integration-config/client-mule-bootstrap-config.xml";
	}	

	/**
	 * Test party details.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testCustomerDetails() {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/PartyDetailRequest.xml"));			
			logger.info("CustomerDetails Request Payload : " + strXML);
			String responseXML = (String)sendSyncEvent(CUSTOMER_DETAILS_ENDPOINT, strXML, null, 0);
			logger.info("CustomerDetails Response Payload : " + responseXML);
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}
	
    }
	
	/**
	 * Test party update.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testCustomerUpdate() {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/PartyPersonNewRequest.xml"));
			logger.info("CustomerUpdate Request Payload  : " + strXML);
			//ClientOrganizationDTO personObj = (ClientOrganizationDTO)getXStreamInstance().convert2Object(strXML);			
			String responseXML = (String)sendSyncEvent(CUSTOMER_UPDATE_ENDPOINT, strXML, null, 0);
			logger.info("CustomerUpdate Response Payload : " + responseXML);
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}
	
    }
	
	/**
	 * Test duplicate parties.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testCustomerDupCheck() {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/DuplicatePartiesRequest.xml"));			
			logger.info("CustomerDupCheck Request Payload : " + strXML);
			String responseXML = (String)sendSyncEvent(CUSTOMER_DUPCHECK_ENDPOINT, strXML, null, 0);
			logger.info("CustomerDupCheck Response Payload : " + responseXML);
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}
	
    }
	
	public static void main(String[] args){
		CustomerServicesTestCase test = new CustomerServicesTestCase("Party");
		test.testCustomerUpdate();
		
	}
	
}
