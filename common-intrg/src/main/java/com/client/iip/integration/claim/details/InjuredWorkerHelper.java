package com.client.iip.integration.claim.details;


import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.stoneriver.iip.claims.ClaimInformationRequestDTO;
import com.stoneriver.iip.claims.composite.WCClaimsInjuredWorkerWagesHelper;
import com.stoneriver.iip.claims.details.ClaimDetailsCompositeDTO;
import com.stoneriver.iip.claims.wc.WCLiteHelper;

@Pojo(id = "com.client.iip.integration.claims.details.InjuredWorkerHelper")
public class InjuredWorkerHelper {
	private WCClaimsInjuredWorkerWagesHelper injuredCompositeHelper;
	/**
	 * @param value the injuredCompositeHelper to set
	 */
	@Inject(PojoRef = "com.stoneriver.iip.claims.composite.WCClaimsInjuredWorkerWagesHelper")
	public void setInjuredCompositeHelper(WCClaimsInjuredWorkerWagesHelper value) {
		this.injuredCompositeHelper = value;
	}

	/**
	 * @return the injuredCompositeHelper
	 */
	public WCClaimsInjuredWorkerWagesHelper getInjuredCompositeHelper() {
		return injuredCompositeHelper;
	}
	public ClaimDetailsCompositeDTO populateInjuredWorker(ClaimInformationRequestDTO claimParam,
			ClaimDetailsCompositeDTO detailsCompositeDTO){
		// don't retrieve for WC Lite
		if (!WCLiteHelper.isWCLite() && NoticeOfLossHelper.isWC(claimParam.getLobCode())) {
			detailsCompositeDTO
					.setInjuredWorkerComposite(getInjuredCompositeHelper()
							.createInjuredWorkerComposite(
									claimParam.getClaimId()));
		}
		return detailsCompositeDTO;
	}
}
