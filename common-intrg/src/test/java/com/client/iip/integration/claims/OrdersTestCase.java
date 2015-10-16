package com.client.iip.integration.claims;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.ClientServiceBaseTestCase;
import com.stoneriver.iip.orders.api.OrdersDTO;

public class OrdersTestCase extends ClientServiceBaseTestCase{
	
	private final Logger logger =
			LoggerFactory.getLogger(OrdersTestCase.class);	

	private final static String SEND_ORDER_ENDPOINT = "vm://bpm.process.start.in?connector=vmSync";
		
	
	
	/**
	 * Constructor for ValidationBaseServiceTestCase.
	 * @param name name.
	 */
	public OrdersTestCase(final String name) {
		super(name);
	}
	
	/**
	 * @return location for bootstrap
	 */	
	protected String getIIPBootstrapConfigFile() {
		return "src/test/resources/iip-test-integration-config/client-mule-bootstrap-config.xml";
	}	

	
	/**
	 * Test Orders Service - Call going out from stream into external system 
	 * Real Time push Pattern
	 */
	public void testSendOrders() {

		try{
			//Convert File to String
			String strXML = FileUtils.readFileToString(new File("src/test/resources/xml/OrderRequest.xml"));			
			//Convert xml into Stream Object
			logger.info("Order Request Payload : " + strXML);
			OrdersDTO orderObj = (OrdersDTO)getXStreamInstance().convert2Object(strXML);
			Map<String, Object> orders = new HashMap<String, Object>();
			orders.put("order", orderObj);
			//Load to HashMap and set and set as Inbound properties
			HashMap<String, Object> propertyMap = new HashMap<String, Object>();
			propertyMap.put("createProcessParameters", orders);
			Object payload = sendSyncEvent(SEND_ORDER_ENDPOINT, orderObj, propertyMap, 0);
			logger.info("Order Response Payload : " + getXStreamInstance().convert2XML(payload));
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e);
		}
	
    }
	
		
}
