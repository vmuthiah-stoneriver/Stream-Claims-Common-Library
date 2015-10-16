package com.client.iip.integration.sa;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fiserv.isd.iip.core.dataobject.SysadminAuditWithExpireData;
/**
 * 
 * @author abhishake.apoorva
 *
 */
@Entity
@AttributeOverrides({
	@AttributeOverride(name  = "recordId" , 
		column = @Column(name = "CMTCFX_ID" , columnDefinition = "numeric(19)"))
})
@NamedQuery(
		name = "clientRetrieveClaimsMonetaryTypeTransactionTypeColl",
		query = "FROM ClientClaimsMonetaryTypesTransactionTypeBO AS bo WHERE bo.reserveTypeMonetaryTypesId = :reserveTypeMonetaryTypesId " 
)
@Table(name = "CLM_MNY_TYP_CRTMCLX_FTTC_XREF")
public class ClientClaimsMonetaryTypesTransactionTypeBO extends SysadminAuditWithExpireData{

	private static final long serialVersionUID = -8868151143096866403L;
	private Long reserveTypeMonetaryTypesId;
	private String fnclTranTypeCode;
	private boolean impactIncurInd;

	/**
	 * @return the impactIncurInd
	 */
	@Column(name="CMTCFX_IMPCT_INCUR_IND")
	public String getImpactIncurIndInternal() {
		return booleanToString(impactIncurInd);
	}
	/**
	 * @param param the impactIncurInd to set
	 */
	public void setImpactIncurIndInternal(String param) {
		this.impactIncurInd = stringToBoolean(param);
	}
	
	/**
	 * @return the impactIncurInd
	 */
	@Transient
	public boolean getImpactIncurInd() {
		return impactIncurInd;
	}

	/**
	 * @param param the impactIncurInd to set
	 */
	public void setImpactIncurInd(boolean param) {
		this.impactIncurInd = param;
	}
	/**
	 * @return the fnclTranTypeCode
	 */
	@Column(name="FNCL_TRAN_TYP_ID")
	public String getFnclTranTypeCode() {
		return fnclTranTypeCode;
	}

	/**
	 * @param param the fnclTranTypeCode to set
	 */
	public void setFnclTranTypeCode(String param) {
		this.fnclTranTypeCode = param;
	}

	/**
	 * @return the reserveTypeMonetaryTypesId
	 */
	@Column(name="CMTCX_ID")
	public Long getReserveTypeMonetaryTypesId() {
		return reserveTypeMonetaryTypesId;
	}

	/**
	 * @param param the reserveTypeMonetaryTypesId to set
	 */
	public void setReserveTypeMonetaryTypesId(Long param) {
		this.reserveTypeMonetaryTypesId = param;
	}

}
