package com.client.iip.integration.claim.details;


import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.stoneriver.iip.claims.composite.CompositeServiceHelper;
import com.stoneriver.iip.claims.unit.ClaimUnitDTO;
import com.stoneriver.iip.claims.unit.ClaimUnitSummaryDTO;

@Pojo(id = "com.client.iip.integration.claims.details.ClaimsCompositeServiceHelper")
public class ClaimsCompositeServiceHelper extends CompositeServiceHelper {

	@SuppressWarnings("unchecked")
	public <T extends ClaimUnitDTO> T retrieveClaimUnit(
			ClaimUnitSummaryDTO claimUnitSummaryDTO, String unitDataType) {
		return (T)super.retrieveClaimUnit(claimUnitSummaryDTO, unitDataType);
	}

}
