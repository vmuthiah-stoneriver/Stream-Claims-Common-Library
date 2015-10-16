package com.client.iip.integration.sa;

/**
 * This is class is used to fetch all loss types with company, lob, reserve type and coverage information.
 * @author Gaurav Rai
 *
 */
public class LossTypeCompLOBResCovDTO extends CoverageCompLOBResTypeDTO {

	private String lossTypeCode;
	private String lossTypeName;
	/**
	 * @return the lossTypeCode
	 */
	public String getLossTypeCode() {
		return lossTypeCode;
	}
	/**
	 * @param lossTypeCode the lossTypeCode to set
	 */
	public void setLossTypeCode(String lossTypeCode) {
		this.lossTypeCode = lossTypeCode;
	}
	/**
	 * @return the lossTypeName
	 */
	public String getLossTypeName() {
		return lossTypeName;
	}
	/**
	 * @param lossTypeName the lossTypeName to set
	 */
	public void setLossTypeName(String lossTypeName) {
		this.lossTypeName = lossTypeName;
	}
	
	
}
