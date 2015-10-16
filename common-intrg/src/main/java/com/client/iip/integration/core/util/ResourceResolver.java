package com.client.iip.integration.core.util;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class ResourceResolver  implements LSResourceResolver {  
	
	private Logger logger = LoggerFactory.getLogger(ResourceResolver.class);
	
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
		String schemaPath = systemId.contains("../")?systemId.substring(systemId.lastIndexOf("../")+3):systemId;
		logger.info("schemaPath: " + schemaPath);
		// note: in this sample, the XSD's are expected to be in the root of the classpath     
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(schemaPath);
		
		return new Input(publicId, systemId, resourceAsStream);
		
		}   

}
