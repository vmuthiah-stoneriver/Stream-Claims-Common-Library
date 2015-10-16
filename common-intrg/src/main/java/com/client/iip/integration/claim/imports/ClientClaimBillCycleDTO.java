package com.client.iip.integration.claim.imports;

import java.util.ArrayList;
import java.util.Collection;

import com.stoneriver.iip.claims.bill.ClaimBillCycleNCDTO;
import com.stoneriver.iip.claims.bill.lineitem.ClaimBillLineItemDTO;

/**
 * @author vmuthiah
 *
 */
public class ClientClaimBillCycleDTO extends ClaimBillCycleNCDTO {
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 05312012L;
	
	private Collection<ClaimBillLineItemDTO> claimBillLineItems = new ArrayList<ClaimBillLineItemDTO>();

	/**
	 * @return Claim Bill Line Items
	 */
	public Collection<ClaimBillLineItemDTO> getClaimBillLineItems() {
		return claimBillLineItems;
	}

	/**
	 * @param lineItems Claim Bill Line Items to set
	 */
	public void setClaimBillLineItems(
			Collection<ClaimBillLineItemDTO> lineItems) {
		this.claimBillLineItems = lineItems;
	}

}
