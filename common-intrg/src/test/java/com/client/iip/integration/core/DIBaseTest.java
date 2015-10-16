package com.client.iip.integration.core;

/*
 * © 2008 Fiserv Insurance Solutions, Inc.
 *
 * Proprietary and Confidential
 */
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 *  Can populate your test using normal setter dependency injection or by
 *  Field Injection. Declare protected variables of the required type which 
 *  match named beans in the context. This is autowire by name, rather than 
 *  type. Setter Dependency Injection is the default: set the 
 *  populateProtectedVariables property to true in the constructor to switch 
 *  on Field Injection.
 *
 * The following are some useful methods:
 * 
 * getConfigLocations() return new String[] { "your-context.xml" };
 *   This will load your configuration into the application context for testing.
 *   The file must be located in "src/main/resources", otherwise Maven can not 
 *   find the file. The file could contain something like the following to then 
 *   point to "src/test/resources": 
 *   <import resource="classpath*:/application-config/data-sources-1.0.xml"/>
 * 
 * onSetUp() and  onTearDown()
 *   Use this instead of setUp() and tearDown() 
 * 
 * getApplicationContext()
 *   Use this call to get the application Context 
 *   
 */
//@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
//@ContextConfiguration(locations = {
//      "classpath:/iip-test-integration-config/client-mule-bootstrap-config.xml"})
public class DIBaseTest 
		extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Constructor.
     */

	public DIBaseTest() throws Exception {
		setUp();
	}
	
  /**
   * Constructor.
   * 
   * @param name the name of the test case
   */
	public DIBaseTest(String name) throws Exception{
		super(name);
	}
	
	// specifies the Spring configuration to load for this fixture
		protected String[] getConfigLocations() {
			// applicationContext-dbaccess-test.xml is the place where access to my test db
			// is defined and overrides access defined in applicationContext.xml
			return new String[] {
			        "iip-integration-config/client-mule-bean-config.xml",
			        };
		}
	


}

