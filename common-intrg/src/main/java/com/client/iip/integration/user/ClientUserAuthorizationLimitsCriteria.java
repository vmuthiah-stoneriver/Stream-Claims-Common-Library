/**
 * 
 */
package com.client.iip.integration.user;

import java.util.Collection;

/**
 * @author raja.malik
 * 
 */
public class ClientUserAuthorizationLimitsCriteria {

	private Collection<String> authorizationTypes;
	private String monetaryCode;
	private String coverageCode;
	private Long jurisdictionId;
	private Long companyLobId;

	private String claimCoverageCode;

	/**
	 * @return the authorizationTypes
	 */
	public Collection<String> getAuthorizationTypes() {
		return authorizationTypes;
	}

	/**
	 * @param value
	 *            the authorizationTypes to set
	 */
	public void setAuthorizationTypes(Collection<String> value) {
		this.authorizationTypes = value;
	}

	/**
	 * @return the monetaryCode
	 */
	public String getMonetaryCode() {
		return monetaryCode;
	}

	/**
	 * @param value
	 *            the monetaryCode to set
	 */
	public void setMonetaryCode(String value) {
		monetaryCode = value;
	}

	/**
	 * @return the coverageCode
	 */
	public String getCoverageCode() {
		return coverageCode;
	}

	/**
	 * @param value
	 *            the coverageCode to set
	 */
	public void setCoverageCode(String value) {
		this.coverageCode = value;
	}

	/**
	 * @return the jurisdictionId
	 */
	public Long getJurisdictionId() {
		return jurisdictionId;
	}

	/**
	 * @param value
	 *            the jurisdictionId to set
	 */
	public void setJurisdictionId(Long value) {
		this.jurisdictionId = value;
	}

	/**
	 * @return the lob
	 */
	public Long getCompanyLobId() {
		return companyLobId;
	}

	/**
	 * @param value
	 *            the lob to set
	 */
	public void setCompanyLobId(Long value) {
		this.companyLobId = value;
	}

	/**
	 * @return the claimCoverageCode
	 */
	public String getClaimCoverageCode() {
		return claimCoverageCode;
	}

	/**
	 * @param claimCoverageCode
	 *            the claimCoverageCode to set
	 */
	public void setClaimCoverageCode(String claimCoverageCode) {
		this.claimCoverageCode = claimCoverageCode;
	}
}
