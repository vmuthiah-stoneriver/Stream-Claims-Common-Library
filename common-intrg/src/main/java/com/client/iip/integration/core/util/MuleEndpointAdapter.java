package com.client.iip.integration.core.util;

import java.util.Map;

import org.mule.api.MuleException;
import org.mule.module.client.MuleClient;

import com.fiserv.isd.iip.core.service.MuleContextAccessor;

/**
 * Base class for all adapter implementations.
 */
public class MuleEndpointAdapter {

	/**
	 * Maintain reference of MuleClient.
	 */
	private MuleClient muleClient = null;

	/**
	 * Default constructor.
	 * 
	 */
	public MuleEndpointAdapter() {
		super();
	}

	/**
	 * Retrieve the MuleClient initializing if necessary.
	 * 
	 * @return the MuleClient
	 * @throws MuleException
	 *             exception sending event
	 */
	public MuleClient getMuleClient() throws MuleException {

		if (muleClient == null) {
			muleClient = new MuleClient(MuleContextAccessor.getMuleContext());
		}

		return muleClient;
	}

	/**
	 * Helper method to send a synchronous event to the ESB abstracting the
	 * details of the Mule implementation.
	 * 
	 * @param url
	 *            the Mule used to determine the destination and transport of
	 *            the message
	 * @param payload
	 *            the object that is the payload of the event
	 * 
	 * @return populated return object, or null if not found
	 * @throws MuleException
	 *             exception sending event
	 */
	public Object sendSyncEvent(String url, Object payload)
			throws MuleException {

		return sendSyncEvent(url, payload, null, 0);

	}
	
	/**
	 * Helper method to send a Asynchronous event to the ESB abstracting the
	 * details of the Mule implementation.
	 * 
	 * @param url
	 *            the Mule used to determine the destination and transport of
	 *            the message
	 * @param payload
	 *            the object that is the payload of the event
	 * 
	 * @return populated return object, or null if not found
	 * @throws MuleException
	 *             exception sending event
	 */
	public Object sendASyncEvent(String url, Object payload)
			throws MuleException {

		return sendSyncEvent(url, payload, null, 0);

	}	

	/**
	 * Helper method to send a synchronous event to the ESB abstracting the
	 * details of the Mule implementation.
	 * 
	 * @param url
	 *            the Mule used to determine the destination and transport of
	 *            the message
	 * @param payload
	 *            the object that is the payload of the event
	 * @param messageProperties
	 *            properties to be associated with the payload. In the case of
	 *            JMS you could set the JMSReplyTo property in these properties.
	 * 
	 * @return populated return object, or null if not found
	 * @throws MuleException
	 *             exception sending event
	 * 
	 */
	public Object sendSyncEvent(String url, Object payload,
			Map<String, Object> messageProperties) throws Exception {

		return sendASyncEvent(url, payload, messageProperties, 0);

	}

	/**
	 * Helper method to send a synchronous event to the ESB abstracting the
	 * details of the Mule implementation.
	 * 
	 * @param url
	 *            the Mule used to determine the destination and transport of
	 *            the message
	 * @param payload
	 *            the object that is the payload of the event
	 * @param messageProperties
	 *            properties to be associated with the payload. In the case of
	 *            JMS you could set the JMSReplyTo property in these properties.
	 * @param timeout
	 *            how long to block in milliseconds waiting for a result
	 * 
	 * @return populated return object, or null if not found
	 * @throws MuleException
	 *             exception sending event
	 * 
	 */
	public Object sendSyncEvent(String url, Object payload,
			Map<String, Object> messageProperties, int timeout)
			throws MuleException {

		return getMuleClient().send(url, payload, messageProperties, timeout)
				.getPayload();

	}
	
	/**
	 * Helper method to send a ASynchronous event to the ESB abstracting the
	 * details of the Mule implementation.
	 * 
	 * @param url
	 *            the Mule used to determine the destination and transport of
	 *            the message
	 * @param payload
	 *            the object that is the payload of the event
	 * @param messageProperties
	 *            properties to be associated with the payload. In the case of
	 *            JMS you could set the JMSReplyTo property in these properties.
	 * @param timeout
	 *            how long to block in milliseconds waiting for a result
	 * 
	 * @return populated return object, or null if not found
	 * @throws MuleException
	 *             exception sending event
	 * 
	 */
	
	public Object sendASyncEvent(String url, Object payload,
			Map<String, Object> messageProperties, int timeout)
			throws Exception {
		return getMuleClient().sendAsync(url, payload, messageProperties, timeout).getMessage().getPayload();

	}

}


