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

/**
 * 
 * Preview a Document.
 * 
 */
public class DocumentRedirectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(DocumentRedirectServlet.class);

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
		
		logger.info("req.getQueryString(): " + req.getQueryString());

		String mediaFileUrl = StringUtils.substringAfter(req.getQueryString(), "=");

		try {
			
			if (StringUtils.isEmpty(mediaFileUrl)) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().print("Error loading document using URL: " + mediaFileUrl);
				resp.getWriter().close();
			}else if (mediaFileUrl.startsWith("file:")) {
				logger.info("Streaming from Media File URL: {}", mediaFileUrl);
				
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
					logger.error("Error loading document {}  exception - {}"
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
				logger.info("Redirecting to http URL: {}", mediaFileUrl);
				resp.sendRedirect(mediaFileUrl);
			}			
			
		} catch (IOException e) {
			logger.error("IOException occurred redirecting to Media File URL {}: {}", mediaFileUrl, e.getMessage());

		}

	}

}
