package com.client.iip.integration.claim.imports;

import java.util.ArrayList;
import java.util.Collection;

import com.stoneriver.iip.claims.unit.ClaimUnitVehicleDTO;


/**
 * Adding MinimalPartyForDriverDTO as MinimalParty.
 * @author saurabh.bhatnagar
 *
 */
public class ClientClaimUnitVehicleDTO extends ClaimUnitVehicleDTO{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3810366715312567947L;
	
	private Collection<ClientClaimParticipationDTO> unknownDrivers = new ArrayList<ClientClaimParticipationDTO>();

	/**
	 * @return the unknownDrivers
	 */
	public Collection<ClientClaimParticipationDTO> getUnknownDrivers() {
		return unknownDrivers;
	}

	/**
	 * @param unknownDrivers the unknownDrivers to set
	 */
	public void setUnknownDrivers(
			Collection<ClientClaimParticipationDTO> unknownDrivers) {
		this.unknownDrivers = unknownDrivers;
	}
	
	

}
