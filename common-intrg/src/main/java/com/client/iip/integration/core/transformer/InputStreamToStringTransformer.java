package com.client.iip.integration.core.transformer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractDiscoverableTransformer;
import org.mule.transformer.types.DataTypeFactory;

/**
 * Converts an {@link javax.servlet.http.HttpServletRequest} into an array of
 * bytes by extracting the payload of the request.
 */
public class InputStreamToStringTransformer extends AbstractDiscoverableTransformer {
	public InputStreamToStringTransformer() {
		registerSourceType(DataTypeFactory.create(InputStream.class));
		setReturnDataType(DataTypeFactory.STRING);
	}

	@Override
	protected Object doTransform(Object src, String outputEncoding) throws TransformerException {
		String payload = StringUtils.EMPTY;
		try {
			payload = IOUtils.toString((InputStream) src);
		} catch (IOException e) {
			throw new TransformerException(this, e);
		}
		return payload;
	}
}
