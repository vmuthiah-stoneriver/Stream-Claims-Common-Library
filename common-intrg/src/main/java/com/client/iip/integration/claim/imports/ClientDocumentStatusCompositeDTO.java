package com.client.iip.integration.claim.imports;

import com.stoneriver.iip.document.api.DocumentStatusCompositeDTO;
import com.stoneriver.iip.document.api.DocumentStatusDTO;

/**
 * Client Replication of Composite DTO for Document Statuses.
 * 
 * @author vmuthiah
 *
 */
public class ClientDocumentStatusCompositeDTO extends
		DocumentStatusCompositeDTO {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 8112012L;

	private DocumentStatusDTO documentStatusDTO;
	
	
	public ClientDocumentStatusCompositeDTO(){
		super();
		
	}

	/**
	 * @return Document Status DTO
	 */
	public DocumentStatusDTO getdocumentStatusDTO() {
		return documentStatusDTO;
	}

	/**
	 * @param value Document Status DTO to set
	 */
	public void setDocumentStatusDTO(DocumentStatusDTO value) {
		this.documentStatusDTO = value;
	}
	
	public DocumentStatusDTO getCurrent(){
		return this.documentStatusDTO;
	}

}
