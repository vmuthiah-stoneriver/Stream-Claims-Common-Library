package com.client.iip.integration.claim.imports;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.client.iip.integration.sa.ClientEnterpriseConfigDAO;
import com.fiserv.isd.iip.bc.party.PartyService;
import com.fiserv.isd.iip.bc.party.TaxStatusIndicator;
import com.fiserv.isd.iip.bc.party.api.MinimalPartyDTO;
import com.fiserv.isd.iip.bc.party.api.MinimalPartyRequestDTO;
import com.fiserv.isd.iip.core.dataobject.EntityData;
import com.fiserv.isd.iip.core.dataobject.ExternalSourceIdentifiable;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.dto.DataTransferObject;
import com.fiserv.isd.iip.core.dto.search.SearchResultsDTO;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.bound.BoundValidationException;
import com.fiserv.isd.iip.util.bean.ClassUtils;
import com.fiserv.isd.iip.util.bean.DataMergeException;
import com.fiserv.isd.iip.util.bean.ExternalInterfaceBeanMerge;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.fiserv.isd.iip.util.string.StringUtility;
import com.stoneriver.iip.casetool.api.CaseDTO;
import com.stoneriver.iip.claims.CALClaimParticipationDTO;
import com.stoneriver.iip.claims.CWSClaimService;
import com.stoneriver.iip.claims.CWSNoticeOfLossService;
import com.stoneriver.iip.claims.ClaimDTO;
import com.stoneriver.iip.claims.ClaimIdentifiable;
import com.stoneriver.iip.claims.ClaimImportProfileStatementDTO;
import com.stoneriver.iip.claims.ClaimInformationRequestDTO;
import com.stoneriver.iip.claims.ClaimInitiateDTO;
import com.stoneriver.iip.claims.ClaimsAllParticipationConstants;
import com.stoneriver.iip.claims.ClaimsProfileStatementDTO;
import com.stoneriver.iip.claims.ClaimsProfileStatementResponseBO;
import com.stoneriver.iip.claims.ClaimsServiceConstants;
import com.stoneriver.iip.claims.RelatedClaimBO;
import com.stoneriver.iip.claims.RelatedClaimDTO;
import com.stoneriver.iip.claims.RelatedClaimService;
import com.stoneriver.iip.claims.authority.ClaimParticipationAuthorityDTO;
import com.stoneriver.iip.claims.composite.ClaimServicesCompositeService;
import com.stoneriver.iip.claims.composite.witness.ClaimWitnessCompositeService;
import com.stoneriver.iip.claims.cws.CALClaimParticipationBO;
import com.stoneriver.iip.claims.cws.CWSClaimConstants;
import com.stoneriver.iip.claims.cws.unit.ClaimUnitBO;
import com.stoneriver.iip.claims.cws.unit.ClaimUnitConstants;
import com.stoneriver.iip.claims.dao.ClaimUnitDAO;
import com.stoneriver.iip.claims.details.ClaimDetailsCompositeDTO;
import com.stoneriver.iip.claims.financial.ClaimFinancialsService;
import com.stoneriver.iip.claims.financial.ClaimMiscTransactionDTO;
import com.stoneriver.iip.claims.financial.ImportUtilPojo;
import com.stoneriver.iip.claims.imports.ClaimImportException;
import com.stoneriver.iip.claims.imports.financial.util.ClaimUnitParticipantCommonDTO;
import com.stoneriver.iip.claims.imports.financials.ClaimFinancialsImportException;
import com.stoneriver.iip.claims.imports.recovery.ClaimRecovery;
import com.stoneriver.iip.claims.mantain.MaintainLossIndicatorService;
import com.stoneriver.iip.claims.participation.ClaimParticipationRequestDTO;
import com.stoneriver.iip.claims.payment.ClaimPayable;
import com.stoneriver.iip.claims.payment.ClaimPayment;
import com.stoneriver.iip.claims.payment.ClaimReservePaymentService;
import com.stoneriver.iip.claims.policy.AllClaimsPolicyService;
import com.stoneriver.iip.claims.policy.ClaimPolicyConstants;
import com.stoneriver.iip.claims.policy.ClaimPolicyDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyParticipationDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyUnitDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyUnitVehicleDTO;
import com.stoneriver.iip.claims.policy.PolicyUnitSearchCriteriaDTO;
import com.stoneriver.iip.claims.recovery.ClaimReserveRecoveryService;
import com.stoneriver.iip.claims.release.ClaimReleaseService;
import com.stoneriver.iip.claims.reserve.CALClaimReserveDTO;
import com.stoneriver.iip.claims.reserve.CalClaimReserveCompositeDTO;
import com.stoneriver.iip.claims.reserve.CalClaimReserveStatusCompositeDTO;
import com.stoneriver.iip.claims.reserve.CalClaimReserveStatusDTO;
import com.stoneriver.iip.claims.reserve.CalClaimReserveTransactionDTO;
import com.stoneriver.iip.claims.reserve.ClaimReserveCommonDTO;
import com.stoneriver.iip.claims.reserve.ClaimReserveConstants;
import com.stoneriver.iip.claims.reserve.ClaimReserveService;
import com.stoneriver.iip.claims.reserve.ClaimUnitAndParticipantNestedResultData;
import com.stoneriver.iip.claims.reserve.ImpactIncurredCriteria;
import com.stoneriver.iip.claims.reserve.ImportReserveCriteria;
import com.stoneriver.iip.claims.search.ClaimEntitySearchResultDTO;
import com.stoneriver.iip.claims.search.ClaimSearchCriteriaDTO;
import com.stoneriver.iip.claims.search.ClaimSearchException;
import com.stoneriver.iip.claims.search.ClaimSearchResultDTO;
import com.stoneriver.iip.claims.status.AllClaimStatusService;
import com.stoneriver.iip.claims.status.ClaimStatusCompositeDTO;
import com.stoneriver.iip.claims.status.ClaimStatusConstants;
import com.stoneriver.iip.claims.status.ClaimStatusDTO;
import com.stoneriver.iip.claims.unit.AllClaimUnitService;
import com.stoneriver.iip.claims.unit.CWSClaimParticipationHelper;
import com.stoneriver.iip.claims.unit.ClaimDamageServicesDTO;
import com.stoneriver.iip.claims.unit.ClaimServicesCompositeDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitSummaryDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitVehicleDTO;
import com.stoneriver.iip.claims.unit.SaveClaimUnitRequestDTO;
import com.stoneriver.iip.claims.witness.CWSClaimWitnessService;
import com.stoneriver.iip.claims.witness.ClaimWitnessDTO;
import com.stoneriver.iip.entconfig.api.CompanyLOBInfo;
import com.stoneriver.iip.entconfig.api.CompanyOrganizationUnitDTO;
import com.stoneriver.iip.entconfig.api.EnterpriseConfigCodeLookupCollectionService;
import com.stoneriver.iip.entconfig.api.EnterpriseConfigService;
import com.stoneriver.iip.entconfig.api.ProductDefinitionInfo;
import com.stoneriver.iip.entconfig.base.tool.AgreementDataDTO;
import com.stoneriver.iip.entconfig.base.tool.AgreementDataValueDTO;
import com.stoneriver.iip.entconfig.base.tool.BaseToolDTO;
import com.stoneriver.iip.entconfig.codelookup.api.LineOfBusinessCodeLookUpDTO;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementBaseDTO;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementItem;
import com.stoneriver.iip.entconfig.profilestatement.model.ProfileStatementSelection;
import com.stoneriver.iip.entconfig.sa.organizationunit.api.SysAdminCompanyOrgUnitDefinitionDTO;
import com.stoneriver.iip.entconfig.sequence.SequenceService;
import com.stoneriver.iip.notepad.model.NotepadDTO;
import com.stoneriver.iip.workflow.api.WorkItemAssignmentDTO;


/**
 * @author Saurabh.Bhatnagar
 * ******* !!! Warning - This class is not Thread safe please create a new instance for the call !!! ********
 */
@Pojo(id = "claims.client.import.ClaimImportHelper")
public class ClaimImportHelper {
	
	private Logger logger = LoggerFactory.getLogger(ClaimImportHelper.class);
	
	private EnterpriseConfigService enterpriseConfigService;
	
	private CWSClaimService claimService = null;
	
	private CWSNoticeOfLossService noticeOfLossService = null;
	
	private SequenceService sequenceService;
	
	Long companyId;
	Long corporationId;
	String lobCode;
	
	//Map to hold the Claim Unit External Id as key and Claim Unit Id (record Id) as value
	Map<String, Long> mClaimUnits = new HashMap<String, Long>();
	//Map to hold the Claim Unit Participant External Id as key and Claim Unit Participant Id (record Id) as value
	Map<String, Long> mClaimUnitParticipants = new HashMap<String, Long>();

	//The following maps are used for setting UI Link Code for Units and Participants 
	//for Assignments, Work Items, Notes etc
	//Map to hold the Claim Unit External Id as key and Claim Unit UI Link Code as value
	Map<String, String> mClaimUnitUILinkCode = new HashMap<String, String>();
	//Map to hold the Claim Unit Participant External Id as key and Claim Unit Participant UI Link Code as value
	Map<String, String> mClaimUnitParticipantUILinkCode = new HashMap<String, String>();

	//Map to hold the Claim Reserve External Id as key and CALClaimReserveDTO as value
	Map<String, CALClaimReserveDTO> mClaimReserves = new HashMap<String, CALClaimReserveDTO>();
	
	//Map to hold Company Ord Unit configured for a User
	//Key --> User Id, Company Ord Unit Id --> Value
	Map<Long, Long> mCompanyOrgUnitIdForUser = new HashMap<Long, Long>();
	
	//Map to hold Functional Role Code configured for a User
	//Key --> User Id, Functional Role Code --> Value
	Map<Long, String> mFunctionalRoleCodeForUser = new HashMap<Long, String>();
	
	private static final String UI_LINK_CLAIM = "clm";
	private static final String UI_LINK_CLAIM_UNIT_THIRDPARTY = "clm_thirdParties";
	private static final String UI_LINK_CLAIM_UNIT_LOCATION = "clm_locations";
	private static final String UI_LINK_CLAIM_UNIT_PROPERTY = "clm_property";
	private static final String UI_LINK_CLAIM_UNIT_INDIVIDUAL = "clm_individual";
	private static final String UI_LINK_CLAIM_UNIT_VEHICLE = "clm_vehicle";
	private static final String UI_LINK_CLAIM_UNIT_OTHER = "clm_otherUnits";
	private static final String UI_LINK_CLAIM_UNIT_COVERAGE = "clm_coverage";
	private static final String UI_LINK_CLAIM_UNIT_RESERVE = "clm_reserves";
	
	private static final String UI_LINK_CLAIM_UNIT_PTCP_OWNER = "clm_owner";
	private static final String UI_LINK_CLAIM_UNIT_PTCP_DRIVER = "clm_driver";
	private static final String UI_LINK_CLAIM_UNIT_PTCP_INJ_PERSON = "clm_injured_person";
	private static final String UI_LINK_CLAIM_UNIT_PTCP_SERV = "clm_service_unit_level";
	private static final String UI_LINK_CLAIM_UNIT_PTCP_OTHER_PARTIES = "clm_other_parties";
	private static final String UI_LINK_CLAIM_UNIT_PTCP_OTHER_CARR = "clm_other_carrier";
	private static final String UI_LINK_CLAIM_UNIT_PTCP_FIN_INT = "clm_financial_interest";
	private static final String UI_LINK_CLAIM_UNIT_PTCP_SALVAGE = "clm_salvage";
	private static final String UI_LINK_CLAIM_UNIT_PTCP_PASSENGER = "clm_passenger";

	ClaimReserveService claimReserveService;

	/**
	 * Getter for CWSClaimService instance.
	 * @return the CWSClaimService implementation
	 */
	protected CWSClaimService getClaimsService() {
		if (claimService == null) {
			claimService = MuleServiceFactory.getService(CWSClaimService.class);
		}
		return claimService;
	}

	/**
	 * Getter for CWSNoticeOfLossService instance.
	 * @return the CWSNoticeOfLossService implementation
	 */
	protected CWSNoticeOfLossService getNoticeOfLossService() {
		if(noticeOfLossService == null) {
			noticeOfLossService = MuleServiceFactory.getService(CWSNoticeOfLossService.class);
		}
		return noticeOfLossService;
	}
	
	/**
	 * @return Instance of EnterpriseConfigService
	 */
	public EnterpriseConfigService getEnterpriseConfigService() {
		if(enterpriseConfigService == null) {
			enterpriseConfigService = MuleServiceFactory.getService(EnterpriseConfigService.class);
		}
		return enterpriseConfigService;
	}
	
	/**
	 * Transform Claim DTO to Claim Initiate DTO and create the Claim.
	 * @param claimDTO to translate to ClaimInitiateDTO
	 * @return ClaimInitiateDTO
	 */
	public ClaimInitiateDTO initiateClaim(ClaimDTO claimDTO) {
		
		ClaimInitiateDTO claimInitiateDTO = new ClaimInitiateDTO();
		
		claimInitiateDTO.setNotificationDate(claimDTO.getNotificationDate());
		claimInitiateDTO.setNotificationMethodCode(claimDTO.getNotificationMethodCode());
		claimInitiateDTO.setNotificationSourceCode(claimDTO.getNotificationSourceCode());
		claimInitiateDTO.setNotificationTimeZone(claimDTO.getNotificationDateTimeZoneCode());
		claimInitiateDTO.setNotificationTime(claimDTO.getNotificationTime());
		
		claimInitiateDTO.setOccurrenceDate(claimDTO.getDateOfLossDate());
		claimInitiateDTO.setOccurrenceTime(claimDTO.getLossDateTime());
		claimInitiateDTO.setOccurrenceTimeZone(claimDTO.getLossDateTimeZoneCode());
		
		claimInitiateDTO.setReporterName(claimDTO.getCallerName()); 
		if(claimDTO.getPolicy() != null) {
			claimInitiateDTO.setPolicyNumber(claimDTO.getPolicy().getPolicyNumber());
			claimInitiateDTO.setPolicyEffectiveDate(claimDTO.getPolicy().getPolicyEffectiveDate());
			claimInitiateDTO.setPolicyEndDate(claimDTO.getPolicy().getPolicyEndDate());
			claimInitiateDTO.setInsuredPartyId(retrievePrimaryInsured(claimDTO.getPolicy()));
			claimInitiateDTO.setPolicy(claimDTO.getPolicy());
		}
		
		if( claimInitiateDTO.getPolicy().getParticipations() == null) {
			claimInitiateDTO.getPolicy().setParticipations(new ArrayList<ClaimPolicyParticipationDTO>());
		}
		
		claimInitiateDTO.getPolicy().setInsuredPartyId(claimInitiateDTO.getInsuredPartyId());
		//claimInitiateDTO.getPolicy().setExternalSourceId(DateUtility.getSystemDateTime().toString());
		claimInitiateDTO.getPolicy().setClaimExternalSourceId(claimDTO.getExternalSourceId());
		
		claimInitiateDTO.setCompanyLOBId(claimDTO.getCompanyLOBId());
		
		retrieveEnterpriseConfiguration(claimDTO.getCompanyLOBId());
		
		claimInitiateDTO.setCompanyId(companyId);
		claimInitiateDTO.setCorporationId(corporationId);
		claimInitiateDTO.setLobCode(lobCode);
		
		if(claimDTO.getClaimMadeDate() != null) {
			Collection<String> codeIdentifiers = new ArrayList<String>();
			codeIdentifiers.add("USER_LINE_OF_BUSINESS");
			Map<String, Collection<CodeLookupBO>> codeDataMap = MuleServiceFactory
					.getService(EnterpriseConfigCodeLookupCollectionService.class).findCodeData(codeIdentifiers);
			Collection<CodeLookupBO> lobCodesByUser = codeDataMap.get("USER_LINE_OF_BUSINESS");
			
			LineOfBusinessCodeLookUpDTO lobLookupDTO = null;
			boolean bClaimMadeDateRequired = false;
			if (lobCodesByUser != null && !lobCodesByUser.isEmpty()) {
				for (CodeLookupBO lobCodeLookupDTO : lobCodesByUser) {
					if (lobCodeLookupDTO instanceof LineOfBusinessCodeLookUpDTO) {
						lobLookupDTO = (LineOfBusinessCodeLookUpDTO) lobCodeLookupDTO;
						if (lobLookupDTO.getLobCode().equals(lobCode)
								&& "y".equals(lobLookupDTO.getClaimMadeInd())) {
							bClaimMadeDateRequired = true;
							break;
						}
					}
				}
			}
			
			if(!bClaimMadeDateRequired) {
				//throw new IIPCoreSystemException("Claim Made Date is not required for " + lobCode + ". Please remove Claim Made Date and import the Claim again.");
				claimDTO.setClaimMadeDate(null);
				claimDTO.setClaimMadeTime(null);
				claimDTO.setClaimMadeDateTimeZoneCode(null);
			}
		}
		
		claimInitiateDTO.setClaimMadeDate(claimDTO.getClaimMadeDate());
		claimInitiateDTO.setClaimMadeTime(claimDTO.getClaimMadeTime());// TimeStamp to Date
		claimInitiateDTO.setClaimMadeTimeZone(claimDTO.getClaimMadeDateTimeZoneCode());
		
		claimInitiateDTO.setClaimSourceCode(claimDTO.getClaimSourceCode());
		claimInitiateDTO.setExternalSourceId(claimDTO.getExternalSourceId());
		claimInitiateDTO.setJurisdictionId(claimDTO.getJurisdictionId());
		claimInitiateDTO.setVerified(!claimDTO.isUnverifiedPolicy());
		
		claimInitiateDTO.setClaimSourceCode(ClaimsServiceConstants.CLAIM_SOURCE_CLAIM_IMPORT);
		
		claimInitiateDTO.setClaimNumber(claimDTO.getClaimNumber());
		
		if(!claimInitiateDTO.isVerified()) {
			if(claimInitiateDTO.getInsuredParty() != null) {
				if(claimInitiateDTO.getInsuredParty().isNew()) {
					claimInitiateDTO.setInsuredPartyId(this.saveInsuredParty(claimInitiateDTO.getInsuredParty()).getRecordId());
				} else {
					claimInitiateDTO.setInsuredPartyId(claimInitiateDTO.getInsuredParty().getRecordId());
					saveInsuredParty(claimInitiateDTO.getInsuredPartyId());
				}
			}
			ClaimPolicyDTO policy = new ClaimPolicyDTO();
			policy.setUnverified(true);
			policy.setCompanyLOBId(claimInitiateDTO.getCompanyLOBId());
			policy.setPolicyNumber(claimInitiateDTO.getPolicyNumber());
			policy.setJurisdictionId(claimInitiateDTO.getJurisdictionId());

			claimInitiateDTO.setPolicy(policy);
		}
		
		return getClaimsService().initiateClaim(claimInitiateDTO);
	}
	
