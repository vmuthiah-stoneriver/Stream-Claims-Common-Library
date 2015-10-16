package com.client.iip.integration.documents;

import java.util.Collection;

import com.fiserv.isd.iip.core.data.annotation.Query;
import com.fiserv.isd.iip.core.meta.annotation.DaoInterface;
import com.stoneriver.iip.document.DocumentDataElement;

/**
 * Client Document Data Access Object layer.
 * 
 * @author sudharsan.sriram
 *
 */
@DaoInterface(id = "document.daointerface.clientDocumentDao", daoInterfaceFactory = "document.dataaccess.documentDaoInterfaceFactory")
public interface ClientDocumentDAO{

	/**
	 * Retrieve list of Document Id.
	 * @return List of Document Id
	 */
	@Query(accessId="documents.batch.ProduceOutbound")
	Collection<Long> retriveDocumentIds();
	
	/**
	 * @return List of DocumentDataElementCodeBO
	 */
	@Query(accessId="documents.batch.retrieveDocumentDataElements")
	Collection<DocumentDataElement> retrieveDocumentDataElements();
}
