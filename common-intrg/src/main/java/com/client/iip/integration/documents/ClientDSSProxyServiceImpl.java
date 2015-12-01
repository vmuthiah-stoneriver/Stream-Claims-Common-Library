package com.client.iip.integration.documents;

import java.io.InputStream;
import java.net.URL;

import com.fiserv.isd.iip.core.meta.annotation.Service;
import com.stoneriver.iip.document.api.ImageIndexDTO;
import com.stoneriver.iip.document.dss.DSSProxyService;
import com.stoneriver.iip.document.dss.DSSResponse;
import com.stoneriver.iip.document.dss.IIPDSSIntegrationException;
import com.stoneriver.iip.document.dss.StoreDocumentRequest;


public class ClientDSSProxyServiceImpl implements DSSProxyService {
	
	public static final String DOCUMENT_VIEWER_URL = System.getProperty("documentViewerURL",
			"http://maps.google.com/maps?docId=");	

	@Override
	public void deleteDocument(ImageIndexDTO imageIndexDTO)
			throws IIPDSSIntegrationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DSSResponse getDocument(ImageIndexDTO imageIndexDTO)
			throws IIPDSSIntegrationException {
		DSSResponse	response = new DSSResponse();
		try{
			//Step 1: Get the Image Id and open a URL Connection to the image server
			String mediaFileUrl = DOCUMENT_VIEWER_URL + imageIndexDTO.getDocumentImageId();
			URL url = new URL(mediaFileUrl);
			//Step 2: Get the URL FileInputstream and load the response.
			InputStream in = url.openStream();
			response.setDocumentData(in);
			response.setDocumentIndex(imageIndexDTO);
			}catch(Exception ex){
				throw new IIPDSSIntegrationException("Get Document Failure from Image Server", ex);
			}
		return response;
	}

	@Override
	public DSSResponse reindexDocument(ImageIndexDTO imageIndexDTO)
			throws IIPDSSIntegrationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DSSResponse storeDocument(StoreDocumentRequest request)
			throws IIPDSSIntegrationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DSSResponse updateDocument(StoreDocumentRequest request)
			throws IIPDSSIntegrationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DSSResponse copyDocument(ImageIndexDTO source, ImageIndexDTO target)
			throws IIPDSSIntegrationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restoreDocument(ImageIndexDTO imageIndexDTO)
			throws IIPDSSIntegrationException {
		// TODO Auto-generated method stub
		
	}

}
