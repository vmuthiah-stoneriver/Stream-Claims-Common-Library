package com.client.iip.integration.financials.disbursement.external.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.fiserv.isd.iip.bc.financials.batch.FinancialsBatchConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementAddressDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementBankAccountSummaryDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementBankingInfoDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementBatchService;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementConstants;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementPartyDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.DisbursementService;
import com.fiserv.isd.iip.bc.financials.disbursement.external.DisbursementPartyRoleVendorData;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentHeaderDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentMarkerInterface;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentPayeeEFTDTO;
import com.fiserv.isd.iip.bc.financials.disbursement.external.PaymentProcessor;
import com.fiserv.isd.iip.bc.financials.disbursement.external.processor.BasePaymentProcessor;
import com.fiserv.isd.iip.bc.party.PartyDAO;
import com.fiserv.isd.iip.bc.party.PartyService;
import com.fiserv.isd.iip.bc.party.api.PartyContextCriteria;
import com.fiserv.isd.iip.bc.party.api.PartyInteractionChannelDataDTO;
import com.fiserv.isd.iip.bc.party.bo.PartyBankAccountBO;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.reserve.ClaimReserveConstants;
import com.stoneriver.iip.entconfig.date.BusinessDateType;
import com.stoneriver.iip.entconfig.date.DateService;

/**
 * Helper class to export payment to external system.
 * 
 * @author Mumtaz Ali
 */
@Pojo(id = "financials.disbursement.pojo.clientPaymentPayeeEFTProcessorPojo")
public class ClientPaymentPayeeEFTProcessor extends BasePaymentProcessor implements PaymentProcessor {
	
	private PartyDAO partyDAO;
	
	/**
	 * Gets PartyDAO.
	 * 
	 * @return PartyDAO
	 */
	public PartyDAO getPartyDAO() {
		return partyDAO;
	}

