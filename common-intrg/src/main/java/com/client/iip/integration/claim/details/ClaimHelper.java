package com.client.iip.integration.claim.details;


import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.CWSClaimService;
import com.stoneriver.iip.claims.ClaimInformationRequestDTO;
import com.stoneriver.iip.claims.MinimalInternalClaimDTO;

@Pojo(id = "com.client.iip.integration.claims.details.ClaimHelper")
public class ClaimHelper {
	private CWSClaimService getClaimService() {
		return MuleServiceFactory.getService(CWSClaimService.class);
	}

	/**
	 * retrieves minimal claim data. 
	 * @param request
	 *            request details
	 * @return MinimalClaimDTO claim details
	 */
	public MinimalInternalClaimDTO retrieveMinimalInternalClaim(
			ClaimInformationRequestDTO request) {
		return getClaimService().retrieveMinimalInternalClaim(request);
	}

}
