package com.client.iip.integration.claim.imports;

import com.fiserv.isd.iip.core.service.ServiceEndpoint;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;

@ServiceEndpoint("integration.endpoint.claimImportInboundEndPoint")
public interface ClientClaimImportService {
	
	/**
	 * Import Claim from ClaimImportCompositeDTO provided by client.
	 * @param claimImportCompositeDTO
	 * @return	Claim Number
	 * @throws IIPCoreSystemException
	 */
	String importClaim(ClaimImportCompositeDTO claimImportCompositeDTO);

}
