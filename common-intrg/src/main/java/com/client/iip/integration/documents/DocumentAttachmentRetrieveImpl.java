package com.client.iip.integration.documents;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.document.jcr.MimeTypeMapper;

/**
 * Class that implements the logic for saving attachments.
 * 
 */
public class DocumentAttachmentRetrieveImpl implements Callable {

	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	private static final String DISPOSITION_TYPE_INLINE = "inline";
	private static final String DISPOSITION_TYPES_SEPARATOR = "; ";
	private static final String DISPOSITION_TYPE_FILE_NAME = "filename";
	private static final String URL_PATH_SEPARATOR = "/";
	private static final String FILE_NAME_VALUE_SEPARATOR = "=";
	private static final String ITEM_PATH_PROPERTY_NAME = "itemPath";
	protected static final String MIME_TYPE_FILE_NAME = "mime-type.txt";

	private Logger logger = LoggerFactory.getLogger(DocumentAttachmentRetrieveImpl.class);

	private ClientDocumentHelper clientDocumentHelper;

	/**
	 * Mule entry point that retrieves the attachments for the document.
	 * 
	 * @param eventContext
	 *            the event context
	 * @return Object the response
	 * @throws Exception
	 *             thrown if there is an exception
	 */
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {

		MuleMessage muleMessage = eventContext.getMessage();
		logger.debug("The mule event is : {} ", eventContext.getMessage());

		String mediaFile = muleMessage.getProperty(ITEM_PATH_PROPERTY_NAME, PropertyScope.INBOUND);
		logger.debug("Media File: {} ", mediaFile);

		String errorMessage = "Electronic Media Document could not be loaded using file [" + mediaFile + "]";

		URL mediaFileUrl;
		if (StringUtils.isEmpty(mediaFile)) {
			throw new IIPCoreSystemException(errorMessage);
		}

		InputStream source = null;
		try {
			mediaFileUrl = new URL(mediaFile);
			source = mediaFileUrl.openStream();
			String mimeType = MimeTypeMapper.getMimeType(parseFileName(mediaFile));
			byte[] attachmentPayload = IOUtils.toByteArray(source);

			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(CONTENT_TYPE_HEADER, mimeType);
			properties.put(CONTENT_DISPOSITION, createDispositionHeader(mediaFile));

			muleMessage = new DefaultMuleMessage(attachmentPayload, properties, eventContext.getMuleContext());

		} catch (Exception e) {
			logger.error("Electronic Media Document could not be loaded using file [  {}  ]", mediaFile);
			logger.error("{} caught: ", e.getClass().getName(), e);
			throw new IOException(errorMessage);

		} finally {
			IOUtils.closeQuietly(source);

		}

		return muleMessage;
	}

	/**
	 * This method is used to create Content Disposition header.
	 * 
	 * @param absolutePath
	 *            AttachmentName to extract fileName
	 * @return Content Disposition header
	 */
	private String createDispositionHeader(String absolutePath) {

		String fileName = parseFileName(absolutePath);

		// Currently by default opening as InLine in browser, for known types
		// browser will
		// show the save dialog, so fileName parameter used to show the correct
		// fileName.
		StringBuilder sb = new StringBuilder(DISPOSITION_TYPE_INLINE);
		sb.append(DISPOSITION_TYPES_SEPARATOR).append(DISPOSITION_TYPE_FILE_NAME).append(FILE_NAME_VALUE_SEPARATOR)
				.append(fileName);
		return sb.toString();
	}

	/**
	 * Helper method to parse file name from URL file path.
	 * 
	 * @param absolutePath
	 * 
	 * @return file name
	 */
	private String parseFileName(String absolutePath) {

		return absolutePath.substring(absolutePath.lastIndexOf(URL_PATH_SEPARATOR) + 1);

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

}