	/**
	 * Save Primary Insured in Party for Claim context. 
	 * @param insuredPartyId Primary Insured to be saved.
	 */
	private void saveInsuredParty(Long insuredPartyId) {
		/**
		 * saving insured party
		 * Assumption is for all lines of business, party already exists
		 */
		PartyService partyService = MuleServiceFactory.getService(
				PartyService.class);
		MinimalPartyRequestDTO request = new MinimalPartyRequestDTO();
		request.setContextType(ClaimsServiceConstants.CONTEXT_CLAIM);
		request.setPartyId(insuredPartyId);
		MinimalPartyDTO insuredParty = partyService.retrieveMinimalParty(request);
		insuredParty.setUpdated(true);
		//now saving party to make sure claim context is created
		partyService.saveMinimalParty(insuredParty);
	}

	/**
	 * Save Primary Insured in Party for Claim context. 
	 * @param insuredParty Primary Insured to be saved.
	 * @return saved minimal party dto
	 */
	private MinimalPartyDTO saveInsuredParty(MinimalPartyDTO insuredParty) {
		if(insuredParty == null) {
			return insuredParty;
		}
		PartyService partyService = MuleServiceFactory.getService(
				PartyService.class);
		
		ensureTaxInformation(insuredParty);
		insuredParty.setContext(ClaimsServiceConstants.CONTEXT_CLAIM);
		
		return partyService.saveMinimalParty(insuredParty);
	}

	/**
	 * makes sure there's valid tax information - this is required if the minimal party control on ui was setup to hide tax information.
	 * @param minimalParty minimalParty to modify.
	 */
	private void ensureTaxInformation(MinimalPartyDTO minimalParty) {
		//Sometimes Tax is not filed in, but Tax status indicator does not reflect this, and DB constraints are violated.
		if(minimalParty.getTaxIdentifier() == null || minimalParty.getTaxIdentifier().getTaxIdentifierNumber() == null) {
			if(minimalParty.getTaxStatusIndicator() == null) {
				minimalParty.setTaxStatusIndicator(new TaxStatusIndicator());
			}
			
			if(!minimalParty.getTaxStatusIndicator().isForeignCitizen()
					&& !minimalParty.getTaxStatusIndicator().isTaxIdDocumentReceived()
					&& !minimalParty.getTaxStatusIndicator().isTaxIdDocumentReceiver()
					&& !minimalParty.getTaxStatusIndicator().isTaxIdExempt()
					&& !minimalParty.getTaxStatusIndicator().isTaxIdUnavailable()) {
				minimalParty.setTaxIdentifier(null);
				minimalParty.getTaxStatusIndicator().setTaxIdUnavailable(true);
			}
		}
	}

	private void retrieveEnterpriseConfiguration(Long companyLOBId) {
		// Retrieve Company Id and Corporation Id on the basis of Company LOB ID.
		ProductDefinitionInfo definition = new ProductDefinitionInfo();
		definition.setCompanyLOBId(companyLOBId);
		definition = getEnterpriseConfigService().getCompanyId(definition);
		Collection<CodeLookupBO> codeLookupBOs = getEnterpriseConfigService().retrieveCorporationByCompanyId(definition.getCompanyId());
		companyId = definition.getCompanyId();
		corporationId = new Long(codeLookupBOs.iterator().next().getCode().toString());
		// To set LOB Code we need to retrieve Company LOB info For Company and then on the basis of Company LoB ID
		Collection<CompanyLOBInfo> companyLOBInfos = getEnterpriseConfigService().retrieveCompanyLOBForCompany(definition.getCompanyId());
		for(CompanyLOBInfo companyLOBInfo : companyLOBInfos){
			if(companyLOBInfo.getLobId().equals(companyLOBId)){
				lobCode = companyLOBInfo.getLobCode();
			}
		}
	}
	
	/**
	 * Translate ClaimDTO to ClaimNoticeOfLossCompositeDTO and save Claim NOL.
	 * @param claimDTO ClaimDTO
	 * @return ClaimNoticeOfLossCompositeDTO
	 */
	public void saveClaimNoticeOfLoss(ClaimImportCompositeDTO claimImportCompositeDTO) {
		
		ClaimDTO claimDTO = claimImportCompositeDTO.getClaimDTO();
		
		if(claimDTO.getClaimTypeCode() != null && claimDTO.getClaimTypeCode().equals("")) {
			claimDTO.setClaimTypeCode(null);
		}
		
        //Temporary fix to overcome DTO Validation for Notification date. DTO validation
        //is done in date only. But during initiate claim validation is done on date and time.
        //so stripping time portion or prevent validation error.
		Calendar calendar = DateUtility.toCalendar(claimDTO.getNotificationDate());

		// clear out the time components
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
        claimDTO.setNotificationDate(DateUtility.toDate(calendar));
        

		if(claimImportCompositeDTO.getClaimExternalSourceId()!=null){
			claimDTO.setExternalSourceId(claimImportCompositeDTO.getClaimExternalSourceId());
		}
		
		if(claimDTO.getClaimStatus().getCurrent() != null
				&& claimDTO.getClaimStatus().getCurrent().getEffectiveDateTime() == null) {
			claimDTO.getClaimStatus().getCurrent().setEffectiveDateTime(DateUtility.getSystemDateTime());
		}
		
		ClaimDTO clmDTO = (ClaimDTO)getClaimsService().retrieveDTOOfClaim(claimImportCompositeDTO.getClaimId(), ClaimDTO.class);
		
		ArrayList<String> ignoreProperties = new ArrayList<String>();
		Field[] fields = ClassUtils.getAnnotatedDeclaredFields(
				claimDTO.getClaimIndicators().getClass(), XmlTransient.class, true);
		for (Field f : fields) {
			ignoreProperties.add(f.getName());
		}

		BeanUtils.copyProperties(
				claimImportCompositeDTO.getClaimDTO().getClaimIndicators(),
				clmDTO.getClaimIndicators(), ignoreProperties.toArray(new String[ignoreProperties.size()]));

		BeanUtils.copyProperties(clmDTO.getClaimIndicators(), 
				claimImportCompositeDTO.getClaimDTO().getClaimIndicators());
		
		claimImportCompositeDTO.getClaimDTO().getClaimIndicators().setUpdated(true);
		
		ClaimStatusCompositeDTO importStatus = claimImportCompositeDTO.getClaimDTO().getClaimStatus();
		
		claimImportCompositeDTO.getClaimDTO().setClaimStatus(clmDTO.getClaimStatus());
		
		getNoticeOfLossService().saveLossQuestions(null, claimDTO);
		
		claimDTO.setClaimStatus(importStatus);
  }

	/**
	 * retrieves primary insured.
	 * @param policy policy details
	 * @return Long primary insured party id
	 */
	public Long retrievePrimaryInsured(ClaimPolicyDTO policy) {
		Long insuredPartyId = null;
		if(policy!=null && policy.getParticipations()!=null 
				&& !policy.getParticipations().isEmpty()){
			for(ClaimPolicyParticipationDTO participation : policy.getParticipations()){
				if(participation.getParticipationTypeCd()!=null
						&& ClaimPolicyConstants.CLAIM_POLICY_PARTICIPATION_INSURED.equals(
								participation.getParticipationTypeCd())
						&& participation.isPrimary()){
					insuredPartyId = participation.getPartyId();
				}
			}
		} else if(policy!=null && policy.getClmPolicyInsuredParticipants() != null
				&& !policy.getClmPolicyInsuredParticipants().isEmpty()) {
			for(ClaimPolicyParticipationDTO participation : policy.getClmPolicyInsuredParticipants()){
				if(participation.getParticipationTypeCd()!=null
						&& ClaimPolicyConstants.CLAIM_POLICY_PARTICIPATION_INSURED.equals(
								participation.getParticipationTypeCd())
						&& participation.isPrimary()){
					insuredPartyId = participation.getPartyId();
				}
			}
		}
		return insuredPartyId;
	}

	/**
	 * Create Notepad DTO with ClaimId.
	 * @param notepadDTO
	 * @param ClaimId
	 * @return NotepadDTO
	 */
	public NotepadDTO buildNotepad(NotepadDTO notepadDTO, Long claimId, ClaimImportCompositeDTO claimImportCompositeDTO){
		
		/*AgreementDataValueDTO noteArgDataVal = new AgreementDataValueDTO();
		noteArgDataVal.setAgreementDataValue(claimId.toString());
		Collection<AgreementDataValueDTO> noteArgDataValList = new ArrayList<AgreementDataValueDTO>();
		noteArgDataValList.add(noteArgDataVal);
		
		AgreementDataDTO noteArg = new AgreementDataDTO();
		noteArg.setAgreementDataCd(ClaimFinancialsConstants.EVENT_PARAM_CLAIM_ID);
		noteArg.setAgreementDataValue(noteArgDataValList);
		Collection<AgreementDataDTO> dtoList = new ArrayList<AgreementDataDTO>();
		
		dtoList.add(noteArg);
		
		notepadDTO.setAgreementData(dtoList);*/
		notepadDTO.setAgreementIdDerived(claimId);
		notepadDTO.setAgreementTypeCodeDerived(ClaimsServiceConstants.CONTEXT_CLAIM);
		//notepadDTO.setUserInterfaceLinkCode(ClaimsServiceConstants.CONTEXT_CLAIM);

		if(notepadDTO.getAgreementSubTypeCodeDerived() != null) {
			setSubTypeDetails(claimImportCompositeDTO.isUseExternalIdentifier(), 
					claimImportCompositeDTO.getClaimId(), notepadDTO, notepadDTO.getAgreementSubTypeIdDerived());
		}
		setAgreementDataDetails(notepadDTO, claimImportCompositeDTO);

		return notepadDTO;
		
	}
	/**
	 * Search Claim on the basis of Claim Number or Claim External Source ID.
	 * @param claimImportCompositeDTO
	 * @return ClaimSearchResultDTO
	 */
	public ClaimSearchResultDTO searchClaim(ClaimImportCompositeDTO claimImportCompositeDTO){
		
		//Retrieve Claim ID based on Claim Number or Claim External Source ID.
		ClaimSearchCriteriaDTO criteria = new ClaimSearchCriteriaDTO();
		ClaimSearchResultDTO claimSearch = new ClaimSearchResultDTO();
		if(claimImportCompositeDTO.getClaimExternalSourceId()!= null){
			criteria.setExternalSourceId(claimImportCompositeDTO.getClaimExternalSourceId());
		}
		if(claimImportCompositeDTO.getClaimNumber()!= null){
			criteria.setClaimNumber(
					StringUtility.uppercaseNoWhitespace(claimImportCompositeDTO.getClaimNumber()));
		}
		if(claimImportCompositeDTO.getClaimId() != null) {
			criteria.setClaimId(claimImportCompositeDTO.getClaimId());
		}
		SearchResultsDTO result;
		try {
			result = getClaimsService().searchClaim(criteria);
			logger.info("Claim Search Results Returned: " + result.getSearchResults().size());
			/*if (result.getSearchResults().isEmpty()) {
			  throw new IIPCoreSystemException("Unable to import Claim Details. There is no Claim ");
			} else*/
			if (result.getSearchResults().size() > 1) {
				throw new IIPCoreSystemException("Unable to import Claim Details. More than one Claim ");
			}
			if(!result.getSearchResults().isEmpty()){
				claimSearch = (ClaimSearchResultDTO) result.getSearchResults().iterator().next();
				logger.info("Claim Found RecordId: " + claimSearch.getRecordId());
				//Version Id is not available in the ClaimSearchResultDTO, Get it from getClaimNoticeOfLossDAO().retrieveClaim(claimId);
				ClaimDTO claimDTO = (ClaimDTO) getClaimsService().retrieveDTOOfClaim(claimSearch.getRecordId(), ClaimDTO.class);
				logger.info("Claim Found VersionId: " + claimDTO.getVersion());
				claimSearch.setVersion(claimDTO.getVersion());
			}
			return claimSearch;
			
		} catch (ClaimSearchException e) {
			throw new IIPCoreSystemException("Unable to import Claim Details", e);
		}	
		
	}
	/**
	 * Create Case DTO with Claim ID.
	 * @param caseDTO
	 * @param claimImportCompositeDTO
	 * @return CaseDTO
	 */
	public CaseDTO buildCaseDTO(CaseDTO caseDTO, ClaimImportCompositeDTO claimImportCompositeDTO){
		caseDTO.getCaseContext().setAgreementId(claimImportCompositeDTO.getClaimId());
		caseDTO.setAgreementIdDerived(claimImportCompositeDTO.getClaimId());
		if(caseDTO.getAgreementTypeCode() == null 
				|| caseDTO.getAgreementTypeCodeDerived() == null) {
			caseDTO.setAgreementTypeCode("clm");
			caseDTO.setAgreementTypeCodeDerived("clm");
		}
		
		if(caseDTO.getAgreementSubTypeCodeDerived() != null) {
			setSubTypeDetails(claimImportCompositeDTO.isUseExternalIdentifier(), 
					claimImportCompositeDTO.getClaimId(), caseDTO, caseDTO.getAgreementSubTypeIdDerived());
		}
		setAgreementDataDetails(caseDTO, claimImportCompositeDTO);
		
		if(caseDTO.getCaseNumber() == null) {
			caseDTO.setCaseNumber(getSequenceService().getNextNumberInSequence("cn").toString());
		}
		return caseDTO;
	}
	/**
	 * Generate External Source Id.
	 * @return ExternalSourceId
	 */
	/*public String generateExternalSourceId(){
		Date d = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String dateString = df.format(d);		
		String externaID = "ExternalID_" + dateString;
		return externaID;
	}*/
	
	/**
	 * @param claimDTO Claim DTO to set
	 * @param profileStatements Collection of Profile Statements
	 * @return Collection of ClaimImportProfileStatementDTO
	 */
	public Collection<ClaimImportProfileStatementDTO> createClaimImportProfileStatementDTO(
			ClaimDTO claimDTO, Collection<ProfileStatementItem> profileStatements) {
		
		Collection<ClaimImportProfileStatementDTO> claimImportProfileStatementDTOs
			= new ArrayList<ClaimImportProfileStatementDTO>();
		for(ProfileStatementItem profileStatement : profileStatements) {
			ClaimImportProfileStatementDTO claimImportProfileStatement = 
				new ClaimImportProfileStatementDTO();
			
			claimImportProfileStatement.setParentId(claimDTO.getRecordId());
			
			claimImportProfileStatement.setProfileStatementId(profileStatement.getProfileStatementId());
			claimImportProfileStatement.setProfileStatementNestedId(profileStatement.getProfileStatementNestedId());
			claimImportProfileStatement.setPsnsId(profileStatement.getPsnsId());
			claimImportProfileStatement.setProfileAreaCode(profileStatement.getProfileAreaCd());
			claimImportProfileStatement.setProfileSubAreaCode(profileStatement.getProfileSubAreaCd());
			claimImportProfileStatement.setProfileSubAreaCategoryCode(profileStatement.getProfileSubAreaCategoryCode());
			claimImportProfileStatement.setProfileResponseText(profileStatement.getAnswer());
			
			if(profileStatement.getProfileSection() != null 
					&& !profileStatement.getProfileSection().isEmpty()) {
				
				for(ProfileStatementSelection profileStatementSelection : profileStatement.getProfileSection()) {
					if(profileStatementSelection.getProfileItem() != null 
							&& !profileStatementSelection.getProfileItem().isEmpty()) {
					}
					Collection<ClaimImportProfileStatementDTO> nestedProfileStatements = 
							createClaimImportProfileStatementDTO(claimDTO, profileStatementSelection.getProfileItem());
					if(nestedProfileStatements != null) {
						claimImportProfileStatementDTOs.addAll(nestedProfileStatements);
					}
				}
			}
			claimImportProfileStatementDTOs.add(claimImportProfileStatement);
		}
		
		return claimImportProfileStatementDTOs;
	}
	
