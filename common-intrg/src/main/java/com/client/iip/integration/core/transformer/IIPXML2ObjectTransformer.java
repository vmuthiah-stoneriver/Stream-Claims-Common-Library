package com.client.iip.integration.core.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.converter.IIPXStreamDateTimestampConverter;
import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.client.iip.integration.core.util.IIPXStream;
import com.client.iip.integration.core.util.MessageTracker;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.IIPObjectError;
import com.thoughtworks.xstream.XStream;

public class IIPXML2ObjectTransformer extends AbstractMessageTransformer {

	private final Logger logger = LoggerFactory.getLogger(IIPXML2ObjectTransformer.class);

	public static final String INTERFACE_PROPERTY = "interface";
		
	public static final String WSDL_OPERATION = "operationName";
	
	private IIPXStream xstream = null;

	private List<String> aliasFileList;

	public IIPXML2ObjectTransformer() {

	}

	private void init() {

		// Convert XML 2 OBJECT based on the alias Mapping file
		try {

			if (CollectionUtils.isEmpty(aliasFileList)) {
				xstream = new IIPXStream();
			} else {
				xstream = new IIPXStream(aliasFileList);
			}

			//Set Default TimeZone
			//xstream.registerConverter(new DateConverter(
			//		IIPXStreamDateConverter.DEFAULT_DATETIMEONLY_FORMAT,
			//		IIPXStreamDateConverter.ACCEPTABLE_DATETIMEONLY_FORMATS));
			xstream.registerConverter(new IIPXStreamDateTimestampConverter(),
					XStream.PRIORITY_VERY_HIGH);
			xstream.addDefaultImplementation(java.util.ArrayList.class, java.util.Collection.class);
			xstream.addDefaultImplementation(java.util.HashMap.class, java.util.Map.class);

		} catch (Exception ex) {
			logger.error("{} caught initializing IIPXML2ObjectTransformer. exception - {}", ex.getClass().getName(), ex);			
		}
	}

	/**
	 * @return the aliasFileList
	 */
	public List<String> getAliasFileList() {
		return aliasFileList;
	}

	/**
	 * @param aliasFileList
	 *            the aliasFileList to set
	 */
	public void setAliasFileList(List<String> aliasFileList) {
		this.aliasFileList = aliasFileList;
		init();
	}

	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {

        IIPThreadContext threadCtx = IIPThreadContextFactory.getIIPThreadContext();
        IIPDataContext context = threadCtx.getDataContext();		
		String synchronousResponse = (context == null || context.getAppData("ExternalInterfaceRequest")==null)?"false":(String)context.getAppData("ExternalInterfaceRequest");		

		String payload = null;
		try {

			if (message.getPayload() != null
					&& message.getPayload() instanceof String) {
				payload = message.getPayloadAsString();
				logger.info("Payload : " + payload);
				if(MessageTracker.isEnabled()){
					String commType = synchronousResponse.equals("true")?"Response":"Request";
					String transTime = "Transaction Time: " + DateUtility.getSystemDateTime();
					String payloadData = commType + " Payload : "+ payload;
					MessageTracker.write(transTime);
					MessageTracker.write(payloadData);
				}				
				return xstream.convert2Object(payload);
			}

		} catch (Throwable ex) {
			if(synchronousResponse.equals("true")){
				IIPCoreSystemException coreEx = new IIPCoreSystemException("Exception occured while processing response: " + ex.toString());
				message.setPayload(coreEx);
			}else{
			logger.error("Exception occured while converting to Object: ", ex);
			Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
			IIPErrorResponse thisError = new IIPErrorResponse();
			Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
			String[] codes = new String[] {"XML2ObjectXStreamfailure"};
			IIPObjectError ioe = new IIPObjectError("IIPXML2ObjectTransformer", "transformMessage",
					null, codes, null, MessageConstants.SEVERITY_ERROR);
			ioe.setFormattedMessage(ex.toString());
			ioes.add(ioe);
			thisError.setFormattedErrors(ioes);
			errResponse.add(thisError);
			message.setPayload(errResponse);
			}
			message.setExceptionPayload(null);
			message.setProperty(INTERFACE_PROPERTY, "INBNDERR",
					PropertyScope.OUTBOUND);
			
			return message;
		}

		return message;

	}

}
