/**
 * 
 */
package com.client.iip.integration.user;

import java.math.BigDecimal;

/**
 * @author raja.malik
 * 
 */
public class ClientUserAuthorizationLimitDTO {

	private String authorizationType;
	private BigDecimal authorizationLimit;
	private String coverageType;
	private String monetaryCategory;
	private Long jurisdictionId;
	private Long lob;

	/**
	 * @return the authorizationType
	 */
	public String getAuthorizationType() {
		return authorizationType;
	}

	/**
	 * @param value
	 *            the authorizationType to set
	 */
	public void setAuthorizationType(String value) {
		this.authorizationType = value;
	}

	/**
	 * @return the coverageType
	 */
	public String getCoverageType() {
		return coverageType;
	}

	/**
	 * @param value
	 *            the coverageType to set
	 */
	public void setCoverageType(String value) {
		this.coverageType = value;
	}

	/**
	 * @return the authorizationLimit
	 */
	public BigDecimal getAuthorizationLimit() {
		return authorizationLimit;
	}

	/**
	 * @param value
	 *            the authorizationLimit to set
	 */
	public void setAuthorizationLimit(BigDecimal value) {
		this.authorizationLimit = value;
	}

	/**
	 * @return the monetaryCategory
	 */
	public String getMonetaryCategory() {
		return monetaryCategory;
	}

	/**
	 * @param value
	 *            the monetaryCategory to set
	 */
	public void setMonetaryCategory(String value) {
		this.monetaryCategory = value;
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
	public Long getLob() {
		return lob;
	}

	/**
	 * @param value
	 *            the lob to set
	 */
	public void setLob(Long value) {
		this.lob = value;
	}
}
