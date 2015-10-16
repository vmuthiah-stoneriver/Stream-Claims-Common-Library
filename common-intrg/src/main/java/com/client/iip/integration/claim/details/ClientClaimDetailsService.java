package com.client.iip.integration.claim.details;


import java.util.Collection;

import org.mule.api.MuleException;

import com.client.iip.integration.claim.ClientClaimSearchCriteriaDTO;
import com.client.iip.integration.claim.imports.ClaimImportCompositeDTO;
import com.fiserv.isd.iip.core.service.ServiceEndpoint;
import com.stoneriver.iip.claims.details.ClaimDetailRequestCriteria;
import com.stoneriver.iip.claims.search.ClaimSearchException;
import com.stoneriver.iip.claims.search.ClaimSearchResultDTO;

/**
 * Composite service used to retrieve claim details.
 * @author brook
 *
 */
@ServiceEndpoint("integration.endpoint.claimDetailsInboundEndPoint")
public interface ClientClaimDetailsService {

	/**
	 * Retrieve all of the Claim and Claim Unit details including documents, notes, assignments,
	 * work items, bill and financial transactions.
	 * @param request the original request containing the claim identifier
	 * @return a populated ClaimImportCompositeDTO
	 * @throws ClaimSearchException 
	 * @throws MuleException 
	 */
	ClaimImportCompositeDTO retrieveClaimDetails(ClaimDetailRequestCriteria request) throws ClaimSearchException, MuleException;
	
	/**
	 * Retrieve all of the work items and their details for a claim.
	 * @param request contains the claimId.
	 * @return ClaimImportCompositeDTO populated with the work items details.
	 */
	ClaimImportCompositeDTO retrieveClaimWorkItemDetails(ClaimImportCompositeDTO request);

	/**
	 * Retrieve all of the assignments and their details for a claim.
	 * @param request contains the claimId.
	 * @return ClaimImportCompositeDTO populated with the assignments details.
	 */
	ClaimImportCompositeDTO retrieveClaimAssignmentsDetails(ClaimImportCompositeDTO request);
	
	/**  
	 * Search the Claim based on the criteria.
	 * 
	 * @param criteria
	 *            the claim search criteria
	 * @return Collection<ClaimSearchResultDTO> the search results
	 * @throws ClaimSearchException
	 *             in case of any errors
	 */
	Collection<ClaimSearchResultDTO> searchClaim(ClientClaimSearchCriteriaDTO criteria)
			throws ClaimSearchException;	
	
	

}
