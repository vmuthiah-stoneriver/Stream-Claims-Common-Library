package com.client.iip.integration.sa;

import com.stoneriver.iip.claims.sa.api.unitdefinition.ClaimsMonetaryTypesDTO;


public class ClientClaimsMonetaryTypesDTO extends ClaimsMonetaryTypesDTO {

	private static final long serialVersionUID = -2539913143034684918L;

	private String monetaryTypeName;

	public String getMonetaryTypeName() {
		return monetaryTypeName;
	}

	public void setMonetaryTypeName(String monetaryTypeName) {
		this.monetaryTypeName = monetaryTypeName;
	}

}
