package com.client.iip.integration.party;

import java.util.Collection;

import com.fiserv.isd.iip.bc.party.api.DuplicatePartySearchResultComposite;
import com.fiserv.isd.iip.bc.party.api.PartyDTO;
import com.fiserv.isd.iip.core.service.ServiceEndpoint;

@ServiceEndpoint("integration.endpoint.clientPartyServiceInbound")
public interface ClientPartyService {
	
	PartyDTO retrievePartyDetails(Long partyId);
	
	DuplicatePartySearchResultComposite duplicatePartySearch(PartyDTO party);
	
	PartyDTO saveParty(PartyDTO party);
	
	Collection<ClientPartySearchResult> searchParty(ClientPartySearchCriteria criteria);
	
	Collection<ClientPartySearchResult> retrieveVendors(VendorExportRequest request);

}
