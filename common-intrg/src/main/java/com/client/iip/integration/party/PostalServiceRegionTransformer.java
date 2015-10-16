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
import com.fiserv.isd.iip.bc.address.api.AddressCollectionDTO;
import com.fiserv.isd.iip.bc.address.api.AddressDTO;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

/**
 * Postal Service Region Transformer to process postal region details in party address.
 * 
 * @author sudharsan.sriram
 *
 */
public class PostalServiceRegionTransformer extends AbstractMessageTransformer {

	private Logger logger = LoggerFactory.getLogger(PostalServiceRegionTransformer.class);
	
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
			if(payload instanceof PartyDTO){
				PartyDTO partyDTO = (PartyDTO) payload;
				processPartyAddressDetails(partyDTO);
			}
		}
		catch (Exception ex){
			logger.error("PostalServiceRegionTransformer exception: ", ex);
			Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
			IIPErrorResponse thisError = new IIPErrorResponse();
			Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
			String[] codes = new String[] {"PartyAddressTransformationfailure"};
			IIPObjectError ioe = new IIPObjectError("PostalServiceRegionTransformer", "transformMessage",
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
	private void processPartyAddressDetails(PartyDTO partyDTO) throws Exception{

		AddressCollectionDTO addressCollectionDTO = partyDTO.getAddressCollection();

		//Check whether addressCollection is populated
		if(addressCollectionDTO != null){
			processPostalRegionDetails(addressCollectionDTO);
		}
	}

	/**
	 * Process Postal region details in address.
	 * 
	 * @param addressCollectionDTO AddressCollectionDTO
	 */
	private void processPostalRegionDetails(AddressCollectionDTO addressCollectionDTO) throws Exception{

		Collection<AddressDTO> addresses = addressCollectionDTO.getAddresses();

		//Check whether addresses are populated
		if(addresses != null && !addresses.isEmpty()){
			for (AddressDTO addressDTO : addresses) {
				//Check whether postal service region id is unavailable
				if(addressDTO.getPostalServiceRegionId() == null){
					String postalSvcRegionCode = addressDTO.getPostalSvcRegionCode();
					if(postalSvcRegionCode != null){
						//Retrieve postal service region id based on postal service region code
						Long postalServiceRegionId = 
							getPartyAddressService().retrievePostalServiceRegionIdByRegionAbrv(postalSvcRegionCode);
						if(postalServiceRegionId == null){
							throw new Exception("Invalid Postal Service Region Code: " + postalSvcRegionCode);
						}
						addressDTO.setPostalServiceRegionId(postalServiceRegionId);
					}else{
							throw new Exception("Postal Service Region Code is null");
					}
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
