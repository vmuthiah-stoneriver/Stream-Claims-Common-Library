package com.client.iip.integration.claim.imports;

import java.io.Serializable;
import java.util.Collection;

import com.client.iip.integration.documents.ClientDocumentDTO;
import com.client.iip.integration.party.ClientOrganizationDTO;
import com.client.iip.integration.party.ClientPersonDTO;
import com.stoneriver.iip.claims.claimblock.ClaimBlocksCompositeDTO;
import com.stoneriver.iip.claims.details.ClaimDetailsResponseDTO;
import com.stoneriver.iip.policy.mediation.policyimport.PolicyImportDTO;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @author vmuthiah
 */
public class ClaimImportCompositeDTO extends ClaimDetailsResponseDTO implements Serializable {

	private static final long serialVersionUID = 1151553969520640211L;
	private PolicyImportDTO policy;
	private Collection<ClientPersonDTO> persons;
	private Collection<ClientOrganizationDTO> organizations;
	private Collection<ClientImportAssignmentDTO> claimAssignments;
	private Collection<ClientWorkItemDTO> workItems;
	private Collection<ClientClaimBillDTO> bills;
	private Collection<ClientCaseDTO> claimCases;
	private ClaimBlocksCompositeDTO claimBlocksCompositeDTO;
	private Collection<ClientDocumentDTO> claimDocs;
	@XStreamOmitField
	private boolean useExternalIdentifier = false;

	private Long companyId;
	
	private boolean closeRecovery = false;
	

	/**
	 * @return the closeRecoveryReserve
	 */
	public boolean isCloseRecovery() {
		return closeRecovery;
	}

	/**
	 * @param closeRecoveryReserve the closeRecoveryReserve to set
	 */
	public void setCloseRecovery(boolean closeRecovery) {
		this.closeRecovery = closeRecovery;
	}

	/**
	 * @return PolicyImport DTO
	 */
	public PolicyImportDTO getPolicy() {
		return policy;
	}

	/**
	 * @param policyImportDTO
	 *            Policy to set
	 */
	public void setPolicy(PolicyImportDTO policyImportDTO) {
		this.policy = policyImportDTO;
	}

	/**
	 * @return the workItems
	 */
	public Collection<ClientWorkItemDTO> getWorkItems() {
		return workItems;
	}

	/**
	 * @param value
	 *            the workItems to set
	 */
	public void setWorkItems(Collection<ClientWorkItemDTO> value) {
		this.workItems = value;
	}

	/**
	 * @return the bills
	 */
	public Collection<ClientClaimBillDTO> getBills() {
		return bills;
	}

	/**
	 * @param value
	 *            the bills to set
	 */
	public void setBills(Collection<ClientClaimBillDTO> value) {
		this.bills = value;
	}

	/**
	 * @return the claimAssignments
	 */
	public Collection<ClientImportAssignmentDTO> getClaimAssignments() {
		return claimAssignments;
	}

	/**
	 * @param value
	 *            the claimAssignments to set
	 */
	public void setClaimAssignments(Collection<ClientImportAssignmentDTO> value) {
		this.claimAssignments = value;
	}

	/**
	 * @return Collection of Organization DTO
	 */
	public Collection<ClientOrganizationDTO> getOrganizations() {
		return organizations;
	}

	/**
	 * @param organizationDTOs
	 *            Collection of Organization DTO to set
	 */
	public void setOrganizations(Collection<ClientOrganizationDTO> organizationDTOs) {
		this.organizations = organizationDTOs;
	}

	/**
	 * @return Collection of Person DTO
	 */
	public Collection<ClientPersonDTO> getPersons() {
		return persons;
	}

	/**
	 * @param personDTOs
	 *            Collection of Person DTO to set
	 */
	public void setPersons(Collection<ClientPersonDTO> personDTOs) {
		this.persons = personDTOs;
	}
	
	
	/**
	 * @return Indicates whether to Use External Identifier or not
	 */
	public boolean isUseExternalIdentifier() {
		return useExternalIdentifier;
	}

	/**
	 * @param useIdentifier Use External Identifier indicator to set
	 */
	public void setUseExternalIdentifier(boolean useIdentifier) {
		this.useExternalIdentifier = useIdentifier;
	}
	
	/**
	 * @return the cases
	 */
	public Collection<ClientCaseDTO> getClaimCases() {
		return claimCases;
	}

	/**
	 * @param value the cases to set
	 */
	public void setClaimCases(Collection<ClientCaseDTO> value) {
		this.claimCases = value;
	}

	/**
	 * @return the companyId
	 */
	public Long getCompanyId() {
		return companyId;
	}

	/**
	 * @param value Company Id to set
	 */
	public void setCompanyId(Long value) {
		this.companyId = value;
	}
	
	// 11/21/2013 @GR - Claim Level Block Attributes.
	public ClaimBlocksCompositeDTO getClaimBlocksCompositeDTO() {
		return claimBlocksCompositeDTO;
	}

	public void setClaimBlocksCompositeDTO(
			ClaimBlocksCompositeDTO _claimBlocksCompositeDTO) {
		this.claimBlocksCompositeDTO = _claimBlocksCompositeDTO;
	}
	
	/**
	 * 10/20/2014 @GR - Override Document Objects for the Documents Viewer Enhancement
	 */
	public void setClaimDocs(Collection<ClientDocumentDTO> _doc){
		this.claimDocs = _doc;
	}
	
	public Collection<ClientDocumentDTO> getClaimDocs(){
		return this.claimDocs;
	}
}