	/**
	 * Sets PartyDAO.
	 * 
	 * @param daoValue
	 *            PartyDAO
	 */
	@Inject(DaoInterface = "party.daointerface.partyDao")
	public void setPartyDAO(PartyDAO _partyDAO) {
		this.partyDAO = _partyDAO;
	}	
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
	public PaymentPayeeEFTDTO populateStagingData(DisbursementDTO dtoToProcess, PaymentMarkerInterface parentDto) {
		DisbursementPartyRoleVendorData partyRoleVendorData = null;
		DisbursementPartyDTO disbursementPartyDTO = dtoToProcess.getDisbursementParty();
		DisbursementAddressDTO disbursementAddressDTO = dtoToProcess.getDisbursementAddress();
		DisbursementBankingInfoDTO disbursementBankingInfoDTO = dtoToProcess.getDisbursementBankingInfo();
		/*
		 * AP Export Performance @GR - 03/31/2014 - Calling retrieveMinimalParty() here is causing heavy
		 * dirty flush on hibernate significantly slowing down performance. Commenting this since we hardly
		 * use Fax and Email for Payee.
		 */
		/*
		 * Use MinimalPartyPojo to retrieve all other data Note: context is for
		 * getting address by context, we won't be using the address data anyway
		 *
		MinimalPartyRequestDTO minimalPartyRequestDTO = new MinimalPartyRequestDTO();
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
		PaymentPayeeEFTDTO paymentPayeeEFTDTO = new PaymentPayeeEFTDTO();
		paymentPayeeEFTDTO.setRecordTypeId(new BigDecimal(220));
		if (disbursementPartyDTO != null) {
			paymentPayeeEFTDTO.setPrimaryPayeeName(disbursementPartyDTO.getPartyPayeePrimaryNameDerived());
			paymentPayeeEFTDTO.setSecondaryPayeeName(disbursementPartyDTO.getPartyPayeeSecondaryNameDerived());
			paymentPayeeEFTDTO.setInCareOfPayeeName(disbursementPartyDTO.getPartyIdInCareOfNameDerived());
			paymentPayeeEFTDTO.setPrimaryPayeePartyId(disbursementPartyDTO.getPartyIdPayeePrimary());
			paymentPayeeEFTDTO.setPartyPayOrdOfNameDerived(disbursementPartyDTO.getPartyPayOrdOfNameDerived());			
		}
		if (disbursementAddressDTO != null) {
			paymentPayeeEFTDTO.setAddressLine1(disbursementAddressDTO.getAddressLine1());
			paymentPayeeEFTDTO.setAddressLine2(disbursementAddressDTO.getAddressLine2());
			paymentPayeeEFTDTO.setCity(disbursementAddressDTO.getCity());
			paymentPayeeEFTDTO.setState(disbursementAddressDTO.getPrimarySubDivision());

			paymentPayeeEFTDTO.setAddressZip(disbursementAddressDTO.getPostalCode());
			paymentPayeeEFTDTO.setCountry(disbursementAddressDTO.getCountry());
		}
		//Set Email and Fax Number
		PartyContextCriteria criteria = new PartyContextCriteria();
		criteria.setPartyId(disbursementPartyDTO.getPartyIdPayeePrimary());
		criteria.setContextTypeCode("party");
		PartyInteractionChannelDataDTO channel = MuleServiceFactory.getService(PartyService.class).retrievePartyInteractionChannelForContext(criteria);
		if (channel != null) {
			paymentPayeeEFTDTO.setEmail(channel.getBusinessEmail()==null?channel.getPersonalEmail():channel.getBusinessEmail());
			paymentPayeeEFTDTO.setFaxNumber(channel.getFax());
		}
		paymentPayeeEFTDTO.setCresPreference(DisbursementConstants.PAY_PAYEE_EFT);
		if (disbursementBankingInfoDTO != null){
			//payee
			paymentPayeeEFTDTO.setPayeeRoutingNumber(disbursementBankingInfoDTO.getFinancialInstitutionRouteNoPayee());
			paymentPayeeEFTDTO.setPayeeAccountNumber(disbursementBankingInfoDTO.getPartyBankAccountNoPayee());
			paymentPayeeEFTDTO.setPayeeAccountType(disbursementBankingInfoDTO.getBankAccountTypeCodePayee());
			//payor
			paymentPayeeEFTDTO
					.setPayorRoutingNumber(disbursementBankingInfoDTO.getFinancialInstitutionRouteNoCompany());
			paymentPayeeEFTDTO.setPayorAccountType(disbursementBankingInfoDTO.getBankAccountTypeCodeCompany());
			paymentPayeeEFTDTO.setPayorAccountNumber(disbursementBankingInfoDTO.getPartyBankAccountNoCompany());
		}else{
			//payee
			Collection<PartyBankAccountBO> accounts = getPartyDAO().retrieveBankAccountsForParty(disbursementPartyDTO.getPartyIdPayeePrimary());
			if(accounts != null && !accounts.isEmpty()){
				PartyBankAccountBO bankAccount = accounts.iterator().next();
				paymentPayeeEFTDTO.setPayeeRoutingNumber(bankAccount.getRoutingNumber());
				paymentPayeeEFTDTO.setPayeeAccountNumber(bankAccount.getBankAccountNumber());
				paymentPayeeEFTDTO.setPayeeAccountType(bankAccount.getBankAccountTypeCode());
			}
			//payor
			Date businessDate = MuleServiceFactory.getService(DateService.class).getBusinessDate(new Long(-1),
					BusinessDateType.BUSINESS);			
			DisbursementBankAccountSummaryDTO payorBankAccountSummaryDTO = retrieveDisbursementPayorBankAccountDetails(dtoToProcess.getCompanyId(),businessDate);
			if(payorBankAccountSummaryDTO != null){
				paymentPayeeEFTDTO.setPayorRoutingNumber(payorBankAccountSummaryDTO.getRoutingNumber());
				paymentPayeeEFTDTO.setPayorAccountType(payorBankAccountSummaryDTO.getBankAccountTypeCode());
				paymentPayeeEFTDTO.setPayorAccountNumber(payorBankAccountSummaryDTO.getBankAccountNumber());
			}			
		}
		java.util.Collection<DisbursementPartyRoleVendorData> partyRoleVendors = MuleServiceFactory.getService(
				DisbursementService.class).retrieveDisbursementPartyRoleVendorInfo(
				dtoToProcess.getDisbursementParty().getPartyIdPayeePrimary());
		if (partyRoleVendors != null && !partyRoleVendors.isEmpty()) {
			partyRoleVendorData = partyRoleVendors.iterator().next();
			paymentPayeeEFTDTO.setVendorAccountsPayableTypeCode(partyRoleVendorData.getVendorTypeCode());
			paymentPayeeEFTDTO.setVendorAccountsPayableRefNo(partyRoleVendorData.getVendorNumber());
		}
		// Set Payee EFT within parent (I.e. Payment header)
		setPayeeEFTInParent(parentDto, paymentPayeeEFTDTO);
		return paymentPayeeEFTDTO;
	}
	
	/**
	 * Method to retrieve payor's (company) bank account.
	 * @param payorCompanyId Long
	 * @param businessDate business date
	 * @return payorBankAccountSummaryDTO DisbursementBankAccountSummaryDTO
	 * 
	 */
	private DisbursementBankAccountSummaryDTO retrieveDisbursementPayorBankAccountDetails(Long payorCompanyId,Date businessDate) {
		
		DisbursementBankAccountSummaryDTO payorBankAccountSummaryDTO = null;
		if(payorCompanyId != null) {

				payorBankAccountSummaryDTO = MuleServiceFactory.getService(DisbursementBatchService.class).
						retrievePayorPrimaryDisbursableUsageBankAccountDetails(
						payorCompanyId, ClaimReserveConstants.CLAIM_DISBURSEMENT_TYPE_CODE,
						FinancialsBatchConstants.FINANCIAL_USAGE_CODE_DISBURSEMENTS, businessDate);
			
		}
		
		return payorBankAccountSummaryDTO;
	}	

	/**
	 * Set Payee EFT within parent (I.e. Payment header).
	 * 
	 * @param parentDto
	 *            Input parent DTO.
	 * @param paymentPayeeEFTDTO
	 *            Input payee EFT DTO.
	 */
	private void setPayeeEFTInParent(PaymentMarkerInterface parentDto, PaymentPayeeEFTDTO paymentPayeeEFTDTO) {
		// Set payment header within parent (I.e. File header)
		if (parentDto instanceof PaymentHeaderDTO) {
			java.util.Collection<PaymentPayeeEFTDTO> payeeEFT = ((PaymentHeaderDTO) parentDto).getPaymentPayeeEFT();
			if (payeeEFT == null) {
				payeeEFT = new ArrayList<PaymentPayeeEFTDTO>();
			}
			payeeEFT.add(paymentPayeeEFTDTO);
			((PaymentHeaderDTO) parentDto).setPaymentPayeeEFT(payeeEFT);
		}
	}

}
