package com.client.iip.integration.claim.imports;

import com.fiserv.isd.iip.bc.party.api.MinimalPartyForDriversDTO;
import com.stoneriver.iip.claims.CALClaimParticipationDTO;


/**
 * Adding MinimalPartyForDriverDTO as MinimalParty.
 * 
 * @author saurabh.bhatnagar
 *
 */
public class ClientClaimParticipationDTO extends CALClaimParticipationDTO {

	private static final long serialVersionUID = 5168005577939545103L;
	private MinimalPartyForDriversDTO driverParticipation;

	/**
	 * @return the driverParticipation
	 */
	public MinimalPartyForDriversDTO getDriverParticipation() {
		return driverParticipation;
	}

	/**
	 * @param driverParticipation the driverParticipation to set
	 */
	public void setDriverParticipation(MinimalPartyForDriversDTO driverParticipation) {
		this.driverParticipation = driverParticipation;
	}
	
	public MinimalPartyForDriversDTO getParticipation() {
		return driverParticipation;
	} 
}
