package com.client.iip.integration.sa;

import com.fiserv.isd.iip.core.framework.client.DTO;

/**
 * This is class is used to fetch all coverages with company, lob, reserve type information.
 * @author Gaurav Rai
 *
 */

@DTO(base=false)
public class CoverageCompLOBResTypeDTO {
	
	
	private Long corpId;
	private String corporation;
	private Long compId;
	private String company;
	private Long companyLOBId;	
	private String lobCode;
	private String lobName;
	private String reserveTypeCode;
	private String reserveTypeName;
	private String coverageCode;
	private String coverageName;
	
	/**
	 * @return the corpId
	 */
	public Long getCorpId() {
		return corpId;
	}

	/**
	 * @param param the corpId to set
	 */
	public void setCorpId(Long param) {
		this.corpId = param;
	}

	/**
	 * @return the compId
	 */
	public Long getCompId() {
		return compId;
	}

	/**
	 * @param param the compId to set
	 */
	public void setCompId(Long param) {
		this.compId = param;
	}
	
	/**
	 * @return the corporation
	 */
	public String getCorporation() {
		return corporation;
	}
	/**
	 * @param value the corporation to set
	 */
	public void setCorporation(String value) {
		this.corporation = value;
	}
	
	/**
	 * @return the company
	 */
	public String getCompany() {
		return company;
	}
	/**
	 * @param value the company to set
	 */
	public void setCompany(String value) {
		this.company = value;
	}
		
	/**
	 * @return the companyLOBId
	 */
	public Long getCompanyLOBId() {
		return companyLOBId;
	}
	/**
	 * @param value the companyLOBId to set
	 */
	public void setCompanyLOBId(Long value) {
		this.companyLOBId = value;
	}

	/**
	 * @return the lobCode
	 */
	public String getLobCode() {
		return lobCode;
	}

	/**
	 * @param lobCode the lobCode to set
	 */
	public void setLobCode(String lobCode) {
		this.lobCode = lobCode;
	}

	/**
	 * @return the lobName
	 */
	public String getLobName() {
		return lobName;
	}

	/**
	 * @param lobName the lobName to set
	 */
	public void setLobName(String lobName) {
		this.lobName = lobName;
	}

	/**
	 * @return the reserveTypeCode
	 */
	public String getReserveTypeCode() {
		return reserveTypeCode;
	}

	/**
	 * @param reserveTypeCode the reserveTypeCode to set
	 */
	public void setReserveTypeCode(String reserveTypeCode) {
		this.reserveTypeCode = reserveTypeCode;
	}

	/**
	 * @return the reserveTypeName
	 */
	public String getReserveTypeName() {
		return reserveTypeName;
	}

	/**
	 * @param reserveTypeName the reserveTypeName to set
	 */
	public void setReserveTypeName(String reserveTypeName) {
		this.reserveTypeName = reserveTypeName;
	}

	/**
	 * @return the coverageCode
	 */
	public String getCoverageCode() {
		return coverageCode;
	}

	/**
	 * @param coverageCode the coverageCode to set
	 */
	public void setCoverageCode(String coverageCode) {
		this.coverageCode = coverageCode;
	}

	/**
	 * @return the coverageName
	 */
	public String getCoverageName() {
		return coverageName;
	}

	/**
	 * @param coverageName the coverageName to set
	 */
	public void setCoverageName(String coverageName) {
		this.coverageName = coverageName;
	}

	
	
}
