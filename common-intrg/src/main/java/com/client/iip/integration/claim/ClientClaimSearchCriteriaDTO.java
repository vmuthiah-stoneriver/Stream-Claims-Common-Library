package com.client.iip.integration.claim;

import com.stoneriver.iip.claims.search.ClaimSearchCriteriaDTO;

/**
 * Extension of ClaimSearchCriteriaDTO to allow additional policy search fields that will be implemented 
 * in a future base product release.
 * @author brook
 *
 */
public class ClientClaimSearchCriteriaDTO extends ClaimSearchCriteriaDTO {

	private static final long serialVersionUID = -2934294191290422959L;
	private String policyParticipantName;
	private String policyParticipantType;
	private Long policyParticipantPartyId;
	/**
	 * getter.
	 * @return the policyParticipantName
	 */
	public String getPolicyParticipantName() {
		return policyParticipantName;
	}
	/**
	 * setter.
	 * @param policyParticipantNameIn the policyParticipantName to set
	 */
	public void setPolicyParticipantName(String policyParticipantNameIn) {
		this.policyParticipantName = policyParticipantNameIn;
	}
	/**
	 * getter.
	 * @return the policyParticipantType
	 */
	public String getPolicyParticipantType() {
		return policyParticipantType;
	}
	/**
	 * setter.
	 * @param policyParticipantTypeIn the policyParticipantType to set
	 */
	public void setPolicyParticipantType(String policyParticipantTypeIn) {
		this.policyParticipantType = policyParticipantTypeIn;
	}
	/**
	 * getter.
	 * @return the policyParticipantPartyId
	 */
	public Long getPolicyParticipantPartyId() {
		return policyParticipantPartyId;
	}
	/**
	 * setter.
	 * @param policyParticipantPartyIdIn the policyParticipantPartyId to set
	 */
	public void setPolicyParticipantPartyId(Long policyParticipantPartyIdIn) {
		this.policyParticipantPartyId = policyParticipantPartyIdIn;
	}
	
	
}
