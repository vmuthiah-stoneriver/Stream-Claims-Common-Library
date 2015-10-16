package com.client.iip.integration.documents;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.claims.CWSClaimService;
import com.stoneriver.iip.claims.ClaimInformationRequestDTO;
import com.stoneriver.iip.claims.MinimalInternalClaimDTO;
import com.stoneriver.iip.document.api.DocumentDTO;

/**
 * 
 * Preview a Document.
 * 
 */
public class DocumentPreviewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(DocumentPreviewServlet.class);

	public static final String DOCUMENT_VIEWER_URL = System.getProperty("documentViewerURL",
			"http://maps.google.com/maps?docId=");

	private CWSClaimService claimService;

	private ClientDocumentHelper clientDocumentHelper;

	/**
	 * 
	 * Called by the server to allow a servlet to handle the document preview
	 * request.
	 * 
	 * 
	 * @param req
	 *            an {@link HttpServletRequest} object that contains the request
	 *            the client has made of the servlet
	 * 
	 * @param resp
	 *            an {@link HttpServletResponse} object that contains the
	 *            response the servlet sends to the client
	 * 
	 * 
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {

		Long docId = Long.valueOf(req.getParameter("docId"));

		logger.debug("Document ID: {}", docId);

		String mediaFileUrl = getClientDocumentHelper().retrieveElectronicMediaFile(docId);

		if (StringUtils.isNotEmpty(mediaFileUrl) && mediaFileUrl.startsWith("file:")) {

			resp.setContentType(getServletContext().getMimeType(mediaFileUrl));

			InputStream source = null;
			OutputStream destination = null;
			try {
				URL url = new URL(mediaFileUrl);
				source = url.openStream();
				destination = resp.getOutputStream();
				IOUtils.copy(source, destination);

			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error loading document {} exception - {}"
						, mediaFileUrl, e.getMessage());
				try {
					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					resp.getWriter().print("Error loading document using URL: " + mediaFileUrl);
					resp.getWriter().close();
				} catch (IOException e1) {
				}
				return;

			} finally {
				IOUtils.closeQuietly(source);
				IOUtils.closeQuietly(destination);
			}

		} else {

			String claimNumberUrl = StringUtils.EMPTY;
			try {
				// 10/18/2014 @GR - This is refactored to be generic enough for both doc as well as Claim level view, 
				//Associate the request parameter within the URL itself
				//claimNumberUrl = DOCUMENT_VIEWER_URL + "?claimNumber=" + retrieveClaimNumber(docId);
				if(DOCUMENT_VIEWER_URL.contains("claimNumber=")){
					claimNumberUrl = DOCUMENT_VIEWER_URL + retrieveClaimNumber(docId);
				}else{
					claimNumberUrl = DOCUMENT_VIEWER_URL + getClientDocumentHelper().retrieveDocument(docId).getDocumentReferenceNumber();
				}
				resp.sendRedirect(claimNumberUrl);
			} catch (IOException e) {
				try {
					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					resp.getWriter().print("Error launching Document Viewer using URL: " + claimNumberUrl);
					resp.getWriter().close();
				} catch (IOException ioe) {
					logger.error("{} caught: {}", e.getClass().getName(), e.getMessage());
				}
			}

		}
	}

	/**
	 * Helper method to retrieve the claim number for a given document ID.
	 * 
	 * @param docId
	 *            document ID
	 * @return claimNumber
	 * 
	 */
	private String retrieveClaimNumber(Long docId) {

		DocumentDTO document = getClientDocumentHelper().retrieveDocument(docId);

		ClaimInformationRequestDTO request = new ClaimInformationRequestDTO();
		request.setClaimId(document.getAgreementIdDerived());

		MinimalInternalClaimDTO claim = getClaimsService().retrieveMinimalInternalClaim(request);

		return claim.getClaimNumber();
	}

	/**
	 * @return the clientDocumentHelper
	 */
	public ClientDocumentHelper getClientDocumentHelper() {
		return clientDocumentHelper;
	}

	/**
	 * @param helper
	 *            the clientDocumentHelper to set
	 */
	@Inject(PojoRef = "client.document.pojo.clientDocumentHelper")
	public void setClientDocumentHelper(ClientDocumentHelper helper) {
		this.clientDocumentHelper = helper;
	}

	/**
	 * Getter for CWSClaimService instance.
	 * 
	 * @return the CWSClaimService implementation
	 */
	private CWSClaimService getClaimsService() {
		if (claimService == null) {
			claimService = MuleServiceFactory.getService(CWSClaimService.class);
		}
		return claimService;
	}

}
