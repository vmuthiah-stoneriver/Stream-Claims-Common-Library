package com.client.iip.integration.party;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.fiserv.isd.iip.bc.party.api.BlockDTO;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.core.data.annotation.Parameter;
import com.fiserv.isd.iip.core.data.annotation.Query;
import com.fiserv.isd.iip.core.data.annotation.Search;
import com.fiserv.isd.iip.core.meta.annotation.DaoInterface;

/**
 * DAO to interact with the Party table.
 */
@DaoInterface(id="client.daointerface.clientPartyDao", daoInterfaceFactory = "clientDaoInterfaceFactory")
public interface ClientPartyDAO {

	/**
	 * Retrieve a list of party IDs that have a vendor role associated with the
	 * party.
	 * 
	 * @param partyIds
	 *            party IDs
	 * @return Collection of Long
	 */
	@Query(accessId = "client.party.roles.vendor.retrieveVendorPartyIds")
	Collection<Long> retrieveVendorPartyIDs(
			@Parameter(name = "partyIds") List<Long> partyIds);
	

	/**
	 * Search the party based on criteria.
	 * @param criteria PartySearchCriteria
	 * @return Collection of PartySearchResult
	 */
	@Search(accessId="client.party.search.main")
	Collection<ClientPartySearchResult> search(ClientPartySearchCriteria criteria);
	
	/*
	 * Fetch all Blocks for the given PartyId
	 */
	@Query(accessId="client.party.retrievePartyBlocks")
	Collection<BlockDTO> retrievePartyBlocks(@Parameter(name = "partyId") Long partyId);
	
	/**
	 * Retrieve a list of PartyDTO containing Party ID and Party Type Code for a given date range.
	 * 
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 *            
	 * @return Collection list of PartyDTO objects
	 */
	@Query(accessId = "client.events.retrieveVendorPartiesForDate")
	Collection<PartyDTO> retrieveVendorPartiesForDate(
			@Parameter(name = "startDate") Date startDate,
			@Parameter(name = "endDate") Date endDate);
	
}