	/**
	 * @param claimDTO Claim DTO to set
	 * @param externalInterfaceMerge SimpleBeanMerge instance to merge DTO's
	 */
	public void saveProfileStatements(ClaimDTO claimDTO, ExternalInterfaceBeanMerge externalInterfaceMerge) {
       if(claimDTO.getClaimJurisdictionProfiles() != null 
        		&& !claimDTO.getClaimJurisdictionProfiles().isEmpty()) {
        	Collection<ClaimImportProfileStatementDTO> claimImportProfileStatements = 
        		createClaimImportProfileStatementDTO(claimDTO, claimDTO.getClaimJurisdictionProfiles());
    		for (ClaimImportProfileStatementDTO profile : claimImportProfileStatements) {
            	saveClaimProfile(profile, externalInterfaceMerge, claimDTO.getRecordId());
    		}
        }
        
        if(claimDTO.getClaimLossProfiles() != null 
        		&& !claimDTO.getClaimLossProfiles().isEmpty()) {
        	Collection<ClaimImportProfileStatementDTO> claimImportProfileStatements = 
        		createClaimImportProfileStatementDTO(claimDTO, claimDTO.getClaimLossProfiles());
    		for (ClaimImportProfileStatementDTO profile : claimImportProfileStatements) {
            	saveClaimProfile(profile, externalInterfaceMerge, claimDTO.getRecordId());
    		}
        }
        
        if(claimDTO.getClaimLossIndicatorProfiles() != null 
        		&& !claimDTO.getClaimLossIndicatorProfiles().isEmpty()) {
        	Collection<ClaimImportProfileStatementDTO> claimImportProfileStatements = 
        		createClaimImportProfileStatementDTO(claimDTO, claimDTO.getClaimLossIndicatorProfiles());
    		for (ClaimImportProfileStatementDTO profile : claimImportProfileStatements) {
            	saveClaimProfile(profile, externalInterfaceMerge, claimDTO.getRecordId());
    		}
        }
		
        if(claimDTO.getClaimPolicyIndicatorProfiles() != null 
        		&& !claimDTO.getClaimPolicyIndicatorProfiles().isEmpty()) {
        	Collection<ClaimImportProfileStatementDTO> claimImportProfileStatements = 
        		createClaimImportProfileStatementDTO(claimDTO, claimDTO.getClaimPolicyIndicatorProfiles());
    		for (ClaimImportProfileStatementDTO profile : claimImportProfileStatements) {
            	saveClaimProfile(profile, externalInterfaceMerge, claimDTO.getRecordId());
    		}
        }
	}
	
	/**
	 * Template algorithm to import a profile (claim/unit/participant).
	 * @param profile ClaimImportProfileStatementDTO to import
	 * @param externalInterfaceMerge SimpleBeanMerge instance to merge DTO's
	 * @param claimId Claims for which the profiles are saved
	 * @return imported profile
	 * @throws ClaimImportException Exception thrown when a Data Merge exception occurs or any other errors.
 	 * @throws BoundValidationException Exception thrown when there are DTO Validation and other validation errors.  
	 */
	public ClaimImportProfileStatementDTO saveClaimProfile(ClaimImportProfileStatementDTO profile, 
			ExternalInterfaceBeanMerge externalInterfaceMerge, Long claimId) 
			throws ClaimImportException, BoundValidationException {			
		try {
			
			Long profileRecordIdVerified = null;
			if(profile.getRecordId() != null) {
				profileRecordIdVerified = getClaimsService().checkRecordIdValidity(profile.getRecordId(),ClaimsProfileStatementResponseBO.class);
				if(profileRecordIdVerified == null || profileRecordIdVerified.compareTo(-1L) == 0) {
					//RecordId passed in is not valid.
					throw new IIPCoreSystemException("Unable to Import Claim Profile:" +profile.getClass() + " Profile RecordId passed in is not valid.");
				} 
			} else if(profile.getExternalSourceId() != null){
				//RecordId not passed in. But externalSourceId passed in.
				profileRecordIdVerified = getClaimsService().findRecordIdByExternalSourceId(profile.getExternalSourceId(), claimId, 
						ClaimsProfileStatementResponseBO.class);
			}
			
			
			ClaimsProfileStatementDTO dto = new ClaimsProfileStatementDTO(profile);
			dto.setClaimsId(profile.getParentId());
			
			ExternalSourceIdentifiable toImport = (ExternalSourceIdentifiable) dto;						
			toImport.setExternalSourceId(profile.getExternalSourceId());
			if (profileRecordIdVerified != null) {
				ExternalSourceIdentifiable existentProfile = 
					(ExternalSourceIdentifiable) getNoticeOfLossService().retrieveClaimProfileStatementWithResponse(profileRecordIdVerified);
				
				externalInterfaceMerge.deepMerge(toImport, existentProfile);
				getNoticeOfLossService().saveProfileStatementWithResponse((ProfileStatementBaseDTO) existentProfile);
			} else {
				getNoticeOfLossService().saveProfileStatementWithResponse((ProfileStatementBaseDTO) toImport);
			}
			return profile;
		} catch (DataMergeException e) {
			throw new ClaimImportException(e);
		}
	}
	
	/**
	 * @param claimUnitToImport Claim Unit to Import 
	 * @param externalInterfaceMerge SimpleBeanMerge instance to merge DTO's
	 * @param claimParticipationHelper helper class to retrieve financial interest for Claim Unit
	 * @return Saved Claim Unit 
	 * @throws ClaimImportException
	 * @throws BoundValidationException
	 */
	public void saveClaimUnit(ClaimImportCompositeDTO claimImportCompositeDTO, ExternalInterfaceBeanMerge externalInterfaceMerge, 
			CWSClaimParticipationHelper claimParticipationHelper) 
					throws ClaimImportException, BoundValidationException {
		mClaimUnits = new HashMap<String, Long>();
		mClaimUnitParticipants = new HashMap<String, Long>();

		Long recordIdVerified = null;
		Collection<ClaimUnitSummaryDTO> allUnits = new ArrayList<ClaimUnitSummaryDTO>();
		
		AllClaimUnitService claimUnitService = MuleServiceFactory.getService(AllClaimUnitService.class);

		Collection<ClaimPolicyUnitDTO> claimPolicyUnits = null;
		
		for(ClaimUnitDTO claimUnitToImport : claimImportCompositeDTO.getUnits()) {
			if(claimUnitToImport.getClaimId() == null) {
				claimUnitToImport.setClaimId(claimImportCompositeDTO.getClaimId());
			}
			
			//createUnitMap(claimUnitToImport);
			if(claimUnitToImport.getLobCode() == null) {
				claimUnitToImport.setLobCode(getLobCode());
			}
			
			recordIdVerified = null;
			ClaimEntitySearchResultDTO clmEntySrchResultDTO = findRecordIdAndClaimSearchResult(claimUnitToImport, 
					claimUnitToImport.getRecordId(), ClaimUnitBO.class, claimImportCompositeDTO.getClaimId());
			if(clmEntySrchResultDTO != null) {
				recordIdVerified = clmEntySrchResultDTO.getEntityRecordId();
			}

			ClaimUnitDTO claimUnit;
			
			ClaimUnitSummaryDTO clmUnitSummary = new ClaimUnitSummaryDTO();
			clmUnitSummary.setLobCode(getLobCode());
			clmUnitSummary.setUnitDataTypeCode(claimUnitToImport.getUnitDataTypeCode());
			clmUnitSummary.setChildUpdated(true);
			
			if(claimUnitToImport.getClaimDamageDTO() != null && claimUnitToImport.getClaimDamageDTO().getDamageServicesDTO() != null
					&& !claimUnitToImport.getClaimDamageDTO().getDamageServicesDTO().isEmpty() ) {
				
				for (ClaimDamageServicesDTO claimDamageServicesDTO : claimUnitToImport.getClaimDamageDTO().getDamageServicesDTO()) {
					claimDamageServicesDTO.setUpdated(true);
				}
			}
			
			//If legal Rep Services are available then set the flag to update to true so that data can be saved to DB
			checkIfLegalRepAvailable(claimUnitToImport);

			if (recordIdVerified == null && claimUnitToImport.getPolicyUnitId() == null) {
				claimUnit = claimUnitToImport;
				if(claimPolicyUnits == null) {
					AllClaimsPolicyService claimsPolicyService = 
							MuleServiceFactory.getService(AllClaimsPolicyService.class);
					
					PolicyUnitSearchCriteriaDTO criteria = new PolicyUnitSearchCriteriaDTO();
					criteria.setUnitDataTypeCode(ClaimsServiceConstants.UNIT_DATA_TYPE_ALLUNITS);
					criteria.setClaimId(claimImportCompositeDTO.getClaimId());
					claimPolicyUnits = claimsPolicyService.retrieveClaimsPolicyUnits(criteria);
				}
				
				if((claimPolicyUnits == null || claimPolicyUnits.isEmpty()) 
						&& claimImportCompositeDTO.getClaimReservesWithCoverages() != null 
						&& !claimImportCompositeDTO.getClaimReservesWithCoverages().isEmpty()) {

					throw new IIPCoreSystemException("No valid Policy Unit found for Claim Unit " 
							+ claimUnit.getUnitItemDescription() + ". Please verify Claim Import.");
					
				}
				
				//If the Unit is Scheduled then get the Policy Unit from which we need to get the Coverage.
				//We use the Unit Identifier to get the Policy Unit. If the Unit Identifier is not available
				//then we Item Description
				if(ClaimsServiceConstants.UNIT_SUB_TYPE_SCHEDULED.equals(claimUnit.getUnitSubTypeCode())
						|| ClaimsServiceConstants.UNIT_SUB_TYPE_COMBO.equals(claimUnit.getUnitSubTypeCode())) {
					for(ClaimPolicyUnitDTO claimPolicyUnit : claimPolicyUnits) {
						if(claimPolicyUnit.getUnitIdTxt() != null && claimUnit.getUnitIdentifierValue() != null 
								&& claimPolicyUnit.getUnitIdTxt().equals(claimUnit.getUnitIdentifierValue())
									&& claimPolicyUnit.getUnitTypeCd().equals(claimUnit.getUnitTypeCode())
									&& claimPolicyUnit.getUnitSubTypeCd().equals(claimUnit.getUnitSubTypeCode())
									&& claimPolicyUnit.getUnitCategoryCd().equals(claimUnit.getUnitCategoryCode())
									&& claimPolicyUnit.getUnitDataTypeCd().equals(claimUnit.getUnitDataTypeCode())
									) {
								claimUnit.setPolicyUnitId(claimPolicyUnit.getRecordId());
								break;
						} else if(claimPolicyUnit.getItemDescription() != null 
								&& claimPolicyUnit.getItemDescription().equals(claimUnit.getUnitItemDescription())
									&& claimPolicyUnit.getUnitTypeCd().equals(claimUnit.getUnitTypeCode())
									&& claimPolicyUnit.getUnitSubTypeCd().equals(claimUnit.getUnitSubTypeCode())
									&& claimPolicyUnit.getUnitCategoryCd().equals(claimUnit.getUnitCategoryCode())
									&& claimPolicyUnit.getUnitDataTypeCd().equals(claimUnit.getUnitDataTypeCode())
									) {
								claimUnit.setPolicyUnitId(claimPolicyUnit.getRecordId());
								break;
						} else if (ClaimsServiceConstants.UNIT_DATA_TYPE_VEHICLE.equals(claimUnit.getUnitDataTypeCode())
								&& ClaimsServiceConstants.UNIT_DATA_TYPE_VEHICLE.equals(claimPolicyUnit.getUnitDataTypeCd())) {
							
							ClaimUnitVehicleDTO vehicleDTO = (ClaimUnitVehicleDTO)claimUnit;
							ClaimPolicyUnitVehicleDTO policyVehicleDTO = (ClaimPolicyUnitVehicleDTO) claimPolicyUnit;
							if(vehicleDTO.getVinNumber() != null && policyVehicleDTO.getVin() != null && 
									vehicleDTO.getVinNumber().equals(policyVehicleDTO.getVin())) {
								claimUnit.setPolicyUnitId(claimPolicyUnit.getRecordId());
								break;
							}
						}
					}
					//If Financial interest is added for the Policy Unit then automatically add it to the Claim Unit also.
					clmUnitSummary.setClaimId(claimImportCompositeDTO.getClaimId());
					clmUnitSummary.setPolicyUnitId(claimUnit.getPolicyUnitId());
					
					if(claimUnitToImport.getFinancialInterestList() == null 
							|| claimUnitToImport.getFinancialInterestList().isEmpty()) {
						claimUnitToImport.setFinancialInterestList(
								claimParticipationHelper.createFinancialInterestForUnit(clmUnitSummary));
					}						
					//If the Unit is Third Party then get the Scheduled Policy Unit from which we need to get the Coverage.
					//We use external source to get the scheduled Policy Unit 
				} else if(ClaimsServiceConstants.UNIT_SUB_TYPE_THIRD_PARTY.equals(claimUnitToImport.getUnitSubTypeCode())
						|| ClaimsServiceConstants.UNIT_SUB_TYPE_UNSCHEDULED.equals(claimUnitToImport.getUnitSubTypeCode())) {
					for(ClaimPolicyUnitDTO claimPolicyUnit : claimPolicyUnits) {
						if(claimPolicyUnit.getExternalSourceId() != null 
								&& claimUnit.getExternalSourceId() != null
								/*&& ClaimsServiceConstants.UNIT_SUB_TYPE_SCHEDULED.equals(claimPolicyUnit.getUnitSubTypeCd())
								&& claimPolicyUnit.getUnitCategoryCd().equals(claimUnit.getUnitCategoryCode())
								&& claimPolicyUnit.getUnitDataTypeCd().equals(claimUnit.getUnitDataTypeCode())*/) {
							
							String policyUnitExternalSourceId = claimPolicyUnit.getExternalSourceId();
							
							/*if(!claimImportCompositeDTO.isUseExternalIdentifier()) {
								externalSourceId = claimPolicyUnit.getExternalSourceId() + "_" + 
													claimImportCompositeDTO.getClaimId();
							}*/
							
							if(claimUnit.getExternalSourceId().indexOf(policyUnitExternalSourceId) != -1) {
								claimUnit.setPolicyUnitId(claimPolicyUnit.getRecordId());
								break;
							}
						}
					}
					clmUnitSummary.setClaimId(claimImportCompositeDTO.getClaimId());
					clmUnitSummary.setPolicyUnitId(claimUnit.getPolicyUnitId());
				}
				
				if(claimUnit.getPolicyUnitId() == null
						&& claimImportCompositeDTO.getClaimReservesWithCoverages() != null 
						&& !claimImportCompositeDTO.getClaimReservesWithCoverages().isEmpty()) {
					throw new IIPCoreSystemException("No valid Policy Unit found for Claim Unit " 
							+ claimUnit.getUnitItemDescription() + ". Please verify Claim Import.");
				}
				
				clmUnitSummary.setClaimUnit(claimUnit);
			} else {
				clmUnitSummary.setRecordId(recordIdVerified);
				clmUnitSummary.setClaimId(claimImportCompositeDTO.getClaimId());
				if(claimUnitToImport instanceof ClaimUnitDTO) {
					ClaimUnitVehicleDTO unitDTO = new ClaimUnitVehicleDTO();
					
					BeanUtils.copyProperties(claimUnitToImport, unitDTO);
					clmUnitSummary.setClaimUnit(unitDTO);
				} else {
					clmUnitSummary.setClaimUnit(claimUnitToImport);
				}
			}
			allUnits.add(clmUnitSummary);
		}
		
		if(allUnits != null && !allUnits.isEmpty()) {
			SaveClaimUnitRequestDTO unitReq = new SaveClaimUnitRequestDTO();
			unitReq.setUnits(allUnits);
			unitReq.setClaimId(claimImportCompositeDTO.getClaimId());
			claimUnitService.saveClaimUnits(unitReq);
			for(ClaimUnitSummaryDTO clmUnitSummary : allUnits) {
				createUnitMap(clmUnitSummary.getClaimUnit(), claimImportCompositeDTO.isUseExternalIdentifier());
			}
		}
	}
	
