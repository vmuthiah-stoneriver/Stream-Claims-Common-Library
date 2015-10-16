package com.client.iip.integration.claim.details;


import com.stoneriver.iip.claims.policy.ClaimPolicyDTO;

/**
 * DTO class to hold Jurisdiction Name.
 * @author vmuthiah
 *
 */
public class ClientClaimPolicyDTO extends ClaimPolicyDTO {
	
	private static final long serialVersionUID = 9042012L;
	
	private String jurisdictionName;

	/**
	 * @return Jurisdiction Name
	 */
	public String getJurisdictionName() {
		return jurisdictionName;
	}

	/**
	 * @param jurisdictionName Jurisdiction Name to set
	 */
	public void setJurisdictionName(String jurisdictionName) {
		this.jurisdictionName = jurisdictionName;
	}
}
