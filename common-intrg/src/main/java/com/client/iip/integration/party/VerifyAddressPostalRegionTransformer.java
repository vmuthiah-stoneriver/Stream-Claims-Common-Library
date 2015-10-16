package com.client.iip.integration.party;

import java.util.ArrayList;
import java.util.Collection;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.fiserv.isd.iip.bc.address.PartyAddressService;
import com.fiserv.isd.iip.bc.address.api.AddressDTO;
import com.fiserv.isd.iip.bc.address.api.VerifyAddressDTO;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

/**
 * Postal Service Region Transformer to process postal region details in party address.
 * 
 * @author sudharsan.sriram
 *
 */
public class VerifyAddressPostalRegionTransformer extends AbstractMessageTransformer {

	private Logger logger = LoggerFactory.getLogger(VerifyAddressPostalRegionTransformer.class);
	
	public static final String INTERFACE_PROPERTY = "interface";

	private PartyAddressService partyAddressService;

	/**
	 * Transform party details.
	 * 
	 * @param message MuleMessage - payload
	 * @param outputEncoding String
	 * @return Object payload
	 * @throws TransformerException
	 */
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		Object payload = message.getPayload();

		try{
			if(payload instanceof Collection){
				processVerifyAddressDetails((Collection)payload);
			}
		}
		catch (Exception ex){
			logger.error("VerifyAddressPostalRegionTransformer exception: ", ex);
			Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
			IIPErrorResponse thisError = new IIPErrorResponse();
			Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
			String[] codes = new String[] {"PartyAddressTransformationfailure"};
			IIPObjectError ioe = new IIPObjectError("VerifyAddressPostalRegionTransformer", "transformMessage",
					null, codes, null, MessageConstants.SEVERITY_ERROR);
			ioe.setFormattedMessage(ex.toString());
			ioes.add(ioe);
			thisError.setFormattedErrors(ioes);
			errResponse.add(thisError);
			message.setPayload(errResponse);
			message.setExceptionPayload(null);
			message.setProperty(INTERFACE_PROPERTY, "INBNDERR" , PropertyScope.OUTBOUND);			
			return message;	
		}

		return message;
	}

	/**
	 * Process party address details.
	 * 
	 * @param partyDTO PartyDTO
	 */
	private void processVerifyAddressDetails(Collection addressList) throws Exception{
		
		if(addressList != null && !addressList.isEmpty()){
		for(Object address:addressList){
			if(address instanceof VerifyAddressDTO){
				if(((VerifyAddressDTO)address).getPostalServiceRegionId() == null){
				String postalSvcRegionCode = ((VerifyAddressDTO)address).getPostalSvcRegionCode();
				if(postalSvcRegionCode != null){
					//Retrieve postal service region id based on postal service region code
					Long postalServiceRegionId = 
						getPartyAddressService().retrievePostalServiceRegionIdByRegionAbrv(postalSvcRegionCode);
					if(postalServiceRegionId == null){
						throw new Exception("Invalid Postal Service Region Code: " + postalSvcRegionCode);
					}
					((VerifyAddressDTO)address).setPostalServiceRegionId(postalServiceRegionId);
				}else{
						throw new Exception("Postal Service Region Code is null");
				}
				
			  }
		   }else if(address instanceof AddressDTO){
				if(((AddressDTO)address).getPostalServiceRegionId() == null){
				String postalSvcRegionCode = ((AddressDTO)address).getPostalSvcRegionCode();
				if(postalSvcRegionCode != null){
					//Retrieve postal service region id based on postal service region code
					Long postalServiceRegionId = 
						getPartyAddressService().retrievePostalServiceRegionIdByRegionAbrv(postalSvcRegionCode);
					if(postalServiceRegionId == null){
						throw new Exception("Invalid Postal Service Region Code: " + postalSvcRegionCode);
					}
					((AddressDTO)address).setPostalServiceRegionId(postalServiceRegionId);
				}else{
						throw new Exception("Postal Service Region Code is null");
				}
				
			  }			   
		   }else{
			   throw new Exception("Invalid Response, Please check the Response XML");
		   }
			
		 }
		
	   }

	}

	/**
	 * @return PartyAddressService
	 */
	public PartyAddressService getPartyAddressService(){
		if(partyAddressService == null){
			partyAddressService = MuleServiceFactory.getService(PartyAddressService.class);
		}
		return partyAddressService;
	}

}
