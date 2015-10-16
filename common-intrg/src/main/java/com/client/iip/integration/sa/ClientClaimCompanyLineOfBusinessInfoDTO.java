package com.client.iip.integration.sa;

import com.stoneriver.iip.claims.sa.api.unitdefinition.ClaimCompanyLineOfBusinessInfoDTO;

public class ClientClaimCompanyLineOfBusinessInfoDTO extends ClaimCompanyLineOfBusinessInfoDTO {

	private static final long serialVersionUID = -2539913143034636918L;
	private String lobCode;
	private String lobName;
	private boolean lobPkgInd;

	public String getLobCode() {
		return lobCode;
	}

	public void setLobCode(String lobCode) {
		this.lobCode = lobCode;
	}

	public String getLobName() {
		return lobName;
	}

	public void setLobName(String lobName) {
		this.lobName = lobName;
	}

	public boolean isLobPkgInd() {
		return lobPkgInd;
	}

	public void setLobPkgInd(boolean lobPkgInd) {
		this.lobPkgInd = lobPkgInd;
	}

}