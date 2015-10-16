package com.client.iip.integration.sa;


import java.util.Date;

import com.stoneriver.iip.entconfig.sa.organizationunit.api.SysAdminCompanyOrganizationUnitDTO;


/**
 * This DTO is used for Get All organization units interface
 * @author Gaurav Rai
 *
 */
public class SysAdminCompanyOrgUnitDetailDTO extends SysAdminCompanyOrganizationUnitDTO  {

	private static final long serialVersionUID = 721201240374930008L;

	private Date effectiveDateTime;
	private Date endDateTime;
	
	public SysAdminCompanyOrgUnitDetailDTO() {
		super();
	}
	
	/**
     * @return this entity's effective date and time
     */
	public Date getEffectiveDateTime() {
    	return effectiveDateTime;
	}

    /**
     * @param dateTime the entity's effective data and time
     */
	public void setEffectiveDateTime(final Date dateTime) {
		this.effectiveDateTime = dateTime;
		//this.effectiveDateTime = DateUtility.getStartOfDay(dateTime);
		//DK - changed to comply with interpretation of the modeling team, that
		//system effective dates should preserve the time component.

	}

    /**
     * @return this entity's end date and time
     */
	public Date getEndDateTime() {
    	return endDateTime;
	}

    /**
     * @param dateTime the entity's end data and time
     */
	public void setEndDateTime(final Date dateTime) {
		this.endDateTime = dateTime;
		//this.endDateTime = (dateTime == null) ? null : DateUtility.getStartOfDay(dateTime);//
		//DK - changed to comply with interpretation of the modeling team, that
		//system effective dates should preserve the time component.

	}
}
