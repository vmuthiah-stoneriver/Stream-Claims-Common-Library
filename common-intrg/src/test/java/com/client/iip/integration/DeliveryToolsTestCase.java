package com.client.iip.integration;

import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.ClientServiceBaseTestCase;
import com.stoneriver.iip.entconfig.api.UserDetailsDTO;
import com.stoneriver.iip.entconfig.tool.ToolDeliveryDetailsCompositeDTO;

public class DeliveryToolsTestCase extends ClientServiceBaseTestCase {
	
	private final Logger logger =
			LoggerFactory.getLogger(DeliveryToolsTestCase.class);
	
	private final static String EMAIL_TOOL_ENDPOINT = "emailToolEndpoint";
	
	/**
	 * Constructor for ValidationBaseServiceTestCase.
	 * @param name name.
	 */
	public DeliveryToolsTestCase(final String name) {
		super(name);
	}	
	
	@Override
	protected String getIIPBootstrapConfigFile() {
		return "src/test/resources/iip-test-integration-config/client-mule-bootstrap-config.xml";
	}
	
	public void testDeliverEmail() {

		try{
			ToolDeliveryDetailsCompositeDTO toolCriteria = new ToolDeliveryDetailsCompositeDTO();
			UserDetailsDTO user = new UserDetailsDTO();
			user.setEmail("guru.radhakrishnan@stoneriver.com");
			Collection<UserDetailsDTO> userList= new ArrayList<UserDetailsDTO>();
			userList.add(user);
			toolCriteria.setToRecipients(userList);
			toolCriteria.setSubject("Email Subject - Test Subject");
			toolCriteria.setDescription("Email Body - Test Body");
			Object payload = sendSyncEvent(EMAIL_TOOL_ENDPOINT, toolCriteria, null, 0);
			logger.info("Email Delivered");
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}
	
    }	
	

}
