package com.client.iip.integration.sa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.bc.address.api.PartyCorrespondenceDTO;
import com.fiserv.isd.iip.core.dto.DTOUtils;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Service;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupService;
import com.stoneriver.iip.claims.lookup.ClmPlcyEndorsementFormCodeLookupDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyUnitCoverageDTO;
import com.stoneriver.iip.claims.sa.api.unitdefinition.ClaimCompanyLineOfBusinessInfoDTO;
import com.stoneriver.iip.claims.sa.api.unitdefinition.ClaimCompanyReserveTypesDTO;
import com.stoneriver.iip.claims.sa.api.unitdefinition.ClaimsMonetaryTypesDTO;
import com.stoneriver.iip.document.render.CompanyRelatedInformation;
import com.stoneriver.iip.entconfig.EnterpriseConfigConstants;
import com.stoneriver.iip.entconfig.batch.BatchLogDTO;
import com.stoneriver.iip.entconfig.batch.BatchLogDetailDTO;
import com.stoneriver.iip.entconfig.sa.api.SysAdminCompanyAddressCompositeDTO;
import com.stoneriver.iip.entconfig.sa.api.SysAdminCompanyAddressDTO;
import com.stoneriver.iip.entconfig.sa.api.SysAdminCompanyDTO;
import com.stoneriver.iip.entconfig.sa.api.codes.ProfileStatementCodeDTO;

/**
 * Implementation of Get configuration service to retrieve details from Claim Sysadmin
 * 
 * @author Gaurav Rai
 *
 */

@Service(id = "integration.serviceObject.ClientConfigService")
public class ClientConfigServiceImpl implements ClientConfigService {

	private final Logger logger = LoggerFactory.getLogger(ClientConfigServiceImpl.class);
	private ClientSysAdminClaimsDAO sysAdminClaimsDao;
	private CodeLookupService codeLookupService = null;
	Collection<CodeLookupBO> monetaryTypeCodes;

	ClientEnterpriseConfigDAO clientEnterpriseConfigDAO = null;
	/**
	 * @return clientClaimStatusDAO
	 */
	public ClientEnterpriseConfigDAO getClientEnterpriseConfigDAO() {
		return clientEnterpriseConfigDAO;
	}

	/**
	 * @param value
	 *            Client Enterprise Config DAO to set
	 */
	@Inject(DaoInterface = "client.daointerface.clientEntConfigDao")
	public void setClientClaimStatusDAO(ClientEnterpriseConfigDAO value) {
		this.clientEnterpriseConfigDAO = value;
	}
	
	/**
	 * @return the sysAdminClaimsDao
	 */
	public ClientSysAdminClaimsDAO getSysAdminClaimsDao() {
		return sysAdminClaimsDao;
	}

	/**
	 * @param value
	 *            the sysAdminClaimsDao to set
	 */
	@Inject(DaoInterface = "client.daointerface.clientSysadminClaimsDao")
	public void setSysAdminClaimsDao(ClientSysAdminClaimsDAO value) {
		this.sysAdminClaimsDao = value;
	}	
	
	
	
	/**
	 * To retrieve Claim and Company LOB Info.
	 * 
	 * @param dummyId
	 *            The dummyId.
	 * @return ClientClaimCompanyLOBCompositeDTO The
	 *         ClientClaimCompanyLOBCompositeDTO.
	 */

	public Collection<ClientClaimCompanyLineOfBusinessInfoDTO> retrieveClaimCompanyLOBInfo(String dummy) {

		Collection<ClientClaimCompanyLineOfBusinessInfoDTO> clientClaimCmpyLOBDetails = null;

		try {
			//Refactored to fetch all information in a single query.
			clientClaimCmpyLOBDetails = getSysAdminClaimsDao()
					.retrieveClaimCompanyLOBInfo();

		} catch (Exception e) {
			String errorMessage = "Error occurred retrieving Claim Company LOB Information: ";
			logger.error(errorMessage, e);
			throw new IIPCoreSystemException(errorMessage);
		}

		return clientClaimCmpyLOBDetails;
	}

