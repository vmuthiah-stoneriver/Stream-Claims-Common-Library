package com.client.iip.integration.claim.details;

import java.util.Collection;

import com.fiserv.isd.iip.core.data.annotation.Query;
import com.fiserv.isd.iip.core.meta.annotation.DaoInterface;
import com.stoneriver.iip.claims.status.ClaimStatusTransitionDTO;

/**
 * DAO Class to retrieve all Status Transitions.
 * @author vmuthiah
 *
 */
@DaoInterface(id = "client.daointerface.clientClaimStatusDao", daoInterfaceFactory = "clientDaoInterfaceFactory")
public interface ClientClaimStatusDAO {

	/**
	 * Retrieve all Status Transitions configured.
	 * @return Collection of ClaimStatusTransitionDTO
	 */
	@Query(accessId="client.claims.allStatusTransitions")
	Collection<ClaimStatusTransitionDTO> retrieveAllStatusTransitionDetails();
}
