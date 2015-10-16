package com.client.iip.integration.orders;

import java.util.HashMap;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;

import com.stoneriver.iip.orders.api.OrdersDTO;

/**
 * 
 * Convert string to object
 * 
 * @author Saurabh.Bhatnagar
 * 
 */
public class OrdersOutboundTransformer extends AbstractMessageTransformer {

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {

		@SuppressWarnings("unchecked")
		Map<String, Object> variableMap = (HashMap<String, Object>) message
				.getProperty("createProcessParameters", PropertyScope.INBOUND);

		message.setPayload((OrdersDTO) variableMap.get("order"));

		return message;
	}

}
