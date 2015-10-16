package com.client.iip.integration.core.webservice;

import javax.jws.WebService;

@WebService
public interface SOAPReceiver {
	
	String XML(String inputDocument) throws Exception;

}