	/**
	 * Map containing Record Id for Units with External Id as Key and Unit Record Id as value.
	 * @param claimUnitToImport
	 * @param useExternalIdentifier
	 */
	private void createUnitMap(ClaimUnitDTO claimUnitToImport, boolean useExternalIdentifier) {
		if(claimUnitToImport.getExternalSourceId() != null) {
			mClaimUnits.put(claimUnitToImport.getExternalSourceId(), claimUnitToImport.getRecordId());
			mClaimUnits.put(claimUnitToImport.getUnitNumber(), claimUnitToImport.getRecordId());
			//if(useExternalIdentifier){
				setUnitUILinkCode(claimUnitToImport, claimUnitToImport.getUnitNumber());
				setUnitUILinkCode(claimUnitToImport, claimUnitToImport.getExternalSourceId());
			//} else {
				//setUnitUILinkCode(claimUnitToImport, claimUnitToImport.getUnitNumber() + "_" + claimUnitToImport.getClaimId());
			//}
			
			createUnitParticipantsMap(claimUnitToImport.getOwners(), useExternalIdentifier);
			if(claimUnitToImport instanceof ClaimUnitVehicleDTO) {
				createUnitParticipantsMap(((ClaimUnitVehicleDTO)claimUnitToImport).getDrivers(), useExternalIdentifier);
				createUnitParticipantsMap(((ClaimUnitVehicleDTO)claimUnitToImport).getPassengers(), useExternalIdentifier);
			}
			createUnitParticipantsMap(claimUnitToImport.getInjuredPersonList(), useExternalIdentifier);
			createUnitParticipantsMap(claimUnitToImport.getOtherPartiesList(), useExternalIdentifier);
		}
	}
	
