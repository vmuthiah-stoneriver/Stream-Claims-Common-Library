/**
 * 
 */
package com.client.iip.integration.sa;

import java.util.Collection;
import java.util.List;

import com.fiserv.isd.iip.bc.address.api.PartyCorrespondenceDTO;
import com.fiserv.isd.iip.core.data.annotation.Parameter;
import com.fiserv.isd.iip.core.data.annotation.Query;
import com.fiserv.isd.iip.core.meta.annotation.DaoInterface;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.stoneriver.iip.document.render.CompanyRelatedInformation;
import com.stoneriver.iip.entconfig.EnterpriseConfigDAO;
import com.stoneriver.iip.entconfig.api.CompanyLOBInfo;
import com.stoneriver.iip.entconfig.batch.BatchLogDTO;
import com.stoneriver.iip.entconfig.batch.BatchLogDetailDTO;
import com.stoneriver.iip.entconfig.sa.api.codes.ProfileStatementCodeDTO;
import com.stoneriver.iip.entconfig.sa.organizationunit.api.SysAdminCompanyOrgUnitDefinitionDTO;

/**
 * DAO Class to retrieve Functional Role Code for User and Company Org Unit
 * @author vmuthiah
 *
 */
@DaoInterface(id = "client.daointerface.clientEntConfigDao", daoInterfaceFactory="clientDaoInterfaceFactory")
public interface ClientEnterpriseConfigDAO extends EnterpriseConfigDAO{
	
	/**
	 * Return Functional Roles based on user id and company org unit.
	 *  
	 * @param userId Long
	 * @param companyOrgUnitId Long
	 * @return Collection of Functional Roles
	 */
	@Query(accessId = "client.entconfig.query.retrieveFunctionalRolesForUserAndCompOrgUnit", cache=true)
	Collection<CodeLookupBO> retrieveFunctionalRolesForUserAndCompOrgUnit(
			@Parameter(name="userId")Long userId,
			@Parameter(name="companyOrgUnitId")Long companyOrgUnitId);

	/**
	 * Return all Rules with confirmation Messages.
	 *  
	 * @return Collection of Confirmation Message Rules
	 */
	@Query(accessId = "client.entconfig.query.retrieveConfirmationMessageRules", cache=true)
	Collection<CodeLookupBO> retrieveConfirmationMessageRules();
	
	/**
	 * Return Corporation and Company Assciated to a Company Org Unit
	 * @param companyOrgUnitId Long
	 * @return Collection of SysAdminCompanyOrgUnitDefinitionDTO
	 */
	@Query(accessId = "client.entconfig.query.retrieveCorpCompanyForCompanyOrgUnit", cache=true)
	Collection<SysAdminCompanyOrgUnitDefinitionDTO> retrieveCorpCompanyForCompanyOrgUnit(
			@Parameter(name="companyOrgUnitId")Long companyOrgUnitId);


	/**
	 * Return Company Org Units for Functional Role and user id.
	 *  
	 * @param functionalRoleCode String
	 * @param userId Long
	 * @return Collection of Company Org Units
	 */
	@Query(accessId = "client.entconfig.query.retrieveCompOrgUnitForFunctionalRoleAndUser", cache=true)
	Collection<CodeLookupBO> retrieveCompOrgUnitForFunctionalRolesAndUser(
			@Parameter(name="func_role_cd")String func_role_cd,
			@Parameter(name="userId")Long userId);
	
	/**
	 * Retrieves the Company Prefix Name on the basis of Company Id.
	 * 
	 * @param companyId
	 * 
	 * @return Collection CompanyLOBInfo
	 */

	@Query(accessId = "client.company.retrieveCompanyPrefixNameByCompanyId", cache=true)
	Collection<CompanyLOBInfo> retrieveCompanyPrefixNameByCompanyId(
			@Parameter(name = "companyId") Long companyId);
	
	/**
	 * Retrieves a list of Company LOBs for the list of Company IDs.
	 * 
	 * @param companyIds
	 *            the list company IDs
	 * 
	 * @return Collection list of Company LOBs
	 * 
	 */
	@Query(accessId = "client.entconfig.query.retrieveCompanyLOBByCompanies", cache=true)
	Collection<CompanyLOBInfo> retrieveCompanyLOBForCompanies(
			@Parameter(name = "companyIds") List<Long> companyIds);
    /**
     * @param companyId companyId.
     * @return CompanyRelatedInformation
     */
    @Query(accessId = "client.entconfig.retrieveCompanyRelatedInformation", cache=true)
    Collection<CompanyRelatedInformation> retrieveCompanyRelatedInformation(@Parameter(name="companyId")Long companyId);
    
    /**
     * @param companyId companyId.
     * @param addressType Address type
     * @return Collection PartyCorrespondenceDTO.
     */
    @Query(accessId = "retrieveCompanyAddressInformation", cache=true)
    Collection<PartyCorrespondenceDTO> retrieveCompanyAddressInformation(@Parameter(name="companyId")Long companyId, 
    		@Parameter(name="addressType")String addressType);
    
    /**
     * @param companyId companyId.
     * @return Collection of PartyCorrespondenceDTO.
     */
    @Query(accessId = "retrieveCompanyInterationChannles", cache=true)
    Collection<PartyCorrespondenceDTO> retrieveCompanyInteractionChannels(@Parameter(name="companyId")Long companyId);
    
	/**
	 * Retrieves the user login name by login ID
	 * @param loginId
	 * @return name userName
	 */

	@Query(accessId = "user.retrieveUserLoginNameByUserId", cache=true)
	Collection<String> retrieveUserLoginNameByUserId(
			@Parameter(name = "userId") Long userId);
	
	/**
	 * @param jobId - onot cache, we need latest resultset for the job. Called via polling mechanism.
	 * @return Collection<BatchLogDTO>
	 */
	
	@Query(accessId = "retrieveBatchLog")
	Collection<BatchLogDTO> retrieveBatchLog(@Parameter(name = "jobId") Long jobId);
	
	/**
	 * @param batchLogId -- Donot cache, we need latest resultset for the job. Called via polling mechanism.
	 * @return
	 */
	@Query(accessId = "retrieveBatchLogDetailInfo")
	Collection<BatchLogDetailDTO> retrieveBatchLogDetailInfo(@Parameter(name = "batchLogId") Long batchLogId);
	
	/**
	 * @param agreementTypeCode
	 * @return
	 */
	@Query(accessId = "retrieveProfileStatements", cache=true)
	Collection<ProfileStatementCodeDTO> retrieveProfileStatementsByAgreementType(@Parameter(name = "agreementTypeCode") String agreementTypeCode);
	
}
