package com.client.iip.integration.claim.details;


import java.util.Collection;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.stoneriver.iip.entconfig.EnterpriseConfigDAO;

/**
 * Helper class to retrieve all Jurisdictions configured.
 * @author vmuthiah
 *
 */
@Pojo(id = "com.client.iip.integration.claims.details.JurisdictionHelper")
public class JurisdictionHelper {
	
	EnterpriseConfigDAO entConfigDAO;
	Collection<CodeLookupBO> jurisdictions; 
	
	/**
	 * @return Enterprise Configuration DAO
	 */
	public EnterpriseConfigDAO getEnterpriseConfigDAO() {
		return entConfigDAO;
	}
	
	/**
	 * @param configDAO
	 *            Enterprise Configuration DAO to set
	 */
	@Inject(DaoInterface = "entconfig.daointerface.entconfigDao")
	public void setEnterpriseConfigDAO(EnterpriseConfigDAO configDAO) {
		this.entConfigDAO = configDAO;
	}
	
	/**
	 * Retrieve Jurisdictions configured. 
	 * @return Collection of CodeLookupBO
	 */
	public Collection<CodeLookupBO> retrieveJurisdictions() {
		if(jurisdictions == null) {
			jurisdictions = 
				getEnterpriseConfigDAO().retrieveJurisdictions();
		}
		return jurisdictions;
	}
	
	/**
	 * Retrieve Jurisdiction Name for given Jurisdiction Code.
	 * @param jurisdictionCode
	 * @return Jurisdiction Name 
	 */
	public String retrieveJurisdictionNameForCode(String jurisdictionCode) {
		if(jurisdictions == null) {
			this.retrieveJurisdictions();
		}
		
		for(CodeLookupBO jurisdictionLookup : jurisdictions) {
			if(jurisdictionLookup.getCode().toString().equals(jurisdictionCode)) {
				return jurisdictionLookup.getValue();
			}
		}
		return null;
	}
}
