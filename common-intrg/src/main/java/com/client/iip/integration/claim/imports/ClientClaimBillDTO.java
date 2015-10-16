package com.client.iip.integration.claim.imports;

import java.util.Collection;

import com.stoneriver.iip.claims.bill.ClaimBillCycleNCDTO;
import com.stoneriver.iip.claims.bill.ClaimBillDetailDTO;

/**
 * @author vmuthiah
 *
 */
public class ClientClaimBillDTO extends ClaimBillDetailDTO {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 05312012L;
	
	private ClientClaimBillCycleDTO currentBillCycle;
	
	private Collection<ClientClaimBillCycleDTO> previousBillCycles;

	/**
	 * @return Current Claim Bill Cycle
	 */
	public ClientClaimBillCycleDTO getCurrentBillCycle() {
		return currentBillCycle;
	}

	/**
	 * @param cycle Current Claim Bill Cycle to set
	 */
	public void setCurrentBillCycle(ClientClaimBillCycleDTO cycle) {
		this.currentBillCycle = cycle;
	}

	/**
	 * @return Previous Claim Bill Cycles 
	 */
	public Collection<ClientClaimBillCycleDTO> getPreviousBillCycles() {
		return previousBillCycles;
	}

	/**
	 * @param cycles Previous Claim Bill Cycles to set
	 */
	public void setPreviousBillCycles(Collection<ClientClaimBillCycleDTO> cycles) {
		this.previousBillCycles = cycles;
	}	

	/**
	 * @return Current Claim Bill Cycle
	 */
	public ClaimBillCycleNCDTO getCurrentCycle() {
		return currentBillCycle;
	}
}