	/**
	 * To retrieve reserve types without monetary codes list
	 * 
	 * @param dummyId
	 *            The dummyId.
	 * @return ClaimCompanyReserveTypesDTO
	 */
	@SuppressWarnings("unchecked")
	public Collection<ClaimCompanyReserveTypesDTO> retrieveReserveTypes(String dummy) {

		Collection<ClaimCompanyReserveTypesDTO> claimCompanyReserveTypesDTOs = new ArrayList<ClaimCompanyReserveTypesDTO>();

		Collection<ClientClaimCompanyReserveTypesBO> reserveTypesBOColl = getSysAdminClaimsDao().retrieveReserveTypes();
		Collection<CodeLookupBO> claimReserveTypeColl = getSysAdminClaimsDao().retrieveClaimReserveType();
		Collection<ClientClaimCompanyLineOfBusinessInfoDTO> claimCmpyLOBDetails = getSysAdminClaimsDao()
				.retrieveClaimCompanyLOBInfo();

		for (ClientClaimCompanyReserveTypesBO reserveTypesBO : reserveTypesBOColl) {
			ClaimCompanyReserveTypesDTO reserveTypesDTO = DTOUtils.retrieveDTOHierarchy(reserveTypesBO,
					ClaimCompanyReserveTypesDTO.class);

			for (CodeLookupBO bo : claimReserveTypeColl) {
				if (((String) bo.getCode()).equals(reserveTypesDTO.getReservTypeCd())) {
					reserveTypesDTO.setReservTypeName(bo.getValue());
				}
			}

			for (ClaimCompanyLineOfBusinessInfoDTO lineOfBusinessInfo : claimCmpyLOBDetails) {
				if (reserveTypesDTO.getCompLOBId().equals(lineOfBusinessInfo.getCompanyLOBId())) {
					reserveTypesDTO.setCompId(lineOfBusinessInfo.getCompId());
					reserveTypesDTO.setCorpId(lineOfBusinessInfo.getCorpId());
					reserveTypesDTO.setCompName(lineOfBusinessInfo.getCompany());
					reserveTypesDTO.setCorpName(lineOfBusinessInfo.getCorporation());
					reserveTypesDTO.setCompLOBName(lineOfBusinessInfo.getCompanyLOB());
					reserveTypesDTO.setEnterpriseLOB(lineOfBusinessInfo.getEnterpriseLOB());
				}
			}
			if (reserveTypesDTO.getMonetaryTypeColl() != null && reserveTypesDTO.getMonetaryTypeColl().size() > 0) {
				reserveTypesDTO.getMonetaryTypeColl().clear();
			}
			claimCompanyReserveTypesDTOs.add(reserveTypesDTO);
		}
		Collections.sort((ArrayList<ClaimCompanyReserveTypesDTO>) claimCompanyReserveTypesDTOs);

		return claimCompanyReserveTypesDTOs;
	}

	/**
	 * To retrieve reserve types with monetary codes list
	 * 
	 * @param dummyId
	 *            The dummyId.
	 * @return ClaimCompanyReserveTypesDTO
	 */

