package com.client.iip.integration.claim;

import java.util.Date;

import com.stoneriver.iip.claims.search.ClaimSearchResultDTO;

/**
 * Extension of ClaimSearchResultDTO to allow for additional fields that are not currently
 * supported but will be available in a future release.
 * 
 * @author brook
 *
 */
public class ClientClaimSearchResultDTO extends ClaimSearchResultDTO {

	private static final long serialVersionUID = 6803487789157079298L;
	private Date dateClaimReported;
	
	/**
	 * getter.
	 * @return the dateClaimReported
	 */
	public Date getDateClaimReported() {
		return dateClaimReported;
	}
	/**
	 * setter.
	 * @param dateClaimReportedIn the dateClaimReported to set
	 */
	public void setDateClaimReported(Date dateClaimReportedIn) {
		this.dateClaimReported = dateClaimReportedIn;
	}
}
