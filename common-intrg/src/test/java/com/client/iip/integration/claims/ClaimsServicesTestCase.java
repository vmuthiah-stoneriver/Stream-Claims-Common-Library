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
public class ClaimsServicesTestCase extends ClientServiceBaseTestCase {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final static String CLAIM_IMPORT_ENDPOINT = "vm://claimImport?connector=vmSync";

	/**
	 * Constructor for ApplyCreditsTestCase.
	 * 
	 * @param name
	 *            name.
	 */
	public ClaimsServicesTestCase(String name) {
		super(name);
		logger.debug("------------------------------ Process started ------------------------------");
		logger.debug("Inside ClaimsServicesTestCase constructor. Test case initiated.");
	}

	/**
	 * @return location for bootstrap
	 */	
	protected String getIIPBootstrapConfigFile() {
		return "src/test/resources/iip-test-integration-config/client-mule-bootstrap-config.xml";
	}

	/**
	 * Test Claim Import method.
	 * 
	 * @throws Exception
	 *             when there are errors.
	 */
	public void testClaimImport() throws Exception {
		try{
		//Convert File to String
		String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/Claim Import - Notes.xml"));			
		logger.info("ClaimImport Request Payload : " + strXML);
		String responseXML = (String)sendSyncEvent(CLAIM_IMPORT_ENDPOINT, strXML, null, 0);
		logger.info("ClaimImport Response Payload : " + responseXML);

		} catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}

	}
	
}
