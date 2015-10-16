package com.client.iip.integration.core;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.mule.api.MuleContext;
import org.mule.config.spring.SpringRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fiserv.isd.iip.core.framework.test.MuleTestContextHolder;


public class SpringBatchTestCase extends ClientServiceBaseTestCase {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final static String BATCH_STATUS_ENDPOINT = "vm://batchJobStatusInquiry?connector=vmSync";

	/**
	 * Constructor for SpringBatchTestCase.
	 * 
	 * @param name name.
	 */
	public SpringBatchTestCase(String name) {
		super(name);
		logger.debug("------------------------------ Process started ------------------------------");
		logger.debug("Inside SpringBatchTestCase constructor. Test case initiated.");
	}

	/**
	 * SetUp method.
	 * 
	 * @throws Exception when there are errors
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		MuleContext muleContext = MuleTestContextHolder.getMuleContext();
		ConfigurableApplicationContext springContext = (ConfigurableApplicationContext)
			muleContext.getRegistry().lookupObject(SpringRegistry.SPRING_APPLICATION_CONTEXT);		
		
		UsernamePasswordAuthenticationToken login =
				new UsernamePasswordAuthenticationToken("sysadmin", "password");

		AuthenticationManager authManager = (AuthenticationManager)
				springContext.getBean("authenticationManager");

		SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));		
	}
	
	@Override
	protected String getIIPBootstrapConfigFile() {
		return "iip-test-integration-config/client-mule-bootstrap-config.xml";
	}
	

	/**
	 * Tear down method.
	 * 
	 * @throws Exception when there are errors
	 */
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		logger.debug("Inside tearDown() method of SpringBatchTestCase.");
	}


    
	/**
	 * Invoke Batch Job.
	 * 
	 * @throws Exception  when there are errors.
	 */
	public void testBatchJob() throws Exception {
		
		try {
			logger.debug("Batch Test Case");
			
			JobExecution exec = launchStep("glExport", "glExportStep");
			
		
			ExitStatus exitStatus = exec.getExitStatus();
			assertTrue("Job didn't complete. - " + exitStatus.getExitDescription(), 
				ExitStatus.COMPLETED.getExitCode().equals(exitStatus.getExitCode()));
		
			logger.debug("Batch Test Case:"
					+ exitStatus);
			logger.debug("----------------Process Ended----------------------");
		} catch(Throwable t) {
			fail(t);
		}
	}
	
	public void testBatchJobStatus(){
		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/BatchJobStatusRequest.xml"));			
			logger.info("BatchJobStatus Request Payload : " + strXML);
			String responseXML = (String)sendSyncEvent(BATCH_STATUS_ENDPOINT, strXML, null, 0);
			logger.info("BatchJobStatus Response Payload : " + responseXML);
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}		
	}
	
	
	/*protected JobParameters getUniqueJobParameters() {
		Map<String, JobParameter> parameters = new HashMap<String, JobParameter>();
		parameters.put("runDate", new JobParameter(DateUtility.getSystemDateOnly()));
		parameters.put("lastRunDate", new JobParameter(DateUtility.subtractDays(
				DateUtility.getSystemDateTime(), 100)));
		
		return new JobParameters(parameters);
	}	*/
	
}
