package com.client.iip.integration.claim.details;

import com.stoneriver.iip.claims.details.ClaimDetailsCompositeDTO;

/**
 * @author vmuthiah
 *
 */
public class ClientClaimDetailsCompositeDTO extends ClaimDetailsCompositeDTO {

	private static final long serialVersionUID = 9042012L;
	
	ClientClaimPolicyDTO clientClaimPolicyDTO;
	
	/**
	 * @return the policyDetails
	 */
	public ClientClaimPolicyDTO getClientClaimPolicyDetails() {
		return clientClaimPolicyDTO;
	}

	/**
	 * @param value the policyDetails to set
	 */
	public void setClientClaimPolicyDetails(ClientClaimPolicyDTO value) {
		this.clientClaimPolicyDTO = value;
	}
}