	@SuppressWarnings("unchecked")
	public Collection<ClaimCompanyReserveTypesDTO> retrieveReserveMonetaryTypes(String dummy) {

		Collection<ClaimCompanyReserveTypesDTO> claimCompanyReserveTypesDTOs = new ArrayList<ClaimCompanyReserveTypesDTO>();

		try {
			Collection<ClientClaimCompanyReserveTypesBO> reserveTypesBOColl = getSysAdminClaimsDao().retrieveReserveTypes();
			Collection<CodeLookupBO> claimReserveTypeColl = sysAdminClaimsDao.retrieveClaimReserveType();
			Collection<ClientClaimCompanyLineOfBusinessInfoDTO> claimCmpyLOBDetails = getSysAdminClaimsDao()
					.retrieveClaimCompanyLOBInfo();
			
			for (ClientClaimCompanyReserveTypesBO reserveTypesBO : reserveTypesBOColl) {
				ClaimCompanyReserveTypesDTO reserveTypesDTO = DTOUtils.retrieveDTOHierarchy(reserveTypesBO,
						ClaimCompanyReserveTypesDTO.class);

				for (CodeLookupBO bo : claimReserveTypeColl) {
					if (((String) bo.getCode()).equals(reserveTypesDTO.getReservTypeCd())) {
						reserveTypesDTO.setReservTypeName(bo.getValue());
					}
				}

				for (ClaimCompanyLineOfBusinessInfoDTO lineOfBusinessInfo : claimCmpyLOBDetails) {
					if (reserveTypesDTO.getCompLOBId().equals(lineOfBusinessInfo.getCompanyLOBId())) {
						reserveTypesDTO.setCompId(lineOfBusinessInfo.getCompId());
						reserveTypesDTO.setCorpId(lineOfBusinessInfo.getCorpId());
						reserveTypesDTO.setCompName(lineOfBusinessInfo.getCompany());
						reserveTypesDTO.setCorpName(lineOfBusinessInfo.getCorporation());
						reserveTypesDTO.setCompLOBName(lineOfBusinessInfo.getCompanyLOB());
						reserveTypesDTO.setEnterpriseLOB(lineOfBusinessInfo.getEnterpriseLOB());
					}
				}

				Collection<ClaimsMonetaryTypesDTO> monetaryTypesDTO = reserveTypesDTO.getMonetaryTypeColl();
				//If Monetary Type Collection is null then retrieve from ClientClaimCompanyReserveTypesBO Monetary Type Collection
				if(monetaryTypesDTO == null && reserveTypesBO.getMonetaryTypeColl() != null) {
					Collection<ClaimsMonetaryTypesDTO> monetaryTypeColl = 
						DTOUtils.retrieveDTO(reserveTypesBO.getMonetaryTypeColl(), ClaimsMonetaryTypesDTO.class);
					reserveTypesDTO.setMonetaryTypeColl(monetaryTypeColl);
					monetaryTypesDTO = monetaryTypeColl;
				}
				
				if(monetaryTypesDTO != null) {
					Collection<ClaimsMonetaryTypesDTO> clientMonetaryTypesDTO = new ArrayList<ClaimsMonetaryTypesDTO>(
							monetaryTypesDTO.size());
					for (Iterator<ClaimsMonetaryTypesDTO> iterator = monetaryTypesDTO.iterator(); iterator.hasNext();) {
						ClaimsMonetaryTypesDTO claimsMonetaryTypesDTO = (ClaimsMonetaryTypesDTO) iterator.next();
						ClientClaimsMonetaryTypesDTO clientClaimsMonetaryTypesDTO = new ClientClaimsMonetaryTypesDTO();
						BeanUtils.copyProperties(clientClaimsMonetaryTypesDTO, claimsMonetaryTypesDTO);
						String monetaryTypeCodeValue = findMonetaryTypeCodeValue(clientClaimsMonetaryTypesDTO.getMonetaryTypeCd());
						clientClaimsMonetaryTypesDTO.setMonetaryTypeName(monetaryTypeCodeValue);
						clientMonetaryTypesDTO.add(clientClaimsMonetaryTypesDTO);
					}
					reserveTypesDTO.setMonetaryTypeColl(clientMonetaryTypesDTO);
				}
				claimCompanyReserveTypesDTOs.add(reserveTypesDTO);
			}
			Collections.sort((ArrayList<ClaimCompanyReserveTypesDTO>) claimCompanyReserveTypesDTOs);

		} catch (Exception e) {
			String errorMessage = "Error occurred retrieving Reserve Monetary Types Information: " + e.getMessage();
			logger.error(errorMessage);
			throw new IIPCoreSystemException(errorMessage);
		}

		return claimCompanyReserveTypesDTOs;
	}

	private String findMonetaryTypeCodeValue(String code) {
	
		if (monetaryTypeCodes == null) {
			monetaryTypeCodes = getCodeLookupService().findAll("CLAIM_MONETARY_TYPE_CD");
		}
		
		String value = StringUtils.EMPTY;
		for (CodeLookupBO codeLookup : monetaryTypeCodes) {
			if (codeLookup.getCode().equals(code)) {
				value = codeLookup.getValue();
				break;
			}
		}
		return value;
	}

