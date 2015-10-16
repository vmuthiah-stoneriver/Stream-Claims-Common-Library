package com.client.iip.integration.sa;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fiserv.isd.iip.core.dataobject.SysadminAuditWithExpireData;

/**
 * 
 * @author abhishake.apoorva
 *
 */

@Entity
@AttributeOverrides({
	@AttributeOverride(name  = "recordId" , 
		column = @Column(name = "CRTMCLX_ID" , columnDefinition = "numeric(19)"))
})
@NamedQuery(
		name = "clientRetrieveCompanyReserveColl",
		query = "FROM ClientClaimCompanyReserveTypesBO AS bo " 
	)
@Table(name = "CLM_RSRV_TYP_MNY_CTG_LOB_XREF")
public class ClientClaimCompanyReserveTypesBO extends SysadminAuditWithExpireData{

	
	private static final long serialVersionUID = 2863775083276313515L;
	private Long compLOBId;
	private BigDecimal thresholdAmount;
	private String reservTypeCd;
	private String monetaryCategoryCd;
	private Set<ClientClaimsMonetaryTypesBO> monetaryTypeColl = new HashSet<ClientClaimsMonetaryTypesBO>();

	/**
	 * @return the compLOBId
	 */
	@Column(name="CMPY_LOB_ID")
	public Long getCompLOBId() {
		return compLOBId;
	}

	/**
	 * @param param the compLOBId to set
	 */
	public void setCompLOBId(Long param) {
		this.compLOBId = param;
	}
	
	/**
	 * @return the thresholdAmount
	 */
	@Column(name="CRTMCLX_RSRV_INCRS_THRSHLD_AMT")
	public BigDecimal getThresholdAmount() {
		return thresholdAmount;
	}

	/**
	 * @param param the thresholdAmount to set
	 */
	public void setThresholdAmount(BigDecimal param) {
		this.thresholdAmount = param;
	}

	/**
	 * @return the reservTypeCd
	 */
	@Column(name="CLM_RSRV_TYP_CD")
	public String getReservTypeCd() {
		return reservTypeCd;
	}

	/**
	 * @param param the reservTypeCd to set
	 */
	public void setReservTypeCd(String param) {
		this.reservTypeCd = param;
	}

	/**
	 * @return the monetaryCategoryCd
	 */
	@Column(name="CLM_MNY_CTG_CD")
	public String getMonetaryCategoryCd() {
		return monetaryCategoryCd;
	}

	/**
	 * @param param the monetaryCategoryCd to set
	 */
	public void setMonetaryCategoryCd(String param) {
		this.monetaryCategoryCd = param;
	}

	/**
	 * @return the monetaryTypeColl
	 */
	@OneToMany(cascade = CascadeType.ALL , fetch=FetchType.EAGER , mappedBy="reserveTypesBO")
	public Set<ClientClaimsMonetaryTypesBO> getMonetaryTypeColl() {
		return monetaryTypeColl;
	}

	/**
	 * @param param the monetaryTypeColl to set
	 */
	public void setMonetaryTypeColl(Set<ClientClaimsMonetaryTypesBO> param) {
		this.monetaryTypeColl = param;
	}
}
