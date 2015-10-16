package com.client.iip.integration.core.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.Status;

import org.apache.commons.collections.CollectionUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.claim.imports.ClaimImportCompositeDTO;
import com.client.iip.integration.core.converter.CustomReflectionConverter;
import com.client.iip.integration.core.converter.IIPXStreamDateConverter;
import com.client.iip.integration.core.converter.ProfileAttributeConverter;
import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.client.iip.integration.core.util.IIPXStream;
import com.client.iip.integration.core.util.MessageTracker;
import com.client.iip.integration.party.ClientPartySearchResult;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.thread.IIPDataContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContext;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.fiserv.isd.iip.core.tx.IIPTransactionUtil;
import com.fiserv.isd.iip.core.validation.IIPObjectError;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

public class IIPObject2XMLTransformer extends AbstractMessageTransformer {

	private final Logger logger = LoggerFactory.getLogger(IIPObject2XMLTransformer.class);
	
	private List<String> aliasFileList;

	private IIPXStream xstream = null;
	
	public IIPObject2XMLTransformer() {

	}

	private void init() {

		// Convert Object to XML based on the alias Mapping file
		try {

			if (CollectionUtils.isEmpty(aliasFileList)) {
				xstream = new IIPXStream();
			} else {
				xstream = new IIPXStream(aliasFileList);
			}
			
			
			//Process annotations for ClientPartySearchResult
			xstream.processAnnotations(ClientPartySearchResult.class);
			//Process annotations for ClaimImportCompositeDTO
			xstream.processAnnotations(ClaimImportCompositeDTO.class);
			xstream.registerConverter(new ProfileAttributeConverter());
			xstream.registerConverter(new CustomReflectionConverter(xstream.getMapper(),new PureJavaReflectionProvider()));

	
			//Process Dates
			xstream.registerConverter(new IIPXStreamDateConverter());
			xstream.addDefaultImplementation(java.sql.Date.class, java.util.Date.class);
			xstream.addDefaultImplementation(java.sql.Timestamp.class, java.util.Date.class);
			xstream.addDefaultImplementation(java.sql.Time.class, java.util.Date.class);
			xstream.addDefaultImplementation(java.util.ArrayList.class, java.util.List.class);
			xstream.addDefaultImplementation(java.util.HashMap.class, java.util.Map.class);
			xstream.addDefaultImplementation(com.fiserv.isd.iip.bc.address.api.PartyAddressDTO.class, 
					com.fiserv.isd.iip.bc.address.api.AddressDTO.class);			

		} catch (Exception ex) {
			logger.error("{} caught initializing IIPObject2XMLTransformer. exception - {}", ex.getClass().getName(), ex);
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

		Object payload = null;
		try {
	        IIPThreadContext threadCtx = IIPThreadContextFactory.getIIPThreadContext();
	        IIPDataContext context = threadCtx.getDataContext();		
			String requestFromStream = (context == null || context.getAppData("ExternalInterfaceRequest")==null)?"false":(String)context.getAppData("ExternalInterfaceRequest");				
			payload = message.getPayload();
			if (payload.toString().startsWith("{NullPayload}")) {
				rollbackTransaction(payload);
				payload = generateExceptionPayload("payloadError", "ERROR", 
						"Required data missing from request.  Please review request and view log files for additional information.");
			} else if(payload instanceof ArrayList) {
				//If the instance of Payload is IIPErrorResponse then rollback the transaction.
				List lstPayload = (ArrayList) payload;
				if(lstPayload.size() > 0) {
					Object errorObject = lstPayload.get(0);
					if(errorObject instanceof IIPErrorResponse) {
						rollbackTransaction(payload);					
					}
				}
			}
			
			/* We find for the new releases the following situations exist for the DTO
			 * 1. Either new attributes are inserted in between
			 * 2. New attributes gets added in the end.
			 * 3. Existing attributes are removed and renamed.
			 * 4. Original Sequence remain the same 
			 * Best Trade off solution : Reflect Object Type and Mark the new Attribute Transient so they don't get marshalled out.
			 * This will make sure the object confirms to the existing xsd. We will add it later if it is required.
			 * This is a tradeoff considering the performance hit with deep reflection and merge with JAXB Binding objects.
			 * We will use the existing API's for XSD lookup to find the new attributes added in the release.
			 */

			String xmlString = xstream.convert2XML(payload);
			
			//Set the xmlString Payload into request parameter if the flag is set to true
			String payloadParamter = message.getProperty("sendPayloadAsParameter", PropertyScope.SESSION);
			
			boolean isPayloadParameter = (payloadParamter == null 
										|| (payloadParamter != null && !payloadParamter.equals("true")))?false:true;
			
			if(isPayloadParameter){
				message.setProperty("payload", xmlString, PropertyScope.OUTBOUND);
			}
			
			if(MessageTracker.isEnabled()){
				String commType = requestFromStream.equals("true")?"Request":"Response";
				String transTime = "Transaction Time: " + DateUtility.getSystemDateTime();
				String payloadData = commType + " Payload : "+ xmlString;
				MessageTracker.write(transTime);
				MessageTracker.write(payloadData);
				logger.info("Request Written to File");
			}			
			logger.info("IIPObject2XMLTransformer payload {} ", xmlString);
			return xmlString;
		} catch (Throwable ex) {
			logger.error("Exception occured while converting to XML: ", ex);
			payload = generateExceptionPayload("Object2XMLXStreamfailure", "ERROR", 
					ex.toString());
			rollbackTransaction(payload);
			return payload;
		}
	}
	
	/**
	 * Generate Error Response Collection so that the output could be in a formatted manner 
	 * @param errorCode
	 * @param severityCode
	 * @param formattedMessage
	 * @return Collection of IIP Error Response
	 */
	private Collection<IIPErrorResponse> generateExceptionPayload(String errorCode, 
			String severityCode, String formattedMessage) {
		Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
		IIPErrorResponse thisError = new IIPErrorResponse();
		Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
		String[] codes = new String[] {errorCode};
		IIPObjectError ioe = new IIPObjectError("IIPObject2XMLTransformer", "generateExceptionPayload",
				null, codes, null, severityCode);
		ioe.setFormattedMessage(formattedMessage);
		ioes.add(ioe);
		thisError.setFormattedErrors(ioes);
		errResponse.add(thisError);
		return errResponse;
	}

	/**
	 * If there is an exception in the integration layer, the transaction will not be rolled back.
	 * Explicitly calling Rollback so that any open transactions can be rolled back.
	 * @param payload
	 */
	private void rollbackTransaction(Object payload) {
		//If there is an exception in the integration layer, the transaction will not be rolled back.
		//Explicitly calling Rollback so that any open transactions can be rolled back.
		try {
			if(IIPTransactionUtil.getTransactionManager() != null 
					&& IIPTransactionUtil.getTransactionManager().getTransaction() != null
					&& (IIPTransactionUtil.getTransactionManager().getTransaction().getStatus() == Status.STATUS_ACTIVE
					|| IIPTransactionUtil.getTransactionManager().getTransaction().getStatus() == Status.STATUS_COMMITTING)) {
				IIPTransactionUtil.getTransactionManager().getTransaction().setRollbackOnly();
			}
		} catch (Throwable ex) {
			logger.error("Exception occured while converting to XML: ", ex);
			payload = generateExceptionPayload("Object2XMLXStreamfailure", "ERROR", 
					ex.toString());
		}
	}

}