	/**
	 * To retrieve all company organization units information
	 * 
	 * @param dummyId
	 * @return ControlNumberDTO
	 */
	public Collection<SysAdminCompanyOrgUnitDetailDTO> retrieveAllOrgUnits(String dummy) {
		Collection<SysAdminCompanyOrgUnitDetailDTO> orgUnitDetailDTOs = new ArrayList<SysAdminCompanyOrgUnitDetailDTO>();
		orgUnitDetailDTOs = getSysAdminClaimsDao().retrieveAllOrgUnits();
		return orgUnitDetailDTOs;
	}

	/**
	 * Use to retrieve coverages with company, LOB and reserve type
	 * 
	 * @param dummy
	 * @return
	 */
	public Collection<CoverageCompLOBResTypeDTO> retrieveAllCoveragesByComLOBRT(String dummy) {
		Collection<CoverageCompLOBResTypeDTO> compLOBResTypeDTOs = new ArrayList<CoverageCompLOBResTypeDTO>();
		compLOBResTypeDTOs = getSysAdminClaimsDao().retrieveCoveragesCompLOBRTInfo();

		return compLOBResTypeDTOs;
	}

	/**
	 * Use to retrieve loss types by company, LOB, reserve type and coverage
	 * 
	 * @param dummy
	 * @return
	 */
	public Collection<LossTypeCompLOBResCovDTO> retrieveAllLossTypesByCompLOBRTCov(String dummy) {

		Collection<LossTypeCompLOBResCovDTO> lossTypeCompLOBResCovDTOs = new ArrayList<LossTypeCompLOBResCovDTO>();
		lossTypeCompLOBResCovDTOs = getSysAdminClaimsDao().retrieveLossTypesCovCompLOBReserveTypeInfo();
		return lossTypeCompLOBResCovDTOs;

	}
	
	/**
	 * Return a map of all the code tables code/ CodeLookupBO.
	 *
	 * @param codeTableName name of the code table.
	 * @return a map of all code tables code/CodeLookupBO
	 */
	public Collection<CodeLookupBO> retrieveCodeValue(String lookupCode){
			
		logger.info("Going to send = {} ", lookupCode);
		Collection<CodeLookupBO> bos  = getCodeLookupService().findAll(lookupCode);
		
		return bos;
		
	}	
	
	/**
	 * Return a map of Jurisdiction code/ CodeLookupBO.
	 *
	 * @param codeTableName name of the code table.
	 * @return a map of all Jurisdiction codes/CodeLookupBO
	 */
	public Collection<CodeLookupBO> retrieveJurisdictions(String dummy){
		Collection<CodeLookupBO> bos  = getClientEnterpriseConfigDAO().retrieveJurisdictions();
		return bos;
	}
	