	/**
	 * Adds Unit UI Link Code for the given Unit (External Source Id) into a Map to be used for 
	 * Assignments, Work Items, Notes etc 
	 * @param claimUnit
	 * @param externalSourceId
	 */
	private void setUnitUILinkCode(ClaimUnitDTO claimUnit, String externalSourceId) {
		if(claimUnit.getUnitSubTypeCode().equals(ClaimUnitConstants.UNIT_TYPE_THIRD)) {
			mClaimUnitUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_THIRDPARTY);
		} else {
			if(claimUnit.getUnitDataTypeCode().equals(CWSClaimConstants.UNIT_DATA_TYPE_VEHICLE)) {
				mClaimUnitUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_VEHICLE);	
			} else if(claimUnit.getUnitDataTypeCode().equals(CWSClaimConstants.UNIT_DATA_TYPE_LOCATION)) {
				mClaimUnitUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_LOCATION);	
			} else if(claimUnit.getUnitDataTypeCode().equals(CWSClaimConstants.UNIT_DATA_TYPE_PROPERTY)) {
				mClaimUnitUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_PROPERTY);	
			} else if(claimUnit.getUnitDataTypeCode().equals(CWSClaimConstants.UNIT_DATA_TYPE_INDIVIDUAL)) {
				mClaimUnitUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_INDIVIDUAL);	
			} else if(claimUnit.getUnitDataTypeCode().equals(CWSClaimConstants.UNIT_DATA_TYPE_OTHER)) {
				mClaimUnitUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_OTHER);
			}
		}
	}
	
	/**
	 * Adds Unit Participant UI Link Code for the given Unit Participant (External Source Id) into a Map
	 * to be used for Assignments, Work Items, Notes etc 
	 * @param participant
	 * @param externalSourceId
	 */
	private void setUnitParticipantUILinkCode(CALClaimParticipationDTO participant, String externalSourceId) {
		if(participant.getParticipantTypeCode().equals(ClaimsAllParticipationConstants.PARTICIPATION_AREA_OWNER)) {
			mClaimUnitParticipantUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_PTCP_OWNER);
		} else if(participant.getParticipantTypeCode().equals(ClaimsAllParticipationConstants.PARTICIPATION_AREA_DRIVER)) {
			mClaimUnitParticipantUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_PTCP_DRIVER);
		} else if(participant.getParticipantTypeCode().equals(ClaimsAllParticipationConstants.PARTICIPATION_AREA_PASSENGER)) {
			mClaimUnitParticipantUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_PTCP_PASSENGER);
		} else if(participant.getParticipantTypeCode().equals(ClaimsAllParticipationConstants.PARTICIPATION_AREA_FINANCIAL_INTEREST)) {
			mClaimUnitParticipantUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_PTCP_FIN_INT);
		} else if(participant.getParticipantTypeCode().equals(ClaimsAllParticipationConstants.PARTICIPATION_AREA_INJURED_PERSON)) {
			mClaimUnitParticipantUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_PTCP_INJ_PERSON);
		} else if(participant.getParticipantTypeCode().equals(ClaimsAllParticipationConstants.PARTICIPATION_AREA_OTHER_CARRIERS)) {
			mClaimUnitParticipantUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_PTCP_OTHER_CARR);
		} else if(participant.getParticipantTypeCode().equals(ClaimsAllParticipationConstants.PARTICIPATION_AREA_UNIT_SERVICE_PROVIDER)) {
			mClaimUnitParticipantUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_PTCP_SERV);
		} else if(participant.getParticipantTypeCode().equals(ClaimsAllParticipationConstants.PARTICIPATION_AREA_SALVOR_PROVIDER)) {
			mClaimUnitParticipantUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_PTCP_SALVAGE);
		} else {
			mClaimUnitParticipantUILinkCode.put(externalSourceId, UI_LINK_CLAIM_UNIT_PTCP_OTHER_PARTIES);
		}
	}

	/**
	 * Map containing Record Id for all Participants with External Id as Key and Participant Record Id as value.
	 * @param participants
	 * @param useExternalIdentifier
	 */
	private void createUnitParticipantsMap(Collection<? extends CALClaimParticipationDTO> participants, boolean useExternalIdentifier) {
		if(participants != null && !participants.isEmpty()) {
			for(CALClaimParticipationDTO participant : participants) {
				if(participant.getExternalSourceId() != null) {
					//if(useExternalIdentifier) {
						mClaimUnitParticipants.put(participant.getExternalSourceId(), participant.getRecordId());
						setUnitParticipantUILinkCode(participant, participant.getExternalSourceId());
					//} else {
						mClaimUnitParticipants.put(participant.getExternalSourceId(), participant.getRecordId());
						setUnitParticipantUILinkCode(participant, participant.getExternalSourceId());
					//}
				}
			}
		}
	}

	/**
	 * @return the lobCode
	 */
	public String getLobCode() {
		return lobCode;
	}

	/**
	 * @param lobCode the lobCode to set
	 */
	public void setLobCode(String lobCode) {
		this.lobCode = lobCode;
	}

	/**
	 * Finds the valid recordIf of the dto passed in based on either the recordIdsent itself or using the claimId/claimExternalSourceId of the dto.
	 * Updates the 'recordIdVerified' and 'claimSearch' with appropriate values.
	 * @param dto ClaimIdentifiable
	 * @param recordIdSent recordId sent in the dto to be verified.
	 * @param boClass BO class of the dto
	 * @param claimId Claim for which the entity needs to be checked
	 * @return ClaimEntitySearchResultDTO with the claim search result and recordId of the particular entity under claim(unit,ptcp..)
	 */
	private ClaimEntitySearchResultDTO findRecordIdAndClaimSearchResult(ClaimIdentifiable dto, 
			Long recordIdSent, Class<? extends EntityData> boClass, Long claimId) {
		ClaimEntitySearchResultDTO returnDTO = new ClaimEntitySearchResultDTO();
		Long recordIdVerified = null;
		if(recordIdSent != null) {
			//verify if the recordId passed in is valid or not.
			recordIdVerified = getClaimsService().checkRecordIdValidity(recordIdSent, boClass);
			returnDTO.setEntityRecordId(recordIdVerified);
			if(recordIdVerified == null || recordIdVerified.compareTo(-1L) == 0) {
				//RecordId passed in is not valid.
				throw new IIPCoreSystemException("Unable to Import Claim :" +dto.getClass() + "RecordId passed in is not valid.");
			}
		} else {
			//recordId not passed in. check if externalSourceid is passed in.
			if(dto.getExternalSourceId() != null) {
				//check if this is a valid externalSourceid.
				recordIdVerified = getClaimsService().findRecordIdByExternalSourceId(dto.getExternalSourceId(), claimId, boClass);
				returnDTO.setEntityRecordId(recordIdVerified);
			}
		}
		return returnDTO;
	}
	
	/**
	 * @param authorityToImport Claim Authority to Import
	 * @param externalInterfaceMerge SimpleBeanMerge instance to merge DTO's
	 * @return Saved Authority information
	 * @throws ClaimImportException
	 * @throws BoundValidationException
	 */
	public void saveClaimLevelParticipantAuthority(
			ClaimImportCompositeDTO claimImportCompositeDTO, ExternalInterfaceBeanMerge externalInterfaceMerge) 
			throws ClaimImportException, BoundValidationException {

		Long recordIdVerified = null;
		Collection<ClaimParticipationAuthorityDTO> authorities = new ArrayList<ClaimParticipationAuthorityDTO>();
		setUpdatedFlagTrueForDamageServicesDTO(claimImportCompositeDTO.getAuthorities());
		for(ClaimParticipationAuthorityDTO authorityToImport : claimImportCompositeDTO.getAuthorities()) {
			/*ClaimAuthorityCompositeService compositeService = MuleServiceFactory
					.getService(ClaimAuthorityCompositeService.class);*/
			recordIdVerified = null;
			
			ClaimEntitySearchResultDTO clmEntySrchResultDTO = findRecordIdAndClaimSearchResult(authorityToImport, 
					authorityToImport.getRecordId(), CALClaimParticipationBO.class, claimImportCompositeDTO.getClaimId());
			if(clmEntySrchResultDTO != null) {
				recordIdVerified = clmEntySrchResultDTO.getEntityRecordId();
			}

			if(authorityToImport.getClaimExternalSourceId()==null){
				authorityToImport.setClaimExternalSourceId(claimImportCompositeDTO.getClaimExternalSourceId());
			}
			if(authorityToImport.getClaimId() == null) {
				authorityToImport.setClaimId(claimImportCompositeDTO.getClaimId());
			}

			if (recordIdVerified == null) {
				authorityToImport.setParticipantTypeCode(ClaimsAllParticipationConstants.PARTICIPATION_AUTHORITY);

			}
			authorities.add(authorityToImport);
		}
		
		getClaimsService().saveClaimParticipationAuthorities(authorities, getLobCode());
	}

	/**
	 * @param witnessToImport Claim Witness to import
	 * @param externalInterfaceMerge SimpleBeanMerge instance to merge DTO's
	 * @return Saved Witness Information
	 * @throws ClaimImportException
	 * @throws BoundValidationException
	 */
	public void saveClaimLevelParticipantWitness(ClaimImportCompositeDTO claimImportCompositeDTO, 
			ExternalInterfaceBeanMerge externalInterfaceMerge) throws ClaimImportException, BoundValidationException {
		ClaimWitnessDTO witnessToReturn = new ClaimWitnessDTO();
		Long recordIdVerified = null;
		CWSClaimWitnessService witnessService = MuleServiceFactory.getService(CWSClaimWitnessService.class);
		ClaimWitnessCompositeService compositeService = MuleServiceFactory
		.getService(ClaimWitnessCompositeService.class);

		Collection<ClaimWitnessDTO> witnesses = witnessService.retrieveClaimWitnessesAndContacts(claimImportCompositeDTO.getClaimId());
		
		Collection<ClaimWitnessDTO> witnessToSave = new ArrayList<ClaimWitnessDTO>();
		setUpdatedFlagTrueForDamageServicesDTO(claimImportCompositeDTO.getWitnesses());
		
		for(ClaimWitnessDTO witnessToImport : claimImportCompositeDTO.getWitnesses()) {
			
			if(witnessToImport.getClaimExternalSourceId()==null){
				witnessToImport.setClaimExternalSourceId(claimImportCompositeDTO.getClaimExternalSourceId());
			}
			if(witnessToImport.getClaimId() == null) {
				witnessToImport.setClaimId(claimImportCompositeDTO.getClaimId());
			}

			recordIdVerified = null;
			ClaimEntitySearchResultDTO clmEntySrchResultDTO = findRecordIdAndClaimSearchResult(witnessToImport, 
					witnessToImport.getRecordId(), CALClaimParticipationBO.class, claimImportCompositeDTO.getClaimId());
			if(clmEntySrchResultDTO != null) {
				recordIdVerified = clmEntySrchResultDTO.getEntityRecordId();
			}
			
			if (recordIdVerified == null) {
				witnessToReturn = witnessToImport;
				witnessToReturn.setClaimId(witnessToImport.getClaimId());
			} else {
				if(witnesses != null && !witnesses.isEmpty()) {
					
					ClaimParticipationRequestDTO participationRequest = new ClaimParticipationRequestDTO();
					participationRequest.setClaimId(witnessToImport.getClaimId().toString());
					participationRequest.setLobCode(getLobCode());
					participationRequest.setParticipationId(recordIdVerified);
					
					witnessToReturn = compositeService.retrieveClaimWitnessParticipantLevel(participationRequest);

					// merge record statement
					if (witnessToImport.getRecordedStatement() != null 
							&& witnessToReturn.getRecordedStatement() != null
							&& witnessToImport.getExternalSourceId() != null && witnessToReturn.getExternalSourceId() != null
							&& witnessToImport.getExternalSourceId().equals(witnessToReturn.getExternalSourceId())) {
						witnessToImport.getRecordedStatement().setRecordId(witnessToReturn.getRecordedStatement().getRecordId());
						witnessToImport.getRecordedStatement().setVersion(witnessToReturn.getRecordedStatement().getVersion());
					}
					witnessToReturn = witnessToImport;
				}
			}
			witnessToSave.add(witnessToReturn);
		}

		witnessService.saveClaimWitnessesAndContacts(witnessToSave, getLobCode());
	}


	/**
	 * @param claimMiscTransactionDTO
	 *            ClaimMiscTransactionDTO
	 * @throws ClaimFinancialsImportException Exception thrown when a Data Merge exception occurs or any other errors.
	 * @throws BoundValidationException Exception thrown when there are DTO Validation and other validation errors.  
	 */
	public void importClaimMiscellaneousTransactions(ClaimMiscTransactionDTO claimMiscTransactionDTO) throws ClaimFinancialsImportException, BoundValidationException {
		ClaimFinancialsService financialService = MuleServiceFactory.getService(ClaimFinancialsService.class);
		financialService.saveImportClaimMiscFinancialTransactions(claimMiscTransactionDTO);
	}
	
	/**
	 * 
	 * @param claimImportCompositeDTO ClaimImportCompositeDTO
	 * @param calClaimReserveDTO CALClaimReserveDTO
	 * @param importUtilPojo Util class to check whether Unit or Participant is valid and to retrieve the corresponding record IDs
	  * @throws ClaimFinancialsImportException Exception thrown when a Data Merge exception occurs or any other errors.
	 * @throws BoundValidationException Exception thrown when there are DTO Validation and other validation errors.  
	 */
	public void importClaimReserveTransactions(ClaimImportCompositeDTO claimImportCompositeDTO, 
			CALClaimReserveDTO calClaimReserveDTO, ImportUtilPojo importUtilPojo, ClaimUnitDAO claimUnitDAO) throws ClaimFinancialsImportException, BoundValidationException {
		if(calClaimReserveDTO.getExternalClaimIdentifier() == null){
			calClaimReserveDTO.setExternalClaimIdentifier(claimImportCompositeDTO.getClaimExternalSourceId());
		}
		if(calClaimReserveDTO.getCalClaimIdDerived() == null) {
			calClaimReserveDTO.setCalClaimIdDerived(claimImportCompositeDTO.getClaimId());
		}
		//Fix for Recoveries. 09/16/2014 @GR - Set transaction status "auth" for recovery reserves.
		if(calClaimReserveDTO.getMonetaryCategoryCode().equals(ClaimReserveConstants.MNY_CAT_RECOVERY)
				&& calClaimReserveDTO.getReserveTxnAuthStatus() == null){
			calClaimReserveDTO.setReserveTxnAuthStatus("auth");
		}
		
		Collection<CalClaimReserveTransactionDTO> reserveTransactions = calClaimReserveDTO.getCalClaimReserveTransactionCollections();

		//If Use External Identifier flag is false then append Claim Id to External Identifier being passed.
		//If External Identifier is not passed then don't append Claim Id.
		if(!claimImportCompositeDTO.isUseExternalIdentifier()) {
			if(calClaimReserveDTO.getExternalUnitIdentifier()  != null
					&& calClaimReserveDTO.getRecordId() == null) {
				calClaimReserveDTO.setExternalUnitIdentifier(calClaimReserveDTO.getExternalUnitIdentifier() + "_" + claimImportCompositeDTO.getClaimId());
			}

			if(calClaimReserveDTO.getExternalParticipantIdentifier() != null
					&& calClaimReserveDTO.getRecordId() == null) {
				calClaimReserveDTO.setExternalParticipantIdentifier(calClaimReserveDTO.getExternalParticipantIdentifier() + "_" + claimImportCompositeDTO.getClaimId());
			}
			
			if(calClaimReserveDTO.getExternalSourceId() != null
					&& calClaimReserveDTO.getRecordId() == null) {
				calClaimReserveDTO.setExternalSourceId(calClaimReserveDTO.getExternalSourceId() + "_" + claimImportCompositeDTO.getClaimId());
			}
		}
		calClaimReserveDTO.setImportReserve(true);
		calClaimReserveDTO.setImportPaymentFlow(true);
		calClaimReserveDTO.setOperationType(ClaimReserveConstants.OPERATION_TYPE_RESERVE);

		if(reserveTransactions == null || reserveTransactions.size() == 0) {
			reserveTransactions = new ArrayList<CalClaimReserveTransactionDTO>();
			
			CalClaimReserveTransactionDTO reserveTransactionDTO = new CalClaimReserveTransactionDTO();
			reserveTransactionDTO.setReserveTransReasonCode(calClaimReserveDTO.getReasonCode());
			
			if(calClaimReserveDTO.getActionType() == null) {
				reserveTransactionDTO.setReserveTransTypeCode(ClaimReserveConstants.CLAIM_RESERVE_TRANS_TYPE_CODE_OPEN);
			} else {
				if(calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_OPEN_ACTION)) {
					reserveTransactionDTO.setReserveTransTypeCode(ClaimReserveConstants.CLAIM_RESERVE_TRANS_TYPE_CODE_OPEN);
				} else if(calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_ADJUST_ACTION)) {
					reserveTransactionDTO.setReserveTransTypeCode(ClaimReserveConstants.CLAIM_RESERVE_TRANS_TYPE_CODE_ADUJSTED);
				} else if(calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_REOPEN_ACTION)) {
					reserveTransactionDTO.setReserveTransTypeCode(ClaimReserveConstants.CLAIM_RESERVE_TRANS_TYPE_CODE_REOPEN);
				} else if(calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_CLOSE_ACTION)) {
					reserveTransactionDTO.setReserveTransTypeCode(ClaimReserveConstants.CLAIM_RESERVE_TRANS_TYPE_CODE_CLOSE);
				}
			}
			reserveTransactions.add(reserveTransactionDTO);
			calClaimReserveDTO.setCalClaimReserveTransactionCollections(reserveTransactions);
		} else {
			for(CalClaimReserveTransactionDTO claimReserveTransaction: reserveTransactions) {
				if(claimReserveTransaction.getReserveTransactionDate() != null) {
					claimReserveTransaction.setReserveTransactionDate(null);
				}
			}
		}
		
		boolean reserveClosed = false;
		
		if(calClaimReserveDTO.getActionType() != null) {
			ClaimReserveCommonDTO claimReserveCommonDTO = null;
			if(calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_ADJUST_ACTION)
					|| calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_CLOSE_ACTION)
					|| calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_REOPEN_ACTION) ) {
				checkIfValidReserve(calClaimReserveDTO, importUtilPojo);
				
				ImportReserveCriteria criteria = createImportReserveCriteria(calClaimReserveDTO);
				claimReserveCommonDTO = getClaimReserveService().isReserveExist(criteria);
			}
			
			//When adjusting a reserve, check if reserve is open or not. If reserve is closed then reopen the reserve and make the adjustment.
			if(calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_ADJUST_ACTION)
					|| calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_REOPEN_ACTION) ) {
				if(claimReserveCommonDTO == null) {
					throw new IIPCoreSystemException("Unable to Adjust a Reserve. Reserve not found. Please check the data.");
				}
				
				if(claimImportCompositeDTO.getClaimPayments() != null && !claimImportCompositeDTO.getClaimPayments().isEmpty()) {
					//check to see if we are making an adjust reserve to make a payment
					for(ClaimPayment claimPayment : claimImportCompositeDTO.getClaimPayments()){
						for(ClaimPayable claimPayable : claimPayment.getClaimPayables()) {
							
							if(claimPayable.getClaimId() == null) {
								claimPayable.setClaimId(claimImportCompositeDTO.getClaimId());
							}
							if(claimPayable.getClaimNumber() == null) {
								claimPayable.setClaimNumber(claimImportCompositeDTO.getClaimNumber());
							}
							
							ClaimReserveCommonDTO claimReservePaymentCommonDTO = 
								checkIfValidPaymentAndReturnCommonData(claimPayable, importUtilPojo, claimUnitDAO);
							
							if(claimReserveCommonDTO.getClaimUnitId().equals(claimReservePaymentCommonDTO.getClaimUnitId()) 
									&& claimReserveCommonDTO.getClaimParticpantId().equals(claimReservePaymentCommonDTO.getClaimParticpantId())
									&& calClaimReserveDTO.getClmCoverageCode().equals(claimPayable.getClmCoverageCode())
									&& calClaimReserveDTO.getClmLossTypeCode().equals(claimPayable.getClmLossTypeCode())
									&& calClaimReserveDTO.getClmReserveTypeCode().equals(claimPayable.getClmReserveTypeCode()) ) {
								
								boolean bImpactIncurredIndicator = isImpactIncurred(claimPayable);
								//If Impact Incurred is 'n' for Monetary Type Code and Reserve Type Code then it is not required to adjust the reserve for
								//making the payment as the reserve will not reflect the payment
								if(!bImpactIncurredIndicator) {
									calClaimReserveDTO.setAdjustmentAmount(new BigDecimal(0));
									calClaimReserveDTO.setCalClaimReserveAmtDerived(new BigDecimal(0));
								}
							}
						}
					}
				}
				reserveClosed = reopenAdjustReserveIfClosed(calClaimReserveDTO, importUtilPojo, claimReserveCommonDTO);
			} else if(calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_CLOSE_ACTION)) {
				//when closing the reserve check whether the reserve is valid and get the existing outstanding amount to set in the DTO.
				if(claimReserveCommonDTO == null) {
					throw new IIPCoreSystemException("Unable to close the Reserve. Reserve not found. Please check the data.");
				}
				
				BigDecimal outstandingReserve = getClaimReserveService().calculateOutstandingReserve(claimReserveCommonDTO.getReserveId());
				calClaimReserveDTO.setExistingOutstandingAmount(outstandingReserve);
				if(calClaimReserveDTO.getReserveTxnAuthStatus() == null) {
					calClaimReserveDTO.setReserveTxnAuthStatus("auth");
				}
				if(calClaimReserveDTO.getVersion() == null) {
					CALClaimReserveDTO claimReserve = getClaimReserveService().retrieveClaimReserve(claimReserveCommonDTO.getReserveId());
					calClaimReserveDTO.setCalClaimReserveStatus(claimReserve.getCalClaimReserveStatus());
					calClaimReserveDTO.setCalClaimReserveTransactionCollections(claimReserve.getCalClaimReserveTransactionCollections());
					calClaimReserveDTO.setVersion(claimReserve.getVersion());
					calClaimReserveDTO.setVersionGroupId(claimReserve.getVersionGroupId());
				}
			}
		}
		
		if(reserveClosed && calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_REOPEN_ACTION)) {
			mClaimReserves.put(calClaimReserveDTO.getExternalSourceId(), calClaimReserveDTO);
		} else {
			CalClaimReserveCompositeDTO claimReserveCompositeDTO = getClaimReserveService().authorizeClaimReserveTransactionFromExternalSystem(calClaimReserveDTO);
			
			if(!claimReserveCompositeDTO.getAuthorizations().isEmpty()){
				throw new IIPCoreSystemException("User: " + calClaimReserveDTO.getUser() + " is not authorized to import claim reserve transactions");
			}
			
			if(claimReserveCompositeDTO != null && claimReserveCompositeDTO.getClaimReserveDTO() != null) {
				//if(claimImportCompositeDTO.isUseExternalIdentifier()){
					mClaimReserves.put(calClaimReserveDTO.getExternalSourceId(), claimReserveCompositeDTO.getClaimReserveDTO());
					
				//} else {
					//mClaimReserves.put(calClaimReserveDTO.getExternalSourceId()+"_"+claimImportCompositeDTO, claimReserveCompositeDTO.getClaimReserveDTO());
				//}
			}
		}
	}
	
	/**
	 * Check if the unit, participant and reserve is valid
	 * @param calClaimReserveDTO
	 * @param importUtilPojo
	 */
	private void checkIfValidReserve(CALClaimReserveDTO calClaimReserveDTO, ImportUtilPojo importUtilPojo) {
		ClaimUnitParticipantCommonDTO commonDTO = createReserveClaimUnitParticipantCommonDTO(calClaimReserveDTO);
		StringBuffer invalidResult = new StringBuffer();
		
		importUtilPojo.isValidClaimIdOrExternalClaimId(commonDTO, invalidResult);
		importUtilPojo.isValidUnitIdOrExternalUnitId(commonDTO, invalidResult);
		importUtilPojo.isValidparticipantIdOrExternalParticiapntd(commonDTO, invalidResult);
		
		int errorMessage = invalidResult.length();
		if (errorMessage > 0) {
			throw new IIPCoreSystemException("unable to import claim reserve transaction for " +  invalidResult.toString());
		}

		ClaimUnitAndParticipantNestedResultData data = getClaimReserveService().retrieveClaimUnitParticipantInfo(commonDTO);
		if (data == null) {
			String msg = "Unable to import Claim Reserve Transactions as the combination for unit="+ commonDTO.getUnitId() +", particpant="+commonDTO.getparticipantId()
					+" and claim id="+commonDTO.getClaimId()+ " could not be found";
			throw new IIPCoreSystemException(
					msg);
		}
		
		if(calClaimReserveDTO.getCalClaimUnitIdDerived() == null && data.getClaimUnitId() != null) {
			calClaimReserveDTO.setCalClaimUnitIdDerived(data.getClaimUnitId());
		}
		if(calClaimReserveDTO.getCalClaimPtcpId() == null && data.getClaimParticpantId() != null) {
			calClaimReserveDTO.setCalClaimPtcpId(data.getClaimParticpantId());
		}
		if(calClaimReserveDTO.getClaimPolicyUnitId() == null && data.getClaimPolicyUnitId() != null) {
			calClaimReserveDTO.setClaimPolicyUnitId(data.getClaimPolicyUnitId());
		}
		
		//The data to search for the reserve is correct, check to see if some reserve exists with the same combination.
		if (calClaimReserveDTO.getClmCoverageCode() == null || calClaimReserveDTO.getCalClaimPtcpId() == null 
				|| calClaimReserveDTO.getCalClaimUnitIdDerived() == null || calClaimReserveDTO.getClmLossTypeCode() == null 
				|| calClaimReserveDTO.getClmReserveTypeCode() == null){
			throw new IIPCoreSystemException("Unable to import Claim. Unable to find reserve. Please check the data .");
		}
	}
	
	/**
	 * Reopen the Reserve that is being adjusted if closed. 
	 * @param calClaimReserveDTO
	 * @param importUtilPojo
	 */
	private boolean reopenAdjustReserveIfClosed(CALClaimReserveDTO calClaimReserveDTO, ImportUtilPojo importUtilPojo,
			ClaimReserveCommonDTO claimReserveCommonDTO) {
		/*checkIfValidReserve(calClaimReserveDTO, importUtilPojo);
		
		ImportReserveCriteria criteria = createImportReserveCriteria(calClaimReserveDTO);
		ClaimReserveCommonDTO claimReserveCommonDTO = getClaimReserveService().isReserveExist(criteria);

		if(claimReserveCommonDTO == null) {
			throw new IIPCoreSystemException("Unable to Adjust a Reserve. Reserve not found. Please check the data.");
		}*/
		
		Boolean reserveClosed = getClaimReserveService().isReserveClosed(claimReserveCommonDTO.getReserveId());
		if(reserveClosed) {
			//reopen reserve
			CALClaimReserveDTO reopenReserveDTO = new CALClaimReserveDTO();
			BeanUtils.copyProperties(calClaimReserveDTO, reopenReserveDTO);
			if(calClaimReserveDTO.getActionType().equals(ClaimReserveConstants.CLAIM_RESERVE_REOPEN_ACTION)) {
				reopenReserveDTO.setCalClaimReserveAmtDerived(calClaimReserveDTO.getCalClaimReserveAmtDerived());
			} else {
				reopenReserveDTO.setCalClaimReserveAmtDerived(new BigDecimal(0));
			}
			
			reopenReserveDTO.setReasonCode("open");
			reopenReserveDTO.setActionType(ClaimReserveConstants.CLAIM_RESERVE_REOPEN_ACTION);
			reopenReserveDTO.setReserveTxnAuthStatus(ClaimReserveConstants.CLAIM_RESERVE_TRANS_STATUS_AUTHORIZED);

			Collection<CalClaimReserveTransactionDTO> reserveTransactions = new ArrayList<CalClaimReserveTransactionDTO>();
			
			CalClaimReserveTransactionDTO reserveTransactionDTO = new CalClaimReserveTransactionDTO();
			reserveTransactionDTO.setReserveTransReasonCode(reopenReserveDTO.getReasonCode());
			
			reserveTransactionDTO.setReserveTransTypeCode(ClaimReserveConstants.CLAIM_RESERVE_TRANS_TYPE_CODE_REOPEN);

			reserveTransactions.add(reserveTransactionDTO);
			reopenReserveDTO.setCalClaimReserveTransactionCollections(reserveTransactions);

			reopenReserveDTO.setImportReserve(true);
			
			reopenReserveDTO.setOperationType(ClaimReserveConstants.OPERATION_TYPE_RESERVE);
			
			reopenReserveDTO.setImportPaymentFlow(true);
			
			getClaimReserveService().maintainReserve(reopenReserveDTO);
		}
		return reserveClosed;
	}

	/**
	 *
	 * @param claimReserve
	 *            ClaimReserve.
	 * @return ClaimUnitParticipantCommonDTO
	 */
	private ClaimUnitParticipantCommonDTO createReserveClaimUnitParticipantCommonDTO(
			CALClaimReserveDTO claimReserve) {
		ClaimUnitParticipantCommonDTO claimUnitParticipantCommonDTO = new ClaimUnitParticipantCommonDTO();
		claimUnitParticipantCommonDTO.setUnitId(claimReserve.getCalClaimUnitIdDerived());
		claimUnitParticipantCommonDTO.setClaimId(claimReserve.getCalClaimIdDerived());
		claimUnitParticipantCommonDTO.setParticipantId(claimReserve
				.getCalClaimPtcpId());
		claimUnitParticipantCommonDTO.setExternalClaimIdentifier(claimReserve
				.getExternalClaimIdentifier());
		claimUnitParticipantCommonDTO.setExternalUnitIdentifier(claimReserve
				.getExternalUnitIdentifier());
		claimUnitParticipantCommonDTO
				.setExternalParticipantIdentifier(claimReserve
						.getExternalParticipantIdentifier());
		claimUnitParticipantCommonDTO.setReseveType(claimReserve
				.getClmReserveTypeCode());
		claimUnitParticipantCommonDTO.setLossType(claimReserve.getClmLossTypeCode());
		claimUnitParticipantCommonDTO.setCoverageCode(claimReserve
				.getClmCoverageCode());
		return claimUnitParticipantCommonDTO;
	}

	/**
	 * Imports the disbursement transaction for claim.
	 * @param claimPayment the claimPayment
	 * @throws ClaimFinancialsImportException the ClaimFinancialsImportException
	 * @throws BoundValidationException the BoundValidationException
	 */
	public void importClaimDisbursementTransactions(ClaimPayment claimPayment) throws ClaimFinancialsImportException, BoundValidationException{
		/*ClaimReservePaymentService claimReservePaymentService = MuleServiceFactory.getService(ClaimReservePaymentService.class);
		try {*/
			MuleServiceFactory.getService(ClaimReservePaymentService.class).saveClaimPaymentFromExternalSystem(claimPayment);
		/*} catch(IIPCoreSystemException iipe) {
			//The reason exception is checked to see whether the reserve is available or not because
			//we need to duplicate base code here to check whether reserve exists or not. The conditions 
			//that needs to checked are whether the Claim, Unit and Participant is valid if the Record Id or
			//external identifier is passed. If external identifier is passed then we need to find the Record Id.\
			//To prevent this we are calling the import payment method and checking if reserve doesn't exist.
			//If base adds a method to import fast track payment then we need to remove this check and call the new method.
			if(iipe.getMessage() != null
					&& iipe.getMessage().equals("Unable to import Claim Disbursement. Unable to find reserve.")) {
				
				//Reserve doesn't exist so create a new reserve, import the payment and close it.
				createReserveForFastTrack(claimPayment);
			} else {
				throw iipe;
			}
		}*/
	}
	
	/**
	 * Method to create a Fast Track Reserve Transaction if for a payment reserve doesn't exist.
	 * @param transactionObject
	 * @param team
	 * @param userName
	 * @param claimPolicyUnitId
	 */
	private void createReserveForFastTrack(Object transactionObject, Long team, 
			String userName, Long claimPolicyUnitId, boolean bImpactIncurredIndicator) {
		if(transactionObject instanceof ClaimPayable) {
			ClaimPayable claimPayable = (ClaimPayable) transactionObject;
			CALClaimReserveDTO reserveDTO = new CALClaimReserveDTO();

			reserveDTO.setClmCoverageCode(claimPayable.getClmCoverageCode());
			reserveDTO.setClmReserveTypeCode(claimPayable.getClmReserveTypeCode());
			reserveDTO.setClmLossTypeCode(claimPayable.getClmLossTypeCode());
			reserveDTO.setMonetaryCategoryCode(claimPayable.getMonetaryCategoryCode());

			
			if(bImpactIncurredIndicator) {
				reserveDTO.setCalClaimReserveAmtDerived(claimPayable.getTransactionAmount());
			} else {
				//If Impact Incurred = 'n' bImpactIncurredIndicator will be false, the reserve will not be reduced when the payment is done.
				//So when creating a reserve for fast track payment, the reserve create with zero amount
				reserveDTO.setCalClaimReserveAmtDerived(new BigDecimal(0));
			}

			reserveDTO.setPreApprovedIndicator(false);
			
			reserveDTO.setActionType(ClaimReserveConstants.CLAIM_RESERVE_FAST_TRACK_ACTION);
			reserveDTO.setReserveTxnAuthStatus(ClaimReserveConstants.CLAIM_RESERVE_TRANS_STATUS_AUTHORIZED);

			reserveDTO.setReasonCode(ClaimReserveConstants.CLAIM_RESERVE_FAST_TRACK_REASON);
			reserveDTO.setTransactionAmount(claimPayable.getTransactionAmount());
			reserveDTO.setReserveTransactionDate(claimPayable.getTransactionDate());
			
			reserveDTO.setClaimNumber(claimPayable.getClaimNumber());
			reserveDTO.setTeamId(team);
			reserveDTO.setUser(userName);
			
			reserveDTO.setExternalUnitIdentifier(claimPayable.getExternalUnitIdentifier());
			reserveDTO.setExternalParticipantIdentifier(claimPayable.getExternalParticipantIdentifier());
			
			reserveDTO.setCalClaimIdDerived(claimPayable.getClaimId());
			reserveDTO.setCalClaimUnitIdDerived(claimPayable.getUnitId());
			reserveDTO.setCalClaimPtcpId(claimPayable.getParticipantId());
			
			reserveDTO.setClaimPolicyUnitId(claimPolicyUnitId);
			
			reserveDTO.setVersionGroupId(new Long(0));
			
			CalClaimReserveStatusCompositeDTO claimReserveStatusCompositeDTO = new CalClaimReserveStatusCompositeDTO();
			
			CalClaimReserveStatusDTO reserveStatus = new CalClaimReserveStatusDTO();
			reserveStatus.setCalClaimReserveStatusCode(ClaimReserveConstants.CLAIM_RESERVE_STATUS_OPEN);
			reserveStatus.setEffectiveDateTime(DateUtility.getSystemDateTime());
			
			claimReserveStatusCompositeDTO.setCurrent(reserveStatus);
			reserveDTO.setCalClaimReserveStatus(claimReserveStatusCompositeDTO);
			
			Collection<CalClaimReserveTransactionDTO> reserveTransactions = new ArrayList<CalClaimReserveTransactionDTO>();
				
			CalClaimReserveTransactionDTO reserveTransactionDTO = new CalClaimReserveTransactionDTO();
			reserveTransactionDTO.setReserveTransReasonCode(reserveDTO.getReasonCode());
			
			reserveTransactionDTO.setReserveTransTypeCode(ClaimReserveConstants.CLAIM_RESERVE_TRANS_TYPE_CODE_OPEN);

			reserveTransactions.add(reserveTransactionDTO);
			reserveDTO.setCalClaimReserveTransactionCollections(reserveTransactions);
			
			if(claimPayable.getExternalUnitIdentifier() != null && 
					claimPayable.getExternalParticipantIdentifier() != null) {
				reserveDTO.setExternalSourceId(claimPayable.getExternalUnitIdentifier()
						+ "_" + claimPayable.getExternalParticipantIdentifier());
			} else {
				reserveDTO.setExternalSourceId(claimPayable.getUnitId()
						+ "_" + claimPayable.getParticipantId());
			}
			
			reserveDTO.setImportReserve(true);
			reserveDTO.setImportPaymentFlow(true);
			
			reserveDTO.setOperationType(ClaimReserveConstants.OPERATION_TYPE_FAST_TRACK);
			claimPayable.setCloseReserveIndicator(true);
			
			getClaimReserveService().maintainReserve(reserveDTO);
		} /*else if(transactionObject instanceof ClaimRecovery) {
			ClaimRecovery claimRecovery = (ClaimRecovery) transactionObject;
			CALClaimReserveDTO reserveDTO = new CALClaimReserveDTO();

			for(ClaimRecoveryPayable claimRecoveryPayable : claimRecovery.getClaimRecoveryPayables()) {
				reserveDTO.setClmCoverageCode(claimRecoveryPayable.getCoverageType());
				reserveDTO.setClmReserveTypeCode(claimRecoveryPayable.getReserveType());
				reserveDTO.setClmLossTypeCode(claimRecoveryPayable.getLossType());
				
				reserveDTO.setCalClaimReserveAmtDerived(claimRecoveryPayable.getTransactionAmount());
				reserveDTO.setPreApprovedIndicator(false);
				
				reserveDTO.setActionType(ClaimReserveConstants.CLAIM_RESERVE_FAST_TRACK_ACTION);
				reserveDTO.setReserveTxnAuthStatus(ClaimReserveConstants.CLAIM_RESERVE_TRANS_STATUS_AUTHORIZED);

				reserveDTO.setReasonCode(ClaimReserveConstants.CLAIM_RESERVE_FAST_TRACK_REASON);
				reserveDTO.setTransactionAmount(claimRecoveryPayable.getTransactionAmount());
				reserveDTO.setReserveTransactionDate(claimRecoveryPayable.getTransactionDate());
				
				reserveDTO.setClaimNumber(claimRecoveryPayable.getClaimNumber());
				
				reserveDTO.setExternalUnitIdentifier(claimRecoveryPayable.getExternalUnitIdentifier());
				reserveDTO.setExternalParticipantIdentifier(claimRecoveryPayable.getExternalParticipantIdentifier());
				
				reserveDTO.setCalClaimIdDerived(claimRecoveryPayable.getClaimId());
				reserveDTO.setCalClaimUnitIdDerived(claimRecoveryPayable.getUnitId());
				reserveDTO.setCalClaimPtcpId(claimRecoveryPayable.getParticipantId());
				
				CalClaimReserveStatusCompositeDTO claimReserveStatusCompositeDTO = new CalClaimReserveStatusCompositeDTO();
				
				CalClaimReserveStatusDTO reserveStatus = new CalClaimReserveStatusDTO();
				reserveStatus.setCalClaimReserveStatusCode(ClaimReserveConstants.CLAIM_RESERVE_STATUS_OPEN);
				reserveStatus.setEffectiveDateTime(DateUtility.getSystemDateTime());
				
				claimReserveStatusCompositeDTO.setCurrent(reserveStatus);
				reserveDTO.setCalClaimReserveStatus(claimReserveStatusCompositeDTO);
				
				if(claimRecoveryPayable.getExternalUnitIdentifier() != null && 
						claimRecoveryPayable.getExternalParticipantIdentifier() != null) {
					reserveDTO.setExternalSourceId(claimRecoveryPayable.getExternalUnitIdentifier()
							+ "_" + claimRecoveryPayable.getExternalParticipantIdentifier());
				} else {
					reserveDTO.setExternalSourceId(claimRecoveryPayable.getUnitId()
							+ "_" + claimRecoveryPayable.getParticipantId());
				}
				
				reserveDTO.setImportReserve(true);
				
				//claimRecoveryPayable.setCloseReserveIndicator(true);
				break;
			}
			importClaimReserveTransactions(reserveDTO);
			saveClaimRecoveries(claimRecovery);
		}*/
	}
	
	/**
	 * Method to create a Fast Track Reserve Transaction if for a payment reserve doesn't exist.
	 * @param transactionObject
	 * @param team
	 * @param userName
	 * @param claimPolicyUnitId
	 */
	private void reopenClosedReserveForPayment(Object transactionObject, Long team, 
			String userName, Long claimPolicyUnitId, Long claimReserveId, boolean bImpactIncurredIndicator) {
		if(transactionObject instanceof ClaimPayable) {
			ClaimPayable claimPayable = (ClaimPayable) transactionObject;
			CALClaimReserveDTO reserveDTO = new CALClaimReserveDTO();
			
			reserveDTO.setRecordId(claimReserveId);

			reserveDTO.setClmCoverageCode(claimPayable.getClmCoverageCode());
			reserveDTO.setClmReserveTypeCode(claimPayable.getClmReserveTypeCode());
			reserveDTO.setClmLossTypeCode(claimPayable.getClmLossTypeCode());
			reserveDTO.setMonetaryCategoryCode(claimPayable.getMonetaryCategoryCode());

			if(bImpactIncurredIndicator) {
				reserveDTO.setCalClaimReserveAmtDerived(claimPayable.getTransactionAmount());
			} else {
				//If Impact Incurred = 'n' bImpactIncurredIndicator will be false, the reserve will not be reduced when the payment is done.
				//So when reopening the reserve create with zero amount
				reserveDTO.setCalClaimReserveAmtDerived(new BigDecimal(0));
			}
			reserveDTO.setPreApprovedIndicator(false);
			
			reserveDTO.setReasonCode("open");
			reserveDTO.setActionType(ClaimReserveConstants.CLAIM_RESERVE_REOPEN_ACTION);
			reserveDTO.setReserveTxnAuthStatus(ClaimReserveConstants.CLAIM_RESERVE_TRANS_STATUS_AUTHORIZED);

			reserveDTO.setTransactionAmount(new BigDecimal(0));
			reserveDTO.setReserveTransactionDate(claimPayable.getTransactionDate());
			
			reserveDTO.setClaimNumber(claimPayable.getClaimNumber());
			reserveDTO.setTeamId(team);
			reserveDTO.setUser(userName);
			
			reserveDTO.setExternalUnitIdentifier(claimPayable.getExternalUnitIdentifier());
			reserveDTO.setExternalParticipantIdentifier(claimPayable.getExternalParticipantIdentifier());
			
			reserveDTO.setCalClaimIdDerived(claimPayable.getClaimId());
			reserveDTO.setCalClaimUnitIdDerived(claimPayable.getUnitId());
			reserveDTO.setCalClaimPtcpId(claimPayable.getParticipantId());
			
			reserveDTO.setClaimPolicyUnitId(claimPolicyUnitId);
			
			reserveDTO.setVersionGroupId(new Long(0));
			
			CalClaimReserveStatusCompositeDTO claimReserveStatusCompositeDTO = new CalClaimReserveStatusCompositeDTO();
			
			CalClaimReserveStatusDTO reserveStatus = new CalClaimReserveStatusDTO();
			reserveStatus.setCalClaimReserveStatusCode(ClaimReserveConstants.CLAIM_RESERVE_STATUS_OPEN);
			reserveStatus.setEffectiveDateTime(DateUtility.getSystemDateTime());
			
			claimReserveStatusCompositeDTO.setCurrent(reserveStatus);
			reserveDTO.setCalClaimReserveStatus(claimReserveStatusCompositeDTO);
			
			Collection<CalClaimReserveTransactionDTO> reserveTransactions = new ArrayList<CalClaimReserveTransactionDTO>();
				
			CalClaimReserveTransactionDTO reserveTransactionDTO = new CalClaimReserveTransactionDTO();
			reserveTransactionDTO.setReserveTransReasonCode(reserveDTO.getReasonCode());
			
			reserveTransactionDTO.setReserveTransTypeCode(ClaimReserveConstants.CLAIM_RESERVE_TRANS_TYPE_CODE_OPEN);

			reserveTransactions.add(reserveTransactionDTO);
			reserveDTO.setCalClaimReserveTransactionCollections(reserveTransactions);
			
			if(claimPayable.getExternalUnitIdentifier() != null && 
					claimPayable.getExternalParticipantIdentifier() != null) {
				reserveDTO.setExternalSourceId(claimPayable.getExternalUnitIdentifier()
						+ "_" + claimPayable.getExternalParticipantIdentifier());
			} else {
				reserveDTO.setExternalSourceId(claimPayable.getUnitId()
						+ "_" + claimPayable.getParticipantId());
			}
			
			reserveDTO.setImportReserve(true);
			reserveDTO.setImportPaymentFlow(true);
			
			reserveDTO.setOperationType(ClaimReserveConstants.OPERATION_TYPE_ISSUE_PAYMENT_AFTER_CLOSE);

			claimPayable.setCloseReserveIndicator(true);
			
			getClaimReserveService().maintainReserve(reserveDTO);
		}
	}
	
	/**
	 * @param claimImportCompositeDTO Claim Information
	 */
	public void saveClaimStatus(ClaimImportCompositeDTO claimImportCompositeDTO) {
		//ClaimStatusDTO status = new ClaimStatusDTO();
		AllClaimStatusService claimStatusService = MuleServiceFactory.getService(AllClaimStatusService.class);
		
		ClaimStatusDTO current = claimStatusService.retrieveClaimCurrentStatus(claimImportCompositeDTO.getClaimDTO().getRecordId());

		if(current.getClaimId() == null) {
			current.setClaimId(claimImportCompositeDTO.getClaimDTO().getRecordId());
		}
		if(claimImportCompositeDTO.getClaimDTO().getClaimStatus() != null && 
				claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent() != null) {
			if(claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent().getExternalSourceId()!=null) {
				current.setExternalSourceId(claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent().getExternalSourceId());
			}
			if(claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent().getClaimExternalSourceId() != null && 
					claimImportCompositeDTO.getClaimExternalSourceId() != null){
				current.setClaimExternalSourceId(claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent().getClaimExternalSourceId());
			}
		}
		
		current.setActionCode(claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent().getActionCode());
		current.setReasonCode(claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent().getReasonCode());
		current.setStateCode(claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent().getStateCode());
		current.setStatusCode(claimImportCompositeDTO.getClaimDTO().getClaimStatus().getCurrent().getStatusCode());

		if (ClaimStatusConstants.ACTION_RELEASE_CLAIM.equals(current.getActionCode())) {
			// action == "rlseclm" => Release claim
			ClaimInformationRequestDTO claimInformationRequestDTO = new ClaimInformationRequestDTO();
			claimInformationRequestDTO.setClaimId(claimImportCompositeDTO.getClaimDTO().getRecordId());
			claimInformationRequestDTO.setLobCode(lobCode);
			MuleServiceFactory.getService(ClaimReleaseService.class)
					.releaseClaim(claimInformationRequestDTO);
			//service.retrieveClaimCurrentStatus(cla);
		} else {
			// not releasing a claim, changing state/status with values provided by client.
			// Provided values are assumed to be a valid status transition.
			claimStatusService.saveClaimStatus(current);
		}
	}
	
	/**
	 * Save Claim Indicators. 
	 * @param claimImportCompositeDTO
	 */
	//We are using maintain method available in MaintainLossIndicatorService to save Claim Indicators. Since it is a maintain method
	//we need to retrieve the claim and merge the properties to save the indicators. The reason is a default row is created in
	//CLAIM_INDICATOR table when a claim is saved.
	public void saveClaimIndicators(ClaimImportCompositeDTO claimImportCompositeDTO) {
		
		ClaimDTO claimDTO = ClaimDTO.class.cast(getClaimsService().retrieveDTOOfClaim(claimImportCompositeDTO.getClaimId(), ClaimDTO.class));
		
		ArrayList<String> ignoreProperties = new ArrayList<String>();

		// Get the Fields marked as @XmlTransient and use the list as ignore
		// properties
		Field[] fields = ClassUtils.getAnnotatedDeclaredFields(
				claimDTO.getClaimIndicators().getClass(), XmlTransient.class, true);
		for (Field f : fields) {
			ignoreProperties.add(f.getName());
		}

		BeanUtils.copyProperties(
				claimImportCompositeDTO.getClaimDTO().getClaimIndicators(),
				claimDTO.getClaimIndicators(), ignoreProperties.toArray(new String[ignoreProperties.size()]));
		
		claimDTO.getClaimIndicators().setUpdated(true);
		
		ClaimDetailsCompositeDTO claimDetailsCompositeDTO = new ClaimDetailsCompositeDTO();
		claimDetailsCompositeDTO.setSystemIndicators(claimDTO.getClaimIndicators());
		
		MaintainLossIndicatorService maintainLossIndicatorService = MuleServiceFactory.getService(MaintainLossIndicatorService.class);
		maintainLossIndicatorService.saveSystemIndicator(claimDTO, claimDetailsCompositeDTO);
		
	}

	/**
	 * Save Claim Services
	 * @param claimImportCompositeDTO
	 * @param externalInterfaceMerge SimpleBeanMerge instance to merge DTO's
	 * @throws ClaimImportException
	 * @throws BoundValidationException
	 */
	public void saveClaimServices(ClaimImportCompositeDTO claimImportCompositeDTO, ExternalInterfaceBeanMerge externalInterfaceMerge) 
				throws ClaimImportException, BoundValidationException {
		
		ClaimServicesCompositeService compositeService = MuleServiceFactory
		.getService(ClaimServicesCompositeService.class);
		
		for(ClaimDamageServicesDTO claimService : claimImportCompositeDTO.getClaimServices()) {
			
			claimService.setUpdated(true);

			if(claimService.getClaimExternalSourceId()==null){
				claimService.setClaimExternalSourceId(claimImportCompositeDTO.getClaimExternalSourceId());
			}
			if(claimService.getClaimId() == null) {
				claimService.setClaimId(claimImportCompositeDTO.getClaimId());
			}
			
			ClaimInformationRequestDTO claimRequest = new ClaimInformationRequestDTO();
			claimRequest.setClaimId(claimService.getClaimId());
			claimRequest.setLobCode(getLobCode());
			
			ClaimServicesCompositeDTO serviceComposite = new ClaimServicesCompositeDTO();
			serviceComposite.getClaimServicesSummarys().add(claimService);
			serviceComposite.setClaimRequest(claimRequest);
			
			compositeService.saveClaimServices(serviceComposite);
		}
	}
	
	/**
	 * @return Sequence Service
	 */
	private SequenceService getSequenceService() {
		if(sequenceService == null) {
			sequenceService = MuleServiceFactory.getService(SequenceService.class);
		}
		return sequenceService;
	}
	
	/**
	 * Set the Agreement Details for Component  
	 * @param baseToolInformation Component for which the agreement details need to be set.
	 * @param claimImportCompDTO DTO containing the Claim Details
	 */
	public void setAgreementDataDetails( BaseToolDTO baseToolInformation, 
			ClaimImportCompositeDTO claimImportCompDTO) {
		
		if(baseToolInformation.getAgreementData() == null
				|| baseToolInformation.getAgreementData().isEmpty()) {
			
			Collection<AgreementDataDTO> agreementData = new ArrayList<AgreementDataDTO>();
			baseToolInformation.setAgreementData(agreementData);
			
			baseToolInformation.getAgreementData().add(createAgreementDataValue("clm_id", claimImportCompDTO.getClaimId().toString()));
			baseToolInformation.getAgreementData().add(createAgreementDataValue("lob_cd", getLobCode()));
			//baseToolInformation.getAgreementData().add(createAgreementDataValue("clm_nbr", claimImportCompDTO.getClaimNumber()));
			
			if(baseToolInformation.getAgreementSubTypeIdDerived() != null) {
				if(CWSClaimConstants.CLAIM_UNIT_SUBTYPE.equals(baseToolInformation.getAgreementSubTypeCodeDerived())) {
					baseToolInformation.getAgreementData().add(createAgreementDataValue("clm_unt_id", baseToolInformation.getAgreementSubTypeIdDerived().toString()));
				} else if(CWSClaimConstants.CLAIM_UNIT_PARTICIPANTION_SUBTYPE.equals(baseToolInformation.getAgreementSubTypeCodeDerived())) {
					baseToolInformation.getAgreementData().add(createAgreementDataValue("clm_unt_ptcp_id", baseToolInformation.getAgreementSubTypeIdDerived().toString()));
				} /*else if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_PARTICIPATION_SUBTYPE)) {
				} */ else if(CWSClaimConstants.CLAIM_RESERVE_SUBTYPE.equals(baseToolInformation.getAgreementSubTypeCodeDerived())) {
					baseToolInformation.getAgreementData().add(createAgreementDataValue("clm_rsrv_id", baseToolInformation.getAgreementSubTypeIdDerived().toString()));
				}  else if(CWSClaimConstants.CLAIM_COVERAGE_SUBTYPE.equals(baseToolInformation.getAgreementSubTypeCodeDerived())) {
					baseToolInformation.getAgreementData().add(createAgreementDataValue("clm_cov_id", baseToolInformation.getAgreementSubTypeIdDerived().toString()));
				}
			}
		}
	}
	
	/**
	 * Create agreement data for the given code and value.
	 * @param code
	 * @param value
	 * @return AgreementDataDTO
	 */
	private AgreementDataDTO createAgreementDataValue(String code, String value) {
		AgreementDataDTO agreementData = new AgreementDataDTO();
		agreementData.setAgreementDataCd(code);
		Collection<AgreementDataValueDTO> agreementDataValue = new ArrayList<AgreementDataValueDTO>();
		AgreementDataValueDTO agreementDataValueDTO = new AgreementDataValueDTO();
		agreementDataValueDTO.setAgreementDataValue(value);
		agreementDataValue.add(agreementDataValueDTO);
		
		agreementData.setAgreementDataValue(agreementDataValue);
		
		return agreementData;
	}

	/**
	 * Sets Agreement Sub Type ID based on Sub Type Code
	 * @param useExternalIdentifier Boolean indicating whether External Identifier should be used or not
	 * @param baseToolInformation Component for which Sub Type details need to be set.
	 */
	public void setSubTypeDetails(boolean useExternalIdentifier, 
					Long claimId, BaseToolDTO baseToolInformation, Long entityIdentifier) {
		
		baseToolInformation.setUserInterfaceLinkCode(UI_LINK_CLAIM);
		
		if(baseToolInformation.getAgreementSubTypeCodeDerived() != null
				&& entityIdentifier != null) {
			if(!useExternalIdentifier) {
					if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_UNIT_SUBTYPE)) {
						if(mClaimUnits.get(entityIdentifier + "_" + claimId) != null) {
							baseToolInformation.setAgreementSubTypeIdDerived(mClaimUnits.get(entityIdentifier + "_" + claimId));
							baseToolInformation.setUserInterfaceLinkCode(mClaimUnitUILinkCode.get(entityIdentifier + "_" + claimId));
						} else if(mClaimUnits.get(entityIdentifier.toString()) != null) {
							baseToolInformation.setAgreementSubTypeIdDerived(mClaimUnits.get(entityIdentifier.toString()));
							baseToolInformation.setUserInterfaceLinkCode(mClaimUnitUILinkCode.get(entityIdentifier.toString()));
							
						}
					} else if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_UNIT_PARTICIPANTION_SUBTYPE)) {
						if(mClaimUnitParticipants.get(entityIdentifier + "_" + claimId) != null) {
							baseToolInformation.setAgreementSubTypeIdDerived(mClaimUnitParticipants.get(entityIdentifier + "_" + claimId));
							baseToolInformation.setUserInterfaceLinkCode(mClaimUnitParticipantUILinkCode.get(entityIdentifier + "_" + claimId));
						}
					} /*else if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_PARTICIPATION_SUBTYPE)) {
						
					}  */else if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_RESERVE_SUBTYPE)) {
						CALClaimReserveDTO reserveDetails = mClaimReserves.get(entityIdentifier + "_" + claimId);
						if(reserveDetails == null) {
							reserveDetails = mClaimReserves.get(entityIdentifier);
						}
						if(reserveDetails != null) {
							baseToolInformation.setAgreementSubTypeIdDerived(reserveDetails.getRecordId());
							baseToolInformation.setUserInterfaceLinkCode(UI_LINK_CLAIM_UNIT_RESERVE);
						}
					}  else if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_COVERAGE_SUBTYPE)) {
						CALClaimReserveDTO reserveDetails = mClaimReserves.get(entityIdentifier + "_" + claimId);
						if(reserveDetails == null) {
							reserveDetails = mClaimReserves.get(entityIdentifier);
						}
						if(reserveDetails != null) {
							baseToolInformation.setAgreementSubTypeIdDerived(reserveDetails.getClaimCoverageId());
							baseToolInformation.setUserInterfaceLinkCode(UI_LINK_CLAIM_UNIT_COVERAGE);
						}
					}
			} else {
				//Converting to a String since the identifier is added a string into the Map.
				String identifier = entityIdentifier.toString();
				if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_UNIT_SUBTYPE)) {
					if(mClaimUnits.get(identifier) != null) {
						baseToolInformation.setAgreementSubTypeIdDerived(mClaimUnits.get(identifier));
						baseToolInformation.setUserInterfaceLinkCode(mClaimUnitUILinkCode.get(identifier));
					}
				} else if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_UNIT_PARTICIPANTION_SUBTYPE)) {
					if(mClaimUnitParticipants.get(identifier) != null) {
						baseToolInformation.setAgreementSubTypeIdDerived(mClaimUnitParticipants.get(identifier));
						baseToolInformation.setUserInterfaceLinkCode(mClaimUnitParticipantUILinkCode.get(identifier));
					}
				} /*else if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_PARTICIPATION_SUBTYPE)) {
					
				}  */else if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_RESERVE_SUBTYPE)) {
					CALClaimReserveDTO reserveDetails = mClaimReserves.get(identifier);
					if(reserveDetails != null) {
						baseToolInformation.setAgreementSubTypeIdDerived(reserveDetails.getRecordId());
						baseToolInformation.setUserInterfaceLinkCode(UI_LINK_CLAIM_UNIT_RESERVE);
					}
				}  else if(baseToolInformation.getAgreementSubTypeCodeDerived().equals(CWSClaimConstants.CLAIM_COVERAGE_SUBTYPE)) {
					CALClaimReserveDTO reserveDetails = mClaimReserves.get(identifier);
					if(reserveDetails != null) {
						baseToolInformation.setAgreementSubTypeIdDerived(reserveDetails.getClaimCoverageId());
						baseToolInformation.setUserInterfaceLinkCode(UI_LINK_CLAIM_UNIT_COVERAGE);
					}
				}
			}
		}
	}
	
	/**
	 * Save Related Claims
	 * @param importCompositeDTO
	 * @param externalInterfaceMerge SimpleBeanMerge instance to merge DTO's
	 */
	public void saveRelatedClaims(ClaimImportCompositeDTO importCompositeDTO, ExternalInterfaceBeanMerge externalInterfaceMerge) {
		RelatedClaimService relatedClaimService = MuleServiceFactory.getService(RelatedClaimService.class);
		Collection<RelatedClaimDTO> relatedClaimsToSave = new ArrayList<RelatedClaimDTO>();
		for (RelatedClaimDTO relatedClaimDTO : importCompositeDTO.getRelatedClaims()) {
			
			relatedClaimDTO.setUpdated(true);
			
			RelatedClaimDTO existingRelatedClaimDTO = new RelatedClaimDTO();
			Long recordIdVerified = null;
			ClaimSearchResultDTO claimSearch = null;
			
			ClaimEntitySearchResultDTO clmEntySrchResultDTO = findRecordIdAndClaimSearchResult(relatedClaimDTO, 
					relatedClaimDTO.getRecordId(), RelatedClaimBO.class, importCompositeDTO.getClaimId());
			if(clmEntySrchResultDTO != null) {
				recordIdVerified = clmEntySrchResultDTO.getEntityRecordId();
				claimSearch = clmEntySrchResultDTO.getClaimSearchResult();
			}
			if(claimSearch == null || claimSearch.getRecordId() == null) {
				throw new IIPCoreSystemException("Unable to import Related Claims. Can not identify the claim under which to setup this policy.");
			}
			
			if (recordIdVerified == null) {
				existingRelatedClaimDTO = relatedClaimDTO;
				existingRelatedClaimDTO.setClaimId(importCompositeDTO.getClaimId());
				if(!relatedClaimDTO.isExternalClaimFlag()){					
					ClaimSearchCriteriaDTO criteria = new ClaimSearchCriteriaDTO();
					criteria.setClaimNumber(relatedClaimDTO.getExternalClaimNumber());
					//SearchResultsDTO result = getClaimsService().searchClaim(criteria);

					ClaimSearchResultDTO resultDTO = searchClaim(importCompositeDTO);
					existingRelatedClaimDTO.setExternalClaimNumber(resultDTO.getClaimNumber());
					existingRelatedClaimDTO.setRelatedClaimId(resultDTO.getRecordId());
					existingRelatedClaimDTO.setExternalClaimInsuredName(resultDTO.getInsuredName());
				}
			} else {
				existingRelatedClaimDTO=relatedClaimDTO;
			}
			relatedClaimsToSave.add(existingRelatedClaimDTO);
			relatedClaimService.saveRelatedClaim(existingRelatedClaimDTO);
		}
	}

	/**
	 * Save Claim Recovery
	 * @param claimRecovery
	 */
	public void saveClaimRecoveries(ClaimRecovery claimRecovery) {
		ClaimReserveRecoveryService claimReserveRecoveryService = MuleServiceFactory.getService(ClaimReserveRecoveryService.class);
		//try {
			claimReserveRecoveryService.createAndApplyRecoveriesFromExternalSystem(claimRecovery);
		/*} catch(IIPCoreSystemException iipe) {
			if(iipe.getMessage() != null
					&& iipe.getMessage().startsWith("Unable to import Claim Recovery. Unable to find reserve.")) {
				
				//Reserve doesn't exist so create a new reserve and close it.
				createReserveForFastTrack(claimRecovery);
			} else {
				throw iipe;
			}
		}*/
	}
	
	/**
	 * If Company Org Unit Id is not passed for import then retrieve Company Org Unit Id configured for the User.
	 * If more than one Company Org Unit is configured for the User then the first value returned will be used.  
	 * @param assignmentDTO
	 * @param companyLOBId
	 * @param clientEnterpriseConfigDAO
	 */
	public void setCompanyOrgUnitIdForAssignments(ClientImportAssignmentDTO assignmentDTO, Long companyLOBId, 
			ClientEnterpriseConfigDAO clientEnterpriseConfigDAO) {
		
		if(assignmentDTO.getCompanyOrgUnitId() == null) {
			if(mCompanyOrgUnitIdForUser.containsKey(assignmentDTO.getUserId())) {
				assignmentDTO.setCompanyOrgUnitId(mCompanyOrgUnitIdForUser.get(assignmentDTO.getUserId()));
			} else {
				Collection<CompanyOrganizationUnitDTO> companyOrgUnitsIds;
				if (assignmentDTO.getFunctionalRoleCode() != null
						&& assignmentDTO.getUserId() != null
						&& assignmentDTO.getCompanyOrgUnitId() == null) {
					
					companyOrgUnitsIds = new ArrayList<CompanyOrganizationUnitDTO>();
					
					Collection<CodeLookupBO> companyOrgUnits =
							clientEnterpriseConfigDAO.retrieveCompOrgUnitForFunctionalRolesAndUser(
									assignmentDTO.getFunctionalRoleCode(), assignmentDTO.getUserId());
					
					for(CodeLookupBO codeLookup : companyOrgUnits) {
						CompanyOrganizationUnitDTO companyOrganizationUnitDTO = new CompanyOrganizationUnitDTO();
						companyOrganizationUnitDTO.setRecordId(new Long(codeLookup.getCode().toString()));
						companyOrgUnitsIds.add(companyOrganizationUnitDTO);
					}
				} else {
					companyOrgUnitsIds = 
						getEnterpriseConfigService().retrieveAllOrgUnitsOfSelectedUser(assignmentDTO.getUserId());
				}

				if(companyOrgUnitsIds == null || companyOrgUnitsIds.isEmpty()) {
					throw new IIPCoreSystemException("Unable to import Assigments for Claim. Company Org Unit Id is not configured for user " + assignmentDTO.getUserId());
				} else {
					if(companyId == null) {
						ProductDefinitionInfo definition = new ProductDefinitionInfo();
						definition.setCompanyLOBId(companyLOBId);
						definition = getEnterpriseConfigService().getCompanyId(definition);
						companyId = definition.getCompanyId();
					}
					
					if(corporationId == null) {
						Collection<CodeLookupBO> codeLookupBOs = getEnterpriseConfigService().retrieveCorporationByCompanyId(companyId);
						corporationId = new Long(codeLookupBOs.iterator().next().getCode().toString());
					}
					checkIfCompanyOrgValidForCompany(companyLOBId, companyOrgUnitsIds,
							assignmentDTO.getUserId(), clientEnterpriseConfigDAO);
					
					assignmentDTO.setCompanyOrgUnitId(mCompanyOrgUnitIdForUser.get(assignmentDTO.getUserId()));
				}
			}
		}
	}
	
	/**
	 * Check if the Company Org Unit Id for the User is valid for the Company LOB ID
	 * @param companyLOBId
	 * @param companyOrgUnitsIds as Collection<CompanyOrganizationUnitDTO>
	 * @param userId
	 * @param clientEnterpriseConfigDAO
	 */
	private void checkIfCompanyOrgValidForCompany(Long companyLOBId, Collection<CompanyOrganizationUnitDTO> companyOrgUnitsIds, Long userId,
			ClientEnterpriseConfigDAO clientEnterpriseConfigDAO) {
		
		boolean companyOrgUnitAvailable = false;
		
		for(CompanyOrganizationUnitDTO companyOrganizationUnitDTO : companyOrgUnitsIds) {
			Long companyOrgUnitId = companyOrganizationUnitDTO.getRecordId();
			Collection<SysAdminCompanyOrgUnitDefinitionDTO> compOrgUnitDefs = 
				clientEnterpriseConfigDAO.retrieveCorpCompanyForCompanyOrgUnit(companyOrgUnitId);
			for(SysAdminCompanyOrgUnitDefinitionDTO companyOrgUnitDefinitionDTO : compOrgUnitDefs) {
				if(companyOrgUnitDefinitionDTO.getCompanyId() == null) {
					if(companyOrgUnitDefinitionDTO.getCorporationId().equals(corporationId)) {
						companyOrgUnitAvailable = true;
						mCompanyOrgUnitIdForUser.put(userId, companyOrgUnitId);
						return;
					}
				} else {
					if(companyOrgUnitDefinitionDTO.getCompanyId().equals(companyId)) {
						companyOrgUnitAvailable = true;
						mCompanyOrgUnitIdForUser.put(userId, companyOrgUnitId);
						return;
					}
				}
			}
		}
		
		if(!companyOrgUnitAvailable) {
			throw new IIPCoreSystemException("Unable to import Claim. Company/Corporation is not valid for the " +
					"Company Org Unit configured for user " + userId);
		}
	}

	/**
	 * If Functional Role Code is not passed for import then retrieve Functional Role Code configured for the User.
	 * If more than one Functional Role Code is configured for the User then the first value returned will be used.  
	 * @param assignmentDTO
	 * @param clientEnterpriseConfigDAO
	 */
	public void setFunctionalRoleCodeForAssignments(ClientImportAssignmentDTO assignmentDTO,
			ClientEnterpriseConfigDAO clientEnterpriseConfigDAO) {
		if(assignmentDTO.getFunctionalRoleCode() == null) {
			if(mFunctionalRoleCodeForUser.containsKey(assignmentDTO.getUserId())) {
				assignmentDTO.setFunctionalRoleCode(mFunctionalRoleCodeForUser.get(assignmentDTO.getUserId()));
			} else {
				Collection<CodeLookupBO> functionalRoleCodes =
					clientEnterpriseConfigDAO.retrieveFunctionalRolesForUserAndCompOrgUnit(assignmentDTO.getUserId(), 
						assignmentDTO.getCompanyOrgUnitId());
				
				if(functionalRoleCodes == null || functionalRoleCodes.isEmpty()) {
					throw new IIPCoreSystemException("Unable to import Assigments for Claim. Functional Role Code is not configured for user " + assignmentDTO.getUserId());
				} else {
					CodeLookupBO functionalRoleCode = functionalRoleCodes.iterator().next();
					assignmentDTO.setFunctionalRoleCode(functionalRoleCode.getCode().toString());
					mFunctionalRoleCodeForUser.put(assignmentDTO.getUserId(), assignmentDTO.getFunctionalRoleCode());
				}
			}
		}
	}

	/**
	 * If Company Org Unit Id is not passed for import then retrieve Company Org Unit Id configured for the User.
	 * If more than one Company Org Unit is configured for the User then the first value returned will be used.  
	 * @param workItemDTO
	 * @param companyLOBId
	 * @param clientEnterpriseConfigDAO
	 */
	public void setCompanyOrgUnitIdForWorkItems(ClientWorkItemDTO workItemDTO, Long companyLOBId, 
			ClientEnterpriseConfigDAO clientEnterpriseConfigDAO) {
		Collection<WorkItemAssignmentDTO> workItemAssignments = workItemDTO.getWorkItemAssignment();
		if(workItemAssignments != null && !workItemAssignments.isEmpty()) {
			for(WorkItemAssignmentDTO workItemAssignmentDTO : workItemAssignments) {
				if(workItemAssignmentDTO.getWorkItemOrganizationUnit() == null) {
					if(mCompanyOrgUnitIdForUser.containsKey(workItemAssignmentDTO.getWorkItemUserId())) {
						workItemAssignmentDTO.setWorkItemOrganizationUnit(
								mCompanyOrgUnitIdForUser.get(workItemAssignmentDTO.getWorkItemUserId()));
					} else {
						Collection<CompanyOrganizationUnitDTO> companyOrgUnitsIds = 
							getEnterpriseConfigService().retrieveAllOrgUnitsOfSelectedUser(workItemAssignmentDTO.getWorkItemUserId());
						
						if(companyOrgUnitsIds == null || companyOrgUnitsIds.isEmpty()) {
							throw new IIPCoreSystemException("Unable to import Work Items for Claim. Company Org Unit Id is not configured for user " + 
									workItemAssignmentDTO.getWorkItemUserId());
						} else {
							if(companyId == null) {
								ProductDefinitionInfo definition = new ProductDefinitionInfo();
								definition.setCompanyLOBId(companyLOBId);
								definition = getEnterpriseConfigService().getCompanyId(definition);
								companyId = definition.getCompanyId();
							}
							
							if(corporationId == null) {
								Collection<CodeLookupBO> codeLookupBOs = getEnterpriseConfigService().retrieveCorporationByCompanyId(companyId);
								corporationId = new Long(codeLookupBOs.iterator().next().getCode().toString());
							}
							
							checkIfCompanyOrgValidForCompany(companyLOBId, companyOrgUnitsIds,
									workItemAssignmentDTO.getWorkItemUserId(), clientEnterpriseConfigDAO);
							workItemAssignmentDTO.setWorkItemOrganizationUnit(mCompanyOrgUnitIdForUser.get(workItemAssignmentDTO.getWorkItemUserId()));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Retrieve Claim information for Claim Id in ClaimImportCompositeDTO.
	 * @param claimImportCompositeDTO
	 */
	public void retrieveClaim(ClaimImportCompositeDTO claimImportCompositeDTO) {
		claimImportCompositeDTO.setClaimDTO(
				(ClaimDTO)getClaimsService().retrieveDTOOfClaim(claimImportCompositeDTO.getClaimId(), ClaimDTO.class));
		
	}
	
	/**
	 * @return the claimReserveService
	 */
	public ClaimReserveService getClaimReserveService() {
		if(claimReserveService == null){
			claimReserveService = MuleServiceFactory.getService(ClaimReserveService.class);
		}
		return claimReserveService;
	}

	/**
	 * Util method to create an instance of ClaimUnitParticipantCommonDTO
	 * @param payable ClaimPayable.
	 * @return ClaimUnitParticipantCommonDTO
	 */
	private ClaimUnitParticipantCommonDTO createClaimUnitParticipantCommonDTO(ClaimPayable payable){
		ClaimUnitParticipantCommonDTO claimUnitParticipantCommonDTO = new ClaimUnitParticipantCommonDTO();
		claimUnitParticipantCommonDTO.setUnitId(payable.getUnitId());
		claimUnitParticipantCommonDTO.setClaimId(payable.getClaimId());
		claimUnitParticipantCommonDTO.setParticipantId(payable.getParticipantId());
		claimUnitParticipantCommonDTO.setExternalClaimIdentifier(payable.getExternalClaimIdentifier());
		claimUnitParticipantCommonDTO.setExternalUnitIdentifier(payable.getExternalUnitIdentifier());
		claimUnitParticipantCommonDTO.setExternalParticipantIdentifier(payable.getExternalParticipantIdentifier());
		claimUnitParticipantCommonDTO.setReseveType(payable.getClmReserveTypeCode());
		claimUnitParticipantCommonDTO.setLossType(payable.getClmLossTypeCode());
		claimUnitParticipantCommonDTO.setCoverageCode(payable.getClmCoverageCode());
		return claimUnitParticipantCommonDTO;
	}
	
	Long claimPolicyUnitId = null;
	
	/**
	 * Check if Payment is valid and the reserve associated is valid
	 * @param claimPayable
	 * @param importUtilPojo
	 * @param claimUnitDAO
	 * @return ClaimReserveCommonDTO
	 */
	private ClaimReserveCommonDTO checkIfValidPaymentAndReturnCommonData(ClaimPayable claimPayable, 
			ImportUtilPojo importUtilPojo, ClaimUnitDAO claimUnitDAO) {
		
		ClaimUnitParticipantCommonDTO commonDTO = createClaimUnitParticipantCommonDTO(claimPayable);
		StringBuffer invalidResult = new StringBuffer();
		
		importUtilPojo.isValidClaimIdOrExternalClaimId(commonDTO, invalidResult);
		importUtilPojo.isValidUnitIdOrExternalUnitId(commonDTO, invalidResult);
		importUtilPojo.isValidparticipantIdOrExternalParticiapntd(commonDTO, invalidResult);
		claimPayable.setUnitId(commonDTO.getUnitId());
		claimPayable.setParticipantId(commonDTO.getparticipantId());
		claimPayable.setClaimId(commonDTO.getClaimId());
		
		int errorMessage = invalidResult.length();
		if(errorMessage > 0){
			throw new IIPCoreSystemException("unable to import claim payment transaction for " +  invalidResult.toString());
		}
		
		ClaimUnitAndParticipantNestedResultData data = getClaimReserveService().retrieveClaimUnitParticipantInfo(commonDTO);
		if(data == null) {
			throw new IIPCoreSystemException("Unable to import Claim Disbursement Transactions as the combination for unit, particpant and claim external id could not be found");
		}
		
		CALClaimParticipationBO claimParticipantBO = claimUnitDAO.retrieveUnitParticipantBO(commonDTO.getparticipantId());
		if(claimParticipantBO.isParticipantUnknown()) {
			throw new IIPCoreSystemException("Unable to import Claim as Payment cannot be made to an Unknown Participant. Please pass a valid participant");
		}
		
		data.setCoverageCode(claimPayable.getClmCoverageCode());
		data.setReserveTypeCode(claimPayable.getClmReserveTypeCode());
		data.setLossTypeCode(claimPayable.getClmLossTypeCode());
		claimPayable.setUnitId(commonDTO.getUnitId());
		claimPayable.setParticipantId(commonDTO.getparticipantId());
		Boolean isValidReserveAndLossType = getClaimReserveService().isValidReserveAndLossType(data);
		if (!isValidReserveAndLossType){
			throw new IIPCoreSystemException("Unable to import Claim Disbursement as the combination is not correct for this Disbursement Transaction for unit Type Code" + data.getUnitTypeCode() + ","  + " unit Sub Type" + data.getUnitSubType() + ","  + "company Lob Id" + data.getCompLobId() + ","
					+ "participant type" + data.getParticipantType() + "," +" coverage code" + data.getCoverageCode() + "," + "reserve type code" + data.getReserveTypeCode() + "," + "loss type code" + data.getLossTypeCode());
		}

		ClaimReserveCommonDTO claimReserveCommonDTO = null;

		//The data to search for the reserve is correct, check to see if some reserve exists with the same combination.
		if (claimPayable.getClmCoverageCode() != null && claimPayable.getParticipantId() != null 
				&& claimPayable.getUnitId() != null && claimPayable.getClmLossTypeCode() != null 
				&& claimPayable.getClmReserveTypeCode() != null){

			ImportReserveCriteria criteria = createImportReserveCriteria(claimPayable);
			claimReserveCommonDTO = getClaimReserveService().isReserveExist(criteria);
			
			claimPolicyUnitId = data.getClaimPolicyUnitId();
			if(claimReserveCommonDTO != null) {
				claimReserveCommonDTO.setClaimPolicyUnitId(data.getClaimPolicyUnitId());
			}
		} else {
			throw new IIPCoreSystemException("Unable to import Claim. Unable to find reserve. Please check the data .");
		}
		return claimReserveCommonDTO;
	}
	
	/**
	 * Checks if reserve exists for the given Claim Payable. If it doesn't exist create a reserve.
	 * @param claimPayable Payment for which reserve needs to be checked
	 * @param team Team for whom the payment is created 
	 * @param userName User for whom the payment is created 
	 * @param importUtilPojo Util class to check whether Unit or Participant is valid and to retrieve 
	 * 			the corresponding record IDs
	 * @return ClaimPayable updated with Unit Id, Participant Id
	 */
	public ClaimPayable createReserveIfNotAvailable(ClaimPayment claimPayment, ClaimPayable claimPayable, 
			ImportUtilPojo importUtilPojo, ClaimUnitDAO claimUnitDAO) {
		
		Long team = claimPayment.getTeamId();
		String userName = claimPayment.getUserName();

		ClaimReserveCommonDTO claimReserveCommonDTO = checkIfValidPaymentAndReturnCommonData(claimPayable, importUtilPojo, claimUnitDAO);

		if(claimReserveCommonDTO == null) {
			//check if impact incurred flag is 'y'. If it is 'n' the open reserve with zero amount.
			boolean bImpactIncurredIndicator = isImpactIncurred(claimPayable);

			createReserveForFastTrack(claimPayable, team, userName, claimPolicyUnitId, bImpactIncurredIndicator);
			claimPayment.setOperationTypeForPayment(ClaimReserveConstants.OPERATION_TYPE_FAST_TRACK);
		} else {
			Boolean reserveClosed = getClaimReserveService().isReserveClosed(claimReserveCommonDTO.getReserveId());
			if(reserveClosed) {
				
				//check if impact incurred flag is 'y'. If it is 'n' the reopen reserve with zero amount.
				boolean bImpactIncurredIndicator = isImpactIncurred(claimPayable);
				
				//reopen reserve
				reopenClosedReserveForPayment(claimPayable, team, userName, 
						claimReserveCommonDTO.getClaimPolicyUnitId(), claimReserveCommonDTO.getReserveId(), bImpactIncurredIndicator);

				claimPayment.setOperationTypeForPayment(ClaimReserveConstants.OPERATION_TYPE_ISSUE_PAYMENT_AFTER_CLOSE);
				claimPayable.setCloseReserveIndicator(true);
			}
		}

		return claimPayable;
	}
	
	private boolean isImpactIncurred(ClaimPayable claimPayable) {
		//check if impact incurred flag is 'y'. If it is 'y' the reopen reserve with zero amount.
		ImpactIncurredCriteria impactIncurredCriteria = new ImpactIncurredCriteria();
	
		impactIncurredCriteria.setMonetoryCatType(claimPayable.getMonetaryCategoryCode());
		impactIncurredCriteria.setReserveType(claimPayable.getClmReserveTypeCode());
		impactIncurredCriteria.setPaymentType(claimPayable.getMonitoryTypeCode());
		impactIncurredCriteria.setClaimId(claimPayable.getClaimId());
		
		String impactIndicator = MuleServiceFactory.getService(ClaimReservePaymentService.class).retrieveImpactIncurredIndicator(impactIncurredCriteria);
		boolean bImpactIncurredIndicator = true;
		if(impactIndicator != null && impactIndicator.equalsIgnoreCase("n")) {
			bImpactIncurredIndicator = false;
		}
		
		return bImpactIncurredIndicator;
	}

	/**
	 * Util method to create Reserve Criteria for a checking whether a reserve exists or not.
	 * @param objFinancialObject DTO from which to create ImportReserveCriteria.
	 * @return ImportReserveCriteria
	 */
	private ImportReserveCriteria createImportReserveCriteria(DataTransferObject objFinancialObject) {
		
		ImportReserveCriteria criteria = new ImportReserveCriteria();
		if(objFinancialObject instanceof ClaimPayable) {
			ClaimPayable claimPayable = (ClaimPayable) objFinancialObject;
			criteria.setParticipantId(claimPayable.getParticipantId());
			criteria.setUnitId(claimPayable.getUnitId());
			criteria.setCoverageCode(claimPayable.getClmCoverageCode());
			criteria.setLossType(claimPayable.getClmLossTypeCode());
			criteria.setReserveType(claimPayable.getClmReserveTypeCode());
		} else if (objFinancialObject instanceof CALClaimReserveDTO) {
			CALClaimReserveDTO calClaimReserve = (CALClaimReserveDTO)objFinancialObject;
			criteria.setParticipantId(calClaimReserve.getCalClaimPtcpId());
			criteria.setUnitId(calClaimReserve.getCalClaimUnitIdDerived());
			criteria.setCoverageCode(calClaimReserve.getClmCoverageCode());
			criteria.setLossType(calClaimReserve.getClmLossTypeCode());
			criteria.setReserveType(calClaimReserve.getClmReserveTypeCode());
		}
		
		return criteria;
	}
	
	/**
	 * Loop thru' all participants in the Unit and set updated flag to true if Legal Rep Services is available
	 * @param claimUnit
	 */
	private void checkIfLegalRepAvailable(ClaimUnitDTO claimUnit) {
		if(claimUnit instanceof ClaimUnitVehicleDTO) {
			ClaimUnitVehicleDTO claimUnitVehicle = (ClaimUnitVehicleDTO) claimUnit;
			setUpdatedFlagTrueForDamageServicesDTO(claimUnitVehicle.getDrivers());
			setUpdatedFlagTrueForDamageServicesDTO(claimUnitVehicle.getPassengers());
		}
		if(claimUnit.getClaimDamageDTO() != null ) {
			setUpdatedFlagTrueForDamageServicesDTO(claimUnit.getClaimDamageDTO().getDamageServicesDTO());
		}
		if(claimUnit.getClaimSalvageDTO() != null) {
			setUpdatedFlagTrueForDamageServicesDTO(claimUnit.getClaimSalvageDTO().getSalvageSalvorsDTO());
		}
		setUpdatedFlagTrueForDamageServicesDTO(claimUnit.getFinancialInterestList());

		setUpdatedFlagTrueForDamageServicesDTO(claimUnit.getInjuredPersonList());
		setUpdatedFlagTrueForDamageServicesDTO(claimUnit.getOtherCarriersList());
		setUpdatedFlagTrueForDamageServicesDTO(claimUnit.getOtherPartiesList());

		setUpdatedFlagTrueForDamageServicesDTO(claimUnit.getOwners());
	}
	
	/**
	 * If we are saving Legal Rep Services (DTO used is ClaimDamageServicesDTO) then updated flag needs to be set to true
	 * so that data can be saved in tables
	 * @param lstParticipantDTO
	 */
	private void setUpdatedFlagTrueForDamageServicesDTO(Collection<? extends CALClaimParticipationDTO> lstParticipantDTO) {
		if(lstParticipantDTO != null && !lstParticipantDTO.isEmpty()) {
			for(CALClaimParticipationDTO participantDTO : lstParticipantDTO) {
				if(participantDTO.getPtcpLevelServices() != null 
						&& !participantDTO.getPtcpLevelServices().isEmpty()) {
					for(ClaimDamageServicesDTO claimService : participantDTO.getPtcpLevelServices()) {
						claimService.setUpdated(true);
					}
				}
			}
		}
	}	
}