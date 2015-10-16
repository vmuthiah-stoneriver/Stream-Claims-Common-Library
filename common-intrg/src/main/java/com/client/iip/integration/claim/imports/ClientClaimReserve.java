package com.client.iip.integration.claim.imports;

import com.stoneriver.iip.claims.reserve.ClaimReserve;

/**
 * @author vmuthiah
 *
 */
public class ClientClaimReserve extends ClaimReserve {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 9302012L;
	
	String externalSourceId;
	
	/**
	 * Sets the external source identifier from the imported system.
	 * 
	 * @param value the external source id
	 */
	public void setExternalSourceId(String value) {
		externalSourceId = value;
	}
	
	/**
	 * Returns the external source identifier for the imported system.
	 * 
	 * @return the external source id
	 */
	public String getExternalSourceId() {
		return externalSourceId;
	}

}
