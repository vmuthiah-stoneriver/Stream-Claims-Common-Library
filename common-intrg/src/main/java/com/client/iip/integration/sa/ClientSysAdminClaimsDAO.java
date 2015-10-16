package com.client.iip.integration.sa;

import java.util.Collection;

import com.fiserv.isd.iip.core.data.annotation.Parameter;
import com.fiserv.isd.iip.core.data.annotation.Query;
import com.fiserv.isd.iip.core.meta.annotation.DaoInterface;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.stoneriver.iip.claims.lookup.ClmPlcyEndorsementFormCodeLookupDTO;
import com.stoneriver.iip.claims.policy.ClaimPolicyUnitCoverageDTO;

/**
 * 
 * @author abhishake.apoorva
 *
 */

@DaoInterface(id = "client.daointerface.clientSysadminClaimsDao", daoInterfaceFactory = "clientDaoInterfaceFactory")
public interface ClientSysAdminClaimsDAO {
		
	/**
	 * Returns all Company Line of Business and Claims Info.
	 * @return Collection of ClaimCompanyLineOfBusinessInfoDTO.
	 * 
	 */
	@Query(accessId="client.claims.sa.query.retrieveClaimCompanyLOBInfo", cache=true)
	Collection<ClientClaimCompanyLineOfBusinessInfoDTO> retrieveClaimCompanyLOBInfo();
	
	/**
	 * Use to retrieve all reserve types and all associated monetary types related to reserve type.
	 * @return Collection of ClaimCompanyReserveTypesBO
	 * 
	 */
	@Query(accessId="client.claims.sa.query.retrieveCompanyReserveColl", cache=true)
	Collection<ClientClaimCompanyReserveTypesBO> retrieveReserveTypes();
	
	/**
	 * Returns All Claims Monetary Type TransactionTypeColl.
	 * @param reserveTypeMonetaryTypesId Reserve Type Monetary Type ID.
	 * @return Collection of ClaimsMonetaryTypesTransactionTypeBO
	 * 
	 */
	@Query(accessId="client.claims.sa.query.retrieveClaimsMonetaryTypeTransactionTypeColl", cache=true)
	Collection<ClientClaimsMonetaryTypesTransactionTypeBO> retrieveClaimsMonetaryTypeTransactionTypeColl(@Parameter(name="reserveTypeMonetaryTypesId") Long reserveTypeMonetaryTypesId);
	
	/**
	 * Returns all reserve types.
	 * @return Collection of CodeLookupBO.
	 * 
	 */
	@Query(accessId="client.claims.sa.query.retrieveClaimReserveType", cache=true)
	Collection<CodeLookupBO> retrieveClaimReserveType();
	
	/**
	 * Returns all COMPANY ORGANIZATION UNIT.
	 * @return Collection of SysAdminCompanyOrgUnitDetailDTO.
	 * 
	 */
	@Query(accessId="client.claims.sa.query.retrieveAllOrgUnits", cache=true)
	Collection<SysAdminCompanyOrgUnitDetailDTO> retrieveAllOrgUnits();
	
	/**
	 * Returns all Coverages with company, lob and reserve type info
	 * @return Collection of CoverageCompLOBResTypeDTO.
	 * 
	 */
	@Query(accessId="client.claims.sa.query.retrieveCoveragesCompLOBReserveTypeInfo", cache=true)
	Collection<CoverageCompLOBResTypeDTO> retrieveCoveragesCompLOBRTInfo();
	
	
	/**
	 * Returns all loss types with company, lob, reserve type info and coverage info
	 * @return Collection of LossTypeCompLOBResCovDTO.
	 * 
	 */
	@Query(accessId="client.claims.sa.query.retrieveLossTypesCovCompLOBReserveTypeInfo", cache=true)
	Collection<LossTypeCompLOBResCovDTO> retrieveLossTypesCovCompLOBReserveTypeInfo();
	
	
   /**
	* Returns Claim Policy Unit Endorsement Form Code by Line Of Business[LOB].
	* 
	* @param lobCode the line of business code against which the endorsement form information is to be retrieved
	* @return the collection of endorsement forms
	*/
	@Query(accessId = "client.claims.sa.query.retrieveClmPlcyEndorsementFormInfoByLOB", cache=true)
	Collection<ClmPlcyEndorsementFormCodeLookupDTO> retrieveClmPlcyEndorsementFormInfoByLOB(@Parameter(name="lobCode")String lobCode);
	
	/**
	 * Return Unit Data Info for the LOB
	 *  @param lobCode the line of business code against which unit information is to be retrieved
	 * @return the collection of Unit information
	 */
	@Query(accessId = "client.claims.sa.query.retrieveUnitRoleCategoryInfoByLOB", cache=true)
	Collection<UnitDataTypeRoleCategoryDTO> retriveClmPlcyUnitDataTypeRoleCategory(@Parameter(name="lobCode")String lobCode);
	
	/**
	 * Return Unit Data Info for the Coverage LOB
	 *  @param lobCode the line of business code against which unit information is to be retrieved
	 * @return the collection of Unit information
	 */
	@Query(accessId = "client.claims.sa.query.retrieveUnitRoleCategoryInfoForCoverageLOB", cache=true)
	Collection<UnitDataTypeRoleCategoryDTO> retriveClmPlcyUnitDataByCovLOB(
			@Parameter(name="covCode")String coverageCode, @Parameter(name="companyLobId")Long companyLobId);
	
	/**
	 * @param companyId
	 * @return
	 */
	@Query(accessId = "client.claims.claimCoverageCodeForPolicyCoverage", cache=true)
	Collection<ClaimPolicyUnitCoverageDTO> retrieveClaimCoverageCodeForPolicy(
			@Parameter(name = "policyCovCode") String policyCovCode);	
	
}
