package com.client.iip.integration.claims;



import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.ClientServiceBaseTestCase;

/**
 * 
 * @author Guru Radhakrishnan
 * 
 */
public class EnterpriseConfigTestCase extends ClientServiceBaseTestCase {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final static String RETRIEVE_COMPANIES_ENDPOINT = "vm://companyForCorporationList?connector=vmSync";
	
	private final static String RETRIEVE_COMPANY_LOB_ENDPOINT = "vm://companyLOBForCompanyList?connector=vmSync";
	
	private final static String RETRIEVE_CITIES_ENDPOINT = "vm://cityList?connector=vmSync";
	
	private final static String RETRIEVE_COUNTIES_ENDPOINT = "vm://countyList?connector=vmSync";
	
	private final static String RETRIEVE_STATES_ENDPOINT = "vm://stateList?connector=vmSync";
	
	private final static String RETRIEVE_MONCTG_ENDPOINT = "vm://monetaryCategory?connector=vmSync";
	
	private final static String RETRIEVE_RESERVES_ENDPOINT = "vm://reserveFinancialDetails?connector=vmSync";
	
	private final static String RETRIEVE_COVERAGES_ENDPOINT = "vm://coverageFinancialDetails?connector=vmSync";
	
	private final static String RETRIEVE_ALLLOBBYCOMPANY_ENDPOINT = "vm://allLOBsByCompanyList?connector=vmSync";

	/**
	 * Constructor for GetConfigurationTestCase.
	 * 
	 * @param name
	 *            name.
	 */
	public EnterpriseConfigTestCase(String name) {
		super(name);
		logger.debug("------------------------------ Process started ------------------------------");
		logger.debug("Inside EnterpriseConfigTestCase constructor. Test case initiated.");
	}
	
	/**
	 * @return location for bootstrap
	 */	
	protected String getIIPBootstrapConfigFile() {
		return "src/test/resources/iip-test-integration-config/client-mule-bootstrap-config.xml";
	}
	
	/**
	 * Test Retrieve Companies For Corporation method.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testRetrieveCompaniesForCorporation() throws Exception {

		try{
		//Convert File to String
		String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/AllCompaniesForCorpRequest.xml"));			
		logger.info("RetrieveCompaniesForCorporation Request Payload : " + strXML);
		String responseXML = (String)sendSyncEvent(RETRIEVE_COMPANIES_ENDPOINT, strXML, null, 0);
		logger.info("RetrieveCompaniesForCorporation Response Payload : " + responseXML);

		} catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}

	}
	
	/**
	 * Test Retrieve Company LOB For Company method.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testRetrieveCompanyLOBForCompany() throws Exception {

	try{
		//Convert File to String
		String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/CompanyLOBForCompanyRequest.xml"));			
		logger.info("RetrieveCompanyLOBForCompany Request Payload : " + strXML);
		String responseXML = (String)sendSyncEvent(RETRIEVE_COMPANY_LOB_ENDPOINT, strXML, null, 0);
		logger.info("RetrieveCompanyLOBForCompany Response Payload : " + responseXML);

		} catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}

	}
	
	/**
	 * Test Retrieve Cities method.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testRetrieveCities() throws Exception {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/AllCitiesRequest.xml"));			
			logger.info("RetrieveCities Request Payload : " + strXML);
			String responseXML = (String)sendSyncEvent(RETRIEVE_CITIES_ENDPOINT, strXML, null, 0);
			logger.info("RetrieveCities Response Payload : " + responseXML);

			} catch (Throwable e) {
				e.printStackTrace();
				fail(e);
			}

	}
	
	/**
	 * Test Retrieve Counties method.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testRetrieveCounties() throws Exception {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/AllCountiesRequest.xml"));			
			logger.info("RetrieveCounties Request Payload : " + strXML);
			String responseXML = (String)sendSyncEvent(RETRIEVE_COUNTIES_ENDPOINT, strXML, null, 0);
			logger.info("RetrieveCounties Response Payload : " + responseXML);

			} catch (Throwable e) {
				e.printStackTrace();
				fail(e);
			}

	}
	
	/**
	 * Test Retrieve States method.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testRetrieveStates() throws Exception {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/AllStatesRequest.xml"));			
			logger.info("RetrieveStates Request Payload : " + strXML);
			String responseXML = (String)sendSyncEvent(RETRIEVE_STATES_ENDPOINT, strXML, null, 0);
			logger.info("RetrieveStates Response Payload : " + responseXML);

			} catch (Throwable e) {
				e.printStackTrace();
				fail(e);
			}

	}
	
	/**
	 * Test Retrieve Monetary Category Code method.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testReteriveMonetaryCategoryCode() throws Exception {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/MonetaryCategoryCodeRequest.xml"));			
			logger.info("ReteriveMonetaryCategoryCode Request Payload : " + strXML);
			String responseXML = (String)sendSyncEvent(RETRIEVE_MONCTG_ENDPOINT, strXML, null, 0);
			logger.info("ReteriveMonetaryCategoryCode Response Payload : " + responseXML);

			} catch (Throwable e) {
				e.printStackTrace();
				fail(e);
			}

	}
	
	/**
	 * Test Retrieve Reserve Financial Data method.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testRetrieveReserveFinanicalData() throws Exception {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/FinancialReserveRequest_1.xml"));			
			logger.info("RetrieveReserveFinanicalData Request Payload : " + strXML);
			String responseXML = (String)sendSyncEvent(RETRIEVE_RESERVES_ENDPOINT, strXML, null, 0);
			logger.info("RetrieveReserveFinanicalData Response Payload : " + responseXML);

			} catch (Throwable e) {
				e.printStackTrace();
				fail(e);
			}

	}
	
	/**
	 * Test Retrieve Coverage Financial Data method.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testRetrieveCoverageFinanicalData() throws Exception {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/FinancialCoverageRequest_1.xml"));			
			logger.info("RetrieveCoverageFinanicalData Request Payload : " + strXML);
			String responseXML = (String)sendSyncEvent(RETRIEVE_COVERAGES_ENDPOINT, strXML, null, 0);
			logger.info("RetrieveCoverageFinanicalData Response Payload : " + responseXML);

			} catch (Throwable e) {
				e.printStackTrace();
				fail(e);
			}

	}
	

	
}
