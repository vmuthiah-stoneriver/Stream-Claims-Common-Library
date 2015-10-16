package com.client.iip.integration.sa;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import com.fiserv.isd.iip.core.dataobject.EntityData;
import com.fiserv.isd.iip.core.dataobject.SysadminAuditWithExpireData;
import com.fiserv.isd.iip.core.framework.client.DTOTransient;
/**
 * @author abhishake.apoorva
 *
 */
@Entity
@AttributeOverrides({
	@AttributeOverride(name  = "recordId" , 
		column = @Column(name = "CMTCX_ID" , columnDefinition = "numeric(19)"))
})
@Table(name = "CLM_MNY_TYP_CRTMCLX_XREF")
public class ClientClaimsMonetaryTypesBO extends SysadminAuditWithExpireData{

	private static final long serialVersionUID = -947990386015551324L;
	private String monetaryTypeCd;
	private ClientClaimCompanyReserveTypesBO reserveTypesBO;
	
	/**
	 * Getter for the parent.
	 * 
	 * @return EntityData the parent entity
	 */
	@Transient
	@XmlTransient
	@DTOTransient	
	public EntityData getParent(){
		return getReserveTypesBO();
	}
	
	/**
	 * Setter for the parent.
	 * 
	 * @param parent the parent entity
	 */
	public void setParent(EntityData parent){
		setReserveTypesBO((ClientClaimCompanyReserveTypesBO) parent);
	}
	
	/**
	 * @return the monetaryTypeCd
	 */
	@Column(name="CLM_MNY_TYP_CD")
	public String getMonetaryTypeCd() {
		return monetaryTypeCd;
	}
	/**
	 * @param param the monetaryTypeCd to set
	 */
	public void setMonetaryTypeCd(String param) {
		this.monetaryTypeCd = param;
	}
	
	
	
	/**
	 * @return the reserveTypesBO
	 */
	@ManyToOne(targetEntity = ClientClaimCompanyReserveTypesBO.class)
	@JoinColumn(name = "CRTMCLX_ID")
	public ClientClaimCompanyReserveTypesBO getReserveTypesBO() {
		return reserveTypesBO;
	}
	/**
	 * @param param the reserveTypesBO to set
	 */
	public void setReserveTypesBO(ClientClaimCompanyReserveTypesBO param) {
		this.reserveTypesBO = param;
	}
}
