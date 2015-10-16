package com.client.iip.integration.documents;

import com.stoneriver.iip.document.api.DocumentDTO;

public class ClientDocumentDTO extends DocumentDTO {
	
	private String documentFileName;
	
	private String documentFileLocationCode;
	
	/**
	 * @return the documentFileName
	 */
	public String getDocumentFileName() {
		return documentFileName;
	}

	/**
	 * @param documentFileName the documentFileName to set
	 */
	public void setDocumentFileName(String documentFileName) {
		this.documentFileName = documentFileName;
	}

	public String getDocumentFileLocationCode() {
		return documentFileLocationCode;
	}

	public void setDocumentFileLocationCode(String documentFileLocationCode) {
		this.documentFileLocationCode = documentFileLocationCode;
	}
	

}
