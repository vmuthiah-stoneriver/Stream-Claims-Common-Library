package com.client.iip.integration;


import java.util.HashMap;

import com.client.iip.integration.core.PayloadRequestProcessor;

public class WorkerThread implements Runnable {
	
	private String interfaceName;
	
	private String payload;
	
	private String url;
	
	private HashMap hm;

	public WorkerThread(String _interfaceName, String _payload, String _url, HashMap _hm) {
		this.interfaceName = _interfaceName;
		this.payload = _payload;
		this.url = _url;
		this.hm = _hm;
	}

	@Override
	public void run() {
		System.out.println("Request Dispatched by the worker thread");
		try{
		PayloadRequestProcessor reqClient = PayloadRequestProcessor.getInstance();
		reqClient.callService(interfaceName, payload, url, hm);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
