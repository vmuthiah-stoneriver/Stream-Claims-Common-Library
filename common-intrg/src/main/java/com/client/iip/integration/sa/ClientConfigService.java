package com.client.iip.integration.sa;

import java.util.Collection;

import com.fiserv.isd.iip.core.service.ServiceEndpoint;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.stoneriver.iip.claims.lookup.ClmPlcyEndorsementFormCodeLookupDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyUnitCoverageDTO;
import com.stoneriver.iip.claims.sa.api.unitdefinition.ClaimCompanyReserveTypesDTO;
import com.stoneriver.iip.entconfig.batch.BatchLogDTO;
import com.stoneriver.iip.entconfig.sa.api.SysAdminCompanyDTO;
import com.stoneriver.iip.entconfig.sa.api.codes.ProfileStatementCodeDTO;

/**
 * Composite service used to retrieve details from Claim Sysadmin
 * 
 * @author Gaurav Rai
 * 
 */
@ServiceEndpoint("integration.endpoint.configInboundEndPoint")
public interface ClientConfigService {
	
	/**
	 * 
	 * @param dummy
	 * @return a map of jurisdiction codes/CodeLookupBO
	 */
	Collection<CodeLookupBO> retrieveJurisdictions(String dummy);
	
	
	/**
	 * To retrieve Claim and Company LOB Info.
	 * 
	 * @param dummyId
	 * @return ClaimCompanyLOBCompositeDTO The ClaimCompanyLOBCompositeDTO.
	 */
	Collection<ClientClaimCompanyLineOfBusinessInfoDTO> retrieveClaimCompanyLOBInfo(String dummy);

	/**
	 * Use to retrieve all reserve types.
	 * 
	 * @param dummyId
	 * @return reserveTypesCompositeDTO.
	 */
	Collection<ClaimCompanyReserveTypesDTO> retrieveReserveTypes(String dummy);

	/**
	 * Use to retrieve all monetary types.
	 * 
	 * @param dummyId
	 * @return reserveTypesCompositeDTO.
	 */
	Collection<ClaimCompanyReserveTypesDTO> retrieveReserveMonetaryTypes(String dummy);


	/**
	 * Use to retrieve all organization unit info
	 * 
	 * @param dummy
	 * @return
	 */
	Collection<SysAdminCompanyOrgUnitDetailDTO> retrieveAllOrgUnits(String dummy);

	/**
	 * Use to retrieve coverages with company, LOB and reserve type
	 * 
	 * @param dummy
	 * @return
	 */
	Collection<CoverageCompLOBResTypeDTO> retrieveAllCoveragesByComLOBRT(String dummy);

	/**
	 * Use to retrieve loss types by company, LOB, reserve type and coverage
	 * 
	 * @param dummy
	 * @return
	 */
	Collection<LossTypeCompLOBResCovDTO> retrieveAllLossTypesByCompLOBRTCov(String dummy);
	
	/**
	 * Return a Collection of all the code tables code/ CodeLookupBO.
	 *
	 * @param codeTableName name of the code table.
	 * @return a map of all code tables code/CodeLookupBO
	 */
	Collection<CodeLookupBO> retrieveCodeValue(String lookupCode);
	
	/**
	 * Return a Collection Policy Endorsement Forms for the LOB
	 * 
	 */
	Collection<ClmPlcyEndorsementFormCodeLookupDTO> retrieveClmPlcyEndorsementFormInfoByLOB(String lobCode);
	
	/**
	 * @param lobCode
	 * @return Collection Unit Data Type Roles for the LOB
	 */
	Collection<UnitDataTypeRoleCategoryDTO> retriveClmPlcyUnitDataTypeRoleCategory(String lobCode);
	
	/**
	 * @param coverageCode
	 * @param companyLOBId
	 * @return Collection Unit Data Type Roles for the LOB
	 */
	Collection<UnitDataTypeRoleCategoryDTO> retriveClmPlcyUnitDataTypeByCoverage(String coverageCode, Long companyLOBId);	
	
	/**
	 * 
	 * @param companyID
	 * @return Company Info
	 */
	
	SysAdminCompanyDTO retrieveCompanyInfo(Long companyID);
	
	/**
	 * @param policyCoverageCode
	 * @return ClaimPolicyUnitCoverageDTO
	 */
	ClaimPolicyUnitCoverageDTO retrieveClaimCoverageCodeForPolicyCovCode(String policyCoverageCode);
	
	/**
	 * @param roleCode
	 * @param companyOrgUnitId
	 * @return Collection<CodeLookupBO>
	 */
	Collection<CodeLookupBO> retrieveFunctionalRolesForUserAndCompOrgUnit(Long userId, Long companyOrgUnitId);
	
	/**
	 * @param jobId
	 * @return Collection<BatchLogDTO>
	 */
	Collection<BatchLogDTO> retrieveBatchLog(Long jobId);
	
	/**
	 * @param agreementTypeCode
	 * @return Collection<ProfileStatementCodeDTO>
	 */
	Collection<ProfileStatementCodeDTO> retrieveProfileStatementCodes(String agreementTypeCode);
}