	/**
	 * The CodeLookupService
	 * 
	 * @return the CodeLookupService
	 */
	private CodeLookupService getCodeLookupService() {
		
		if(codeLookupService == null) {
			codeLookupService = MuleServiceFactory.getService(CodeLookupService.class);
		}
		return codeLookupService;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.client.iip.integration.sa.ClientConfigService#retrieveClmPlcyEndorsementFormInfoByLOB(java.lang.String)
	 */
	
	@Override
	public Collection<ClmPlcyEndorsementFormCodeLookupDTO> retrieveClmPlcyEndorsementFormInfoByLOB(
			String lobCode) {
		return getSysAdminClaimsDao().retrieveClmPlcyEndorsementFormInfoByLOB(lobCode);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.client.iip.integration.sa.ClientConfigService#retriveClmPlcyUnitDataTypeRoleCategory(java.lang.String)
	 */
	@Override
	public Collection<UnitDataTypeRoleCategoryDTO> retriveClmPlcyUnitDataTypeRoleCategory(
			String lobCode) {
		return getSysAdminClaimsDao().retriveClmPlcyUnitDataTypeRoleCategory(lobCode);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.client.iip.integration.sa.ClientConfigService#retrieveCompanyInfo(java.lang.Long)
	 */
	@Override
	public SysAdminCompanyDTO retrieveCompanyInfo(Long companyID) {

		SysAdminCompanyDTO sysAdminCompanyDTO = new SysAdminCompanyDTO();
		Collection<CompanyRelatedInformation> companyRelatedInfo = getClientEnterpriseConfigDAO().retrieveCompanyRelatedInformation(
				companyID);
		CompanyRelatedInformation companyInfo = (companyRelatedInfo != null && companyRelatedInfo.size() > 0) ? companyRelatedInfo.iterator()
				.next() : new CompanyRelatedInformation();
		
		sysAdminCompanyDTO.setNaicsCode(companyInfo.getNaicsCode());

		Collection<PartyCorrespondenceDTO> companyAddress= getClientEnterpriseConfigDAO().retrieveCompanyAddressInformation(companyID, EnterpriseConfigConstants.COMPANY_ADDR_TYPE_CD_MAIL);
		if(companyAddress.iterator().hasNext()){
			PartyCorrespondenceDTO compAddress = companyAddress.iterator().next();
			SysAdminCompanyAddressCompositeDTO addressComposite = new SysAdminCompanyAddressCompositeDTO();
			SysAdminCompanyAddressDTO address = new SysAdminCompanyAddressDTO();
			address.setAddressLine1(compAddress.getMailAddressLine1());
			address.setCityName(compAddress.getMailAddressCityName());
			address.setPostalSvcRegionCode(compAddress.getMailPostalSvcRegionCode());
			address.setPostalCode(compAddress.getMailPostalCode());
			addressComposite.setAddressForm(address);
			Collection<SysAdminCompanyAddressCompositeDTO> addrColl = new ArrayList<SysAdminCompanyAddressCompositeDTO>();
			addrColl.add(addressComposite);
			sysAdminCompanyDTO.setCompanyAddress(addrColl);
		}
		return sysAdminCompanyDTO;
	}

	@Override
	public ClaimPolicyUnitCoverageDTO retrieveClaimCoverageCodeForPolicyCovCode(
			String policyCoverageCode) {

		ClaimPolicyUnitCoverageDTO claimCoverage = null;
		
		Collection<ClaimPolicyUnitCoverageDTO> coverages = getSysAdminClaimsDao().retrieveClaimCoverageCodeForPolicy(policyCoverageCode);
		
		for(ClaimPolicyUnitCoverageDTO coverage: coverages){
			claimCoverage = coverage;
			break;
		}
		
		return claimCoverage;
	}

	@Override
	public Collection<CodeLookupBO> retrieveFunctionalRolesForUserAndCompOrgUnit(
			Long userId, Long companyOrgUnitId) {
	
		Collection<CodeLookupBO> roles = getClientEnterpriseConfigDAO().retrieveFunctionalRolesForUserAndCompOrgUnit(userId, companyOrgUnitId);
		return roles;
	}

	@Override
	public Collection<UnitDataTypeRoleCategoryDTO> retriveClmPlcyUnitDataTypeByCoverage(
			String coverageCode, Long companyLOBId) {

		Collection<UnitDataTypeRoleCategoryDTO> unitDefinitionList = getSysAdminClaimsDao().retriveClmPlcyUnitDataByCovLOB(coverageCode, companyLOBId);

		return unitDefinitionList;
	}

	@Override
	public Collection<BatchLogDTO> retrieveBatchLog(Long jobId) {
		Collection<BatchLogDTO> btchlogs = getClientEnterpriseConfigDAO().retrieveBatchLog(jobId);
		//Get Batch Log Detail
		for(BatchLogDTO log: btchlogs){
			Collection<BatchLogDetailDTO> batchLogDetailDTOList = getClientEnterpriseConfigDAO().retrieveBatchLogDetailInfo(log.getRecordId());
			log.setBatchLogDetail(batchLogDetailDTOList);
		}
		return btchlogs;		
	}

	@Override
	public Collection<ProfileStatementCodeDTO> retrieveProfileStatementCodes(
			String agreementTypeCode) {
		
		if(StringUtils.isBlank(agreementTypeCode)) {
			return null;
		}
		
		Collection<ProfileStatementCodeDTO> profileStatementCodes = 
				getClientEnterpriseConfigDAO().retrieveProfileStatementsByAgreementType(agreementTypeCode);
		
		return profileStatementCodes;
	}	
	

}
