package com.client.iip.integration.financials.disbursement.external.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementAddressDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementPartyDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementService;
import com.fiserv.isd.iip.bc.financials.disbursement.external.DisbursementPartyRoleVendorData;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentHeaderDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentMarkerInterface;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentPayeeCheckDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentProcessor;
import com.fiserv.isd.iip.bc.financials.disbursement.external.processor.BasePaymentProcessor;
import com.fiserv.isd.iip.bc.party.PartyService;
import com.fiserv.isd.iip.bc.party.api.PartyContextCriteria;
import com.fiserv.isd.iip.bc.party.api.PartyInteractionChannelDataDTO;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;

/**
 * Helper class to export payment to external system.
 * 
 * @author Mumtaz Ali
 */
@Pojo(id = "financials.disbursement.pojo.clientPaymentPayeeCheckProcessorPojo")
public class ClientPaymentPayeeCheckProcessor extends BasePaymentProcessor implements PaymentProcessor {
	/**
	 * Method to process payment for context billing.
	 * 
	 * @param dtoToProcess
	 *            Input DisbursementDTO
	 * @param parentDto
	 *            Input Parent Object where data should be populated and added.
	 * @return Processed object.
	 */
	@Override
	public PaymentPayeeCheckDTO populateStagingData(DisbursementDTO dtoToProcess, PaymentMarkerInterface parentDto) {

		DisbursementPartyRoleVendorData partyRoleVendorData = null;

		DisbursementPartyDTO disbursementPartyDTO = dtoToProcess.getDisbursementParty();
		DisbursementAddressDTO disbursementAddressDTO = dtoToProcess.getDisbursementAddress();
		/*
		 * AP Export Performance @GR - 03/31/2014 - Calling retrieveMinimalParty() here is causing heavy
		 * dirty flush on hibernate significantly slowing down performance. Commenting this since we hardly
		 * use Fax and Email for Payee.
		 */
		/*
		 * Use MinimalPartyPojo to retrieve all other data Note: context is for
		 * getting address by context, we won't be using the address data anyway
		 *
		/*MinimalPartyRequestDTO minimalPartyRequestDTO = new MinimalPartyRequestDTO();
		minimalPartyRequestDTO.setPartyId(disbursementPartyDTO.getPartyIdPayeePrimary());
		/*
		 * Need to set something, it's only used for fetching address, which we
		 * don't care about
		 *
		minimalPartyRequestDTO.setContextType("");
		MinimalPartyDTO minParty = MuleServiceFactory.getService(PartyService.class).retrieveMinimalParty(
				minimalPartyRequestDTO);
		PhoneChannelDTO phoneChannelDTO = null;
		AddressChannelDTO emailAddress = null;
		if (minParty != null) {
			MinimalPartyContactDTO minimalPartyContactDTO = minParty.getContact();
			if (minimalPartyContactDTO != null) {
				phoneChannelDTO = minimalPartyContactDTO.getFaxNumber();
				emailAddress = minimalPartyContactDTO.getEmailAddress();
			}
		}*/
		PaymentPayeeCheckDTO paymentPayeeCheckDTO = new PaymentPayeeCheckDTO();
		paymentPayeeCheckDTO.setRecordTypeId(new BigDecimal(210));
		if (disbursementPartyDTO != null) {
			paymentPayeeCheckDTO.setPrimaryPayeeName(disbursementPartyDTO.getPartyPayeePrimaryNameDerived());
			paymentPayeeCheckDTO.setSecondaryPayeeName(disbursementPartyDTO.getPartyPayeeSecondaryNameDerived());
			paymentPayeeCheckDTO.setInCareOfPayeeName(disbursementPartyDTO.getPartyIdInCareOfNameDerived());
			paymentPayeeCheckDTO.setPrimaryPayeePartyId(disbursementPartyDTO.getPartyIdPayeePrimary());
			paymentPayeeCheckDTO.setPartyPayOrdOfNameDerived(disbursementPartyDTO.getPartyPayOrdOfNameDerived());
		}
		if (disbursementAddressDTO != null) {
			paymentPayeeCheckDTO.setAddressLine1(disbursementAddressDTO.getAddressLine1());
			paymentPayeeCheckDTO.setAddressLine2(disbursementAddressDTO.getAddressLine2());
			paymentPayeeCheckDTO.setCity(disbursementAddressDTO.getCity());
			paymentPayeeCheckDTO.setState(disbursementAddressDTO.getPrimarySubDivision());

			paymentPayeeCheckDTO.setAddressZip(disbursementAddressDTO.getPostalCode());
			paymentPayeeCheckDTO.setCountry(disbursementAddressDTO.getCountry());
		}
		//Set Email and Fax Number
		PartyContextCriteria criteria = new PartyContextCriteria();
		criteria.setPartyId(disbursementPartyDTO.getPartyIdPayeePrimary());
		criteria.setContextTypeCode("party");
		PartyInteractionChannelDataDTO channel = MuleServiceFactory.getService(PartyService.class).retrievePartyInteractionChannelForContext(criteria);
		if (channel != null) {
			paymentPayeeCheckDTO.setEmail(channel.getBusinessEmail()==null?channel.getPersonalEmail():channel.getBusinessEmail());
			paymentPayeeCheckDTO.setFaxNumber(channel.getFax());
		}
		paymentPayeeCheckDTO.setCresPreference(DisbursementConstants.PAY_PAYEE_CHECK);
		java.util.Collection<DisbursementPartyRoleVendorData> partyRoleVendors = MuleServiceFactory.getService(
				DisbursementService.class).retrieveDisbursementPartyRoleVendorInfo(
				dtoToProcess.getDisbursementParty().getPartyIdPayeePrimary());
		if (partyRoleVendors != null && !partyRoleVendors.isEmpty()) {
			partyRoleVendorData = partyRoleVendors.iterator().next();
			paymentPayeeCheckDTO.setVendorAccountsPayableTypeCode(partyRoleVendorData.getVendorTypeCode());
			paymentPayeeCheckDTO.setVendorAccountsPayableRefNo(partyRoleVendorData.getVendorNumber());
		}
		// Set Payee check within parent (I.e. Payment header)
		setPayeeCheckInParent(parentDto, paymentPayeeCheckDTO);
		return paymentPayeeCheckDTO;
	}

	/**
	 * Set Payee check within parent (I.e. Payment header).
	 * 
	 * @param parentDto
	 *            Input parent DTO.
	 * @param paymentPayeeCheckDTO
	 *            Input payee check DTO.
	 */
	private void setPayeeCheckInParent(PaymentMarkerInterface parentDto, PaymentPayeeCheckDTO paymentPayeeCheckDTO) {
		// Set payment header within parent (I.e. File header)
		if (parentDto instanceof PaymentHeaderDTO) {
			Collection<PaymentPayeeCheckDTO> payeeCheck = ((PaymentHeaderDTO) parentDto).getPaymentPayeeCheck();
			if (payeeCheck == null) {
				payeeCheck = new ArrayList<PaymentPayeeCheckDTO>();
			}
			payeeCheck.add(paymentPayeeCheckDTO);
			((PaymentHeaderDTO) parentDto).setPaymentPayeeCheck(payeeCheck);
		}
	}


}
